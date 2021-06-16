package github.pitbox46.itemblacklist;

import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FileUtils;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogManager.getLogger();

    public static File initialize(Path folder, String folderName, String fileName) {
        File file = new File(FileUtils.getOrCreateDirectory(folder, folderName).toFile(), fileName);
        try {
            if(file.createNewFile()) {
                FileWriter configWriter = new FileWriter(file);
                configWriter.write(gson.toJson(new JsonArray()));
                configWriter.close();
            }
        } catch(IOException e) {
            LOGGER.warn(e.getMessage());
        }
        return file;
    }

    /**
     * Reads items from a Json that has a top level array
     */
    public static List<Item> readItemsFromJson(File jsonFile) {
        try {
            Reader reader = new FileReader(jsonFile);
            JsonArray array = JSONUtils.fromJson(gson, reader, JsonArray.class);
            List<Item> returnedArrays = new ArrayList<>();
            assert array != null;
            for(JsonElement element: array) {
                returnedArrays.add(ForgeRegistries.ITEMS.getValue(new ResourceLocation(element.getAsString())).getItem());
            }
            return returnedArrays;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Writes a new item to a json that has a top level array
     */
    public static void appendItemToJson(File jsonFile, Item item) {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonArray array = JSONUtils.fromJson(gson, reader, JsonArray.class);
            assert array != null;

            JsonPrimitive string = new JsonPrimitive(item.getRegistryName().toString());
            if(!array.contains(string))
                array.add(string);

            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                fileWriter.write(gson.toJson(array));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ItemBlacklist.BANNED_ITEMS = JsonUtils.readItemsFromJson(ItemBlacklist.BANLIST);
    }

    /**
     * Removes an item from a json that has a top level array
     */
    public static void removeItemFromJson(File jsonFile, Item item) throws IndexOutOfBoundsException {
        try (Reader reader = new FileReader(jsonFile)) {
            JsonArray array = JSONUtils.fromJson(gson, reader, JsonArray.class);
            assert array != null;
            int itemLocation = -1;
            int i = 0;
            for(JsonElement element: array) {
                if(element.getAsString().equals(item.getRegistryName().toString())) itemLocation = i;
                i++;
            }
            array.remove(itemLocation);
            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                fileWriter.write(gson.toJson(array));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ItemBlacklist.BANNED_ITEMS = JsonUtils.readItemsFromJson(ItemBlacklist.BANLIST);
    }

    public static void removeAllItemsFromJson(File jsonFile) throws IndexOutOfBoundsException {
        try (FileWriter fileWriter = new FileWriter(jsonFile)) {
            fileWriter.write(gson.toJson(new JsonArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ItemBlacklist.BANNED_ITEMS = JsonUtils.readItemsFromJson(ItemBlacklist.BANLIST);
    }
}
