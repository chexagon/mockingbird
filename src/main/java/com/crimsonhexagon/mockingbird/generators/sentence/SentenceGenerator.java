package com.crimsonhexagon.mockingbird.generators.sentence;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;

public class SentenceGenerator extends Generator<String> {

    private static final MarkovChain markovChain;
    static {
        markovChain = new MarkovChain(2);

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(SentenceGenerator.class.getResourceAsStream("/alice-in-wonderland.txt")))) {
            markovChain.train(buffer.lines().collect(Collectors.joining("\n")));
        } catch (IOException e) { e.printStackTrace(); }

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(SentenceGenerator.class.getResourceAsStream("/sherlock-holmes.txt")))) {
            markovChain.train(buffer.lines().collect(Collectors.joining("\n")));
        } catch (IOException e) { e.printStackTrace(); }
    }

    public SentenceGenerator() {
        super(String.class);
    }

    @Override
    public String generate(SourceOfRandomness random, GenerationStatus status) {
        String sentence = null;
        while (sentence == null)
            try {
                sentence = markovChain.generateParagraph(2);
            } catch (NullPointerException | NoSuchElementException e) {/* keep going */}
        return sentence;
    }

    public static void main(String[] args) {
        SentenceGenerator gen = new SentenceGenerator();

        while (true) {
            String sentence = gen.generate(new SourceOfRandomness(new Random(1L)), null);
            if (sentence.length() == 140)
            System.out.println(String.format("[%d] %s", sentence.length(), sentence));
        }
    }
}
