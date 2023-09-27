package jp.xhw.datapackupload.plugin.datapack;

import jp.xhw.datapackupload.nms.datapack.IDataPackService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class DataPackManager {

    private final Plugin plugin;
    private final IDataPackService dataPackService;
    private final File dataPackDir;

    public DataPackManager(Plugin plugin) {
        this.plugin = plugin;
        try {
            final String packageName = Bukkit.getServer().getClass().getPackage().getName();
            final String version = packageName.substring(packageName.lastIndexOf('.') + 1);
            this.dataPackService = (IDataPackService) Class.forName("jp.xhw.datapackupload.nms." + version + ".datapack.DataPackService")
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            throw new RuntimeException(e);
        }
        final String worldName = plugin.getServer().getWorlds().get(0).getName();
        this.dataPackDir = new File(worldName + "/datapacks/");
    }

    public List<String> getDataPackNames() {
        final List<String> response = new ArrayList<>();
        final File[] dataPackFiles = dataPackDir.listFiles();
        if (dataPackFiles == null) return response;
        for (File file : dataPackFiles) {
            if (file.getName().endsWith(".zip")) {
                response.add(file.getName());
            }
        }
        return response;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public byte[] getDataPackData(String fileName) {
        final File file = new File(dataPackDir, fileName);
        if (!file.exists()) {
            return new byte[0];
        }
        try (FileInputStream inputStream = new FileInputStream(file)) {
            final byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public PackState getPackState(String fileName) {
        dataPackService.reloadPackRepository();
        final String entry = fileNameToEntry(fileName);
        final boolean isExists = new File(dataPackDir, fileName).exists();
        final boolean isAvailable = dataPackService.getAvailablePacks().contains(entry);
        final boolean isSelected = dataPackService.getSelectedPacks().contains(entry);
        if (!isExists) {
            return PackState.NOT_EXISTS;
        }
        if (!isAvailable) {
            return PackState.NOT_AVAILABLE;
        }
        if (!isSelected) {
            return PackState.DISABLED;
        }
        return PackState.ENABLED;
    }

    public CompletableFuture<Void> enable(String fileName) {
        return dataPackService.enablePack(fileNameToEntry(fileName));
    }

    public CompletableFuture<Void> disable(String fileName) {
        return dataPackService.disablePack(fileNameToEntry(fileName));
    }

    public CompletableFuture<Void> install(String fileName, byte[] data, boolean enableIfDisabled) {
        final File file = new File(dataPackDir, fileName);
        final PackState packState = getPackState(fileName);
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return CompletableFuture.completedFuture(null);
                }
            }

            try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
                outputStream.write(data);
            }

            if (packState == PackState.NOT_EXISTS || packState == PackState.NOT_AVAILABLE) {
                dataPackService.reloadPackRepository();
                return dataPackService.enablePack(fileNameToEntry(fileName));
            } else {
                CompletableFuture<Void> response = dataPackService.reloadPacks();
                if (enableIfDisabled) {
                    response.thenRun(() -> dataPackService.enablePack(fileNameToEntry(fileName)).join());
                }
                return response;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public CompletableFuture<Void> uninstall(String fileName) {
        switch (getPackState(fileName)) {
            case NOT_EXISTS -> {
                return CompletableFuture.completedFuture(null);
            }
            case ENABLED -> {
                return dataPackService.disablePack(fileNameToEntry(fileName))
                        .thenRun(() -> {
                            new File(dataPackDir, fileName).delete();
                            dataPackService.reloadPacks().join();
                        });
            }
            case DISABLED -> {
                return CompletableFuture
                        .supplyAsync(() -> {
                            new File(dataPackDir, fileName).delete();
                            return null;
                        })
                        .thenRun(() -> dataPackService.reloadPacks().join());
            }
            case NOT_AVAILABLE -> {
                return CompletableFuture
                        .supplyAsync(() -> {
                            new File(dataPackDir, fileName).delete();
                            return null;
                        });
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    private String fileNameToEntry(String fileName) {
        return "file/" + fileName;
    }

}