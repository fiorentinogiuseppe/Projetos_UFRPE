package weka.gui.beans;

import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;






























public class AbstractDataSinkBeanInfo
  extends SimpleBeanInfo
{
  public AbstractDataSinkBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    EventSetDescriptor[] esds = new EventSetDescriptor[0];
    return esds;
  }
}
