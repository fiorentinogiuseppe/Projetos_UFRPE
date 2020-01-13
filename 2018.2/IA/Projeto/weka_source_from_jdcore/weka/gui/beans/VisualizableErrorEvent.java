package weka.gui.beans;

import java.util.EventObject;
import weka.gui.visualize.PlotData2D;

































public class VisualizableErrorEvent
  extends EventObject
{
  private static final long serialVersionUID = -5811819270887223400L;
  private PlotData2D m_dataSet;
  
  public VisualizableErrorEvent(Object source, PlotData2D dataSet)
  {
    super(source);
    m_dataSet = dataSet;
  }
  




  public PlotData2D getDataSet()
  {
    return m_dataSet;
  }
}
