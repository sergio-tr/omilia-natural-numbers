package com.omilia.numbers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MainTest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @AfterEach
    void restoreStreamsAndExitHandler() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        System.setErr(originalErr);
        Main.resetExitHandler();
    }

    @Test
    void mainSucceedsAndExitsWithZero() {
        AtomicInteger status = new AtomicInteger(Integer.MIN_VALUE);
        Main.setExitHandler(status::set);

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        System.setIn(new ByteArrayInputStream("20 5\nexit\n".getBytes(StandardCharsets.UTF_8)));
        System.setOut(new PrintStream(stdout, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(stderr, true, StandardCharsets.UTF_8));

        Main.main(new String[] {});

        assertEquals(0, status.get());
        assertTrue(stdout.toString(StandardCharsets.UTF_8).contains("205"));
        assertEquals("", stderr.toString(StandardCharsets.UTF_8));
    }

    @Test
    void mainReportsReadFailureAndExitsWithOne() {
        AtomicInteger status = new AtomicInteger(Integer.MIN_VALUE);
        Main.setExitHandler(status::set);

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        System.setIn(new FailingInputStream("disk full"));
        System.setOut(new PrintStream(stdout, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(stderr, true, StandardCharsets.UTF_8));

        Main.main(new String[] {"ignored"});

        assertEquals(1, status.get());
        assertEquals(
            "Could not read input: disk full" + System.lineSeparator(),
            stderr.toString(StandardCharsets.UTF_8)
        );
        assertTrue(stdout.toString(StandardCharsets.UTF_8).contains("Input: "));
        assertFalse(stdout.toString(StandardCharsets.UTF_8).contains("Interpretations"));
    }

    @Test
    void runReturnsStatusWithoutCallingSystemExit() throws IOException {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(stdout, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(stderr, true, StandardCharsets.UTF_8);

        assertEquals(
            0,
            Main.run(new ByteArrayInputStream("75\nexit\n".getBytes(StandardCharsets.UTF_8)), out, err)
        );
        assertTrue(stdout.toString(StandardCharsets.UTF_8).contains("705"));
        assertEquals("", stderr.toString(StandardCharsets.UTF_8));

        assertEquals(1, Main.run(new FailingInputStream("broken pipe"), out, err));
        assertTrue(stderr.toString(StandardCharsets.UTF_8).contains("Could not read input: broken pipe"));
    }

    @Test
    void launchWiresParsingInterpretationAndPhoneValidation() throws IOException {
        byte[] input = "2 10 69 30 6 6 4\nexit\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        Main.launch(new ByteArrayInputStream(input), out);

        String text = buffer.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("2106930664"));
        assertTrue(text.contains("VALID"));
        assertTrue(text.contains("21060930664"));
        assertTrue(text.contains("INVALID"));
        assertFalse(text.contains("Invalid input:"));
    }

    private static final class FailingInputStream extends InputStream {
        private final String message;

        private FailingInputStream(String message) {
            this.message = message;
        }

        @Override
        public int read() throws IOException {
            throw new IOException(message);
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            throw new IOException(message);
        }
    }
}
