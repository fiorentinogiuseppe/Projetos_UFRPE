package weka.gui.streams;

import java.util.EventObject;












































public class InstanceEvent
  extends EventObject
{
  private static final long serialVersionUID = 3207259868110667379L;
  public static final int FORMAT_AVAILABLE = 1;
  public static final int INSTANCE_AVAILABLE = 2;
  public static final int BATCH_FINISHED = 3;
  private int m_ID;
  
  public InstanceEvent(Object source, int ID)
  {
    super(source);
    m_ID = ID;
  }
  





  public int getID()
  {
    return m_ID;
  }
}
