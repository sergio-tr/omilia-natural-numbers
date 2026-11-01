package com.omilia.numbers.domain;

import java.util.List;
import java.util.Objects;

/**
 * Non-empty immutable row of recognized number groups.
 *
 * <p>The interpreter trusts this type and does not re-validate token spelling.
 *
 * @param tokens ordered recognized groups; copied defensively and never empty
 */
public record RecognizedSequence(List<DigitToken> tokens) {

    /**
     * Creates a sequence from the given tokens.
     *
     * @throws NullPointerException if {@code tokens} is {@code null}
     * @throws IllegalArgumentException if {@code tokens} is empty
     */
    public RecognizedSequence {
        Objects.requireNonNull(tokens, "tokens");
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("At least one recognized number is required.");
        }
        tokens = List.copyOf(tokens);
    }
}
