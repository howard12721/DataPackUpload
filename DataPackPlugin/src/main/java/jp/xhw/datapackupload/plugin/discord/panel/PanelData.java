package jp.xhw.datapackupload.plugin.discord.panel;

import jp.xhw.datapackupload.plugin.datapack.PackState;
import jp.xhw.datapackupload.plugin.discord.DataPackData;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;

@Data
@RequiredArgsConstructor
public class PanelData {

    private final Message.Attachment targetAttachment;
    private final Message panelMessage;
    private boolean doEnable = false;
    private DataPackData dataPack;
    private DataPackData backup;
    private PackState previousState;

}
