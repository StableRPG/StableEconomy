package org.stablerpg.stableeconomy.config.currency;

import lombok.Getter;

public enum CurrencyMessageType {

  VIEW_OWN("view-command.own", "balance"),
  VIEW_OTHER("view-command.other", "player", "balance"),

  TRANSFER_SEND("transfer-command.send", "sender", "receiver", "amount", "old-balance", "new-balance"),
  TRANSFER_RECEIVE("transfer-command.receive", "sender", "receiver", "amount", "old-balance", "new-balance"),
  INSUFFICIENT_BALANCE("transfer-command.errors.insufficient-balance", "balance", "amount"),

  LEADERBOARD_TITLE("leaderboard-command.title", "currency", "page", "max-page"),
  LEADERBOARD_SERVER_TOTAL("leaderboard-command.server-total", "server-total"),
  LEADERBOARD_BALANCE_VIEW("leaderboard-command.balance-view", "position", "player", "balance"),
  LEADERBOARD_NEXT_PAGE("leaderboard-command.next-page", "command", "page", "next-page", "max-page"),

  ADMIN_SET("admin-command.set-balance", "player", "old-balance", "new-balance"),
  ADMIN_ADD("admin-command.give-balance", "player", "old-balance", "balance-change", "new-balance"),
  ADMIN_REMOVE("admin-command.take-balance", "player", "old-balance", "balance-change", "new-balance"),
  ADMIN_RESET("admin-command.reset-balance", "player", "old-balance");

  @Getter
  private final String key;
  private final String[] requiredTags;

  CurrencyMessageType(String key, String... requiredTags) {
    this.key = key;
    this.requiredTags = requiredTags;
  }

  public void checkTags(String[] tags) {
    if (requiredTags.length == 0) return;

    if (tags.length / 2 < requiredTags.length) throw new IllegalArgumentException("Missing required tags");

    if (tags.length % 2 != 0) throw new IllegalArgumentException("Incomplete tag pairs");

    outer:
    for (String req : requiredTags) {
      for (int i = 0; i < tags.length; i += 2)
        if (req.equals(tags[i])) continue outer;
      throw new IllegalArgumentException("Missing required tag: " + req);
    }
  }

}