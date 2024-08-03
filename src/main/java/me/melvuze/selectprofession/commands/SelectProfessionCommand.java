package me.melvuze.selectprofession.commands;

import me.melvuze.selectprofession.SelectProfession;
import me.melvuze.selectprofession.core.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SelectProfessionCommand implements TabCompleter, CommandExecutor {
    private SelectProfession plugin;
    public SelectProfessionCommand(SelectProfession plugin){
        this.plugin = plugin;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return List.of();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(!(sender instanceof Player player)){
            sender.sendMessage(Config.getMessage("only-for-players"));
            return true;
        }

        plugin.getEngine().openGui(player);


        return true;
    }
}
