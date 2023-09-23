package jp.xhw.datapackupload.plugin.discord;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

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
            "data/",
            "pack.mcmeta"
    );

    private final DiscordBot bot;

    public EventHandler(DiscordBot bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            if (attachment.getFileExtension() == null || !attachment.getFileExtension().equals("zip")) {
                continue;
            }
            if (this.bot.getPlugin().getDataPackManager().dataPackExists(attachment.getFileName())) {
                event.getMessage().reply(String.format("「%s」は既に存在しています。上書きしますか?", attachment.getFileName()))
                        .addActionRow(
                                Button.success("overwrite-" + attachment.getId() + "-" + event.getMessageId() + "-" + event.getMessage().getAuthor().getId(), "上書き"),
                                Button.secondary("cancel-" + event.getMessage().getAuthor().getId(), "キャンセル")
                        )
                        .queue();
                continue;
            }
            Message message = event.getMessage().reply(String.format("「%s」をダウンロードしています...", attachment.getFileName())).complete();
            downloadDataPack(attachment).thenAccept(
                    dataPackData -> {
                        if (dataPackData == null) {
                            message.editMessage(String.format("「%s」は有効なデータパックではありません", attachment.getFileName())).queue();
                            return;
                        }
                        this.bot.getPlugin().getDataPackManager().install(dataPackData.fileName(), dataPackData.data(), false);
                        message.editMessage(String.format("「%s」をサーバーに導入しました！", attachment.getFileName())).queue();
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
            event.getInteraction().editMessage("キャンセルしました").setComponents().queue();
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
                event.getInteraction().reply("メッセージが見つかりませんでした").queue();
                return;
            }

            for (Message.Attachment attachment : message.getAttachments()) {
                if (attachment.getFileExtension() == null || !attachment.getFileExtension().equals("zip")) {
                    continue;
                }
                if (attachment.getId().equals(targetAttachmentId)) {
                    event.getInteraction().editMessage(String.format("「%s」を上書きします...", attachment.getFileName())).setComponents().queue();
                    downloadDataPack(attachment).thenAccept(
                            dataPackData -> {
                                if (dataPackData == null) {
                                    event.getMessage().editMessage(String.format("「%s」は有効なデータパックではありません", attachment.getFileName())).queue();
                                    return;
                                }
                                this.bot.getPlugin().getDataPackManager().install(dataPackData.fileName(), dataPackData.data(), true);
                                if (this.bot.getPlugin().getDataPackManager().isDisabled(dataPackData.fileName())) {
                                    event.getMessage().editMessage(String.format("「%s」は無効化されています。有効にしますか?", attachment.getFileName()))
                                            .setActionRow(
                                                    Button.success("enable-" + attachment.getFileName() + "-" + event.getInteraction().getUser().getId(), "有効化する"),
                                                    Button.secondary("no-" + event.getInteraction().getUser().getId(), "有効化しない")
                                            )
                                            .queue();
                                } else {
                                    event.getMessage().editMessage(String.format("「%s」を上書きしました", attachment.getFileName())).queue();
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
                event.getInteraction().editMessage(String.format("「%s」を有効化しました", fileName)).setComponents().queue();
            } else {
                event.getInteraction().editMessage(String.format("「%s」は既に有効化されています", fileName)).setComponents().queue();
            }
        }
        if (event.getComponentId().startsWith("no-")) {
            final String userId = event.getComponentId().substring(3);
            if (!event.getInteraction().getUser().getId().equals(userId)) {
                return;
            }
            event.getInteraction().editMessage("有効化せずに上書きしました").setComponents().queue();
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
                                    Set<String> nameList = new HashSet<>();
                                    ZipEntry entry;
                                    while ((entry = zipInputStream.getNextEntry()) != null) {
                                        nameList.add(entry.getName());
                                    }
                                    if (!nameList.containsAll(requiredEntries)) {
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