# Writing an Annotation Processor

What is an annotation processor? It's code that writes code. In fact, you may have already used an annotation processor,
such as [Immutables](https://immutables.github.io/) or [Dagger](https://dagger.dev/).
For example, you can annotate an interface with `@Value.Immutable`, and Immutables will generate an implementation.

But what if you want to write (and test) an annotation processor?
This project demystifies that process and provides a reference example.

## Example

This annotation processor, `ImmutableProcessor`, generates a simplified implementation of an immutable interface.

For example, you can create an interface annotated with `@Immutable`, like this:

```java
package org.example.immutable.example;

import org.example.immutable.Immutable;

@Immutable
public interface Rectangle {

    static Rectangle of(double width, double height) {
        return new ImmutableRectangle(width, height);
    }

    double width();

    double height();

    default double area() {
        return width() * height();
    }
}
```

...and `ImmutableProcessor` will generate this implementation:

```java
package org.example.immutable.example;

import javax.annotation.processing.Generated;

@Generated("org.example.immutable.processor.ImmutableProcessor")
class ImmutableRectangle implements Rectangle {

    private final double width;
    private final double height;

    ImmutableRectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double width() {
        return width;
    }

    @Override
    public double height() {
        return height;
    }
}
```

(A real-world annotation processor would also implement `equals()`, `hashCode()`, and `toString()`, among other things.)

## Goals and Non-Goals

### Goals

- Demystify the process of writing an annotation processor.
- Include enough features to demonstrate the complexity of writing an annotation processor.
- Provide guidance for how to...
  - ...debug an annotation processor.
  - ...design an annotation processor.
  - ...unit-test an annotation processor.

### Non-Goals

- Build an annotation processor that should be used in the real world.
  - I.e., you should use [Immutables](https://immutables.github.io/) in the real world.

## Quickstart

### I just want to jump into the code. Where do I start?

- [`ImmutableLiteProcessor`](immutable-processor/src/main/java/org/example/immutable/processor/ImmutableLiteProcessor.java)
  is a good entry point.
- You could also look at the [individual PRs](https://github.com/mikewacker/annotation-processor-example/pulls?q=is%3Apr+is%3Aclosed).
  - PRs #1-3 set up the basic infrastructure.
  - PRs #4-14 implement the processing logic incrementally.

### How do I debug the annotation processor?

- `./gradlew -Dorg.gradle.debug=true --no-daemon :immutable-example:clean :immutable-example:compileJava`
  - From there, you can attach a debugger to Gradle.
  - If you use the [Gradle Error Prone plugin](https://github.com/tbroyer/gradle-errorprone-plugin),
    you will also need to [add some JVM args](gradle.properties).
- You could also debug a test written with [Compile Testing](https://github.com/google/compile-testing).

### Where can the generated sources be found?

`immutable-example/build/generated/sources/annotationProcessor/java/main`

## Design

We will start with [`ImmutableLiteProcessor`](immutable-processor/src/main/java/org/example/immutable/processor/ImmutableLiteProcessor.java)
and first work downstream from there.

- `ImmutableLiteProcessor` implements a single method, `process(TypeElement typeElement)`.
- The `TypeElement` corresponds to a type that is annotated with `@Immutable`.

### Overview

The annotation processor is split into two stages:

1. The `modeler` stage converts the `TypeElement` to an `ImmutableImpl`.
   - The entry point for this stage is
     [`ImmutableImpls`](immutable-processor/src/main/java/org/example/immutable/processor/modeler/ImmutableImpls.java).
   - The code lives in the
     [`org.example.immutable.processor.modeler`](immutable-processor/src/main/java/org/example/immutable/processor/modeler) package.
2. The `generator` stage converts the `ImmutableImpl` to source code.
   - The entry point for this stage is
     [`ImmutableGenerator`](immutable-processor/src/main/java/org/example/immutable/processor/generator/ImmutableGenerator.java).
   - The code lives in the
     [`org.example.immutable.processor.generator`](immutable-processor/src/main/java/org/example/immutable/processor/generator) package.

A single line of code in `ImmutableLiteProcessor` runs both stages.

```java
implFactory.create(typeElement).ifPresent(impl -> generator.generateSource(impl, typeElement));
```

[`ImmutableImpl`](immutable-processor/src/main/java/org/example/immutable/processor/model/ImmutableImpl.java)
lives in the [`org.example.immutable.processor.model`](immutable-processor/src/main/java/org/example/immutable/processor/model) package.

- Types in this package are `@Value.Immutable` interfaces (with some `@Value.Derived` members).
- Types in this package can easily be created directly.
- Most types in this package have a corresponding factory class in the `modeler` package.

### Processing Environment

Annotation processors will use many objects that are provided as part of Java's
[`ProcessingEnvironment`](https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/annotation/processing/ProcessingEnvironment.html).

- The [`Messager`](https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/annotation/processing/Messager.html)
  reports compilation errors (and warnings).
- The [`Filer`](https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/annotation/processing/Filer.html)
  creates source files.
- [`Elements`](https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/lang/model/util/Elements.html)
  and [`Types`](https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/lang/model/util/Types.html)
  provide utilities for working with `Element`'s and `TypeMirror`'s.

[`ProcessorModule`](immutable-processor/src/main/java/org/example/immutable/processor/base/ProcessorModule.java)
provides these objects; if you need them, you can put them in an `@Inject`'able constructor.

### Incremental Annotation Processing

This annotation processor generates a single output for each input. Thus, it can be configured to support
[incremental annotation processing](https://docs.gradle.org/current/userguide/java_plugin.html#sec:incremental_annotation_processing).

The following steps are needed to enable incremental annotation processing:

- Use `CLASS` or `RUNTIME` retention for the annotation. (The default is `CLASS`.)
- Use [`gradle-incap-helper`](https://github.com/tbroyer/gradle-incap-helper) to enable incremental processing.
- Include the originating element (i.e., the annotated `TypeElement`) when creating a file via
  [`Filer.createSourceFile()`](https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/annotation/processing/Filer.html#createSourceFile(java.lang.CharSequence,javax.lang.model.element.Element...)).

### Reporting Compilation Errors

[`Errors`](immutable-processor/src/main/java/org/example/immutable/processor/error/Errors.java)
serves as a useful wrapper around the `Messager`. `Messager` should not be used directly.

The `Element` should usually be included when an error is reported.
This `Element` provides the compiler with a location for the error:
the line and column number where the `Element` occurs in the source code.

`Errors` also provides an `Errors.Tracker` class, which is an `AutoCloseable` type.
This class will convert the final result to `Optional.empty()` if an error was reported in the try-with-resources block.

See this condensed snippet from
[`ImmutableImpls`](immutable-processor/src/main/java/org/example/immutable/processor/modeler/ImmutableImpls.java):

```java
try (Errors.Tracker errorTracker = errorReporter.createErrorTracker()) {
    // [snip]
    ImmutableImpl impl = ImmutableImpl.of(type, members);
    return errorTracker.checkNoErrors(impl);
}
```

This allows processing to continue for non-fatal errors. (Your compiler typically does not stop on the first error.)

###  Upstream Design

So how do we work upstream from `ImmutableLiteProcessor` to `ImmutableProcessor`?

- Most of the infrastructure lives in the
  [`org.example.immutable.processor.base`](immutable-processor/src/main/java/org/example/immutable/processor/base) package.
- `ImmutableProcessor` and `ImmutableLiteProcessor` live in the
  [`org.example.immutable.processor`](immutable-processor/src/main/java/org/example/immutable/processor) package.

#### "Lite" Processors

- [`LiteProcessor`](immutable-processor/src/main/java/org/example/immutable/processor/base/LiteProcessor.java) interface
  - Simplifies Java's [`Processor`](https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/annotation/processing/Processor.html) interface.
  - Contains a single method, `process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)`.
- [`ImmutableBaseLiteProcessor`](immutable-processor/src/main/java/org/example/immutable/processor/base/ImmutableBaseLiteProcessor.java) abstract class
  - Implements `process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)`.
    - Finds all types annotated with `@Immutable`.
    - Processes each type with the abstract `process(TypeElement typeElement)` method.
- [`ImmutableLiteProcessor`](immutable-processor/src/main/java/org/example/immutable/processor/ImmutableLiteProcessor.java) final class
  - Implements `process(TypeElement typeElement)` to process each type.

#### "Full" Processors

- [`Processor`](https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/annotation/processing/Processor.html) interface (Java)
- [`ImmutableBaseProcessor`](immutable-processor/src/main/java/org/example/immutable/processor/base/ImmutableBaseProcessor.java) abstract class
  - Implements boilerplate processing logic.
  - Provides a `LiteProcessor` via the abstract `initLiteProcessor(ProcessingEnvironment processingEnv)` method.
  - Delegates core processing logic to the `LiteProcessor`.
- [`ImmutableProcessor`](immutable-processor/src/main/java/org/example/immutable/processor/ImmutableProcessor.java) final class
  - Implements `initLiteProcessor(ProcessingEnvironment processingEnv)` to return an `ImmutableLiteProcessor`.
  - Uses dependency injection (via [Dagger](https://dagger.dev/)) to create the `ImmutableLiteProcessor`.

## End-to-End Testing

For end-to-end-testing, Google's [Compile Testing](https://github.com/google/compile-testing) framework is used.

- Example sources can be found in the [test](immutable-processor/src/test/resources/test) resource folder.
  - `Rectangle.java`
  - `ColoredRectangle.java`
  - `Empty.java`
- The expected generated sources can be found in the
  [generated/test](immutable-processor/src/test/resources/generated/test) resource folder.
  - `ImmutableRectangle.java`
  - `ImmutableColoredRectangle.java`
  - `ImmutableEmpty.java`

### Compiling the Code

[`TestCompiler`](immutable-processor/src/test/java/org/example/immutable/processor/test/TestCompiler.java)
serves as a useful wrapper around the Compile Testing framework.

For example, here is the snippet to compile a single source with `ImmutableProcessor`:

```java
Compilation compilation = TestCompiler.create().compile(sourcePath);
````

Without `TestCompiler`, you could also directly compile this source with this snippet:

```java
Compilation compilation = Compiler.javac()
        .withProcessors(new ImmutableProcessor())
        // Suppress this warning: "Implicitly compiled files were not subject to annotation processing."
        .withOptions("-implicit:none")
        .compile(JavaFileObjects.forResource(sourcePath));
```

### Verifying the Compilation Status

`TestCompiler` also verifies that the compilation succeeded or failed.
By default, it expects that the compilation will succeed.

See this snippet from [`ImmutableProcessorTest`](immutable-processor/src/test/java/org/example/immutable/processor/ImmutableProcessorTest.java),
where a compilation failure is expected:

````java
private void error(String sourcePath) {
    TestCompiler.create().expectingCompilationFailure().compile(sourcePath);
}
````

[`CompilationError.fromCompilation()`](immutable-processor/src/test/java/org/example/immutable/processor/test/CompilationError.java)
can be used to obtain a subject to run fluent assertions (i.e., `assertThat()`) against.

### Verifying the Generated Code

Compile Testing also provides fluent assertions. Here is the static import to use those assertions:

```java
import static com.google.testing.compile.CompilationSubject.assertThat;
```

See this snippet from `ImmutableProcessorTest`:

```java
private void compile(String sourcePath, String generatedQualifiedName, String expectedGeneratedSourcePath) {
    Compilation compilation = TestCompiler.create().compile(sourcePath);
    assertThat(compilation)
            .generatedSourceFile(generatedQualifiedName)
            .hasSourceEquivalentTo(JavaFileObjects.forResource(expectedGeneratedSourcePath));
}
```

## Unit Testing

Many design decisions were made with testability in mind. Case in point, most classes have a corresponding test class.

### Overview

The two-stage design of the annotation processor design facilitates testability as well.

More specifically, `ImmutableImpl` is a pure type that can easily be created directly.

- For testing the `modeler` stage, an `ImmutableImpl` (or its subtypes) can be used as the expected value.
- For testing the `generator` stage, an `ImmutableImpl` can be used as the starting point.

[`TestImmutableImpls`](immutable-processor/src/test/java/org/example/immutable/processor/test/TestImmutableImpls.java)
provides pre-built `ImmutableImpl`'s that correspond to the examples sources:

- `TestImmutableImpls.rectangle()`
- `TestImmutableImpls.coloredRectangle()`
- `TestImmutableImpls.empty()`

### Testability Challenges

Here are the core testability challenges:

- In the `modeler` stage, it is costly and/or difficult to directly create (or mock out) the various
  [`Element`](https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/lang/model/element/Element.html)'s.
- In the `generator` stage, it is costly and/or difficult and to directly create (or mock out) a
  [`Filer`](https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/annotation/processing/Filer.html).

In both cases, running an annotation processor is the best (known) way to obtain those objects.

### `modeler` Stage

#### Strategy

The unit testing strategy for the `modeler` stage is built around custom annotation processors.

1. The custom annotation processor creates a Java object.
2. The annotation processor serializes that Java object to JSON.
   - `ImmutableImpl` is designed to be serializable (via [Jackson](https://github.com/FasterXML/jackson)).
3. The annotation processor writes that JSON to a generated resource file (instead of a generated source file).
4. The test reads and deserializes that resource file to obtain the Java object.
5. The test verifies the contents of the deserialized Java object.

#### Creating Custom Annotation Processors

`TestCompiler.create()` has another overload, `TestCompiler.create(Class<? extends LiteProcessor> liteProcessorClass)`.

```java
/**
 * Creates a compiler with {@link ImmutableBaseProcessor} and a custom {@link LiteProcessor}.
 *
 * <p>To be used, the {@link LiteProcessor} class must also be added to {@link TestProcessorModule}.</p>
 */
public static TestCompiler create(Class<? extends LiteProcessor> liteProcessorClass) {
    Processor processor = new TestImmutableProcessor(liteProcessorClass);
    return new TestCompiler(processor);
}
```

In practice, the custom `LiteProcessor` class will usually extend `ImmutableBaseLiteProcessor`,
and `TestCompiler.TestImmutableProcessor` itself extends `ImmutableBaseProcessor`.
(`ImmutableBaseLiteProcessor` and `ImmutableBaseProcessor` were designed so that it is easy
to create multiple annotation processors without repeating yourself.)

The simplest example is [`ImmutableBaseLiteProcessorTest.TestLiteProcessor`](immutable-processor/src/test/java/org/example/immutable/processor/base/ImmutableBaseLiteProcessorTest.java),
where the Java object is a `String` containing the fully qualified name of the type.

```java
@ProcessorScope
public static final class TestLiteProcessor extends ImmutableBaseLiteProcessor {

    private final Elements elementUtils;
    private final Filer filer;

    @Inject
    TestLiteProcessor(Elements elementUtils, Filer filer) {
        this.elementUtils = elementUtils;
        this.filer = filer;
    }

    @Override
    protected void process(TypeElement typeElement) {
        String qualifiedName = typeElement.getQualifiedName().toString();
        TestResources.saveObject(filer, typeElement, elementUtils, qualifiedName);
    }
}
```

One slight drawback to this design is that every test implementation of `LiteProcessor`
needs to be added to [`TestProcessorModule`](immutable-processor/src/test/java/org/example/immutable/processor/test/TestProcessorModule.java).

```java
@Binds
@ProcessorScope
@IntoMap
@LiteProcessorClassKey(ImmutableBaseLiteProcessorTest.TestLiteProcessor.class)
LiteProcessor bindImmutableBaseLiteProcessorTestLiteProcessor(
        ImmutableBaseLiteProcessorTest.TestLiteProcessor liteProcessor);
```

#### Generating Resource Files

[`TestResources`](immutable-processor/src/test/java/org/example/immutable/processor/test/TestResources.java)
handles the details of saving Java objects to generated resource files and then loading those objects.

See this snippet (again) from `ImmutableBaseLiteProcessorTest.TestLiteProcessor`, which saves the Java object:

```java
@Override
protected void process(TypeElement typeElement) {
    String qualifiedName = typeElement.getQualifiedName().toString();
    TestResources.saveObject(filer, typeElement, elementUtils, qualifiedName);
}
```

...and this snippet from `ImmutableBaseLiteProcessorTest`, which loads the Java object and verifies it:

```java
private void getQualifiedName(String sourcePath, String expectedQualifiedName) throws Exception {
    Compilation compilation = TestCompiler.create(TestLiteProcessor.class).compile(sourcePath);
    String qualifiedName = TestResources.loadObjectForSource(compilation, sourcePath, new TypeReference<>() {});
    assertThat(qualifiedName).isEqualTo(expectedQualifiedName);
}
```

### `generator` Stage

[`SourceWriter`](immutable-processor/src/main/java/org/example/immutable/processor/generator/SourceWriter.java)
does most of the work, namely `SourceWriter.writeSource(Writer writer, ImmutableImpl impl)`.

- For [`ImmutableGenerator`](immutable-processor/src/main/java/org/example/immutable/processor/generator/ImmutableGenerator.java) in `main`:
  - The `Writer` is sourced from the `Filer`.
    - `Filer.createSourceFile()` is called to obtain a `JavaFileObject`.
    - `JavaFileObject.openWriter()` is called to obtain a `Writer`.
  - The `ImmutableImpl` is sourced from `ImmutableImpls`.
- For [`SourceWriterTest`](immutable-processor/src/test/java/org/example/immutable/processor/generator/SourceWriterTest.java) in `test`:
  - A `StringWriter` is used so that the source code can be written to a `String`.
  - The `ImmutableImpl` is sourced from `TestImmutableImpls` (or created directly).


[`ImmutableGeneratorTest`](immutable-processor/src/test/java/org/example/immutable/processor/generator/ImmutableGeneratorTest.java)
also does some light end-to-end testing for the `generator` stage.
