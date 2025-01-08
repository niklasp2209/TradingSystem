package de.bukkitnews.trading.trade.command;

import de.bukkitnews.trading.Trading;
import de.bukkitnews.trading.util.MessageUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * This class handles the "/trade" command logic, allowing players to invite others to trade
 * and to accept trade invitations.
 */
@RequiredArgsConstructor
public class TradeCommand implements CommandExecutor {

    @NonNull private final Trading plugin;

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            player.sendMessage(MessageUtil.getMessage("command_trade_usage"));
            return true;
        }

        Optional<Player> targetOpt = Optional.ofNullable(Bukkit.getPlayer(args[args.length - 1]));

        if (targetOpt.isEmpty()) {
            player.sendMessage(MessageUtil.getMessage("player_not_online"));
            return true;
        }

        Player target = targetOpt.get();

        if (args.length == 2 && args[0].equalsIgnoreCase("accept")) {
            handleAcceptCommand(player, target);
            return true;
        }

        if (args.length == 1) {
            handleInviteCommand(player, target);
            return true;
        }

        player.sendMessage(MessageUtil.getMessage("command_main_usage"));
        return true;
    }

    /**
     * This method handles the '/trade accept <Player>' command.
     * It checks if the player has a valid trade invitation and accepts the trade if valid.
     *
     * @param player The player accepting the trade.
     * @param target The player who sent the trade invitation.
     */
    private void handleAcceptCommand(@NonNull Player player, @NonNull Player target) {
        if (!plugin.getTradeManager().inviteValid(player)) {
            player.sendMessage(MessageUtil.getMessage("trade_no_invites"));
            return;
        }

        plugin.getTradeManager().unregisterTrade(player);
        plugin.getTradeManager().unregisterTrade(target);
        plugin.getTradeManager().createTrade(player, target);
        player.sendMessage(MessageUtil.getMessage("player_trade_accept", target.getName()));
        target.sendMessage(MessageUtil.getMessage("target_trade_accept", player.getName()));
    }

    /**
     * This method handles the '/trade <Player>' command.
     * It sends an invitation to the target player to initiate a trade.
     *
     * @param player The player who is sending the trade invitation.
     * @param target The player who is being invited to trade.
     */
    private void handleInviteCommand(@NonNull Player player, @NonNull Player target) {
        if (player.equals(target)) {
            player.sendMessage(MessageUtil.getMessage("command_trade_yourself"));
            return;
        }

        plugin.getTradeManager().registerInvite(player, target);
        player.sendMessage(MessageUtil.getMessage("player_trade_invite", target.getName()));
        target.sendMessage(MessageUtil.getMessage("target_trade_invite", player.getName()));
    }
}
