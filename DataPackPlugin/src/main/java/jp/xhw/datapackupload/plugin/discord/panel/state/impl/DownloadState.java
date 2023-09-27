package jp.xhw.datapackupload.plugin.discord.panel.state.impl;

import jp.xhw.datapackupload.plugin.DataPackPlugin;
import jp.xhw.datapackupload.plugin.discord.DataPackData;
import jp.xhw.datapackupload.plugin.discord.panel.IDataPackPanel;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelState;
import jp.xhw.datapackupload.plugin.discord.panel.state.PanelStateType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.awt.*;
import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

public class DownloadState extends PanelState {

    private static final Set<String> requiredEntries = Set.of(
            "pack.mcmeta"
    );

    public DownloadState(DataPackPlugin plugin) {
        super(plugin);
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void onStart(IDataPackPanel panel) {
        final File tmpFile = new File(getPlugin().getDataFolder() + "/tmp", UUID.randomUUID().toString());
        final Message.Attachment attachment = panel.getData().getTargetAttachment();
        final AtomicReference<PanelStateType> nextStateType = new AtomicReference<>();

        panel.getData().getPanelMessage()
                .editMessageEmbeds(
                        new EmbedBuilder()
                                .setTitle("ダウンロードしています...")
                                .setColor(Color.GRAY)
                                .build()
                )
                .complete();

        attachment.getProxy()
                .downloadToFile(tmpFile)
                .thenAcceptAsync(
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
                                        nextStateType.set(PanelStateType.VALIDATION_ERROR);
                                        return;
                                    }
                                } catch (ZipException e) {
                                    nextStateType.set(PanelStateType.VALIDATION_ERROR);
                                    return;
                                }

                                panel.getData().setDataPack(new DataPackData(fileName, data));
                                nextStateType.set(PanelStateType.INSTALL);
                                return;

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            nextStateType.set(PanelStateType.VALIDATION_ERROR);
                        }
                )
                .thenRunAsync(
                        () -> {
                            if (tmpFile.exists()) {
                                tmpFile.delete();
                            }
                            panel.changeState(nextStateType.get());
                        }
                );
    }

}
