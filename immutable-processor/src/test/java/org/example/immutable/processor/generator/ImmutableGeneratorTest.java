package org.example.immutable.processor.generator;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.util.Optional;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.processor.base.ImmutableBaseLiteProcessor;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.test.TestCompiler;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.junit.jupiter.api.Test;

public final class ImmutableGeneratorTest {

    @Test
    public void generateSource_Rectangle() {
        generateSource("test/Rectangle.java", "test.ImmutableRectangle", "generated/test/ImmutableRectangle.java");
    }

    private void generateSource(String sourcePath, String generatedQualifiedName, String expectedGeneratedSourcePath) {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class).compile(sourcePath);
        assertThat(compilation)
                .generatedSourceFile(generatedQualifiedName)
                .hasSourceEquivalentTo(JavaFileObjects.forResource(expectedGeneratedSourcePath));
    }

    @ProcessorScope
    public static final class TestLiteProcessor extends ImmutableBaseLiteProcessor {

        private final ImmutableGenerator generator;

        @Inject
        TestLiteProcessor(ImmutableGenerator generator) {
            this.generator = generator;
        }

        @Override
        protected void process(TypeElement typeElement) {
            loadImmutableImpl(typeElement).ifPresent(generator::generateSource);
        }

        private Optional<ImmutableImpl> loadImmutableImpl(TypeElement typeElement) {
            String qualifiedName = typeElement.getQualifiedName().toString();
            return switch (qualifiedName) {
                case "test.Rectangle" -> Optional.of(TestImmutableImpls.rectangle());
                default -> Optional.empty();
            };
        }
    }
}
