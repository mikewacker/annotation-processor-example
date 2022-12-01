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
import javax.lang.model.util.Elements;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.NamedType;
import org.example.immutable.processor.model.TopLevelType;

/** Creates {@link ImmutableType}'s from {@link TypeElement}'s. */
@ProcessorScope
final class ImmutableTypes {

    private final TopLevelTypes typeFactory;
    private final Elements elementUtils;

    @Inject
    ImmutableTypes(TopLevelTypes typeFactory, Elements elementUtils) {
        this.typeFactory = typeFactory;
        this.elementUtils = elementUtils;
    }

    /** Creates an {@link ImmutableType}, or empty if validation fails. */
    public Optional<ImmutableType> create(TypeElement typeElement) {
        // Create and validate the raw types.
        TopLevelType rawInterfaceType = createRawInterfaceType(typeElement);
        TopLevelType rawImplType = createRawImplType(rawInterfaceType);
        Set<String> packageTypes = createPackageTypes(typeElement);

        // Create and validate the types. Generics are not yet supported.
        List<String> typeVars = List.of();
        NamedType interfaceType = NamedType.of(rawInterfaceType);
        NamedType implType = NamedType.of(rawImplType);

        // Create the immutable type.
        ImmutableType type = ImmutableType.of(rawImplType, packageTypes, typeVars, implType, interfaceType);
        return Optional.of(type);
    }

    /** Creates a raw interface type from the type element. */
    private TopLevelType createRawInterfaceType(TypeElement typeElement) {
        return typeFactory.create(typeElement).get();
    }

    /** Creates a raw implementation type from the raw interface type. */
    private TopLevelType createRawImplType(TopLevelType rawInterfaceType) {
        String simpleImplName = String.format("Immutable%s", rawInterfaceType.simpleName());
        TopLevelType rawImplType = TopLevelType.of(rawInterfaceType.packageName(), simpleImplName);
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
}
