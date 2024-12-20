package de.bukkitnews.trading.trade.listener;

import de.bukkitnews.trading.Trading;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * This listener handles events related to player disconnections, either by quitting or being kicked.
 * It ensures that the player's trade and invite status are properly cleaned up when they leave.
 */
@RequiredArgsConstructor
public class PlayerQuitListener implements Listener {

    private final Trading trading;

    @EventHandler
    public void handleQuit(@NonNull PlayerQuitEvent event) {
        unregisterPlayer(event.getPlayer());
    }

    @EventHandler
    public void handleKick(@NonNull PlayerKickEvent event) {
        unregisterPlayer(event.getPlayer());
    }

    /**
     * This method unregisters a player's trade and invite status from the TradeManager.
     * It ensures that the player is no longer involved in any trade or invitation after disconnecting.
     *
     * @param player The player whose trade and invite data should be removed.
     */
    private void unregisterPlayer(@NonNull Player player) {
        trading.getTradeManager().unregisterTrade(player);
        trading.getTradeManager().unregisterInvite(player);
    }
}