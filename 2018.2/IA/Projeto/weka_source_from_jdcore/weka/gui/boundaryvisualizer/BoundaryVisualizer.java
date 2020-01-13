package weka.gui.boundaryvisualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.logging.Logger;
import weka.core.logging.Logger.Level;
import weka.gui.ExtensionFileFilter;
import weka.gui.GenericObjectEditor;
import weka.gui.PropertyPanel;
import weka.gui.PropertySheetPanel;
import weka.gui.visualize.ClassPanel;


















































public class BoundaryVisualizer
  extends JPanel
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = 3933877580074013208L;
  
  private class AxisPanel
    extends JPanel
  {
    private static final long serialVersionUID = -7421022416674492712L;
    private static final int MAX_PRECISION = 10;
    private boolean m_vertical = false;
    private final int PAD = 5;
    private FontMetrics m_fontMetrics;
    private int m_fontHeight;
    
    public AxisPanel(boolean vertical) {
      m_vertical = vertical;
      setBackground(Color.black);
      
      String fontFamily = getFont().getFamily();
      Font newFont = new Font(fontFamily, 0, 10);
      setFont(newFont);
    }
    
    public Dimension getPreferredSize() {
      if (m_fontMetrics == null) {
        Graphics g = getGraphics();
        m_fontMetrics = g.getFontMetrics();
        m_fontHeight = m_fontMetrics.getHeight();
      }
      if (!m_vertical) {
        return new Dimension(getSizewidth, 7 + m_fontHeight);
      }
      return new Dimension(50, getSizeheight);
    }
    
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      setBackground(Color.black);
      if (m_fontMetrics == null) {
        m_fontMetrics = g.getFontMetrics();
        m_fontHeight = m_fontMetrics.getHeight();
      }
      
      Dimension d = getSize();
      Dimension d2 = m_boundaryPanel.getSize();
      g.setColor(Color.gray);
      int hf = m_fontMetrics.getAscent();
      if (!m_vertical) {
        g.drawLine(width, 5, width - width, 5);
        
        if (getInstances() != null) {
          int precisionXmax = 1;
          int precisionXmin = 1;
          int whole = (int)Math.abs(m_maxX);
          double decimal = Math.abs(m_maxX) - whole;
          
          int nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
          


          precisionXmax = decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(m_maxX)) / Math.log(10.0D)) + 2 : 1;
          


          if (precisionXmax > 10) {
            precisionXmax = 1;
          }
          String maxStringX = Utils.doubleToString(m_maxX, nondecimal + 1 + precisionXmax, precisionXmax);
          


          whole = (int)Math.abs(m_minX);
          decimal = Math.abs(m_minX) - whole;
          nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
          

          precisionXmin = decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(m_minX)) / Math.log(10.0D)) + 2 : 1;
          


          if (precisionXmin > 10) {
            precisionXmin = 1;
          }
          
          String minStringX = Utils.doubleToString(m_minX, nondecimal + 1 + precisionXmin, precisionXmin);
          

          g.drawString(minStringX, width - width, 5 + hf + 2);
          int maxWidth = m_fontMetrics.stringWidth(maxStringX);
          g.drawString(maxStringX, width - maxWidth, 5 + hf + 2);
        }
      } else {
        g.drawLine(width - 5, 0, width - 5, height);
        
        if (getInstances() != null) {
          int precisionYmax = 1;
          int precisionYmin = 1;
          int whole = (int)Math.abs(m_maxY);
          double decimal = Math.abs(m_maxY) - whole;
          
          int nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
          


          precisionYmax = decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(m_maxY)) / Math.log(10.0D)) + 2 : 1;
          


          if (precisionYmax > 10) {
            precisionYmax = 1;
          }
          String maxStringY = Utils.doubleToString(m_maxY, nondecimal + 1 + precisionYmax, precisionYmax);
          


          whole = (int)Math.abs(m_minY);
          decimal = Math.abs(m_minY) - whole;
          nondecimal = whole > 0 ? (int)(Math.log(whole) / Math.log(10.0D)) : 1;
          

          precisionYmin = decimal > 0.0D ? (int)Math.abs(Math.log(Math.abs(m_minY)) / Math.log(10.0D)) + 2 : 1;
          


          if (precisionYmin > 10) {
            precisionYmin = 1;
          }
          
          String minStringY = Utils.doubleToString(m_minY, nondecimal + 1 + precisionYmin, precisionYmin);
          

          int maxWidth = m_fontMetrics.stringWidth(minStringY);
          g.drawString(minStringY, width - 5 - maxWidth - 2, height);
          maxWidth = m_fontMetrics.stringWidth(maxStringY);
          g.drawString(maxStringY, width - 5 - maxWidth - 2, hf);
        }
      }
    }
  }
  

  protected static int m_WindowCount = 0;
  

  protected static boolean m_ExitIfNoWindowsOpen = true;
  

  private Instances m_trainingInstances;
  

  private Classifier m_classifier;
  

  protected int m_plotAreaWidth = 384;
  protected int m_plotAreaHeight = 384;
  


  protected BoundaryPanel m_boundaryPanel;
  

  protected JComboBox m_classAttBox = new JComboBox();
  protected JComboBox m_xAttBox = new JComboBox();
  protected JComboBox m_yAttBox = new JComboBox();
  
  protected Dimension COMBO_SIZE = new Dimension((int)(m_plotAreaWidth * 0.75D), m_classAttBox.getPreferredSize().height);
  
  protected JButton m_startBut;
  
  protected JCheckBox m_plotTrainingData;
  
  protected JPanel m_controlPanel;
  
  protected ClassPanel m_classPanel;
  
  private AxisPanel m_xAxisPanel;
  
  private AxisPanel m_yAxisPanel;
  
  private double m_maxX;
  
  private double m_maxY;
  
  private double m_minX;
  
  private double m_minY;
  
  private int m_xIndex;
  
  private int m_yIndex;
  
  private KDDataGenerator m_dataGenerator;
  
  private int m_numberOfSamplesFromEachRegion;
  
  private int m_generatorSamplesBase;
  
  private int m_kernelBandwidth;
  
  private JTextField m_regionSamplesText;
  
  private JTextField m_generatorSamplesText;
  
  private JTextField m_kernelBandwidthText;
  
  protected GenericObjectEditor m_classifierEditor;
  
  protected PropertyPanel m_ClassifierPanel;
  
  protected JFileChooser m_FileChooser;
  
  protected ExtensionFileFilter m_arffFileFilter;
  
  protected JLabel dataFileLabel;
  
  protected JPanel m_addRemovePointsPanel;
  
  protected JComboBox m_classValueSelector;
  
  protected JRadioButton m_addPointsButton;
  
  protected JRadioButton m_removePointsButton;
  
  protected ButtonGroup m_addRemovePointsButtonGroup;
  
  protected JButton removeAllButton;
  
  protected JButton chooseButton;
  
  static
  {
    GenericObjectEditor.registerEditors();
  }
  




  public String globalInfo()
  {
    Messages.getInstance();return Messages.getString("BoundaryVisualizer_GlobalInfo_Text") + getTechnicalInformation().toString();
  }
  









  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    Messages.getInstance();result.setValue(TechnicalInformation.Field.AUTHOR, Messages.getString("BoundaryVisualizer_GetTechnicalInformation_FieldAUTHOR"));
    Messages.getInstance();result.setValue(TechnicalInformation.Field.TITLE, Messages.getString("BoundaryVisualizer_GetTechnicalInformation_FieldTITLE"));
    Messages.getInstance();result.setValue(TechnicalInformation.Field.BOOKTITLE, Messages.getString("BoundaryVisualizer_GetTechnicalInformation_FieldBOOKTITLE"));
    Messages.getInstance();result.setValue(TechnicalInformation.Field.YEAR, Messages.getString("BoundaryVisualizer_GetTechnicalInformation_FieldYEAR"));
    Messages.getInstance();result.setValue(TechnicalInformation.Field.PAGES, Messages.getString("BoundaryVisualizer_GetTechnicalInformation_FieldPAGES"));
    Messages.getInstance();result.setValue(TechnicalInformation.Field.PUBLISHER, Messages.getString("BoundaryVisualizer_GetTechnicalInformation_FieldPUBLISHER"));
    Messages.getInstance();result.setValue(TechnicalInformation.Field.ADDRESS, Messages.getString("BoundaryVisualizer_GetTechnicalInformation_FieldADDRESS"));
    
    return result;
  }
  
  public BoundaryVisualizer()
  {
    Messages.getInstance();m_startBut = new JButton(Messages.getString("BoundaryVisualizer_Start_JButton_Text"));
    
    Messages.getInstance();m_plotTrainingData = new JCheckBox(Messages.getString("BoundaryVisualizer_PlotTrainingData_JCheckBox_Text"));
    


    m_classPanel = new ClassPanel();
    

























    m_regionSamplesText = new JTextField("0");
    

    m_generatorSamplesText = new JTextField("0");
    

    m_kernelBandwidthText = new JTextField("3  ");
    


    m_classifierEditor = new GenericObjectEditor();
    m_ClassifierPanel = new PropertyPanel(m_classifierEditor);
    
    m_FileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
    
    Messages.getInstance();m_arffFileFilter = new ExtensionFileFilter(".arff", Messages.getString("BoundaryVisualizer_ExtensionFileFilter_Text"));
    

    dataFileLabel = new JLabel();
    m_addRemovePointsPanel = new JPanel();
    m_classValueSelector = new JComboBox();
    m_addPointsButton = new JRadioButton();
    m_removePointsButton = new JRadioButton();
    m_addRemovePointsButtonGroup = new ButtonGroup();
    Messages.getInstance();removeAllButton = new JButton(Messages.getString("BoundaryVisualizer_RemoveAll_JButton_Text"));
    Messages.getInstance();chooseButton = new JButton(Messages.getString("BoundaryVisualizer_Choose_JButton_Text"));
    










































    setLayout(new BorderLayout());
    m_classAttBox.setMinimumSize(COMBO_SIZE);
    m_classAttBox.setPreferredSize(COMBO_SIZE);
    m_classAttBox.setMaximumSize(COMBO_SIZE);
    m_classAttBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_classAttBox.getItemCount() != 0)
        {
          try {
            m_classPanel.setCindex(m_classAttBox.getSelectedIndex());
            plotTrainingData();
            Messages.getInstance();System.err.println(Messages.getString("BoundaryVisualizer_SetLayout_Error_Text"));
          } catch (Exception ex) { ex.printStackTrace();
          }
          
          BoundaryVisualizer.this.setUpClassValueSelectorCB();
        }
        
      }
    });
    m_xAttBox.setMinimumSize(COMBO_SIZE);
    m_xAttBox.setPreferredSize(COMBO_SIZE);
    m_xAttBox.setMaximumSize(COMBO_SIZE);
    
    m_yAttBox.setMinimumSize(COMBO_SIZE);
    m_yAttBox.setPreferredSize(COMBO_SIZE);
    m_yAttBox.setMaximumSize(COMBO_SIZE);
    
    m_classPanel.setMinimumSize(new Dimension((int)COMBO_SIZE.getWidth() * 2, (int)COMBO_SIZE.getHeight() * 2));
    

    m_classPanel.setPreferredSize(new Dimension((int)COMBO_SIZE.getWidth() * 2, (int)COMBO_SIZE.getHeight() * 2));
    



    m_controlPanel = new JPanel();
    m_controlPanel.setLayout(new BorderLayout());
    

    JPanel dataChooseHolder = new JPanel(new BorderLayout());
    Messages.getInstance();dataChooseHolder.setBorder(BorderFactory.createTitledBorder(Messages.getString("BoundaryVisualizer_DataChooseHolder_JPanel_Text")));
    dataChooseHolder.add(dataFileLabel, "West");
    
    m_FileChooser.setFileSelectionMode(0);
    m_FileChooser.addChoosableFileFilter(m_arffFileFilter);
    chooseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          setInstancesFromFileQ();
          int classIndex = m_classAttBox.getSelectedIndex();
          if ((m_trainingInstances != null) && (m_classifier != null) && (m_trainingInstances.attribute(classIndex).isNominal())) {
            m_startBut.setEnabled(true);
          }
          
        }
        catch (Exception ex)
        {
          ex.printStackTrace(System.out);
          System.err.println("exception");
        }
        
      }
    });
    dataChooseHolder.add(chooseButton, "East");
    
    JPanel classifierHolder = new JPanel();
    Messages.getInstance();classifierHolder.setBorder(BorderFactory.createTitledBorder(Messages.getString("BoundaryVisualizer_ClassifierHolder_JPanel_Text")));
    classifierHolder.setLayout(new BorderLayout());
    m_classifierEditor.setClassType(Classifier.class);
    
    m_classifierEditor.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        m_classifier = ((Classifier)m_classifierEditor.getValue());
        try {
          int classIndex = m_classAttBox.getSelectedIndex();
          if ((m_trainingInstances != null) && (m_classifier != null) && (m_trainingInstances.attribute(classIndex).isNominal())) {
            m_startBut.setEnabled(true);
          }
        } catch (Exception ex) {}
      }
    });
    classifierHolder.add(m_ClassifierPanel, "Center");
    


    JPanel cHolder = new JPanel();
    Messages.getInstance();cHolder.setBorder(BorderFactory.createTitledBorder(Messages.getString("BoundaryVisualizer_CHolder_JPanel_Text")));
    cHolder.add(m_classAttBox);
    
    JPanel vAttHolder = new JPanel();
    vAttHolder.setLayout(new GridLayout(2, 1));
    Messages.getInstance();vAttHolder.setBorder(BorderFactory.createTitledBorder(Messages.getString("BoundaryVisualizer_VAttHolder_JPanel_Text")));
    
    vAttHolder.add(m_xAttBox);
    vAttHolder.add(m_yAttBox);
    
    JPanel colOne = new JPanel();
    colOne.setLayout(new BorderLayout());
    colOne.add(dataChooseHolder, "North");
    colOne.add(cHolder, "Center");
    

    JPanel tempPanel = new JPanel();
    Messages.getInstance();tempPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("BoundaryVisualizer_TempPanel_JPanel_Text")));
    
    tempPanel.setLayout(new GridLayout(3, 1));
    
    JPanel colTwo = new JPanel();
    colTwo.setLayout(new BorderLayout());
    JPanel gsP = new JPanel();gsP.setLayout(new BorderLayout());
    Messages.getInstance();gsP.add(new JLabel(Messages.getString("BoundaryVisualizer_ColTwo_JPanel_Text")), "Center");
    gsP.add(m_generatorSamplesText, "West");
    tempPanel.add(gsP);
    
    JPanel rsP = new JPanel();rsP.setLayout(new BorderLayout());
    Messages.getInstance();rsP.add(new JLabel(Messages.getString("BoundaryVisualizer_RsP_JPanel_Text")), "Center");
    rsP.add(m_regionSamplesText, "West");
    tempPanel.add(rsP);
    
    JPanel ksP = new JPanel();ksP.setLayout(new BorderLayout());
    Messages.getInstance();ksP.add(new JLabel(Messages.getString("BoundaryVisualizer_KsP_JPanel_Text")), "Center");
    ksP.add(m_kernelBandwidthText, "West");
    tempPanel.add(ksP);
    
    colTwo.add(classifierHolder, "North");
    
    colTwo.add(vAttHolder, "Center");
    
    JPanel startPanel = new JPanel();
    Messages.getInstance();startPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("BoundaryVisualizer_StartPanel_JPanel_Text")));
    
    startPanel.setLayout(new BorderLayout());
    startPanel.add(m_startBut, "Center");
    startPanel.add(m_plotTrainingData, "West");
    


    m_controlPanel.add(colOne, "West");
    m_controlPanel.add(colTwo, "Center");
    JPanel classHolder = new JPanel();
    classHolder.setLayout(new BorderLayout());
    Messages.getInstance();classHolder.setBorder(BorderFactory.createTitledBorder(Messages.getString("BoundaryVisualizer_ClassHolder_JPanel_Text")));
    classHolder.add(m_classPanel, "Center");
    m_controlPanel.add(classHolder, "South");
    
    JPanel aboutAndControlP = new JPanel();
    aboutAndControlP.setLayout(new BorderLayout());
    aboutAndControlP.add(m_controlPanel, "South");
    
    PropertySheetPanel psp = new PropertySheetPanel();
    psp.setTarget(this);
    JPanel aboutPanel = psp.getAboutPanel();
    
    aboutAndControlP.add(aboutPanel, "North");
    
    add(aboutAndControlP, "North");
    



    Messages.getInstance();m_addRemovePointsPanel.setBorder(BorderFactory.createTitledBorder(Messages.getString("BoundaryVisualizer_AddRemovePointsPanel_JPanel_Text")));
    m_addRemovePointsPanel.setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    weightx = 1.0D;
    weighty = 1.0D;
    gridx = 0;
    gridy = 0;
    fill = 1;
    m_addRemovePointsPanel.add(m_addPointsButton);
    gridx = 1;
    Messages.getInstance();m_addRemovePointsPanel.add(new JLabel(Messages.getString("BoundaryVisualizer_AddRemovePointsPanel_JLabel_Text_First")), constraints);
    gridx = 2;
    m_addRemovePointsPanel.add(m_classValueSelector);
    gridx = 0;
    gridy = 1;
    m_addRemovePointsPanel.add(m_removePointsButton, constraints);
    gridx = 1;
    Messages.getInstance();m_addRemovePointsPanel.add(new JLabel(Messages.getString("BoundaryVisualizer_AddRemovePointsPanel_JLabel_Text_Second")), constraints);
    

    removeAllButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (m_trainingInstances != null)
        {
          Messages.getInstance(); if (m_startBut.getText().equals(Messages.getString("BoundaryVisualizer_AddRemovePointsPanel_ActionPerformed_StartBut_Text_Stop_First")))
            return;
          m_boundaryPanel.removeAllInstances();
          BoundaryVisualizer.this.computeBounds();
          m_xAxisPanel.repaint(0L, 0, 0, m_xAxisPanel.getWidth(), m_xAxisPanel.getHeight());
          m_yAxisPanel.repaint(0L, 0, 0, m_yAxisPanel.getWidth(), m_yAxisPanel.getHeight());
          try {
            m_boundaryPanel.plotTrainingData();
          } catch (Exception ex) {}
        }
      } });
    gridx = 2;
    m_addRemovePointsPanel.add(removeAllButton, constraints);
    





    m_addRemovePointsButtonGroup.add(m_addPointsButton);
    m_addRemovePointsButtonGroup.add(m_removePointsButton);
    m_addPointsButton.setSelected(true);
    



    m_boundaryPanel = new BoundaryPanel(m_plotAreaWidth, m_plotAreaHeight);
    m_numberOfSamplesFromEachRegion = m_boundaryPanel.getNumSamplesPerRegion();
    m_regionSamplesText.setText("" + m_numberOfSamplesFromEachRegion + "  ");
    m_generatorSamplesBase = ((int)m_boundaryPanel.getGeneratorSamplesBase());
    m_generatorSamplesText.setText("" + m_generatorSamplesBase + "  ");
    
    m_dataGenerator = new KDDataGenerator();
    m_kernelBandwidth = m_dataGenerator.getKernelBandwidth();
    m_kernelBandwidthText.setText("" + m_kernelBandwidth + "  ");
    m_boundaryPanel.setDataGenerator(m_dataGenerator);
    

    JPanel gfxPanel = new JPanel();
    gfxPanel.setLayout(new BorderLayout());
    gfxPanel.setBorder(BorderFactory.createEtchedBorder());
    


    gfxPanel.add(m_boundaryPanel, "Center");
    m_xAxisPanel = new AxisPanel(false);
    gfxPanel.add(m_xAxisPanel, "South");
    m_yAxisPanel = new AxisPanel(true);
    gfxPanel.add(m_yAxisPanel, "West");
    
    JPanel containerPanel = new JPanel();
    containerPanel.setLayout(new BorderLayout());
    containerPanel.add(gfxPanel, "Center");
    add(containerPanel, "West");
    
    JPanel rightHandToolsPanel = new JPanel();
    rightHandToolsPanel.setLayout(new BoxLayout(rightHandToolsPanel, 3));
    
    rightHandToolsPanel.add(m_addRemovePointsPanel);
    
    Messages.getInstance();JButton newWindowButton = new JButton(Messages.getString("BoundaryVisualizer_AddRemovePointsPanel_NewWindowButton_JButton_Text"));
    

    newWindowButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Instances newTrainingData = null;
          Classifier newClassifier = null;
          if (m_trainingInstances != null)
            newTrainingData = new Instances(m_trainingInstances);
          if (m_classifier != null)
            newClassifier = Classifier.makeCopy(m_classifier);
          BoundaryVisualizer.createNewVisualizerWindow(newClassifier, newTrainingData);
        } catch (Exception ex) { ex.printStackTrace();
        }
      } });
    JPanel newWindowHolder = new JPanel();
    newWindowHolder.add(newWindowButton);
    rightHandToolsPanel.add(newWindowHolder);
    rightHandToolsPanel.add(tempPanel);
    rightHandToolsPanel.add(startPanel);
    
    containerPanel.add(rightHandToolsPanel, "East");
    







    m_startBut.setEnabled(false);
    m_startBut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Messages.getInstance(); if (m_startBut.getText().equals(Messages.getString("BoundaryVisualizer_AddRemovePointsPanel_ActionPerformed_StartBut_Text_Start_First"))) {
          if ((m_trainingInstances != null) && (m_classifier != null)) {
            try
            {
              int BPSuccessCode = setUpBoundaryPanel();
              
              if (BPSuccessCode == 1) {
                Messages.getInstance();JOptionPane.showMessageDialog(null, Messages.getString("BoundaryVisualizer_AddRemovePointsPanel_ActionPerformed_JOptionPaneShowMessageDialog_Text_First"));
              } else if (BPSuccessCode == 2) {
                Messages.getInstance();JOptionPane.showMessageDialog(null, Messages.getString("BoundaryVisualizer_AddRemovePointsPanel_ActionPerformed_JOptionPaneShowMessageDialog_Text_Second"));
              } else {
                m_boundaryPanel.start();
                Messages.getInstance();m_startBut.setText(Messages.getString("BoundaryVisualizer_AddRemovePointsPanel_ActionPerformed_StartBut_Text_Stop_Second"));
                BoundaryVisualizer.this.setControlEnabledStatus(false);
              }
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        } else {
          m_boundaryPanel.stopPlotting();
          Messages.getInstance();m_startBut.setText(Messages.getString("BoundaryVisualizer_AddRemovePointsPanel_ActionPerformed_StartBut_Text_Start_Second"));
          BoundaryVisualizer.this.setControlEnabledStatus(true);
        }
        
      }
    });
    m_boundaryPanel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Messages.getInstance();m_startBut.setText(Messages.getString("BoundaryVisualizer_AddRemovePointsPanel_ActionPerformed_StartBut_Text_Start_Third"));
        BoundaryVisualizer.this.setControlEnabledStatus(true);
      }
      
    });
    m_classPanel.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        try {
          FastVector colors = m_boundaryPanel.getColors();
          FileOutputStream fos = new FileOutputStream("colors.ser");
          ObjectOutputStream oos = new ObjectOutputStream(fos);
          oos.writeObject(colors);
          oos.flush();
          oos.close();
        }
        catch (Exception ex) {}
        m_boundaryPanel.replot();

      }
      

    });
    m_boundaryPanel.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e) {
        if (m_trainingInstances != null) {
          Messages.getInstance(); if (m_startBut.getText().equals(Messages.getString("BoundaryVisualizer_BoundaryPanel_MouseClicked_StartBut_Stop_Text"))) {
            return;
          }
          if (m_addPointsButton.isSelected()) {
            double classVal = 0.0D;
            boolean validInput = true;
            if (m_trainingInstances.attribute(m_classAttBox.getSelectedIndex()).isNominal()) {
              classVal = m_classValueSelector.getSelectedIndex();
            } else {
              String indexStr = "";
              try {
                indexStr = (String)m_classValueSelector.getSelectedItem();
                classVal = Double.parseDouble(indexStr);
              } catch (Exception ex) {
                if (indexStr == null) indexStr = "";
                Messages.getInstance();Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(null, Messages.getString("BoundaryVisualizer_BoundaryPanel_MouseClicked_JOptionPaneShowMessageDialog_Text_First") + indexStr + Messages.getString("BoundaryVisualizer_BoundaryPanel_MouseClicked_JOptionPaneShowMessageDialog_Text_Second") + Messages.getString("BoundaryVisualizer_BoundaryPanel_MouseClicked_JOptionPaneShowMessageDialog_Text_Third"));
                
                validInput = false;
              }
            }
            
            if (validInput) {
              m_boundaryPanel.addTrainingInstanceFromMouseLocation(e.getX(), e.getY(), m_classAttBox.getSelectedIndex(), classVal);
            }
          } else {
            m_boundaryPanel.removeTrainingInstanceFromMouseLocation(e.getX(), e.getY());
          }
          try { plotTrainingData(); } catch (Exception ex) {}
          m_xAxisPanel.repaint(0L, 0, 0, m_xAxisPanel.getWidth(), m_xAxisPanel.getHeight());
          m_yAxisPanel.repaint(0L, 0, 0, m_yAxisPanel.getWidth(), m_yAxisPanel.getHeight());
        }
      }
    });
  }
  




  private void setControlEnabledStatus(boolean status)
  {
    m_classAttBox.setEnabled(status);
    m_xAttBox.setEnabled(status);
    m_yAttBox.setEnabled(status);
    m_regionSamplesText.setEnabled(status);
    m_generatorSamplesText.setEnabled(status);
    m_kernelBandwidthText.setEnabled(status);
    m_plotTrainingData.setEnabled(status);
    removeAllButton.setEnabled(status);
    m_classValueSelector.setEnabled(status);
    m_addPointsButton.setEnabled(status);
    m_removePointsButton.setEnabled(status);
    m_FileChooser.setEnabled(status);
    chooseButton.setEnabled(status);
  }
  





  public void setClassifier(Classifier newClassifier)
    throws Exception
  {
    m_classifier = newClassifier;
    try
    {
      int classIndex = m_classAttBox.getSelectedIndex();
      
      if ((m_classifier != null) && (m_trainingInstances != null) && (m_trainingInstances.attribute(classIndex).isNominal()))
      {
        m_startBut.setEnabled(true);
      }
      else {
        m_startBut.setEnabled(false);
      }
    }
    catch (Exception e) {}
  }
  


  private void computeBounds()
  {
    m_boundaryPanel.computeMinMaxAtts();
    
    String xName = (String)m_xAttBox.getSelectedItem();
    if (xName == null) {
      return;
    }
    Messages.getInstance();xName = Utils.removeSubstring(xName, Messages.getString("BoundaryVisualizer_ComputeBounds_XName_Substring_Text_First") + " ");
    Messages.getInstance();xName = Utils.removeSubstring(xName, " " + Messages.getString("BoundaryVisualizer_ComputeBounds_XName_Substring_Text_Second"));
    
    String yName = (String)m_yAttBox.getSelectedItem();
    Messages.getInstance();yName = Utils.removeSubstring(yName, Messages.getString("BoundaryVisualizer_ComputeBounds_YName_Substring_Text_First") + " ");
    Messages.getInstance();yName = Utils.removeSubstring(yName, " " + Messages.getString("BoundaryVisualizer_ComputeBounds_YName_Substring_Text_Second"));
    
    m_xIndex = -1;
    m_yIndex = -1;
    for (int i = 0; i < m_trainingInstances.numAttributes(); i++) {
      if (m_trainingInstances.attribute(i).name().equals(xName)) {
        m_xIndex = i;
      }
      if (m_trainingInstances.attribute(i).name().equals(yName)) {
        m_yIndex = i;
      }
    }
    
    m_minX = m_boundaryPanel.getMinXBound();
    m_minY = m_boundaryPanel.getMinYBound();
    m_maxX = m_boundaryPanel.getMaxXBound();
    m_maxY = m_boundaryPanel.getMaxYBound();
    
    m_xAxisPanel.repaint(0L, 0, 0, m_xAxisPanel.getWidth(), m_xAxisPanel.getHeight());
    m_yAxisPanel.repaint(0L, 0, 0, m_yAxisPanel.getWidth(), m_yAxisPanel.getHeight());
  }
  




  public Instances getInstances()
  {
    return m_trainingInstances;
  }
  



  public void setInstances(Instances inst)
    throws Exception
  {
    if (inst == null) {
      m_trainingInstances = inst;
      m_classPanel.setInstances(m_trainingInstances);
      return;
    }
    

    int numCount = 0;
    for (int i = 0; i < inst.numAttributes(); i++) {
      if (inst.attribute(i).isNumeric()) {
        numCount++;
      }
    }
    
    if (numCount < 2) {
      Messages.getInstance();JOptionPane.showMessageDialog(null, Messages.getString("BoundaryVisualizer_ComputeBounds_JOptionPaneShowMessageDialog_Text"));
      return;
    }
    
    m_trainingInstances = inst;
    m_classPanel.setInstances(m_trainingInstances);
    
    String[] classAttNames = new String[m_trainingInstances.numAttributes()];
    Vector xAttNames = new Vector();
    Vector yAttNames = new Vector();
    
    for (int i = 0; i < m_trainingInstances.numAttributes(); i++) {
      classAttNames[i] = m_trainingInstances.attribute(i).name();
      String type = "";
      switch (m_trainingInstances.attribute(i).type()) {
      case 1: 
        Messages.getInstance();type = Messages.getString("BoundaryVisualizer_SetInstances_AttributeNOMINAL_Text");
        break;
      case 0: 
        Messages.getInstance();type = Messages.getString("BoundaryVisualizer_SetInstances_AttributeNUMERIC_Text");
        break;
      case 2: 
        Messages.getInstance();type = Messages.getString("BoundaryVisualizer_SetInstances_AttributeSTRING_Text");
        break;
      case 3: 
        Messages.getInstance();type = Messages.getString("BoundaryVisualizer_SetInstances_AttributeDATE_Text");
        break;
      case 4: 
        Messages.getInstance();type = Messages.getString("BoundaryVisualizer_SetInstances_AttributeRELATIONAL_Text");
        break;
      default: 
        Messages.getInstance();type = Messages.getString("BoundaryVisualizer_SetInstances_AttributeDEFAULT_Text");
      }
      int tmp292_290 = i; String[] tmp292_289 = classAttNames;tmp292_289[tmp292_290] = (tmp292_289[tmp292_290] + " " + type);
      if (m_trainingInstances.attribute(i).isNumeric()) {
        Messages.getInstance();xAttNames.addElement(Messages.getString("BoundaryVisualizer_ComputeBounds_XAttNames_Text") + classAttNames[i]);
        Messages.getInstance();yAttNames.addElement(Messages.getString("BoundaryVisualizer_ComputeBounds_YAttNames_Text") + classAttNames[i]);
      }
    }
    
    m_classAttBox.setModel(new DefaultComboBoxModel(classAttNames));
    m_xAttBox.setModel(new DefaultComboBoxModel(xAttNames));
    m_yAttBox.setModel(new DefaultComboBoxModel(yAttNames));
    if (xAttNames.size() > 1) {
      m_yAttBox.setSelectedIndex(1);
    }
    
    m_classAttBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        BoundaryVisualizer.this.configureForClassAttribute();
      }
      
    });
    m_xAttBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == 1)
        {






          BoundaryVisualizer.this.computeBounds();
          repaint();
          try { plotTrainingData(); } catch (Exception ex) { ex.printStackTrace();
          }
        }
      }
    });
    m_yAttBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == 1)
        {






          BoundaryVisualizer.this.computeBounds();
          repaint();
          try { plotTrainingData(); } catch (Exception ex) { ex.printStackTrace();
          }
        }
      }
    });
    if (classAttNames.length > 0) {
      m_classAttBox.setSelectedIndex(classAttNames.length - 1);
    }
    
    setUpClassValueSelectorCB();
    
    configureForClassAttribute();
    
    m_classPanel.setCindex(m_classAttBox.getSelectedIndex());
    plotTrainingData();
    computeBounds();
    revalidate();
    repaint();
    
    if ((getTopLevelAncestor() instanceof Window)) {
      ((Window)getTopLevelAncestor()).pack();
    }
  }
  

  private void setUpClassValueSelectorCB()
  {
    m_classValueSelector.removeAllItems();
    int classAttribute = m_classAttBox.getSelectedIndex();
    
    m_trainingInstances.setClassIndex(classAttribute);
    if (m_trainingInstances.attribute(classAttribute).isNominal()) {
      m_classValueSelector.setEditable(false);
      for (int i = 0; i < m_trainingInstances.numClasses(); i++)
        m_classValueSelector.insertItemAt(m_trainingInstances.attribute(classAttribute).value(i), i);
      m_classValueSelector.setSelectedIndex(0);
    }
    else {
      m_classValueSelector.setEditable(true);
    }
  }
  


  private void configureForClassAttribute()
  {
    int classIndex = m_classAttBox.getSelectedIndex();
    if (classIndex >= 0)
    {
      if ((!m_trainingInstances.attribute(classIndex).isNominal()) || (m_classifier == null)) {
        m_startBut.setEnabled(false);
      } else {
        m_startBut.setEnabled(true);
      }
      
      FastVector colors = new FastVector();
      if (!m_trainingInstances.attribute(m_classAttBox.getSelectedIndex()).isNominal())
      {
        for (int i = 0; i < BoundaryPanel.DEFAULT_COLORS.length; i++) {
          colors.addElement(BoundaryPanel.DEFAULT_COLORS[i]);
        }
      } else {
        for (int i = 0; i < m_trainingInstances.attribute(classIndex).numValues(); 
            i++) {
          colors.addElement(BoundaryPanel.DEFAULT_COLORS[(i % BoundaryPanel.DEFAULT_COLORS.length)]);
        }
      }
      


      m_classPanel.setColours(colors);
      m_boundaryPanel.setColors(colors);
    }
  }
  







  public void setInstancesFromFileQ()
  {
    int returnVal = m_FileChooser.showOpenDialog(this);
    if (returnVal == 0) {
      File selected = m_FileChooser.getSelectedFile();
      
      try
      {
        Reader r = new BufferedReader(new FileReader(selected));
        
        Instances i = new Instances(r);
        setInstances(i);
        

        String relationName = i.relationName();
        String truncatedN = relationName;
        if (relationName.length() > 25) {
          truncatedN = relationName.substring(0, 25) + "...";
        }
        dataFileLabel.setText(truncatedN);
        dataFileLabel.setToolTipText(relationName);
      }
      catch (Exception e) {
        Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(this, Messages.getString("BoundaryVisualizer_SetInstancesFromFileQ_JOptionPaneShowMessageDialog_Text"), Messages.getString("BoundaryVisualizer_SetInstancesFromFileQ_JOptionPaneShowMessageDialog_Text_First"), 2);
        

        e.printStackTrace();
      }
    }
  }
  





  public int setUpBoundaryPanel()
    throws Exception
  {
    int returner = 0;
    int tempSamples = m_numberOfSamplesFromEachRegion;
    try {
      tempSamples = Integer.parseInt(m_regionSamplesText.getText().trim());
    }
    catch (Exception ex) {
      m_regionSamplesText.setText("" + tempSamples);
    }
    m_numberOfSamplesFromEachRegion = tempSamples;
    m_boundaryPanel.setNumSamplesPerRegion(tempSamples);
    

    tempSamples = m_generatorSamplesBase;
    try {
      tempSamples = Integer.parseInt(m_generatorSamplesText.getText().trim());
    }
    catch (Exception ex) {
      m_generatorSamplesText.setText("" + tempSamples);
    }
    m_generatorSamplesBase = tempSamples;
    m_boundaryPanel.setGeneratorSamplesBase(tempSamples);
    
    tempSamples = m_kernelBandwidth;
    try {
      tempSamples = Integer.parseInt(m_kernelBandwidthText.getText().trim());
    }
    catch (Exception ex) {
      m_kernelBandwidthText.setText("" + tempSamples);
    }
    m_kernelBandwidth = tempSamples;
    m_dataGenerator.setKernelBandwidth(tempSamples);
    
    if (m_kernelBandwidth < 0) returner = 1;
    if (m_kernelBandwidth >= m_trainingInstances.numInstances()) { returner = 2;
    }
    m_trainingInstances.setClassIndex(m_classAttBox.getSelectedIndex());
    
    m_boundaryPanel.setClassifier(m_classifier);
    m_boundaryPanel.setTrainingData(m_trainingInstances);
    m_boundaryPanel.setXAttribute(m_xIndex);
    m_boundaryPanel.setYAttribute(m_yIndex);
    m_boundaryPanel.setPlotTrainingData(m_plotTrainingData.isSelected());
    

    return returner;
  }
  

  public void plotTrainingData()
    throws Exception
  {
    m_boundaryPanel.initialize();
    setUpBoundaryPanel();
    computeBounds();
    m_boundaryPanel.plotTrainingData();
  }
  

  public void stopPlotting()
  {
    m_boundaryPanel.stopPlotting();
  }
  





  public static void setExitIfNoWindowsOpen(boolean value)
  {
    m_ExitIfNoWindowsOpen = value;
  }
  





  public static boolean getExitIfNoWindowsOpen()
  {
    return m_ExitIfNoWindowsOpen;
  }
  


  public static void createNewVisualizerWindow(Classifier classifier, Instances instances)
    throws Exception
  {
    m_WindowCount += 1;
    
    Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("BoundaryVisualizer_CreateNewVisualizerWindow_Title_Text"));
    
    jf.getContentPane().setLayout(new BorderLayout());
    BoundaryVisualizer bv = new BoundaryVisualizer();
    jf.getContentPane().add(bv, "Center");
    jf.setSize(bv.getMinimumSize());
    jf.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        BoundaryVisualizer.m_WindowCount -= 1;
        val$bv.stopPlotting();
        jf.dispose();
        if ((BoundaryVisualizer.m_WindowCount == 0) && (BoundaryVisualizer.m_ExitIfNoWindowsOpen)) {
          System.exit(0);
        }
        
      }
    });
    jf.pack();
    jf.setVisible(true);
    jf.setResizable(false);
    
    if (classifier == null) {
      bv.setClassifier(null);
    } else {
      bv.setClassifier(classifier);
      m_classifierEditor.setValue(classifier);
    }
    
    if (instances == null) {
      bv.setInstances(null);
    }
    else {
      bv.setInstances(instances);
      try
      {
        dataFileLabel.setText(instances.relationName());
        bv.plotTrainingData();
        m_classPanel.setCindex(m_classAttBox.getSelectedIndex());
        bv.repaint(0L, 0, 0, bv.getWidth(), bv.getHeight());
      }
      catch (Exception ex) {}
    }
  }
  




  public static void main(String[] args)
  {
    Messages.getInstance();Logger.log(Logger.Level.INFO, Messages.getString("BoundaryVisualizer_Main_Logger_Text"));
    try {
      if (args.length < 2) {
        createNewVisualizerWindow(null, null);
      }
      else {
        String[] argsR = null;
        if (args.length > 2) {
          argsR = new String[args.length - 2];
          for (int j = 2; j < args.length; j++) {
            argsR[(j - 2)] = args[j];
          }
        }
        Classifier c = Classifier.forName(args[1], argsR);
        
        Messages.getInstance();System.err.println(Messages.getString("BoundaryVisualizer_Main_Error_Text") + args[0]);
        Reader r = new BufferedReader(new FileReader(args[0]));
        
        Instances i = new Instances(r);
        
        createNewVisualizerWindow(c, i);
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
