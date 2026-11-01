package com.omilia.numbers.application;

/**
 * Result of {@link InterpretNumbers#interpret(String)}.
 *
 * <p>Both a finished {@link InterpretationReport} and a {@link RejectedInput} are normal outcomes.
 * Neither case is modelled as an exception at the use-case boundary.
 */
public sealed interface InterpretationOutcome permits InterpretationReport, RejectedInput {
}
