package org.example.processor.imports;

import java.io.PrintWriter;
import java.util.List;
import org.example.processor.source.SourceGenerator;
import org.example.processor.type.ImportableType;

/**
 * Generates the package declaration and import declarations from an {@link ImportManager}.
 *
 * <p>It assumes that there are no static imports.</p>
 */
public final class ImportGenerator implements SourceGenerator<ImportManager> {

    private static final SourceGenerator<ImportManager> INSTANCE = new ImportGenerator();

    /** Gets the singleton instance of {@link ImportGenerator}. */
    public static SourceGenerator<ImportManager> instance() {
        return INSTANCE;
    }

    @Override
    public void generateSource(PrintWriter writer, ImportManager importManager) {
        generatePackageDeclaration(writer, importManager.packageName());
        generateImportDeclarations(writer, importManager.importDeclarations());
    }

    private ImportGenerator() {}

    /** Generates a package declaration, unless the unnamed package is used. */
    private void generatePackageDeclaration(PrintWriter writer, String packageName) {
        if (packageName.isEmpty()) {
            return;
        }

        writer.format("package %s;", packageName).println();
        writer.println();
    }

    /** Generates the import declarations. */
    private void generateImportDeclarations(PrintWriter writer, List<ImportableType> importDeclarations) {
        if (importDeclarations.isEmpty()) {
            return;
        }

        importDeclarations.forEach(
                type -> writer.format("import %s;", type.qualifiedName()).println());
        writer.println();
    }
}
