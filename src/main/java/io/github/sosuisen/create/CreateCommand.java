package io.github.sosuisen.create;

import module java.base;

import io.github.sosuisen.Archetype;
import io.github.sosuisen.ArchetypeCatalogService;

import jakarta.inject.Inject;

import picocli.CommandLine.Command;

/**
 * Subcommand that performs project scaffolding.
 * Orchestrates environment check, catalog fetch, user selection, and Maven execution.
 */
@Command(name = "create", mixinStandardHelpOptions = true,
        description = "Create a new project from an archetype")
public class CreateCommand implements Runnable {

    @Inject
    EnvironmentChecker environmentChecker;

    @Inject
    ArchetypeCatalogService catalogService;

    @Inject
    InteractiveMenu interactiveMenu;

    @Inject
    MavenExecutor mavenExecutor;

    @Override
    public void run() {
        if (!checkEnvironment()) {
            return;
        }

        var archetypes = fetchCatalog();
        if (archetypes == null) {
            return;
        }

        var selection = runInteractiveMenu(archetypes);
        if (selection == null) {
            return;
        }

        mavenExecutor.execute(selection.archetype(), selection.artifactId());
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
