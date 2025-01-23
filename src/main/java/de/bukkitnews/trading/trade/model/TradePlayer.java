package de.bukkitnews.trading.trade.model;

import de.bukkitnews.trading.util.ItemUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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

    private final @NotNull Player player;
    private @NotNull Trade.State state;
    private final @NotNull ArrayList<ItemStack> items;

    private @NotNull Optional<Integer> coins;
    private @NotNull Optional<Integer> value;

    public TradePlayer(@NotNull Player player) {
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
        int emptySlots = 0;
        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getItem(i) == null) {
                emptySlots++;
            }
        }
        return emptySlots;
    }

    /**
     * Creates a custom ItemStack to display the coin and value amounts for a player in the trade.
     *
     * @return An ItemStack representing the player's coins and value.
     */
    public @NotNull ItemStack getCoinsItem() {
        return new ItemUtil(Material.SUNFLOWER)
                .setDisplayname(coins + " " + value + " 400")
                .build();
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