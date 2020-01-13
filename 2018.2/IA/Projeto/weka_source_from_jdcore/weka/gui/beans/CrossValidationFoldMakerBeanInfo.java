package weka.gui.beans;

import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;































public class CrossValidationFoldMakerBeanInfo
  extends AbstractTrainAndTestSetProducerBeanInfo
{
  public CrossValidationFoldMakerBeanInfo() {}
  
  public PropertyDescriptor[] getPropertyDescriptors()
  {
    try
    {
      PropertyDescriptor p1 = new PropertyDescriptor("folds", CrossValidationFoldMaker.class);
      PropertyDescriptor p2 = new PropertyDescriptor("seed", CrossValidationFoldMaker.class);
      return new PropertyDescriptor[] { p1, p2 };
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
  




  public BeanDescriptor getBeanDescriptor()
  {
    return new BeanDescriptor(CrossValidationFoldMaker.class, CrossValidationFoldMakerCustomizer.class);
  }
}
