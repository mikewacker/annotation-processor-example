package org.example.processor.diagnostic;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.Messager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import org.example.processor.base.ProcessorScope;

/**
 * Wrapper for {@link Messager} that tracks errors and tags messages.
 *
 * <p>The associated {@link ErrorTracker} will convert the result to {@code Optional.empty()} if an error occurs.
 * This allow processing to continue even if an error occurs.</p>
 *
 * <p>{@link Messager} should not be used directly; this will break error tracking.</p>
 */
@ProcessorScope
public class Diagnostics {

    private final Messager messager;
    private final String tag;

    private final Set<ErrorTracker> errorTrackers = new HashSet<>();

    @Inject
    public Diagnostics(Messager messager, @Named("diagnosticTag") String tag) {
        this.messager = messager;
        this.tag = tag;
    }

    /** Adds a diagnostic of the specified kind, returning false if the diagnostic is an error. */
    public boolean add(Diagnostic.Kind kind, CharSequence msg) {
        String taggedMsg = tagMessage(msg);
        messager.printMessage(kind, taggedMsg);
        return reportIfError(kind);
    }

    /**
     * Adds a diagnostic of the specified kind at the location of the element,
     * returning false if the diagnostic is an error.
     */
    public boolean add(Diagnostic.Kind kind, CharSequence msg, Element e) {
        String taggedMsg = tagMessage(msg);
        messager.printMessage(kind, taggedMsg, e);
        return reportIfError(kind);
    }

    /**
     * Adds a diagnostic of the specified kind at the location of the annotation mirror of the annotated element,
     * returning false if the diagnostic is an error.
     */
    public boolean add(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
        String taggedMsg = tagMessage(msg);
        messager.printMessage(kind, taggedMsg, e, a);
        return reportIfError(kind);
    }

    /**
     * Adds a diagnostic of the specified kind at the location of the annotation value
     * inside the annotation mirror of the annotated element, returning false if the diagnostic is an error.
     */
    public boolean add(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) {
        String taggedMsg = tagMessage(msg);
        messager.printMessage(kind, taggedMsg, e, a, v);
        return reportIfError(kind);
    }

    /** Creates an error tracker. */
    public ErrorTracker trackErrors() {
        return new ErrorTracker();
    }

    /** Prepends the message with a tag. */
    private String tagMessage(CharSequence msg) {
        return String.format("[%s] %s", tag, msg);
    }

    /** If an error occurs, reports it to all error trackers, and returns false. */
    private boolean reportIfError(Diagnostic.Kind kind) {
        if (kind != Diagnostic.Kind.ERROR) {
            return true;
        }

        errorTrackers.forEach(ErrorTracker::reportError);
        return false;
    }

    /** Tracks errors, converting the final result to empty if an error occurred. */
    public class ErrorTracker implements AutoCloseable {

        private boolean validates = true;

        /** Checks that no errors occurred, returning empty otherwise. */
        public <T> Optional<T> checkNoErrors(T result) {
            return validates ? Optional.of(result) : Optional.empty();
        }

        @Override
        public void close() {
            errorTrackers.remove(this);
        }

        private ErrorTracker() {
            errorTrackers.add(this);
        }

        /** Reports an error to the tracker. */
        private void reportError() {
            validates = false;
        }
    }
}
