package de.bukkitnews.trading.trade.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Optional;

/**
 * This class represents a player involved in a trade.
 * It stores the player's inventory items, coin balance, trade value,
 * and the state of the trade for the player.
 */
@Getter
@Setter
public class TradePlayer {

    private final Player player;
    private Trade.State state;
    private final ArrayList<ItemStack> items;

    private Optional<Integer> coins;
    private Optional<Integer> value;

    public TradePlayer(@NonNull Player player) {
        this.player = player;
        this.state = Trade.State.UNFINISHED;
        this.items = new ArrayList<>();
        this.coins = Optional.of(0);
        this.value = Optional.of(1);
    }

    /**
     * Calculates the number of empty slots in the player's inventory.
     *
     * @return The number of empty slots in the player's inventory.
     */
    public int amountOfEmptySlots() {
        int result = 0;
        for (int i = 0; i < 36; i++) {
            if (this.player.getInventory().getItem(i) == null)
                result++;
        }
        return result;
    }

    /**
     * Sets the coin balance for the player.
     *
     * @param coins The number of coins to set for the player.
     */
    public void setCoins(int coins) {
        this.coins = Optional.of(coins);
    }

    /**
     * Sets the trade value for the player.
     *
     * @param value The value to set for the trade.
     */
    public void setValue(int value) {
        this.value = Optional.of(value);
    }

    /**
     * Gets the current coin balance for the player.
     *
     * @return The number of coins the player has, or 0 if not set.
     */
    public int getCoins() {
        return coins.orElse(0);
    }

    public int getValue() {
        return value.orElse(1);
    }
}