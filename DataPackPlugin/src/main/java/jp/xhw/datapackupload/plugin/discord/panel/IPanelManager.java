package jp.xhw.datapackupload.plugin.discord.panel;

import jp.xhw.datapackupload.plugin.discord.panel.interact.PanelInteraction;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelState;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelStateType;
import net.dv8tion.jda.api.entities.Message;

import java.util.UUID;

public interface IPanelManager {

    PanelState getState(PanelStateType stateType);

    void createPanel(String userId, Message.Attachment targetAttachment, Message panelMessage);

    void deletePanel(UUID panelId);

    void handleInteraction(PanelInteraction interaction);

}
