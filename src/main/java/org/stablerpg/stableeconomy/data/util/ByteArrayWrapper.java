package org.stablerpg.stableeconomy.data.util;

import java.util.Arrays;
import java.util.UUID;

public record ByteArrayWrapper(byte[] bytes) {

  public UUID toUUID() {
    return DataUtils.uuidFromBytes(bytes);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof ByteArrayWrapper(byte[] that))) return false;
    return Arrays.equals(bytes, that);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }

}
