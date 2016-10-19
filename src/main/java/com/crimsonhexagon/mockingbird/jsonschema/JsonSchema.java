package com.crimsonhexagon.mockingbird.jsonschema;

import com.pholser.junit.quickcheck.generator.GeneratorConfiguration;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link JsonSchema} defines an annotation to declare the JSON schema path to use.
 *
 * <pre>
 *     &#64;Property(trials = 500)
 *     public void testSomethingWithRandomData(
 *         &#64;From(JsonNodeGenerator.class)
 *         &#64;JsonSchema("/the-json-schema.json")
 *         JsonNode randomObject
 *     )
 * </pre>
 */
@Target({ PARAMETER, FIELD, ANNOTATION_TYPE, TYPE_USE })
@Retention(RUNTIME)
@GeneratorConfiguration
public @interface JsonSchema {

    /**
     * The path to a JSON Schema on the classpath.
     *
     * @return {@link String} path to a JSON Schema on the classpath
     */
    String value();
}
