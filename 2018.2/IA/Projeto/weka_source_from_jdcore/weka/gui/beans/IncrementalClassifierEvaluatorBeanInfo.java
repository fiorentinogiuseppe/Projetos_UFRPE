package weka.gui.beans;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;




























public class IncrementalClassifierEvaluatorBeanInfo
  extends SimpleBeanInfo
{
  public IncrementalClassifierEvaluatorBeanInfo() {}
  
  public PropertyDescriptor[] getPropertyDescriptors()
  {
    try
    {
      PropertyDescriptor p1 = new PropertyDescriptor("statusFrequency", IncrementalClassifierEvaluator.class);
      PropertyDescriptor p2 = new PropertyDescriptor("outputPerClassInfoRetrievalStats", IncrementalClassifierEvaluator.class);
      
      return new PropertyDescriptor[] { p1, p2 };
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
  



  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(IncrementalClassifierEvaluator.class, "chart", ChartListener.class, "acceptDataPoint"), new EventSetDescriptor(IncrementalClassifierEvaluator.class, "text", TextListener.class, "acceptText") };




    }
    catch (Exception ex)
    {




      ex.printStackTrace();
    }
    return null;
  }
  




  public BeanDescriptor getBeanDescriptor()
  {
    return new BeanDescriptor(IncrementalClassifierEvaluator.class, IncrementalClassifierEvaluatorCustomizer.class);
  }
}
