package dev.ebullient.pockets.commands;

import static dev.ebullient.pockets.Util.PROJECT_PATH;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.Util;
import dev.ebullient.pockets.config.MockLocalPocketsConfigProducer.MockLocalPocketsConfig;

public class CommandTestBase {
    static final Path TARGET = PROJECT_PATH.resolve("target/pockets/test");
    static final Path configYaml = TARGET.resolve("config.yaml");

    @BeforeAll
    public static void prep() {
        configYaml.getParent().toFile().mkdirs();
    }

    Path testOutputPath;

    @BeforeEach
    public void createTestConfig(TestInfo info) throws IOException {
        String methodName = info.getTestMethod().map(Method::getName).orElse("createItem");
        testOutputPath = TARGET.resolve(methodName);
        testOutputPath.toFile().mkdirs();
        Util.deleteDir(testOutputPath);

        MockLocalPocketsConfig pocketsConfig = new MockLocalPocketsConfig();
        pocketsConfig.setDefaultDirectory(testOutputPath);
        pocketsConfig.enableJsonEventLogging("test-5e");
        pocketsConfig.enableMarkdownEmitter("test-5e");

        // write config file
        Transform.mapper(configYaml).writeValue(configYaml.toFile(), pocketsConfig);
    }

    @AfterEach
    public void removeTestConfig() {
        // delete config file
        configYaml.toFile().delete();
    }

}
