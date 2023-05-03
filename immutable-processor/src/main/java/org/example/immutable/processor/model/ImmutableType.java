package org.example.immutable.processor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Splitter;
import java.util.List;
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

    static ImmutableType of(MemberType implType, MemberType interfaceType) {
        return ImmutableImmutableType.builder()
                .implType(implType)
                .interfaceType(interfaceType)
                .build();
    }

    /** Gets the type of the implementing class. */
    MemberType implType();

    /** Gets the type of the implemented interface. */
    MemberType interfaceType();

    @Value.Derived
    @JsonIgnore
    default List<String> typeVars() {
        String nameFormat = interfaceType().nameFormat();
        int beginIndex = nameFormat.indexOf('<');
        if (beginIndex == -1) {
            return List.of();
        }

        int endIndex = nameFormat.indexOf('>');
        String typeVarsText = nameFormat.substring(beginIndex + 1, endIndex);
        return Splitter.on(", ").splitToList(typeVarsText);
    }
}
