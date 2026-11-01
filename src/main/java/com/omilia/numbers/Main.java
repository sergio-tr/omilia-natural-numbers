package com.omilia.numbers;

import com.omilia.numbers.application.InterpretNumbers;
import com.omilia.numbers.cli.ConsoleApplication;
import com.omilia.numbers.cli.ReportFormatter;
import com.omilia.numbers.interpretation.NumberInterpreter;
import com.omilia.numbers.parsing.NumberInputParser;
import com.omilia.numbers.phone.GreekPhoneNumberValidator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.IntConsumer;

/**
 * Composition root of the standalone console application.
 *
 * <p>Program arguments are ignored. Numbers are read from standard input until the user types
 * {@code exit} or the stream ends.
 */
public final class Main {

    private static IntConsumer exitHandler = System::exit;

    private Main() {
    }

    /**
     * Starts the interactive application on the process standard streams.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        exitHandler.accept(run(System.in, System.out, System.err));
    }

    /**
     * Runs the console application and returns a process status code.
     *
     * @param in source of user lines
     * @param out destination for prompts and reports
     * @param err destination for technical read failures
     * @return {@code 0} on success; {@code 1} when standard input cannot be read
     * @throws NullPointerException if any argument is {@code null}
     */
    static int run(InputStream in, PrintStream out, PrintStream err) {
        Objects.requireNonNull(in, "in");
        Objects.requireNonNull(out, "out");
        Objects.requireNonNull(err, "err");
        try {
            launch(in, out);
            return 0;
        } catch (IOException exception) {
            err.println("Could not read input: " + exception.getMessage());
            return 1;
        }
    }

    /**
     * Wires collaborators and runs one console session on the given streams.
     *
     * @param in source of user lines
     * @param out destination for prompts and reports
     * @throws IOException if reading fails
     */
    static void launch(InputStream in, PrintStream out) throws IOException {
        ConsoleApplication application = new ConsoleApplication(
            new InterpretNumbers(
                new NumberInputParser(),
                new NumberInterpreter(),
                new GreekPhoneNumberValidator()
            ),
            new ReportFormatter()
        );
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8)
            )
        ) {
            application.run(reader, out);
        }
    }

    /**
     * Replaces {@link System#exit(int)} so unit tests can exercise {@link #main(String[])}.
     *
     * @param handler non-null exit callback
     */
    static void setExitHandler(IntConsumer handler) {
        exitHandler = Objects.requireNonNull(handler, "handler");
    }

    /**
     * Restores the default {@link System#exit(int)} handler after a test.
     */
    static void resetExitHandler() {
        exitHandler = System::exit;
    }
}
