package jp.xhw.datapackupload.plugin.discord.panel.state.impl;

import jp.xhw.datapackupload.plugin.DataPackPlugin;
import jp.xhw.datapackupload.plugin.discord.DataPackData;
import jp.xhw.datapackupload.plugin.discord.panel.IDataPackPanel;
import jp.xhw.datapackupload.plugin.discord.panel.interact.PanelInteraction;
import jp.xhw.datapackupload.plugin.discord.panel.state.Interactive;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelState;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelStateType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.awt.*;

public class RollbackState extends PanelState implements Interactive {

    public RollbackState(DataPackPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onStart(IDataPackPanel panel) {
        panel.getData().getPanelMessage()
                .editMessageEmbeds(
                        new EmbedBuilder()
                                .setTitle(String.format("「%s」の導入に失敗しました。元の状態に戻しますか?", panel.getData().getTargetAttachment().getFileName()))
                                .setColor(Color.RED)
                                .build()
                )
                .setActionRow(
                        createButton(panel, ButtonStyle.DANGER, "rollback", "ロールバック"),
                        createButton(panel, ButtonStyle.SECONDARY, "nothing", "何もしない")
                )
                .complete();
    }

    @Override
    public void onInteraction(IDataPackPanel panel, PanelInteraction interaction) {
        if (interaction.getAction().equals("rollback")) {
            final DataPackData backup = panel.getData().getBackup();
            final String fileName = panel.getData().getTargetAttachment().getFileName();
            switch (panel.getData().getPreviousState()) {
                case NOT_EXISTS -> getPlugin().getDataPackManager().uninstall(fileName);
                case NOT_AVAILABLE, DISABLED ->
                        getPlugin().getDataPackManager().install(fileName, backup.data(), false);
                case ENABLED -> getPlugin().getDataPackManager().install(fileName, backup.data(), true);
            }
            panel.changeState(PanelStateType.DONE);
        } else if (interaction.getAction().equals("nothing")) {
            panel.changeState(PanelStateType.DONE);
        }

    }

}
