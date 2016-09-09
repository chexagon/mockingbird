package com.crimsonhexagon.mockingbird;

import com.crimsonhexagon.chug2.twitter.stream.TwitterHosebirdClient;
import com.crimsonhexagon.mockingbird.generators.RegexPattern;
import com.crimsonhexagon.mockingbird.generators.StringFromRegexGenerator;
import com.crimsonhexagon.mockingbird.jsonschema.JsonGenerator;
import com.pholser.junit.quickcheck.generator.Gen;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import com.twitter.hbc.core.Client;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.crimsonhexagon.mockingbird.jsonschema.SchemaUtil.loadSchemaFromClasspathResource;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.join;

public class MockingbirdClient extends TwitterHosebirdClient {

	private final static Logger logger = LoggerFactory.getLogger(MockingbirdClient.class);
	private static final String NAME = "Mockingbird (fake tweet generator)";

	private final JsonStringGenerator generator;
	private Thread generatorThread;
	private Thread watchdogThread;

	private volatile boolean runGenerator;
	private volatile boolean runWatcher;
	private final AtomicLong count;

	public MockingbirdClient() {
		generator = new JsonStringGenerator(
				loadSchemaFromClasspathResource("/com/crimsonhexagon/mockingbird/reply.json"),
				loadSchemaFromClasspathResource("/com/crimsonhexagon/mockingbird/retweet.json"),
				loadSchemaFromClasspathResource("/com/crimsonhexagon/mockingbird/tweet.json")
		);
		count = new AtomicLong(0);
		logger.info("Created MockingbirdClient");
	}

	@Override
	protected String getName() {
		return NAME;
	}

	@Override
	public void start(BlockingQueue<String> messageQueue) {
		logger.info("Starting MockingbirdClient");

		runGenerator = true;
		runWatcher = true;
		this.messageQueue = messageQueue;

		generatorThread = new Thread(new Generator(), "generator");
		generatorThread.start();
		watchdogThread = new Thread(new Watchdog(), "watchdog");
		watchdogThread.start();
	}

	@Override
	public void stop() throws InterruptedException {
		logger.info("Stopping MockingbirdClient, join generator thread to me.");

		runGenerator = false;
		runWatcher = false;
		generatorThread.join();
		watchdogThread.join();
	}

	@Override
	protected Client buildClient() {
		return null;
	}

	@Override
	public void logStats() {
		// no-op
	}

	private class Generator implements Runnable {

		@Override
		public void run() {
			logger.info("Running Generator thread: " + currentThread().getName());

			while (runGenerator) {
				// new seed on every generation
				String msg = generator.generate(new SourceOfRandomness(new Random()), null);
				try {
					messageQueue.offer(msg, 10L, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					logger.error("Could not offer to queue within 10 seconds.");
					logger.error("Disabling generator thread from myself.");
					runGenerator = false;
					continue;
				}

				count.incrementAndGet();

				if (!runGenerator) {
					logger.info(Generator.class.getSimpleName() + " is stopping.");
					return;
				}
			}

			logger.info("Ending Generator thread: " + currentThread().getName());
		}
	}

	private class Watchdog implements Runnable {
		private long lastCount = 0;
		private int numFailures = 0;

		@Override
		public void run() {
			logger.info("Running Watchdog thread: " + currentThread().getName());

			while (runWatcher) {
				try {
					Thread.sleep(5_000); // sleep for 5 seconds
				} catch (InterruptedException ignored) {}

				try {
					long val = count.get();
					if (val == lastCount) {
						numFailures++;
						logger.warn("Message count of {} has not changed in {} seconds", count, (numFailures * 5));
					} else {
						lastCount = val;
						numFailures = 0;
					}

					if (numFailures == 6) {
						// 30 seconds with no messages being processed
						logger.error("30 seconds with message count stuck at {} - restarting client", lastCount);

						logger.info("Ending Generator thread from watcher.");
						runGenerator = false;
						try {
							generatorThread.join(10000);
						} catch (InterruptedException e) {
							logger.error("Someone interrupted me (" + currentThread().getName() + ") while I was waiting for generator.");
						}

						logger.info("Restarting Generator thread.");
						runGenerator = true;
						generatorThread = new Thread(new Generator());
						generatorThread.start();
					}

				} catch (Exception e) {
					logger.error("Watchdog encountered an error", e);
				}
			}
			logger.info("Ending Watchdog thread: " + currentThread().getName());
		}
	}

