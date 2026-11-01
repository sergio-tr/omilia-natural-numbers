package com.omilia.numbers.interpretation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.omilia.numbers.domain.DigitToken;
import com.omilia.numbers.domain.RecognizedSequence;
import com.omilia.numbers.parsing.NumberInputParser;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class NumberInterpreterTest {

    private final NumberInputParser parser = new NumberInputParser();
    private final NumberInterpreter interpreter = new NumberInterpreter();

    @ParameterizedTest(name = "{0}")
    @MethodSource("documentedReadings")
    void interpretsInStableDepthFirstOrder(String input, List<String> expected) {
        assertEquals(expected, interpret(input));
    }

    static Stream<Arguments> documentedReadings() {
        return Stream.of(
            Arguments.of("20 5", List.of("205", "25")),
            Arguments.of("75", List.of("75", "705")),
            Arguments.of("30 6", List.of("306", "36")),
            Arguments.of("700 24", List.of("70024", "700204", "724")),
            Arguments.of("700 20 5", List.of("700205", "70025", "7205", "725")),
            Arguments.of("2 10 6 9 30 6 6 4", List.of("2106930664", "210693664")),
            Arguments.of(
                "2 10 69 30 6 6 4",
                List.of("2106930664", "210693664", "21060930664", "2106093664")
            ),
            Arguments.of(
                "0 0 30 69 700 24 1 3 50 2",
                List.of(
                    "0030697002413502",
                    "003069700241352",
                    "00306970020413502",
                    "0030697002041352",
                    "00306972413502",
                    "0030697241352",
                    "00306097002413502",
                    "0030609700241352",
                    "003060970020413502",
                    "00306097002041352",
                    "003060972413502",
                    "00306097241352"
                )
            ),
            Arguments.of("10 6", List.of("106")),
            Arguments.of("09", List.of("09")),
            Arguments.of("11", List.of("11")),
            Arguments.of("00", List.of("00")),
            Arguments.of("0 5", List.of("05")),
            Arguments.of("30 06", List.of("3006")),
            Arguments.of("700 024", List.of("700024")),
            Arguments.of("700 6", List.of("7006", "706")),
            Arguments.of("700 10", List.of("70010", "710")),
            Arguments.of("100 100", List.of("100100")),
            Arguments.of("100 30 6", List.of("100306", "10036", "1306", "136")),
            Arguments.of("900 99", List.of("90099", "900909", "999")),
            Arguments.of("21 21", List.of("2121", "21201", "20121", "201201")),
            Arguments.of("69 30 6", List.of("69306", "6936", "609306", "60936")),
            Arguments.of("2 69", List.of("269", "2609")),
            Arguments.of("75 5", List.of("755", "7055")),
            Arguments.of(
                "700 20 5 69 30 6",
                List.of(
                    "70020569306",
                    "7002056936",
                    "700205609306",
                    "70020560936",
                    "7002569306",
                    "700256936",
                    "70025609306",
                    "7002560936",
                    "720569306",
                    "72056936",
                    "7205609306",
                    "720560936",
                    "72569306",
                    "7256936",
                    "725609306",
                    "72560936"
                )
            )
        );
    }

    @Test
    void literalConcatenationIsFirstAndCallsAgree() {
        List<String> first = interpret("2 10 69 30 6 6 4");
        List<String> second = interpret("2 10 69 30 6 6 4");

        assertEquals(first, second);
        assertEquals("2106930664", first.get(0));
        assertEquals(first.size(), new HashSet<>(first).size());
    }

    @Test
    void independentExpansionsAreAllReturned() {
        String input = "21 ".repeat(10).strip();
        List<String> results = interpret(input);

        assertEquals(1024, results.size());
        assertEquals("21".repeat(10), results.get(0));
        assertTrue(results.contains("201".repeat(10)));
    }

    @Test
    void shortReadingsAreNotRemoved() {
        assertEquals(List.of("205", "25"), interpret("20 5"));
    }

    @Test
    void randomMixturesStayUniqueStableAndStartWithTheLiteralReading() {
        String[] pool = {
            "0", "2", "6", "9", "10", "11", "20", "21", "30", "69", "75", "100", "700", "900", "09", "00"
        };
        Random random = new Random(20261030L);
        for (int trial = 0; trial < 40; trial++) {
            int length = 1 + random.nextInt(8);
            String[] parts = new String[length];
            for (int index = 0; index < length; index++) {
                parts[index] = pool[random.nextInt(pool.length)];
            }
            String input = String.join(" ", parts);
            List<String> first = interpret(input);
            List<String> second = interpret(input);

            assertEquals(first, second, input);
            assertEquals(first.size(), new HashSet<>(first).size(), input);
            assertEquals(String.join("", parts), first.get(0), input);
            assertTrue(first.stream().allMatch(value -> value.chars().allMatch(ch -> ch >= '0' && ch <= '9')), input);
        }
    }

    @Test
    void searchFollowsTheRulesItIsGiven() {
        NumberInterpreter literalsOnly = new NumberInterpreter(List.of(InterpretationRule.LITERAL));
        assertEquals(List.of("6930"), literalsOnly.interpret(parser.parse("69 30")));

        NumberInterpreter expansionOnly = new NumberInterpreter(List.of(InterpretationRule.COMPOUND_TENS));
        assertEquals(List.of("609"), expansionOnly.interpret(sequence("69")));
        assertThrows(IllegalStateException.class, () -> expansionOnly.interpret(sequence("10")));
    }

    @Test
    void rejectsAnEmptyRuleListAndANullSequence() {
        assertThrows(IllegalArgumentException.class, () -> new NumberInterpreter(List.of()));
        assertThrows(NullPointerException.class, () -> interpreter.interpret(null));
        assertThrows(UnsupportedOperationException.class, () -> interpret("75").add("705"));
    }

    @Test
    void rejectsARuleThatConsumesPastTheEndOfTheSequence() {
        TokenRule overConsuming = (tokens, index) -> List.of(new Alternative("9", tokens.size() + 1));
        NumberInterpreter broken = new NumberInterpreter(List.of(overConsuming));

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> broken.interpret(sequence("5"))
        );
        assertEquals("A rule consumed past the end of the sequence at index 0.", exception.getMessage());
    }

    private List<String> interpret(String input) {
        return interpreter.interpret(parser.parse(input));
    }

    private static RecognizedSequence sequence(String text) {
        return new RecognizedSequence(List.of(new DigitToken(text)));
    }
}
