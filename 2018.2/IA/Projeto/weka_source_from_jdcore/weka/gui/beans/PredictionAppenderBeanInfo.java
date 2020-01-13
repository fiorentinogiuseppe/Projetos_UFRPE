package weka.gui.beans;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;




























public class PredictionAppenderBeanInfo
  extends SimpleBeanInfo
{
  public PredictionAppenderBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(PredictionAppender.class, "dataSet", DataSourceListener.class, "acceptDataSet"), new EventSetDescriptor(PredictionAppender.class, "instance", InstanceListener.class, "acceptInstance"), new EventSetDescriptor(PredictionAppender.class, "trainingSet", TrainingSetListener.class, "acceptTrainingSet"), new EventSetDescriptor(PredictionAppender.class, "testSet", TestSetListener.class, "acceptTestSet") };








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
      PropertyDescriptor p1 = new PropertyDescriptor("appendPredictedProbabilities", PredictionAppender.class);
      
      return new PropertyDescriptor[] { p1 };
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
  




  public BeanDescriptor getBeanDescriptor()
  {
    return new BeanDescriptor(PredictionAppender.class, PredictionAppenderCustomizer.class);
  }
}
