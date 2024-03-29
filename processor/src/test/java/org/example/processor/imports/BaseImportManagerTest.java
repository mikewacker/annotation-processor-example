package org.example.processor.imports;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class BaseImportManagerTest {

    @Test
    public void equals() {
        new EqualsTester()
                .addEqualityGroup(createImportManager(), createImportManager())
                .testEquals();
    }

    @Test
    public void asString() {
        ImportManager importManager = createImportManager();
        assertThat(importManager.toString())
                .isEqualTo(
                        "ImportManager{packageName=org.example, importDeclarations=[java.util.List, java.util.Map$Entry], implicitlyImportedTypes=[java.lang.Object, java.lang.String]}");
    }

    private static ImportManager createImportManager() {
        return DirectImportManager.of(
                "org.example",
                List.of(ImportableType.ofClass(List.class), ImportableType.ofClass(Map.Entry.class)),
                Set.of(ImportableType.ofClass(Object.class), ImportableType.ofClass(String.class)));
    }

    /** Directly provides the package name and the imported types. */
    private static final class DirectImportManager extends BaseImportManager {

        private final String packageName;
        private final List<ImportableType> importDeclarations;
        private final Set<ImportableType> implicitlyImportedTypes;

        public static ImportManager of(
                String packageName,
                List<ImportableType> importDeclarations,
                Set<ImportableType> implicitlyImportedTypes) {
            return new DirectImportManager(packageName, importDeclarations, implicitlyImportedTypes);
        }

        @Override
        public String packageName() {
            return packageName;
        }

        @Override
        public List<ImportableType> importDeclarations() {
            return importDeclarations;
        }

        @Override
        public Set<ImportableType> implicitlyImportedTypes() {
            return implicitlyImportedTypes;
        }

        @Override
        public void generateSource(PrintWriter writer, ImportableType type) {
            throw new UnsupportedOperationException();
        }

        private DirectImportManager(
                String packageName,
                List<ImportableType> importDeclarations,
                Set<ImportableType> implicitlyImportedTypes) {
            this.packageName = packageName;
            this.importDeclarations = importDeclarations;
            this.implicitlyImportedTypes = implicitlyImportedTypes;
        }
    }
}
