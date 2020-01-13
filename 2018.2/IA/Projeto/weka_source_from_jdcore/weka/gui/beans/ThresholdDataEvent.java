package weka.gui.beans;

import java.util.EventObject;
import weka.core.Attribute;
import weka.gui.visualize.PlotData2D;


































public class ThresholdDataEvent
  extends EventObject
{
  private static final long serialVersionUID = -8309334224492439644L;
  private PlotData2D m_dataSet;
  private Attribute m_classAttribute;
  
  public ThresholdDataEvent(Object source, PlotData2D dataSet)
  {
    this(source, dataSet, null);
  }
  
  public ThresholdDataEvent(Object source, PlotData2D dataSet, Attribute classAtt) {
    super(source);
    m_dataSet = dataSet;
    m_classAttribute = classAtt;
  }
  




  public PlotData2D getDataSet()
  {
    return m_dataSet;
  }
  





  public Attribute getClassAttribute()
  {
    return m_classAttribute;
  }
}
