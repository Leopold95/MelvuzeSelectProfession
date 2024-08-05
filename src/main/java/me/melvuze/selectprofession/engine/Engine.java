package me.melvuze.selectprofession.engine;

import lombok.Getter;
import me.melvuze.selectprofession.SelectProfession;
import me.melvuze.selectprofession.core.Config;
import me.melvuze.selectprofession.inventories.SelectProfessionInventory;
import me.melvuze.selectprofession.models.ProfessionModel;
import net.kyori.adventure.text.Component;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

public class Engine {
    private SelectProfession plugin;

    @Getter
    private List<ProfessionModel> professions;
    @Getter
    private List<Integer> bannedSlots;
    //@Getter
    //private List<String> professionsNameSpaces;
    @Getter
    private List<List<ProfessionModel>> groups;

    private final ButtonFormats buttonFormats;

    private final static String PROFESSION_SECTION = "professions";

    public Engine(SelectProfession plugin){
        this.plugin = plugin;

        bannedSlots = new ArrayList<>();
        //professionsNameSpaces = new ArrayList<>();

        loadProfessions();
        loadProfessionGroups();

        buttonFormats = new ButtonFormats(this, this.plugin);
    }

    public void openGui(Player player){
        Inventory inv = new SelectProfessionInventory().getInventory();

        buttonFormats.formatButtons(player, inv);
        buttonFormats.formatPoints(player, inv);
        buttonFormats.formatStatus(player, inv);
        buttonFormats.formatDesign(inv);

        player.openInventory(inv);
    }

    public void resetProfessions(CommandSender sender, Player player){
        List<ProfessionModel> playerProfessions = getPlayerProfessions(player);

        for(ProfessionModel model: playerProfessions){
            if(player.hasPermission(model.getPermission())){
                plugin.getApi().getUserManager().modifyUser(player.getUniqueId(), user -> {
                    user.data().remove(Node.builder(model.getPermission()).build());
                    plugin.getApi().getUserManager().saveUser(user);
                });
            }
        }

        player.getPersistentDataContainer().set(plugin.getKeys().PROFESSION_POINTS_AMOUNT, PersistentDataType.INTEGER, Config.getInt("default-prof-points"));
        player.getPersistentDataContainer().set(plugin.getKeys().PROFESSION_POINTS_MAX, PersistentDataType.INTEGER, Config.getInt("default-prof-points"));

        player.sendMessage(Config.getMessage("professions-resetted"));
        sender.sendMessage(Config.getMessage("professions-resetted-to").replace("%player%", player.getName()));
    }

//    public void addPoints(PersistentDataContainer pdc){
//        String amountString = args[2];
//        int amount = Integer.parseInt(amountString);
//
//        int oldCurrentAmount = commandPlayer.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_POINTS_AMOUNT, PersistentDataType.INTEGER);
//        int oldMaxAmount = commandPlayer.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_POINTS_MAX, PersistentDataType.INTEGER);
//
//        int newCurrentAmount = oldCurrentAmount + amount;
//        commandPlayer.getPersistentDataContainer().set(plugin.getKeys().PROFESSION_POINTS_AMOUNT, PersistentDataType.INTEGER, newCurrentAmount);
//
//        int newMaxAmount = oldMaxAmount + amount;
//        commandPlayer.getPersistentDataContainer().set(plugin.getKeys().PROFESSION_POINTS_MAX, PersistentDataType.INTEGER, newMaxAmount);
//    }

