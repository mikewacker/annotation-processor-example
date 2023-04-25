package org.example.immutable.processor.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.example.processor.source.SourceGenerator;
import org.example.processor.type.ImportableType;
import org.immutables.value.Value;

/**
 * Type for an immutable member (or for the declaration of the immutable type itself).
 *
 * <p>Its name is represented as a format string, with {@link ImportableType}'s as the arguments.</p>
 */
@Value.Immutable
@JsonSerialize(as = ImmutableMemberType.class)
@JsonDeserialize(as = ImmutableMemberType.class)
public interface MemberType {

    /** Creates a {@link MemberType} from a format string for the name and the {@link ImportableType} arguments. */
    static MemberType of(String nameFormat, List<ImportableType> args) {
        return ImmutableMemberType.builder().nameFormat(nameFormat).args(args).build();
    }

    /** Creates a {@link MemberType} from a format string for the name and the {@link ImportableType} arguments. */
    static MemberType of(String nameFormat, ImportableType... args) {
        return of(nameFormat, List.of(args));
    }

    /** Creates a {@link MemberType} for a primitive type. */
    static MemberType primitiveType(String primitive) {
        return of(primitive);
    }

    /** Creates a {@link MemberType} for an array type. */
    static MemberType arrayType(MemberType componentType) {
        String nameFormat = String.format("%s[]", componentType.nameFormat());
        return of(nameFormat, componentType.args());
    }

    /** Creates a {@link MemberType} for a declared type, possibly a generic type with type arguments. */
    static MemberType declaredType(ImportableType rawType, List<MemberType> typeArgs) {
        if (typeArgs.isEmpty()) {
            return of("%s", rawType);
        }

        String nameFormat = typeArgs.stream().map(MemberType::nameFormat).collect(Collectors.joining(", ", "%s<", ">"));
        List<ImportableType> args = new ArrayList<>(List.of(rawType));
        typeArgs.stream().map(MemberType::args).forEach(args::addAll);
        return of(nameFormat, args);
    }

    /** Creates a {@link MemberType} for a declared type, possibly a generic type with type arguments. */
    static MemberType declaredType(ImportableType rawType, MemberType... typeArgs) {
        return declaredType(rawType, List.of(typeArgs));
    }

    /** Creates a {@link MemberType} for a type parameter, which may be bounded to extend one or more types. */
    static MemberType typeParameter(String name, List<MemberType> bounds) {
        if (bounds.isEmpty()) {
            return of(name);
        }

        String prefix = String.format("%s extends ", name);
        String nameFormat = bounds.stream().map(MemberType::nameFormat).collect(Collectors.joining(" & ", prefix, ""));
        List<ImportableType> args =
                bounds.stream().map(MemberType::args).flatMap(List::stream).toList();
        return of(nameFormat, args);
    }

    /** Creates a {@link MemberType} for a type parameter, which may be bounded to extend one or more types. */
    static MemberType typeParameter(String name, MemberType... bounds) {
        return typeParameter(name, List.of(bounds));
    }

    /** Creates a {@link MemberType} for a type variable. */
    static MemberType typeVariable(String name) {
        return of(name);
    }

    /** Creates a {@link MemberType} for a wildcard type. */
    static MemberType wildcardType() {
        return of("?");
    }

    /** Creates a {@link MemberType} for a wildcard type that is bounded to extend a type. */
    static MemberType wildcardExtendsType(MemberType bound) {
        String nameFormat = String.format("? extends %s", bound.nameFormat());
        return of(nameFormat, bound.args());
    }

    /** Creates a {@link MemberType} for a wildcard type that is bounded to be a super type. */
    static MemberType wildcardSuperType(MemberType bound) {
        String nameFormat = String.format("? super %s", bound.nameFormat());
        return of(nameFormat, bound.args());
    }

    /** Gets the format string for the name. */
    String nameFormat();

    /** Gets the {@link ImportableType} arguments. */
    List<ImportableType> args();

    /** Gets the raw type for a declared type. */
    @Value.Lazy
    @JsonIgnore
    default ImportableType rawType() {
        return args().get(0);
    }

    /**
     * Adds type arguments to an outer type that is generic.
     *
     * <p>If multiple outer types are generic, type arguments should be added to the innermost type first.</p>
     */
    default MemberType addTypeArgumentsToOuterType(ImportableType rawOuterType, List<MemberType> outerTypeArgs) {
        String qualifiedSuffix = rawType().qualifiedSuffix(rawOuterType);
        String suffix = String.format(">%s%s", qualifiedSuffix, nameFormat().substring(2));
        String nameFormat =
                outerTypeArgs.stream().map(MemberType::nameFormat).collect(Collectors.joining(", ", "%s<", suffix));

        List<ImportableType> args = new ArrayList<>(List.of(rawOuterType));
        outerTypeArgs.stream().map(MemberType::args).forEach(args::addAll);
        args.addAll(args().subList(1, args().size()));
        return of(nameFormat, args);
    }

    /**
     * Adds type arguments to an outer type that is generic.
     *
     * <p>If multiple outer types are generic, type arguments should be added to the innermost type first.</p>
     */
    default MemberType addTypeArgumentsToOuterType(ImportableType rawOuterType, MemberType... outerTypeArgs) {
        return addTypeArgumentsToOuterType(rawOuterType, List.of(outerTypeArgs));
    }

    /** Gets a {@link MemberType} for a declared top-level type that can be used for the type's declaration. */
    @Value.Lazy
    @JsonIgnore
    default MemberType topLevelDeclaration() {
        String nameFormat =
                String.format("%s%s", rawType().simpleName(), nameFormat().substring(2));
        List<ImportableType> args = args().subList(1, args().size());
        return of(nameFormat, args);
    }

    /** Generates the source for a {@link MemberType}. */
    class Namer implements SourceGenerator<MemberType> {

        private final SourceGenerator<ImportableType> importableTypeNamer;

        /** Creates a namer for {@link MemberType}'s from a namer for {@link ImportableType}'s. */
        public static Namer of(SourceGenerator<ImportableType> importableTypeNamer) {
            return new Namer(importableTypeNamer);
        }

        @Override
        public void generateSource(PrintWriter writer, MemberType type) {
            Object[] args =
                    type.args().stream().map(importableTypeNamer::toSource).toArray();
            String name = String.format(type.nameFormat(), args);
            writer.print(name);
        }

        private Namer(SourceGenerator<ImportableType> importableTypeNamer) {
            this.importableTypeNamer = importableTypeNamer;
        }
    }
}
