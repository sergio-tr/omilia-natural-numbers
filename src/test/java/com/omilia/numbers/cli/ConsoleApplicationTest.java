package com.omilia.numbers.cli;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.omilia.numbers.application.InterpretNumbers;
import com.omilia.numbers.interpretation.NumberInterpreter;
import com.omilia.numbers.parsing.NumberInputParser;
import com.omilia.numbers.phone.GreekPhoneNumberValidator;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ConsoleApplicationTest {

    private final ConsoleApplication application = new ConsoleApplication(
        new InterpretNumbers(
            new NumberInputParser(),
            new NumberInterpreter(),
            new GreekPhoneNumberValidator()
        ),
        new ReportFormatter()
    );

    @Test
    void sessionPrintsReadingsContinuesAfterABadLineAndStopsOnExit() throws IOException {
        String output = run("""
            20 5
            20 X
             exit\s
            75
            """);

        assertTrue(output.contains("Natural Numbers Interpretation"));
        assertTrue(output.contains("Interpretations (2):"));
        assertTrue(output.contains("205"));
        assertTrue(output.contains("25"));
        assertTrue(output.contains("INVALID"));
        assertTrue(output.contains("Invalid input: Each input item must contain 1 to 3 digits: 'X'."));
        assertFalse(output.contains("705"));
    }

    @Test
    void endOfStreamStopsTheLoop() throws IOException {
        String output = run("2 10 6 9 30 6 6 4\n");

        assertTrue(output.contains("2106930664"));
        assertTrue(output.contains("VALID"));
        assertTrue(output.contains("210693664"));
        assertTrue(output.contains("INVALID"));
    }

    @Test
    void blankLineIsReportedAndThePromptReturns() throws IOException {
        String output = run("\n75\nexit\n");

        assertTrue(output.contains("Invalid input: Input must contain at least one number."));
        assertTrue(output.contains("75"));
        assertTrue(output.contains("705"));
    }

    private String run(String input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        application.run(new BufferedReader(new StringReader(input)), out);
        out.flush();
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
