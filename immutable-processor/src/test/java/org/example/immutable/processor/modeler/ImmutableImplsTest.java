package org.example.immutable.processor.modeler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.immutable.processor.test.CompilationErrorsSubject.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.testing.compile.Compilation;
import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.processor.base.ImmutableBaseLiteProcessor;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.test.CompilationError;
import org.example.immutable.processor.test.TestCompiler;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.immutable.processor.test.TestResources;
import org.junit.jupiter.api.Test;

public final class ImmutableImplsTest {

    @Test
    public void create_Empty() throws Exception {
        create("test/Empty.java", TestImmutableImpls.empty());
    }

    @Test
    public void createRectangle() throws Exception {
        create("test/Rectangle.java", TestImmutableImpls.rectangle());
    }

    private void create(String sourcePath, ImmutableImpl expectedImpl) throws Exception {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class).compile(sourcePath);
        ImmutableImpl impl = TestResources.loadObjectForSource(compilation, sourcePath, new TypeReference<>() {});
        assertThat(impl).isEqualTo(expectedImpl);
    }

    @Test
    public void unsupported_ColoredRectangle() {
        error(
                "test/ColoredRectangle.java",
                CompilationError.of(14, "[@Immutable] declared types are not supported"),
                CompilationError.of(16, "[@Immutable] declared types are not supported"),
                CompilationError.of(18, "[@Immutable] declared types are not supported"));
    }

    @Test
    public void error_ImmutableType() {
        error("test/type/error/Class.java", CompilationError.of(6, "[@Immutable] type must be an interface"));
    }

    @Test
    public void error_ImmutableMember() {
        error(
                "test/method/error/MethodWithParams.java",
                CompilationError.of(8, "[@Immutable] method must not have parameters"));
    }

    private void error(String sourcePath, CompilationError... expectedErrors) {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class)
                .expectingCompilationFailure()
                .compile(sourcePath);
        assertThat(compilation.errors()).containsExactlyInAnyOrder(expectedErrors);
    }

    @ProcessorScope
    public static final class TestLiteProcessor extends ImmutableBaseLiteProcessor {

        private final ImmutableImpls implFactory;
        private final Filer filer;

        @Inject
        TestLiteProcessor(ImmutableImpls implFactory, Filer filer) {
            this.implFactory = implFactory;
            this.filer = filer;
        }

        @Override
        protected void process(TypeElement typeElement) {
            implFactory.create(typeElement).ifPresent(impl -> TestResources.saveObject(filer, typeElement, impl));
        }
    }
}
