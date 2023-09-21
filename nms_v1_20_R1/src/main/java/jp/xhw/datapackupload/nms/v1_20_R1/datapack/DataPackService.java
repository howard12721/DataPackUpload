package jp.xhw.datapackupload.nms.v1_20_R1.datapack;

import jp.xhw.datapackupload.nms.datapack.IDataPackService;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class DataPackService implements IDataPackService {

    @Override
    public void enablePack(String entry) {
        try {
            // Classes
            final Class<?> minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
            final Class<?> craftServerClass = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".CraftServer");
            final Class<?> packRepositoryClass = Class.forName("net.minecraft.server.packs.repository.ResourcePackRepository");
            final Class<?> packClass = Class.forName("net.minecraft.server.packs.repository.ResourcePackLoader");
            final Class<?> positionClass = Class.forName("net.minecraft.server.packs.repository.ResourcePackLoader$Position");
            // Fields
            final Field consoleField = craftServerClass.getDeclaredField("console");
            consoleField.setAccessible(true);
            // Methods
            final Method getPackRepositoryMethod = minecraftServerClass.getMethod("aB");
            final Method reloadResourcesMethod = minecraftServerClass.getMethod("a", Collection.class);
            final Method getSelectedPacksMethod = packRepositoryClass.getMethod("f");
            final Method getPackMethod = packRepositoryClass.getMethod("c", String.class);
            final Method getDefaultPositionMethod = packClass.getMethod("i");
            final Method getIdMethod = packClass.getMethod("f");
            final Method insertMethod = Arrays.stream(positionClass.getMethods())
                    .filter(method -> method.getName().equals("a") && method.getParameterCount() == 4)
                    .findAny()
                    .orElseThrow();

            final Object minecraftServer = consoleField.get(Bukkit.getServer());
            final Object packRepository = getPackRepositoryMethod.invoke(minecraftServer);
            final Object pack = getPackMethod.invoke(packRepository, entry);
            if (pack == null) {
                throw new NoSuchElementException("データパックが見つかりませんでした");
            }
            final List<Object> packList = new ArrayList<>((Collection<?>) getSelectedPacksMethod.invoke(packRepository));
            if (packList.contains(pack)) {
                return;
            }


            final Object position = getDefaultPositionMethod.invoke(pack);
            insertMethod.invoke(position, packList, pack, (Function<Object, Object>) o -> o, false);

            Collection<String> idList = packList.stream()
                    .map(p -> {
                        try {
                            return (String) getIdMethod.invoke(p);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            reloadResourcesMethod.invoke(minecraftServer, idList);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disablePack(String entry) {
        try {
            // Classes
            final Class<?> minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
            final Class<?> craftServerClass = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".CraftServer");
            final Class<?> packRepositoryClass = Class.forName("net.minecraft.server.packs.repository.ResourcePackRepository");
            final Class<?> packClass = Class.forName("net.minecraft.server.packs.repository.ResourcePackLoader");
            // Fields
            final Field consoleField = craftServerClass.getDeclaredField("console");
            consoleField.setAccessible(true);
            // Methods
            final Method getPackRepositoryMethod = minecraftServerClass.getMethod("aB");
            final Method reloadResourcesMethod = minecraftServerClass.getMethod("a", Collection.class);
            final Method getSelectedPacksMethod = packRepositoryClass.getMethod("f");
            final Method getPackMethod = packRepositoryClass.getMethod("c", String.class);
            final Method getIdMethod = packClass.getMethod("f");

            final Object minecraftServer = consoleField.get(Bukkit.getServer());
            final Object packRepository = getPackRepositoryMethod.invoke(minecraftServer);
            final Object pack = getPackMethod.invoke(packRepository, entry);
            if (pack == null) {
                throw new NoSuchElementException("データパックが見つかりませんでした");
            }
            final List<Object> packList = new ArrayList<>((Collection<?>) getSelectedPacksMethod.invoke(packRepository));
            if (!packList.contains(pack)) {
                return;
            }

            packList.remove(pack);

            Collection<String> idList = packList.stream()
                    .map(p -> {
                        try {
                            return (String) getIdMethod.invoke(p);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            reloadResourcesMethod.invoke(minecraftServer, idList);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reloadPacks() {
        try {
            // Classes
            Class<?> minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
            Class<?> craftServerClass = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".CraftServer");
            Class<?> packRepositoryClass = Class.forName("net.minecraft.server.packs.repository.ResourcePackRepository");
            // Fields
            Field consoleField = craftServerClass.getDeclaredField("console");
            consoleField.setAccessible(true);
            //Methods
            Method getPackRepositoryMethod = minecraftServerClass.getMethod("aB");
            Method reloadMethod = packRepositoryClass.getMethod("a");

            Object minecraftServer = consoleField.get(Bukkit.getServer());
            Object packRepository = getPackRepositoryMethod.invoke(minecraftServer);
            reloadMethod.invoke(packRepository);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
