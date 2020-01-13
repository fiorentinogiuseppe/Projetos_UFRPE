package weka.experiment;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import weka.core.FastVector;
import weka.core.RevisionUtils;







































public class DatabaseResultListener
  extends DatabaseUtils
  implements ResultListener
{
  static final long serialVersionUID = 7388014746954652818L;
  protected ResultProducer m_ResultProducer;
  protected String m_ResultsTableName;
  protected boolean m_Debug = true;
  

  protected String m_CacheKeyName = "";
  

  protected int m_CacheKeyIndex;
  

  protected Object[] m_CacheKey;
  

  protected FastVector m_Cache = new FastVector();
  





  public String globalInfo()
  {
    return "Takes results from a result producer and sends them to a database.";
  }
  






  public DatabaseResultListener()
    throws Exception
  {}
  






  public void preProcess(ResultProducer rp)
    throws Exception
  {
    m_ResultProducer = rp;
    

    updateResultsTableName(m_ResultProducer);
  }
  







  public void postProcess(ResultProducer rp)
    throws Exception
  {
    if (m_ResultProducer != rp) {
      throw new Error("Unrecognized ResultProducer calling postProcess!!");
    }
    disconnectFromDatabase();
  }
  












  public String[] determineColumnConstraints(ResultProducer rp)
    throws Exception
  {
    FastVector cNames = new FastVector();
    updateResultsTableName(rp);
    DatabaseMetaData dbmd = m_Connection.getMetaData();
    ResultSet rs;
    ResultSet rs;
    if (m_checkForUpperCaseNames) {
      rs = dbmd.getColumns(null, null, m_ResultsTableName.toUpperCase(), null);
    } else {
      rs = dbmd.getColumns(null, null, m_ResultsTableName, null);
    }
    boolean tableExists = false;
    int numColumns = 0;
    
    while (rs.next()) {
      tableExists = true;
      
      String name = rs.getString(4);
      if (name.toLowerCase().startsWith("measure")) {
        numColumns++;
        cNames.addElement(name);
      }
    }
    

    if (!tableExists) {
      return null;
    }
    

    String[] columnNames = new String[numColumns];
    for (int i = 0; i < numColumns; i++) {
      columnNames[i] = ((String)(String)cNames.elementAt(i));
    }
    
    return columnNames;
  }
  








  public void acceptResult(ResultProducer rp, Object[] key, Object[] result)
    throws Exception
  {
    if (m_ResultProducer != rp) {
      throw new Error("Unrecognized ResultProducer calling acceptResult!!");
    }
    

    if (result != null) {
      putResultInTable(m_ResultsTableName, rp, key, result);
    }
  }
  









  public boolean isResultRequired(ResultProducer rp, Object[] key)
    throws Exception
  {
    if (m_ResultProducer != rp) {
      throw new Error("Unrecognized ResultProducer calling isResultRequired!");
    }
    if (m_Debug) {
      System.err.print("Is result required...");
      for (int i = 0; i < key.length; i++) {
        System.err.print(" " + key[i]);
      }
      System.err.flush();
    }
    boolean retval = false;
    

    if (!m_CacheKeyName.equals("")) {
      if (!isCacheValid(key)) {
        loadCache(rp, key);
      }
      retval = !isKeyInCache(rp, key);
    }
    else {
      retval = !isKeyInTable(m_ResultsTableName, rp, key);
    }
    

    if (m_Debug) {
      System.err.println(" ..." + (retval ? "required" : "not required") + (m_CacheKeyName.equals("") ? "" : " (cache)"));
      
      System.err.flush();
    }
    return retval;
  }
  








  protected void updateResultsTableName(ResultProducer rp)
    throws Exception
  {
    if (!isConnected()) {
      connectToDatabase();
    }
    if (!experimentIndexExists()) {
      createExperimentIndex();
    }
    
    String tableName = getResultsTableName(rp);
    if (tableName == null) {
      tableName = createExperimentIndexEntry(rp);
    }
    if (!tableExists(tableName)) {
      createResultsTable(rp, tableName);
    }
    m_ResultsTableName = tableName;
  }
  




  public String cacheKeyNameTipText()
  {
    return "Set the name of the key field by which to cache.";
  }
  





  public String getCacheKeyName()
  {
    return m_CacheKeyName;
  }
  






  public void setCacheKeyName(String newCacheKeyName)
  {
    m_CacheKeyName = newCacheKeyName;
  }
  









  protected boolean isCacheValid(Object[] key)
  {
    if (m_CacheKey == null) {
      return false;
    }
    if (m_CacheKey.length != key.length) {
      return false;
    }
    for (int i = 0; i < key.length; i++) {
      if ((i != m_CacheKeyIndex) && (!m_CacheKey[i].equals(key[i]))) {
        return false;
      }
    }
    return true;
  }
  









  protected boolean isKeyInCache(ResultProducer rp, Object[] key)
    throws Exception
  {
    for (int i = 0; i < m_Cache.size(); i++) {
      if (m_Cache.elementAt(i).equals(key[m_CacheKeyIndex])) {
        return true;
      }
    }
    return false;
  }
  







  protected void loadCache(ResultProducer rp, Object[] key)
    throws Exception
  {
    System.err.print(" (updating cache)");System.err.flush();
    m_Cache.removeAllElements();
    m_CacheKey = null;
    String query = "SELECT Key_" + m_CacheKeyName + " FROM " + m_ResultsTableName;
    
    String[] keyNames = rp.getKeyNames();
    if (keyNames.length != key.length) {
      throw new Exception("Key names and key values of different lengths");
    }
    m_CacheKeyIndex = -1;
    for (int i = 0; i < keyNames.length; i++) {
      if (keyNames[i].equalsIgnoreCase(m_CacheKeyName)) {
        m_CacheKeyIndex = i;
        break;
      }
    }
    if (m_CacheKeyIndex == -1) {
      throw new Exception("No key field named " + m_CacheKeyName + " (as specified for caching)");
    }
    
    boolean first = true;
    for (int i = 0; i < key.length; i++) {
      if ((key[i] != null) && (i != m_CacheKeyIndex)) {
        if (first) {
          query = query + " WHERE ";
          first = false;
        } else {
          query = query + " AND ";
        }
        query = query + "Key_" + keyNames[i] + '=';
        if ((key[i] instanceof String)) {
          query = query + "'" + DatabaseUtils.processKeyString(key[i].toString()) + "'";
        } else {
          query = query + key[i].toString();
        }
      }
    }
    ResultSet rs = select(query);
    while (rs.next()) {
      String keyVal = rs.getString(1);
      if (!rs.wasNull()) {
        m_Cache.addElement(keyVal);
      }
    }
    close(rs);
    m_CacheKey = ((Object[])key.clone());
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5125 $");
  }
}
