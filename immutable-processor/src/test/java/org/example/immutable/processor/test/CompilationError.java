package org.example.immutable.processor.test;

import org.immutables.value.Value;

/** Unique compilation error, identified by the line number and the message. */
@Value.Immutable
public interface CompilationError {

    static CompilationError of(long lineNumber, String message) {
        return ImmutableCompilationError.builder()
                .lineNumber(lineNumber)
                .message(message)
                .build();
    }

    long lineNumber();

    String message();
}
