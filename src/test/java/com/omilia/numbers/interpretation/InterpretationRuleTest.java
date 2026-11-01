package com.omilia.numbers.interpretation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.omilia.numbers.domain.DigitToken;
import java.util.List;
import org.junit.jupiter.api.Test;

class InterpretationRuleTest {

    @Test
    void alternativeRejectsASegmentThatIsNotDigits() {
        assertThrows(IllegalArgumentException.class, () -> new Alternative(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new Alternative("", 1));
        assertThrows(IllegalArgumentException.class, () -> new Alternative("12a", 1));
        assertThrows(IllegalArgumentException.class, () -> new Alternative("12 ", 1));
        assertThrows(IllegalArgumentException.class, () -> new Alternative("1", 0));
    }

    @Test
    void declarationOrderIsTheSearchOrder() {
        assertEquals(
            List.of(
                InterpretationRule.LITERAL,
                InterpretationRule.COMPOUND_TENS,
                InterpretationRule.TENS_AND_UNITS,
                InterpretationRule.HUNDREDS_AND_REMAINDER,
                InterpretationRule.HUNDREDS_TENS_AND_UNITS
            ),
            List.of(InterpretationRule.values())
        );
    }

    @Test
    void literalKeepsTheOriginalSpelling() {
        assertEquals(
            List.of(new Alternative("09", 1)),
            InterpretationRule.LITERAL.alternatives(List.of(new DigitToken("09")), 0)
        );
    }

    @Test
    void compoundTensExpandsOnlyWhenTheTokenIsCompound() {
        assertEquals(
            List.of(new Alternative("609", 1)),
            InterpretationRule.COMPOUND_TENS.alternatives(List.of(new DigitToken("69")), 0)
        );
        assertTrue(InterpretationRule.COMPOUND_TENS.alternatives(List.of(new DigitToken("11")), 0).isEmpty());
        assertTrue(InterpretationRule.COMPOUND_TENS.alternatives(List.of(new DigitToken("30")), 0).isEmpty());
    }

    @Test
    void tensAndUnitsRequiresATensWordFollowedByAUnit() {
        assertEquals(List.of(new Alternative("36", 2)), rule(InterpretationRule.TENS_AND_UNITS, "30", "6"));
        assertTrue(rule(InterpretationRule.TENS_AND_UNITS, "10", "6").isEmpty());
        assertTrue(rule(InterpretationRule.TENS_AND_UNITS, "30", "06").isEmpty());
        assertTrue(rule(InterpretationRule.TENS_AND_UNITS, "30").isEmpty());
    }

    @Test
    void hundredsCombineWithARemainderOrWithTensAndUnits() {
        assertEquals(List.of(new Alternative("724", 2)), rule(InterpretationRule.HUNDREDS_AND_REMAINDER, "700", "24"));
        assertEquals(List.of(new Alternative("706", 2)), rule(InterpretationRule.HUNDREDS_AND_REMAINDER, "700", "6"));
        assertTrue(rule(InterpretationRule.HUNDREDS_AND_REMAINDER, "700", "024").isEmpty());
        assertTrue(rule(InterpretationRule.HUNDREDS_AND_REMAINDER, "700", "100").isEmpty());

        assertEquals(
            List.of(new Alternative("725", 3)),
            rule(InterpretationRule.HUNDREDS_TENS_AND_UNITS, "700", "20", "5")
        );
        assertTrue(rule(InterpretationRule.HUNDREDS_TENS_AND_UNITS, "700", "24", "1").isEmpty());
        assertTrue(rule(InterpretationRule.HUNDREDS_TENS_AND_UNITS, "700", "20").isEmpty());
    }

    private static List<Alternative> rule(InterpretationRule rule, String... parts) {
        List<DigitToken> tokens = List.of(parts).stream().map(DigitToken::new).toList();
        return rule.alternatives(tokens, 0);
    }
}
