package dev.ebullient.pockets.config;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dev.ebullient.pockets.io.InvalidPocketState;
import picocli.CommandLine.ExitCode;

public abstract class EmitterConfig<T extends EmitterConfig<?>> {

    static class Resolved<T extends EmitterConfig<?>> {
        LocalConfig globalDefaults;
        T emitterDefaults;
        String profileName;
    }

    @JsonIgnore
    Resolved<T> resolved;

    Boolean enabled;
    Path directory;

    enum EmitterType {
        markdown,
        eventLog,
        unknown
    }

    public abstract EmitterType getType();

    public void setType(EmitterType type) {
        // do nothing
    }

    void setDefaults(LocalConfig globalDefaults, T emitterDefaults, String profileName) {
        resolved = new Resolved<>();
        resolved.globalDefaults = globalDefaults;
        if (emitterDefaults != this) {
            resolved.emitterDefaults = emitterDefaults;
        }
        resolved.profileName = profileName;
    }

    public boolean getEnabled() {
        if (enabled == null) {
            enabled = resolved.emitterDefaults == null ? false : resolved.emitterDefaults.getEnabled();
        }
        return enabled;
    }

    public Path getDirectory() {
        if (directory == null) {
            directory = resolved.emitterDefaults == null
                    ? resolved.globalDefaults.directory
                    : resolved.emitterDefaults.directory;
        }
        return directory;
    }

    public static class EventLogConfiguration extends EmitterConfig<EventLogConfiguration> {
        public EmitterType getType() {
            return EmitterType.eventLog;
        }
    }

    public static class MarkdownConfiguration extends EmitterConfig<MarkdownConfiguration> {
        public enum TemplateType {
            inventory,
            pocket,
            eventLog
        }

        public EmitterType getType() {
            return EmitterType.markdown;
        }

        Map<TemplateType, String> templatePath = new HashMap<>();

        @Override
        public Path getDirectory() {
            if (directory == null) {
                Path p = super.getDirectory();
                if (resolved.profileName != null) {
                    directory = p.resolve(resolved.profileName);
                }
            }
            return directory;
        }

        public String templateFor(TemplateType templateType) {
            String template = templatePath.get(templateType);
            if (template == null) {
                template = resolved.emitterDefaults == null
                        ? defaultTemplate(templateType)
                        : resolved.emitterDefaults.templateFor(templateType);
                templatePath.put(templateType, template);
            }
            return template;
        }

        String defaultTemplate(TemplateType templateType) {
            switch (templateType) {
                case inventory:
                    return "stream:templates/inventory.txt";
                case pocket:
                    return "stream:templates/pocket.txt";
                case eventLog:
                    return "stream:templates/eventLog.txt";
            }
            throw new InvalidPocketState(ExitCode.SOFTWARE, "Valid template required: %s", templateType);
        }
    }
}
