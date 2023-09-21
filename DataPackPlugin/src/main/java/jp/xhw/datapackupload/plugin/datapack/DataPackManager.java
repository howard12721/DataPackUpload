package jp.xhw.datapackupload.plugin.datapack;

import jp.xhw.datapackupload.nms.datapack.IDataPackService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;

public class DataPackManager {

    private final Plugin plugin;
    private final IDataPackService dataPackService;

    public DataPackManager(Plugin plugin) {
        this.plugin = plugin;
        try {
            this.dataPackService = (IDataPackService) Class.forName("jp.xhw.datapackupload.nms." + Bukkit.getVersion() + ".datapack.DataPackService")
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            throw new RuntimeException(e);
        }
    }

}