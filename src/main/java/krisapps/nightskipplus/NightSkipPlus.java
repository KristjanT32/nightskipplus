package krisapps.nightskipplus;

import krisapps.nightskipplus.commands.ForceSkip;
import krisapps.nightskipplus.commands.SetLanguage;
import krisapps.nightskipplus.commands.VoteAgainst;
import krisapps.nightskipplus.commands.VoteSkip;
import krisapps.nightskipplus.commands.tabcompletion.LanguageAC;
import krisapps.nightskipplus.events.NightListener;
import krisapps.nightskipplus.events.NightStartEvent;
import krisapps.nightskipplus.utils.DataUtility;
import krisapps.nightskipplus.utils.LocalizationUtility;
import krisapps.nightskipplus.utils.MessageUtility;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public final class NightSkipPlus extends JavaPlugin {

    public FileConfiguration pluginConfig;
    public File configFile = new File(getDataFolder(), "config.yml");
    public FileConfiguration pluginLocalization;
    public File localizationFile = new File(getDataFolder(), "/localization/localization.yml");

    public FileConfiguration pluginData;
    public File dataFile = new File(getDataFolder(), "data.yml");

    public DataUtility dataUtility = new DataUtility(this);
    public MessageUtility messageUtility = new MessageUtility(this);
    public LocalizationUtility localizationUtility = new LocalizationUtility(this);

    public int TIME_CHECKER_TASK = -1;
    public HashMap<UUID, Boolean> cooldowns = new HashMap<UUID, Boolean>();
    File logFile = new File(getDataFolder(), "nightskip-plus.log");

    @Override
    public void onEnable() {
        registerCommands();
        registerEvents();
        loadFiles();
        startTimeChecker();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadFiles() {
        if (!configFile.getParentFile().exists() || !configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", true);
        }

        if (!dataFile.getParentFile().exists() || !dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!localizationFile.getParentFile().exists() || !localizationFile.exists()) {
            localizationFile.getParentFile().mkdirs();
            saveResource("en-US.yml", true);
            try {
                if (!Files.exists(localizationFile.toPath())) {
                    saveResource("localization.yml", true);
                    Files.move(Path.of(getDataFolder() + "/localization.yml"), localizationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                Files.move(Path.of(getDataFolder() + "/en-US.yml"), Path.of(getDataFolder().toPath() + "/localization/en-US.yml"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!Files.exists(Path.of(getDataFolder() + "/localization/en-US.yml"))) {
            saveResource("en-US.yml", true);
            try {
                Files.move(Path.of(getDataFolder() + "/en-US.yml"), Path.of(getDataFolder().toPath() + "/localization/en-US.yml"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        pluginConfig = new YamlConfiguration();
        pluginData = new YamlConfiguration();
        pluginLocalization = new YamlConfiguration();

        try {
            pluginConfig.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().warning("Failed to load the config file: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            pluginData.load(dataFile);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("Failed to load the data file: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            pluginLocalization.load(localizationFile);
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().warning("Failed to load the localization information file: " + e.getMessage());
            e.printStackTrace();
        }

        getLogger().info("Starting localization discovery...");
        loadLocalizations();
    }

    private void registerCommands() {
        // Commands here
        getCommand("votefor").setExecutor(new VoteSkip(this));
        getCommand("voteagainst").setExecutor(new VoteAgainst(this));
        getCommand("forceskip").setExecutor(new ForceSkip(this));
        getCommand("setlanguage").setExecutor(new SetLanguage(this));

        getCommand("setlanguage").setTabCompleter(new LanguageAC(this));
    }

    private void registerEvents() {
        // Events here
        getServer().getPluginManager().registerEvents(new NightListener(this), this);
    }

    private void loadLocalizations() {
        LocalizationUtility localizationUtility = new LocalizationUtility(this);

        int foundLocalizations = 0;
        ArrayList<String> langList = (ArrayList<String>) pluginLocalization.getList("languages");
        ArrayList<String> missingLocalizations = new ArrayList<>();

        for (String langCode : langList) {
            File langFile = new File(getDataFolder(), "/localization/" + langCode + ".yml");
            if (!langFile.exists()) {
                getLogger().warning("[404] Could not find the localization file for " + langCode);
                missingLocalizations.add(langCode);
            } else {
                getLogger().info("[OK] Successfully recognized localization file for " + langCode);
                foundLocalizations++;
            }
        }
        getLogger().info("Localization discovery complete. Found " + foundLocalizations + " localization files out of " + langList.size() + " specified localizations.");
        if (!missingLocalizations.isEmpty()) {
            getLogger().info("Missing localization files: " + Arrays.toString(missingLocalizations.toArray()));
        }
        localizationUtility.setupCurrentLanguageFile();
    }

    public int resetDefaultLanguageFile() {
        saveResource("en-US.yml", true);
        try {
            Files.move(Path.of(getDataFolder() + "/en-US.yml"), Path.of(getDataFolder().toPath() + "/localization/en-US.yml"), StandardCopyOption.REPLACE_EXISTING);
            return 200;
        } catch (IOException e) {
            e.printStackTrace();
            return 500;
        }
    }

    public void reloadCurrentLanguageFile() {
        localizationUtility.setupCurrentLanguageFile();
    }

    public void saveConfig() {
        try {
            pluginConfig.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().warning("An error occurred while trying to save the Configuration File.\nReason: " + e.getMessage());
        }
    }

    public void saveData() {
        try {
            pluginData.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().warning("An error occurred while trying to save the Data File.\nReason: " + e.getMessage());
        }
    }

    public void appendToLog(String msg) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
            LocalDateTime date = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());

            bw.append("\n").append("[" + DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss").format(date) + "] ").append(msg);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startTimeChecker() {

        for (World world : getServer().getWorlds()) {
            cooldowns.put(world.getUID(), true);
            System.out.println("Discovered world: " + world.getName() + ", (" + world.getUID() + ")");
        }

        TIME_CHECKER_TASK = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (World world : getServer().getWorlds()) {
                    long time = world.getTime();
                    if (time >= 12300 && time <= 12500) {
                        if (cooldowns.get(world.getUID())) {
                            getServer().getScheduler().runTaskLater(NightSkipPlus.this, () -> {
                                getServer().getPluginManager().callEvent(new NightStartEvent(world));
                            }, 0L);
                            cooldowns.replace(world.getUID(), false);
                        }
                    } else if (time >= 13400) {
                        cooldowns.replace(world.getUID(), true);
                    }
                }
            }
        }, 0, 20);
    }
}
