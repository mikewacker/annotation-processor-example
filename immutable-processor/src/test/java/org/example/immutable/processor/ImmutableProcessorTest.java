package org.example.immutable.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.example.immutable.processor.test.TestCompiler;
import org.junit.jupiter.api.Test;

public final class ImmutableProcessorTest {

    @Test
    public void compile_Empty() {
        compile("test/Empty.java", "test.ImmutableEmpty", "generated/test/ImmutableEmpty.java");
    }

    @Test
    public void compile_Rectangle() {
        compile("test/Rectangle.java", "test.ImmutableRectangle", "generated/test/ImmutableRectangle.java");
    }

    @Test
    public void compile_ColoredRectangle() {
        compile(
                "test/ColoredRectangle.java",
                "test.ImmutableColoredRectangle",
                "generated/test/ImmutableColoredRectangle.java");
    }

    private void compile(String sourcePath, String generatedSourceName, String expectedGeneratedSourcePath) {
        Compilation compilation = TestCompiler.create().compile(sourcePath);
        assertThat(compilation)
                .generatedSourceFile(generatedSourceName)
                .hasSourceEquivalentTo(JavaFileObjects.forResource(expectedGeneratedSourcePath));
    }

    @Test
    public void compileWithoutVerifyingSource_MethodSources() throws IOException {
        compileWithoutVerifyingSource(getSourcePaths("test/method"));
    }

    @Test
    public void compileWithoutVerifyingSource_TypeSources() throws IOException {
        compileWithoutVerifyingSource(getSourcePaths("test/type"));
    }

    private void compileWithoutVerifyingSource(Iterable<String> sourcePaths) {
        TestCompiler.create().compile(sourcePaths);
    }

    @Test
    public void error() {
        error("test/type/error/Class.java");
    }

    private void error(String sourcePath) {
        TestCompiler.create().expectingCompilationFailure().compile(sourcePath);
    }

    /** Gets all sources in the source directory. */
    private static Iterable<String> getSourcePaths(String sourceDirPath) throws IOException {
        Path dirPath = Paths.get(JavaFileObjects.forResource(sourceDirPath).toUri());
        // dirPath is an absolute path, so we must convert absolute paths back to relative paths.
        int relativeIndex = dirPath.toString().length() - sourceDirPath.length();
        try (Stream<Path> paths = Files.walk(dirPath, 1)) {
            return paths.map(Path::toString)
                    .map(path -> path.substring(relativeIndex))
                    .filter(path -> path.endsWith(".java"))
                    .toList();
        }
    }
}
