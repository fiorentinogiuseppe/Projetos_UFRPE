package weka.gui.visualize;

import weka.core.FastVector;
import weka.core.Instances;


































public class VisualizePanelEvent
{
  public static int NONE = 0;
  public static int RECTANGLE = 1;
  public static int OVAL = 2;
  public static int POLYGON = 3;
  public static int LINE = 4;
  public static int VLINE = 5;
  public static int HLINE = 6;
  


  private FastVector m_values;
  


  private Instances m_inst;
  


  private Instances m_inst2;
  


  private int m_attrib1;
  

  private int m_attrib2;
  


  public VisualizePanelEvent(FastVector ar, Instances i, Instances i2, int at1, int at2)
  {
    m_values = ar;
    m_inst = i;
    m_inst2 = i2;
    m_attrib1 = at1;
    m_attrib2 = at2;
  }
  




  public FastVector getValues()
  {
    return m_values;
  }
  


  public Instances getInstances1()
  {
    return m_inst;
  }
  


  public Instances getInstances2()
  {
    return m_inst2;
  }
  


  public int getAttribute1()
  {
    return m_attrib1;
  }
  


  public int getAttribute2()
  {
    return m_attrib2;
  }
}
