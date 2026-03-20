package io.github.sosuisen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.sosuisen.create.EnvironmentChecker;
import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;

@QuarkusTest
public class EnvironmentCheckerTest {

    @Inject
    EnvironmentChecker environmentChecker;

    @Test
    public void testJavaAndMvnDetected() {
        // In the test environment, java and mvn should be available
        var result = environmentChecker.check();
        assertTrue(result, "java and mvn should be found in PATH");
    }
}
