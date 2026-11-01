package com.omilia.numbers.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.omilia.numbers.domain.DigitToken;
import com.omilia.numbers.domain.InvalidInputException;
import com.omilia.numbers.domain.RecognizedSequence;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class NumberInputParserTest {

    private final NumberInputParser parser = new NumberInputParser();

    @Test
    void preservesLeadingZeroesAndCollapsesWhitespace() {
        assertEquals(tokens("0", "0", "30", "69"), parser.parse("0 0 30 69"));
        assertEquals(tokens("002", "09"), parser.parse("  002   09  "));
        assertEquals(tokens("20", "5"), parser.parse("20\t5"));
        assertEquals(tokens("1", "2"), parser.parse("1\n2"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    void rejectsBlankInput(String input) {
        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> parser.parse(input));
        assertEquals("Input must contain at least one number.", exception.getMessage());
    }

    @Test
    void namesTheFirstIllFormedToken() {
        InvalidInputException exception = assertThrows(
            InvalidInputException.class,
            () -> parser.parse("20 X 5")
        );
        assertEquals("Each input item must contain 1 to 3 digits: 'X'.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234", "20 -5", "20 4.5", "+20", "20 5a", "1,2", "１２"})
    void rejectsTokensOutsideOneToThreeDigits(String input) {
        assertThrows(InvalidInputException.class, () -> parser.parse(input));
    }

    private static RecognizedSequence tokens(String... parts) {
        return new RecognizedSequence(List.of(parts).stream().map(DigitToken::new).toList());
    }
}
