package com.omilia.numbers.application;

import com.omilia.numbers.domain.InvalidInputException;
import com.omilia.numbers.domain.RecognizedSequence;
import com.omilia.numbers.interpretation.NumberInterpreter;
import com.omilia.numbers.parsing.NumberInputParser;
import com.omilia.numbers.phone.GreekPhoneNumberValidator;
import java.util.List;
import java.util.Objects;

/**
 * Application use case: parse a line, enumerate every reading, and annotate each one.
 *
 * <p>This type does not read the console and does not drop a reading because the phone check
 * failed. Invalid input becomes {@link RejectedInput}; successful work becomes
 * {@link InterpretationReport}.
 */
public final class InterpretNumbers {

    private final NumberInputParser parser;
    private final NumberInterpreter interpreter;
    private final GreekPhoneNumberValidator validator;

    /**
     * Creates the use case with its concrete collaborators.
     *
     * @param parser input tokenizer and syntax validator
     * @param interpreter ambiguity enumerator
     * @param validator Greek phone-format checker
     * @throws NullPointerException if any argument is {@code null}
     */
    public InterpretNumbers(
        NumberInputParser parser,
        NumberInterpreter interpreter,
        GreekPhoneNumberValidator validator
    ) {
        this.parser = Objects.requireNonNull(parser, "parser");
        this.interpreter = Objects.requireNonNull(interpreter, "interpreter");
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    /**
     * Interprets one console line.
     *
     * @param input whitespace-separated recognized numbers; may be {@code null} or blank
     * @return either a report of annotated readings or a rejection with a user-facing message
     */
    public InterpretationOutcome interpret(String input) {
        final RecognizedSequence sequence;
        try {
            sequence = parser.parse(input);
        } catch (InvalidInputException exception) {
            return new RejectedInput(exception.getMessage());
        }

        List<NumberInterpretation> interpretations = interpreter.interpret(sequence).stream()
            .map(digits -> new NumberInterpretation(digits, validator.validate(digits)))
            .toList();
        return new InterpretationReport(interpretations);
    }
}
