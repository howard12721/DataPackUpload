package jp.xhw.datapackupload.plugin.discord.handlers;

import jp.xhw.datapackupload.plugin.discord.ButtonHandler;
import jp.xhw.datapackupload.plugin.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

public class OverwriteButton extends ButtonHandler {

    public OverwriteButton(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void handle(ButtonInteractionEvent event) {
        final String componentId = event.getComponentId();
        final int lastIndex = componentId.lastIndexOf('-');
        final int secondLastIndex = componentId.substring(0, componentId.lastIndexOf('-')).lastIndexOf('-');
        final String targetAttachmentId = componentId.substring(10, secondLastIndex);
        final String messageId = componentId.substring(secondLastIndex + 1, lastIndex);
        final String userId = componentId.substring(lastIndex + 1);
        final Message message = event.getChannel().retrieveMessageById(messageId).complete();
        if (!event.getInteraction().getUser().getId().equals(userId)) {
            return;
        }
        if (message == null) {
            event.getInteraction().editMessageEmbeds(
                            new EmbedBuilder()
                                    .setTitle("メッセージが見つかりませんでした")
                                    .setColor(Color.RED)
                                    .build()
                    )
                    .setComponents()
                    .queue();
            return;
        }

        for (Message.Attachment attachment : message.getAttachments()) {
            if (attachment.getFileExtension() == null || !attachment.getFileExtension().equals("zip")) {
                continue;
            }
            if (attachment.getId().equals(targetAttachmentId)) {
                event.getInteraction().editMessageEmbeds(
                                new EmbedBuilder()
                                        .setTitle(String.format("「%s」を上書きします...", attachment.getFileName()))
                                        .setColor(Color.GRAY)
                                        .build()
                        )
                        .setComponents()
                        .queue();
                this.bot.downloadDataPack(attachment).thenAccept(
                        dataPackData -> {
                            if (dataPackData == null) {
                                event.getMessage()
                                        .editMessageEmbeds(new EmbedBuilder()
                                                .setTitle(String.format("「%s」は有効なデータパックではありません", attachment.getFileName()))
                                                .setColor(Color.RED)
                                                .build())
                                        .queue();
                                return;
                            }
                            this.bot.getPlugin().getDataPackManager().install(dataPackData.fileName(), dataPackData.data(), true);
                            if (this.bot.getPlugin().getDataPackManager().isDisabled(dataPackData.fileName())) {
                                event.getMessage()
                                        .editMessageEmbeds(
                                                new EmbedBuilder()
                                                        .setTitle(String.format("「%s」は無効化されています。有効にしますか?", attachment.getFileName()))
                                                        .setColor(Color.GRAY)
                                                        .build()
                                        )
                                        .setActionRow(
                                                net.dv8tion.jda.api.interactions.components.buttons.Button.success("enable-" + attachment.getFileName() + "-" + event.getInteraction().getUser().getId(), "有効化する"),
                                                Button.secondary("no-" + event.getInteraction().getUser().getId(), "有効化しない")
                                        )
                                        .queue();
                            } else {
                                event.getMessage()
                                        .editMessageEmbeds(
                                                new EmbedBuilder()
                                                        .setTitle(String.format("「%s」を上書きしました", attachment.getFileName()))
                                                        .setColor(Color.GREEN)
                                                        .build()
                                        )
                                        .queue();
                            }
                        }
                );
            }
        }
    }

}
