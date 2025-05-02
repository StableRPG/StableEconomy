package me.jeremiah.stableeconomy.data.util;

public interface Dirtyable {

  boolean isDirty();
  void markClean();

}
