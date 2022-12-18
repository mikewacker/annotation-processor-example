package org.example.immutable.processor.modeler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.testing.compile.Compilation;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
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
        NamedType implType = NamedType.ofTopLevelType(rawImplType);
        NamedType interfaceType = NamedType.ofTopLevelType(rawInterfaceType);
        ImmutableType expectedType = ImmutableType.of(rawImplType, Set.of(), typeVars, implType, interfaceType);
        create("test/type/Interface.java", expectedType);
    }

    @Test
    public void create_InterfaceGeneric() throws Exception {
        TopLevelType rawImplType = TopLevelType.of("test.type", "ImmutableInterfaceGeneric");
        TopLevelType rawInterfaceType = TopLevelType.of("test.type", "InterfaceGeneric");
        List<String> typeVars = List.of("T", "U");
        NamedType implType = NamedType.of("%s<T, U>", rawImplType);
        NamedType interfaceType = NamedType.of("%s<T, U>", rawInterfaceType);
        ImmutableType expectedType = ImmutableType.of(rawImplType, Set.of(), typeVars, implType, interfaceType);
        create("test/type/InterfaceGeneric.java", expectedType);
    }

    @Test
    public void create_InterfaceGenericBounds() throws Exception {
        TopLevelType rawImplType = TopLevelType.of("test.type", "ImmutableInterfaceGenericBounds");
        TopLevelType rawInterfaceType = TopLevelType.of("test.type", "InterfaceGenericBounds");
        List<String> typeVars = List.of("T");
        NamedType implType = NamedType.of(
                "%s<T extends %s & %s<%s>>",
                rawImplType,
                TopLevelType.ofClass(Runnable.class),
                TopLevelType.ofClass(Callable.class),
                TopLevelType.ofClass(Void.class));
        NamedType interfaceType = NamedType.of("%s<T>", rawInterfaceType);
        ImmutableType expectedType = ImmutableType.of(rawImplType, Set.of(), typeVars, implType, interfaceType);
        create("test/type/InterfaceGenericBounds.java", expectedType);
    }

    @Test
    public void create_InterfaceNested() throws Exception {
        TopLevelType rawImplType = TopLevelType.of("test.type", "ImmutableInterfaceNested_Inner");
        TopLevelType topLevelInterfaceType = TopLevelType.of("test.type", "InterfaceNested");
        List<String> typeVars = List.of();
        NamedType implType = NamedType.ofTopLevelType(rawImplType);
        NamedType interfaceType = NamedType.of("%s.Inner", topLevelInterfaceType);
        ImmutableType expectedType = ImmutableType.of(rawImplType, Set.of(), typeVars, implType, interfaceType);
        create("test/type/InterfaceNested.java", "test/type/InterfaceNested$Inner.json", expectedType);
    }

    @Test
    public void create_InterfaceWithoutPackage() throws Exception {
        TopLevelType rawImplType = TopLevelType.of("", "ImmutableInterfaceWithoutPackage");
        TopLevelType rawInterfaceType = TopLevelType.of("", "InterfaceWithoutPackage");
        List<String> typeVars = List.of();
        NamedType implType = NamedType.ofTopLevelType(rawImplType);
        NamedType interfaceType = NamedType.ofTopLevelType(rawInterfaceType);
        ImmutableType expectedType = ImmutableType.of(rawImplType, Set.of(), typeVars, implType, interfaceType);
        create("InterfaceWithoutPackage.java", expectedType);
    }

    private void create(String sourcePath, ImmutableType expectedType) throws Exception {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class).compile(sourcePath);
        ImmutableType type = TestResources.loadObjectForSource(compilation, sourcePath, new TypeReference<>() {});
        assertThat(normalizeType(type)).isEqualTo(normalizeType(expectedType));
    }

    private void create(String sourcePath, String resourcePath, ImmutableType expectedType) throws Exception {
        Compilation compilation = TestCompiler.create(TestLiteProcessor.class).compile(sourcePath);
        ImmutableType type =
                TestResources.loadObjectFromGeneratedResource(compilation, resourcePath, new TypeReference<>() {});
        assertThat(normalizeType(type)).isEqualTo(normalizeType(expectedType));
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

    /** Empties the package types for comparison purposes. */
    private ImmutableType normalizeType(ImmutableType type) {
        return ImmutableType.of(type.rawImplType(), Set.of(), type.typeVars(), type.implType(), type.interfaceType());
    }

    @ProcessorScope
    public static final class TestLiteProcessor extends ImmutableBaseLiteProcessor {

        private final ImmutableTypes typeFactory;
        private final Elements elementUtils;
        private final Filer filer;

        @Inject
        TestLiteProcessor(ImmutableTypes types, Elements elementUtils, Filer filer) {
            this.typeFactory = types;
            this.elementUtils = elementUtils;
            this.filer = filer;
        }

        @Override
        protected void process(TypeElement typeElement) {
            typeFactory
                    .create(typeElement)
                    .ifPresent(type -> TestResources.saveObject(filer, typeElement, elementUtils, type));
        }
    }
}
