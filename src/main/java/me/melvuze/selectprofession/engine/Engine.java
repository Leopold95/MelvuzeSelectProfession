package me.melvuze.selectprofession.engine;

import lombok.Getter;
import me.melvuze.selectprofession.SelectProfession;
import me.melvuze.selectprofession.core.Config;
import me.melvuze.selectprofession.inventories.SelectProfessionInventory;
import me.melvuze.selectprofession.models.ProfessionModel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Engine {
    private SelectProfession plugin;

    @Getter
    private ArrayList<ProfessionModel> professions;
    @Getter
    private ArrayList<Integer> bannedSlots;
    @Getter
    private ArrayList<String> professionsNameSpaces;

    private final ButtonFormats buttonFormats;

    private final static String PROFESSION_SECTION = "professions";

    public Engine(SelectProfession plugin){
        this.plugin = plugin;

        bannedSlots = new ArrayList<>();
        professionsNameSpaces = new ArrayList<>();

        tryLoadProfessions();

        buttonFormats = new ButtonFormats(this, this.plugin);
    }

    public void openGui(Player player){
        Inventory inv = new SelectProfessionInventory().getInventory();

        buttonFormats.formatButtons(player, inv);
        buttonFormats.formatDesign(inv);

        player.openInventory(inv);
    }

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

    private Optional<ProfessionModel> getByStringNameSpace(String key){
        return professions.stream().filter(p -> p.getKey().asString().equals(key)).findFirst();
    }

    /**
     * Найди модель по правам
     * @param permission искомые права модели
     * @return модель профессии
     */
    private ProfessionModel getByPermission(String permission){
        for(ProfessionModel model: professions){
            if(model.getPermission().equals(permission))
                return model;
        }

        return null;
    }

    /**
     * Прогрущка всех проффесий
     */
    private void tryLoadProfessions(){
        professions = new ArrayList<>();

        ConfigurationSection section = Config.getSection("professions");

        if(section == null || section.getKeys(false).isEmpty()){
            plugin.getLogger().warning(Config.getString("bad-profession-load"));
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

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

                professions.add(new ProfessionModel(
                        name,
                        Config.getInt(PROFESSION_SECTION + "." + key + ".slot"),
                        Config.getString(PROFESSION_SECTION + "." + key + ".command"),
                        Config.getString(PROFESSION_SECTION + "." + key + ".permission"),
                        Config.getString(PROFESSION_SECTION + "." + key + ".material"),
                        nsKey,
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
    }
}
