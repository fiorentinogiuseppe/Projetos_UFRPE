package weka.gui.beans;

import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;























public class ClassifierPerformanceEvaluatorBeanInfo
  extends SimpleBeanInfo
{
  public ClassifierPerformanceEvaluatorBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(ClassifierPerformanceEvaluator.class, "text", TextListener.class, "acceptText"), new EventSetDescriptor(ClassifierPerformanceEvaluator.class, "thresholdData", ThresholdDataListener.class, "acceptDataSet"), new EventSetDescriptor(ClassifierPerformanceEvaluator.class, "visualizableError", VisualizableErrorListener.class, "acceptDataSet") };






    }
    catch (Exception ex)
    {





      ex.printStackTrace();
    }
    return null;
  }
}
