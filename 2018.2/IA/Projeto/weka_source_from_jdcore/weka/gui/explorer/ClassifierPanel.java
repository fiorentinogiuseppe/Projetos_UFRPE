package weka.gui.explorer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
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
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.Sourcable;
import weka.classifiers.evaluation.CostCurve;
import weka.classifiers.evaluation.MarginCurve;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.pmml.consumer.PMMLClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Drawable;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Memory;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.SerializedObject;
import weka.core.Utils;
import weka.core.Version;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.converters.IncrementalConverter;
import weka.core.converters.Loader;
import weka.core.pmml.MiningSchema;
import weka.core.pmml.PMMLFactory;
import weka.core.pmml.PMMLModel;
import weka.gui.CostMatrixEditor;
import weka.gui.ExtensionFileFilter;
import weka.gui.GenericObjectEditor;
import weka.gui.LogPanel;
import weka.gui.Logger;
import weka.gui.PropertyDialog;
import weka.gui.PropertyPanel;
import weka.gui.ResultHistoryPanel;
import weka.gui.SaveBuffer;
import weka.gui.SetInstancesPanel;
import weka.gui.SysErrLog;
import weka.gui.TaskLogger;
import weka.gui.beans.CostBenefitAnalysis;
import weka.gui.graphvisualizer.BIFFormatException;
import weka.gui.graphvisualizer.GraphVisualizer;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;
import weka.gui.visualize.VisualizePanel;
import weka.gui.visualize.plugins.ErrorVisualizePlugin;
import weka.gui.visualize.plugins.GraphVisualizePlugin;
import weka.gui.visualize.plugins.TreeVisualizePlugin;
import weka.gui.visualize.plugins.VisualizePlugin;




































