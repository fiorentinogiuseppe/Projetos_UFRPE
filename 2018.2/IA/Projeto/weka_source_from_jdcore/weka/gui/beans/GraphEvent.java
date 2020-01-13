package weka.gui.beans;

import java.util.EventObject;







































public class GraphEvent
  extends EventObject
{
  private static final long serialVersionUID = 2099494034652519986L;
  protected String m_graphString;
  protected String m_graphTitle;
  protected int m_graphType;
  
  public GraphEvent(Object source, String graphString, String graphTitle, int graphType)
  {
    super(source);
    m_graphString = graphString;
    m_graphTitle = graphTitle;
    m_graphType = graphType;
  }
  




  public String getGraphString()
  {
    return m_graphString;
  }
  




  public String getGraphTitle()
  {
    return m_graphTitle;
  }
  




  public int getGraphType()
  {
    return m_graphType;
  }
}
