import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Plain Kotlin/JVM library — used by `herald-core` and `herald-testing`.
 *
 * Core is intentionally NOT an Android library. Keeping it on the JVM makes "no platform types in
 * core" a fact the build enforces rather than a rule a reviewer has to remember.
 */
plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }

    explicitApi()

}

tasks.withType<Test>().configureEach {
    useJUnit()
}
