# Module Dokka: Material for MkDocs format

Gradle plugin to configure Dokka 2+ with Material for MkDocs.

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.dokka.mkdocs%22%20AND%20a:%22dokka-mkdocs%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.dokka.mkdocs/dokka-mkdocs.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/alpha/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.dokka.mkdocs/dokka-mkdocs"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

This is a Gradle plugin using Dokka to extract Kotlin documentation and embed it in a Material for MkDocs site. To learn more about the project, see [the documentation](https://opensavvy.gitlab.io/automation/dokka-material-mkdocs/docs/).

## Configuration

Create a `build.gradle.kts` file in the same directory as your `mkdocs.yml` file:
```kotlin
id("dev.opensavvy.dokka-mkdocs") version "VERSION HERE" 

dependencies {
    // Embeds the documentation from project :foo-bar
    dokka(project("foo-bar"))
}
```

Add each project that you want to document in the `dependencies {}` block. Each project mentioned in that way must have the Dokkatoo plugin applied.

Add the directory in which you created the project in the `settings.gradle.kts` file:
```kotlin
// Everything else…

include("docs") // or whatever other path you used
```

In `mkdocs.yml`, add the marker of where you want the documentation to be generated:
```yaml
# Everything else…

nav:
  - Home: # your existing pages…
      - index.md
      - setup.md

  - Another: another.md # your existing pages…

# !!! EMBEDDED DOKKA START, DO NOT COMMIT !!! #
# !!! EMBEDDED DOKKA END, DO NOT COMMIT !!! #

  - Whatever: whatever.md # your existing pages…
```

## Running

Run the following command, replacing `docs` by the project path of where you put your documentation:
```shell
./gradlew docs:embedDokkaIntoMkDocs
```
