package jp.xhw.datapackupload.plugin.datapack;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

public record DataPack(InputStream inputStream) {

    private static final Set<String> requiredEntries = Set.of(
            "data",
            "pack.mcmeta"
    );

    public DataPack {
        // Validation
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            Set<String> nameList = new HashSet<>();
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                nameList.add(entry.getName());
            }
            if (!nameList.containsAll(requiredEntries)) {
                throw new IllegalArgumentException("有効なデータパックファイルではありません");
            }
        } catch (ZipException e) {
            throw new IllegalArgumentException("有効なzipファイルではありません");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
