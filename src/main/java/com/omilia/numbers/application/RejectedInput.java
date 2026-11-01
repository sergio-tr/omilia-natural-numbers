package com.omilia.numbers.application;

import java.util.Objects;

/**
 * Use-case outcome for a line that could not be parsed.
 *
 * @param message explanation safe to show to the console user
 */
public record RejectedInput(String message) implements InterpretationOutcome {

    /**
     * Creates a rejection with a non-blank message.
     *
     * @throws NullPointerException if {@code message} is {@code null}
     * @throws IllegalArgumentException if {@code message} is blank
     */
    public RejectedInput {
        Objects.requireNonNull(message, "message");
        if (message.isBlank()) {
            throw new IllegalArgumentException("A rejection needs a message.");
        }
    }
}
