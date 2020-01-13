package weka.gui.beans;

import java.beans.BeanDescriptor;






























public class LoaderBeanInfo
  extends AbstractDataSourceBeanInfo
{
  public LoaderBeanInfo() {}
  
  public BeanDescriptor getBeanDescriptor()
  {
    return new BeanDescriptor(Loader.class, LoaderCustomizer.class);
  }
}
