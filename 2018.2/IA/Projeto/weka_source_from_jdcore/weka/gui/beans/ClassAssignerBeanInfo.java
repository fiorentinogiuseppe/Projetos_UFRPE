package weka.gui.beans;

import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;



























public class ClassAssignerBeanInfo
  extends SimpleBeanInfo
{
  public ClassAssignerBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(DataSource.class, "dataSet", DataSourceListener.class, "acceptDataSet"), new EventSetDescriptor(DataSource.class, "instance", InstanceListener.class, "acceptInstance"), new EventSetDescriptor(TrainingSetProducer.class, "trainingSet", TrainingSetListener.class, "acceptTrainingSet"), new EventSetDescriptor(TestSetProducer.class, "testSet", TestSetListener.class, "acceptTestSet") };








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
      PropertyDescriptor p1 = new PropertyDescriptor("classColumn", ClassAssigner.class);
      return new PropertyDescriptor[] { p1 };
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }
  
  public BeanDescriptor getBeanDescriptor() {
    return new BeanDescriptor(ClassAssigner.class, ClassAssignerCustomizer.class);
  }
}
