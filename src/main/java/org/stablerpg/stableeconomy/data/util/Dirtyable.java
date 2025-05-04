package org.stablerpg.stableeconomy.data.util;

public interface Dirtyable {

  boolean isDirty();

  void markClean();

}
