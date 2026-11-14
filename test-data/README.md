# Module Test module

This module contains declarations that are used as regression tests for the plugin.

Each package is a test.

## Writing tests

Tests are structured as `.kt` and `.md` files. Each `.md` file is an assertion that that exact file, at that exact path, will be generated when running Dokka.

The workflow to create a new test is:
- Create a Kotlin file that contains the example you want to test.
- Create a matching Markdown file with a prototype of what you expect the output to be.
- Run the tests with `./gradlew :test-runner:check`.
- If the tests fail, diff the generated file and the expected file.
- If the expected file is not correct, edit it.
- Run the tests again to verify they are successful.
