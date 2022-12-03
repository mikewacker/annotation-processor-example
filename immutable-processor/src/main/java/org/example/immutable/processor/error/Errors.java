package org.example.immutable.processor.error;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.processing.Messager;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import org.example.immutable.processor.base.ProcessorScope;

/**
 * Reports diagnostic errors, which result in compilation errors.
 *
 * <p>It also tracks errors, which allows processing to continue for non-fatal errors.</p>
 *
 * <p>{@link Messager} should not be used directly; this will break the error tracking.</p>
 */
@ProcessorScope
public final class Errors {

    private final Messager messager;
    private final Set<Tracker> errorTrackers = new HashSet<>();

    @Inject
    Errors(Messager messager) {
        this.messager = messager;
    }

    /** Reports an error, returning false. */
    public boolean error(String message, Element element) {
        return error(message, m -> messager.printMessage(Diagnostic.Kind.ERROR, m, element));
    }

    /** Reports an error, returning false. */
    public boolean error(String message) {
        return error(message, m -> messager.printMessage(Diagnostic.Kind.ERROR, m));
    }

    /** Reports an error, returning false. */
    private boolean error(String message, Consumer<String> messagePrinter) {
        String annotatedMessage = String.format("[@Immutable] %s", message);
        messagePrinter.accept(annotatedMessage);
        errorTrackers.forEach(Tracker::reportError);
        return false;
    }

    /** Creates an error tracker. */
    public Tracker createErrorTracker() {
        return new Tracker();
    }

    /** Tracks errors, converting the final result to empty if an error occurred. */
    public final class Tracker implements AutoCloseable {

        private boolean validates = true;

        private Tracker() {
            errorTrackers.add(this);
        }

        /** Checks that no errors occurred, returning empty otherwise. */
        public <T> Optional<T> checkNoErrors(T result) {
            return validates ? Optional.of(result) : Optional.empty();
        }

        @Override
        public void close() {
            errorTrackers.remove(this);
        }

        /** Reports an error to the tracker. */
        private void reportError() {
            validates = false;
        }
    }
}
