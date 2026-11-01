package com.omilia.numbers.cli;

import com.omilia.numbers.application.InterpretationReport;
import com.omilia.numbers.application.InterpretNumbers;
import com.omilia.numbers.application.RejectedInput;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;

/**
 * Interactive console loop for the assignment.
 *
 * <p>All number logic stays behind {@link InterpretNumbers}. This class only prompts, prints and
 * stops on {@code exit} or end of stream.
 */
public final class ConsoleApplication {

    private final InterpretNumbers useCase;
    private final ReportFormatter formatter;

    /**
     * Creates the console adapter.
     *
     * @param useCase application entry point
     * @param formatter report renderer
     * @throws NullPointerException if any argument is {@code null}
     */
    public ConsoleApplication(InterpretNumbers useCase, ReportFormatter formatter) {
        this.useCase = Objects.requireNonNull(useCase, "useCase");
        this.formatter = Objects.requireNonNull(formatter, "formatter");
    }

    /**
     * Runs the prompt loop until the user exits or the input stream ends.
     *
     * @param reader line-oriented input
     * @param out destination for prompts and reports
     * @throws IOException if reading from {@code reader} fails
     * @throws NullPointerException if any argument is {@code null}
     */
    public void run(BufferedReader reader, PrintStream out) throws IOException {
        Objects.requireNonNull(reader, "reader");
        Objects.requireNonNull(out, "out");
        out.println("Natural Numbers Interpretation");
        out.println("Enter space-separated numbers (1-3 digits each), or 'exit'.");

        while (true) {
            out.print("Input: ");
            out.flush();
            String line = reader.readLine();
            if (line == null || "exit".equalsIgnoreCase(line.strip())) {
                return;
            }
            switch (useCase.interpret(line)) {
                case InterpretationReport report -> out.print(formatter.format(report));
                case RejectedInput rejected -> out.println("Invalid input: " + rejected.message());
            }
            out.println();
        }
    }
}
