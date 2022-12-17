package org.example.immutable.processor.generator;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.CharStreams;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.model.ImmutableMember;
import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.NamedType;
import org.example.immutable.processor.model.TopLevelType;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.junit.jupiter.api.Test;

public final class SourceWriterTest {

    @Test
    public void writeSource_Rectangle() throws IOException {
        writeSource(TestImmutableImpls.rectangle(), "generated/test/ImmutableRectangle.java");
    }

    @Test
    public void writeSource_ColoredRectangle() throws IOException {
        writeSource(TestImmutableImpls.coloredRectangle(), "generated/test/ImmutableColoredRectangle.java");
    }

    @Test
    public void writeSource_Empty() throws IOException {
        writeSource(TestImmutableImpls.empty(), "generated/test/ImmutableEmpty.java");
    }

    @Test
    public void writeSource_QualifiedTypes() throws IOException {
        writeSource(createImpl_QualifiedTypes(), "generated/test/source/ImmutableQualifiedTypes.java");
    }

    @Test
    public void writeSource_QualifiedTypesDeclaration() throws IOException {
        writeSource(
                createImpl_QualifiedTypesDeclaration(),
                "generated/test/source/ImmutableQualifiedTypesDeclaration.java");
    }

    private void writeSource(ImmutableImpl impl, String expectedSourcePath) throws IOException {
        String source = writeSourceToString(impl);
        assertThat(source).isEqualTo(loadSource(expectedSourcePath));
    }

    private static ImmutableImpl createImpl_QualifiedTypes() {
        // Create the type.
        TopLevelType rawImplType = TopLevelType.of("test.source", "ImmutableQualifiedTypes");
        TopLevelType rawInterfaceType = TopLevelType.of("test.source", "QualifiedTypes");
        Set<String> packageTypes = Set.of("QualifiedTypes", "String", "Generated", "Override");

        List<String> typeVars = List.of();
        NamedType implType = NamedType.ofTopLevelType(rawImplType);
        NamedType interfaceType = NamedType.ofTopLevelType(rawInterfaceType);

        ImmutableType type = ImmutableType.of(rawImplType, packageTypes, typeVars, implType, interfaceType);

        // Create the members
        TopLevelType stringPackageImport = TopLevelType.of("test.source", "String");
        TopLevelType stringImport = TopLevelType.ofClass(String.class);
        TopLevelType generatedPackageImport = TopLevelType.of("test.source", "Generated");

        ImmutableMember member1 = ImmutableMember.of("member1", NamedType.ofTopLevelType(stringPackageImport));
        ImmutableMember member2 = ImmutableMember.of("member2", NamedType.ofTopLevelType(stringImport));
        ImmutableMember member3 = ImmutableMember.of("member3", NamedType.ofTopLevelType(generatedPackageImport));

        // Create the implementation.
        return ImmutableImpl.of(type, List.of(member1, member2, member3));
    }

    private static ImmutableImpl createImpl_QualifiedTypesDeclaration() {
        // Create the type.
        TopLevelType rawImplType = TopLevelType.of("test.source", "ImmutableQualifiedTypesDeclaration");
        TopLevelType rawInterfaceType = TopLevelType.of("test.source", "QualifiedTypesDeclaration");
        Set<String> packageTypes = Set.of("QualifiedTypesDeclaration");

        List<String> typeVars = List.of("ImmutableQualifiedTypesDeclaration");
        NamedType implType =
                NamedType.of("%s<ImmutableQualifiedTypesDeclaration extends %s>", rawImplType, rawImplType);
        NamedType interfaceType = NamedType.of("%s<ImmutableQualifiedTypesDeclaration>", rawInterfaceType);

        ImmutableType type = ImmutableType.of(rawImplType, packageTypes, typeVars, implType, interfaceType);

        // Create the implementation.
        return ImmutableImpl.of(type, List.of());
    }

    private static String writeSourceToString(ImmutableImpl impl) {
        StringWriter writer = new StringWriter();
        SourceWriter.writeSource(writer, impl);
        return writer.toString();
    }

    private static String loadSource(String sourcePath) throws IOException {
        JavaFileObject file = JavaFileObjects.forResource(sourcePath);
        try (Reader reader = file.openReader(false)) {
            return CharStreams.toString(reader);
        }
    }
}
