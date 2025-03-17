package github.pitbox46.unifiedcrops;

import com.google.gson.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogManager.getLogger();

    public static File initialize(Path folder, String fileName, HolderLookup.Provider registryAccess) {
        File file = new File(folder.toFile(), fileName);
        try {
            if(file.createNewFile()) {
                Path defaultConfigPath = FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).resolve("itemblacklist.json");
                if (Files.exists(defaultConfigPath)) {
                    //If a default config file exists, copy it
                    Files.copy(defaultConfigPath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    //If a default config file doesn't exist, create a null file
                    FileWriter configWriter = new FileWriter(file);
                    List<CropData> emptyBlacklist = new ArrayList<>();
                    configWriter.write(GSON.toJson(CropData.encodeToJson(registryAccess, emptyBlacklist)));
                    configWriter.close();
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