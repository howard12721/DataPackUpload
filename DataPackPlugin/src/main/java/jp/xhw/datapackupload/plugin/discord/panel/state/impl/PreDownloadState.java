package jp.xhw.datapackupload.plugin.discord.panel.state.impl;

import jp.xhw.datapackupload.plugin.DataPackPlugin;
import jp.xhw.datapackupload.plugin.datapack.DataPackManager;
import jp.xhw.datapackupload.plugin.datapack.PackState;
import jp.xhw.datapackupload.plugin.discord.panel.IDataPackPanel;
import jp.xhw.datapackupload.plugin.discord.panel.PanelData;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelState;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelStateType;
import net.dv8tion.jda.api.entities.Message;

public class PreDownloadState extends PanelState {

    private static final int SIZE_LIMIT = 26214400;

    public PreDownloadState(DataPackPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onStart(IDataPackPanel panel) {
        final PanelData data = panel.getData();
        final Message.Attachment attachment = data.getTargetAttachment();
        final DataPackManager dataPackManager = getPlugin().getDataPackManager();
        final PackState packState = dataPackManager.getPackState(attachment.getFileName());
        if (attachment.getSize() > SIZE_LIMIT) {
            panel.changeState(PanelStateType.SIZE_ERROR);
        } else if (packState != PackState.NOT_EXISTS) {
            panel.changeState(PanelStateType.OVERWRITE_CHECK);
        } else {
            panel.changeState(PanelStateType.DOWNLOAD);
        }
    }

}
