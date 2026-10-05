/**
 * The API reference for the website — applied to the root project only.
 *
 * Aggregates the Dokka output of every published module into one HTML publication at
 * `build/dokka/html`, which herald-docs' build places under the site's `api/`. A module joins
 * by applying `herald.publish`; nothing is listed by hand, so a new adapter cannot be left out.
 */
plugins {
    id("org.jetbrains.dokka")
}

dokka {
    moduleName.set("Herald")
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
    }
    // Dokka shows a custom asset named logo-icon.svg in place of its own logo.
    pluginsConfiguration.html {
        customAssets.from(layout.projectDirectory.file("docs/assets/logo-icon.svg"))
        footerMessage.set("© 2026 Mkhytar Mkhoian — Apache License 2.0")
    }
}

subprojects {
    pluginManager.withPlugin("herald.publish") {
        rootProject.dependencies.add("dokka", this@subprojects)
    }
}
