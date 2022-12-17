package org.example.immutable.processor.base;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.testing.compile.Compilation;
import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.example.immutable.processor.test.TestCompiler;
import org.example.immutable.processor.test.TestResources;
import org.junit.jupiter.api.Test;

public final class ImmutableBaseLiteProcessorTest {

    @Test
    public void getQualifiedName_Rectangle() throws Exception {
        getQualifiedName("test/Rectangle.java", "test.Rectangle");
    }

    public void getQualifiedName(String sourcePath, String expectedQualifiedName) throws Exception {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class).compile(sourcePath);
        String qualifiedName = TestResources.loadObjectForSource(compilation, sourcePath, new TypeReference<>() {});
        assertThat(qualifiedName).isEqualTo(expectedQualifiedName);
    }

    @ProcessorScope
    public static final class TestLiteProcessor extends ImmutableBaseLiteProcessor {

        private final Elements elementUtils;
        private final Filer filer;

        @Inject
        TestLiteProcessor(Elements elementUtils, Filer filer) {
            this.elementUtils = elementUtils;
            this.filer = filer;
        }

        @Override
        protected void process(TypeElement typeElement) {
            String qualifiedName = typeElement.getQualifiedName().toString();
            TestResources.saveObject(filer, typeElement, elementUtils, qualifiedName);
        }
    }
}
