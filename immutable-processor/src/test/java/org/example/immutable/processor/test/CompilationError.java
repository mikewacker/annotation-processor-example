package org.example.immutable.processor.test;

import com.google.testing.compile.Compilation;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import org.immutables.value.Value;

/** Unique compilation error, identified by the line number and the message. */
@Value.Immutable
public interface CompilationError {

    static List<CompilationError> fromCompilation(Compilation compilation) {
        return compilation.errors().stream()
                .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)
                .map(diagnostic -> CompilationError.of(diagnostic.getLineNumber(), diagnostic.getMessage(Locale.US)))
                .toList();
    }

    static CompilationError of(long lineNumber, String message) {
        return ImmutableCompilationError.builder()
                .lineNumber(lineNumber)
                .message(message)
                .build();
    }

    long lineNumber();

    String message();
}
