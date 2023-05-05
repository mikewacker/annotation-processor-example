package org.example.immutable.processor.modeler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.testing.compile.Compilation;
import java.util.concurrent.Callable;
import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.Immutable;
import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.MemberType;
import org.example.immutable.processor.test.CompilationError;
import org.example.immutable.processor.test.TestCompiler;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.immutable.processor.test.TestResources;
import org.example.processor.base.IsolatingLiteProcessor;
import org.example.processor.base.ProcessorScope;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class ImmutableTypesTest {

    @Test
    public void create_Rectangle() throws Exception {
        create("test/Rectangle.java", TestImmutableImpls.rectangle().type());
    }

    @Test
    public void create_Interface() throws Exception {
        ImmutableType expectedType = ImmutableType.of(
                MemberType.declaredType(ImportableType.of("test.type.ImmutableInterface")),
                MemberType.declaredType(ImportableType.of("test.type.Interface")));
        create("test/type/Interface.java", expectedType);
    }

    @Test
    public void create_InterfaceGeneric() throws Exception {
        ImmutableType expectedType = ImmutableType.of(
                MemberType.declaredType(
                        ImportableType.of("test.type.ImmutableInterfaceGeneric"),
                        MemberType.typeParameter("T"),
                        MemberType.typeParameter("U")),
                MemberType.declaredType(
                        ImportableType.of("test.type.InterfaceGeneric"),
                        MemberType.typeVariable("T"),
                        MemberType.typeVariable("U")));
        create("test/type/InterfaceGeneric.java", expectedType);
    }

    @Test
    public void create_InterfaceGenericBounds() throws Exception {
        ImmutableType expectedType = ImmutableType.of(
                MemberType.declaredType(
                        ImportableType.of("test.type.ImmutableInterfaceGenericBounds"),
                        MemberType.typeParameter(
                                "T",
                                MemberType.declaredType(ImportableType.ofClass(Runnable.class)),
                                MemberType.declaredType(
                                        ImportableType.ofClass(Callable.class),
                                        MemberType.declaredType(ImportableType.ofClass(Void.class))))),
                MemberType.declaredType(
                        ImportableType.of("test.type.InterfaceGenericBounds"), MemberType.typeVariable("T")));
        create("test/type/InterfaceGenericBounds.java", expectedType);
    }

    @Test
    public void create_InterfaceNested() throws Exception {
        ImmutableType expectedType = ImmutableType.of(
                MemberType.declaredType(ImportableType.of("test.type.ImmutableInterfaceNested_Inner")),
                MemberType.declaredType(ImportableType.of("test.type.InterfaceNested$Inner")));
        create("test/type/InterfaceNested.java", expectedType);
    }

    private void create(String sourcePath, ImmutableType expectedType) throws Exception {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class).compile(sourcePath);
        ImmutableType type = TestResources.loadObjectForSource(compilation, sourcePath, new TypeReference<>() {});
        assertThat(type).isEqualTo(expectedType);
    }

    @Test
    public void error_Class() {
        error("test/type/error/Class.java", CompilationError.of(6, "[@Immutable] type must be an interface"));
    }

    @Test
    public void error_InterfaceNestedWithImpl() {
        error(
                "test/type/error/InterfaceNestedWithImpl.java",
                CompilationError.of(
                        8,
                        "[@Immutable] flat interface type already exists as @Immutable type: test.type.error.InterfaceNestedWithImpl_Inner"));
    }

    @Test
    public void error_InterfacePrivate() {
        error(
                "test/type/error/InterfacePrivate.java",
                CompilationError.of(8, "[@Immutable] interface must not be privately visible"));
    }

    @Test
    public void error_InterfacePrivateNested() {
        error(
                "test/type/error/InterfacePrivateNested.java",
                CompilationError.of(10, "[@Immutable] interface must not be privately visible"));
    }

    @Test
    public void error_InterfaceWithImpl() {
        error(
                "test/type/error/InterfaceWithImpl.java",
                CompilationError.of(
                        6,
                        "[@Immutable] implementation type already exists: test.type.error.ImmutableInterfaceWithImpl"));
    }

    private void error(String sourcePath, CompilationError expectedError) {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class)
                .expectingCompilationFailure()
                .compile(sourcePath);
        assertThat(CompilationError.fromCompilation(compilation)).containsExactlyInAnyOrder(expectedError);
    }

    @ProcessorScope
    public static final class TestLiteProcessor extends IsolatingLiteProcessor<TypeElement> {

        private final ImmutableTypes typeFactory;
        private final Filer filer;

        @Inject
        TestLiteProcessor(ImmutableTypes types, Filer filer) {
            super(Immutable.class);
            this.typeFactory = types;
            this.filer = filer;
        }

        @Override
        protected void process(TypeElement typeElement) {
            typeFactory.create(typeElement).ifPresent(type -> TestResources.saveObject(filer, typeElement, type));
        }
    }
}
