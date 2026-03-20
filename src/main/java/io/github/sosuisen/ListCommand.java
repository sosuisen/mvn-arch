package io.github.sosuisen;

import jakarta.inject.Inject;

import picocli.CommandLine.Command;

/**
 * Subcommand that lists available archetypes.
 */
@Command(name = "list", mixinStandardHelpOptions = true, description = "List available archetypes")
public class ListCommand implements Runnable {

    @Inject
    ArchetypeCatalogService catalogService;

    @Override
    public void run() {
        var result = catalogService.fetchCatalog();
        if (result.isEmpty()) {
            return;
        }
        var archetypes = result.get();
        if (archetypes.isEmpty()) {
            System.err.println("Error: Archetype catalog is empty.");
            return;
        }

        System.out.println("Available archetypes:");
        System.out.println();
        for (int i = 0; i < archetypes.size(); i++) {
            var a = archetypes.get(i);
            System.out.printf("  %d. %s%n", i + 1, a.name());
            System.out.printf("     %s:%s:%s%n", a.groupId(), a.artifactId(), a.version());
        }
    }
}
