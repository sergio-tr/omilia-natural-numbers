package com.omilia.numbers.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.omilia.numbers.interpretation.NumberInterpreter;
import com.omilia.numbers.parsing.NumberInputParser;
import com.omilia.numbers.phone.GreekPhoneNumberValidator;
import com.omilia.numbers.phone.PhoneValidity;
import java.util.List;
import org.junit.jupiter.api.Test;

class InterpretNumbersTest {

    private final InterpretNumbers useCase = new InterpretNumbers(
        new NumberInputParser(),
        new NumberInterpreter(),
        new GreekPhoneNumberValidator()
    );

    @Test
    void keepsEveryReadingAndMarksThePhoneCheck() {
        assertEquals(
            new InterpretationReport(List.of(
                new NumberInterpretation("205", PhoneValidity.INVALID),
                new NumberInterpretation("25", PhoneValidity.INVALID)
            )),
            useCase.interpret("20 5")
        );
        assertEquals(
            new InterpretationReport(List.of(
                new NumberInterpretation("75", PhoneValidity.INVALID),
                new NumberInterpretation("705", PhoneValidity.INVALID)
            )),
            useCase.interpret("75")
        );
        assertEquals(
            new InterpretationReport(List.of(
                new NumberInterpretation("2106930664", PhoneValidity.VALID),
                new NumberInterpretation("210693664", PhoneValidity.INVALID)
            )),
            useCase.interpret("2 10 6 9 30 6 6 4")
        );
        assertEquals(
            new InterpretationReport(List.of(
                new NumberInterpretation("2106930664", PhoneValidity.VALID),
                new NumberInterpretation("210693664", PhoneValidity.INVALID),
                new NumberInterpretation("21060930664", PhoneValidity.INVALID),
                new NumberInterpretation("2106093664", PhoneValidity.VALID)
            )),
            useCase.interpret("2 10 69 30 6 6 4")
        );
    }

    @Test
    void thirdExampleReturnsValidAndInvalidNumbersTogether() {
        InterpretationReport report = assertInstanceOf(
            InterpretationReport.class,
            useCase.interpret("0 0 30 69 700 24 1 3 50 2")
        );

        assertEquals(12, report.interpretations().size());
        assertTrue(report.interpretations().contains(
            new NumberInterpretation("00306972413502", PhoneValidity.VALID)
        ));
        assertTrue(report.interpretations().contains(
            new NumberInterpretation("0030697002413502", PhoneValidity.INVALID)
        ));
        assertTrue(report.interpretations().contains(
            new NumberInterpretation("003069700241352", PhoneValidity.INVALID)
        ));
        assertTrue(report.interpretations().contains(
            new NumberInterpretation("00306097241352", PhoneValidity.INVALID)
        ));
        assertTrue(report.interpretations().stream().anyMatch(
            interpretation -> interpretation.phoneValidity() == PhoneValidity.INVALID
        ));
    }

    @Test
    void invalidInputIsAResultRatherThanAThrownPhoneFailure() {
        RejectedInput rejected = assertInstanceOf(RejectedInput.class, useCase.interpret("20 X"));
        assertEquals("Each input item must contain 1 to 3 digits: 'X'.", rejected.message());
        assertInstanceOf(RejectedInput.class, useCase.interpret("   "));
        assertInstanceOf(RejectedInput.class, useCase.interpret(null));
    }

    @Test
    void reportDoesNotExposeAModifiableList() {
        InterpretationReport report = assertInstanceOf(InterpretationReport.class, useCase.interpret("75"));
        assertThrows(
            UnsupportedOperationException.class,
            () -> report.interpretations().add(new NumberInterpretation("1", PhoneValidity.INVALID))
        );
    }

    @Test
    void rejectsABlankRejectionMessageAndNonDigits() {
        assertThrows(IllegalArgumentException.class, () -> new RejectedInput("  "));
        assertThrows(IllegalArgumentException.class, () -> new NumberInterpretation("12a", PhoneValidity.VALID));
        assertThrows(IllegalArgumentException.class, () -> new NumberInterpretation("12 ", PhoneValidity.VALID));
        assertThrows(IllegalArgumentException.class, () -> new NumberInterpretation("", PhoneValidity.INVALID));
        assertThrows(NullPointerException.class, () -> new NumberInterpretation(null, PhoneValidity.VALID));
    }
}
