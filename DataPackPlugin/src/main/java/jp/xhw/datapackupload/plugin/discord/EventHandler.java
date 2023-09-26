package jp.xhw.datapackupload.plugin.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

public class EventHandler extends ListenerAdapter {

    private static final Set<String> requiredEntries = Set.of(
            "pack.mcmeta"
    );

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
            if (this.bot.getPlugin().getDataPackManager().dataPackExists(attachment.getFileName())) {
                event.getMessage()
                        .replyEmbeds(
                                new EmbedBuilder()
                                        .setTitle(String.format("「%s」は既に存在しています。上書きしますか?", attachment.getFileName()))
                                        .setColor(Color.RED)
                                        .build()
                        )
                        .addActionRow(
                                Button.success("overwrite-" + attachment.getId() + "-" + event.getMessageId() + "-" + event.getMessage().getAuthor().getId(), "上書き"),
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
            downloadDataPack(attachment).thenAccept(
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

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().startsWith("cancel-")) {
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
        if (event.getComponentId().startsWith("overwrite-")) {
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
                    downloadDataPack(attachment).thenAccept(
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
                                                    Button.success("enable-" + attachment.getFileName() + "-" + event.getInteraction().getUser().getId(), "有効化する"),
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
        if (event.getComponentId().startsWith("enable-")) {
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
        if (event.getComponentId().startsWith("no-")) {
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

    private CompletableFuture<DataPackData> downloadDataPack(Message.Attachment attachment) {
        final File tmpFile = new File(this.bot.getTmpDir(), UUID.randomUUID().toString());

        return attachment.getProxy()
                .downloadToFile(tmpFile)
                .thenApplyAsync(
                        file -> {
                            try (InputStream inputStream = new FileInputStream(file)) {
                                final String fileName = attachment.getFileName();
                                final byte[] data = new byte[inputStream.available()];
                                inputStream.read(data);

                                try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(data))) {
                                    boolean dataDirExists = false;
                                    Set<String> nameList = new HashSet<>();
                                    ZipEntry entry;
                                    while ((entry = zipInputStream.getNextEntry()) != null) {
                                        if (entry.getName().startsWith("data/")) {
                                            dataDirExists = true;
                                        }
                                        nameList.add(entry.getName());
                                    }
                                    if (!nameList.containsAll(requiredEntries) || !dataDirExists) {
                                        return null;
                                    }
                                } catch (ZipException e) {
                                    return null;
                                }

                                return new DataPackData(fileName, data);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                )
                .thenApplyAsync(
                        dp -> {
                            if (tmpFile.exists()) {
                                tmpFile.delete();
                            }
                            return dp;
                        }
                );
    }

}
