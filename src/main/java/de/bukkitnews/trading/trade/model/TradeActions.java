package de.bukkitnews.trading.trade.model;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This interface defines the actions that can be performed during a trade between players.
 * It provides methods for adding/removing items, setting coin values, updating trade state,
 * and managing inventory interactions for players involved in the trade.
 */
public interface TradeActions {

    /**
     * Adds an item to the trade for the specified player at a given slot.
     *
     * @param tradePlayer The player who is adding the item.
     * @param slot        The slot in the inventory where the item should be added.
     * @param itemStack   The item to be added.
     * @return true if the item was successfully added, false otherwise.
     */
    boolean addItem(@NotNull TradePlayer tradePlayer, int slot, @NotNull ItemStack itemStack);

    /**
     * Removes an item from the trade for the specified player at a given slot.
     *
     * @param tradePlayer The player who is removing the item.
     * @param slot        The slot in the inventory where the item should be removed from.
     */
    void removeItem(@NotNull TradePlayer tradePlayer, int slot);

    /**
     * Sets the amount of coins for the specified player.
     *
     * @param tradePlayer The player whose coins are being set.
     * @param coins       The number of coins to set.
     */
    void setCoins(@NotNull TradePlayer tradePlayer, int coins);

    /**
     * Sets the value of the trade for the specified player.
     *
     * @param tradePlayer The player whose trade value is being set.
     * @param value       The value to set for the trade.
     */
    void setValue(@NotNull TradePlayer tradePlayer, int value);

    /**
     * Updates the state of the trade for the specified player.
     *
     * @param tradePlayer The player whose trade state is being updated.
     * @param state       The new state of the trade.
     */
    void updateState(@NotNull TradePlayer tradePlayer, Trade.State state);

    /**
     * Returns a list of valid slots available for the trade.
     *
     * @return A list of integers representing valid slot indices.
     */
    List<Integer> getValidSlots();

    /**
     * Retrieves the target slots based on the given index.
     *
     * @param i The index used to get target slots.
     * @return The target slot associated with the given index.
     */
    int getTargetSlots(int i);

    /**
     * Finalizes and completes the trade process.
     */
    void finishTrade();

    /**
     * Creates the inventory for the specified player involved in the trade.
     *
     * @param tradePlayer The player for whom the inventory is being created.
     */
    void createInventory(@NotNull TradePlayer tradePlayer);

    /**
     * Updates the coin item for the specified player.
     *
     * @param tradePlayer The player whose coin item should be updated.
     */
    void updateCoinsItem(@NotNull TradePlayer tradePlayer);
}