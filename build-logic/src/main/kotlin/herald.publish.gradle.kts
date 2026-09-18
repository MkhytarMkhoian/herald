import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository

/**
 * Maven Central publishing for every Herald module, through the Central Portal.
 *
 * The publish plugin does the work Central's validation demands and `maven-publish` alone does
 * not: a sources jar and a javadoc jar for JVM *and* Android modules (Central rejects a module
 * without both), GPG signatures on every file, and the upload itself. Dokka renders the javadoc
 * jar, so what ships is the KDoc.
 *
 * Nothing is uploaded by `build` or `check`. `publishToMavenLocal` needs no credentials: signing is
 * required only when a task publishes to a remote repository, so an unsigned upload to Central
 * fails but the local and CI `publishToMavenLocal` never need a key. `publishToMavenCentral` needs
 * these, as Gradle properties or as environment variables prefixed `ORG_GRADLE_PROJECT_`:
 *
 * - `mavenCentralUsername` / `mavenCentralPassword` — a Central Portal user token
 * - `signingInMemoryKey` / `signingInMemoryKeyPassword` — an ASCII-armored GPG private key
 *
 * The POM carries the `licenses`, `scm`, `developers` and `url` Central requires. Coordinates are
 * `group` and `version` from gradle.properties, with the module name as the artifact id.
 */
plugins {
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
    signing
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(project.group.toString(), project.name, project.version.toString())

    // Picks AndroidSingleVariantLibrary("release") or KotlinJvm from the plugins already applied
    // by herald.android.library / herald.kotlin.library, so this must stay after them.
    configureBasedOnAppliedPlugins(
        javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationHtml"),
        sourcesJar = SourcesJar.Sources(),
    )

    pom {
        name.set(project.name)
        description.set("Herald — a pluggable analytics library for Android.")
        inceptionYear.set("2026")
        url.set("https://github.com/MkhytarMkhoian/herald")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("MkhytarMkhoian")
                name.set("Mkhytar Mkhoian")
                url.set("https://github.com/MkhytarMkhoian")
            }
        }

        scm {
            url.set("https://github.com/MkhytarMkhoian/herald")
            connection.set("scm:git:git://github.com/MkhytarMkhoian/herald.git")
            developerConnection.set("scm:git:ssh://git@github.com/MkhytarMkhoian/herald.git")
        }
    }
}

// Evaluated when the Sign tasks run, by which point the task graph is known. PublishToMavenLocal
// is not a PublishToMavenRepository, so only a remote publish makes a missing key an error.
signing {
    setRequired({ gradle.taskGraph.allTasks.any { it is PublishToMavenRepository } })
}
