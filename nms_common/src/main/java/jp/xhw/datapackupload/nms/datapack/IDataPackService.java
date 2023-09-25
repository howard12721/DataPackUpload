package jp.xhw.datapackupload.nms.datapack;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IDataPackService {

    CompletableFuture<Void> enablePack(String entry);

    CompletableFuture<Void> disablePack(String entry);

    void reloadPackRepository();

    CompletableFuture<Void> reloadPacks();

    List<String> getSelectedPacks();

}
