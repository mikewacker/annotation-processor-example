package org.example.immutable.processor.modeler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.immutable.processor.test.CompilationErrorsSubject.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.testing.compile.Compilation;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.processor.base.ImmutableBaseLiteProcessor;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.NamedType;
import org.example.immutable.processor.model.TopLevelType;
import org.example.immutable.processor.test.CompilationError;
import org.example.immutable.processor.test.TestCompiler;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.immutable.processor.test.TestResources;
import org.junit.jupiter.api.Test;

public final class ImmutableTypesTest {

    @Test
    public void create_Rectangle() throws Exception {
        create("test/Rectangle.java", TestImmutableImpls.rectangle().type());
    }

    @Test
    public void create_Interface() throws Exception {
        TopLevelType rawImplType = TopLevelType.of("test.type", "ImmutableInterface");
        TopLevelType rawInterfaceType = TopLevelType.of("test.type", "Interface");
        List<String> typeVars = List.of();
        NamedType implType = NamedType.of(rawImplType);
        NamedType interfaceType = NamedType.of(rawInterfaceType);
        ImmutableType expectedType = ImmutableType.of(rawImplType, Set.of(), typeVars, implType, interfaceType);
        create("test/type/Interface.java", expectedType);
    }

    private void create(String sourcePath, ImmutableType expectedType) throws Exception {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class).compile(sourcePath);
        ImmutableType type = TestResources.loadObjectForSource(compilation, sourcePath, new TypeReference<>() {});
        assertThat(normalizeType(type)).isEqualTo(normalizeType(expectedType));
    }

    @Test
    public void unsupported_InterfaceGeneric() {
        error(
                "test/type/unsupported/InterfaceGeneric.java",
                CompilationError.of(6, "[@Immutable] generic interfaces are not supported"));
    }

    @Test
    public void unsupported_InterfaceGenericBounds() {
        error(
                "test/type/unsupported/InterfaceGenericBounds.java",
                CompilationError.of(8, "[@Immutable] generic interfaces are not supported"));
    }

    @Test
    public void unsupported_Nested() {
        error(
                "test/type/unsupported/InterfaceNested.java",
                CompilationError.of(8, "[@Immutable] nested interfaces are not supported"));
    }

    @Test
    public void error_Class() {
        error("test/type/error/Class.java", CompilationError.of(6, "[@Immutable] type must be an interface"));
    }

    @Test
    public void error_InterfaceNestedWithImpl() {
        error(
                "test/type/error/InterfaceNestedWithImpl.java",
                CompilationError.of(8, "[@Immutable] nested interfaces are not supported"));
    }

    @Test
    public void error_InterfaceWithoutPackage() {
        error(
                "test/type/error/InterfaceWithoutPackage.java",
                CompilationError.of(4, "[@Immutable] type must have a package"));
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
        assertThat(compilation.errors()).containsExactlyInAnyOrder(expectedError);
    }

    /** Empties the package types for comparison purposes. */
    private ImmutableType normalizeType(ImmutableType type) {
        return ImmutableType.of(type.rawImplType(), Set.of(), type.typeVars(), type.implType(), type.interfaceType());
    }

    @ProcessorScope
    public static final class TestLiteProcessor extends ImmutableBaseLiteProcessor {

        private final ImmutableTypes typeFactory;
        private final Filer filer;

        @Inject
        TestLiteProcessor(ImmutableTypes types, Filer filer) {
            this.typeFactory = types;
            this.filer = filer;
        }

        @Override
        protected void process(TypeElement typeElement) {
            typeFactory.create(typeElement).ifPresent(type -> TestResources.saveObject(filer, typeElement, type));
        }
    }
}
