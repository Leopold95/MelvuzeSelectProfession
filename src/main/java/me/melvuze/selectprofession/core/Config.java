package me.melvuze.selectprofession.core;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Config {
    private static File messagesConfigFile;
    private static FileConfiguration messagesConfig;

    private static File configFile;
    private static FileConfiguration config;

    private static File designFile;
    @Getter
    private static FileConfiguration designConfig;

    public static void register(JavaPlugin plugin) {
        createMessagesConfig("messages.yml", plugin);
        createConfig("config.yml", plugin);
        createDesignConfig("design.yml", plugin);
    }

    public static ConfigurationSection getDesingSection(String path) { return designConfig.getConfigurationSection(path);}

    public static boolean existsConfig(String path){
        return config.contains(path);
    }

    public static boolean existsMessage(String path){
        return config.contains(path);
    }

    public static void setConfig(String path, Object object) {
        try {

            config.set(path, object);
            config.save(configFile);
        }
        catch (Exception exp){
            exp.printStackTrace();
        }
    }

    public static void setMessage(String path, Object object) throws IOException {
        messagesConfig.set(path, object);
        messagesConfig.save(messagesConfigFile);
    }

    public static ConfigurationSection getSection(String path){return config.getConfigurationSection(path);}

    public static List<String> getStringList(String path) {
        return config.getStringList(path).stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList()); // Or .collect(Collectors.toList()) for older Java versions
    }

    public static List<Integer> getIntList(String path) {
        return config.getIntegerList(path);
    }

    public static String getString(String path) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path));
    }

    public static int getInt(String path) {
        return config.getInt(path);
    }
    public static long getLong(String path) {
        return config.getLong(path);
    }


    public static boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public static double getDouble(String path) {
        return config.getDouble(path);
    }

    public static String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(path));
    }

    public static List<String> getMessageList(String path){
        List<String> l = new ArrayList<>();

        for(String line: messagesConfig.getStringList(path)){
            l.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        return l;
    }

    private static void createDesignConfig(String fileName, JavaPlugin plugin){
        designFile = new File(plugin.getDataFolder(), fileName);
        if (!designFile.exists()) {
            designFile.getParentFile().mkdirs();
            plugin.saveResource(fileName, false);
        }
        designConfig = YamlConfiguration.loadConfiguration(designFile);
        try {
            designConfig.save(designFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createMessagesConfig(String file, JavaPlugin plugin) {
        messagesConfigFile = new File(plugin.getDataFolder(), file);
        if (!messagesConfigFile.exists()) {
            messagesConfigFile.getParentFile().mkdirs();
            plugin.saveResource(file, false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesConfigFile);
        try {
            messagesConfig.save(messagesConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createConfig(String file, JavaPlugin plugin) {
        configFile = new File(plugin.getDataFolder(), file);
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource(file, false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
