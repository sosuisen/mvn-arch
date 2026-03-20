# mvn-arch

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
