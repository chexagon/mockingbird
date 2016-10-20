package com.crimsonhexagon.mockingbird.api.generators;

import com.pholser.junit.quickcheck.generator.Gen;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;

/**
 * {@link JsonStringGenerator} generates a random JSON {@link String} based
 * on some {@link Schema}.
 */
public class JsonStringGenerator implements Gen<String> {

	private final Schema[] schemas;
	private final JsonGenerator generator;

	public JsonStringGenerator(Schema... schemas) {
		this.schemas = schemas;
		this.generator = new JsonGenerator();
	}

	@Override
	public String generate(SourceOfRandomness rnd, GenerationStatus gen) {
		return generator.generateObject((ObjectSchema) chooseSchema(rnd), rnd.seed()).toString();
	}

	private Schema chooseSchema(SourceOfRandomness rnd) {
		return schemas[rnd.nextInt(0, schemas.length - 1)];
	}
}
