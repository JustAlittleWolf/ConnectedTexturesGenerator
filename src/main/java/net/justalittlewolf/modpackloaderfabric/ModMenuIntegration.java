package net.justalittlewolf.modpackloaderfabric;

import com.google.gson.*;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static net.justalittlewolf.modpackloaderfabric.ModpackLoaderFabric.URLReader;
import static net.justalittlewolf.modpackloaderfabric.ModpackLoaderFabric.updateMods;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {

            Gson gson = new Gson();
            String configPath = FabricLoader.getInstance().getConfigDir().toString() + "/ModpackLoaderConfig.json";
            File file = new File(configPath);
            JsonObject json = gson.fromJson("{\"local\":[],\"host\":[\"default\"],\"url\":[],\"lastUpdate\":0,\"updateInterval\":1,\"updateOnStart\":true}", JsonObject.class);

            try {
                if (!file.exists()) {
                    file.createNewFile();
                    FileWriter fileW = new FileWriter(configPath);
                    fileW.write(json.toString());
                    fileW.close();
                } else {
                    json = gson.fromJson(Files.readString(Paths.get(configPath)), JsonObject.class);
                    if (!json.has("lastUpdate")) {
                        json = gson.fromJson("{\"local\":[],\"host\":[\"default\"],\"url\":[],\"lastUpdate\":0,\"updateInterval\":1,\"updateOnStart\":true}", JsonObject.class);
                        FileWriter fileW = new FileWriter(configPath);
                        fileW.write(json.toString());
                        fileW.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.of("ModpackLoader Config"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            final Boolean[] updateOnStart = {true, false, false};
            final int[] updatePause = {1};
            Map<String, Boolean> localModpacksEnabled = new HashMap<>();
            Map<String, Boolean> hostModpacksEnabled = new HashMap<>();
            Map<String, Boolean> urlModpacksEnabled = new HashMap<>();

            ConfigCategory general = builder.getOrCreateCategory(Text.of("General"));
            general.addEntry(entryBuilder.startBooleanToggle(Text.of("Check for updates on game start"), json.get("updateOnStart").getAsBoolean())
                    .setDefaultValue(true)
                    .setTooltip(Text.of("Disabling this option basically disables the mod"))
                    .setSaveConsumer(newValue -> updateOnStart[0] = newValue)
                    .build());

            general.addEntry(entryBuilder.startIntField(Text.of("Update interval (in days)"), json.get("updateInterval").getAsInt())
                    .setDefaultValue(1)
                    .setTooltip(Text.of("How often the mod checks for updates"))
                    .setSaveConsumer(newValue -> updatePause[0] = newValue)
                    .build());
            general.addEntry(entryBuilder.startBooleanToggle(Text.of("Force update on next start"), false)
                    .setSaveConsumer(newValue -> updateOnStart[1] = newValue)
                    .build());
            general.addEntry(entryBuilder.startBooleanToggle(Text.of("Update mods on \"Save & Quit\""), false)
                    .setDefaultValue(false)
                    .setTooltip(Text.of("WARNING! This may take a while! Restart required."))
                    .setSaveConsumer(newValue -> updateOnStart[2] = newValue)
                    .build());

            ConfigCategory local = builder.getOrCreateCategory(Text.of("Local Modpacks"));
            File localModPackFolder = new File(FabricLoader.getInstance().getConfigDir().toString() + "/MPLF_Modpacks/");
            localModPackFolder.mkdirs();
            File[] localModpacks = localModPackFolder.listFiles();
            assert localModpacks != null;
            for (File localModpack : localModpacks) {
                if (localModpack.isFile()) {
                    String packName = FilenameUtils.removeExtension(localModpack.getName());
                    String description = null;
                    try {
                        JsonObject modPack = gson.fromJson(Files.readString(Paths.get(localModPackFolder + "\\" + packName + ".json")), JsonObject.class);
                        if (modPack.has("description")) {
                            description = modPack.get("description").getAsString();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    JsonPrimitive elem = new JsonPrimitive(packName);
                    boolean state = json.get("local").getAsJsonArray().contains(elem);
                    if (description != null) {
                        local.addEntry(entryBuilder.startBooleanToggle(Text.of(packName), state)
                                .setTooltip(Text.of(description))
                                .setSaveConsumer(newValue -> localModpacksEnabled.put(packName, newValue))
                                .build());
                    } else {
                        local.addEntry(entryBuilder.startBooleanToggle(Text.of(packName), state)
                                .setSaveConsumer(newValue -> localModpacksEnabled.put(packName, newValue))
                                .build());
                    }
                }
            }
            local.addEntry(entryBuilder.startTextDescription(Text.of("Add your own by creating a file in " + localModPackFolder)).build());

            ConfigCategory host = builder.getOrCreateCategory(Text.of("Hosted Modpacks"));
            JsonArray hostPacks = null;
            try {
                URL hostURL = new URL("https://wolfii.me/ModpackLoaderFabric/availableModpacks.php");
                hostPacks = gson.fromJson(URLReader(hostURL), JsonArray.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert hostPacks != null;
            for (JsonElement hostModpack : hostPacks) {
                String packName = FilenameUtils.removeExtension(hostModpack.getAsString());
                String description = null;
                try {
                    JsonObject modPack = gson.fromJson(URLReader(new URL("https://wolfii.me/ModpackLoaderFabric/packs/" + packName + ".json")), JsonObject.class);
                    if (modPack.has("description")) {
                        description = modPack.get("description").getAsString();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JsonPrimitive elem = new JsonPrimitive(packName);
                boolean state = json.get("host").getAsJsonArray().contains(elem);
                if (description != null) {
                    host.addEntry(entryBuilder.startBooleanToggle(Text.of(packName), state)
                            .setTooltip(Text.of(description))
                            .setSaveConsumer(newValue -> hostModpacksEnabled.put(packName, newValue))
                            .build());
                } else {
                    host.addEntry(entryBuilder.startBooleanToggle(Text.of(packName), state)
                            .setSaveConsumer(newValue -> hostModpacksEnabled.put(packName, newValue))
                            .build());
                }
            }
            host.addEntry(entryBuilder.startTextDescription(Text.of("Create your own on https://wolfii.me/ModpackLoaderFabric")).build());

            ConfigCategory url = builder.getOrCreateCategory(Text.of("External Modpacks"));
            JsonArray urlPacks = json.get("url").getAsJsonArray();
            assert urlPacks != null;
            for (JsonElement urlModpack : urlPacks) {
                String packName = urlModpack.getAsString();
                String description = null;
                try {
                    JsonObject modPack = gson.fromJson(URLReader(new URL(urlModpack.getAsString())), JsonObject.class);
                    if (modPack.has("description")) {
                        description = modPack.get("description").getAsString();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JsonPrimitive elem = new JsonPrimitive(packName);
                boolean state = json.get("url").getAsJsonArray().contains(elem);
                if (description != null) {
                    url.addEntry(entryBuilder.startBooleanToggle(Text.of(packName), state)
                            .setTooltip(Text.of(description))
                            .setSaveConsumer(newValue -> urlModpacksEnabled.put(packName, newValue))
                            .build());
                } else {
                    url.addEntry(entryBuilder.startBooleanToggle(Text.of(packName), state)
                            .setSaveConsumer(newValue -> urlModpacksEnabled.put(packName, newValue))
                            .build());
                }
            }

            JsonObject finalJson = json;

            url.addEntry(entryBuilder.startStrField(Text.of("Custom Modpack (url)"), "")
                    .setDefaultValue("")
                    .setTooltip(Text.of("Click \"Save & Quit\" to save. Invalid URL's will be dismissed"))
                    .setSaveConsumer(newValue -> {
                        if (!finalJson.get("url").getAsJsonArray().contains(new JsonPrimitive(newValue))) {
                            urlModpacksEnabled.put(newValue, true);
                        }
                    })
                    .build());
            url.addEntry(entryBuilder.startTextDescription(Text.of("Custom URL's for a modpack - only use this if you know what you're doing.\nFormat: https://example.com/Modpack.json")).build());

            builder.setSavingRunnable(() ->
            {
                finalJson.addProperty("updateInterval", updatePause[0]);
                finalJson.addProperty("updateOnStart", updateOnStart[0]);
                if (updateOnStart[1]) {
                    finalJson.addProperty("lastUpdate", 0);
                }
                for (Map.Entry<String, Boolean> entry : localModpacksEnabled.entrySet()) {
                    JsonPrimitive elem = new JsonPrimitive(entry.getKey());
                    if (entry.getValue() && !finalJson.get("local").getAsJsonArray().contains(elem)) {
                        finalJson.get("local").getAsJsonArray().add(elem);
                    } else if (!entry.getValue() && finalJson.get("local").getAsJsonArray().contains(elem)) {
                        finalJson.get("local").getAsJsonArray().remove(elem);
                    }
                }

                for (Map.Entry<String, Boolean> entry : hostModpacksEnabled.entrySet()) {
                    JsonPrimitive elem = new JsonPrimitive(entry.getKey());
                    if (entry.getValue() && !finalJson.get("host").getAsJsonArray().contains(elem)) {
                        finalJson.get("host").getAsJsonArray().add(elem);
                    } else if (!entry.getValue() && finalJson.get("host").getAsJsonArray().contains(elem)) {
                        finalJson.get("host").getAsJsonArray().remove(elem);
                    }
                }

                for (Map.Entry<String, Boolean> entry : urlModpacksEnabled.entrySet()) {
                    JsonPrimitive elem = new JsonPrimitive(entry.getKey());
                    if (entry.getValue() && !finalJson.get("url").getAsJsonArray().contains(elem)) {
                        try {
                            if (!Objects.equals(entry.getKey(), "")) {
                                URL hostURL = new URL(entry.getKey());
                                JsonObject urlPack = gson.fromJson(URLReader(hostURL), JsonObject.class);
                                if (urlPack.has("curseforge") || urlPack.has("modrinth")) {
                                    finalJson.get("url").getAsJsonArray().add(elem);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (!entry.getValue() && finalJson.get("url").getAsJsonArray().contains(elem)) {
                        finalJson.get("url").getAsJsonArray().remove(elem);
                    }
                }

                try {
                    FileWriter fileW = new FileWriter(configPath);
                    fileW.write(finalJson.toString());
                    fileW.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(updateOnStart[2]) {
                    updateMods(true);
                }
            });

            return builder.build();
        };
    }
}
