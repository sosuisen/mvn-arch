package io.github.sosuisen;

import io.github.sosuisen.create.CreateCommand;
import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;

/**
 * Top-level command for the mvn-arch CLI.
 */
@TopCommand
@Command(name = "mvn-arch", mixinStandardHelpOptions = true,
        version = "mvn-arch 1.0.0",
        subcommands = {CreateCommand.class, ListCommand.class},
        description = "A wrapper CLI for `mvn archetype:generate`")
public class EntryCommand {
}
