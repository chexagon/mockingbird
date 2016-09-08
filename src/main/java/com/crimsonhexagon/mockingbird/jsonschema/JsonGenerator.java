package com.crimsonhexagon.mockingbird.jsonschema;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.java.lang.BooleanGenerator;
import com.pholser.junit.quickcheck.generator.java.lang.Encoded;
import com.pholser.junit.quickcheck.generator.java.lang.strings.CodePoints;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.everit.json.schema.*;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Character.toChars;
import static java.util.Collections.shuffle;

/**
 * {@link JsonGenerator} generates random JSON data based on a JSON schema.
 * <br><br>
 * First generate a {@link Schema} by calling {@link #loadSchema(InputStream)}. Then generate an example object,
 * that corresponds to {@link Schema}, with {@link #generate(Schema, long)}.
 */
public class JsonGenerator {

    /**
     * Creates a {@link Schema} object from the JSON Schema read from the {@link InputStream stream}.
     *
     * @param jsonSchemaStream {@link InputStream} the JSON Schema stream
     * @return {@link Schema} the JSON Schema object
     * @throws JSONException thrown if the JSON Schema cannot be parsed
     */
    public Schema loadSchema(InputStream jsonSchemaStream) throws JSONException {
        JSONObject jsonSchema = new JSONObject(new JSONTokener(new InputStreamReader(jsonSchemaStream)));
        return SchemaLoader.load(jsonSchema);
    }

    /**
     * Generates a random value for {@link Schema}.
     * <br><br>
     * Randomization is seeded with the {@code seed} value.
     *
     * @param schema {@link Schema} the JSON Schema object
     * @param seed {@code long} the seed value for randomization
     * @return {@link Object} the random object
     */
    public Object generate(Schema schema, long seed) {
        if (schema.getClass().equals(CombinedSchema.class)) {
            return generateCombined((CombinedSchema) schema, seed);
        } else if (schema.getClass().equals(ObjectSchema.class)) {
            return generateObject((ObjectSchema) schema, seed);
        } else if (schema.getClass().equals(ArraySchema.class)) {
            return generateArray((ArraySchema) schema, seed);
        } else if (schema.getClass().equals(BooleanSchema.class)) {
            return generateBoolean(seed);
        } else if (schema.getClass().equals(NumberSchema.class)) {
            return generateNumber((NumberSchema) schema, seed);
        } else if (schema.getClass().equals(StringSchema.class)) {
            return generateString((StringSchema) schema, seed);
        } else if (schema.getClass().equals(EnumSchema.class)) {
            return generateEnum((EnumSchema) schema, seed);
        } else if (schema.getClass().equals(NullSchema.class)) {
            return null;
        }

        return null;
    }

    /**
     * Generates a JSON array from a schema.
     * <br><br>
     * Randomization is seeded with the {@code seed} value.
     *
     * @param schema {@link ArraySchema} a JSON Schema for a JSON Array type
     * @param seed {@code long} the seed value for randomization
     * @return {@link JSONArray} the random JSON array
     */
    public JSONArray generateArray(ArraySchema schema, long seed) {
        if (schema.getAllItemSchema() != null) {
            int min = schema.getMinItems() != null ? schema.getMinItems() : 0;
            int max = schema.getMaxItems() != null ? schema.getMaxItems() : 10;
            Schema allItemSchema = schema.getAllItemSchema();
            int elementCount = new Random(seed).nextInt(max) + min;

            Collection<Object> elements = new ArrayList<>();
            for (int i = 0; i < elementCount; i++)
                elements.add(generate(allItemSchema, seed));
            return new JSONArray(elements);
        } else if (schema.getItemSchemas() != null) {
            List<Schema> itemSchemas = schema.getItemSchemas();
            Collection<Object> elements = new ArrayList<>(itemSchemas.size());
            for (Schema itemSchema : itemSchemas)
                elements.add(generate(itemSchema, seed));
            return new JSONArray(elements);
        }

        return null;
    }

