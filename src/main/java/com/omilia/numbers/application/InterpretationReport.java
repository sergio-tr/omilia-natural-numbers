package com.omilia.numbers.application;

import java.util.List;
import java.util.Objects;

/**
 * Successful use-case outcome: every distinct reading with its phone label.
 *
 * @param interpretations immutable ordered readings; may contain both VALID and INVALID phones
 */
public record InterpretationReport(List<NumberInterpretation> interpretations)
    implements InterpretationOutcome {

    /**
     * Copies {@code interpretations} so callers cannot mutate the report.
     *
     * @throws NullPointerException if {@code interpretations} is {@code null}
     */
    public InterpretationReport {
        interpretations = List.copyOf(Objects.requireNonNull(interpretations, "interpretations"));
    }
}
