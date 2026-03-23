package io.github.sosuisen.create;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.sosuisen.Archetype;

public class MavenExecutorTest {

    private final MavenExecutor executor = new MavenExecutor();

    private final Archetype archetype = new Archetype(
            "Maven Quickstart",
            "org.apache.maven.archetypes",
            "maven-archetype-quickstart",
            "1.5");

    @Test
    void buildMvnCommandWithAllProjectCoordinates() {
        var command = executor.buildMvnCommand(
                archetype, "com.myapp", "my-service", "2.0.0");

        assertTrue(command.startsWith("mvn archetype:generate -B"));
        assertTrue(command.contains("-DarchetypeGroupId=org.apache.maven.archetypes"));
        assertTrue(command.contains("-DarchetypeArtifactId=maven-archetype-quickstart"));
        assertTrue(command.contains("-DarchetypeVersion=1.5"));
        assertTrue(command.contains("-DgroupId=com.myapp"));
        assertTrue(command.contains("-DartifactId=my-service"));
        assertTrue(command.contains("-Dversion=2.0.0"));
    }

    @Test
    void buildMvnCommandWithNullVersionOmitsVersionFlag() {
        var command = executor.buildMvnCommand(
                archetype, "com.example", "demo-app", null);

        assertTrue(command.contains("-DgroupId=com.example"));
        assertTrue(command.contains("-DartifactId=demo-app"));
        assertFalse(command.contains("-Dversion="));
    }

    @Test
    void buildMvnCommandProducesExactExpectedString() {
        var simpleArchetype = new Archetype("test", "g1", "a1", "v1");
        var command = executor.buildMvnCommand(simpleArchetype, "g2", "a2", "v2");

        assertEquals("mvn archetype:generate -B"
                + " -DarchetypeGroupId=g1"
                + " -DarchetypeArtifactId=a1"
                + " -DarchetypeVersion=v1"
                + " -DgroupId=g2"
                + " -DartifactId=a2"
                + " -Dversion=v2", command);
    }
}
