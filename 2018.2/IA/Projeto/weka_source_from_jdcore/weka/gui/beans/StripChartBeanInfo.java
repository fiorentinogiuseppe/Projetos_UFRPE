package weka.gui.beans;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;




























public class StripChartBeanInfo
  extends SimpleBeanInfo
{
  public StripChartBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    EventSetDescriptor[] esds = new EventSetDescriptor[0];
    return esds;
  }
  





  public PropertyDescriptor[] getPropertyDescriptors()
  {
    try
    {
      PropertyDescriptor p1 = new PropertyDescriptor("xLabelFreq", StripChart.class);
      PropertyDescriptor p2 = new PropertyDescriptor("refreshFreq", StripChart.class);
      return new PropertyDescriptor[] { p1, p2 };
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
  




  public BeanDescriptor getBeanDescriptor()
  {
    return new BeanDescriptor(StripChart.class, StripChartCustomizer.class);
  }
}
