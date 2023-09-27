package jp.xhw.datapackupload.plugin.discord;

import jp.xhw.datapackupload.plugin.discord.panel.interact.ButtonPanelInteraction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class EventHandler extends ListenerAdapter {

    private final DiscordBot bot;

    public EventHandler(DiscordBot bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!this.bot.getPlugin().getSettings().getUploadChannels().contains(event.getChannel().getIdLong())) {
            return;
        }

        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            if (attachment.getFileExtension() == null || !attachment.getFileExtension().equals("zip")) {
                continue;
            }

            Message message = event.getMessage().replyEmbeds(
                    new EmbedBuilder()
                            .setTitle("ファイルを確認しています...")
                            .setColor(Color.GRAY)
                            .build()
            ).complete();
            this.bot.createPanel(event.getMessage().getAuthor().getId(), attachment, message);
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        this.bot.handleInteraction(new ButtonPanelInteraction(event.getInteraction()));
    }

}
