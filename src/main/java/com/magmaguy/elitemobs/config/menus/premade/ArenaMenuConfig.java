package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.VersionChecker;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ArenaMenuConfig extends MenusConfigFields {
    @Getter
    private static String menuName;
    @Getter
    private static ItemStack playerItem;
    @Getter
    private static int playerItemSlot;
    @Getter
    private static ItemStack spectatorItem;
    @Getter
    private static int spectatorItemSlot;

    public ArenaMenuConfig() {
        super("arena_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        menuName = ConfigurationEngine.setString(file, fileConfiguration, "menuName", "", true);
        playerItem = ConfigurationEngine.setItemStack(fileConfiguration, "playerItem",
                ItemStackGenerator.generateItemStack(Material.DIAMOND_SWORD, "&4Challenge the arena!", List.of("&2Fight in the arena!")));
        playerItemSlot = ConfigurationEngine.setInt(fileConfiguration, "playerItemSlot", 6);
        if (!VersionChecker.serverVersionOlderThan(17, 0))
            spectatorItem = ConfigurationEngine.setItemStack(fileConfiguration, "spectatorItem",
                    ItemStackGenerator.generateItemStack(Material.SPYGLASS, "&aSpectate!", List.of("&2Spectate players in the arena!"), MetadataHandler.signatureID));
        else
            spectatorItem = ConfigurationEngine.setItemStack(fileConfiguration, "spectatorItem",
                    ItemStackGenerator.generateItemStack(Material.NOTE_BLOCK, "&aSpectate!", List.of("&2Spectate players in the arena!"), MetadataHandler.signatureID));
        spectatorItemSlot = ConfigurationEngine.setInt(fileConfiguration, "spectatorItemSlot", 2);
    }

}
