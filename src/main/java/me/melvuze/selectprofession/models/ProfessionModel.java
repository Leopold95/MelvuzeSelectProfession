package me.melvuze.selectprofession.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.NamespacedKey;

import java.util.List;

@AllArgsConstructor
@Getter
@ToString
public class ProfessionModel {
    private String name;
    private int cost;
    private int slot;
    private String command;
    private String removeCommand;
    private String permission;
    private String material;
    //private NamespacedKey key;
    private String configId;
    private List<String> lore;
    private List<String> banned;
}
