package com.omilia.numbers.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.omilia.numbers.interpretation.NumberInterpreter;
import com.omilia.numbers.parsing.NumberInputParser;
import com.omilia.numbers.phone.GreekPhoneNumberValidator;
import com.omilia.numbers.phone.PhoneValidity;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Locks the examples printed in the Omilia assignment PDF.
 * These are not inferred edge cases: each expected output and phone label comes from the statement.
 */
@DisplayName("Omilia assignment PDF examples")
class AssignmentExamplesTest {

    private final InterpretNumbers useCase = new InterpretNumbers(
        new NumberInputParser(),
        new NumberInterpreter(),
        new GreekPhoneNumberValidator()
    );

    @Nested
    @DisplayName("Base level: phone number validation")
    class BaseLevel {

        /**
         * The base table shows one concatenated reading and its phone label.
         * With the advanced engine that reading remains first (literal join) and keeps the stated label.
         */
        @ParameterizedTest(name = "[{index}] {0} → {1} {2}")
        @CsvSource({
            "30 2 5 58,                302558,          INVALID",
            "2 10 69 30 6 6 4,          2106930664,      VALID",
            "2 10 69 30 6 60 4,         21069306604,     INVALID",
            "0 0 30 69 74 0 9 22 52,    00306974092252,  VALID"
        })
        void matchesOfficialBaseTable(String input, String expectedDigits, PhoneValidity expectedValidity) {
            InterpretationReport report = report(input);

            NumberInterpretation first = report.interpretations().getFirst();
            assertEquals(expectedDigits, first.digits(), "literal concatenation from the PDF table");
            assertEquals(expectedValidity, first.phoneValidity(), "phone label from the PDF table");
            assertTrue(
                report.interpretations().contains(new NumberInterpretation(expectedDigits, expectedValidity)),
                "the official base reading must remain among all interpretations"
            );
        }

        @Test
        @DisplayName("Sample inputs from the task statement are accepted")
        void acceptsSampleInputSequencesFromTheTask() {
            assertEquals("302558", report("30 2 5 58").interpretations().getFirst().digits());
            assertEquals("21069306604", report("2 10 69 30 6 60 4").interpretations().getFirst().digits());
            assertEquals("697400237405", report("6 97 400 23 7 40 5").interpretations().getFirst().digits());
        }
    }

    @Nested
    @DisplayName("Advanced level: natural number ambiguities")
    class AdvancedLevel {

        @Test
        @DisplayName("PDF prose: 20 5 → 205 and 25")
        void twentyFiveAmbiguityFromTheStatement() {
            assertEquals(
                Set.of(
                    new NumberInterpretation("205", PhoneValidity.INVALID),
                    new NumberInterpretation("25", PhoneValidity.INVALID)
                ),
                Set.copyOf(report("20 5").interpretations())
            );
        }

        @Test
        @DisplayName("PDF prose: 75 → 75 and 705")
        void seventyFiveAmbiguityFromTheStatement() {
            assertEquals(
                Set.of(
                    new NumberInterpretation("75", PhoneValidity.INVALID),
                    new NumberInterpretation("705", PhoneValidity.INVALID)
                ),
                Set.copyOf(report("75").interpretations())
            );
        }

        @Test
        @DisplayName("Example 1 — 2 10 6 9 30 6 6 4")
        void example1MatchesOfficialInterpretationsAndLabels() {
            assertEquals(
                Set.of(
                    new NumberInterpretation("2106930664", PhoneValidity.VALID),
                    new NumberInterpretation("210693664", PhoneValidity.INVALID)
                ),
                Set.copyOf(report("2 10 6 9 30 6 6 4").interpretations())
            );
        }

        @Test
        @DisplayName("Example 2 — 2 10 69 30 6 6 4")
        void example2MatchesOfficialInterpretationsAndLabels() {
            assertEquals(
                Set.of(
                    new NumberInterpretation("2106930664", PhoneValidity.VALID),
                    new NumberInterpretation("210693664", PhoneValidity.INVALID),
                    new NumberInterpretation("2106093664", PhoneValidity.VALID),
                    new NumberInterpretation("21060930664", PhoneValidity.INVALID)
                ),
                Set.copyOf(report("2 10 69 30 6 6 4").interpretations())
            );
        }

        @Test
        @DisplayName("Example 3 — 0 0 30 69 700 24 1 3 50 2")
        void example3IncludesEveryOfficiallyListedReading() {
            InterpretationReport report = report("0 0 30 69 700 24 1 3 50 2");
            List<NumberInterpretation> interpretations = report.interpretations();

            assertEquals(
                new NumberInterpretation("0030697002413502", PhoneValidity.INVALID),
                interpretations.getFirst(),
                "interpretation 1 in the PDF"
            );
            assertTrue(
                interpretations.contains(new NumberInterpretation("003069700241352", PhoneValidity.INVALID)),
                "interpretation 2 in the PDF"
            );
            assertTrue(
                interpretations.contains(new NumberInterpretation("00306972413502", PhoneValidity.VALID)),
                "interpretation n in the PDF"
            );
            assertTrue(
                interpretations.contains(new NumberInterpretation("00306097241352", PhoneValidity.INVALID)),
                "interpretation m in the PDF"
            );

            assertTrue(
                interpretations.stream().anyMatch(item -> item.phoneValidity() == PhoneValidity.VALID),
                "the PDF shows at least one VALID reading"
            );
            assertTrue(
                interpretations.stream().anyMatch(item -> item.phoneValidity() == PhoneValidity.INVALID),
                "the PDF shows INVALID readings in the same report"
            );
        }

        @Test
        @DisplayName("Example 3 keeps the official VALID reading without discarding INVALID ones")
        void example3DoesNotDropInvalidCandidates() {
            Set<String> digits = report("0 0 30 69 700 24 1 3 50 2").interpretations().stream()
                .map(NumberInterpretation::digits)
                .collect(Collectors.toSet());

            assertTrue(digits.contains("0030697002413502"));
            assertTrue(digits.contains("003069700241352"));
            assertTrue(digits.contains("00306972413502"));
            assertTrue(digits.contains("00306097241352"));
            assertTrue(digits.size() >= 4, "the PDF ellipsis implies more than the four listed rows");
        }
    }

    private InterpretationReport report(String input) {
        return assertInstanceOf(InterpretationReport.class, useCase.interpret(input));
    }
}
