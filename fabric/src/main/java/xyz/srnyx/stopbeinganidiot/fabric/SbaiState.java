package xyz.srnyx.stopbeinganidiot.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


final class SbaiState {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("sbai.json");

    boolean enabled;
    boolean hardcoreOfflineDeaths;
    long deathWave;
    Map<String, Long> playerWave = new HashMap<>();

    @NotNull
    static SbaiState load(@NotNull Logger logger) {
        if (Files.notExists(FILE_PATH)) return new SbaiState();
        try (final Reader reader = Files.newBufferedReader(FILE_PATH)) {
            final SbaiState loaded = GSON.fromJson(reader, SbaiState.class);
            if (loaded == null) return new SbaiState();
            loaded.normalize();
            return loaded;
        } catch (final IOException | JsonParseException e) {
            logger.warn("Failed to read state from {}. Using defaults.", FILE_PATH, e);
            return new SbaiState();
        }
    }

    private void normalize() {
        if (playerWave == null) playerWave = new HashMap<>();
        if (deathWave < 0) deathWave = 0;
    }

    void save(@NotNull Logger logger) {
        try {
            Files.createDirectories(FILE_PATH.getParent());
            try (final Writer writer = Files.newBufferedWriter(FILE_PATH)) {
                GSON.toJson(this, writer);
            }
        } catch (final IOException e) {
            logger.warn("Failed to save state to {}", FILE_PATH, e);
        }
    }
}
