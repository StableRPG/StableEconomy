package org.stablerpg.stableeconomy.shop.exceptions;

public class NotEnoughToSellException extends SellException {

  public NotEnoughToSellException() {

  }

  public NotEnoughToSellException(String message) {
    super(message);
  }

}
