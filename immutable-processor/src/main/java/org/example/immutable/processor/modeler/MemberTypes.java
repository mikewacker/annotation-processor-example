package org.example.immutable.processor.modeler;

import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import org.example.immutable.processor.error.Errors;
import org.example.immutable.processor.model.MemberType;
import org.example.processor.base.ProcessorScope;
import org.example.processor.type.ImportableType;

/**
 * Creates {@link MemberType}'s from {@link TypeMirror}'s.
 *
 * <p>The source {@link Element} is also provided for error reporting purposes.</p>
 */
@ProcessorScope
final class MemberTypes {

    public static final MemberType ERROR_TYPE = MemberType.of("!");

    private final Errors errorReporter;
    private final Elements elementUtils;

    @Inject
    MemberTypes(Errors errorReporter, Elements elementUtils) {
        this.errorReporter = errorReporter;
        this.elementUtils = elementUtils;
    }

    /** Creates a {@link MemberType} from a {@link TypeMirror}, or empty if validation fails. */
    public Optional<MemberType> create(TypeMirror typeMirror, Element sourceElement) {
        try (Errors.Tracker errorTracker = errorReporter.createErrorTracker()) {
            MemberType typeModel = new Builder(sourceElement).build(typeMirror);
            return errorTracker.checkNoErrors(typeModel);
        }
    }

    /** Recursively builds the {@link MemberType} from the {@link TypeMirror}. */
    private class Builder implements TypeVisitor<MemberType, Void> {

        private final Element sourceElement;

        public Builder(Element sourceElement) {
            this.sourceElement = sourceElement;
        }

        public MemberType build(TypeMirror typeMirror) {
            return typeMirror.accept(this, null);
        }

        @Override
        public MemberType visitPrimitive(PrimitiveType primitiveType, Void unused) {
            return MemberType.primitiveType(primitiveType.toString());
        }

        @Override
        public MemberType visitArray(ArrayType arrayType, Void unused) {
            MemberType componentTypeModel = build(arrayType.getComponentType());
            return MemberType.arrayType(componentTypeModel);
        }

        @Override
        public MemberType visitDeclared(DeclaredType declaredType, Void unused) {
            ImportableType rawType = toImportableType(declaredType);
            List<MemberType> typeArgModels = visitTypeArguments(declaredType);
            MemberType typeModel = MemberType.declaredType(rawType, typeArgModels);
            return addTypeArgumentsToOuterTypes(declaredType, typeModel);
        }

        @Override
        public MemberType visitTypeVariable(TypeVariable typeVariable, Void unused) {
            return MemberType.typeVariable(typeVariable.toString());
        }

        @Override
        public MemberType visitWildcard(WildcardType wildcardType, Void unused) {
            Optional<TypeMirror> maybeExtendsBound = Optional.ofNullable(wildcardType.getExtendsBound());
            if (maybeExtendsBound.isPresent()) {
                TypeMirror bound = maybeExtendsBound.get();
                MemberType boundModel = build(bound);
                return MemberType.wildcardExtendsType(boundModel);
            }

            Optional<TypeMirror> maybeSuperBound = Optional.ofNullable(wildcardType.getSuperBound());
            if (maybeSuperBound.isPresent()) {
                TypeMirror bound = maybeSuperBound.get();
                MemberType boundModel = build(bound);
                return MemberType.wildcardSuperType(boundModel);
            }

            return MemberType.wildcardType();
        }

        @Override
        public MemberType visitNoType(NoType noType, Void unused) {
            return error("void type not allowed");
        }

        @Override
        public MemberType visitError(ErrorType errorType, Void unused) {
            return error("type failed to compile");
        }

        @Override
        public MemberType visit(TypeMirror typeMirror, Void unused) {
            return error("unexpected: type");
        }

        @Override
        public MemberType visitNull(NullType nullType, Void unused) {
            return error("unexpected: null type");
        }

        @Override
        public MemberType visitExecutable(ExecutableType executableType, Void unused) {
            return error("unexpected: executable type");
        }

        @Override
        public MemberType visitUnion(UnionType unionType, Void unused) {
            // Union types are only used in catch statements.
            return error("unexpected: union type");
        }

        @Override
        public MemberType visitIntersection(IntersectionType intersectionType, Void unused) {
            // Intersection types are only used in casts.
            return error("unexpected: intersection type");
        }

        @Override
        public MemberType visitUnknown(TypeMirror typeMirror, Void unused) {
            return error("unexpected: unknown type");
        }

        /** Converts a {@link DeclaredType} to an {@link ImportableType}. */
        private ImportableType toImportableType(DeclaredType declaredType) {
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            String binaryName = elementUtils.getBinaryName(typeElement).toString();
            return ImportableType.of(binaryName);
        }

        /** Visits the type arguments for a {@link DeclaredType}. */
        private List<MemberType> visitTypeArguments(DeclaredType declaredType) {
            return declaredType.getTypeArguments().stream().map(this::build).toList();
        }

        /** Adds type arguments to outer types that are generic. */
        private MemberType addTypeArgumentsToOuterTypes(DeclaredType declaredType, MemberType typeModel) {
            DeclaredType outerDeclaredType = declaredType;
            while (outerDeclaredType.getEnclosingType().getKind() != TypeKind.NONE) {
                outerDeclaredType = (DeclaredType) outerDeclaredType.getEnclosingType();
                if (outerDeclaredType.getTypeArguments().isEmpty()) {
                    continue;
                }

                ImportableType rawOuterType = toImportableType(outerDeclaredType);
                List<MemberType> outerTypeArgModels = visitTypeArguments(outerDeclaredType);
                typeModel = typeModel.addTypeArgumentsToOuterType(rawOuterType, outerTypeArgModels);
            }
            return typeModel;
        }

        /** Reports an error and returns an error type. */
        private MemberType error(String message) {
            errorReporter.error(message, sourceElement);
            return ERROR_TYPE;
        }
    }
}
