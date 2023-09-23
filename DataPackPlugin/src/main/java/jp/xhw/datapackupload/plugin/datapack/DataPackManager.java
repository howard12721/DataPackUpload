package jp.xhw.datapackupload.plugin.datapack;

import jp.xhw.datapackupload.nms.datapack.IDataPackService;
import jp.xhw.datapackupload.plugin.exceptions.DataPackAlreadyExistsException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

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

    public boolean dataPackExists(String fileName) {
        final File file = new File(dataPackDir, fileName);
        return file.exists();
    }

    public boolean isDisabled(String fileName) {
        dataPackService.reloadPackRepository();
        return !dataPackService.getSelectedPacks().contains(fileNameToEntry(fileName));
    }

    public void enable(String fileName) {
        dataPackService.enablePack(fileNameToEntry(fileName));
    }

    public void disable(String fileName) {
        dataPackService.disablePack(fileNameToEntry(fileName));
    }

    public void install(String fileName, byte[] data, boolean replace) {
        final File file = new File(dataPackDir, fileName);
        try {
            boolean reloadOnly = false;
            if (file.exists()) {
                if (!replace) {
                    throw new DataPackAlreadyExistsException();
                } else {
                    reloadOnly = true;
                }
            } else {
                if (!file.createNewFile()) {
                    return;
                }
            }
            try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
                outputStream.write(data);
            }

            if (reloadOnly) {
                dataPackService.reloadPacks();
            } else {
                dataPackService.reloadPackRepository();
                dataPackService.enablePack(fileNameToEntry(fileName));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uninstall(String fileName) {
        if (!dataPackExists(fileName)) return;

        dataPackService.disablePack(fileNameToEntry(fileName));

        new File(dataPackDir, fileName).delete();
    }

    private String fileNameToEntry(String fileName) {
        return "file/" + fileName;
    }

}