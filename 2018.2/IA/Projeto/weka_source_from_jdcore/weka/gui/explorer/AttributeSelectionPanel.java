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
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
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
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeEvaluator;
import weka.attributeSelection.AttributeTransformer;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Memory;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.gui.ExtensionFileFilter;
import weka.gui.GenericObjectEditor;
import weka.gui.LogPanel;
import weka.gui.Logger;
import weka.gui.PropertyPanel;
import weka.gui.ResultHistoryPanel;
import weka.gui.SaveBuffer;
import weka.gui.SysErrLog;
import weka.gui.TaskLogger;
import weka.gui.visualize.MatrixPanel;

































public class AttributeSelectionPanel
  extends JPanel
  implements Explorer.CapabilitiesFilterChangeListener, Explorer.ExplorerPanel, Explorer.LogHandler
{
  static final long serialVersionUID = 5627185966993476142L;
  protected Explorer m_Explorer = null;
  

  protected GenericObjectEditor m_AttributeEvaluatorEditor = new GenericObjectEditor();
  


  protected GenericObjectEditor m_AttributeSearchEditor = new GenericObjectEditor();
  


  protected PropertyPanel m_AEEPanel = new PropertyPanel(m_AttributeEvaluatorEditor);
  


  protected PropertyPanel m_ASEPanel = new PropertyPanel(m_AttributeSearchEditor);
  


  protected JTextArea m_OutText = new JTextArea(20, 40);
  

  protected Logger m_Log = new SysErrLog();
  

  SaveBuffer m_SaveOut = new SaveBuffer(m_Log, this);
  

  protected ResultHistoryPanel m_History = new ResultHistoryPanel(m_OutText);
  

  protected JComboBox m_ClassCombo = new JComboBox();
  protected JRadioButton m_CVBut;
  
  public AttributeSelectionPanel() { Messages.getInstance();m_CVBut = new JRadioButton(Messages.getString("AttributeSelectionPanel_CVBut_JRadioButton_Text"));
    

    Messages.getInstance();m_TrainBut = new JRadioButton(Messages.getString("AttributeSelectionPanel_TrainBut_JRadioButton_Text"));
    

    Messages.getInstance();m_CVLab = new JLabel(Messages.getString("AttributeSelectionPanel_CVLab_JLabel_Text"), 4);
    

    Messages.getInstance();m_CVText = new JTextField(Messages.getString("AttributeSelectionPanel_CVText_JTextField_Text"));
    



    Messages.getInstance();m_SeedLab = new JLabel(Messages.getString("AttributeSelectionPanel_SeedLab_JLabel_Text"), 4);
    

    Messages.getInstance();m_SeedText = new JTextField(Messages.getString("AttributeSelectionPanel_SeedText_JTextField_Text"));
    























    m_RadioListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateRadioLinks();
      }
      

    };
    Messages.getInstance();m_StartBut = new JButton(Messages.getString("AttributeSelectionPanel_StartBut_JButton_Text"));
    

    Messages.getInstance();m_StopBut = new JButton(Messages.getString("AttributeSelectionPanel_StopBut_JButton_Text"));
    

    COMBO_SIZE = new Dimension(150, m_StartBut.getPreferredSize().height);
    

















    m_OutText.setEditable(false);
    m_OutText.setFont(new Font("Monospaced", 0, 12));
    m_OutText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    m_OutText.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if ((e.getModifiers() & 0x10) != 16)
        {
          m_OutText.selectAll();
        }
      }
    });
    Messages.getInstance();m_History.setBorder(BorderFactory.createTitledBorder(Messages.getString("AttributeSelectionPanel_History_BorderFactoryCreateTitledBorder_Text")));
    m_AttributeEvaluatorEditor.setClassType(ASEvaluation.class);
    m_AttributeEvaluatorEditor.setValue(ExplorerDefaults.getASEvaluator());
    m_AttributeEvaluatorEditor.addPropertyChangeListener(new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent e) {
        if ((m_AttributeEvaluatorEditor.getValue() instanceof AttributeEvaluator)) {
          if (!(m_AttributeSearchEditor.getValue() instanceof Ranker)) {
            Object backup = m_AttributeEvaluatorEditor.getBackup();
            Messages.getInstance();Messages.getInstance();Messages.getInstance();int result = JOptionPane.showConfirmDialog(null, Messages.getString("AttributeSelectionPanel_Result_JOptionPaneShowConfirmDialog_Text_First") + m_AttributeEvaluatorEditor.getValue().getClass().getName() + Messages.getString("AttributeSelectionPanel_Result_JOptionPaneShowConfirmDialog_Text_Second"), Messages.getString("AttributeSelectionPanel_Result_JOptionPaneShowConfirmDialog_Text_Third"), 0);
            



            if (result == 0) {
              m_AttributeSearchEditor.setValue(new Ranker());

            }
            else if (backup != null) {
              m_AttributeEvaluatorEditor.setValue(backup);
            }
            
          }
        }
        else if ((m_AttributeSearchEditor.getValue() instanceof Ranker)) {
          Object backup = m_AttributeEvaluatorEditor.getBackup();
          Messages.getInstance();Messages.getInstance();Messages.getInstance();int result = JOptionPane.showConfirmDialog(null, Messages.getString("AttributeSelectionPanel_Result_JOptionPaneShowConfirmDialog_Text_Fourth") + m_AttributeEvaluatorEditor.getValue().getClass().getName() + Messages.getString("AttributeSelectionPanel_Result_JOptionPaneShowConfirmDialog_Text_Fifth"), Messages.getString("AttributeSelectionPanel_Result_JOptionPaneShowConfirmDialog_Text_Sixth"), 0);
          



          if (result == 0) {
            m_AttributeSearchEditor.setValue(new GreedyStepwise());

          }
          else if (backup != null) {
            m_AttributeEvaluatorEditor.setValue(backup);
          }
        }
        

        updateRadioLinks();
        
        m_StartBut.setEnabled(true);
        
        Capabilities currentFilter = m_AttributeEvaluatorEditor.getCapabilitiesFilter();
        ASEvaluation evaluator = (ASEvaluation)m_AttributeEvaluatorEditor.getValue();
        Capabilities currentSchemeCapabilities = null;
        if ((evaluator != null) && (currentFilter != null) && ((evaluator instanceof CapabilitiesHandler)))
        {
          currentSchemeCapabilities = evaluator.getCapabilities();
          
          if ((!currentSchemeCapabilities.supportsMaybe(currentFilter)) && (!currentSchemeCapabilities.supports(currentFilter)))
          {
            m_StartBut.setEnabled(false);
          }
        }
        repaint();
      }
      
    });
    m_AttributeSearchEditor.setClassType(ASSearch.class);
    m_AttributeSearchEditor.setValue(ExplorerDefaults.getASSearch());
    m_AttributeSearchEditor.addPropertyChangeListener(new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent e) {
        if ((m_AttributeSearchEditor.getValue() instanceof Ranker)) {
          if (!(m_AttributeEvaluatorEditor.getValue() instanceof AttributeEvaluator)) {
            Object backup = m_AttributeSearchEditor.getBackup();
            Messages.getInstance();Messages.getInstance();int result = JOptionPane.showConfirmDialog(null, Messages.getString("AttributeSelectionPanel_Result_JOptionPaneShowConfirmDialog_Text_Seventh"), Messages.getString("AttributeSelectionPanel_Result_JOptionPaneShowConfirmDialog_Text_Eigth"), 0);
            

            if (result == 0) {
              m_AttributeEvaluatorEditor.setValue(new InfoGainAttributeEval());

            }
            else if (backup != null) {
              m_AttributeSearchEditor.setValue(backup);
            }
            
          }
        }
        else if ((m_AttributeEvaluatorEditor.getValue() instanceof AttributeEvaluator)) {
          Object backup = m_AttributeSearchEditor.getBackup();
          Messages.getInstance();Messages.getInstance();Messages.getInstance();int result = JOptionPane.showConfirmDialog(null, Messages.getString("AttributeSelectionPanel_Result_JOptionPaneShowConfirmDialog_Text_Nineth") + m_AttributeEvaluatorEditor.getValue().getClass().getName() + Messages.getString("AttributeSelectionPanel_Result_JOptionPaneShowConfirmDialog_Text_Tenth"), Messages.getString("AttributeSelectionPanel_Result_JOptionPaneShowConfirmDialog_Text_Eleventh"), 0);
          




          if (result == 0) {
            m_AttributeEvaluatorEditor.setValue(new CfsSubsetEval());

          }
          else if (backup != null) {
            m_AttributeSearchEditor.setValue(backup);
          }
        }
        

        repaint();
      }
      
    });
    m_ClassCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateCapabilitiesFilter(m_AttributeEvaluatorEditor.getCapabilitiesFilter());
      }
      
    });
    Messages.getInstance();m_ClassCombo.setToolTipText(Messages.getString("AttributeSelectionPanel_ClassCombo_SetToolTipText_Text"));
    Messages.getInstance();m_TrainBut.setToolTipText(Messages.getString("AttributeSelectionPanel_TrainBut_SetToolTipText_Text"));
    Messages.getInstance();m_CVBut.setToolTipText(Messages.getString("AttributeSelectionPanel_CVBut_SetToolTipText_Text"));
    
    Messages.getInstance();m_StartBut.setToolTipText(Messages.getString("AttributeSelectionPanel_StartBut_SetToolTipText_Text"));
    Messages.getInstance();m_StopBut.setToolTipText(Messages.getString("AttributeSelectionPanel_StopBut_SetToolTipText_Text"));
    
    m_ClassCombo.setPreferredSize(COMBO_SIZE);
    m_ClassCombo.setMaximumSize(COMBO_SIZE);
    m_ClassCombo.setMinimumSize(COMBO_SIZE);
    m_History.setPreferredSize(COMBO_SIZE);
    m_History.setMaximumSize(COMBO_SIZE);
    m_History.setMinimumSize(COMBO_SIZE);
    
    m_ClassCombo.setEnabled(false);
    m_TrainBut.setSelected(ExplorerDefaults.getASTestMode() == 0);
    m_CVBut.setSelected(ExplorerDefaults.getASTestMode() == 1);
    updateRadioLinks();
    ButtonGroup bg = new ButtonGroup();
    bg.add(m_TrainBut);
    bg.add(m_CVBut);
    
    m_TrainBut.addActionListener(m_RadioListener);
    m_CVBut.addActionListener(m_RadioListener);
    
    m_CVText.setText("" + ExplorerDefaults.getASCrossvalidationFolds());
    m_SeedText.setText("" + ExplorerDefaults.getASRandomSeed());
    
    m_StartBut.setEnabled(false);
    m_StopBut.setEnabled(false);
    
    m_StartBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean proceed = true;
        if (Explorer.m_Memory.memoryIsLow()) {
          proceed = Explorer.m_Memory.showMemoryIsLow();
        }
        
        if (proceed) {
          startAttributeSelection();
        }
      }
    });
    m_StopBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        stopAttributeSelection();
      }
      
    });
    m_History.setHandleRightClicks(false);
    
    m_History.getList().addMouseListener(new MouseAdapter() {
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
    JPanel p1 = new JPanel();
    Messages.getInstance();p1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("AttributeSelectionPanel_P1_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    


    p1.setLayout(new BorderLayout());
    p1.add(m_AEEPanel, "North");
    
    JPanel p1_1 = new JPanel();
    Messages.getInstance();p1_1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("AttributeSelectionPanel_P1_1_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    


    p1_1.setLayout(new BorderLayout());
    p1_1.add(m_ASEPanel, "North");
    
    JPanel p_new = new JPanel();
    p_new.setLayout(new BorderLayout());
    p_new.add(p1, "North");
    p_new.add(p1_1, "Center");
    
    JPanel p2 = new JPanel();
    GridBagLayout gbL = new GridBagLayout();
    p2.setLayout(gbL);
    Messages.getInstance();p2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("AttributeSelectionPanel_P2_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    


    GridBagConstraints gbC = new GridBagConstraints();
    anchor = 17;
    gridy = 2;gridx = 0;
    gbL.setConstraints(m_TrainBut, gbC);
    p2.add(m_TrainBut);
    
    gbC = new GridBagConstraints();
    anchor = 17;
    gridy = 4;gridx = 0;
    gbL.setConstraints(m_CVBut, gbC);
    p2.add(m_CVBut);
    
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 4;gridx = 1;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(m_CVLab, gbC);
    p2.add(m_CVLab);
    
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 4;gridx = 2;weightx = 100.0D;
    ipadx = 20;
    gbL.setConstraints(m_CVText, gbC);
    p2.add(m_CVText);
    
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 6;gridx = 1;
    insets = new Insets(2, 10, 2, 10);
    gbL.setConstraints(m_SeedLab, gbC);
    p2.add(m_SeedLab);
    
    gbC = new GridBagConstraints();
    anchor = 13;
    fill = 2;
    gridy = 6;gridx = 2;weightx = 100.0D;
    ipadx = 20;
    gbL.setConstraints(m_SeedText, gbC);
    p2.add(m_SeedText);
    

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
    Messages.getInstance();p3.setBorder(BorderFactory.createTitledBorder(Messages.getString("AttributeSelectionPanel_P3_BorderFactoryCreateTitledBorder_Text")));
    
    p3.setLayout(new BorderLayout());
    JScrollPane js = new JScrollPane(m_OutText);
    p3.add(js, "Center");
    js.getViewport().addChangeListener(new ChangeListener() {
      private int lastHeight;
      
      public void stateChanged(ChangeEvent e) { JViewport vp = (JViewport)e.getSource();
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
    gridy = 0;gridx = 0;weightx = 0.0D;
    gbL.setConstraints(p2, gbC);
    mondo.add(p2);
    gbC = new GridBagConstraints();
    anchor = 11;
    fill = 2;
    gridy = 1;gridx = 0;weightx = 0.0D;
    gbL.setConstraints(buttons, gbC);
    mondo.add(buttons);
    gbC = new GridBagConstraints();
    fill = 1;
    gridy = 2;gridx = 0;weightx = 0.0D;weighty = 100.0D;
    gbL.setConstraints(m_History, gbC);
    mondo.add(m_History);
    gbC = new GridBagConstraints();
    fill = 1;
    gridy = 0;gridx = 1;
    gridheight = 3;
    weightx = 100.0D;weighty = 100.0D;
    gbL.setConstraints(p3, gbC);
    mondo.add(p3);
    
    setLayout(new BorderLayout());
    add(p_new, "North");
    add(mondo, "Center");
  }
  



  protected void updateRadioLinks()
  {
    m_CVBut.setEnabled(true);
    m_CVText.setEnabled(m_CVBut.isSelected());
    m_CVLab.setEnabled(m_CVBut.isSelected());
    m_SeedText.setEnabled(m_CVBut.isSelected());
    m_SeedLab.setEnabled(m_CVBut.isSelected());
    
    if ((m_AttributeEvaluatorEditor.getValue() instanceof AttributeTransformer))
    {
      m_CVBut.setSelected(false);
      m_CVBut.setEnabled(false);
      m_CVText.setEnabled(false);
      m_CVLab.setEnabled(false);
      m_SeedText.setEnabled(false);
      m_SeedLab.setEnabled(false);
      m_TrainBut.setSelected(true);
    }
  }
  





  public void setLog(Logger newLog)
  {
    m_Log = newLog;
  }
  





  public void setInstances(Instances inst)
  {
    m_Instances = inst;
    String[] attribNames = new String[m_Instances.numAttributes() + 1];
    attribNames[0] = "No class";
    for (int i = 0; i < inst.numAttributes(); i++) {
      String type = "";
      switch (m_Instances.attribute(i).type()) {
      case 1: 
        Messages.getInstance();type = Messages.getString("AttributeSelectionPanel_SetInstances_AttributeNOMINAL_Type_Text");
        break;
      case 0: 
        Messages.getInstance();type = Messages.getString("AttributeSelectionPanel_SetInstances_AttributeNUMERIC_Type_Text");
        break;
      case 2: 
        Messages.getInstance();type = Messages.getString("AttributeSelectionPanel_SetInstances_AttributeSTRING_Type_Text");
        break;
      case 3: 
        Messages.getInstance();type = Messages.getString("AttributeSelectionPanel_SetInstances_AttributeDATE_Type_Text");
        break;
      case 4: 
        Messages.getInstance();type = Messages.getString("AttributeSelectionPanel_SetInstances_AttributeRELATIONAL_Type_Text");
        break;
      default: 
        Messages.getInstance();type = Messages.getString("AttributeSelectionPanel_SetInstances_AttributeDEFAULT_Type_Text");
      }
      String attnm = m_Instances.attribute(i).name();
      
      attribNames[(i + 1)] = (type + attnm);
    }
    m_StartBut.setEnabled(m_RunThread == null);
    m_StopBut.setEnabled(m_RunThread != null);
    m_ClassCombo.setModel(new DefaultComboBoxModel(attribNames));
    if (inst.classIndex() == -1) {
      m_ClassCombo.setSelectedIndex(attribNames.length - 1);
    } else
      m_ClassCombo.setSelectedIndex(inst.classIndex());
    m_ClassCombo.setEnabled(true);
  }
  


  protected JRadioButton m_TrainBut;
  
  protected JLabel m_CVLab;
  
  protected void startAttributeSelection()
  {
    if (m_RunThread == null) {
      m_StartBut.setEnabled(false);
      m_StopBut.setEnabled(true);
      m_RunThread = new Thread()
      {
        public void run() {
          Messages.getInstance();m_Log.statusMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_StatusMessage_Text_First"));
          Instances inst = new Instances(m_Instances);
          
          int testMode = 0;
          int numFolds = 10;
          int seed = 1;
          int classIndex = m_ClassCombo.getSelectedIndex() - 1;
          ASEvaluation evaluator = (ASEvaluation)m_AttributeEvaluatorEditor.getValue();
          

          ASSearch search = (ASSearch)m_AttributeSearchEditor.getValue();
          
          StringBuffer outBuff = new StringBuffer();
          String name = new SimpleDateFormat("HH:mm:ss - ").format(new Date());
          
          String sname = search.getClass().getName();
          if (sname.startsWith("weka.attributeSelection.")) {
            name = name + sname.substring("weka.attributeSelection.".length());
          } else {
            name = name + sname;
          }
          String ename = evaluator.getClass().getName();
          if (ename.startsWith("weka.attributeSelection.")) {
            name = name + " + " + ename.substring("weka.attributeSelection.".length());
          }
          else {
            name = name + " + " + ename;
          }
          






          Vector<String> list = new Vector();
          list.add("-s");
          if ((search instanceof OptionHandler)) {
            list.add(sname + " " + Utils.joinOptions(((OptionHandler)search).getOptions()));
          } else
            list.add(sname);
          if ((evaluator instanceof OptionHandler)) {
            String[] opt = ((OptionHandler)evaluator).getOptions();
            for (int i = 0; i < opt.length; i++)
              list.add(opt[i]);
          }
          String cmd = ename + " " + Utils.joinOptions((String[])list.toArray(new String[list.size()]));
          


          weka.filters.supervised.attribute.AttributeSelection filter = new weka.filters.supervised.attribute.AttributeSelection();
          
          filter.setEvaluator((ASEvaluation)m_AttributeEvaluatorEditor.getValue());
          filter.setSearch((ASSearch)m_AttributeSearchEditor.getValue());
          String cmdFilter = filter.getClass().getName() + " " + Utils.joinOptions(filter.getOptions());
          


          AttributeSelectedClassifier cls = new AttributeSelectedClassifier();
          
          cls.setEvaluator((ASEvaluation)m_AttributeEvaluatorEditor.getValue());
          cls.setSearch((ASSearch)m_AttributeSearchEditor.getValue());
          String cmdClassifier = cls.getClass().getName() + " " + Utils.joinOptions(cls.getOptions());
          

          weka.attributeSelection.AttributeSelection eval = null;
          try
          {
            if (m_CVBut.isSelected()) {
              testMode = 1;
              numFolds = Integer.parseInt(m_CVText.getText());
              seed = Integer.parseInt(m_SeedText.getText());
              if (numFolds <= 1) {
                Messages.getInstance();throw new Exception(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Exception_Text_First"));
              }
            }
            
            if (classIndex >= 0) {
              inst.setClassIndex(classIndex);
            }
            

            Messages.getInstance();m_Log.logMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_LogMessage_Text_First") + ename);
            Messages.getInstance();m_Log.logMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_LogMessage_Text_Second") + cmd);
            Messages.getInstance();m_Log.logMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_LogMessage_Text_Third") + cmdFilter);
            Messages.getInstance();m_Log.logMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_LogMessage_Text_Fourth") + cmdClassifier);
            if ((m_Log instanceof TaskLogger)) {
              ((TaskLogger)m_Log).taskStarted();
            }
            Messages.getInstance();outBuff.append(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_OutBuffer_Text_First"));
            Messages.getInstance();outBuff.append(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_OutBuffer_Text_Second") + ename);
            if ((evaluator instanceof OptionHandler)) {
              String[] o = ((OptionHandler)evaluator).getOptions();
              outBuff.append(" " + Utils.joinOptions(o));
            }
            Messages.getInstance();outBuff.append(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_OutBuffer_Text_Third") + sname);
            if ((search instanceof OptionHandler)) {
              String[] o = ((OptionHandler)search).getOptions();
              outBuff.append(" " + Utils.joinOptions(o));
            }
            Messages.getInstance();outBuff.append(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_OutBuffer_Text_Fourth"));
            Messages.getInstance();outBuff.append(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_OutBuffer_Text_Fifth") + inst.relationName() + '\n');
            Messages.getInstance();outBuff.append(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_OutBuffer_Text_Sixth") + inst.numInstances() + '\n');
            Messages.getInstance();outBuff.append(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_OutBuffer_Text_Seventh") + inst.numAttributes() + '\n');
            if (inst.numAttributes() < 100) {
              for (int i = 0; i < inst.numAttributes(); i++) {
                outBuff.append("              " + inst.attribute(i).name() + '\n');
              }
            }
            else {
              Messages.getInstance();outBuff.append(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_OutBuffer_Text_Eigth"));
            }
            Messages.getInstance();outBuff.append(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_OutBuffer_Text_Nineth"));
            switch (testMode) {
            case 0: 
              Messages.getInstance();outBuff.append(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_OutBuffer_Text_Tenth"));
              break;
            case 1: 
              Messages.getInstance();outBuff.append("" + numFolds + Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_OutBuffer_Text_Eleventh"));
            }
            
            outBuff.append("\n");
            m_History.addResult(name, outBuff);
            m_History.setSingle(name);
            

            Messages.getInstance();m_Log.statusMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_StatusMessage_Text"));
            m_History.updateResult(name);
            
            eval = new weka.attributeSelection.AttributeSelection();
            eval.setEvaluator(evaluator);
            eval.setSearch(search);
            eval.setFolds(numFolds);
            eval.setSeed(seed);
            if (testMode == 1) {
              eval.setXval(true);
            }
            
            switch (testMode) {
            case 0: 
              Messages.getInstance();m_Log.statusMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_StatusMessage_Text_First"));
              eval.SelectAttributes(inst);
              break;
            
            case 1: 
              Messages.getInstance();m_Log.statusMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_StatusMessage_Text_Second"));
              Random random = new Random(seed);
              inst.randomize(random);
              if (inst.attribute(classIndex).isNominal()) {
                Messages.getInstance();m_Log.statusMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_StatusMessage_Text_Third"));
                inst.stratify(numFolds);
              }
              for (int fold = 0; fold < numFolds; fold++) {
                Messages.getInstance();Messages.getInstance();m_Log.statusMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_StatusMessage_Text_Fourth") + (fold + 1) + Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_StatusMessage_Text_Fifth"));
                
                Instances train = inst.trainCV(numFolds, fold, random);
                Messages.getInstance();Messages.getInstance();m_Log.statusMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_StatusMessage_Text_Sixth") + (fold + 1) + Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_StatusMessage_Text_Seventh"));
                

                eval.selectAttributesCVSplit(train);
              }
              break;
            default: 
              Messages.getInstance();throw new Exception(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Exception_Text_Second"));
            }
            
            if (testMode == 0) {
              outBuff.append(eval.toResultsString());
            } else {
              outBuff.append(eval.CVResultsString());
            }
            
            Messages.getInstance();outBuff.append(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_OutBuffer_Text_Thirteenth"));
            m_History.updateResult(name);
            Messages.getInstance();m_Log.logMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_LogMessage_Text_Fifth") + ename + " " + sname);
            Messages.getInstance();m_Log.statusMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_StatusMessage_Text_Eigth"));
          } catch (Exception ex) {
            m_Log.logMessage(ex.getMessage());
            Messages.getInstance();m_Log.statusMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_StatusMessage_Text_Ninth"));
          } finally {
            if ((evaluator instanceof AttributeTransformer)) {
              try {
                Instances transformed = ((AttributeTransformer)evaluator).transformedData(inst);
                
                Messages.getInstance();transformed.setRelationName(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Transformed_SetRelationName_Text") + transformed.relationName());
                

                FastVector vv = new FastVector();
                vv.addElement(transformed);
                m_History.addObject(name, vv);
              } catch (Exception ex) {
                System.err.println(ex);
                ex.printStackTrace();
              }
            } else if (testMode == 0) {
              try {
                Instances reducedInst = eval.reduceDimensionality(inst);
                FastVector vv = new FastVector();
                vv.addElement(reducedInst);
                m_History.addObject(name, vv);
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
            if (isInterrupted()) {
              Messages.getInstance();m_Log.logMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_LogMessage_Text_Sixth") + ename + " " + sname);
              Messages.getInstance();m_Log.statusMessage(Messages.getString("AttributeSelectionPanel_StartAttributeSelection_Run_Log_StatusMessage_Text_Tenth"));
            }
            m_RunThread = null;
            m_StartBut.setEnabled(true);
            m_StopBut.setEnabled(false);
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
  



  protected void stopAttributeSelection()
  {
    if (m_RunThread != null) {
      m_RunThread.interrupt();
      

      m_RunThread.stop();
    }
  }
  




  protected void saveBuffer(String name)
  {
    StringBuffer sb = m_History.getNamedBuffer(name);
    if ((sb != null) && 
      (m_SaveOut.save(sb))) {
      Messages.getInstance();m_Log.logMessage(Messages.getString("AttributeSelectionPanel_SaveBuffer_Log_LogMessage_Text"));
    }
  }
  





  protected void visualizeTransformedData(Instances ti)
  {
    if (ti != null) {
      MatrixPanel mp = new MatrixPanel();
      mp.setInstances(ti);
      String plotName = ti.relationName();
      Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("AttributeSelectionPanel_VisualizeTransformedData_JFrame_Text") + plotName);
      

      jf.setSize(800, 600);
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(mp, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          jf.dispose();
        }
        
      });
      jf.setVisible(true);
    }
  }
  


  protected JTextField m_CVText;
  

  protected JLabel m_SeedLab;
  
  protected JTextField m_SeedText;
  
  protected void saveTransformedData(Instances ti)
  {
    JFileChooser fc = new JFileChooser();
    Messages.getInstance();ExtensionFileFilter filter = new ExtensionFileFilter(".arff", Messages.getString("AttributeSelectionPanel_SaveTransformedData_Filter_Text"));
    fc.setFileFilter(filter);
    int retVal = fc.showSaveDialog(this);
    
    if (retVal == 0) {
      try {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fc.getSelectedFile()));
        writer.write(ti.toString());
        writer.flush();
        writer.close();
      }
      catch (Exception e) {
        e.printStackTrace();
        Messages.getInstance();m_Log.logMessage(Messages.getString("AttributeSelectionPanel_SaveTransformedData_Log_LogMessage_Text") + e.getMessage());
        Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("AttributeSelectionPanel_SaveTransformedData_JOptionPaneShowMessageDialog_Text_First") + e.getMessage(), Messages.getString("AttributeSelectionPanel_SaveTransformedData_JOptionPaneShowMessageDialog_Text_Second"), 0);
      }
    }
  }
  

  ActionListener m_RadioListener;
  
  protected JButton m_StartBut;
  
  protected JButton m_StopBut;
  
  private Dimension COMBO_SIZE;
  protected Instances m_Instances;
  protected Thread m_RunThread;
  protected void visualize(String name, int x, int y)
  {
    final String selectedName = name;
    JPopupMenu resultListMenu = new JPopupMenu();
    
    Messages.getInstance();JMenuItem visMainBuffer = new JMenuItem(Messages.getString("AttributeSelectionPanel_Visualize_VisMainBuffer_JMenuItem_Text"));
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
    
    Messages.getInstance();JMenuItem visSepBuffer = new JMenuItem(Messages.getString("AttributeSelectionPanel_Visualize_VisSepBuffer_JMenuItem_Text"));
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
    
    Messages.getInstance();JMenuItem saveOutput = new JMenuItem(Messages.getString("AttributeSelectionPanel_Visualize_SaveOutput_JMenuItem_Text"));
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
    
    Messages.getInstance();JMenuItem deleteOutput = new JMenuItem(Messages.getString("AttributeSelectionPanel_Visualize_DeleteOutput_JMenuItem_Text"));
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
    

    FastVector o = null;
    if (selectedName != null) {
      o = (FastVector)m_History.getNamedObject(selectedName);
    }
    

    Instances tempTransformed = null;
    
    if (o != null) {
      for (int i = 0; i < o.size(); i++) {
        Object temp = o.elementAt(i);
        
        if ((temp instanceof Instances))
        {
          tempTransformed = (Instances)temp;
        }
      }
    }
    

    final Instances ti = tempTransformed;
    JMenuItem visTrans = null;
    
    if (ti != null) {
      Messages.getInstance(); if (ti.relationName().startsWith(Messages.getString("AttributeSelectionPanel_Visualize_RelationName_Text_First"))) {
        Messages.getInstance();visTrans = new JMenuItem(Messages.getString("AttributeSelectionPanel_Visualize_VisTrans_JMenuItem_Text_First"));
      } else {
        Messages.getInstance();visTrans = new JMenuItem(Messages.getString("AttributeSelectionPanel_Visualize_VisTrans_JMenuItem_Text_Second"));
      }
      resultListMenu.addSeparator();
    }
    

    if ((ti != null) && (visTrans != null)) {
      visTrans.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          visualizeTransformedData(ti);
        }
      });
    }
    
    if (visTrans != null) {
      resultListMenu.add(visTrans);
    }
    
    JMenuItem saveTrans = null;
    if (ti != null) {
      Messages.getInstance(); if (ti.relationName().startsWith(Messages.getString("AttributeSelectionPanel_Visualize_RelationName_Text_Second"))) {
        Messages.getInstance();saveTrans = new JMenuItem(Messages.getString("AttributeSelectionPanel_Visualize_SaveTrans_JMenuItem_Text_First"));
      } else {
        Messages.getInstance();saveTrans = new JMenuItem(Messages.getString("AttributeSelectionPanel_Visualize_SaveTrans_JMenuItem_Text_Second"));
      } }
    if (saveTrans != null) {
      saveTrans.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          saveTransformedData(ti);
        }
      });
      resultListMenu.add(saveTrans);
    }
    
    resultListMenu.show(m_History.getList(), x, y);
  }
  







  protected void updateCapabilitiesFilter(Capabilities filter)
  {
    if (filter == null) {
      m_AttributeEvaluatorEditor.setCapabilitiesFilter(new Capabilities(null));
      m_AttributeSearchEditor.setCapabilitiesFilter(new Capabilities(null)); return;
    }
    Instances tempInst;
    Instances tempInst;
    if (!ExplorerDefaults.getInitGenericObjectEditorFilter()) {
      tempInst = new Instances(m_Instances, 0);
    } else
      tempInst = new Instances(m_Instances);
    int clIndex = m_ClassCombo.getSelectedIndex() - 1;
    
    if (clIndex >= 0) {
      tempInst.setClassIndex(clIndex);
    }
    Capabilities filterClass;
    try {
      filterClass = Capabilities.forInstances(tempInst);
    }
    catch (Exception e) {
      filterClass = new Capabilities(null);
    }
    

    m_AttributeEvaluatorEditor.setCapabilitiesFilter(filterClass);
    m_AttributeSearchEditor.setCapabilitiesFilter(filterClass);
    
    m_StartBut.setEnabled(true);
    
    Capabilities currentFilter = m_AttributeEvaluatorEditor.getCapabilitiesFilter();
    ASEvaluation evaluator = (ASEvaluation)m_AttributeEvaluatorEditor.getValue();
    Capabilities currentSchemeCapabilities = null;
    if ((evaluator != null) && (currentFilter != null) && ((evaluator instanceof CapabilitiesHandler)))
    {
      currentSchemeCapabilities = evaluator.getCapabilities();
      
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
    Messages.getInstance();return Messages.getString("AttributeSelectionPanel_GetTabTitle_Text");
  }
  




  public String getTabTitleToolTip()
  {
    Messages.getInstance();return Messages.getString("AttributeSelectionPanel_GetTabTitleToolTip_Text");
  }
  




  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("AttributeSelectionPanel_Main_JFrame_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      AttributeSelectionPanel sp = new AttributeSelectionPanel();
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
      jf.setVisible(true);
      if (args.length == 1) {
        Messages.getInstance();System.err.println(Messages.getString("AttributeSelectionPanel_Main_Error_Text") + args[0]);
        Reader r = new BufferedReader(new FileReader(args[0]));
        
        Instances i = new Instances(r);
        sp.setInstances(i);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
  
  static {}
}
