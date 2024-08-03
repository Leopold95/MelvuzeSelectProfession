package me.melvuze.selectprofession.core;

import me.melvuze.selectprofession.SelectProfession;
import org.bukkit.NamespacedKey;

public class Keys {
    private SelectProfession plugin;

    public NamespacedKey PROFESSION_COUNT;
    public NamespacedKey PROFESSIONS_LIST;


    public NamespacedKey PROFESSION_FIRST;
    public NamespacedKey PROFESSION_FIRST_NAME;
    public NamespacedKey PROFESSION_SECOND;
    public NamespacedKey PROFESSION_SECOND_NAME;

    public NamespacedKey PROFESSION_POINTS_AMOUNT;
    public NamespacedKey PROFESSION_POINTS_MAX;
    public NamespacedKey PROFESSION_LIST;
    public NamespacedKey PROFESSION_ITEM_KEY;

    public Keys(SelectProfession plugin){
        this.plugin = plugin;

        PROFESSION_COUNT = new NamespacedKey(plugin, "PROFESSION_COUNT");
        PROFESSIONS_LIST = new NamespacedKey(plugin, "PROFESSIONS_LIST");

        PROFESSION_FIRST = new NamespacedKey(plugin, "PROFESSION_FIRST");
        PROFESSION_SECOND = new NamespacedKey(plugin, "PROFESSION_SECOND");

        PROFESSION_FIRST_NAME = new NamespacedKey(plugin, "PROFESSION_FIRST_NAME");
        PROFESSION_SECOND_NAME = new NamespacedKey(plugin, "PROFESSION_SECOND_NAME");

        PROFESSION_POINTS_AMOUNT = new NamespacedKey(plugin, "PROFESSION_POINTS_AMOUNT");
        PROFESSION_POINTS_MAX = new NamespacedKey(plugin, "PROFESSION_POINTS_MAX");
        PROFESSION_LIST = new NamespacedKey(plugin, "PROFESSION_LIST");
        PROFESSION_ITEM_KEY = new NamespacedKey(plugin, "PROFESSION_ITEM_KEY");
    }
}
