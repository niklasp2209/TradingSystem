package de.bukkitnews.trading.trade.model;

import de.bukkitnews.trading.util.TradeItems;
import de.bukkitnews.trading.util.ItemUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * This class represents a trade between two players, allowing them to exchange items and coins.
 * It handles the various actions that can be performed during the trade, such as adding/removing items,
 * setting coins, and managing the state of the trade.
 */
@Getter
public record Trade(TradePlayer host, TradePlayer target) implements TradeActions {

    public Trade(@NonNull TradePlayer host, @NonNull TradePlayer target) {
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
    public Optional<TradePlayer> getPlayer(@Nullable Player player) {
        return Optional.ofNullable(player)
                .map(p -> p.equals(this.host.getPlayer()) ? this.host : this.target);
    }

    /**
     * Retrieves the opposing TradePlayer based on the given TradePlayer.
     *
     * @param tradePlayer The TradePlayer whose opposing player is to be retrieved.
     * @return The opposing TradePlayer.
     * @throws IllegalArgumentException If the provided TradePlayer is not part of the trade.
     */
    public TradePlayer getTarget(@Nullable TradePlayer tradePlayer) {
        return Optional.ofNullable(tradePlayer)
                .map(p -> p.equals(this.host) ? this.target : this.host)
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
    public boolean addItem(@NonNull TradePlayer tradePlayer, int slot, @NonNull ItemStack itemStack) {
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
     * @param itemStack   The item being removed from the trade.
     */
    @Override
    public void removeItem(@NonNull TradePlayer tradePlayer, int slot, @NonNull ItemStack itemStack) {
        TradePlayer target = getTarget(tradePlayer);
        updateState(tradePlayer, State.UNFINISHED);
        updateState(target, State.UNFINISHED);

        tradePlayer.getPlayer().getInventory().addItem(itemStack);
        tradePlayer.getPlayer().getOpenInventory().setItem(slot, new ItemStack(Material.AIR));
        tradePlayer.getItems().remove(itemStack);
        target.getPlayer().getOpenInventory().setItem(getTargetSlots(slot), new ItemStack(Material.AIR));
    }

    /**
     * Sets the coin amount for the specified player and updates their inventory accordingly.
     *
     * @param tradePlayer The player whose coins are to be set.
     * @param coins       The number of coins to set.
     */
    @Override
    public void setCoins(@NonNull TradePlayer tradePlayer, int coins) {
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
    public void setValue(@NonNull TradePlayer tradePlayer, int value) {
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
    public void updateState(@NonNull TradePlayer tradePlayer, State state) {
        if (tradePlayer.getState() == state) {
            return;
        }

        tradePlayer.setState(state);
        ItemStack statusItem = switch (state) {
            case UNFINISHED -> TradeItems.ITEM_STATUS_UNFINISHED;
            case PROCESSING -> TradeItems.ITEM_STATUS_PROCESSING;
            case DONE -> TradeItems.ITEM_STATUS_DONE;
        };

        IntStream.range(18, 22).forEach(i -> tradePlayer.getPlayer().getOpenInventory().setItem(i, statusItem));
        TradePlayer target = getTarget(tradePlayer);
        IntStream.range(23, 27).forEach(i -> target.getPlayer().getOpenInventory().setItem(i, statusItem));

        if (state == State.UNFINISHED) {
            tradePlayer.getPlayer().getOpenInventory().setItem(22, TradeItems.ITEM_HANDLING_PROCESSING);
        } else if (state == State.PROCESSING) {
            tradePlayer.getPlayer().getOpenInventory().setItem(22, TradeItems.ITEM_HANDLING_SURE);
        } else {
            tradePlayer.getPlayer().getOpenInventory().setItem(22, TradeItems.ITEM_ITEM_FRAME);
            finishTrade();
        }
    }

    /**
     * Returns a list of valid inventory slots for placing items in the trade.
     *
     * @return A list of valid slots for placing items.
     */
    @Override
    public List<Integer> getValidSlots() {
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
        if (this.host.getState() != State.DONE || this.target.getState() != State.DONE) {
            return;
        }

        if (this.host.getItems().size() > this.target.amountOfEmptySlots()) {
            this.target.getPlayer().sendMessage("Test 1");
            this.target.getPlayer().closeInventory();
            return;
        }

        if (this.target.getItems().size() > this.host.amountOfEmptySlots()) {
            this.host.getPlayer().sendMessage("Test 2");
            this.host.getPlayer().closeInventory();
            return;
        }

        if (this.host.getCoins() > 0 && this.host.getCoins() > this.target.getCoins()) {
            this.host.getPlayer().sendMessage("Test 3");
            this.host.getPlayer().closeInventory();
            return;
        }

        if (this.target.getCoins() > 0 && this.target.getCoins() > this.host.getCoins()) {
            this.target.getPlayer().sendMessage("Test 4");
            this.target.getPlayer().closeInventory();
            return;
        }

        this.host.getItems().forEach(item -> this.target.getPlayer().getInventory().addItem(item));
        this.target.getItems().forEach(item -> this.host.getPlayer().getInventory().addItem(item));

        this.host.getPlayer().closeInventory();
        this.target.getPlayer().closeInventory();
        this.host.getPlayer().sendMessage("Test 5");
        this.target.getPlayer().sendMessage("Test 6");
    }

    /**
     * Creates the inventory for the specified TradePlayer, displaying their items, coins, and trade status.
     *
     * @param tradePlayer The TradePlayer whose inventory is to be created.
     */
    @Override
    public void createInventory(@NonNull TradePlayer tradePlayer) {
        TradePlayer target = getTarget(tradePlayer);
        Inventory inventory = Bukkit.createInventory((InventoryHolder) tradePlayer.getPlayer(),
                54, "Inventory");

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

        inventory.setItem(3, getCoinsItem(tradePlayer));
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
    public void updateCoinsItem(@NonNull TradePlayer tradePlayer) {
        TradePlayer target = getTarget(tradePlayer);
        updateState(tradePlayer, State.UNFINISHED);
        updateState(target, State.UNFINISHED);
        tradePlayer.getPlayer().getOpenInventory().setItem(3, getCoinsItem(tradePlayer));
        target.getPlayer().getOpenInventory().setItem(5, new ItemUtil(Material.SUNFLOWER)
                .setDisplayname("Coins:" + tradePlayer.getCoins())
                .build());
    }

    /**
     * Creates a custom ItemStack to display the coin and value amounts for a player in the trade.
     *
     * @param tradePlayer The player whose coin item is to be created.
     * @return An ItemStack representing the player's coins and value.
     */
    @Override
    public ItemStack getCoinsItem(@NonNull TradePlayer tradePlayer) {
        return new ItemUtil(Material.SUNFLOWER)
                .setDisplayname(tradePlayer.getCoins() + " " + tradePlayer.getValue() + " 400")
                .build();
    }

    /**
     * Enum representing the possible states of the trade: UNFINISHED, PROCESSING, and DONE.
     */
    public enum State {
        UNFINISHED, PROCESSING, DONE;
    }
}
