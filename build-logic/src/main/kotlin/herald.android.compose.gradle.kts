import com.android.build.api.dsl.LibraryExtension

/**
 * Compose on top of [herald.android.library]. The Compose compiler ships as a Kotlin plugin,
 * versioned with Kotlin, so there is no extension version to pin. Compose 1.12 compiles against
 * API 37, one above the adapters; that raises nothing for consumers, whose own compileSdk wins.
 */
plugins {
    id("herald.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

extensions.configure<LibraryExtension> {
    compileSdk = 37

    buildFeatures {
        compose = true
    }
}
