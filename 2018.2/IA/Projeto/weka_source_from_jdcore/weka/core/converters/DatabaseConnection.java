package weka.core.converters;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import weka.core.RevisionUtils;
import weka.experiment.DatabaseUtils;









































public class DatabaseConnection
  extends DatabaseUtils
{
  static final long serialVersionUID = 1673169848863178695L;
  
  public DatabaseConnection()
    throws Exception
  {}
  
  public boolean getUpperCase()
  {
    return m_checkForUpperCaseNames;
  }
  




  public DatabaseMetaData getMetaData()
    throws Exception
  {
    if (!isConnected()) {
      throw new IllegalStateException("Not connected, please connect first!");
    }
    return m_Connection.getMetaData();
  }
  




  public int getUpdateCount()
    throws SQLException
  {
    if (!isConnected()) {
      throw new IllegalStateException("Not connected, please connect first!");
    }
    return m_PreparedStatement.getUpdateCount();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
}
