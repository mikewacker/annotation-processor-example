package org.example.immutable.processor.modeler;

import java.util.Optional;
import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.error.Errors;
import org.example.immutable.processor.model.ImmutableMember;
import org.example.immutable.processor.model.NamedType;

/** Creates {@link ImmutableMember}'s from {@link ExecutableElement}'s. */
@ProcessorScope
final class ImmutableMembers {

    private static final NamedType ERROR_TYPE = NamedType.of("?");

    private final NamedTypes typeFactory;
    private final Errors errorReporter;

    @Inject
    ImmutableMembers(NamedTypes typeFactory, Errors errorReporter) {
        this.typeFactory = typeFactory;
        this.errorReporter = errorReporter;
    }

    /** Creates an {@link ImmutableMember}, or empty if validation fails. */
    public Optional<ImmutableMember> create(ExecutableElement methodElement) {
        try (Errors.Tracker errorTracker = errorReporter.createErrorTracker()) {
            // Examine the signature.
            checkDoesNotHaveParameters(methodElement);
            checkDoesNotHaveTypeParameters(methodElement);
            TypeMirror returnType = methodElement.getReturnType();

            // Create the member.
            String name = methodElement.getSimpleName().toString();
            NamedType typeModel = typeFactory.create(returnType, methodElement).orElse(ERROR_TYPE);
            ImmutableMember member = ImmutableMember.of(name, typeModel);
            return errorTracker.checkNoErrors(member);
        }
    }

    private boolean checkDoesNotHaveParameters(ExecutableElement methodElement) {
        return !methodElement.getParameters().isEmpty()
                ? errorReporter.error("method must not have parameters", methodElement)
                : true;
    }

    private boolean checkDoesNotHaveTypeParameters(ExecutableElement methodElement) {
        return !methodElement.getTypeParameters().isEmpty()
                ? errorReporter.error("method must not have type parameters", methodElement)
                : true;
    }
}
