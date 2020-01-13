package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.JPanel;
import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.Instances;
import weka.core.Tag;
import weka.core.Utils;
import weka.core.xml.KOML;
import weka.core.xml.XStream;
import weka.gui.Logger;


































public class SerializedModelSaver
  extends JPanel
  implements BeanCommon, Visible, BatchClassifierListener, IncrementalClassifierListener, BatchClustererListener, EnvironmentHandler, Serializable
{
  private static final long serialVersionUID = 3956528599473814287L;
  protected BeanVisual m_visual = new BeanVisual("AbstractDataSink", "weka/gui/beans/icons/SerializedModelSaver.gif", "weka/gui/beans/icons/SerializedModelSaver_animated.gif");
  






  protected Object m_listenee = null;
  



  protected transient Logger m_logger = null;
  



  private String m_filenamePrefix = "";
  



  private File m_directory = new File(System.getProperty("user.dir"));
  

  private Tag m_fileFormat;
  

  public static final int BINARY = 0;
  

  public static final int KOMLV = 1;
  

  public static final int XSTREAM = 2;
  

  public static final String FILE_EXTENSION = "model";
  

  private boolean m_useRelativePath = false;
  






  public static ArrayList<Tag> s_fileFormatsAvailable = new ArrayList();
  static { Messages.getInstance();Messages.getInstance();s_fileFormatsAvailable.add(new Tag(0, Messages.getString("SerializedModelSaver_FileFormatsAvailable_Text_First") + "model" + Messages.getString("SerializedModelSaver_FileFormatsAvailable_Text_Second"), "", false));
    



    if (KOML.isPresent()) {
      Messages.getInstance();Messages.getInstance();s_fileFormatsAvailable.add(new Tag(1, Messages.getString("SerializedModelSaver_FileFormatsAvailable_Text_Third") + ".koml" + "model" + Messages.getString("SerializedModelSaver_FileFormatsAvailable_Text_Fourth"), "", false));
    }
    





    if (XStream.isPresent()) {
      Messages.getInstance();Messages.getInstance();s_fileFormatsAvailable.add(new Tag(2, Messages.getString("SerializedModelSaver_FileFormatsAvailable_Text_Fifth") + ".xstream" + "model" + Messages.getString("SerializedModelSaver_FileFormatsAvailable_Text_Sixth"), "", false));
    }
  }
  






  protected transient Environment m_env;
  




  public SerializedModelSaver()
  {
    useDefaultVisual();
    setLayout(new BorderLayout());
    add(m_visual, "Center");
    m_fileFormat = ((Tag)s_fileFormatsAvailable.get(0));
    
    m_env = Environment.getSystemWide();
  }
  





  public void setCustomName(String name)
  {
    m_visual.setText(name);
  }
  





  public String getCustomName()
  {
    return m_visual.getText();
  }
  




  public void useDefaultVisual()
  {
    m_visual.loadIcons("weka/gui/beans/icons/SerializedModelSaver.gif", "weka/gui/beans/icons/SerializedModelSaver_animated.gif");
    
    m_visual.setText("SerializedModelSaver");
  }
  





  public void setVisual(BeanVisual newVisual)
  {
    m_visual = newVisual;
  }
  




  public BeanVisual getVisual()
  {
    return m_visual;
  }
  







  public boolean connectionAllowed(EventSetDescriptor esd)
  {
    return connectionAllowed(esd.getName());
  }
  







  public boolean connectionAllowed(String eventName)
  {
    return m_listenee == null;
  }
  









  public synchronized void connectionNotification(String eventName, Object source)
  {
    if (connectionAllowed(eventName)) {
      m_listenee = source;
    }
  }
  









  public synchronized void disconnectionNotification(String eventName, Object source)
  {
    if (m_listenee == source) {
      m_listenee = null;
    }
  }
  





  public void setLog(Logger logger)
  {
    m_logger = logger;
  }
  




  public void stop()
  {
    if ((m_listenee instanceof BeanCommon)) {
      ((BeanCommon)m_listenee).stop();
    }
  }
  






  public boolean isBusy()
  {
    return false;
  }
  






  protected String sanitizeFilename(String filename)
  {
    return filename.replaceAll("\\\\", "_").replaceAll(":", "_").replaceAll("/", "_");
  }
  






  public void acceptClusterer(BatchClustererEvent ce)
  {
    if ((ce.getTestSet() == null) || (ce.getTestOrTrain() == BatchClustererEvent.TEST) || (ce.getTestSet().isStructureOnly()))
    {

      return;
    }
    
    Instances trainHeader = ce.getTestSet().getDataSet().stringFreeStructure();
    String titleString = ce.getClusterer().getClass().getName();
    titleString = titleString.substring(titleString.lastIndexOf('.') + 1, titleString.length());
    

    String prefix = "";
    try {
      prefix = m_env.substitute(m_filenamePrefix);
    } catch (Exception ex) {
      Messages.getInstance();Messages.getInstance();String message = Messages.getString("SerializedModelSaver_AcceptClusterer_Message_Text_First") + statusMessagePrefix() + Messages.getString("SerializedModelSaver_AcceptClusterer_Message_Text_Second") + ex.getMessage();
      




      if (m_logger != null) {
        m_logger.logMessage(message);
        Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("SerializedModelSaver_AcceptClusterer_StatusMessage_Text_First"));
      }
      else
      {
        System.err.println(message);
      }
      return;
    }
    String fileName = "" + prefix + titleString + "_" + ce.getSetNumber() + "_" + ce.getMaxSetNumber();
    
    fileName = sanitizeFilename(fileName);
    
    String dirName = m_directory.getPath();
    try {
      dirName = m_env.substitute(dirName);
    } catch (Exception ex) {
      Messages.getInstance();Messages.getInstance();String message = Messages.getString("SerializedModelSaver_AcceptClusterer_Message_Text_Third") + statusMessagePrefix() + Messages.getString("SerializedModelSaver_AcceptClusterer_Message_Text_Fourth") + ex.getMessage();
      




      if (m_logger != null) {
        m_logger.logMessage(message);
        Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("SerializedModelSaver_AcceptClusterer_StatusMessage_Text_Second"));
      }
      else
      {
        System.err.println(message);
      }
      return;
    }
    File tempFile = new File(dirName);
    fileName = tempFile.getAbsolutePath() + File.separator + fileName;
    
    saveModel(fileName, trainHeader, ce.getClusterer());
  }
  





  public void acceptClassifier(IncrementalClassifierEvent ce)
  {
    if (ce.getStatus() == 2)
    {
      Instances header = ce.getStructure();
      String titleString = ce.getClassifier().getClass().getName();
      titleString = titleString.substring(titleString.lastIndexOf('.') + 1, titleString.length());
      

      String prefix = "";
      try {
        prefix = m_env.substitute(m_filenamePrefix);
      } catch (Exception ex) {
        Messages.getInstance();Messages.getInstance();String message = Messages.getString("SerializedModelSaver_AcceptClassifier_Message_Text_First") + statusMessagePrefix() + Messages.getString("SerializedModelSaver_AcceptClassifier_Message_Text_Second") + ex.getMessage();
        




        if (m_logger != null) {
          m_logger.logMessage(message);
          Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("SerializedModelSaver_AcceptClassifier_StatusMessage_Text_First"));


        }
        else
        {

          System.err.println(message);
        }
        return;
      }
      
      String fileName = "" + prefix + titleString;
      fileName = sanitizeFilename(fileName);
      
      String dirName = m_directory.getPath();
      try {
        dirName = m_env.substitute(dirName);
      } catch (Exception ex) {
        Messages.getInstance();Messages.getInstance();String message = Messages.getString("SerializedModelSaver_AcceptClassifier_Message_Text_Third") + statusMessagePrefix() + Messages.getString("SerializedModelSaver_AcceptClassifier_Message_Text_Fourth") + ex.getMessage();
        




        if (m_logger != null) {
          m_logger.logMessage(message);
          Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("SerializedModelSaver_AcceptClassifier_StatusMessage_Text_Second"));


        }
        else
        {

          System.err.println(message);
        }
        return;
      }
      File tempFile = new File(dirName);
      
      fileName = tempFile.getAbsolutePath() + File.separator + fileName;
      
      saveModel(fileName, header, ce.getClassifier());
    }
  }
  





  public void acceptClassifier(BatchClassifierEvent ce)
  {
    if ((ce.getTrainSet() == null) || (ce.getTrainSet().isStructureOnly())) {
      return;
    }
    Instances trainHeader = new Instances(ce.getTrainSet().getDataSet(), 0);
    String titleString = ce.getClassifier().getClass().getName();
    titleString = titleString.substring(titleString.lastIndexOf('.') + 1, titleString.length());
    

    String prefix = "";
    try {
      prefix = m_env.substitute(m_filenamePrefix);
    } catch (Exception ex) {
      Messages.getInstance();Messages.getInstance();String message = Messages.getString("SerializedModelSaver_AcceptClassifier_Message_Text_Fifth") + statusMessagePrefix() + Messages.getString("SerializedModelSaver_AcceptClassifier_Message_Text_Sixth") + ex.getMessage();
      




      if (m_logger != null) {
        m_logger.logMessage(message);
        Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("SerializedModelSaver_AcceptClassifier_StatusMessage_Text_Third"));
      }
      else
      {
        System.err.println(message);
      }
      return;
    }
    
    String fileName = "" + prefix + titleString + "_" + ce.getSetNumber() + "_" + ce.getMaxSetNumber();
    
    fileName = sanitizeFilename(fileName);
    
    String dirName = m_directory.getPath();
    try {
      dirName = m_env.substitute(dirName);
    } catch (Exception ex) {
      Messages.getInstance();Messages.getInstance();String message = Messages.getString("SerializedModelSaver_AcceptClassifier_Message_Text_Seventh") + statusMessagePrefix() + Messages.getString("SerializedModelSaver_AcceptClassifier_Message_Text_Eighth") + ex.getMessage();
      




      if (m_logger != null) {
        m_logger.logMessage(message);
        Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("SerializedModelSaver_AcceptClassifier_StatusMessage_Text_Fourth"));
      }
      else
      {
        System.err.println(message);
      }
      return;
    }
    File tempFile = new File(dirName);
    
    fileName = tempFile.getAbsolutePath() + File.separator + fileName;
    
    saveModel(fileName, trainHeader, ce.getClassifier());
  }
  



  private void saveModel(String fileName, Instances trainHeader, Object model)
  {
    m_fileFormat = validateFileFormat(m_fileFormat);
    if (m_fileFormat == null)
    {
      m_fileFormat = ((Tag)s_fileFormatsAvailable.get(0));
    }
    try {
      switch (m_fileFormat.getID()) {
      case 1: 
        fileName = fileName + ".koml" + "model";
        saveKOML(new File(fileName), model, trainHeader);
        break;
      case 2: 
        fileName = fileName + ".xstream" + "model";
        saveXStream(new File(fileName), model, trainHeader);
        break;
      default: 
        fileName = fileName + "." + "model";
        saveBinary(new File(fileName), model, trainHeader);
      }
    }
    catch (Exception ex) {
      Messages.getInstance();System.err.println(Messages.getString("SerializedModelSaver_SaveModel_Error_Text"));
      
      if (m_logger != null) {
        Messages.getInstance();Messages.getInstance();m_logger.logMessage(Messages.getString("SerializedModelSaver_SaveModel_LogMessage_Text_First") + statusMessagePrefix() + Messages.getString("SerializedModelSaver_SaveModel_LogMessage_Text_Second") + " : " + ex.getMessage());
        





        Messages.getInstance();m_logger.statusMessage(statusMessagePrefix() + Messages.getString("SerializedModelSaver_SaveModel_StatusMessage_Text"));
      }
    }
  }
  









  public static void saveBinary(File saveTo, Object model, Instances header)
    throws IOException
  {
    ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(saveTo)));
    
    os.writeObject(model);
    
    if (header != null) {
      os.writeObject(header);
    }
    os.close();
  }
  







  public static void saveKOML(File saveTo, Object model, Instances header)
    throws Exception
  {
    Vector v = new Vector();
    v.add(model);
    if (header != null) {
      v.add(header);
    }
    v.trimToSize();
    KOML.write(saveTo.getAbsolutePath(), v);
  }
  







  public static void saveXStream(File saveTo, Object model, Instances header)
    throws Exception
  {
    Vector v = new Vector();
    v.add(model);
    if (header != null) {
      v.add(header);
    }
    v.trimToSize();
    XStream.write(saveTo.getAbsolutePath(), v);
  }
  




  public File getDirectory()
  {
    return m_directory;
  }
  




  public void setDirectory(File d)
  {
    m_directory = d;
    if (m_useRelativePath) {
      try {
        m_directory = Utils.convertToRelativePath(m_directory);
      }
      catch (Exception ex) {}
    }
  }
  





  public void setUseRelativePath(boolean rp)
  {
    m_useRelativePath = rp;
  }
  





  public boolean getUseRelativePath()
  {
    return m_useRelativePath;
  }
  




  public String getPrefix()
  {
    return m_filenamePrefix;
  }
  




  public void setPrefix(String p)
  {
    m_filenamePrefix = p;
  }
  




  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("SerializedModelSaver_GlobalInfo_Text");
  }
  





  public void setFileFormat(Tag ff)
  {
    m_fileFormat = ff;
  }
  




  public Tag getFileFormat()
  {
    return m_fileFormat;
  }
  





  public Tag validateFileFormat(Tag ff)
  {
    Tag r = ff;
    if (ff.getID() == 0) {
      return ff;
    }
    
    if ((ff.getID() == 1) && (!KOML.isPresent())) {
      r = null;
    }
    
    if ((ff.getID() == 2) && (!XStream.isPresent())) {
      r = null;
    }
    
    return r;
  }
  
  private String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|";
  }
  





  public void setEnvironment(Environment env)
  {
    m_env = env;
  }
  

  private void readObject(ObjectInputStream aStream)
    throws IOException, ClassNotFoundException
  {
    aStream.defaultReadObject();
    

    m_env = Environment.getSystemWide();
  }
}
