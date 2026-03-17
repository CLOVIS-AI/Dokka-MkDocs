# Dokka for Material for MkDocs

This project is a Dokka plugin to embed Kotlin documentation into Material for MkDocs.

Structure:

- `renderer/src/main/kotlin/MaterialForMkDocsPlugin.kt`: Dokka plugin that generates MkDocs-style Markdown pages
- `dokka-mkdocs/src/main/kotlin/DokkaMkDocsPlugin.kt`: Gradle plugin that configures the Dokka plugin and generates the MkDocs navigation bar
- `docs/example/example-core/src/commonMain/kotlin`: Example Kotlin code for testing the plugin
- `docs/website/docs/api`: Location of the generated Markdown for the example

You can regenerate the example output by running `./gradlew :website:embedDokkaIntoMkDocs -p docs`.

Users should not require editing their `mkdocs.yml` or MkDocs overrides to be able to use this project.
