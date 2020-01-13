package weka.gui.experiment;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Range;
import weka.core.converters.CSVLoader;
import weka.experiment.CSVResultListener;
import weka.experiment.DatabaseResultListener;
import weka.experiment.Experiment;
import weka.experiment.InstanceQuery;
import weka.experiment.ResultMatrix;
import weka.experiment.Tester;
import weka.gui.DatabaseConnectionDialog;
import weka.gui.ExtensionFileFilter;
import weka.gui.ListSelectorDialog;
import weka.gui.ResultHistoryPanel;
import weka.gui.SaveBuffer;

public class ResultsPanel extends JPanel
{
  private static final long serialVersionUID = -4913007978534178569L;
  protected static final String NO_SOURCE;
  protected JButton m_FromFileBut;
  protected JButton m_FromDBaseBut;
  protected JButton m_FromExpBut;
  protected JLabel m_FromLab;
  private static String[] FOR_JFC_1_1_DCBM_BUG;
  protected DefaultComboBoxModel m_DatasetModel;
  protected DefaultComboBoxModel m_CompareModel;
  protected DefaultComboBoxModel m_SortModel;
  protected DefaultListModel m_TestsModel;
  protected DefaultListModel m_DisplayedModel;
  protected JLabel m_TesterClassesLabel;
  protected static DefaultComboBoxModel m_TesterClassesModel;
  protected static Vector m_Testers;
  protected JComboBox m_TesterClasses;
  protected JLabel m_DatasetKeyLabel;
  protected JButton m_DatasetKeyBut;
  protected DefaultListModel m_DatasetKeyModel;
  protected JList m_DatasetKeyList;
  protected JLabel m_ResultKeyLabel;
  protected JButton m_ResultKeyBut;
  protected DefaultListModel m_ResultKeyModel;
  protected JList m_ResultKeyList;
  protected JButton m_TestsButton;
  protected JButton m_DisplayedButton;
  protected JList m_TestsList;
  protected JList m_DisplayedList;
  protected JComboBox m_CompareCombo;
  protected JComboBox m_SortCombo;
  protected JTextField m_SigTex;
  protected JCheckBox m_ShowStdDevs;
  protected JButton m_OutputFormatButton;
  protected JButton m_PerformBut;
  protected JButton m_SaveOutBut;
  SaveBuffer m_SaveOut;
  protected JTextArea m_OutText;
  protected ResultHistoryPanel m_History;
  protected JFileChooser m_FileChooser;
  protected ExtensionFileFilter m_csvFileFilter;
  protected ExtensionFileFilter m_arffFileFilter;
  protected Tester m_TTester;
  protected Instances m_Instances;
  protected InstanceQuery m_InstanceQuery;
  protected Thread m_LoadThread;
  protected Experiment m_Exp;
  private Dimension COMBO_SIZE;
  protected ResultMatrix m_ResultMatrix;
  
