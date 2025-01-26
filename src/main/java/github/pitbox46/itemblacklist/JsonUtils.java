package github.pitbox46.itemblacklist;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JsonUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogManager.getLogger();

    public static File initialize(Path folder, String folderName, String fileName) {
        File file = new File(getOrCreateDirectory(folder.resolve(folderName)).toFile(), fileName);
        try {
            if(file.createNewFile()) {
                Path defaultConfigPath = FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).resolve("itemblacklist.json");
                if (Files.exists(defaultConfigPath)) {
                    Files.copy(defaultConfigPath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    FileWriter configWriter = new FileWriter(file);
                    configWriter.write(gson.toJson(new JsonArray()));
                    configWriter.close();
                }
            }
        } catch(IOException e) {
            LOGGER.warn(e.getMessage());
        }
        return file;
    }

    public static Path getOrCreateDirectory(Path path) {
        try {
            if (Files.exists(path)) {
                return path;
            } else {
                return Files.createDirectory(path);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads items from a Json that has a top level array
     */
    public static Set<Item> readItemsFromJson(File jsonFile) {
        try {
            Reader reader = new FileReader(jsonFile);
            JsonArray array = GsonHelper.fromJson(gson, reader, JsonArray.class);
            Set<Item> returnedArrays = new HashSet<>();
            for(JsonElement element: array) {
                Item item = BuiltInRegistries.ITEM.getValue(ResourceLocation.parse(element.getAsString()));
                if(!(item instanceof AirItem)) {
                    returnedArrays.add(item);
                }
            }
            return returnedArrays;
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return null;
    }

    /**
     * Writes a new item to a json that has a top level array
     */
    public static void appendItemToJson(File jsonFile, Item item) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonArray array = GsonHelper.fromJson(gson, reader, JsonArray.class);
            JsonPrimitive string = new JsonPrimitive(BuiltInRegistries.ITEM.getKey(item).toString());
            if(!array.contains(string))
                array.add(string);

            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                fileWriter.write(gson.toJson(array));
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
        ItemBlacklist.BANNED_ITEMS = JsonUtils.readItemsFromJson(ItemBlacklist.BANLIST);
    }

    /**
     * Removes an item from a json that has a top level array
     */
    public static void removeItemFromJson(File jsonFile, Item item) throws IndexOutOfBoundsException {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonArray array = GsonHelper.fromJson(gson, reader, JsonArray.class);
            int itemLocation = -1;
            int i = 0;
            for(JsonElement element: array) {
                if(element.getAsString().equals(BuiltInRegistries.ITEM.getKey(item).toString())) {
                    itemLocation = i;
                }
                i++;
            }
            array.remove(itemLocation);
            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                fileWriter.write(gson.toJson(array));
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
        ItemBlacklist.BANNED_ITEMS = JsonUtils.readItemsFromJson(ItemBlacklist.BANLIST);
    }

    public static void removeAllItemsFromJson(File jsonFile) throws IndexOutOfBoundsException {
        try (FileWriter fileWriter = new FileWriter(jsonFile)) {
            fileWriter.write(gson.toJson(new JsonArray()));
        } catch (IOException e) {
            LOGGER.error(e);
        }
        ItemBlacklist.BANNED_ITEMS = JsonUtils.readItemsFromJson(ItemBlacklist.BANLIST);
    }
}
