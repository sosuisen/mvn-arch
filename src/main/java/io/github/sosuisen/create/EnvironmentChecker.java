package io.github.sosuisen.create;

import module java.base;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Checks that the {@code java} and {@code mvn} commands exist on the PATH.
 */
@ApplicationScoped
public class EnvironmentChecker {

    private static final boolean IS_WINDOWS = System.getProperty("os.name", "")
            .toLowerCase().contains("win");

    /**
     * Verifies that {@code java} and {@code mvn} are available on the PATH.
     * Prints an error message for each missing command.
     *
     * @return true if both are found, false otherwise
     */
    public boolean check() {
        var javaOk = commandExists("java");
        var mvnOk = commandExists("mvn");

        if (!javaOk) {
            System.err.println("Error: 'java' command not found in PATH.");
            System.err.println("Please install JDK and add it to your PATH.");
        }
        if (!mvnOk) {
            System.err.println("Error: 'mvn' command not found in PATH.");
            System.err.println("Please install Maven and add it to your PATH.");
        }
        return javaOk && mvnOk;
    }

    private boolean commandExists(String command) {
        try {
            var whichCommand = IS_WINDOWS
                    ? new String[]{"cmd.exe", "/c", "where", command}
                    : new String[]{"which", command};
            var process = new ProcessBuilder(whichCommand)
                    .redirectErrorStream(true)
                    .start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}
