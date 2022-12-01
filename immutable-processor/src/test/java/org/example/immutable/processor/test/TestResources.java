package org.example.immutable.processor.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.testing.compile.Compilation;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;
import javax.annotation.processing.Filer;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

/**
 * Saves Java objects to JSON resource files during annotation processing, and loads those objects as well.
 *
 * <p>It assumes that the Java objects can be serialized to and deserialized from JSON.</p>
 */
public final class TestResources {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Saves a corresponding object for the type to a resource file during annotation processing.
     *
     * <p>For top-level types with the same name as the source,
     * the resource path is simply the source path, but with a .json extension.</p>
     */
    public static void saveObject(Filer filer, TypeElement typeElement, Object object) {
        try {
            FileObject resourceFile = createResourceFile(filer, typeElement);
            saveObject(resourceFile, object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads an object from a generated resource file.
     *
     * <p>It assumes that the resource path is the source path, but with a .json extension.
     * It can be used for top-level types with the same name as the source.</p>
     */
    public static <T> T loadObjectForSource(Compilation compilation, String sourcePath, TypeReference<T> type)
            throws Exception {
        String resourcePath = sourcePath.replace(".java", ".json");
        FileObject resourceFile = getResourceFile(compilation, resourcePath);
        return loadObject(resourceFile, type);
    }

    /** Creates a resource file for the type during annotation processing. */
    private static FileObject createResourceFile(Filer filer, TypeElement typeElement) throws IOException {
        // This does not work for nested types.
        PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();
        String packageName = packageElement.getQualifiedName().toString();
        String simpleName = typeElement.getSimpleName().toString();
        String fileName = String.format("%s.json", simpleName);
        return filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, fileName, typeElement);
    }

    /** Gets a resource file generated during annotation processing. */
    private static FileObject getResourceFile(Compilation compilation, String resourcePath) {
        Optional<JavaFileObject> maybeResourceFile =
                compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, resourcePath);
        assertThat(maybeResourceFile)
                .withFailMessage("resource not found: %s", resourcePath)
                .isPresent();
        return maybeResourceFile.get();
    }

    /** Serializes the object to JSON, and writes it to the resource file. */
    private static void saveObject(FileObject resourceFile, Object object) throws Exception {
        String serializedObject = mapper.writeValueAsString(object);
        try (Writer writer = resourceFile.openWriter()) {
            writer.write(serializedObject);
        }
    }

    /** Reads JSON from the resource file, and deserializes it to an object. */
    private static <T> T loadObject(FileObject resourceFile, TypeReference<T> type) throws Exception {
        try (Reader reader = resourceFile.openReader(false)) {
            return mapper.readValue(reader, type);
        }
    }

    // static class
    private TestResources() {}
}
