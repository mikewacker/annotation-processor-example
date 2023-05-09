# Writing an Annotation Processor

What is an annotation processor? It's code that writes code. In fact, you may have already used an annotation processor,
such as [Immutables][org.immutables] or [Dagger][com.google.dagger].
For example, you can annotate an interface with `@Value.Immutable`, and Immutables will generate an implementation.

But what if you want to write (and test) an annotation processor?
This project demystifies that process and provides a reference example.

## Example

[`ImmutableProcessor`][ImmutableProcessor] generates a simplified implementation of an immutable interface.

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
  - I.e., you should use [Immutables][org.immutables] in the real world.

## Quickstart

### I just want to jump into the code. Where do I start?

[`ImmutableLiteProcessor`][ImmutableLiteProcessor]

### How do I debug the annotation processor?

- `./gradlew -Dorg.gradle.debug=true --no-daemon :immutable-example:clean :immutable-example:compileJava`
  - From there, you can attach a debugger to Gradle.
  - If you use the [Gradle Error Prone plugin][net.ltgt.errorprone] with JDK 16+,
    you will also need to [add some JVM args](gradle.properties).
- You could also debug a test written with [Compile Testing][com.google.testing.compile].

### Where can the generated sources be found?

`immutable-example/build/generated/sources/annotationProcessor/java/main`

## Design

We will start with [`ImmutableLiteProcessor`][ImmutableLiteProcessor] and work downstream from there:

- `ImmutableLiteProcessor` implements a single method: `void process(TypeElement annotatedElement) throws Exception`
- The `TypeElement` corresponds to a type that is annotated with `@Immutable`.

### Overview

[`ImmutableLiteProcessor`][ImmutableLiteProcessor] processes an interface annotated with `@Immutable` in two stages:

1. The `modeler` stage converts the `TypeElement` to an `ImmutableImpl`.
   - The entry point for this stage is [`ImmutableImpls`][ImmutableImpls].
   - The code lives in the [`org.example.immutable.processor.modeler`][modeler] package.
2. The `generator` stage converts the `ImmutableImpl` to source code.
   - The entry point for this stage is [`ImmutableGenerator`][ImmutableGenerator].
   - The code lives in the [`org.example.immutable.processor.generator`][generator] package.

[`ImmutableImpl`][ImmutableImpl] lives in the [`org.example.immutable.processor.model`][model] package:

- Types in this package are [`@Value.Immutable`][org.immutables] interfaces that can easily be created directly.
- Types in this package are designed to be serializable (via [Jackson][com.fasterxml.jackson]).
- Types in this package have corresponding types in the `modeler` and `generator` packages.

The implementation of the annotation processor is split into two projects:

- [`processor`](processor) contains generic, reusable logic for annotation processing.
- [`immutable-processor`](immutable-processor) contains logic specific to the `@Immutable` annotation.

### Processing Environment

Annotation processors will use tools that are provided via Java's [`ProcessingEnvironment`][ProcessingEnvironment]:

- The [`Messager`][Messager] reports compilation errors (and warnings).
- The [`Filer`][Filer] creates source files.
- [`Elements`][Elements] and [`Types`][Types] provide utilities for working with `Element`'s and `TypeMirror`'s.

[`ProcessorModule`][ProcessorModule] can be used to `@Inject` these objects (via [Dagger][com.google.dagger]).

### Incremental Annotation Processing

This annotation processor generates a single output for each input.
Thus, it can be configured to support [incremental annotation processing][incremental-annotation-processing].

The following steps are needed to enable incremental annotation processing:

- Use `CLASS` or `RUNTIME` retention for the annotation. (The default is `CLASS`.)
- Use [`gradle-incap-helper`][net.ltgt.gradle.incap] to enable incremental annotation processing.
- Include the originating element when creating a file via `Filer.createSourceFile()`.

### Reporting Compilation Errors

[`Diagnostics`][Diagnostics] is used to report any diagnostics, including compilation errors.
It serves as a wrapper around [`Messager`][Messager].

The main reason that `Diagnostics` wraps `Messager` is to enable error tracking.
The error tracker converts the result to `Optional.empty()` if `Diagnostics.add(Diagnostic.Kind.ERROR, ...)` is called.
This allows processing to continue for non-fatal errors; compilers don't stop on the first error.

See this condensed snippet from [`ImmutableImpls`][ImmutableImpls]:

```java
try (Diagnostics.ErrorTracker errorTracker = diagnostics.trackErrors()) {
    // [snip]
    ImmutableImpl impl = ImmutableImpl.of(type, members);
    return errorTracker.checkNoErrors(impl);
}
```

