package de.bukkitnews.trading.trade.model;

import de.bukkitnews.trading.util.MessageUtil;
import de.bukkitnews.trading.util.TradeItems;
import de.bukkitnews.trading.util.ItemUtil;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static org.bukkit.Bukkit.getServer;

/**
 * This class represents a trade between two players, allowing them to exchange items and coins.
 * It handles the various actions that can be performed during the trade, such as adding/removing items,
 * setting coins, and managing the state of the trade.
 */
public record Trade(@NotNull TradePlayer host,
                    @NotNull TradePlayer target) implements TradeActions {

    public Trade(@NotNull TradePlayer host, @NotNull TradePlayer target) {
        this.host = host;
        this.target = target;
        Arrays.asList(host, target).forEach(this::createInventory);
    }

    /**
     * Retrieves the TradePlayer corresponding to the provided player.
     *
     * @param player The player whose corresponding TradePlayer is to be retrieved.
     * @return An Optional containing the TradePlayer or empty if the player is null.
     */
    public @NotNull Optional<TradePlayer> getPlayer(@NotNull Player player) {
        return Optional.ofNullable(player)
                .map(p -> p.equals(host.getPlayer()) ? host : target);
    }

    /**
     * Retrieves the opposing TradePlayer based on the given TradePlayer.
     *
     * @param tradePlayer The TradePlayer whose opposing player is to be retrieved.
     * @return The opposing TradePlayer.
     * @throws IllegalArgumentException If the provided TradePlayer is not part of the trade.
     */
    public @Nullable TradePlayer getTarget(@Nullable TradePlayer tradePlayer) {
        return Optional.ofNullable(tradePlayer)
                .map(p -> p.equals(host) ? target : host)
                .orElseThrow(() -> new IllegalArgumentException("TradePlayer not part of the trade"));
    }

    /**
     * Adds an item to the trade for the specified player at the given slot.
     * It updates the state of both players and adjusts their inventories accordingly.
     *
     * @param tradePlayer The player adding the item.
     * @param slot        The slot where the item is being placed.
     * @param itemStack   The item being added to the trade.
     * @return true if the item was successfully added, false otherwise.
     */
    @Override
    public boolean addItem(@NotNull TradePlayer tradePlayer, int slot, @NotNull ItemStack itemStack) {
        return getValidSlots().stream()
                .filter(i -> tradePlayer.getPlayer().getOpenInventory().getItem(i) == null)
                .findFirst()
                .map(i -> {
                    TradePlayer target = getTarget(tradePlayer);
                    updateState(tradePlayer, State.UNFINISHED);
                    updateState(target, State.UNFINISHED);
                    tradePlayer.getPlayer().getOpenInventory().setItem(i, itemStack);
                    tradePlayer.getPlayer().getInventory().setItem(slot, new ItemStack(Material.AIR));
                    tradePlayer.getItems().add(itemStack);
                    target.getPlayer().getOpenInventory().setItem(getTargetSlots(i), itemStack);
                    return true;
                }).orElse(false);
    }

    /**
     * Removes an item from the trade for the specified player at the given slot.
     * It updates the state of both players and adjusts their inventories accordingly.
     *
     * @param tradePlayer The player removing the item.
     * @param slot        The slot where the item is being removed from.
     */
    @Override
    public void removeItem(@NotNull TradePlayer tradePlayer, int slot) {
        TradePlayer target = getTarget(tradePlayer);
        updateState(tradePlayer, State.UNFINISHED);
        updateState(target, State.UNFINISHED);

        ItemStack itemStack = tradePlayer.getPlayer().getOpenInventory().getItem(slot);

        if (itemStack != null && !itemStack.getType().isAir()) {
            tradePlayer.getPlayer().getInventory().addItem(itemStack);

            tradePlayer.getPlayer().getOpenInventory().setItem(slot, new ItemStack(Material.AIR));
            tradePlayer.getItems().remove(itemStack);

            target.getPlayer().getOpenInventory().setItem(getTargetSlots(slot), new ItemStack(Material.AIR));
        }
    }

    /**
     * Sets the coin amount for the specified player and updates their inventory accordingly.
     *
     * @param tradePlayer The player whose coins are to be set.
     * @param coins       The number of coins to set.
     */
    @Override
    public void setCoins(@NotNull TradePlayer tradePlayer, int coins) {
        tradePlayer.setCoins(coins);
        updateCoinsItem(tradePlayer);
    }

    /**
     * Sets the value for the specified player and updates their inventory accordingly.
     *
     * @param tradePlayer The player whose value is to be set.
     * @param value       The value to set.
     */
    @Override
    public void setValue(@NotNull TradePlayer tradePlayer, int value) {
        tradePlayer.setValue(value);
        updateCoinsItem(tradePlayer);
    }

    /**
     * Updates the state of the specified player in the trade.
     * The state can be UNFINISHED, PROCESSING, or DONE, and this method adjusts the inventory accordingly.
     *
     * @param tradePlayer The player whose state is to be updated.
     * @param state       The new state to set for the player.
     */
    @Override
    public void updateState(@NotNull TradePlayer tradePlayer, @NotNull State state) {
        if (tradePlayer.getState() == state) {
            return;
        }

        tradePlayer.setState(state);

        ItemStack statusItem = state.getStatusItem();
        IntStream.range(18, 22).forEach(i -> tradePlayer.getPlayer().getOpenInventory().setItem(i, statusItem));
        TradePlayer target = getTarget(tradePlayer);
        IntStream.range(23, 27).forEach(i -> target.getPlayer().getOpenInventory().setItem(i, statusItem));

        tradePlayer.getPlayer().getOpenInventory().setItem(22, state.getActionItem());

        if (state == State.DONE) {
            finishTradeAsync();
        }
    }


    /**
     * Returns a list of valid inventory slots for placing items in the trade.
     *
     * @return A list of valid slots for placing items.
     */
    @Override
    public @NotNull List<Integer> getValidSlots() {
        return Arrays.asList(27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48);
    }

    /**
     * Retrieves the corresponding target slot for a given inventory slot.
     *
     * @param i The inventory slot index to map to a target slot.
     * @return The target slot corresponding to the given inventory slot.
     */
    @Override
    public int getTargetSlots(int i) {
        return 0; // Slot mapping logic can be added here.
    }

    /**
     * Completes the trade by transferring items and coins between the two players.
     * This method ensures that the trade conditions are met before proceeding.
     */
    @Override
    public void finishTrade() {
        Player hostPlayer = host.getPlayer();
        Player targetPlayer = target.getPlayer();
        Economy economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();

        if (host.getState() != State.DONE || target.getState() != State.DONE) {
            return;
        }

        if (!checkInventorySpace(hostPlayer, targetPlayer)) {
            return;
        }

        if (!checkBalance(hostPlayer, targetPlayer, economy)) {
            return;
        }

        //handle coins
        if (host.getCoins() > 0) {
            economy.withdrawPlayer(hostPlayer, host.getCoins());
            economy.depositPlayer(targetPlayer, host.getCoins());
        }

        if (target.getCoins() > 0) {
            economy.withdrawPlayer(targetPlayer, target.getCoins());
            economy.depositPlayer(hostPlayer, target.getCoins());
        }

        //handle items
        host.getItems().forEach(item -> targetPlayer.getInventory().addItem(item));
        target.getItems().forEach(item -> hostPlayer.getInventory().addItem(item));

        hostPlayer.closeInventory();
        targetPlayer.closeInventory();
        hostPlayer.sendMessage(MessageUtil.getMessage("trade_success"));
        targetPlayer.sendMessage(MessageUtil.getMessage("trade_success"));
    }

    /**
     * Creates the inventory for the specified TradePlayer, displaying their items, coins, and trade status.
     *
     * @param tradePlayer The TradePlayer whose inventory is to be created.
     */
    @Override
    public void createInventory(@NotNull TradePlayer tradePlayer) {
        TradePlayer target = getTarget(tradePlayer);
        Inventory inventory = Bukkit.createInventory((InventoryHolder) tradePlayer.getPlayer(),
                54, MessageUtil.getMessage("inventory"));

        inventory.setItem(0, new ItemUtil(Material.PLAYER_HEAD)
                .setSkullOwner(tradePlayer.getPlayer().getName())
                .setDisplayname("§7• §e" + tradePlayer.getPlayer().getName())
                .build());

        inventory.setItem(1, TradeItems.ITEM_ITEM_UPPERLAYER);
        inventory.setItem(2, TradeItems.ITEM_ITEM_UPPERLAYER);
        inventory.setItem(4, TradeItems.ITEM_ITEM_FRAME);
        inventory.setItem(6, TradeItems.ITEM_ITEM_UPPERLAYER);
        inventory.setItem(7, TradeItems.ITEM_ITEM_UPPERLAYER);
        inventory.setItem(8, new ItemUtil(Material.PLAYER_HEAD)
                .setSkullOwner(target.getPlayer().getName())
                .setDisplayname("§7• §e" + target.getPlayer().getName())
                .build());

        inventory.setItem(3, tradePlayer.getCoinsItem());
        inventory.setItem(5, new ItemUtil(Material.SUNFLOWER).setDisplayname("" + target.getCoins()).build());

        IntStream.range(9, 18).forEach(i -> inventory.setItem(i, TradeItems.ITEM_ITEM_FRAME));
        IntStream.range(18, 27).forEach(i -> inventory.setItem(i, new ItemUtil(Material.GRAY_STAINED_GLASS_PANE).build()));

        inventory.setItem(22, TradeItems.ITEM_HANDLING_PROCESSING);
        inventory.setItem(31, TradeItems.ITEM_ITEM_FRAME);
        inventory.setItem(40, TradeItems.ITEM_ITEM_FRAME);
        inventory.setItem(49, TradeItems.ITEM_ITEM_FRAME);

        tradePlayer.getPlayer().openInventory(inventory);
    }

    /**
     * Updates the coins display item in the trade inventory for both players.
     *
     * @param tradePlayer The player whose coins display item is to be updated.
     */
    @Override
    public void updateCoinsItem(@NotNull TradePlayer tradePlayer) {
        TradePlayer target = getTarget(tradePlayer);
        updateState(tradePlayer, State.UNFINISHED);
        updateState(target, State.UNFINISHED);
        tradePlayer.getPlayer().getOpenInventory().setItem(3, tradePlayer.getCoinsItem());
        target.getPlayer().getOpenInventory().setItem(5, new ItemUtil(Material.SUNFLOWER)
                .setDisplayname("Coins:" + tradePlayer.getCoins())
                .build());
    }

    /**
     * Enum representing the possible states of the trade: UNFINISHED, PROCESSING, and DONE.
     */
    @Getter
    public enum State {
        UNFINISHED(TradeItems.ITEM_STATUS_UNFINISHED, TradeItems.ITEM_HANDLING_PROCESSING),
        PROCESSING(TradeItems.ITEM_STATUS_PROCESSING, TradeItems.ITEM_HANDLING_SURE),
        DONE(TradeItems.ITEM_STATUS_DONE, TradeItems.ITEM_ITEM_FRAME);

        private final ItemStack statusItem;
        private final ItemStack actionItem;

        State(ItemStack statusItem, ItemStack actionItem) {
            this.statusItem = statusItem;
            this.actionItem = actionItem;
        }

    }

    /**
     * Checks if both players have enough inventory space to complete the trade.
     *
     * @param hostPlayer   The player initiating the trade (host).
     * @param targetPlayer The player receiving the trade (target).
     * @return Returns true if both players have enough inventory space, false otherwise.
     */
    private boolean checkInventorySpace(Player hostPlayer, Player targetPlayer) {
        if (host.getItems().size() > target.amountOfEmptySlots()) {
            targetPlayer.sendMessage(MessageUtil.getMessage("trade_notenough"));
            targetPlayer.closeInventory();
            return false;
        }

        if (target.getItems().size() > host.amountOfEmptySlots()) {
            hostPlayer.sendMessage(MessageUtil.getMessage("trade_notenough"));
            hostPlayer.closeInventory();
            return false;
        }

        return true;
    }

    /**
     * Checks if both players have enough balance to complete the trade.
     *
     * @param hostPlayer   The player initiating the trade (host).
     * @param targetPlayer The player receiving the trade (target).
     * @param economy      The economy system to check the players' balances.
     * @return Returns true if both players have enough balance, false otherwise.
     */
    private boolean checkBalance(Player hostPlayer, Player targetPlayer, Economy economy) {
        if ((host.getCoins() > 0 && economy.getBalance(hostPlayer) < host.getCoins()) ||
                (target.getCoins() > 0 && economy.getBalance(targetPlayer) < target.getCoins())) {

            if (host.getCoins() > 0) {
                hostPlayer.sendMessage(MessageUtil.getMessage("trade_notcoins"));
                hostPlayer.closeInventory();
            }

            if (target.getCoins() > 0) {
                targetPlayer.sendMessage(MessageUtil.getMessage("trade_notcoins"));
                targetPlayer.closeInventory();
            }

            return false;
        }

        return true;
    }

    /**
     * Initiates the trade completion asynchronously.
     * This method runs the `finishTrade` method in a separate thread to avoid blocking the main thread.
     */
    private void finishTradeAsync() {
        CompletableFuture.runAsync(this::finishTrade);
    }
}
