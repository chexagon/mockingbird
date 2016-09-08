package com.crimsonhexagon.mockingbird.jsonschema;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.everit.json.schema.ObjectSchema;
import org.json.JSONObject;

import java.io.IOException;

/**
 * {@link ObjectNodeGenerator} generates random {@link ObjectNode} objects based on the supplied {@link ObjectSchema}.
 */
public class ObjectNodeGenerator extends Generator<ObjectNode> {

    private final ObjectSchema jsonObjectSchema;

    /**
     * Creates {@link ObjectNodeGenerator} from the supplied {@link ObjectSchema}.
     * @param jsonObjectSchema {@link ObjectSchema} the object schema to generate random data from
     */
    public ObjectNodeGenerator(ObjectSchema jsonObjectSchema) {
        super(ObjectNode.class);
        this.jsonObjectSchema = jsonObjectSchema;
    }

    /**
     * Generates a random {@link ObjectNode} object from the configured {@link ObjectSchema}.
     * <br><br>
     * Randomization is controlled via injection of {@link SourceOfRandomness}.
     *
     * @param random {@link SourceOfRandomness} controls randomization
     * @param status {@link GenerationStatus} parameters for generation tuning
     * @return {@link ObjectNode} the random {@link ObjectNode} object
     */
    @Override
    public ObjectNode generate(SourceOfRandomness random, GenerationStatus status) {
        JsonGenerator gen = new JsonGenerator();

        JSONObject jsonObject = gen.generateObject(jsonObjectSchema, random.seed());
        try {
            return new ObjectMapper(new JsonFactory()).readValue(jsonObject.toString(), ObjectNode.class);
        } catch (IOException e) {
            // Error parsing JSON object using jackson. Should not happen since the schema was already read from JSON.
            // Rethrow to fail tests.
            throw new RuntimeException("Failed to parse test JSON object.", e);
        }
    }
}
