package weka.gui.visualize;











public class AttributePanelEvent
{
  public boolean m_xChange;
  









  public boolean m_yChange;
  









  public int m_indexVal;
  










  public AttributePanelEvent(boolean xChange, boolean yChange, int indexVal)
  {
    m_xChange = xChange;
    m_yChange = yChange;
    m_indexVal = indexVal;
  }
}
