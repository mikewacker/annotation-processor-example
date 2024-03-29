plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.18.0")
    implementation("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:3.1.0")
}
