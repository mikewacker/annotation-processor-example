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
import org.example.immutable.Immutable;
import org.example.immutable.processor.test.TestCompiler;
import org.example.immutable.processor.test.TestResources;
import org.example.processor.base.IsolatingLiteProcessor;
import org.example.processor.base.ProcessorScope;
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
    public static final class TestLiteProcessor extends IsolatingLiteProcessor<TypeElement> {

        private final ElementNavigator navigator;
        private final Filer filer;

        @Inject
        TestLiteProcessor(ElementNavigator navigator, Filer filer) {
            super(Immutable.class);
            this.navigator = navigator;
            this.filer = filer;
        }

        @Override
        protected void process(TypeElement typeElement) {
            List<String> methodNames = navigator
                    .getMethodsToImplement(typeElement)
                    .map(Element::getSimpleName)
                    .map(Name::toString)
                    .toList();
            TestResources.saveObject(filer, typeElement, methodNames);
        }
    }
}
