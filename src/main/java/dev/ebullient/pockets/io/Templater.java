package dev.ebullient.pockets.io;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@ApplicationScoped
public class Templater {

    @Inject
    Engine engine;

    Map<String, Integer[]> columnWidth = new HashMap<>();

    public TemplateInstance getTemplateInstance(String templatePath) throws RuntimeException {
        final Template tpl;
        if (engine.isTemplateLoaded(templatePath)) {
            tpl = engine.getTemplate(templatePath);
        } else {
            Tui.debugf("📝 loading template %s", templatePath);
            try {
                if (templatePath.startsWith("stream:")) {
                    tpl = engine.parse(streamToString(templatePath));
                } else {
                    tpl = engine.parse(Files.readString(Path.of(templatePath)));
                }
                engine.putTemplate(templatePath, tpl);
            } catch (IOException e) {
                throw new InvalidPocketState(e, "Failed reading template from %s", templatePath);
            }
        }
        return tpl.instance();
    }

    public String streamToString(String templatePath) throws IOException {
        try (InputStream in = this.getClass().getResourceAsStream(templatePath.replace("stream:", "/"))) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            in.transferTo(buf);
            return buf.toString("UTF-8");
        }
    }

    public <T> String render(String key, T data) {
        TemplateInstance tpl = getTemplateInstance(key);
        Integer[] widths = columnWidth.get(key);

        return tpl
                .data("data", data)
                .data("columnWidth", widths)
                .render()
                .trim();
    }
}
