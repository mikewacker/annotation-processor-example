package org.example.immutable.processor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Raw top-level type.
 *
 * <p>It can be assumed that the type has a package.</p>
 */
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

    static TopLevelType of(Class<?> clazz) {
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
        return String.format("%s.%s", packageName(), simpleName());
    }
}
