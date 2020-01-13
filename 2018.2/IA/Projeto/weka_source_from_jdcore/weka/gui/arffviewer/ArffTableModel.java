package weka.gui.arffviewer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Undoable;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Reorder;
import weka.gui.ComponentHelper;











































public class ArffTableModel
  implements TableModel, Undoable
{
  private HashSet m_Listeners;
  private Instances m_Data;
  private boolean m_NotificationEnabled;
  private boolean m_UndoEnabled;
  private boolean m_IgnoreChanges;
  private Vector m_UndoList;
  private boolean m_ReadOnly;
  
  private ArffTableModel()
  {
    m_Listeners = new HashSet();
    m_Data = null;
    m_NotificationEnabled = true;
    m_UndoList = new Vector();
    m_IgnoreChanges = false;
    m_UndoEnabled = true;
    m_ReadOnly = false;
  }
  




  public ArffTableModel(String filename)
  {
    this();
    
    if ((filename != null) && (!filename.equals(""))) {
      loadFile(filename);
    }
  }
  



  public ArffTableModel(Instances data)
  {
    this();
    
    m_Data = data;
  }
  




  public boolean isNotificationEnabled()
  {
    return m_NotificationEnabled;
  }
  




  public void setNotificationEnabled(boolean enabled)
  {
    m_NotificationEnabled = enabled;
  }
  




  public boolean isUndoEnabled()
  {
    return m_UndoEnabled;
  }
  




  public void setUndoEnabled(boolean enabled)
  {
    m_UndoEnabled = enabled;
  }
  




  public boolean isReadOnly()
  {
    return m_ReadOnly;
  }
  




  public void setReadOnly(boolean value)
  {
    m_ReadOnly = value;
  }
  






  private void loadFile(String filename)
  {
    AbstractFileLoader loader = ConverterUtils.getLoaderForFile(filename);
    
    if (loader != null) {
      try {
        loader.setFile(new File(filename));
        m_Data = loader.getDataSet();
      }
      catch (Exception e) {
        Messages.getInstance();ComponentHelper.showMessageBox(null, Messages.getString("ArffTableModel_LoadFile_ComponentHelperShowMessageBox_Text"), e.toString(), 2, 0);
        




        System.out.println(e);
        m_Data = null;
      }
    }
  }
  




  public void setInstances(Instances data)
  {
    m_Data = data;
  }
  




  public Instances getInstances()
  {
    return m_Data;
  }
  






  public Attribute getAttributeAt(int columnIndex)
  {
    if ((columnIndex > 0) && (columnIndex < getColumnCount())) {
      return m_Data.attribute(columnIndex - 1);
    }
    return null;
  }
  





  public int getType(int columnIndex)
  {
    return getType(0, columnIndex);
  }
  








  public int getType(int rowIndex, int columnIndex)
  {
    int result = 2;
    
    if ((rowIndex >= 0) && (rowIndex < getRowCount()) && (columnIndex > 0) && (columnIndex < getColumnCount()))
    {
      result = m_Data.instance(rowIndex).attribute(columnIndex - 1).type();
    }
    return result;
  }
  




  public void deleteAttributeAt(int columnIndex)
  {
    deleteAttributeAt(columnIndex, true);
  }
  





  public void deleteAttributeAt(int columnIndex, boolean notify)
  {
    if ((columnIndex > 0) && (columnIndex < getColumnCount())) {
      if (!m_IgnoreChanges)
        addUndoPoint();
      m_Data.deleteAttributeAt(columnIndex - 1);
      if (notify) {
        notifyListener(new TableModelEvent(this, -1));
      }
    }
  }
  





  public void deleteAttributes(int[] columnIndices)
  {
    Arrays.sort(columnIndices);
    
    addUndoPoint();
    
    m_IgnoreChanges = true;
    for (int i = columnIndices.length - 1; i >= 0; i--)
      deleteAttributeAt(columnIndices[i], false);
    m_IgnoreChanges = false;
    
    notifyListener(new TableModelEvent(this, -1));
  }
  





  public void renameAttributeAt(int columnIndex, String newName)
  {
    if ((columnIndex > 0) && (columnIndex < getColumnCount())) {
      addUndoPoint();
      m_Data.renameAttribute(columnIndex - 1, newName);
      notifyListener(new TableModelEvent(this, -1));
    }
  }
  









  public void attributeAsClassAt(int columnIndex)
  {
    if ((columnIndex > 0) && (columnIndex < getColumnCount())) {
      addUndoPoint();
      
      try
      {
        String order = "";
        for (int i = 1; i < m_Data.numAttributes() + 1; i++)
        {
          if (i != columnIndex)
          {

            if (!order.equals(""))
              order = order + ",";
            order = order + Integer.toString(i);
          } }
        if (!order.equals(""))
          order = order + ",";
        order = order + Integer.toString(columnIndex);
        

        Reorder reorder = new Reorder();
        reorder.setAttributeIndices(order);
        reorder.setInputFormat(m_Data);
        m_Data = Filter.useFilter(m_Data, reorder);
        

        m_Data.setClassIndex(m_Data.numAttributes() - 1);
      }
      catch (Exception e) {
        e.printStackTrace();
        undo();
      }
      
      notifyListener(new TableModelEvent(this, -1));
    }
  }
  




  public void deleteInstanceAt(int rowIndex)
  {
    deleteInstanceAt(rowIndex, true);
  }
  





  public void deleteInstanceAt(int rowIndex, boolean notify)
  {
    if ((rowIndex >= 0) && (rowIndex < getRowCount())) {
      if (!m_IgnoreChanges)
        addUndoPoint();
      m_Data.delete(rowIndex);
      if (notify) {
        notifyListener(new TableModelEvent(this, rowIndex, rowIndex, -1, -1));
      }
    }
  }
  








  public void deleteInstances(int[] rowIndices)
  {
    Arrays.sort(rowIndices);
    
    addUndoPoint();
    
    m_IgnoreChanges = true;
    for (int i = rowIndices.length - 1; i >= 0; i--)
      deleteInstanceAt(rowIndices[i], false);
    m_IgnoreChanges = false;
    
    notifyListener(new TableModelEvent(this, rowIndices[0], rowIndices[(rowIndices.length - 1)], -1, -1));
  }
  







  public void sortInstances(int columnIndex)
  {
    if ((columnIndex > 0) && (columnIndex < getColumnCount())) {
      addUndoPoint();
      m_Data.sort(columnIndex - 1);
      notifyListener(new TableModelEvent(this));
    }
  }
  








  public int getAttributeColumn(String name)
  {
    int result = -1;
    
    for (int i = 0; i < m_Data.numAttributes(); i++) {
      if (m_Data.attribute(i).name().equals(name)) {
        result = i + 1;
        break;
      }
    }
    
    return result;
  }
  








  public Class getColumnClass(int columnIndex)
  {
    Class result = null;
    
    if ((columnIndex >= 0) && (columnIndex < getColumnCount())) {
      if (columnIndex == 0) {
        result = Integer.class;
      } else if (getType(columnIndex) == 0) {
        result = Double.class;
      } else {
        result = String.class;
      }
    }
    return result;
  }
  






  public int getColumnCount()
  {
    int result = 1;
    if (m_Data != null) {
      result += m_Data.numAttributes();
    }
    return result;
  }
  








  private boolean isClassIndex(int columnIndex)
  {
    int index = m_Data.classIndex();
    boolean result = ((index == -1) && (m_Data.numAttributes() == columnIndex)) || (index == columnIndex - 1);
    

    return result;
  }
  







  public String getColumnName(int columnIndex)
  {
    String result = "";
    
    if ((columnIndex >= 0) && (columnIndex < getColumnCount())) {
      if (columnIndex == 0) {
        Messages.getInstance();result = Messages.getString("ArffTableModel_GetColumnName_Result_Text_First");

      }
      else if ((m_Data != null) && 
        (columnIndex - 1 < m_Data.numAttributes())) {
        Messages.getInstance();result = Messages.getString("ArffTableModel_GetColumnName_Result_Text_Second");
        
        if (isClassIndex(columnIndex)) {
          Messages.getInstance();Messages.getInstance();result = result + Messages.getString("ArffTableModel_GetColumnName_Result_Text_Third") + m_Data.attribute(columnIndex - 1).name() + Messages.getString("ArffTableModel_GetColumnName_Result_Text_Forth");
        }
        else
        {
          result = result + m_Data.attribute(columnIndex - 1).name();
        }
        
        switch (getType(columnIndex)) {
        case 3: 
          Messages.getInstance();result = result + Messages.getString("ArffTableModel_GetColumnName_Result_Date_Text");
          break;
        case 1: 
          Messages.getInstance();result = result + Messages.getString("ArffTableModel_GetColumnName_Result_Nominal_Text");
          break;
        case 2: 
          Messages.getInstance();result = result + Messages.getString("ArffTableModel_GetColumnName_Result_String_Text");
          break;
        case 0: 
          Messages.getInstance();result = result + Messages.getString("ArffTableModel_GetColumnName_Result_Numeric_Text");
          break;
        case 4: 
          Messages.getInstance();result = result + Messages.getString("ArffTableModel_GetColumnName_Result_Relational_Text");
          break;
        default: 
          Messages.getInstance();result = result + Messages.getString("ArffTableModel_GetColumnName_Result_Default_Text");
        }
        
        Messages.getInstance();result = result + Messages.getString("ArffTableModel_GetColumnName_Result_Text_End");
      }
    }
    


    return result;
  }
  




  public int getRowCount()
  {
    if (m_Data == null) {
      return 0;
    }
    return m_Data.numInstances();
  }
  








  public boolean isMissingAt(int rowIndex, int columnIndex)
  {
    boolean result = false;
    
    if ((rowIndex >= 0) && (rowIndex < getRowCount()) && (columnIndex > 0) && (columnIndex < getColumnCount()))
    {
      result = m_Data.instance(rowIndex).isMissing(columnIndex - 1);
    }
    return result;
  }
  









  public double getInstancesValueAt(int rowIndex, int columnIndex)
  {
    double result = -1.0D;
    
    if ((rowIndex >= 0) && (rowIndex < getRowCount()) && (columnIndex > 0) && (columnIndex < getColumnCount()))
    {
      result = m_Data.instance(rowIndex).value(columnIndex - 1);
    }
    return result;
  }
  









  public Object getValueAt(int rowIndex, int columnIndex)
  {
    Object result = null;
    
    if ((rowIndex >= 0) && (rowIndex < getRowCount()) && (columnIndex >= 0) && (columnIndex < getColumnCount()))
    {
      if (columnIndex == 0) {
        result = new Integer(rowIndex + 1);

      }
      else if (isMissingAt(rowIndex, columnIndex)) {
        result = null;
      }
      else {
        switch (getType(columnIndex)) {
        case 1: 
        case 2: 
        case 3: 
        case 4: 
          result = m_Data.instance(rowIndex).stringValue(columnIndex - 1);
          break;
        case 0: 
          result = new Double(m_Data.instance(rowIndex).value(columnIndex - 1));
          break;
        default: 
          result = "-can't display-";
        }
        
        if ((getType(columnIndex) != 0) && 
          (result != null)) {
          String tmp = result.toString();
          
          if ((tmp.indexOf('<') > -1) || (tmp.indexOf('>') > -1)) {
            tmp = tmp.replace("<", "(");
            tmp = tmp.replace(">", ")");
          }
          
          if ((tmp.indexOf("\n") > -1) || (tmp.indexOf("\r") > -1)) {
            tmp = tmp.replaceAll("\\r\\n", "<font color=\"red\"><b>\\\\r\\\\n</b></font>");
            tmp = tmp.replaceAll("\\r", "<font color=\"red\"><b>\\\\r</b></font>");
            tmp = tmp.replaceAll("\\n", "<font color=\"red\"><b>\\\\n</b></font>");
            tmp = "<html>" + tmp + "</html>";
          }
          result = tmp;
        }
      }
    }
    


    return result;
  }
  






  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return (columnIndex > 0) && (!isReadOnly());
  }
  







  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    setValueAt(aValue, rowIndex, columnIndex, true);
  }
  















  public void setValueAt(Object aValue, int rowIndex, int columnIndex, boolean notify)
  {
    if (!m_IgnoreChanges) {
      addUndoPoint();
    }
    Object oldValue = getValueAt(rowIndex, columnIndex);
    int type = getType(rowIndex, columnIndex);
    int index = columnIndex - 1;
    Instance inst = m_Data.instance(rowIndex);
    Attribute att = inst.attribute(index);
    

    if (aValue == null) {
      inst.setValue(index, Instance.missingValue());
    }
    else {
      String tmp = aValue.toString();
      
      switch (type) {
      case 3: 
        try {
          att.parseDate(tmp);
          inst.setValue(index, att.parseDate(tmp));
        }
        catch (Exception e) {}
      



      case 1: 
        if (att.indexOfValue(tmp) > -1) {
          inst.setValue(index, att.indexOfValue(tmp));
        }
        break;
      case 2: 
        inst.setValue(index, tmp);
        break;
      case 0: 
        try
        {
          Double.parseDouble(tmp);
          inst.setValue(index, Double.parseDouble(tmp));
        }
        catch (Exception e) {}
      


      case 4: 
        try
        {
          inst.setValue(index, inst.attribute(index).addRelation((Instances)aValue));
        }
        catch (Exception e) {}
      



      default: 
        Messages.getInstance();Messages.getInstance();throw new IllegalArgumentException(Messages.getString("ArffTableModel_SetValueAt_Default_Error_Text_Front") + type + Messages.getString("ArffTableModel_SetValueAt_Default_Error_Text_End"));
      }
      
    }
    
    if ((notify) && (!("" + oldValue).equals("" + aValue))) {
      notifyListener(new TableModelEvent(this, rowIndex, columnIndex));
    }
  }
  




  public void addTableModelListener(TableModelListener l)
  {
    m_Listeners.add(l);
  }
  





  public void removeTableModelListener(TableModelListener l)
  {
    m_Listeners.remove(l);
  }
  








  public void notifyListener(TableModelEvent e)
  {
    if (!isNotificationEnabled()) {
      return;
    }
    Iterator iter = m_Listeners.iterator();
    while (iter.hasNext()) {
      TableModelListener l = (TableModelListener)iter.next();
      l.tableChanged(e);
    }
  }
  


  public void clearUndo()
  {
    m_UndoList = new Vector();
  }
  





  public boolean canUndo()
  {
    return !m_UndoList.isEmpty();
  }
  






  public void undo()
  {
    if (canUndo())
    {
      File tempFile = (File)m_UndoList.get(m_UndoList.size() - 1);
      try
      {
        ObjectInputStream ooi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
        Instances inst = (Instances)ooi.readObject();
        ooi.close();
        

        setInstances(inst);
        notifyListener(new TableModelEvent(this, -1));
        notifyListener(new TableModelEvent(this));
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      tempFile.delete();
      

      m_UndoList.remove(m_UndoList.size() - 1);
    }
  }
  








  public void addUndoPoint()
  {
    if (!isUndoEnabled()) {
      return;
    }
    if (getInstances() != null) {
      try
      {
        File tempFile = File.createTempFile("arffviewer", null);
        tempFile.deleteOnExit();
        

        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
        oos.writeObject(getInstances());
        oos.flush();
        oos.close();
        

        m_UndoList.add(tempFile);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
