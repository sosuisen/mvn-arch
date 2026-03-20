package io.github.sosuisen;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * A record holding archetype information.
 *
 * @param name display name
 * @param groupId Maven group ID
 * @param artifactId Maven artifact ID
 * @param version version
 */
@RegisterForReflection
public record Archetype(String name, String groupId, String artifactId, String version) {
}
