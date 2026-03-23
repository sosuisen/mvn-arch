package io.github.sosuisen.create;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class CreateCommandTest {

    @Test
    @Launch(value = {"create", "g:a:v"}, exitCode = 1)
    void onlyArchetypeCoordsShowsError(LaunchResult result) {
        assertTrue(result.getErrorOutput().contains("Both archetype and project coordinates are required"));
    }

    @Test
    @Launch(value = {"create", "g:a:v", "bad-format"}, exitCode = 1)
    void invalidProjectCoordsFormatShowsError(LaunchResult result) {
        assertTrue(result.getErrorOutput().contains("Project coordinates must be in groupId:artifactId:version format"));
    }

    @Test
    @Launch(value = {"create", "bad-format", "g:a:v"}, exitCode = 1)
    void invalidArchetypeCoordsFormatShowsError(LaunchResult result) {
        assertTrue(result.getErrorOutput().contains("Archetype coordinates must be in groupId:artifactId:version format"));
    }

    @Test
    @Launch(value = {"create", "g::v", "g:a:v"}, exitCode = 1)
    void blankSegmentInArchetypeCoordsShowsError(LaunchResult result) {
        assertTrue(result.getErrorOutput().contains("Archetype coordinates must not contain blank segments"));
    }

    @Test
    @Launch(value = {"create", "g:a:v", "g: :v"}, exitCode = 1)
    void blankSegmentInProjectCoordsShowsError(LaunchResult result) {
        assertTrue(result.getErrorOutput().contains("Project coordinates must not contain blank segments"));
    }

    @Test
    @Launch(value = {"create", "a:b:c:d", "g:a:v"}, exitCode = 1)
    void tooManySegmentsShowsError(LaunchResult result) {
        assertTrue(result.getErrorOutput().contains("Archetype coordinates must be in groupId:artifactId:version format"));
    }

    @Test
    void parseCoordinatesWithValidInput() {
        var command = new CreateCommand();
        var result = command.parseCoordinates("com.example:my-app:1.0.0", "Test");
        assertArrayEquals(new String[]{"com.example", "my-app", "1.0.0"}, result);
    }

    @Test
    void parseCoordinatesWithTooFewParts() {
        var command = new CreateCommand();
        assertNull(command.parseCoordinates("com.example:my-app", "Test"));
    }

    @Test
    void parseCoordinatesWithTooManyParts() {
        var command = new CreateCommand();
        assertNull(command.parseCoordinates("a:b:c:d", "Test"));
    }

    @Test
    void parseCoordinatesWithBlankSegment() {
        var command = new CreateCommand();
        assertNull(command.parseCoordinates("com.example::1.0.0", "Test"));
    }
}
