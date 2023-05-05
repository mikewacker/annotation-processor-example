package org.example.processor.base;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import java.io.IOException;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.tools.StandardLocation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public final class IsolatingLiteProcessorTest {

    @Test
    public void process() {
        Processor processor = TestProcessor.of(TestLiteProcessor.class);
        Compilation compilation = TestCompiler.compile(processor);
        assertThat(compilation).succeededWithoutWarnings();
        Assertions.assertThat(compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, "equals"))
                .isPresent();
        Assertions.assertThat(compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, "hashCode"))
                .isPresent();
        Assertions.assertThat(compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, "toString"))
                .isPresent();
    }

    /** Generates an empty file for each method annotated with {@link Override}. */
    public static final class TestLiteProcessor extends IsolatingLiteProcessor<ExecutableElement> {

        private final Filer filer;

        @Inject
        TestLiteProcessor(Filer filer) {
            super(Override.class);
            this.filer = filer;
        }

        @Override
        protected void process(ExecutableElement annotatedElement) throws IOException {
            String name = annotatedElement.getSimpleName().toString();
            filer.createResource(StandardLocation.SOURCE_OUTPUT, "", name);
        }
    }
}
