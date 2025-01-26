package de.bukkitnews.trading.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TradeItems {

    public static final @NotNull ItemStack ITEM_ITEM_UPPERLAYER = new ItemUtil(Material.GRAY_STAINED_GLASS_PANE).build();
    public static final @NotNull ItemStack ITEM_ITEM_FRAME = new ItemUtil(Material.ITEM_FRAME).build();
    public static final @NotNull ItemStack ITEM_HANDLING_PROCESSING = new ItemUtil(Material.CRAFTING_TABLE).setDisplayname("§7• §eProcessing").build();
    public static final @NotNull ItemStack ITEM_HANDLING_SURE = new ItemUtil(Material.CRAFTING_TABLE).setDisplayname("§7• §eSure").build();
    public static final @NotNull ItemStack ITEM_STATUS_UNFINISHED = new ItemUtil(Material.RED_STAINED_GLASS_PANE).setDisplayname("§7• §cUnfinished").build();
    public static final @NotNull ItemStack ITEM_STATUS_PROCESSING = new ItemUtil(Material.ORANGE_STAINED_GLASS_PANE).setDisplayname("§7• §eProcessing").build();
    public static final @NotNull ItemStack ITEM_STATUS_DONE = new ItemUtil(Material.GREEN_STAINED_GLASS_PANE).setDisplayname("§7• §aDone").build();

    public static final @NotNull ItemStack ITEM_ITEM_FRAME_COINS = new ItemUtil(Material.SUNFLOWER).setDisplayname("§7Coins").build();
    public static final @NotNull ItemStack ITEM_ITEM_FRAME_COINS_DESCRIPTION = new ItemUtil(Material.SUNFLOWER).setDisplayname("§7Coins: §e§n{coins}").build();
}