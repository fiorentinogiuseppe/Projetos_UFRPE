package weka.gui.beans;

import java.util.EventObject;
import weka.core.Instance;
import weka.core.Instances;









































public class InstanceEvent
  extends EventObject
{
  private static final long serialVersionUID = 6104920894559423946L;
  public static final int FORMAT_AVAILABLE = 0;
  public static final int INSTANCE_AVAILABLE = 1;
  public static final int BATCH_FINISHED = 2;
  private Instances m_structure;
  private Instance m_instance;
  private int m_status;
  
  public InstanceEvent(Object source, Instance instance, int status)
  {
    super(source);
    m_instance = instance;
    m_status = status;
  }
  






  public InstanceEvent(Object source, Instances structure)
  {
    super(source);
    m_structure = structure;
    m_status = 0;
  }
  
  public InstanceEvent(Object source) {
    super(source);
  }
  




  public Instance getInstance()
  {
    return m_instance;
  }
  




  public void setInstance(Instance i)
  {
    m_instance = i;
  }
  




  public int getStatus()
  {
    return m_status;
  }
  




  public void setStatus(int s)
  {
    m_status = s;
  }
  




  public void setStructure(Instances structure)
  {
    m_structure = structure;
    m_instance = null;
    m_status = 0;
  }
  





  public Instances getStructure()
  {
    return m_structure;
  }
}
