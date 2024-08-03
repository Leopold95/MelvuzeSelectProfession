package me.melvuze.selectprofession.inventories;

import me.melvuze.selectprofession.core.Config;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class SelectProfessionInventory implements InventoryHolder {
    private final Inventory inventory;

    public SelectProfessionInventory() {
        inventory = Bukkit.createInventory(this, 54, Component.text(Config.getDesignConfig().getString("menu-name")));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}