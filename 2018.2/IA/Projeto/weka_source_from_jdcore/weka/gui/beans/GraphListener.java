package weka.gui.beans;

import java.util.EventListener;

public abstract interface GraphListener
  extends EventListener
{
  public abstract void acceptGraph(GraphEvent paramGraphEvent);
}
