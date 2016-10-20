package com.crimsonhexagon.mockingbird.api.annotations;

import com.crimsonhexagon.mockingbird.api.generators.StringFromRegexGenerator;
import com.pholser.junit.quickcheck.generator.GeneratorConfiguration;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link IncludeStrings} declares {@link String strings} to include when generating strings.
 * <br><br>
 * This is most commonly configured within a {@link StringFromRegexGenerator}:
 *
 * <pre>
 *     &#64;Property(trials = 500)
 *     public void testSomethingWithRandomData(
 *         &#64;From(StringFromRegexGenerator.class)
 *         &#64;RegexPattern("[&#92;u0000-&#92;uFFFF]+")
 *         &#64;IncludeStrings({"&#92;u0091", "&#92;u0092"})
 *         String string
 *     )
 * </pre>
 */
@Target({ PARAMETER, FIELD, ANNOTATION_TYPE, TYPE_USE })
@Retention(RUNTIME)
@GeneratorConfiguration
public @interface IncludeStrings {

    /**
     * The {@link String strings} to include in the generated data.
     *
     * @return {@code String[]} strings to include in the generated data
     */
    String[] value();
}
