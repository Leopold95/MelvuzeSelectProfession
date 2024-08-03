package me.melvuze.selectprofession.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.NamespacedKey;

import java.util.List;

@AllArgsConstructor
@Getter
public class ProfessionModel {
    private String name;
    private int slot;
    private String command;
    private String permission;
    private String material;
    private NamespacedKey key;
    private List<String> lore;
    private List<String> banned;
}
