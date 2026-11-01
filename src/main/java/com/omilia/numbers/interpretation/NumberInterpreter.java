package com.omilia.numbers.interpretation;

import com.omilia.numbers.domain.DigitToken;
import com.omilia.numbers.domain.RecognizedSequence;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Enumerates every distinct digit reading of a {@link RecognizedSequence}.
 *
 * <p>The search is depth-first and backtracks over a shared {@link StringBuilder}. A
 * {@link LinkedHashSet} keeps the first occurrence of each digit string, which makes the order
 * stable and drops duplicate derivations. The literal concatenation is always first because
 * {@link InterpretationRule#LITERAL} is tried before any other rule.
 *
 * <p>Suffix memoization was considered and rejected: ambiguous groups reconverge on the next
 * original token, so a careful traversal is already proportional to the size of the output.
 * There is no silent length cutoff; invalid phone candidates are still returned.
 *
 * <p>The call stack grows with the number of groups. That fits a spoken phone number and is the
 * wrong shape for thousands of groups.
 */
public final class NumberInterpreter {

    private final List<TokenRule> rules;

    /**
     * Creates an interpreter that uses the full closed {@link InterpretationRule} set.
     */
    public NumberInterpreter() {
        this(List.of(InterpretationRule.values()));
    }

    /**
     * Creates an interpreter driven by the supplied rules.
     *
     * <p>Package-private so tests can inject a subset of rules or a deliberately broken rule.
     *
     * @param rules non-empty rule list; declaration/injection order is the search order
     * @throws NullPointerException if {@code rules} is {@code null}
     * @throws IllegalArgumentException if {@code rules} is empty
     */
    NumberInterpreter(List<? extends TokenRule> rules) {
        this.rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
        if (this.rules.isEmpty()) {
            throw new IllegalArgumentException("At least one interpretation rule is required.");
        }
    }

    /**
     * Returns every distinct reading of {@code sequence} in stable depth-first order.
     *
     * @param sequence non-null recognized input
     * @return immutable list of digit strings; never empty for a legal sequence
     * @throws NullPointerException if {@code sequence} is {@code null}
     */
    public List<String> interpret(RecognizedSequence sequence) {
        Objects.requireNonNull(sequence, "sequence");
        Set<String> results = new LinkedHashSet<>();
        explore(sequence.tokens(), 0, new StringBuilder(), results);
        return List.copyOf(results);
    }

    /**
     * Explores every alternative from {@code index} and accumulates completed readings.
     *
     * @param tokens original recognized groups
     * @param index current token index
     * @param digits mutable prefix built so far
     * @param results destination set preserving insertion order
     */
    private void explore(List<DigitToken> tokens, int index, StringBuilder digits, Set<String> results) {
        if (index == tokens.size()) {
            results.add(digits.toString());
            return;
        }

        boolean matched = false;
        for (TokenRule rule : rules) {
            for (Alternative alternative : rule.alternatives(tokens, index)) {
                int nextIndex = index + alternative.consumed();
                if (nextIndex > tokens.size()) {
                    throw new IllegalStateException(
                        "A rule consumed past the end of the sequence at index " + index + "."
                    );
                }
                matched = true;
                int mark = digits.length();
                digits.append(alternative.segment());
                explore(tokens, nextIndex, digits, results);
                digits.setLength(mark);
            }
        }
        if (!matched) {
            throw new IllegalStateException("No interpretation rule accepted the token at index " + index + ".");
        }
    }
}
