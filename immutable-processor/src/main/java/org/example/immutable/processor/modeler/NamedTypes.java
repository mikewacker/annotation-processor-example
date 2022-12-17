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
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.error.Errors;
import org.example.immutable.processor.model.NamedType;

/**
 * Creates {@link NamedType}'s from {@link TypeMirror}'s.
 *
 * <p>The source {@link Element} is also provided for error reporting purposes.</p>
 *
 * <p>It supports the following types:</p>
 * <ol>
 *     <li>the type declaration: both the implementing class and the implemented interface</li>
 *     <li>the return type of a getter method (and also the type of the backing field)</li>
 * </ol>
 */
@ProcessorScope
final class NamedTypes {

    private static final NamedType ERROR_TYPE = NamedType.of("?");

    private final Errors errorReporter;
    private final Elements elementUtils;

    @Inject
    NamedTypes(Errors errorReporter, Elements elementUtils) {
        this.errorReporter = errorReporter;
        this.elementUtils = elementUtils;
    }

    /** Creates an {@link NamedType}, or empty if validation fails. */
    public Optional<NamedType> create(TypeMirror type, Element sourceElement) {
        try (Errors.Tracker errorTracker = errorReporter.createErrorTracker()) {
            ElementTypeVisitor typeVisitor = new ElementTypeVisitor(sourceElement);
            NamedType typeModel = type.accept(typeVisitor, null);
            return errorTracker.checkNoErrors(typeModel);
        }
    }

    /** Recursively constructs the {@link NamedType}. */
    private class ElementTypeVisitor implements TypeVisitor<NamedType, Void> {

        private final Element sourceElement;

        ElementTypeVisitor(Element sourceElement) {
            this.sourceElement = sourceElement;
        }

        @Override
        public NamedType visit(TypeMirror type, Void unused) {
            return error("unexpected: type");
        }

        @Override
        public NamedType visitPrimitive(PrimitiveType primitiveType, Void unused) {
            return NamedType.of(primitiveType.toString());
        }

        @Override
        public NamedType visitNull(NullType nullType, Void unused) {
            // Null types are only used for the expression "null".
            return error("unexpected: null type");
        }

        @Override
        public NamedType visitArray(ArrayType arrayType, Void unused) {
            NamedType componentTypeModel = accept(arrayType.getComponentType());
            return NamedType.concat(componentTypeModel, "[]");
        }

        @Override
        public NamedType visitDeclared(DeclaredType declaredType, Void unused) {
            // Return the raw type model if no type arguments exist.
            NamedType rawTypeModel = visitDeclaredRaw(declaredType);
            List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
            if (typeArgs.isEmpty()) {
                return rawTypeModel;
            }

            // Append the type arguments.
            List<NamedType> typeArgModels = typeArgs.stream().map(this::accept).toList();
            NamedType typeArgsModel = NamedType.join(typeArgModels, ", ", "<", ">");
            return NamedType.concat(rawTypeModel, typeArgsModel);
        }

        @Override
        public NamedType visitError(ErrorType errorType, Void unused) {
            return error("type failed to compile");
        }

        @Override
        public NamedType visitTypeVariable(TypeVariable typeVariable, Void unused) {
            return NamedType.of(typeVariable.toString());
        }

        @Override
        public NamedType visitWildcard(WildcardType wildcardType, Void unused) {
            // Visit the extends bound if applicable.
            TypeMirror extendsBound = wildcardType.getExtendsBound();
            if (extendsBound != null) {
                return visitWildcardBound(extendsBound, "extends");
            }

            // Visit the super bound if applicable.
            TypeMirror superBound = wildcardType.getSuperBound();
            if (superBound != null) {
                return visitWildcardBound(superBound, "super");
            }

            // Wildcard has no bounds.
            return NamedType.of("?");
        }

        @Override
        public NamedType visitExecutable(ExecutableType executableType, Void unused) {
            // Executable types are unexpected for type declarations or return types.
            return error("unexpected: executable type");
        }

        @Override
        public NamedType visitNoType(NoType noType, Void unused) {
            return error("void type not allowed");
        }

        @Override
        public NamedType visitUnknown(TypeMirror type, Void unused) {
            return error("unexpected: unknown type");
        }

        @Override
        public NamedType visitUnion(UnionType unionType, Void unused) {
            // Union types are only used in catch statements.
            return error("unexpected: union type");
        }

        @Override
        public NamedType visitIntersection(IntersectionType intersectionType, Void unused) {
            // Type parameter bounds are represented as a list of types, not as a type which could be an intersection.
            return error("unexpected: intersection type");
        }

        /** Visits the raw type for a declared type. */
        private NamedType visitDeclaredRaw(DeclaredType declaredType) {
            // The base case is top-level type or a nested static type.
            if (declaredType.getEnclosingType().getKind() == TypeKind.NONE) {
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                String binaryName = elementUtils.getBinaryName(typeElement).toString();
                return NamedType.ofBinaryName(binaryName);
            }

            // Visit the outer type.
            DeclaredType outerDeclaredType = (DeclaredType) declaredType.getEnclosingType();
            NamedType outerTypeModel = accept(outerDeclaredType);

            // Get the names of the inner types.
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            TypeElement outerTypeElement = (TypeElement) outerDeclaredType.asElement();
            String qualifiedName = typeElement.getQualifiedName().toString();
            String outerQualifiedName = outerTypeElement.getQualifiedName().toString();
            String innerNameSuffix = qualifiedName.substring(outerQualifiedName.length());

            // Create the type model.
            return NamedType.concat(outerTypeModel, innerNameSuffix);
        }

        /** Visits a bounded wildcard. */
        private NamedType visitWildcardBound(TypeMirror bound, String boundKind) {
            NamedType boundModel = accept(bound);
            String prefix = String.format("? %s ", boundKind);
            return NamedType.concat(prefix, boundModel);
        }

        private NamedType accept(TypeMirror type) {
            return type.accept(this, null);
        }

        /** Reports an error, returning the error type. */
        private NamedType error(String message) {
            errorReporter.error(message, sourceElement);
            return ERROR_TYPE;
        }
    }
}
