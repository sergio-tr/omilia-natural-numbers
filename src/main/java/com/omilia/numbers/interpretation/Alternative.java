package com.omilia.numbers.interpretation;

/**
 * One local reading of the tokens that start at the current search index.
 *
 * <p>{@code segment} is appended as digit text and is never tokenized again.
 * {@code consumed} is how many original input tokens this reading uses.
 *
 * @param segment non-empty digit string to append to the current candidate
 * @param consumed number of original tokens consumed; always at least one
 */
record Alternative(String segment, int consumed) {

    /**
     * Validates the segment and consumption width.
     *
     * @throws IllegalArgumentException if {@code segment} is not digits or {@code consumed < 1}
     */
    Alternative {
        if (segment == null || segment.isEmpty() || segment.chars().anyMatch(ch -> ch < '0' || ch > '9')) {
            throw new IllegalArgumentException("A segment must be a non-empty digit string.");
        }
        if (consumed < 1) {
            throw new IllegalArgumentException("A rule must consume at least one token.");
        }
    }
}
