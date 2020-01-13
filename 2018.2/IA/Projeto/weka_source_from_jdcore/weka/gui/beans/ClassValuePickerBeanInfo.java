package weka.gui.beans;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;



























public class ClassValuePickerBeanInfo
  extends SimpleBeanInfo
{
  public ClassValuePickerBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(ClassValuePicker.class, "dataSet", DataSourceListener.class, "acceptDataSet") };


    }
    catch (Exception ex)
    {

      ex.printStackTrace();
    }
    return null;
  }
  




  public PropertyDescriptor[] getPropertyDescriptors()
  {
    try
    {
      PropertyDescriptor p1 = new PropertyDescriptor("classValue", ClassValuePicker.class);
      return new PropertyDescriptor[] { p1 };
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
  
  public BeanDescriptor getBeanDescriptor() {
    return new BeanDescriptor(ClassValuePicker.class, ClassValuePickerCustomizer.class);
  }
}
