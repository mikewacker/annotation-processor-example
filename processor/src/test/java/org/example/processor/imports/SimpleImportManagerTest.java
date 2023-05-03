package org.example.processor.imports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class SimpleImportManagerTest {

    @Test
    public void getPackageName() {
        ImportManager importManager = SimpleImportManager.of("org.example", Set.of());
        assertThat(importManager.packageName()).isEqualTo("org.example");
    }

    @Test
    public void getImportDeclarations_SortedOrder() {
        ImportManager importManager = SimpleImportManager.of(
                "org.example",
                Set.of(
                        ImportableType.ofClass(Generated.class),
                        ImportableType.ofClass(Map.class),
                        ImportableType.ofClass(List.class)));
        assertThat(importManager.importDeclarations())
                .containsExactly(
                        ImportableType.ofClass(List.class),
                        ImportableType.ofClass(Map.class),
                        ImportableType.ofClass(Generated.class));
        assertThat(importManager.implicitlyImportedTypes()).isEmpty();
    }

    @Test
    public void getImportDeclarations_ExcludeJavaLangPackage() {
        ImportManager importManager =
                SimpleImportManager.of("org.example", Set.of(ImportableType.ofClass(String.class)));
        assertThat(importManager.importDeclarations()).isEmpty();
        assertThat(importManager.implicitlyImportedTypes())
                .containsExactlyInAnyOrder(ImportableType.ofClass(String.class));
    }

    @Test
    public void getImportDeclarations_ExcludeCurrentPackage() {
        ImportManager importManager =
                SimpleImportManager.of("org.example", Set.of(ImportableType.of("org.example.Example")));
        assertThat(importManager.importDeclarations()).isEmpty();
        assertThat(importManager.implicitlyImportedTypes())
                .containsExactlyInAnyOrder(ImportableType.of("org.example.Example"));
    }

    @Test
    public void getImportDeclarations_IncludeCurrentSubPackage() {
        ImportManager importManager =
                SimpleImportManager.of("org.example", Set.of(ImportableType.of("org.example.sub.SubExample")));
        assertThat(importManager.importDeclarations()).containsExactly(ImportableType.of("org.example.sub.SubExample"));
        assertThat(importManager.implicitlyImportedTypes()).isEmpty();
    }

    @Test
    public void toSource_Imported() {
        ImportManager importManager = SimpleImportManager.of("org.example", Set.of(ImportableType.ofClass(Map.class)));
        assertThat(importManager.toSource(ImportableType.ofClass(Map.class))).isEqualTo("Map");
    }

    @Test
    public void toSource_NotImported() {
        ImportManager importManager = SimpleImportManager.of("org.example", Set.of());
        assertThat(importManager.toSource(ImportableType.ofClass(Map.class))).isEqualTo("java.util.Map");
    }

    @Test
    public void toSource_PartiallyImported() {
        ImportManager importManager = SimpleImportManager.of("org.example", Set.of(ImportableType.ofClass(Map.class)));
        assertThat(importManager.toSource(ImportableType.ofClass(Map.Entry.class)))
                .isEqualTo("Map.Entry");
    }

    @Test
    public void toSource_ShortestName() {
        ImportManager importManager = SimpleImportManager.of(
                "org.example", Set.of(ImportableType.ofClass(Map.class), ImportableType.ofClass(Map.Entry.class)));
        assertThat(importManager.toSource(ImportableType.ofClass(Map.Entry.class)))
                .isEqualTo("Entry");
    }

    @Test
    public void error_of_ConflictingImports() {
        String expectedMessage = String.join(
                "\n", "conflicting imports found:", "Map: {", "    java.util.Map,", "    org.example.Map,", "}");
        assertThatThrownBy(() -> SimpleImportManager.of(
                        "org.example",
                        ImmutableSet.of(ImportableType.ofClass(Map.class), ImportableType.of("org.example.Map"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
    }
}
