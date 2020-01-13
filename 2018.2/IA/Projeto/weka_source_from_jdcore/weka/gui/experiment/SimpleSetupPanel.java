package weka.gui.experiment;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;
import weka.classifiers.Classifier;
import weka.core.xml.KOML;
import weka.experiment.CSVResultListener;
import weka.experiment.ClassifierSplitEvaluator;
import weka.experiment.CrossValidationResultProducer;
import weka.experiment.DatabaseResultListener;
import weka.experiment.Experiment;
import weka.experiment.InstancesResultListener;
import weka.experiment.PropertyNode;
import weka.experiment.RandomSplitResultProducer;
import weka.experiment.RegressionSplitEvaluator;
import weka.experiment.SplitEvaluator;
import weka.gui.DatabaseConnectionDialog;
import weka.gui.ExtensionFileFilter;






































public class SimpleSetupPanel
  extends JPanel
{
  private static final long serialVersionUID = 5257424515609176509L;
  protected Experiment m_Exp;
  protected SetupModePanel m_modePanel = null;
  

  protected String m_destinationDatabaseURL;
  

  protected String m_destinationFilename = "";
  

  protected int m_numFolds = 10;
  

  protected double m_trainPercent = 66.0D;
  

  protected int m_numRepetitions = 10;
  

  protected boolean m_userHasBeenAskedAboutConversion;
  

  protected ExtensionFileFilter m_csvFileFilter;
  

  protected ExtensionFileFilter m_arffFileFilter;
  

  protected JButton m_OpenBut;
  

  protected JButton m_SaveBut;
  

  protected JButton m_NewBut;
  

  protected FileFilter m_ExpFilter;
  

  protected FileFilter m_KOMLFilter;
  

  protected FileFilter m_XMLFilter;
  

  protected JFileChooser m_FileChooser;
  

  protected JFileChooser m_DestFileChooser;
  

  protected JComboBox m_ResultsDestinationCBox;
  

  protected JLabel m_ResultsDestinationPathLabel;
  

  protected JTextField m_ResultsDestinationPathTField;
  

  protected JButton m_BrowseDestinationButton;
  

  protected JComboBox m_ExperimentTypeCBox;
  

  protected JLabel m_ExperimentParameterLabel;
  

  protected JTextField m_ExperimentParameterTField;
  

  protected JRadioButton m_ExpClassificationRBut;
  

  protected JRadioButton m_ExpRegressionRBut;
  

  protected JTextField m_NumberOfRepetitionsTField;
  

  protected JRadioButton m_OrderDatasetsFirstRBut;
  

  protected JRadioButton m_OrderAlgorithmsFirstRBut;
  

  protected static String DEST_DATABASE_TEXT;
  

  protected static String DEST_ARFF_TEXT;
  
  protected static String DEST_CSV_TEXT;
  
  protected static String TYPE_CROSSVALIDATION_TEXT;
  
  protected static String TYPE_RANDOMSPLIT_TEXT;
  

  static
  {
    Messages.getInstance();DEST_DATABASE_TEXT = Messages.getString("SimpleSetupPanel_DEST_DATABASE_TEXT_Text");
    Messages.getInstance();DEST_ARFF_TEXT = Messages.getString("SimpleSetupPanel_DEST_ARFF_TEXT_Text");
    Messages.getInstance();DEST_CSV_TEXT = Messages.getString("SimpleSetupPanel_DEST_CSV_TEXT_Text");
    Messages.getInstance();TYPE_CROSSVALIDATION_TEXT = Messages.getString("SimpleSetupPanel_TYPE_CROSSVALIDATION_TEXT_Text");
    Messages.getInstance();TYPE_RANDOMSPLIT_TEXT = Messages.getString("SimpleSetupPanel_TYPE_RANDOMSPLIT_TEXT_Text");
    Messages.getInstance(); } protected static String TYPE_FIXEDSPLIT_TEXT = Messages.getString("SimpleSetupPanel_TYPE_FIXEDSPLIT_TEXT_Text");
  


  protected DatasetListPanel m_DatasetListPanel;
  


  protected AlgorithmListPanel m_AlgorithmListPanel;
  


  protected JButton m_NotesButton;
  


  protected JFrame m_NotesFrame;
  


  protected JTextArea m_NotesText;
  


  protected PropertyChangeSupport m_Support;
  


  public SimpleSetupPanel(Experiment exp)
  {
    this();
    setExperiment(exp);
  }
  
  public SimpleSetupPanel()
  {
    Messages.getInstance();m_csvFileFilter = new ExtensionFileFilter(".csv", Messages.getString("SimpleSetupPanel_CsvFileFilter_Text"));
    


    Messages.getInstance();m_arffFileFilter = new ExtensionFileFilter(".arff", Messages.getString("SimpleSetupPanel_ArffFileFilter_Text"));
    


    Messages.getInstance();m_OpenBut = new JButton(Messages.getString("SimpleSetupPanel_OpenBut_JButton_Text"));
    

    Messages.getInstance();m_SaveBut = new JButton(Messages.getString("SimpleSetupPanel_SaveBut_JButton_Text"));
    

    Messages.getInstance();m_NewBut = new JButton(Messages.getString("SimpleSetupPanel_NewBut_JButton_Text"));
    

    Messages.getInstance();Messages.getInstance();m_ExpFilter = new ExtensionFileFilter(Experiment.FILE_EXTENSION, Messages.getString("SimpleSetupPanel_ExpFilter_ExtensionFileFilter_Text_First") + Experiment.FILE_EXTENSION + Messages.getString("SimpleSetupPanel_ExpFilter_ExtensionFileFilter_Text_Second"));
    



    Messages.getInstance();Messages.getInstance();m_KOMLFilter = new ExtensionFileFilter(".koml", Messages.getString("SimpleSetupPanel_KOMLFilter_ExtensionFileFilter_Text_First") + ".koml" + Messages.getString("SimpleSetupPanel_KOMLFilter_ExtensionFileFilter_Text_Second"));
    



    Messages.getInstance();m_XMLFilter = new ExtensionFileFilter(".xml", Messages.getString("SimpleSetupPanel_XMLFilter_ExtensionFileFilter_Text"));
    



    m_FileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
    


    m_DestFileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
    


    m_ResultsDestinationCBox = new JComboBox();
    

    Messages.getInstance();m_ResultsDestinationPathLabel = new JLabel(Messages.getString("SimpleSetupPanel_ResultsDestinationPathLabel_JLabel_Text"));
    

    m_ResultsDestinationPathTField = new JTextField();
    

    Messages.getInstance();m_BrowseDestinationButton = new JButton(Messages.getString("SimpleSetupPanel_BrowseDestinationButton_JButton_Text"));
    

    m_ExperimentTypeCBox = new JComboBox();
    

    Messages.getInstance();m_ExperimentParameterLabel = new JLabel(Messages.getString("SimpleSetupPanel_ExperimentParameterLabel_JLabel_Text"));
    

    m_ExperimentParameterTField = new JTextField();
    

    Messages.getInstance();m_ExpClassificationRBut = new JRadioButton(Messages.getString("SimpleSetupPanel_ExpClassificationRBut_JRadioButton_Text"));
    


    Messages.getInstance();m_ExpRegressionRBut = new JRadioButton(Messages.getString("SimpleSetupPanel_ExpRegressionRBut_JRadioButton_Text"));
    


    m_NumberOfRepetitionsTField = new JTextField();
    

    Messages.getInstance();m_OrderDatasetsFirstRBut = new JRadioButton(Messages.getString("SimpleSetupPanel_OrderDatasetsFirstRBut_JRadioButton_Text"));
    


    Messages.getInstance();m_OrderAlgorithmsFirstRBut = new JRadioButton(Messages.getString("SimpleSetupPanel_OrderAlgorithmsFirstRBut_JRadioButton_Text"));
    










    m_DatasetListPanel = new DatasetListPanel();
    

    m_AlgorithmListPanel = new AlgorithmListPanel();
    

    Messages.getInstance();m_NotesButton = new JButton(Messages.getString("SimpleSetupPanel_NotesButton_JButton_Text"));
    

    Messages.getInstance();m_NotesFrame = new JFrame(Messages.getString("SimpleSetupPanel_NotesFrame_JFrame_Text"));
    

    m_NotesText = new JTextArea(null, 10, 0);
    




    m_Support = new PropertyChangeSupport(this);
    

















    m_ResultsDestinationCBox.setEnabled(false);
    m_ResultsDestinationPathLabel.setEnabled(false);
    m_ResultsDestinationPathTField.setEnabled(false);
    m_BrowseDestinationButton.setEnabled(false);
    m_ExperimentTypeCBox.setEnabled(false);
    m_ExperimentParameterLabel.setEnabled(false);
    m_ExperimentParameterTField.setEnabled(false);
    m_ExpClassificationRBut.setEnabled(false);
    m_ExpRegressionRBut.setEnabled(false);
    m_NumberOfRepetitionsTField.setEnabled(false);
    m_OrderDatasetsFirstRBut.setEnabled(false);
    m_OrderAlgorithmsFirstRBut.setEnabled(false);
    
    try
    {
      m_destinationDatabaseURL = new DatabaseResultListener().getDatabaseURL();
    }
    catch (Exception e) {}
    
    m_NewBut.setMnemonic('N');
    m_NewBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Experiment newExp = new Experiment();
        CrossValidationResultProducer cvrp = new CrossValidationResultProducer();
        cvrp.setNumFolds(10);
        cvrp.setSplitEvaluator(new ClassifierSplitEvaluator());
        newExp.setResultProducer(cvrp);
        newExp.setPropertyArray(new Classifier[0]);
        newExp.setUsePropertyIterator(true);
        setExperiment(newExp);
        

        if (ExperimenterDefaults.getUseClassification()) {
          m_ExpClassificationRBut.setSelected(true);
        } else {
          m_ExpRegressionRBut.setSelected(true);
        }
        setSelectedItem(m_ResultsDestinationCBox, ExperimenterDefaults.getDestination());
        
        SimpleSetupPanel.this.destinationTypeChanged();
        
        setSelectedItem(m_ExperimentTypeCBox, ExperimenterDefaults.getExperimentType());
        

        m_numRepetitions = ExperimenterDefaults.getRepetitions();
        m_NumberOfRepetitionsTField.setText("" + m_numRepetitions);
        

        if (ExperimenterDefaults.getExperimentType().equals(SimpleSetupPanel.TYPE_CROSSVALIDATION_TEXT))
        {
          m_numFolds = ExperimenterDefaults.getFolds();
          m_ExperimentParameterTField.setText("" + m_numFolds);
        }
        else
        {
          m_trainPercent = ExperimenterDefaults.getTrainPercentage();
          m_ExperimentParameterTField.setText("" + m_trainPercent);
        }
        

        if (ExperimenterDefaults.getDatasetsFirst()) {
          m_OrderDatasetsFirstRBut.setSelected(true);
        } else {
          m_OrderAlgorithmsFirstRBut.setSelected(true);
        }
        SimpleSetupPanel.this.expTypeChanged();
      }
    });
    m_SaveBut.setEnabled(false);
    m_SaveBut.setMnemonic('S');
    m_SaveBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SimpleSetupPanel.this.saveExperiment();
      }
    });
    m_OpenBut.setMnemonic('O');
    m_OpenBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SimpleSetupPanel.this.openExperiment();
      }
    });
    m_FileChooser.addChoosableFileFilter(m_ExpFilter);
    if (KOML.isPresent())
      m_FileChooser.addChoosableFileFilter(m_KOMLFilter);
    m_FileChooser.addChoosableFileFilter(m_XMLFilter);
    if (ExperimenterDefaults.getExtension().equals(".xml")) {
      m_FileChooser.setFileFilter(m_XMLFilter);
    } else if ((KOML.isPresent()) && (ExperimenterDefaults.getExtension().equals(".koml"))) {
      m_FileChooser.setFileFilter(m_KOMLFilter);
    } else
      m_FileChooser.setFileFilter(m_ExpFilter);
    m_FileChooser.setFileSelectionMode(0);
    m_DestFileChooser.setFileSelectionMode(0);
    
    m_BrowseDestinationButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        if (m_ResultsDestinationCBox.getSelectedItem() == SimpleSetupPanel.DEST_DATABASE_TEXT) {
          SimpleSetupPanel.this.chooseURLUsername();
        } else {
          SimpleSetupPanel.this.chooseDestinationFile();
        }
        
      }
    });
    m_ExpClassificationRBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SimpleSetupPanel.this.expTypeChanged();
      }
      
    });
    m_ExpRegressionRBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SimpleSetupPanel.this.expTypeChanged();
      }
      
    });
    m_OrderDatasetsFirstRBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_Exp != null) {
          m_Exp.setAdvanceDataSetFirst(true);
          m_Support.firePropertyChange("", null, null);
        }
        
      }
    });
    m_OrderAlgorithmsFirstRBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_Exp != null) {
          m_Exp.setAdvanceDataSetFirst(false);
          m_Support.firePropertyChange("", null, null);
        }
        
      }
    });
    m_ResultsDestinationPathTField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { SimpleSetupPanel.this.destinationAddressChanged(); }
      public void removeUpdate(DocumentEvent e) { SimpleSetupPanel.this.destinationAddressChanged(); }
      public void changedUpdate(DocumentEvent e) { SimpleSetupPanel.this.destinationAddressChanged();
      }
    });
    m_ExperimentParameterTField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { SimpleSetupPanel.this.expParamChanged(); }
      public void removeUpdate(DocumentEvent e) { SimpleSetupPanel.this.expParamChanged(); }
      public void changedUpdate(DocumentEvent e) { SimpleSetupPanel.this.expParamChanged();
      }
    });
    m_NumberOfRepetitionsTField.getDocument().addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) { SimpleSetupPanel.this.numRepetitionsChanged(); }
      public void removeUpdate(DocumentEvent e) { SimpleSetupPanel.this.numRepetitionsChanged(); }
      public void changedUpdate(DocumentEvent e) { SimpleSetupPanel.this.numRepetitionsChanged();
      }
    });
    m_NotesFrame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        m_NotesButton.setEnabled(true);
      }
    });
    m_NotesFrame.getContentPane().add(new JScrollPane(m_NotesText));
    m_NotesFrame.setSize(600, 400);
    
    m_NotesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        m_NotesButton.setEnabled(false);
        m_NotesFrame.setVisible(true);
      }
    });
    m_NotesButton.setEnabled(false);
    
    m_NotesText.setEditable(true);
    
    m_NotesText.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        m_Exp.setNotes(m_NotesText.getText());
      }
    });
    m_NotesText.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent e) {
        m_Exp.setNotes(m_NotesText.getText());
      }
      

    });
    JPanel buttons = new JPanel();
    GridBagLayout gb = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    buttons.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    buttons.setLayout(gb);
    gridx = 0;gridy = 0;weightx = 5.0D;
    fill = 2;
    gridwidth = 1;gridheight = 1;
    insets = new Insets(0, 2, 0, 2);
    buttons.add(m_OpenBut, constraints);
    gridx = 1;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    buttons.add(m_SaveBut, constraints);
    gridx = 2;gridy = 0;weightx = 5.0D;
    gridwidth = 1;gridheight = 1;
    buttons.add(m_NewBut, constraints);
    
    JPanel destName = new JPanel();
    destName.setLayout(new BorderLayout(5, 5));
    destName.add(m_ResultsDestinationPathLabel, "West");
    destName.add(m_ResultsDestinationPathTField, "Center");
    
    m_ResultsDestinationCBox.addItem(DEST_ARFF_TEXT);
    m_ResultsDestinationCBox.addItem(DEST_CSV_TEXT);
    m_ResultsDestinationCBox.addItem(DEST_DATABASE_TEXT);
    
    m_ResultsDestinationCBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SimpleSetupPanel.this.destinationTypeChanged();
      }
      
    });
    JPanel destInner = new JPanel();
    destInner.setLayout(new BorderLayout(5, 5));
    destInner.add(m_ResultsDestinationCBox, "West");
    destInner.add(destName, "Center");
    destInner.add(m_BrowseDestinationButton, "East");
    
    JPanel dest = new JPanel();
    dest.setLayout(new BorderLayout());
    Messages.getInstance();dest.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("SimpleSetupPanel_Dest_JPanel_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    


    dest.add(destInner, "North");
    
    JPanel expParam = new JPanel();
    expParam.setLayout(new BorderLayout(5, 5));
    expParam.add(m_ExperimentParameterLabel, "West");
    expParam.add(m_ExperimentParameterTField, "Center");
    
    ButtonGroup typeBG = new ButtonGroup();
    typeBG.add(m_ExpClassificationRBut);
    typeBG.add(m_ExpRegressionRBut);
    m_ExpClassificationRBut.setSelected(true);
    
    JPanel typeRButtons = new JPanel();
    typeRButtons.setLayout(new GridLayout(1, 0));
    typeRButtons.add(m_ExpClassificationRBut);
    typeRButtons.add(m_ExpRegressionRBut);
    
    m_ExperimentTypeCBox.addItem(TYPE_CROSSVALIDATION_TEXT);
    m_ExperimentTypeCBox.addItem(TYPE_RANDOMSPLIT_TEXT);
    m_ExperimentTypeCBox.addItem(TYPE_FIXEDSPLIT_TEXT);
    
    m_ExperimentTypeCBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SimpleSetupPanel.this.expTypeChanged();
      }
      
    });
    JPanel typeInner = new JPanel();
    typeInner.setLayout(new GridLayout(0, 1));
    typeInner.add(m_ExperimentTypeCBox);
    typeInner.add(expParam);
    typeInner.add(typeRButtons);
    
    JPanel type = new JPanel();
    type.setLayout(new BorderLayout());
    Messages.getInstance();type.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("SimpleSetupPanel_Type_JPanel_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    


    type.add(typeInner, "North");
    
    ButtonGroup iterBG = new ButtonGroup();
    iterBG.add(m_OrderDatasetsFirstRBut);
    iterBG.add(m_OrderAlgorithmsFirstRBut);
    m_OrderDatasetsFirstRBut.setSelected(true);
    
    JPanel numIter = new JPanel();
    numIter.setLayout(new BorderLayout(5, 5));
    Messages.getInstance();numIter.add(new JLabel(Messages.getString("SimpleSetupPanel_NumIter_JPanel_Add_JLabel_Text")), "West");
    numIter.add(m_NumberOfRepetitionsTField, "Center");
    
    JPanel controlInner = new JPanel();
    controlInner.setLayout(new GridLayout(0, 1));
    controlInner.add(numIter);
    controlInner.add(m_OrderDatasetsFirstRBut);
    controlInner.add(m_OrderAlgorithmsFirstRBut);
    
    JPanel control = new JPanel();
    control.setLayout(new BorderLayout());
    Messages.getInstance();control.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("SimpleSetupPanel_Control_JPanel_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    


    control.add(controlInner, "North");
    
    JPanel type_control = new JPanel();
    type_control.setLayout(new GridLayout(1, 0));
    type_control.add(type);
    type_control.add(control);
    
    JPanel notes = new JPanel();
    notes.setLayout(new BorderLayout());
    notes.add(m_NotesButton, "Center");
    
    JPanel top1 = new JPanel();
    top1.setLayout(new BorderLayout());
    top1.add(dest, "North");
    top1.add(type_control, "Center");
    
    JPanel top = new JPanel();
    top.setLayout(new BorderLayout());
    top.add(buttons, "North");
    top.add(top1, "Center");
    
    JPanel datasets = new JPanel();
    datasets.setLayout(new BorderLayout());
    datasets.add(m_DatasetListPanel, "Center");
    
    JPanel algorithms = new JPanel();
    algorithms.setLayout(new BorderLayout());
    algorithms.add(m_AlgorithmListPanel, "Center");
    
    JPanel schemes = new JPanel();
    schemes.setLayout(new GridLayout(1, 0));
    schemes.add(datasets);
    schemes.add(algorithms);
    
    setLayout(new BorderLayout());
    add(top, "North");
    add(schemes, "Center");
    add(notes, "South");
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
  


  protected void removeNotesFrame()
  {
    m_NotesFrame.setVisible(false);
  }
  





  private boolean userWantsToConvert()
  {
    if (m_userHasBeenAskedAboutConversion) return true;
    m_userHasBeenAskedAboutConversion = true;
    Messages.getInstance();Messages.getInstance();return JOptionPane.showConfirmDialog(this, Messages.getString("SimpleSetupPanel_UserWantsToConvert_JOptionPaneShowConfirmDialog_Text_First"), Messages.getString("SimpleSetupPanel_UserWantsToConvert_JOptionPaneShowConfirmDialog_Text_Second"), 0, 2) == 0;
  }
  









  public void setModePanel(SetupModePanel modePanel)
  {
    m_modePanel = modePanel;
  }
  






  public boolean setExperiment(Experiment exp)
  {
    m_userHasBeenAskedAboutConversion = false;
    m_Exp = null;
    m_SaveBut.setEnabled(true);
    
    if ((exp.getResultListener() instanceof DatabaseResultListener)) {
      m_ResultsDestinationCBox.setSelectedItem(DEST_DATABASE_TEXT);
      Messages.getInstance();m_ResultsDestinationPathLabel.setText(Messages.getString("SimpleSetupPanel_SetExperiment_DatabaseResultListener_ResultsDestinationPathLabel_Text"));
      m_destinationDatabaseURL = ((DatabaseResultListener)exp.getResultListener()).getDatabaseURL();
      m_ResultsDestinationPathTField.setText(m_destinationDatabaseURL);
      m_BrowseDestinationButton.setEnabled(true);
    } else if ((exp.getResultListener() instanceof InstancesResultListener)) {
      m_ResultsDestinationCBox.setSelectedItem(DEST_ARFF_TEXT);
      Messages.getInstance();m_ResultsDestinationPathLabel.setText(Messages.getString("SimpleSetupPanel_SetExperiment_InstancesResultListener_Text"));
      m_destinationFilename = ((InstancesResultListener)exp.getResultListener()).outputFileName();
      m_ResultsDestinationPathTField.setText(m_destinationFilename);
      m_BrowseDestinationButton.setEnabled(true);
    } else if ((exp.getResultListener() instanceof CSVResultListener)) {
      m_ResultsDestinationCBox.setSelectedItem(DEST_CSV_TEXT);
      Messages.getInstance();m_ResultsDestinationPathLabel.setText(Messages.getString("SimpleSetupPanel_SetExperiment_CSVResultListener_Text"));
      m_destinationFilename = ((CSVResultListener)exp.getResultListener()).outputFileName();
      m_ResultsDestinationPathTField.setText(m_destinationFilename);
      m_BrowseDestinationButton.setEnabled(true);
    }
    else {
      Messages.getInstance();System.out.println(Messages.getString("SimpleSetupPanel_SetExperiment_UnrecognisedResultListener_Text"));
      if (userWantsToConvert()) {
        m_ResultsDestinationCBox.setSelectedItem(DEST_ARFF_TEXT);
        Messages.getInstance();m_ResultsDestinationPathLabel.setText(Messages.getString("SimpleSetupPanel_SetExperiment_UnrecognisedResultListener_UserWantsToConvert_Text"));
        m_destinationFilename = "";
        m_ResultsDestinationPathTField.setText(m_destinationFilename);
        m_BrowseDestinationButton.setEnabled(true);
      } else {
        return false;
      }
    }
    m_ResultsDestinationCBox.setEnabled(true);
    m_ResultsDestinationPathLabel.setEnabled(true);
    m_ResultsDestinationPathTField.setEnabled(true);
    
    if ((exp.getResultProducer() instanceof CrossValidationResultProducer)) {
      CrossValidationResultProducer cvrp = (CrossValidationResultProducer)exp.getResultProducer();
      m_numFolds = cvrp.getNumFolds();
      m_ExperimentParameterTField.setText("" + m_numFolds);
      
      if ((cvrp.getSplitEvaluator() instanceof ClassifierSplitEvaluator)) {
        m_ExpClassificationRBut.setSelected(true);
        m_ExpRegressionRBut.setSelected(false);
      } else if ((cvrp.getSplitEvaluator() instanceof RegressionSplitEvaluator)) {
        m_ExpClassificationRBut.setSelected(false);
        m_ExpRegressionRBut.setSelected(true);
      }
      else {
        Messages.getInstance();System.out.println(Messages.getString("SimpleSetupPanel_SetExperiment_UnknownSplitEvaluator_Text"));
        if (userWantsToConvert()) {
          m_ExpClassificationRBut.setSelected(true);
          m_ExpRegressionRBut.setSelected(false);
        } else {
          return false;
        }
      }
      m_ExperimentTypeCBox.setSelectedItem(TYPE_CROSSVALIDATION_TEXT);
    } else if ((exp.getResultProducer() instanceof RandomSplitResultProducer)) {
      RandomSplitResultProducer rsrp = (RandomSplitResultProducer)exp.getResultProducer();
      if (rsrp.getRandomizeData()) {
        m_ExperimentTypeCBox.setSelectedItem(TYPE_RANDOMSPLIT_TEXT);
      } else {
        m_ExperimentTypeCBox.setSelectedItem(TYPE_FIXEDSPLIT_TEXT);
      }
      if ((rsrp.getSplitEvaluator() instanceof ClassifierSplitEvaluator)) {
        m_ExpClassificationRBut.setSelected(true);
        m_ExpRegressionRBut.setSelected(false);
      } else if ((rsrp.getSplitEvaluator() instanceof RegressionSplitEvaluator)) {
        m_ExpClassificationRBut.setSelected(false);
        m_ExpRegressionRBut.setSelected(true);
      }
      else {
        Messages.getInstance();System.out.println(Messages.getString("SimpleSetupPanel_SetExperiment_UnknownSplitEvaluator_Text_First"));
        if (userWantsToConvert()) {
          m_ExpClassificationRBut.setSelected(true);
          m_ExpRegressionRBut.setSelected(false);
        } else {
          return false;
        }
      }
      m_trainPercent = rsrp.getTrainPercent();
      m_ExperimentParameterTField.setText("" + m_trainPercent);
    }
    else
    {
      Messages.getInstance();System.out.println(Messages.getString("SimpleSetupPanel_SetExperiment_UnknownPropertyIteration_Text"));
      if (userWantsToConvert()) {
        m_ExperimentTypeCBox.setSelectedItem(TYPE_CROSSVALIDATION_TEXT);
        m_ExpClassificationRBut.setSelected(true);
        m_ExpRegressionRBut.setSelected(false);
      } else {
        return false;
      }
    }
    
    m_ExperimentTypeCBox.setEnabled(true);
    m_ExperimentParameterLabel.setEnabled(true);
    m_ExperimentParameterTField.setEnabled(true);
    m_ExpClassificationRBut.setEnabled(true);
    m_ExpRegressionRBut.setEnabled(true);
    
    if (exp.getRunLower() == 1) {
      m_numRepetitions = exp.getRunUpper();
      m_NumberOfRepetitionsTField.setText("" + m_numRepetitions);
    }
    else {
      Messages.getInstance();System.out.println(Messages.getString("SimpleSetupPanel_SetExperiment_UnknownPropertyIteration_Text_Alpha"));
      if (userWantsToConvert()) {
        exp.setRunLower(1);
        if (m_ExperimentTypeCBox.getSelectedItem() == TYPE_FIXEDSPLIT_TEXT) {
          exp.setRunUpper(1);
          m_NumberOfRepetitionsTField.setEnabled(false);
          m_NumberOfRepetitionsTField.setText("1");
        } else {
          exp.setRunUpper(10);
          m_numRepetitions = 10;
          m_NumberOfRepetitionsTField.setText("" + m_numRepetitions);
        }
      }
      else {
        return false;
      }
    }
    m_NumberOfRepetitionsTField.setEnabled(true);
    
    m_OrderDatasetsFirstRBut.setSelected(exp.getAdvanceDataSetFirst());
    m_OrderAlgorithmsFirstRBut.setSelected(!exp.getAdvanceDataSetFirst());
    m_OrderDatasetsFirstRBut.setEnabled(true);
    m_OrderAlgorithmsFirstRBut.setEnabled(true);
    
    m_NotesText.setText(exp.getNotes());
    m_NotesButton.setEnabled(true);
    
    if ((!exp.getUsePropertyIterator()) || (!(exp.getPropertyArray() instanceof Classifier[])))
    {
      Messages.getInstance();System.out.println(Messages.getString("SimpleSetupPanel_SetExperiment_UnknownPropertyIteration_Text_First"));
      if (userWantsToConvert()) {
        exp.setPropertyArray(new Classifier[0]);
        exp.setUsePropertyIterator(true);
      } else {
        return false;
      }
    }
    
    m_DatasetListPanel.setExperiment(exp);
    m_AlgorithmListPanel.setExperiment(exp);
    
    m_Exp = exp;
    expTypeChanged();
    
    m_Support.firePropertyChange("", null, null);
    
    return true;
  }
  





  public Experiment getExperiment()
  {
    return m_Exp;
  }
  



  private void openExperiment()
  {
    int returnVal = m_FileChooser.showOpenDialog(this);
    if (returnVal != 0) {
      return;
    }
    File expFile = m_FileChooser.getSelectedFile();
    

    if (m_FileChooser.getFileFilter() == m_ExpFilter) {
      if (!expFile.getName().toLowerCase().endsWith(Experiment.FILE_EXTENSION)) {
        expFile = new File(expFile.getParent(), expFile.getName() + Experiment.FILE_EXTENSION);
      }
    } else if (m_FileChooser.getFileFilter() == m_KOMLFilter) {
      if (!expFile.getName().toLowerCase().endsWith(".koml")) {
        expFile = new File(expFile.getParent(), expFile.getName() + ".koml");
      }
    } else if ((m_FileChooser.getFileFilter() == m_XMLFilter) && 
      (!expFile.getName().toLowerCase().endsWith(".xml"))) {
      expFile = new File(expFile.getParent(), expFile.getName() + ".xml");
    }
    try
    {
      Experiment exp = Experiment.read(expFile.getAbsolutePath());
      if ((!setExperiment(exp)) && 
        (m_modePanel != null)) { m_modePanel.switchToAdvanced(exp);
      }
      Messages.getInstance();System.err.println(Messages.getString("SimpleSetupPanel_OpenExperiment_Exception_JOptionPaneShowMessageDialog_Text") + exp);
    } catch (Exception ex) {
      ex.printStackTrace();
      Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("SimpleSetupPanel_OpenExperiment_Exception_JOptionPaneShowMessageDialog_Text_First") + expFile + Messages.getString("SimpleSetupPanel_OpenExperiment_Exception_JOptionPaneShowMessageDialog_Text_Second") + ex.getMessage(), Messages.getString("SimpleSetupPanel_OpenExperiment_Exception_JOptionPaneShowMessageDialog_Text_Third"), 0);
    }
  }
  









  private void saveExperiment()
  {
    int returnVal = m_FileChooser.showSaveDialog(this);
    if (returnVal != 0) {
      return;
    }
    File expFile = m_FileChooser.getSelectedFile();
    

    if (m_FileChooser.getFileFilter() == m_ExpFilter) {
      if (!expFile.getName().toLowerCase().endsWith(Experiment.FILE_EXTENSION)) {
        expFile = new File(expFile.getParent(), expFile.getName() + Experiment.FILE_EXTENSION);
      }
    } else if (m_FileChooser.getFileFilter() == m_KOMLFilter) {
      if (!expFile.getName().toLowerCase().endsWith(".koml")) {
        expFile = new File(expFile.getParent(), expFile.getName() + ".koml");
      }
    } else if ((m_FileChooser.getFileFilter() == m_XMLFilter) && 
      (!expFile.getName().toLowerCase().endsWith(".xml"))) {
      expFile = new File(expFile.getParent(), expFile.getName() + ".xml");
    }
    try
    {
      Experiment.write(expFile.getAbsolutePath(), m_Exp);
      Messages.getInstance();System.err.println(Messages.getString("SimpleSetupPanel_SaveExperiment_Error_Text") + m_Exp);
    } catch (Exception ex) {
      ex.printStackTrace();
      Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("SimpleSetupPanel_SaveExperiment_Exception_JOptionPaneShowMessageDialog_Text_First") + expFile + Messages.getString("SimpleSetupPanel_SaveExperiment_Exception_JOptionPaneShowMessageDialog_Text_Second") + ex.getMessage(), Messages.getString("SimpleSetupPanel_SaveExperiment_Exception_JOptionPaneShowMessageDialog_Text_Third"), 0);
    }
  }
  








  public void addPropertyChangeListener(PropertyChangeListener l)
  {
    m_Support.addPropertyChangeListener(l);
  }
  




  public void removePropertyChangeListener(PropertyChangeListener l)
  {
    m_Support.removePropertyChangeListener(l);
  }
  



  private void destinationTypeChanged()
  {
    if (m_Exp == null) { return;
    }
    String str = "";
    
    if (m_ResultsDestinationCBox.getSelectedItem() == DEST_DATABASE_TEXT) {
      Messages.getInstance();m_ResultsDestinationPathLabel.setText(Messages.getString("SimpleSetupPanel_DestinationTypeChanged_DEST_DATABASE_TEXT_ResultsDestinationPathLabel_Text_First"));
      str = m_destinationDatabaseURL;
      m_BrowseDestinationButton.setEnabled(true);
      Messages.getInstance();m_BrowseDestinationButton.setText(Messages.getString("SimpleSetupPanel_DestinationTypeChanged_DEST_DATABASE_TEXT_BrowseDestinationButton_Text"));
    } else {
      Messages.getInstance();m_ResultsDestinationPathLabel.setText(Messages.getString("SimpleSetupPanel_DestinationTypeChanged_DEST_DATABASE_TEXT_ResultsDestinationPathLabel_Text_Second"));
      if (m_ResultsDestinationCBox.getSelectedItem() == DEST_ARFF_TEXT) {
        int ind = m_destinationFilename.lastIndexOf(".csv");
        if (ind > -1) {
          m_destinationFilename = (m_destinationFilename.substring(0, ind) + ".arff");
        }
      }
      if (m_ResultsDestinationCBox.getSelectedItem() == DEST_CSV_TEXT) {
        int ind = m_destinationFilename.lastIndexOf(".arff");
        if (ind > -1) {
          m_destinationFilename = (m_destinationFilename.substring(0, ind) + ".csv");
        }
      }
      str = m_destinationFilename;
      if (m_ResultsDestinationCBox.getSelectedItem() == DEST_ARFF_TEXT) {
        int ind = str.lastIndexOf(".csv");
        if (ind > -1) {
          str = str.substring(0, ind) + ".arff";
        }
      }
      if (m_ResultsDestinationCBox.getSelectedItem() == DEST_CSV_TEXT) {
        int ind = str.lastIndexOf(".arff");
        if (ind > -1) {
          str = str.substring(0, ind) + ".csv";
        }
      }
      m_BrowseDestinationButton.setEnabled(true);
      Messages.getInstance();m_BrowseDestinationButton.setText(Messages.getString("SimpleSetupPanel_DestinationTypeChanged_BrowseDestinationButton_Text"));
    }
    
    if (m_ResultsDestinationCBox.getSelectedItem() == DEST_DATABASE_TEXT) {
      DatabaseResultListener drl = null;
      try {
        drl = new DatabaseResultListener();
      } catch (Exception e) {
        e.printStackTrace();
      }
      drl.setDatabaseURL(m_destinationDatabaseURL);
      m_Exp.setResultListener(drl);
    }
    else if (m_ResultsDestinationCBox.getSelectedItem() == DEST_ARFF_TEXT) {
      InstancesResultListener irl = new InstancesResultListener();
      if (!m_destinationFilename.equals("")) {
        irl.setOutputFile(new File(m_destinationFilename));
      }
      m_Exp.setResultListener(irl);
    } else if (m_ResultsDestinationCBox.getSelectedItem() == DEST_CSV_TEXT) {
      CSVResultListener crl = new CSVResultListener();
      if (!m_destinationFilename.equals("")) {
        crl.setOutputFile(new File(m_destinationFilename));
      }
      m_Exp.setResultListener(crl);
    }
    

    m_ResultsDestinationPathTField.setText(str);
    
    m_Support.firePropertyChange("", null, null);
  }
  



  private void destinationAddressChanged()
  {
    if (m_Exp == null) { return;
    }
    if (m_ResultsDestinationCBox.getSelectedItem() == DEST_DATABASE_TEXT) {
      m_destinationDatabaseURL = m_ResultsDestinationPathTField.getText();
      if ((m_Exp.getResultListener() instanceof DatabaseResultListener)) {
        ((DatabaseResultListener)m_Exp.getResultListener()).setDatabaseURL(m_destinationDatabaseURL);
      }
    } else {
      File resultsFile = null;
      m_destinationFilename = m_ResultsDestinationPathTField.getText();
      

      if (m_destinationFilename.equals("")) {
        try {
          if (m_ResultsDestinationCBox.getSelectedItem() == DEST_ARFF_TEXT) {
            resultsFile = File.createTempFile("weka_experiment", ".arff");
          }
          if (m_ResultsDestinationCBox.getSelectedItem() == DEST_CSV_TEXT) {
            resultsFile = File.createTempFile("weka_experiment", ".csv");
          }
          resultsFile.deleteOnExit();
        } catch (Exception e) {
          Messages.getInstance();System.err.println(Messages.getString("SimpleSetupPanel_DestinationAddressChanged_Exception_Text"));
          resultsFile = new File("-");
        }
      } else {
        if ((m_ResultsDestinationCBox.getSelectedItem() == DEST_ARFF_TEXT) && 
          (!m_destinationFilename.endsWith(".arff"))) {
          m_destinationFilename += ".arff";
        }
        
        if ((m_ResultsDestinationCBox.getSelectedItem() == DEST_CSV_TEXT) && 
          (!m_destinationFilename.endsWith(".csv"))) {
          m_destinationFilename += ".csv";
        }
        
        resultsFile = new File(m_destinationFilename);
      }
      ((CSVResultListener)m_Exp.getResultListener()).setOutputFile(resultsFile);
      ((CSVResultListener)m_Exp.getResultListener()).setOutputFileName(m_destinationFilename);
    }
    
    m_Support.firePropertyChange("", null, null);
  }
  



  private void expTypeChanged()
  {
    if (m_Exp == null) { return;
    }
    
    if (m_ExperimentTypeCBox.getSelectedItem() == TYPE_CROSSVALIDATION_TEXT) {
      Messages.getInstance();m_ExperimentParameterLabel.setText(Messages.getString("SimpleSetupPanel_ExpTypeChanged_TYPE_CROSSVALIDATION_TEXT_ExperimentParameterLabel_Text_First"));
      m_ExperimentParameterTField.setText("" + m_numFolds);
    } else {
      Messages.getInstance();m_ExperimentParameterLabel.setText(Messages.getString("SimpleSetupPanel_ExpTypeChanged_TYPE_CROSSVALIDATION_TEXT_ExperimentParameterLabel_Text_Second"));
      m_ExperimentParameterTField.setText("" + m_trainPercent);
    }
    

    if (m_ExperimentTypeCBox.getSelectedItem() == TYPE_FIXEDSPLIT_TEXT) {
      m_NumberOfRepetitionsTField.setEnabled(false);
      m_NumberOfRepetitionsTField.setText("1");
      m_Exp.setRunLower(1);
      m_Exp.setRunUpper(1);
    } else {
      m_NumberOfRepetitionsTField.setText("" + m_numRepetitions);
      m_NumberOfRepetitionsTField.setEnabled(true);
      m_Exp.setRunLower(1);
      m_Exp.setRunUpper(m_numRepetitions);
    }
    
    SplitEvaluator se = null;
    Classifier sec = null;
    if (m_ExpClassificationRBut.isSelected()) {
      se = new ClassifierSplitEvaluator();
      sec = ((ClassifierSplitEvaluator)se).getClassifier();
    } else {
      se = new RegressionSplitEvaluator();
      sec = ((RegressionSplitEvaluator)se).getClassifier();
    }
    

    if (m_ExperimentTypeCBox.getSelectedItem() == TYPE_CROSSVALIDATION_TEXT) {
      CrossValidationResultProducer cvrp = new CrossValidationResultProducer();
      cvrp.setNumFolds(m_numFolds);
      cvrp.setSplitEvaluator(se);
      
      PropertyNode[] propertyPath = new PropertyNode[2];
      try {
        propertyPath[0] = new PropertyNode(se, new PropertyDescriptor("splitEvaluator", CrossValidationResultProducer.class), CrossValidationResultProducer.class);
        

        propertyPath[1] = new PropertyNode(sec, new PropertyDescriptor("classifier", se.getClass()), se.getClass());
      }
      catch (IntrospectionException e)
      {
        e.printStackTrace();
      }
      
      m_Exp.setResultProducer(cvrp);
      m_Exp.setPropertyPath(propertyPath);
    }
    else {
      RandomSplitResultProducer rsrp = new RandomSplitResultProducer();
      rsrp.setRandomizeData(m_ExperimentTypeCBox.getSelectedItem() == TYPE_RANDOMSPLIT_TEXT);
      rsrp.setTrainPercent(m_trainPercent);
      rsrp.setSplitEvaluator(se);
      
      PropertyNode[] propertyPath = new PropertyNode[2];
      try {
        propertyPath[0] = new PropertyNode(se, new PropertyDescriptor("splitEvaluator", RandomSplitResultProducer.class), RandomSplitResultProducer.class);
        

        propertyPath[1] = new PropertyNode(sec, new PropertyDescriptor("classifier", se.getClass()), se.getClass());
      }
      catch (IntrospectionException e)
      {
        e.printStackTrace();
      }
      
      m_Exp.setResultProducer(rsrp);
      m_Exp.setPropertyPath(propertyPath);
    }
    

    m_Exp.setUsePropertyIterator(true);
    m_Support.firePropertyChange("", null, null);
  }
  



  private void expParamChanged()
  {
    if (m_Exp == null) { return;
    }
    if (m_ExperimentTypeCBox.getSelectedItem() == TYPE_CROSSVALIDATION_TEXT) {
      try {
        m_numFolds = Integer.parseInt(m_ExperimentParameterTField.getText());
      } catch (NumberFormatException e) {
        return;
      }
    } else {
      try {
        m_trainPercent = Double.parseDouble(m_ExperimentParameterTField.getText());
      } catch (NumberFormatException e) {
        return;
      }
    }
    
    if (m_ExperimentTypeCBox.getSelectedItem() == TYPE_CROSSVALIDATION_TEXT)
    {
      if ((m_Exp.getResultProducer() instanceof CrossValidationResultProducer)) {
        CrossValidationResultProducer cvrp = (CrossValidationResultProducer)m_Exp.getResultProducer();
        cvrp.setNumFolds(m_numFolds);

      }
      


    }
    else if ((m_Exp.getResultProducer() instanceof RandomSplitResultProducer)) {
      RandomSplitResultProducer rsrp = (RandomSplitResultProducer)m_Exp.getResultProducer();
      rsrp.setRandomizeData(m_ExperimentTypeCBox.getSelectedItem() == TYPE_RANDOMSPLIT_TEXT);
      rsrp.setTrainPercent(m_trainPercent);
    }
    else {
      return;
    }
    

    m_Support.firePropertyChange("", null, null);
  }
  



  private void numRepetitionsChanged()
  {
    if ((m_Exp == null) || (!m_NumberOfRepetitionsTField.isEnabled())) return;
    try
    {
      m_numRepetitions = Integer.parseInt(m_NumberOfRepetitionsTField.getText());
    } catch (NumberFormatException e) {
      return;
    }
    
    m_Exp.setRunLower(1);
    m_Exp.setRunUpper(m_numRepetitions);
    
    m_Support.firePropertyChange("", null, null);
  }
  


  private void chooseURLUsername()
  {
    String dbaseURL = ((DatabaseResultListener)m_Exp.getResultListener()).getDatabaseURL();
    String username = ((DatabaseResultListener)m_Exp.getResultListener()).getUsername();
    DatabaseConnectionDialog dbd = new DatabaseConnectionDialog(null, dbaseURL, username);
    dbd.setVisible(true);
    

    if (dbd.getReturnValue() == -1) {
      return;
    }
    
    ((DatabaseResultListener)m_Exp.getResultListener()).setUsername(dbd.getUsername());
    ((DatabaseResultListener)m_Exp.getResultListener()).setPassword(dbd.getPassword());
    ((DatabaseResultListener)m_Exp.getResultListener()).setDatabaseURL(dbd.getURL());
    ((DatabaseResultListener)m_Exp.getResultListener()).setDebug(dbd.getDebug());
    m_ResultsDestinationPathTField.setText(dbd.getURL());
  }
  


  private void chooseDestinationFile()
  {
    FileFilter fileFilter = null;
    if (m_ResultsDestinationCBox.getSelectedItem() == DEST_CSV_TEXT) {
      fileFilter = m_csvFileFilter;
    } else {
      fileFilter = m_arffFileFilter;
    }
    m_DestFileChooser.setFileFilter(fileFilter);
    int returnVal = m_DestFileChooser.showSaveDialog(this);
    if (returnVal != 0) {
      return;
    }
    m_ResultsDestinationPathTField.setText(m_DestFileChooser.getSelectedFile().toString());
  }
}
