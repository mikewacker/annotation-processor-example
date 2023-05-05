package org.example.processor.base;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Iterables;
import com.google.testing.compile.Compilation;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.jupiter.api.Test;

public final class AdapterProcessorTest {

    @Test
    public void process() {
        Processor processor = TestProcessor.of(TestLiteProcessor.class);
        Compilation compilation = TestCompiler.compile(processor);
        assertThat(compilation).succeededWithoutWarnings();
        assertThat(compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, "equals"))
                .isPresent();
        assertThat(compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, "hashCode"))
                .isPresent();
        assertThat(compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, "toString"))
                .isPresent();
    }

    @Test
    public void handleUncaughtException() {
        Processor processor = TestProcessor.of(ErrorLiteProcessor.class);
        Compilation compilation = TestCompiler.compile(processor);
        assertThat(compilation).failed();
        assertThat(compilation.diagnostics()).hasSize(1);
        Diagnostic<? extends JavaFileObject> diagnostic = Iterables.getOnlyElement(compilation.diagnostics());
        assertThat(diagnostic.getKind()).isEqualTo(Diagnostic.Kind.ERROR);
        String message = diagnostic.getMessage(Locale.US);
        assertThat(message)
                .startsWith("Uncaught exception processing annotations in org.example.processor.base.TestProcessor:");
        assertThat(message).contains("java.lang.RuntimeException: error123");
    }

    /** Generates an empty file for each method annotated with {@link Override}. */
    public static final class TestLiteProcessor implements LiteProcessor {

        private final Filer filer;

        @Inject
        TestLiteProcessor(Filer filer) {
            this.filer = filer;
        }

        @Override
        public void process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
            Set<? extends Element> annotatedElements = getAnnotatedElements(annotations, roundEnv);
            for (Element annotatedElement : annotatedElements) {
                generateTestResource(annotatedElement);
            }
        }

        private Set<? extends Element> getAnnotatedElements(
                Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            TypeElement annotation = Iterables.getOnlyElement(annotations);
            return roundEnv.getElementsAnnotatedWith(annotation);
        }

        private void generateTestResource(Element annotatedElement) throws IOException {
            String name = annotatedElement.getSimpleName().toString();
            filer.createResource(StandardLocation.SOURCE_OUTPUT, "", name, annotatedElement);
        }
    }

    /** Lite processor with an uncaught exception. */
    public static final class ErrorLiteProcessor implements LiteProcessor {

        @Inject
        ErrorLiteProcessor() {}

        @Override
        public void process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            throw new RuntimeException("error123");
        }
    }
}
