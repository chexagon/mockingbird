package com.crimsonhexagon.mockingbird;

import com.crimsonhexagon.mockingbird.generators.JsonStringGenerator;
import com.crimsonhexagon.mockingbird.jsonschema.SchemaUtil;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.everit.json.schema.Schema;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.crimsonhexagon.mockingbird.Guards.requireAllNonNull;
import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.lang.String.format;

/**
 * {@link Mockingbird} provides a {@link Stream stream} of JSON-formatted
 * {@link String strings} randomly created from {@link Schema JSON Schema}.
 * <br/><br/>
 * The caller is free to consume as much or as little as they need.
 */
public class Mockingbird {

	public Stream<String> generate(Schema... jsonSchemas) {
		requireAllNonNull(jsonSchemas);

		final JsonStringGenerator generator = new JsonStringGenerator(jsonSchemas);
		return Stream.generate(() -> generator.generate(new SourceOfRandomness(new Random()), null));
	}

	public Stream<String> generate(InputStream... jsonSchemas) {
		requireAllNonNull(jsonSchemas);

		return generate(stream(jsonSchemas)
				.map(SchemaUtil::loadSchemaFromInputStream)
				.toArray(Schema[]::new));
	}

	public Stream<String> generate(File... jsonSchemas) {
		requireAllNonNull(jsonSchemas);

		List<File> unreadableFiles = stream(jsonSchemas)
				.filter(((Predicate<? super File>) File::canRead).negate())
				.collect(toList());

		if (!unreadableFiles.isEmpty()) {
			throw new IllegalStateException(
					format("Unable to read JSON Schema files: \n%s",
					join("\n", unreadableFiles.stream().map(File::getAbsolutePath).collect(toList()))));
		}

		return generate(stream(jsonSchemas).map(file -> {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				// should not happen, all files checked that they can be read
				throw new IllegalStateException(
						format("Unable to read JSON Schema file: \n%s", file.getAbsolutePath()));
			}
		}).toArray(InputStream[]::new));
	}
}
