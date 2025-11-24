/*
 * DeluxeCoinflip Plugin
 * Copyright (c) 2021 - 2025 Zithium Studios. All rights reserved.
 */

package net.zithium.deluxecoinflip.api.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public class CoinflipCompletedEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final UUID visitorId;
    private final UUID loserId;
    private final OfflinePlayer winner;
    private final OfflinePlayer loser;
    private final long winnings;
    private final boolean forfeit;

    public CoinflipCompletedEvent(@NotNull UUID winnerId, @NotNull UUID loserId,
                                  @Nullable OfflinePlayer winner, @Nullable OfflinePlayer loser,
                                  long winnings, boolean forfeit) {
        this.visitorId = winnerId;
        this.loserId = loserId;
        this.winner = winner;
        this.loser = loser;
        this.winnings = winnings;
        this.forfeit = forfeit;
    }

    public UUID getWinnerId() {
        return visitorId;
    }

    public UUID getLoserId() {
        return loserId;
    }

    @Nullable
    public OfflinePlayer getWinner() {
        return winner;
    }

    @Nullable
    public OfflinePlayer getLoser() {
        return loser;
    }

    public long getWinnings() {
        return winnings;
    }

    public boolean isForfeit() {
        return forfeit;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
