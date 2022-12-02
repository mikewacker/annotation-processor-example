package org.example.immutable.processor.error;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import org.junit.jupiter.api.Test;

public class ErrorsTest {

    @Test
    public void reportError() {
        MockMessager messager = new MockMessager();
        Errors errorReporter = new Errors(messager);
        errorReporter.error("error");
        assertThat(messager.getMessage()).isEqualTo("[@Immutable] error");
    }

    @Test
    public void trackErrors() {
        Errors errorReporter = new Errors(new MockMessager());
        try (Errors.Tracker errorTracker = errorReporter.createErrorTracker()) {
            Object result = new Object();
            assertThat(errorTracker.checkNoErrors(result)).isPresent();
            errorReporter.error("error");
            assertThat(errorTracker.checkNoErrors(result)).isEmpty();
        }
    }

    /** Stores the last error message. */
    private static final class MockMessager implements Messager {

        private String message = "";

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence charSequence) {
            storeErrorMessage(kind, charSequence);
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence charSequence, Element element) {
            storeErrorMessage(kind, charSequence);
        }

        @Override
        public void printMessage(
                Diagnostic.Kind kind, CharSequence charSequence, Element element, AnnotationMirror annotationMirror) {
            storeErrorMessage(kind, charSequence);
        }

        @Override
        public void printMessage(
                Diagnostic.Kind kind,
                CharSequence charSequence,
                Element element,
                AnnotationMirror annotationMirror,
                AnnotationValue annotationValue) {
            storeErrorMessage(kind, charSequence);
        }

        public String getMessage() {
            return message;
        }

        private void storeErrorMessage(Diagnostic.Kind kind, CharSequence charSequence) {
            if (kind != Diagnostic.Kind.ERROR) {
                return;
            }

            message = charSequence.toString();
        }
    }
}