public class ClassifierPanel
  extends JPanel
  implements Explorer.CapabilitiesFilterChangeListener, Explorer.ExplorerPanel, Explorer.LogHandler
{
  static final long serialVersionUID = 6959973704963624003L;
  protected Explorer m_Explorer = null;
  

  public static String MODEL_FILE_EXTENSION = ".model";
  

  public static String PMML_FILE_EXTENSION = ".xml";
  

  protected GenericObjectEditor m_ClassifierEditor = new GenericObjectEditor();
  

  protected PropertyPanel m_CEPanel = new PropertyPanel(m_ClassifierEditor);
  

  protected JTextArea m_OutText = new JTextArea(20, 40);
  

  protected Logger m_Log = new SysErrLog();
  

  SaveBuffer m_SaveOut = new SaveBuffer(m_Log, this);
  

  protected ResultHistoryPanel m_History = new ResultHistoryPanel(m_OutText);
  

  protected JComboBox m_ClassCombo = new JComboBox();
  



  protected JRadioButton m_CVBut;
  



  protected JRadioButton m_PercentBut;
  



  protected JRadioButton m_TrainBut;
  



  protected JRadioButton m_TestSplitBut;
  



  protected JCheckBox m_StorePredictionsBut;
  



  protected JCheckBox m_OutputModelBut;
  



  protected JCheckBox m_OutputPerClassBut;
  


  protected JCheckBox m_OutputConfusionBut;
  


  protected JCheckBox m_OutputEntropyBut;
  


  protected JCheckBox m_OutputPredictionsTextBut;
  


  protected JTextField m_OutputAdditionalAttributesText;
  


  protected JLabel m_OutputAdditionalAttributesLab;
  


  protected Range m_OutputAdditionalAttributesRange;
  


  protected JCheckBox m_EvalWRTCostsBut;
  


  protected JButton m_SetCostsBut;
  


  protected JLabel m_CVLab;
  


  protected JTextField m_CVText;
  


  protected JLabel m_PercentLab;
  


  protected JTextField m_PercentText;
  


  protected JButton m_SetTestBut;
  


  protected JFrame m_SetTestFrame;
  


  protected PropertyDialog m_SetCostsFrame;
  


  ActionListener m_RadioListener;
  


  JButton m_MoreOptions;
  


  protected JTextField m_RandomSeedText;
  


  protected JLabel m_RandomLab;
  


  protected JCheckBox m_PreserveOrderBut;
  


  protected JCheckBox m_OutputSourceCode;
  


  protected JTextField m_SourceCodeClass;
  


  protected JButton m_StartBut;
  


  protected JButton m_StopBut;
  


  private final Dimension COMBO_SIZE;
  


  protected CostMatrixEditor m_CostMatrixEditor;
  


  protected Instances m_Instances;
  


  protected Loader m_TestLoader;
  


  protected Thread m_RunThread;
  


  protected VisualizePanel m_CurrentVis;
  


  protected FileFilter m_ModelFilter;
  


  protected FileFilter m_PMMLModelFilter;
  


  protected JFileChooser m_FileChooser;
  



  static
  {
    GenericObjectEditor.registerEditors();
  }
  
  public ClassifierPanel()
  {
    Messages.getInstance();m_CVBut = new JRadioButton(Messages.getString("ClassifierPanel_CVBut_JRadioButton_Text"));
    


    Messages.getInstance();m_PercentBut = new JRadioButton(Messages.getString("ClassifierPanel_PercentBut_JRadioButton_Text"));
    


    Messages.getInstance();m_TrainBut = new JRadioButton(Messages.getString("ClassifierPanel_TrainBut_JRadioButton_Text"));
    


    Messages.getInstance();m_TestSplitBut = new JRadioButton(Messages.getString("ClassifierPanel_TestSplitBut_JRadioButton_Text"));
    




    Messages.getInstance();m_StorePredictionsBut = new JCheckBox(Messages.getString("ClassifierPanel_StorePredictionsBut_JCheckBox_Text"));
    



    Messages.getInstance();m_OutputModelBut = new JCheckBox(Messages.getString("ClassifierPanel_OutputModelBut_JCheckBox_Text"));
    


    Messages.getInstance();m_OutputPerClassBut = new JCheckBox(Messages.getString("ClassifierPanel_OutputPerClassBut_JCheckBox_Text"));
    



    Messages.getInstance();m_OutputConfusionBut = new JCheckBox(Messages.getString("ClassifierPanel_OutputConfusionBut_JCheckBox_Text"));
    



    Messages.getInstance();m_OutputEntropyBut = new JCheckBox(Messages.getString("ClassifierPanel_OutputEntropyBut_JCheckBox_Text"));
    


    Messages.getInstance();m_OutputPredictionsTextBut = new JCheckBox(Messages.getString("ClassifierPanel_OutputPredictionsTextBut_JCheckBox_Text"));
    



    m_OutputAdditionalAttributesText = new JTextField("", 10);
    

    Messages.getInstance();m_OutputAdditionalAttributesLab = new JLabel(Messages.getString("ClassifierPanel_OutputAdditionalAttributesLab_JLabel_Text"));
    



    m_OutputAdditionalAttributesRange = null;
    

    Messages.getInstance();m_EvalWRTCostsBut = new JCheckBox(Messages.getString("ClassifierPanel_EvalWRTCostsBut_JCheckBox_Text"));
    


    Messages.getInstance();m_SetCostsBut = new JButton(Messages.getString("ClassifierPanel_SetCostsBut_JButton_Text"));
    


    Messages.getInstance();m_CVLab = new JLabel(Messages.getString("ClassifierPanel_CVLab_JLabel_Text"), 4);
    


    Messages.getInstance();m_CVText = new JTextField(Messages.getString("ClassifierPanel_CVText_JTextField_Text"), 3);
    


    Messages.getInstance();m_PercentLab = new JLabel(Messages.getString("ClassifierPanel_PercentLab_JLabel_Text"), 4);
    


    Messages.getInstance();m_PercentText = new JTextField(Messages.getString("ClassifierPanel_PercentText_JTextField_Text"), 3);
    


    Messages.getInstance();m_SetTestBut = new JButton(Messages.getString("ClassifierPanel_SetTestBut_JButton_Text"));
    











    m_RadioListener = new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        updateRadioLinks();
      }
      

    };
    Messages.getInstance();m_MoreOptions = new JButton(Messages.getString("ClassifierPanel_MoreOptions_JButton_Text"));
    


    Messages.getInstance();m_RandomSeedText = new JTextField(Messages.getString("ClassifierPanel_RandomSeedText_JTextField_Text"), 3);
    


    Messages.getInstance();m_RandomLab = new JLabel(Messages.getString("ClassifierPanel_RandomLab_JLabel_Text"), 4);
    


    Messages.getInstance();m_PreserveOrderBut = new JCheckBox(Messages.getString("ClassifierPanel_PreserveOrderBut_JCheckBox_Text"));
    





    Messages.getInstance();m_OutputSourceCode = new JCheckBox(Messages.getString("ClassifierPanel_OutputSourceCode_JCheckBox_Text"));
    


    Messages.getInstance();m_SourceCodeClass = new JTextField(Messages.getString("ClassifierPanel_SourceCodeClass_JTextField_Text"), 10);
    



    Messages.getInstance();m_StartBut = new JButton(Messages.getString("ClassifierPanel_StartBut_JButton_Text"));
    


    Messages.getInstance();m_StopBut = new JButton(Messages.getString("ClassifierPanel_StopBut_JButton_Text"));
    


    COMBO_SIZE = new Dimension(150, m_StartBut.getPreferredSize().height);
    


    m_CostMatrixEditor = new CostMatrixEditor();
    










    m_CurrentVis = null;
    

    Messages.getInstance();m_ModelFilter = new ExtensionFileFilter(MODEL_FILE_EXTENSION, Messages.getString("ClassifierPanel_ModelFilter_FileFilter_Text"));
    


    Messages.getInstance();m_PMMLModelFilter = new ExtensionFileFilter(PMML_FILE_EXTENSION, Messages.getString("ClassifierPanel_PMMLModelFilter_FileFilter_Text"));
    



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
    Messages.getInstance();m_History.setBorder(BorderFactory.createTitledBorder(Messages.getString("ClassifierPanel_History_BorderFactoryCreateTitledBorder_Text")));
    

    m_ClassifierEditor.setClassType(Classifier.class);
    m_ClassifierEditor.setValue(ExplorerDefaults.getClassifier());
    m_ClassifierEditor.addPropertyChangeListener(new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent e) {
        m_StartBut.setEnabled(true);
        
        Capabilities currentFilter = m_ClassifierEditor.getCapabilitiesFilter();
        Classifier classifier = (Classifier)m_ClassifierEditor.getValue();
        Capabilities currentSchemeCapabilities = null;
        if ((classifier != null) && (currentFilter != null) && ((classifier instanceof CapabilitiesHandler)))
        {
          currentSchemeCapabilities = classifier.getCapabilities();
          

          if ((!currentSchemeCapabilities.supportsMaybe(currentFilter)) && (!currentSchemeCapabilities.supports(currentFilter)))
          {
            m_StartBut.setEnabled(false);
          }
        }
        repaint();
      }
      
    });
    Messages.getInstance();m_ClassCombo.setToolTipText(Messages.getString("ClassifierPanel_ClassCombo_SetToolTipText_Text"));
    
    Messages.getInstance();m_TrainBut.setToolTipText(Messages.getString("ClassifierPanel_TrainBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_CVBut.setToolTipText(Messages.getString("ClassifierPanel_CVBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_PercentBut.setToolTipText(Messages.getString("ClassifierPanel_PercentBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_TestSplitBut.setToolTipText(Messages.getString("ClassifierPanel_TestSplitBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_StartBut.setToolTipText(Messages.getString("ClassifierPanel_StartBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_StopBut.setToolTipText(Messages.getString("ClassifierPanel_StopBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_StorePredictionsBut.setToolTipText(Messages.getString("ClassifierPanel_StorePredictionsBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_OutputModelBut.setToolTipText(Messages.getString("ClassifierPanel_OutputModelBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_OutputPerClassBut.setToolTipText(Messages.getString("ClassifierPanel_OutputPerClassBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_OutputConfusionBut.setToolTipText(Messages.getString("ClassifierPanel_OutputConfusionBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_OutputEntropyBut.setToolTipText(Messages.getString("ClassifierPanel_OutputEntropyBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_EvalWRTCostsBut.setToolTipText(Messages.getString("ClassifierPanel_EvalWRTCostsBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_OutputPredictionsTextBut.setToolTipText(Messages.getString("ClassifierPanel_OutputPredictionsTextBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_OutputAdditionalAttributesText.setToolTipText(Messages.getString("ClassifierPanel_OutputAdditionalAttributesText_SetToolTipText_Text"));
    

    Messages.getInstance();m_RandomLab.setToolTipText(Messages.getString("ClassifierPanel_RandomLab_SetToolTipText_Text"));
    
    m_RandomSeedText.setToolTipText(m_RandomLab.getToolTipText());
    Messages.getInstance();m_PreserveOrderBut.setToolTipText(Messages.getString("ClassifierPanel_PreserveOrderBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_OutputSourceCode.setToolTipText(Messages.getString("ClassifierPanel_OutputSourceCode_SetToolTipText_Text"));
    
    Messages.getInstance();m_SourceCodeClass.setToolTipText(Messages.getString("ClassifierPanel_SourceCodeClass_SetToolTipText_Text"));
    

    m_FileChooser.addChoosableFileFilter(m_PMMLModelFilter);
    m_FileChooser.setFileFilter(m_ModelFilter);
    
    m_FileChooser.setFileSelectionMode(0);
    
    m_StorePredictionsBut.setSelected(ExplorerDefaults.getClassifierStorePredictionsForVis());
    
    m_OutputModelBut.setSelected(ExplorerDefaults.getClassifierOutputModel());
    m_OutputPerClassBut.setSelected(ExplorerDefaults.getClassifierOutputPerClassStats());
    
    m_OutputConfusionBut.setSelected(ExplorerDefaults.getClassifierOutputConfusionMatrix());
    
    m_EvalWRTCostsBut.setSelected(ExplorerDefaults.getClassifierCostSensitiveEval());
    
    m_OutputEntropyBut.setSelected(ExplorerDefaults.getClassifierOutputEntropyEvalMeasures());
    
    m_OutputPredictionsTextBut.setSelected(ExplorerDefaults.getClassifierOutputPredictions());
    
    m_OutputPredictionsTextBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        m_OutputAdditionalAttributesText.setEnabled(m_OutputPredictionsTextBut.isSelected());
      }
      
    });
    m_OutputAdditionalAttributesText.setText(ExplorerDefaults.getClassifierOutputAdditionalAttributes());
    
    m_OutputAdditionalAttributesText.setEnabled(m_OutputPredictionsTextBut.isSelected());
    
    m_RandomSeedText.setText("" + ExplorerDefaults.getClassifierRandomSeed());
    m_PreserveOrderBut.setSelected(ExplorerDefaults.getClassifierPreserveOrder());
    
    m_OutputSourceCode.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        m_SourceCodeClass.setEnabled(m_OutputSourceCode.isSelected());
      }
    });
    m_OutputSourceCode.setSelected(ExplorerDefaults.getClassifierOutputSourceCode());
    
    m_SourceCodeClass.setText(ExplorerDefaults.getClassifierSourceCodeClass());
    m_SourceCodeClass.setEnabled(m_OutputSourceCode.isSelected());
    m_ClassCombo.setEnabled(false);
    m_ClassCombo.setPreferredSize(COMBO_SIZE);
    m_ClassCombo.setMaximumSize(COMBO_SIZE);
    m_ClassCombo.setMinimumSize(COMBO_SIZE);
    
    m_CVBut.setSelected(true);
    
    m_CVBut.setSelected(ExplorerDefaults.getClassifierTestMode() == 1);
    m_PercentBut.setSelected(ExplorerDefaults.getClassifierTestMode() == 2);
    m_TrainBut.setSelected(ExplorerDefaults.getClassifierTestMode() == 3);
    m_TestSplitBut.setSelected(ExplorerDefaults.getClassifierTestMode() == 4);
    m_PercentText.setText("" + ExplorerDefaults.getClassifierPercentageSplit());
    m_CVText.setText("" + ExplorerDefaults.getClassifierCrossvalidationFolds());
    updateRadioLinks();
    ButtonGroup bg = new ButtonGroup();
    bg.add(m_TrainBut);
    bg.add(m_CVBut);
    bg.add(m_PercentBut);
    bg.add(m_TestSplitBut);
    m_TrainBut.addActionListener(m_RadioListener);
    m_CVBut.addActionListener(m_RadioListener);
    m_PercentBut.addActionListener(m_RadioListener);
    m_TestSplitBut.addActionListener(m_RadioListener);
    m_SetTestBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        setTestSet();
      }
    });
    m_EvalWRTCostsBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        m_SetCostsBut.setEnabled(m_EvalWRTCostsBut.isSelected());
        if ((m_SetCostsFrame != null) && (!m_EvalWRTCostsBut.isSelected())) {
          m_SetCostsFrame.setVisible(false);
        }
      }
    });
    m_CostMatrixEditor.setValue(new CostMatrix(1));
    m_SetCostsBut.setEnabled(m_EvalWRTCostsBut.isSelected());
    m_SetCostsBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        m_SetCostsBut.setEnabled(false);
        if (m_SetCostsFrame == null) {
          if (PropertyDialog.getParentDialog(ClassifierPanel.this) != null) {
            m_SetCostsFrame = new PropertyDialog(PropertyDialog.getParentDialog(ClassifierPanel.this), m_CostMatrixEditor, 100, 100);
          }
          else
          {
            m_SetCostsFrame = new PropertyDialog(PropertyDialog.getParentFrame(ClassifierPanel.this), m_CostMatrixEditor, 100, 100);
          }
          

          Messages.getInstance();m_SetCostsFrame.setTitle(Messages.getString("ClassifierPanel_SetCostsFrame_SetTitle_Text"));
          

          m_SetCostsFrame.addWindowListener(new WindowAdapter()
          {
            public void windowClosing(WindowEvent p) {
              m_SetCostsBut.setEnabled(m_EvalWRTCostsBut.isSelected());
              if ((m_SetCostsFrame != null) && (!m_EvalWRTCostsBut.isSelected()))
              {
                m_SetCostsFrame.setVisible(false);
              }
            }
          });
          m_SetCostsFrame.setVisible(true);
        }
        

        int classIndex = m_ClassCombo.getSelectedIndex();
        int numClasses = m_Instances.attribute(classIndex).numValues();
        if (numClasses != ((CostMatrix)m_CostMatrixEditor.getValue()).numColumns())
        {
          m_CostMatrixEditor.setValue(new CostMatrix(numClasses));
        }
        
        m_SetCostsFrame.setVisible(true);
      }
      
    });
    m_StartBut.setEnabled(false);
    m_StopBut.setEnabled(false);
    m_StartBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        boolean proceed = true;
        if (Explorer.m_Memory.memoryIsLow()) {
          proceed = Explorer.m_Memory.showMemoryIsLow();
        }
        
        if (proceed) {
          startClassifier();
        }
      }
    });
    m_StopBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        stopClassifier();
      }
      
    });
    m_ClassCombo.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        int selected = m_ClassCombo.getSelectedIndex();
        if (selected != -1) {
          boolean isNominal = m_Instances.attribute(selected).isNominal();
          m_OutputPerClassBut.setEnabled(isNominal);
          m_OutputConfusionBut.setEnabled(isNominal);
        }
        updateCapabilitiesFilter(m_ClassifierEditor.getCapabilitiesFilter());
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
            visualize(name, e.getX(), e.getY());
          } else {
            visualize(null, e.getX(), e.getY());
          }
          
        }
      }
    });
    m_MoreOptions.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        m_MoreOptions.setEnabled(false);
        JPanel moreOptionsPanel = new JPanel();
        moreOptionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        moreOptionsPanel.setLayout(new GridLayout(11, 1));
        moreOptionsPanel.add(m_OutputModelBut);
        moreOptionsPanel.add(m_OutputPerClassBut);
        moreOptionsPanel.add(m_OutputEntropyBut);
        moreOptionsPanel.add(m_OutputConfusionBut);
        moreOptionsPanel.add(m_StorePredictionsBut);
        moreOptionsPanel.add(m_OutputPredictionsTextBut);
        JPanel additionalAttsPanel = new JPanel(new FlowLayout(0));
        additionalAttsPanel.add(m_OutputAdditionalAttributesLab);
        additionalAttsPanel.add(m_OutputAdditionalAttributesText);
        moreOptionsPanel.add(additionalAttsPanel);
        JPanel costMatrixOption = new JPanel(new FlowLayout(0));
        costMatrixOption.add(m_EvalWRTCostsBut);
        costMatrixOption.add(m_SetCostsBut);
        moreOptionsPanel.add(costMatrixOption);
        JPanel seedPanel = new JPanel(new FlowLayout(0));
        seedPanel.add(m_RandomLab);
        seedPanel.add(m_RandomSeedText);
        moreOptionsPanel.add(seedPanel);
        moreOptionsPanel.add(m_PreserveOrderBut);
        JPanel sourcePanel = new JPanel(new FlowLayout(0));
        m_OutputSourceCode.setEnabled(m_ClassifierEditor.getValue() instanceof Sourcable);
        m_SourceCodeClass.setEnabled((m_OutputSourceCode.isEnabled()) && (m_OutputSourceCode.isSelected()));
        
        sourcePanel.add(m_OutputSourceCode);
        sourcePanel.add(m_SourceCodeClass);
        moreOptionsPanel.add(sourcePanel);
        
        JPanel all = new JPanel();
        all.setLayout(new BorderLayout());
        
        Messages.getInstance();JButton oK = new JButton(Messages.getString("ClassifierPanel_OK_JButton_Text"));
        
        JPanel okP = new JPanel();
        okP.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        okP.setLayout(new GridLayout(1, 1, 5, 5));
        okP.add(oK);
        
        all.add(moreOptionsPanel, "Center");
        all.add(okP, "South");
        
        Messages.getInstance();final JDialog jd = new JDialog(PropertyDialog.getParentFrame(ClassifierPanel.this), Messages.getString("ClassifierPanel_JD_JDialog_Text"));
        

        jd.getContentPane().setLayout(new BorderLayout());
        jd.getContentPane().add(all, "Center");
        jd.addWindowListener(new WindowAdapter()
        {
          public void windowClosing(WindowEvent w) {
            jd.dispose();
            m_MoreOptions.setEnabled(true);
          }
        });
        oK.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent a) {
            m_MoreOptions.setEnabled(true);
            jd.dispose();
          }
        });
        jd.pack();
        jd.setLocation(m_MoreOptions.getLocationOnScreen());
        jd.setVisible(true);
      }
      

    });
    JPanel p1 = new JPanel();
    Messages.getInstance();p1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("ClassifierPanel_P1_JPanel_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    


    p1.setLayout(new BorderLayout());
    p1.add(m_CEPanel, "North");
    
    JPanel p2 = new JPanel();
    GridBagLayout gbL = new GridBagLayout();
    p2.setLayout(gbL);
    Messages.getInstance();p2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("ClassifierPanel_P2_JPanel_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    


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
    gbL.setConstraints(m_CVBut, gbC);
    p2.add(m_CVBut);
    
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 2;
    gridx = 1;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(m_CVLab, gbC);
    p2.add(m_CVLab);
    
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 2;
    gridx = 2;
    weightx = 100.0D;
    ipadx = 20;
    gbL.setConstraints(m_CVText, gbC);
    p2.add(m_CVText);
    
    gbC = new GridBagConstraints();
    anchor = 17;
    gridy = 3;
    gridx = 0;
    gbL.setConstraints(m_PercentBut, gbC);
    p2.add(m_PercentBut);
    
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 3;
    gridx = 1;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(m_PercentLab, gbC);
    p2.add(m_PercentLab);
    
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 3;
    gridx = 2;
    weightx = 100.0D;
    ipadx = 20;
    gbL.setConstraints(m_PercentText, gbC);
    p2.add(m_PercentText);
    
    gbC = new GridBagConstraints();
    anchor = 17;
    fill = 2;
    gridy = 4;
    gridx = 0;
    weightx = 100.0D;
    gridwidth = 3;
    
    insets = new Insets(3, 0, 1, 0);
    gbL.setConstraints(m_MoreOptions, gbC);
    p2.add(m_MoreOptions);
    
    JPanel buttons = new JPanel();
    buttons.setLayout(new GridLayout(2, 2));
    buttons.add(m_ClassCombo);
    m_ClassCombo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    JPanel ssButs = new JPanel();
    ssButs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    ssButs.setLayout(new GridLayout(1, 2, 5, 5));
    ssButs.add(m_StartBut);
    ssButs.add(m_StopBut);
    
    buttons.add(ssButs);
    
    JPanel p3 = new JPanel();
    Messages.getInstance();p3.setBorder(BorderFactory.createTitledBorder(Messages.getString("ClassifierPanel_P3_JPanel_BorderFactoryCreateTitledBorder_Text")));
    

    p3.setLayout(new BorderLayout());
    JScrollPane js = new JScrollPane(m_OutText);
    p3.add(js, "Center");
    js.getViewport().addChangeListener(new ChangeListener()
    {
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
    m_CVText.setEnabled(m_CVBut.isSelected());
    m_CVLab.setEnabled(m_CVBut.isSelected());
    m_PercentText.setEnabled(m_PercentBut.isSelected());
    m_PercentLab.setEnabled(m_PercentBut.isSelected());
  }
  






  public void setLog(Logger newLog)
  {
    m_Log = newLog;
  }
  





  public void setInstances(Instances inst)
  {
    m_Instances = inst;
    
    String[] attribNames = new String[m_Instances.numAttributes()];
    for (int i = 0; i < attribNames.length; i++) {
      String type = "";
      switch (m_Instances.attribute(i).type()) {
      case 1: 
        Messages.getInstance();type = Messages.getString("ClassifierPanel_SetInstances_Type_AttributeNOMINAL_Text");
        
        break;
      case 0: 
        Messages.getInstance();type = Messages.getString("ClassifierPanel_SetInstances_Type_AttributeNUMERIC_Text");
        
        break;
      case 2: 
        Messages.getInstance();type = Messages.getString("ClassifierPanel_SetInstances_Type_AttributeSTRING_Text");
        
        break;
      case 3: 
        Messages.getInstance();type = Messages.getString("ClassifierPanel_SetInstances_Type_AttributeDATE_Text");
        
        break;
      case 4: 
        Messages.getInstance();type = Messages.getString("ClassifierPanel_SetInstances_Type_AttributeRELATIONAL_Text");
        
        break;
      default: 
        Messages.getInstance();type = Messages.getString("ClassifierPanel_SetInstances_Type_AttributeDEFAULT_Text");
      }
      
      attribNames[i] = (type + m_Instances.attribute(i).name());
    }
    m_ClassCombo.setModel(new DefaultComboBoxModel(attribNames));
    if (attribNames.length > 0) {
      if (inst.classIndex() == -1) {
        m_ClassCombo.setSelectedIndex(attribNames.length - 1);
      } else {
        m_ClassCombo.setSelectedIndex(inst.classIndex());
      }
      m_ClassCombo.setEnabled(true);
      m_StartBut.setEnabled(m_RunThread == null);
      m_StopBut.setEnabled(m_RunThread != null);
    } else {
      m_StartBut.setEnabled(false);
      m_StopBut.setEnabled(false);
    }
  }
  






  protected void setTestSet()
  {
    if (m_SetTestFrame == null) {
      final SetInstancesPanel sp = new SetInstancesPanel(true, m_Explorer.getPreprocessPanel().m_FileChooser);
      
      if (m_TestLoader != null) {
        try {
          if (m_TestLoader.getStructure() != null) {
            sp.setInstances(m_TestLoader.getStructure());
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
      sp.addPropertyChangeListener(new PropertyChangeListener()
      {
        public void propertyChange(PropertyChangeEvent e) {
          m_TestLoader = sp.getLoader();
        }
        

      });
      Messages.getInstance();m_SetTestFrame = new JFrame(Messages.getString("ClassifierPanel_SetTestSet_SetTestFrame_JFrame_Text"));
      
      sp.setParentFrame(m_SetTestFrame);
      m_SetTestFrame.getContentPane().setLayout(new BorderLayout());
      m_SetTestFrame.getContentPane().add(sp, "Center");
      m_SetTestFrame.pack();
    }
    m_SetTestFrame.setVisible(true);
  }
  
















  public static void processClassifierPrediction(Instance toPredict, Classifier classifier, Evaluation eval, Instances plotInstances, FastVector plotShape, FastVector plotSize)
  {
    try
    {
      double pred = eval.evaluateModelOnceAndRecordPrediction(classifier, toPredict);
      

      if (plotInstances != null) {
        double[] values = new double[plotInstances.numAttributes()];
        for (int i = 0; i < plotInstances.numAttributes(); i++) {
          if (i < toPredict.classIndex()) {
            values[i] = toPredict.value(i);
          } else if (i == toPredict.classIndex()) {
            values[i] = pred;
            values[(i + 1)] = toPredict.value(i);
            




            i++;
          } else {
            values[i] = toPredict.value(i - 1);
          }
        }
        
        plotInstances.add(new Instance(1.0D, values));
        if (toPredict.classAttribute().isNominal()) {
          if ((toPredict.isMissing(toPredict.classIndex())) || (Instance.isMissingValue(pred)))
          {
            plotShape.addElement(new Integer(2000));
          } else if (pred != toPredict.classValue())
          {
            plotShape.addElement(new Integer(1000));
          }
          else {
            plotShape.addElement(new Integer(-1));
          }
          plotSize.addElement(new Integer(2));
        }
        else {
          Double errd = null;
          if ((!toPredict.isMissing(toPredict.classIndex())) && (!Instance.isMissingValue(pred)))
          {
            errd = new Double(pred - toPredict.classValue());
            plotShape.addElement(new Integer(-1));
          }
          else
          {
            plotShape.addElement(new Integer(2000));
          }
          plotSize.addElement(errd);
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  





  private void postProcessPlotInfo(FastVector plotSize)
  {
    int maxpSize = 20;
    double maxErr = Double.NEGATIVE_INFINITY;
    double minErr = Double.POSITIVE_INFINITY;
    

    for (int i = 0; i < plotSize.size(); i++) {
      Double errd = (Double)plotSize.elementAt(i);
      if (errd != null) {
        double err = Math.abs(errd.doubleValue());
        if (err < minErr) {
          minErr = err;
        }
        if (err > maxErr) {
          maxErr = err;
        }
      }
    }
    
    for (int i = 0; i < plotSize.size(); i++) {
      Double errd = (Double)plotSize.elementAt(i);
      if (errd != null) {
        double err = Math.abs(errd.doubleValue());
        if (maxErr - minErr > 0.0D) {
          double temp = (err - minErr) / (maxErr - minErr) * maxpSize;
          plotSize.setElementAt(new Integer((int)temp), i);
        } else {
          plotSize.setElementAt(new Integer(1), i);
        }
      } else {
        plotSize.setElementAt(new Integer(1), i);
      }
    }
  }
  








  public static Instances setUpVisualizableInstances(Instances trainInstances)
  {
    FastVector hv = new FastVector();
    

    Attribute classAt = trainInstances.attribute(trainInstances.classIndex());
    Attribute predictedClass; Attribute predictedClass; if (classAt.isNominal()) {
      FastVector attVals = new FastVector();
      for (int i = 0; i < classAt.numValues(); i++) {
        attVals.addElement(classAt.value(i));
      }
      Messages.getInstance();predictedClass = new Attribute(Messages.getString("ClassifierPanel_SetUpVisualizableInstances_PredictedClass_Attribute_Text_First") + classAt.name(), attVals);


    }
    else
    {

      Messages.getInstance();predictedClass = new Attribute(Messages.getString("ClassifierPanel_SetUpVisualizableInstances_PredictedClass_Attribute_Text_Second") + classAt.name());
    }
    





    for (int i = 0; i < trainInstances.numAttributes(); i++) {
      if (i == trainInstances.classIndex()) {
        hv.addElement(predictedClass);
      }
      hv.addElement(trainInstances.attribute(i).copy());
    }
    return new Instances(trainInstances.relationName() + "_predicted", hv, trainInstances.numInstances());
  }
  








  protected void printPredictionsHeader(StringBuffer outBuff, Instances inst, String title)
  {
    Messages.getInstance();Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_PrintPredictionsHeader_OutBuffer_Text_First") + title + " " + Messages.getString("ClassifierPanel_PrintPredictionsHeader_OutBuffer_Text_First_Alpha"));
    




    Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_PrintPredictionsHeader_OutBuffer_Text_Second"));
    
    if (inst.classAttribute().isNominal()) {
      Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_PrintPredictionsHeader_OutBuffer_Text_Third"));
    }
    
    if (m_OutputAdditionalAttributesRange != null) {
      outBuff.append(" (");
      boolean first = true;
      for (int i = 0; i < inst.numAttributes() - 1; i++) {
        if (m_OutputAdditionalAttributesRange.isInRange(i)) {
          if (!first) {
            outBuff.append(",");
          } else {
            first = false;
          }
          outBuff.append(inst.attribute(i).name());
        }
      }
      outBuff.append(")");
    }
    outBuff.append("\n");
  }
  






  protected void startClassifier()
  {
    if (m_RunThread == null) {
      synchronized (this) {
        m_StartBut.setEnabled(false);
        m_StopBut.setEnabled(true);
      }
      m_RunThread = new Thread()
      {
        public void run()
        {
          Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_First"));
          
          CostMatrix costMatrix = null;
          Instances inst = new Instances(m_Instances);
          ConverterUtils.DataSource source = null;
          Instances userTestStructure = null;
          
          FastVector plotShape = new FastVector();
          FastVector plotSize = new FastVector();
          Instances predInstances = null;
          

          long trainTimeStart = 0L;long trainTimeElapsed = 0L;
          try
          {
            if ((m_TestLoader != null) && (m_TestLoader.getStructure() != null)) {
              m_TestLoader.reset();
              source = new ConverterUtils.DataSource(m_TestLoader);
              userTestStructure = source.getStructure();
            }
          } catch (Exception ex) {
            ex.printStackTrace();
          }
          if (m_EvalWRTCostsBut.isSelected()) {
            costMatrix = new CostMatrix((CostMatrix)m_CostMatrixEditor.getValue());
          }
          
          boolean outputModel = m_OutputModelBut.isSelected();
          boolean outputConfusion = m_OutputConfusionBut.isSelected();
          boolean outputPerClass = m_OutputPerClassBut.isSelected();
          boolean outputSummary = true;
          boolean outputEntropy = m_OutputEntropyBut.isSelected();
          boolean saveVis = m_StorePredictionsBut.isSelected();
          boolean outputPredictionsText = m_OutputPredictionsTextBut.isSelected();
          
          if (m_OutputAdditionalAttributesText.getText().equals("")) {
            m_OutputAdditionalAttributesRange = null;
          } else {
            try {
              m_OutputAdditionalAttributesRange = new Range(m_OutputAdditionalAttributesText.getText());
              
              m_OutputAdditionalAttributesRange.setUpper(inst.numAttributes() - 1);
            }
            catch (IllegalArgumentException e) {
              Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(ClassifierPanel.this, Messages.getString("ClassifierPanel_OutputAdditionalAttributes_First") + "\n\n" + m_OutputAdditionalAttributesText.getText() + "\n\n" + Messages.getString("ClassifierPanel_OutputAdditionalAttributes_Second"), Messages.getString("ClassifierPanel_OutputAdditionalAttributes_Title"), 0);
              










              m_OutputAdditionalAttributesRange = null;
            }
          }
          
          String grph = null;
          
          int testMode = 0;
          int numFolds = 10;
          double percent = 66.0D;
          int classIndex = m_ClassCombo.getSelectedIndex();
          Classifier classifier = (Classifier)m_ClassifierEditor.getValue();
          Classifier template = null;
          try {
            template = Classifier.makeCopy(classifier);
          } catch (Exception ex) {
            Messages.getInstance();m_Log.logMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_LogMessage_Text_First") + ex.getMessage());
          }
          

          Classifier fullClassifier = null;
          StringBuffer outBuff = new StringBuffer();
          String name = new SimpleDateFormat("HH:mm:ss - ").format(new Date());
          
          String cname = classifier.getClass().getName();
          if (cname.startsWith("weka.classifiers.")) {
            name = name + cname.substring("weka.classifiers.".length());
          } else {
            name = name + cname;
          }
          String cmd = m_ClassifierEditor.getValue().getClass().getName();
          if ((m_ClassifierEditor.getValue() instanceof OptionHandler)) {
            cmd = cmd + " " + Utils.joinOptions(((OptionHandler)m_ClassifierEditor.getValue()).getOptions());
          }
          

          Evaluation eval = null;
          try {
            if (m_CVBut.isSelected()) {
              testMode = 1;
              numFolds = Integer.parseInt(m_CVText.getText());
              if (numFolds <= 1) {
                Messages.getInstance();throw new Exception(Messages.getString("ClassifierPanel_StartClassifier_Exception_Text_First"));
              }
            }
            else if (m_PercentBut.isSelected()) {
              testMode = 2;
              percent = Double.parseDouble(m_PercentText.getText());
              if ((percent <= 0.0D) || (percent >= 100.0D)) {
                Messages.getInstance();throw new Exception(Messages.getString("ClassifierPanel_StartClassifier_Exception_Text_Second"));
              }
            }
            else if (m_TrainBut.isSelected()) {
              testMode = 3;
            } else if (m_TestSplitBut.isSelected()) {
              testMode = 4;
              
              if (source == null) {
                Messages.getInstance();throw new Exception(Messages.getString("ClassifierPanel_StartClassifier_Exception_Text_Third"));
              }
              
              if (!inst.equalHeaders(userTestStructure)) {
                Messages.getInstance();throw new Exception(Messages.getString("ClassifierPanel_StartClassifier_Exception_Text_Fourth"));
              }
              
              userTestStructure.setClassIndex(classIndex);
            } else {
              Messages.getInstance();throw new Exception(Messages.getString("ClassifierPanel_StartClassifier_Exception_Text_Fifth"));
            }
            
            inst.setClassIndex(classIndex);
            


            if (saveVis) {
              predInstances = ClassifierPanel.setUpVisualizableInstances(inst);
              predInstances.setClassIndex(inst.classIndex() + 1);
            }
            

            Messages.getInstance();m_Log.logMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_LogMessage_Text_Second") + cname);
            

            Messages.getInstance();m_Log.logMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_LogMessage_Text_Third") + cmd);
            

            if ((m_Log instanceof TaskLogger)) {
              ((TaskLogger)m_Log).taskStarted();
            }
            Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_First"));
            
            Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_Second") + cname);
            

            if ((classifier instanceof OptionHandler)) {
              String[] o = classifier.getOptions();
              outBuff.append(" " + Utils.joinOptions(o));
            }
            outBuff.append("\n");
            Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_Fourth") + inst.relationName() + '\n');
            

            Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_Sixth") + inst.numInstances() + '\n');
            

            Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_Eigth") + inst.numAttributes() + '\n');
            

            if (inst.numAttributes() < 100) {
              for (int i = 0; i < inst.numAttributes(); i++) {
                outBuff.append("              " + inst.attribute(i).name() + '\n');
              }
            }
            else {
              Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_Twelveth"));
            }
            

            Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_Thirteenth"));
            
            switch (testMode) {
            case 3: 
              Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_Fourteenth"));
              
              break;
            case 1: 
              Messages.getInstance();outBuff.append("" + numFolds + Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_Sixteenth"));
              


              break;
            case 2: 
              Messages.getInstance();Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_Seventeenth") + percent + Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_Eighteenth"));
              



              break;
            case 4: 
              if (source.isIncremental()) {
                Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_Nineteenth"));
              }
              else {
                Messages.getInstance();Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_Twentyth") + source.getDataSet().numInstances() + Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_TwentyFirst"));
              }
              


              break;
            }
            
            


            if (costMatrix != null) {
              Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_TwentySecond")).append(costMatrix.toString()).append("\n");
            }
            





            outBuff.append("\n");
            m_History.addResult(name, outBuff);
            m_History.setSingle(name);
            

            if ((outputModel) || (testMode == 3) || (testMode == 4)) {
              Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Second"));
              




              trainTimeStart = System.currentTimeMillis();
              classifier.buildClassifier(inst);
              trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
            }
            
            if (outputModel) {
              Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_TwentySixth"));
              
              outBuff.append(classifier.toString() + "\n");
              Messages.getInstance();Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_TwentyEighth") + Utils.doubleToString(trainTimeElapsed / 1000.0D, 2) + " " + Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_TwentyNineth"));
              









              m_History.updateResult(name);
              if ((classifier instanceof Drawable)) {
                grph = null;
                try {
                  grph = ((Drawable)classifier).graph();
                }
                catch (Exception ex) {}
              }
              
              SerializedObject so = new SerializedObject(classifier);
              fullClassifier = (Classifier)so.getObject();
            }
            int rnd;
            switch (testMode) {
            case 3: 
              m_Log.statusMessage("Evaluating on training data...");
              eval = new Evaluation(inst, costMatrix);
              
              if (outputPredictionsText) {
                printPredictionsHeader(outBuff, inst, "training set");
              }
              
              for (int jj = 0; jj < inst.numInstances(); jj++) {
                ClassifierPanel.processClassifierPrediction(inst.instance(jj), classifier, eval, predInstances, plotShape, plotSize);
                

                if (outputPredictionsText) {
                  outBuff.append(predictionText(classifier, inst.instance(jj), jj + 1));
                }
                
                if (jj % 100 == 0) {
                  m_Log.statusMessage("Evaluating on training data. Processed " + jj + " instances...");
                }
              }
              
              if (outputPredictionsText) {
                outBuff.append("\n");
              }
              outBuff.append("=== Evaluation on training set ===\n");
              break;
            
            case 1: 
              m_Log.statusMessage("Randomizing instances...");
              rnd = 1;
              try {
                rnd = Integer.parseInt(m_RandomSeedText.getText().trim());
              }
              catch (Exception ex) {
                m_Log.logMessage("Trouble parsing random seed value");
                rnd = 1;
              }
              Random random = new Random(rnd);
              inst.randomize(random);
              if (inst.attribute(classIndex).isNominal()) {
                m_Log.statusMessage("Stratifying instances...");
                inst.stratify(numFolds);
              }
              eval = new Evaluation(inst, costMatrix);
              
              if (outputPredictionsText) {
                printPredictionsHeader(outBuff, inst, "test data");
              }
              

              for (int fold = 0; fold < numFolds; fold++) {
                Messages.getInstance();Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Eighth") + (fold + 1) + Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Nineth"));
                








                Instances train = inst.trainCV(numFolds, fold, random);
                eval.setPriors(train);
                Messages.getInstance();Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Tenth") + (fold + 1) + Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Twelveth"));
                








                Classifier current = null;
                try {
                  current = Classifier.makeCopy(template);
                } catch (Exception ex) {
                  Messages.getInstance();m_Log.logMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_LogMessage_Text_Fifth") + ex.getMessage());
                }
                




                current.buildClassifier(train);
                Instances test = inst.testCV(numFolds, fold);
                Messages.getInstance();Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Eleventh") + (fold + 1) + Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Twelveth"));
                








                for (int jj = 0; jj < test.numInstances(); jj++) {
                  ClassifierPanel.processClassifierPrediction(test.instance(jj), current, eval, predInstances, plotShape, plotSize);
                  
                  if (outputPredictionsText) {
                    outBuff.append(predictionText(current, test.instance(jj), jj + 1));
                  }
                }
              }
              
              if (outputPredictionsText) {
                outBuff.append("\n");
              }
              if (inst.attribute(classIndex).isNominal()) {
                Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_ThirtyThird"));

              }
              else
              {

                Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_ThirtyFourth"));
              }
              



              break;
            
            case 2: 
              if (!m_PreserveOrderBut.isSelected()) {
                Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Thirteenth"));
                


                try
                {
                  rnd = Integer.parseInt(m_RandomSeedText.getText().trim());
                } catch (Exception ex) {
                  Messages.getInstance();m_Log.logMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Fourteenth"));
                  



                  rnd = 1;
                }
                inst.randomize(new Random(rnd));
              }
              int trainSize = (int)Math.round(inst.numInstances() * percent / 100.0D);
              
              int testSize = inst.numInstances() - trainSize;
              Instances train = new Instances(inst, 0, trainSize);
              Instances test = new Instances(inst, trainSize, testSize);
              Messages.getInstance();Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Fifteenth") + trainSize + Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Sixteenth"));
              








              Classifier current = null;
              try {
                current = Classifier.makeCopy(template);
              } catch (Exception ex) {
                Messages.getInstance();m_Log.logMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_LogMessage_Text_Sixth") + ex.getMessage());
              }
              

              current.buildClassifier(train);
              eval = new Evaluation(train, costMatrix);
              Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Seventeenth"));
              




              if (outputPredictionsText) {
                Messages.getInstance();printPredictionsHeader(outBuff, inst, Messages.getString("ClassifierPanel_StartClassifier_PrintPredictionsHeader_Text_First"));
              }
              






              for (int jj = 0; jj < test.numInstances(); jj++) {
                ClassifierPanel.processClassifierPrediction(test.instance(jj), current, eval, predInstances, plotShape, plotSize);
                
                if (outputPredictionsText) {
                  outBuff.append(predictionText(current, test.instance(jj), jj + 1));
                }
                
                if (jj % 100 == 0) {
                  Messages.getInstance();Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Eighteenth") + jj + Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Nineteenth"));
                }
              }
              








              if (outputPredictionsText) {
                outBuff.append("\n");
              }
              Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_ThirtySixth"));
              
              break;
            
            case 4: 
              Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_Twentyth"));
              



              eval = new Evaluation(inst, costMatrix);
              
              if (outputPredictionsText) {
                Messages.getInstance();printPredictionsHeader(outBuff, inst, Messages.getString("ClassifierPanel_StartClassifier_PrintPredictionsHeader_Text_Second"));
              }
              







              int jj = 0;
              while (source.hasMoreElements(userTestStructure)) {
                Instance instance = source.nextElement(userTestStructure);
                ClassifierPanel.processClassifierPrediction(instance, classifier, eval, predInstances, plotShape, plotSize);
                
                if (outputPredictionsText) {
                  outBuff.append(predictionText(classifier, instance, jj + 1));
                }
                jj++; if (jj % 100 == 0) {
                  Messages.getInstance();Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_TwentyFirst") + jj + Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_TwentySecond"));
                }
              }
              









              if (outputPredictionsText) {
                outBuff.append("\n");
              }
              Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_ThirtyEighth"));
              
              break;
            
            default: 
              Messages.getInstance();throw new Exception(Messages.getString("ClassifierPanel_StartClassifier_Exception_Text"));
            }
            
            
            if (outputSummary) {
              outBuff.append(eval.toSummaryString(outputEntropy) + "\n");
            }
            
            if (inst.attribute(classIndex).isNominal())
            {
              if (outputPerClass) {
                outBuff.append(eval.toClassDetailsString() + "\n");
              }
              
              if (outputConfusion) {
                outBuff.append(eval.toMatrixString() + "\n");
              }
            }
            
            if (((fullClassifier instanceof Sourcable)) && (m_OutputSourceCode.isSelected()))
            {
              Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_StartClassifier_OutBuffer_Text_FourtySecond"));
              
              outBuff.append(Evaluation.wekaStaticWrapper((Sourcable)fullClassifier, m_SourceCodeClass.getText()));
            }
            

            m_History.updateResult(name);
            Messages.getInstance();m_Log.logMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_LogMessage_Text_Seventh") + cname);
            

            Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_TwentyThird"));

          }
          catch (Exception ex)
          {

            ex.printStackTrace();
            m_Log.logMessage(ex.getMessage());
            Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(ClassifierPanel.this, Messages.getString("ClassifierPanel_StartClassifier_JOptionPaneShowMessageDialog_Text_First") + ex.getMessage(), Messages.getString("ClassifierPanel_StartClassifier_JOptionPaneShowMessageDialog_Text_Second"), 0);
            











            Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_TwentyFourth"));

          }
          finally
          {
            try
            {
              if ((!saveVis) && (outputModel)) {
                FastVector vv = new FastVector();
                vv.addElement(fullClassifier);
                Instances trainHeader = new Instances(m_Instances, 0);
                trainHeader.setClassIndex(classIndex);
                vv.addElement(trainHeader);
                if (grph != null) {
                  vv.addElement(grph);
                }
                m_History.addObject(name, vv);
              } else if ((saveVis) && (predInstances != null) && (predInstances.numInstances() > 0))
              {
                if (predInstances.attribute(predInstances.classIndex()).isNumeric())
                {
                  ClassifierPanel.this.postProcessPlotInfo(plotSize);
                }
                m_CurrentVis = new VisualizePanel();
                m_CurrentVis.setName(name + " (" + inst.relationName() + ")");
                m_CurrentVis.setLog(m_Log);
                PlotData2D tempd = new PlotData2D(predInstances);
                tempd.setShapeSize(plotSize);
                tempd.setShapeType(plotShape);
                tempd.setPlotName(name + " (" + inst.relationName() + ")");
                

                m_CurrentVis.addPlot(tempd);
                
                m_CurrentVis.setColourIndex(predInstances.classIndex());
                
                FastVector vv = new FastVector();
                if (outputModel) {
                  vv.addElement(fullClassifier);
                  Instances trainHeader = new Instances(m_Instances, 0);
                  trainHeader.setClassIndex(classIndex);
                  vv.addElement(trainHeader);
                  if (grph != null) {
                    vv.addElement(grph);
                  }
                }
                vv.addElement(m_CurrentVis);
                
                if ((eval != null) && (eval.predictions() != null)) {
                  vv.addElement(eval.predictions());
                  vv.addElement(inst.classAttribute());
                }
                m_History.addObject(name, vv);
              }
            } catch (Exception ex) {
              ex.printStackTrace();
            }
            
            if (isInterrupted()) {
              Messages.getInstance();m_Log.logMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_LogMessage_Text_Eighth") + cname);
              

              Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_StartClassifier_Log_StatusMessage_Text_TwentyFourth"));
            }
            




            synchronized (this) {
              m_StartBut.setEnabled(true);
              m_StopBut.setEnabled(false);
              m_RunThread = null;
            }
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
  











  protected String predictionText(Classifier classifier, Instance inst, int instNum)
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    
    text.append(Utils.padLeft(new StringBuilder().append("").append(instNum).toString(), 6) + " ");
    if (inst.classAttribute().isNominal())
    {

      if (inst.classIsMissing()) {
        text.append(Utils.padLeft("?", 10) + " ");
      } else {
        text.append(Utils.padLeft(new StringBuilder().append("").append((int)inst.classValue() + 1).append(":").append(inst.stringValue(inst.classAttribute())).toString(), 10) + " ");
      }
      



      double[] probdist = null;
      double pred;
      if (inst.classAttribute().isNominal()) {
        probdist = classifier.distributionForInstance(inst);
        double pred = Utils.maxIndex(probdist);
        if (probdist[((int)pred)] <= 0.0D) {
          pred = Instance.missingValue();
        }
      } else {
        pred = classifier.classifyInstance(inst);
      }
      text.append(Utils.padLeft(Instance.isMissingValue(pred) ? "?" : new StringBuilder().append((int)pred + 1).append(":").append(inst.classAttribute().value((int)pred)).toString(), 10) + " ");
      



      if (pred == inst.classValue()) {
        text.append(Utils.padLeft(" ", 6) + " ");
      } else {
        text.append(Utils.padLeft("+", 6) + " ");
      }
      

      if (inst.classAttribute().type() == 1) {
        for (int i = 0; i < probdist.length; i++) {
          if (i == (int)pred) {
            text.append(" *");
          } else {
            text.append("  ");
          }
          text.append(Utils.doubleToString(probdist[i], 5, 3));
        }
      }
    }
    else
    {
      if (inst.classIsMissing()) {
        text.append(Utils.padLeft("?", 10) + " ");
      } else {
        text.append(Utils.doubleToString(inst.classValue(), 10, 3) + " ");
      }
      

      double pred = classifier.classifyInstance(inst);
      if (Instance.isMissingValue(pred)) {
        text.append(Utils.padLeft("?", 10) + " ");
      } else {
        text.append(Utils.doubleToString(pred, 10, 3) + " ");
      }
      

      if ((!inst.classIsMissing()) && (!Instance.isMissingValue(pred))) {
        text.append(Utils.doubleToString(pred - inst.classValue(), 10, 3));
      }
    }
    

    if (m_OutputAdditionalAttributesRange != null) {
      text.append(" (");
      boolean first = true;
      for (int i = 0; i < inst.numAttributes() - 1; i++) {
        if (m_OutputAdditionalAttributesRange.isInRange(i)) {
          if (!first) {
            text.append(",");
          } else {
            first = false;
          }
          text.append(inst.toString(i));
        }
      }
      text.append(")");
    }
    
    text.append("\n");
    return text.toString();
  }
  







  protected void visualize(String name, int x, int y)
  {
    final String selectedName = name;
    JPopupMenu resultListMenu = new JPopupMenu();
    
    Messages.getInstance();JMenuItem visMainBuffer = new JMenuItem(Messages.getString("ClassifierPanel_Visualize_VisMainBuffer_JMenuItem_Text"));
    
    if (selectedName != null) {
      visMainBuffer.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          m_History.setSingle(selectedName);
        }
      });
    } else {
      visMainBuffer.setEnabled(false);
    }
    resultListMenu.add(visMainBuffer);
    
    Messages.getInstance();JMenuItem visSepBuffer = new JMenuItem(Messages.getString("ClassifierPanel_Visualize_VisSepBuffer_JMenuItem_Text"));
    
    if (selectedName != null) {
      visSepBuffer.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          m_History.openFrame(selectedName);
        }
      });
    } else {
      visSepBuffer.setEnabled(false);
    }
    resultListMenu.add(visSepBuffer);
    
    Messages.getInstance();JMenuItem saveOutput = new JMenuItem(Messages.getString("ClassifierPanel_Visualize_SaveOutput_JMenuItem_Text"));
    
    if (selectedName != null) {
      saveOutput.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          saveBuffer(selectedName);
        }
      });
    } else {
      saveOutput.setEnabled(false);
    }
    resultListMenu.add(saveOutput);
    
    Messages.getInstance();JMenuItem deleteOutput = new JMenuItem(Messages.getString("ClassifierPanel_Visualize_DeleteOutput_JMenuItem_Text"));
    
    if (selectedName != null) {
      deleteOutput.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          m_History.removeResult(selectedName);
        }
      });
    } else {
      deleteOutput.setEnabled(false);
    }
    resultListMenu.add(deleteOutput);
    
    resultListMenu.addSeparator();
    
    Messages.getInstance();JMenuItem loadModel = new JMenuItem(Messages.getString("ClassifierPanel_Visualize_LoadModel_JMenuItem_Text"));
    
    loadModel.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        loadClassifier();
      }
    });
    resultListMenu.add(loadModel);
    
    FastVector o = null;
    if (selectedName != null) {
      o = (FastVector)m_History.getNamedObject(selectedName);
    }
    
    VisualizePanel temp_vp = null;
    String temp_grph = null;
    FastVector temp_preds = null;
    Attribute temp_classAtt = null;
    Classifier temp_classifier = null;
    Instances temp_trainHeader = null;
    
    if (o != null) {
      for (int i = 0; i < o.size(); i++) {
        Object temp = o.elementAt(i);
        if ((temp instanceof Classifier)) {
          temp_classifier = (Classifier)temp;
        } else if ((temp instanceof Instances)) {
          temp_trainHeader = (Instances)temp;
        } else if ((temp instanceof VisualizePanel)) {
          temp_vp = (VisualizePanel)temp;
        } else if ((temp instanceof String)) {
          temp_grph = (String)temp;
        } else if ((temp instanceof FastVector)) {
          temp_preds = (FastVector)temp;
        } else if ((temp instanceof Attribute)) {
          temp_classAtt = (Attribute)temp;
        }
      }
    }
    
    final VisualizePanel vp = temp_vp;
    final String grph = temp_grph;
    final FastVector preds = temp_preds;
    final Attribute classAtt = temp_classAtt;
    final Classifier classifier = temp_classifier;
    final Instances trainHeader = temp_trainHeader;
    
    Messages.getInstance();JMenuItem saveModel = new JMenuItem(Messages.getString("ClassifierPanel_Visualize_SaveModel_JMenuItem_Text"));
    
    if (classifier != null) {
      saveModel.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          saveClassifier(selectedName, classifier, trainHeader);
        }
      });
    } else {
      saveModel.setEnabled(false);
    }
    resultListMenu.add(saveModel);
    
    Messages.getInstance();JMenuItem reEvaluate = new JMenuItem(Messages.getString("ClassifierPanel_Visualize_ReEvaluate_JMenuItem_Text"));
    
    if ((classifier != null) && (m_TestLoader != null)) {
      reEvaluate.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          reevaluateModel(selectedName, classifier, trainHeader);
        }
      });
    } else {
      reEvaluate.setEnabled(false);
    }
    resultListMenu.add(reEvaluate);
    
    resultListMenu.addSeparator();
    
    Messages.getInstance();JMenuItem visErrors = new JMenuItem(Messages.getString("ClassifierPanel_Visualize_VisErrors_JMenuItem_Text"));
    
    if (vp != null) {
      if ((vp.getXIndex() == 0) && (vp.getYIndex() == 1)) {
        try {
          vp.setXIndex(vp.getInstances().classIndex());
          vp.setYIndex(vp.getInstances().classIndex() - 1);
        }
        catch (Exception e) {}
      }
      
      visErrors.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          visualizeClassifierErrors(vp);
        }
      });
    } else {
      visErrors.setEnabled(false);
    }
    resultListMenu.add(visErrors);
    
    Messages.getInstance();JMenuItem visGrph = new JMenuItem(Messages.getString("ClassifierPanel_Visualize_VisGrph_JMenuItem_Text_First"));
    
    if (grph != null) {
      if (((Drawable)temp_classifier).graphType() == 1) {
        visGrph.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            String title;
            String title;
            if (vp != null) {
              title = vp.getName();
            } else {
              title = selectedName;
            }
            visualizeTree(grph, title);
          }
        });
      } else if (((Drawable)temp_classifier).graphType() == 2) {
        Messages.getInstance();visGrph.setText(Messages.getString("ClassifierPanel_Visualize_VisGrph_JMenuItem_Text_Second"));
        
        visGrph.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e) {
            Thread th = new Thread()
            {
              public void run() {
                visualizeBayesNet(val$grph, val$selectedName);
              }
            };
            th.start();
          }
        });
      } else {
        visGrph.setEnabled(false);
      }
    } else {
      visGrph.setEnabled(false);
    }
    resultListMenu.add(visGrph);
    
    Messages.getInstance();JMenuItem visMargin = new JMenuItem(Messages.getString("ClassifierPanel_Visualize_VisMargin_JMenuItem_Text"));
    
    if (preds != null) {
      visMargin.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          try {
            MarginCurve tc = new MarginCurve();
            Instances result = tc.getCurve(preds);
            VisualizePanel vmc = new VisualizePanel();
            vmc.setName(result.relationName());
            vmc.setLog(m_Log);
            PlotData2D tempd = new PlotData2D(result);
            tempd.setPlotName(result.relationName());
            tempd.addInstanceNumberAttribute();
            vmc.addPlot(tempd);
            visualizeClassifierErrors(vmc);
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      });
    } else {
      visMargin.setEnabled(false);
    }
    resultListMenu.add(visMargin);
    
    Messages.getInstance();JMenu visThreshold = new JMenu(Messages.getString("ClassifierPanel_Visualize_VisThreshold_JMenu_Text"));
    
    if ((preds != null) && (classAtt != null)) {
      for (int i = 0; i < classAtt.numValues(); i++) {
        JMenuItem clv = new JMenuItem(classAtt.value(i));
        final int classValue = i;
        clv.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e) {
            try {
              ThresholdCurve tc = new ThresholdCurve();
              Instances result = tc.getCurve(preds, classValue);
              
              ThresholdVisualizePanel vmc = new ThresholdVisualizePanel();
              Messages.getInstance();Messages.getInstance();vmc.setROCString(Messages.getString("ClassifierPanel_Visualize_VMC_SetROCString_Text_First") + Utils.doubleToString(ThresholdCurve.getROCArea(result), 4) + Messages.getString("ClassifierPanel_Visualize_VMC_SetROCString_Text_Second"));
              



              vmc.setLog(m_Log);
              Messages.getInstance();Messages.getInstance();vmc.setName(result.relationName() + Messages.getString("ClassifierPanel_Visualize_VMC_SetName_Text_First") + classAtt.value(classValue) + Messages.getString("ClassifierPanel_Visualize_VMC_SetName_Text_Second"));
              




              PlotData2D tempd = new PlotData2D(result);
              tempd.setPlotName(result.relationName());
              tempd.addInstanceNumberAttribute();
              
              boolean[] cp = new boolean[result.numInstances()];
              for (int n = 1; n < cp.length; n++) {
                cp[n] = true;
              }
              tempd.setConnectPoints(cp);
              
              vmc.addPlot(tempd);
              visualizeClassifierErrors(vmc);
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        });
        visThreshold.add(clv);
      }
    } else {
      visThreshold.setEnabled(false);
    }
    resultListMenu.add(visThreshold);
    
    Messages.getInstance();JMenu visCostBenefit = new JMenu(Messages.getString("ClassifierPanel_Visualize_VisCostBenefit_JMenu_Text"));
    
    if ((preds != null) && (classAtt != null) && (classAtt.isNominal())) {
      for (int i = 0; i < classAtt.numValues(); i++) {
        JMenuItem clv = new JMenuItem(classAtt.value(i));
        final int classValue = i;
        clv.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e) {
            try {
              ThresholdCurve tc = new ThresholdCurve();
              Instances result = tc.getCurve(preds, classValue);
              


              Attribute classAttToUse = classAtt;
              if (classValue != 0) {
                FastVector newNames = new FastVector();
                newNames.addElement(classAtt.value(classValue));
                for (int k = 0; k < classAtt.numValues(); k++) {
                  if (k != classValue) {
                    newNames.addElement(classAtt.value(k));
                  }
                }
                classAttToUse = new Attribute(classAtt.name(), newNames);
              }
              
              CostBenefitAnalysis cbAnalysis = new CostBenefitAnalysis();
              
              PlotData2D tempd = new PlotData2D(result);
              tempd.setPlotName(result.relationName());
              m_alwaysDisplayPointsOfThisSize = 10;
              
              boolean[] cp = new boolean[result.numInstances()];
              for (int n = 1; n < cp.length; n++) {
                cp[n] = true;
              }
              tempd.setConnectPoints(cp);
              
              String windowTitle = "";
              if (classifier != null) {
                String cname = classifier.getClass().getName();
                if (cname.startsWith("weka.classifiers.")) {
                  windowTitle = "" + cname.substring("weka.classifiers.".length()) + " ";
                }
              }
              
              Messages.getInstance();Messages.getInstance();windowTitle = windowTitle + Messages.getString("ClassifierPanel_Visualize_WindowTitle_Text_First") + classAttToUse.value(0) + Messages.getString("ClassifierPanel_Visualize_WindowTitle_Text_Second");
              





              cbAnalysis.setCurveData(tempd, classAttToUse);
              visualizeCostBenefitAnalysis(cbAnalysis, windowTitle);
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        });
        visCostBenefit.add(clv);
      }
    } else {
      visCostBenefit.setEnabled(false);
    }
    resultListMenu.add(visCostBenefit);
    
    Messages.getInstance();JMenu visCost = new JMenu(Messages.getString("ClassifierPanel_VisCost_JMenu_Text"));
    
    if ((preds != null) && (classAtt != null)) {
      for (int i = 0; i < classAtt.numValues(); i++) {
        JMenuItem clv = new JMenuItem(classAtt.value(i));
        final int classValue = i;
        clv.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e) {
            try {
              CostCurve cc = new CostCurve();
              Instances result = cc.getCurve(preds, classValue);
              VisualizePanel vmc = new VisualizePanel();
              vmc.setLog(m_Log);
              Messages.getInstance();Messages.getInstance();vmc.setName(result.relationName() + Messages.getString("ClassifierPanel_Visualize_VMC_SetName_Text_Third") + classAtt.value(classValue) + Messages.getString("ClassifierPanel_Visualize_VMC_SetName_Text_Fourth"));
              




              PlotData2D tempd = new PlotData2D(result);
              m_displayAllPoints = true;
              tempd.setPlotName(result.relationName());
              boolean[] connectPoints = new boolean[result.numInstances()];
              for (int jj = 1; jj < connectPoints.length; jj += 2) {
                connectPoints[jj] = true;
              }
              tempd.setConnectPoints(connectPoints);
              
              vmc.addPlot(tempd);
              visualizeClassifierErrors(vmc);
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        });
        visCost.add(clv);
      }
    } else {
      visCost.setEnabled(false);
    }
    resultListMenu.add(visCost);
    
    Messages.getInstance();JMenu visPlugins = new JMenu(Messages.getString("ClassifierPanel_Visualize_VisPlugins_JMenu_Text"));
    
    Vector pluginsVector = GenericObjectEditor.getClassnames(VisualizePlugin.class.getName());
    
    boolean availablePlugins = false;
    
    for (int i = 0; i < pluginsVector.size(); i++) {
      String className = (String)pluginsVector.elementAt(i);
      try {
        VisualizePlugin plugin = (VisualizePlugin)Class.forName(className).newInstance();
        
        if (plugin != null)
        {

          availablePlugins = true;
          JMenuItem pluginMenuItem = plugin.getVisualizeMenuItem(preds, classAtt);
          Version version = new Version();
          if (pluginMenuItem != null)
          {









            visPlugins.add(pluginMenuItem);
          }
        }
      }
      catch (ClassNotFoundException cnfe) {}catch (InstantiationException ie) {}catch (IllegalAccessException iae) {}
    }
    








    pluginsVector = GenericObjectEditor.getClassnames(ErrorVisualizePlugin.class.getName());
    
    for (int i = 0; i < pluginsVector.size(); i++) {
      String className = (String)pluginsVector.elementAt(i);
      try {
        ErrorVisualizePlugin plugin = (ErrorVisualizePlugin)Class.forName(className).newInstance();
        
        if (plugin != null)
        {

          availablePlugins = true;
          JMenuItem pluginMenuItem = plugin.getVisualizeMenuItem(vp.getInstances());
          
          Version version = new Version();
          if (pluginMenuItem != null)
          {






            visPlugins.add(pluginMenuItem);
          }
        }
      }
      catch (Exception e) {}
    }
    

    if (grph != null)
    {
      if (((Drawable)temp_classifier).graphType() == 1) {
        pluginsVector = GenericObjectEditor.getClassnames(TreeVisualizePlugin.class.getName());
        
        for (int i = 0; i < pluginsVector.size(); i++) {
          String className = (String)pluginsVector.elementAt(i);
          try {
            TreeVisualizePlugin plugin = (TreeVisualizePlugin)Class.forName(className).newInstance();
            
            if (plugin != null)
            {

              availablePlugins = true;
              JMenuItem pluginMenuItem = plugin.getVisualizeMenuItem(grph, selectedName);
              
              Version version = new Version();
              if (pluginMenuItem != null)
              {







                visPlugins.add(pluginMenuItem);
              }
            }
          }
          catch (Exception e) {}
        }
      }
      else
      {
        pluginsVector = GenericObjectEditor.getClassnames(GraphVisualizePlugin.class.getName());
        
        for (int i = 0; i < pluginsVector.size(); i++) {
          String className = (String)pluginsVector.elementAt(i);
          try {
            GraphVisualizePlugin plugin = (GraphVisualizePlugin)Class.forName(className).newInstance();
            
            if (plugin != null)
            {

              availablePlugins = true;
              JMenuItem pluginMenuItem = plugin.getVisualizeMenuItem(grph, selectedName);
              
              Version version = new Version();
              if (pluginMenuItem != null)
              {







                visPlugins.add(pluginMenuItem);
              }
            }
          }
          catch (Exception e) {}
        }
      }
    }
    
    if (availablePlugins) {
      resultListMenu.add(visPlugins);
    }
    
    resultListMenu.show(m_History.getList(), x, y);
  }
  






  protected void visualizeTree(String dottyString, String treeName)
  {
    Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("ClassifierPanel_VisualizeTree_JF_JFrame_Text") + treeName);
    
    jf.setSize(500, 400);
    jf.getContentPane().setLayout(new BorderLayout());
    TreeVisualizer tv = new TreeVisualizer(null, dottyString, new PlaceNode2());
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
  






  protected void visualizeBayesNet(String XMLBIF, String graphName)
  {
    Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("ClassifierPanel_VisualizeBayesNet_JF_JFrame_Text") + graphName);
    

    jf.setSize(500, 400);
    jf.getContentPane().setLayout(new BorderLayout());
    GraphVisualizer gv = new GraphVisualizer();
    try {
      gv.readBIF(XMLBIF);
    } catch (BIFFormatException be) {
      Messages.getInstance();System.err.println(Messages.getString("ClassifierPanel_VisualizeBayesNet_Error_Text"));
      
      be.printStackTrace();
    }
    gv.layoutGraph();
    
    jf.getContentPane().add(gv, "Center");
    jf.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e) {
        jf.dispose();
      }
      
    });
    jf.setVisible(true);
  }
  






  protected void visualizeClassifierErrors(VisualizePanel sp)
  {
    if (sp != null) {
      String plotName = sp.getName();
      Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("ClassifierPanel_VisualizeClassifierErrors_JF_JFrame_Text") + plotName);
      


      jf.setSize(600, 400);
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
  





  protected void visualizeCostBenefitAnalysis(CostBenefitAnalysis cb, String classifierAndRelationName)
  {
    if (cb != null) {
      Messages.getInstance();String windowTitle = Messages.getString("ClassifierPanel_VisualizeCostBenefitAnalysis_WindowTitle_Text");
      
      if (classifierAndRelationName != null) {
        windowTitle = windowTitle + "- " + classifierAndRelationName;
      }
      final JFrame jf = new JFrame(windowTitle);
      jf.setSize(1000, 600);
      jf.getContentPane().setLayout(new BorderLayout());
      
      jf.getContentPane().add(cb, "Center");
      jf.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          jf.dispose();
        }
        
      });
      jf.setVisible(true);
    }
  }
  




  protected void saveBuffer(String name)
  {
    StringBuffer sb = m_History.getNamedBuffer(name);
    if ((sb != null) && 
      (m_SaveOut.save(sb))) {
      Messages.getInstance();m_Log.logMessage(Messages.getString("ClassifierPanel_SaveBuffer_Log_LogMessage_Text"));
    }
  }
  





  protected void stopClassifier()
  {
    if (m_RunThread != null) {
      m_RunThread.interrupt();
      

      m_RunThread.stop();
    }
  }
  








  protected void saveClassifier(String name, Classifier classifier, Instances trainHeader)
  {
    File sFile = null;
    boolean saveOK = true;
    
    m_FileChooser.removeChoosableFileFilter(m_PMMLModelFilter);
    m_FileChooser.setFileFilter(m_ModelFilter);
    int returnVal = m_FileChooser.showSaveDialog(this);
    if (returnVal == 0) {
      sFile = m_FileChooser.getSelectedFile();
      if (!sFile.getName().toLowerCase().endsWith(MODEL_FILE_EXTENSION)) {
        sFile = new File(sFile.getParent(), sFile.getName() + MODEL_FILE_EXTENSION);
      }
      
      Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_SaveClassifier_Log_StatusMessage_Text"));
      
      try
      {
        OutputStream os = new FileOutputStream(sFile);
        if (sFile.getName().endsWith(".gz")) {
          os = new GZIPOutputStream(os);
        }
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
        objectOutputStream.writeObject(classifier);
        trainHeader = trainHeader.stringFreeStructure();
        if (trainHeader != null) {
          objectOutputStream.writeObject(trainHeader);
        }
        objectOutputStream.flush();
        objectOutputStream.close();
      }
      catch (Exception e) {
        Messages.getInstance();JOptionPane.showMessageDialog(null, e, Messages.getString("ClassifierPanel_SaveClassifier_JOptionPaneShowMessageDialog_Text_First"), 0);
        







        saveOK = false;
      }
      if (saveOK) {
        Messages.getInstance();Messages.getInstance();Messages.getInstance();m_Log.logMessage(Messages.getString("ClassifierPanel_SaveClassifier_Log_LogMessage_Text_First") + name + Messages.getString("ClassifierPanel_SaveClassifier_Log_LogMessage_Text_Second") + sFile.getName() + Messages.getString("ClassifierPanel_SaveClassifier_Log_LogMessage_Text_Third"));
      }
      






      Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_SaveClassifier_JOptionPaneShowMessageDialog_Text"));
    }
  }
  




  protected void loadClassifier()
  {
    m_FileChooser.addChoosableFileFilter(m_PMMLModelFilter);
    m_FileChooser.setFileFilter(m_ModelFilter);
    int returnVal = m_FileChooser.showOpenDialog(this);
    if (returnVal == 0) {
      File selected = m_FileChooser.getSelectedFile();
      Classifier classifier = null;
      Instances trainHeader = null;
      
      Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_LoadClassifier_Log_StatusMessage_Text_First"));
      
      try
      {
        InputStream is = new FileInputStream(selected);
        if (selected.getName().endsWith(PMML_FILE_EXTENSION)) {
          PMMLModel model = PMMLFactory.getPMMLModel(is, m_Log);
          if ((model instanceof PMMLClassifier)) {
            classifier = (PMMLClassifier)model;

          }
          else
          {

            Messages.getInstance();throw new Exception(Messages.getString("ClassifierPanel_LoadClassifier_Exception_Text"));
          }
        }
        else {
          if (selected.getName().endsWith(".gz")) {
            is = new GZIPInputStream(is);
          }
          ObjectInputStream objectInputStream = new ObjectInputStream(is);
          classifier = (Classifier)objectInputStream.readObject();
          try {
            trainHeader = (Instances)objectInputStream.readObject();
          }
          catch (Exception e) {}
          objectInputStream.close();
        }
      }
      catch (Exception e) {
        Messages.getInstance();JOptionPane.showMessageDialog(null, e, Messages.getString("ClassifierPanel_LoadClassifier_JOptionPaneShowMessageDialog_Text"), 0);
      }
      








      Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_LoadClassifier_Log_StatusMessage_Text_Second"));
      

      if (classifier != null) {
        Messages.getInstance();Messages.getInstance();m_Log.logMessage(Messages.getString("ClassifierPanel_SaveClassifier_Log_LogMessage_Text_Fourth") + selected.getName() + Messages.getString("ClassifierPanel_SaveClassifier_Log_LogMessage_Text_Fifth"));
        



        String name = new SimpleDateFormat("HH:mm:ss - ").format(new Date());
        String cname = classifier.getClass().getName();
        if (cname.startsWith("weka.classifiers.")) {
          cname = cname.substring("weka.classifiers.".length());
        }
        Messages.getInstance();Messages.getInstance();name = name + cname + Messages.getString("ClassifierPanel_SaveClassifier_Name_Text_First") + selected.getName() + Messages.getString("ClassifierPanel_SaveClassifier_Name_Text_Second");
        




        StringBuffer outBuff = new StringBuffer();
        
        Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_First"));
        
        Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_Second") + selected.getName() + "\n");
        

        Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_Fourth") + classifier.getClass().getName());
        

        if ((classifier instanceof OptionHandler)) {
          String[] o = classifier.getOptions();
          outBuff.append(" " + Utils.joinOptions(o));
        }
        outBuff.append("\n");
        if (trainHeader != null) {
          Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_Fifth") + trainHeader.relationName() + '\n');
          

          Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_Seventh") + trainHeader.numAttributes() + '\n');
          

          if (trainHeader.numAttributes() < 100) {
            for (int i = 0; i < trainHeader.numAttributes(); i++) {
              outBuff.append("              " + trainHeader.attribute(i).name() + '\n');
            }
          }
          else {
            Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_Nineth"));
          }
        }
        else {
          Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_Tenth"));
        }
        

        Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_Eleventh"));
        
        outBuff.append(classifier.toString() + "\n");
        
        m_History.addResult(name, outBuff);
        m_History.setSingle(name);
        FastVector vv = new FastVector();
        vv.addElement(classifier);
        if (trainHeader != null) {
          vv.addElement(trainHeader);
        }
        
        String grph = null;
        if ((classifier instanceof Drawable)) {
          try {
            grph = ((Drawable)classifier).graph();
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
  









  protected void reevaluateModel(final String name, final Classifier classifier, final Instances trainHeader)
  {
    if (m_RunThread == null) {
      synchronized (this) {
        m_StartBut.setEnabled(false);
        m_StopBut.setEnabled(true);
      }
      m_RunThread = new Thread()
      {
        public void run()
        {
          Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_ReEvaluateModel_Log_StatusMessage_Text_First"));
          

          StringBuffer outBuff = m_History.getNamedBuffer(name);
          ConverterUtils.DataSource source = null;
          Instances userTestStructure = null;
          
          FastVector plotShape = new FastVector();
          FastVector plotSize = new FastVector();
          Instances predInstances = null;
          
          CostMatrix costMatrix = null;
          if (m_EvalWRTCostsBut.isSelected()) {
            costMatrix = new CostMatrix((CostMatrix)m_CostMatrixEditor.getValue());
          }
          
          boolean outputConfusion = m_OutputConfusionBut.isSelected();
          boolean outputPerClass = m_OutputPerClassBut.isSelected();
          boolean outputSummary = true;
          boolean outputEntropy = m_OutputEntropyBut.isSelected();
          boolean saveVis = m_StorePredictionsBut.isSelected();
          boolean outputPredictionsText = m_OutputPredictionsTextBut.isSelected();
          
          String grph = null;
          Evaluation eval = null;
          
          try
          {
            boolean incrementalLoader = m_TestLoader instanceof IncrementalConverter;
            if ((m_TestLoader != null) && (m_TestLoader.getStructure() != null)) {
              m_TestLoader.reset();
              source = new ConverterUtils.DataSource(m_TestLoader);
              userTestStructure = source.getStructure();
            }
            
            if (source == null) {
              Messages.getInstance();throw new Exception(Messages.getString("ClassifierPanel_ReEvaluateModel_Exception_Text_First"));
            }
            
            if (trainHeader != null) {
              if (trainHeader.classIndex() > userTestStructure.numAttributes() - 1) {
                Messages.getInstance();throw new Exception(Messages.getString("ClassifierPanel_ReEvaluateModel_Exception_Text_Second"));
              }
              
              userTestStructure.setClassIndex(trainHeader.classIndex());
              if (!trainHeader.equalHeaders(userTestStructure)) {
                Messages.getInstance();throw new Exception(Messages.getString("ClassifierPanel_ReEvaluateModel_Exception_Text_Third"));
              }
              
            }
            else if ((classifier instanceof PMMLClassifier))
            {
              Instances miningSchemaStructure = ((PMMLClassifier)classifier).getMiningSchema().getMiningSchemaAsInstances();
              
              String className = miningSchemaStructure.classAttribute().name();
              
              Attribute classMatch = userTestStructure.attribute(className);
              if (classMatch == null) {
                Messages.getInstance();Messages.getInstance();throw new Exception(Messages.getString("ClassifierPanel_ReEvaluateModel_Exception_Text_Fourth") + className + Messages.getString("ClassifierPanel_ReEvaluateModel_Exception_Text_Fifth"));
              }
              



              userTestStructure.setClass(classMatch);
            } else {
              userTestStructure.setClassIndex(userTestStructure.numAttributes() - 1);
            }
            

            if ((m_Log instanceof TaskLogger)) {
              ((TaskLogger)m_Log).taskStarted();
            }
            Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_ReEvaluateModel_Log_StatusMessage_Text_Second"));
            
            Messages.getInstance();Messages.getInstance();m_Log.logMessage(Messages.getString("ClassifierPanel_ReEvaluateModel_Log_LogMessage_Text_First") + name + Messages.getString("ClassifierPanel_ReEvaluateModel_Log_LogMessage_Text_Second"));
            



            eval = new Evaluation(userTestStructure, costMatrix);
            eval.useNoPriors();
            


            if (saveVis) {
              predInstances = ClassifierPanel.setUpVisualizableInstances(userTestStructure);
              predInstances.setClassIndex(userTestStructure.classIndex() + 1);
            }
            
            Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_Twelveth"));
            
            Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_Thirteenth"));
            
            Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_Fourteenth") + userTestStructure.relationName() + '\n');
            

            if (incrementalLoader) {
              Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_Sixteenth"));
            }
            else {
              Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_Seventeenth") + source.getDataSet().numInstances() + "\n");
            }
            

            Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_Nineteenth") + userTestStructure.numAttributes() + "\n\n");
            

            if ((trainHeader == null) && (!(classifier instanceof PMMLClassifier)))
            {

              Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_TwentyFirst"));
            }
            


            if (outputPredictionsText) {
              Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_TwentySecond"));
              
              Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_TwentyThird"));
              
              if (userTestStructure.classAttribute().isNominal()) {
                Messages.getInstance();outBuff.append(Messages.getString("ClassifierPanel_SaveClassifier_OutBuffer_Text_TwentyFourth"));
              }
              



              outBuff.append("\n");
            }
            

            int jj = 0;
            while (source.hasMoreElements(userTestStructure)) {
              Instance instance = source.nextElement(userTestStructure);
              ClassifierPanel.processClassifierPrediction(instance, classifier, eval, predInstances, plotShape, plotSize);
              
              if (outputPredictionsText) {
                outBuff.append(predictionText(classifier, instance, jj + 1));
              }
              jj++; if (jj % 100 == 0) {
                Messages.getInstance();Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_ReEvaluateModel_Log_StatusMessage_Text_Third") + jj + Messages.getString("ClassifierPanel_ReEvaluateModel_Log_StatusMessage_Text_Fourth"));
              }
            }
            









            if (outputPredictionsText) {
              outBuff.append("\n");
            }
            
            if (outputSummary) {
              outBuff.append(eval.toSummaryString(outputEntropy) + "\n");
            }
            
            if (userTestStructure.classAttribute().isNominal())
            {
              if (outputPerClass) {
                outBuff.append(eval.toClassDetailsString() + "\n");
              }
              
              if (outputConfusion) {
                outBuff.append(eval.toMatrixString() + "\n");
              }
            }
            
            m_History.updateResult(name);
            Messages.getInstance();m_Log.logMessage(Messages.getString("ClassifierPanel_ReEvaluateModel_Log_LogMessage_Text_Third"));
            
            Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_ReEvaluateModel_Log_StatusMessage_Text_Fifth"));
          }
          catch (Exception ex) {
            ex.printStackTrace();
            m_Log.logMessage(ex.getMessage());
            Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_ReEvaluateModel_Log_StatusMessage_Text_Sixth"));
            

            ex.printStackTrace();
            m_Log.logMessage(ex.getMessage());
            Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(ClassifierPanel.this, Messages.getString("ClassifierPanel_ReEvaluateModel_JOptionPaneShowMessageDialog_Text_First") + ex.getMessage(), Messages.getString("ClassifierPanel_ReEvaluateModel_JOptionPaneShowMessageDialog_Text_Second"), 0);
            











            Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_ReEvaluateModel_Log_StatusMessage_Text_Seventh"));

          }
          finally
          {
            try
            {
              if ((classifier instanceof PMMLClassifier))
              {



                ((PMMLClassifier)classifier).done();
              }
              
              if ((predInstances != null) && (predInstances.numInstances() > 0)) {
                if (predInstances.attribute(predInstances.classIndex()).isNumeric())
                {
                  ClassifierPanel.this.postProcessPlotInfo(plotSize);
                }
                m_CurrentVis = new VisualizePanel();
                m_CurrentVis.setName(name + " (" + userTestStructure.relationName() + ")");
                
                m_CurrentVis.setLog(m_Log);
                PlotData2D tempd = new PlotData2D(predInstances);
                tempd.setShapeSize(plotSize);
                tempd.setShapeType(plotShape);
                tempd.setPlotName(name + " (" + userTestStructure.relationName() + ")");
                


                m_CurrentVis.addPlot(tempd);
                m_CurrentVis.setColourIndex(predInstances.classIndex());
                

                if ((classifier instanceof Drawable)) {
                  try {
                    grph = ((Drawable)classifier).graph();
                  }
                  catch (Exception ex) {}
                }
                
                if (saveVis) {
                  FastVector vv = new FastVector();
                  vv.addElement(classifier);
                  if (trainHeader != null) {
                    vv.addElement(trainHeader);
                  }
                  vv.addElement(m_CurrentVis);
                  if (grph != null) {
                    vv.addElement(grph);
                  }
                  if ((eval != null) && (eval.predictions() != null)) {
                    vv.addElement(eval.predictions());
                    vv.addElement(userTestStructure.classAttribute());
                  }
                  m_History.addObject(name, vv);
                } else {
                  FastVector vv = new FastVector();
                  vv.addElement(classifier);
                  if (trainHeader != null) {
                    vv.addElement(trainHeader);
                  }
                  m_History.addObject(name, vv);
                }
              }
            } catch (Exception ex) {
              ex.printStackTrace();
            }
            if (isInterrupted()) {
              Messages.getInstance();m_Log.logMessage(Messages.getString("ClassifierPanel_ReEvaluateModel_Log_LogMessage_Text_Fourth"));
              
              Messages.getInstance();m_Log.statusMessage(Messages.getString("ClassifierPanel_ReEvaluateModel_Log_StatusMessage_Text_Seventh"));
            }
            




            synchronized (this) {
              m_StartBut.setEnabled(true);
              m_StopBut.setEnabled(false);
              m_RunThread = null;
            }
            
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
      m_ClassifierEditor.setCapabilitiesFilter(new Capabilities(null)); return;
    }
    Instances tempInst;
    Instances tempInst;
    if (!ExplorerDefaults.getInitGenericObjectEditorFilter()) {
      tempInst = new Instances(m_Instances, 0);
    } else {
      tempInst = new Instances(m_Instances);
    }
    tempInst.setClassIndex(m_ClassCombo.getSelectedIndex());
    Capabilities filterClass;
    try {
      filterClass = Capabilities.forInstances(tempInst);
    } catch (Exception e) {
      filterClass = new Capabilities(null);
    }
    

    m_ClassifierEditor.setCapabilitiesFilter(filterClass);
    
    m_StartBut.setEnabled(true);
    
    Capabilities currentFilter = m_ClassifierEditor.getCapabilitiesFilter();
    Classifier classifier = (Classifier)m_ClassifierEditor.getValue();
    Capabilities currentSchemeCapabilities = null;
    if ((classifier != null) && (currentFilter != null) && ((classifier instanceof CapabilitiesHandler)))
    {
      currentSchemeCapabilities = classifier.getCapabilities();
      

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
    Messages.getInstance();return Messages.getString("ClassifierPanel_GetTabTitle_Text");
  }
  





  public String getTabTitleToolTip()
  {
    Messages.getInstance();return Messages.getString("ClassifierPanel_GetTabTitleToolTip_Text");
  }
  





  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("ClassifierPanel_Main_JFrame_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      ClassifierPanel sp = new ClassifierPanel();
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
        Messages.getInstance();System.err.println(Messages.getString("ClassifierPanel_Main_Error_Text") + args[0]);
        

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
