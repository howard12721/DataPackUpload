package jp.xhw.datapackupload.plugin.config;

import lombok.Getter;
import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;

import java.util.List;

@Getter
@YamlFile
public class Settings {

    @YamlKey("discord.token")
    private String botToken;

    @YamlKey("discord.upload-channels")
    private List<Long> uploadChannels;

}
