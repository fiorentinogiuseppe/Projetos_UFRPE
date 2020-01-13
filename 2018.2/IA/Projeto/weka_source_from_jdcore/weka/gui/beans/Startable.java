package weka.gui.beans;

public abstract interface Startable
{
  public abstract void start()
    throws Exception;
  
  public abstract String getStartMessage();
}
