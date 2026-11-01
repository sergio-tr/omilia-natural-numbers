package com.omilia.numbers.interpretation;

import com.omilia.numbers.domain.DigitToken;
import java.util.List;

/**
 * Closed set of readings that encode the assignment grammar.
 *
 * <p>Declaration order is observable: {@link NumberInterpreter} tries the constants from top to
 * bottom, so this order is part of the stable output contract. Each rule looks only at original
 * tokens; digits produced by another rule are never fed back in.
 */
enum InterpretationRule implements TokenRule {

    /**
     * Keeps the current token exactly as recognized.
     *
     * <p>Always applies and consumes one token. Because it is declared first, the literal
     * concatenation of the whole input is always the first emitted reading.
     */
    LITERAL {
        @Override
        public List<Alternative> alternatives(List<DigitToken> tokens, int index) {
            return List.of(new Alternative(tokens.get(index).text(), 1));
        }
    },

    /**
     * Expands compound tens such as {@code 69 → 609} or {@code 75 → 705}.
     *
     * <p>Consumes one token. Teens and multiples of ten do not match.
     */
    COMPOUND_TENS {
        @Override
        public List<Alternative> alternatives(List<DigitToken> tokens, int index) {
            DigitToken token = tokens.get(index);
            if (!token.isCompoundTens()) {
                return List.of();
            }
            return List.of(new Alternative(token.expandCompoundTens(), 1));
        }
    },

    /**
     * Combines a tens word with a following unit, for example {@code 30 6 → 36}.
     *
     * <p>Does not apply to {@code 10 6}. Consumes two tokens.
     */
    TENS_AND_UNITS {
        @Override
        public List<Alternative> alternatives(List<DigitToken> tokens, int index) {
            if (!has(tokens, index, 2)) {
                return List.of();
            }
            DigitToken tens = tokens.get(index);
            DigitToken units = tokens.get(index + 1);
            if (!tens.isTens() || !units.isUnits()) {
                return List.of();
            }
            return List.of(new Alternative(DigitToken.sum(tens, units), 2));
        }
    },

    /**
     * Combines hundreds with a following remainder, for example {@code 700 24 → 724}.
     *
     * <p>Also covers {@code 700 6 → 706}. Consumes two tokens.
     */
    HUNDREDS_AND_REMAINDER {
        @Override
        public List<Alternative> alternatives(List<DigitToken> tokens, int index) {
            if (!has(tokens, index, 2)) {
                return List.of();
            }
            DigitToken hundreds = tokens.get(index);
            DigitToken remainder = tokens.get(index + 1);
            if (!hundreds.isHundreds() || !remainder.isRemainder()) {
                return List.of();
            }
            return List.of(new Alternative(DigitToken.sum(hundreds, remainder), 2));
        }
    },

    /**
     * Combines hundreds, tens and units, for example {@code 700 20 5 → 725}.
     *
     * <p>Consumes three tokens.
     */
    HUNDREDS_TENS_AND_UNITS {
        @Override
        public List<Alternative> alternatives(List<DigitToken> tokens, int index) {
            if (!has(tokens, index, 3)) {
                return List.of();
            }
            DigitToken hundreds = tokens.get(index);
            DigitToken tens = tokens.get(index + 1);
            DigitToken units = tokens.get(index + 2);
            if (!hundreds.isHundreds() || !tens.isTens() || !units.isUnits()) {
                return List.of();
            }
            return List.of(new Alternative(DigitToken.sum(hundreds, tens, units), 3));
        }
    };

    /**
     * Returns whether {@code count} tokens remain from {@code index}.
     *
     * @param tokens recognized sequence
     * @param index current position
     * @param count required token width
     * @return {@code true} when the slice fits inside the sequence
     */
    private static boolean has(List<DigitToken> tokens, int index, int count) {
        return index + count <= tokens.size();
    }
}
