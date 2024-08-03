package me.melvuze.selectprofession;

import lombok.Getter;
import me.melvuze.selectprofession.commands.SelectProfessionCommand;
import me.melvuze.selectprofession.core.Config;
import me.melvuze.selectprofession.core.Keys;
import me.melvuze.selectprofession.engine.Engine;
import me.melvuze.selectprofession.enums.Commands;
import me.melvuze.selectprofession.listeners.InventoryClickedListener;
import me.melvuze.selectprofession.listeners.PlayerJoinListener;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class SelectProfession extends JavaPlugin {
    @Getter
    private Keys keys;
    @Getter
    private Engine engine;
    @Getter
    private LuckPerms api;

    @Override
    public void onEnable() {
        Config.register(this);
        keys = new Keys(this);
        engine = new Engine(this);
        api = LuckPermsProvider.get();

        getCommand(Commands.SELECT_PROFESSION).setExecutor(new SelectProfessionCommand(this));
        getCommand(Commands.SELECT_PROFESSION).setTabCompleter(new SelectProfessionCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickedListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