  static
  {
    Messages.getInstance();NO_SOURCE = Messages.getString("ResultsPanel_FromFileBut_JButton_Text");
    

















    FOR_JFC_1_1_DCBM_BUG = new String[] { "" };
    

























    m_TesterClassesModel = new DefaultComboBoxModel(FOR_JFC_1_1_DCBM_BUG);
    




    m_Testers = null;
    





    Vector classes = weka.gui.GenericObjectEditor.getClassnames(Tester.class.getName());
    

    m_Testers = new Vector();
    m_TesterClassesModel = new DefaultComboBoxModel();
    for (int i = 0; i < classes.size(); i++) {
      try {
        Class cls = Class.forName(classes.get(i).toString());
        Tester tester = (Tester)cls.newInstance();
        m_Testers.add(cls);
        m_TesterClassesModel.addElement(tester.getDisplayName());
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  public ResultsPanel()
  {
    Messages.getInstance();m_FromFileBut = new JButton(Messages.getString("ResultsPanel_FromFileBut_JButton_Text"));
    

    Messages.getInstance();m_FromDBaseBut = new JButton(Messages.getString("ResultsPanel_FromDBaseBut_JButton_Text"));
    

    Messages.getInstance();m_FromExpBut = new JButton(Messages.getString("ResultsPanel_FromExpBut_JButton_Text"));
    

    m_FromLab = new JLabel(NO_SOURCE);
    








    m_DatasetModel = new DefaultComboBoxModel(FOR_JFC_1_1_DCBM_BUG);
    


    m_CompareModel = new DefaultComboBoxModel(FOR_JFC_1_1_DCBM_BUG);
    


    m_SortModel = new DefaultComboBoxModel(FOR_JFC_1_1_DCBM_BUG);
    


    m_TestsModel = new DefaultListModel();
    

    m_DisplayedModel = new DefaultListModel();
    

    Messages.getInstance();m_TesterClassesLabel = new JLabel(Messages.getString("ResultsPanel_TesterClassesLabel_JButton_Text"), 4);
    





































    m_TesterClasses = new JComboBox(m_TesterClassesModel);
    


    Messages.getInstance();m_DatasetKeyLabel = new JLabel(Messages.getString("ResultsPanel_DatasetKeyLabel_JButton_Text"), 4);
    


    Messages.getInstance();m_DatasetKeyBut = new JButton(Messages.getString("ResultsPanel_DatasetKeyBut_JButton_Text"));
    

    m_DatasetKeyModel = new DefaultListModel();
    

    m_DatasetKeyList = new JList(m_DatasetKeyModel);
    

    Messages.getInstance();m_ResultKeyLabel = new JLabel(Messages.getString("ResultsPanel_ResultKeyLabel_JButton_Text"), 4);
    


    Messages.getInstance();m_ResultKeyBut = new JButton(Messages.getString("ResultsPanel_ResultKeyBut_JButton_Text"));
    

    m_ResultKeyModel = new DefaultListModel();
    

    m_ResultKeyList = new JList(m_ResultKeyModel);
    

    Messages.getInstance();m_TestsButton = new JButton(Messages.getString("ResultsPanel_TestsButton_JButton_Text"));
    

    Messages.getInstance();m_DisplayedButton = new JButton(Messages.getString("ResultsPanel_DisplayedButton_JButton_Text"));
    

    m_TestsList = new JList(m_TestsModel);
    

    m_DisplayedList = new JList(m_DisplayedModel);
    

    m_CompareCombo = new JComboBox(m_CompareModel);
    

    m_SortCombo = new JComboBox(m_SortModel);
    

    m_SigTex = new JTextField("" + ExperimenterDefaults.getSignificance());
    



    m_ShowStdDevs = new JCheckBox("");
    


    Messages.getInstance();m_OutputFormatButton = new JButton(Messages.getString("ResultsPanel_OutputFormatButton_JButton_Text"));
    

    Messages.getInstance();m_PerformBut = new JButton(Messages.getString("ResultsPanel_PerformBut_JButton_Text"));
    

    Messages.getInstance();m_SaveOutBut = new JButton(Messages.getString("ResultsPanel_SaveOutBut_JButton_Text"));
    

    m_SaveOut = new SaveBuffer(null, this);
    

    m_OutText = new JTextArea();
    

    m_History = new ResultHistoryPanel(m_OutText);
    

    m_FileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
    


    Messages.getInstance();m_csvFileFilter = new ExtensionFileFilter(CSVLoader.FILE_EXTENSION, Messages.getString("ResultsPanel_CsvFileFilter_ExtensionFileFilter_Text"));
    


    Messages.getInstance();m_arffFileFilter = new ExtensionFileFilter(".arff", Messages.getString("ResultsPanel_ArffFileFilter_ExtensionFileFilter_Text"));
    


    m_TTester = new weka.experiment.PairedCorrectedTTester();
    













    COMBO_SIZE = new Dimension(150, m_ResultKeyBut.getPreferredSize().height);
    

    m_ResultMatrix = new weka.experiment.ResultMatrixPlainText();
    






    m_TTester.setSignificanceLevel(ExperimenterDefaults.getSignificance());
    m_TTester.setShowStdDevs(ExperimenterDefaults.getShowStdDevs());
    try {
      m_ResultMatrix = ((ResultMatrix)Class.forName(ExperimenterDefaults.getOutputFormat()).newInstance());
    }
    catch (Exception e)
    {
      m_ResultMatrix = new weka.experiment.ResultMatrixPlainText();
    }
    m_ResultMatrix.setShowStdDev(ExperimenterDefaults.getShowStdDevs());
    m_ResultMatrix.setMeanPrec(ExperimenterDefaults.getMeanPrecision());
    m_ResultMatrix.setStdDevPrec(ExperimenterDefaults.getStdDevPrecision());
    m_ResultMatrix.setRemoveFilterName(ExperimenterDefaults.getRemoveFilterClassnames());
    m_ResultMatrix.setShowAverage(ExperimenterDefaults.getShowAverage());
    


    m_FileChooser.addChoosableFileFilter(m_csvFileFilter);
    
    m_FileChooser.addChoosableFileFilter(m_arffFileFilter);
    

    m_FileChooser.setFileSelectionMode(0);
    m_FromExpBut.setEnabled(false);
    m_FromExpBut.setMnemonic('E');
    m_FromExpBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_LoadThread == null) {
          m_LoadThread = new Thread() {
            public void run() {
              setInstancesFromExp(m_Exp);
              m_LoadThread = null;
            }
          };
          m_LoadThread.start();
        }
      }
    });
    m_FromDBaseBut.setMnemonic('D');
    m_FromDBaseBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_LoadThread == null) {
          m_LoadThread = new Thread() {
            public void run() {
              setInstancesFromDBaseQuery();
              m_LoadThread = null;
            }
          };
          m_LoadThread.start();
        }
      }
    });
    m_FromFileBut.setMnemonic('F');
    m_FromFileBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int returnVal = m_FileChooser.showOpenDialog(ResultsPanel.this);
        if (returnVal == 0) {
          final File selected = m_FileChooser.getSelectedFile();
          if (m_LoadThread == null) {
            m_LoadThread = new Thread() {
              public void run() {
                setInstancesFromFile(selected);
                m_LoadThread = null;
              }
            };
            m_LoadThread.start();
          }
        }
      }
    });
    setComboSizes();
    m_TesterClasses.setEnabled(false);
    m_DatasetKeyBut.setEnabled(false);
    m_DatasetKeyBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setDatasetKeyFromDialog();
      }
    });
    m_DatasetKeyList.setSelectionMode(2);
    
    m_ResultKeyBut.setEnabled(false);
    m_ResultKeyBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setResultKeyFromDialog();
      }
    });
    m_ResultKeyList.setSelectionMode(2);
    
    m_CompareCombo.setEnabled(false);
    m_SortCombo.setEnabled(false);
    
    m_SigTex.setEnabled(false);
    m_TestsButton.setEnabled(false);
    m_TestsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setTestBaseFromDialog();
      }
      
    });
    m_DisplayedButton.setEnabled(false);
    m_DisplayedButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setDisplayedFromDialog();
      }
      
    });
    m_ShowStdDevs.setEnabled(false);
    m_ShowStdDevs.setSelected(ExperimenterDefaults.getShowStdDevs());
    m_OutputFormatButton.setEnabled(false);
    m_OutputFormatButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setOutputFormatFromDialog();
      }
      
    });
    m_PerformBut.setEnabled(false);
    m_PerformBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        performTest();
        m_SaveOutBut.setEnabled(true);
      }
      
    });
    m_PerformBut.setToolTipText(m_TTester.getToolTipText());
    
    m_SaveOutBut.setEnabled(false);
    m_SaveOutBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveBuffer();
      }
    });
    m_OutText.setFont(new java.awt.Font("Monospaced", 0, 12));
    m_OutText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    m_OutText.setEditable(false);
    Messages.getInstance();m_History.setBorder(BorderFactory.createTitledBorder(Messages.getString("ResultsPanel_OutText_SetBorder_BorderFactoryCreateTitledBorder_Text")));
    


    JPanel p1 = new JPanel();
    Messages.getInstance();p1.setBorder(BorderFactory.createTitledBorder(Messages.getString("ResultsPanel_OutText_SetBorder_BorderFactoryCreateTitledBorder_Text_First")));
    JPanel p2 = new JPanel();
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    p2.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
    
    p2.setLayout(gb);
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(0, 2, 0, 2);
    p2.add(m_FromFileBut, constraints);
    gridx = 1;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    p2.add(m_FromDBaseBut, constraints);
    gridx = 2;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    p2.add(m_FromExpBut, constraints);
    p1.setLayout(new BorderLayout());
    p1.add(m_FromLab, "Center");
    p1.add(p2, "East");
    
    JPanel p3 = new JPanel();
    Messages.getInstance();p3.setBorder(BorderFactory.createTitledBorder(Messages.getString("ResultsPanel_P3_SetBorder_BorderFactoryCreateTitledBorder_Text")));
    GridBagLayout gbL = new GridBagLayout();
    p3.setLayout(gbL);
    
    int y = 0;
    GridBagConstraints gbC = new GridBagConstraints();
    anchor = 13;
    gridy = y;gridx = 0;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(m_TesterClassesLabel, gbC);
    m_TesterClassesLabel.setDisplayedMnemonic('w');
    m_TesterClassesLabel.setLabelFor(m_TesterClasses);
    p3.add(m_TesterClassesLabel);
    gbC = new GridBagConstraints();
    gridy = y;gridx = 1;weightx = 100.0D;
    insets = new Insets(5, 0, 5, 0);
    fill = 2;
    gbL.setConstraints(m_TesterClasses, gbC);
    p3.add(m_TesterClasses);
    m_TesterClasses.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setTester();
      }
    });
    setSelectedItem(m_TesterClasses, ExperimenterDefaults.getTester());
    
    y++;
    gbC = new GridBagConstraints();
    anchor = 13;
    gridy = y;gridx = 0;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(m_DatasetKeyLabel, gbC);
    m_DatasetKeyLabel.setDisplayedMnemonic('R');
    m_DatasetKeyLabel.setLabelFor(m_DatasetKeyBut);
    p3.add(m_DatasetKeyLabel);
    gbC = new GridBagConstraints();
    fill = 2;
    gridy = y;gridx = 1;weightx = 100.0D;
    insets = new Insets(5, 0, 5, 0);
    gbL.setConstraints(m_DatasetKeyBut, gbC);
    p3.add(m_DatasetKeyBut);
    
    y++;
    gbC = new GridBagConstraints();
    anchor = 13;
    gridy = y;gridx = 0;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(m_ResultKeyLabel, gbC);
    m_ResultKeyLabel.setDisplayedMnemonic('C');
    m_ResultKeyLabel.setLabelFor(m_ResultKeyBut);
    p3.add(m_ResultKeyLabel);
    gbC = new GridBagConstraints();
    fill = 2;
    gridy = y;gridx = 1;weightx = 100.0D;
    insets = new Insets(5, 0, 5, 0);
    gbL.setConstraints(m_ResultKeyBut, gbC);
    p3.add(m_ResultKeyBut);
    
    y++;
    Messages.getInstance();JLabel lab = new JLabel(Messages.getString("ResultsPanel_Lab_JLabel_Text_First"), 4);
    lab.setDisplayedMnemonic('m');
    lab.setLabelFor(m_CompareCombo);
    gbC = new GridBagConstraints();
    anchor = 13;
    gridy = y;gridx = 0;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(lab, gbC);
    p3.add(lab);
    gbC = new GridBagConstraints();
    gridy = y;gridx = 1;weightx = 100.0D;
    insets = new Insets(5, 0, 5, 0);
    fill = 2;
    gbL.setConstraints(m_CompareCombo, gbC);
    p3.add(m_CompareCombo);
    
    y++;
    Messages.getInstance();lab = new JLabel(Messages.getString("ResultsPanel_Lab_JLabel_Text_Second"), 4);
    lab.setDisplayedMnemonic('g');
    lab.setLabelFor(m_SigTex);
    gbC = new GridBagConstraints();
    anchor = 13;
    gridy = y;gridx = 0;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(lab, gbC);
    p3.add(lab);
    gbC = new GridBagConstraints();
    fill = 2;
    gridy = y;gridx = 1;weightx = 100.0D;
    gbL.setConstraints(m_SigTex, gbC);
    p3.add(m_SigTex);
    
    y++;
    Messages.getInstance();lab = new JLabel(Messages.getString("ResultsPanel_Lab_JLabel_Text_Third"), 4);
    lab.setDisplayedMnemonic('S');
    lab.setLabelFor(m_SortCombo);
    gbC = new GridBagConstraints();
    anchor = 13;
    gridy = y;gridx = 0;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(lab, gbC);
    p3.add(lab);
    gbC = new GridBagConstraints();
    anchor = 17;
    fill = 2;
    gridy = y;gridx = 1;weightx = 100.0D;
    insets = new Insets(5, 0, 5, 0);
    gbL.setConstraints(m_SortCombo, gbC);
    p3.add(m_SortCombo);
    
    y++;
    Messages.getInstance();lab = new JLabel(Messages.getString("ResultsPanel_Lab_JLabel_Text_Fourth"), 4);
    lab.setDisplayedMnemonic('b');
    lab.setLabelFor(m_TestsButton);
    gbC = new GridBagConstraints();
    anchor = 13;
    gridy = y;gridx = 0;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(lab, gbC);
    p3.add(lab);
    gbC = new GridBagConstraints();
    fill = 2;
    gridy = y;gridx = 1;weightx = 100.0D;
    insets = new Insets(5, 0, 5, 0);
    gbL.setConstraints(m_TestsButton, gbC);
    p3.add(m_TestsButton);
    
    y++;
    Messages.getInstance();lab = new JLabel(Messages.getString("ResultsPanel_Lab_JLabel_Text_Fifth"), 4);
    lab.setDisplayedMnemonic('i');
    lab.setLabelFor(m_DisplayedButton);
    gbC = new GridBagConstraints();
    anchor = 13;
    gridy = y;gridx = 0;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(lab, gbC);
    p3.add(lab);
    gbC = new GridBagConstraints();
    fill = 2;
    gridy = y;gridx = 1;weightx = 100.0D;
    insets = new Insets(5, 0, 5, 0);
    gbL.setConstraints(m_DisplayedButton, gbC);
    p3.add(m_DisplayedButton);
    
    y++;
    Messages.getInstance();lab = new JLabel(Messages.getString("ResultsPanel_Lab_JLabel_Text_Sixth"), 4);
    lab.setDisplayedMnemonic('a');
    lab.setLabelFor(m_ShowStdDevs);
    gbC = new GridBagConstraints();
    anchor = 13;
    gridy = y;gridx = 0;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(lab, gbC);
    p3.add(lab);
    gbC = new GridBagConstraints();
    anchor = 17;
    gridy = y;gridx = 1;weightx = 100.0D;
    insets = new Insets(5, 0, 5, 0);
    gbL.setConstraints(m_ShowStdDevs, gbC);
    p3.add(m_ShowStdDevs);
    
    y++;
    Messages.getInstance();lab = new JLabel(Messages.getString("ResultsPanel_Lab_JLabel_Text_Seventh"), 4);
    lab.setDisplayedMnemonic('O');
    lab.setLabelFor(m_OutputFormatButton);
    gbC = new GridBagConstraints();
    anchor = 13;
    gridy = y;gridx = 0;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(lab, gbC);
    p3.add(lab);
    gbC = new GridBagConstraints();
    anchor = 17;
    fill = 2;
    gridy = y;gridx = 1;weightx = 100.0D;
    insets = new Insets(5, 0, 5, 0);
    gbL.setConstraints(m_OutputFormatButton, gbC);
    p3.add(m_OutputFormatButton);
    
    JPanel output = new JPanel();
    output.setLayout(new BorderLayout());
    Messages.getInstance();output.setBorder(BorderFactory.createTitledBorder(Messages.getString("ResultsPanel_Output_SetBorder_BorderFactoryCreateTitledBorder_Text")));
    output.add(new javax.swing.JScrollPane(m_OutText), "Center");
    
    JPanel mondo = new JPanel();
    gbL = new GridBagLayout();
    mondo.setLayout(gbL);
    gbC = new GridBagConstraints();
    

    gridy = 0;gridx = 0;
    gbL.setConstraints(p3, gbC);
    mondo.add(p3);
    
    JPanel bts = new JPanel();
    m_PerformBut.setMnemonic('t');
    m_SaveOutBut.setMnemonic('S');
    bts.setLayout(new java.awt.GridLayout(1, 2, 5, 5));
    bts.add(m_PerformBut);
    bts.add(m_SaveOutBut);
    
    gbC = new GridBagConstraints();
    anchor = 11;
    fill = 2;
    gridy = 1;gridx = 0;
    insets = new Insets(5, 5, 5, 5);
    gbL.setConstraints(bts, gbC);
    mondo.add(bts);
    gbC = new GridBagConstraints();
    
    fill = 1;
    gridy = 2;gridx = 0;weightx = 0.0D;
    weighty = 100.0D;
    gbL.setConstraints(m_History, gbC);
    mondo.add(m_History);
    






    JSplitPane splitPane = new JSplitPane(1, mondo, output);
    
    splitPane.setOneTouchExpandable(true);
    

    setLayout(new BorderLayout());
    add(p1, "North");
    
    add(splitPane, "Center");
  }
  




  protected void setComboSizes()
  {
    m_TesterClasses.setPreferredSize(COMBO_SIZE);
    m_DatasetKeyBut.setPreferredSize(COMBO_SIZE);
    m_ResultKeyBut.setPreferredSize(COMBO_SIZE);
    m_CompareCombo.setPreferredSize(COMBO_SIZE);
    m_SigTex.setPreferredSize(COMBO_SIZE);
    m_SortCombo.setPreferredSize(COMBO_SIZE);
    
    m_TesterClasses.setMaximumSize(COMBO_SIZE);
    m_DatasetKeyBut.setMaximumSize(COMBO_SIZE);
    m_ResultKeyBut.setMaximumSize(COMBO_SIZE);
    m_CompareCombo.setMaximumSize(COMBO_SIZE);
    m_SigTex.setMaximumSize(COMBO_SIZE);
    m_SortCombo.setMaximumSize(COMBO_SIZE);
    
    m_TesterClasses.setMinimumSize(COMBO_SIZE);
    m_DatasetKeyBut.setMinimumSize(COMBO_SIZE);
    m_ResultKeyBut.setMinimumSize(COMBO_SIZE);
    m_CompareCombo.setMinimumSize(COMBO_SIZE);
    m_SigTex.setMinimumSize(COMBO_SIZE);
    m_SortCombo.setMinimumSize(COMBO_SIZE);
  }
  





  public void setExperiment(Experiment exp)
  {
    m_Exp = exp;
    m_FromExpBut.setEnabled(exp != null);
  }
  



  protected void setInstancesFromDBaseQuery()
  {
    try
    {
      if (m_InstanceQuery == null) {
        m_InstanceQuery = new InstanceQuery();
      }
      String dbaseURL = m_InstanceQuery.getDatabaseURL();
      String username = m_InstanceQuery.getUsername();
      String passwd = m_InstanceQuery.getPassword();
      









      DatabaseConnectionDialog dbd = new DatabaseConnectionDialog(null, dbaseURL, username);
      dbd.setVisible(true);
      

      if (dbd.getReturnValue() == -1) {
        Messages.getInstance();m_FromLab.setText(Messages.getString("ResultsPanel_SetInstancesFromDBaseQuery_FromLab_Text_First"));
        return;
      }
      dbaseURL = dbd.getURL();
      username = dbd.getUsername();
      passwd = dbd.getPassword();
      m_InstanceQuery.setDatabaseURL(dbaseURL);
      m_InstanceQuery.setUsername(username);
      m_InstanceQuery.setPassword(passwd);
      m_InstanceQuery.setDebug(dbd.getDebug());
      
      m_InstanceQuery.connectToDatabase();
      if (!m_InstanceQuery.experimentIndexExists()) {
        Messages.getInstance();System.err.println(Messages.getString("ResultsPanel_SetInstancesFromDBaseQuery_Error_Text_First"));
        Messages.getInstance();m_FromLab.setText(Messages.getString("ResultsPanel_SetInstancesFromDBaseQuery_FromLab_Text_Second"));
        m_InstanceQuery.disconnectFromDatabase();
        return;
      }
      Messages.getInstance();System.err.println(Messages.getString("ResultsPanel_SetInstancesFromDBaseQuery_Error_Text_Second"));
      Messages.getInstance();m_FromLab.setText(Messages.getString("ResultsPanel_SetInstancesFromDBaseQuery_FromLab_Text_Fourth"));
      Instances index = m_InstanceQuery.retrieveInstances("SELECT * FROM Experiment_index");
      
      if (index.numInstances() == 0) {
        Messages.getInstance();m_FromLab.setText(Messages.getString("ResultsPanel_SetInstancesFromDBaseQuery_FromLab_Text_Fifth"));
        m_InstanceQuery.disconnectFromDatabase();
        return;
      }
      Messages.getInstance();m_FromLab.setText(Messages.getString("ResultsPanel_SetInstancesFromDBaseQuery_FromLab_Text_Sixth"));
      
      DefaultListModel lm = new DefaultListModel();
      for (int i = 0; i < index.numInstances(); i++) {
        lm.addElement(index.instance(i).toString());
      }
      JList jl = new JList(lm);
      jl.setSelectedIndex(0);
      int result;
      int result;
      if (jl.getModel().getSize() != 1) {
        ListSelectorDialog jd = new ListSelectorDialog(null, jl);
        result = jd.showDialog();
      }
      else {
        result = 0;
      }
      if (result != 0) {
        Messages.getInstance();m_FromLab.setText(Messages.getString("ResultsPanel_SetInstancesFromDBaseQuery_FromLab_Text_Sixth"));
        m_InstanceQuery.disconnectFromDatabase();
        return;
      }
      weka.core.Instance selInst = index.instance(jl.getSelectedIndex());
      Attribute tableAttr = index.attribute("Result_table");
      String table = "Results" + selInst.toString(tableAttr);
      
      setInstancesFromDatabaseTable(table);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      
      Messages.getInstance();Messages.getInstance();m_FromLab.setText(Messages.getString("ResultsPanel_SetInstancesFromDBaseQuery_FromLab_Text_Seventh_First") + ex.getMessage() + Messages.getString("ResultsPanel_SetInstancesFromDBaseQuery_FromLab_Text_Seventh_Second"));
    }
  }
  






  protected void setInstancesFromExp(Experiment exp)
  {
    if ((exp.getResultListener() instanceof CSVResultListener)) {
      File resultFile = ((CSVResultListener)exp.getResultListener()).getOutputFile();
      
      if (resultFile == null) {
        Messages.getInstance();m_FromLab.setText(Messages.getString("ResultsPanel_SetInstancesFromExp_FromLab_Text_First"));
      } else {
        setInstancesFromFile(resultFile);
      }
    } else if ((exp.getResultListener() instanceof DatabaseResultListener)) {
      String dbaseURL = ((DatabaseResultListener)exp.getResultListener()).getDatabaseURL();
      try
      {
        if (m_InstanceQuery == null) {
          m_InstanceQuery = new InstanceQuery();
        }
        m_InstanceQuery.setDatabaseURL(dbaseURL);
        m_InstanceQuery.connectToDatabase();
        String tableName = m_InstanceQuery.getResultsTableName(exp.getResultProducer());
        
        setInstancesFromDatabaseTable(tableName);
      } catch (Exception ex) {
        Messages.getInstance();m_FromLab.setText(Messages.getString("ResultsPanel_SetInstancesFromExp_FromLab_Text_Second"));
      }
    } else {
      Messages.getInstance();m_FromLab.setText(Messages.getString("ResultsPanel_SetInstancesFromExp_FromLab_Text_Third"));
    }
  }
  






  protected void setInstancesFromDatabaseTable(String tableName)
  {
    try
    {
      Messages.getInstance();m_FromLab.setText(Messages.getString("ResultsPanel_SetInstancesFromDatabaseTable_FromLab_Text"));
      final Instances i = m_InstanceQuery.retrieveInstances("SELECT * FROM " + tableName);
      
      javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
        public void run() {
          setInstances(i);
        }
      });
      m_InstanceQuery.disconnectFromDatabase();
    } catch (Exception ex) {
      m_FromLab.setText(ex.getMessage());
    }
  }
  






  protected void setInstancesFromFile(File f)
  {
    String fileType = f.getName();
    try {
      Messages.getInstance();m_FromLab.setText(Messages.getString("ResultsPanel_SetInstancesFromFile_FromLab_Text"));
      if (f.getName().toLowerCase().endsWith(".arff")) {
        fileType = "arff";
        java.io.Reader r = new java.io.BufferedReader(new java.io.FileReader(f));
        setInstances(new Instances(r));
        r.close();
      } else if (f.getName().toLowerCase().endsWith(CSVLoader.FILE_EXTENSION)) {
        fileType = "csv";
        CSVLoader cnv = new CSVLoader();
        cnv.setSource(f);
        Instances inst = cnv.getDataSet();
        setInstances(inst);
      } else {
        Messages.getInstance();throw new Exception(Messages.getString("ResultsPanel_SetInstancesFromFile_Error_Text"));
      }
    } catch (Exception ex) {
      Messages.getInstance();Messages.getInstance();Messages.getInstance();m_FromLab.setText(Messages.getString("ResultsPanel_SetInstancesFromFile_Error_FromLab_Text_First") + f.getName() + Messages.getString("ResultsPanel_SetInstancesFromFile_Error_FromLab_Text_Second") + fileType + Messages.getString("ResultsPanel_SetInstancesFromFile_Error_FromLab_Text_Third"));
      
      Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance();Messages.getInstance(); String[] tmp311_308 = new String[1];Messages.getInstance();tmp311_308[0] = Messages.getString("ResultsPanel_SetInstancesFromFile_Error_JOptionPaneShowOptionDialog_Text_Sixth"); if (javax.swing.JOptionPane.showOptionDialog(this, Messages.getString("ResultsPanel_SetInstancesFromFile_Error_JOptionPaneShowOptionDialog_Text_First") + f.getName() + Messages.getString("ResultsPanel_SetInstancesFromFile_Error_JOptionPaneShowOptionDialog_Text_Second") + fileType + Messages.getString("ResultsPanel_SetInstancesFromFile_Error_JOptionPaneShowOptionDialog_Text_Third") + Messages.getString("ResultsPanel_SetInstancesFromFile_Error_JOptionPaneShowOptionDialog_Text_Fourth") + ex.getMessage(), Messages.getString("ResultsPanel_SetInstancesFromFile_Error_JOptionPaneShowOptionDialog_Text_Fifth"), 0, 0, null, tmp311_308, null) != 1) {}
    }
  }
  




























  protected Vector determineColumnNames(String list, String defaultList, Instances inst)
  {
    Vector atts = new Vector();
    for (int i = 0; i < inst.numAttributes(); i++) {
      atts.add(inst.attribute(i).name().toLowerCase());
    }
    
    Vector result = new Vector();
    StringTokenizer tok = new StringTokenizer(list, ",");
    while (tok.hasMoreTokens()) {
      String item = tok.nextToken().toLowerCase();
      if (atts.contains(item)) {
        result.add(item);
      }
      else {
        result.clear();
      }
    }
    


    if (result.size() == 0) {
      tok = new StringTokenizer(defaultList, ",");
      while (tok.hasMoreTokens()) {
        result.add(tok.nextToken().toLowerCase());
      }
    }
    return result;
  }
  






  public void setInstances(Instances newInstances)
  {
    m_Instances = newInstances;
    m_TTester.setInstances(m_Instances);
    Messages.getInstance();Messages.getInstance();m_FromLab.setText(Messages.getString("ResultsPanel_SetInstances_FromLab_Text_First") + m_Instances.numInstances() + Messages.getString("ResultsPanel_SetInstances_FromLab_Text_Second"));
    

    Vector rows = determineColumnNames(ExperimenterDefaults.getRow(), "Key_Dataset", m_Instances);
    
    Vector cols = determineColumnNames(ExperimenterDefaults.getColumn(), "Key_Scheme,Key_Scheme_options,Key_Scheme_version_ID", m_Instances);
    


    m_DatasetKeyModel.removeAllElements();
    m_ResultKeyModel.removeAllElements();
    m_CompareModel.removeAllElements();
    m_SortModel.removeAllElements();
    m_SortModel.addElement("<default>");
    m_TTester.setSortColumn(-1);
    String selectedList = "";
    String selectedListDataset = "";
    boolean comparisonFieldSet = false;
    for (int i = 0; i < m_Instances.numAttributes(); i++) {
      String name = m_Instances.attribute(i).name();
      if (name.toLowerCase().startsWith("key_", 0)) {
        m_DatasetKeyModel.addElement(name.substring(4));
        m_ResultKeyModel.addElement(name.substring(4));
        m_CompareModel.addElement(name.substring(4));
      } else {
        m_DatasetKeyModel.addElement(name);
        m_ResultKeyModel.addElement(name);
        m_CompareModel.addElement(name);
        if (m_Instances.attribute(i).isNumeric()) {
          m_SortModel.addElement(name);
        }
      }
      if (rows.contains(name.toLowerCase())) {
        m_DatasetKeyList.addSelectionInterval(i, i);
        selectedListDataset = selectedListDataset + "," + (i + 1);
      } else if (name.toLowerCase().equals("key_run")) {
        m_TTester.setRunColumn(i);
      } else if (name.toLowerCase().equals("key_fold")) {
        m_TTester.setFoldColumn(i);
      } else if (cols.contains(name.toLowerCase())) {
        m_ResultKeyList.addSelectionInterval(i, i);
        selectedList = selectedList + "," + (i + 1);
      } else if (name.toLowerCase().indexOf(ExperimenterDefaults.getComparisonField()) != -1) {
        m_CompareCombo.setSelectedIndex(i);
        comparisonFieldSet = true;
      }
      else if ((name.toLowerCase().indexOf("root_relative_squared_error") != -1) && (!comparisonFieldSet))
      {
        m_CompareCombo.setSelectedIndex(i);
        comparisonFieldSet = true;
      }
    }
    m_TesterClasses.setEnabled(true);
    m_DatasetKeyBut.setEnabled(true);
    m_ResultKeyBut.setEnabled(true);
    m_CompareCombo.setEnabled(true);
    m_SortCombo.setEnabled(true);
    if (ExperimenterDefaults.getSorting().length() != 0) {
      setSelectedItem(m_SortCombo, ExperimenterDefaults.getSorting());
    }
    Range generatorRange = new Range();
    if (selectedList.length() != 0) {
      try {
        generatorRange.setRanges(selectedList);
      } catch (Exception ex) {
        ex.printStackTrace();
        System.err.println(ex.getMessage());
      }
    }
    m_TTester.setResultsetKeyColumns(generatorRange);
    
    generatorRange = new Range();
    if (selectedListDataset.length() != 0) {
      try {
        generatorRange.setRanges(selectedListDataset);
      } catch (Exception ex) {
        ex.printStackTrace();
        System.err.println(ex.getMessage());
      }
    }
    m_TTester.setDatasetKeyColumns(generatorRange);
    
    m_SigTex.setEnabled(true);
    
    setTTester();
  }
  








  protected void setSelectedItem(JComboBox cb, String item)
  {
    for (int i = 0; i < cb.getItemCount(); i++) {
      if (cb.getItemAt(i).toString().equals(item)) {
        cb.setSelectedIndex(i);
        break;
      }
    }
  }
  




  protected void setTTester()
  {
    m_TTester.setDisplayedResultsets(null);
    
    Messages.getInstance();String name = new SimpleDateFormat("HH:mm:ss - ").format(new java.util.Date()) + Messages.getString("ResultsPanel_SetTTester_Name_Text");
    

    StringBuffer outBuff = new StringBuffer();
    Messages.getInstance();outBuff.append(Messages.getString("ResultsPanel_SetTTester_OutBuff_Text_First") + m_TTester.resultsetKey() + "\n\n");
    
    m_History.addResult(name, outBuff);
    m_History.setSingle(name);
    
    m_TestsModel.removeAllElements();
    for (int i = 0; i < m_TTester.getNumResultsets(); i++) {
      String tname = m_TTester.getResultsetName(i);
      


      m_TestsModel.addElement(tname);
    }
    
    m_DisplayedModel.removeAllElements();
    for (int i = 0; i < m_TestsModel.size(); i++) {
      m_DisplayedModel.addElement(m_TestsModel.elementAt(i));
    }
    Messages.getInstance();m_TestsModel.addElement(Messages.getString("ResultsPanel_SetTTester_TestsModel_Element_Text_First"));
    Messages.getInstance();m_TestsModel.addElement(Messages.getString("ResultsPanel_SetTTester_TestsModel_Element_Text_Second"));
    
    m_TestsList.setSelectedIndex(0);
    m_DisplayedList.setSelectionInterval(0, m_DisplayedModel.size() - 1);
    
    m_TestsButton.setEnabled(true);
    m_DisplayedButton.setEnabled(true);
    m_ShowStdDevs.setEnabled(true);
    m_OutputFormatButton.setEnabled(true);
    m_PerformBut.setEnabled(true);
  }
  





  protected void performTest()
  {
    String sigStr = m_SigTex.getText();
    if (sigStr.length() != 0) {
      m_TTester.setSignificanceLevel(new Double(sigStr).doubleValue());
    } else {
      m_TTester.setSignificanceLevel(ExperimenterDefaults.getSignificance());
    }
    

    m_TTester.setShowStdDevs(m_ShowStdDevs.isSelected());
    if (m_Instances.attribute(m_SortCombo.getSelectedItem().toString()) != null) {
      m_TTester.setSortColumn(m_Instances.attribute(m_SortCombo.getSelectedItem().toString()).index());
    }
    else
    {
      m_TTester.setSortColumn(-1); }
    int compareCol = m_CompareCombo.getSelectedIndex();
    int tType = m_TestsList.getSelectedIndex();
    
    String name = new SimpleDateFormat("HH:mm:ss - ").format(new java.util.Date()) + (String)m_CompareCombo.getSelectedItem() + " - " + (String)m_TestsList.getSelectedValue();
    


    StringBuffer outBuff = new StringBuffer();
    outBuff.append(m_TTester.header(compareCol));
    outBuff.append("\n");
    m_History.addResult(name, outBuff);
    m_History.setSingle(name);
    m_TTester.setDisplayedResultsets(m_DisplayedList.getSelectedIndices());
    m_TTester.setResultMatrix(m_ResultMatrix);
    try {
      if (tType < m_TTester.getNumResultsets()) {
        outBuff.append(m_TTester.multiResultsetFull(tType, compareCol));
      } else if (tType == m_TTester.getNumResultsets()) {
        outBuff.append(m_TTester.multiResultsetSummary(compareCol));
      } else {
        outBuff.append(m_TTester.multiResultsetRanking(compareCol));
      }
      outBuff.append("\n");
    } catch (Exception ex) {
      outBuff.append(ex.getMessage() + "\n");
    }
    m_History.updateResult(name);
  }
  

  public void setResultKeyFromDialog()
  {
    ListSelectorDialog jd = new ListSelectorDialog(null, m_ResultKeyList);
    

    int result = jd.showDialog();
    

    if (result == 0) {
      int[] selected = m_ResultKeyList.getSelectedIndices();
      String selectedList = "";
      for (int i = 0; i < selected.length; i++) {
        selectedList = selectedList + "," + (selected[i] + 1);
      }
      Range generatorRange = new Range();
      if (selectedList.length() != 0) {
        try {
          generatorRange.setRanges(selectedList);
        } catch (Exception ex) {
          ex.printStackTrace();
          System.err.println(ex.getMessage());
        }
      }
      m_TTester.setResultsetKeyColumns(generatorRange);
      setTTester();
    }
  }
  
  public void setDatasetKeyFromDialog()
  {
    ListSelectorDialog jd = new ListSelectorDialog(null, m_DatasetKeyList);
    

    int result = jd.showDialog();
    

    if (result == 0) {
      int[] selected = m_DatasetKeyList.getSelectedIndices();
      String selectedList = "";
      for (int i = 0; i < selected.length; i++) {
        selectedList = selectedList + "," + (selected[i] + 1);
      }
      Range generatorRange = new Range();
      if (selectedList.length() != 0) {
        try {
          generatorRange.setRanges(selectedList);
        } catch (Exception ex) {
          ex.printStackTrace();
          System.err.println(ex.getMessage());
        }
      }
      m_TTester.setDatasetKeyColumns(generatorRange);
      setTTester();
    }
  }
  
  public void setTestBaseFromDialog() {
    ListSelectorDialog jd = new ListSelectorDialog(null, m_TestsList);
    

    jd.showDialog();
  }
  
  public void setDisplayedFromDialog() {
    ListSelectorDialog jd = new ListSelectorDialog(null, m_DisplayedList);
    

    jd.showDialog();
  }
  



  public void setOutputFormatFromDialog()
  {
    OutputFormatDialog dialog = new OutputFormatDialog(null);
    
    dialog.setResultMatrix(m_ResultMatrix.getClass());
    dialog.setMeanPrec(m_ResultMatrix.getMeanPrec());
    dialog.setStdDevPrec(m_ResultMatrix.getStdDevPrec());
    dialog.setRemoveFilterName(m_ResultMatrix.getRemoveFilterName());
    dialog.setShowAverage(m_ResultMatrix.getShowAverage());
    
    if (dialog.showDialog() == 0) {
      try {
        m_ResultMatrix = ((ResultMatrix)dialog.getResultMatrix().newInstance());
      }
      catch (Exception e) {
        e.printStackTrace();
        m_ResultMatrix = new weka.experiment.ResultMatrixPlainText();
      }
      m_ResultMatrix.setMeanPrec(dialog.getMeanPrec());
      m_ResultMatrix.setStdDevPrec(dialog.getStdDevPrec());
      m_ResultMatrix.setRemoveFilterName(dialog.getRemoveFilterName());
      m_ResultMatrix.setShowAverage(dialog.getShowAverage());
    }
  }
  


  protected void saveBuffer()
  {
    StringBuffer sb = m_History.getSelectedBuffer();
    if (sb != null) {
      if (m_SaveOut.save(sb)) {
        Messages.getInstance();Messages.getInstance();javax.swing.JOptionPane.showMessageDialog(this, Messages.getString("ResultsPanel_SetTTester_SaveBuffer_JOptionPaneShowMessageDialog_Text_First"), Messages.getString("ResultsPanel_SetTTester_SaveBuffer_JOptionPaneShowMessageDialog_Text_Second"), 1);
      }
      

    }
    else {
      m_SaveOutBut.setEnabled(false);
    }
  }
  






  protected void setTester()
  {
    if (m_TesterClasses.getSelectedItem() == null) {
      return;
    }
    Tester tester = null;
    
    try
    {
      for (int i = 0; i < m_Testers.size(); i++) {
        Tester t = (Tester)((Class)m_Testers.get(i)).newInstance();
        if (t.getDisplayName().equals(m_TesterClasses.getSelectedItem())) {
          tester = t;
          break;
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    if (tester == null) {
      tester = new weka.experiment.PairedCorrectedTTester();
      m_TesterClasses.setSelectedItem(tester.getDisplayName());
    }
    
    tester.assign(m_TTester);
    m_TTester = tester;
    m_PerformBut.setToolTipText(m_TTester.getToolTipText());
    Messages.getInstance();System.out.println(Messages.getString("ResultsPanel_SetTTester_SetTester_Text") + m_TTester.getClass().getName());
  }
  




  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("ResultsPanel_SetTTester_Main_JFrame_Text"));
      jf.getContentPane().setLayout(new BorderLayout());
      ResultsPanel sp = new ResultsPanel();
      
      jf.getContentPane().add(sp, "Center");
      jf.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setSize(700, 550);
      jf.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
