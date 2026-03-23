package io.github.sosuisen;

import module java.base;

import org.eclipse.microprofile.config.ConfigProvider;

import picocli.CommandLine.IVersionProvider;

/**
 * Provides the application version from the Quarkus configuration.
 * Quarkus automatically sets {@code quarkus.application.version}
 * from the pom.xml {@code <version>} element at build time.
 */
public class VersionProvider implements IVersionProvider {

    @Override
    public String[] getVersion() {
        var version = ConfigProvider.getConfig()
                .getOptionalValue("quarkus.application.version", String.class)
                .orElse("unknown");
        return new String[]{"mvn-arch " + version};
    }
}
