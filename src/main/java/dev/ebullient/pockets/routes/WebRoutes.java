package dev.ebullient.pockets.routes;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

import dev.ebullient.pockets.config.Types.PocketConfigData;
import dev.ebullient.pockets.io.InvalidPocketState;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine;

@ApplicationScoped
public class WebRoutes extends RouteBuilder {

    @Inject
    WebRouteHandler webRouteHandler;

    @Override
    public void configure() throws Exception {
        getContext().getGlobalOptions().put("CamelJacksonEnableTypeConverter", "true");
        getContext().getGlobalOptions().put("CamelJacksonTypeConverterToPojo", "true");

        restConfiguration()
                .bindingMode(RestBindingMode.json)
                .componentProperty("lazyStartProducer", "true")
                .dataFormatProperty("autoDiscoverObjectMapper", "true");

        // -- Fetch presets

        rest("/preset")
                .get("/{preset}")
                .to("direct:getPreset");

        // -- Get/Put config data (all profiles)

        rest("/config")
                .get("current")
                .to("direct:getConfig")

                .put("current")
                .type(PocketConfigData.class)
                .to("direct:setConfig");

        // -- Get/Put things in pockets

        rest("/pockets")
                .get()
                .to("direct:getProfiles")

                .get("/{profile}")
                .to("direct:getProfile")

                .post("/{profile}")
                .to("direct:validateAndModifyPockets");

        from("direct:getPreset")
                .autoStartup(false)
                .log(LoggingLevel.INFO, "${in.header.preset}")
                .bean(webRouteHandler, "getPreset(${header.preset})");

        from("direct:getConfig")
                .autoStartup(false)
                .bean(webRouteHandler, "fetchConfigData")
                .to("log:dev.ebullient.pockets?level=DEBUG");

        from("direct:setConfig")
                .autoStartup(false)
                .bean(webRouteHandler, "updateConfigData")
                .to("log:dev.ebullient.pockets?level=DEBUG");

        from("direct:getProfiles")
                .autoStartup(false)
                .onException(InvalidPocketState.class).handled(true).process(new HandleInvalidPocketState()).end()
                .to("log:dev.ebullient.pockets?level=DEBUG")
                .bean(webRouteHandler, "getProfiles()")
                .to("log:dev.ebullient.pockets?level=DEBUG");

        from("direct:getProfile")
                .autoStartup(false)
                .onException(InvalidPocketState.class).handled(true).process(new HandleInvalidPocketState()).end()
                .process((e) -> webRouteHandler.validateProfile(e))
                .to("log:dev.ebullient.pockets?showAll=true&multiline=true&level=DEBUG")
                .bean(webRouteHandler, "getProfile(${header.profile})")
                .to("log:dev.ebullient.pockets?showAll=true&multiline=true&level=DEBUG");

        from("direct:validateAndModifyPockets")
                .autoStartup(false)
                .onException(InvalidPocketState.class).handled(true).process(new HandleInvalidPocketState()).end()
                .to("log:dev.ebullient.pockets?showAll=true&multiline=true&level=DEBUG")
                .process((e) -> webRouteHandler.validateProfile(e))
                .bean(webRouteHandler, "doModification(${body}, ${header.profile})")
                .wireTap("direct:emitRoute")
                .to("log:dev.ebullient.pockets?showAll=true&multiline=true&level=DEBUG");
    }

    static class HandleInvalidPocketState implements Processor {
        int getStatusCode(int exitCode) {
            switch (exitCode) {
                case CommandLine.ExitCode.USAGE:
                    return 400;
                case PocketTui.NOT_FOUND:
                    return 404;
                case PocketTui.CONFLICT:
                    return 409;
                default:
                    return 500;
            }
        }

        @Override
        public void process(Exchange exchange) throws Exception {
            // the caused by exception is stored in a property on the exchange
            InvalidPocketState caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, InvalidPocketState.class);
            Message msg = exchange.getMessage();
            msg.setHeader(Exchange.HTTP_RESPONSE_CODE, getStatusCode(caused.getExitCode()));
            msg.setHeader(Exchange.CONTENT_TYPE, "text/plain");
            msg.setBody(caused.getMessage());
        }
    }
}
