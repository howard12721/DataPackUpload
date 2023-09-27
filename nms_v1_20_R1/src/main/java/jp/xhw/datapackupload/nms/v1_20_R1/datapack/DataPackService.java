package jp.xhw.datapackupload.nms.v1_20_R1.datapack;

import jp.xhw.datapackupload.nms.datapack.IDataPackService;
import jp.xhw.datapackupload.nms.datapack.exceptions.DataPackNotFoundException;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class DataPackService implements IDataPackService {

    final Class<?> minecraftServerClass;
    final Class<?> craftServerClass;
    final Class<?> packRepositoryClass;
    final Class<?> packClass;
    final Class<?> positionClass;

    final Field consoleField;

    final Method getPackRepositoryMethod;
    final Method reloadResourcesMethod;
    final Method reloadPackRepositoryMethod;
    final Method getAvailablePacksMethod;
    final Method getSelectedPacksMethod;
    final Method getPackMethod;
    final Method getDefaultPositionMethod;
    final Method getIdMethod;
    final Method insertMethod;

    {
        try {
            minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
            craftServerClass = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".CraftServer");
            packRepositoryClass = Class.forName("net.minecraft.server.packs.repository.ResourcePackRepository");
            packClass = Class.forName("net.minecraft.server.packs.repository.ResourcePackLoader");
            positionClass = Class.forName("net.minecraft.server.packs.repository.ResourcePackLoader$Position");

            consoleField = craftServerClass.getDeclaredField("console");
            consoleField.setAccessible(true);

            getPackRepositoryMethod = minecraftServerClass.getMethod("aB");
            reloadResourcesMethod = minecraftServerClass.getMethod("a", Collection.class);
            reloadPackRepositoryMethod = packRepositoryClass.getMethod("a");
            getAvailablePacksMethod = packRepositoryClass.getMethod("c");
            getSelectedPacksMethod = packRepositoryClass.getMethod("f");
            getPackMethod = packRepositoryClass.getMethod("c", String.class);
            getDefaultPositionMethod = packClass.getMethod("i");
            getIdMethod = packClass.getMethod("f");
            insertMethod = Arrays.stream(positionClass.getMethods())
                    .filter(method -> method.getName().equals("a") && method.getParameterCount() == 4)
                    .findAny()
                    .orElseThrow();

        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public CompletableFuture<Void> enablePack(String entry) {
        try {
            final Object minecraftServer = consoleField.get(Bukkit.getServer());
            final Object packRepository = getPackRepositoryMethod.invoke(minecraftServer);
            final Object pack = getPackMethod.invoke(packRepository, entry);
            if (pack == null) {
                return CompletableFuture.failedFuture(new DataPackNotFoundException());
            }
            final List<Object> packList = new ArrayList<>((Collection<?>) getSelectedPacksMethod.invoke(packRepository));
            if (packList.contains(pack)) {
                return CompletableFuture.completedFuture(null);
            }


            final Object position = getDefaultPositionMethod.invoke(pack);
            insertMethod.invoke(position, packList, pack, (Function<Object, Object>) o -> o, false);

            return reloadPacksInternal(convertPackListToIdList(packList));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> disablePack(String entry) {
        try {
            final Object minecraftServer = consoleField.get(Bukkit.getServer());
            final Object packRepository = getPackRepositoryMethod.invoke(minecraftServer);
            final Object pack = getPackMethod.invoke(packRepository, entry);
            if (pack == null) {
                return CompletableFuture.failedFuture(new DataPackNotFoundException());
            }
            final List<Object> packList = new ArrayList<>((Collection<?>) getSelectedPacksMethod.invoke(packRepository));
            if (!packList.contains(pack)) {
                return null;
            }

            packList.remove(pack);

            return reloadPacksInternal(convertPackListToIdList(packList));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void reloadPackRepository() {
        try {
            Object minecraftServer = consoleField.get(Bukkit.getServer());
            Object packRepository = getPackRepositoryMethod.invoke(minecraftServer);
            reloadPackRepositoryMethod.invoke(packRepository);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<Void> reloadPacks() {
        try {
            final Object minecraftServer = consoleField.get(Bukkit.getServer());
            final Object packRepository = getPackRepositoryMethod.invoke(minecraftServer);
            final List<Object> packList = new ArrayList<>((Collection<?>) getSelectedPacksMethod.invoke(packRepository));
            return reloadPacksInternal(convertPackListToIdList(packList));
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public List<String> getAvailablePacks() {
        try {
            final Object minecraftServer = consoleField.get(Bukkit.getServer());
            final Object packRepository = getPackRepositoryMethod.invoke(minecraftServer);
            final List<Object> packList = new ArrayList<>((Collection<?>) getAvailablePacksMethod.invoke(packRepository));
            return convertPackListToIdList(packList);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    @Override
    public List<String> getSelectedPacks() {
        try {
            final Object minecraftServer = consoleField.get(Bukkit.getServer());
            final Object packRepository = getPackRepositoryMethod.invoke(minecraftServer);
            final List<Object> packList = new ArrayList<>((Collection<?>) getSelectedPacksMethod.invoke(packRepository));
            return convertPackListToIdList(packList);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<Void> reloadPacksInternal(Collection<String> idList) {
        try {
            Object minecraftServer = consoleField.get(Bukkit.getServer());

            return (CompletableFuture<Void>) reloadResourcesMethod.invoke(minecraftServer, idList);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    private List<String> convertPackListToIdList(List<Object> packList) {
        return packList.stream()
                .map(p -> {
                    try {
                        return (String) getIdMethod.invoke(p);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
