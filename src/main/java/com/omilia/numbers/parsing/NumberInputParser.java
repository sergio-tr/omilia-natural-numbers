package com.omilia.numbers.parsing;

import com.omilia.numbers.domain.DigitToken;
import com.omilia.numbers.domain.InvalidInputException;
import com.omilia.numbers.domain.RecognizedSequence;
import java.util.ArrayList;
import java.util.List;

/**
 * Splits a console line on whitespace into recognized number groups.
 *
 * <p>Each item must contain one to three digits. Leading zeroes are preserved because tokens
 * stay as text.
 */
public final class NumberInputParser {

    /** Creates a parser for whitespace-separated recognized numbers. */
    public NumberInputParser() {
    }

    /**
     * Parses {@code input} into a non-empty {@link RecognizedSequence}.
     *
     * @param input whitespace-separated groups; surrounding blanks are ignored
     * @return immutable sequence of well-formed tokens
     * @throws InvalidInputException if the line is blank or any item is not 1–3 digits
     */
    public RecognizedSequence parse(String input) {
        if (input == null || input.isBlank()) {
            throw new InvalidInputException("Input must contain at least one number.");
        }

        String[] parts = input.strip().split("\\s+");
        List<DigitToken> tokens = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (!DigitToken.isWellFormed(part)) {
                throw new InvalidInputException(
                    "Each input item must contain 1 to 3 digits: '" + part + "'."
                );
            }
            tokens.add(new DigitToken(part));
        }
        return new RecognizedSequence(tokens);
    }
}
