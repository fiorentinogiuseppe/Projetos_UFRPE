package weka.core.converters;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.experiment.InstanceQuery;



















































































public class DatabaseLoader
  extends AbstractLoader
  implements BatchConverter, IncrementalConverter, DatabaseConverter, OptionHandler
{
  static final long serialVersionUID = -7936159015338318659L;
  protected Instances m_structure;
  private Instances m_datasetPseudoInc;
  private Instances m_oldStructure;
  private DatabaseConnection m_DataBaseConnection;
  private String m_query = "SELECT * FROM Results0";
  


  private boolean m_pseudoIncremental;
  

  private boolean m_checkForTable;
  

  private int m_nominalToStringLimit;
  

  private int m_rowCount;
  

  private int m_counter;
  

  private int m_choice;
  

  private boolean m_firstTime;
  

  private boolean m_inc;
  

  private FastVector m_orderBy;
  

  private Hashtable[] m_nominalIndexes;
  

  private FastVector[] m_nominalStrings;
  

  private String m_idColumn;
  

  protected static String PROPERTY_FILE = "weka/experiment/DatabaseUtils.props";
  

  protected static Properties PROPERTIES;
  

  protected String m_URL = null;
  

  protected String m_User = null;
  

  protected String m_Password = null;
  

  protected String m_Keys = null;
  
  static
  {
    try
    {
      PROPERTIES = Utils.readProperties(PROPERTY_FILE);
    }
    catch (Exception ex) {
      System.err.println("Problem reading properties. Fix before continuing.");
      System.err.println(ex);
    }
  }
  




  public DatabaseLoader()
    throws Exception
  {
    reset();
    m_pseudoIncremental = false;
    m_checkForTable = true;
    String props = PROPERTIES.getProperty("nominalToStringLimit");
    m_nominalToStringLimit = Integer.parseInt(props);
    m_idColumn = PROPERTIES.getProperty("idColumn");
    if (PROPERTIES.getProperty("checkForTable", "").equalsIgnoreCase("FALSE")) {
      m_checkForTable = false;
    }
  }
  




  public String globalInfo()
  {
    return "Reads Instances from a Database. Can read a database in batch or incremental mode.\nIn inremental mode MySQL and HSQLDB are supported.\nFor all other DBMS set a pseudoincremental mode is used:\nIn pseudo incremental mode the instances are read into main memory all at once and then incrementally provided to the user.\nFor incremental loading the rows in the database table have to be ordered uniquely.\nThe reason for this is that every time only a single row is fetched by extending the user query by a LIMIT clause.\nIf this extension is impossible instances will be loaded pseudoincrementally. To ensure that every row is fetched exaclty once, they have to ordered.\nTherefore a (primary) key is necessary.This approach is chosen, instead of using JDBC driver facilities, because the latter one differ betweeen different drivers.\nIf you use the DatabaseSaver and save instances by generating automatically a primary key (its name is defined in DtabaseUtils), this primary key will be used for ordering but will not be part of the output. The user defined SQL query to extract the instances should not contain LIMIT and ORDER BY clauses (see -Q option).\nIn addition, for incremental loading,  you can define in the DatabaseUtils file how many distinct values a nominal attribute is allowed to have. If this number is exceeded, the column will become a string attribute.\nIn batch mode no string attributes will be created.";
  }
  

















  public void reset()
    throws Exception
  {
    resetStructure();
    if ((m_DataBaseConnection != null) && (m_DataBaseConnection.isConnected()))
      m_DataBaseConnection.disconnectFromDatabase();
    m_DataBaseConnection = new DatabaseConnection();
    

    if (m_URL != null)
      m_DataBaseConnection.setDatabaseURL(m_URL);
    if (m_User != null)
      m_DataBaseConnection.setUsername(m_User);
    if (m_Password != null) {
      m_DataBaseConnection.setPassword(m_Password);
    }
    m_orderBy = new FastVector();
    
    if (m_Keys != null) {
      setKeys(m_Keys);
    }
    m_inc = false;
  }
  





  public void resetStructure()
  {
    m_structure = null;
    m_datasetPseudoInc = null;
    m_oldStructure = null;
    m_rowCount = 0;
    m_counter = 0;
    m_choice = 0;
    m_firstTime = true;
    setRetrieval(0);
  }
  





  public void setQuery(String q)
  {
    q = q.replaceAll("[fF][rR][oO][mM]", "FROM");
    q = q.replaceFirst("[sS][eE][lL][eE][cC][tT]", "SELECT");
    m_query = q;
  }
  




  public String getQuery()
  {
    return m_query;
  }
  





  public String queryTipText()
  {
    return "The query that should load the instances.\n The query has to be of the form SELECT <column-list>|* FROM <table> [WHERE <conditions>]";
  }
  






  public void setKeys(String keys)
  {
    m_Keys = keys;
    m_orderBy.removeAllElements();
    StringTokenizer st = new StringTokenizer(keys, ",");
    while (st.hasMoreTokens()) {
      String column = st.nextToken();
      column = column.replaceAll(" ", "");
      m_orderBy.addElement(column);
    }
  }
  





  public String getKeys()
  {
    StringBuffer key = new StringBuffer();
    for (int i = 0; i < m_orderBy.size(); i++) {
      key.append((String)m_orderBy.elementAt(i));
      if (i != m_orderBy.size() - 1)
        key.append(", ");
    }
    return key.toString();
  }
  





  public String keysTipText()
  {
    return "For incremental loading a unique identiefer has to be specified.\nIf the query includes all columns of a table (SELECT *...) a primary key\ncan be detected automatically depending on the JDBC driver. If that is not possible\nspecify the key columns here in a comma separated list.";
  }
  








  public void setUrl(String url)
  {
    m_URL = url;
    m_DataBaseConnection.setDatabaseURL(url);
  }
  






  public String getUrl()
  {
    return m_DataBaseConnection.getDatabaseURL();
  }
  





  public String urlTipText()
  {
    return "The URL of the database";
  }
  





  public void setUser(String user)
  {
    m_User = user;
    m_DataBaseConnection.setUsername(user);
  }
  





  public String getUser()
  {
    return m_DataBaseConnection.getUsername();
  }
  





  public String userTipText()
  {
    return "The user name for the database";
  }
  





  public void setPassword(String password)
  {
    m_Password = password;
    m_DataBaseConnection.setPassword(password);
  }
  




  public String getPassword()
  {
    return m_DataBaseConnection.getPassword();
  }
  





  public String passwordTipText()
  {
    return "The database password";
  }
  







  public void setSource(String url, String userName, String password)
  {
    try
    {
      m_DataBaseConnection = new DatabaseConnection();
      setUrl(url);
      setUser(userName);
      setPassword(password);
    } catch (Exception ex) {
      printException(ex);
    }
  }
  




  public void setSource(String url)
  {
    try
    {
      m_DataBaseConnection = new DatabaseConnection();
      setUrl(url);
      m_User = m_DataBaseConnection.getUsername();
      m_Password = m_DataBaseConnection.getPassword();
    } catch (Exception ex) {
      printException(ex);
    }
  }
  




  public void setSource()
    throws Exception
  {
    m_DataBaseConnection = new DatabaseConnection();
    m_URL = m_DataBaseConnection.getDatabaseURL();
    m_User = m_DataBaseConnection.getUsername();
    m_Password = m_DataBaseConnection.getPassword();
  }
  


  public void connectToDatabase()
  {
    try
    {
      if (!m_DataBaseConnection.isConnected()) {
        m_DataBaseConnection.connectToDatabase();
      }
    } catch (Exception ex) {
      printException(ex);
    }
  }
  










  private String endOfQuery(boolean onlyTableName)
  {
    int beginIndex = m_query.indexOf("FROM ") + 5;
    while (m_query.charAt(beginIndex) == ' ')
      beginIndex++;
    int endIndex = m_query.indexOf(" ", beginIndex);
    String table; String table; if ((endIndex != -1) && (onlyTableName)) {
      table = m_query.substring(beginIndex, endIndex);
    } else
      table = m_query.substring(beginIndex);
    if (m_DataBaseConnection.getUpperCase())
      table = table.toUpperCase();
    return table;
  }
  









  private boolean checkForKey()
    throws Exception
  {
    String query = m_query;
    
    query = query.replaceAll(" +", " ");
    
    if (!query.startsWith("SELECT *"))
      return false;
    m_orderBy.removeAllElements();
    if (!m_DataBaseConnection.isConnected())
      m_DataBaseConnection.connectToDatabase();
    DatabaseMetaData dmd = m_DataBaseConnection.getMetaData();
    String table = endOfQuery(true);
    

    ResultSet rs = dmd.getPrimaryKeys(null, null, table);
    while (rs.next()) {
      m_orderBy.addElement(rs.getString(4));
    }
    rs.close();
    if (m_orderBy.size() != 0) {
      return true;
    }
    rs = dmd.getBestRowIdentifier(null, null, table, 2, false);
    ResultSetMetaData rmd = rs.getMetaData();
    int help = 0;
    while (rs.next()) {
      m_orderBy.addElement(rs.getString(2));
      help++;
    }
    rs.close();
    if (help == rmd.getColumnCount()) {
      m_orderBy.removeAllElements();
    }
    if (m_orderBy.size() != 0) {
      return true;
    }
    return false;
  }
  







  private void stringToNominal(ResultSet rs, int i)
    throws Exception
  {
    while (rs.next()) {
      String str = rs.getString(1);
      if (!rs.wasNull()) {
        Double index = (Double)m_nominalIndexes[(i - 1)].get(str);
        if (index == null) {
          index = new Double(m_nominalStrings[(i - 1)].size());
          m_nominalIndexes[(i - 1)].put(str, index);
          m_nominalStrings[(i - 1)].addElement(str);
        }
      }
    }
  }
  











  private String limitQuery(String query, int offset, int choice)
  {
    StringBuffer order = new StringBuffer();
    String orderByString = "";
    
    if (m_orderBy.size() != 0) {
      order.append(" ORDER BY ");
      for (int i = 0; i < m_orderBy.size() - 1; i++) {
        if (m_DataBaseConnection.getUpperCase()) {
          order.append(((String)m_orderBy.elementAt(i)).toUpperCase());
        } else
          order.append((String)m_orderBy.elementAt(i));
        order.append(", ");
      }
      if (m_DataBaseConnection.getUpperCase()) {
        order.append(((String)m_orderBy.elementAt(m_orderBy.size() - 1)).toUpperCase());
      } else
        order.append((String)m_orderBy.elementAt(m_orderBy.size() - 1));
      orderByString = order.toString();
    }
    if (choice == 0) {
      String limitedQuery = query.replaceFirst("SELECT", "SELECT LIMIT " + offset + " 1");
      limitedQuery = limitedQuery.concat(orderByString);
      return limitedQuery;
    }
    if (choice == 1) {
      String limitedQuery = query.concat(orderByString + " LIMIT 1 OFFSET " + offset);
      return limitedQuery;
    }
    String limitedQuery = query.concat(orderByString + " LIMIT " + offset + ", 1");
    
    return limitedQuery;
  }
  





  private int getRowCount()
    throws Exception
  {
    String query = "SELECT COUNT(*) FROM " + endOfQuery(false);
    if (!m_DataBaseConnection.execute(query)) {
      throw new Exception("Cannot count results tuples.");
    }
    ResultSet rs = m_DataBaseConnection.getResultSet();
    rs.next();
    int i = rs.getInt(1);
    rs.close();
    return i;
  }
  






  public Instances getStructure()
    throws IOException
  {
    if (m_DataBaseConnection == null) {
      throw new IOException("No source database has been specified");
    }
    connectToDatabase();
    try
    {
      if ((m_pseudoIncremental) && (m_structure == null)) {
        if (getRetrieval() == 1) {
          throw new IOException("Cannot mix getting instances in both incremental and batch modes");
        }
        setRetrieval(0);
        m_datasetPseudoInc = getDataSet();
        m_structure = new Instances(m_datasetPseudoInc, 0);
        setRetrieval(0);
        return m_structure;
      }
      if (m_structure == null) {
        if ((m_checkForTable) && 
          (!m_DataBaseConnection.tableExists(endOfQuery(true)))) {
          throw new IOException("Table does not exist according to metadata from JDBC driver. If you are convinced the table exists, set 'checkForTable' to 'False' in your DatabaseUtils.props file and try again.");
        }
        



        int choice = 0;
        boolean rightChoice = false;
        while (!rightChoice) {
          try {
            if (!m_DataBaseConnection.execute(limitQuery(m_query, 0, choice))) {
              throw new IOException("Query didn't produce results");
            }
            m_choice = choice;
            rightChoice = true;
          }
          catch (SQLException ex) {
            choice++;
            if (choice == 3) {
              System.out.println("Incremental loading not supported for that DBMS. Pseudoincremental mode is used if you use incremental loading.\nAll rows are loaded into memory once and retrieved incrementally from memory instead of from the database.");
              m_pseudoIncremental = true;
              break label1383;
            }
          }
        }
        String end = endOfQuery(false);
        ResultSet rs = m_DataBaseConnection.getResultSet();
        ResultSetMetaData md = rs.getMetaData();
        
        int numAttributes = md.getColumnCount();
        int[] attributeTypes = new int[numAttributes];
        m_nominalIndexes = new Hashtable[numAttributes];
        m_nominalStrings = new FastVector[numAttributes];
        for (int i = 1; i <= numAttributes; i++) { String columnName;
          String query; ResultSet rs1; switch (m_DataBaseConnection.translateDBColumnType(md.getColumnTypeName(i)))
          {

          case 0: 
            columnName = md.getColumnLabel(i);
            if (m_DataBaseConnection.getUpperCase())
              columnName = columnName.toUpperCase();
            m_nominalIndexes[(i - 1)] = new Hashtable();
            m_nominalStrings[(i - 1)] = new FastVector();
            


            if (getRetrieval() != 2) {
              attributeTypes[(i - 1)] = 2;
            }
            else
            {
              query = "SELECT COUNT(DISTINCT( " + columnName + " )) FROM " + end;
              if (m_DataBaseConnection.execute(query) == true) {
                ResultSet rs1 = m_DataBaseConnection.getResultSet();
                rs1.next();
                int count = rs1.getInt(1);
                rs1.close();
                
                if ((count > m_nominalToStringLimit) || (!m_DataBaseConnection.execute("SELECT DISTINCT ( " + columnName + " ) FROM " + end + " ORDER BY " + columnName)))
                {





                  attributeTypes[(i - 1)] = 2;
                  continue;
                }
                rs1 = m_DataBaseConnection.getResultSet();
              }
              else
              {
                attributeTypes[(i - 1)] = 2;
                continue;
              }
              attributeTypes[(i - 1)] = 1;
              stringToNominal(rs1, i);
              rs1.close(); }
            break;
          
          case 9: 
            columnName = md.getColumnLabel(i);
            if (m_DataBaseConnection.getUpperCase())
              columnName = columnName.toUpperCase();
            m_nominalIndexes[(i - 1)] = new Hashtable();
            m_nominalStrings[(i - 1)] = new FastVector();
            


            if (getRetrieval() != 2) {
              attributeTypes[(i - 1)] = 2;
            }
            else
            {
              query = "SELECT COUNT(DISTINCT( " + columnName + " )) FROM " + end;
              if (m_DataBaseConnection.execute(query) == true) {
                rs1 = m_DataBaseConnection.getResultSet();
                stringToNominal(rs1, i);
                rs1.close();
              }
              attributeTypes[(i - 1)] = 2; }
            break;
          
          case 1: 
            attributeTypes[(i - 1)] = 1;
            m_nominalIndexes[(i - 1)] = new Hashtable();
            m_nominalIndexes[(i - 1)].put("false", new Double(0.0D));
            m_nominalIndexes[(i - 1)].put("true", new Double(1.0D));
            m_nominalStrings[(i - 1)] = new FastVector();
            m_nominalStrings[(i - 1)].addElement("false");
            m_nominalStrings[(i - 1)].addElement("true");
            break;
          
          case 2: 
            attributeTypes[(i - 1)] = 0;
            break;
          
          case 3: 
            attributeTypes[(i - 1)] = 0;
            break;
          
          case 4: 
            attributeTypes[(i - 1)] = 0;
            break;
          
          case 5: 
            attributeTypes[(i - 1)] = 0;
            break;
          
          case 6: 
            attributeTypes[(i - 1)] = 0;
            break;
          
          case 7: 
            attributeTypes[(i - 1)] = 0;
            break;
          case 8: 
            attributeTypes[(i - 1)] = 3;
            break;
          case 10: 
            attributeTypes[(i - 1)] = 3;
            break;
          
          default: 
            attributeTypes[(i - 1)] = 2; }
          
        }
        FastVector attribInfo = new FastVector();
        for (int i = 0; i < numAttributes; i++)
        {

          String attribName = md.getColumnLabel(i + 1);
          switch (attributeTypes[i]) {
          case 1: 
            attribInfo.addElement(new Attribute(attribName, m_nominalStrings[i]));
            break;
          case 0: 
            attribInfo.addElement(new Attribute(attribName));
            break;
          case 2: 
            Attribute att = new Attribute(attribName, (FastVector)null);
            for (int n = 0; n < m_nominalStrings[i].size(); n++) {
              att.addStringValue((String)m_nominalStrings[i].elementAt(n));
            }
            attribInfo.addElement(att);
            break;
          case 3: 
            attribInfo.addElement(new Attribute(attribName, (String)null));
            break;
          default: 
            throw new IOException("Unknown attribute type");
          }
        }
        m_structure = new Instances(endOfQuery(true), attribInfo, 0);
        
        if (m_DataBaseConnection.getUpperCase()) {
          m_idColumn = m_idColumn.toUpperCase();
        }
        if (m_structure.attribute(0).name().equals(m_idColumn)) {
          m_oldStructure = new Instances(m_structure, 0);
          m_oldStructure.deleteAttributeAt(0);
        }
        else
        {
          m_oldStructure = new Instances(m_structure, 0);
        }
        if (m_DataBaseConnection.getResultSet() != null) {
          rs.close();
        }
        

      }
      else if (m_oldStructure == null) {
        m_oldStructure = new Instances(m_structure, 0);
      }
      m_DataBaseConnection.disconnectFromDatabase();
    }
    catch (Exception ex) {
      ex.printStackTrace();
      printException(ex);
    }
    label1383:
    return m_oldStructure;
  }
  






  public Instances getDataSet()
    throws IOException
  {
    if (m_DataBaseConnection == null) {
      throw new IOException("No source database has been specified");
    }
    if (getRetrieval() == 2) {
      throw new IOException("Cannot mix getting Instances in both incremental and batch modes");
    }
    setRetrieval(1);
    

    Instances result = null;
    
    try
    {
      InstanceQuery iq = new InstanceQuery();
      iq.setDatabaseURL(m_URL);
      iq.setUsername(m_User);
      iq.setPassword(m_Password);
      iq.setQuery(m_query);
      
      result = iq.retrieveInstances();
      
      if (m_DataBaseConnection.getUpperCase()) {
        m_idColumn = m_idColumn.toUpperCase();
      }
      
      if (result.attribute(0).name().equals(m_idColumn)) {
        result.deleteAttributeAt(0);
      }
      
      m_structure = new Instances(result, 0);
      iq.disconnectFromDatabase();
    }
    catch (Exception ex) {
      printException(ex);
      StringBuffer text = new StringBuffer();
      if (m_query.equals("Select * from Results0")) {
        text.append("\n\nDatabaseLoader options:\n");
        Enumeration enumi = listOptions();
        
        while (enumi.hasMoreElements()) {
          Option option = (Option)enumi.nextElement();
          text.append(option.synopsis() + '\n');
          text.append(option.description() + '\n');
        }
        System.out.println(text);
      }
    }
    
    return result;
  }
  







  private Instance readInstance(ResultSet rs)
    throws Exception
  {
    ResultSetMetaData md = rs.getMetaData();
    int numAttributes = md.getColumnCount();
    double[] vals = new double[numAttributes];
    m_structure.delete();
    for (int i = 1; i <= numAttributes; i++) { String str;
      switch (m_DataBaseConnection.translateDBColumnType(md.getColumnTypeName(i))) {
      case 0: 
        str = rs.getString(i);
        if (rs.wasNull()) {
          vals[(i - 1)] = Instance.missingValue();
        } else {
          Double index = (Double)m_nominalIndexes[(i - 1)].get(str);
          if (index == null) {
            index = new Double(m_structure.attribute(i - 1).addStringValue(str));
          }
          vals[(i - 1)] = index.doubleValue();
        }
        break;
      case 9: 
        str = rs.getString(i);
        if (rs.wasNull()) {
          vals[(i - 1)] = Instance.missingValue();
        }
        else {
          Double index = (Double)m_nominalIndexes[(i - 1)].get(str);
          if (index == null) {
            index = new Double(m_structure.attribute(i - 1).addStringValue(str));
          }
          vals[(i - 1)] = index.doubleValue();
        }
        break;
      case 1: 
        boolean boo = rs.getBoolean(i);
        if (rs.wasNull()) {
          vals[(i - 1)] = Instance.missingValue();
        } else {
          vals[(i - 1)] = (boo ? 1.0D : 0.0D);
        }
        break;
      
      case 2: 
        double dd = rs.getDouble(i);
        
        if (rs.wasNull()) {
          vals[(i - 1)] = Instance.missingValue();
        }
        else {
          vals[(i - 1)] = dd;
        }
        break;
      case 3: 
        byte by = rs.getByte(i);
        if (rs.wasNull()) {
          vals[(i - 1)] = Instance.missingValue();
        } else {
          vals[(i - 1)] = by;
        }
        break;
      case 4: 
        short sh = rs.getShort(i);
        if (rs.wasNull()) {
          vals[(i - 1)] = Instance.missingValue();
        } else {
          vals[(i - 1)] = sh;
        }
        break;
      case 5: 
        int in = rs.getInt(i);
        if (rs.wasNull()) {
          vals[(i - 1)] = Instance.missingValue();
        } else {
          vals[(i - 1)] = in;
        }
        break;
      case 6: 
        long lo = rs.getLong(i);
        if (rs.wasNull()) {
          vals[(i - 1)] = Instance.missingValue();
        } else {
          vals[(i - 1)] = lo;
        }
        break;
      case 7: 
        float fl = rs.getFloat(i);
        if (rs.wasNull()) {
          vals[(i - 1)] = Instance.missingValue();
        } else {
          vals[(i - 1)] = fl;
        }
        break;
      case 8: 
        Date date = rs.getDate(i);
        if (rs.wasNull()) {
          vals[(i - 1)] = Instance.missingValue();
        }
        else {
          vals[(i - 1)] = date.getTime();
        }
        break;
      case 10: 
        Time time = rs.getTime(i);
        if (rs.wasNull()) {
          vals[(i - 1)] = Instance.missingValue();
        }
        else {
          vals[(i - 1)] = time.getTime();
        }
        break;
      default: 
        vals[(i - 1)] = Instance.missingValue();
      }
    }
    Instance inst = new Instance(1.0D, vals);
    
    if (m_DataBaseConnection.getUpperCase())
      m_idColumn = m_idColumn.toUpperCase();
    if (m_structure.attribute(0).name().equals(m_idColumn)) {
      inst.deleteAttributeAt(0);
      m_oldStructure.add(inst);
      inst = m_oldStructure.instance(0);
      m_oldStructure.delete(0);
    }
    else
    {
      m_structure.add(inst);
      inst = m_structure.instance(0);
      m_structure.delete(0);
    }
    return inst;
  }
  













  public Instance getNextInstance(Instances structure)
    throws IOException
  {
    m_structure = structure;
    
    if (m_DataBaseConnection == null)
      throw new IOException("No source database has been specified");
    if (getRetrieval() == 1) {
      throw new IOException("Cannot mix getting Instances in both incremental and batch modes");
    }
    
    if (m_pseudoIncremental) {
      setRetrieval(2);
      if (m_datasetPseudoInc.numInstances() > 0) {
        Instance current = m_datasetPseudoInc.instance(0);
        m_datasetPseudoInc.delete(0);
        return current;
      }
      
      resetStructure();
      return null;
    }
    

    setRetrieval(2);
    try {
      if (!m_DataBaseConnection.isConnected()) {
        connectToDatabase();
      }
      if ((m_firstTime) && (m_orderBy.size() == 0) && 
        (!checkForKey())) {
        throw new Exception("A unique order cannot be detected automatically.\nYou have to use SELECT * in your query to enable this feature.\nMaybe JDBC driver is not able to detect key.\nDefine primary key in your database or use -P option (command line) or enter key columns in the GUI.");
      }
      if (m_firstTime) {
        m_firstTime = false;
        m_rowCount = getRowCount();
      }
      
      if (m_counter < m_rowCount) {
        if (!m_DataBaseConnection.execute(limitQuery(m_query, m_counter, m_choice))) {
          throw new Exception("Tuple could not be retrieved.");
        }
        m_counter += 1;
        ResultSet rs = m_DataBaseConnection.getResultSet();
        rs.next();
        Instance current = readInstance(rs);
        rs.close();
        return current;
      }
      
      m_DataBaseConnection.disconnectFromDatabase();
      resetStructure();
      return null;
    }
    catch (Exception ex) {
      printException(ex);
    }
    return null;
  }
  







  public String[] getOptions()
  {
    Vector options = new Vector();
    
    if ((getUrl() != null) && (getUrl().length() != 0)) {
      options.add("-url");
      options.add(getUrl());
    }
    
    if ((getUser() != null) && (getUser().length() != 0)) {
      options.add("-user");
      options.add(getUser());
    }
    
    if ((getPassword() != null) && (getPassword().length() != 0)) {
      options.add("-password");
      options.add(getPassword());
    }
    
    options.add("-Q");
    options.add(getQuery());
    
    StringBuffer text = new StringBuffer();
    for (int i = 0; i < m_orderBy.size(); i++) {
      if (i > 0)
        text.append(", ");
      text.append((String)m_orderBy.elementAt(i));
    }
    options.add("-P");
    options.add(text.toString());
    
    if (m_inc) {
      options.add("-I");
    }
    return (String[])options.toArray(new String[options.size()]);
  }
  





  public Enumeration listOptions()
  {
    FastVector newVector = new FastVector();
    
    newVector.addElement(new Option("\tThe JDBC URL to connect to.\n\t(default: from DatabaseUtils.props file)", "url", 1, "-url <JDBC URL>"));
    



    newVector.addElement(new Option("\tThe user to connect with to the database.\n\t(default: none)", "user", 1, "-user <name>"));
    



    newVector.addElement(new Option("\tThe password to connect with to the database.\n\t(default: none)", "password", 1, "-password <password>"));
    



    newVector.addElement(new Option("\tSQL query of the form\n\t\tSELECT <list of columns>|* FROM <table> [WHERE]\n\tto execute.\n\t(default: Select * From Results0)", "Q", 1, "-Q <query>"));
    





    newVector.addElement(new Option("\tList of column names uniquely defining a DB row\n\t(separated by ', ').\n\tUsed for incremental loading.\n\tIf not specified, the key will be determined automatically,\n\tif possible with the used JDBC driver.\n\tThe auto ID column created by the DatabaseSaver won't be loaded.", "P", 1, "-P <list of column names>"));
    







    newVector.addElement(new Option("\tSets incremental loading", "I", 0, "-I"));
    


    return newVector.elements();
  }
  









































  public void setOptions(String[] options)
    throws Exception
  {
    String optionString = Utils.getOption('Q', options);
    
    String keyString = Utils.getOption('P', options);
    
    reset();
    
    String tmpStr = Utils.getOption("url", options);
    if (tmpStr.length() != 0) {
      setUrl(tmpStr);
    }
    tmpStr = Utils.getOption("user", options);
    if (tmpStr.length() != 0) {
      setUser(tmpStr);
    }
    tmpStr = Utils.getOption("password", options);
    if (tmpStr.length() != 0) {
      setPassword(tmpStr);
    }
    if (optionString.length() != 0) {
      setQuery(optionString);
    }
    m_orderBy.removeAllElements();
    
    m_inc = Utils.getFlag('I', options);
    
    if (m_inc) {
      StringTokenizer st = new StringTokenizer(keyString, ",");
      while (st.hasMoreTokens()) {
        String column = st.nextToken();
        column = column.replaceAll(" ", "");
        m_orderBy.addElement(column);
      }
    }
  }
  



  private void printException(Exception ex)
  {
    System.out.println("\n--- Exception caught ---\n");
    while (ex != null) {
      System.out.println("Message:   " + ex.getMessage());
      
      if ((ex instanceof SQLException)) {
        System.out.println("SQLState:  " + ((SQLException)ex).getSQLState());
        
        System.out.println("ErrorCode: " + ((SQLException)ex).getErrorCode());
        
        ex = ((SQLException)ex).getNextException();
      }
      else {
        ex = null; }
      System.out.println("");
    }
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11199 $");
  }
  



  public static void main(String[] options)
  {
    try
    {
      DatabaseLoader atf = new DatabaseLoader();
      atf.setOptions(options);
      atf.setSource(atf.getUrl(), atf.getUser(), atf.getPassword());
      if (!m_inc) {
        System.out.println(atf.getDataSet());
      } else {
        Instances structure = atf.getStructure();
        System.out.println(structure);
        Instance temp;
        do {
          temp = atf.getNextInstance(structure);
          if (temp != null) {
            System.out.println(temp);
          }
        } while (temp != null);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("\n" + e.getMessage());
    }
  }
}
