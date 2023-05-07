package org.example.processor.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

public final class IsolatingSourceFileGeneratorTest {

    @Test
    public void generateSourceFile() throws IOException {
        StringWriter stringWriter = new StringWriter();
        IsolatingSourceFileGenerator<String, Element> generator = TestSourceFileGenerator.of(stringWriter);
        generator.generateSourceFile("code", mock(Element.class));
        String sourceCode = stringWriter.toString();
        assertThat(sourceCode).isEqualTo("code");
    }

    /** Writes the source code to a {@link StringWriter} when the source name is {@code test.Test}. */
    private static final class TestSourceFileGenerator extends IsolatingSourceFileGenerator<String, Element> {

        public static IsolatingSourceFileGenerator<String, Element> of(StringWriter stringWriter) {
            Filer filer = createFiler(stringWriter);
            return new TestSourceFileGenerator(filer);
        }

        @Override
        protected String getSourceName(String sourceCode) {
            return "test.Test";
        }

        @Override
        protected SourceGenerator<String> createSourceGenerator(String sourceCode) {
            return (writer, sc) -> writer.print(sc);
        }

        private static Filer createFiler(StringWriter stringWriter) {
            try {
                JavaFileObject sourceFileObject = mock(JavaFileObject.class);
                when(sourceFileObject.openWriter()).thenReturn(stringWriter);
                Filer filer = mock(Filer.class);
                when(filer.createSourceFile(eq("test.Test"), any())).thenReturn(sourceFileObject);
                return filer;
            } catch (IOException e) {
                throw new RuntimeException("unexpected", e);
            }
        }

        private TestSourceFileGenerator(Filer filer) {
            super(filer);
        }
    }
}
