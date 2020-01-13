package weka.gui.beans;

import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;























public class AbstractTrainAndTestSetProducerBeanInfo
  extends SimpleBeanInfo
{
  public AbstractTrainAndTestSetProducerBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(TrainingSetProducer.class, "trainingSet", TrainingSetListener.class, "acceptTrainingSet"), new EventSetDescriptor(TestSetProducer.class, "testSet", TestSetListener.class, "acceptTestSet") };




    }
    catch (Exception ex)
    {



      ex.printStackTrace();
    }
    return null;
  }
}
