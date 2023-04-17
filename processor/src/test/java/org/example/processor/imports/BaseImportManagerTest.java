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
                        "ImportManager{importDeclarations=[java.util.List, java.util.Map$Entry], implicitlyImportedTypes=[java.lang.Object, java.lang.String]}");
    }

    private static ImportManager createImportManager() {
        return DirectImportManager.of(
                List.of(ImportableType.ofClass(List.class), ImportableType.ofClass(Map.Entry.class)),
                Set.of(ImportableType.ofClass(Object.class), ImportableType.ofClass(String.class)));
    }

    /** Directly provides the imported types. */
    private static final class DirectImportManager extends BaseImportManager {

        private final List<ImportableType> importDeclarations;
        private final Set<ImportableType> implicitlyImportedTypes;

        public static ImportManager of(
                List<ImportableType> importDeclarations, Set<ImportableType> implicitlyImportedTypes) {
            return new DirectImportManager(importDeclarations, implicitlyImportedTypes);
        }

        @Override
        public List<ImportableType> getImportDeclarations() {
            return importDeclarations;
        }

        @Override
        public Set<ImportableType> getImplicitlyImportedTypes() {
            return implicitlyImportedTypes;
        }

        @Override
        public void generateSource(PrintWriter writer, ImportableType entity) {
            throw new UnsupportedOperationException();
        }

        private DirectImportManager(
                List<ImportableType> importDeclarations, Set<ImportableType> implicitlyImportedTypes) {
            this.importDeclarations = importDeclarations;
            this.implicitlyImportedTypes = implicitlyImportedTypes;
        }
    }
}
