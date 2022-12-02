package org.example.immutable.processor.modeler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.testing.compile.Compilation;
import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.example.immutable.processor.base.ImmutableBaseLiteProcessor;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.model.NamedType;
import org.example.immutable.processor.test.CompilationError;
import org.example.immutable.processor.test.CompilationErrorsSubject;
import org.example.immutable.processor.test.TestCompiler;
import org.example.immutable.processor.test.TestResources;
import org.junit.jupiter.api.Test;

public final class NamedTypesTest {

    @Test
    public void create_TypeArray() throws Exception {
        NamedType expectedType = NamedType.of("int[][]");
        create("test/method/TypeArray.java", expectedType);
    }

    @Test
    public void create_TypePrimitive() throws Exception {
        NamedType expectedType = NamedType.of("int");
        create("test/method/TypePrimitive.java", expectedType);
    }

    @Test
    public void create_TypeVariable() throws Exception {
        NamedType expectedType = NamedType.of("T");
        create("test/method/TypeVariable.java", expectedType);
    }

    private void create(String sourcePath, NamedType expectedType) throws Exception {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class).compile(sourcePath);
        NamedType type = TestResources.loadObjectForSource(compilation, sourcePath, new TypeReference<>() {});
        assertThat(type).isEqualTo(expectedType);
    }

    @Test
    public void unsupported_TypeDeclared() {
        error(
                "test/method/unsupported/TypeDeclared.java",
                CompilationError.of(8, "[@Immutable] declared types are not supported"));
    }

    @Test
    public void unsupported_TypeDeclaredGeneric() {
        error(
                "test/method/unsupported/TypeDeclaredGeneric.java",
                CompilationError.of(9, "[@Immutable] declared types are not supported"));
    }

    @Test
    public void unsupported_TypeDeclaredNested() {
        error(
                "test/method/unsupported/TypeDeclaredNested.java",
                CompilationError.of(8, "[@Immutable] declared types are not supported"));
    }

    @Test
    public void unsupported_TypeDeclaredNestedGenericInstance() {
        error(
                "test/method/unsupported/TypeDeclaredNestedGenericInstance.java",
                CompilationError.of(8, "[@Immutable] declared types are not supported"));
    }

    @Test
    public void unsupported_TypeDeclaredNestedGenericStatic() {
        error(
                "test/method/unsupported/TypeDeclaredNestedGenericStatic.java",
                CompilationError.of(9, "[@Immutable] declared types are not supported"));
    }

    @Test
    public void unsupported_TypeDeclaredPathological() {
        error(
                "test/method/unsupported/TypeDeclaredPathological.java",
                CompilationError.of(8, "[@Immutable] declared types are not supported"));
    }

    @Test
    public void unsupported_TypeWildcard() {
        error(
                "test/method/unsupported/TypeWildcard.java",
                CompilationError.of(9, "[@Immutable] declared types are not supported"));
    }

    @Test
    public void unsupported_TypeWildcardExtends() {
        error(
                "test/method/unsupported/TypeWildcardExtends.java",
                CompilationError.of(9, "[@Immutable] declared types are not supported"));
    }

    @Test
    public void unsupported_TypeWildcardSuper() {
        error(
                "test/method/unsupported/TypeWildcardSuper.java",
                CompilationError.of(9, "[@Immutable] declared types are not supported"));
    }

    @Test
    public void error_TypeError() {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class)
                .expectingCompilationFailure()
                .expectingCompilationFailureWithoutProcessor()
                .compile("test/method/error/TypeError.java");
        CompilationErrorsSubject.assertThat(compilation.errors())
                .contains(CompilationError.of(8, "[@Immutable] type failed to compile"));
    }

    @Test
    public void error_TypeVoid() {
        error("test/method/error/TypeVoid.java", CompilationError.of(8, "[@Immutable] void type not allowed"));
    }

    private void error(String sourcePath, CompilationError expectedError) {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class)
                .expectingCompilationFailure()
                .compile(sourcePath);
        CompilationErrorsSubject.assertThat(compilation.errors()).containsExactlyInAnyOrder(expectedError);
    }

    @ProcessorScope
    public static final class TestLiteProcessor extends ImmutableBaseLiteProcessor {

        private final NamedTypes typeFactory;
        private final ElementNavigator navigator;
        private final Filer filer;

        @Inject
        TestLiteProcessor(NamedTypes types, ElementNavigator navigator, Filer filer) {
            this.typeFactory = types;
            this.navigator = navigator;
            this.filer = filer;
        }

        @Override
        protected void process(TypeElement typeElement) {
            ExecutableElement sourceElement =
                    navigator.getMethodsToImplement(typeElement).findFirst().get();
            TypeMirror returnType = sourceElement.getReturnType();
            typeFactory
                    .create(returnType, sourceElement)
                    .ifPresent(type -> TestResources.saveObject(filer, typeElement, type));
        }
    }
}
