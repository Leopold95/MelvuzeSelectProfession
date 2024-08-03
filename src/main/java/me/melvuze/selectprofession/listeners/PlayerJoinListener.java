package me.melvuze.selectprofession.listeners;

import me.melvuze.selectprofession.SelectProfession;
import me.melvuze.selectprofession.core.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PlayerJoinListener implements Listener {
    private SelectProfession plugin;

    public PlayerJoinListener(SelectProfession plugin){
        this.plugin = plugin;
    }


    @EventHandler
    private void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        if(!pdc.has(plugin.getKeys().PROFESSION_COUNT)){
            pdc.set(plugin.getKeys().PROFESSION_COUNT, PersistentDataType.INTEGER, 0);
        }

        if(!pdc.has(plugin.getKeys().PROFESSION_POINTS_AMOUNT))
            pdc.set(plugin.getKeys().PROFESSION_POINTS_AMOUNT, PersistentDataType.INTEGER, Config.getInt("default-prof-points"));

        if(!pdc.has(plugin.getKeys().PROFESSION_POINTS_MAX))
            pdc.set(plugin.getKeys().PROFESSION_POINTS_MAX, PersistentDataType.INTEGER, Config.getInt("default-prof-points"));
    }
}
