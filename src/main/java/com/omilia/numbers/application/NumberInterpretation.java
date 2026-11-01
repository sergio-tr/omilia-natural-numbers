package com.omilia.numbers.application;

import com.omilia.numbers.phone.PhoneValidity;
import java.util.Objects;

/**
 * One reading of the input, annotated with the assignment's phone-format check.
 *
 * @param digits non-empty digit string produced by the interpreter
 * @param phoneValidity format label for {@code digits}; never {@code null}
 */
public record NumberInterpretation(String digits, PhoneValidity phoneValidity) {

    /**
     * Creates an annotated reading.
     *
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if {@code digits} is empty or contains a non-digit
     */
    public NumberInterpretation {
        Objects.requireNonNull(digits, "digits");
        Objects.requireNonNull(phoneValidity, "phoneValidity");
        if (digits.isEmpty() || digits.chars().anyMatch(ch -> ch < '0' || ch > '9')) {
            throw new IllegalArgumentException("Digits must be a non-empty numeric string.");
        }
    }
}
