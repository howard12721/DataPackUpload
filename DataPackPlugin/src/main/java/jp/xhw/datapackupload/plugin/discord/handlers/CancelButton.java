package jp.xhw.datapackupload.plugin.discord.handlers;

import jp.xhw.datapackupload.plugin.discord.ButtonHandler;
import jp.xhw.datapackupload.plugin.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.awt.*;

public class CancelButton extends ButtonHandler {

    public CancelButton(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void handle(ButtonInteractionEvent event) {
        final String userId = event.getComponentId().substring(7);
        if (!event.getInteraction().getUser().getId().equals(userId)) {
            return;
        }
        event.getInteraction().editMessageEmbeds(
                        new EmbedBuilder()
                                .setTitle("キャンセルしました")
                                .setColor(Color.GRAY)
                                .build()
                )
                .setComponents()
                .queue();
    }

}
