package com.omilia.numbers.interpretation;

import com.omilia.numbers.domain.DigitToken;
import java.util.List;

/**
 * Proposes zero or more readings for the tokens that start at a given index.
 *
 * <p>Production code uses the closed {@link InterpretationRule} enum. Tests may inject a custom
 * implementation to exercise interpreter guards.
 */
@FunctionalInterface
interface TokenRule {

    /**
     * Returns the alternatives available at {@code index}.
     *
     * @param tokens full recognized sequence
     * @param index current search position; always in range for a healthy search
     * @return zero or more alternatives; an empty list means this rule does not apply
     */
    List<Alternative> alternatives(List<DigitToken> tokens, int index);
}
