package org.example.processor.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * Type that can be imported via an import declaration (or is implicitly imported).
 *
 * <p>The binary name is used to create an {@link ImportableType};
 * all other relevant information can be derived from the binary name.</p>
 *
 * <p>{@link ImportableType}'s are ordered based on their qualified name.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableImportableType.class)
@JsonDeserialize(as = ImmutableImportableType.class)
public interface ImportableType extends Comparable<ImportableType> {

    /** Creates an {@link ImportableType} from the type's binary name. */
    static ImportableType of(String binaryName) {
        return ImmutableImportableType.builder().binaryName(binaryName).build();
    }

    /** Creates an {@link ImportableType} from the {@link Class}. */
    static ImportableType ofClass(Class<?> clazz) {
        return of(clazz.getName());
    }

    /** Gets the binary name. */
    String binaryName();

    /** Gets the fully qualified name. */
    @Value.Derived
    @JsonIgnore
    default String qualifiedName() {
        return binaryName().replace('$', '.');
    }

    /** Gets the simple name. */
    @Value.Derived
    @JsonIgnore
    default String simpleName() {
        int lastNestingIndex = className().lastIndexOf('.');
        return (lastNestingIndex != -1) ? className().substring(lastNestingIndex + 1) : className();
    }

    /** Gets the package name. */
    @Value.Derived
    @JsonIgnore
    default String packageName() {
        int packageLength = binaryName().length() - className().length() - 1;
        return (packageLength != -1) ? binaryName().substring(0, packageLength) : "";
    }

    /** Gets the class name, using '.' to delimit nested classes. */
    @Value.Derived
    @JsonIgnore
    default String className() {
        int lastDotIndex = binaryName().lastIndexOf('.');
        String binaryClassName = (lastDotIndex != -1) ? binaryName().substring(lastDotIndex + 1) : binaryName();
        return binaryClassName.replace('$', '.');
    }

    /** Determines if this type is a top-level type. */
    @Value.Derived
    @JsonIgnore
    default boolean isTopLevelType() {
        return !binaryName().contains("$");
    }

    /** Gets the top-level type, or this type if it is a top-level type. */
    @Value.Lazy
    @JsonIgnore
    default ImportableType topLevelType() {
        if (isTopLevelType()) {
            return this;
        }

        int nestingIndex = binaryName().indexOf('$');
        String topLevelBinaryName = binaryName().substring(0, nestingIndex);
        return ImportableType.of(topLevelBinaryName);
    }

    /** Tries to get the enclosing type. */
    @Value.Lazy
    @JsonIgnore
    default Optional<ImportableType> enclosingType() {
        if (isTopLevelType()) {
            return Optional.empty();
        }

        int lastNestingIndex = binaryName().lastIndexOf('$');
        String enclosingBinaryName = binaryName().substring(0, lastNestingIndex);
        return Optional.of(ImportableType.of(enclosingBinaryName));
    }

    /** Gets the suffix of the qualified name, based on the outer type. */
    default String qualifiedSuffix(ImportableType outerType) {
        String binaryPrefix = String.format("%s$", outerType.binaryName());
        if (!binaryName().startsWith(binaryPrefix)) {
            String message = String.format("%s is not an outer type of %s", outerType.binaryName(), binaryName());
            throw new IllegalArgumentException(message);
        }

        String suffix = binaryName().substring(outerType.binaryName().length());
        return suffix.replace('$', '.');
    }

    @Override
    default int compareTo(ImportableType other) {
        return qualifiedName().compareTo(other.qualifiedName());
    }
}
