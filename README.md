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

[`ImmutableLiteProcessor`](immutable-processor/src/main/java/org/example/immutable/processor/ImmutableLiteProcessor.java)

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

- `ImmutableLiteProcessor` implements a single method: `void process(TypeElement annotatedElement) throws Exception`
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

[`ImmutableLiteProcessor`](immutable-processor/src/main/java/org/example/immutable/processor/ImmutableLiteProcessor.java)
runs both stages:

```java
@Override
protected void process(TypeElement typeElement) throws IOException {
    Optional<ImmutableImpl> maybeImpl = implFactory.create(typeElement);
    if (maybeImpl.isEmpty()) {
        return;
    }
    ImmutableImpl impl = maybeImpl.get();
    generator.generateSource(impl, typeElement);
}
```

[`ImmutableImpl`](immutable-processor/src/main/java/org/example/immutable/processor/model/ImmutableImpl.java)
lives in the [`org.example.immutable.processor.model`](immutable-processor/src/main/java/org/example/immutable/processor/model) package.

- Types in this package are `@Value.Immutable` interfaces.
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

[`ProcessorModule`](processor/src/main/java/org/example/processor/base/ProcessorModule.java)
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

[`Diagnostics`](processor/src/main/java/org/example/processor/diagnostic/Diagnostics.java)
is used to report any diagnostics, including compilation errors. It serves as a wrapper around `Messager`.

The main reason it wraps `Messager` is to enable error tracking.
The error tracker converts the result to `Optional.empty()` if `Diagnostics.add(Diagnostic.Kind.ERROR, ...)` is called.
This allows processing to continue for non-fatal errors; compilers don't stop on the first error.
See this condensed snippet from
[`ImmutableImpls`](immutable-processor/src/main/java/org/example/immutable/processor/modeler/ImmutableImpls.java):

```java
try (Diagnostics.ErrorTracker errorTracker = diagnostics.trackErrors()) {
    // [snip]
    ImmutableImpl impl = ImmutableImpl.of(type, members);
    return errorTracker.checkNoErrors(impl);
}
```

###  Upstream Design

So how do we work upstream from `ImmutableLiteProcessor` to `ImmutableProcessor`?

These classes rely on generic infrastructure in the
[`org.example.processor.base`](processor/src/main/java/org/example/processor/base) package:

- [`LiteProcessor`](processor/src/main/java/org/example/processor/base/LiteProcessor.java) interface
  - Lightweight, simple version of Java's
    [`Processor`](https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/annotation/processing/Processor.html)
  - Contains a single method:
    `void process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws Exception`
- [`IsolatingLiteProcessor<E extends Element>`](processor/src/main/java/org/example/processor/base/IsolatingLiteProcessor.java) abstract class
  - Partially implements `LiteProcessor`
  - Designed for isolating annotation processors where each output is generated from a single input
  - Contains a single abstract method: `void process(E annotatedElement) throws Exception`
- [`AdapterProcessor`](immutable-processor/src/main/java/org/example/immutable/processor/ImmutableLiteProcessor.java) abstract class
  - Partially implements `Processor`
  - Adapts a `LiteProcessor` to a `Processor`
  - The `LiteProcessor` is provided by an abstract method:
    `LiteProcessor createLiteProcessor(ProcessingEnvironment processingEnv)`

Here is how the annotation processor for `@Immutable` consumes this infrastructure:

- [`ImmutableLiteProcessor`](immutable-processor/src/main/java/org/example/immutable/processor/ImmutableProcessor.java)
  extends `IsolatingLiteProcessor<TypeElement>`
- [`ImmutableProcessor`](immutable-processor/src/main/java/org/example/immutable/processor/ImmutableProcessor.java)
  extends `AdapterProcessor`

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

`TestCompiler.create()` has another overload: `TestCompiler.create(Class<? extends LiteProcessor> liteProcessorClass)`

- It uses [`TestImmutableProcessor`](immutable-processor/src/test/java/org/example/immutable/processor/test/TestImmutableProcessor.java),
  which uses a custom implementation of `LiteProcessor`.
- A unit test can create a test implementation of `LiteProcessor`.
- One slight drawback is that every test implementation of `LiteProcessor` needs to be added to
  [`TestProcessorModule`](immutable-processor/src/test/java/org/example/immutable/processor/test/TestProcessorModule.java).

#### Generating Resource Files

[`TestResources`](immutable-processor/src/test/java/org/example/immutable/processor/test/TestResources.java)
handles the details of saving Java objects to generated resource files and then loading those objects.

See this snippet from
[`ImmutableImplsTest.TestLiteProcessor`](immutable-processor/src/test/java/org/example/immutable/processor/modeler/ImmutableImplsTest.java),
which saves the Java object:

```java
@Override
protected void process(TypeElement typeElement) {
    implFactory.create(typeElement).ifPresent(impl -> TestResources.saveObject(filer, typeElement, impl));
}
```

...and this snippet from `ImmutableImplsTest`, which loads the Java object and verifies it:

```java
private void create(String sourcePath, ImmutableImpl expectedImpl) throws Exception {
    Compilation compilation = TestCompiler.create(TestLiteProcessor.class).compile(sourcePath);
    ImmutableImpl impl = TestResources.loadObjectForSource(compilation, sourcePath, new TypeReference<>() {});
    assertThat(impl).isEqualTo(expectedImpl);
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
