package org.example.processor.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import org.junit.jupiter.api.Test;

public final class DiagnosticTest {

    @Test
    public void addError() {
        MessageStorer messageStorer = MessageStorer.of();
        Diagnostics diagnostics = new Diagnostics(messageStorer, "Test");
        boolean result = diagnostics.add(Diagnostic.Kind.ERROR, "message");
        assertThat(result).isFalse();
        assertThat(messageStorer.getMessage()).isEqualTo("[Test] message");
    }

    @Test
    public void addWarning() {
        MessageStorer messageStorer = MessageStorer.of();
        Diagnostics diagnostics = new Diagnostics(messageStorer, "Test");
        boolean result = diagnostics.add(Diagnostic.Kind.WARNING, "message");
        assertThat(result).isTrue();
        assertThat(messageStorer.getMessage()).isEqualTo("[Test] message");
    }

    @Test
    public void trackErrors_NoDiagnostics() {
        Diagnostics diagnostics = new Diagnostics(MessageStorer.of(), "Test");
        try (Diagnostics.ErrorTracker errorTracker = diagnostics.trackErrors()) {
            Optional<Object> maybeValue = errorTracker.checkNoErrors(new Object());
            assertThat(maybeValue).isPresent();
        }
    }

    @Test
    public void trackErrors_Error() {
        Diagnostics diagnostics = new Diagnostics(MessageStorer.of(), "Test");
        try (Diagnostics.ErrorTracker errorTracker = diagnostics.trackErrors()) {
            diagnostics.add(Diagnostic.Kind.ERROR, "message");
            Optional<Object> maybeValue = errorTracker.checkNoErrors(new Object());
            assertThat(maybeValue).isEmpty();
        }
    }

    @Test
    public void trackErrors_Warning() {
        Diagnostics diagnostics = new Diagnostics(MessageStorer.of(), "Test");
        try (Diagnostics.ErrorTracker errorTracker = diagnostics.trackErrors()) {
            Optional<Object> maybeValue = errorTracker.checkNoErrors(new Object());
            diagnostics.add(Diagnostic.Kind.WARNING, "message");
            assertThat(maybeValue).isPresent();
        }
    }

    /** Stores the message for retrieval. */
    private static final class MessageStorer implements Messager {

        private String message;

        public static MessageStorer of() {
            return new MessageStorer();
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            message = msg.toString();
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
            message = msg.toString();
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
            message = msg.toString();
        }

        @Override
        public void printMessage(
                Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) {
            message = msg.toString();
        }

        public String getMessage() {
            return message;
        }

        private MessageStorer() {}
    }
}
