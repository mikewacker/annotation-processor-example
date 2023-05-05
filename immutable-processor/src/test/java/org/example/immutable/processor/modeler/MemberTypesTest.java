package org.example.immutable.processor.modeler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.testing.compile.Compilation;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.example.immutable.Immutable;
import org.example.immutable.processor.model.MemberType;
import org.example.immutable.processor.test.CompilationError;
import org.example.immutable.processor.test.TestCompiler;
import org.example.immutable.processor.test.TestResources;
import org.example.processor.base.IsolatingLiteProcessor;
import org.example.processor.base.ProcessorScope;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class MemberTypesTest {

    @Test
    public void create_TypeArray() throws Exception {
        MemberType expectedType = MemberType.of("int[][]");
        create("test/method/TypeArray.java", expectedType);
    }

    @Test
    public void create_TypeDeclared() throws Exception {
        MemberType expectedType = MemberType.of("%s", ImportableType.ofClass(String.class));
        create("test/method/TypeDeclared.java", expectedType);
    }

    @Test
    public void create_TypeDeclaredGeneric() throws Exception {
        MemberType expectedType = MemberType.of(
                "%s<%s, %s>",
                ImportableType.ofClass(Map.class),
                ImportableType.ofClass(String.class),
                ImportableType.ofClass(String.class));
        create("test/method/TypeDeclaredGeneric.java", expectedType);
    }

    @Test
    public void create_TypeDeclaredInner_GenericGeneric() throws Exception {
        MemberType expectedType = MemberType.of("%s<O>.Inner<I>", ImportableType.of("test.method.GenericGenericOuter"));
        create("test/method/TypeDeclaredInner_GenericGeneric.java", expectedType);
    }

    @Test
    public void create_TypeDeclaredInner_GenericNonGeneric() throws Exception {
        MemberType expectedType = MemberType.of("%s<O>.Inner", ImportableType.of("test.method.GenericNonGenericOuter"));
        create("test/method/TypeDeclaredInner_GenericNonGeneric.java", expectedType);
    }

    @Test
    public void create_TypeDeclaredInner_NonGenericGeneric() throws Exception {
        MemberType expectedType = MemberType.of("%s<I>", ImportableType.of("test.method.NonGenericGenericOuter$Inner"));
        create("test/method/TypeDeclaredInner_NonGenericGeneric.java", expectedType);
    }

    @Test
    public void create_TypeDeclaredInner_NonGenericNonGeneric() throws Exception {
        MemberType expectedType = MemberType.of("%s", ImportableType.of("test.method.NonGenericNonGenericOuter$Inner"));
        create("test/method/TypeDeclaredInner_NonGenericNonGeneric.java", expectedType);
    }

    @Test
    public void create_TypePrimitive() throws Exception {
        MemberType expectedType = MemberType.of("int");
        create("test/method/TypePrimitive.java", expectedType);
    }

    @Test
    public void create_TypeVariable() throws Exception {
        MemberType expectedType = MemberType.of("T");
        create("test/method/TypeVariable.java", expectedType);
    }

    @Test
    public void create_TypeWildcard() throws Exception {
        MemberType expectedType = MemberType.of("%s<?>", ImportableType.ofClass(List.class));
        create("test/method/TypeWildcard.java", expectedType);
    }

    @Test
    public void create_TypeWildcardExtends() throws Exception {
        MemberType expectedType = MemberType.of(
                "%s<? extends %s>", ImportableType.ofClass(List.class), ImportableType.ofClass(Runnable.class));
        create("test/method/TypeWildcardExtends.java", expectedType);
    }

    @Test
    public void create_TypeWildcardSuper() throws Exception {
        MemberType expectedType = MemberType.of(
                "%s<? super %s>", ImportableType.ofClass(List.class), ImportableType.ofClass(Runnable.class));
        create("test/method/TypeWildcardSuper.java", expectedType);
    }

    private void create(String sourcePath, MemberType expectedType) throws Exception {
        Compilation compilation =
                TestCompiler.create(MemberTypesTest.TestLiteProcessor.class).compile(sourcePath);
        MemberType type = TestResources.loadObjectForSource(compilation, sourcePath, new TypeReference<>() {});
        assertThat(type).isEqualTo(expectedType);
    }

    @Test
    public void error_TypeVoid() {
        error("test/method/error/TypeVoid.java", CompilationError.of(8, "[@Immutable] void type not allowed"));
    }

    @Test
    public void error_TypeError() {
        Compilation compilation = TestCompiler.create(MemberTypesTest.TestLiteProcessor.class)
                .expectingCompilationFailure()
                .expectingCompilationFailureWithoutProcessor()
                .compile("test/method/error/TypeError.java");
        assertThat(CompilationError.fromCompilation(compilation))
                .contains(CompilationError.of(8, "[@Immutable] type failed to compile"));
    }

    private void error(String sourcePath, CompilationError expectedError) {
        Compilation compilation = TestCompiler.create(MemberTypesTest.TestLiteProcessor.class)
                .expectingCompilationFailure()
                .compile(sourcePath);
        assertThat(CompilationError.fromCompilation(compilation)).containsExactlyInAnyOrder(expectedError);
    }

    @ProcessorScope
    public static final class TestLiteProcessor extends IsolatingLiteProcessor<TypeElement> {

        private final MemberTypes typeFactory;
        private final ElementNavigator navigator;
        private final Filer filer;

        @Inject
        TestLiteProcessor(MemberTypes types, ElementNavigator navigator, Filer filer) {
            super(Immutable.class);
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
