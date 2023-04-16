package org.example.immutable.processor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.example.processor.type.ImportableType;
import org.immutables.value.Value;

/** Raw top-level type. */
@Value.Immutable
@JsonSerialize(as = ImmutableTopLevelType.class)
@JsonDeserialize(as = ImmutableTopLevelType.class)
public interface TopLevelType {

    static TopLevelType of(String packageName, String simpleName) {
        return ImmutableTopLevelType.builder()
                .packageName(packageName)
                .simpleName(simpleName)
                .build();
    }

    static TopLevelType ofQualifiedName(String qualifiedName) {
        int lastDotIndex = qualifiedName.lastIndexOf('.');
        int packageEndIndex = (lastDotIndex != -1) ? lastDotIndex : 0;
        int classIndex = (lastDotIndex != -1) ? lastDotIndex + 1 : 0;
        String packageName = qualifiedName.substring(0, packageEndIndex);
        String simpleName = qualifiedName.substring(classIndex);
        return TopLevelType.of(packageName, simpleName);
    }

    static TopLevelType ofClass(Class<?> clazz) {
        return of(clazz.getPackageName(), clazz.getSimpleName());
    }

    /** Gets the fully qualified name of the type's package. */
    String packageName();

    /** Gets the simple name of the type. */
    String simpleName();

    @Value.Derived
    @JsonIgnore
    /** Gets the fully qualified name of the type. */
    default String qualifiedName() {
        return !packageName().isEmpty() ? String.format("%s.%s", packageName(), simpleName()) : simpleName();
    }

    @Value.Derived
    @JsonIgnore
    default ImportableType toImportableType() {
        return ImportableType.of(qualifiedName());
    }
}
