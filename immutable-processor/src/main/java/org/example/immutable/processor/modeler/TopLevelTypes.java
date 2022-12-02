package org.example.immutable.processor.modeler;

import java.util.Optional;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.error.Errors;
import org.example.immutable.processor.model.TopLevelType;

/**
 * Creates {@link TopLevelType}'s from {@link TypeElement}'s.
 *
 * <p>The source {@link Element} is also provided for error reporting purposes.</p>
 */
@ProcessorScope
final class TopLevelTypes {

    private final Errors errorReporter;
    private final Elements elementUtils;

    @Inject
    TopLevelTypes(Errors errorReporter, Elements elementUtils) {
        this.errorReporter = errorReporter;
        this.elementUtils = elementUtils;
    }

    /** Creates a {@link TopLevelType}, or empty if validation fails. */
    public Optional<TopLevelType> create(TypeElement typeElement, Element sourceElement) {
        if (!checkHasPackage(typeElement, sourceElement) || !checkIsTopLevelType(typeElement, sourceElement)) {
            return Optional.empty();
        }

        PackageElement packageElement = elementUtils.getPackageOf(typeElement);
        String packageName = packageElement.getQualifiedName().toString();
        String simpleName = typeElement.getSimpleName().toString();
        TopLevelType type = TopLevelType.of(packageName, simpleName);
        return Optional.of(type);
    }

    private boolean checkHasPackage(TypeElement typeElement, Element sourceElement) {
        return elementUtils.getPackageOf(typeElement).isUnnamed()
                ? errorReporter.error("type must have a package", sourceElement)
                : true;
    }

    private boolean checkIsTopLevelType(TypeElement typeElement, Element sourceElement) {
        return (typeElement.getEnclosingElement().getKind() != ElementKind.PACKAGE)
                ? errorReporter.error("precondition: type is not a top-level type", sourceElement)
                : true;
    }
}
