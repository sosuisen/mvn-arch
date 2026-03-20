package io.github.sosuisen.create;

import module java.base;

import io.github.sosuisen.Archetype;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Executes the {@code mvn archetype:generate} command.
 */
@ApplicationScoped
public class MavenExecutor {

    private static final boolean IS_WINDOWS = System.getProperty("os.name", "")
            .toLowerCase().contains("win");

    /**
     * Generates a Maven project from the given archetype and artifactId.
     *
     * @param archetype the archetype to use
     * @param artifactId the project's artifactId
     * @return true on success, false on failure
     * @throws NullPointerException if archetype or artifactId is null
     */
    public boolean execute(Archetype archetype, String artifactId) {
        Objects.requireNonNull(archetype, "archetype must not be null");
        Objects.requireNonNull(artifactId, "artifactId must not be null");

        var mvnCommand = buildMvnCommand(archetype, artifactId);

        System.out.println("Generating project with Maven...");
        System.out.println("  Archetype: " + archetype.name());
        System.out.println("  artifactId: " + artifactId);
        System.out.println();

        try {
            var processBuilder = IS_WINDOWS
                    ? new ProcessBuilder("cmd.exe", "/c", mvnCommand)
                    : new ProcessBuilder("sh", "-c", mvnCommand);
            processBuilder.redirectErrorStream(true);

            var process = processBuilder.start();
            printProcessOutput(process);
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("Error: Maven command failed with exit code " + exitCode);
                return false;
            }

            return verifyProjectCreated(artifactId);
        } catch (IOException e) {
            handleIOException(e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error: Maven command was interrupted.");
            return false;
        }
    }

    private String buildMvnCommand(Archetype archetype, String artifactId) {
        return "mvn archetype:generate -B"
                + " -DarchetypeGroupId=" + archetype.groupId()
                + " -DarchetypeArtifactId=" + archetype.artifactId()
                + " -DarchetypeVersion=" + archetype.version()
                + " -DgroupId=com.example"
                + " -DartifactId=" + artifactId;
    }

    private void printProcessOutput(Process process) throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    private boolean verifyProjectCreated(String artifactId) {
        var projectDir = Path.of(artifactId);
        if (Files.isDirectory(projectDir)) {
            System.out.println();
            System.out.println("Project created successfully: " + projectDir.toAbsolutePath());
            return true;
        } else {
            System.err.println("Error: Project directory '" + artifactId + "' was not created.");
            return false;
        }
    }

    private void handleIOException(IOException e) {
        var message = e.getMessage();
        if (message != null && message.contains("Cannot run program")) {
            System.err.println("Error: 'mvn' command not found. Please check your PATH.");
        } else {
            System.err.println("Error: Failed to execute Maven command.");
            System.err.println(message);
        }
    }
}
