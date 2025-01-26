package de.bukkitnews.trading.trade.command;

import de.bukkitnews.trading.Trading;
import de.bukkitnews.trading.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * This class handles the "/trade" command logic, allowing players to invite others to trade
 * and to accept trade invitations.
 */
@RequiredArgsConstructor
public class TradeCommand implements CommandExecutor, TabCompleter {

    private final @NotNull Trading plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(MessageUtil.getMessage("command_trade_usage"));
            return true;
        }


        if (args.length == 2 && args[0].equalsIgnoreCase("logs")) {
            if (!player.hasPermission("trading.command.logs")) {
                player.sendMessage(MessageUtil.getMessage("no_permission"));
                return true;
            }
            Optional<Player> targetOpt = Optional.ofNullable(Bukkit.getPlayer(args[1]));
            if (targetOpt.isEmpty()) {
                player.sendMessage(MessageUtil.getMessage("player_not_online"));
                return true;
            }

            Player target = targetOpt.get();
            displayTradeLogs(player, target);
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
    private void handleAcceptCommand(@NotNull Player player, @NotNull Player target) {
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
    private void handleInviteCommand(@NotNull Player player, @NotNull Player target) {
        if (player.equals(target)) {
            player.sendMessage(MessageUtil.getMessage("command_trade_yourself"));
            return;
        }

        plugin.getTradeManager().registerInvite(player, target);
        player.sendMessage(MessageUtil.getMessage("player_trade_invite", target.getName()));
        target.sendMessage(MessageUtil.getMessage("target_trade_invite", player.getName()));
    }

    /**
     * Displays all trade logs for a player sorted by timestamp, most recent first.
     *
     * @param player The player requesting the logs.
     * @param target The target player whose logs are being displayed.
     */
    private void displayTradeLogs(@NotNull Player player, @NotNull Player target) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            Optional<List<String>> tradeLogsOpt = plugin.getTradeLogger().getTradeLogsForPlayer(target);

            if (tradeLogsOpt.isEmpty()) {
                player.sendMessage(MessageUtil.getMessage("no_trade_logs_found", target.getName()));
                return;
            }

            List<String> sortedLogs = tradeLogsOpt.get().stream()
                    .sorted((log1, log2) -> {
                        String timestamp1 = extractTimestamp(log1);
                        String timestamp2 = extractTimestamp(log2);
                        return timestamp2.compareTo(timestamp1);
                    })
                    .collect(Collectors.toList());

            sortedLogs.forEach(player::sendMessage);
        });

        future.join();
    }

    /**
     * Extracts the timestamp from the trade log string. This assumes that the timestamp is in a known format.
     *
     * @param tradeLog The trade log string.
     * @return The timestamp as a string.
     */
    private @NotNull String extractTimestamp(@NotNull String tradeLog) {
        String timestampPrefix = "Timestamp: ";
        int timestampStartIndex = tradeLog.indexOf(timestampPrefix) + timestampPrefix.length();
        int timestampEndIndex = tradeLog.indexOf("\n", timestampStartIndex);
        return tradeLog.substring(timestampStartIndex, timestampEndIndex).trim();
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1 && args[0].equalsIgnoreCase("trade")) {
            suggestions.add("logs");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("trade")) {
            suggestions.add("accept");
        }

        return suggestions;
    }
}
