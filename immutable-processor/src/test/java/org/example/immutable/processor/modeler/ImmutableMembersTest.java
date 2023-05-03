package org.example.immutable.processor.modeler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.testing.compile.Compilation;
import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.example.immutable.processor.base.ImmutableBaseLiteProcessor;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.model.ImmutableMember;
import org.example.immutable.processor.test.CompilationError;
import org.example.immutable.processor.test.TestCompiler;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.immutable.processor.test.TestResources;
import org.junit.jupiter.api.Test;

public final class ImmutableMembersTest {

    @Test
    public void create_Rectangle() throws Exception {
        ImmutableMember expectedMember =
                TestImmutableImpls.rectangle().members().get(0);
        create("test/Rectangle.java", expectedMember);
    }

    private void create(String sourcePath, ImmutableMember expectedMember) throws Exception {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class).compile(sourcePath);
        ImmutableMember member = TestResources.loadObjectForSource(compilation, sourcePath, new TypeReference<>() {});
        assertThat(member).isEqualTo(expectedMember);
    }

    @Test
    public void error_MethodWithParams() {
        error(
                "test/method/error/MethodWithParams.java",
                CompilationError.of(8, "[@Immutable] method must not have parameters"));
    }

    @Test
    public void error_MethodGeneric() {
        error(
                "test/method/error/MethodGeneric.java",
                CompilationError.of(8, "[@Immutable] method must not have type parameters"));
    }

    @Test
    public void error_TypeVoid() {
        error("test/method/error/TypeVoid.java", CompilationError.of(8, "[@Immutable] void type not allowed"));
    }

    private void error(String sourcePath, CompilationError expectedError) {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class)
                .expectingCompilationFailure()
                .compile(sourcePath);
        assertThat(CompilationError.fromCompilation(compilation)).containsExactlyInAnyOrder(expectedError);
    }

    @ProcessorScope
    public static final class TestLiteProcessor extends ImmutableBaseLiteProcessor {

        private final ImmutableMembers memberFactory;
        private final ElementNavigator navigator;
        private final Filer filer;

        @Inject
        TestLiteProcessor(ImmutableMembers memberFactory, ElementNavigator navigator, Filer filer) {
            this.memberFactory = memberFactory;
            this.navigator = navigator;
            this.filer = filer;
        }

        @Override
        protected void process(TypeElement typeElement) {
            ExecutableElement methodElement =
                    navigator.getMethodsToImplement(typeElement).findFirst().get();
            memberFactory
                    .create(methodElement)
                    .ifPresent(member -> TestResources.saveObject(filer, typeElement, member));
        }
    }
}
