package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Vector;
import javax.swing.JPanel;
import weka.core.Instances;
import weka.gui.Logger;


































public class ClassAssigner
  extends JPanel
  implements Visible, DataSourceListener, TrainingSetListener, TestSetListener, DataSource, TrainingSetProducer, TestSetProducer, BeanCommon, EventConstraints, Serializable, InstanceListener, StructureProducer
{
  private static final long serialVersionUID = 4011131665025817924L;
  private String m_classColumn = "last";
  
  private Instances m_connectedFormat;
  
  private Object m_trainingProvider;
  
  private Object m_testProvider;
  
  private Object m_dataProvider;
  private Object m_instanceProvider;
  private Vector m_trainingListeners = new Vector();
  private Vector m_testListeners = new Vector();
  private Vector m_dataListeners = new Vector();
  private Vector m_instanceListeners = new Vector();
  
  private Vector m_dataFormatListeners = new Vector();
  
  protected transient Logger m_logger = null;
  
  protected BeanVisual m_visual = new BeanVisual("ClassAssigner", "weka/gui/beans/icons/ClassAssigner.gif", "weka/gui/beans/icons/ClassAssigner_animated.gif");
  







  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("ClassAssigner_GlobalInfo_Text");
  }
  
  public ClassAssigner() {
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
  




  public String classColumnTipText()
  {
    Messages.getInstance();return Messages.getString("ClassAssigner_ClassColumnTipText_Text");
  }
  
  private Instances getUpstreamStructure() {
    if ((m_dataProvider != null) && ((m_dataProvider instanceof StructureProducer))) {
      return ((StructureProducer)m_dataProvider).getStructure("dataSet");
    }
    if ((m_trainingProvider != null) && ((m_trainingProvider instanceof StructureProducer)))
    {
      return ((StructureProducer)m_trainingProvider).getStructure("trainingSet");
    }
    if ((m_testProvider != null) && ((m_testProvider instanceof StructureProducer))) {
      return ((StructureProducer)m_testProvider).getStructure("testSet");
    }
    if ((m_instanceProvider != null) && ((m_instanceProvider instanceof StructureProducer)))
    {
      return ((StructureProducer)m_instanceProvider).getStructure("instance");
    }
    return null;
  }
  












  public Instances getStructure(String eventName)
  {
    if ((!eventName.equals("trainingSet")) && (!eventName.equals("testSet")) && (!eventName.equals("dataSet")) && (!eventName.equals("instance")))
    {
      return null;
    }
    if ((m_trainingProvider == null) && (m_testProvider == null) && (m_dataProvider == null) && (m_instanceProvider == null))
    {
      return null;
    }
    
    if ((eventName.equals("dataSet")) && (m_dataListeners.size() == 0))
    {

      return null;
    }
    
    if ((eventName.equals("trainingSet")) && (m_trainingListeners.size() == 0))
    {

      return null;
    }
    
    if ((eventName.equals("testSet")) && (m_testListeners.size() == 0))
    {

      return null;
    }
    
    if ((eventName.equals("instance")) && (m_instanceListeners.size() == 0))
    {

      return null;
    }
    
    if (m_connectedFormat == null) {
      m_connectedFormat = getUpstreamStructure();
    }
    
    assignClass(m_connectedFormat);
    return m_connectedFormat;
  }
  













  public Instances getConnectedFormat()
  {
    if (m_connectedFormat == null)
    {

      m_connectedFormat = getUpstreamStructure();
    }
    
    return m_connectedFormat;
  }
  
  public void setClassColumn(String col) {
    m_classColumn = col;
    if (m_connectedFormat != null) {
      assignClass(m_connectedFormat);
    }
  }
  
  public String getClassColumn() {
    return m_classColumn;
  }
  
  public void acceptDataSet(DataSetEvent e) {
    Instances dataSet = e.getDataSet();
    assignClass(dataSet);
    notifyDataListeners(e);
    if (e.isStructureOnly()) {
      m_connectedFormat = e.getDataSet();
      
      notifyDataFormatListeners();
    }
  }
  
  public void acceptTrainingSet(TrainingSetEvent e) {
    Instances trainingSet = e.getTrainingSet();
    assignClass(trainingSet);
    notifyTrainingListeners(e);
    
    if (e.isStructureOnly()) {
      m_connectedFormat = e.getTrainingSet();
      
      notifyDataFormatListeners();
    }
  }
  
  public void acceptTestSet(TestSetEvent e) {
    Instances testSet = e.getTestSet();
    assignClass(testSet);
    notifyTestListeners(e);
    
    if (e.isStructureOnly()) {
      m_connectedFormat = e.getTestSet();
      
      notifyDataFormatListeners();
    }
  }
  
  public void acceptInstance(InstanceEvent e) {
    if (e.getStatus() == 0)
    {
      m_connectedFormat = e.getStructure();
      

      assignClass(m_connectedFormat);
      notifyInstanceListeners(e);
      

      Messages.getInstance();System.err.println(Messages.getString("ClassAssigner_AcceptInstance_Error_Text"));
      notifyDataFormatListeners();
    }
    else
    {
      notifyInstanceListeners(e);
    }
  }
  
  private void assignClass(Instances dataSet) {
    int classCol = -1;
    if (m_classColumn.toLowerCase().compareTo("last") == 0) {
      dataSet.setClassIndex(dataSet.numAttributes() - 1);
    } else if (m_classColumn.toLowerCase().compareTo("first") == 0) {
      dataSet.setClassIndex(0);
    } else {
      classCol = Integer.parseInt(m_classColumn) - 1;
      if (classCol > dataSet.numAttributes() - 1) {
        if (m_logger != null) {
          Messages.getInstance();m_logger.logMessage(Messages.getString("ClassAssigner_AssignClass_LogMessage_Text"));
        }
      } else {
        dataSet.setClassIndex(classCol);
      }
    }
  }
  
  protected void notifyTestListeners(TestSetEvent tse) {
    Vector l;
    synchronized (this) {
      l = (Vector)m_testListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        Messages.getInstance();System.err.println(Messages.getString("ClassAssigner_NotifyTestListeners_Error_Text"));
        ((TestSetListener)l.elementAt(i)).acceptTestSet(tse);
      }
    }
  }
  
  protected void notifyTrainingListeners(TrainingSetEvent tse) {
    Vector l;
    synchronized (this) {
      l = (Vector)m_trainingListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        Messages.getInstance();System.err.println(Messages.getString("ClassAssigner_NotifyTrainingListeners_Error_Text"));
        ((TrainingSetListener)l.elementAt(i)).acceptTrainingSet(tse);
      }
    }
  }
  
  protected void notifyDataListeners(DataSetEvent tse) {
    Vector l;
    synchronized (this) {
      l = (Vector)m_dataListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        Messages.getInstance();System.err.println(Messages.getString("ClassAssigner_NotifyDataListeners_Error_Text"));
        ((DataSourceListener)l.elementAt(i)).acceptDataSet(tse);
      }
    }
  }
  
  protected void notifyInstanceListeners(InstanceEvent tse) {
    Vector l;
    synchronized (this) {
      l = (Vector)m_instanceListeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++)
      {


        ((InstanceListener)l.elementAt(i)).acceptInstance(tse);
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
      for (int i = 0; i < l.size(); i++)
      {

        ((DataFormatListener)l.elementAt(i)).newDataFormat(dse);
      }
    }
  }
  
  public synchronized void addInstanceListener(InstanceListener tsl) {
    m_instanceListeners.addElement(tsl);
    if (m_connectedFormat != null) {
      InstanceEvent e = new InstanceEvent(this, m_connectedFormat);
      tsl.acceptInstance(e);
    }
  }
  
  public synchronized void removeInstanceListener(InstanceListener tsl) {
    m_instanceListeners.removeElement(tsl);
  }
  
  public synchronized void addDataSourceListener(DataSourceListener tsl) {
    m_dataListeners.addElement(tsl);
    
    if (m_connectedFormat != null) {
      DataSetEvent e = new DataSetEvent(this, m_connectedFormat);
      tsl.acceptDataSet(e);
    }
  }
  
  public synchronized void removeDataSourceListener(DataSourceListener tsl) {
    m_dataListeners.removeElement(tsl);
  }
  
  public synchronized void addTrainingSetListener(TrainingSetListener tsl) {
    m_trainingListeners.addElement(tsl);
    
    if (m_connectedFormat != null) {
      TrainingSetEvent e = new TrainingSetEvent(this, m_connectedFormat);
      tsl.acceptTrainingSet(e);
    }
  }
  
  public synchronized void removeTrainingSetListener(TrainingSetListener tsl) {
    m_trainingListeners.removeElement(tsl);
  }
  
  public synchronized void addTestSetListener(TestSetListener tsl) {
    m_testListeners.addElement(tsl);
    
    if (m_connectedFormat != null) {
      TestSetEvent e = new TestSetEvent(this, m_connectedFormat);
      tsl.acceptTestSet(e);
    }
  }
  
  public synchronized void removeTestSetListener(TestSetListener tsl) {
    m_testListeners.removeElement(tsl);
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
    m_visual.loadIcons("weka/gui/beans/icons/ClassAssigner.gif", "weka/gui/beans/icons/ClassAssigner_animated.gif");
  }
  








  public boolean connectionAllowed(String eventName)
  {
    if ((eventName.compareTo("trainingSet") == 0) && ((m_trainingProvider != null) || (m_dataProvider != null) || (m_instanceProvider != null)))
    {

      return false;
    }
    
    if ((eventName.compareTo("testSet") == 0) && (m_testProvider != null))
    {
      return false;
    }
    
    if (((eventName.compareTo("instance") == 0) && (m_instanceProvider != null)) || (m_trainingProvider != null) || (m_dataProvider != null))
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
    if (connectionAllowed(eventName)) {
      if (eventName.compareTo("trainingSet") == 0) {
        m_trainingProvider = source;
      } else if (eventName.compareTo("testSet") == 0) {
        m_testProvider = source;
      } else if (eventName.compareTo("dataSet") == 0) {
        m_dataProvider = source;
      } else if (eventName.compareTo("instance") == 0) {
        m_instanceProvider = source;
      }
      m_connectedFormat = null;
    }
  }
  









  public synchronized void disconnectionNotification(String eventName, Object source)
  {
    if ((eventName.compareTo("trainingSet") == 0) && 
      (m_trainingProvider == source)) {
      m_trainingProvider = null;
    }
    
    if ((eventName.compareTo("testSet") == 0) && 
      (m_testProvider == source)) {
      m_testProvider = null;
    }
    
    if ((eventName.compareTo("dataSet") == 0) && 
      (m_dataProvider == source)) {
      m_dataProvider = null;
    }
    

    if ((eventName.compareTo("instance") == 0) && 
      (m_instanceProvider == source)) {
      m_instanceProvider = null;
    }
    
    m_connectedFormat = null;
  }
  
  public void setLog(Logger logger) {
    m_logger = logger;
  }
  
  public void stop()
  {
    if ((m_trainingProvider != null) && ((m_trainingProvider instanceof BeanCommon))) {
      ((BeanCommon)m_trainingProvider).stop();
    }
    
    if ((m_testProvider != null) && ((m_testProvider instanceof BeanCommon))) {
      ((BeanCommon)m_testProvider).stop();
    }
    
    if ((m_dataProvider != null) && ((m_dataProvider instanceof BeanCommon))) {
      ((BeanCommon)m_dataProvider).stop();
    }
    
    if ((m_instanceProvider != null) && ((m_instanceProvider instanceof BeanCommon))) {
      ((BeanCommon)m_instanceProvider).stop();
    }
  }
  





  public boolean isBusy()
  {
    return false;
  }
  








  public boolean eventGeneratable(String eventName)
  {
    if (eventName.compareTo("trainingSet") == 0) {
      if (m_trainingProvider == null) {
        return false;
      }
      if (((m_trainingProvider instanceof EventConstraints)) && 
        (!((EventConstraints)m_trainingProvider).eventGeneratable("trainingSet")))
      {
        return false;
      }
    }
    


    if (eventName.compareTo("dataSet") == 0) {
      if (m_dataProvider == null) {
        if (m_instanceProvider == null) {
          m_connectedFormat = null;
          notifyDataFormatListeners();
        }
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
    


    if (eventName.compareTo("instance") == 0) {
      if (m_instanceProvider == null) {
        if (m_dataProvider == null) {
          m_connectedFormat = null;
          notifyDataFormatListeners();
        }
        return false;
      }
      if (((m_instanceProvider instanceof EventConstraints)) && 
        (!((EventConstraints)m_instanceProvider).eventGeneratable("instance")))
      {
        m_connectedFormat = null;
        notifyDataFormatListeners();
        return false;
      }
    }
    


    if (eventName.compareTo("testSet") == 0) {
      if (m_testProvider == null) {
        return false;
      }
      if (((m_testProvider instanceof EventConstraints)) && 
        (!((EventConstraints)m_testProvider).eventGeneratable("testSet")))
      {
        return false;
      }
    }
    

    return true;
  }
}
