package weka.gui.beans;

import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;




























public class DataVisualizerBeanInfo
  extends SimpleBeanInfo
{
  public DataVisualizerBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(DataVisualizer.class, "dataSet", DataSourceListener.class, "acceptDataSet") };


    }
    catch (Exception ex)
    {


      ex.printStackTrace();
    }
    return null;
  }
}
