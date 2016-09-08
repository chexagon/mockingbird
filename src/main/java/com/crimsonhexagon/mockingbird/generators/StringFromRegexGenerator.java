package com.crimsonhexagon.mockingbird.generators;

import com.crimsonhexagon.mockingbird.jsonschema.Xeger;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import org.json.JSONException;

public class StringFromRegexGenerator extends Generator<String> {

    private Xeger strGenerator = new Xeger("[\u0000-\uffff]{0,1000}");
    private String[] substrings = new String[] {""};

    public StringFromRegexGenerator() {
        super(String.class);
    }

    public void configure(RegexPattern regexPattern) {
        strGenerator = new Xeger(regexPattern.value());
    }

    public void configure(IncludeStrings canInclude) throws JSONException {
        substrings = canInclude.value();
    }

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        String str = strGenerator.generate();
        StringBuilder sb = new StringBuilder(str);
        sb.insert(0, randomSubstring(random));
        sb.insert(sb.length(), randomSubstring(random));
        sb.insert(sb.length() / 2, randomSubstring(random));
        return sb.toString();
    }

    private String randomSubstring(SourceOfRandomness random) {
        return substrings[random.nextInt(0, substrings.length-1)];
    }
}
