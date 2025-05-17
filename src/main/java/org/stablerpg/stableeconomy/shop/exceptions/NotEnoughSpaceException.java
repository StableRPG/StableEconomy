package org.stablerpg.stableeconomy.shop.exceptions;

public class NotEnoughSpaceException extends BuyException {

  public NotEnoughSpaceException() {

  }

  public NotEnoughSpaceException(String message) {
    super(message);
  }

}
