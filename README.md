# mvn-arch

A wrapper CLI for `mvn archetype:generate`

```
Usage: mvn-arch [-hV] [COMMAND]
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  create  Create a new project from an archetype
  list    List available archetypes
```

## Archetype catalog

The list of available archetypes is fetched from the JSON file specified by `mvn-arch.catalog.url` in `application.properties`.

Default: https://raw.githubusercontent.com/sosuisen/mvn-arch-catalog/refs/heads/main/archetypes.json

## Dev mode

```bash
./mvnw clean package -DskipTests && java -Dquarkus.profile=dev -jar target/quarkus-app/quarkus-run.jar create
```

## Windows native executable

Mandrel (JDK 25+) and Visual Studio Build Tools (C/C++ workload) are required.

Switch to Mandrel with SDKMAN!:

[bash]
```bash
sdk install java 25.0.2.r25-mandrel
sdk default java 25.0.2.r25-mandrel
```

Use `sdk default` to enable mandrel in Command Prompt.

Set the `GRAALVM_HOME` user environment variable on Windows:

```
GRAALVM_HOME = %USERPROFILE%\.sdkman\candidates\java\current
```

Run in **Developer Command Prompt for VS**:

[cmd]
```cmd
mvnw clean package -Dnative -DskipTests
```

The executable is generated at:

```
target\mvn-arch-runner.exe
```

Rename it:

[cmd]
```cmd
ren target\mvn-arch-runner.exe mvn-arch.exe
```

## Note

This project was also started out of interest in building CLI tools with Quarkus.
