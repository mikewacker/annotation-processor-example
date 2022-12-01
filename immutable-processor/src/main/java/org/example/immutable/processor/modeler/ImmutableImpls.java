package org.example.immutable.processor.modeler;

import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.model.ImmutableType;

/** Creates {@link ImmutableImpl}'s from {@link TypeElement}'s. */
@ProcessorScope
public final class ImmutableImpls {

    private final ImmutableTypes typeFactory;
    private final ElementNavigator navigator;

    @Inject
    ImmutableImpls(ImmutableTypes typeFactory, ElementNavigator navigator) {
        this.typeFactory = typeFactory;
        this.navigator = navigator;
    }

    /** Creates an {@link ImmutableImpl}, or empty if validation fails. */
    public Optional<ImmutableImpl> create(TypeElement typeElement) {
        // Create and validate the type.
        ImmutableType type = typeFactory.create(typeElement).get();

        // Create and validate the methods.
        if (navigator.getMethodsToImplement(typeElement).findAny().isPresent()) {
            return Optional.empty();
        }

        // Create the implementation.
        ImmutableImpl impl = ImmutableImpl.of(type, List.of());
        return Optional.of(impl);
    }
}
