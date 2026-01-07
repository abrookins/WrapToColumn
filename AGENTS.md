# WrapToColumn - Code Agent Configuration

An IntelliJ plugin that wraps text to a maximum line width.

## Project Overview

- **Type**: IntelliJ Platform Plugin (Kotlin/Java)
- **Build System**: Gradle with IntelliJ Plugin
- **Language**: Kotlin (JVM target 17)
- **Plugin ID**: `com.andrewbrookins.wrap_to_column`

## Prerequisites

- **Java 17+** (required for compilation and running)
- Gradle 8.5 (included via wrapper)

## Running Tests

```bash
./gradlew test
```

Tests are located in `src/test/java/com/andrewbrookins/idea/wrap/CodeWrapperTest.kt` and use JUnit 5.

## Building

### Build Plugin (Development)

```bash
./gradlew buildPlugin
```

This creates a distributable zip in `build/distributions/`.

### Build Plugin Jar

```bash
./gradlew jar
```

### Full Build with All Tasks

```bash
./gradlew build
```

## Available Gradle Tasks

| Task | Description |
|------|-------------|
| `./gradlew test` | Run unit tests |
| `./gradlew buildPlugin` | Build plugin zip for distribution |
| `./gradlew runIde` | Run IntelliJ with the plugin installed (for testing) |
| `./gradlew signPlugin` | Sign the plugin (requires certificates) |
| `./gradlew publishPlugin` | Publish to JetBrains Marketplace |
| `./gradlew clean` | Clean build artifacts |

## Releasing

### Version Bump

1. Update `version` in `build.gradle.kts`
2. Update `<version>` in `src/main/resources/META-INF/plugin.xml`
3. Add changelog entry in `plugin.xml` under `<change-notes>`

### Signing Requirements

The following environment variables are required for signing:

- `CERTIFICATE_CHAIN` - Plugin signing certificate chain
- `PRIVATE_KEY` - Private key for signing
- `PRIVATE_KEY_PASSWORD` - Password for the private key

### Publishing Requirements

- `PUBLISH_TOKEN` - JetBrains Marketplace API token

### Release Process

```bash
# 1. Bump version in build.gradle.kts and plugin.xml
# 2. Update changelog in plugin.xml
# 3. Build and sign
./gradlew buildPlugin signPlugin

# 4. Publish to JetBrains Marketplace
./gradlew publishPlugin
```

### Manual Release to GitHub

1. Create a GitHub release with tag matching the version
2. Attach the zip file from `build/distributions/`

## Project Structure

```
src/
├── main/
│   ├── java/com/andrewbrookins/idea/wrap/
│   │   ├── CodeWrapper.kt          # Core wrapping logic
│   │   ├── WrapAction.kt           # Wrap Line action
│   │   ├── WrapParagraphAction.kt  # Wrap Paragraph action
│   │   ├── WrappingAlgorithms.kt   # Greedy/min-raggedness algorithms
│   │   ├── utils.kt                # Utility functions
│   │   └── config/                 # Settings classes
│   └── resources/META-INF/
│       └── plugin.xml              # Plugin descriptor
└── test/
    └── java/com/andrewbrookins/idea/wrap/
        └── CodeWrapperTest.kt      # Unit tests
```

## IntelliJ Compatibility

- Minimum: IntelliJ 2022.2.5 (build 222)
- Tested with: IC (Community Edition)
- Works with all IntelliJ-based IDEs (PyCharm, WebStorm, etc.)

## Development Workflow

1. Make changes to source files
2. Run `./gradlew test` to verify tests pass
3. Run `./gradlew runIde` to test in a live IDE
4. Run `./gradlew buildPlugin` to create distribution

## Debugging

Run the IDE with the plugin in debug mode:

```bash
./gradlew runIde --debug-jvm
```

Then attach a remote debugger to port 5005.

