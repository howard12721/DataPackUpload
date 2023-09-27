package jp.xhw.datapackupload.plugin.discord.panel.state.impl;

import jp.xhw.datapackupload.plugin.DataPackPlugin;
import jp.xhw.datapackupload.plugin.datapack.PackState;
import jp.xhw.datapackupload.plugin.discord.DataPackData;
import jp.xhw.datapackupload.plugin.discord.panel.IDataPackPanel;
import jp.xhw.datapackupload.plugin.discord.panel.PanelData;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelState;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelStateType;

public class InstallState extends PanelState {

    public InstallState(DataPackPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onStart(IDataPackPanel panel) {
        final PanelData panelData = panel.getData();
        final String fileName = panelData.getTargetAttachment().getFileName();
        panelData.setPreviousState(getPlugin().getDataPackManager().getPackState(fileName));
        panelData.setBackup(new DataPackData(fileName, getPlugin().getDataPackManager().getDataPackData(fileName)));
        getPlugin().getDataPackManager()
                .install(panelData.getDataPack().fileName(), panelData.getDataPack().data(), panelData.isDoEnable())
                .thenRun(() -> {
                    if (getPlugin().getDataPackManager().getPackState(fileName) == PackState.NOT_AVAILABLE) {
                        panel.changeState(PanelStateType.ROLLBACK);
                        return;
                    }
                    panel.changeState(PanelStateType.DONE);
                })
                .exceptionally(e -> {
                    panel.changeState(PanelStateType.ROLLBACK);
                    return null;
                });
    }

}
