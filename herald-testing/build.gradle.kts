plugins {
    id("herald.kotlin.library")
    id("herald.publish")
}

dependencies {
    api(project(":herald-core"))
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
}
