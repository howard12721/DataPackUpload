package jp.xhw.datapackupload.plugin.discord;

import jp.xhw.datapackupload.plugin.DataPackPlugin;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

public class DiscordBot {

    private static final Set<String> requiredEntries = Set.of(
            "pack.mcmeta"
    );

    @Getter
    private final DataPackPlugin plugin;
    @Getter
    private JDA jda;
    @Getter
    private File tmpDir;

    public DiscordBot(DataPackPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        this.jda = JDABuilder
                .createDefault(plugin.getSettings().getBotToken(), GatewayIntent.GUILD_VOICE_STATES)
                .addEventListeners(new EventListener(this))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.SCHEDULED_EVENTS)
                .build();

        this.tmpDir = new File(plugin.getDataFolder() + "/tmp");
        if (!tmpDir.exists()) {
            tmpDir.mkdir();
        }
    }

    public void stop() {
        this.jda.shutdownNow();
    }

    public CompletableFuture<DataPackData> downloadDataPack(Message.Attachment attachment) {
        final File tmpFile = new File(tmpDir, UUID.randomUUID().toString());

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
