package com.crimsonhexagon.mockingbird.jsonschema;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;

/**
 * {@link JsonNodeGenerator} generates random {@link JsonNode} objects based on the configured
 * {@link String JSON Schema path}.
 */
public class JsonNodeGenerator extends Generator<JsonNode> {

    private Schema schema;
    private String jsonSchemaPath;
    private JsonGenerator generator;

    /**
     * Constructor required for junit-quickcheck to initialze with {@link com.pholser.junit.quickcheck.From}
     * annotation.
     */
    public JsonNodeGenerator() {
        super(JsonNode.class);
    }

    /**
     * Creates a {@link JsonNodeGenerator} with a {@link String JSON Schema path}.
     *
     * @param jsonSchemaPath {@link String} the JSON Schema path
     * @throws JSONException thrown when the JSON Schema fails to parse
     */
    public JsonNodeGenerator(String jsonSchemaPath) throws JSONException {
        super(JsonNode.class);
        this.jsonSchemaPath = jsonSchemaPath;
        setupSchema();
    }

    /**
     * Configures this {@link JsonNodeGenerator} with a {@link JsonSchema}.
     *
     * @param jsonSchema {@link JsonSchema} the JSON Schema annotation
     * @throws JSONException thrown when the JSON Schema fails to parse
     */
    public void configure(JsonSchema jsonSchema) throws JSONException {
        jsonSchemaPath = jsonSchema.value();
        setupSchema();
    }

    /**
     * Generates a random {@link JsonNode} object.
     * <br><br>
     * Randomization is controlled via injection of {@link SourceOfRandomness}.
     *
     * @param random {@link SourceOfRandomness} controls randomization
     * @param status {@link GenerationStatus} parameters for generation tuning
     * @return {@link JsonNode} the random {@link JsonNode} object
     */
    @Override
    public JsonNode generate(SourceOfRandomness random, GenerationStatus status) {
        JSONObject jsonObject = generator.generateObject((ObjectSchema) schema, random.seed());
        try {
            return new ObjectMapper(new JsonFactory()).readTree(jsonObject.toString());
        } catch (IOException e) {
            String msg = "Invalid JSON for schema \"%s\". Message: %s";
            throw new RuntimeException(format(msg, jsonSchemaPath, e.getMessage()), e);
        }
    }

    private void setupSchema() throws JSONException {
        InputStream jsonSchemaInput = JsonNodeGenerator.class.getResourceAsStream(jsonSchemaPath);

        generator = new JsonGenerator();
        schema = generator.loadSchema(jsonSchemaInput);
    }
}
