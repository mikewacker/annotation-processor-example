package org.example.immutable.processor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.immutables.value.Value;

/** Ensures that all top-level types are either imported or referenced via their fully qualified name. */
@Value.Immutable
@JsonSerialize(as = ImmutableTypeQualifier.class)
@JsonDeserialize(as = ImmutableTypeQualifier.class)
public interface TypeQualifier {

    static TypeQualifier of(String packageName, Set<String> typeVars, Set<TopLevelType> referencedTypes) {
        return ImmutableTypeQualifier.builder()
                .packageName(packageName)
                .typeVars(typeVars)
                .referencedTypes(referencedTypes)
                .build();
    }

    /** Gets the package name of the source. */
    String packageName();

    /** Gets the type variables that are referenced in the source. */
    Set<String> typeVars();

    /** Gets all top-level types that are referenced in the source. */
    Set<TopLevelType> referencedTypes();

    /** Gets all top-level types that require an import statement, in sorted order. */
    @Value.Derived
    @JsonIgnore
    default Set<TopLevelType> importedTypes() {
        Set<String> implicitlyImportedPackages = Set.of("java.lang", packageName());
        return referencedTypes().stream()
                .filter(type -> !implicitlyImportedPackages.contains(type.packageName()))
                .filter(type -> !qualifiedTypes().contains(type))
                .sorted(Comparator.comparing(TopLevelType::qualifiedName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Gets all top-level types that must be referenced via their fully qualified name. */
    @Value.Derived
    @JsonIgnore
    default Set<TopLevelType> qualifiedTypes() {
        // If multiple types have the same simple name, qualify the types.
        // Types in the source's package are not qualified for this reason, however.
        Map<String, Set<TopLevelType>> typesBySimpleName =
                referencedTypes().stream().collect(Collectors.groupingBy(TopLevelType::simpleName, Collectors.toSet()));
        Stream<TopLevelType> typeConflicts = typesBySimpleName.values().stream()
                .filter(topLevelTypes -> topLevelTypes.size() > 1)
                .flatMap(Set::stream)
                .filter(type -> !type.packageName().equals(packageName()));

        // If a type's simple name conflicts with a type variable, qualify the type.
        Stream<TopLevelType> typeVarConflicts =
                referencedTypes().stream().filter(type -> typeVars().contains(type.simpleName()));

        return Stream.concat(typeConflicts, typeVarConflicts).collect(Collectors.toSet());
    }
}
