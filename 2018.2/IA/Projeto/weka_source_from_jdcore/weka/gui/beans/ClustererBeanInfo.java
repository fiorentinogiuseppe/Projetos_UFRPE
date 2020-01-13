package weka.gui.beans;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;






















public class ClustererBeanInfo
  extends SimpleBeanInfo
{
  public ClustererBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(Clusterer.class, "batchClusterer", BatchClustererListener.class, "acceptClusterer"), new EventSetDescriptor(Clusterer.class, "graph", GraphListener.class, "acceptGraph"), new EventSetDescriptor(Clusterer.class, "text", TextListener.class, "acceptText") };






    }
    catch (Exception ex)
    {






      ex.printStackTrace();
    }
    return null;
  }
  




  public BeanDescriptor getBeanDescriptor()
  {
    return new BeanDescriptor(Clusterer.class, ClustererCustomizer.class);
  }
}
