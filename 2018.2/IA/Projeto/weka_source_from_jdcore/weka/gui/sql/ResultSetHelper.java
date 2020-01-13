package weka.gui.sql;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Vector;
































public class ResultSetHelper
{
  protected ResultSet m_ResultSet;
  protected boolean m_Initialized = false;
  

  protected int m_MaxRows = 0;
  

  protected int m_ColumnCount = 0;
  

  protected int m_RowCount = 0;
  

  protected String[] m_ColumnNames = null;
  

  protected boolean[] m_NumericColumns = null;
  

  protected Class[] m_ColumnClasses = null;
  




  public ResultSetHelper(ResultSet rs)
  {
    this(rs, 0);
  }
  








  public ResultSetHelper(ResultSet rs, int max)
  {
    m_ResultSet = rs;
    m_MaxRows = max;
  }
  





  protected void initialize()
  {
    if (m_Initialized) {
      return;
    }
    try {
      ResultSetMetaData meta = m_ResultSet.getMetaData();
      

      m_ColumnNames = new String[meta.getColumnCount()];
      for (int i = 1; i <= meta.getColumnCount(); i++) {
        m_ColumnNames[(i - 1)] = meta.getColumnLabel(i);
      }
      
      m_NumericColumns = new boolean[meta.getColumnCount()];
      for (i = 1; i <= meta.getColumnCount(); i++) {
        m_NumericColumns[(i - 1)] = typeIsNumeric(meta.getColumnType(i));
      }
      
      m_ColumnClasses = new Class[meta.getColumnCount()];
      for (i = 1; i <= meta.getColumnCount(); i++) {
        try {
          m_ColumnClasses[(i - 1)] = typeToClass(meta.getColumnType(i));
        }
        catch (Exception ex) {
          m_ColumnClasses[(i - 1)] = String.class;
        }
      }
      

      m_ColumnCount = meta.getColumnCount();
      


      if (m_ResultSet.getType() == 1003) {
        m_RowCount = -1;
      }
      else {
        m_RowCount = 0;
        m_ResultSet.first();
        if (m_MaxRows > 0) {
          try {
            m_ResultSet.absolute(m_MaxRows);
            m_RowCount = m_ResultSet.getRow();

          }
          catch (Exception ex) {}
        }
        else
        {
          m_ResultSet.last();
          m_RowCount = m_ResultSet.getRow();
        }
        

        try
        {
          if ((m_RowCount == 0) && (m_ResultSet.first())) {
            m_RowCount = 1;
            while (m_ResultSet.next()) {
              m_RowCount += 1;
              if (m_ResultSet.getRow() == m_MaxRows) {
                break;
              }
            }
          }
        }
        catch (Exception e) {}
      }
      

      m_Initialized = true;
    }
    catch (Exception ex) {}
  }
  






  public ResultSet getResultSet()
  {
    return m_ResultSet;
  }
  




  public int getColumnCount()
  {
    initialize();
    
    return m_ColumnCount;
  }
  






  public int getRowCount()
  {
    initialize();
    
    return m_RowCount;
  }
  




  public String[] getColumnNames()
  {
    initialize();
    
    return m_ColumnNames;
  }
  




  public boolean[] getNumericColumns()
  {
    initialize();
    
    return m_NumericColumns;
  }
  




  public Class[] getColumnClasses()
  {
    initialize();
    
    return m_ColumnClasses;
  }
  




  public boolean hasMaxRows()
  {
    return m_MaxRows > 0;
  }
  




  public int getMaxRows()
  {
    return m_MaxRows;
  }
  













  public Object[][] getCells()
  {
    initialize();
    
    Vector<Object[]> result = new Vector();
    

    try
    {
      int rowCount = getRowCount();
      boolean proceed; boolean proceed; if (rowCount == -1) {
        rowCount = getMaxRows();
        proceed = m_ResultSet.next();
      }
      else {
        proceed = m_ResultSet.first();
      }
      
      if (proceed) {
        int i = 0;
        for (;;) {
          Object[] row = new Object[getColumnCount()];
          result.add(row);
          
          for (int n = 0; n < getColumnCount(); n++) {
            try
            {
              if (getColumnClasses()[n] == String.class) {
                row[n] = m_ResultSet.getString(n + 1);
              } else {
                row[n] = m_ResultSet.getObject(n + 1);
              }
            } catch (Exception e) {
              row[n] = null;
            }
          }
          

          if (i == rowCount - 1) {
            break;
          }
          

          if (!m_ResultSet.next()) {
            break;
          }
          
          i++;
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return (Object[][])result.toArray(new Object[result.size()][getColumnCount()]);
  }
  



  public static Class typeToClass(int type)
  {
    Class result;
    


    switch (type) {
    case -5: 
      result = Long.class;
      break;
    case -2: 
      result = String.class;
      break;
    case -7: 
      result = Boolean.class;
      break;
    case 1: 
      result = Character.class;
      break;
    case 91: 
      result = Date.class;
      break;
    case 3: 
      result = Double.class;
      break;
    case 8: 
      result = Double.class;
      break;
    case 6: 
      result = Float.class;
      break;
    case 4: 
      result = Integer.class;
      break;
    case -4: 
      result = String.class;
      break;
    case -1: 
      result = String.class;
      break;
    case 0: 
      result = String.class;
      break;
    case 2: 
      result = Double.class;
      break;
    case 1111: 
      result = String.class;
      break;
    case 7: 
      result = Double.class;
      break;
    case 5: 
      result = Short.class;
      break;
    case 92: 
      result = Time.class;
      break;
    case 93: 
      result = Timestamp.class;
      break;
    case -6: 
      result = Short.class;
      break;
    case -3: 
      result = String.class;
      break;
    case 12: 
      result = String.class;
      break;
    default: 
      result = null;
    }
    
    return result;
  }
  




  public static boolean typeIsNumeric(int type)
  {
    boolean result;
    


    switch (type) {
    case -5: 
      result = true;
      break;
    case -2: 
      result = false;
    case -7: 
      result = false;
      break;
    case 1: 
      result = false;
      break;
    case 91: 
      result = false;
      break;
    case 3: 
      result = true;
      break;
    case 8: 
      result = true;
      break;
    case 6: 
      result = true;
      break;
    case 4: 
      result = true;
      break;
    case -4: 
      result = false;
      break;
    case -1: 
      result = false;
      break;
    case 0: 
      result = false;
      break;
    case 2: 
      result = true;
      break;
    case 1111: 
      result = false;
      break;
    case 7: 
      result = true;
      break;
    case 5: 
      result = true;
      break;
    case 92: 
      result = false;
      break;
    case 93: 
      result = true;
      break;
    case -6: 
      result = true;
      break;
    case -3: 
      result = false;
      break;
    case 12: 
      result = false;
      break;
    default: 
      result = false;
    }
    
    return result;
  }
}
