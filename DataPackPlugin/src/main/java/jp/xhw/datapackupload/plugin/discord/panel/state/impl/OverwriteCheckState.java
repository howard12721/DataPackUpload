package jp.xhw.datapackupload.plugin.discord.panel.state.impl;

import jp.xhw.datapackupload.plugin.DataPackPlugin;
import jp.xhw.datapackupload.plugin.datapack.PackState;
import jp.xhw.datapackupload.plugin.discord.panel.IDataPackPanel;
import jp.xhw.datapackupload.plugin.discord.panel.interact.PanelInteraction;
import jp.xhw.datapackupload.plugin.discord.panel.state.Interactive;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelState;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelStateType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.awt.*;

public class OverwriteCheckState extends PanelState implements Interactive {

    public OverwriteCheckState(DataPackPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onStart(IDataPackPanel panel) {
        panel.getData().getPanelMessage()
                .editMessageEmbeds(
                        new EmbedBuilder()
                                .setTitle(String.format("「%s」は既に存在しています。上書きしますか?", panel.getData().getTargetAttachment().getFileName()))
                                .setColor(Color.RED)
                                .build()
                )
                .setActionRow(
                        createButton(panel, ButtonStyle.SUCCESS, "overwrite", "上書き"),
                        createButton(panel, ButtonStyle.SECONDARY, "cancel", "キャンセル")
                )
                .complete();
    }

    @Override
    public void onInteraction(IDataPackPanel panel, PanelInteraction interaction) {
        switch (interaction.getAction()) {
            case "overwrite" -> {
                final PackState packState = getPlugin().getDataPackManager().getPackState(panel.getData().getTargetAttachment().getFileName());
                if (packState == PackState.DISABLED) {
                    panel.changeState(PanelStateType.ENABLE_CHECK);
                } else {
                    panel.changeState(PanelStateType.DOWNLOAD);
                }
            }
            case "cancel" -> panel.changeState(PanelStateType.CANCEL);
        }
    }

}
