plugins {
    id("org.example.immutable.java-conventions")
    `java-library`
}

dependencies {
    compileOnly("org.immutables:value-annotations")
    annotationProcessor("com.google.dagger:dagger-compiler")
    annotationProcessor("org.immutables:value")

    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.google.dagger:dagger")
    implementation("com.google.guava:guava")
    implementation("javax.inject:javax.inject")

    testAnnotationProcessor("com.google.dagger:dagger-compiler")

    testImplementation("com.google.guava:guava-testlib")
    testImplementation("com.google.testing.compile:compile-testing")
    testImplementation("org.mockito:mockito-core")
}
