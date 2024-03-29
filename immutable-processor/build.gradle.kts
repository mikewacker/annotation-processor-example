plugins {
    id("org.example.immutable.java-conventions")
    `java-library`
}

dependencies {
    compileOnly("com.google.auto.service:auto-service-annotations")
    compileOnly("net.ltgt.gradle.incap:incap")
    compileOnly("org.immutables:value-annotations")
    annotationProcessor("com.google.auto.service:auto-service")
    annotationProcessor("com.google.dagger:dagger-compiler")
    annotationProcessor("net.ltgt.gradle.incap:incap-processor")
    annotationProcessor("org.immutables:value")

    implementation(project(":immutable-annotations"))
    implementation(project(":processor"))
    implementation("com.google.dagger:dagger")
    implementation("com.google.guava:guava")
    implementation("javax.inject:javax.inject")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    testCompileOnly("net.ltgt.gradle.incap:incap")
    testCompileOnly("org.immutables:value-annotations")
    testAnnotationProcessor("com.google.dagger:dagger-compiler")
    testAnnotationProcessor("org.immutables:value-processor")

    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-guava")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    testImplementation("com.google.testing.compile:compile-testing")
    testImplementation("org.mockito:mockito-core")
}

tasks.named<Test>("test") {
    // See: https://github.com/google/compile-testing/issues/222
    jvmArgs("--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
    jvmArgs("--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
    jvmArgs("--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
}
