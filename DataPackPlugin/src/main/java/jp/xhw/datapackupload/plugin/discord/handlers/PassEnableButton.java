package jp.xhw.datapackupload.plugin.discord.handlers;

import jp.xhw.datapackupload.plugin.discord.ButtonHandler;
import jp.xhw.datapackupload.plugin.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.awt.*;

public class PassEnableButton extends ButtonHandler {

    public PassEnableButton(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void handle(ButtonInteractionEvent event) {
        final String userId = event.getComponentId().substring(3);
        if (!event.getInteraction().getUser().getId().equals(userId)) {
            return;
        }
        event.getInteraction()
                .editMessage("")
                .setEmbeds(
                        new EmbedBuilder()
                                .setTitle("有効化せずに上書きしました")
                                .setColor(Color.GREEN)
                                .build()
                )
                .setComponents()
                .queue();
    }

}
