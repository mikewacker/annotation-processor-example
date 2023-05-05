package org.example.immutable.processor.generator;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.util.Optional;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.Immutable;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.test.TestCompiler;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.processor.base.IsolatingLiteProcessor;
import org.example.processor.base.ProcessorScope;
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
    public static final class TestLiteProcessor extends IsolatingLiteProcessor<TypeElement> {

        private final ImmutableGenerator generator;

        @Inject
        TestLiteProcessor(ImmutableGenerator generator) {
            super(Immutable.class);
            this.generator = generator;
        }

        @Override
        protected void process(TypeElement typeElement) throws IOException {
            Optional<ImmutableImpl> maybeImpl = loadImmutableImpl(typeElement);
            if (maybeImpl.isEmpty()) {
                return;
            }
            ImmutableImpl impl = maybeImpl.get();
            generator.generateSource(impl, typeElement);
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