	private class JsonStringGenerator implements Gen<String> {

		private final Schema[] schemas;
		private final JsonGenerator generator;
		private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss +0000 yyyy");
		private final StringFromRegexGenerator strGenerator;

		// see https://github.com/twitter/twitter-text/blob/master/rb/lib/twitter-text/regex.rb#L59
		private final String[] INVALID_TWITTER_TEXT_CHARS = {
			"\u0000",                                        // NUL
			"\uFFFE", "\uFEFF",                              // BOM
			"\uFFFF",                                        // Special
			"\u202A", "\u202B", "\u202C", "\u202D", "\u202E" // Directional change
		};

		JsonStringGenerator(Schema... schemas) {
			this.schemas = schemas;
			this.generator = new JsonGenerator();
			this.strGenerator = new StringFromRegexGenerator();
			this.strGenerator.configure(new RegexPattern() {

				@Override
				public Class<? extends Annotation> annotationType() {
					return RegexPattern.class;
				}

				@Override
				public String value() {
					return format("[^%s]{140}", join(INVALID_TWITTER_TEXT_CHARS));
				}
			});
		}

		@Override
		public String generate(SourceOfRandomness rnd, GenerationStatus gen) {
			JSONObject jsonObj = generator.generateObject((ObjectSchema) chooseSchema(rnd), rnd.seed());

			// generate UTC times for created_at dates
			ZonedDateTime utcNowMinu5Seconds  = ZonedDateTime.ofInstant(now().minus(5, SECONDS), ZoneId.of("UTC"));
			ZonedDateTime utcNowMinu10Minutes = ZonedDateTime.ofInstant(now().minus(10, MINUTES), ZoneId.of("UTC"));

			// set tweet date
			jsonObj.put("created_at", utcNowMinu5Seconds.format(dtf));

			JSONObject extendedTweet;
			switch(jsonObj.getString("twitter_schema_type")) {
				case "tweet":
				case "reply":
					extendedTweet = jsonObj.getJSONObject("extended_tweet");
					setFullText(extendedTweet, rnd, gen);
					break;
				case "retweet":
					JSONObject retweetedStatus = jsonObj.getJSONObject("retweeted_status");
					extendedTweet = retweetedStatus.getJSONObject("extended_tweet");
					setFullText(extendedTweet, rnd, gen);

					retweetedStatus.put("created_at", utcNowMinu10Minutes.format(dtf));
					retweetedStatus.put("text", "TruncatedTextValue - in retweeted_status - retweet type…");
					jsonObj.put("text", "TruncatedTextValue - at root - retweet type…");
					break;
			}

			return jsonObj.toString();
		}

		private void setFullText(JSONObject extendedTweet, SourceOfRandomness rnd, GenerationStatus gen) {
			// ensure full text is >= 140
			String sentence = strGenerator.generate(rnd, gen);

			// add prefix marker then truncate to 140
			StringBuilder tweetText = new StringBuilder();
			tweetText.append(("FullTextValue: " + sentence).substring(0, sentence.length() < 140 ? sentence.length() : 140));

			// set last character as suffix marker (pipe)
			tweetText.setCharAt(tweetText.length()-1, '|');

			String fullText = extendedTweet.getString("full_text");
			fullText = fullText.replace("{TEXT}", tweetText.toString());

			extendedTweet.put("full_text", fullText);
			extendedTweet.put("display_text_range", new JSONArray()
					.put(fullText.indexOf("FullTextValue:"))
					.put(fullText.lastIndexOf(" http:")));
		}

		private Schema chooseSchema(SourceOfRandomness rnd) {
			return schemas[rnd.nextInt(0, schemas.length - 1)];
		}
	}
}
