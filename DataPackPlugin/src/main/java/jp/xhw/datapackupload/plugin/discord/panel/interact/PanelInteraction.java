package jp.xhw.datapackupload.plugin.discord.panel.interact;

import java.util.UUID;

public interface PanelInteraction {

    String getUserId();

    UUID getTargetPanel();

    String getAction();

}
