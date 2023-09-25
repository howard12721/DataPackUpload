package jp.xhw.datapackupload.plugin.discord.handlers;

import jp.xhw.datapackupload.plugin.discord.DiscordBot;
import jp.xhw.datapackupload.plugin.discord.MessageHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

public class MessageHandlerImpl extends MessageHandler {

    public MessageHandlerImpl(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void handle(MessageReceivedEvent event) {
        if (!this.bot.getPlugin().getSettings().getUploadChannels().contains(event.getChannel().getIdLong())) {
            return;
        }

        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            if (attachment.getFileExtension() == null || !attachment.getFileExtension().equals("zip")) {
                continue;
            }
            if (this.bot.getPlugin().getDataPackManager().dataPackExists(attachment.getFileName())) {
                event.getMessage()
                        .replyEmbeds(
                                new EmbedBuilder()
                                        .setTitle(String.format("「%s」は既に存在しています。上書きしますか?", attachment.getFileName()))
                                        .setColor(Color.RED)
                                        .build()
                        )
                        .addActionRow(
                                net.dv8tion.jda.api.interactions.components.buttons.Button.success("overwrite-" + attachment.getId() + "-" + event.getMessageId() + "-" + event.getMessage().getAuthor().getId(), "上書き"),
                                Button.secondary("cancel-" + event.getMessage().getAuthor().getId(), "キャンセル")
                        )
                        .queue();
                continue;
            }
            Message message = event.getMessage().replyEmbeds(
                            new EmbedBuilder()
                                    .setTitle(String.format("「%s」をダウンロードしています...", attachment.getFileName()))
                                    .setColor(Color.GRAY)
                                    .build()
                    )
                    .complete();
            this.bot.downloadDataPack(attachment).thenAccept(
                    dataPackData -> {
                        if (dataPackData == null) {
                            message
                                    .editMessageEmbeds(
                                            new EmbedBuilder()
                                                    .setTitle(String.format("「%s」は有効なデータパックではありません", attachment.getFileName()))
                                                    .setColor(Color.RED)
                                                    .build()
                                    )
                                    .queue();
                            return;
                        }
                        this.bot.getPlugin().getDataPackManager().install(dataPackData.fileName(), dataPackData.data(), false);
                        message
                                .editMessageEmbeds(
                                        new EmbedBuilder()
                                                .setTitle(String.format("「%s」をサーバーに導入しました！", attachment.getFileName()))
                                                .setColor(Color.GREEN)
                                                .build()
                                )
                                .queue();
                    }
            );

        }
    }
}
