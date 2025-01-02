package me.jeremiah.economy.data.util;

public interface Dirtyable {

  boolean isDirty();
  void markClean();

}
