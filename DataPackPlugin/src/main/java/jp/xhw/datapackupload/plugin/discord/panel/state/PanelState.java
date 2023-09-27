package jp.xhw.datapackupload.plugin.discord.panel.state;

import jp.xhw.datapackupload.plugin.DataPackPlugin;
import jp.xhw.datapackupload.plugin.discord.panel.IDataPackPanel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public abstract class PanelState {

    private final DataPackPlugin plugin;

    protected PanelState(DataPackPlugin plugin) {
        this.plugin = plugin;
    }

    public void start(IDataPackPanel panel) {
        onStart(panel);
    }

    protected abstract void onStart(IDataPackPanel panel);

    protected DataPackPlugin getPlugin() {
        return plugin;
    }

    protected Button createButton(IDataPackPanel panel, ButtonStyle style, String action, String label) {
        return Button.of(style, action + "-" + panel.getUniqueId(), label);
    }

}
