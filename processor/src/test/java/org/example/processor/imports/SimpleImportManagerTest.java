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
    public void getImportDeclarations_SortedOrder() {
        ImportManager importManager = SimpleImportManager.of(
                Set.of(
                        ImportableType.ofClass(Generated.class),
                        ImportableType.ofClass(Map.class),
                        ImportableType.ofClass(List.class)),
                "org.example");
        assertThat(importManager.getImportDeclarations())
                .containsExactly(
                        ImportableType.ofClass(List.class),
                        ImportableType.ofClass(Map.class),
                        ImportableType.ofClass(Generated.class));
        assertThat(importManager.getImplicitlyImportedTypes()).isEmpty();
    }

    @Test
    public void getImportDeclarations_ExcludeJavaLangPackage() {
        ImportManager importManager =
                SimpleImportManager.of(Set.of(ImportableType.ofClass(String.class)), "org.example");
        assertThat(importManager.getImportDeclarations()).isEmpty();
        assertThat(importManager.getImplicitlyImportedTypes())
                .containsExactlyInAnyOrder(ImportableType.ofClass(String.class));
    }

    @Test
    public void getImportDeclarations_ExcludeCurrentPackage() {
        ImportManager importManager =
                SimpleImportManager.of(Set.of(ImportableType.of("org.example.Example")), "org.example");
        assertThat(importManager.getImportDeclarations()).isEmpty();
        assertThat(importManager.getImplicitlyImportedTypes())
                .containsExactlyInAnyOrder(ImportableType.of("org.example.Example"));
    }

    @Test
    public void getImportDeclarations_IncludeCurrentSubPackage() {
        ImportManager importManager =
                SimpleImportManager.of(Set.of(ImportableType.of("org.example.sub.SubExample")), "org.example");
        assertThat(importManager.getImportDeclarations())
                .containsExactly(ImportableType.of("org.example.sub.SubExample"));
        assertThat(importManager.getImplicitlyImportedTypes()).isEmpty();
    }

    @Test
    public void toSource_Imported() {
        ImportManager importManager = SimpleImportManager.of(Set.of(ImportableType.ofClass(Map.class)), "org.example");
        assertThat(importManager.toSource(ImportableType.ofClass(Map.class))).isEqualTo("Map");
    }

    @Test
    public void toSource_NotImported() {
        ImportManager importManager = SimpleImportManager.of(Set.of(), "org.example");
        assertThat(importManager.toSource(ImportableType.ofClass(Map.class))).isEqualTo("java.util.Map");
    }

    @Test
    public void toSource_PartiallyImported() {
        ImportManager importManager = SimpleImportManager.of(Set.of(ImportableType.ofClass(Map.class)), "org.example");
        assertThat(importManager.toSource(ImportableType.ofClass(Map.Entry.class)))
                .isEqualTo("Map.Entry");
    }

    @Test
    public void toSource_ShortestName() {
        ImportManager importManager = SimpleImportManager.of(
                Set.of(ImportableType.ofClass(Map.class), ImportableType.ofClass(Map.Entry.class)), "org.example");
        assertThat(importManager.toSource(ImportableType.ofClass(Map.Entry.class)))
                .isEqualTo("Entry");
    }

    @Test
    public void error_of_ConflictingImports() {
        String expectedMessage = String.join(
                "\n", "conflicting imports found:", "Map: {", "    java.util.Map,", "    org.example.Map,", "}");
        assertThatThrownBy(() -> SimpleImportManager.of(
                        ImmutableSet.of(ImportableType.ofClass(Map.class), ImportableType.of("org.example.Map")),
                        "org.example"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
    }
}
