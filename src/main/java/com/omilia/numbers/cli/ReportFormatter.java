package com.omilia.numbers.cli;

import com.omilia.numbers.application.InterpretationReport;
import com.omilia.numbers.application.NumberInterpretation;
import com.omilia.numbers.phone.PhoneValidity;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Renders an {@link InterpretationReport} as aligned console text.
 *
 * <p>Formatting only: this class never decides phone validity.
 */
public final class ReportFormatter {

    /** Creates a formatter for console interpretation reports. */
    public ReportFormatter() {
    }

    /**
     * Formats {@code report} with numbered rows and {@code VALID}/{@code INVALID} labels.
     *
     * @param report completed interpretation report
     * @return multi-line text ready to print; never {@code null}
     * @throws NullPointerException if {@code report} is {@code null}
     */
    public String format(InterpretationReport report) {
        Objects.requireNonNull(report, "report");
        StringBuilder text = new StringBuilder();
        text.append(String.format(Locale.ROOT, "Interpretations (%d):%n", report.interpretations().size()));
        text.append(String.format(Locale.ROOT, "%-6s %-22s %s%n", "#", "Output", "Phone number"));
        List<NumberInterpretation> interpretations = report.interpretations();
        for (int index = 0; index < interpretations.size(); index++) {
            NumberInterpretation interpretation = interpretations.get(index);
            text.append(String.format(
                Locale.ROOT,
                "%-6d %-22s %s%n",
                index + 1,
                interpretation.digits(),
                label(interpretation.phoneValidity())
            ));
        }
        return text.toString();
    }

    private static String label(PhoneValidity validity) {
        return switch (validity) {
            case VALID -> "VALID";
            case INVALID -> "INVALID";
        };
    }
}
