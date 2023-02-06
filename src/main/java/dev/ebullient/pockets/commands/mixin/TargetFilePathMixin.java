package dev.ebullient.pockets.commands.mixin;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.ProfileConfigData;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;

public class TargetFilePathMixin {
    @Option(names = { "--file" }, description = "Path to JSON or YAML file.", required = true)
    Path filePath;

    public boolean notWritable(boolean overwrite) {
        if (filePath.toFile().exists() && !overwrite) {
            Tui.errorf("Specified file already exists. Use the '-f' option to overwrite the existing file.%nFile: %s",
                    filePath.toString());
            return true;
        }
        Path parent = filePath.getParent();
        parent.toFile().mkdirs();
        return false;
    }

    public boolean notReadable() {
        if (!filePath.toFile().exists()) {
            Tui.errorf("Specified file does not exist.%nFile: %s", filePath.toString());
            return true;
        }
        return false;
    }

    public int writeProfileConfigValue(ProfileConfigData pc) {
        ObjectMapper mapper = Transform.mapper(filePath);
        try {
            mapper.writeValue(filePath.toFile(), pc);
        } catch (IOException e) {
            Tui.errorf(e, "Unable to export profile %s: %s", pc.slug, e.getMessage());
            return ExitCode.SOFTWARE;
        }
        return ExitCode.OK;
    }

    public ProfileConfigData readProfileConfig() {
        ObjectMapper mapper = Transform.mapper(filePath);
        try {
            return mapper.readValue(filePath.toFile(), ProfileConfigData.class);
        } catch (IOException e) {
            Tui.errorf(e, "Unable to read data from file %s: %s", filePath.toString(), e.getMessage());
        }
        return null;
    }
}
