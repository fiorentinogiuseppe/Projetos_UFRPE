package weka.experiment;

import java.io.PrintStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;









































































public class InstanceQuery
  extends DatabaseUtils
  implements OptionHandler
{
  static final long serialVersionUID = 718158370917782584L;
  boolean m_CreateSparseData = false;
  

  String m_Query = "SELECT * from ?";
  






  public InstanceQuery()
    throws Exception
  {}
  





  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tSQL query to execute.", "Q", 1, "-Q <query>"));
    

    result.addElement(new Option("\tReturn sparse rather than normal instances.", "S", 0, "-S"));
    

    result.addElement(new Option("\tThe username to use for connecting.", "U", 1, "-U <username>"));
    

    result.addElement(new Option("\tThe password to use for connecting.", "P", 1, "-P <password>"));
    

    result.addElement(new Option("\tEnables debug output.", "D", 0, "-D"));
    
    return result.elements();
  }
  





































  public void setOptions(String[] options)
    throws Exception
  {
    setSparseData(Utils.getFlag('S', options));
    
    String tmpStr = Utils.getOption('Q', options);
    if (tmpStr.length() != 0) {
      setQuery(tmpStr);
    }
    tmpStr = Utils.getOption('U', options);
    if (tmpStr.length() != 0) {
      setUsername(tmpStr);
    }
    tmpStr = Utils.getOption('P', options);
    if (tmpStr.length() != 0) {
      setPassword(tmpStr);
    }
    setDebug(Utils.getFlag('D', options));
  }
  





  public String queryTipText()
  {
    return "The SQL query to execute against the database.";
  }
  




  public void setQuery(String q)
  {
    m_Query = q;
  }
  




  public String getQuery()
  {
    return m_Query;
  }
  





  public String sparseDataTipText()
  {
    return "Encode data as sparse instances.";
  }
  




  public void setSparseData(boolean s)
  {
    m_CreateSparseData = s;
  }
  




  public boolean getSparseData()
  {
    return m_CreateSparseData;
  }
  





  public String[] getOptions()
  {
    Vector options = new Vector();
    
    options.add("-Q");
    options.add(getQuery());
    
    if (getSparseData()) {
      options.add("-S");
    }
    if (!getUsername().equals("")) {
      options.add("-U");
      options.add(getUsername());
    }
    
    if (!getPassword().equals("")) {
      options.add("-P");
      options.add(getPassword());
    }
    
    if (getDebug()) {
      options.add("-D");
    }
    return (String[])options.toArray(new String[options.size()]);
  }
  





  public Instances retrieveInstances()
    throws Exception
  {
    return retrieveInstances(m_Query);
  }
  







  public Instances retrieveInstances(String query)
    throws Exception
  {
    if (m_Debug)
      System.err.println("Executing query: " + query);
    connectToDatabase();
    if (!execute(query)) {
      if (m_PreparedStatement.getUpdateCount() == -1) {
        throw new Exception("Query didn't produce results");
      }
      if (m_Debug) {
        System.err.println(m_PreparedStatement.getUpdateCount() + " rows affected.");
      }
      close();
      return null;
    }
    
    ResultSet rs = getResultSet();
    if (m_Debug)
      System.err.println("Getting metadata...");
    ResultSetMetaData md = rs.getMetaData();
    if (m_Debug) {
      System.err.println("Completed getting metadata...");
    }
    
    int numAttributes = md.getColumnCount();
    int[] attributeTypes = new int[numAttributes];
    Hashtable[] nominalIndexes = new Hashtable[numAttributes];
    FastVector[] nominalStrings = new FastVector[numAttributes];
    for (int i = 1; i <= numAttributes; i++)
    {





      switch (translateDBColumnType(md.getColumnTypeName(i)))
      {

      case 0: 
        attributeTypes[(i - 1)] = 1;
        nominalIndexes[(i - 1)] = new Hashtable();
        nominalStrings[(i - 1)] = new FastVector();
        break;
      
      case 9: 
        attributeTypes[(i - 1)] = 2;
        nominalIndexes[(i - 1)] = new Hashtable();
        nominalStrings[(i - 1)] = new FastVector();
        break;
      
      case 1: 
        attributeTypes[(i - 1)] = 1;
        nominalIndexes[(i - 1)] = new Hashtable();
        nominalIndexes[(i - 1)].put("false", new Double(0.0D));
        nominalIndexes[(i - 1)].put("true", new Double(1.0D));
        nominalStrings[(i - 1)] = new FastVector();
        nominalStrings[(i - 1)].addElement("false");
        nominalStrings[(i - 1)].addElement("true");
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
      case 11: 
        attributeTypes[(i - 1)] = 3;
        break;
      
      default: 
        attributeTypes[(i - 1)] = 2;
      }
      
    }
    


    Vector<String> columnNames = new Vector();
    for (int i = 0; i < numAttributes; i++) {
      columnNames.add(md.getColumnLabel(i + 1));
    }
    

    if (m_Debug)
      System.err.println("Creating instances...");
    FastVector instances = new FastVector();
    int rowCount = 0;
    while (rs.next()) {
      if ((rowCount % 100 == 0) && 
        (m_Debug)) {
        System.err.print("read " + rowCount + " instances \r");
        System.err.flush();
      }
      
      double[] vals = new double[numAttributes];
      for (int i = 1; i <= numAttributes; i++)
      {




        switch (translateDBColumnType(md.getColumnTypeName(i))) {
        case 0: 
          String str = rs.getString(i);
          
          if (rs.wasNull()) {
            vals[(i - 1)] = Instance.missingValue();
          } else {
            Double index = (Double)nominalIndexes[(i - 1)].get(str);
            if (index == null) {
              index = new Double(nominalStrings[(i - 1)].size());
              nominalIndexes[(i - 1)].put(str, index);
              nominalStrings[(i - 1)].addElement(str);
            }
            vals[(i - 1)] = index.doubleValue();
          }
          break;
        case 9: 
          String txt = rs.getString(i);
          
          if (rs.wasNull()) {
            vals[(i - 1)] = Instance.missingValue();
          } else {
            Double index = (Double)nominalIndexes[(i - 1)].get(txt);
            if (index == null)
            {


              index = Double.valueOf(new Double(nominalStrings[(i - 1)].size()).doubleValue() + 1.0D);
              nominalIndexes[(i - 1)].put(txt, index);
              nominalStrings[(i - 1)].addElement(txt);
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
        case 11: 
          Timestamp ts = rs.getTimestamp(i);
          if (rs.wasNull()) {
            vals[(i - 1)] = Instance.missingValue();
          } else {
            vals[(i - 1)] = ts.getTime();
          }
          break;
        default: 
          vals[(i - 1)] = Instance.missingValue();
        } }
      Instance newInst;
      Instance newInst;
      if (m_CreateSparseData) {
        newInst = new SparseInstance(1.0D, vals);
      } else {
        newInst = new Instance(1.0D, vals);
      }
      instances.addElement(newInst);
      rowCount++;
    }
    


    if (m_Debug)
      System.err.println("Creating header...");
    FastVector attribInfo = new FastVector();
    for (int i = 0; i < numAttributes; i++)
    {

      String attribName = attributeCaseFix((String)columnNames.get(i));
      switch (attributeTypes[i]) {
      case 1: 
        attribInfo.addElement(new Attribute(attribName, nominalStrings[i]));
        break;
      case 0: 
        attribInfo.addElement(new Attribute(attribName));
        break;
      case 2: 
        Attribute att = new Attribute(attribName, (FastVector)null);
        attribInfo.addElement(att);
        for (int n = 0; n < nominalStrings[i].size(); n++) {
          att.addStringValue((String)nominalStrings[i].elementAt(n));
        }
        break;
      case 3: 
        attribInfo.addElement(new Attribute(attribName, (String)null));
        break;
      default: 
        throw new Exception("Unknown attribute type");
      }
    }
    Instances result = new Instances("QueryResult", attribInfo, instances.size());
    
    for (int i = 0; i < instances.size(); i++) {
      result.add((Instance)instances.elementAt(i));
    }
    close(rs);
    
    return result;
  }
  





  public static void main(String[] args)
  {
    try
    {
      InstanceQuery iq = new InstanceQuery();
      String query = Utils.getOption('Q', args);
      if (query.length() == 0) {
        iq.setQuery("select * from Experiment_index");
      } else {
        iq.setQuery(query);
      }
      iq.setOptions(args);
      try {
        Utils.checkForRemainingOptions(args);
      } catch (Exception e) {
        System.err.println("Options for weka.experiment.InstanceQuery:\n");
        Enumeration en = iq.listOptions();
        while (en.hasMoreElements()) {
          Option o = (Option)en.nextElement();
          System.err.println(o.synopsis() + "\n" + o.description());
        }
        System.exit(1);
      }
      
      Instances aha = iq.retrieveInstances();
      iq.disconnectFromDatabase();
      
      if (aha == null) {
        return;
      }
      

      System.out.println(new Instances(aha, 0));
      for (int i = 0; i < aha.numInstances(); i++) {
        System.out.println(aha.instance(i));
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11887 $");
  }
}
