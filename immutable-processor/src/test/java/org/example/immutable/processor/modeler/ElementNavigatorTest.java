package org.example.immutable.processor.modeler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.testing.compile.Compilation;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.example.immutable.processor.base.ImmutableBaseLiteProcessor;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.test.TestCompiler;
import org.example.immutable.processor.test.TestResources;
import org.junit.jupiter.api.Test;

public final class ElementNavigatorTest {

    @Test
    public void getMethodsToImplement_Rectangle() throws Exception {
        getMethodsToImplement("test/Rectangle.java", List.of("width", "height"));
    }

    private void getMethodsToImplement(String sourcePath, List<String> expectedMethodNames) throws Exception {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class).compile(sourcePath);
        List<String> methodNames = TestResources.loadObjectForSource(compilation, sourcePath, new TypeReference<>() {});
        assertThat(methodNames).isEqualTo(expectedMethodNames);
    }

    @ProcessorScope
    public static final class TestLiteProcessor extends ImmutableBaseLiteProcessor {

        private final ElementNavigator navigator;
        private final Elements elementUtils;
        private final Filer filer;

        @Inject
        TestLiteProcessor(ElementNavigator navigator, Elements elementUtils, Filer filer) {
            this.navigator = navigator;
            this.elementUtils = elementUtils;
            this.filer = filer;
        }

        @Override
        protected void process(TypeElement typeElement) {
            List<String> methodNames = navigator
                    .getMethodsToImplement(typeElement)
                    .map(Element::getSimpleName)
                    .map(Name::toString)
                    .toList();
            TestResources.saveObject(filer, typeElement, elementUtils, methodNames);
        }
    }
}
