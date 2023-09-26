package jp.xhw.datapackupload.plugin.discord;

import jp.xhw.datapackupload.plugin.DataPackPlugin;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;

public class DiscordBot {

    @Getter
    private final DataPackPlugin plugin;
    @Getter
    private JDA jda;
    @Getter
    private File tmpDir;

    public DiscordBot(DataPackPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        this.jda = JDABuilder
                .createDefault(plugin.getSettings().getBotToken(), GatewayIntent.GUILD_VOICE_STATES)
                .addEventListeners(new EventHandler(this))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.SCHEDULED_EVENTS)
                .build();

        this.tmpDir = new File(plugin.getDataFolder() + "/tmp");
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
    }

    public void stop() {
        this.jda.shutdownNow();
    }

}
