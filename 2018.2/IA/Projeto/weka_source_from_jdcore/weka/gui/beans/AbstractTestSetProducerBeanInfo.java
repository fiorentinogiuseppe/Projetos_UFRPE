package weka.gui.beans;

import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;























public class AbstractTestSetProducerBeanInfo
  extends SimpleBeanInfo
{
  public AbstractTestSetProducerBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(TestSetProducer.class, "testSet", TestSetListener.class, "acceptTestSet") };


    }
    catch (Exception ex)
    {


      ex.printStackTrace();
    }
    return null;
  }
}
