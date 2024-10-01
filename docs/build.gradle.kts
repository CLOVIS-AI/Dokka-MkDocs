
plugins {
    alias(opensavvyConventions.plugins.base)
    alias(opensavvyConventions.plugins.root)

    // Some plugins *must* be configured on the root project.
    // In these cases, we explicitly tell Gradle not to apply them.
    alias(opensavvyConventions.plugins.aligned.kotlin) apply false
}
