package me.melvuze.selectprofession.listeners;

import me.melvuze.selectprofession.SelectProfession;
import me.melvuze.selectprofession.inventories.SelectProfessionInventory;
import me.melvuze.selectprofession.models.ProfessionModel;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import javax.swing.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class InventoryClickedListener implements Listener {
    private SelectProfession plugin;
    public InventoryClickedListener(SelectProfession plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof SelectProfessionInventory) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onSelectProfessionInvClicked(InventoryClickEvent event){
        if(!(event.getInventory().getHolder() instanceof SelectProfessionInventory))
            return;

        if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
            event.setCancelled(true);

        if(event.getAction() == InventoryAction.PICKUP_ALL)
            event.setCancelled(true);

        if(event.getAction() == InventoryAction.PLACE_ALL)
            event.setCancelled(true);

        if(event.getAction() == InventoryAction.PICKUP_HALF)
            event.setCancelled(true);

        if(!(event.getWhoClicked() instanceof Player player))
            return;

        if(event.getCurrentItem() == null)
            return;

        //только клики в меню
        if(event.getSlot() != event.getRawSlot())
            return;

        if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getClick() == ClickType.SHIFT_LEFT)
            event.setCancelled(true);

        //блокировка действий при нажатии на кнопки интерфнйса
        if(plugin.getEngine().getBannedSlots().contains(event.getSlot()))
            event.setCancelled(true);

        ItemStack button = event.getCurrentItem();
        Set<NamespacedKey> keys = button.getItemMeta().getPersistentDataContainer().getKeys();

        String possibleKey = null;

        l1: for(String keyStr: plugin.getEngine().getProfessionsNameSpaces()){
            //System.out.println("my: " + keyStr);

            l2: for(NamespacedKey key: keys){
                //System.out.println("item: " + key.asString());
                if(key.asString().equals(keyStr)){
                    possibleKey = keyStr;
                     break l1;
                }
            }
        }

        if(possibleKey != null){
            if(event.getClick().isLeftClick())
                plugin.getEngine().onSomeProfessionClicked(player, event.getSlot(), event.getInventory(), possibleKey, "1");
            if(event.getClick().isRightClick())
                plugin.getEngine().onSomeProfessionClicked(player, event.getSlot(), event.getInventory(), possibleKey, "2");
        }
    }
}
