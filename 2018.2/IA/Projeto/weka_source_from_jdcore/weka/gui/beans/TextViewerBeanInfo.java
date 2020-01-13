package weka.gui.beans;

import java.beans.EventSetDescriptor;
import java.beans.SimpleBeanInfo;




























public class TextViewerBeanInfo
  extends SimpleBeanInfo
{
  public TextViewerBeanInfo() {}
  
  public EventSetDescriptor[] getEventSetDescriptors()
  {
    try
    {
      return new EventSetDescriptor[] { new EventSetDescriptor(TextViewer.class, "text", TextListener.class, "acceptText") };


    }
    catch (Exception ex)
    {


      ex.printStackTrace();
    }
    return null;
  }
}
