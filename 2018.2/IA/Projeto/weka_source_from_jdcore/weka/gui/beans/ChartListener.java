package weka.gui.beans;

import java.util.EventListener;

public abstract interface ChartListener
  extends EventListener
{
  public abstract void acceptDataPoint(ChartEvent paramChartEvent);
}
