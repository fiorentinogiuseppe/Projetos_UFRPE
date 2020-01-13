package weka.core.converters;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;




























































































public class DatabaseSaver
  extends AbstractSaver
  implements BatchConverter, IncrementalConverter, DatabaseConverter, OptionHandler
{
  static final long serialVersionUID = 863971733782624956L;
  private DatabaseConnection m_DataBaseConnection;
  private String m_tableName;
  private String m_inputFile;
  private String m_createText;
  private String m_createDouble;
  private String m_createInt;
  private String m_createDate;
  private SimpleDateFormat m_DateFormat;
  private String m_idColumn;
  private int m_count;
  private boolean m_id;
  private boolean m_tabName;
  private String m_Username;
  private String m_Password;
  protected static String PROPERTY_FILE = "weka/experiment/DatabaseUtils.props";
  
  protected static Properties PROPERTIES;
  

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
  




  public DatabaseSaver()
    throws Exception
  {
    resetOptions();
    m_createText = PROPERTIES.getProperty("CREATE_STRING");
    m_createDouble = PROPERTIES.getProperty("CREATE_DOUBLE");
    m_createInt = PROPERTIES.getProperty("CREATE_INT");
    m_createDate = PROPERTIES.getProperty("CREATE_DATE", "DATETIME");
    m_DateFormat = new SimpleDateFormat(PROPERTIES.getProperty("DateFormat", "yyyy-MM-dd HH:mm:ss"));
    m_idColumn = PROPERTIES.getProperty("idColumn");
  }
  



  public void resetOptions()
  {
    super.resetOptions();
    setRetrieval(0);
    m_tableName = "";
    m_Username = "";
    m_Password = "";
    m_count = 1;
    m_id = false;
    m_tabName = true;
    try {
      if ((m_DataBaseConnection != null) && (m_DataBaseConnection.isConnected()))
        m_DataBaseConnection.disconnectFromDatabase();
      m_DataBaseConnection = new DatabaseConnection();
    } catch (Exception ex) {
      printException(ex);
    }
  }
  




  public void cancel()
  {
    if (getWriteMode() == 2) {
      try {
        m_DataBaseConnection.update("DROP TABLE " + m_tableName);
        if (m_DataBaseConnection.tableExists(m_tableName))
          System.err.println("Table cannot be dropped.");
      } catch (Exception ex) {
        printException(ex);
      }
      resetOptions();
    }
  }
  





  public String globalInfo()
  {
    return "Writes to a database (tested with MySQL, InstantDB, HSQLDB).";
  }
  






  public void setTableName(String tn)
  {
    m_tableName = tn;
  }
  





  public String getTableName()
  {
    return m_tableName;
  }
  





  public String tableNameTipText()
  {
    return "Sets the name of the table.";
  }
  





  public void setAutoKeyGeneration(boolean flag)
  {
    m_id = flag;
  }
  





  public boolean getAutoKeyGeneration()
  {
    return m_id;
  }
  





  public String autoKeyGenerationTipText()
  {
    return "If set to true, a primary key column is generated automatically (containing the row number as INTEGER). The name of the key is read from DatabaseUtils (idColumn) This primary key can be used for incremental loading (requires an unique key). This primary key will not be loaded as an attribute.";
  }
  






  public void setRelationForTableName(boolean flag)
  {
    m_tabName = flag;
  }
  





  public boolean getRelationForTableName()
  {
    return m_tabName;
  }
  





  public String relationForTableNameTipText()
  {
    return "If set to true, the relation name will be used as name for the database table. Otherwise the user has to provide a table name.";
  }
  





  public void setUrl(String url)
  {
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
    m_Username = user;
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
  






  public void setDestination(String url, String userName, String password)
  {
    try
    {
      m_DataBaseConnection = new DatabaseConnection();
      m_DataBaseConnection.setDatabaseURL(url);
      m_DataBaseConnection.setUsername(userName);
      m_DataBaseConnection.setPassword(password);
    } catch (Exception ex) {
      printException(ex);
    }
  }
  




  public void setDestination(String url)
  {
    try
    {
      m_DataBaseConnection = new DatabaseConnection();
      m_DataBaseConnection.setDatabaseURL(url);
      m_DataBaseConnection.setUsername(m_Username);
      m_DataBaseConnection.setPassword(m_Password);
    } catch (Exception ex) {
      printException(ex);
    }
  }
  
  public void setDestination()
  {
    try
    {
      m_DataBaseConnection = new DatabaseConnection();
      m_DataBaseConnection.setUsername(m_Username);
      m_DataBaseConnection.setPassword(m_Password);
    } catch (Exception ex) {
      printException(ex);
    }
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.STRING_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.STRING_CLASS);
    result.enable(Capabilities.Capability.NO_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  



  public void connectToDatabase()
  {
    try
    {
      if (!m_DataBaseConnection.isConnected())
        m_DataBaseConnection.connectToDatabase();
    } catch (Exception ex) {
      printException(ex);
    }
  }
  




  private void writeStructure()
    throws Exception
  {
    StringBuffer query = new StringBuffer();
    Instances structure = getInstances();
    query.append("CREATE TABLE ");
    if ((m_tabName) || (m_tableName.equals("")))
      m_tableName = m_DataBaseConnection.maskKeyword(structure.relationName());
    if (m_DataBaseConnection.getUpperCase()) {
      m_tableName = m_tableName.toUpperCase();
      m_createInt = m_createInt.toUpperCase();
      m_createDouble = m_createDouble.toUpperCase();
      m_createText = m_createText.toUpperCase();
      m_createDate = m_createDate.toUpperCase();
    }
    m_tableName = m_tableName.replaceAll("[^\\w]", "_");
    m_tableName = m_DataBaseConnection.maskKeyword(m_tableName);
    query.append(m_tableName);
    if (structure.numAttributes() == 0)
      throw new Exception("Instances have no attribute.");
    query.append(" ( ");
    if (m_id) {
      if (m_DataBaseConnection.getUpperCase())
        m_idColumn = m_idColumn.toUpperCase();
      query.append(m_DataBaseConnection.maskKeyword(m_idColumn));
      query.append(" ");
      query.append(m_createInt);
      query.append(" PRIMARY KEY,");
    }
    for (int i = 0; i < structure.numAttributes(); i++) {
      Attribute att = structure.attribute(i);
      String attName = att.name();
      attName = attName.replaceAll("[^\\w]", "_");
      attName = m_DataBaseConnection.maskKeyword(attName);
      if (m_DataBaseConnection.getUpperCase()) {
        query.append(attName.toUpperCase());
      } else
        query.append(attName);
      if (att.isDate()) {
        query.append(" " + m_createDate);
      }
      else if (att.isNumeric()) {
        query.append(" " + m_createDouble);
      } else {
        query.append(" " + m_createText);
      }
      if (i != structure.numAttributes() - 1)
        query.append(", ");
    }
    query.append(" )");
    
    m_DataBaseConnection.update(query.toString());
    m_DataBaseConnection.close();
    if (!m_DataBaseConnection.tableExists(m_tableName)) {
      throw new IOException("Table cannot be built.");
    }
  }
  





  private void writeInstance(Instance inst)
    throws Exception
  {
    StringBuffer insert = new StringBuffer();
    insert.append("INSERT INTO ");
    insert.append(m_tableName);
    insert.append(" VALUES ( ");
    if (m_id) {
      insert.append(m_count);
      insert.append(", ");
      m_count += 1;
    }
    for (int j = 0; j < inst.numAttributes(); j++) {
      if (inst.isMissing(j)) {
        insert.append("NULL");
      }
      else if (inst.attribute(j).isDate()) {
        insert.append("'" + m_DateFormat.format(Long.valueOf(inst.value(j))) + "'");
      } else if (inst.attribute(j).isNumeric()) {
        insert.append(inst.value(j));
      } else {
        String stringInsert = "'" + inst.stringValue(j) + "'";
        if (stringInsert.length() > 2)
          stringInsert = stringInsert.replaceAll("''", "'");
        insert.append(stringInsert);
      }
      
      if (j != inst.numAttributes() - 1)
        insert.append(", ");
    }
    insert.append(" )");
    
    if (m_DataBaseConnection.update(insert.toString()) < 1) {
      throw new IOException("Tuple cannot be inserted.");
    }
    
    m_DataBaseConnection.close();
  }
  







  public void writeIncremental(Instance inst)
    throws IOException
  {
    int writeMode = getWriteMode();
    Instances structure = getInstances();
    
    if (m_DataBaseConnection == null)
      throw new IOException("No database has been set up.");
    if (getRetrieval() == 1)
      throw new IOException("Batch and incremental saving cannot be mixed.");
    setRetrieval(2);
    try
    {
      if (!m_DataBaseConnection.isConnected())
        connectToDatabase();
      if (writeMode == 1) {
        if (structure == null) {
          setWriteMode(2);
          if (inst != null) {
            throw new Exception("Structure(Header Information) has to be set in advance");
          }
        } else {
          setWriteMode(3); }
        writeMode = getWriteMode();
      }
      if (writeMode == 2) {
        cancel();
      }
      if (writeMode == 3) {
        setWriteMode(0);
        writeStructure();
        writeMode = getWriteMode();
      }
      if (writeMode == 0) {
        if (structure == null)
          throw new IOException("No instances information available.");
        if (inst != null)
        {
          writeInstance(inst);
        }
        else
        {
          m_DataBaseConnection.disconnectFromDatabase();
          resetStructure();
          m_count = 1;
        }
      }
    } catch (Exception ex) {
      printException(ex);
    }
  }
  




  public void writeBatch()
    throws IOException
  {
    Instances instances = getInstances();
    if (instances == null)
      throw new IOException("No instances to save");
    if (getRetrieval() == 2)
      throw new IOException("Batch and incremental saving cannot be mixed.");
    if (m_DataBaseConnection == null)
      throw new IOException("No database has been set up.");
    setRetrieval(1);
    try {
      if (!m_DataBaseConnection.isConnected())
        connectToDatabase();
      setWriteMode(0);
      writeStructure();
      for (int i = 0; i < instances.numInstances(); i++) {
        writeInstance(instances.instance(i));
      }
      m_DataBaseConnection.disconnectFromDatabase();
      setWriteMode(1);
      resetStructure();
      m_count = 1;
    } catch (Exception ex) {
      printException(ex);
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
    
    if ((m_tableName != null) && (m_tableName.length() != 0)) {
      options.add("-T");
      options.add(m_tableName);
    }
    
    if (m_id) {
      options.add("-P");
    }
    if ((m_inputFile != null) && (m_inputFile.length() != 0)) {
      options.add("-i");
      options.add(m_inputFile);
    }
    
    return (String[])options.toArray(new String[options.size()]);
  }
  





  public Enumeration listOptions()
  {
    FastVector newVector = new FastVector();
    
    newVector.addElement(new Option("\tThe JDBC URL to connect to.\n\t(default: from DatabaseUtils.props file)", "url", 1, "-url <JDBC URL>"));
    



    newVector.addElement(new Option("\tThe user to connect with to the database.\n\t(default: none)", "user", 1, "-user <name>"));
    



    newVector.addElement(new Option("\tThe password to connect with to the database.\n\t(default: none)", "password", 1, "-password <password>"));
    



    newVector.addElement(new Option("\tThe name of the table.\n\t(default: the relation name)", "T", 1, "-T <table name>"));
    



    newVector.addElement(new Option("\tAdd an ID column as primary key. The name is specified\n\tin the DatabaseUtils file ('idColumn'). The DatabaseLoader\n\twon't load this column.", "P", 0, "-P"));
    




    newVector.addElement(new Option("\tInput file in arff format that should be saved in database.", "i", 1, "-i <input file name>"));
    


    return newVector.elements();
  }
  




































  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    
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
    String tableString = Utils.getOption('T', options);
    
    String inputString = Utils.getOption('i', options);
    
    if (tableString.length() != 0) {
      m_tableName = tableString;
      m_tabName = false;
    }
    
    m_id = Utils.getFlag('P', options);
    
    if (inputString.length() != 0) {
      try {
        m_inputFile = inputString;
        ArffLoader al = new ArffLoader();
        File inputFile = new File(inputString);
        al.setSource(inputFile);
        setInstances(al.getDataSet());
        
        if (tableString.length() == 0)
          m_tableName = getInstances().relationName();
      } catch (Exception ex) {
        printException(ex);
        ex.printStackTrace();
      }
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7499 $");
  }
  





  public static void main(String[] options)
  {
    StringBuffer text = new StringBuffer();
    text.append("\n\nDatabaseSaver options:\n");
    try {
      DatabaseSaver asv = new DatabaseSaver();
      try {
        Enumeration enumi = asv.listOptions();
        while (enumi.hasMoreElements()) {
          Option option = (Option)enumi.nextElement();
          text.append(option.synopsis() + '\n');
          text.append(option.description() + '\n');
        }
        asv.setOptions(options);
        asv.setDestination();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      











      asv.writeBatch();
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println(text);
    }
  }
}
