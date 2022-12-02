package org.example.immutable.processor.test;

import com.google.testing.compile.Compilation;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;

/** Provides assertions for the errors returned by {@link Compilation#errors()}. */
public final class CompilationErrorsSubject {

    private final ListAssert<CompilationError> assertThat;

    public static CompilationErrorsSubject assertThat(List<Diagnostic<? extends JavaFileObject>> diagnosticErrors) {
        return new CompilationErrorsSubject(diagnosticErrors);
    }

    private CompilationErrorsSubject(List<Diagnostic<? extends JavaFileObject>> diagnosticErrors) {
        List<CompilationError> errors =
                diagnosticErrors.stream().map(CompilationErrorsSubject::toError).toList();
        assertThat = Assertions.assertThat(errors);
    }

    /** Asserts that the errors contains the provided errors. */
    public void contains(CompilationError... expectedErrors) {
        assertThat.contains(expectedErrors);
    }

    /** Asserts that the errors contains exactly the provided errors in any order. */
    public void containsExactlyInAnyOrder(CompilationError... expectedErrors) {
        assertThat.containsExactlyInAnyOrder(expectedErrors);
    }

    private static CompilationError toError(Diagnostic<? extends JavaFileObject> diagnosticError) {
        return CompilationError.of(diagnosticError.getLineNumber(), diagnosticError.getMessage(Locale.US));
    }
}
