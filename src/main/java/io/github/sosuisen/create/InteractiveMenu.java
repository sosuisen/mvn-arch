package io.github.sosuisen.create;

import module java.base;

import io.github.sosuisen.Archetype;

import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Cursor-based interactive menu using JLine3.
 */
@ApplicationScoped
public class InteractiveMenu {

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "unknown")
    String version;
    private static final Pattern ARTIFACT_ID_PATTERN = Pattern.compile("^[a-z][a-z0-9]*(-[a-z0-9]+)*$");

    private static final String ANSI_CYAN = "\u001b[36m";
    private static final String ANSI_RESET = "\u001b[0m";
    private static final String ANSI_REVERSE = "\u001b[7m";

    private enum Action {
        UP, DOWN, ENTER, QUIT, IGNORE
    }

    /**
     * Runs the full interactive session: archetype selection followed by artifactId
     * input.
     * Uses a single terminal for the entire session.
     *
     * @param archetypes the list of archetypes to choose from
     * @return selected archetype and artifactId, or empty if cancelled
     * @throws NullPointerException     if archetypes is null
     * @throws IllegalArgumentException if archetypes is empty
     */
    public Optional<SelectionResult> run(List<Archetype> archetypes) {
        Objects.requireNonNull(archetypes, "archetypes must not be null");
        if (archetypes.isEmpty()) {
            throw new IllegalArgumentException("archetypes must not be empty");
        }

        try (var terminal = TerminalBuilder.builder()
                .system(true)
                .build()) {

            var selected = selectArchetype(terminal, archetypes);
            if (selected.isEmpty()) {
                return Optional.empty();
            }

            var artifactId = promptArtifactId(terminal);
            if (artifactId.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new SelectionResult(selected.get(), artifactId.get()));
        } catch (IOException e) {
            System.err.println("Error: Failed to create interactive terminal.");
            System.err.println(e.getMessage());
            return Optional.empty();
        } finally {
            restoreSystemOut();
        }
    }

    private Optional<Archetype> selectArchetype(Terminal terminal, List<Archetype> archetypes) {
        var savedAttrs = terminal.enterRawMode();
        try {
            var bindingReader = new BindingReader(terminal.reader());
            var keyMap = buildKeyMap(terminal);

            int selected = 0;
            while (true) {
                renderMenu(terminal, archetypes, selected);
                var action = bindingReader.readBinding(keyMap);
                switch (action) {
                    case QUIT -> {
                        clearScreen(terminal);
                        return Optional.empty();
                    }
                    case ENTER -> {
                        clearScreen(terminal);
                        return Optional.of(archetypes.get(selected));
                    }
                    case UP -> selected = (selected - 1 + archetypes.size()) % archetypes.size();
                    case DOWN -> selected = (selected + 1) % archetypes.size();
                    default -> {
                        /* ignore */ }
                }
            }
        } finally {
            terminal.setAttributes(savedAttrs);
        }
    }

    // Reads a line from the terminal character by character.
    // Cannot use Scanner(System.in) because on macOS the terminal driver may
    // not fully restore input processing (e.g. CR→LF translation) after JLine's
    // raw mode, causing Enter to echo ^M instead of submitting input.
    // Cannot use JLine's LineReader because it does not handle backspace
    // correctly after the terminal has been used in raw mode for menu selection.
    private Optional<String> promptArtifactId(Terminal terminal) {
        var writer = terminal.writer();
        var reader = terminal.reader();

        // Enter raw mode so we can read character by character and handle
        // backspace ourselves.  This also avoids the macOS issue where the
        // terminal driver does not fully restore CR→LF translation after
        // JLine's earlier raw-mode session.
        var savedAttrs = terminal.enterRawMode();
        try {
            writer.println("Input your artifactId (type 'q' to cancel):");

            while (true) {
                writer.print("> ");
                writer.flush();
                var line = readLine(reader, writer);

                if (line == null) {
                    return Optional.empty();
                }
                var input = line.trim();

                if ("q".equalsIgnoreCase(input)) {
                    return Optional.empty();
                }
                if (ARTIFACT_ID_PATTERN.matcher(input).matches()) {
                    return Optional.of(input);
                }
                writer.println("Invalid artifactId: '" + input + "'");
                writer.println("  Only lowercase letters, digits, and hyphens are allowed. (e.g. my-app)");
            }
        } finally {
            terminal.setAttributes(savedAttrs);
        }
    }

    private String readLine(java.io.Reader reader, PrintWriter writer) {
        var sb = new StringBuilder();
        try {
            while (true) {
                int ch = reader.read();
                if (ch == -1) {
                    return null;
                }
                if (ch == '\r' || ch == '\n') {
                    writer.println();
                    writer.flush();
                    return sb.toString();
                }
                // Backspace / DEL
                if (ch == 127 || ch == 8) {
                    if (!sb.isEmpty()) {
                        sb.deleteCharAt(sb.length() - 1);
                        // Erase character on screen: move back, overwrite with space, move back
                        writer.print("\b \b");
                        writer.flush();
                    }
                    continue;
                }
                // Ignore non-printable control characters
                if (ch < 32) {
                    continue;
                }
                sb.append((char) ch);
                writer.print((char) ch);
                writer.flush();
            }
        } catch (IOException e) {
            return null;
        }
    }

    private KeyMap<Action> buildKeyMap(Terminal terminal) {
        var keyMap = new KeyMap<Action>();
        keyMap.setAmbiguousTimeout(100);

        // Arrow keys via capability strings
        var keyUp = terminal.getStringCapability(Capability.key_up);
        var keyDown = terminal.getStringCapability(Capability.key_down);
        if (keyUp != null) {
            keyMap.bind(Action.UP, keyUp);
        }
        if (keyDown != null) {
            keyMap.bind(Action.DOWN, keyDown);
        }

        // Fallback: standard ANSI escape sequences
        keyMap.bind(Action.UP, "\u001b[A");
        keyMap.bind(Action.DOWN, "\u001b[B");
        // Windows Console Virtual Terminal sequences
        keyMap.bind(Action.UP, "\u001bOA");
        keyMap.bind(Action.DOWN, "\u001bOB");

        keyMap.bind(Action.ENTER, "\r");
        keyMap.bind(Action.ENTER, "\n");
        keyMap.bind(Action.QUIT, "q");
        keyMap.bind(Action.QUIT, "Q");

        keyMap.setNomatch(Action.IGNORE);

        return keyMap;
    }

    private void renderMenu(Terminal terminal, List<Archetype> archetypes, int selected) {
        var writer = terminal.writer();
        clearScreen(terminal);

        writer.println("mvn-arch (wrapper of `mvn archetype:generate`)");
        writer.println("ver." + version);
        writer.println();
        writer.println("Choose a project type (Up/Down to move, Enter to select, q to quit):");
        writer.println();

        for (int i = 0; i < archetypes.size(); i++) {
            var archetype = archetypes.get(i);
            var prefix = (i == selected) ? ANSI_REVERSE + " > " : "   ";
            var suffix = (i == selected) ? ANSI_RESET : "";
            writer.println(prefix + (i + 1) + ". " + ANSI_CYAN + archetype.name() + ANSI_RESET + suffix);
            writer.println("      " + archetype.groupId() + ":" + archetype.artifactId() + ":" + archetype.version());
        }
        writer.flush();
    }

    private void clearScreen(Terminal terminal) {
        var writer = terminal.writer();
        writer.print("\u001b[2J\u001b[H");
        writer.flush();
    }

    private static final boolean IS_WINDOWS = System.getProperty("os.name", "")
            .toLowerCase().contains("win");

    /**
     * Restores System.out after JLine's system terminal is closed.
     * On macOS, closing JLine's system terminal can corrupt the underlying
     * file descriptor, leaving System.out in a state where writes produce
     * no visible output. Opening /dev/tty directly bypasses the broken fd.
     */
    private void restoreSystemOut() {
        if (IS_WINDOWS) {
            return;
        }
        try {
            System.setOut(new PrintStream(new FileOutputStream("/dev/tty"), true));
        } catch (FileNotFoundException ignored) {
        }
    }

    /**
     * Result of the interactive selection.
     *
     * @param archetype  the selected archetype
     * @param artifactId the entered artifactId
     */
    public record SelectionResult(Archetype archetype, String artifactId) {
    }
}
