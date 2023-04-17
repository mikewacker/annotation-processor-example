package org.example.processor.imports;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class ImportTrieTest {

    @Test
    public void navigateTrie() {
        ImportTrie root = createTrie();
        assertThat(root.isImportedType()).isFalse();
        assertThat(root.isImplicitlyImportedPackage()).isFalse();
        assertThat(root.getChildren()).hasSize(1);
        assertThat(root.tryGetChild("java")).isPresent();
        assertThat(root.tryGetChild("org")).isEmpty();

        ImportTrie javaNode = root.tryGetChild("java").get();
        assertThat(javaNode.isImportedType()).isFalse();
        assertThat(javaNode.isImplicitlyImportedPackage()).isFalse();
        assertThat(javaNode.getChildren()).hasSize(1);
        assertThat(javaNode.tryGetChild("lang")).isPresent();

        ImportTrie javaLangNode = javaNode.tryGetChild("lang").get();
        assertThat(javaLangNode.isImportedType()).isFalse();
        assertThat(javaLangNode.isImplicitlyImportedPackage()).isTrue();
        assertThat(javaLangNode.getChildren()).hasSize(2);
        assertThat(javaLangNode.tryGetChild("String")).isPresent();

        ImportTrie stringNode = javaLangNode.tryGetChild("String").get();
        assertThat(stringNode.isImportedType()).isTrue();
        assertThat(stringNode.getImportedType()).isEqualTo(ImportableType.ofClass(String.class));
        assertThat(stringNode.isImplicitlyImportedPackage()).isFalse();
        assertThat(stringNode.getChildren()).isEmpty();
    }

    @Test
    public void getChildrenInOrder() {
        ImportTrie javaLangNode =
                createTrie().tryGetChild("java").get().tryGetChild("lang").get();
        List<ImportableType> importedTypes = javaLangNode.getChildren().stream()
                .filter(ImportTrie::isImportedType)
                .map(ImportTrie::getImportedType)
                .toList();
        assertThat(importedTypes)
                .containsExactly(ImportableType.ofClass(Object.class), ImportableType.ofClass(String.class));
    }

    private static ImportTrie createTrie() {
        ImportTrie root = ImportTrie.createRoot();
        ImportTrie stringNode = root.getOrAddChild("java").getOrAddChild("lang").getOrAddChild("String");
        stringNode.setImportedType(ImportableType.ofClass(String.class));
        ImportTrie objectNode = root.getOrAddChild("java").getOrAddChild("lang").getOrAddChild("Object");
        objectNode.setImportedType(ImportableType.ofClass(Object.class));
        ImportTrie javaLangNode = root.getOrAddChild("java").getOrAddChild("lang");
        javaLangNode.setImplicitlyImportedPackage();
        return root;
    }
}
