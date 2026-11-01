package com.omilia.numbers.phone;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class GreekPhoneNumberValidatorTest {

    private final GreekPhoneNumberValidator validator = new GreekPhoneNumberValidator();

    @ParameterizedTest
    @ValueSource(strings = {
        "2106930664",
        "2000000000",
        "2900000000",
        "6900000000",
        "6974092252",
        "2106093664"
    })
    void acceptsTenDigitsStartingWith2Or69(String digits) {
        assertEquals(PhoneValidity.VALID, validator.validate(digits));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "00302106930664",
        "00302000000000",
        "00306974092252",
        "00306972413502",
        "00306900000000"
    })
    void acceptsFourteenDigitsStartingWith00302Or003069(String digits) {
        assertEquals(PhoneValidity.VALID, validator.validate(digits));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "3106930664",
        "6000000000",
        "210693066",
        "21069306604",
        "69000000000000",
        "00306106930664",
        "0030697241350",
        "003069724135021",
        "00302",
        "69",
        "2",
        "0030697241",
        "00306972413502X",
        "2106930664 ",
        "210-693066"
    })
    void rejectsAnythingOutsideThoseTwoFormats(String digits) {
        assertEquals(PhoneValidity.INVALID, validator.validate(digits));
    }
}
