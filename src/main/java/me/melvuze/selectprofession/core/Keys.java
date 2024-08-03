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

    public Keys(SelectProfession plugin){
        this.plugin = plugin;

        PROFESSION_COUNT = new NamespacedKey(plugin, "PROFESSION_COUNT");
        PROFESSIONS_LIST = new NamespacedKey(plugin, "PROFESSIONS_LIST");

        PROFESSION_FIRST = new NamespacedKey(plugin, "PROFESSION_FIRST");
        PROFESSION_SECOND = new NamespacedKey(plugin, "PROFESSION_SECOND");

        PROFESSION_FIRST_NAME = new NamespacedKey(plugin, "PROFESSION_FIRST_NAME");
        PROFESSION_SECOND_NAME = new NamespacedKey(plugin, "PROFESSION_SECOND_NAME");
    }
}
