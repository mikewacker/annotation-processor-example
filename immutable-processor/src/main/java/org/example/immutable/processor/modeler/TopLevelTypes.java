package org.example.immutable.processor.modeler;

import java.util.Optional;
import javax.inject.Inject;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.model.TopLevelType;

/** Creates {@link TopLevelType}'s from {@link TypeElement}'s. */
@ProcessorScope
final class TopLevelTypes {

    private final Elements elementUtils;

    @Inject
    TopLevelTypes(Elements elementUtils) {
        this.elementUtils = elementUtils;
    }

    /** Creates a {@link TopLevelType}, or empty if validation fails. */
    public Optional<TopLevelType> create(TypeElement typeElement) {
        PackageElement packageElement = elementUtils.getPackageOf(typeElement);
        String packageName = packageElement.getQualifiedName().toString();
        String simpleName = typeElement.getSimpleName().toString();
        TopLevelType type = TopLevelType.of(packageName, simpleName);
        return Optional.of(type);
    }
}
