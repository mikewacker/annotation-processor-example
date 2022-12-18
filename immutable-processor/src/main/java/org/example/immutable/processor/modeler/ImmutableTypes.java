package org.example.immutable.processor.modeler;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
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

    private static final NamedType BOUND_ERROR_TYPE = NamedType.of("?");
    private static final String OBJECT_CANONICAL_NAME = Object.class.getCanonicalName();

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
        try (Errors.Tracker errorTracker = errorReporter.createErrorTracker()) {
            // Create and validate the raw types.
            Optional<NamedType> maybeRawInterfaceType = createRawInterfaceType(typeElement);
            if (maybeRawInterfaceType.isEmpty()) {
                return Optional.empty();
            }
            NamedType rawInterfaceType = maybeRawInterfaceType.get();

            TopLevelType rawImplType = createRawImplType(rawInterfaceType, typeElement);
            Set<String> packageTypes = createPackageTypes(typeElement);

            // Create and validate the (possibly generic) types.
            List<? extends TypeParameterElement> typeParamElements = typeElement.getTypeParameters();
            List<String> typeVars = getTypeVars(typeParamElements);
            NamedType interfaceType = createInterfaceType(rawInterfaceType, typeParamElements);
            NamedType implType = createImplType(rawImplType, typeParamElements);

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

        checkIsNotPrivate(typeElement);
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
        String unqualifiedInterfaceName = rawInterfaceType.name();
        TopLevelType topLevelType = rawInterfaceType.args().get(0);
        if (!unqualifiedInterfaceName.contains(".")) {
            return topLevelType;
        }

        // Flatten the nested interface type.
        String simpleFlatInterfaceName = unqualifiedInterfaceName.replace(".", "_");
        TopLevelType rawFlatInterfaceType = TopLevelType.of(topLevelType.packageName(), simpleFlatInterfaceName);
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

    /** Gets a list of all type variables. */
    private List<String> getTypeVars(List<? extends TypeParameterElement> typeParamElements) {
        return typeParamElements.stream()
                .map(Element::getSimpleName)
                .map(Name::toString)
                .toList();
    }

    /** Creates the interface type from the raw interface type and the type parameters. */
    private NamedType createInterfaceType(
            NamedType rawInterfaceType, List<? extends TypeParameterElement> typeParamElements) {
        // Return the raw type for non-generic types.
        if (typeParamElements.isEmpty()) {
            return rawInterfaceType;
        }

        // Append type variables to the raw type.
        String typeVars = getTypeVars(typeParamElements).stream().collect(Collectors.joining(", ", "<", ">"));
        return NamedType.concat(rawInterfaceType, typeVars);
    }

    /** Creates the implementation type from the raw implementation type and the type parameters. */
    private NamedType createImplType(
            TopLevelType rawImplTopLevelType, List<? extends TypeParameterElement> typeParamElements) {
        NamedType rawImplType = NamedType.ofTopLevelType(rawImplTopLevelType);

        // Return the raw type for non-generic types.
        if (typeParamElements.isEmpty()) {
            return rawImplType;
        }

        // Append type parameters to the raw type.
        List<NamedType> typeParamTypes =
                typeParamElements.stream().map(this::createTypeParamType).toList();
        NamedType typeParamsType = NamedType.join(typeParamTypes, ", ", "<", ">");
        return NamedType.concat(rawImplType, typeParamsType);
    }

    /** Creates a type for the type parameter. */
    private NamedType createTypeParamType(TypeParameterElement typeParamElement) {
        String typeVar = typeParamElement.getSimpleName().toString();
        List<? extends TypeMirror> bounds = typeParamElement.getBounds();

        // Return the type variable for type parameters without bounds.
        if (!hasBounds(bounds)) {
            return NamedType.of(typeVar);
        }

        // Create the bounded type.
        List<NamedType> boundTypes = bounds.stream()
                .map(bound -> typeFactory.create(bound, typeParamElement))
                .map(maybeType -> maybeType.orElse(BOUND_ERROR_TYPE))
                .toList();
        String prefix = String.format("%s extends ", typeVar);
        return NamedType.join(boundTypes, " & ", prefix, "");
    }

    /** Determines if a type parameter has bounds (not including the {@link Object} bound). */
    private boolean hasBounds(List<? extends TypeMirror> bounds) {
        if (bounds.size() > 1) {
            return true;
        }

        TypeMirror bound = bounds.get(0);
        return !bound.toString().equals(OBJECT_CANONICAL_NAME);
    }

    private boolean checkIsInterface(TypeElement typeElement) {
        return !typeElement.getKind().isInterface()
                ? errorReporter.error("type must be an interface", typeElement)
                : true;
    }

    private boolean checkIsNotPrivate(TypeElement typeElement) {
        for (Element element = typeElement;
                element.getKind() != ElementKind.PACKAGE;
                element = element.getEnclosingElement()) {
            if (element.getModifiers().contains(Modifier.PRIVATE)) {
                return errorReporter.error("interface must not be privately visible", typeElement);
            }
        }
        return true;
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
}
