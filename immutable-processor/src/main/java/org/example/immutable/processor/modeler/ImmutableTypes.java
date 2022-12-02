package org.example.immutable.processor.modeler;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.example.immutable.Immutable;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.error.Errors;
import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.NamedType;
import org.example.immutable.processor.model.TopLevelType;

/** Creates {@link ImmutableType}'s from {@link TypeElement}'s. */
@ProcessorScope
final class ImmutableTypes {

    private final NamedTypes typeFactory;
    private final Errors errorReporter;
    private final Elements elementUtils;
    private final Types typeUtils;

    @Inject
    ImmutableTypes(NamedTypes typeFactory, Errors errorReporter, Elements elementUtils, Types typeUtils) {
        this.typeFactory = typeFactory;
        this.errorReporter = errorReporter;
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
    }

    /** Creates an {@link ImmutableType}, or empty if validation fails. */
    public Optional<ImmutableType> create(TypeElement typeElement) {
        // Create and validate the raw types.
        try (Errors.Tracker errorTracker = errorReporter.createErrorTracker()) {
            Optional<NamedType> maybeRawInterfaceType = createRawInterfaceType(typeElement);
            if (maybeRawInterfaceType.isEmpty()) {
                return Optional.empty();
            }
            NamedType rawInterfaceType = maybeRawInterfaceType.get();

            TopLevelType rawImplType = createRawImplType(rawInterfaceType, typeElement);
            Set<String> packageTypes = createPackageTypes(typeElement);

            // Create and validate the types. Generics are not yet supported.
            checkDoesNotHaveTypeParameters(typeElement);
            List<String> typeVars = List.of();
            NamedType interfaceType = rawInterfaceType;
            NamedType implType = NamedType.of(rawImplType);

            // Create the immutable type.
            ImmutableType type = ImmutableType.of(rawImplType, packageTypes, typeVars, implType, interfaceType);
            return errorTracker.checkNoErrors(type);
        }
    }

    /** Creates a raw interface type from the type element. */
    private Optional<NamedType> createRawInterfaceType(TypeElement typeElement) {
        if (!checkIsInterface(typeElement)) {
            return Optional.empty();
        }

        TypeMirror rawTypeMirror = typeUtils.erasure(typeElement.asType());
        return typeFactory.create(rawTypeMirror, typeElement);
    }

    /** Creates a raw implementation type from the raw interface type. */
    private TopLevelType createRawImplType(NamedType rawInterfaceType, TypeElement sourceElement) {
        TopLevelType rawFlatInterfaceType = createRawFlatInterfaceType(rawInterfaceType, sourceElement);
        return createRawImplType(rawFlatInterfaceType, sourceElement);
    }

    /**
     * Creates a raw flat interface type from the raw interface type.
     *
     * <p>Flattening replaces a nested type with a top-level type, replacing '.' with '_'.
     * (E.g., replace "package.Outer.Inner" with "package.Outer_Inner".)</p>
     */
    private TopLevelType createRawFlatInterfaceType(NamedType rawInterfaceType, Element sourceElement) {
        // Top-level types do not require flattening.
        if (!rawInterfaceType.nameFormat().contains(".")) {
            return rawInterfaceType.args().get(0);
        }

        // Normalize the nested interface type.
        String packageName = rawInterfaceType.args().get(0).packageName();
        String simpleFlatInterfaceName = rawInterfaceType.name().replace(".", "_");
        TopLevelType rawFlatInterfaceType = TopLevelType.of(packageName, simpleFlatInterfaceName);
        checkFlatInterfaceTypeDoesNotExistAsImmutable(rawFlatInterfaceType, sourceElement);
        return rawFlatInterfaceType;
    }

    /** Creates the raw implementation type from the raw flat interface type. */
    private TopLevelType createRawImplType(TopLevelType rawFlatInterfaceType, Element sourceElement) {
        String simpleImplName = String.format("Immutable%s", rawFlatInterfaceType.simpleName());
        TopLevelType rawImplType = TopLevelType.of(rawFlatInterfaceType.packageName(), simpleImplName);
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

    /** Checks that the implementation type to be generated does not already exist. */
    private void checkImplTypeDoesNotExist(TopLevelType rawImplType, Element sourceElement) {
        String qualifiedName = rawImplType.qualifiedName();
        if (elementUtils.getTypeElement(qualifiedName) == null) {
            return;
        }

        String message = String.format("implementation type already exists: %s", qualifiedName);
        errorReporter.error(message, sourceElement);
    }

    /**
     * Checks that the flat interface type does not exist as a type annotated with {@link Immutable}.
     *
     * <p>If the flat interface type already exists and is annotated with {@link Immutable},
     * then the interface type and the flat interface type would have the same implementation type.</p>
     */
    private void checkFlatInterfaceTypeDoesNotExistAsImmutable(
            TopLevelType rawFlatInterfaceType, Element sourceElement) {
        String qualifiedName = rawFlatInterfaceType.qualifiedName();
        TypeElement flatTypeElement = elementUtils.getTypeElement(qualifiedName);
        if (flatTypeElement == null) {
            return;
        }

        Immutable immutable = flatTypeElement.getAnnotation(Immutable.class);
        if (immutable == null) {
            return;
        }

        String message = String.format("flat interface type already exists as @Immutable type: %s", qualifiedName);
        errorReporter.error(message, sourceElement);
    }

    private boolean checkDoesNotHaveTypeParameters(TypeElement typeElement) {
        return !typeElement.getTypeParameters().isEmpty()
                ? errorReporter.error("generic interfaces are not supported", typeElement)
                : true;
    }
}
