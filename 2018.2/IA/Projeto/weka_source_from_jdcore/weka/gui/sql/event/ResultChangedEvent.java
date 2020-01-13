package weka.gui.sql.event;

import java.util.EventObject;



















































public class ResultChangedEvent
  extends EventObject
{
  private static final long serialVersionUID = 36042516077236111L;
  protected String m_Query;
  protected String m_URL;
  protected String m_User;
  protected String m_Password;
  
  public ResultChangedEvent(Object source, String url, String user, String pw, String query)
  {
    super(source);
    
    m_URL = url;
    m_User = user;
    m_Password = pw;
    m_Query = query;
  }
  


  public String getURL()
  {
    return m_URL;
  }
  


  public String getUser()
  {
    return m_User;
  }
  


  public String getPassword()
  {
    return m_Password;
  }
  


  public String getQuery()
  {
    return m_Query;
  }
  





  public String toString()
  {
    String result = super.toString();
    result = result.substring(0, result.length() - 1);
    result = result + ",url=" + getURL() + ",user=" + getUser() + ",password=" + getPassword().replaceAll(".", "*") + ",query=" + getQuery() + "]";
    




    return result;
  }
}
