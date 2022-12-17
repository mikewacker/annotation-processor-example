package org.example.immutable.processor.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.immutable.processor.test.TestResources;
import org.junit.jupiter.api.Test;

public final class TypeQualifierTest {

    @Test
    public void importedTypes_Order() {
        TypeQualifier typeQualifier = TypeQualifier.of(
                "test",
                Set.of(),
                Set.of(),
                Set.of(
                        TopLevelType.ofClass(Generated.class),
                        TopLevelType.ofClass(List.class),
                        TopLevelType.ofClass(Callable.class)));
        assertThat(typeQualifier.importedTypes())
                .containsExactly(
                        TopLevelType.ofClass(List.class),
                        TopLevelType.ofClass(Callable.class),
                        TopLevelType.ofClass(Generated.class));
    }

    @Test
    public void importedTypes_DoNotImportImplicitlyImportedTypes() {
        TypeQualifier typeQualifier = TypeQualifier.of(
                "test",
                Set.of(),
                Set.of(),
                Set.of(
                        TopLevelType.ofClass(String.class),
                        TopLevelType.ofClass(List.class),
                        TopLevelType.of("test", "Rectangle")));
        assertThat(typeQualifier.importedTypes()).containsExactlyInAnyOrder(TopLevelType.ofClass(List.class));
    }

    @Test
    public void importedTypes_DoNotImportQualifiedTypes() {
        TypeQualifier typeQualifier = TypeQualifier.of(
                "test",
                Set.of(),
                Set.of(),
                Set.of(TopLevelType.of("test.sub1", "Type"), TopLevelType.of("test.sub2", "Type")));
        assertThat(typeQualifier.importedTypes()).isEmpty();
    }

    @Test
    public void qualifiedTypes_TypeConflictsWithTypeNotInSourcePackage() {
        TypeQualifier typeQualifier = TypeQualifier.of(
                "test",
                Set.of(),
                Set.of(),
                Set.of(
                        TopLevelType.ofClass(List.class),
                        TopLevelType.of("test.sub1", "Type"),
                        TopLevelType.of("test.sub2", "Type")));
        assertThat(typeQualifier.qualifiedTypes())
                .containsExactlyInAnyOrder(TopLevelType.of("test.sub1", "Type"), TopLevelType.of("test.sub2", "Type"));
    }

    @Test
    public void qualifiedTypes_TypeConflictsWithTypeInSourcePackage() {
        TypeQualifier typeQualifier = TypeQualifier.of(
                "test", Set.of(), Set.of(), Set.of(TopLevelType.ofClass(List.class), TopLevelType.of("test", "List")));
        assertThat(typeQualifier.qualifiedTypes()).containsExactlyInAnyOrder(TopLevelType.ofClass(List.class));
    }

    @Test
    public void qualifiedTypes_TypeNotInSourcePackageConflictsWithTypeVariable() {
        TypeQualifier typeQualifier =
                TypeQualifier.of("test", Set.of(), Set.of("String"), Set.of(TopLevelType.ofClass(String.class)));
        assertThat(typeQualifier.qualifiedTypes()).containsExactlyInAnyOrder(TopLevelType.ofClass(String.class));
    }

    @Test
    public void qualifiedTypes_TypeInSourcePackageConflictsWithTypeVariable() {
        TypeQualifier typeQualifier =
                TypeQualifier.of("test", Set.of(), Set.of("T"), Set.of(TopLevelType.of("Type", "T")));
        assertThat(typeQualifier.qualifiedTypes()).containsExactlyInAnyOrder(TopLevelType.of("Type", "T"));
    }

    @Test
    public void qualifiedTypes_JavaLangTypeConflictsWithTypeInSourcePackage() {
        TypeQualifier typeQualifier =
                TypeQualifier.of("test", Set.of("Override"), Set.of(), Set.of(TopLevelType.ofClass(Override.class)));
        assertThat(typeQualifier.qualifiedTypes()).containsExactlyInAnyOrder(TopLevelType.ofClass(Override.class));
    }

    @Test
    public void serializeAndDeserialize() throws JsonProcessingException {
        TypeQualifier typeQualifier = TestImmutableImpls.coloredRectangle().typeQualifier();
        TestResources.serializeAndDeserialize(typeQualifier, new TypeReference<>() {});
    }
}
