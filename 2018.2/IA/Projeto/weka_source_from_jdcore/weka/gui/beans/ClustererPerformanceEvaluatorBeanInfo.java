package weka.gui.beans;

import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;























public class ClustererPerformanceEvaluatorBeanInfo
  extends SimpleBeanInfo
{
  public ClustererPerformanceEvaluatorBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(ClustererPerformanceEvaluator.class, "text", TextListener.class, "acceptText") };


    }
    catch (Exception ex)
    {


      ex.printStackTrace();
    }
    return null;
  }
}
