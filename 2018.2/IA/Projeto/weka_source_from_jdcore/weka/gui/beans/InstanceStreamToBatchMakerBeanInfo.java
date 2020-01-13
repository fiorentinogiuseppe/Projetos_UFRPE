package weka.gui.beans;

import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;





























public class InstanceStreamToBatchMakerBeanInfo
  extends SimpleBeanInfo
{
  public InstanceStreamToBatchMakerBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(DataSource.class, "dataSet", DataSourceListener.class, "acceptDataSet") };


    }
    catch (Exception ex)
    {

      ex.printStackTrace();
    }
    return null;
  }
}
