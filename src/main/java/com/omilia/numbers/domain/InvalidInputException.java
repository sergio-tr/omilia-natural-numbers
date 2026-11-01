package com.omilia.numbers.domain;

/**
 * Signals that the caller supplied text that is not a sequence of recognized numbers.
 *
 * <p>Phone numbers that fail the Greek format check are <em>not</em> this failure: they remain
 * ordinary interpretations marked {@link com.omilia.numbers.phone.PhoneValidity#INVALID}.
 */
public final class InvalidInputException extends RuntimeException {

    /**
     * Creates an exception with a message safe to show to the console user.
     *
     * @param message human-readable description of the invalid input
     */
    public InvalidInputException(String message) {
        super(message);
    }
}
