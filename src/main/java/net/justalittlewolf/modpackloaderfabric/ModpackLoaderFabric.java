package net.justalittlewolf.modpackloaderfabric;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ModpackLoaderFabric implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("modpackloaderfabric");
    public static final String modSuffix = "_MPLF";
    //public static final String MOD_ID = "modpackloaderfabric";

    @Override
    public void onInitialize() {
        updateMods(false);
    }

    public static void updateMods(boolean force) {
        boolean modPacksLoaded = false;

        Gson gson = new Gson();
        try {
            String configPath = FabricLoader.getInstance().getConfigDir().toString() + "/ModpackLoaderConfig.json";
            File file = new File(configPath);
            JsonObject json = gson.fromJson("{\"local\":[],\"host\":[\"default\"],\"url\":[],\"lastUpdate\":0,\"updateInterval\":1,\"updateOnStart\":true}", JsonObject.class);

            File localModPacks = new File(FabricLoader.getInstance().getConfigDir().toString() + "/MPLF_Modpacks");
            localModPacks.mkdirs();

            boolean skipLastUpdate = false;
            if (!file.exists()) {
                skipLastUpdate = true;
                file.createNewFile();
                FileWriter fileW = new FileWriter(configPath);
                fileW.write(json.toString());
                fileW.close();
            } else {
                json = gson.fromJson(Files.readString(Paths.get(configPath)), JsonObject.class);
                if (!json.has("lastUpdate")) {
                    skipLastUpdate = true;
                    json = gson.fromJson("{\"local\":[],\"host\":[\"default\"],\"url\":[],\"lastUpdate\":0,\"updateInterval\":1,\"updateOnStart\":true}", JsonObject.class);
                    FileWriter fileW = new FileWriter(configPath);
                    fileW.write(json.toString());
                    fileW.close();
                }
            }

            File modPackExample = new File(localModPacks + "/ModpackExample.json");
            if (!modPackExample.exists()) {
                ReadableByteChannel readableByteChannel = Channels.newChannel(new URL("https://wolfii.me/ModpackLoaderFabric/ModPackExample.json").openStream());
                FileOutputStream fileOutputStream = new FileOutputStream(String.valueOf(modPackExample));
                fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            }

            if (json.has("updateOnStart")) {
                if (json.get("updateOnStart").getAsBoolean()) {
                    long unixTime = Instant.now().getEpochSecond();
                    if ((unixTime - json.get("lastUpdate").getAsInt()) / 86400.0 >= json.get("updateInterval").getAsFloat() || json.get("updateInterval").getAsInt() == 0 || force) {
                        if (!skipLastUpdate) {
                            json.addProperty("lastUpdate", unixTime);
                            FileWriter fileW = new FileWriter(configPath);
                            fileW.write(json.toString());
                            fileW.close();
                        }
                        JsonObject modPack;
                        if (json.get("host").getAsJsonArray().size() > 0) {
                            for (int i = 0; i < json.get("host").getAsJsonArray().size(); i++) {
                                URL modPackURL = new URL("https://wolfii.me/ModpackLoaderFabric/packs/" + json.get("host").getAsJsonArray().get(i).getAsString() + ".json");
                                modPack = gson.fromJson(URLReader(modPackURL), JsonObject.class);
                                LOGGER.info("[ModpackLoaderFabric] Loading Modpack from " + modPackURL);
                                modPacksLoaded = loadModPack(modPack) || modPacksLoaded;
                            }
                        }
                        if (json.get("url").getAsJsonArray().size() > 0) {
                            for (int i = 0; i < json.get("url").getAsJsonArray().size(); i++) {
                                URL modPackURL = new URL(json.get("url").getAsJsonArray().get(i).getAsString());
                                modPack = gson.fromJson(URLReader(modPackURL), JsonObject.class);
                                LOGGER.info("[ModpackLoaderFabric] Loading Modpack from " + modPackURL);
                                modPacksLoaded = loadModPack(modPack) || modPacksLoaded;
                            }
                        }
                        if (json.get("local").getAsJsonArray().size() > 0) {
                            for (int i = 0; i < json.get("local").getAsJsonArray().size(); i++) {
                                modPack = gson.fromJson(Files.readString(Paths.get(localModPacks + "/" + json.get("local").getAsJsonArray().get(i).getAsString() + ".json")), JsonObject.class);
                                LOGGER.info("[ModpackLoaderFabric] Loading Modpack from " + localModPacks + "\\" + json.get("local").getAsJsonArray().get(i).getAsString() + ".json");
                                modPacksLoaded = loadModPack(modPack) || modPacksLoaded;
                            }
                        }
                    } else {
                        LOGGER.info("[ModpackLoaderFabric] Upading mods skipped (last update less than " + json.get("updateInterval").getAsInt() + " day(s) ago)");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (modPacksLoaded) {
            LOGGER.info("[ModpackLoaderFabric] All mods up to date");
        }
    }


    public static boolean loadModPack(JsonObject json) {
        boolean downloaded = false;
        try {
            if (json.has("modrinth")) {
                downloaded = true;
                JsonObject modrinthMods = json.get("modrinth").getAsJsonObject();

                JsonArray versionsArray = modrinthMods.get("versions").getAsJsonArray();
                String[] allowedVersionsDefault = new String[versionsArray.size()];
                for (int i = 0; i < versionsArray.size(); i++) {
                    allowedVersionsDefault[i] = versionsArray.get(i).getAsString();
                }

                JsonArray modsArray = modrinthMods.get("mods").getAsJsonArray();
                String[] modList = new String[modsArray.size()];
                for (int i = 0; i < modsArray.size(); i++) {
                    modList[i] = modsArray.get(i).getAsString();
                }

                for (String mod : modList) {
                    downloadModModrinth(mod, allowedVersionsDefault);
                }
            }

            if (json.has("curseforge")) {
                downloaded = true;
                JsonObject curseforgeMods = json.get("curseforge").getAsJsonObject();

                String allowedVersionsDefault = curseforgeMods.get("versions").getAsString();//73250

                JsonArray modsArray = curseforgeMods.get("mods").getAsJsonArray();
                String[] modList = new String[modsArray.size()];
                for (int i = 0; i < modsArray.size(); i++) {
                    modList[i] = modsArray.get(i).getAsString();
                }

                String apiKey = ""; //Add your Curseforge API-Token here https://docs.curseforge.com/#accessing-the-service

                for (String mod : modList) {
                    downloadModCurseforge(mod, allowedVersionsDefault, apiKey);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return downloaded;
    }

    public static void downloadModModrinth(String id, String[] allowedVersions) throws IOException {
        Gson gson = new Gson();

        String vers = gson.toJson(allowedVersions);
        URL req = new URL(("https://api.modrinth.com/v2/project/" + id + "/version?loaders=[\"fabric\"]&game_versions=" + vers).replaceAll("\"", "%22"));
        JsonArray modVersions = gson.fromJson(URLReader(req), JsonArray.class);

        if (modVersions.size() > 0) {
            URL downloadURL = new URL(modVersions.get(0).getAsJsonObject().get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString());

            String modDir = FabricLoader.getInstance().getGameDir().toString() + "\\mods";
            new File(modDir).mkdirs();
            String loadedModslistPath = FabricLoader.getInstance().getConfigDir().toString() + "/ModpackLoaderLoadedModlist.json";
            File file = new File(loadedModslistPath);
            JsonObject json = gson.fromJson("{}", JsonObject.class);

            if (!file.exists()) {
                file.createNewFile();
            } else {
                json = gson.fromJson(Files.readString(Paths.get(loadedModslistPath)), JsonObject.class);
                if (json == null) {
                    json = gson.fromJson("{}", JsonObject.class);
                }
            }
            String newFileName = modVersions.get(0).getAsJsonObject().get("files").getAsJsonArray().get(0).getAsJsonObject().get("filename").getAsString();
            int lastDotIndex = newFileName.lastIndexOf('.');
            newFileName = newFileName.substring(0, lastDotIndex) + modSuffix + newFileName.substring(lastDotIndex);
            String newVer = modVersions.get(0).getAsJsonObject().get("version_number").getAsString();
            boolean written = false;
            if (json.has(id)) {
                String loggedVer = json.get(id).getAsJsonObject().get("version").getAsString();
                if (!Objects.equals(loggedVer, newVer)) {
                    File toDelete = new File(modDir + "\\" + json.get(id).getAsJsonObject().get("file").getAsString());
                    if (toDelete.exists()) {
                        toDelete.delete();
                    }
                    writeFile(downloadURL, modDir + "\\" + newFileName);
                    written = true;
                }
            } else {
                File toDelete = new File(modDir + "\\" + newFileName);
                if (toDelete.exists()) {
                    toDelete.delete();
                }
                writeFile(downloadURL, modDir + "\\" + newFileName);
                written = true;
            }
            if (written) {
                if (json.has(id)) {
                    LOGGER.info("[ModpackLoaderFabric] Updated " + json.get(id).getAsJsonObject().get("file").getAsString() + " to " + newFileName);
                } else {
                    LOGGER.info("[ModpackLoaderFabric] Downloaded mod " + newFileName);
                }
                JsonObject jsonID = new JsonObject();
                jsonID.addProperty("version", newVer);
                jsonID.addProperty("file", newFileName);
                json.add(id, jsonID);
                FileWriter fileW = new FileWriter(loadedModslistPath);
                fileW.write(json.toString());
                fileW.close();
            }
        }
    }

    public static void downloadModCurseforge(String id, String allowedVersions, String key) throws IOException {
        Gson gson = new Gson();

        //String minecraftID = "432";
        URL req = new URL(("https://api.curseforge.com/v1/mods/" + id + "/files?pageSize=1&modLoaderType=fabric&gameVersionTypeId=" + allowedVersions).replaceAll("\"", "%22"));
        HttpURLConnection conn = (HttpURLConnection) req.openConnection();
        conn.setRequestProperty("x-api-key", key);

        BufferedReader br;
        if (100 <= conn.getResponseCode() && conn.getResponseCode() <= 399) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            sb.append(output);
        }
        output = sb.toString();
        JsonObject modVersions = gson.fromJson(output, JsonObject.class);

        if (modVersions.size() > 0) {
            if (modVersions.get("data").getAsJsonArray().size() > 0) {
                URL downloadURL = new URL(modVersions.get("data").getAsJsonArray().get(0).getAsJsonObject().get("downloadUrl").getAsString());

                String modDir = FabricLoader.getInstance().getGameDir().toString() + "\\mods";
                new File(modDir).mkdirs();
                String loadedModslistPath = FabricLoader.getInstance().getConfigDir().toString() + "/ModpackLoaderLoadedModlist.json";
                File file = new File(loadedModslistPath);
                JsonObject json = gson.fromJson("{}", JsonObject.class);

                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    json = gson.fromJson(Files.readString(Paths.get(loadedModslistPath)), JsonObject.class);
                    if (json == null) {
                        json = gson.fromJson("{}", JsonObject.class);
                    }
                }

                String newFileName = modVersions.get("data").getAsJsonArray().get(0).getAsJsonObject().get("fileName").getAsString();
                int lastDotIndex = newFileName.lastIndexOf('.');
                newFileName = newFileName.substring(0, lastDotIndex) + modSuffix + newFileName.substring(lastDotIndex);
                String newVer = modVersions.get("data").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
                boolean written = false;
                if (json.has(id)) {
                    String loggedVer = json.get(id).getAsJsonObject().get("version").getAsString();
                    if (!Objects.equals(loggedVer, newVer)) {
                        File toDelete = new File(modDir + "\\" + json.get(id).getAsJsonObject().get("file").getAsString());
                        if (toDelete.exists()) {
                            toDelete.delete();
                        }
                        writeFile(downloadURL, modDir + "\\" + newFileName);
                        written = true;
                    }
                } else {
                    File toDelete = new File(modDir + "\\" + newFileName);
                    if (toDelete.exists()) {
                        toDelete.delete();
                    }
                    writeFile(downloadURL, modDir + "\\" + newFileName);
                    written = true;
                }
                if (written) {
                    if (json.has(id)) {
                        LOGGER.info("[ModpackLoaderFabric] Updated " + json.get(id).getAsJsonObject().get("file").getAsString() + " to " + newFileName);
                    } else {
                        LOGGER.info("[ModpackLoaderFabric] Downloaded mod " + newFileName);
                    }
                    JsonObject jsonID = new JsonObject();
                    jsonID.addProperty("version", newVer);
                    jsonID.addProperty("file", newFileName);
                    json.add(id, jsonID);
                    FileWriter fileW = new FileWriter(loadedModslistPath);
                    fileW.write(json.toString());
                    fileW.close();
                }
            }
        }
    }

    public static void writeFile(URL downloadURL, String fileName) throws IOException {
        Gson gson = new Gson();

        ReadableByteChannel readableByteChannel = Channels.newChannel(downloadURL.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        JarFile downloaded = new JarFile(fileName);
        InputStream modJsonInputStream = downloaded.getInputStream(downloaded.getJarEntry("fabric.mod.json"));
        JsonObject fabricModJson = gson.fromJson(new BufferedReader(new InputStreamReader(modJsonInputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n")), JsonObject.class);
        String modID = fabricModJson.get("id").getAsString();
        File modFolder = new File(FabricLoader.getInstance().getGameDir().toString() + "\\mods");
        for (final File fileEntry : Objects.requireNonNull(modFolder.listFiles())) {
            if (fileEntry.isFile() && fileEntry.getName().endsWith(".jar")) {
                int lastDotIndex = fileEntry.getName().lastIndexOf('.');
                if (!fileEntry.getName().startsWith(modSuffix, lastDotIndex - modSuffix.length())) {
                    JarFile local = new JarFile(fileEntry.getAbsoluteFile());
                    InputStream localJsonInputStream = local.getInputStream(local.getJarEntry("fabric.mod.json"));
                    JsonObject fabricModJsonLocal = gson.fromJson(new BufferedReader(new InputStreamReader(localJsonInputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n")), JsonObject.class);
                    String modIDLocal = fabricModJsonLocal.get("id").getAsString();
                    localJsonInputStream.close();
                    local.close();
                    if (modIDLocal.equals(modID) && !fileName.equals(fileEntry.getAbsoluteFile().toString())) {
                        String oldFileName = fileEntry.getAbsoluteFile().toString();
                        Files.move(Path.of(oldFileName), Path.of(fileEntry.getAbsoluteFile() + ".old"));
                        LOGGER.info("[ModpackLoaderFabric] Disabled duplicate mod " + fileEntry.getName());
                    }
                }
            }
        }
    }

    public static String URLReader(URL url) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;

        try (InputStream in = url.openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }

        return sb.toString();
    }
}
