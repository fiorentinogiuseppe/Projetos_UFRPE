package weka.gui.sql.event;

import java.util.EventObject;
import weka.gui.sql.DbUtils;
















































public class ConnectionEvent
  extends EventObject
{
  private static final long serialVersionUID = 5420308930427835037L;
  public static final int CONNECT = 0;
  public static final int DISCONNECT = 1;
  protected int m_Type;
  protected DbUtils m_DbUtils;
  protected Exception m_Exception;
  
  public ConnectionEvent(Object source, int type, DbUtils utils)
  {
    this(source, type, utils, null);
  }
  







  public ConnectionEvent(Object source, int type, DbUtils utils, Exception ex)
  {
    super(source);
    
    m_Type = type;
    m_DbUtils = utils;
    m_Exception = ex;
  }
  





  public int getType()
  {
    return m_Type;
  }
  



  public boolean failed()
  {
    return getException() != null;
  }
  



  public boolean isConnected()
  {
    return m_DbUtils.isConnected();
  }
  


  public Exception getException()
  {
    return m_Exception;
  }
  




  public DbUtils getDbUtils()
  {
    return m_DbUtils;
  }
  





  public String toString()
  {
    String result = super.toString();
    result = result.substring(0, result.length() - 1);
    result = result + ",url=" + m_DbUtils.getDatabaseURL() + ",user=" + m_DbUtils.getUsername() + ",password=" + m_DbUtils.getPassword().replaceAll(".", "*") + ",connected=" + isConnected() + ",exception=" + getException() + "]";
    





    return result;
  }
}
