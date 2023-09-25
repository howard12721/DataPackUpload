package jp.xhw.datapackupload.plugin.discord;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public abstract class ButtonHandler {

    final protected DiscordBot bot;

    protected ButtonHandler(DiscordBot bot) {
        this.bot = bot;
    }

    public abstract void handle(ButtonInteractionEvent event);

}