    /**
     * Клик на проффесию в меню
     * @param player игрок
     * @param inv инвентарь
     * @param clickedProfession проффесия, на которую нажали
     */
    public void onProfessionClicked(Player player, Inventory inv, ProfessionModel clickedProfession){
        //List<ProfessionModel> playerProfessions = getPlayerProfessions(player);
        if(getPlayerProfessions(player).contains(clickedProfession)){
            player.sendMessage(Config.getMessage("already-selected"));
            return;
        }

        boolean isPreviousProfessionSelected = isPreviousProfessionUnlocked(player, clickedProfession);
        if(!isPreviousProfessionSelected){
            player.sendMessage(Config.getMessage("bad-previous-profession"));
            return;
        }

        boolean isEnounghPoints = isPlayerHasPoints(player, clickedProfession);
        if(!isEnounghPoints){
            player.sendMessage(Config.getMessage("not-enough-points"));
            return;
        }

        int currentPoints = player.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_POINTS_AMOUNT, PersistentDataType.INTEGER);
        currentPoints -= clickedProfession.getCost();
        player.getPersistentDataContainer().set(plugin.getKeys().PROFESSION_POINTS_AMOUNT, PersistentDataType.INTEGER, currentPoints);

        plugin.getApi().getUserManager().modifyUser(player.getUniqueId(), user -> {
            user.data().add(Node.builder(clickedProfession.getPermission()).build());
            plugin.getApi().getUserManager().saveUser(user);
        });

