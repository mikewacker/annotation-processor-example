plugins {
    id("org.example.immutable.java-conventions")
    application
}

dependencies {
    compileOnly(project(":immutable-annotations"))
    annotationProcessor(project(":immutable-processor"))
}

application {
    mainClass.set("org.example.immutable.example.Main")
}
