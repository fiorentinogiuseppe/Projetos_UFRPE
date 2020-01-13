package weka.gui.beans;

public abstract interface Visible
{
  public abstract void useDefaultVisual();
  
  public abstract void setVisual(BeanVisual paramBeanVisual);
  
  public abstract BeanVisual getVisual();
}
