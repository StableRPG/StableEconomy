package me.jeremiah.economy.storage;

public interface Dirtyable {

  boolean isDirty();
  void markClean();

}
