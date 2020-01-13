package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Vector;
import javax.swing.JPanel;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.SwapValues;
import weka.gui.Logger;



































public class ClassValuePicker
  extends JPanel
  implements Visible, DataSourceListener, BeanCommon, EventConstraints, Serializable, StructureProducer
{
  private static final long serialVersionUID = -1196143276710882989L;
  private String m_classValue;
  private Instances m_connectedFormat;
  private Object m_dataProvider;
  private Vector m_dataListeners = new Vector();
  private Vector m_dataFormatListeners = new Vector();
  
  protected transient Logger m_logger = null;
  
  protected BeanVisual m_visual = new BeanVisual("ClassValuePicker", "weka/gui/beans/icons/ClassValuePicker.gif", "weka/gui/beans/icons/ClassValuePicker_animated.gif");
  







  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("ClassValuePicker_GlobalInfo_Text");
  }
  
  public ClassValuePicker() {
    setLayout(new BorderLayout());
    add(m_visual, "Center");
  }
  




  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  




  public String getCustomName()
  {
    return m_visual.getText();
  }
  
  public Instances getStructure(String eventName) {
    if (!eventName.equals("dataSet")) {
      return null;
    }
    if (m_dataProvider == null) {
      return null;
    }
    
    if ((m_dataProvider != null) && ((m_dataProvider instanceof StructureProducer))) {
      m_connectedFormat = ((StructureProducer)m_dataProvider).getStructure("dataSet");
    }
    
    return m_connectedFormat;
  }
  
  protected Instances getStructure() {
    if (m_dataProvider != null) {
      return getStructure("dataSet");
    }
    
    return null;
  }
  








  public Instances getConnectedFormat()
  {
    return getStructure();
  }
  





  public void setClassValue(String value)
  {
    m_classValue = value;
    if (m_connectedFormat != null) {
      notifyDataFormatListeners();
    }
  }
  





  public String getClassValue()
  {
    return m_classValue;
  }
  
  public void acceptDataSet(DataSetEvent e) {
    if ((e.isStructureOnly()) && (
      (m_connectedFormat == null) || (!m_connectedFormat.equalHeaders(e.getDataSet()))))
    {
      m_connectedFormat = new Instances(e.getDataSet(), 0);
      
      notifyDataFormatListeners();
    }
    
    Instances dataSet = e.getDataSet();
    Instances newDataSet = assignClassValue(dataSet);
    
    if (newDataSet != null) {
      e = new DataSetEvent(this, newDataSet);
      notifyDataListeners(e);
    }
  }
  
  private Instances assignClassValue(Instances dataSet) {
    if (dataSet.classIndex() < 0) {
      if (m_logger != null) {
        Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("ClassValuePicker_AssignClassValue_LogMessage_Text_First") + statusMessagePrefix() + Messages.getString("ClassValuePicker_AssignClassValue_LogMessage_Text_Second"));
        


        Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("ClassValuePicker_AssignClassValue_StatusMessage_Text_First"));
      }
      
      return dataSet;
    }
    
    if (dataSet.classAttribute().isNumeric()) {
      if (m_logger != null) {
        Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("ClassValuePicker_AssignClassValue_LogMessage_Text_Third") + statusMessagePrefix() + Messages.getString("ClassValuePicker_AssignClassValue_LogMessage_Text_Fourth"));
        


        Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("ClassValuePicker_AssignClassValue_StatusMessage_Text_Second"));
      }
      

      return dataSet;
    }
    if (m_logger != null) {
      Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("ClassValuePicker_AssignClassValue_StatusMessage_Text_Third"));
    }
    

    if (((m_classValue == null) || (m_classValue.length() == 0)) && (dataSet.numInstances() > 0))
    {

      if (m_logger != null) {
        m_logger.logMessage("[ClassValuePicker] " + statusMessagePrefix() + " Class value to consider as positive has not been set" + " (ClassValuePicker)");
        



        m_logger.statusMessage(statusMessagePrefix() + "WARNING: Class value to consider as positive has not been set.");
      }
      
      return dataSet;
    }
    
    if (m_classValue == null)
    {


      return dataSet;
    }
    
    Attribute classAtt = dataSet.classAttribute();
    int classValueIndex = -1;
    


    if ((m_classValue.startsWith("/")) && (m_classValue.length() > 1)) {
      String remainder = m_classValue.substring(1);
      remainder = remainder.trim();
      if (remainder.equalsIgnoreCase("first")) {
        classValueIndex = 0;
      } else if (remainder.equalsIgnoreCase("last")) {
        classValueIndex = classAtt.numValues() - 1;
      } else {
        try
        {
          classValueIndex = Integer.parseInt(remainder);
          classValueIndex--;
          
          if ((classValueIndex < 0) || (classValueIndex > classAtt.numValues() - 1))
          {
            if (m_logger != null) {
              m_logger.logMessage("[ClassValuePicker] " + statusMessagePrefix() + " Class value index is out of range!" + " (ClassValuePicker)");
              



              m_logger.statusMessage(statusMessagePrefix() + "ERROR: Class value index is out of range!.");
            }
          }
        }
        catch (NumberFormatException n) {
          if (m_logger != null) {
            m_logger.logMessage("[ClassValuePicker] " + statusMessagePrefix() + " Unable to parse supplied class value index as an integer" + " (ClassValuePicker)");
            



            m_logger.statusMessage(statusMessagePrefix() + "WARNING: Unable to parse supplied class value index " + "as an integer.");
            

            return dataSet;
          }
        }
      }
    }
    else {
      classValueIndex = classAtt.indexOfValue(m_classValue.trim());
    }
    
    if (classValueIndex < 0) {
      return null;
    }
    
    if (classValueIndex != 0) {
      try
      {
        SwapValues sv = new SwapValues();
        sv.setAttributeIndex("" + (dataSet.classIndex() + 1));
        sv.setFirstValueIndex("first");
        sv.setSecondValueIndex("" + (classValueIndex + 1));
        sv.setInputFormat(dataSet);
        Instances newDataSet = Filter.useFilter(dataSet, sv);
        newDataSet.setRelationName(dataSet.relationName());
        return newDataSet;
      } catch (Exception ex) {
        if (m_logger != null) {
          m_logger.logMessage("[ClassValuePicker] " + statusMessagePrefix() + " Unable to swap class attibute values.");
          


          m_logger.statusMessage(statusMessagePrefix() + "ERROR: (See log for details)");
          
          return null;
        }
      }
    }
    return dataSet;
  }
  
  protected void notifyDataListeners(DataSetEvent tse) {
    Vector l;
    synchronized (this) {
      l = (Vector)m_dataListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        Messages.getInstance();System.err.println(Messages.getString("ClassValuePicker_NotifyDataListeners_Text"));
        ((DataSourceListener)l.elementAt(i)).acceptDataSet(tse);
      }
    }
  }
  
  protected void notifyDataFormatListeners() {
    Vector l;
    synchronized (this) {
      l = (Vector)m_dataFormatListeners.clone();
    }
    if (l.size() > 0) {
      DataSetEvent dse = new DataSetEvent(this, m_connectedFormat);
      for (int i = 0; i < l.size(); i++) {
        ((DataFormatListener)l.elementAt(i)).newDataFormat(dse);
      }
    }
  }
  
  public synchronized void addDataSourceListener(DataSourceListener tsl) {
    m_dataListeners.addElement(tsl);
  }
  
  public synchronized void removeDataSourceListener(DataSourceListener tsl) {
    m_dataListeners.removeElement(tsl);
  }
  
  public synchronized void addDataFormatListener(DataFormatListener dfl) {
    m_dataFormatListeners.addElement(dfl);
  }
  
  public synchronized void removeDataFormatListener(DataFormatListener dfl) {
    m_dataFormatListeners.removeElement(dfl);
  }
  
  public void setVisual(BeanVisual newVisual) {
    m_visual = newVisual;
  }
  
  public BeanVisual getVisual() {
    return m_visual;
  }
  
  public void useDefaultVisual() {
    m_visual.loadIcons("weka/gui/beans/icons/ClassValuePicker.gif", "weka/gui/beans/icons/ClassValuePicker_animated.gif");
  }
  








  public boolean connectionAllowed(String eventName)
  {
    if ((eventName.compareTo("dataSet") == 0) && (m_dataProvider != null))
    {
      return false;
    }
    
    return true;
  }
  







  public boolean connectionAllowed(EventSetDescriptor esd)
  {
    return connectionAllowed(esd.getName());
  }
  








  public synchronized void connectionNotification(String eventName, Object source)
  {
    if ((connectionAllowed(eventName)) && 
      (eventName.compareTo("dataSet") == 0)) {
      m_dataProvider = source;
    }
    
    m_connectedFormat = null;
  }
  









  public synchronized void disconnectionNotification(String eventName, Object source)
  {
    if ((eventName.compareTo("dataSet") == 0) && 
      (m_dataProvider == source)) {
      m_dataProvider = null;
    }
    
    m_connectedFormat = null;
  }
  
  public void setLog(Logger logger) {
    m_logger = logger;
  }
  




  public void stop() {}
  



  public boolean isBusy()
  {
    return false;
  }
  








  public boolean eventGeneratable(String eventName)
  {
    if (eventName.compareTo("dataSet") != 0) {
      return false;
    }
    
    if (eventName.compareTo("dataSet") == 0) {
      if (m_dataProvider == null) {
        m_connectedFormat = null;
        notifyDataFormatListeners();
        return false;
      }
      if (((m_dataProvider instanceof EventConstraints)) && 
        (!((EventConstraints)m_dataProvider).eventGeneratable("dataSet")))
      {
        m_connectedFormat = null;
        notifyDataFormatListeners();
        return false;
      }
    }
    

    return true;
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|";
  }
}
