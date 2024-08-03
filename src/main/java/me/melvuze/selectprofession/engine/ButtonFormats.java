package me.melvuze.selectprofession.engine;

import me.melvuze.selectprofession.SelectProfession;
import me.melvuze.selectprofession.core.Config;
import me.melvuze.selectprofession.models.ProfessionModel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ButtonFormats {
    private final Engine engine;
    private final SelectProfession plugin;

    private final static String DESIGN_SECTION = "slots";

    public ButtonFormats(Engine engine, SelectProfession plugin){
        this.engine = engine;
        this.plugin = plugin;
    }

    public void formatDesign(Inventory inv){
        ConfigurationSection section = Config.getDesingSection(DESIGN_SECTION);

        if(section == null){
            plugin.getLogger().warning(Config.getMessage("bad-design-section"));
            return;
        }

        for(String key: section.getKeys(false)){
            int slot = Integer.parseInt(key);

            engine.getBannedSlots().add(slot);

            String materialName = Config.getDesignConfig().getString(DESIGN_SECTION + "." + key);
            Material material;

            try {
                material = Material.valueOf(materialName);
                inv.setItem(slot, new ItemStack(material));
            }
            catch (Exception exception){
                String message = Config.getMessage("bad-design-material")
                        .replace("%exp%", exception.getMessage());
                plugin.getLogger().warning(message);
            }
        }
    }

    public void formatButtons(Player player, Inventory inv){
        formatStatus(player, inv);

        for(ProfessionModel profession: engine.getProfessions()){
            ItemStack item = new ItemStack(Material.valueOf(profession.getMaterial()));
            ItemMeta meta = item.getItemMeta();

            meta.displayName(Component.text(profession.getName()));
            meta.getPersistentDataContainer().set(profession.getKey(), PersistentDataType.INTEGER, 1);
            meta.setLore(profession.getLore());

            item.setItemMeta(meta);
            inv.setItem(profession.getSlot(), item);
            engine.getBannedSlots().add(profession.getSlot());
        }
    }

    public void formatStatus(Player player, Inventory inv){
        int statusSlot = Config.getDesignConfig().getInt("status.slot");
        String statusMaterial= Config.getDesignConfig().getString("status.material");
        String statusName = Config.getDesignConfig().getString("status.name");

        try {
            Material mat = Material.valueOf(statusMaterial);

            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            String firstProfession = player.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_FIRST_NAME, PersistentDataType.STRING);
            String secondProfession = player.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_SECOND_NAME, PersistentDataType.STRING);

            if(firstProfession == null)
                firstProfession = Config.getMessage("profession-not-selected");

            if(secondProfession == null)
                secondProfession = Config.getMessage("profession-not-selected");

            String finalFirstProfession = firstProfession;
            String finalSecondProfession = secondProfession;
            List<TextComponent> newLove =  Config.getDesignConfig().getStringList("status.lore")
                    .stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&',
                            line.replace("%first%", finalFirstProfession).replace("%second%", finalSecondProfession)
                    ))
//                .map(line -> line.replace("%first%", finalFirstProfession))
//                .map(line -> line.replace("%second%", finalSecondProfession))
//                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .map(Component::text)
                    .toList();

            meta.displayName(Component.text(statusName));
            meta.lore(newLove);

            item.setItemMeta(meta);
            inv.setItem(statusSlot, item);
        }
        catch (Exception exception){
            String message = Config.getMessage("error-wile-loading-status")
                    .replace("%exp%", exception.getMessage());
            plugin.getLogger().warning(message);
        }
    }
}
