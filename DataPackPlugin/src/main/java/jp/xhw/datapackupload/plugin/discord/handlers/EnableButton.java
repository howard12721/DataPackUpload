package jp.xhw.datapackupload.plugin.discord.handlers;

import jp.xhw.datapackupload.plugin.discord.ButtonHandler;
import jp.xhw.datapackupload.plugin.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.awt.*;

public class EnableButton extends ButtonHandler {

    public EnableButton(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void handle(ButtonInteractionEvent event) {
        final int lastIndex = event.getComponentId().lastIndexOf('-');
        final String fileName = event.getComponentId().substring(7, lastIndex);
        final String userId = event.getComponentId().substring(lastIndex + 1);
        if (!event.getInteraction().getUser().getId().equals(userId)) {
            return;
        }

        if (this.bot.getPlugin().getDataPackManager().isDisabled(fileName)) {
            this.bot.getPlugin().getDataPackManager().enable(fileName);
            event.getInteraction()
                    .editMessageEmbeds(
                            new EmbedBuilder()
                                    .setTitle(String.format("「%s」を有効化しました", fileName))
                                    .setColor(Color.GREEN)
                                    .build()
                    )
                    .setComponents()
                    .queue();
        } else {
            event.getInteraction()
                    .editMessage("")
                    .setEmbeds(
                            new EmbedBuilder()
                                    .setTitle(String.format("「%s」は既に有効化されています", fileName))
                                    .setColor(Color.GRAY)
                                    .build()
                    )
                    .setComponents()
                    .queue();
        }
    }

}
