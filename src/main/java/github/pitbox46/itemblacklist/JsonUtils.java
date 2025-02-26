package github.pitbox46.itemblacklist;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import github.pitbox46.itemblacklist.blacklist.Blacklist;
import github.pitbox46.itemblacklist.blacklist.Group;
import net.minecraft.Util;
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
import java.util.Optional;

public class JsonUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogManager.getLogger();

    public static File initialize(Path folder, String fileName) {
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
                    Group defaultGroup = new Group(
                            "default",
                            new Group.Properties(
                                    0,
                                    5,
                                    Optional.empty(),
                                    Optional.empty(),
                                    Optional.empty(),
                                    Optional.empty()
                            )
                    );
                    Blacklist emptyBlacklist = new Blacklist(new ArrayList<>(), Util.make(new ArrayList<>(1), l -> l.add(defaultGroup)));
                    configWriter.write(GSON.toJson(emptyBlacklist.encodeToJSON()));
                    configWriter.close();
                }
            }
        } catch(IOException e) {
            LOGGER.warn(e.getMessage());
        }
        return file;
    }

    public static Blacklist readFromJson(File jsonFile) {
        try {
            Reader reader = new FileReader(jsonFile);
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            return Blacklist.readBlacklist(json);
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return null;
    }

    public static void writeJson(File jsonFile, Blacklist blacklist) {
        try (Reader reader = new FileReader(jsonFile)) {
            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                fileWriter.write(GSON.toJson(blacklist.encodeToJSON()));
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }
}
