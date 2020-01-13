package weka.core;

public abstract interface Undoable
{
  public abstract boolean isUndoEnabled();
  
  public abstract void setUndoEnabled(boolean paramBoolean);
  
  public abstract void clearUndo();
  
  public abstract boolean canUndo();
  
  public abstract void undo();
  
  public abstract void addUndoPoint();
}
