package me.jeremiah.economy.data.util;

import java.util.Arrays;
import java.util.UUID;

public record ByteArrayWrapper(byte[] bytes) {

  public UUID toUUID() {
    return DataUtils.uuidFromBytes(bytes);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    ByteArrayWrapper that = (ByteArrayWrapper) obj;
    return Arrays.equals(bytes, that.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }

}
