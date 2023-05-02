package dev.ebullient.pockets.routes;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileConstants;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.actions.ModifyPockets;
import dev.ebullient.pockets.io.InvalidPocketState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmitterRoutes extends RouteBuilder {

    @Inject
    ModifyPockets modifyPockets;

    @Inject
    EmitRouteHandler emitHandler;

    @Override
    public void configure() throws Exception {

        // This route assumes that the profile context has already been set before modify pockets has been invoked
        from("direct:modifyPockets")
                .errorHandler(noErrorHandler())
                .to("log:dev.ebullient.pockets?level=DEBUG")
                .bean(modifyPockets, "modifyPockets(${body})")
                .process((e) -> emitHandler.setProfile(e))
                .wireTap("direct:emitRoute")
                .to("log:dev.ebullient.pockets?level=DEBUG");

        from("direct:emitRoute")
                .to("log:dev.ebullient.pockets?level=DEBUG")
                .bean(emitHandler, "emitRoute")
                .recipientList(header("routes")).parallelProcessing();

        from("direct:jsonEmitter")
                .onException(InvalidPocketState.class).handled(true).process(new HandleInvalidPocketState()).end()
                .setHeader(FileConstants.FILE_NAME, simple("${header.profile}.events.jsonl"))
                .setBody((e) -> Transform.toJsonString(e.getMessage().getBody()))
                .toD("file:${header.jsonEventLogDirectory}?"
                        + "fileExist=Append&appendChars=\n&allowNullBody=true")
                .to("log:dev.ebullient.pockets?level=DEBUG");

        from("direct:markdownEmitter")
                .onException(InvalidPocketState.class).handled(true).process(new HandleInvalidPocketState()).end()
                .bean(emitHandler, "emitMarkdownFiles(${body}, ${header.profile})")
                .to("log:dev.ebullient.pockets?level=DEBUG");

        from("direct:markdownFile")
                .onException(InvalidPocketState.class).handled(true).process(new HandleInvalidPocketState()).end()
                .to("qute:dummy?allowTemplateFromHeader=true")
                .toD("file:${header.markdownDirectory}?fileExist=Override&allowNullBody=true")
                .to("log:dev.ebullient.pockets?level=DEBUG");
    }

    static class HandleInvalidPocketState implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            // the caused by exception is stored in a property on the exchange
            InvalidPocketState caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, InvalidPocketState.class);
            Tui.error(caused);
        }
    }
}
