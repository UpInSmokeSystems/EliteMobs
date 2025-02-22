package com.magmaguy.elitemobs.menus;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.economy.EconomyHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SharedShopElements {

    public static boolean inventoryNullPointerPreventer(InventoryClickEvent event) {
        return sellMenuNullPointPreventer(event);
    }

    public static boolean sellMenuNullPointPreventer(InventoryClickEvent event) {

        //Check if current item is valid
        if (event.getCurrentItem() == null) return false;
        if (event.getCurrentItem().getType().equals(Material.AIR)) return false;
        return event.getCurrentItem().getItemMeta() != null;

    }

    public static void buyMessage(Player player, String itemDisplayName, double itemValue) {

        new BukkitRunnable() {

            @Override
            public void run() {

                player.sendMessage(
                        ChatColorConverter.convert(
                                EconomySettingsConfig.getShopBuyMessage()
                                        .replace("$item_name", itemDisplayName)
                                        .replace("$item_value", itemValue + "")
                                        .replace("$currency_name", EconomySettingsConfig.getCurrencyName())));

                player.sendMessage(
                        ChatColorConverter.convert(
                                EconomySettingsConfig.getShopCurrentBalance()
                                        .replace("$currency_amount", EconomyHandler.checkCurrency(player.getUniqueId()) + "")
                                        .replace("$currency_name", EconomySettingsConfig.getCurrencyName())));


            }


        }.runTaskLater(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS), 2);

    }

    public static void insufficientFundsMessage(Player player, double itemValue) {

        new BukkitRunnable() {

            @Override
            public void run() {

                player.sendMessage(
                        ChatColorConverter.convert(
                                EconomySettingsConfig.getShopInsufficientFundsMessage()
                                        .replace("$currency_name", EconomySettingsConfig.getCurrencyName())));

                player.sendMessage(
                        ChatColorConverter.convert(
                                EconomySettingsConfig.getShopCurrentBalance()
                                        .replace("$currency_amount", EconomyHandler.checkCurrency(player.getUniqueId()) + "")
                                        .replace("$currency_name", EconomySettingsConfig.getCurrencyName())));

                player.sendMessage(
                        ChatColorConverter.convert(
                                EconomySettingsConfig.getShopItemPrice()
                                        .replace("$item_value", itemValue + "")
                                        .replace("$currency_name", EconomySettingsConfig.getCurrencyName())));

            }


        }.runTaskLater(Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS), 2);

        player.closeInventory();

    }


}
