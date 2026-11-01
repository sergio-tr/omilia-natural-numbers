package com.omilia.numbers.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DigitTokenTest {

    @ParameterizedTest
    @CsvSource({
        "0, true, false, false, false, false, false",
        "1, true, true, false, false, false, true",
        "9, true, true, false, false, false, true",
        "10, true, false, false, false, false, true",
        "11, true, false, false, false, false, true",
        "19, true, false, false, false, false, true",
        "20, true, false, true, false, false, true",
        "21, true, false, false, true, false, true",
        "25, true, false, false, true, false, true",
        "69, true, false, false, true, false, true",
        "75, true, false, false, true, false, true",
        "90, true, false, true, false, false, true",
        "99, true, false, false, true, false, true",
        "100, true, false, false, false, true, false",
        "101, true, false, false, false, false, false",
        "110, true, false, false, false, false, false",
        "200, true, false, false, false, true, false",
        "900, true, false, false, false, true, false",
        "999, true, false, false, false, false, false",
        "00, false, false, false, false, false, false",
        "09, false, false, false, false, false, false",
        "024, false, false, false, false, false, false",
        "000, false, false, false, false, false, false"
    })
    void classifiesSpokenRoles(
        String text,
        boolean canonical,
        boolean units,
        boolean tens,
        boolean compoundTens,
        boolean hundreds,
        boolean remainder
    ) {
        DigitToken token = new DigitToken(text);

        assertEquals(text, token.text());
        assertEquals(canonical, token.canonical());
        assertEquals(units, token.isUnits());
        assertEquals(tens, token.isTens());
        assertEquals(compoundTens, token.isCompoundTens());
        assertEquals(hundreds, token.isHundreds());
        assertEquals(remainder, token.isRemainder());
    }

    @ParameterizedTest
    @CsvSource({"21, 201", "25, 205", "69, 609", "75, 705", "99, 909"})
    void expandsCompoundTensWithoutRetokenizing(String text, String expanded) {
        assertEquals(expanded, new DigitToken(text).expandCompoundTens());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "9", "10", "11", "19", "20", "90", "100", "09", "00"})
    void refusesToExpandTokensThatAreNotCompoundTens(String text) {
        assertThrows(IllegalArgumentException.class, () -> new DigitToken(text).expandCompoundTens());
    }

    @Test
    void addsCanonicalGroupsAndKeepsTheDecimalSpelling() {
        assertEquals("36", DigitToken.sum(new DigitToken("30"), new DigitToken("6")));
        assertEquals("724", DigitToken.sum(new DigitToken("700"), new DigitToken("24")));
        assertEquals("725", DigitToken.sum(new DigitToken("700"), new DigitToken("20"), new DigitToken("5")));
    }

    @Test
    void refusesToAddAPaddedToken() {
        assertThrows(
            IllegalArgumentException.class,
            () -> DigitToken.sum(new DigitToken("09"), new DigitToken("1"))
        );
        assertThrows(IllegalArgumentException.class, () -> DigitToken.sum());
        assertThrows(IllegalArgumentException.class, () -> DigitToken.sum((DigitToken[]) null));
        assertThrows(IllegalArgumentException.class, () -> DigitToken.sum(new DigitToken("30"), null));
    }

    @Test
    void leadingZeroesAreADifferentTokenFromTheCanonicalNumber() {
        assertNotEquals(new DigitToken("09"), new DigitToken("9"));
        assertEquals(9, new DigitToken("09").value());
        assertFalse(new DigitToken("09").isUnits());
        assertTrue(new DigitToken("9").isUnits());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "1234", "1a", "-1", "+2", "4.5", "１２"})
    void rejectsTextThatIsNotOneToThreeDigits(String text) {
        assertFalse(DigitToken.isWellFormed(text));
        assertThrows(IllegalArgumentException.class, () -> new DigitToken(text));
    }
}
