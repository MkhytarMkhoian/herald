plugins {
    id("herald.android.library")
    id("herald.publish")
}

android {
    namespace = "io.github.mkhytarmkhoian.herald.mixpanel"
}

dependencies {
    api(project(":herald-core"))
    api(libs.mixpanel.android)
    api(libs.kotlinx.coroutines.core)


    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
