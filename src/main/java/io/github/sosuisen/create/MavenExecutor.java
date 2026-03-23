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
     * Generates a Maven project from the given archetype and project coordinates.
     *
     * @param archetype      the archetype to use
     * @param projGroupId    the project's groupId
     * @param projArtifactId the project's artifactId
     * @param projVersion    the project's version, or null to use the Maven default
     * @return true on success, false on failure
     * @throws NullPointerException if archetype, projGroupId, or projArtifactId is null
     */
    public boolean execute(Archetype archetype,
                           String projGroupId, String projArtifactId, String projVersion) {
        Objects.requireNonNull(archetype, "archetype must not be null");
        Objects.requireNonNull(projGroupId, "projGroupId must not be null");
        Objects.requireNonNull(projArtifactId, "projArtifactId must not be null");

        var mvnCommand = buildMvnCommand(archetype, projGroupId, projArtifactId, projVersion);

        System.out.println("Generating project with Maven...");
        System.out.println("  Archetype: " + archetype.name());
        System.out.println("  groupId: " + projGroupId);
        System.out.println("  artifactId: " + projArtifactId);
        if (projVersion != null) {
            System.out.println("  version: " + projVersion);
        }
        System.out.println();
        System.out.println("  " + mvnCommand);
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

            return verifyProjectCreated(projArtifactId);
        } catch (IOException e) {
            handleIOException(e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error: Maven command was interrupted.");
            return false;
        }
    }

    String buildMvnCommand(Archetype archetype,
                                    String projGroupId, String projArtifactId,
                                    String projVersion) {
        var command = "mvn archetype:generate -B"
                + " -DarchetypeGroupId=" + archetype.groupId()
                + " -DarchetypeArtifactId=" + archetype.artifactId()
                + " -DarchetypeVersion=" + archetype.version()
                + " -DgroupId=" + projGroupId
                + " -DartifactId=" + projArtifactId;
        if (projVersion != null) {
            command += " -Dversion=" + projVersion;
        }
        return command;
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
