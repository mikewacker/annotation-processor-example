package org.example.immutable.processor.modeler;

import java.util.List;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.example.processor.base.ProcessorScope;

/** Navigates immutable types to extract the relevant elements. */
@ProcessorScope
final class ElementNavigator {

    private static final String OBJECT_CANONICAL_NAME = Object.class.getCanonicalName();

    private final Elements elementUtils;

    @Inject
    ElementNavigator(Elements elementUtils) {
        this.elementUtils = elementUtils;
    }

    /** Gets all methods that must be implemented. */
    public Stream<ExecutableElement> getMethodsToImplement(TypeElement typeElement) {
        List<? extends Element> memberElements = elementUtils.getAllMembers(typeElement);
        List<ExecutableElement> methodElements = ElementFilter.methodsIn(memberElements);
        return methodElements.stream()
                .filter(this::isNotBuiltInMethod)
                .filter(this::isInstanceMethod)
                .filter(this::isNotDefaultMethod);
    }

    private boolean isNotBuiltInMethod(ExecutableElement methodElement) {
        TypeElement typeElement = (TypeElement) methodElement.getEnclosingElement();
        return !typeElement.getQualifiedName().contentEquals(OBJECT_CANONICAL_NAME);
    }

    private boolean isInstanceMethod(ExecutableElement methodElement) {
        return !methodElement.getModifiers().contains(Modifier.STATIC);
    }

    private boolean isNotDefaultMethod(ExecutableElement methodElement) {
        return !methodElement.getModifiers().contains(Modifier.DEFAULT);
    }
}
