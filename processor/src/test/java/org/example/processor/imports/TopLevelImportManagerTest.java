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
                Set.of(ImportableType.of("org.example.Example"), ImportableType.ofClass(Map.Entry.class)),
                "org.example",
                Set.of());
        assertThat(importManager.getImportDeclarations()).containsExactly(ImportableType.ofClass(Map.class));
        assertThat(importManager.getImplicitlyImportedTypes())
                .containsExactlyInAnyOrder(ImportableType.of("org.example.Example"));
        assertThat(importManager.toSource(ImportableType.ofClass(Map.Entry.class)))
                .isEqualTo("Map.Entry");
        assertThat(importManager.toSource(ImportableType.of("org.example.Example")))
                .isEqualTo("Example");
    }

    @Test
    public void of_DoNotImportNonPackageTypeConflictingWithType() {
        ImportManager importManager = TopLevelImportManager.of(
                Set.of(ImportableType.of("test.Map$Nested"), ImportableType.ofClass(Map.Entry.class)),
                "org.example",
                Set.of());
        assertThat(importManager.getImportDeclarations()).isEmpty();
        assertThat(importManager.getImplicitlyImportedTypes()).isEmpty();
    }

    @Test
    public void of_ImportPackageTypeConflictingWithType() {
        ImportManager importManager = TopLevelImportManager.of(
                Set.of(ImportableType.of("org.example.Map"), ImportableType.ofClass(Map.class)),
                "org.example",
                Set.of());
        assertThat(importManager.getImportDeclarations()).isEmpty();
        assertThat(importManager.getImplicitlyImportedTypes())
                .containsExactlyInAnyOrder(ImportableType.of("org.example.Map"));
    }

    @Test
    public void of_DoNotImportTypeConflictingWithInScopeName() {
        ImportManager importManager =
                TopLevelImportManager.of(Set.of(ImportableType.ofClass(Map.class)), "org.example", Set.of("Map"));
        assertThat(importManager.getImportDeclarations()).isEmpty();
        assertThat(importManager.getImplicitlyImportedTypes()).isEmpty();
    }
}
