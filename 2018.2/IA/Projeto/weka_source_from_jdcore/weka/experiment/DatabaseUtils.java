package weka.experiment;

import java.io.PrintStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;























































public class DatabaseUtils
  implements Serializable, RevisionHandler
{
  static final long serialVersionUID = -8252351994547116729L;
  public static final String EXP_INDEX_TABLE = "Experiment_index";
  public static final String EXP_TYPE_COL = "Experiment_type";
  public static final String EXP_SETUP_COL = "Experiment_setup";
  public static final String EXP_RESULT_COL = "Result_table";
  public static final String EXP_RESULT_PREFIX = "Results";
  public static final String PROPERTY_FILE = "weka/experiment/DatabaseUtils.props";
  protected Vector DRIVERS = new Vector();
  

  protected static Vector DRIVERS_ERRORS;
  

  protected Properties PROPERTIES;
  

  public static final int STRING = 0;
  

  public static final int BOOL = 1;
  

  public static final int DOUBLE = 2;
  

  public static final int BYTE = 3;
  

  public static final int SHORT = 4;
  

  public static final int INTEGER = 5;
  
  public static final int LONG = 6;
  
  public static final int FLOAT = 7;
  
  public static final int DATE = 8;
  
  public static final int TEXT = 9;
  
  public static final int TIME = 10;
  
  public static final int TIMESTAMP = 11;
  
  protected String m_DatabaseURL;
  
  protected transient PreparedStatement m_PreparedStatement;
  
  protected transient Connection m_Connection;
  
  protected boolean m_Debug = false;
  

  protected String m_userName = "";
  

  protected String m_password = "";
  


  protected String m_stringType = "LONGVARCHAR";
  
  protected String m_intType = "INT";
  
  protected String m_doubleType = "DOUBLE";
  

  protected boolean m_checkForUpperCaseNames = false;
  

  protected boolean m_checkForLowerCaseNames = false;
  

  protected boolean m_setAutoCommit = true;
  

  protected boolean m_createIndex = false;
  

  protected HashSet<String> m_Keywords = new HashSet();
  

  protected String m_KeywordsMaskChar = "_";
  



  public DatabaseUtils()
    throws Exception
  {
    if (DRIVERS_ERRORS == null) {
      DRIVERS_ERRORS = new Vector();
    }
    try
    {
      PROPERTIES = Utils.readProperties("weka/experiment/DatabaseUtils.props");
      

      String drivers = PROPERTIES.getProperty("jdbcDriver", "jdbc.idbDriver");
      
      if (drivers == null) {
        throw new Exception("No database drivers (JDBC) specified");
      }
      

      StringTokenizer st = new StringTokenizer(drivers, ", ");
      while (st.hasMoreTokens()) {
        String driver = st.nextToken();
        boolean result;
        try {
          Class.forName(driver);
          DRIVERS.addElement(driver);
          result = true;
        } catch (Exception e) {
          result = false;
        }
        if ((m_Debug) || ((!result) && (!DRIVERS_ERRORS.contains(driver)))) {
          System.err.println("Trying to add database driver (JDBC): " + driver + " - " + (result ? "Success!" : "Error, not in CLASSPATH?"));
        }
        
        if (!result) {
          DRIVERS_ERRORS.add(driver);
        }
      }
    } catch (Exception ex) {
      System.err.println("Problem reading properties. Fix before continuing.");
      System.err.println(ex);
    }
    
    m_DatabaseURL = PROPERTIES.getProperty("jdbcURL", "jdbc:idb=experiments.prp");
    
    m_stringType = PROPERTIES.getProperty("CREATE_STRING", "LONGVARCHAR");
    m_intType = PROPERTIES.getProperty("CREATE_INT", "INT");
    m_doubleType = PROPERTIES.getProperty("CREATE_DOUBLE", "DOUBLE");
    m_checkForUpperCaseNames = PROPERTIES.getProperty("checkUpperCaseNames", "false").equals("true");
    
    m_checkForLowerCaseNames = PROPERTIES.getProperty("checkLowerCaseNames", "false").equals("true");
    
    m_setAutoCommit = PROPERTIES.getProperty("setAutoCommit", "true").equals("true");
    
    m_createIndex = PROPERTIES.getProperty("createIndex", "false").equals("true");
    
    setKeywords(PROPERTIES.getProperty("Keywords", "AND,ASC,BY,DESC,FROM,GROUP,INSERT,ORDER,SELECT,UPDATE,WHERE"));
    
    setKeywordsMaskChar(PROPERTIES.getProperty("KeywordsMaskChar", "_"));
  }
  






  protected String attributeCaseFix(String columnName)
  {
    if (m_checkForUpperCaseNames) {
      String ucname = columnName.toUpperCase();
      if (ucname.equals("Experiment_type".toUpperCase()))
        return "Experiment_type";
      if (ucname.equals("Experiment_setup".toUpperCase()))
        return "Experiment_setup";
      if (ucname.equals("Result_table".toUpperCase())) {
        return "Result_table";
      }
      return columnName;
    }
    if (m_checkForLowerCaseNames) {
      String ucname = columnName.toLowerCase();
      if (ucname.equals("Experiment_type".toLowerCase()))
        return "Experiment_type";
      if (ucname.equals("Experiment_setup".toLowerCase()))
        return "Experiment_setup";
      if (ucname.equals("Result_table".toLowerCase())) {
        return "Result_table";
      }
      return columnName;
    }
    
    return columnName;
  }
  















  public int translateDBColumnType(String type)
  {
    try
    {
      String value = PROPERTIES.getProperty(type);
      String typeUnderscore = type.replaceAll(" ", "_");
      if (value == null) {
        value = PROPERTIES.getProperty(typeUnderscore);
      }
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Unknown data type: " + type + ". " + "Add entry in " + "weka/experiment/DatabaseUtils.props" + ".\n" + "If the type contains blanks, either escape them with a backslash " + "or use underscores instead of blanks.");
    }
  }
  









  public static String arrayToString(Object[] array)
  {
    String result = "";
    if (array == null) {
      result = "<null>";
    } else {
      for (Object element : array) {
        if (element == null) {
          result = result + " ?";
        } else {
          result = result + " " + element;
        }
      }
    }
    return result;
  }
  





  public static String typeName(int type)
  {
    switch (type) {
    case -5: 
      return "BIGINT ";
    case -2: 
      return "BINARY";
    case -7: 
      return "BIT";
    case 1: 
      return "CHAR";
    case 91: 
      return "DATE";
    case 3: 
      return "DECIMAL";
    case 8: 
      return "DOUBLE";
    case 6: 
      return "FLOAT";
    case 4: 
      return "INTEGER";
    case -4: 
      return "LONGVARBINARY";
    case -1: 
      return "LONGVARCHAR";
    case 0: 
      return "NULL";
    case 2: 
      return "NUMERIC";
    case 1111: 
      return "OTHER";
    case 7: 
      return "REAL";
    case 5: 
      return "SMALLINT";
    case 92: 
      return "TIME";
    case 93: 
      return "TIMESTAMP";
    case -6: 
      return "TINYINT";
    case -3: 
      return "VARBINARY";
    case 12: 
      return "VARCHAR";
    }
    return "Unknown";
  }
  






  public String databaseURLTipText()
  {
    return "Set the URL to the database.";
  }
  




  public String getDatabaseURL()
  {
    return m_DatabaseURL;
  }
  




  public void setDatabaseURL(String newDatabaseURL)
  {
    m_DatabaseURL = newDatabaseURL;
  }
  





  public String debugTipText()
  {
    return "Whether debug information is printed.";
  }
  





  public void setDebug(boolean d)
  {
    m_Debug = d;
  }
  





  public boolean getDebug()
  {
    return m_Debug;
  }
  





  public String usernameTipText()
  {
    return "The user to use for connecting to the database.";
  }
  




  public void setUsername(String username)
  {
    m_userName = username;
  }
  




  public String getUsername()
  {
    return m_userName;
  }
  





  public String passwordTipText()
  {
    return "The password to use for connecting to the database.";
  }
  




  public void setPassword(String password)
  {
    m_password = password;
  }
  




  public String getPassword()
  {
    return m_password;
  }
  



  public void connectToDatabase()
    throws Exception
  {
    if (m_Debug) {
      System.err.println("Connecting to " + m_DatabaseURL);
    }
    if (m_Connection == null) {
      if (m_userName.equals("")) {
        try {
          m_Connection = DriverManager.getConnection(m_DatabaseURL);
        }
        catch (SQLException e)
        {
          for (int i = 0; i < DRIVERS.size(); i++) {
            try {
              Class.forName((String)DRIVERS.elementAt(i));
            }
            catch (Exception ex) {}
          }
          
          m_Connection = DriverManager.getConnection(m_DatabaseURL);
        }
      } else {
        try {
          m_Connection = DriverManager.getConnection(m_DatabaseURL, m_userName, m_password);

        }
        catch (SQLException e)
        {
          for (int i = 0; i < DRIVERS.size(); i++) {
            try {
              Class.forName((String)DRIVERS.elementAt(i));
            }
            catch (Exception ex) {}
          }
          
          m_Connection = DriverManager.getConnection(m_DatabaseURL, m_userName, m_password);
        }
      }
    }
    
    m_Connection.setAutoCommit(m_setAutoCommit);
  }
  



  public void disconnectFromDatabase()
    throws Exception
  {
    if (m_Debug) {
      System.err.println("Disconnecting from " + m_DatabaseURL);
    }
    if (m_Connection != null) {
      m_Connection.close();
      m_Connection = null;
    }
  }
  




  public boolean isConnected()
  {
    return m_Connection != null;
  }
  











  public boolean isCursorScrollSensitive()
  {
    boolean result = false;
    try
    {
      if (isConnected()) {
        result = m_Connection.getMetaData().supportsResultSetConcurrency(1005, 1007);
      }
    }
    catch (Exception e) {}
    


    return result;
  }
  






  public boolean isCursorScrollable()
  {
    return getSupportedCursorScrollType() != -1;
  }
  











  public int getSupportedCursorScrollType()
  {
    int result = -1;
    try
    {
      if (isConnected()) {
        if (m_Connection.getMetaData().supportsResultSetConcurrency(1005, 1007))
        {
          result = 1005;
        }
        
        if ((result == -1) && 
          (m_Connection.getMetaData().supportsResultSetConcurrency(1004, 1007)))
        {
          result = 1004;
        }
      }
    }
    catch (Exception e) {}
    


    return result;
  }
  







  public boolean execute(String query)
    throws SQLException
  {
    if (!isConnected()) {
      throw new IllegalStateException("Not connected, please connect first!");
    }
    
    if (!isCursorScrollable()) {
      m_PreparedStatement = m_Connection.prepareStatement(query, 1003, 1007);
    }
    else {
      m_PreparedStatement = m_Connection.prepareStatement(query, getSupportedCursorScrollType(), 1007);
    }
    

    return m_PreparedStatement.execute();
  }
  







  public ResultSet getResultSet()
    throws SQLException
  {
    if (m_PreparedStatement != null) {
      return m_PreparedStatement.getResultSet();
    }
    return null;
  }
  






  public int update(String query)
    throws SQLException
  {
    if (!isConnected()) {
      throw new IllegalStateException("Not connected, please connect first!");
    }
    Statement statement;
    Statement statement;
    if (!isCursorScrollable()) {
      statement = m_Connection.createStatement(1003, 1007);
    }
    else {
      statement = m_Connection.createStatement(getSupportedCursorScrollType(), 1007);
    }
    
    int result = statement.executeUpdate(query);
    statement.close();
    
    return result;
  }
  






  public ResultSet select(String query)
    throws SQLException
  {
    if (!isConnected()) {
      throw new IllegalStateException("Not connected, please connect first!");
    }
    Statement statement;
    Statement statement;
    if (!isCursorScrollable()) {
      statement = m_Connection.createStatement(1003, 1007);
    }
    else {
      statement = m_Connection.createStatement(getSupportedCursorScrollType(), 1007);
    }
    
    ResultSet result = statement.executeQuery(query);
    
    return result;
  }
  





  public void close(ResultSet rs)
  {
    try
    {
      Statement statement = rs.getStatement();
      rs.close();
      statement.close();
      statement = null;
      rs = null;
    }
    catch (Exception e) {}
  }
  



  public void close()
  {
    if (m_PreparedStatement != null) {
      try {
        m_PreparedStatement.close();
        m_PreparedStatement = null;
      }
      catch (Exception e) {}
    }
  }
  






  public boolean tableExists(String tableName)
    throws Exception
  {
    if (!isConnected()) {
      throw new IllegalStateException("Not connected, please connect first!");
    }
    
    if (m_Debug) {
      System.err.println("Checking if table " + tableName + " exists...");
    }
    DatabaseMetaData dbmd = m_Connection.getMetaData();
    ResultSet rs;
    ResultSet rs; if (m_checkForUpperCaseNames) {
      rs = dbmd.getTables(null, null, tableName.toUpperCase(), null); } else { ResultSet rs;
      if (m_checkForLowerCaseNames) {
        rs = dbmd.getTables(null, null, tableName.toLowerCase(), null);
      } else
        rs = dbmd.getTables(null, null, tableName, null);
    }
    boolean tableExists = rs.next();
    if (rs.next()) {
      throw new Exception("This table seems to exist more than once!");
    }
    rs.close();
    if (m_Debug) {
      if (tableExists) {
        System.err.println("... " + tableName + " exists");
      } else {
        System.err.println("... " + tableName + " does not exist");
      }
    }
    return tableExists;
  }
  






  public static String processKeyString(String s)
  {
    return s.replaceAll("\\\\", "/").replaceAll("'", "''");
  }
  










  protected boolean isKeyInTable(String tableName, ResultProducer rp, Object[] key)
    throws Exception
  {
    String query = "SELECT Key_Run FROM " + tableName;
    String[] keyNames = rp.getKeyNames();
    if (keyNames.length != key.length) {
      throw new Exception("Key names and key values of different lengths");
    }
    boolean first = true;
    for (int i = 0; i < key.length; i++) {
      if (key[i] != null) {
        if (first) {
          query = query + " WHERE ";
          first = false;
        } else {
          query = query + " AND ";
        }
        query = query + "Key_" + keyNames[i] + '=';
        if ((key[i] instanceof String)) {
          query = query + "'" + processKeyString(key[i].toString()) + "'";
        } else {
          query = query + key[i].toString();
        }
      }
    }
    boolean retval = false;
    ResultSet rs = select(query);
    if (rs.next()) {
      retval = true;
      if (rs.next()) {
        throw new Exception("More than one result entry for result key: " + query);
      }
    }
    
    close(rs);
    return retval;
  }
  










  public Object[] getResultFromTable(String tableName, ResultProducer rp, Object[] key)
    throws Exception
  {
    String query = "SELECT ";
    String[] resultNames = rp.getResultNames();
    for (int i = 0; i < resultNames.length; i++) {
      if (i != 0) {
        query = query + ", ";
      }
      query = query + resultNames[i];
    }
    query = query + " FROM " + tableName;
    String[] keyNames = rp.getKeyNames();
    if (keyNames.length != key.length) {
      throw new Exception("Key names and key values of different lengths");
    }
    boolean first = true;
    for (int i = 0; i < key.length; i++) {
      if (key[i] != null) {
        if (first) {
          query = query + " WHERE ";
          first = false;
        } else {
          query = query + " AND ";
        }
        query = query + "Key_" + keyNames[i] + '=';
        if ((key[i] instanceof String)) {
          query = query + "'" + processKeyString(key[i].toString()) + "'";
        } else {
          query = query + key[i].toString();
        }
      }
    }
    ResultSet rs = select(query);
    ResultSetMetaData md = rs.getMetaData();
    int numAttributes = md.getColumnCount();
    if (!rs.next()) {
      throw new Exception("No result for query: " + query);
    }
    
    Object[] result = new Object[numAttributes];
    for (int i = 1; i <= numAttributes; i++) {
      switch (translateDBColumnType(md.getColumnTypeName(i))) {
      case 0: 
        result[(i - 1)] = rs.getString(i);
        if (rs.wasNull()) {
          result[(i - 1)] = null;
        }
        break;
      case 2: 
      case 7: 
        result[(i - 1)] = new Double(rs.getDouble(i));
        if (rs.wasNull()) {
          result[(i - 1)] = null;
        }
        break;
      default: 
        throw new Exception("Unhandled SQL result type (field " + (i + 1) + "): " + typeName(md.getColumnType(i)));
      }
      
    }
    if (rs.next()) {
      throw new Exception("More than one result entry for result key: " + query);
    }
    
    close(rs);
    return result;
  }
  










  public void putResultInTable(String tableName, ResultProducer rp, Object[] key, Object[] result)
    throws Exception
  {
    String query = "INSERT INTO " + tableName + " VALUES ( ";
    
    for (int i = 0; i < key.length; i++) {
      if (i != 0) {
        query = query + ',';
      }
      if (key[i] != null) {
        if ((key[i] instanceof String)) {
          query = query + "'" + processKeyString(key[i].toString()) + "'";
        } else if ((key[i] instanceof Double)) {
          query = query + safeDoubleToString((Double)key[i]);
        } else {
          query = query + key[i].toString();
        }
      } else {
        query = query + "NULL";
      }
    }
    for (Object element : result) {
      query = query + ',';
      if (element != null) {
        if ((element instanceof String)) {
          query = query + "'" + element.toString() + "'";
        } else if ((element instanceof Double)) {
          query = query + safeDoubleToString((Double)element);
        } else {
          query = query + element.toString();
        }
        
      }
      else {
        query = query + "NULL";
      }
    }
    query = query + ')';
    
    if (m_Debug) {
      System.err.println("Submitting result: " + query);
    }
    update(query);
    close();
  }
  







  private String safeDoubleToString(Double number)
  {
    if (number.isNaN()) {
      return "NULL";
    }
    
    String orig = number.toString();
    
    int pos = orig.indexOf('E');
    if ((pos == -1) || (orig.charAt(pos + 1) == '-')) {
      return orig;
    }
    StringBuffer buff = new StringBuffer(orig);
    buff.insert(pos + 1, '+');
    return new String(buff);
  }
  





  public boolean experimentIndexExists()
    throws Exception
  {
    return tableExists("Experiment_index");
  }
  



  public void createExperimentIndex()
    throws Exception
  {
    if (m_Debug) {
      System.err.println("Creating experiment index table...");
    }
    













    String query = "CREATE TABLE Experiment_index ( Experiment_type " + m_stringType + "," + "  " + "Experiment_setup" + " " + m_stringType + "," + "  " + "Result_table" + " " + m_intType + " )";
    





    update(query);
    close();
  }
  






  public String createExperimentIndexEntry(ResultProducer rp)
    throws Exception
  {
    if (m_Debug) {
      System.err.println("Creating experiment index entry...");
    }
    

    int numRows = 0;
    











    String query = "SELECT COUNT(*) FROM Experiment_index";
    ResultSet rs = select(query);
    if (m_Debug) {
      System.err.println("...getting number of rows");
    }
    if (rs.next()) {
      numRows = rs.getInt(1);
    }
    close(rs);
    

    String expType = rp.getClass().getName();
    String expParams = rp.getCompatibilityState();
    query = "INSERT INTO Experiment_index VALUES ('" + expType + "', '" + expParams + "', " + numRows + " )";
    
    if ((update(query) > 0) && 
      (m_Debug)) {
      System.err.println("...create returned resultset");
    }
    
    close();
    








    if (!m_setAutoCommit) {
      m_Connection.commit();
      m_Connection.setAutoCommit(true);
    }
    

    String tableName = getResultsTableName(rp);
    if (tableName == null) {
      throw new Exception("Problem adding experiment index entry");
    }
    


    try
    {
      query = "DROP TABLE " + tableName;
      if (m_Debug) {
        System.err.println(query);
      }
      update(query);
    } catch (SQLException ex) {
      System.err.println(ex.getMessage());
    }
    return tableName;
  }
  








  public String getResultsTableName(ResultProducer rp)
    throws Exception
  {
    if (m_Debug) {
      System.err.println("Getting results table name...");
    }
    String expType = rp.getClass().getName();
    String expParams = rp.getCompatibilityState();
    String query = "SELECT Result_table FROM Experiment_index WHERE Experiment_type='" + expType + "' AND " + "Experiment_setup" + "='" + expParams + "'";
    

    String tableName = null;
    ResultSet rs = select(query);
    if (rs.next()) {
      tableName = rs.getString(1);
      if (rs.next()) {
        throw new Exception("More than one index entry for experiment config: " + query);
      }
    }
    
    close(rs);
    if (m_Debug) {
      System.err.println("...results table = " + (tableName == null ? "<null>" : new StringBuilder().append("Results").append(tableName).toString()));
    }
    
    return "Results" + tableName;
  }
  








  public String createResultsTable(ResultProducer rp, String tableName)
    throws Exception
  {
    if (m_Debug) {
      System.err.println("Creating results table " + tableName + "...");
    }
    String query = "CREATE TABLE " + tableName + " ( ";
    
    String[] names = rp.getKeyNames();
    Object[] types = rp.getKeyTypes();
    if (names.length != types.length) {
      throw new Exception("key names types differ in length");
    }
    for (int i = 0; i < names.length; i++) {
      query = query + "Key_" + names[i] + " ";
      if ((types[i] instanceof Double)) {
        query = query + m_doubleType;
      } else if ((types[i] instanceof String))
      {










        query = query + m_stringType + " ";
      }
      else {
        throw new Exception("Unknown/unsupported field type in key");
      }
      query = query + ", ";
    }
    
    names = rp.getResultNames();
    types = rp.getResultTypes();
    if (names.length != types.length) {
      throw new Exception("result names and types differ in length");
    }
    for (int i = 0; i < names.length; i++) {
      query = query + names[i] + " ";
      if ((types[i] instanceof Double)) {
        query = query + m_doubleType;
      } else if ((types[i] instanceof String))
      {










        query = query + m_stringType + " ";
      }
      else {
        throw new Exception("Unknown/unsupported field type in key");
      }
      if (i < names.length - 1) {
        query = query + ", ";
      }
    }
    query = query + " )";
    
    update(query);
    if (m_Debug) {
      System.err.println("table created");
    }
    close();
    
    if (m_createIndex) {
      query = "CREATE UNIQUE INDEX Key_IDX ON " + tableName + " (";
      
      String[] keyNames = rp.getKeyNames();
      
      boolean first = true;
      for (String keyName : keyNames) {
        if (keyName != null) {
          if (first) {
            first = false;
            query = query + "Key_" + keyName;
          } else {
            query = query + ",Key_" + keyName;
          }
        }
      }
      query = query + ")";
      
      update(query);
    }
    return tableName;
  }
  







  public void setKeywords(String value)
  {
    m_Keywords.clear();
    
    String[] keywords = value.replaceAll(" ", "").split(",");
    for (int i = 0; i < keywords.length; i++) {
      m_Keywords.add(keywords[i].toUpperCase());
    }
  }
  








  public String getKeywords()
  {
    Vector<String> list = new Vector(m_Keywords);
    Collections.sort(list);
    
    String result = "";
    for (int i = 0; i < list.size(); i++) {
      if (i > 0) {
        result = result + ",";
      }
      result = result + (String)list.get(i);
    }
    
    return result;
  }
  





  public void setKeywordsMaskChar(String value)
  {
    m_KeywordsMaskChar = value;
  }
  




  public String getKeywordsMaskChar()
  {
    return m_KeywordsMaskChar;
  }
  






  public boolean isKeyword(String s)
  {
    return m_Keywords.contains(s.toUpperCase());
  }
  








  public String maskKeyword(String s)
  {
    if (isKeyword(s)) {
      return s + m_KeywordsMaskChar;
    }
    return s;
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11887 $");
  }
}
