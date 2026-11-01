package com.omilia.numbers.domain;

import java.util.regex.Pattern;

/**
 * One recognized group from speech recognition, stored as text so leading zeroes survive.
 *
 * <p>{@link #text()} is the original spelling. {@link #value()} is the numeric reading and
 * therefore drops redundant zeroes. Grammatical roles such as {@link #isUnits()} or
 * {@link #isTens()} apply only to {@linkplain #canonical() canonical} spellings: {@code 09}
 * is not the spoken number nine and must stay literal.
 *
 * @param text original digit characters; must contain one to three decimal digits
 */
public record DigitToken(String text) {

    private static final Pattern DIGITS = Pattern.compile("[0-9]{1,3}");

    private static final int MIN_UNITS = 1;
    private static final int MAX_UNITS = 9;
    /** Ten is not a tens word here: {@code 10 6} stays {@code 106}, not sixteen. */
    private static final int MIN_TENS = 20;
    private static final int MAX_TENS = 90;
    /** Eleven through nineteen are not spoken as tens plus a unit. */
    private static final int MIN_COMPOUND_TENS = 21;
    private static final int MAX_COMPOUND_TENS = 99;
    private static final int MIN_HUNDREDS = 100;
    private static final int MAX_HUNDREDS = 900;
    private static final int MIN_REMAINDER = 1;
    private static final int MAX_REMAINDER = 99;

    /**
     * Creates a token after checking that {@code text} is well formed.
     *
     * @throws IllegalArgumentException if {@code text} is not one to three digits
     */
    public DigitToken {
        if (!isWellFormed(text)) {
            throw new IllegalArgumentException("A token must contain 1 to 3 digits.");
        }
    }

    /**
     * Returns whether {@code text} contains one to three decimal digits.
     *
     * @param text candidate token text; may be {@code null}
     * @return {@code true} only when the text matches {@code [0-9]{1,3}}
     */
    public static boolean isWellFormed(String text) {
        return text != null && DIGITS.matcher(text).matches();
    }

    /**
     * Returns the integer value of this token.
     *
     * <p>Leading zeroes are discarded by the conversion. Prefer {@link #text()} when the
     * spelling must be preserved.
     *
     * @return numeric value of {@link #text()}
     */
    public int value() {
        return Integer.parseInt(text);
    }

    /**
     * Returns whether this token has no redundant leading zero.
     *
     * <p>{@code 0} is canonical; {@code 00} and {@code 09} are not.
     *
     * @return {@code true} when {@link #text()} equals {@code Integer.toString(value())}
     */
    public boolean canonical() {
        return Integer.toString(value()).equals(text);
    }

    /**
     * Returns whether this token is a canonical unit from 1 to 9.
     *
     * @return {@code true} for spoken units usable in tens-plus-units combinations
     */
    public boolean isUnits() {
        return canonical() && inRange(value(), MIN_UNITS, MAX_UNITS);
    }

    /**
     * Returns whether this token is a canonical tens word from 20 to 90.
     *
     * <p>{@code 10} is excluded so {@code 10 6} stays {@code 106}.
     *
     * @return {@code true} for multiples of ten in {@code 20..90}
     */
    public boolean isTens() {
        int number = value();
        return canonical() && inRange(number, MIN_TENS, MAX_TENS) && number % 10 == 0;
    }

    /**
     * Returns whether this token is a canonical compound tens value from 21 to 99.
     *
     * <p>Teens ({@code 11..19}) are excluded because their spoken form does not split into a
     * tens word plus a unit.
     *
     * @return {@code true} when the token can expand as tens digits followed by a units digit
     */
    public boolean isCompoundTens() {
        int number = value();
        return canonical() && inRange(number, MIN_COMPOUND_TENS, MAX_COMPOUND_TENS) && number % 10 != 0;
    }

    /**
     * Returns whether this token is a canonical hundreds word from 100 to 900.
     *
     * @return {@code true} for multiples of one hundred in {@code 100..900}
     */
    public boolean isHundreds() {
        int number = value();
        return canonical() && inRange(number, MIN_HUNDREDS, MAX_HUNDREDS) && number % 100 == 0;
    }

    /**
     * Returns whether this token is a canonical remainder from 1 to 99.
     *
     * <p>Units, teens, tens and compound tens are all remainders for hundreds combinations.
     *
     * @return {@code true} for canonical values in {@code 1..99}
     */
    public boolean isRemainder() {
        return canonical() && inRange(value(), MIN_REMAINDER, MAX_REMAINDER);
    }

    /**
     * Expands a compound tens token into tens digits followed by the units digit.
     *
     * <p>Example: {@code 69} becomes {@code 609}. The result is appended text and is never
     * re-tokenized by the interpreter.
     *
     * @return digit string produced by the expansion
     * @throws IllegalArgumentException if {@link #isCompoundTens()} is {@code false}
     */
    public String expandCompoundTens() {
        if (!isCompoundTens()) {
            throw new IllegalArgumentException(
                "Only compound tens can be expanded, but was '" + text + "'."
            );
        }
        int number = value();
        int tens = number - (number % 10);
        int units = number % 10;
        return Integer.toString(tens) + units;
    }

    /**
     * Adds canonical tokens and returns the decimal spelling of the total.
     *
     * <p>Used by combination rules such as {@code 30 + 6 = 36} or {@code 700 + 20 + 5 = 725}.
     *
     * @param tokens one or more canonical tokens
     * @return decimal spelling of the sum
     * @throws IllegalArgumentException if the array is empty/null or any token is not canonical
     */
    public static String sum(DigitToken... tokens) {
        if (tokens == null || tokens.length == 0) {
            throw new IllegalArgumentException("At least one canonical token is required.");
        }
        int total = 0;
        for (DigitToken token : tokens) {
            if (token == null || !token.canonical()) {
                throw new IllegalArgumentException("Only canonical numbers can be added.");
            }
            total += token.value();
        }
        return Integer.toString(total);
    }

    private static boolean inRange(int number, int minimum, int maximum) {
        return number >= minimum && number <= maximum;
    }
}
