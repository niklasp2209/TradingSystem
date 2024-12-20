package de.bukkitnews.trading.trade.model;

import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

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
    boolean addItem(@NonNull TradePlayer tradePlayer, int slot, @NonNull ItemStack itemStack);

    /**
     * Removes an item from the trade for the specified player at a given slot.
     *
     * @param tradePlayer The player who is removing the item.
     * @param slot        The slot in the inventory where the item should be removed from.
     * @param itemStack   The item to be removed.
     */
    void removeItem(@NonNull TradePlayer tradePlayer, int slot, @NonNull ItemStack itemStack);

    /**
     * Sets the amount of coins for the specified player.
     *
     * @param tradePlayer The player whose coins are being set.
     * @param coins       The number of coins to set.
     */
    void setCoins(@NonNull TradePlayer tradePlayer, int coins);

    /**
     * Sets the value of the trade for the specified player.
     *
     * @param tradePlayer The player whose trade value is being set.
     * @param value       The value to set for the trade.
     */
    void setValue(@NonNull TradePlayer tradePlayer, int value);

    /**
     * Updates the state of the trade for the specified player.
     *
     * @param tradePlayer The player whose trade state is being updated.
     * @param state       The new state of the trade.
     */
    void updateState(@NonNull TradePlayer tradePlayer, Trade.State state);

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
    void createInventory(@NonNull TradePlayer tradePlayer);

    /**
     * Updates the coin item for the specified player.
     *
     * @param tradePlayer The player whose coin item should be updated.
     */
    void updateCoinsItem(@NonNull TradePlayer tradePlayer);

    /**
     * Retrieves the coin item for the specified player.
     *
     * @param tradePlayer The player whose coin item is being retrieved.
     * @return The ItemStack representing the coin item.
     */
    ItemStack getCoinsItem(@NonNull TradePlayer tradePlayer);
}