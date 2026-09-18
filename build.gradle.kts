// Root build file. Deliberately thin: everything shared lives in `build-logic` convention
// plugins so that each module declares only what is genuinely its own.
//
// `group` and `version` are set for every project in gradle.properties.
//
// ---------------------------------------------------------------------------------------------
// Public API validation, and the hole in it (2026-09-09)
// ---------------------------------------------------------------------------------------------
// The validator dumps each module's public API to `<module>/api/*.api`, checked in and compared by
// `apiCheck` on every `check`. It is the other half of `explicitApi()`: that stops a symbol
// becoming public by accident, this makes an existing symbol changing or disappearing show up as a
// line in a pull request diff — including changes that are source-compatible but binary-
// incompatible, such as adding a defaulted parameter to `HeraldBuilder.provider`.
//
// When an API change is intended: `./gradlew apiDump`, then commit the result.
//
// **It covers `herald-core` and `herald-testing` only.** Under AGP 9 the Android
// modules are not validated by anything. binary-compatibility-validator finds Android modules by
// hooking the `org.jetbrains.kotlin.android` plugin id, and AGP 9 does not apply it — it brings
// Kotlin in through `KotlinBaseApiPlugin` instead, so the hook never fires and no tasks are
// registered. Kotlin's own `abiValidation` is worse: it registers `updateKotlinAbi` for those
// modules, and the task then succeeds while writing no file at all.
//
// The gap is accepted rather than fixed because the alternative is holding Gradle and AGP back
// indefinitely. It is made loud by `verifyApiValidationCoverage` below — the original failure mode
// was that `apiCheck` passed while quietly covering a third of the codebase.
//
// The `api/*.api` files for the Android modules are kept, but they are **frozen snapshots
// taken under AGP 8 and nothing compares against them**. They stay because they cannot be
// regenerated on this toolchain, so they are the only baseline available for the day the validator
// learns about AGP 9.
//
// The Kotlin and Android plugins used to be declared here (unapplied) so the validator could see
// `KotlinAndroidProjectExtension` on the root buildscript classpath — without them it failed with
// `NoClassDefFoundError`. Under AGP 9 the validator never takes its Android path, so it never
// loads those classes and the declarations are gone. If it ever gains AGP 9 support, that error
// can come back; re-adding `kotlin.jvm`, `kotlin.android` and `android.library` here as
// `apply false` is the fix.
plugins {
    alias(libs.plugins.binary.compatibility.validator)
}

// Modules the validator cannot see; see the note above. Listing them explicitly makes the gap a
// decision recorded in the build rather than something you notice by reading a task list.
val unvalidatedByDesign = setOf(
    "herald-adjust",
    "herald-firebase",
    "herald-mixpanel",
    "herald-compose",
)

// Fails the build if API-validation coverage drifts from what the comment above claims — in either
// direction. Losing a module silently is the bug this exists to prevent; gaining one means the
// validator has learned about AGP 9 and the frozen dumps should be regenerated.
val verifyApiValidationCoverage by tasks.registering {
    group = "verification"
    description = "Asserts which modules the binary-compatibility-validator actually covers."
    val validated = provider {
        subprojects.filter { it.tasks.findByName("apiCheck") != null }.map { it.name }.toSet()
    }
    val expected = provider { subprojects.map { it.name }.toSet() - unvalidatedByDesign }
    doLast {
        val actual = validated.get()
        val want = expected.get()
        if (actual != want) {
            val lost = want - actual
            val gained = actual - want
            error(
                buildString {
                    appendLine("Public API validation coverage has changed.")
                    if (lost.isNotEmpty()) {
                        appendLine("  Silently dropped: $lost")
                        appendLine("  `apiCheck` would now pass without checking them. Do not ignore this.")
                    }
                    if (gained.isNotEmpty()) {
                        appendLine("  Newly covered: $gained")
                        appendLine("  Drop them from `unvalidatedByDesign` in build.gradle.kts and run `./gradlew apiDump`.")
                    }
                }
            )
        }
    }
}

subprojects {
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(verifyApiValidationCoverage)
    }
}
