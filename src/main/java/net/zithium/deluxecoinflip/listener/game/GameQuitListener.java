/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.listener.game;

import com.tcoded.folialib.impl.PlatformScheduler;
import net.zithium.deluxecoinflip.DeluxeCoinflipPlugin;
import net.zithium.deluxecoinflip.config.Messages;
import net.zithium.deluxecoinflip.economy.EconomyManager;
import net.zithium.deluxecoinflip.economy.provider.EconomyProvider;
import net.zithium.deluxecoinflip.game.CoinflipGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class GameQuitListener implements Listener {

    private final DeluxeCoinflipPlugin plugin;
    private final PlatformScheduler scheduler;

    public GameQuitListener(@NotNull DeluxeCoinflipPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = DeluxeCoinflipPlugin.scheduler();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        final Player quitter = event.getPlayer();

        final CoinflipGame game = plugin.getGameManager().getCoinflipGame(quitter.getUniqueId());
        if (game == null || game.isActiveGame()) {
            return;
        }

        final EconomyManager economyManager = plugin.getEconomyManager();
        final EconomyProvider economyProvider = economyManager.getEconomyProvider(game.getProvider());
        if (economyProvider == null) {
            plugin.getLogger().warning("[DeluxeCoinflip] Missing economy provider '" + game.getProvider() + "'; refund skipped for " + quitter.getName() + ".");
            scheduler.runAsync(task -> plugin.getStorageManager().getStorageHandler().deleteCoinflip(game.getPlayerUUID()));
            plugin.getGameManager().removeCoinflipGame(game.getPlayerUUID());
            return;
        }

        final long amount = game.getAmount();
        final String amountFormatted = String.format(Locale.US, "%,d", amount);

        economyProvider.deposit(game.getOfflinePlayer(), amount);

        if (quitter.isOnline()) {
            Messages.GAME_REFUNDED.send(
                quitter,
                "{AMOUNT}", amountFormatted,
                "{CURRENCY}", game.getProvider()
            );
        }

        scheduler.runAsync(task -> plugin.getStorageManager().getStorageHandler().deleteCoinflip(game.getPlayerUUID()));
        plugin.getGameManager().removeCoinflipGame(game.getPlayerUUID());
    }
}
