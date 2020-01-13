package weka.gui.sql.event;

import java.util.EventObject;
import javax.swing.DefaultListModel;










































public class HistoryChangedEvent
  extends EventObject
{
  private static final long serialVersionUID = 7476087315774869973L;
  protected String m_HistoryName;
  protected DefaultListModel m_History;
  
  public HistoryChangedEvent(Object source, String name, DefaultListModel history)
  {
    super(source);
    
    m_HistoryName = name;
    m_History = history;
  }
  


  public String getHistoryName()
  {
    return m_HistoryName;
  }
  


  public DefaultListModel getHistory()
  {
    return m_History;
  }
  





  public String toString()
  {
    String result = super.toString();
    result = result.substring(0, result.length() - 1);
    result = result + ",name=" + getHistoryName() + ",history=" + getHistory() + "]";
    


    return result;
  }
}
