package jp.xhw.datapackupload.plugin.discord.panel.state;

import jp.xhw.datapackupload.plugin.discord.panel.IDataPackPanel;
import jp.xhw.datapackupload.plugin.discord.panel.interact.PanelInteraction;

public interface Interactive {

    void onInteraction(IDataPackPanel panel, PanelInteraction interaction);

}
