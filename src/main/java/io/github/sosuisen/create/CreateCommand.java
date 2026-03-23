package io.github.sosuisen.create;

import module java.base;

import io.github.sosuisen.Archetype;
import io.github.sosuisen.ArchetypeCatalogService;

import jakarta.inject.Inject;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * Subcommand that performs project scaffolding.
 * Orchestrates environment check, catalog fetch, user selection, and Maven execution.
 *
 * <p>Supports two modes:</p>
 * <ul>
 *   <li>Interactive mode (no arguments): prompts the user to select an archetype and enter an artifactId.</li>
 *   <li>One-liner mode: {@code mvn-arch create archGroupId:archArtifactId:archVersion projGroupId:projArtifactId:projVersion}</li>
 * </ul>
 */
@Command(name = "create", mixinStandardHelpOptions = true,
        description = "Create a new project from an archetype")
public class CreateCommand implements Callable<Integer> {

    private static final int COORDINATE_PARTS = 3;

    @Parameters(index = "0", arity = "0..1",
            description = "Archetype coordinates (groupId:artifactId:version)")
    String archetypeCoords;

    @Parameters(index = "1", arity = "0..1",
            description = "Project coordinates (groupId:artifactId:version)")
    String projectCoords;

    @Inject
    EnvironmentChecker environmentChecker;

    @Inject
    ArchetypeCatalogService catalogService;

    @Inject
    InteractiveMenu interactiveMenu;

    @Inject
    MavenExecutor mavenExecutor;

    @Override
    public Integer call() {
        if (!checkEnvironment()) {
            return 1;
        }

        if (archetypeCoords != null && projectCoords != null) {
            return runOneLiner();
        } else if (archetypeCoords != null || projectCoords != null) {
            System.err.println("Error: Both archetype and project coordinates are required.");
            System.err.println("Usage: mvn-arch create archGroupId:archArtifactId:archVersion"
                    + " projGroupId:projArtifactId:projVersion");
            return 1;
        } else {
            return runInteractive();
        }
    }

    private int runOneLiner() {
        var archParts = parseCoordinates(archetypeCoords, "Archetype");
        if (archParts == null) {
            return 1;
        }
        var projParts = parseCoordinates(projectCoords, "Project");
        if (projParts == null) {
            return 1;
        }

        var archetype = new Archetype(
                archParts[1],
                archParts[0], archParts[1], archParts[2]);
        return mavenExecutor.execute(archetype, projParts[0], projParts[1], projParts[2]) ? 0 : 1;
    }

    private int runInteractive() {
        var archetypes = fetchCatalog();
        if (archetypes == null) {
            return 1;
        }

        var selection = runInteractiveMenu(archetypes);
        if (selection == null) {
            return 0;
        }

        return mavenExecutor.execute(
                selection.archetype(), "com.example", selection.artifactId(), null) ? 0 : 1;
    }

    /**
     * Parses a colon-separated coordinate string into exactly 3 parts.
     *
     * @param coords the coordinate string (groupId:artifactId:version)
     * @param label  label for error messages (e.g. "Archetype" or "Project")
     * @return a 3-element array, or null if the format is invalid
     */
    String[] parseCoordinates(String coords, String label) {
        assert coords != null : "coords must not be null";

        var parts = coords.split(":");
        if (parts.length != COORDINATE_PARTS) {
            System.err.println("Error: " + label
                    + " coordinates must be in groupId:artifactId:version format.");
            return null;
        }
        for (var part : parts) {
            if (part.isBlank()) {
                System.err.println("Error: " + label
                        + " coordinates must not contain blank segments.");
                return null;
            }
        }
        return parts;
    }

    private boolean checkEnvironment() {
        return environmentChecker.check();
    }

    private List<Archetype> fetchCatalog() {
        var result = catalogService.fetchCatalog();
        if (result.isEmpty()) {
            return null;
        }
        var archetypes = result.get();
        if (archetypes.isEmpty()) {
            System.err.println("Error: Archetype catalog is empty.");
            return null;
        }
        return archetypes;
    }

    private InteractiveMenu.SelectionResult runInteractiveMenu(List<Archetype> archetypes) {
        var result = interactiveMenu.run(archetypes);
        if (result.isEmpty()) {
            System.out.println("Cancelled.");
            return null;
        }
        return result.get();
    }
}
