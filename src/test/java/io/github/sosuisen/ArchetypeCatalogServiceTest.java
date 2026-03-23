package io.github.sosuisen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;

import jakarta.inject.Inject;

@QuarkusTest
public class ArchetypeCatalogServiceTest {

    @Inject
    ArchetypeCatalogService catalogService;

    @Inject
    ObjectMapper objectMapper;

    @Test
    public void testFetchCatalogFromLocalFile() {
        var result = catalogService.fetchCatalog();
        assertTrue(result.isPresent(), "Catalog should be loaded successfully");

        var archetypes = result.get();
        assertFalse(archetypes.isEmpty(), "Catalog should not be empty");
        assertEquals(2, archetypes.size(), "Sample catalog should have 2 entries");

        var first = archetypes.getFirst();
        assertEquals("Maven Quickstart", first.name());
        assertEquals("org.apache.maven.archetypes", first.groupId());
    }

    @Test
    public void testFetchCatalogWithInvalidJson() {
        var service = createServiceWithSource("file", "invalid-catalog.json");
        var result = service.fetchCatalog();
        assertTrue(result.isEmpty(), "Invalid JSON should return empty");
    }

    @Test
    public void testFetchCatalogWithMissingFile() {
        var service = createServiceWithSource("file", "nonexistent.json");
        var result = service.fetchCatalog();
        assertTrue(result.isEmpty(), "Missing file should return empty");
    }

    private ArchetypeCatalogService createServiceWithSource(String source, String localPath) {
        var service = new ArchetypeCatalogService();
        service.source = source;
        service.localPath = localPath;
        service.catalogUrl = "";
        service.objectMapper = objectMapper;
        return service;
    }
}
