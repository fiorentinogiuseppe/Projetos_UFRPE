package weka.gui.beans;

import java.util.EventObject;
import weka.core.Instances;
































public class DataSetEvent
  extends EventObject
{
  private static final long serialVersionUID = -5111218447577318057L;
  private Instances m_dataSet;
  private boolean m_structureOnly;
  
  public DataSetEvent(Object source, Instances dataSet)
  {
    super(source);
    m_dataSet = dataSet;
    if ((m_dataSet != null) && (m_dataSet.numInstances() == 0)) {
      m_structureOnly = true;
    }
  }
  




  public Instances getDataSet()
  {
    return m_dataSet;
  }
  






  public boolean isStructureOnly()
  {
    return m_structureOnly;
  }
}
