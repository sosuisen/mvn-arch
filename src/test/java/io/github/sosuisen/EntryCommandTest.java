package io.github.sosuisen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class EntryCommandTest {

    @Test
    @Launch(value = {"--help"}, exitCode = 0)
    public void testHelpOption(LaunchResult result) {
        assertEquals(0, result.exitCode());
    }

    @Test
    @Launch(value = {"create", "--help"}, exitCode = 0)
    public void testCreateHelpOption(LaunchResult result) {
        assertEquals(0, result.exitCode());
    }

    @Test
    @Launch(value = {"list"}, exitCode = 0)
    public void testListCommand(LaunchResult result) {
        assertEquals(0, result.exitCode());
        assertTrue(result.getOutput().contains("Available archetypes:"));
    }
}
