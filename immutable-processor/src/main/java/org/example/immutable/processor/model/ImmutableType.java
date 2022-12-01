package org.example.immutable.processor.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.Set;
import org.immutables.value.Value;

/**
 * Class type implementing an immutable interface.
 *
 * <p>The class has the same package as the interface.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableImmutableType.class)
@JsonDeserialize(as = ImmutableImmutableType.class)
public interface ImmutableType {

    static ImmutableType of(
            TopLevelType rawImplType,
            Set<String> packageTypes,
            List<String> typeVars,
            NamedType implType,
            NamedType interfaceType) {
        return ImmutableImmutableType.builder()
                .rawImplType(rawImplType)
                .packageTypes(packageTypes)
                .typeVars(typeVars)
                .implType(implType)
                .interfaceType(interfaceType)
                .build();
    }

    /** Gets the raw type of the implementing class. */
    TopLevelType rawImplType();

    /** Gets the simple name of all top-level types in the type's package .*/
    Set<String> packageTypes();

    /** Gets the type variables for generic types, or an empty list for non-generic types. */
    List<String> typeVars();

    /** Gets the type of the implementing class. */
    NamedType implType();

    /** Gets the type of the implemented interface. */
    NamedType interfaceType();
}
