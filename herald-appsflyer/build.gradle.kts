plugins {
    id("herald.android.library")
    id("herald.publish")
}

android {
    namespace = "io.github.mkhytarmkhoian.herald.appsflyer"
}

dependencies {
    api(project(":herald-core"))
    api(libs.appsflyer.android)
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