    /**
     * Generates a JSON object from a schema.
     * <br><br>
     * Randomization is seeded with the {@code seed} value.
     *
     * @param schema {@link ObjectSchema} a JSON Schema for a JSON object type
     * @param seed {@code long} the seed value for randomization
     * @return {@link JSONObject} the random JSON object
     */
    public JSONObject generateObject(ObjectSchema schema, long seed) {
        JSONObject obj = new JSONObject();
        Map<String, Schema> propertySchemas = schema.getPropertySchemas();
        propertySchemas.entrySet().forEach(entry -> {
            Schema propertySchema = entry.getValue();

            try {
                if (propertySchema.getClass().equals(CombinedSchema.class)) {
                    obj.put(entry.getKey(), generateCombined((CombinedSchema) propertySchema, seed));
                } else if (propertySchema.getClass().equals(ObjectSchema.class)) {
                    obj.put(entry.getKey(), generateObject((ObjectSchema) propertySchema, seed));
                } else if (propertySchema.getClass().equals(ArraySchema.class)) {
                    obj.put(entry.getKey(), generateArray((ArraySchema) propertySchema, seed));
                } else if (propertySchema.getClass().equals(BooleanSchema.class)) {
                    obj.put(entry.getKey(), generateBoolean(seed));
                } else if (propertySchema.getClass().equals(NumberSchema.class)) {
                    obj.put(entry.getKey(), generateNumber((NumberSchema) propertySchema, seed));
                } else if (propertySchema.getClass().equals(StringSchema.class)) {
                    obj.put(entry.getKey(), generateString((StringSchema) propertySchema, seed));
                } else if (propertySchema.getClass().equals(EnumSchema.class)) {
                    obj.put(entry.getKey(), generateEnum((EnumSchema) propertySchema, seed));
                } else if (propertySchema.getClass().equals(NullSchema.class)) {
                    obj.put(entry.getKey(), JSONObject.NULL);
                }
            } catch (JSONException ex) {
                // Null key; skip property
            }
        });

        return obj;
    }

    /**
     * Generates an object from a combined schema (i.e. oneOf, allOf, anyOf).
     * <br><br>
     * Randomization is seeded with the {@code seed} value.
     *
     * @param schema {@link CombinedSchema} a combined JSON Schema
     * @param seed {@code long} the seed value for randomization
     * @return {@link Object} the random object
     */
    public Object generateCombined(final CombinedSchema schema, final long seed) {
        List subSchemas = new ArrayList<>(schema.getSubschemas());
        shuffle(subSchemas);

        Optional<Schema> first = subSchemas.stream().findFirst();
        return first.map(s -> generate(s, seed)).orElse(null);
    }

    /**
     * Generates an object from a JSON Enum schema (i.e. <em>enum</em>).
     * <br><br>
     * Randomization is seeded with the {@code seed} value.
     *
     * @param schema {@link EnumSchema} a JSON Schema for an enum
     * @param seed {@code long} the seed value for randomization
     * @return {@link Object} the random enum object
     */
    public Object generateEnum(EnumSchema schema, long seed) {
        Set<Object> choices = schema.getPossibleValues();
        Object[] choiceArray = schema.getPossibleValues().toArray(new Object[choices.size()]);
        return choiceArray[new Random(seed).nextInt(choiceArray.length)];
    }

    /**
     * Generates a random {@link Boolean}.
     * <br><br>
     * Randomization is seeded with the {@code seed} value.
     *
     * @param seed {@code long} the seed value for randomization
     * @return {@link Boolean} a random boolean
     */
    public Boolean generateBoolean(long seed) {
        return new BooleanGenerator().generate(new SourceOfRandomness(new Random(seed)), null);
    }

    /**
     * Generates a random {@link Number} from a JSON Number schema.
     * <br><br>
     * Randomization is seeded with the {@code seed} value.
     *
     * @param schema {@link NumberSchema} the JSON Number schema
     * @param seed {@code long} the seed value for randomization
     * @return {@link Number} a random number
     */
    public Number generateNumber(NumberSchema schema, long seed) {
        return new NumberGenerator(schema).generate(new SourceOfRandomness(new Random(seed)), null);
    }

