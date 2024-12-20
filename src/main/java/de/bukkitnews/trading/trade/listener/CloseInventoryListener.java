package de.bukkitnews.trading.trade.listener;

import de.bukkitnews.trading.Trading;
import de.bukkitnews.trading.trade.model.TradePlayer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * This listener handles the event when a player closes their inventory during a trade.
 * It ensures that the player's items are returned to their inventory and the trade is properly cleaned up.
 */
@RequiredArgsConstructor
public class CloseInventoryListener implements Listener {

    private final Trading trading;

    /**
     * This event handler is triggered when a player closes their inventory.
     * It checks if the player was engaged in a trade and handles the necessary clean-up actions.
     *
     * @param event The InventoryCloseEvent that is fired when the player closes their inventory.
     */
    @EventHandler
    public void onClose(@NonNull InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (!event.getView().getTitle().equals("Inventory")) {
            return;
        }

        this.trading.getTradeManager().getTrade(player).ifPresent(trade -> trade.getPlayer(player).ifPresent(tradePlayer -> {
            tradePlayer.getItems().forEach(itemStack ->
                    tradePlayer.getPlayer().getInventory().addItem(itemStack)
            );

            tradePlayer.getPlayer().sendMessage("Test 18");

            this.trading.getTradeManager().unregisterTrade(tradePlayer.getPlayer());

            TradePlayer target = trade.getTarget(tradePlayer);
            target.getPlayer().getOpenInventory().close();
        }));

    }
}