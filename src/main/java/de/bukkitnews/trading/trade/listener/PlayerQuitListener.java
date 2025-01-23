package de.bukkitnews.trading.trade.listener;

import de.bukkitnews.trading.Trading;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This listener handles events related to player disconnections, either by quitting or being kicked.
 * It ensures that the player's trade and invite status are properly cleaned up when they leave.
 */
@RequiredArgsConstructor
public class PlayerQuitListener implements Listener {

    private final @NotNull Trading trading;

    /**
     * This method unregisters a player's trade and invite status from the TradeManager.
     * It ensures that the player is no longer involved in any trade or invitation after disconnecting.
     */
    @EventHandler
    public void handleQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        trading.getTradeManager().unregisterTrade(player);
        trading.getTradeManager().unregisterInvite(player);
    }
}