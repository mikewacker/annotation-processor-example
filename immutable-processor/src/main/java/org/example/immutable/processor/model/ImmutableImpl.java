package org.example.immutable.processor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.Generated;
import org.example.processor.imports.ImportManager;
import org.example.processor.imports.TopLevelImportManager;
import org.example.processor.type.ImportableType;
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
    default ImportManager importManager() {
        // Collect all the referenced types.
        Set<ImportableType> referencedTypes = new HashSet<>();
        referencedTypes.addAll(Set.of(ImportableType.ofClass(Generated.class), ImportableType.ofClass(Override.class)));
        addAllTopLevelTypes(referencedTypes, type().implType().args());
        addAllTopLevelTypes(referencedTypes, type().interfaceType().args());
        members()
                .forEach(member ->
                        addAllTopLevelTypes(referencedTypes, member.type().args()));

        // Create the import manager.
        String packageName = type().rawImplType().packageName();
        Set<String> inScopeNames = Set.copyOf(type().typeVars());
        return TopLevelImportManager.of(referencedTypes, packageName, inScopeNames);
    }

    private static void addAllTopLevelTypes(
            Set<ImportableType> referencedTypes, Collection<TopLevelType> topLevelTypes) {
        Set<ImportableType> importableTypes =
                topLevelTypes.stream().map(TopLevelType::toImportableType).collect(Collectors.toSet());
        referencedTypes.addAll(importableTypes);
    }
}
