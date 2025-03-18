package github.pitbox46.unifiedcrops;

import com.google.gson.*;
import net.minecraft.core.HolderLookup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public class JsonUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogManager.getLogger();

    public static File initialize(Path folder, String fileName, HolderLookup.Provider registryAccess) {
        File file = new File(folder.toFile(), fileName);
        try {
            if(file.createNewFile()) {
                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    if (CropMapDataProvider.GEN_CROP_MAP) {
                        try (OutputStreamWriter configWriter = new OutputStreamWriter(outputStream)) {
                            List<CropData> emptyBlacklist;
                            emptyBlacklist = CropMapDataProvider.gather();
                            configWriter.write(GSON.toJson(CropData.encodeToJson(registryAccess, emptyBlacklist)));
                        }
                    } else {
                        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("crop_data.json")) {
                            inputStream.transferTo(outputStream);
                        }
                    }
                }
            }
        } catch(IOException e) {
            LOGGER.warn(e.getMessage());
        }
        return file;
    }

    public static List<CropData> readFromJson(File jsonFile, HolderLookup.Provider registryAccess) {
        try {
            Reader reader = new FileReader(jsonFile);
            JsonArray json = GSON.fromJson(reader, JsonArray.class);
            return CropData.decodeFromJson(registryAccess, json);
        } catch (IOException | JsonParseException e) {
            LOGGER.error(e);
        }
        return List.of();
    }
}