package org.example.processor.imports;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.example.processor.type.ImportableType;

/** Imports only top-level types, computing the imports from a set of referenced types (and other information). */
public final class TopLevelImportManager {

    /**
     * Creates an {@link ImportManager} from a set of referenced types and other information.
     *
     * <p>One edge case that is not handled is when an unreferenced type in the current package
     * has the same simple name as a referenced type in the {@code java.lang} package.
     * To handle this edge case, add all top-level types in the package to the set of referenced types.</p>
     *
     * @param referencedTypes set of types referenced in the generated source code
     * @param packageName package name for the generated source code
     * @param inScopeNames set of other names that could be in scope (e.g., type variables)
     */
    public static ImportManager of(Set<ImportableType> referencedTypes, String packageName, Set<String> inScopeNames) {
        Set<ImportableType> importedTypes = getImportedTypes(referencedTypes, packageName, inScopeNames);
        return SimpleImportManager.of(importedTypes, packageName);
    }

    /** Gets the set of imported types, including implicitly imported types. */
    private static Set<ImportableType> getImportedTypes(
            Set<ImportableType> referencedTypes, String packageName, Set<String> inScopeNames) {
        Set<ImportableType> topLevelTypes = getTopLevelTypes(referencedTypes);
        Set<ImportableType> qualifiedTypes = Stream.concat(
                        getTypeAndTypeConflicts(topLevelTypes, packageName),
                        getTypeAndNameConflicts(topLevelTypes, inScopeNames))
                .collect(Collectors.toSet());
        return topLevelTypes.stream()
                .filter(type -> !qualifiedTypes.contains(type))
                .collect(Collectors.toSet());
    }

    /** Gets the set of top-level types for the provided types. */
    private static Set<ImportableType> getTopLevelTypes(Set<ImportableType> types) {
        return types.stream().map(ImportableType::topLevelType).collect(Collectors.toSet());
    }

    /** Gets types that conflict on their simple name, excluding types in the current package. */
    private static Stream<ImportableType> getTypeAndTypeConflicts(Set<ImportableType> types, String packageName) {
        Map<String, Set<ImportableType>> typesBySimpleName =
                types.stream().collect(Collectors.groupingBy(ImportableType::simpleName, Collectors.toSet()));
        return typesBySimpleName.values().stream()
                .filter(groupedTypes -> groupedTypes.size() > 1)
                .flatMap(Set::stream)
                .filter(type -> !type.packageName().equals(packageName));
    }

    /** Gets types whose simple name conflicts with a name in scope. */
    private static Stream<ImportableType> getTypeAndNameConflicts(Set<ImportableType> types, Set<String> inScopeNames) {
        return types.stream().filter(type -> inScopeNames.contains(type.simpleName()));
    }

    // static class
    private TopLevelImportManager() {}
}
