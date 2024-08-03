package me.melvuze.selectprofession.commands;

import me.melvuze.selectprofession.SelectProfession;
import me.melvuze.selectprofession.core.Config;
import me.melvuze.selectprofession.enums.Commands;
import me.melvuze.selectprofession.enums.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
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
        return List.of(
                Commands.SELECT_PROFESSION_RESET,
                Commands.SELECT_PROFESSION_ADDPOINT
        );
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length == 0){
            if(!(sender instanceof Player player)){
                sender.sendMessage(Config.getMessage("only-for-players"));
                return true;
            }

            plugin.getEngine().openGui(player);
            return true;
        }

        switch (args[0]){
            case Commands.SELECT_PROFESSION_RESET -> {
                if(!sender.hasPermission(Permissions.RESET)){
                    sender.sendMessage(Config.getMessage("not-enough-perms"));
                    return true;
                }

                if(args.length != 2){
                    String mesage = Config.getMessage("reset-bad-usage")
                            .replace("%first%", Commands.SELECT_PROFESSION)
                            .replace("%second%", Commands.SELECT_PROFESSION_RESET);

                    sender.sendMessage(mesage);
                    return true;
                }

                String playerName = args[1];
                Player commandPlayer = Bukkit.getPlayer(playerName);

                if(commandPlayer == null){
                    sender.sendMessage(Config.getMessage("bad-command-player"));
                    return true;
                }

                plugin.getEngine().resetProfessions(sender, commandPlayer);
            }
            case Commands.SELECT_PROFESSION_ADDPOINT -> {
                if(!sender.hasPermission(Permissions.ADD_POINT)){
                    sender.sendMessage(Config.getMessage("not-enough-perms"));
                    return true;
                }

                if (args.length != 3){
                    String message = Config.getMessage("add-point-bad-usage")
                            .replace("%first%", Commands.SELECT_PROFESSION)
                            .replace("%second%", Commands.SELECT_PROFESSION_ADDPOINT);
                    sender.sendMessage(message);
                    return true;
                }

                String playerName = args[1];
                Player commandPlayer = Bukkit.getPlayer(playerName);
                if(commandPlayer == null){
                    sender.sendMessage(Config.getMessage("bad-command-player"));
                    return true;
                }

                try {
                    String amountString = args[2];
                    int amount = Integer.parseInt(amountString);


                    int oldCurrentAmount = commandPlayer.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_POINTS_AMOUNT, PersistentDataType.INTEGER);
                    int oldMaxAmount = commandPlayer.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_POINTS_MAX, PersistentDataType.INTEGER);

                    int newCurrentAmount = oldCurrentAmount + amount;
                    commandPlayer.getPersistentDataContainer().set(plugin.getKeys().PROFESSION_POINTS_AMOUNT, PersistentDataType.INTEGER, newCurrentAmount);

                    int newMaxAmount = oldMaxAmount + amount;
                    commandPlayer.getPersistentDataContainer().set(plugin.getKeys().PROFESSION_POINTS_MAX, PersistentDataType.INTEGER, newMaxAmount);

                    String message = Config.getMessage("points-maxed");
                    commandPlayer.sendMessage(message);

                    String messageSender = Config.getMessage("points-maxed-to")
                            .replace("%player%", commandPlayer.getName())
                            .replace("%amount%", commandPlayer.getName());
                    sender.sendMessage(messageSender);
                }
                catch (Exception ignored){
                    String message = Config.getMessage("add-point-bad-usage")
                            .replace("%first%", Commands.SELECT_PROFESSION)
                            .replace("%second%", Commands.SELECT_PROFESSION_ADDPOINT);
                    sender.sendMessage(message);
                }
            }
        }



        return true;
    }
}
