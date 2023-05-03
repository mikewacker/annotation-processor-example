package org.example.processor.imports;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class ImportGeneratorTest {

    @Test
    public void toSource() {
        ImportManager importManager = DirectImportManager.of(
                "org.example", List.of(ImportableType.ofClass(List.class), ImportableType.ofClass(Map.class)));
        String expectedSource = String.join(
                "\n", "package org.example;", "", "import java.util.List;", "import java.util.Map;", "", "");
        assertThat(ImportGenerator.instance().toSource(importManager)).isEqualTo(expectedSource);
    }

    @Test
    public void toSource_UnnamedPackage() {
        ImportManager importManager = DirectImportManager.of("", List.of(ImportableType.ofClass(List.class)));
        String expectedSource = String.join("\n", "import java.util.List;", "", "");
        assertThat(ImportGenerator.instance().toSource(importManager)).isEqualTo(expectedSource);
    }

    @Test
    public void toSource_NoImportDeclarations() {
        ImportManager importManager = DirectImportManager.of("org.example", List.of());
        String expectedSource = String.join("\n", "package org.example;", "", "");
        assertThat(ImportGenerator.instance().toSource(importManager)).isEqualTo(expectedSource);
    }

    /** Directly provides the package name and import declarations. */
    @SuppressWarnings("UnusedVariable") // false positive for Error Prone
    private record DirectImportManager(String packageName, List<ImportableType> importDeclarations)
            implements ImportManager {

        public static ImportManager of(String packageName, List<ImportableType> importDeclarations) {
            return new DirectImportManager(packageName, importDeclarations);
        }

        @Override
        public Set<ImportableType> implicitlyImportedTypes() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void generateSource(PrintWriter writer, ImportableType type) {
            throw new UnsupportedOperationException();
        }
    }
}
