package org.example.immutable.processor.modeler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.testing.compile.Compilation;
import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.processor.base.ImmutableBaseLiteProcessor;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.model.TopLevelType;
import org.example.immutable.processor.test.TestCompiler;
import org.example.immutable.processor.test.TestResources;
import org.junit.jupiter.api.Test;

public final class TopLevelTypesTest {

    @Test
    public void create_Rectangle() throws Exception {
        TopLevelType expectedType = TopLevelType.of("test", "Rectangle");
        create("test/Rectangle.java", expectedType);
    }

    private void create(String sourcePath, TopLevelType expectedType) throws Exception {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class).compile(sourcePath);
        TopLevelType type = TestResources.loadObjectForSource(compilation, sourcePath, new TypeReference<>() {});
        assertThat(type).isEqualTo(expectedType);
    }

    @ProcessorScope
    public static final class TestLiteProcessor extends ImmutableBaseLiteProcessor {

        private final TopLevelTypes typeFactory;
        private final Filer filer;

        @Inject
        TestLiteProcessor(TopLevelTypes typeFactory, Filer filer) {
            this.typeFactory = typeFactory;
            this.filer = filer;
        }

        @Override
        protected void process(TypeElement typeElement) {
            typeFactory.create(typeElement).ifPresent(type -> TestResources.saveObject(filer, typeElement, type));
        }
    }
}