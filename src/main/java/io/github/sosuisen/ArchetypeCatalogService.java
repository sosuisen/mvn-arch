package io.github.sosuisen;

import module java.base;
import module java.net.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Service responsible for fetching and parsing the archetype catalog.
 */
@ApplicationScoped
public class ArchetypeCatalogService {

    @ConfigProperty(name = "mvn-arch.catalog.source")
    String source;

    @ConfigProperty(name = "mvn-arch.catalog.url")
    String catalogUrl;

    @ConfigProperty(name = "mvn-arch.catalog.local-path", defaultValue = "archetypes.json")
    String localPath;

    @Inject
    ObjectMapper objectMapper;

    /**
     * Fetches the archetype list based on the catalog source configuration.
     *
     * @return the archetype list, or empty on failure
     */
    public Optional<List<Archetype>> fetchCatalog() {
        try {
            var json = "file".equals(source) ? readFromFile() : downloadFromUrl();
            var archetypes = objectMapper.readValue(json, new TypeReference<List<Archetype>>() {
            });
            return Optional.of(archetypes);
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            System.err.println("Error: Invalid catalog JSON format.");
            System.err.println(e.getMessage());
            return Optional.empty();
        } catch (IOException e) {
            System.err.println("Error: Failed to read archetype catalog.");
            System.err.println(e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error: Catalog download was interrupted.");
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error: Unexpected error occurred.");
            System.err.println(e.getMessage());
            return Optional.empty();
        }
    }

    private String readFromFile() throws IOException {
        try (var is = Thread.currentThread().getContextClassLoader().getResourceAsStream(localPath)) {
            if (is == null) {
                throw new IOException("Resource not found on classpath: " + localPath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String downloadFromUrl() throws IOException, InterruptedException {
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(catalogUrl))
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode() + ": Failed to download catalog from " + catalogUrl);
            }
            return response.body();
        }
    }
}
