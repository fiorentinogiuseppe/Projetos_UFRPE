package weka.gui.explorer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Drawable;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Memory;
import weka.core.OptionHandler;
import weka.core.SerializedObject;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.ExtensionFileFilter;
import weka.gui.GenericObjectEditor;
import weka.gui.InstancesSummaryPanel;
import weka.gui.ListSelectorDialog;
import weka.gui.LogPanel;
import weka.gui.Logger;
import weka.gui.PropertyPanel;
import weka.gui.ResultHistoryPanel;
import weka.gui.SaveBuffer;
import weka.gui.SetInstancesPanel;
import weka.gui.SysErrLog;
import weka.gui.TaskLogger;
import weka.gui.hierarchyvisualizer.HierarchyVisualizer;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.VisualizePanel;




































public class ClustererPanel
  extends JPanel
  implements Explorer.CapabilitiesFilterChangeListener, Explorer.ExplorerPanel, Explorer.LogHandler
{
  static final long serialVersionUID = -2474932792950820990L;
  protected Explorer m_Explorer = null;
  

  public static String MODEL_FILE_EXTENSION = ".model";
  

  protected GenericObjectEditor m_ClustererEditor = new GenericObjectEditor();
  

  protected PropertyPanel m_CLPanel = new PropertyPanel(m_ClustererEditor);
  

  protected JTextArea m_OutText = new JTextArea(20, 40);
  

  protected Logger m_Log = new SysErrLog();
  

  SaveBuffer m_SaveOut = new SaveBuffer(m_Log, this);
  

  protected ResultHistoryPanel m_History = new ResultHistoryPanel(m_OutText);
  



  protected JRadioButton m_PercentBut;
  


  protected JRadioButton m_TrainBut;
  


  protected JRadioButton m_TestSplitBut;
  


  protected JRadioButton m_ClassesToClustersBut;
  


  protected JComboBox m_ClassCombo;
  


  protected JLabel m_PercentLab;
  


  protected JTextField m_PercentText;
  


  protected JButton m_SetTestBut;
  


  protected JFrame m_SetTestFrame;
  


  protected JButton m_ignoreBut;
  


  protected DefaultListModel m_ignoreKeyModel;
  


  protected JList m_ignoreKeyList;
  


  ActionListener m_RadioListener;
  


  protected JButton m_StartBut;
  


  private final Dimension COMBO_SIZE;
  


  protected JButton m_StopBut;
  


  protected Instances m_Instances;
  


  protected Instances m_TestInstances;
  


  protected VisualizePanel m_CurrentVis;
  


  protected JCheckBox m_StorePredictionsBut;
  


  protected Thread m_RunThread;
  


  protected InstancesSummaryPanel m_Summary;
  


  protected FileFilter m_ModelFilter;
  


  protected JFileChooser m_FileChooser;
  



  static
  {
    GenericObjectEditor.registerEditors();
  }
  
  public ClustererPanel()
  {
    Messages.getInstance();m_PercentBut = new JRadioButton(Messages.getString("ClustererPanel_PercentBut_JRadioButton_Text"));
    


    Messages.getInstance();m_TrainBut = new JRadioButton(Messages.getString("ClustererPanel_TrainBut_JRadioButton_Text"));
    


    Messages.getInstance();m_TestSplitBut = new JRadioButton(Messages.getString("ClustererPanel_TestSplitBut_JRadioButton_Text"));
    


    Messages.getInstance();m_ClassesToClustersBut = new JRadioButton(Messages.getString("ClustererPanel_ClassesToClustersBut_JRadioButton_Text"));
    






    m_ClassCombo = new JComboBox();
    

    m_PercentLab = new JLabel("%", 4);
    

    m_PercentText = new JTextField("66");
    

    Messages.getInstance();m_SetTestBut = new JButton(Messages.getString("ClustererPanel_SetTestBut_JButton_Text"));
    








    Messages.getInstance();m_ignoreBut = new JButton(Messages.getString("ClustererPanel_IgnoreBut_JButton_Text"));
    

    m_ignoreKeyModel = new DefaultListModel();
    m_ignoreKeyList = new JList(m_ignoreKeyModel);
    






    m_RadioListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateRadioLinks();
      }
      

    };
    Messages.getInstance();m_StartBut = new JButton(Messages.getString("ClustererPanel_StartBut_JButton_Text"));
    


    COMBO_SIZE = new Dimension(250, m_StartBut.getPreferredSize().height);
    


    Messages.getInstance();m_StopBut = new JButton(Messages.getString("ClustererPanel_StopBut_JButton_Text"));
    








    m_CurrentVis = null;
    



    Messages.getInstance();m_StorePredictionsBut = new JCheckBox(Messages.getString("ClustererPanel_StopBut_JCheckBox_Text"));
    








    Messages.getInstance();m_ModelFilter = new ExtensionFileFilter(MODEL_FILE_EXTENSION, Messages.getString("ClustererPanel_ModelFilter_ExtensionFileFilter_Text"));
    



    m_FileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
    












    m_OutText.setEditable(false);
    m_OutText.setFont(new Font("Monospaced", 0, 12));
    m_OutText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    m_OutText.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e) {
        if ((e.getModifiers() & 0x10) != 16) {
          m_OutText.selectAll();
        }
      }
    });
    Messages.getInstance();m_History.setBorder(BorderFactory.createTitledBorder(Messages.getString("ClustererPanel_History_BorderFactoryCreateTitledBorder_Text")));
    

    m_ClustererEditor.setClassType(Clusterer.class);
    m_ClustererEditor.setValue(ExplorerDefaults.getClusterer());
    m_ClustererEditor.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        m_StartBut.setEnabled(true);
        Capabilities currentFilter = m_ClustererEditor.getCapabilitiesFilter();
        Clusterer clusterer = (Clusterer)m_ClustererEditor.getValue();
        Capabilities currentSchemeCapabilities = null;
        if ((clusterer != null) && (currentFilter != null) && ((clusterer instanceof CapabilitiesHandler)))
        {
          currentSchemeCapabilities = ((CapabilitiesHandler)clusterer).getCapabilities();
          

          if ((!currentSchemeCapabilities.supportsMaybe(currentFilter)) && (!currentSchemeCapabilities.supports(currentFilter)))
          {
            m_StartBut.setEnabled(false);
          }
        }
        repaint();
      }
      
    });
    Messages.getInstance();m_TrainBut.setToolTipText(Messages.getString("ClustererPanel_TrainBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_PercentBut.setToolTipText(Messages.getString("ClustererPanel_PercentBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_TestSplitBut.setToolTipText(Messages.getString("ClustererPanel_TestSplitBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_ClassesToClustersBut.setToolTipText(Messages.getString("ClustererPanel_ClassesToClustersBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_ClassCombo.setToolTipText(Messages.getString("ClustererPanel_ClassCombo_SetToolTipText_Text"));
    
    Messages.getInstance();m_StartBut.setToolTipText(Messages.getString("ClustererPanel_StartBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_StopBut.setToolTipText(Messages.getString("ClustererPanel_StartBut_StopBut_Text"));
    
    Messages.getInstance();m_StorePredictionsBut.setToolTipText(Messages.getString("ClustererPanel_StorePredictionsBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_ignoreBut.setToolTipText(Messages.getString("ClustererPanel_IgnoreBut_SetToolTipText_Text"));
    

    m_FileChooser.setFileFilter(m_ModelFilter);
    m_FileChooser.setFileSelectionMode(0);
    
    m_ClassCombo.setPreferredSize(COMBO_SIZE);
    m_ClassCombo.setMaximumSize(COMBO_SIZE);
    m_ClassCombo.setMinimumSize(COMBO_SIZE);
    m_ClassCombo.setEnabled(false);
    
    m_PercentBut.setSelected(ExplorerDefaults.getClustererTestMode() == 2);
    m_TrainBut.setSelected(ExplorerDefaults.getClustererTestMode() == 3);
    m_TestSplitBut.setSelected(ExplorerDefaults.getClustererTestMode() == 4);
    m_ClassesToClustersBut.setSelected(ExplorerDefaults.getClustererTestMode() == 5);
    
    m_StorePredictionsBut.setSelected(ExplorerDefaults.getClustererStoreClustersForVis());
    
    updateRadioLinks();
    ButtonGroup bg = new ButtonGroup();
    bg.add(m_TrainBut);
    bg.add(m_PercentBut);
    bg.add(m_TestSplitBut);
    bg.add(m_ClassesToClustersBut);
    m_TrainBut.addActionListener(m_RadioListener);
    m_PercentBut.addActionListener(m_RadioListener);
    m_TestSplitBut.addActionListener(m_RadioListener);
    m_ClassesToClustersBut.addActionListener(m_RadioListener);
    m_SetTestBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setTestSet();
      }
      
    });
    m_StartBut.setEnabled(false);
    m_StopBut.setEnabled(false);
    m_ignoreBut.setEnabled(false);
    m_StartBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean proceed = true;
        if (Explorer.m_Memory.memoryIsLow()) {
          proceed = Explorer.m_Memory.showMemoryIsLow();
        }
        
        if (proceed) {
          startClusterer();
        }
      }
    });
    m_StopBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stopClusterer();
      }
      
    });
    m_ignoreBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ClustererPanel.this.setIgnoreColumns();
      }
      
    });
    m_History.setHandleRightClicks(false);
    
    m_History.getList().addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e) {
        if (((e.getModifiers() & 0x10) != 16) || (e.isAltDown()))
        {
          int index = m_History.getList().locationToIndex(e.getPoint());
          if (index != -1) {
            String name = m_History.getNameAtIndex(index);
            visualizeClusterer(name, e.getX(), e.getY());
          } else {
            visualizeClusterer(null, e.getX(), e.getY());
          }
          
        }
      }
    });
    m_ClassCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateCapabilitiesFilter(m_ClustererEditor.getCapabilitiesFilter());
      }
      

    });
    JPanel p1 = new JPanel();
    Messages.getInstance();p1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("ClustererPanel_P1_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    


    p1.setLayout(new BorderLayout());
    p1.add(m_CLPanel, "North");
    
    JPanel p2 = new JPanel();
    GridBagLayout gbL = new GridBagLayout();
    p2.setLayout(gbL);
    Messages.getInstance();p2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("ClustererPanel_P2_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    


    GridBagConstraints gbC = new GridBagConstraints();
    anchor = 17;
    gridy = 0;
    gridx = 0;
    gbL.setConstraints(m_TrainBut, gbC);
    p2.add(m_TrainBut);
    
    gbC = new GridBagConstraints();
    anchor = 17;
    gridy = 1;
    gridx = 0;
    gbL.setConstraints(m_TestSplitBut, gbC);
    p2.add(m_TestSplitBut);
    
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 1;
    gridx = 1;
    gridwidth = 2;
    insets = new Insets(2, 10, 2, 0);
    gbL.setConstraints(m_SetTestBut, gbC);
    p2.add(m_SetTestBut);
    
    gbC = new GridBagConstraints();
    anchor = 17;
    gridy = 2;
    gridx = 0;
    gbL.setConstraints(m_PercentBut, gbC);
    p2.add(m_PercentBut);
    
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 2;
    gridx = 1;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(m_PercentLab, gbC);
    p2.add(m_PercentLab);
    
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 2;
    gridx = 2;
    weightx = 100.0D;
    ipadx = 20;
    gbL.setConstraints(m_PercentText, gbC);
    p2.add(m_PercentText);
    
    gbC = new GridBagConstraints();
    anchor = 17;
    gridy = 3;
    gridx = 0;
    gridwidth = 2;
    gbL.setConstraints(m_ClassesToClustersBut, gbC);
    p2.add(m_ClassesToClustersBut);
    
    m_ClassCombo.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    gbC = new GridBagConstraints();
    anchor = 17;
    gridy = 4;
    gridx = 0;
    gridwidth = 2;
    gbL.setConstraints(m_ClassCombo, gbC);
    p2.add(m_ClassCombo);
    
    gbC = new GridBagConstraints();
    anchor = 17;
    gridy = 5;
    gridx = 0;
    gridwidth = 2;
    gbL.setConstraints(m_StorePredictionsBut, gbC);
    p2.add(m_StorePredictionsBut);
    
    JPanel buttons = new JPanel();
    buttons.setLayout(new GridLayout(2, 1));
    JPanel ssButs = new JPanel();
    ssButs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    ssButs.setLayout(new GridLayout(1, 2, 5, 5));
    ssButs.add(m_StartBut);
    ssButs.add(m_StopBut);
    
    JPanel ib = new JPanel();
    ib.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    ib.setLayout(new GridLayout(1, 1, 5, 5));
    ib.add(m_ignoreBut);
    buttons.add(ib);
    buttons.add(ssButs);
    
    JPanel p3 = new JPanel();
    Messages.getInstance();p3.setBorder(BorderFactory.createTitledBorder(Messages.getString("ClustererPanel_P3_BorderFactoryCreateTitledBorder_Text")));
    
    p3.setLayout(new BorderLayout());
    JScrollPane js = new JScrollPane(m_OutText);
    p3.add(js, "Center");
    js.getViewport().addChangeListener(new ChangeListener() {
      private int lastHeight;
      
      public void stateChanged(ChangeEvent e) {
        JViewport vp = (JViewport)e.getSource();
        int h = getViewSizeheight;
        if (h != lastHeight) {
          lastHeight = h;
          int x = h - getExtentSizeheight;
          vp.setViewPosition(new Point(0, x));
        }
        
      }
    });
    JPanel mondo = new JPanel();
    gbL = new GridBagLayout();
    mondo.setLayout(gbL);
    gbC = new GridBagConstraints();
    
    fill = 2;
    gridy = 0;
    gridx = 0;
    gbL.setConstraints(p2, gbC);
    mondo.add(p2);
    gbC = new GridBagConstraints();
    anchor = 11;
    fill = 2;
    gridy = 1;
    gridx = 0;
    gbL.setConstraints(buttons, gbC);
    mondo.add(buttons);
    gbC = new GridBagConstraints();
    
    fill = 1;
    gridy = 2;
    gridx = 0;
    weightx = 0.0D;
    gbL.setConstraints(m_History, gbC);
    mondo.add(m_History);
    gbC = new GridBagConstraints();
    fill = 1;
    gridy = 0;
    gridx = 1;
    gridheight = 3;
    weightx = 100.0D;
    weighty = 100.0D;
    gbL.setConstraints(p3, gbC);
    mondo.add(p3);
    
    setLayout(new BorderLayout());
    add(p1, "North");
    add(mondo, "Center");
  }
  



  protected void updateRadioLinks()
  {
    m_SetTestBut.setEnabled(m_TestSplitBut.isSelected());
    if ((m_SetTestFrame != null) && (!m_TestSplitBut.isSelected())) {
      m_SetTestFrame.setVisible(false);
    }
    m_PercentText.setEnabled(m_PercentBut.isSelected());
    m_PercentLab.setEnabled(m_PercentBut.isSelected());
    m_ClassCombo.setEnabled(m_ClassesToClustersBut.isSelected());
    
    updateCapabilitiesFilter(m_ClustererEditor.getCapabilitiesFilter());
  }
  





  public void setLog(Logger newLog)
  {
    m_Log = newLog;
  }
  





  public void setInstances(Instances inst)
  {
    m_Instances = inst;
    
    m_ignoreKeyModel.removeAllElements();
    
    String[] attribNames = new String[m_Instances.numAttributes()];
    for (int i = 0; i < m_Instances.numAttributes(); i++) {
      String name = m_Instances.attribute(i).name();
      m_ignoreKeyModel.addElement(name);
      
      String type = "";
      switch (m_Instances.attribute(i).type()) {
      case 1: 
        Messages.getInstance();type = Messages.getString("ClustererPanel_SetInstances_Type_AttributeNOMINAL_Text");
        
        break;
      case 0: 
        Messages.getInstance();type = Messages.getString("ClustererPanel_SetInstances_Type_AttributeNUMERIC_Text");
        
        break;
      case 2: 
        Messages.getInstance();type = Messages.getString("ClustererPanel_SetInstances_Type_AttributeSTRING_Text");
        
        break;
      case 3: 
        Messages.getInstance();type = Messages.getString("ClustererPanel_SetInstances_Type_AttributeDATE_Text");
        
        break;
      case 4: 
        Messages.getInstance();type = Messages.getString("ClustererPanel_SetInstances_Type_AttributeRELATIONAL_Text");
        
        break;
      default: 
        Messages.getInstance();type = Messages.getString("ClustererPanel_SetInstances_Type_AttributeDEFAULT_Text");
      }
      
      String attnm = m_Instances.attribute(i).name();
      
      attribNames[i] = (type + attnm);
    }
    
    m_StartBut.setEnabled(m_RunThread == null);
    m_StopBut.setEnabled(m_RunThread != null);
    m_ignoreBut.setEnabled(true);
    m_ClassCombo.setModel(new DefaultComboBoxModel(attribNames));
    if (inst.classIndex() == -1) {
      m_ClassCombo.setSelectedIndex(attribNames.length - 1);
    } else
      m_ClassCombo.setSelectedIndex(inst.classIndex());
    updateRadioLinks();
  }
  






  protected void setTestSet()
  {
    if (m_SetTestFrame == null) {
      final SetInstancesPanel sp = new SetInstancesPanel();
      sp.setReadIncrementally(false);
      m_Summary = sp.getSummary();
      if (m_TestInstances != null) {
        sp.setInstances(m_TestInstances);
      }
      sp.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
          m_TestInstances = sp.getInstances();
          m_TestInstances.setClassIndex(-1);

        }
        

      });
      Messages.getInstance();m_SetTestFrame = new JFrame(Messages.getString("ClustererPanel_SetUpVisualizableInstances_JFrame_Text"));
      
      sp.setParentFrame(m_SetTestFrame);
      m_SetTestFrame.getContentPane().setLayout(new BorderLayout());
      m_SetTestFrame.getContentPane().add(sp, "Center");
      m_SetTestFrame.pack();
    }
    m_SetTestFrame.setVisible(true);
  }
  










  public static PlotData2D setUpVisualizableInstances(Instances testInstances, ClusterEvaluation eval)
    throws Exception
  {
    int numClusters = eval.getNumClusters();
    double[] clusterAssignments = eval.getClusterAssignments();
    
    FastVector hv = new FastVector();
    


    FastVector clustVals = new FastVector();
    
    for (int i = 0; i < numClusters; i++) {
      Messages.getInstance();clustVals.addElement(Messages.getString("ClustererPanel_SetUpVisualizableInstances_ClustVals_Text") + i);
    }
    

    Messages.getInstance();Attribute predictedCluster = new Attribute(Messages.getString("ClustererPanel_SetUpVisualizableInstances_PredictedCluster_Text"), clustVals);
    

    for (int i = 0; i < testInstances.numAttributes(); i++) {
      hv.addElement(testInstances.attribute(i).copy());
    }
    hv.addElement(predictedCluster);
    
    Instances newInsts = new Instances(testInstances.relationName() + "_clustered", hv, testInstances.numInstances());
    



    int[] pointShapes = null;
    int[] classAssignments = null;
    if (testInstances.classIndex() >= 0) {
      classAssignments = eval.getClassesToClusters();
      pointShapes = new int[testInstances.numInstances()];
      for (int i = 0; i < testInstances.numInstances(); i++) {
        pointShapes[i] = -1;
      }
    }
    
    for (int i = 0; i < testInstances.numInstances(); i++) {
      double[] values = new double[newInsts.numAttributes()];
      for (int j = 0; j < testInstances.numAttributes(); j++) {
        values[j] = testInstances.instance(i).value(j);
      }
      if (clusterAssignments[i] < 0.0D) {
        values[j] = Instance.missingValue();
      } else {
        values[j] = clusterAssignments[i];
      }
      newInsts.add(new Instance(1.0D, values));
      if (pointShapes != null) {
        if (clusterAssignments[i] >= 0.0D) {
          if ((int)testInstances.instance(i).classValue() != classAssignments[((int)clusterAssignments[i])]) {
            pointShapes[i] = 1000;
          }
        } else {
          pointShapes[i] = 2000;
        }
      }
    }
    PlotData2D plotData = new PlotData2D(newInsts);
    if (pointShapes != null) {
      plotData.setShapeType(pointShapes);
    }
    plotData.addInstanceNumberAttribute();
    return plotData;
  }
  






  protected void startClusterer()
  {
    if (m_RunThread == null) {
      m_StartBut.setEnabled(false);
      m_StopBut.setEnabled(true);
      m_ignoreBut.setEnabled(false);
      m_RunThread = new Thread()
      {
        public void run()
        {
          long trainTimeStart = 0L;long trainTimeElapsed = 0L;
          

          Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_StartClusterer_Run_Log_StatusMessage_Text_First"));
          



          Instances inst = new Instances(m_Instances);
          inst.setClassIndex(-1);
          Instances userTest = null;
          PlotData2D predData = null;
          if (m_TestInstances != null) {
            userTest = new Instances(m_TestInstances);
          }
          
          boolean saveVis = m_StorePredictionsBut.isSelected();
          String grph = null;
          int[] ignoredAtts = null;
          
          int testMode = 0;
          int percent = 66;
          Clusterer clusterer = (Clusterer)m_ClustererEditor.getValue();
          Clusterer fullClusterer = null;
          StringBuffer outBuff = new StringBuffer();
          String name = new SimpleDateFormat("HH:mm:ss - ").format(new Date());
          
          String cname = clusterer.getClass().getName();
          if (cname.startsWith("weka.clusterers.")) {
            name = name + cname.substring("weka.clusterers.".length());
          } else {
            name = name + cname;
          }
          String cmd = m_ClustererEditor.getValue().getClass().getName();
          if ((m_ClustererEditor.getValue() instanceof OptionHandler)) {
            cmd = cmd + " " + Utils.joinOptions(((OptionHandler)m_ClustererEditor.getValue()).getOptions());
          }
          try
          {
            Messages.getInstance();m_Log.logMessage(Messages.getString("ClustererPanel_StartClusterer_Run_Log_LogMessage_Text_First") + cname);
            

            Messages.getInstance();m_Log.logMessage(Messages.getString("ClustererPanel_StartClusterer_Run_Log_LogMessage_Text_Second") + cmd);
            

            if ((m_Log instanceof TaskLogger)) {
              ((TaskLogger)m_Log).taskStarted();
            }
            if (m_PercentBut.isSelected()) {
              testMode = 2;
              percent = Integer.parseInt(m_PercentText.getText());
              if ((percent <= 0) || (percent >= 100)) {
                Messages.getInstance();throw new Exception(Messages.getString("ClustererPanel_StartClusterer_Run_Exception_Text_First"));
              }
            }
            else if (m_TrainBut.isSelected()) {
              testMode = 3;
            } else if (m_TestSplitBut.isSelected()) {
              testMode = 4;
              
              if (userTest == null) {
                Messages.getInstance();throw new Exception(Messages.getString("ClustererPanel_StartClusterer_Run_Exception_Text_Second"));
              }
              
              if (!inst.equalHeaders(userTest)) {
                Messages.getInstance();throw new Exception(Messages.getString("ClustererPanel_StartClusterer_Run_Exception_Text_Third"));
              }
            }
            else if (m_ClassesToClustersBut.isSelected()) {
              testMode = 5;
            } else {
              Messages.getInstance();throw new Exception(Messages.getString("ClustererPanel_StartClusterer_Run_Exception_Text_Fourth"));
            }
            

            Instances trainInst = new Instances(inst);
            if (m_ClassesToClustersBut.isSelected()) {
              trainInst.setClassIndex(m_ClassCombo.getSelectedIndex());
              inst.setClassIndex(m_ClassCombo.getSelectedIndex());
              if (inst.classAttribute().isNumeric()) {
                Messages.getInstance();throw new Exception(Messages.getString("ClustererPanel_StartClusterer_Run_Exception_Text_Fifth"));
              }
            }
            
            if (!m_ignoreKeyList.isSelectionEmpty()) {
              trainInst = ClustererPanel.this.removeIgnoreCols(trainInst);
            }
            

            Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_First"));
            
            Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_Second") + cname);
            

            if ((clusterer instanceof OptionHandler)) {
              String[] o = ((OptionHandler)clusterer).getOptions();
              outBuff.append(" " + Utils.joinOptions(o));
            }
            Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_Third"));
            
            Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_Fourth") + inst.relationName() + '\n');
            

            Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_Sixth") + inst.numInstances() + '\n');
            

            Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_Eighth") + inst.numAttributes() + '\n');
            

            if (inst.numAttributes() < 100) {
              boolean[] selected = new boolean[inst.numAttributes()];
              for (int i = 0; i < inst.numAttributes(); i++) {
                selected[i] = true;
              }
              if (!m_ignoreKeyList.isSelectionEmpty()) {
                int[] indices = m_ignoreKeyList.getSelectedIndices();
                for (int i = 0; i < indices.length; i++) {
                  selected[indices[i]] = false;
                }
              }
              if (m_ClassesToClustersBut.isSelected()) {
                selected[m_ClassCombo.getSelectedIndex()] = false;
              }
              for (int i = 0; i < inst.numAttributes(); i++) {
                if (selected[i] != 0) {
                  outBuff.append("              " + inst.attribute(i).name() + '\n');
                }
              }
              
              if ((!m_ignoreKeyList.isSelectionEmpty()) || (m_ClassesToClustersBut.isSelected()))
              {
                Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_Eleventh"));
                



                for (int i = 0; i < inst.numAttributes(); i++) {
                  if (selected[i] == 0) {
                    outBuff.append("              " + inst.attribute(i).name() + '\n');
                  }
                }
              }
            }
            else {
              Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_Thirteenth"));
            }
            




            if (!m_ignoreKeyList.isSelectionEmpty()) {
              ignoredAtts = m_ignoreKeyList.getSelectedIndices();
            }
            
            if (m_ClassesToClustersBut.isSelected())
            {
              if (ignoredAtts == null) {
                ignoredAtts = new int[1];
                ignoredAtts[0] = m_ClassCombo.getSelectedIndex();
              } else {
                int[] newIgnoredAtts = new int[ignoredAtts.length + 1];
                System.arraycopy(ignoredAtts, 0, newIgnoredAtts, 0, ignoredAtts.length);
                
                newIgnoredAtts[ignoredAtts.length] = m_ClassCombo.getSelectedIndex();
                
                ignoredAtts = newIgnoredAtts;
              }
            }
            
            Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_Fourteenth"));
            
            switch (testMode) {
            case 3: 
              Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_Fifteenth"));
              



              break;
            case 2: 
              Messages.getInstance();Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_Sixteenth") + percent + Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_Seventeenth"));
              








              break;
            case 4: 
              Messages.getInstance();Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_Eighteenth") + userTest.numInstances() + Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_Nineteenth"));
              








              break;
            case 5: 
              Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_Twentyth"));
            }
            
            

            outBuff.append("\n");
            m_History.addResult(name, outBuff);
            m_History.setSingle(name);
            

            Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_StartClusterer_Run_Log_StatusMessage_Second"));
            

            trainTimeStart = System.currentTimeMillis();
            
            clusterer.buildClusterer(ClustererPanel.this.removeClass(trainInst));
            trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
            

            Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_TwentySecond"));
            




            outBuff.append(clusterer.toString() + '\n');
            Messages.getInstance();Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_TimeTakenFull") + Utils.doubleToString(trainTimeElapsed / 1000.0D, 2) + " " + Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_TwentyNineth"));
            










            m_History.updateResult(name);
            if ((clusterer instanceof Drawable)) {
              try {
                grph = ((Drawable)clusterer).graph();
              }
              catch (Exception ex) {}
            }
            
            SerializedObject so = new SerializedObject(clusterer);
            fullClusterer = (Clusterer)so.getObject();
            
            ClusterEvaluation eval = new ClusterEvaluation();
            eval.setClusterer(clusterer);
            switch (testMode) {
            case 3: 
            case 5: 
              Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_StartClusterer_Run_Log_StatusMessage_Third"));
              
              eval.evaluateClusterer(trainInst, "", false);
              predData = ClustererPanel.setUpVisualizableInstances(inst, eval);
              Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_TwentySecond"));
              



              break;
            
            case 2: 
              Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_StartClusterer_Run_Log_StatusMessage_Fourth"));
              



              inst.randomize(new Random(1L));
              trainInst.randomize(new Random(1L));
              int trainSize = trainInst.numInstances() * percent / 100;
              int testSize = trainInst.numInstances() - trainSize;
              Instances train = new Instances(trainInst, 0, trainSize);
              Instances test = new Instances(trainInst, trainSize, testSize);
              Instances testVis = new Instances(inst, trainSize, testSize);
              Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_StartClusterer_Run_Log_StatusMessage_Fifth"));
              
              trainTimeStart = System.currentTimeMillis();
              clusterer.buildClusterer(train);
              trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
              
              Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_StartClusterer_Run_Log_StatusMessage_Sixth"));
              
              eval.evaluateClusterer(test, "", false);
              predData = ClustererPanel.setUpVisualizableInstances(testVis, eval);
              Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_TwentyThird"));
              



              outBuff.append(clusterer.toString() + '\n');
              Messages.getInstance();Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_TimeTakenPercentage") + Utils.doubleToString(trainTimeElapsed / 1000.0D, 2) + " " + Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_TwentyNineth"));
              









              break;
            
            case 4: 
              Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_StartClusterer_Run_Log_StatusMessage_Seventh"));
              



              Instances userTestT = new Instances(userTest);
              if (!m_ignoreKeyList.isSelectionEmpty()) {
                userTestT = ClustererPanel.this.removeIgnoreCols(userTestT);
              }
              eval.evaluateClusterer(userTestT, "", false);
              predData = ClustererPanel.setUpVisualizableInstances(userTest, eval);
              Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_StartClusterer_Run_OutBuffer_Text_TwentyFourth"));
              



              break;
            
            default: 
              Messages.getInstance();throw new Exception(Messages.getString("ClustererPanel_StartClusterer_Run_Exception_Text_Sixth"));
            }
            
            outBuff.append(eval.clusterResultsToString());
            outBuff.append("\n");
            m_History.updateResult(name);
            Messages.getInstance();m_Log.logMessage(Messages.getString("ClustererPanel_StartClusterer_Run_Log_LogMessage_Text_Third") + cname);
            

            Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_StartClusterer_Run_Log_StatusMessage_Eighth"));
          }
          catch (Exception ex) {
            ex.printStackTrace();
            m_Log.logMessage(ex.getMessage());
            Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(ClustererPanel.this, Messages.getString("ClustererPanel_StartClusterer_Run_JOptionPaneShowMessageDialog_Text_First") + ex.getMessage(), Messages.getString("ClustererPanel_StartClusterer_Run_JOptionPaneShowMessageDialog_Text_Second"), 0);
            











            Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_StartClusterer_Run_Log_StatusMessage_Nineth"));
          }
          finally {
            if (predData != null) {
              m_CurrentVis = new VisualizePanel();
              m_CurrentVis.setName(name + " (" + inst.relationName() + ")");
              m_CurrentVis.setLog(m_Log);
              predData.setPlotName(name + " (" + inst.relationName() + ")");
              try
              {
                m_CurrentVis.addPlot(predData);
              } catch (Exception ex) {
                System.err.println(ex);
              }
              
              FastVector vv = new FastVector();
              vv.addElement(fullClusterer);
              Instances trainHeader = new Instances(m_Instances, 0);
              vv.addElement(trainHeader);
              if (ignoredAtts != null)
                vv.addElement(ignoredAtts);
              if (saveVis) {
                vv.addElement(m_CurrentVis);
                if (grph != null) {
                  vv.addElement(grph);
                }
              }
              
              m_History.addObject(name, vv);
            }
            if (isInterrupted()) {
              Messages.getInstance();m_Log.logMessage(Messages.getString("ClustererPanel_StartClusterer_Run_Log_LogMessage_Text_Fourth") + cname);
              




              Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_StartClusterer_Run_Log_StatusMessage_Tenth"));
            }
            
            m_RunThread = null;
            m_StartBut.setEnabled(true);
            m_StopBut.setEnabled(false);
            m_ignoreBut.setEnabled(true);
            if ((m_Log instanceof TaskLogger)) {
              ((TaskLogger)m_Log).taskFinished();
            }
          }
        }
      };
      m_RunThread.setPriority(1);
      m_RunThread.start();
    }
  }
  
  private Instances removeClass(Instances inst) {
    Remove af = new Remove();
    Instances retI = null;
    try
    {
      if (inst.classIndex() < 0) {
        retI = inst;
      } else {
        af.setAttributeIndices("" + (inst.classIndex() + 1));
        af.setInvertSelection(false);
        af.setInputFormat(inst);
        retI = Filter.useFilter(inst, af);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return retI;
  }
  



  private Instances removeIgnoreCols(Instances inst)
  {
    if (m_ClassesToClustersBut.isSelected()) {
      int classIndex = m_ClassCombo.getSelectedIndex();
      if (m_ignoreKeyList.isSelectedIndex(classIndex)) {
        m_ignoreKeyList.removeSelectionInterval(classIndex, classIndex);
      }
    }
    int[] selected = m_ignoreKeyList.getSelectedIndices();
    Remove af = new Remove();
    Instances retI = null;
    try
    {
      af.setAttributeIndicesArray(selected);
      af.setInvertSelection(false);
      af.setInputFormat(inst);
      retI = Filter.useFilter(inst, af);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return retI;
  }
  
  private Instances removeIgnoreCols(Instances inst, int[] toIgnore)
  {
    Remove af = new Remove();
    Instances retI = null;
    try
    {
      af.setAttributeIndicesArray(toIgnore);
      af.setInvertSelection(false);
      af.setInputFormat(inst);
      retI = Filter.useFilter(inst, af);
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return retI;
  }
  



  protected void stopClusterer()
  {
    if (m_RunThread != null) {
      m_RunThread.interrupt();
      

      m_RunThread.stop();
    }
  }
  







  protected void visualizeTree(String graphString, String treeName)
  {
    Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("ClustererPanel_VisualizeTree_JFrame_Text") + treeName);
    
    jf.setSize(500, 400);
    jf.getContentPane().setLayout(new BorderLayout());
    if (graphString.contains("digraph")) {
      TreeVisualizer tv = new TreeVisualizer(null, graphString, new PlaceNode2());
      
      jf.getContentPane().add(tv, "Center");
      jf.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          jf.dispose();
        }
        
      });
      jf.setVisible(true);
      tv.fitToScreen();
    } else { Messages.getInstance(); if (graphString.startsWith(Messages.getString("ClustererPanel_VisualizeTree_GraphStringStartsWith_Text")))
      {
        HierarchyVisualizer tv = new HierarchyVisualizer(graphString.substring(7));
        jf.getContentPane().add(tv, "Center");
        jf.addWindowListener(new WindowAdapter()
        {
          public void windowClosing(WindowEvent e) {
            jf.dispose();
          }
        });
        jf.setVisible(true);
        tv.fitToScreen();
      }
    }
  }
  



  protected void visualizeClusterAssignments(VisualizePanel sp)
  {
    if (sp != null) {
      String plotName = sp.getName();
      Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("ClustererPanel_VisualizeClusterAssignments_JFrame_Text") + plotName);
      


      jf.setSize(500, 400);
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(sp, "Center");
      jf.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          jf.dispose();
        }
        
      });
      jf.setVisible(true);
    }
  }
  







  protected void visualizeClusterer(String name, int x, int y)
  {
    final String selectedName = name;
    JPopupMenu resultListMenu = new JPopupMenu();
    
    Messages.getInstance();JMenuItem visMainBuffer = new JMenuItem(Messages.getString("ClustererPanel_VisualizeClusterer_VisMainBuffer_JMenuItem_Text"));
    
    if (selectedName != null) {
      visMainBuffer.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          m_History.setSingle(selectedName);
        }
      });
    } else {
      visMainBuffer.setEnabled(false);
    }
    resultListMenu.add(visMainBuffer);
    
    Messages.getInstance();JMenuItem visSepBuffer = new JMenuItem(Messages.getString("ClustererPanel_VisualizeClusterer_VisSepBuffer_JMenuItem_Text"));
    
    if (selectedName != null) {
      visSepBuffer.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          m_History.openFrame(selectedName);
        }
      });
    } else {
      visSepBuffer.setEnabled(false);
    }
    resultListMenu.add(visSepBuffer);
    
    Messages.getInstance();JMenuItem saveOutput = new JMenuItem(Messages.getString("ClustererPanel_VisualizeClusterer_SaveOutput_JMenuItem_Text"));
    
    if (selectedName != null) {
      saveOutput.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveBuffer(selectedName);
        }
      });
    } else {
      saveOutput.setEnabled(false);
    }
    resultListMenu.add(saveOutput);
    
    Messages.getInstance();JMenuItem deleteOutput = new JMenuItem(Messages.getString("ClustererPanel_VisualizeClusterer_DeleteOutput_JMenuItem_Text"));
    
    if (selectedName != null) {
      deleteOutput.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          m_History.removeResult(selectedName);
        }
      });
    } else {
      deleteOutput.setEnabled(false);
    }
    resultListMenu.add(deleteOutput);
    
    resultListMenu.addSeparator();
    
    Messages.getInstance();JMenuItem loadModel = new JMenuItem(Messages.getString("ClustererPanel_VisualizeClusterer_LoadModel_JMenuItem_Text"));
    
    loadModel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        loadClusterer();
      }
    });
    resultListMenu.add(loadModel);
    
    FastVector o = null;
    if (selectedName != null) {
      o = (FastVector)m_History.getNamedObject(selectedName);
    }
    
    VisualizePanel temp_vp = null;
    String temp_grph = null;
    Clusterer temp_clusterer = null;
    Instances temp_trainHeader = null;
    int[] temp_ignoreAtts = null;
    
    if (o != null) {
      for (int i = 0; i < o.size(); i++) {
        Object temp = o.elementAt(i);
        if ((temp instanceof Clusterer)) {
          temp_clusterer = (Clusterer)temp;
        } else if ((temp instanceof Instances)) {
          temp_trainHeader = (Instances)temp;
        } else if ((temp instanceof int[])) {
          temp_ignoreAtts = (int[])temp;
        } else if ((temp instanceof VisualizePanel)) {
          temp_vp = (VisualizePanel)temp;
        } else if ((temp instanceof String)) {
          temp_grph = (String)temp;
        }
      }
    }
    
    final VisualizePanel vp = temp_vp;
    final String grph = temp_grph;
    final Clusterer clusterer = temp_clusterer;
    final Instances trainHeader = temp_trainHeader;
    final int[] ignoreAtts = temp_ignoreAtts;
    
    Messages.getInstance();JMenuItem saveModel = new JMenuItem(Messages.getString("ClustererPanel_VisualizeClusterer_SaveModel_JMenuItem_Text"));
    
    if (clusterer != null) {
      saveModel.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveClusterer(selectedName, clusterer, trainHeader, ignoreAtts);
        }
      });
    } else {
      saveModel.setEnabled(false);
    }
    resultListMenu.add(saveModel);
    
    Messages.getInstance();JMenuItem reEvaluate = new JMenuItem(Messages.getString("ClustererPanel_VisualizeClusterer_ReEvaluate_JMenuItem_Text"));
    
    if ((clusterer != null) && (m_TestInstances != null)) {
      reEvaluate.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          reevaluateModel(selectedName, clusterer, trainHeader, ignoreAtts);
        }
      });
    } else {
      reEvaluate.setEnabled(false);
    }
    resultListMenu.add(reEvaluate);
    
    resultListMenu.addSeparator();
    
    Messages.getInstance();JMenuItem visClusts = new JMenuItem(Messages.getString("ClustererPanel_VisualizeClusterer_VisClusts_JMenuItem_Text"));
    
    if (vp != null) {
      visClusts.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          visualizeClusterAssignments(vp);
        }
        
      });
    } else {
      visClusts.setEnabled(false);
    }
    resultListMenu.add(visClusts);
    
    Messages.getInstance();JMenuItem visTree = new JMenuItem(Messages.getString("ClustererPanel_VisualizeClusterer_VisTree_JMenuItem_Text"));
    
    if (grph != null) {
      visTree.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) { String title;
          String title;
          if (vp != null) {
            title = vp.getName();
          } else
            title = selectedName;
          visualizeTree(grph, title);
        }
      });
    } else {
      visTree.setEnabled(false);
    }
    resultListMenu.add(visTree);
    
    resultListMenu.show(m_History.getList(), x, y);
  }
  




  protected void saveBuffer(String name)
  {
    StringBuffer sb = m_History.getNamedBuffer(name);
    if ((sb != null) && 
      (m_SaveOut.save(sb))) {
      Messages.getInstance();m_Log.logMessage(Messages.getString("ClustererPanel_VisualizeClusterer_SaveBuffer_Log_LohMessage_Text"));
    }
  }
  




  private void setIgnoreColumns()
  {
    ListSelectorDialog jd = new ListSelectorDialog(null, m_ignoreKeyList);
    

    int result = jd.showDialog();
    
    if (result != 0)
    {
      m_ignoreKeyList.clearSelection();
    }
    updateCapabilitiesFilter(m_ClustererEditor.getCapabilitiesFilter());
  }
  




  protected void saveClusterer(String name, Clusterer clusterer, Instances trainHeader, int[] ignoredAtts)
  {
    File sFile = null;
    boolean saveOK = true;
    
    int returnVal = m_FileChooser.showSaveDialog(this);
    if (returnVal == 0) {
      sFile = m_FileChooser.getSelectedFile();
      if (!sFile.getName().toLowerCase().endsWith(MODEL_FILE_EXTENSION)) {
        sFile = new File(sFile.getParent(), sFile.getName() + MODEL_FILE_EXTENSION);
      }
      
      Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_VisualizeClusterer_SaveBuffer_Log_LohMessage_Text_Alpha"));
      



      try
      {
        OutputStream os = new FileOutputStream(sFile);
        if (sFile.getName().endsWith(".gz")) {
          os = new GZIPOutputStream(os);
        }
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
        objectOutputStream.writeObject(clusterer);
        if (trainHeader != null)
          objectOutputStream.writeObject(trainHeader);
        if (ignoredAtts != null)
          objectOutputStream.writeObject(ignoredAtts);
        objectOutputStream.flush();
        objectOutputStream.close();
      }
      catch (Exception e) {
        Messages.getInstance();JOptionPane.showMessageDialog(null, e, Messages.getString("ClustererPanel_VisualizeClusterer_SaveCluster_JOptionPaneShowMessageDialog_Text"), 0);
        







        saveOK = false;
      }
      if (saveOK) {
        Messages.getInstance();Messages.getInstance();m_Log.logMessage(Messages.getString("ClustererPanel_VisualizeClusterer_SaveCluster_Log_LogMessage_Text") + name + Messages.getString("ClustererPanel_VisualizeClusterer_SaveCluster_Log_LogMessage_Text_Alpha") + sFile.getName() + "'");
      }
      









      Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_VisualizeClusterer_SaveCluster_Log_StatusMessage_Text"));
    }
  }
  







  protected void loadClusterer()
  {
    int returnVal = m_FileChooser.showOpenDialog(this);
    if (returnVal == 0) {
      File selected = m_FileChooser.getSelectedFile();
      Clusterer clusterer = null;
      Instances trainHeader = null;
      int[] ignoredAtts = null;
      
      Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_VisualizeClusterer_LoadClusterer_Log_StatusSessage_Text_First"));
      



      try
      {
        InputStream is = new FileInputStream(selected);
        if (selected.getName().endsWith(".gz")) {
          is = new GZIPInputStream(is);
        }
        ObjectInputStream objectInputStream = new ObjectInputStream(is);
        clusterer = (Clusterer)objectInputStream.readObject();
        try {
          trainHeader = (Instances)objectInputStream.readObject();
          ignoredAtts = (int[])objectInputStream.readObject();
        }
        catch (Exception e) {}
        objectInputStream.close();
      }
      catch (Exception e) {
        Messages.getInstance();JOptionPane.showMessageDialog(null, e, Messages.getString("ClustererPanel_VisualizeClusterer_LoadClusterer_JOptionPaneShowMessageDialog_Text"), 0);
      }
      








      Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_VisualizeClusterer_LoadClusterer_Log_StatusMessage_Text_Second"));
      




      if (clusterer != null) {
        Messages.getInstance();m_Log.logMessage(Messages.getString("ClustererPanel_VisualizeClusterer_LoadClusterer_Log_LogMessage_Text_First") + selected.getName() + "'");
        




        String name = new SimpleDateFormat("HH:mm:ss - ").format(new Date());
        String cname = clusterer.getClass().getName();
        if (cname.startsWith("weka.clusterers."))
          cname = cname.substring("weka.clusterers.".length());
        Messages.getInstance();name = name + cname + Messages.getString("ClustererPanel_VisualizeClusterer_LoadClusterer_CNAme_Text_First") + selected.getName() + "'";
        




        StringBuffer outBuff = new StringBuffer();
        
        Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_VisualizeClusterer_LoadClusterer_OutBuffer_Text_First"));
        



        Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_VisualizeClusterer_LoadClusterer_OutBuffer_Text_Second") + selected.getName() + "\n");
        




        Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_VisualizeClusterer_LoadClusterer_OutBuffer_Text_Fourth") + clusterer.getClass().getName());
        




        if ((clusterer instanceof OptionHandler)) {
          String[] o = ((OptionHandler)clusterer).getOptions();
          outBuff.append(" " + Utils.joinOptions(o));
        }
        outBuff.append("\n");
        
        if (trainHeader != null)
        {
          Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_VisualizeClusterer_LoadClusterer_OutBuffer_Text_Sixth") + trainHeader.relationName() + '\n');
          




          Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_VisualizeClusterer_LoadClusterer_OutBuffer_Text_Eighth") + trainHeader.numAttributes() + '\n');
          




          if (trainHeader.numAttributes() < 100) {
            boolean[] selectedAtts = new boolean[trainHeader.numAttributes()];
            for (int i = 0; i < trainHeader.numAttributes(); i++) {
              selectedAtts[i] = true;
            }
            
            if (ignoredAtts != null) {
              for (int i = 0; i < ignoredAtts.length; i++)
                selectedAtts[ignoredAtts[i]] = false;
            }
            for (int i = 0; i < trainHeader.numAttributes(); i++) {
              if (selectedAtts[i] != 0) {
                outBuff.append("              " + trainHeader.attribute(i).name() + '\n');
              }
            }
            
            if (ignoredAtts != null) {
              Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_VisualizeClusterer_LoadClusterer_OutBuffer_Text_Eleventh"));
              



              for (int i = 0; i < ignoredAtts.length; i++) {
                outBuff.append("              " + trainHeader.attribute(ignoredAtts[i]).name() + '\n');
              }
            }
          } else {
            Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_VisualizeClusterer_LoadClusterer_OutBuffer_Text_Twelveth"));
          }
          

        }
        else
        {
          Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_VisualizeClusterer_LoadClusterer_OutBuffer_Text_Thirteenth"));
        }
        




        Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_VisualizeClusterer_LoadClusterer_OutBuffer_Text_Fourteenth"));
        



        outBuff.append(clusterer.toString() + "\n");
        
        m_History.addResult(name, outBuff);
        m_History.setSingle(name);
        FastVector vv = new FastVector();
        vv.addElement(clusterer);
        if (trainHeader != null)
          vv.addElement(trainHeader);
        if (ignoredAtts != null) {
          vv.addElement(ignoredAtts);
        }
        String grph = null;
        if ((clusterer instanceof Drawable)) {
          try {
            grph = ((Drawable)clusterer).graph();
          }
          catch (Exception ex) {}
        }
        if (grph != null) {
          vv.addElement(grph);
        }
        m_History.addObject(name, vv);
      }
    }
  }
  











  protected void reevaluateModel(final String name, final Clusterer clusterer, final Instances trainHeader, final int[] ignoredAtts)
  {
    if (m_RunThread == null) {
      m_StartBut.setEnabled(false);
      m_StopBut.setEnabled(true);
      m_ignoreBut.setEnabled(false);
      m_RunThread = new Thread()
      {
        public void run()
        {
          Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_ReEvaluateModel_Run_Log_StatusMessage_Text_First"));
          




          StringBuffer outBuff = m_History.getNamedBuffer(name);
          Instances userTest = null;
          
          PlotData2D predData = null;
          if (m_TestInstances != null) {
            userTest = new Instances(m_TestInstances);
          }
          
          boolean saveVis = m_StorePredictionsBut.isSelected();
          String grph = null;
          try
          {
            if (userTest == null) {
              Messages.getInstance();throw new Exception(Messages.getString("ClustererPanel_ReEvaluateModel_Run_Exception_Text_First"));
            }
            
            if ((trainHeader != null) && (!trainHeader.equalHeaders(userTest))) {
              Messages.getInstance();throw new Exception(Messages.getString("ClustererPanel_ReEvaluateModel_Run_Exception_Text_Second"));
            }
            

            Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_ReEvaluateModel_Run_Log_StatusMessage_Text_Second"));
            



            Messages.getInstance();Messages.getInstance();m_Log.logMessage(Messages.getString("ClustererPanel_ReEvaluateModel_Run_Log_LogMessage_Text_First") + name + Messages.getString("ClustererPanel_ReEvaluateModel_Run_Log_LogMessage_Text_Second"));
            









            Messages.getInstance();m_Log.logMessage(Messages.getString("ClustererPanel_ReEvaluateModel_Run_Log_LogMessage_Text_Third"));
            



            if ((m_Log instanceof TaskLogger)) {
              ((TaskLogger)m_Log).taskStarted();
            }
            ClusterEvaluation eval = new ClusterEvaluation();
            eval.setClusterer(clusterer);
            
            Instances userTestT = new Instances(userTest);
            if (ignoredAtts != null) {
              userTestT = ClustererPanel.this.removeIgnoreCols(userTestT, ignoredAtts);
            }
            
            eval.evaluateClusterer(userTestT);
            
            predData = ClustererPanel.setUpVisualizableInstances(userTest, eval);
            
            Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_ReEvaluateModel_Run_OutBuffer_Text_First"));
            
            Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_ReEvaluateModel_Run_OutBuffer_Text_Second"));
            
            Messages.getInstance();Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_ReEvaluateModel_Run_OutBuffer_Text_Third") + userTest.relationName() + Messages.getString("ClustererPanel_ReEvaluateModel_Run_OutBuffer_Text_Fourth"));
            






            Messages.getInstance();Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_ReEvaluateModel_Run_OutBuffer_Text_Fifth") + userTest.numInstances() + Messages.getString("ClustererPanel_ReEvaluateModel_Run_OutBuffer_Text_Sixth"));
            



            Messages.getInstance();Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_ReEvaluateModel_Run_OutBuffer_Text_Seventh") + userTest.numAttributes() + Messages.getString("ClustererPanel_ReEvaluateModel_Run_OutBuffer_Text_Eighth"));
            








            if (trainHeader == null) {
              Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_ReEvaluateModel_Run_OutBuffer_Text_Nineth"));
            }
            
            outBuff.append(eval.clusterResultsToString());
            Messages.getInstance();outBuff.append(Messages.getString("ClustererPanel_ReEvaluateModel_Run_OutBuffer_Text_Tenth"));
            
            m_History.updateResult(name);
            Messages.getInstance();m_Log.logMessage(Messages.getString("ClustererPanel_ReEvaluateModel_Run_Log_LogMessage_Text_Fourth"));
            



            Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_ReEvaluateModel_Run_Log_StatusMessage_Text_Third"));

          }
          catch (Exception ex)
          {

            ex.printStackTrace();
            m_Log.logMessage(ex.getMessage());
            Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(ClustererPanel.this, Messages.getString("ClustererPanel_ReEvaluateModel_Run_JOptionPaneShowMessageDialog_Text_First") + ex.getMessage(), Messages.getString("ClustererPanel_ReEvaluateModel_Run_JOptionPaneShowMessageDialog_Text_Second"), 0);
            











            Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_ReEvaluateModel_Run_Log_StatusMessage_Text_Fourth"));


          }
          finally
          {

            if (predData != null) {
              m_CurrentVis = new VisualizePanel();
              m_CurrentVis.setName(name + " (" + userTest.relationName() + ")");
              m_CurrentVis.setLog(m_Log);
              predData.setPlotName(name + " (" + userTest.relationName() + ")");
              try
              {
                m_CurrentVis.addPlot(predData);
              } catch (Exception ex) {
                System.err.println(ex);
              }
              
              FastVector vv = new FastVector();
              vv.addElement(clusterer);
              if (trainHeader != null)
                vv.addElement(trainHeader);
              if (ignoredAtts != null)
                vv.addElement(ignoredAtts);
              if (saveVis) {
                vv.addElement(m_CurrentVis);
                if (grph != null) {
                  vv.addElement(grph);
                }
              }
              
              m_History.addObject(name, vv);
            }
            
            if (isInterrupted()) {
              Messages.getInstance();m_Log.logMessage(Messages.getString("ClustererPanel_ReEvaluateModel_Run_Log_LogMessage_Text_Fifth"));
              



              Messages.getInstance();m_Log.statusMessage(Messages.getString("ClustererPanel_ReEvaluateModel_Run_Log_StatusMessage_Text_Fifth"));
            }
            



            m_RunThread = null;
            m_StartBut.setEnabled(true);
            m_StopBut.setEnabled(false);
            m_ignoreBut.setEnabled(true);
            if ((m_Log instanceof TaskLogger)) {
              ((TaskLogger)m_Log).taskFinished();
            }
            
          }
        }
      };
      m_RunThread.setPriority(1);
      m_RunThread.start();
    }
  }
  







  protected void updateCapabilitiesFilter(Capabilities filter)
  {
    if (filter == null) {
      m_ClustererEditor.setCapabilitiesFilter(new Capabilities(null)); return;
    }
    Instances tempInst;
    Instances tempInst;
    if (!ExplorerDefaults.getInitGenericObjectEditorFilter()) {
      tempInst = new Instances(m_Instances, 0);
    } else
      tempInst = new Instances(m_Instances);
    tempInst.setClassIndex(-1);
    
    if (!m_ignoreKeyList.isSelectionEmpty()) {
      tempInst = removeIgnoreCols(tempInst);
    }
    
    if (m_ClassesToClustersBut.isSelected())
    {
      String classSelection = m_ClassCombo.getSelectedItem().toString();
      classSelection = classSelection.substring(classSelection.indexOf(")") + 1).trim();
      
      int classIndex = tempInst.attribute(classSelection).index();
      
      Remove rm = new Remove();
      rm.setAttributeIndices("" + (classIndex + 1));
      try {
        rm.setInputFormat(tempInst);
        tempInst = Filter.useFilter(tempInst, rm);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    Capabilities filterClass;
    try {
      filterClass = Capabilities.forInstances(tempInst);
    } catch (Exception e) {
      filterClass = new Capabilities(null);
    }
    
    m_ClustererEditor.setCapabilitiesFilter(filterClass);
    

    m_StartBut.setEnabled(true);
    Capabilities currentFilter = m_ClustererEditor.getCapabilitiesFilter();
    Clusterer clusterer = (Clusterer)m_ClustererEditor.getValue();
    Capabilities currentSchemeCapabilities = null;
    if ((clusterer != null) && (currentFilter != null) && ((clusterer instanceof CapabilitiesHandler)))
    {
      currentSchemeCapabilities = ((CapabilitiesHandler)clusterer).getCapabilities();
      

      if ((!currentSchemeCapabilities.supportsMaybe(currentFilter)) && (!currentSchemeCapabilities.supports(currentFilter)))
      {
        m_StartBut.setEnabled(false);
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
  




  public void setExplorer(Explorer parent)
  {
    m_Explorer = parent;
  }
  




  public Explorer getExplorer()
  {
    return m_Explorer;
  }
  




  public String getTabTitle()
  {
    Messages.getInstance();return Messages.getString("ClustererPanel_GetTabTitle_Text");
  }
  




  public String getTabTitleToolTip()
  {
    Messages.getInstance();return Messages.getString("ClustererPanel_GetTabTitleToolTip_Text");
  }
  





  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("ClustererPanel_Main_JFrame_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      ClustererPanel sp = new ClustererPanel();
      jf.getContentPane().add(sp, "Center");
      LogPanel lp = new LogPanel();
      sp.setLog(lp);
      jf.getContentPane().add(lp, "South");
      jf.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setSize(800, 600);
      jf.setVisible(true);
      if (args.length == 1) {
        Messages.getInstance();System.err.println(Messages.getString("ClustererPanel_Main_Error_Text_First") + args[0]);
        

        Reader r = new BufferedReader(new FileReader(args[0]));
        
        Instances i = new Instances(r);
        sp.setInstances(i);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
