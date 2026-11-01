package com.omilia.numbers.phone;

/**
 * Result of the assignment's Greek telephone format check.
 *
 * <p>This is not a full national numbering plan. It only encodes the two lengths and four
 * prefixes required by the exercise.
 */
public enum PhoneValidity {

    /** The digit string matches a stated Greek telephone format. */
    VALID,

    /** The digit string is missing, non-numeric, or outside the stated formats. */
    INVALID
}
