package org.example.processor.imports;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.example.processor.type.ImportableType;

/**
 * Implements {@link Object#equals(Object)}, {@link Object#hashCode()}, and {@link Object#toString()}
 * for implementations of {@link ImportManager}.
 *
 * <p>All implementations of {@link ImportManager} should derive from {@link BaseImportManager}.</p>
 */
public abstract class BaseImportManager implements ImportManager {

    @Override
    public final boolean equals(Object o) {
        ImportManager other = (o instanceof ImportManager) ? (ImportManager) o : null;
        if (other == null) {
            return false;
        }

        return packageName().equals(other.packageName())
                && importDeclarations().equals(other.importDeclarations())
                && implicitlyImportedTypes().equals(other.implicitlyImportedTypes());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(packageName(), importDeclarations(), implicitlyImportedTypes());
    }

    @Override
    public final String toString() {
        Set<ImportableType> sortedImplicitlyImportedTypes = new TreeSet<>(implicitlyImportedTypes());
        String fieldsString = List.of(
                        fieldToString("packageName", packageName()),
                        fieldToString("importDeclarations", importDeclarations()),
                        fieldToString("implicitlyImportedTypes", sortedImplicitlyImportedTypes))
                .stream()
                .collect(Collectors.joining(", "));
        return String.format("ImportManager{%s}", fieldsString);
    }

    /** Converts a field to a string. */
    private String fieldToString(String name, String value) {
        return String.format("%s=%s", name, value);
    }

    /** Converts a field to a string. */
    private String fieldToString(String name, Collection<ImportableType> importedTypes) {
        String importedTypesString =
                importedTypes.stream().map(ImportableType::binaryName).collect(Collectors.joining(", "));
        return String.format("%s=[%s]", name, importedTypesString);
    }
}
