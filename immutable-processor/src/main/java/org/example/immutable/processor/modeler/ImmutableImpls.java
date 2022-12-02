package org.example.immutable.processor.modeler;

import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.error.Errors;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.model.ImmutableType;

/** Creates {@link ImmutableImpl}'s from {@link TypeElement}'s. */
@ProcessorScope
public final class ImmutableImpls {

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
        // Create and validate the type.
        ImmutableType type = typeFactory.create(typeElement).get();

        // Create and validate the methods.
        if (!checkDoesNotHaveMethods(typeElement)) {
            return Optional.empty();
        }

        // Create the implementation.
        ImmutableImpl impl = ImmutableImpl.of(type, List.of());
        return Optional.of(impl);
    }

    private boolean checkDoesNotHaveMethods(TypeElement typeElement) {
        return navigator.getMethodsToImplement(typeElement).findAny().isPresent()
                ? errorReporter.error("methods are not supported", typeElement)
                : true;
    }
}
