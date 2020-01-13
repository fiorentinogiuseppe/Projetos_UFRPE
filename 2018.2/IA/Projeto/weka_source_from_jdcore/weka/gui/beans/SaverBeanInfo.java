package weka.gui.beans;

import java.beans.BeanDescriptor;






























public class SaverBeanInfo
  extends AbstractDataSinkBeanInfo
{
  public SaverBeanInfo() {}
  
  public BeanDescriptor getBeanDescriptor()
  {
    return new BeanDescriptor(Saver.class, SaverCustomizer.class);
  }
}
