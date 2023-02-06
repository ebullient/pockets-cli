package dev.ebullient.pockets.config;

import java.nio.file.Path;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import dev.ebullient.pockets.config.EmitterConfig.EmitterType;
import dev.ebullient.pockets.config.EmitterConfig.EventLogConfiguration;
import dev.ebullient.pockets.config.EmitterConfig.MarkdownConfiguration;
import io.quarkus.test.Mock;

@Mock
@ApplicationScoped
public class MockLocalPocketsConfigProducer extends LocalPocketsConfigProducer {

    static MockLocalPocketsConfig mockLocalPocketsConfig = new MockLocalPocketsConfig();

    @Mock
    @Produces
    @ApplicationScoped
    public MockLocalPocketsConfig readConfiguration() {
        LocalPocketsConfig config = super.readConfiguration();
        mockLocalPocketsConfig.defaultInit(config);
        return mockLocalPocketsConfig;
    }

    public static class MockLocalPocketsConfig extends LocalPocketsConfig {
        LocalPocketsConfig defaultConfig = null;

        void defaultInit(LocalPocketsConfig config) {
            this.defaultConfig = config;
            reset();
        }

        public void setDefaultDirectory(Path path) {
            this.directory = path;
        }

        public void setEmitter(EmitterConfig<?> emitter) {
            emitter.setDefaults(this, this.emitterFor(emitter.getType()), null);
            this.emitters.put(emitter.getType(), emitter);
        }

        public void setEmitter(String profileName, EmitterConfig<?> emitter) {
            LocalConfig profile = this.profiles.get(profileName);
            if (profile == null) {
                profile = new LocalConfig();
                this.profiles.put(profileName, profile);
            }
            profile.emitters.put(emitter.getType(), emitter);
            emitter.setDefaults(this, this.emitterFor(emitter.getType()), profileName);
        }

        public void reset() {
            this.directory = defaultConfig.directory;
            this.emitters.clear();
            this.emitters.putAll(defaultConfig.emitters);
            this.profiles.clear();
            this.profiles.putAll(defaultConfig.profiles);
        }

        @Override
        public String toString() {
            return "MockLocalPocketsConfig{" +
                    "defaultConfig=" + defaultConfig +
                    ", profiles=" + profiles +
                    ", directory=" + directory +
                    ", emitters=" + emitters +
                    '}';
        }

        public void enableJsonEventLogging() {
            EventLogConfiguration evtLogCfg = emitterFor(EmitterType.eventLog);
            if (evtLogCfg == null) {
                evtLogCfg = new EventLogConfiguration();
                evtLogCfg.setDefaults(this, null, null);
                this.emitters.put(EmitterType.eventLog, evtLogCfg);
            }
            evtLogCfg.enabled = true;
        }

        public EventLogConfiguration enableJsonEventLogging(String profile) {
            LocalConfig cfg = this.profiles.get(profile);
            if (cfg == null) {
                cfg = new LocalConfig();
                this.profiles.put(profile, cfg);
            }
            EventLogConfiguration evtLogCfg = cfg.emitterFor(EmitterType.eventLog);
            if (evtLogCfg == null) {
                evtLogCfg = new EventLogConfiguration();
                evtLogCfg.setDefaults(this, this.emitterFor(EmitterType.eventLog), profile);
                cfg.emitters.put(EmitterType.eventLog, evtLogCfg);
            }
            evtLogCfg.enabled = true;
            return evtLogCfg;
        }

        public MarkdownConfiguration enableMarkdownEmitter(String profile) {
            LocalConfig cfg = this.profiles.get(profile);
            if (cfg == null) {
                cfg = new LocalConfig();
                this.profiles.put(profile, cfg);
            }
            MarkdownConfiguration mdCfg = cfg.emitterFor(EmitterType.markdown);
            if (mdCfg == null) {
                mdCfg = new MarkdownConfiguration();
                mdCfg.setDefaults(this, this.emitterFor(EmitterType.markdown), profile);
                cfg.emitters.put(EmitterType.markdown, mdCfg);
            }
            mdCfg.enabled = true;
            return mdCfg;
        }
    }
}
