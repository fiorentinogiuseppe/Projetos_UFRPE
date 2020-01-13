package weka.gui.sql.event;

import java.sql.ResultSet;
import java.util.EventObject;
import weka.gui.sql.DbUtils;
























































public class QueryExecuteEvent
  extends EventObject
{
  private static final long serialVersionUID = -5556385019954730740L;
  protected DbUtils m_DbUtils;
  protected String m_Query;
  protected ResultSet m_ResultSet;
  protected Exception m_Exception;
  protected int m_MaxRows;
  
  public QueryExecuteEvent(Object source, DbUtils utils, String query, int rows, ResultSet rs, Exception ex)
  {
    super(source);
    
    m_DbUtils = utils;
    m_Query = query;
    m_MaxRows = rows;
    m_ResultSet = rs;
    m_Exception = ex;
  }
  


  public DbUtils getDbUtils()
  {
    return m_DbUtils;
  }
  


  public String getQuery()
  {
    return m_Query;
  }
  


  public int getMaxRows()
  {
    return m_MaxRows;
  }
  


  public boolean failed()
  {
    return m_Exception != null;
  }
  



  public boolean hasResult()
  {
    return m_ResultSet != null;
  }
  



  public ResultSet getResultSet()
  {
    return m_ResultSet;
  }
  


  public Exception getException()
  {
    return m_Exception;
  }
  





  public String toString()
  {
    String result = super.toString();
    result = result.substring(0, result.length() - 1);
    result = result + ",query=" + getQuery() + ",maxrows=" + getMaxRows() + ",failed=" + failed() + ",exception=" + getException() + "]";
    



    return result;
  }
}
