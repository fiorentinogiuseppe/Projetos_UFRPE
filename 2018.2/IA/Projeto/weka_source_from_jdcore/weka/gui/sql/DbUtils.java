package weka.gui.sql;

import java.sql.Connection;
import weka.core.RevisionUtils;
import weka.experiment.DatabaseUtils;












































public class DbUtils
  extends DatabaseUtils
{
  private static final long serialVersionUID = 103748569037426479L;
  
  public DbUtils()
    throws Exception
  {}
  
  public Connection getConnection()
  {
    return m_Connection;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}
