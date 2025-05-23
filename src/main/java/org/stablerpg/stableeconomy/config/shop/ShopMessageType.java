package org.stablerpg.stableeconomy.config.shop;

import lombok.Getter;

public enum ShopMessageType {

  SUCCESSFULLY_PURCHASED("buy.successfully-purchased", "payed-amount", "amount", "item-name"),
  INSUFFICIENT_BALANCE("buy.insufficient-balance", "required-balance", "balance", "amount", "item-name"),
  NOT_ENOUGH_SPACE("buy.not-enough-space", "amount", "item-name"),

  SUCCESSFULLY_SOLD("sell.successfully-sold", "received-amount", "amount", "item-name"),
  NOT_ENOUGH_TO_SELL("sell.not-enough-to-sell", "amount", "item-name"),;

  @Getter
  private final String key;
  private final String[] requiredTags;

  ShopMessageType(String key, String... requiredTags) {
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
