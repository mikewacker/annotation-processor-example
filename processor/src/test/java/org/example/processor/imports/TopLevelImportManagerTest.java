package org.example.processor.imports;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class TopLevelImportManagerTest {

    @Test
    public void of() {
        ImportManager importManager = TopLevelImportManager.of(
                "org.example",
                Set.of(ImportableType.of("org.example.Example"), ImportableType.ofClass(Map.Entry.class)),
                Set.of());
        assertThat(importManager.importDeclarations()).containsExactly(ImportableType.ofClass(Map.class));
        assertThat(importManager.implicitlyImportedTypes())
                .containsExactlyInAnyOrder(ImportableType.of("org.example.Example"));
        assertThat(importManager.toSource(ImportableType.ofClass(Map.Entry.class)))
                .isEqualTo("Map.Entry");
        assertThat(importManager.toSource(ImportableType.of("org.example.Example")))
                .isEqualTo("Example");
    }

    @Test
    public void of_DoNotImportNonPackageTypeConflictingWithType() {
        ImportManager importManager = TopLevelImportManager.of(
                "org.example",
                Set.of(ImportableType.of("test.Map$Nested"), ImportableType.ofClass(Map.Entry.class)),
                Set.of());
        assertThat(importManager.importDeclarations()).isEmpty();
        assertThat(importManager.implicitlyImportedTypes()).isEmpty();
    }

    @Test
    public void of_ImportPackageTypeConflictingWithType() {
        ImportManager importManager = TopLevelImportManager.of(
                "org.example",
                Set.of(ImportableType.of("org.example.Map"), ImportableType.ofClass(Map.class)),
                Set.of());
        assertThat(importManager.importDeclarations()).isEmpty();
        assertThat(importManager.implicitlyImportedTypes())
                .containsExactlyInAnyOrder(ImportableType.of("org.example.Map"));
    }

    @Test
    public void of_DoNotImportTypeConflictingWithInScopeName() {
        ImportManager importManager =
                TopLevelImportManager.of("org.example", Set.of(ImportableType.ofClass(Map.class)), Set.of("Map"));
        assertThat(importManager.importDeclarations()).isEmpty();
        assertThat(importManager.implicitlyImportedTypes()).isEmpty();
    }
}
