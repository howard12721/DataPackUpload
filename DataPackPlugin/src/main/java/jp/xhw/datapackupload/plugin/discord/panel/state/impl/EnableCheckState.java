package jp.xhw.datapackupload.plugin.discord.panel.state.impl;

import jp.xhw.datapackupload.plugin.DataPackPlugin;
import jp.xhw.datapackupload.plugin.discord.panel.IDataPackPanel;
import jp.xhw.datapackupload.plugin.discord.panel.interact.PanelInteraction;
import jp.xhw.datapackupload.plugin.discord.panel.state.Interactive;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelState;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelStateType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.awt.*;

public class EnableCheckState extends PanelState implements Interactive {

    public EnableCheckState(DataPackPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onStart(IDataPackPanel panel) {
        panel.getData().getPanelMessage()
                .editMessageEmbeds(
                        new EmbedBuilder()
                                .setTitle(String.format("「%s」は無効化されています。上書き後に有効化しますか?", panel.getData().getTargetAttachment().getFileName()))
                                .setColor(Color.GRAY)
                                .build()
                )
                .setActionRow(
                        createButton(panel, ButtonStyle.SUCCESS, "enable", "上書き後に有効化"),
                        createButton(panel, ButtonStyle.DANGER, "nothing", "上書きのみ"),
                        createButton(panel, ButtonStyle.SECONDARY, "cancel", "キャンセル")
                )
                .complete();
    }

    @Override
    public void onInteraction(IDataPackPanel panel, PanelInteraction interaction) {
        switch (interaction.getAction()) {
            case "enable" -> {
                panel.getData().setDoEnable(true);
                panel.changeState(PanelStateType.DOWNLOAD);
            }
            case "nothing" -> {
                panel.getData().setDoEnable(false);
                panel.changeState(PanelStateType.DOWNLOAD);
            }
            case "cancel" -> panel.changeState(PanelStateType.CANCEL);
        }
    }

}
