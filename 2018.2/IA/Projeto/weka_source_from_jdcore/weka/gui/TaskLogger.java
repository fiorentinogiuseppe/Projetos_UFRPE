package weka.gui;

public abstract interface TaskLogger
{
  public abstract void taskStarted();
  
  public abstract void taskFinished();
}
