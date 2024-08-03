package me.melvuze.selectprofession.engine;

import me.melvuze.selectprofession.SelectProfession;
import me.melvuze.selectprofession.core.Config;
import me.melvuze.selectprofession.models.ProfessionModel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
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
        for(ProfessionModel profession: engine.getProfessions()){
            ItemStack item = new ItemStack(Material.valueOf(profession.getMaterial()));
            ItemMeta meta = item.getItemMeta();

            meta.displayName(Component.text(profession.getName()));
            //meta.getPersistentDataContainer().set(profession.getKey(), PersistentDataType.INTEGER, 1);
            meta.getPersistentDataContainer().set(plugin.getKeys().PROFESSION_ITEM_KEY, PersistentDataType.STRING, profession.getConfigId());
            meta.setLore(profession.getLore());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            item.setItemMeta(meta);

            if(player.hasPermission(profession.getPermission()))
                setGlowing(item);

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

            meta.displayName(Component.text(statusName));

            List<TextComponent> lore = new ArrayList<>();
            for (String defaultLoreLine: Config.getDesignConfig().getStringList("status.lore")){
                lore.add(Component.text(defaultLoreLine));
            }

            for(ProfessionModel model: engine.getPlayerProfessions(player)){
                String profLoreLine = Config.getDesignConfig().getString("status.lore-profession")
                        .replace("%prof_name%", model.getName());
                lore.add(Component.text(profLoreLine));
            }

            meta.lore(lore);

            item.setItemMeta(meta);
            inv.setItem(statusSlot, item);
        }
        catch (Exception exception){
            String message = Config.getMessage("error-wile-loading-status")
                    .replace("%msg%", exception.getMessage());
            plugin.getLogger().warning(message);
        }
    }

    public void formatPoints(Player player, Inventory inv){
        int slot = Config.getDesignConfig().getInt("points.slot");
        String material = Config.getDesignConfig().getString("points.material");
        String name = Config.getDesignConfig().getString("points.name");

        try {
            ItemStack item = new ItemStack(Material.valueOf(material));
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(name);

            int avaliablePoints  = player.getPersistentDataContainer().get(plugin.getKeys().PROFESSION_POINTS_AMOUNT, PersistentDataType.INTEGER);

            List<TextComponent> newLove =  Config.getDesignConfig().getStringList("points.lore")
                .stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&',
                        line.replace("%amount%", String.valueOf(avaliablePoints))
                ))
                .map(Component::text)
                .toList();

            meta.lore(newLove);
            item.setItemMeta(meta);
            inv.setItem(slot, item);
        }
        catch (Exception exp){
            String message = Config.getMessage("error-points-formatting")
                    .replace("%msg%", exp.getMessage());
            plugin.getLogger().warning(message);
        }
    }

    private void setGlowing(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }
}
