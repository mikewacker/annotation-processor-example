package org.example.processor.imports;

import java.util.List;
import java.util.Set;
import org.example.processor.source.SourceGenerator;
import org.example.processor.type.ImportableType;

/**
 * Manages imported types, whether they are explicitly imported via an import declaration or implicitly imported.
 * Can shorten the fully qualified name of an {@link ImportableType}, depending on what is imported.
 *
 * <p>An {@link ImportManager} will be a derived value. Thus, implementations do not need to be serializable,
 * but they do need to support the equality operation.
 *
 * <p>Implementations should derive from {@link BaseImportManager}, which implements the equality operation.</p>
 */
public interface ImportManager extends SourceGenerator<ImportableType> {

    /** Gets the package name for the generated source code. */
    String packageName();

    /** Gets the import declarations in sorted order. */
    List<ImportableType> importDeclarations();

    /**
     * Gets the implicitly imported types.
     *
     * <p>If an implicitly imported type must be referenced using its fully qualified name
     * (e.g., the simple name conflicts with another name in scope), it will not be included.</p>
     *
     * <p>Implicitly imported types that are not referenced may or may not be included.</p>
     */
    Set<ImportableType> implicitlyImportedTypes();
}
