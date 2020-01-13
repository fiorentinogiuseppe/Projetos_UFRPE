package weka.gui.beans;

import java.util.EventObject;
import java.util.Vector;














































public class ChartEvent
  extends EventObject
{
  private static final long serialVersionUID = 7812460715499569390L;
  private Vector m_legendText;
  private double m_max;
  private double m_min;
  private boolean m_reset;
  private double[] m_dataPoint;
  
  public ChartEvent(Object source, Vector legendText, double min, double max, double[] dataPoint, boolean reset)
  {
    super(source);
    m_legendText = legendText;
    m_max = max;
    m_min = min;
    m_dataPoint = dataPoint;
    m_reset = reset;
  }
  




  public ChartEvent(Object source)
  {
    super(source);
  }
  




  public Vector getLegendText()
  {
    return m_legendText;
  }
  




  public void setLegendText(Vector lt)
  {
    m_legendText = lt;
  }
  




  public double getMin()
  {
    return m_min;
  }
  




  public void setMin(double m)
  {
    m_min = m;
  }
  




  public double getMax()
  {
    return m_max;
  }
  




  public void setMax(double m)
  {
    m_max = m;
  }
  




  public double[] getDataPoint()
  {
    return m_dataPoint;
  }
  




  public void setDataPoint(double[] dp)
  {
    m_dataPoint = dp;
  }
  




  public void setReset(boolean reset)
  {
    m_reset = reset;
  }
  




  public boolean getReset()
  {
    return m_reset;
  }
}