        if(clickedProfession.getCommand() != null  && !clickedProfession.getCommand().isEmpty())
            Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), clickedProfession.getCommand().replace("%player%", player.getName()));

        player.sendMessage(Component.text(Config.getMessage("get-first-profession").replace("%name%", clickedProfession.getName())));

        openGui(player);

        //player.sendMessage(clickedProfession.getConfigId());
    }

    private boolean isPlayerHasPoints(Player player, ProfessionModel clickedProfession) {
        int avaliable = player.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_POINTS_AMOUNT, PersistentDataType.INTEGER);
        return (avaliable - clickedProfession.getCost()) >= 0;
    }

    /**
     * Найди модель по правам
     * @param permission искомые права модели
     * @return модель профессии
     */
    public ProfessionModel getByPermission(String permission){
        for(ProfessionModel model: professions){
            if(model.getPermission().equals(permission))
                return model;
        }

        return null;
    }

    /**
     * Найди модель ид из конфига
     * @param configId искомые права модели
     * @return модель профессии
     */
    public ProfessionModel getByConfigId(String configId){
        for(ProfessionModel model: professions){
            if(model.getConfigId().equals(configId))
                return model;
        }

        return null;
    }

    /**
     * Прогрузка всех профессий
     */
    private void loadProfessions(){
        professions = new ArrayList<>();

        ConfigurationSection section = Config.getSection("professions");

        if(section == null || section.getKeys(false).isEmpty()){
            plugin.getLogger().warning(Config.getString("bad-profession-load"));
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        int amount = 0;

        for(String key: section.getKeys(false)){
            try {
                Material.valueOf(Config.getString(PROFESSION_SECTION + "." + key + ".material"));

                String name = Config.getString(PROFESSION_SECTION + "." + key + ".name");
                //NamespacedKey nsKey = new NamespacedKey(plugin,  "PROFESSION-" + key);

                ArrayList<String> banned = new ArrayList<>();
                List<String> bannedListConfig = Config.getStringList(PROFESSION_SECTION + "." + key + ".banned");

                if(bannedListConfig != null && !bannedListConfig.isEmpty()){
                    for(String bannedKey: bannedListConfig){
                        banned.add(new NamespacedKey(plugin, "PROFESSION-" + bannedKey).asString());
                    }
                }

                //professionsNameSpaces.add(nsKey.asString());

                String command = Config.getString(PROFESSION_SECTION + "." + key + ".command");
                plugin.getLogger().warning(command);

                amount++;
                professions.add(new ProfessionModel(
                        name,
                        Config.getInt(PROFESSION_SECTION + "." + key + ".cost"),
                        Config.getInt(PROFESSION_SECTION + "." + key + ".slot"),
                        command,
                        Config.getString(PROFESSION_SECTION + "." + key + ".permission"),
                        Config.getString(PROFESSION_SECTION + "." + key + ".material"),
                        key,
                        Config.getStringList(PROFESSION_SECTION + "." + key + ".lore"),
                        banned
                ));
            }
            catch (Exception exp){
                String message = Config.getMessage("error-while-lading-model")
                                .replace("%name%", Config.getString(PROFESSION_SECTION + "." + key + ".name"))
                                .replace("%exp%", exp.getMessage());
                plugin.getLogger().warning(message);
            }
        }

        plugin.getLogger().log(Level.INFO, Config.getMessage("loading.profession").replace("%value%", String.valueOf(amount)));

        if(Config.getBoolean("show-professions-after-loading"))
            plugin.getLogger().log(Level.INFO, professions.toString());
    }

    /**
     * Прогрузка групп профессий
     */
    private void loadProfessionGroups(){
        groups = new ArrayList<>();

        ConfigurationSection section = Config.getSection("groups");
        if(section == null || section.getKeys(false).isEmpty()){
            plugin.getLogger().warning(Config.getString("bad-group-section"));
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        int amount = 0;

        for(String key: section.getKeys(true)){
            List<String> groupList = section.getStringList(key);

            List<ProfessionModel> professionGroup = new ArrayList<>();

            for(String professionConfigId: groupList){
                ProfessionModel model = getByConfigId(professionConfigId);
                if(model == null){
                    plugin.getLogger().log(Level.WARNING,
                            Config.getMessage("bad-profession-group-id").replace("%prof_name%", professionConfigId));
                    continue;
                }

                professionGroup.add(model);
            }

            groups.add(professionGroup);

            amount++;
        }

        plugin.getLogger().log(Level.INFO, Config.getMessage("loading.groups").replace("%value%", String.valueOf(amount)));

        if(Config.getBoolean("show-groups-after-loading"))
            plugin.getLogger().log(Level.INFO, groups.stream().map(g -> g.stream().map(ProfessionModel::getConfigId).toList()).toList().toString());
    }

    /**
     * Проверка что было открыта предыдущая профессия
     * @param player игрок
     * @param clickedProfession профессия
     * @return ДА \ НЕТ
     */
    private boolean isPreviousProfessionUnlocked(Player player, ProfessionModel clickedProfession){
        List<ProfessionModel> firstProfessions = new ArrayList<>();
//        List<ProfessionModel> playerProfessions = getPlayerProfessions(player);
//
        for(List<ProfessionModel> group: groups){
            Optional<ProfessionModel> model = group.stream().findFirst();
            model.ifPresent(firstProfessions::add);
        }

        if(firstProfessions.contains(clickedProfession))
            return true;

        List<ProfessionModel> group = getProfessionGroup(clickedProfession);
        if(group == null){
            plugin.getLogger().warning("77--11");
            return false;
        }

        int index = group.indexOf(clickedProfession);
        System.out.println(index);

        try {
            ProfessionModel nextStage = group.get(index - 1);
            if(player.hasPermission(nextStage.getPermission()))
                return true;
        }
        catch (Exception ignored){
            plugin.getLogger().warning("77--12");
        }

        //List<List<ProfessionModel>> playerOpenedGroups = getPlayerOpenGroups(player);
        //player.sendMessage(playerOpenedGroups.toString());

        return false;
    }

    private List<ProfessionModel> getProfessionGroup(ProfessionModel model){
        for(List<ProfessionModel> group: groups){
            if(group.contains(model))
                return group;
        }
        return null;
    }

    private List<List<ProfessionModel>> getPlayerOpenGroups(Player player){
        List<List<ProfessionModel>> list = new ArrayList<>();

        for(List<ProfessionModel> group: groups){
            Optional<ProfessionModel> firstProfession = group.stream().findFirst();

            if(firstProfession.isEmpty())
                continue;

            boolean isGroupOpen = player.hasPermission(firstProfession.get().getPermission());

            if(isGroupOpen)
                list.add(group);
        }

        return list;
    }

    /**
     * Получить список профессий игрока
     * @param player игрок
     * @return список спрофессий
     */
    public List<ProfessionModel> getPlayerProfessions(Player player){
        List<ProfessionModel> playerProfessions = new ArrayList<>();

        for(ProfessionModel model: professions){
            if(player.hasPermission(model.getPermission()))
                playerProfessions.add(model);
        }

        return playerProfessions;
    }
}
