package de.bukkitnews.trading.trade.listener;

import de.bukkitnews.trading.Trading;
import de.bukkitnews.trading.trade.model.Trade;
import de.bukkitnews.trading.trade.model.TradePlayer;
import de.bukkitnews.trading.util.MessageUtil;
import de.bukkitnews.trading.util.TradeItems;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import java.util.Optional;

/**
 * This listener handles inventory click events during a trade.
 * It ensures that the player can modify their trade items, coins, and trade state correctly.
 */
@RequiredArgsConstructor
public class InventoryClickListener implements Listener {

    private final Trading trading;

    @EventHandler
    public void handleClick(@NonNull InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!event.getView().getTitle().equals(MessageUtil.getMessage("inventory"))) {
            return;
        }

        event.setCancelled(true);

        Optional<Trade> tradeOptional = this.trading.getTradeManager().getTrade(player);
        if (!tradeOptional.isPresent()) {
            event.getView().close();
            return;
        }

        Trade trade = tradeOptional.get();

        Optional<TradePlayer> tradePlayerOptional = trade.getPlayer(player);
        if (!tradePlayerOptional.isPresent()) {
            event.getView().close();
            return;
        }

        TradePlayer tradePlayer = tradePlayerOptional.get();

        if (event.getCurrentItem() == null) {
            return;
        }

        if (event.getCurrentItem().equals(TradeItems.ITEM_HANDLING_PROCESSING) && event.getSlot() == 22) {
            trade.updateState(tradePlayer, Trade.State.PROCESSING);
            return;
        }

        if (event.getCurrentItem().equals(TradeItems.ITEM_HANDLING_SURE) && event.getSlot() == 22) {
            trade.updateState(tradePlayer, Trade.State.DONE);
            return;
        }

        if (event.getCurrentItem().getType() == Material.SUNFLOWER && (event.getSlot() == 3 || event.getSlot() == 5)) {
            handleCoinModification(event, tradePlayer, trade);
            return;
        }

        handleItemModification(event, trade, tradePlayer);
    }

    /**
     * This method handles coin modifications when sunflower items are clicked.
     * The player can modify their coin amount based on the type of click (left, right, shift-left, shift-right).
     *
     * @param event       The InventoryClickEvent that is fired when the player clicks the sunflower.
     * @param tradePlayer The TradePlayer representing the player in the trade.
     * @param trade       The active trade involving the player.
     */
    private void handleCoinModification(@NonNull InventoryClickEvent event, @NonNull TradePlayer tradePlayer, @NonNull Trade trade) {
        switch (event.getClick()) {
            case LEFT:
                int newCoinsLeft = tradePlayer.getCoins() + tradePlayer.getValue();
                if (newCoinsLeft <= 10000000) {
                    trade.setCoins(tradePlayer, newCoinsLeft);
                }
                break;
            case RIGHT:
                int newCoinsRight = tradePlayer.getCoins() - tradePlayer.getValue();
                if (newCoinsRight >= 0) {
                    trade.setCoins(tradePlayer, newCoinsRight);
                }
                break;
            case SHIFT_LEFT:
                int newValueLeft = tradePlayer.getValue() * 10;
                if (newValueLeft <= 100000) {
                    trade.setValue(tradePlayer, newValueLeft);
                }
                break;
            case SHIFT_RIGHT:
                int newValueRight = tradePlayer.getValue() / 10;
                if (newValueRight >= 1) {
                    trade.setValue(tradePlayer, newValueRight);
                }
                break;
        }
    }

    /**
     * This method handles item modifications when a player clicks on items in the trade inventory.
     * It either removes the item from the trade or adds it to the player's trade inventory.
     *
     * @param event       The InventoryClickEvent that is fired when the player clicks on an item.
     * @param trade       The active trade involving the player.
     * @param tradePlayer The TradePlayer representing the player in the trade.
     */
    private void handleItemModification(@NonNull InventoryClickEvent event, @NonNull Trade trade, @NonNull TradePlayer tradePlayer) {
        if (trade.getValidSlots().contains(event.getRawSlot())) {
            trade.removeItem(tradePlayer, event.getRawSlot());
        } else {
            if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.PLAYER)
                return;

            if (!trade.addItem(tradePlayer, event.getSlot(), event.getCurrentItem())) {
                tradePlayer.getPlayer().playSound(tradePlayer.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
            }
        }
    }
}