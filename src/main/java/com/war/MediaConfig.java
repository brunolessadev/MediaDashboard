
        package com.war;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashSet;
import java.util.Set;

public final class MediaConfig {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Set<String> favorites = new LinkedHashSet<>();

    private static boolean initialized = false;

    private MediaConfig() {
    }

    private static Path getConfigPath() {
        return MediaScanner.getMediaDirectory()
                .resolve("config.json");
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        Path configPath = getConfigPath();

        try {
            Files.createDirectories(configPath.getParent());

            if (Files.notExists(configPath)) {
                JsonObject initialConfig = new JsonObject();
                initialConfig.add("favoritos", new JsonArray());
                initialConfig.addProperty("tema", "CYBERPUNK");

                Files.writeString(
                        configPath,
                        GSON.toJson(initialConfig),
                        StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE
                );
            }

            String content = Files.readString(configPath);
            JsonElement parsed = JsonParser.parseString(content);

            if (!parsed.isJsonObject()) {
                throw new IllegalStateException(
                        "A raiz do config.json precisa ser um objeto JSON."
                );
            }

            JsonObject config = parsed.getAsJsonObject();

            favorites.clear();

            if (config.has("favoritos")
                    && config.get("favoritos").isJsonArray()) {

                JsonArray array = config.getAsJsonArray("favoritos");

                for (JsonElement element : array) {
                    if (element.isJsonPrimitive()
                            && element.getAsJsonPrimitive().isString()) {
                        favorites.add(element.getAsString());
                    }
                }
            }

            if (!config.has("tema")
                    || !config.get("tema").isJsonPrimitive()
                    || !config.get("tema").getAsJsonPrimitive().isString()) {
                config.addProperty("tema", "CYBERPUNK");
                writeConfig(config);
            }

            initialized = true;

        } catch (Exception e) {
            System.err.println(
                    "Erro ao carregar a configuração: " + e.getMessage()
            );
        }
    }

    public static synchronized boolean isFavorite(String id) {
        initialize();
        return favorites.contains(id);
    }

    public static synchronized void setFavorite(
            String id,
            boolean favorite
    ) {
        initialize();

        if (id == null || id.isBlank()) {
            return;
        }

        if (favorite) {
            favorites.add(id);
        } else {
            favorites.remove(id);
        }

        save();
    }

    private static synchronized void save() {
        initialize();

        try {
            JsonObject config = readConfig();
            JsonArray array = new JsonArray();

            favorites.stream()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(array::add);

            config.add("favoritos", array);

            writeConfig(config);

        } catch (IOException | RuntimeException e) {
            System.err.println(
                    "Erro ao salvar os favoritos: " + e.getMessage()
            );
        }
    }

    public static synchronized String getTheme() {
        initialize();

        try {
            JsonObject config = readConfig();

            if (config.has("tema")
                    && config.get("tema").isJsonPrimitive()
                    && config.get("tema").getAsJsonPrimitive().isString()) {
                return config.get("tema").getAsString();
            }

        } catch (IOException | RuntimeException e) {
            System.err.println(
                    "Erro ao carregar o tema: " + e.getMessage()
            );
        }

        return "CYBERPUNK";
    }

    public static synchronized void setTheme(String theme) {
        initialize();

        if (theme == null || theme.isBlank()) {
            return;
        }

        try {
            JsonObject config = readConfig();
            config.addProperty("tema", theme);
            writeConfig(config);

        } catch (IOException | RuntimeException e) {
            System.err.println(
                    "Erro ao salvar o tema: " + e.getMessage()
            );
        }
    }

    private static JsonObject readConfig() throws IOException {
        Path configPath = getConfigPath();

        if (Files.notExists(configPath)) {
            JsonObject config = new JsonObject();
            config.add("favoritos", new JsonArray());
            config.addProperty("tema", "CYBERPUNK");
            return config;
        }

        JsonElement parsed = JsonParser.parseString(
                Files.readString(configPath)
        );

        if (!parsed.isJsonObject()) {
            throw new IllegalStateException(
                    "A raiz do config.json precisa ser um objeto JSON."
            );
        }

        return parsed.getAsJsonObject();
    }

    private static void writeConfig(JsonObject config) throws IOException {
        Path configPath = getConfigPath();

        Files.createDirectories(configPath.getParent());

        Files.writeString(
                configPath,
                GSON.toJson(config),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }
}