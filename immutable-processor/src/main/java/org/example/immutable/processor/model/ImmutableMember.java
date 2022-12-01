package org.example.immutable.processor.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/** Member of an immutable class: a method without parameters, backed by a corresponding field. */
@Value.Immutable
@JsonSerialize(as = ImmutableImmutableMember.class)
@JsonDeserialize(as = ImmutableImmutableMember.class)
public interface ImmutableMember {

    static ImmutableMember of(String name, NamedType type) {
        return ImmutableImmutableMember.builder().name(name).type(type).build();
    }

    /** Gets the name of the member. */
    String name();

    /** Gets the type of the member. */
    NamedType type();
}
