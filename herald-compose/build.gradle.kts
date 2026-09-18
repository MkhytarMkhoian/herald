plugins {
    id("herald.android.compose")
    id("herald.publish")
}

android {
    namespace = "io.github.mkhytarmkhoian.herald.compose"

    testOptions {
        unitTests.isIncludeAndroidResources = true // Robolectric, for the Compose UI tests
    }
}

dependencies {
    api(project(":herald-core"))
    api(libs.compose.runtime)
    api(libs.compose.ui)
    api(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(project(":herald-testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.compose.foundation)
    testImplementation(libs.compose.material3)
    testImplementation(libs.compose.ui.test.junit4)
    testImplementation(libs.compose.ui.test.manifest)
}
