package weka.gui.beans;

import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;































public class AbstractDataSourceBeanInfo
  extends SimpleBeanInfo
{
  public AbstractDataSourceBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(DataSource.class, "dataSet", DataSourceListener.class, "acceptDataSet"), new EventSetDescriptor(DataSource.class, "instance", InstanceListener.class, "acceptInstance") };




    }
    catch (Exception ex)
    {




      ex.printStackTrace();
    }
    return null;
  }
}
