package dev.ebullient.pockets.routes;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.file.FileConstants;
import org.apache.camel.component.qute.QuteConstants;
import org.hibernate.Session;

import dev.ebullient.pockets.actions.ModificationResponse;
import dev.ebullient.pockets.config.EmitterConfig.EventLogConfiguration;
import dev.ebullient.pockets.config.EmitterConfig.MarkdownConfiguration;
import dev.ebullient.pockets.config.EmitterConfig.MarkdownConfiguration.TemplateType;
import dev.ebullient.pockets.config.LocalPocketsConfig;
import dev.ebullient.pockets.config.Types.PresetFlavor;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Journal;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketCurrency;
import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.io.InvalidPocketState;
import dev.ebullient.pockets.io.PocketsFormat.CurrencyValue;
import dev.ebullient.pockets.io.Templater;
import io.quarkus.panache.common.Sort;
import io.quarkus.qute.TemplateInstance;

@Dependent
public class EmitRouteHandler {
    @Inject
    LocalPocketsConfig localPocketsConfig;

    @Inject
    ProducerTemplate producerTemplate;

    @Inject
    CamelContext context;

    @Inject
    Templater quteTemplates;

    @SuppressWarnings("unused")
    public void emitRoute(Exchange exchange) {
        Message msg = exchange.getMessage();
        String profileName = msg.getHeader("profile", "default", String.class);

        Tui.debugf("emitResults %s: %s", profileName, localPocketsConfig);

        List<String> emitters = new ArrayList<>();
        if (localPocketsConfig.jsonEventLogEnabled(profileName)) {
            EventLogConfiguration eventLogConfig = localPocketsConfig.eventLogConfig(profileName);
            msg.setHeader("jsonEventLogDirectory", eventLogConfig.getDirectory().toString());
            emitters.add("direct:jsonEmitter");
        }
        if (localPocketsConfig.markdownEnabled(profileName)) {
            emitters.add("direct:markdownEmitter");
        }
        msg.setHeader("routes", String.join(",", emitters));
    }

    @SuppressWarnings("unused")
    @Transactional
    public void emitMarkdownFiles(ModificationResponse result, String profileName) {
        Tui.debugf("emitMarkdownFiles %s: %s, %s", profileName, localPocketsConfig, result);
        MarkdownConfiguration markdownConfiguration = localPocketsConfig.markdownConfig(profileName);
        Path targetDir = markdownConfiguration.getDirectory();

        Profile profile = Profile.findByNaturalId(profileName);
        List<Journal> journals;
        List<Item> items;
        List<Pocket> pockets;

        Session session = Profile.getEntityManager().unwrap(Session.class);
        journals = profile.getJournals();
        journals.forEach(x -> session.setReadOnly(x, true));

        items = Item.list("profile.id", Sort.by("slug"), profile.id);
        items.forEach(x -> session.setReadOnly(x, true));

        pockets = Pocket.list("profile.id", profile.id);
        pockets.forEach(x -> session.setReadOnly(x, true));

        Map<String, CurrencyValue> currency = new HashMap<>();
        List<PocketCurrency> pocketChange = PocketCurrency.list("pocket.profile.id", profile.id);
        for (PocketCurrency pc : pocketChange) {
            session.setReadOnly(pc, true);
            currency.computeIfAbsent(pc.currency, x -> new CurrencyValue(pc)).add(pc);
        }

        // Event Log
        TemplateInstance instance = customTemplateOrDefault(markdownConfiguration, TemplateType.eventLog);
        instance.data("journal", journals);
        instance.data("profileName", profileName);
        instance.data("pcd", profile.config);
        producerTemplate.send("direct:markdownFile",
                createExchange(targetDir, "eventLog", instance));

        // Inventory
        instance = customTemplateOrDefault(markdownConfiguration, TemplateType.inventory);
        instance.data("pockets", pockets);
        instance.data("items", items);
        instance.data("currency", currency.values().stream()
                .sorted(Comparator.comparing(x -> x.unitConversion))
                .collect(Collectors.toList()));
        instance.data("useBulk", profile.config.preset == PresetFlavor.pf2e);
        instance.data("profileName", profileName);
        instance.data("pcd", profile.config);
        producerTemplate.send("direct:markdownFile",
                createExchange(targetDir, "inventory", instance));

        // Pockets
        for (Pocket p : pockets) {
            instance = customTemplateOrDefault(markdownConfiguration, TemplateType.pocket);
            instance.data("pocket", p);
            instance.data("useBulk", profile.config.preset == PresetFlavor.pf2e);
            instance.data("profileName", profileName);
            producerTemplate.send("direct:markdownFile",
                    createExchange(targetDir, "pocket-" + p.slug, instance));
        }
    }

    private Exchange createExchange(Path targetDir, String fileName, TemplateInstance instance) {
        Exchange exchange = context.getEndpoint("direct:markdownFile").createExchange();
        Message msg = exchange.getIn();
        msg.setHeader("markdownDirectory", targetDir.toString());
        msg.setHeader(FileConstants.FILE_NAME, fileName + ".md");
        msg.setHeader(QuteConstants.QUTE_TEMPLATE_INSTANCE, instance);
        return exchange;
    }

    public TemplateInstance customTemplateOrDefault(MarkdownConfiguration markdownConfig, TemplateType templateType) {
        if (markdownConfig == null) {
            throw new InvalidPocketState("Config not set");
        }
        String templatePath = markdownConfig.templateFor(templateType);
        return quteTemplates.getTemplateInstance(templatePath);
    }

    public void setProfile(Exchange e) {
        Message msg = e.getMessage();

        ModificationResponse resp = msg.getBody(ModificationResponse.class);

        msg.setHeader("profile", resp.profileName);
    }
}
