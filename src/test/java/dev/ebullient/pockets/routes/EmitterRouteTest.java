package dev.ebullient.pockets.routes;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.inject.Inject;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.Util;
import dev.ebullient.pockets.actions.ModificationResponse;
import dev.ebullient.pockets.actions.ModifyPockets;
import dev.ebullient.pockets.config.EmitterConfig.EventLogConfiguration;
import dev.ebullient.pockets.config.EmitterConfig.MarkdownConfiguration;
import dev.ebullient.pockets.config.MockLocalPocketsConfigProducer.MockLocalPocketsConfig;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketDetails;
import dev.ebullient.pockets.db.Posting;
import dev.ebullient.pockets.db.Posting.ItemType;
import dev.ebullient.pockets.db.Posting.PostingType;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(CamelQuarkusTestSupport.EmitterRouteTest.class)
public class EmitterRouteTest extends CamelQuarkusTestSupport {

    @Inject
    ModifyPockets modifyPockets;

    @Inject
    MockLocalPocketsConfig localPocketsConfig;

    @Produce("direct:emitRoute")
    ProducerTemplate producer;

    @BeforeEach
    public void beforeEach() {
        localPocketsConfig.reset();
    }

    Path prepareEventLog(String profile) throws IOException {
        EventLogConfiguration eventLogConfiguration = localPocketsConfig.eventLogConfig(profile);
        Path eventLog = eventLogConfiguration.getDirectory().resolve(profile + ".events.jsonl");
        eventLog.getParent().toFile().mkdirs();
        Files.deleteIfExists(eventLog);
        return eventLog;
    }

    Path prepareMarkdown(String profile) throws IOException {
        MarkdownConfiguration markdownConfiguration = localPocketsConfig.markdownConfig(profile);
        Path markdownFolder = markdownConfiguration.getDirectory();
        markdownFolder.toFile().mkdirs();
        Util.deleteDir(markdownFolder);
        return markdownFolder;
    }

    @Test
    @TestTransaction
    public void testNoEnabledEmitter() throws IOException {
        final String profile = "test-5e";

        Path eventLog = prepareEventLog(profile);
        Path markdown = prepareMarkdown(profile);

        makeNoise(profile);

        assertThat(eventLog).doesNotExist();
        assertThat(markdown).exists();
        assertThat(markdown).isEmptyDirectory();
    }

    @Test
    @TestTransaction
    public void testJsonEmitter() throws IOException {
        final String profile = "test-5e";

        localPocketsConfig.enableJsonEventLogging(profile);

        Path eventLog = prepareEventLog(profile);
        Path markdown = prepareMarkdown(profile);

        makeNoise(profile);

        assertThat(eventLog).exists();
        List<String> json = Files.readAllLines(eventLog);
        assertThat(json).hasSize(3);

        assertThat(markdown).exists();
        assertThat(markdown).isDirectoryNotContaining("glob:**.md");
    }

    @Test
    @TestTransaction
    public void testMarkdownEmitter() throws IOException {
        final String profile = "test-5e";

        localPocketsConfig.enableMarkdownEmitter(profile);

        Path eventLog = prepareEventLog(profile);
        Path markdown = prepareMarkdown(profile);

        makeNoise(profile);

        assertThat(eventLog).doesNotExist();

        assertThat(markdown).exists();
        assertThat(markdown).isDirectoryContaining("glob:**.md");
    }

    void makeNoise(String profile) {
        Pocket p = new Pocket();
        p.emoji = "🥡";
        p.slug = "custom-pocket";
        p.name = "Custom Pocket";
        p.pocketDetails = new PocketDetails().setRefId("backpack");

        ModificationResponse response = new ModificationResponse();
        response.datetime = "9032-231-3213";
        response.memo = "The rest of things";
        response.post(new Posting()
                .setType(PostingType.CREATE)
                .setItemType(ItemType.POCKET)
                .setItemId(p.slug)
                .setCreated(p));

        producer.sendBodyAndHeader(response, "profile", profile);
        producer.sendBodyAndHeader(response, "profile", profile);
        producer.sendBodyAndHeader(response, "profile", profile);
    }
}
