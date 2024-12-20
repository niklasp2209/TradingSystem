package de.bukkitnews.trading.trade;

import de.bukkitnews.trading.trade.model.Trade;
import de.bukkitnews.trading.trade.model.TradePlayer;
import lombok.NonNull;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

/**
 * The TradeManager handles the trade invitations and active trades between players.
 * It manages trade invitations, creates trades, and tracks the state of trades for each player.
 */
public class TradeManager {

    private final HashMap<UUID, UUID> invites = new HashMap<>();
    private final HashMap<UUID, Trade> trades = new HashMap<>();

    /**
     * Retrieves the player that has invited the specified player to trade.
     *
     * @param player The player whose invitation status is being checked.
     * @return An Optional containing the invited player, or empty if no invitation exists.
     */
    public Optional<UUID> getInvited(@Nullable Player player) {
        return Optional.ofNullable(player).map(p -> invites.get(p.getUniqueId()));
    }

    /**
     * Registers an invitation between two players for a trade.
     * This establishes a mutual invitation between the player and the target.
     *
     * @param player The player who is inviting.
     * @param target The player who is being invited.
     */
    public void registerInvite(@NonNull Player player, @NonNull Player target) {
        invites.put(player.getUniqueId(), target.getUniqueId());
    }

    /**
     * Unregisters the trade invitation for a given player.
     * Removes the mutual invitation between the player and the target.
     *
     * @param player The player whose invitation is being removed.
     */
    public void unregisterInvite(@NonNull Player player) {
        invites.remove(player.getUniqueId());
    }

    /**
     * Checks if the invitation for the specified player is valid.
     * A valid invitation means the player and their target have mutually agreed to trade.
     *
     * @param player The player whose invitation validity is being checked.
     * @return true if the invitation is valid, false otherwise.
     */
    public boolean inviteValid(@NonNull Player player) {
        UUID playerUUID = player.getUniqueId();
        return getInvited(player)
                .map(invites::get)
                .filter(inviteeUUID -> inviteeUUID.equals(playerUUID))
                .isPresent();
    }

    /**
     * Retrieves the active trade for the specified player, if any.
     *
     * @param player The player whose trade status is being queried.
     * @return An Optional containing the active trade, or empty if no trade exists.
     */
    public Optional<Trade> getTrade(@Nullable Player player) {
        return Optional.ofNullable(trades.get(player));
    }

    /**
     * Creates a new trade between two players by initializing their TradePlayer objects.
     * The trade is then stored in the trades map.
     *
     * @param player The first player in the trade.
     * @param target The second player in the trade.
     */
    public void createTrade(@NonNull Player player, Player target) {
        TradePlayer tpP = new TradePlayer(player);
        TradePlayer tpT = new TradePlayer(target);
        Trade trade = new Trade(tpP, tpT);

        trades.put(player.getUniqueId(), trade);
        trades.put(target.getUniqueId(), trade);
    }

    /**
     * Unregisters the active trade for a specified player.
     * This removes the trade from the trades map.
     *
     * @param player The player whose trade is being removed.
     */
    public void unregisterTrade(@NonNull Player player) {
        this.trades.remove(player.getUniqueId());
    }
}