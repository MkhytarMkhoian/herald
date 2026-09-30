import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

// The code on the website. Doc pages include marked sections of these files, and `build` compiles
// them, so an API change that breaks an example breaks the build instead of the docs.
//
// Not published and not an API: examples read like app code, so explicit API mode is off.
plugins {
    id("herald.android.compose")
}

android {
    namespace = "io.github.mkhytarmkhoian.herald.samples"
}

extensions.configure<KotlinAndroidProjectExtension> {
    explicitApi = ExplicitApiMode.Disabled
}

dependencies {
    implementation(project(":herald-core"))
    implementation(project(":herald-log"))
    implementation(project(":herald-firebase"))
    implementation(project(":herald-adjust"))
    implementation(project(":herald-mixpanel"))
    implementation(project(":herald-appsflyer"))
    implementation(project(":herald-amplitude"))
    implementation(project(":herald-compose"))
    implementation(project(":herald-testing"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.compose.foundation)
    implementation(libs.lifecycle.viewmodel)

    // Annotations and DSLs only: the DI examples need to compile, not to run, so no Hilt processor.
    implementation(libs.koin.core)
    implementation(libs.hilt.android)

    // The testing guide's examples are real tests, run by `build`.
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
