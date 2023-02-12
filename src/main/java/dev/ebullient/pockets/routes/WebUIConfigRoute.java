package dev.ebullient.pockets.routes;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.Consume;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

import dev.ebullient.pockets.config.PocketsConfig;
import dev.ebullient.pockets.config.PocketsConfigProvider.PocketsConfigHolder;
import io.quarkus.arc.WithCaching;

@ApplicationScoped
public class WebUIConfigRoute extends RouteBuilder {

    @Inject
    @WithCaching // always use the same instance at injection time
    PocketsConfigHolder configHolder;

    @Override
    public void configure() throws Exception {
        restConfiguration()
                .inlineRoutes(true)
                .dataFormatProperty("autoDiscoverObjectMapper", "true")
                .bindingMode(RestBindingMode.json);

        rest("/config")
                .get("current")
                .to("direct:getCurrentConfig");
    }

    @Consume("direct:getCurrentConfig")
    public PocketsConfig getCurrentConfig() {
        return configHolder.getConfig();
    }
}
