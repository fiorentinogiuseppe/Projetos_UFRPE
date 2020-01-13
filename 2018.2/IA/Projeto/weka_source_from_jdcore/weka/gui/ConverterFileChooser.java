package weka.gui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintStream;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import weka.core.Capabilities;
import weka.core.Instances;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.AbstractLoader;
import weka.core.converters.AbstractSaver;
import weka.core.converters.ConverterUtils;
import weka.core.converters.FileSourcedConverter;




























































public class ConverterFileChooser
  extends JFileChooser
{
  private static final long serialVersionUID = -5373058011025481738L;
  public static final int UNHANDLED_DIALOG = 0;
  public static final int LOADER_DIALOG = 1;
  public static final int SAVER_DIALOG = 2;
  protected ConverterFileChooser m_Self;
  protected static Vector<ExtensionFileFilter> m_LoaderFileFilters;
  protected static Vector<ExtensionFileFilter> m_SaverFileFilters;
  protected int m_DialogType;
  protected Object m_CurrentConverter;
  protected JButton m_ConfigureButton;
  protected PropertyChangeListener m_Listener;
  protected FileFilter m_LastFilter;
  protected Capabilities m_CapabilitiesFilter;
  protected boolean m_OverwriteWarning = true;
  

  protected boolean m_FileMustExist = true;
  





  protected boolean m_CoreConvertersOnly = false;
  
  static {
    initFilters(true, ConverterUtils.getFileLoaders());
    initFilters(false, ConverterUtils.getFileSavers());
  }
  



  public ConverterFileChooser()
  {
    m_Self = this;
  }
  




  public ConverterFileChooser(File currentDirectory)
  {
    super(currentDirectory);
    m_Self = this;
  }
  




  public ConverterFileChooser(String currentDirectory)
  {
    super(currentDirectory);
    m_Self = this;
  }
  



  protected Vector<ExtensionFileFilter> filterNonCoreLoaderFileFilters(Vector<ExtensionFileFilter> list)
  {
    Vector<ExtensionFileFilter> result;
    


    Vector<ExtensionFileFilter> result;
    


    if (!getCoreConvertersOnly()) {
      result = list;
    }
    else {
      result = new Vector();
      for (int i = 0; i < list.size(); i++) {
        ExtensionFileFilter filter = (ExtensionFileFilter)list.get(i);
        AbstractLoader loader = ConverterUtils.getLoaderForExtension(filter.getExtensions()[0]);
        if (ConverterUtils.isCoreFileLoader(loader.getClass().getName())) {
          result.add(filter);
        }
      }
    }
    return result;
  }
  



  protected Vector<ExtensionFileFilter> filterNonCoreSaverFileFilters(Vector<ExtensionFileFilter> list)
  {
    Vector<ExtensionFileFilter> result;
    


    Vector<ExtensionFileFilter> result;
    


    if (!getCoreConvertersOnly()) {
      result = list;
    }
    else {
      result = new Vector();
      for (int i = 0; i < list.size(); i++) {
        ExtensionFileFilter filter = (ExtensionFileFilter)list.get(i);
        AbstractSaver saver = ConverterUtils.getSaverForExtension(filter.getExtensions()[0]);
        if (ConverterUtils.isCoreFileSaver(saver.getClass().getName())) {
          result.add(filter);
        }
      }
    }
    return result;
  }
  



  protected Vector<ExtensionFileFilter> filterSaverFileFilters(Vector<ExtensionFileFilter> list)
  {
    Vector<ExtensionFileFilter> result;
    


    Vector<ExtensionFileFilter> result;
    


    if (m_CapabilitiesFilter == null) {
      result = list;
    }
    else {
      result = new Vector();
      
      for (int i = 0; i < list.size(); i++) {
        ExtensionFileFilter filter = (ExtensionFileFilter)list.get(i);
        AbstractSaver saver = ConverterUtils.getSaverForExtension(filter.getExtensions()[0]);
        if (saver.getCapabilities().supports(m_CapabilitiesFilter)) {
          result.add(filter);
        }
      }
    }
    return result;
  }
  














  protected static void initFilters(boolean loader, Vector<String> classnames)
  {
    if (loader) {
      m_LoaderFileFilters = new Vector();
    } else {
      m_SaverFileFilters = new Vector();
    }
    for (int i = 0; i < classnames.size(); i++) {
      String classname = (String)classnames.get(i);
      FileSourcedConverter converter;
      String[] ext;
      String desc;
      try { cls = Class.forName(classname);
        converter = (FileSourcedConverter)cls.newInstance();
        ext = converter.getFileExtensions();
        desc = converter.getFileDescription();
      }
      catch (Exception e) {
        Class cls = null;
        converter = null;
        ext = new String[0];
        desc = "";
      }
      
      if (converter != null)
      {


        if (loader) {
          for (int n = 0; n < ext.length; n++) {
            ExtensionFileFilter filter = new ExtensionFileFilter(ext[n], desc + " (*" + ext[n] + ")");
            m_LoaderFileFilters.add(filter);
          }
        }
        
        for (int n = 0; n < ext.length; n++) {
          ExtensionFileFilter filter = new ExtensionFileFilter(ext[n], desc + " (*" + ext[n] + ")");
          m_SaverFileFilters.add(filter);
        }
      }
    }
  }
  









  protected void initGUI(int dialogType)
  {
    boolean acceptAll = isAcceptAllFileFilterUsed();
    

    resetChoosableFileFilters();
    setAcceptAllFileFilterUsed(acceptAll);
    Vector<ExtensionFileFilter> list; Vector<ExtensionFileFilter> list; if (dialogType == 1) {
      list = filterNonCoreLoaderFileFilters(m_LoaderFileFilters);
    } else
      list = filterSaverFileFilters(filterNonCoreSaverFileFilters(m_SaverFileFilters));
    for (int i = 0; i < list.size(); i++) {
      addChoosableFileFilter((FileFilter)list.get(i));
    }
    if (list.size() > 0) {
      if ((m_LastFilter == null) || (!list.contains(m_LastFilter))) {
        setFileFilter((FileFilter)list.get(0));
      } else {
        setFileFilter(m_LastFilter);
      }
    }
    
    if (m_Listener != null)
      removePropertyChangeListener(m_Listener);
    m_Listener = new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("fileFilterChanged")) {
          updateCurrentConverter();
        }
      }
    };
    addPropertyChangeListener(m_Listener);
    
    updateCurrentConverter();
  }
  





  public void setCapabilitiesFilter(Capabilities value)
  {
    m_CapabilitiesFilter = ((Capabilities)value.clone());
  }
  





  public Capabilities getCapabilitiesFilter()
  {
    if (m_CapabilitiesFilter != null) {
      return (Capabilities)m_CapabilitiesFilter.clone();
    }
    return null;
  }
  





  public void setOverwriteWarning(boolean value)
  {
    m_OverwriteWarning = value;
  }
  





  public boolean getOverwriteWarning()
  {
    return m_OverwriteWarning;
  }
  




  public void setFileMustExist(boolean value)
  {
    m_FileMustExist = value;
  }
  




  public boolean getFileMustExist()
  {
    return m_FileMustExist;
  }
  






  public void setCoreConvertersOnly(boolean value)
  {
    m_CoreConvertersOnly = value;
  }
  







  public boolean getCoreConvertersOnly()
  {
    return m_CoreConvertersOnly;
  }
  







  public int showDialog(Component parent, String approveButtonText)
  {
    if (m_DialogType == 0) {
      Messages.getInstance();throw new IllegalStateException(Messages.getString("ConverterFileChooser_ShowDialog_IllegalStateException_Text"));
    }
    return super.showDialog(parent, approveButtonText);
  }
  





  public int showOpenDialog(Component parent)
  {
    m_DialogType = 1;
    m_CurrentConverter = null;
    
    initGUI(1);
    
    int result = super.showOpenDialog(parent);
    
    m_DialogType = 0;
    removePropertyChangeListener(m_Listener);
    

    if ((result == 0) && (getSelectedFile().isFile()) && 
      ((getFileFilter() instanceof ExtensionFileFilter))) {
      String filename = getSelectedFile().getAbsolutePath();
      String[] extensions = ((ExtensionFileFilter)getFileFilter()).getExtensions();
      if (!filename.endsWith(extensions[0])) {
        filename = filename + extensions[0];
        setSelectedFile(new File(filename));
      }
    }
    


    if ((result == 0) && (getFileMustExist()) && (getSelectedFile().isFile()) && (!getSelectedFile().exists()))
    {


      Messages.getInstance();Messages.getInstance();int retVal = JOptionPane.showConfirmDialog(parent, Messages.getString("ConverterFileChooser_ShowOpenDialog_RetVal_JOptionPaneShowConfirmDialog_Text_First") + getSelectedFile() + Messages.getString("ConverterFileChooser_ShowOpenDialog_RetVal_JOptionPaneShowConfirmDialog_Text_Second"));
      



      if (retVal == 0) {
        result = showOpenDialog(parent);
      } else {
        result = 1;
      }
    }
    if (result == 0) {
      m_LastFilter = getFileFilter();
      configureCurrentConverter(1);
    }
    
    return result;
  }
  





  public int showSaveDialog(Component parent)
  {
    m_DialogType = 2;
    m_CurrentConverter = null;
    
    initGUI(2);
    
    boolean acceptAll = isAcceptAllFileFilterUsed();
    



    FileFilter currentFilter = getFileFilter();
    File currentFile = getSelectedFile();
    setAcceptAllFileFilterUsed(false);
    setFileFilter(currentFilter);
    setSelectedFile(currentFile);
    
    int result = super.showSaveDialog(parent);
    

    if ((result == 0) && 
      ((getFileFilter() instanceof ExtensionFileFilter))) {
      String filename = getSelectedFile().getAbsolutePath();
      String[] extensions = ((ExtensionFileFilter)getFileFilter()).getExtensions();
      if (!filename.endsWith(extensions[0])) {
        filename = filename + extensions[0];
        setSelectedFile(new File(filename));
      }
    }
    




    currentFilter = getFileFilter();
    currentFile = getSelectedFile();
    setAcceptAllFileFilterUsed(acceptAll);
    setFileFilter(currentFilter);
    setSelectedFile(currentFile);
    
    m_DialogType = 0;
    removePropertyChangeListener(m_Listener);
    

    if ((result == 0) && (getOverwriteWarning()) && (getSelectedFile().exists()))
    {

      Messages.getInstance();Messages.getInstance();int retVal = JOptionPane.showConfirmDialog(parent, Messages.getString("ConverterFileChooser_ShowOpenDialog_RetVal_JOptionPaneShowConfirmDialog_Text_First") + getSelectedFile() + Messages.getString("ConverterFileChooser_ShowSaveDialog_RetVal_JOptionPaneShowConfirmDialog_Text_Second"));
      



      if (retVal == 0) {
        result = 0;
      } else if (retVal == 1) {
        result = showSaveDialog(parent);
      } else {
        result = 1;
      }
    }
    if (result == 0) {
      m_LastFilter = getFileFilter();
      configureCurrentConverter(2);
    }
    
    return result;
  }
  





  public AbstractFileLoader getLoader()
  {
    configureCurrentConverter(1);
    
    if ((m_CurrentConverter instanceof AbstractFileSaver)) {
      return null;
    }
    return (AbstractFileLoader)m_CurrentConverter;
  }
  





  public AbstractFileSaver getSaver()
  {
    configureCurrentConverter(2);
    
    if ((m_CurrentConverter instanceof AbstractFileLoader)) {
      return null;
    }
    return (AbstractFileSaver)m_CurrentConverter;
  }
  





  protected void updateCurrentConverter()
  {
    if (getFileFilter() == null) {
      return;
    }
    if (!isAcceptAllFileFilterUsed())
    {
      String[] extensions = ((ExtensionFileFilter)getFileFilter()).getExtensions();
      Object newConverter; Object newConverter; if (m_DialogType == 1) {
        newConverter = ConverterUtils.getLoaderForExtension(extensions[0]);
      } else {
        newConverter = ConverterUtils.getSaverForExtension(extensions[0]);
      }
      try {
        if (m_CurrentConverter == null) {
          m_CurrentConverter = newConverter;

        }
        else if (!m_CurrentConverter.getClass().equals(newConverter.getClass())) {
          m_CurrentConverter = newConverter;
        }
      }
      catch (Exception e) {
        m_CurrentConverter = null;
        e.printStackTrace();
      }
    }
    else {
      m_CurrentConverter = null;
    }
  }
  







  protected void configureCurrentConverter(int dialogType)
  {
    if ((getSelectedFile() == null) || (getSelectedFile().isDirectory())) {
      return;
    }
    String filename = getSelectedFile().getAbsolutePath();
    
    if (m_CurrentConverter == null) {
      if (dialogType == 1) {
        m_CurrentConverter = ConverterUtils.getLoaderForFile(filename);
      } else if (dialogType == 2) {
        m_CurrentConverter = ConverterUtils.getSaverForFile(filename);
      } else {
        Messages.getInstance();throw new IllegalStateException(Messages.getString("ConverterFileChooser_ConfigureCurrentConverter_IllegalStateException_Text"));
      }
      
      if (m_CurrentConverter == null) {
        return;
      }
    }
    try {
      File currFile = ((FileSourcedConverter)m_CurrentConverter).retrieveFile();
      if ((currFile == null) || (!currFile.getAbsolutePath().equals(filename))) {
        ((FileSourcedConverter)m_CurrentConverter).setFile(new File(filename));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  










  public static void main(String[] args)
    throws Exception
  {
    ConverterFileChooser fc = new ConverterFileChooser();
    int retVal = fc.showOpenDialog(null);
    

    if (retVal == 0) {
      AbstractFileLoader loader = fc.getLoader();
      Instances data = loader.getDataSet();
      retVal = fc.showSaveDialog(null);
      

      if (retVal == 0) {
        AbstractFileSaver saver = fc.getSaver();
        saver.setInstances(data);
        saver.writeBatch();
      }
      else {
        Messages.getInstance();System.out.println(Messages.getString("ConverterFileChooser_Main_Text_First"));
      }
    }
    else {
      Messages.getInstance();System.out.println(Messages.getString("ConverterFileChooser_Main_Text_Second"));
    }
  }
}
