package org.example.immutable.processor.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.io.CharStreams;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.junit.jupiter.api.Test;

public final class ImmutableGeneratorTest {

    @Test
    public void generateSourceFile_Rectangle() throws IOException {
        generateSourceFile(
                TestImmutableImpls.rectangle(), "test.ImmutableRectangle", "generated/test/ImmutableRectangle.java");
    }

    @Test
    public void generateSourceFile_ColoredRectangle() throws IOException {
        generateSourceFile(
                TestImmutableImpls.coloredRectangle(),
                "test.ImmutableColoredRectangle",
                "generated/test/ImmutableColoredRectangle.java");
    }

    @Test
    public void generateSourceFile_Empty() throws IOException {
        generateSourceFile(TestImmutableImpls.empty(), "test.ImmutableEmpty", "generated/test/ImmutableEmpty.java");
    }

    private void generateSourceFile(ImmutableImpl impl, String generatedSourceName, String expectedGeneratedSourcePath)
            throws IOException {
        Map<String, StringWriter> filesystem = new HashMap<>();
        ImmutableGenerator generator = createImmutableGenerator(filesystem);
        generator.generateSourceFile(impl, mock(TypeElement.class));
        String sourceCode = getGeneratedSourceCode(filesystem, generatedSourceName);
        String expectedSourceCode = loadExpectedSourceCode(expectedGeneratedSourcePath);
        assertThat(sourceCode).isEqualTo(expectedSourceCode);
    }

    private static String getGeneratedSourceCode(Map<String, StringWriter> filesystem, String sourceName) {
        assertThat(filesystem).containsKey(sourceName);
        return filesystem.get(sourceName).toString();
    }

    private static String loadExpectedSourceCode(String sourcePath) throws IOException {
        FileObject file = JavaFileObjects.forResource(sourcePath);
        try (Reader reader = file.openReader(false)) {
            return CharStreams.toString(reader);
        }
    }

    private static ImmutableGenerator createImmutableGenerator(Map<String, StringWriter> filesystem) {
        Filer filer = createFiler(filesystem);
        return new ImmutableGenerator(filer);
    }

    /**
     * Creates a mock {@link Filer} backed by a mock, in-memory filesystem
     * where each source name is mapped to a {@link StringWriter} that stores the file's contents.
     *
     * <p>The mock {@link Filer} implements {@link Filer#createSourceFile(CharSequence, Element...)}.</p>
     */
    private static Filer createFiler(Map<String, StringWriter> filesystem) {
        try {
            Filer filer = mock(Filer.class);
            when(filer.createSourceFile(any(), any())).thenAnswer(invocation -> {
                String sourceName = invocation.getArgument(0);
                Writer writer = filesystem.computeIfAbsent(sourceName, k -> new StringWriter());
                return createSourceFileObject(writer);
            });
            return filer;
        } catch (IOException e) {
            throw new RuntimeException("unexpected", e);
        }
    }

    /**
     * Creates a mock {@link JavaFileObject} that returns the provided {@link Writer}
     * when {@link JavaFileObject#openWriter} is called.
     */
    private static JavaFileObject createSourceFileObject(Writer writer) {
        try {
            JavaFileObject sourceFileObject = mock(JavaFileObject.class);
            when(sourceFileObject.openWriter()).thenReturn(writer);
            return sourceFileObject;
        } catch (IOException e) {
            throw new RuntimeException("unexpected", e);
        }
    }
}
