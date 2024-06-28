package github.pitbox46.itemblacklist;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

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
    public static List<Item> readItemsFromJson(File jsonFile) {
        try {
            Reader reader = new FileReader(jsonFile);
            JsonArray array = GsonHelper.fromJson(gson, reader, JsonArray.class);
            List<Item> returnedArrays = new ArrayList<>();
            assert array != null;
            for(JsonElement element: array) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(element.getAsString())).asItem();
                if(item != null && !(item instanceof AirItem)) {
                    returnedArrays.add(item);
                }
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
            JsonArray array = GsonHelper.fromJson(gson, reader, JsonArray.class);
            assert array != null;

            JsonPrimitive string = new JsonPrimitive(ForgeRegistries.ITEMS.getKey(item).toString());
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
            JsonArray array = GsonHelper.fromJson(gson, reader, JsonArray.class);
            assert array != null;
            int itemLocation = -1;
            int i = 0;
            for(JsonElement element: array) {
                if(element.getAsString().equals(ForgeRegistries.ITEMS.getKey(item).toString())) itemLocation = i;
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
