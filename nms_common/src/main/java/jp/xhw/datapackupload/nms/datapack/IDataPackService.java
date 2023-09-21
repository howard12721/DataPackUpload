package jp.xhw.datapackupload.nms.datapack;

public interface IDataPackService {

    void enablePack(String entry);
    void disablePack(String entry);
    void reloadPacks();

}
