package weka.gui.beans;

import java.util.EventListener;

public abstract interface VisualizableErrorListener
  extends EventListener
{
  public abstract void acceptDataSet(VisualizableErrorEvent paramVisualizableErrorEvent);
}
