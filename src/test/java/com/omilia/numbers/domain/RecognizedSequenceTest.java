package com.omilia.numbers.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecognizedSequenceTest {

    @Test
    void copiesTokensSoTheCallerCannotChangeThem() {
        List<DigitToken> backing = new ArrayList<>();
        backing.add(new DigitToken("20"));
        RecognizedSequence sequence = new RecognizedSequence(backing);

        backing.add(new DigitToken("5"));

        assertEquals(List.of(new DigitToken("20")), sequence.tokens());
        assertThrows(UnsupportedOperationException.class, () -> sequence.tokens().add(new DigitToken("6")));
    }

    @Test
    void rejectsAnEmptyRow() {
        assertThrows(IllegalArgumentException.class, () -> new RecognizedSequence(List.of()));
        assertThrows(NullPointerException.class, () -> new RecognizedSequence(null));
    }
}
