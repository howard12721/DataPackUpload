package jp.xhw.datapackupload.plugin.discord.panel.state.impl;

import jp.xhw.datapackupload.plugin.DataPackPlugin;
import jp.xhw.datapackupload.plugin.discord.panel.IDataPackPanel;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelState;
import jp.xhw.datapackupload.plugin.discord.panel.state.Terminative;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class SimpleMessageState extends PanelState implements Terminative {

    private final String message;
    private final Color color;

    public SimpleMessageState(DataPackPlugin plugin, String message, Color color) {
        super(plugin);
        this.message = message;
        this.color = color;
    }

    @Override
    protected void onStart(IDataPackPanel panel) {
        panel.getData().getPanelMessage()
                .editMessageEmbeds(
                        new EmbedBuilder()
                                .setTitle(message)
                                .setColor(color)
                                .build()
                )
                .setComponents()
                .complete();
    }

}
