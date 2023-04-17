package org.example.processor.imports;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.example.processor.type.ImportableType;

/**
 * Trie that contains all the imported types.
 *
 * <p>A preorder traversal of this trie will visit the imported types in sorted order.</p>
 */
final class ImportTrie {

    private final int depthIndex;
    private Optional<ImportableType> maybeImportedType = Optional.empty();
    private boolean isImplicitlyImportedPackage = false;
    private Map<String, ImportTrie> children = new TreeMap<>();

    /** Creates the root node of an empty trie. */
    public static ImportTrie createRoot() {
        return new ImportTrie(-1);
    }

    /**
     * Gets an index based on the depth of the trie.
     *
     * <p>If a qualified name is split based on '.', the index would point to the simple name in that list.
     * E.g., the node for {@code java.lang.Map} has index 2; the string at index 2 would be {@code Map}.</p>
     */
    public int getDepthIndex() {
        return depthIndex;
    }

    /** Determines if an imported type exists at the current node. */
    public boolean isImportedType() {
        return maybeImportedType.isPresent();
    }

    /** Gets the imported type at the current node. */
    public ImportableType getImportedType() {
        return maybeImportedType.get();
    }

    /** Determines if an implicitly imported package exists at the current node. */
    public boolean isImplicitlyImportedPackage() {
        return isImplicitlyImportedPackage;
    }

    /** Tries to get a child. */
    public Optional<ImportTrie> tryGetChild(String namePart) {
        return Optional.ofNullable(children.get(namePart));
    }

    /** Gets the children in sorted order. */
    public Collection<ImportTrie> getChildren() {
        return children.values();
    }

    /** Sets the imported type at the current node. */
    public void setImportedType(ImportableType importedType) {
        maybeImportedType = Optional.of(importedType);
    }

    /** Sets an implicitly imported package at the current node. */
    public void setImplicitlyImportedPackage() {
        isImplicitlyImportedPackage = true;
    }

    /** Gets or adds a child. */
    public ImportTrie getOrAddChild(String namePart) {
        return children.computeIfAbsent(namePart, k -> createChild());
    }

    /** Creates a child of the current node. */
    private ImportTrie createChild() {
        return new ImportTrie(depthIndex + 1);
    }

    private ImportTrie(int depthIndex) {
        this.depthIndex = depthIndex;
    }
}
