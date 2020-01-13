package weka.gui.beans;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;






















public class AssociatorBeanInfo
  extends SimpleBeanInfo
{
  public AssociatorBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(Associator.class, "text", TextListener.class, "acceptText"), new EventSetDescriptor(Associator.class, "graph", GraphListener.class, "acceptGraph") };




    }
    catch (Exception ex)
    {




      ex.printStackTrace();
    }
    return null;
  }
  




  public BeanDescriptor getBeanDescriptor()
  {
    return new BeanDescriptor(Associator.class, AssociatorCustomizer.class);
  }
}
