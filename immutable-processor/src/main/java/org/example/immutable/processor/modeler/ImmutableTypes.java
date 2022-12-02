package org.example.immutable.processor.modeler;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.error.Errors;
import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.NamedType;
import org.example.immutable.processor.model.TopLevelType;

/** Creates {@link ImmutableType}'s from {@link TypeElement}'s. */
@ProcessorScope
final class ImmutableTypes {

    private final TopLevelTypes typeFactory;
    private final Errors errorReporter;
    private final Elements elementUtils;

    @Inject
    ImmutableTypes(TopLevelTypes typeFactory, Errors errorReporter, Elements elementUtils) {
        this.typeFactory = typeFactory;
        this.errorReporter = errorReporter;
        this.elementUtils = elementUtils;
    }

    /** Creates an {@link ImmutableType}, or empty if validation fails. */
    public Optional<ImmutableType> create(TypeElement typeElement) {
        // Create and validate the raw types.
        try (Errors.Tracker errorTracker = errorReporter.createErrorTracker()) {
            Optional<TopLevelType> maybeRawInterfaceType = createRawInterfaceType(typeElement);
            if (maybeRawInterfaceType.isEmpty()) {
                return Optional.empty();
            }
            TopLevelType rawInterfaceType = maybeRawInterfaceType.get();

            TopLevelType rawImplType = createRawImplType(rawInterfaceType, typeElement);
            Set<String> packageTypes = createPackageTypes(typeElement);

            // Create and validate the types. Generics are not yet supported.
            checkDoesNotHaveTypeParameters(typeElement);
            List<String> typeVars = List.of();
            NamedType interfaceType = NamedType.of(rawInterfaceType);
            NamedType implType = NamedType.of(rawImplType);

            // Create the immutable type.
            ImmutableType type = ImmutableType.of(rawImplType, packageTypes, typeVars, implType, interfaceType);
            return errorTracker.checkNoErrors(type);
        }
    }

    /** Creates a raw interface type from the type element. */
    private Optional<TopLevelType> createRawInterfaceType(TypeElement typeElement) {
        if (!checkIsInterface(typeElement) || !checkIsTopLevelInterface(typeElement)) {
            return Optional.empty();
        }

        return typeFactory.create(typeElement, typeElement);
    }

    /** Creates a raw implementation type from the raw interface type. */
    private TopLevelType createRawImplType(TopLevelType rawInterfaceType, TypeElement sourceElement) {
        String simpleImplName = String.format("Immutable%s", rawInterfaceType.simpleName());
        TopLevelType rawImplType = TopLevelType.of(rawInterfaceType.packageName(), simpleImplName);
        checkImplTypeDoesNotExist(rawImplType, sourceElement);
        return rawImplType;
    }

    /** Creates a list of top-level types in the same package as this type. */
    private Set<String> createPackageTypes(TypeElement typeElement) {
        PackageElement packageElement = elementUtils.getPackageOf(typeElement);
        return packageElement.getEnclosedElements().stream()
                .map(Element::getSimpleName)
                .map(Name::toString)
                .collect(Collectors.toSet());
    }

    private boolean checkIsInterface(TypeElement typeElement) {
        return !typeElement.getKind().isInterface()
                ? errorReporter.error("type must be an interface", typeElement)
                : true;
    }

    private boolean checkIsTopLevelInterface(TypeElement typeElement) {
        return (typeElement.getEnclosingElement().getKind() != ElementKind.PACKAGE)
                ? errorReporter.error("nested interfaces are not supported", typeElement)
                : true;
    }

    /** Checks that the implementation type to be generated does not already exist. */
    private void checkImplTypeDoesNotExist(TopLevelType rawImplType, Element sourceElement) {
        String qualifiedName = rawImplType.qualifiedName();
        if (elementUtils.getTypeElement(qualifiedName) == null) {
            return;
        }

        String message = String.format("implementation type already exists: %s", qualifiedName);
        errorReporter.error(message, sourceElement);
    }

    private boolean checkDoesNotHaveTypeParameters(TypeElement typeElement) {
        return !typeElement.getTypeParameters().isEmpty()
                ? errorReporter.error("generic interfaces are not supported", typeElement)
                : true;
    }
}
