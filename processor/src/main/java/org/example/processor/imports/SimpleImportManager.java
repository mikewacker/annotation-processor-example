package org.example.processor.imports;

import com.google.common.base.Splitter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.example.processor.type.ImportableType;

/** Imports a set of types that are directly provided. */
public final class SimpleImportManager extends BaseImportManager {

    private static final Splitter NAME_SPLITTER = Splitter.on('.');

    private final String packageName;
    private final ImportTrie trie;
    private final List<ImportableType> importDeclarations;
    private final Set<ImportableType> implicitlyImportedTypes;

    /**
     * Creates an {@link ImportManager} from a package name and a set of imported types.
     *
     * <p>If an implicitly imported type must be referenced using its fully qualified name
     * (e.g., the simple name conflicts with another name in scope), it must not be included.</p>
     *
     * @param packageName package name for the generated source code
     * @param importedTypes set of types imported in the generated source code, including implicitly imported types
     */
    public static ImportManager of(String packageName, Set<ImportableType> importedTypes) {
        return new SimpleImportManager(packageName, importedTypes);
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
        List<String> nameParts = splitName(type.qualifiedName());
        int importIndex = findLastImportedIndex(nameParts);
        List<String> shortenedNameParts = nameParts.subList(importIndex, nameParts.size());
        String shortenedName = joinNameParts(shortenedNameParts);
        writer.print(shortenedName);
    }

    /** Creates an import manager from the package name and the imported types. */
    private SimpleImportManager(String packageName, Set<ImportableType> importedTypes) {
        Set<String> implicitlyImportedPackages = Set.of("java.lang", packageName);
        this.packageName = packageName;
        trie = ImportTrie.createRoot();
        populateTrie(importedTypes, implicitlyImportedPackages);
        importDeclarations = collectImportDeclarations();
        implicitlyImportedTypes = collectImplicitlyImportedTypes();
    }

    /** Populates the trie with imported types and implicitly imported packages. */
    private void populateTrie(Set<ImportableType> importedTypes, Set<String> implicitlyImportedPackages) {
        checkNoConflictingImports(importedTypes);
        importedTypes.forEach(this::addImportedName);
        implicitlyImportedPackages.forEach(this::addImplicitlyImportedPackage);
    }

    /** Adds an imported type to the trie. */
    private void addImportedName(ImportableType importedType) {
        List<String> nameParts = splitName(importedType.qualifiedName());
        ImportTrie node = followOrCreatePath(nameParts);
        node.setImportedType(importedType);
    }

    /** Adds an implicitly imported package to the trie. */
    private void addImplicitlyImportedPackage(String implicitlyImportedPackage) {
        List<String> nameParts = splitName(implicitlyImportedPackage);
        ImportTrie node = followOrCreatePath(nameParts);
        node.setImplicitlyImportedPackage();
    }

    /** Collects the import declarations from the trie. */
    private List<ImportableType> collectImportDeclarations() {
        List<ImportableType> importDeclarations = new ArrayList<>();
        collectImportedTypes(importDeclarations, false, trie, false);
        return Collections.unmodifiableList(importDeclarations);
    }

    /** Collects the implicitly imported types from the trie. */
    private Set<ImportableType> collectImplicitlyImportedTypes() {
        Set<ImportableType> implicitlyImportedTypes = new HashSet<>();
        collectImportedTypes(implicitlyImportedTypes, true, trie, false);
        return Collections.unmodifiableSet(implicitlyImportedTypes);
    }

    /** Collects imported types from the trie. */
    private void collectImportedTypes(
            Collection<ImportableType> importedTypes,
            boolean collectImplicit,
            ImportTrie node,
            boolean isImplicitlyImported) {
        if (node.isImportedType()) {
            if ((collectImplicit && isImplicitlyImported) || (!collectImplicit && !isImplicitlyImported)) {
                importedTypes.add(node.getImportedType());
            }
        }

        node.getChildren()
                .forEach(child -> collectImportedTypes(
                        importedTypes, collectImplicit, child, node.isImplicitlyImportedPackage()));
    }

    /** Finds the index of the last imported name part, or 0 if no part is imported. */
    private int findLastImportedIndex(List<String> nameParts) {
        return getPath(nameParts)
                .filter(ImportTrie::isImportedType)
                .mapToInt(ImportTrie::getDepthIndex)
                .max()
                .orElse(0);
    }

    /** Follows or creates a path down the trie, returning the node at end of the path. */
    private ImportTrie followOrCreatePath(List<String> nameParts) {
        return nameParts.stream().reduce(trie, ImportTrie::getOrAddChild, SimpleImportManager::dummyCombiner);
    }

    /** Gets the path down the trie, which terminates if a child is not found. */
    private Stream<ImportTrie> getPath(List<String> nameParts) {
        Iterator<String> namePartItr = nameParts.iterator();
        return Stream.iterate(
                        Optional.of(trie),
                        Optional::isPresent,
                        maybeNode -> maybeNode.flatMap(node ->
                                namePartItr.hasNext() ? node.tryGetChild(namePartItr.next()) : Optional.empty()))
                .map(Optional::get);
    }

    /** Splits a name into individual parts. */
    private static List<String> splitName(String name) {
        return !name.isEmpty() ? NAME_SPLITTER.splitToList(name) : List.of();
    }

    /** Joins the parts into a name. */
    private static String joinNameParts(List<String> nameParts) {
        return String.join(".", nameParts);
    }

    /** Checks that each imported type has a unique simple name. */
    private static void checkNoConflictingImports(Set<ImportableType> importedTypes) {
        Map<String, Set<ImportableType>> importedTypesBySimpleName =
                importedTypes.stream().collect(Collectors.groupingBy(ImportableType::simpleName, Collectors.toSet()));
        Map<String, Set<ImportableType>> simpleNameConflicts = importedTypesBySimpleName.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                // Ensure that conflicts are sorted.
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new TreeSet<>(entry.getValue()),
                        SimpleImportManager::dummyCombiner,
                        TreeMap::new));
        if (!simpleNameConflicts.isEmpty()) {
            String message = createConflictingImportsMessage(simpleNameConflicts);
            throw new IllegalArgumentException(message);
        }
    }

    /** Creates the error message when multiple imported types have the same simple name. */
    private static String createConflictingImportsMessage(Map<String, Set<ImportableType>> simpleNameConflicts) {
        StringWriter messageWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(messageWriter);
        writer.print("conflicting imports found:");
        simpleNameConflicts.forEach((simpleName, types) -> {
            writer.println();
            writer.format("%s: {", simpleName).println();
            for (ImportableType type : types) {
                writer.format("    %s,", type.qualifiedName()).println();
            }
            writer.print("}");
        });
        return messageWriter.toString();
    }

    private static <T> T dummyCombiner(T t1, T t2) {
        return t1;
    }
}
