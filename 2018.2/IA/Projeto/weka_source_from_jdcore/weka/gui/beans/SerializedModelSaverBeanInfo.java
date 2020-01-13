package weka.gui.beans;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;





























public class SerializedModelSaverBeanInfo
  extends SimpleBeanInfo
{
  public SerializedModelSaverBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    EventSetDescriptor[] esds = new EventSetDescriptor[0];
    return esds;
  }
  




  public BeanDescriptor getBeanDescriptor()
  {
    return new BeanDescriptor(SerializedModelSaver.class, SerializedModelSaverCustomizer.class);
  }
}
