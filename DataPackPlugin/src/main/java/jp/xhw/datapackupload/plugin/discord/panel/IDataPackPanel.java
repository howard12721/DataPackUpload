package jp.xhw.datapackupload.plugin.discord.panel;

import jp.xhw.datapackupload.plugin.discord.panel.interact.PanelInteraction;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelStateType;

import java.util.UUID;

public interface IDataPackPanel {

    void changeState(PanelStateType stateType);

    PanelData getData();

    UUID getUniqueId();

    void handleInteraction(PanelInteraction interaction);

}
