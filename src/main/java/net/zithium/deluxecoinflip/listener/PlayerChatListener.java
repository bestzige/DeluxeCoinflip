/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.listener;

import com.tcoded.folialib.impl.PlatformScheduler;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.zithium.deluxecoinflip.DeluxeCoinflipPlugin;
import net.zithium.deluxecoinflip.config.ConfigType;
import net.zithium.deluxecoinflip.config.Messages;
import net.zithium.deluxecoinflip.game.CoinflipGame;
import net.zithium.deluxecoinflip.utility.TextUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Locale;
import java.util.UUID;

public final class PlayerChatListener implements Listener {

    public PlayerChatListener(DeluxeCoinflipPlugin plugin) {
        try {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent", false, plugin.getClass().getClassLoader());
            plugin.getServer().getPluginManager().registerEvents(new PaperHandler(plugin), plugin);
        } catch (ClassNotFoundException ignored) {
            plugin.getServer().getPluginManager().registerEvents(new SpigotHandler(plugin), plugin);
        }
    }

    /**
     * Paper handler using AsyncChatEvent + Adventure.
     * Only registered when Paper's event exists.
     */
    private static final class PaperHandler implements Listener {

        private final DeluxeCoinflipPlugin plugin;
        private final PlatformScheduler scheduler;

        private PaperHandler(DeluxeCoinflipPlugin plugin) {
            this.plugin = plugin;
            this.scheduler = DeluxeCoinflipPlugin.scheduler();
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerChat(AsyncChatEvent event) {
            final Player player = event.getPlayer();
            final UUID uuid = player.getUniqueId();

            final CoinflipGame game = plugin.getListenerCache().getIfPresent(uuid);
            if (game == null) {
                return;
            }

            if (game.isActiveGame()) {
                return;
            }

            final String message = PlainTextComponentSerializer.plainText()
                    .serialize(event.message())
                    .trim();

            if (message.equalsIgnoreCase("cancel")) {
                event.setCancelled(true);
                plugin.getListenerCache().invalidate(uuid);
                Messages.CHAT_CANCELLED.send(player);
                scheduler.runNextTick(task -> plugin.getInventoryManager().getGameBuilderGUI().openGameBuilderGUI(player, game));
                return;
            }

            final Long parsed = TextUtil.parseAmountToLong(message);
            if (parsed == null) {
                event.setCancelled(true);
                Messages.INVALID_AMOUNT.send(player, "{INPUT}", message.replace(",", ""));
                return;
            }

            final long amount = parsed;

            final FileConfiguration config = plugin.getConfigHandler(ConfigType.CONFIG).getConfig();
            final long maximumBet = config.getLong("settings.maximum-bet");
            final long minimumBet = config.getLong("settings.minimum-bet");

            if (amount > maximumBet) {
                event.setCancelled(true);
                Messages.CREATE_MAXIMUM_AMOUNT.send(player, "{MAX_BET}", String.format(Locale.US, "%,d", maximumBet));
                return;
            }

            if (amount < minimumBet) {
                event.setCancelled(true);
                Messages.CREATE_MINIMUM_AMOUNT.send(player, "{MIN_BET}", String.format(Locale.US, "%,d", minimumBet));
                return;
            }

            event.setCancelled(true);
            plugin.getListenerCache().invalidate(uuid);
            game.setAmount(amount);

            scheduler.runNextTick(task -> plugin.getInventoryManager().getGameBuilderGUI().openGameBuilderGUI(player, game));
        }
    }

    /**
     * Spigot handler using AsyncPlayerChatEvent (deprecated on Paper).
     * Registered only when Paper's event is absent.
     */
    private static final class SpigotHandler implements Listener {

        private final DeluxeCoinflipPlugin plugin;
        private final PlatformScheduler scheduler;

        private SpigotHandler(DeluxeCoinflipPlugin plugin) {
            this.plugin = plugin;
            this.scheduler = DeluxeCoinflipPlugin.scheduler();
        }

        @SuppressWarnings("deprecation")
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            final Player player = event.getPlayer();
            final UUID uuid = player.getUniqueId();

            final CoinflipGame game = plugin.getListenerCache().getIfPresent(uuid);
            if (game == null) {
                return;
            }
            if (game.isActiveGame()) {
                return;
            }

            final String message = event.getMessage().trim();

            if (message.equalsIgnoreCase("cancel")) {
                event.setCancelled(true);
                plugin.getListenerCache().invalidate(uuid);
                Messages.CHAT_CANCELLED.send(player);
                scheduler.runNextTick(task -> plugin.getInventoryManager().getGameBuilderGUI().openGameBuilderGUI(player, game));
                return;
            }

            final Long parsed = TextUtil.parseAmountToLong(message);
            if (parsed == null) {
                event.setCancelled(true);
                Messages.INVALID_AMOUNT.send(player, "{INPUT}", message.replace(",", ""));
                return;
            }

            final long amount = parsed;

            final FileConfiguration config = plugin.getConfigHandler(ConfigType.CONFIG).getConfig();
            final long maximumBet = config.getLong("settings.maximum-bet");
            final long minimumBet = config.getLong("settings.minimum-bet");

            if (amount > maximumBet) {
                event.setCancelled(true);
                Messages.CREATE_MAXIMUM_AMOUNT.send(player, "{MAX_BET}", String.format(Locale.US, "%,d", maximumBet));
                return;
            }

            if (amount < minimumBet) {
                event.setCancelled(true);
                Messages.CREATE_MINIMUM_AMOUNT.send(player, "{MIN_BET}", String.format(Locale.US, "%,d", minimumBet));
                return;
            }

            event.setCancelled(true);
            plugin.getListenerCache().invalidate(uuid);
            game.setAmount(amount);

            scheduler.runNextTick(task -> plugin.getInventoryManager().getGameBuilderGUI().openGameBuilderGUI(player, game));
        }
    }
}
