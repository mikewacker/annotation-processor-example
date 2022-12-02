package org.example.immutable.processor.modeler;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.error.Errors;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.NamedType;
import org.example.immutable.processor.model.TopLevelType;

/** Creates {@link ImmutableImpl}'s from {@link TypeElement}'s. */
@ProcessorScope
public final class ImmutableImpls {

    private static final ImmutableType ERROR_TYPE =
            ImmutableType.of(TopLevelType.of("?", "?"), Set.of(), List.of(), NamedType.of("?"), NamedType.of("?"));

    private final ImmutableTypes typeFactory;
    private final ElementNavigator navigator;
    private final Errors errorReporter;

    @Inject
    ImmutableImpls(ImmutableTypes typeFactory, ElementNavigator navigator, Errors errorReporter) {
        this.typeFactory = typeFactory;
        this.navigator = navigator;
        this.errorReporter = errorReporter;
    }

    /** Creates an {@link ImmutableImpl}, or empty if validation fails. */
    public Optional<ImmutableImpl> create(TypeElement typeElement) {
        try (Errors.Tracker errorTracker = errorReporter.createErrorTracker()) {
            // Create and validate the type.
            ImmutableType type = typeFactory.create(typeElement).orElse(ERROR_TYPE);

            // Create and validate the methods.
            checkDoesNotHaveMethods(typeElement);

            // Create the implementation.
            ImmutableImpl impl = ImmutableImpl.of(type, List.of());
            return errorTracker.checkNoErrors(impl);
        }
    }

    private boolean checkDoesNotHaveMethods(TypeElement typeElement) {
        return navigator.getMethodsToImplement(typeElement).findAny().isPresent()
                ? errorReporter.error("methods are not supported", typeElement)
                : true;
    }
}