###  Upstream Design

So how do we work upstream from `ImmutableLiteProcessor` to `ImmutableProcessor`?

These classes rely on generic infrastructure in the [`org.example.processor.base`][base] package:

- [`interface LiteProcessor`][LiteProcessor]
  - Lightweight, simple version of Java's [`Processor`][Processor] interface
  - Contains a single method:
    `void process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws Exception`
- [`abstract class IsolatingLiteProcessor<E extends Element> implements LiteProcessor`][IsolatingLiteProcessor]
  - Designed for isolating annotation processors where each output is generated from a single input
  - Contains a single abstract method: `void process(E annotatedElement) throws Exception`
- [`abstract class AdapterProcessor implements Processor`][AdapterProcessor]
  - Adapts a `LiteProcessor` to a `Processor`
  - The `LiteProcessor` is provided via an abstract method:
    `LiteProcessor createLiteProcessor(ProcessingEnvironment processingEnv)`

Here is how the annotation processor for `@Immutable` consumes this infrastructure:

- [`final class ImmutableLiteProcessor extends IsolatingLiteProcessor<TypeElement>`][ImmutableLiteProcessor]
- [`final class ImmutableProcessor extends AdapterProcessor`][ImmutableProcessor] 

## End-to-End Testing

For end-to-end-testing, Google's [Compile Testing][com.google.testing.compile] framework is used.

### Setup (JDK 16+)

To use Compile Testing with JDK 16+, add these lines to [`build.gradle.kts`](immutable-processor/build.gradle.kts):

```groovy
tasks.named<Test>("test") {
    // See: https://github.com/google/compile-testing/issues/222
    jvmArgs("--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
    jvmArgs("--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
    jvmArgs("--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
}
```

### Compiling the Code

[`TestCompiler`][TestCompiler] serves as a useful wrapper around the Compile Testing framework.

See this snippet to compile a single source with `ImmutableProcessor`:

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

### Verifying the Compilation

`TestCompiler` also verifies that the compilation succeeded or failed.
By default, it expects that the compilation will succeed.

See this snippet from [`ImmutableProcessorTest`][ImmutableProcessorTest], where a compilation failure is expected:

````java
TestCompiler.create().expectingCompilationFailure().compile(sourcePath);
````

Compile Testing also provides fluent assertions. Here is the static import to use those assertions:

```java
import static com.google.testing.compile.CompilationSubject.assertThat;
```

Source files are stored in resource folders:

- Example source files live in the [`test`][resources/test] folder:
  - `Rectangle.java`
  - `ColoredRectangle.java`
  - `Empty.java`
- The expected generated source files live in the [`generated/test`][resources/generated/test] folder:
  - `ImmutableRectangle.java`
  - `ImmutableColoredRectangle.java`
  - `ImmutableEmpty.java`

## Unit Testing

Many design decisions were made with testability in mind. Case in point, most classes have a corresponding test class.

### Overview

The two-stage design of the annotation processor facilitates testability as well.

More specifically, [`ImmutableImpl`][ImmutableImpl] is a pure type that can easily be created directly:

- For testing the `modeler` stage, an `ImmutableImpl` (or other `model` types) can be used as the expected value.
- For testing the `generator` stage, an `ImmutableImpl` can be used as the starting point.

[`TestImmutableImpls`][TestImmutableImpls] provides pre-built `ImmutableImpl`'s that correspond to the examples sources:

- `TestImmutableImpls.rectangle()`
- `TestImmutableImpls.coloredRectangle()`
- `TestImmutableImpls.empty()`

### Testability Challenges

Here are the core testability challenges:

- In the `modeler` stage, it is costly and/or difficult to directly create or mock out the various `Element`'s.
- In the `generator` stage, it is (somewhat less) costly and/or difficult to directly create or mock out a `Filer`.
  - The unit tests verify the conversion of a `model` type (e.g., `ImmutableImpl`) to source code.
  - The end-to-end tests for this stage do mock out a `Filer` (via [Mockito][org.mockito]).
    See [`ImmutableGeneratorTest`][ImmutableGeneratorTest].

### `modeler` Stage

#### Strategy

The unit testing strategy for the `modeler` stage is built around custom annotation processors:

1. The custom annotation processor creates a Java object.
2. The annotation processor serializes that Java object to JSON. (Recall that `ImmutableImpl` is serializable.)
3. The annotation processor writes that JSON to a generated resource file (instead of a generated source file).
4. The test reads and deserializes that resource file to obtain the Java object.
5. The test verifies the contents of the deserialized Java object.

#### Creating Custom Annotation Processors

