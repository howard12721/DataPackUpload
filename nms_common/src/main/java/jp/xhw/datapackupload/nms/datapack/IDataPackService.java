package jp.xhw.datapackupload.nms.datapack;

import java.util.List;

public interface IDataPackService {

    void enablePack(String entry);

    void disablePack(String entry);

    void reloadPackRepository();

    void reloadPacks();

    List<String> getSelectedPacks();

}
