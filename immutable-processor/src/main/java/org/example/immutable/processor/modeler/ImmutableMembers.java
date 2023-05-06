package org.example.immutable.processor.modeler;

import java.util.Optional;
import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import org.example.immutable.processor.model.ImmutableMember;
import org.example.immutable.processor.model.MemberType;
import org.example.processor.base.ProcessorScope;
import org.example.processor.diagnostic.Diagnostics;

/** Creates {@link ImmutableMember}'s from {@link ExecutableElement}'s. */
@ProcessorScope
final class ImmutableMembers {

    private final MemberTypes typeFactory;
    private final Diagnostics diagnostics;

    @Inject
    ImmutableMembers(MemberTypes typeFactory, Diagnostics diagnostics) {
        this.typeFactory = typeFactory;
        this.diagnostics = diagnostics;
    }

    /** Creates an {@link ImmutableMember}, or empty if validation fails. */
    public Optional<ImmutableMember> create(ExecutableElement methodElement) {
        try (Diagnostics.ErrorTracker errorTracker = diagnostics.trackErrors()) {
            // Examine the signature.
            checkDoesNotHaveParameters(methodElement);
            checkDoesNotHaveTypeParameters(methodElement);
            TypeMirror returnType = methodElement.getReturnType();

            // Create the member.
            String name = methodElement.getSimpleName().toString();
            MemberType typeModel = typeFactory.create(returnType, methodElement).orElse(MemberTypes.ERROR_TYPE);
            ImmutableMember member = ImmutableMember.of(name, typeModel);
            return errorTracker.checkNoErrors(member);
        }
    }

    private boolean checkDoesNotHaveParameters(ExecutableElement methodElement) {
        return !methodElement.getParameters().isEmpty()
                ? diagnostics.add(Diagnostic.Kind.ERROR, "method must not have parameters", methodElement)
                : true;
    }

    private boolean checkDoesNotHaveTypeParameters(ExecutableElement methodElement) {
        return !methodElement.getTypeParameters().isEmpty()
                ? diagnostics.add(Diagnostic.Kind.ERROR, "method must not have type parameters", methodElement)
                : true;
    }
}
