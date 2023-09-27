package jp.xhw.datapackupload.plugin.discord.panel;

import jp.xhw.datapackupload.plugin.discord.panel.interact.PanelInteraction;
import jp.xhw.datapackupload.plugin.discord.panel.state.Interactive;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelState;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelStateType;
import jp.xhw.datapackupload.plugin.discord.panel.state.Terminative;
import net.dv8tion.jda.api.entities.Message;

import java.util.UUID;

public class DataPackPanel implements IDataPackPanel {

    private final UUID uniqueId;
    private final IPanelManager panelManager;
    private final PanelData panelData;
    private final String userId;


    private PanelState currentState;

    public DataPackPanel(UUID uniqueId, IPanelManager panelManager, String userId, Message.Attachment targetAttachment, Message panelMessage) {
        this.uniqueId = uniqueId;
        this.panelManager = panelManager;
        this.userId = userId;
        this.panelData = new PanelData(targetAttachment, panelMessage);
    }

    @Override
    public void changeState(PanelStateType stateType) {
        this.currentState = this.panelManager.getState(stateType);
        this.currentState.start(this);
        if (this.currentState instanceof Terminative) {
            this.panelManager.deletePanel(this.uniqueId);
        }
    }

    @Override
    public PanelData getData() {
        return panelData;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public void handleInteraction(PanelInteraction interaction) {
        if (!interaction.getUserId().equals(userId)) {
            return;
        }
        if (this.currentState instanceof Interactive interactive) {
            interactive.onInteraction(this, interaction);
        }
    }

}
