package weka.gui.beans;

import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;




























public class AbstractTrainingSetProducerBeanInfo
  extends SimpleBeanInfo
{
  public AbstractTrainingSetProducerBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(TrainingSetProducer.class, "trainingSet", TrainingSetListener.class, "acceptTrainingSet") };


    }
    catch (Exception ex)
    {


      ex.printStackTrace();
    }
    return null;
  }
}
