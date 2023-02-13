package github.pitbox46.itemblacklist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class JsonFile<T extends JsonElement> {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogManager.getLogger();

    protected File file;
    protected T json;
    protected final String fileName;
    protected final T defaultElement;
    protected final Class<T> clazz;

    public JsonFile(String fileName, T defaultElement, Class<T> clazz) {
        this.fileName = fileName;
        this.defaultElement = defaultElement;
        this.clazz = clazz;
    }

    public void initialize(ServerStartingEvent event) {
        Path modFolder = event.getServer().getWorldPath(new LevelResource("serverconfig"));
        File file = new File(FileUtils.getOrCreateDirectory(modFolder, "serverconfig").toFile(), fileName);
        try {
            if(file.createNewFile()) {
                Path defaultConfigPath = FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).resolve(fileName);
                if (Files.exists(defaultConfigPath)) {
                    Files.copy(defaultConfigPath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    FileWriter configWriter = new FileWriter(file);
                    configWriter.write(GSON.toJson(defaultElement));
                    configWriter.close();
                }
            }
        } catch(IOException e) {
            LOGGER.warn(e.getMessage());
        }
        this.file = file;
        try {
            Reader reader = new FileReader(file);
            json = GsonHelper.fromJson(GSON, reader, clazz);
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    public void save(WorldEvent.Save event) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(GSON.toJson(json));
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
    }
}
