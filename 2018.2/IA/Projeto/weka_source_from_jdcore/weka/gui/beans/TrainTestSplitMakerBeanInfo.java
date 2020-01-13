package weka.gui.beans;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;

































public class TrainTestSplitMakerBeanInfo
  extends AbstractTrainAndTestSetProducerBeanInfo
{
  public TrainTestSplitMakerBeanInfo() {}
  
  public PropertyDescriptor[] getPropertyDescriptors()
  {
    try
    {
      PropertyDescriptor p1 = new PropertyDescriptor("trainPercent", TrainTestSplitMaker.class);
      PropertyDescriptor p2 = new PropertyDescriptor("seed", TrainTestSplitMaker.class);
      return new PropertyDescriptor[] { p1, p2 };
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
  




  public BeanDescriptor getBeanDescriptor()
  {
    return new BeanDescriptor(TrainTestSplitMaker.class, TrainTestSplitMakerCustomizer.class);
  }
}
