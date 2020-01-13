package weka.gui.beans;

public abstract interface WekaWrapper
{
  public abstract void setWrappedAlgorithm(Object paramObject);
  
  public abstract Object getWrappedAlgorithm();
}
