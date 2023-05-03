package org.example.immutable.processor.generator;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.CharStreams;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import javax.tools.JavaFileObject;
import org.example.immutable.processor.model.ImmutableImpl;
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

    private void writeSource(ImmutableImpl impl, String expectedSourcePath) throws IOException {
        String source = writeSourceToString(impl);
        assertThat(source).isEqualTo(loadSource(expectedSourcePath));
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
