package org.example.immutable.processor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.immutables.value.Value;

/**
 * Type that is referenced by name in the source.
 *
 * <p>For top-level types that are referenced, either the simple name or the fully qualified name can be used.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableNamedType.class)
@JsonDeserialize(as = ImmutableNamedType.class)
public interface NamedType {

    static NamedType of(String nameFormat, List<TopLevelType> args) {
        return ImmutableNamedType.builder().nameFormat(nameFormat).args(args).build();
    }

    static NamedType of(String nameFormat, TopLevelType... args) {
        return of(nameFormat, List.of(args));
    }

    static NamedType of(TopLevelType topLevelType) {
        return of("%s", topLevelType);
    }

    /** Concatenates two types into a single type. */
    static NamedType concat(NamedType type1, NamedType type2) {
        return join(List.of(type1, type2), "", "", "");
    }

    /** Concatenates a type and a suffix into a single type. */
    static NamedType concat(NamedType type, String suffix) {
        String nameFormat = String.format("%s%s", type.nameFormat(), suffix);
        return NamedType.of(nameFormat, type.args());
    }

    /** Concatenates a prefix and a type into a single type. */
    static NamedType concat(String prefix, NamedType type) {
        String nameFormat = String.format("%s%s", prefix, type.nameFormat());
        return NamedType.of(nameFormat, type.args());
    }

    /** Joins a list of types into a single type. */
    static NamedType join(List<NamedType> types, String delimiter, String prefix, String suffix) {
        String nameFormat =
                types.stream().map(NamedType::nameFormat).collect(Collectors.joining(delimiter, prefix, suffix));
        List<TopLevelType> args =
                types.stream().map(NamedType::args).flatMap(List::stream).toList();
        return NamedType.of(nameFormat, args);
    }

    /** Gets the format string for the type's name, using "%s" for top-level type arguments. */
    String nameFormat();

    /** Gets the top-level type arguments for the format string. */
    List<TopLevelType> args();

    /** Gets the type's name, using simple names only. */
    @Value.Derived
    @JsonIgnore
    default String name() {
        Object[] nameArgs = args().stream().map(TopLevelType::simpleName).toArray(String[]::new);
        return String.format(nameFormat(), nameArgs);
    }

    /** Gets the type's name, using fully qualified names for the provided top-level types. */
    default String name(Set<TopLevelType> qualifiedTypes) {
        if (qualifiedTypes.isEmpty()) {
            return name();
        }

        Object[] nameArgs = args().stream()
                .map(type -> qualifiedTypes.contains(type) ? type.qualifiedName() : type.simpleName())
                .toArray(String[]::new);
        return String.format(nameFormat(), nameArgs);
    }

    /** Gets the type's name for a declaration, using fully qualified names for the provided top-level types. */
    default String declarationName(Set<TopLevelType> qualifiedTypes) {
        if (qualifiedTypes.isEmpty()) {
            return name();
        }

        TopLevelType rawType = args().get(0);
        String nameFormat =
                String.format("%s%s", rawType.simpleName(), nameFormat().substring(2));
        NamedType declarationType = NamedType.of(nameFormat, args().subList(1, args().size()));
        return declarationType.name(qualifiedTypes);
    }
}
