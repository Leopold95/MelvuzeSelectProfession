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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class Engine {
    private SelectProfession plugin;

    @Getter
    private List<ProfessionModel> professions;
    @Getter
    private List<Integer> bannedSlots;
    @Getter
    private List<String> professionsNameSpaces;
    @Getter
    private List<List<ProfessionModel>> groups;

    private final ButtonFormats buttonFormats;

    private final static String PROFESSION_SECTION = "professions";

    public Engine(SelectProfession plugin){
        this.plugin = plugin;

        bannedSlots = new ArrayList<>();
        professionsNameSpaces = new ArrayList<>();

        loadProfessions();
        loadProfessionGroups();

        buttonFormats = new ButtonFormats(this, this.plugin);
    }

    public void openGui(Player player){
        Inventory inv = new SelectProfessionInventory().getInventory();

        buttonFormats.formatButtons(player, inv);
        buttonFormats.formatPoints(player, inv);
        buttonFormats.formatDesign(inv);

        player.openInventory(inv);
    }

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

        boolean isEnounghPoints = true;
        if(!isEnounghPoints){
            player.sendMessage(Config.getMessage("not-enough-points"));
            return;
        }

        player.sendMessage(clickedProfession.getConfigId());

    }

    /**
     * Клик на проффесиию в меню
     * @param player кликнувший игрок
     * @param slot слот
     * @param inv инвентарь
     * @param key ключ
     * @param type тип
     */
    @Deprecated
    public void onSomeProfessionClicked(Player player, int slot, Inventory inv, String key, String type){
        Optional<ProfessionModel> professionOpt = plugin.getEngine().getProfessions().stream()
                .filter(p -> p.getKey().asString().equals(key))
                .findFirst();

        if (professionOpt.isEmpty())
            return;

        ProfessionModel clickedProfession = professionOpt.get();

        if(!player.hasPermission(clickedProfession.getPermission())){
            player.sendMessage(Config.getMessage("profession-no-prerms"));
            return;
        }

        if(type.equals("1")){
            String current = player.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_FIRST, PersistentDataType.STRING);
            if(current != null && current.equals(clickedProfession.getKey().asString())){
                player.sendMessage(Config.getMessage("already-same-profession-first"));
                return;
            }

            Optional<ProfessionModel> currentProfession = getByStringNameSpace(current);

            String second = player.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_SECOND, PersistentDataType.STRING);
            if(second != null && second.equals(clickedProfession.getKey().asString())){
                player.sendMessage(Config.getMessage("already-another-profession"));
                return;
            } else if (second != null && current != null) {
                if(currentProfession.isPresent() && currentProfession.get().getBanned().contains(clickedProfession.getKey().asString())){
                    String currentName = player.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_FIRST_NAME, PersistentDataType.STRING);

                    player.sendMessage(Config.getMessage("cant-take-banned")
                            .replace("%prof_1%", currentName)
                            .replace("%prof_2%", clickedProfession.getName())
                    );
                    return;
                }
            }

            currentProfession.ifPresent(professionModel -> plugin.getApi().getUserManager().modifyUser(player.getUniqueId(), user -> {
                user.data().remove(Node.builder(professionModel.getPermission()).build());
                user.data().add(Node.builder(clickedProfession.getPermission()).build());
                plugin.getApi().getUserManager().saveUser(user);
            }));

            player.getPersistentDataContainer().set(plugin.getKeys().PROFESSION_FIRST, PersistentDataType.STRING, key);
            player.getPersistentDataContainer().set(plugin.getKeys().PROFESSION_FIRST_NAME, PersistentDataType.STRING, clickedProfession.getName());
            player.sendMessage(Component.text(Config.getMessage("get-first-profession").replace("%name%", clickedProfession.getName())));
            Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), clickedProfession.getCommand().replace("%player%", player.getName()));
            buttonFormats.formatStatus(player, inv);
        }
        else {
            String current = player.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_SECOND, PersistentDataType.STRING);
            if(current != null && current.equals(clickedProfession.getKey().asString())){
                player.sendMessage(Config.getMessage("already-same-profession-second"));
                return;
            }

            Optional<ProfessionModel> currentProfession = getByStringNameSpace(current);

            String first = player.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_FIRST, PersistentDataType.STRING);
            if(first != null && first.equals(clickedProfession.getKey().asString())){
                player.sendMessage(Config.getMessage("already-another-profession"));
                return;
            } else if (first != null && current != null) {
                if(currentProfession.isPresent() && currentProfession.get().getBanned().contains(clickedProfession.getKey().asString())){
                    String currentName = player.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_SECOND_NAME, PersistentDataType.STRING);

                    player.sendMessage(Config.getMessage("cant-take-banned")
                            .replace("%prof_1%", currentName)
                            .replace("%prof_2%", clickedProfession.getName())
                    );
                    return;
                }
            }

            currentProfession.ifPresent(professionModel -> plugin.getApi().getUserManager().modifyUser(player.getUniqueId(), user -> {
                user.data().remove(Node.builder(professionModel.getPermission()).build());
                user.data().add(Node.builder(clickedProfession.getPermission()).build());
                plugin.getApi().getUserManager().saveUser(user);
            }));

            player.getPersistentDataContainer().set(plugin.getKeys().PROFESSION_SECOND, PersistentDataType.STRING, key);
            player.getPersistentDataContainer().set(plugin.getKeys().PROFESSION_SECOND_NAME, PersistentDataType.STRING, clickedProfession.getName());
            player.sendMessage(Component.text(Config.getMessage("get-second-profession").replace("%name%", clickedProfession.getName())));
            Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), clickedProfession.getCommand().replace("%player%", player.getName()));
            buttonFormats.formatStatus(player, inv);
        }
    }

    /**
     * Найти проффесию по строковому ключу
     * @param key ключ
     * @return профессия
     */
    public Optional<ProfessionModel> getByStringNameSpace(String key){
        return professions.stream().filter(p -> p.getKey().asString().equals(key)).findFirst();
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
                NamespacedKey nsKey = new NamespacedKey(plugin,  "PROFESSION-" + key);

                ArrayList<String> banned = new ArrayList<>();
                List<String> bannedListConfig = Config.getStringList(PROFESSION_SECTION + "." + key + ".banned");

                if(bannedListConfig != null && !bannedListConfig.isEmpty()){
                    for(String bannedKey: bannedListConfig){
                        banned.add(new NamespacedKey(plugin, "PROFESSION-" + bannedKey).asString());
                    }
                }

                professionsNameSpaces.add(nsKey.asString());

                amount++;
                professions.add(new ProfessionModel(
                        name,
                        Config.getInt(PROFESSION_SECTION + "." + key + ".cost"),
                        Config.getInt(PROFESSION_SECTION + "." + key + ".slot"),
                        Config.getString(PROFESSION_SECTION + "." + key + ".command"),
                        Config.getString(PROFESSION_SECTION + "." + key + ".permission"),
                        Config.getString(PROFESSION_SECTION + "." + key + ".material"),
                        nsKey,
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
     * @param profession профессия
     * @return ДА \ НЕТ
     */
    private boolean isPreviousProfessionUnlocked(Player player, ProfessionModel profession){
//        List<ProfessionModel> firstProfessions = new ArrayList<>();
//
//        for(List<ProfessionModel> group: groups){
//            Optional<ProfessionModel> model = group.stream().findFirst();
//            model.ifPresent(firstProfessions::add);
//        }
//
//        return firstProfessions.stream().anyMatch(pm -> player.hasPermission(pm.getPermission()));

        return true;
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
    private List<ProfessionModel> getPlayerProfessions(Player player){
        List<ProfessionModel> playerProfessions = new ArrayList<>();

        for(ProfessionModel model: professions){
            if(player.hasPermission(model.getPermission()))
                playerProfessions.add(model);
        }

        return playerProfessions;
    }
}
