package weka.gui.beans;

import java.util.EventListener;

public abstract interface DataSourceListener
  extends EventListener
{
  public abstract void acceptDataSet(DataSetEvent paramDataSetEvent);
}
