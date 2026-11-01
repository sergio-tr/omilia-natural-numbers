package com.omilia.numbers.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.omilia.numbers.application.InterpretationReport;
import com.omilia.numbers.application.NumberInterpretation;
import com.omilia.numbers.phone.PhoneValidity;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportFormatterTest {

    private final ReportFormatter formatter = new ReportFormatter();

    @Test
    void alignsTheSameColumnsTheConsoleUsedToPrint() {
        String formatted = formatter.format(new InterpretationReport(List.of(
            new NumberInterpretation("2106930664", PhoneValidity.VALID),
            new NumberInterpretation("210693664", PhoneValidity.INVALID),
            new NumberInterpretation("21060930664", PhoneValidity.INVALID),
            new NumberInterpretation("2106093664", PhoneValidity.VALID)
        )));

        assertEquals(
            """
            Interpretations (4):
            #      Output                 Phone number
            1      2106930664             VALID
            2      210693664              INVALID
            3      21060930664            INVALID
            4      2106093664             VALID
            """.replace("\n", System.lineSeparator()),
            formatted
        );
    }

    @Test
    void doesNotTruncateAReadingLongerThanTheColumn() {
        String digits = "1".repeat(30);
        String formatted = formatter.format(new InterpretationReport(List.of(
            new NumberInterpretation(digits, PhoneValidity.INVALID)
        )));

        assertTrue(formatted.contains(digits));
        assertTrue(formatted.contains("INVALID"));
    }

    @Test
    void rejectsANullReport() {
        assertThrows(NullPointerException.class, () -> formatter.format(null));
    }
}
