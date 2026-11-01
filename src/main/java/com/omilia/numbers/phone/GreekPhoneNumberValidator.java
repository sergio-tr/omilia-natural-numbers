package com.omilia.numbers.phone;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Applies the Greek telephone formats stated by the assignment.
 *
 * <ul>
 *   <li>10 digits starting with {@code 2} or {@code 69}</li>
 *   <li>14 digits starting with {@code 00302} or {@code 003069}</li>
 * </ul>
 *
 * <p>An ill-formed value yields {@link PhoneValidity#INVALID}. Validation never throws for a bad
 * number and never decides which interpretations the engine should emit.
 */
public final class GreekPhoneNumberValidator {

    private static final Pattern DIGITS = Pattern.compile("[0-9]+");

    /**
     * One accepted length together with its allowed prefixes.
     *
     * @param length exact digit count required by the format
     * @param prefixes accepted leading digit sequences for that length
     */
    private record AcceptedFormat(int length, List<String> prefixes) {
    }

    private static final List<AcceptedFormat> ACCEPTED_FORMATS = List.of(
        new AcceptedFormat(10, List.of("2", "69")),
        new AcceptedFormat(14, List.of("00302", "003069"))
    );

    /** Creates a validator for the assignment formats. */
    public GreekPhoneNumberValidator() {
    }

    /**
     * Labels {@code digits} as {@link PhoneValidity#VALID} or {@link PhoneValidity#INVALID}.
     *
     * @param digits candidate telephone number; may be {@code null}
     * @return format verdict; never {@code null}
     */
    public PhoneValidity validate(String digits) {
        if (digits == null || !DIGITS.matcher(digits).matches()) {
            return PhoneValidity.INVALID;
        }
        for (AcceptedFormat format : ACCEPTED_FORMATS) {
            if (digits.length() == format.length() && startsWithAny(digits, format.prefixes())) {
                return PhoneValidity.VALID;
            }
        }
        return PhoneValidity.INVALID;
    }

    private static boolean startsWithAny(String digits, List<String> prefixes) {
        for (String prefix : prefixes) {
            if (digits.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
