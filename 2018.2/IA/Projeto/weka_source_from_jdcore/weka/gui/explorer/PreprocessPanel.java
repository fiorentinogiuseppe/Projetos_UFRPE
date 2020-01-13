package weka.gui.explorer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils;
import weka.core.converters.Loader;
import weka.core.converters.SerializedInstancesLoader;
import weka.core.converters.URLSourcedLoader;
import weka.datagenerators.DataGenerator;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;
import weka.filters.SupervisedFilter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.AttributeSelectionPanel;
import weka.gui.AttributeSummaryPanel;
import weka.gui.AttributeVisualizationPanel;
import weka.gui.ConverterFileChooser;
import weka.gui.GenericObjectEditor;
import weka.gui.GenericObjectEditor.GOEPanel;
import weka.gui.InstancesSummaryPanel;
import weka.gui.LogPanel;
import weka.gui.Logger;
import weka.gui.PropertyDialog;
import weka.gui.PropertyPanel;
import weka.gui.SysErrLog;
import weka.gui.TaskLogger;
import weka.gui.ViewerDialog;
import weka.gui.beans.AttributeSummarizer;
import weka.gui.sql.SqlViewerDialog;




































public class PreprocessPanel
  extends JPanel
  implements Explorer.CapabilitiesFilterChangeListener, Explorer.ExplorerPanel, Explorer.LogHandler
{
  private static final long serialVersionUID = 6764850273874813049L;
  protected InstancesSummaryPanel m_InstSummaryPanel = new InstancesSummaryPanel();
  protected JButton m_OpenFileBut;
  
  public PreprocessPanel() {
    Messages.getInstance();m_OpenFileBut = new JButton(Messages.getString("PreprocessPanel_OpenFileBut_JButton_Text"));
    

    Messages.getInstance();m_OpenURLBut = new JButton(Messages.getString("PreprocessPanel_OpenURLBut_JButton_Text"));
    

    Messages.getInstance();m_OpenDBBut = new JButton(Messages.getString("PreprocessPanel_OpenDBBut_JButton_Text"));
    

    Messages.getInstance();m_GenerateBut = new JButton(Messages.getString("PreprocessPanel_GenerateBut_JButton_Text"));
    

    Messages.getInstance();m_UndoBut = new JButton(Messages.getString("PreprocessPanel_UndoBut_JButton_Text"));
    

    Messages.getInstance();m_EditBut = new JButton(Messages.getString("PreprocessPanel_EditBut_JButton_Text"));
    

    Messages.getInstance();m_SaveBut = new JButton(Messages.getString("PreprocessPanel_SaveBut_JButton_Text"));
    

    m_AttPanel = new AttributeSelectionPanel();
    

    Messages.getInstance();m_RemoveButton = new JButton(Messages.getString("PreprocessPanel_RemoveButton_JButton_Text"));
    

    m_AttSummaryPanel = new AttributeSummaryPanel();
    


    m_FilterEditor = new GenericObjectEditor();
    


    m_FilterPanel = new PropertyPanel(m_FilterEditor);
    

    Messages.getInstance();m_ApplyFilterBut = new JButton(Messages.getString("PreprocessPanel_ApplyFilterBut_JButton_Text"));
    

    m_FileChooser = new ConverterFileChooser(new File(ExplorerDefaults.getInitialDirectory()));
    


    m_LastURL = "http://";
    

    m_SQLQ = new String("SELECT * FROM ?");
    




    m_DataGenerator = null;
    

    m_AttVisualizePanel = new AttributeVisualizationPanel();
    


    m_tempUndoFiles = new File[20];
    

    m_tempUndoIndex = 0;
    




    m_Support = new PropertyChangeSupport(this);
    




    m_Log = new SysErrLog();
    

    m_Explorer = null;
    










    m_FilterEditor.setClassType(Filter.class);
    if (ExplorerDefaults.getFilter() != null) {
      m_FilterEditor.setValue(ExplorerDefaults.getFilter());
    }
    m_FilterEditor.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        m_ApplyFilterBut.setEnabled(getInstances() != null);
        Capabilities currentCapabilitiesFilter = m_FilterEditor.getCapabilitiesFilter();
        Filter filter = (Filter)m_FilterEditor.getValue();
        Capabilities currentFilterCapabilities = null;
        if ((filter != null) && (currentCapabilitiesFilter != null) && ((filter instanceof CapabilitiesHandler)))
        {
          currentFilterCapabilities = filter.getCapabilities();
          
          if ((!currentFilterCapabilities.supportsMaybe(currentCapabilitiesFilter)) && (!currentFilterCapabilities.supports(currentCapabilitiesFilter))) {
            try
            {
              filter.setInputFormat(getInstances());
            } catch (Exception ex) {
              m_ApplyFilterBut.setEnabled(false);
            }
          }
        }
      }
    });
    Messages.getInstance();m_OpenFileBut.setToolTipText(Messages.getString("PreprocessPanel_OpenFileBut_SetToolTipText_Text"));
    Messages.getInstance();m_OpenURLBut.setToolTipText(Messages.getString("PreprocessPanel_OpenURLBut_SetToolTipText_Text"));
    Messages.getInstance();m_OpenDBBut.setToolTipText(Messages.getString("PreprocessPanel_OpenDBBut_SetToolTipText_Text"));
    Messages.getInstance();m_GenerateBut.setToolTipText(Messages.getString("PreprocessPanel_GenerateBut_SetToolTipText_Text"));
    Messages.getInstance();m_UndoBut.setToolTipText(Messages.getString("PreprocessPanel_UndoBut_SetToolTipText_Text"));
    m_UndoBut.setEnabled(ExplorerDefaults.get("enableUndo", "true").equalsIgnoreCase("true"));
    if (!m_UndoBut.isEnabled()) {
      m_UndoBut.setToolTipText("Undo is disabled - see weka.gui.explorer.Explorer.props to enable");
    }
    
    Messages.getInstance();m_EditBut.setToolTipText(Messages.getString("PreprocessPanel_EditBut_SetToolTipText_Text"));
    Messages.getInstance();m_SaveBut.setToolTipText(Messages.getString("PreprocessPanel_SaveBut_SetToolTipText_Text"));
    Messages.getInstance();m_ApplyFilterBut.setToolTipText(Messages.getString("PreprocessPanel_ApplyFilterBut_SetToolTipText_Text"));
    
    m_FileChooser.setFileSelectionMode(2);
    m_OpenURLBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setInstancesFromURLQ();
      }
    });
    m_OpenDBBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SqlViewerDialog dialog = new SqlViewerDialog(null);
        dialog.setVisible(true);
        if (dialog.getReturnValue() == 0) {
          setInstancesFromDBQ(dialog.getURL(), dialog.getUser(), dialog.getPassword(), dialog.getQuery());
        }
      }
    });
    m_OpenFileBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setInstancesFromFileQ();
      }
    });
    m_GenerateBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        generateInstances();
      }
    });
    m_UndoBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        undo();
      }
    });
    m_EditBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        edit();
      }
    });
    m_SaveBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveWorkingInstancesToFileQ();
      }
    });
    m_ApplyFilterBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        applyFilter((Filter)m_FilterEditor.getValue());
      }
    });
    m_AttPanel.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          ListSelectionModel lm = (ListSelectionModel)e.getSource();
          for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
            if (lm.isSelectedIndex(i)) {
              m_AttSummaryPanel.setAttribute(i);
              m_AttVisualizePanel.setAttribute(i);
              break;
            }
            
          }
          
        }
      }
    });
    Messages.getInstance();m_InstSummaryPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("PreprocessPanel_InstSummaryPanel_BorderFactoryCreateTitledBorder_Text")));
    JPanel attStuffHolderPanel = new JPanel();
    Messages.getInstance();attStuffHolderPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("PreprocessPanel_AttStuffHolderPanel_BorderFactoryCreateTitledBorder_Text")));
    
    attStuffHolderPanel.setLayout(new BorderLayout());
    attStuffHolderPanel.add(m_AttPanel, "Center");
    m_RemoveButton.setEnabled(false);
    Messages.getInstance();m_RemoveButton.setToolTipText(Messages.getString("PreprocessPanel_RemoveButton_SetToolTipText_Text"));
    m_RemoveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Remove r = new Remove();
          int[] selected = m_AttPanel.getSelectedAttributes();
          if (selected.length == 0) {
            return;
          }
          if (selected.length == m_Instances.numAttributes())
          {
            Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(PreprocessPanel.this, Messages.getString("PreprocessPanel_JOptionPaneShowMessageDialog_Text_First"), Messages.getString("PreprocessPanel_JOptionPaneShowMessageDialog_Text_Second"), 0);
            


            Messages.getInstance();m_Log.logMessage(Messages.getString("PreprocessPanel_Log_LogMessage_Text_First"));
            Messages.getInstance();m_Log.statusMessage(Messages.getString("PreprocessPanel_Log_StatusMessage_Text_First"));
            return;
          }
          r.setAttributeIndicesArray(selected);
          applyFilter(r);
        } catch (Exception ex) {
          if ((m_Log instanceof TaskLogger)) {
            ((TaskLogger)m_Log).taskFinished();
          }
          
          Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(PreprocessPanel.this, Messages.getString("PreprocessPanel_JOptionPaneShowMessageDialog_Text_Third") + ex.getMessage(), Messages.getString("PreprocessPanel_JOptionPaneShowMessageDialog_Text_Fourth"), 0);
          



          Messages.getInstance();m_Log.logMessage(Messages.getString("PreprocessPanel_Log_LogMessage_Text_Second") + ex.getMessage());
          Messages.getInstance();m_Log.statusMessage(Messages.getString("PreprocessPanel_Log_StatusMessage_Text_Second"));
        }
        
      }
    });
    JPanel p1 = new JPanel();
    p1.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    p1.setLayout(new BorderLayout());
    p1.add(m_RemoveButton, "Center");
    attStuffHolderPanel.add(p1, "South");
    Messages.getInstance();m_AttSummaryPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("PreprocessPanel_AttSummaryPanel_BorderFactoryCreateTitledBorder_Text")));
    
    m_UndoBut.setEnabled(false);
    m_EditBut.setEnabled(false);
    m_SaveBut.setEnabled(false);
    m_ApplyFilterBut.setEnabled(false);
    

    JPanel buttons = new JPanel();
    buttons.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    buttons.setLayout(new GridLayout(1, 6, 5, 5));
    buttons.add(m_OpenFileBut);
    buttons.add(m_OpenURLBut);
    buttons.add(m_OpenDBBut);
    buttons.add(m_GenerateBut);
    buttons.add(m_UndoBut);
    buttons.add(m_EditBut);
    buttons.add(m_SaveBut);
    
    JPanel attInfo = new JPanel();
    
    attInfo.setLayout(new BorderLayout());
    attInfo.add(attStuffHolderPanel, "Center");
    
    JPanel filter = new JPanel();
    Messages.getInstance();filter.setBorder(BorderFactory.createTitledBorder(Messages.getString("PreprocessPanel_Filter_BorderFactoryCreateTitledBorder_Text")));
    
    filter.setLayout(new BorderLayout());
    filter.add(m_FilterPanel, "Center");
    filter.add(m_ApplyFilterBut, "East");
    
    JPanel attVis = new JPanel();
    attVis.setLayout(new GridLayout(2, 1));
    attVis.add(m_AttSummaryPanel);
    
    JComboBox colorBox = m_AttVisualizePanel.getColorBox();
    Messages.getInstance();colorBox.setToolTipText(Messages.getString("PreprocessPanel_ColorBox_SetToolTipText_Text"));
    colorBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent ie) {
        if (ie.getStateChange() == 1) {
          updateCapabilitiesFilter(m_FilterEditor.getCapabilitiesFilter());
        }
      }
    });
    Messages.getInstance();final JButton visAllBut = new JButton(Messages.getString("PreprocessPanel_VisAllBut_JButton_Text"));
    visAllBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if (m_Instances != null) {
          try {
            AttributeSummarizer as = new AttributeSummarizer();
            
            as.setColoringIndex(m_AttVisualizePanel.getColoringIndex());
            as.setInstances(m_Instances);
            
            final JFrame jf = new JFrame();
            jf.getContentPane().setLayout(new BorderLayout());
            
            jf.getContentPane().add(as, "Center");
            jf.addWindowListener(new WindowAdapter() {
              public void windowClosing(WindowEvent e) {
                val$visAllBut.setEnabled(true);
                jf.dispose();
              }
            });
            jf.setSize(830, 600);
            jf.setVisible(true);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      }
    });
    JPanel histoHolder = new JPanel();
    histoHolder.setLayout(new BorderLayout());
    histoHolder.add(m_AttVisualizePanel, "Center");
    JPanel histoControls = new JPanel();
    histoControls.setLayout(new BorderLayout());
    histoControls.add(colorBox, "Center");
    histoControls.add(visAllBut, "East");
    histoHolder.add(histoControls, "North");
    attVis.add(histoHolder);
    
    JPanel lhs = new JPanel();
    lhs.setLayout(new BorderLayout());
    lhs.add(m_InstSummaryPanel, "North");
    lhs.add(attInfo, "Center");
    
    JPanel rhs = new JPanel();
    rhs.setLayout(new BorderLayout());
    rhs.add(attVis, "Center");
    
    JPanel relation = new JPanel();
    relation.setLayout(new GridLayout(1, 2));
    relation.add(lhs);
    relation.add(rhs);
    
    JPanel middle = new JPanel();
    middle.setLayout(new BorderLayout());
    middle.add(filter, "North");
    middle.add(relation, "Center");
    
    setLayout(new BorderLayout());
    add(buttons, "North");
    add(middle, "Center");
  }
  
  protected JButton m_OpenURLBut;
  protected JButton m_OpenDBBut;
  protected JButton m_GenerateBut;
  protected JButton m_UndoBut;
  protected JButton m_EditBut;
  public void setLog(Logger newLog)
  {
    m_Log = newLog;
  }
  





  public void setInstances(Instances inst)
  {
    m_Instances = inst;
    try {
      Runnable r = new Runnable() {
        public void run() {
          m_InstSummaryPanel.setInstances(m_Instances);
          m_AttPanel.setInstances(m_Instances);
          m_RemoveButton.setEnabled(true);
          m_AttSummaryPanel.setInstances(m_Instances);
          m_AttVisualizePanel.setInstances(m_Instances);
          

          m_AttPanel.getSelectionModel().setSelectionInterval(0, 0);
          m_AttSummaryPanel.setAttribute(0);
          m_AttVisualizePanel.setAttribute(0);
          
          m_ApplyFilterBut.setEnabled(true);
          






          m_SaveBut.setEnabled(true);
          m_EditBut.setEnabled(true);
          Messages.getInstance();m_Log.statusMessage(Messages.getString("PreprocessPanel_SetInstances_Run_Log_StatusMessage_Text_First"));
          
          m_Support.firePropertyChange("", null, null);
          

          try
          {
            getExplorer().notifyCapabilitiesFilterListener(null);
            
            int oldIndex = m_Instances.classIndex();
            m_Instances.setClassIndex(m_AttVisualizePanel.getColorBox().getSelectedIndex() - 1);
            

            if (ExplorerDefaults.getInitGenericObjectEditorFilter()) {
              getExplorer().notifyCapabilitiesFilterListener(Capabilities.forInstances(m_Instances));
            }
            else {
              getExplorer().notifyCapabilitiesFilterListener(Capabilities.forInstances(new Instances(m_Instances, 0)));
            }
            
            m_Instances.setClassIndex(oldIndex);
          }
          catch (Exception e) {
            e.printStackTrace();
            m_Log.logMessage(e.toString());
          }
        }
      };
      if (SwingUtilities.isEventDispatchThread()) {
        r.run();
      } else {
        SwingUtilities.invokeAndWait(r);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  

  protected JButton m_SaveBut;
  
  protected AttributeSelectionPanel m_AttPanel;
  
  protected JButton m_RemoveButton;
  
  protected AttributeSummaryPanel m_AttSummaryPanel;
  
  protected GenericObjectEditor m_FilterEditor;
  
  protected PropertyPanel m_FilterPanel;
  public Instances getInstances()
  {
    return m_Instances;
  }
  





  public void addPropertyChangeListener(PropertyChangeListener l)
  {
    m_Support.addPropertyChangeListener(l);
  }
  





  public void removePropertyChangeListener(PropertyChangeListener l)
  {
    m_Support.removePropertyChangeListener(l);
  }
  





  protected void applyFilter(final Filter filter)
  {
    if (m_IOThread == null) {
      m_IOThread = new Thread()
      {
        public void run() {
          try {
            if (filter != null)
            {
              if ((m_Log instanceof TaskLogger)) {
                ((TaskLogger)m_Log).taskStarted();
              }
              Messages.getInstance();m_Log.statusMessage(Messages.getString("PreprocessPanel_ApplyFilter_Run_Log_StatusMessage_Text_First") + filter.getClass().getName());
              
              String cmd = filter.getClass().getName();
              if ((filter instanceof OptionHandler))
                cmd = cmd + " " + Utils.joinOptions(((OptionHandler)filter).getOptions());
              Messages.getInstance();m_Log.logMessage(Messages.getString("PreprocessPanel_ApplyFilter_Run_Log_LogMessage_Text_First") + cmd);
              int classIndex = m_AttVisualizePanel.getColoringIndex();
              if ((classIndex < 0) && ((filter instanceof SupervisedFilter))) {
                Messages.getInstance();throw new IllegalArgumentException(Messages.getString("PreprocessPanel_ApplyFilter_Run_IllegalArgumentException_Text"));
              }
              Instances copy = new Instances(m_Instances);
              copy.setClassIndex(classIndex);
              Filter filterCopy = Filter.makeCopy(filter);
              filterCopy.setInputFormat(copy);
              Instances newInstances = Filter.useFilter(copy, filterCopy);
              if ((newInstances == null) || (newInstances.numAttributes() < 1)) {
                Messages.getInstance();throw new Exception(Messages.getString("PreprocessPanel_ApplyFilter_Run_Exception_Text"));
              }
              Messages.getInstance();m_Log.statusMessage(Messages.getString("PreprocessPanel_ApplyFilter_Run_Log_StatusMessage_Text_Second"));
              addUndoPoint();
              m_AttVisualizePanel.setColoringIndex(copy.classIndex());
              
              if (m_Instances.classIndex() < 0)
                newInstances.setClassIndex(-1);
              m_Instances = newInstances;
              setInstances(m_Instances);
              if ((m_Log instanceof TaskLogger)) {
                ((TaskLogger)m_Log).taskFinished();
              }
            }
          }
          catch (Exception ex)
          {
            if ((m_Log instanceof TaskLogger)) {
              ((TaskLogger)m_Log).taskFinished();
            }
            
            Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(PreprocessPanel.this, Messages.getString("PreprocessPanel_ApplyFilter_Run_JOptionPaneShowMessageDialog_Text_First") + ex.getMessage(), Messages.getString("PreprocessPanel_ApplyFilter_Run_JOptionPaneShowMessageDialog_Text_Second"), 0);
            



            Messages.getInstance();m_Log.logMessage(Messages.getString("PreprocessPanel_ApplyFilter_Run_Log_LogMessage_Text_Second") + ex.getMessage());
            Messages.getInstance();m_Log.statusMessage(Messages.getString("PreprocessPanel_ApplyFilter_Run_Log_StatusMessage_Text_Third"));
          }
          m_IOThread = null;
        }
      };
      m_IOThread.setPriority(1);
      m_IOThread.start();
    } else {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("PreprocessPanel_ApplyFilter_Run_JOptionPaneShowMessageDialog_Text_Third"), Messages.getString("PreprocessPanel_ApplyFilter_Run_JOptionPaneShowMessageDialog_Text_Fourth"), 2);
    }
  }
  








  public void saveWorkingInstancesToFileQ()
  {
    if (m_IOThread == null) {
      m_FileChooser.setCapabilitiesFilter(m_FilterEditor.getCapabilitiesFilter());
      m_FileChooser.setAcceptAllFileFilterUsed(false);
      int returnVal = m_FileChooser.showSaveDialog(this);
      if (returnVal == 0) {
        Instances inst = new Instances(m_Instances);
        inst.setClassIndex(m_AttVisualizePanel.getColoringIndex());
        saveInstancesToFile(m_FileChooser.getSaver(), inst);
      }
      FileFilter temp = m_FileChooser.getFileFilter();
      m_FileChooser.setAcceptAllFileFilterUsed(true);
      m_FileChooser.setFileFilter(temp);
    }
    else {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("PreprocessPanel_SaveWorkingInstancesToFileQ_JOptionPaneShowMessageDialog_Text_First"), Messages.getString("PreprocessPanel_SaveWorkingInstancesToFileQ_JOptionPaneShowMessageDialog_Text_Second"), 2);
    }
  }
  








  public void saveInstancesToFile(final AbstractFileSaver saver, final Instances inst)
  {
    if (m_IOThread == null) {
      m_IOThread = new Thread() {
        public void run() {
          try {
            Messages.getInstance();m_Log.statusMessage(Messages.getString("PreprocessPanel_SaveInstancesToFile_Run_Log_StatusMessage_Text_First"));
            
            saver.setInstances(inst);
            saver.writeBatch();
            
            Messages.getInstance();m_Log.statusMessage(Messages.getString("PreprocessPanel_SaveInstancesToFile_Run_Log_StatusMessage_Text_Second"));
          }
          catch (Exception ex) {
            ex.printStackTrace();
            m_Log.logMessage(ex.getMessage());
          }
          m_IOThread = null;
        }
      };
      m_IOThread.setPriority(1);
      m_IOThread.start();
    }
    else {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("PreprocessPanel_SaveInstancesToFile_Run_JOptionPaneShowMessageDialog_Text_First"), Messages.getString("PreprocessPanel_SaveInstancesToFile_Run_JOptionPaneShowMessageDialog_Text_Second"), 2);
    }
  }
  








  public void setInstancesFromFileQ()
  {
    if (m_IOThread == null) {
      int returnVal = m_FileChooser.showOpenDialog(this);
      if (returnVal == 0) {
        try {
          addUndoPoint();
        }
        catch (Exception ignored) {}
        


        if (m_FileChooser.getLoader() == null) {
          Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("PreprocessPanel_SetInstancesFromFileQ_JOptionPaneShowMessageDialog_Text_First"), Messages.getString("PreprocessPanel_SetInstancesFromFileQ_JOptionPaneShowMessageDialog_Text_Second"), 0);
          


          converterQuery(m_FileChooser.getSelectedFile());
        }
        else {
          setInstancesFromFile(m_FileChooser.getLoader());
        }
      }
    }
    else {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("PreprocessPanel_SetInstancesFromFileQ_JOptionPaneShowMessageDialog_Text_Third"), Messages.getString("PreprocessPanel_SetInstancesFromFileQ_JOptionPaneShowMessageDialog_Text_Fourth"), 2);
    }
  }
  
  protected JButton m_ApplyFilterBut;
  protected ConverterFileChooser m_FileChooser;
  protected String m_LastURL;
  protected String m_SQLQ;
  protected Instances m_Instances;
  protected DataGenerator m_DataGenerator;
  protected AttributeVisualizationPanel m_AttVisualizePanel;
  protected File[] m_tempUndoFiles;
  protected int m_tempUndoIndex;
  protected PropertyChangeSupport m_Support;
  protected Thread m_IOThread;
  protected Logger m_Log;
  protected Explorer m_Explorer;
  public void setInstancesFromDBQ(String url, String user, String pw, String query)
  {
    if (m_IOThread == null) {
      try {
        InstanceQuery InstQ = new InstanceQuery();
        InstQ.setDatabaseURL(url);
        InstQ.setUsername(user);
        InstQ.setPassword(pw);
        InstQ.setQuery(query);
        

        if (InstQ.isConnected()) {
          InstQ.disconnectFromDatabase();
        }
        InstQ.connectToDatabase();
        try {
          addUndoPoint();
        } catch (Exception ignored) {}
        setInstancesFromDB(InstQ);
      } catch (Exception ex) {
        Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("PreprocessPanel_SetInstancesFromDBQ_JOptionPaneShowMessageDialog_Text_First") + ex.getMessage(), Messages.getString("PreprocessPanel_SetInstancesFromDBQ_JOptionPaneShowMessageDialog_Text_Second"), 0);
      }
      

    }
    else
    {

      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("PreprocessPanel_SetInstancesFromDBQ_JOptionPaneShowMessageDialog_Text_Third"), Messages.getString("PreprocessPanel_SetInstancesFromDBQ_JOptionPaneShowMessageDialog_Text_Fourth"), 2);
    }
  }
  








  public void setInstancesFromURLQ()
  {
    if (m_IOThread == null) {
      try {
        Messages.getInstance();Messages.getInstance();String urlName = (String)JOptionPane.showInputDialog(this, Messages.getString("PreprocessPanel_SetInstancesFromURLQ_JOptionPaneShowMessageDialog_Text_First"), Messages.getString("PreprocessPanel_SetInstancesFromURLQ_JOptionPaneShowMessageDialog_Text_Second"), 3, null, null, m_LastURL);
        





        if (urlName != null) {
          m_LastURL = urlName;
          URL url = new URL(urlName);
          try {
            addUndoPoint();
          } catch (Exception ignored) {}
          setInstancesFromURL(url);
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("PreprocessPanel_SetInstancesFromURLQ_JOptionPaneShowMessageDialog_Text_Third") + ex.getMessage(), Messages.getString("PreprocessPanel_SetInstancesFromURLQ_JOptionPaneShowMessageDialog_Text_Fourth"), 0);
      }
      

    }
    else
    {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("PreprocessPanel_SetInstancesFromURLQ_JOptionPaneShowMessageDialog_Text_Fifth"), Messages.getString("PreprocessPanel_SetInstancesFromURLQ_JOptionPaneShowMessageDialog_Text_Sixth"), 2);
    }
  }
  





  public void generateInstances()
  {
    if (m_IOThread == null) {
      m_IOThread = new Thread()
      {
        public void run() {
          try {
            final DataGeneratorPanel generatorPanel = new DataGeneratorPanel();
            final JDialog dialog = new JDialog();
            Messages.getInstance();JButton generateButton = new JButton(Messages.getString("PreprocessPanel_GenerateInstances_Run_GenerateButton_JButton_Text"));
            Messages.getInstance();final JCheckBox showOutputCheckBox = new JCheckBox(Messages.getString("PreprocessPanel_GenerateInstances_Run_ShowOutputCheckBox_JCheckBox_Text"));
            

            showOutputCheckBox.setMnemonic('S');
            generatorPanel.setLog(m_Log);
            generatorPanel.setGenerator(m_DataGenerator);
            generatorPanel.setPreferredSize(new Dimension(300, (int)generatorPanel.getPreferredSize().getHeight()));
            


            generateButton.setMnemonic('G');
            Messages.getInstance();generateButton.setToolTipText(Messages.getString("PreprocessPanel_GenerateInstances_Run_GenerateButton_SetToolTipText_Text"));
            generateButton.addActionListener(new ActionListener()
            {
              public void actionPerformed(ActionEvent evt) {
                generatorPanel.execute();
                boolean generated = generatorPanel.getInstances() != null;
                if (generated) {
                  setInstances(generatorPanel.getInstances());
                }
                
                dialog.dispose();
                

                m_DataGenerator = generatorPanel.getGenerator();
                

                if ((generated) && (showOutputCheckBox.isSelected()))
                  showGeneratedInstances(generatorPanel.getOutput());
              }
            });
            Messages.getInstance();dialog.setTitle(Messages.getString("PreprocessPanel_GenerateInstances_Run_Dialog_Text"));
            dialog.getContentPane().add(generatorPanel, "Center");
            dialog.getContentPane().add(generateButton, "East");
            dialog.getContentPane().add(showOutputCheckBox, "South");
            dialog.pack();
            

            dialog.setVisible(true);
          }
          catch (Exception ex) {
            ex.printStackTrace();
            m_Log.logMessage(ex.getMessage());
          }
          m_IOThread = null;
        }
      };
      m_IOThread.setPriority(1);
      m_IOThread.start();
    }
    else {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("PreprocessPanel_GenerateInstances_Run_JOptionPaneShowMessageDialog_Text_First"), Messages.getString("PreprocessPanel_GenerateInstances_Run_JOptionPaneShowMessageDialog_Text_Second"), 2);
    }
  }
  







  protected void showGeneratedInstances(String data)
  {
    final JDialog dialog = new JDialog();
    Messages.getInstance();JButton saveButton = new JButton(Messages.getString("PreprocessPanel_ShowGeneratedInstances_SaveButton_JButton_Text"));
    Messages.getInstance();JButton closeButton = new JButton(Messages.getString("PreprocessPanel_ShowGeneratedInstances_CloseButton_JButton_Text"));
    final JTextArea textData = new JTextArea(data);
    JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout(2));
    textData.setEditable(false);
    textData.setFont(new Font("Monospaced", 0, textData.getFont().getSize()));
    

    saveButton.setMnemonic('S');
    Messages.getInstance();saveButton.setToolTipText(Messages.getString("PreprocessPanel_ShowGeneratedInstances_SaveButton_SetToolTipText_Text"));
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JFileChooser filechooser = new JFileChooser();
        int result = filechooser.showSaveDialog(dialog);
        if (result == 0) {
          try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filechooser.getSelectedFile()));
            

            writer.write(textData.getText());
            writer.flush();
            writer.close();
            Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(dialog, Messages.getString("PreprocessPanel_ShowGeneratedInstances_SaveButton_JOptionPaneShowMessageDialog_Text_First") + filechooser.getSelectedFile() + Messages.getString("PreprocessPanel_ShowGeneratedInstances_SaveButton_JOptionPaneShowMessageDialog_Text_Second"), Messages.getString("PreprocessPanel_ShowGeneratedInstances_SaveButton_JOptionPaneShowMessageDialog_Text_Third"), 1);


          }
          catch (Exception e)
          {


            e.printStackTrace();
          }
          dialog.dispose();
        }
      }
    });
    closeButton.setMnemonic('C');
    Messages.getInstance();closeButton.setToolTipText(Messages.getString("PreprocessPanel_ShowGeneratedInstances_CloseButton_SetToolTipText_Text"));
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dialog.dispose();
      }
    });
    panel.add(saveButton);
    panel.add(closeButton);
    Messages.getInstance();dialog.setTitle(Messages.getString("PreprocessPanel_ShowGeneratedInstances_Dialog_SetToolTipText_Text"));
    dialog.getContentPane().add(new JScrollPane(textData), "Center");
    dialog.getContentPane().add(panel, "South");
    dialog.pack();
    

    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int width = dialog.getWidth() > screen.getWidth() * 0.8D ? (int)(screen.getWidth() * 0.8D) : dialog.getWidth();
    
    int height = dialog.getHeight() > screen.getHeight() * 0.8D ? (int)(screen.getHeight() * 0.8D) : dialog.getHeight();
    
    dialog.setSize(width, height);
    

    dialog.setVisible(true);
  }
  




  private void converterQuery(final File f)
  {
    final GenericObjectEditor convEd = new GenericObjectEditor(true);
    try
    {
      convEd.setClassType(Loader.class);
      convEd.setValue(new CSVLoader());
      ((GenericObjectEditor.GOEPanel)convEd.getCustomEditor()).addOkListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          PreprocessPanel.this.tryConverter((Loader)convEd.getValue(), f);
        }
      });
    }
    catch (Exception ex) {}
    PropertyDialog pd;
    PropertyDialog pd;
    if (PropertyDialog.getParentDialog(this) != null) {
      pd = new PropertyDialog(PropertyDialog.getParentDialog(this), convEd, 100, 100);
    } else
      pd = new PropertyDialog(PropertyDialog.getParentFrame(this), convEd, 100, 100);
    pd.setVisible(true);
  }
  






  private void tryConverter(final Loader cnv, final File f)
  {
    if (m_IOThread == null) {
      m_IOThread = new Thread() {
        public void run() {
          try {
            cnv.setSource(f);
            Instances inst = cnv.getDataSet();
            setInstances(inst);
          } catch (Exception ex) {
            Messages.getInstance();m_Log.statusMessage(cnv.getClass().getName() + Messages.getString("PreprocessPanel_TryConverter_Log_StatusMessage_Text") + f.getName());
            
            Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(PreprocessPanel.this, cnv.getClass().getName() + Messages.getString("PreprocessPanel_TryConverter_JOptionPaneShowMessageDialog_Text_First") + f.getName() + Messages.getString("PreprocessPanel_TryConverter_JOptionPaneShowMessageDialog_Text_Second") + Messages.getString("PreprocessPanel_TryConverter_JOptionPaneShowMessageDialog_Text_Third") + ex.getMessage(), Messages.getString("PreprocessPanel_TryConverter_JOptionPaneShowMessageDialog_Text_Fourth"), 0);
            




            m_IOThread = null;
            PreprocessPanel.this.converterQuery(f);
          }
          m_IOThread = null;
        }
      };
      m_IOThread.setPriority(1);
      m_IOThread.start();
    }
  }
  







  public void setInstancesFromFile(final AbstractFileLoader loader)
  {
    if (m_IOThread == null) {
      m_IOThread = new Thread() {
        public void run() {
          try {
            Messages.getInstance();m_Log.statusMessage(Messages.getString("PreprocessPanel_SetInstancesFromFile_Run_Log_StatusMessage_Text_First"));
            Instances inst = loader.getDataSet();
            setInstances(inst);
          }
          catch (Exception ex) {
            Messages.getInstance();Messages.getInstance();Messages.getInstance();m_Log.statusMessage(Messages.getString("PreprocessPanel_SetInstancesFromFile_Run_Log_StatusMessage_Text_Second") + loader.retrieveFile() + Messages.getString("PreprocessPanel_SetInstancesFromFile_Run_Log_StatusMessage_Text_Third") + loader.getFileDescription() + Messages.getString("PreprocessPanel_SetInstancesFromFile_Run_Log_StatusMessage_Text_Fourth"));
            

            m_IOThread = null;
            Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance(); String[] tmp232_229 = new String[2];Messages.getInstance();tmp232_229[0] = Messages.getString("PreprocessPanel_SetInstancesFromFile_Run_JOptionPaneShowOptionDialog_Text_Sixth"); String[] tmp244_232 = tmp232_229;Messages.getInstance();tmp244_232[1] = Messages.getString("PreprocessPanel_SetInstancesFromFile_Run_JOptionPaneShowOptionDialog_Text_Seventh"); if (JOptionPane.showOptionDialog(PreprocessPanel.this, Messages.getString("PreprocessPanel_SetInstancesFromFile_Run_JOptionPaneShowOptionDialog_Text_First") + loader.retrieveFile() + Messages.getString("PreprocessPanel_SetInstancesFromFile_Run_JOptionPaneShowOptionDialog_Text_Second") + loader.getFileDescription() + Messages.getString("PreprocessPanel_SetInstancesFromFile_Run_JOptionPaneShowOptionDialog_Text_Third") + Messages.getString("PreprocessPanel_SetInstancesFromFile_Run_JOptionPaneShowOptionDialog_Text_Fourth") + ex.getMessage(), Messages.getString("PreprocessPanel_SetInstancesFromFile_Run_JOptionPaneShowOptionDialog_Text_Fifth"), 0, 0, null, tmp244_232, null) == 1)
            {











              PreprocessPanel.this.converterQuery(loader.retrieveFile());
            }
          }
          m_IOThread = null;
        }
      };
      m_IOThread.setPriority(1);
      m_IOThread.start();
    } else {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("PreprocessPanel_SetInstancesFromFile_Run_JOptionPaneShowOptionDialog_Text_Eigth"), Messages.getString("PreprocessPanel_SetInstancesFromFile_Run_JOptionPaneShowOptionDialog_Text_Nineth"), 2);
    }
  }
  








  public void setInstancesFromDB(final InstanceQuery iq)
  {
    if (m_IOThread == null) {
      m_IOThread = new Thread()
      {
        public void run() {
          try {
            Messages.getInstance();m_Log.statusMessage(Messages.getString("PreprocessPanel_SetInstancesFromDB_Run_Log_StatusMessage_Text_First"));
            final Instances i = iq.retrieveInstances();
            SwingUtilities.invokeAndWait(new Runnable() {
              public void run() {
                setInstances(new Instances(i));
              }
            });
            iq.disconnectFromDatabase();
          } catch (Exception ex) {
            Messages.getInstance();m_Log.statusMessage(Messages.getString("PreprocessPanel_SetInstancesFromDB_Run_Log_StatusMessage_Text_Second") + m_SQLQ);
            Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(PreprocessPanel.this, Messages.getString("PreprocessPanel_SetInstancesFromDB_Run_JOptionPaneShowMessageDialog_Text_First") + ex.getMessage(), Messages.getString("PreprocessPanel_SetInstancesFromDB_Run_JOptionPaneShowMessageDialog_Text_Second"), 0);
          }
          




          m_IOThread = null;
        }
        
      };
      m_IOThread.setPriority(1);
      m_IOThread.start();
    } else {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("PreprocessPanel_SetInstancesFromDB_Run_JOptionPaneShowMessageDialog_Text_Third"), Messages.getString("PreprocessPanel_SetInstancesFromDB_Run_JOptionPaneShowMessageDialog_Text_Fourth"), 2);
    }
  }
  








  public void setInstancesFromURL(final URL u)
  {
    if (m_IOThread == null) {
      m_IOThread = new Thread()
      {
        public void run() {
          try {
            Messages.getInstance();m_Log.statusMessage(Messages.getString("PreprocessPanel_SetInstancesFromURL_Run_Log_StatusMessage_Text_First"));
            AbstractFileLoader loader = ConverterUtils.getURLLoaderForFile(u.toString());
            if (loader == null) {
              Messages.getInstance();throw new Exception(Messages.getString("PreprocessPanel_SetInstancesFromURL_Run_Exception_Text_First") + u); }
            ((URLSourcedLoader)loader).setURL(u.toString());
            setInstances(loader.getDataSet());
          } catch (Exception ex) {
            ex.printStackTrace();
            Messages.getInstance();m_Log.statusMessage(Messages.getString("PreprocessPanel_SetInstancesFromURL_Run_Log_StatusMessage_Text_Second") + u);
            Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(PreprocessPanel.this, Messages.getString("PreprocessPanel_SetInstancesFromDB_Run_JOptionPaneShowMessageDialog_Text_Fifth") + u + Messages.getString("PreprocessPanel_SetInstancesFromDB_Run_JOptionPaneShowMessageDialog_Text_Sixth") + ex.getMessage(), Messages.getString("PreprocessPanel_SetInstancesFromDB_Run_JOptionPaneShowMessageDialog_Text_Seventh"), 0);
          }
          





          m_IOThread = null;
        }
      };
      m_IOThread.setPriority(1);
      m_IOThread.start();
    } else {
      Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("PreprocessPanel_SetInstancesFromDB_Run_JOptionPaneShowMessageDialog_Text_Eighth"), Messages.getString("PreprocessPanel_SetInstancesFromDB_Run_JOptionPaneShowMessageDialog_Text_Nineth"), 2);
    }
  }
  






  public void addUndoPoint()
    throws Exception
  {
    if (!ExplorerDefaults.get("enableUndo", "true").equalsIgnoreCase("true")) {
      return;
    }
    
    if (m_Instances != null)
    {
      File tempFile = File.createTempFile("weka", SerializedInstancesLoader.FILE_EXTENSION);
      tempFile.deleteOnExit();
      
      if (!ExplorerDefaults.get("undoDirectory", "%t").equalsIgnoreCase("%t")) {
        String dir = ExplorerDefaults.get("undoDirectory", "%t");
        File undoDir = new File(dir);
        if (undoDir.exists()) {
          String fileName = tempFile.getName();
          File newFile = new File(dir + File.separator + fileName);
          if (undoDir.canWrite()) {
            newFile.deleteOnExit();
            tempFile = newFile;
          } else {
            System.err.println("Explorer: it doesn't look like we have permission to write to the user-specified undo directory '" + dir + "'");
          }
        }
        else
        {
          System.err.println("Explorer: user-specified undo directory '" + dir + "' does not exist!");
        }
      }
      

      ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
      



      oos.writeObject(m_Instances);
      oos.flush();
      oos.close();
      

      if (m_tempUndoFiles[m_tempUndoIndex] != null)
      {
        m_tempUndoFiles[m_tempUndoIndex].delete();
      }
      m_tempUndoFiles[m_tempUndoIndex] = tempFile;
      if (++m_tempUndoIndex >= m_tempUndoFiles.length)
      {
        m_tempUndoIndex = 0;
      }
      
      m_UndoBut.setEnabled(true);
    }
  }
  



  public void undo()
  {
    if (--m_tempUndoIndex < 0)
    {
      m_tempUndoIndex = (m_tempUndoFiles.length - 1);
    }
    
    if (m_tempUndoFiles[m_tempUndoIndex] != null)
    {
      AbstractFileLoader loader = ConverterUtils.getLoaderForFile(m_tempUndoFiles[m_tempUndoIndex]);
      try {
        loader.setFile(m_tempUndoFiles[m_tempUndoIndex]);
        setInstancesFromFile(loader);
      }
      catch (Exception e) {
        e.printStackTrace();
        m_Log.logMessage(e.toString());
        Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("PreprocessPanel_Undo_JOptionPaneShowMessageDialog_Text_First") + e.toString(), Messages.getString("PreprocessPanel_Undo_JOptionPaneShowMessageDialog_Text_Second"), 0);
      }
      




      m_tempUndoFiles[m_tempUndoIndex] = null;
    }
    

    int temp = m_tempUndoIndex - 1;
    if (temp < 0) {
      temp = m_tempUndoFiles.length - 1;
    }
    m_UndoBut.setEnabled(m_tempUndoFiles[temp] != null);
  }
  







  public void edit()
  {
    int classIndex = m_AttVisualizePanel.getColoringIndex();
    Instances copy = new Instances(m_Instances);
    copy.setClassIndex(classIndex);
    ViewerDialog dialog = new ViewerDialog(null);
    int result = dialog.showDialog(copy);
    if (result == 0) {
      try {
        addUndoPoint();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      
      Instances newInstances = dialog.getInstances();
      if (m_Instances.classIndex() < 0)
        newInstances.setClassIndex(-1);
      setInstances(newInstances);
    }
  }
  





  public void setExplorer(Explorer parent)
  {
    m_Explorer = parent;
  }
  




  public Explorer getExplorer()
  {
    return m_Explorer;
  }
  







  protected void updateCapabilitiesFilter(Capabilities filter)
  {
    if (filter == null) {
      m_FilterEditor.setCapabilitiesFilter(new Capabilities(null)); return;
    }
    Instances tempInst;
    Instances tempInst;
    if (!ExplorerDefaults.getInitGenericObjectEditorFilter()) {
      tempInst = new Instances(m_Instances, 0);
    } else
      tempInst = new Instances(m_Instances);
    tempInst.setClassIndex(m_AttVisualizePanel.getColorBox().getSelectedIndex() - 1);
    Capabilities filterClass;
    try {
      filterClass = Capabilities.forInstances(tempInst);
    }
    catch (Exception e) {
      filterClass = new Capabilities(null);
    }
    

    m_FilterEditor.setCapabilitiesFilter(filterClass);
    

    m_ApplyFilterBut.setEnabled(true);
    Capabilities currentCapabilitiesFilter = m_FilterEditor.getCapabilitiesFilter();
    Filter currentFilter = (Filter)m_FilterEditor.getValue();
    Capabilities currentFilterCapabilities = null;
    if ((currentFilter != null) && (currentCapabilitiesFilter != null) && ((currentFilter instanceof CapabilitiesHandler)))
    {
      currentFilterCapabilities = currentFilter.getCapabilities();
      
      if ((!currentFilterCapabilities.supportsMaybe(currentCapabilitiesFilter)) && (!currentFilterCapabilities.supports(currentCapabilitiesFilter))) {
        try
        {
          currentFilter.setInputFormat(getInstances());
        } catch (Exception ex) {
          m_ApplyFilterBut.setEnabled(false);
        }
      }
    }
  }
  




  public void capabilitiesFilterChanged(Explorer.CapabilitiesFilterChangeEvent e)
  {
    if (e.getFilter() == null) {
      updateCapabilitiesFilter(null);
    } else {
      updateCapabilitiesFilter((Capabilities)e.getFilter().clone());
    }
  }
  



  public String getTabTitle()
  {
    Messages.getInstance();return Messages.getString("PreprocessPanel_GetTitle_Text");
  }
  




  public String getTabTitleToolTip()
  {
    Messages.getInstance();return Messages.getString("PreprocessPanel_GetTabTitleToolTip_Text");
  }
  




  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("PreprocessPanel_Main_JFRame_Text"));
      jf.getContentPane().setLayout(new BorderLayout());
      PreprocessPanel sp = new PreprocessPanel();
      jf.getContentPane().add(sp, "Center");
      LogPanel lp = new LogPanel();
      sp.setLog(lp);
      jf.getContentPane().add(lp, "South");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setSize(800, 600);
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
  
  static {}
}
