package dev.ebullient.pockets.config;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.interceptor.Interceptor;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Priority;
import io.quarkus.arc.profile.UnlessBuildProfile;

@ApplicationScoped
public class PocketsConfigProvider {
    /**
     * This indirection is used so that Camel routes can get this as an object, and not a proxy
     */
    public static class PocketsConfigHolder {
        final PocketsConfig config = new PocketsConfig();

        public PocketsConfig getConfig() {
            return config;
        }
    }

    PocketsConfigHolder holder = new PocketsConfigHolder();

    @ConfigProperty(name = "pockets.profile")
    Optional<String> activeProfile;

    @ConfigProperty(name = "pockets.directory")
    Optional<String> directory;

    @Produces
    @ApplicationScoped
    @Alternative()
    @UnlessBuildProfile("prod")
    @Priority(Interceptor.Priority.LIBRARY_BEFORE)
    public PocketsConfigHolder testConfiguration() {
        holder.config.init("target/pockets-target", "default");
        return holder;
    }

    @Produces
    @ApplicationScoped
    @DefaultBean
    public PocketsConfigHolder getPocketsConfig() {
        holder.config.init(directory.orElse(null), activeProfile.orElse(null)); // use defaults
        return holder;
    }
}
