package weka.gui.beans;

import java.util.EventListener;

public abstract interface ThresholdDataListener
  extends EventListener
{
  public abstract void acceptDataSet(ThresholdDataEvent paramThresholdDataEvent);
}
