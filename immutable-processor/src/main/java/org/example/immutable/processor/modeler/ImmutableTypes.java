package org.example.immutable.processor.modeler;

import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import org.example.immutable.Immutable;
import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.MemberType;
import org.example.processor.base.ProcessorScope;
import org.example.processor.diagnostic.Diagnostics;
import org.example.processor.type.ImportableType;

/** Creates {@link ImmutableType}'s from {@link TypeElement}'s. */
@ProcessorScope
final class ImmutableTypes {

    private static final String OBJECT_CANONICAL_NAME = Object.class.getCanonicalName();

    private final MemberTypes typeFactory;
    private final Diagnostics diagnostics;
    private final Elements elementUtils;

    @Inject
    ImmutableTypes(MemberTypes typeFactory, Diagnostics diagnostics, Elements elementUtils) {
        this.typeFactory = typeFactory;
        this.diagnostics = diagnostics;
        this.elementUtils = elementUtils;
    }

    /** Creates an {@link ImmutableType}, or empty if validation fails. */
    public Optional<ImmutableType> create(TypeElement typeElement) {
        try (Diagnostics.ErrorTracker errorTracker = diagnostics.trackErrors()) {
            // Create the raw types.
            ImportableType rawInterfaceType = createRawInterfaceType(typeElement);
            ImportableType flatInterfaceType = createFlatInterfaceType(rawInterfaceType, typeElement);
            ImportableType rawImplType = createRawImplType(flatInterfaceType, typeElement);

            // Add in type parameters if present.
            List<? extends TypeParameterElement> typeParamElements = typeElement.getTypeParameters();
            List<MemberType> typeVars =
                    typeParamElements.stream().map(this::createTypeVar).toList();
            MemberType interfaceType = MemberType.declaredType(rawInterfaceType, typeVars);
            List<MemberType> typeParams =
                    typeParamElements.stream().map(this::createTypeParam).toList();
            MemberType implType = MemberType.declaredType(rawImplType, typeParams);
            ImmutableType type = ImmutableType.of(implType, interfaceType);
            return errorTracker.checkNoErrors(type);
        }
    }

    /** Creates a raw interface type from a {@link TypeElement}. */
    private ImportableType createRawInterfaceType(TypeElement typeElement) {
        checkIsInterface(typeElement);
        checkIsNotPrivate(typeElement);
        String binaryName = elementUtils.getBinaryName(typeElement).toString();
        return ImportableType.of(binaryName);
    }

    /** Flattens a nested type into a top-level type by replacing '.' with '_' in the class name. */
    private ImportableType createFlatInterfaceType(ImportableType rawInterfaceType, Element sourceElement) {
        if (rawInterfaceType.isTopLevelType()) {
            return rawInterfaceType;
        }

        String flatInterfaceClassName = rawInterfaceType.className().replace('.', '_');
        ImportableType flatInterfaceType =
                ImportableType.ofPackageAndClass(rawInterfaceType.packageName(), flatInterfaceClassName);
        checkFlatInterfaceTypeDoesNotExistAsImmutable(flatInterfaceType, sourceElement);
        return flatInterfaceType;
    }

    /** Creates a raw implementation type from the flat interface type. */
    private ImportableType createRawImplType(ImportableType flatInterfaceType, Element sourceElement) {
        String implClassName = String.format("Immutable%s", flatInterfaceType.className());
        ImportableType rawImplType = ImportableType.ofPackageAndClass(flatInterfaceType.packageName(), implClassName);
        checkImplTypeDoesNotExist(rawImplType, sourceElement);
        return rawImplType;
    }

    /** Creates a type variable from a {@link TypeParameterElement}. */
    private MemberType createTypeVar(TypeParameterElement typeParamElement) {
        String name = typeParamElement.getSimpleName().toString();
        return MemberType.typeVariable(name);
    }

    /** Creates a type parameter from a {@link TypeParameterElement}. */
    private MemberType createTypeParam(TypeParameterElement typeParamElement) {
        String name = typeParamElement.getSimpleName().toString();
        List<? extends TypeMirror> bounds = normalizeBounds(typeParamElement.getBounds());
        List<MemberType> boundModels = bounds.stream()
                .map(bound -> typeFactory.create(bound, typeParamElement))
                .flatMap(Optional::stream)
                .toList();
        return MemberType.typeParameter(name, boundModels);
    }

    /** Converts a single {@link Object} bound into an empty list. */
    private List<? extends TypeMirror> normalizeBounds(List<? extends TypeMirror> bounds) {
        if (bounds.size() > 1) {
            return bounds;
        }

        TypeMirror bound = bounds.get(0);
        return !bound.toString().equals(OBJECT_CANONICAL_NAME) ? bounds : List.of();
    }

    private boolean checkIsInterface(TypeElement typeElement) {
        return !typeElement.getKind().isInterface()
                ? diagnostics.add(Diagnostic.Kind.ERROR, "type must be an interface", typeElement)
                : true;
    }

    private boolean checkIsNotPrivate(TypeElement typeElement) {
        for (Element element = typeElement;
                element.getKind() != ElementKind.PACKAGE;
                element = element.getEnclosingElement()) {
            if (element.getModifiers().contains(Modifier.PRIVATE)) {
                return diagnostics.add(Diagnostic.Kind.ERROR, "interface must not be privately visible", typeElement);
            }
        }
        return true;
    }

    /**
     * Checks that the flat interface type does not exist as a type annotated with {@link Immutable}.
     *
     * <p>If the flat interface type already exists and is annotated with {@link Immutable},
     * then the interface type and the flat interface type would have the same implementation type.</p>
     */
    private boolean checkFlatInterfaceTypeDoesNotExistAsImmutable(
            ImportableType flatInterfaceType, Element sourceElement) {
        String qualifiedName = flatInterfaceType.qualifiedName();
        TypeElement flatTypeElement = elementUtils.getTypeElement(qualifiedName);
        if (flatTypeElement == null) {
            return true;
        }

        Immutable immutable = flatTypeElement.getAnnotation(Immutable.class);
        if (immutable == null) {
            return true;
        }

        String message = String.format("flat interface type already exists as @Immutable type: %s", qualifiedName);
        return diagnostics.add(Diagnostic.Kind.ERROR, message, sourceElement);
    }

    /** Checks that the implementation type to be generated does not already exist. */
    private boolean checkImplTypeDoesNotExist(ImportableType rawImplType, Element sourceElement) {
        String qualifiedName = rawImplType.qualifiedName();
        if (elementUtils.getTypeElement(qualifiedName) == null) {
            return true;
        }

        String message = String.format("implementation type already exists: %s", qualifiedName);
        return diagnostics.add(Diagnostic.Kind.ERROR, message, sourceElement);
    }
}
