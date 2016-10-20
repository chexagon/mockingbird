package com.crimsonhexagon.mockingbird.api.annotations;

import com.crimsonhexagon.mockingbird.api.generators.StringFromRegexGenerator;
import com.pholser.junit.quickcheck.generator.GeneratorConfiguration;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link RegexPattern} declares a regular expression {@link String} that can be used when generating random
 * {@link String} data.
 * <br><br>
 * This is most commonly configured within a {@link StringFromRegexGenerator}:
 *
 * <pre>
 *     &#64;Property(trials = 500)
 *     public void testSomethingWithRandomData(
 *         &#64;From(StringFromRegexGenerator.class)
 *         &#64;RegexPattern("[&#92;u0000-&#92;uFFFF]+")
 *         String string
 *     )
 * </pre>
 */
@Target({ PARAMETER, FIELD, ANNOTATION_TYPE, TYPE_USE })
@Retention(RUNTIME)
@GeneratorConfiguration
public @interface RegexPattern {

    /**
     * The regular expression {@link String}.
     *
     * @return {@link String} regular expression
     */
    String value();
}
