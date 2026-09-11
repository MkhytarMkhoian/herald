plugins {
    id("herald.android.library")
    id("herald.publish")
}

android {
    namespace = "io.github.mkhytarmkhoian.herald.adjust"
}

dependencies {
    api(project(":herald-core"))
    // `api`, not `implementation`: the consumer constructs the adapter with an `AdjustInstance` and
    // an `AdjustConfig` they own, so the vendor types are part of this module's compile-time surface.
    api(libs.adjust.android)
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
