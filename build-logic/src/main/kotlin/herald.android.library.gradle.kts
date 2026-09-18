import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Android library — used by every vendor adapter module.
 *
 * Adapters are Android libraries because that is where platform types legitimately live:
 * `Bundle` in Firebase, `Application`/`Activity` lifecycle callbacks in Adjust.
 */
plugins {
    id("com.android.library")
}

extensions.configure<LibraryExtension> {
    compileSdk = 36

    defaultConfig {
        minSdk = 23
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

extensions.configure<KotlinAndroidProjectExtension> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }

    explicitApi()
}
