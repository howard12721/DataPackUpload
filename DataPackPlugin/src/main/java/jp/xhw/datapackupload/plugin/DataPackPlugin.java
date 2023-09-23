package jp.xhw.datapackupload.plugin;

import jp.xhw.datapackupload.plugin.config.Settings;
import jp.xhw.datapackupload.plugin.datapack.DataPackManager;
import jp.xhw.datapackupload.plugin.discord.DiscordBot;
import lombok.Getter;
import net.william278.annotaml.Annotaml;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

public final class DataPackPlugin extends JavaPlugin {

    @Getter
    private DataPackManager dataPackManager;
    @Getter
    private Settings settings;
    @Getter
    DiscordBot discordBot;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();

        this.discordBot = new DiscordBot(this);
        this.dataPackManager = new DataPackManager(this);

        discordBot.start();
    }

    @Override
    public void onDisable() {
        discordBot.stop();
    }

    private void loadConfig() throws RuntimeException {
        try {
            this.settings = Annotaml.create(new File(getDataFolder(), "config.yml"), Settings.class).get();
        } catch (IOException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            getLogger().log(Level.SEVERE, "コンフィグの読み込みに失敗しました", e);
            throw new RuntimeException(e);
        }
    }

}