    /**
     * Generates a random {@link String} from a JSON String schema.
     * <br><br>
     * Randomization is seeded with the {@code seed} value.
     *
     * @param schema {@link StringSchema} the JSON String schema
     * @param seed {@code long} the seed value for randomization
     * @return {@link String} a random string
     */
    public String generateString(StringSchema schema, long seed) {
        return new StringGenerator(schema).generate(new SourceOfRandomness(new Random(seed)), null);
    }

    /**
     * {@link NumberGenerator} generates random {@link Number} values.
     */
    static class NumberGenerator extends Generator<Number> {

        private final NumberSchema schema;

        protected NumberGenerator(NumberSchema schema) {
            super(Number.class);
            this.schema = schema;
        }

        @Override
        public Number generate(SourceOfRandomness random, GenerationStatus status) {
            return minMaxNumber(random);
        }

        private Number minMaxNumber(SourceOfRandomness rnd) {
            Number minimum = schema.getMinimum();
            Number maximum = schema.getMaximum();

            if (schema.requiresInteger()) {
                return rnd.nextLong(
                        minimum == null ? Long.MIN_VALUE : (schema.isExclusiveMinimum() ? minimum.longValue() + 1 : minimum.longValue()),
                        maximum == null ? Long.MAX_VALUE : (schema.isExclusiveMaximum() ? maximum.longValue() - 1 : maximum.longValue()));
            } else {
                return rnd.nextFloat(
                        minimum == null ? Float.MIN_VALUE : (schema.isExclusiveMinimum() ? minimum.floatValue() + 1 : minimum.floatValue()),
                        maximum == null ? Float.MAX_VALUE : (schema.isExclusiveMaximum() ? maximum.floatValue() - 1 : maximum.floatValue()));
            }
        }
    }

    /**
     * {@link StringGenerator} generates random {@link String} values.
     */
    static class StringGenerator extends Encoded {

        // Cache results, per pattern, of building regular expression automaton.
        // Dramatically improves testing time for property tests with many trials.
        private static final Map<String, Xeger> xegerCache = new ConcurrentHashMap<>();
        private final StringSchema schema;
        private final Charset charset;
        private CodePoints charsetPoints;

        public StringGenerator(StringSchema schema) {
            this.schema = schema;
            charset = Charset.forName("UTF-8");
            charsetPoints = CodePoints.forCharset(charset);
        }

        public StringGenerator(StringSchema schema, Charset charset) {
            this.schema = schema;
            this.charset = charset;
            charsetPoints = CodePoints.forCharset(charset);
        }

        public String generate(SourceOfRandomness random, GenerationStatus status) {
            Integer min = schema.getMinLength() == null ? 0 : schema.getMinLength();
            Integer max = schema.getMaxLength() == null ? random.nextInt(0, 200) : schema.getMaxLength();

            if (schema.getPattern() != null) {
                final String regexPattern = schema.getPattern().pattern();
                Xeger x = xegerCache.computeIfAbsent(regexPattern, ps -> new Xeger(regexPattern, new Random(random.seed())));
                return x.generate();
            }

            int stringLength = random.nextInt(min, max);
            char[] chars = new char[stringLength];

            int offset = 0;
            while (chars.length != 0 && chars[chars.length - 1] == '\u0000') {
                char[] charValues = toChars(nextCodePoint(random));
                for (int i = 0; i < charValues.length && offset < chars.length; i++) {
                    chars[offset++] = charValues[i];
                }
            }
            return new String(chars);
        }

        @Override protected int nextCodePoint(SourceOfRandomness random) {
            return charsetPoints.at(random.nextInt(0, charsetPoints.size() - 1));
        }

        @Override protected boolean codePointInRange(int codePoint) {
            return charsetPoints.contains(codePoint);
        }
    }
}
