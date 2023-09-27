package jp.xhw.datapackupload.plugin.discord;

import jp.xhw.datapackupload.plugin.DataPackPlugin;
import jp.xhw.datapackupload.plugin.discord.panel.DataPackPanel;
import jp.xhw.datapackupload.plugin.discord.panel.IDataPackPanel;
import jp.xhw.datapackupload.plugin.discord.panel.IPanelManager;
import jp.xhw.datapackupload.plugin.discord.panel.interact.PanelInteraction;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelState;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelStateType;
import jp.xhw.datapackupload.plugin.discord.panel.state.impl.*;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.awt.*;
import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiscordBot implements IPanelManager {

    @Getter
    private final DataPackPlugin plugin;
    @Getter
    private JDA jda;
    @Getter
    private File tmpDir;

    private final EnumMap<PanelStateType, PanelState> stateMap;
    private final Map<UUID, IDataPackPanel> panelMap;

    public DiscordBot(DataPackPlugin plugin) {
        this.plugin = plugin;
        this.stateMap = new EnumMap<>(PanelStateType.class);
        this.stateMap.put(PanelStateType.PRE_DOWNLOAD, new PreDownloadState(this.plugin));
        this.stateMap.put(PanelStateType.OVERWRITE_CHECK, new OverwriteCheckState(this.plugin));
        this.stateMap.put(PanelStateType.ENABLE_CHECK, new EnableCheckState(this.plugin));
        this.stateMap.put(PanelStateType.DOWNLOAD, new DownloadState(this.plugin));
        this.stateMap.put(PanelStateType.INSTALL, new InstallState(this.plugin));
        this.stateMap.put(PanelStateType.ROLLBACK, new RollbackState(this.plugin));
        this.stateMap.put(PanelStateType.VALIDATION_ERROR, new SimpleMessageState(this.plugin, "データパックの検証に失敗しました", Color.RED));
        this.stateMap.put(PanelStateType.SIZE_ERROR, new SimpleMessageState(this.plugin, "サイズが大きすぎます", Color.RED));
        this.stateMap.put(PanelStateType.CANCEL, new SimpleMessageState(this.plugin, "キャンセルしました", Color.GRAY));
        this.stateMap.put(PanelStateType.DONE, new SimpleMessageState(this.plugin, "操作が完了しました", Color.GREEN));
        this.panelMap = new HashMap<>();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
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

    @Override
    public PanelState getState(PanelStateType stateType) {
        return stateMap.get(stateType);
    }

    @Override
    public void createPanel(String userId, Message.Attachment targetAttachment, Message panelMessage) {
        final UUID uuid = UUID.randomUUID();
        DataPackPanel panel = new DataPackPanel(uuid, this, userId, targetAttachment, panelMessage);
        panelMap.put(uuid, panel);
        panel.changeState(PanelStateType.PRE_DOWNLOAD);
    }

    @Override
    public void deletePanel(UUID panelId) {
        panelMap.remove(panelId);
    }

    @Override
    public void handleInteraction(PanelInteraction interaction) {
        final IDataPackPanel panel = panelMap.get(interaction.getTargetPanel());
        panel.handleInteraction(interaction);
    }

}