`TestCompiler.create()` has another overload: `TestCompiler.create(Class<? extends LiteProcessor> liteProcessorClass)`

- It uses [`TestImmutableProcessor`][TestImmutableProcessor], which uses a custom implementation of `LiteProcessor`.
- A unit test can create a test implementation of `LiteProcessor`.
  - See [`ImmutableImplsTest.TestLiteProcessor`][ImmutableImplsTest] for an example. 
  - Each test implementation of `LiteProcessor` also needs to be added to [`TestProcessorModule`][TestProcessorModule].

#### Generating Resource Files

[`TestResources`][TestResources] saves Java objects to generated resource files and then loads those objects.

See this snippet from [`ImmutableImplsTest.TestLiteProcessor`][ImmutableImplsTest], which saves the Java object:

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

<!-- internal Java file references: immutable-processor project -->
[ImmutableLiteProcessor]: immutable-processor/src/main/java/org/example/immutable/processor/ImmutableLiteProcessor.java
[ImmutableProcessor]: immutable-processor/src/main/java/org/example/immutable/processor/ImmutableProcessor.java
[generator]: immutable-processor/src/main/java/org/example/immutable/processor/generator
[ImmutableGenerator]: immutable-processor/src/main/java/org/example/immutable/processor/generator/ImmutableGenerator.java
[model]: immutable-processor/src/main/java/org/example/immutable/processor/model
[ImmutableImpl]: immutable-processor/src/main/java/org/example/immutable/processor/model/ImmutableImpl.java
[modeler]: immutable-processor/src/main/java/org/example/immutable/processor/modeler
[ImmutableImpls]: immutable-processor/src/main/java/org/example/immutable/processor/modeler/ImmutableImpls.java

[ImmutableProcessorTest]: immutable-processor/src/test/java/org/example/immutable/processor/ImmutableProcessorTest.java
[ImmutableGeneratorTest]: immutable-processor/src/test/java/org/example/immutable/processor/generator/ImmutableGeneratorTest.java
[ImmutableImplsTest]: immutable-processor/src/test/java/org/example/immutable/processor/modeler/ImmutableImplsTest.java
[TestCompiler]: immutable-processor/src/test/java/org/example/immutable/processor/test/TestCompiler.java
[TestImmutableImpls]: immutable-processor/src/test/java/org/example/immutable/processor/test/TestImmutableImpls.java
[TestImmutableProcessor]: immutable-processor/src/test/java/org/example/immutable/processor/test/TestImmutableProcessor.java
[TestProcessorModule]: immutable-processor/src/test/java/org/example/immutable/processor/test/TestProcessorModule.java
[TestResources]: immutable-processor/src/test/java/org/example/immutable/processor/test/TestResources.java
[resources/test]: immutable-processor/src/test/resources/test
[resources/generated/test]: immutable-processor/src/test/resources/generated/test

<!-- internal Java file references: processor project -->
[base]: processor/src/main/java/org/example/processor/base
[AdapterProcessor]: processor/src/main/java/org/example/processor/base/AdapterProcessor.java
[IsolatingLiteProcessor]: processor/src/main/java/org/example/processor/base/IsolatingLiteProcessor.java
[LiteProcessor]: processor/src/main/java/org/example/processor/base/LiteProcessor.java
[ProcessorModule]: processor/src/main/java/org/example/processor/base/ProcessorModule.java
[Diagnostics]: processor/src/main/java/org/example/processor/diagnostic/Diagnostics.java

<!-- external Java file references -->
[Filer]: https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/annotation/processing/Filer.html
[Messager]: https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/annotation/processing/Messager.html
[ProcessingEnvironment]: https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/annotation/processing/ProcessingEnvironment.html
[Processor]: https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/annotation/processing/Processor.html
[Elements]: https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/lang/model/util/Elements.html
[Types]: https://docs.oracle.com/en/java/javase/17/docs/api/java.compiler/javax/lang/model/util/Types.html

<!-- external project references -->
[com.fasterxml.jackson]: https://github.com/FasterXML/jackson
[com.google.dagger]: https://dagger.dev/
[com.google.testing.compile]: https://github.com/google/compile-testing
[net.ltgt.errorprone]: https://github.com/tbroyer/gradle-errorprone-plugin
[net.ltgt.gradle.incap]: https://github.com/tbroyer/gradle-incap-helper
[org.immutables]: https://immutables.github.io/
[org.mockito]: https://site.mockito.org/

<!--- miscellaneous references -->
[incremental-annotation-processing]: https://docs.gradle.org/current/userguide/java_plugin.html#sec:incremental_annotation_processing
