package org.example.immutable.processor.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.testing.compile.Compilation;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;
import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

/**
 * Saves Java objects to JSON resource files during annotation processing, and loads those objects as well.
 *
 * <p>A resource file is associated with a {@link TypeElement}, and its path is the same as the source's path,
 * except that it has a .json extension. For nested classes, the binary name is used.<p>
 *
 * <p>It assumes that the Java objects can be serialized to and deserialized from JSON.</p>
 */
public final class TestResources {

    private static final ObjectMapper mapper = createObjectMapper();

    /**
     * Test template that serializes and deserializes an object,
     * verifying that the deserialized object is equal to the original object.
     */
    public static <T> void serializeAndDeserialize(T t, TypeReference<T> type) throws JsonProcessingException {
        String json = mapper.writeValueAsString(t);
        T deserializedT = mapper.readValue(json, type);
        assertThat(deserializedT).isEqualTo(t);
    }

    /** Saves a corresponding object for the type to a resource file during annotation processing. */
    public static void saveObject(Filer filer, TypeElement typeElement, Elements elementUtils, Object object) {
        try {
            FileObject resourceFile = createResourceFile(filer, typeElement, elementUtils);
            saveObject(resourceFile, object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Loads an object from a generated resource file corresponding to the source path. */
    public static <T> T loadObjectForSource(Compilation compilation, String sourcePath, TypeReference<T> type)
            throws Exception {
        String resourcePath = sourcePath.replace(".java", ".json");
        return loadObjectFromGeneratedResource(compilation, resourcePath, type);
    }

    /** Loads an object from a generated resource file. */
    public static <T> T loadObjectFromGeneratedResource(
            Compilation compilation, String resourcePath, TypeReference<T> type) throws Exception {
        FileObject resourceFile = getResourceFile(compilation, resourcePath);
        return loadObject(resourceFile, type);
    }

    private static ObjectMapper createObjectMapper() {
        // @Value.Immutable uses these types behind the scenes.
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new GuavaModule());
        return mapper;
    }

    /** Creates a resource file for the type during annotation processing. */
    private static FileObject createResourceFile(Filer filer, TypeElement typeElement, Elements elementUtils)
            throws IOException {
        String binaryName = elementUtils.getBinaryName(typeElement).toString();
        int lastDotIndex = binaryName.lastIndexOf('.');
        int packageEndIndex = (lastDotIndex != -1) ? lastDotIndex : 0;
        int classIndex = (lastDotIndex != -1) ? lastDotIndex + 1 : 0;
        String packageName = binaryName.substring(0, packageEndIndex);
        String className = binaryName.substring(classIndex);
        String fileName = String.format("%s.json", className);
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
