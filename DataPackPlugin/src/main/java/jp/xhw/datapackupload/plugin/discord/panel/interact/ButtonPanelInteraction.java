package jp.xhw.datapackupload.plugin.discord.panel.interact;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;

import java.util.UUID;

public class ButtonPanelInteraction implements PanelInteraction {

    @Getter
    private final ButtonInteraction interaction;
    private final UUID targetPanel;
    private final String action;

    public ButtonPanelInteraction(ButtonInteraction buttonInteraction) {
        this.interaction = buttonInteraction;
        final String componentId = buttonInteraction.getComponentId();
        this.targetPanel = UUID.fromString(componentId.substring(componentId.indexOf('-') + 1));
        this.action = componentId.substring(0, componentId.indexOf('-'));
        buttonInteraction.editComponents().complete();
    }

    @Override
    public String getUserId() {
        return interaction.getUser().getId();
    }

    @Override
    public UUID getTargetPanel() {
        return targetPanel;
    }

    @Override
    public String getAction() {
        return action;
    }
}
