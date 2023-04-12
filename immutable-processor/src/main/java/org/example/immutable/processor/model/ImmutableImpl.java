package org.example.immutable.processor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.immutables.value.Value;

/**
 * Implementation of an immutable interface.
 *
 * <p>If an {@link ImmutableImpl} can be instantiated, then its source can be generated without errors.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableImmutableImpl.class)
@JsonDeserialize(as = ImmutableImmutableImpl.class)
public interface ImmutableImpl {

    static ImmutableImpl of(ImmutableType type, List<ImmutableMember> members) {
        return ImmutableImmutableImpl.builder().type(type).members(members).build();
    }

    /** Gets the type of the implementing class. */
    ImmutableType type();

    /** Gets the immutable members. */
    List<ImmutableMember> members();

    /** Gets the name of the source. */
    @Value.Derived
    @JsonIgnore
    default String sourceName() {
        return type().rawImplType().qualifiedName();
    }

    /** Gets the type qualifier for all top-level types referenced in the implementation. */
    @Value.Derived
    @JsonIgnore
    default TypeQualifier typeQualifier() {
        String packageName = type().rawImplType().packageName();
        Set<String> typeVars = Set.copyOf(type().typeVars());

        Set<TopLevelType> referencedTypes = new HashSet<>();
        referencedTypes.addAll(Set.of(TopLevelType.ofClass(Generated.class), TopLevelType.ofClass(Override.class)));
        referencedTypes.addAll(type().implType().args());
        referencedTypes.addAll(type().interfaceType().args());
        members().forEach(member -> referencedTypes.addAll(member.type().args()));

        return TypeQualifier.of(packageName, typeVars, referencedTypes);
    }
}
