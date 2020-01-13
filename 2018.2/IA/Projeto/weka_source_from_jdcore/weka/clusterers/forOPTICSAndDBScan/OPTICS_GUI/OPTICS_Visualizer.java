package weka.clusterers.forOPTICSAndDBScan.OPTICS_GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.GregorianCalendar;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import weka.core.FastVector;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.gui.LookAndFeel;












































































public class OPTICS_Visualizer
  implements RevisionHandler
{
  private SERObject serObject;
  private JFrame frame;
  private JFrame statisticsFrame;
  private JFrame helpFrame;
  private FrameListener frameListener;
  private JToolBar toolBar;
  private JButton toolBarButton_open;
  private JButton toolBarButton_save;
  private JButton toolBarButton_parameters;
  private JButton toolBarButton_help;
  private JButton toolBarButton_about;
  private JMenuBar defaultMenuBar;
  private JMenuItem open;
  private JMenuItem save;
  private JMenuItem exit;
  private JMenuItem parameters;
  private JMenuItem help;
  private JMenuItem about;
  private JTabbedPane tabbedPane;
  private JTable resultVectorTable;
  private GraphPanel graphPanel;
  private JScrollPane graphPanelScrollPane;
  private JPanel settingsPanel;
  private JCheckBox showCoreDistances;
  private JCheckBox showReachabilityDistances;
  private int verValue = 30;
  
  private JSlider verticalSlider;
  
  private JButton coreDistanceColorButton;
  
  private JButton reachDistanceColorButton;
  
  private JButton graphBackgroundColorButton;
  
  private JButton resetColorButton;
  
  private JFileChooser jFileChooser;
  
  private String lastPath;
  
  public OPTICS_Visualizer(SERObject serObject, String title)
  {
    this.serObject = serObject;
    
    LookAndFeel.setLookAndFeel();
    
    frame = new JFrame(title);
    
    frame.addWindowListener(new WindowAdapter()
    {

      public void windowClosing(WindowEvent e)
      {

        frame.dispose();
      }
      
    });
    frame.getContentPane().setLayout(new BorderLayout());
    frame.setSize(new Dimension(800, 600));
    Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle windowRectangle = frame.getBounds();
    frame.setLocation((width - width) / 2, (height - height) / 2);
    

    frameListener = new FrameListener(null);
    jFileChooser = new JFileChooser();
    jFileChooser.setFileFilter(new SERFileFilter("ser", "Java Serialized Object File (*.ser)"));
    
    createGUI();
    frame.setVisible(true);
    frame.toFront();
  }
  







  private void createGUI()
  {
    setMenuBar(constructDefaultMenuBar());
    
    frame.getContentPane().add(createToolBar(), "North");
    frame.getContentPane().add(createTabbedPane(), "Center");
    frame.getContentPane().add(createSettingsPanel(), "South");
    disableSettingsPanel();
  }
  



  private JComponent createSettingsPanel()
  {
    settingsPanel = new JPanel(new GridBagLayout());
    
    SettingsPanelListener panelListener = new SettingsPanelListener(null);
    
    JPanel setPanelLeft = new JPanel(new GridBagLayout());
    setPanelLeft.setBorder(BorderFactory.createTitledBorder(" General Settings "));
    
    JPanel checkBoxesPanel = new JPanel(new GridLayout(1, 2));
    showCoreDistances = new JCheckBox("Show Core-Distances");
    showCoreDistances.setSelected(true);
    showReachabilityDistances = new JCheckBox("Show Reachability-Distances");
    showReachabilityDistances.setSelected(true);
    showCoreDistances.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == 1) {
          graphPanel.setShowCoreDistances(true);
          graphPanel.adjustSize(serObject);
          graphPanel.repaint();
        } else if (e.getStateChange() == 2) {
          graphPanel.setShowCoreDistances(false);
          graphPanel.adjustSize(serObject);
          graphPanel.repaint();
        }
      }
    });
    showReachabilityDistances.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == 1) {
          graphPanel.setShowReachabilityDistances(true);
          graphPanel.adjustSize(serObject);
          graphPanel.repaint();
        } else if (e.getStateChange() == 2) {
          graphPanel.setShowReachabilityDistances(false);
          graphPanel.adjustSize(serObject);
          graphPanel.repaint();
        }
        
      }
    });
    checkBoxesPanel.add(showCoreDistances);
    checkBoxesPanel.add(showReachabilityDistances);
    
    JPanel verticalAdPanel = new JPanel(new BorderLayout());
    final JLabel verValueLabel = new JLabel("Vertical Adjustment: " + verValue);
    verticalAdPanel.add(verValueLabel, "North");
    verticalSlider = new JSlider(0, 0, frame.getHeight(), verValue);
    verticalSlider.setMajorTickSpacing(100);
    verticalSlider.setMinorTickSpacing(10);
    verticalSlider.setPaintTicks(true);
    verticalSlider.setPaintLabels(true);
    verticalSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        if (!verticalSlider.getValueIsAdjusting()) {
          verValue = verticalSlider.getValue();
          verValueLabel.setText("Vertical Adjustment: " + verValue);
          graphPanel.setVerticalAdjustment(verValue);
          graphPanel.repaint();
        }
      }
    });
    verticalAdPanel.add(verticalSlider, "Center");
    
    setPanelLeft.add(checkBoxesPanel, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, 10, 1, new Insets(5, 5, 5, 5), 0, 0));
    



    setPanelLeft.add(verticalAdPanel, new GridBagConstraints(0, 1, 1, 1, 1.0D, 1.0D, 10, 1, new Insets(5, 5, 5, 5), 0, 0));
    




    settingsPanel.add(setPanelLeft, new GridBagConstraints(0, 0, 1, 1, 3.0D, 1.0D, 10, 1, new Insets(5, 5, 5, 0), 0, 0));
    




    JPanel setPanelRight = new JPanel(new GridBagLayout());
    setPanelRight.setBorder(BorderFactory.createTitledBorder(" Colors "));
    
    JPanel colorsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
    
    colorsPanel.add(new JLabel("Core-Distance: "));
    coreDistanceColorButton = new JButton();
    coreDistanceColorButton.setBackground(new Color(100, 100, 100));
    coreDistanceColorButton.addActionListener(panelListener);
    colorsPanel.add(coreDistanceColorButton);
    
    colorsPanel.add(new JLabel("Reachability-Distance: "));
    reachDistanceColorButton = new JButton();
    reachDistanceColorButton.setBackground(Color.orange);
    reachDistanceColorButton.addActionListener(panelListener);
    colorsPanel.add(reachDistanceColorButton);
    
    colorsPanel.add(new JLabel("Graph Background: "));
    graphBackgroundColorButton = new JButton();
    graphBackgroundColorButton.setBackground(new Color(255, 255, 179));
    graphBackgroundColorButton.addActionListener(panelListener);
    colorsPanel.add(graphBackgroundColorButton);
    
    colorsPanel.add(new JLabel());
    resetColorButton = new JButton("Reset");
    resetColorButton.addActionListener(panelListener);
    colorsPanel.add(resetColorButton);
    
    setPanelRight.add(colorsPanel, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, 10, 1, new Insets(5, 5, 5, 5), 0, 0));
    




    settingsPanel.add(setPanelRight, new GridBagConstraints(1, 0, 1, 1, 1.0D, 1.0D, 10, 1, new Insets(5, 5, 5, 5), 0, 0));
    




    return settingsPanel;
  }
  



  private void disableSettingsPanel()
  {
    verticalSlider.setEnabled(false);
    coreDistanceColorButton.setEnabled(false);
    reachDistanceColorButton.setEnabled(false);
    graphBackgroundColorButton.setEnabled(false);
    resetColorButton.setEnabled(false);
    settingsPanel.setVisible(false);
  }
  


  private void enableSettingsPanel()
  {
    verticalSlider.setEnabled(true);
    coreDistanceColorButton.setEnabled(true);
    reachDistanceColorButton.setEnabled(true);
    graphBackgroundColorButton.setEnabled(true);
    resetColorButton.setEnabled(true);
    settingsPanel.setVisible(true);
  }
  



  private JComponent createTabbedPane()
  {
    tabbedPane = new JTabbedPane();
    tabbedPane.addTab("Table", new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("weka/clusterers/forOPTICSAndDBScan/OPTICS_GUI/Graphics/Table16.gif"))), clusteringResultsTable(), "Show table of DataObjects, Core- and Reachability-Distances");
    


    if (serObject != null) {
      tabbedPane.addTab("Graph - Epsilon: " + serObject.getEpsilon() + ", MinPoints: " + serObject.getMinPoints(), new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("weka/clusterers/forOPTICSAndDBScan/OPTICS_GUI/Graphics/Graph16.gif"))), graphPanel(), "Show Plot of Core- and Reachability-Distances");

    }
    else
    {

      tabbedPane.addTab("Graph - Epsilon: --, MinPoints: --", new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("weka/clusterers/forOPTICSAndDBScan/OPTICS_GUI/Graphics/Graph16.gif"))), graphPanel(), "Show Plot of Core- and Reachability-Distances");
    }
    




    tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        int c = tabbedPane.getSelectedIndex();
        if (c == 0) {
          OPTICS_Visualizer.this.disableSettingsPanel();
        } else {
          OPTICS_Visualizer.this.enableSettingsPanel();
        }
      }
    });
    return tabbedPane;
  }
  



  private JComponent createToolBar()
  {
    toolBar = new JToolBar();
    toolBar.setName("OPTICS Visualizer ToolBar");
    toolBar.setFloatable(false);
    toolBarButton_open = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("weka/clusterers/forOPTICSAndDBScan/OPTICS_GUI/Graphics/Open16.gif"))));
    
    toolBarButton_open.setToolTipText("Open OPTICS-Session");
    toolBarButton_open.addActionListener(frameListener);
    toolBar.add(toolBarButton_open);
    
    toolBarButton_save = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("weka/clusterers/forOPTICSAndDBScan/OPTICS_GUI/Graphics/Save16.gif"))));
    
    toolBarButton_save.setToolTipText("Save OPTICS-Session");
    toolBarButton_save.addActionListener(frameListener);
    toolBar.add(toolBarButton_save);
    toolBar.addSeparator(new Dimension(10, 25));
    
    toolBarButton_parameters = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("weka/clusterers/forOPTICSAndDBScan/OPTICS_GUI/Graphics/Parameters16.gif"))));
    
    toolBarButton_parameters.setToolTipText("Show epsilon, MinPoints...");
    toolBarButton_parameters.addActionListener(frameListener);
    toolBar.add(toolBarButton_parameters);
    
    toolBar.addSeparator(new Dimension(10, 25));
    
    toolBarButton_help = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("weka/clusterers/forOPTICSAndDBScan/OPTICS_GUI/Graphics/Help16.gif"))));
    
    toolBarButton_help.setToolTipText("Help topics");
    toolBarButton_help.addActionListener(frameListener);
    toolBar.add(toolBarButton_help);
    
    toolBarButton_about = new JButton(new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("weka/clusterers/forOPTICSAndDBScan/OPTICS_GUI/Graphics/Information16.gif"))));
    
    toolBarButton_about.setToolTipText("About");
    toolBarButton_about.addActionListener(frameListener);
    toolBar.add(toolBarButton_about);
    
    return toolBar;
  }
  



  private JComponent clusteringResultsTable()
  {
    resultVectorTable = new JTable();
    String[] resultVectorTableColumnNames = { "Key", "DataObject", "Core-Distance", "Reachability-Distance" };
    



    DefaultTableColumnModel resultVectorTableColumnModel = new DefaultTableColumnModel();
    for (int i = 0; i < resultVectorTableColumnNames.length; i++) {
      TableColumn tc = new TableColumn(i);
      tc.setHeaderValue(resultVectorTableColumnNames[i]);
      resultVectorTableColumnModel.addColumn(tc);
    }
    ResultVectorTableModel resultVectorTableModel;
    ResultVectorTableModel resultVectorTableModel;
    if (serObject != null) {
      resultVectorTableModel = new ResultVectorTableModel(serObject.getResultVector());
    } else
      resultVectorTableModel = new ResultVectorTableModel(null);
    resultVectorTable = new JTable(resultVectorTableModel, resultVectorTableColumnModel);
    resultVectorTable.getColumnModel().getColumn(0).setPreferredWidth(70);
    resultVectorTable.getColumnModel().getColumn(1).setPreferredWidth(400);
    resultVectorTable.getColumnModel().getColumn(2).setPreferredWidth(150);
    resultVectorTable.getColumnModel().getColumn(3).setPreferredWidth(150);
    resultVectorTable.setAutoResizeMode(0);
    
    JScrollPane resultVectorTableScrollPane = new JScrollPane(resultVectorTable, 22, 32);
    


    return resultVectorTableScrollPane;
  }
  




  private JComponent graphPanel()
  {
    if (serObject == null) {
      graphPanel = new GraphPanel(new FastVector(), verValue, true, true);
    }
    else {
      graphPanel = new GraphPanel(serObject.getResultVector(), verValue, true, true);
      graphPanel.setPreferredSize(new Dimension(10 * serObject.getDatabaseSize() + serObject.getDatabaseSize(), graphPanel.getHeight()));
    }
    
    graphPanel.setBackground(new Color(255, 255, 179));
    graphPanel.setOpaque(true);
    
    graphPanelScrollPane = new JScrollPane(graphPanel, 22, 32);
    

    return graphPanelScrollPane;
  }
  



  private JMenuBar constructDefaultMenuBar()
  {
    defaultMenuBar = new JMenuBar();
    
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic('F');
    open = new JMenuItem("Open...", new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("weka/clusterers/forOPTICSAndDBScan/OPTICS_GUI/Graphics/Open16.gif"))));
    
    open.setMnemonic('O');
    open.setAccelerator(KeyStroke.getKeyStroke(79, 2));
    open.addActionListener(frameListener);
    fileMenu.add(open);
    
    save = new JMenuItem("Save...", new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("weka/clusterers/forOPTICSAndDBScan/OPTICS_GUI/Graphics/Save16.gif"))));
    
    save.setMnemonic('S');
    save.setAccelerator(KeyStroke.getKeyStroke(83, 2));
    save.addActionListener(frameListener);
    fileMenu.add(save);
    
    fileMenu.addSeparator();
    
    exit = new JMenuItem("Exit", 88);
    exit.addActionListener(frameListener);
    fileMenu.add(exit);
    
    defaultMenuBar.add(fileMenu);
    
    JMenu toolsMenu = new JMenu("View");
    toolsMenu.setMnemonic('V');
    parameters = new JMenuItem("Parameters...", new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("weka/clusterers/forOPTICSAndDBScan/OPTICS_GUI/Graphics/Parameters16.gif"))));
    
    parameters.setMnemonic('P');
    parameters.setAccelerator(KeyStroke.getKeyStroke(80, 2));
    parameters.addActionListener(frameListener);
    toolsMenu.add(parameters);
    
    defaultMenuBar.add(toolsMenu);
    
    JMenu miscMenu = new JMenu("Help");
    miscMenu.setMnemonic('H');
    help = new JMenuItem("Help Topics", new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("weka/clusterers/forOPTICSAndDBScan/OPTICS_GUI/Graphics/Help16.gif"))));
    
    help.setMnemonic('H');
    help.setAccelerator(KeyStroke.getKeyStroke(72, 2));
    help.addActionListener(frameListener);
    miscMenu.add(help);
    
    about = new JMenuItem("About...", new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("weka/clusterers/forOPTICSAndDBScan/OPTICS_GUI/Graphics/Information16.gif"))));
    
    about.setMnemonic('A');
    about.setAccelerator(KeyStroke.getKeyStroke(65, 2));
    about.addActionListener(frameListener);
    miscMenu.add(about);
    defaultMenuBar.add(miscMenu);
    
    return defaultMenuBar;
  }
  



  private void setMenuBar(JMenuBar menuBar)
  {
    frame.setJMenuBar(menuBar);
  }
  


  private void loadStatisticsFrame()
  {
    statisticsFrame = new JFrame("Parameters");
    statisticsFrame.getContentPane().setLayout(new BorderLayout());
    
    JPanel statPanel_Labels = new JPanel(new GridBagLayout());
    JPanel statPanel_Labels_Left = new JPanel(new GridLayout(9, 1));
    JPanel statPanel_Labels_Right = new JPanel(new GridLayout(9, 1));
    
    statPanel_Labels_Left.add(new JLabel("Number of clustered DataObjects: "));
    statPanel_Labels_Right.add(new JLabel(Integer.toString(serObject.getDatabaseSize())));
    statPanel_Labels_Left.add(new JLabel("Number of attributes: "));
    statPanel_Labels_Right.add(new JLabel(Integer.toString(serObject.getNumberOfAttributes())));
    statPanel_Labels_Left.add(new JLabel("Epsilon: "));
    statPanel_Labels_Right.add(new JLabel(Double.toString(serObject.getEpsilon())));
    statPanel_Labels_Left.add(new JLabel("MinPoints: "));
    statPanel_Labels_Right.add(new JLabel(Integer.toString(serObject.getMinPoints())));
    statPanel_Labels_Left.add(new JLabel("Write results to file: "));
    statPanel_Labels_Right.add(new JLabel(serObject.isOpticsOutputs() ? "yes" : "no"));
    statPanel_Labels_Left.add(new JLabel("Index: "));
    statPanel_Labels_Right.add(new JLabel(serObject.getDatabase_Type()));
    statPanel_Labels_Left.add(new JLabel("Distance-Type: "));
    statPanel_Labels_Right.add(new JLabel(serObject.getDatabase_distanceType()));
    statPanel_Labels_Left.add(new JLabel("Number of generated clusters: "));
    statPanel_Labels_Right.add(new JLabel(Integer.toString(serObject.getNumberOfGeneratedClusters())));
    statPanel_Labels_Left.add(new JLabel("Elapsed-time: "));
    statPanel_Labels_Right.add(new JLabel(serObject.getElapsedTime()));
    statPanel_Labels.setBorder(BorderFactory.createTitledBorder(" OPTICS parameters "));
    
    statPanel_Labels.add(statPanel_Labels_Left, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, 10, 1, new Insets(0, 5, 2, 0), 0, 0));
    




    statPanel_Labels.add(statPanel_Labels_Right, new GridBagConstraints(1, 0, 1, 1, 3.0D, 1.0D, 10, 1, new Insets(0, 5, 2, 5), 0, 0));
    




    statisticsFrame.getContentPane().add(statPanel_Labels, "Center");
    
    statisticsFrame.addWindowListener(new WindowAdapter()
    {

      public void windowClosing(WindowEvent e)
      {

        statisticsFrame.dispose();
      }
      
    });
    JPanel okButtonPanel = new JPanel(new GridBagLayout());
    
    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("OK")) {
          statisticsFrame.dispose();
        }
      }
    });
    okButtonPanel.add(okButton, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, 10, 0, new Insets(5, 0, 5, 0), 0, 0));
    




    statisticsFrame.getContentPane().add(okButtonPanel, "South");
    statisticsFrame.setSize(new Dimension(500, 300));
    Rectangle frameDimension = frame.getBounds();
    Point p = frame.getLocation();
    Rectangle statisticsFrameDimension = statisticsFrame.getBounds();
    statisticsFrame.setLocation((width - width) / 2 + (int)p.getX(), (height - height) / 2 + (int)p.getY());
    
    statisticsFrame.setVisible(true);
    statisticsFrame.toFront();
  }
  


  private void loadHelpFrame()
  {
    helpFrame = new JFrame("Help Topics");
    helpFrame.getContentPane().setLayout(new BorderLayout());
    
    JPanel helpPanel = new JPanel(new GridBagLayout());
    JTextArea helpTextArea = new JTextArea();
    helpTextArea.setEditable(false);
    helpTextArea.append("OPTICS Visualizer Help\n===========================================================\n\nOpen\n - Open OPTICS-Session\n   [Ctrl-O], File | Open\n\nSave\n - Save OPTICS-Session\n   [Ctrl-S], File | Save\n\nExit\n - Exit OPTICS Visualizer\n   [Alt-F4], File | Exit\n\nParameters\n - Show epsilon, MinPoints...\n   [Ctrl-P], View | Parameters\n\nHelp Topics\n - Show this frame\n   [Ctrl-H], Help | Help Topics\n\nAbout\n - Copyright-Information\n   [Ctrl-A], Help | About\n\n\nTable-Pane:\n-----------------------------------------------------------\nThe table represents the calculated clustering-order.\nTo save the table please select File | Save from the\nmenubar. Restart OPTICS with the -F option to obtain\nan ASCII-formatted file of the clustering-order.\n\nGraph-Pane:\n-----------------------------------------------------------\nThe graph draws the plot of core- and reachability-\ndistances. By (de-)activating core- and reachability-\ndistances in the 'General Settings'-Panel you can\ninfluence the visualization in detail. Simply use the\n'Vertical Adjustment'-Slider to emphasize the plot of\ndistances. The 'Colors'-Panel lets you define different\ncolors of the graph background, core- and reachability-\ndistances. Click the 'Reset'-Button to restore the\ndefaults.\n");
    





































    final JScrollPane helpTextAreaScrollPane = new JScrollPane(helpTextArea, 22, 32);
    

    helpTextAreaScrollPane.setBorder(BorderFactory.createEtchedBorder());
    helpPanel.add(helpTextAreaScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, 10, 1, new Insets(5, 5, 7, 5), 0, 0));
    




    helpFrame.getContentPane().add(helpPanel, "Center");
    
    helpFrame.addWindowListener(new WindowAdapter()
    {

      public void windowClosing(WindowEvent e)
      {

        helpFrame.dispose();
      }
      


      public void windowOpened(WindowEvent e)
      {
        helpTextAreaScrollPane.getVerticalScrollBar().setValue(0);
      }
      
    });
    JPanel closeButtonPanel = new JPanel(new GridBagLayout());
    
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Close")) {
          helpFrame.dispose();
        }
      }
    });
    closeButtonPanel.add(closeButton, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, 10, 0, new Insets(0, 0, 5, 0), 0, 0));
    




    helpFrame.getContentPane().add(closeButtonPanel, "South");
    helpFrame.setSize(new Dimension(480, 400));
    Rectangle frameDimension = frame.getBounds();
    Point p = frame.getLocation();
    Rectangle helpFrameDimension = helpFrame.getBounds();
    helpFrame.setLocation((width - width) / 2 + (int)p.getX(), (height - height) / 2 + (int)p.getY());
    
    helpFrame.setVisible(true);
    helpFrame.toFront();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 4791 $");
  }
  





  public static void main(String[] args)
    throws Exception
  {
    SERObject serObject = null;
    if (args.length == 1) {
      System.out.println("Attempting to load: " + args[0]);
      ObjectInputStream is = null;
      try {
        FileInputStream fs = new FileInputStream(args[0]);
        is = new ObjectInputStream(fs);
        serObject = (SERObject)is.readObject();
      }
      catch (Exception e) {
        serObject = null;
        JOptionPane.showMessageDialog(null, "Error loading file:\n" + e, "Error", 0);

      }
      finally
      {
        try
        {
          is.close();
        }
        catch (Exception e) {}
      }
    }
    



    new OPTICS_Visualizer(serObject, "OPTICS Visualizer - Main Window");
  }
  


  private class FrameListener
    implements ActionListener, RevisionHandler
  {
    private FrameListener() {}
    


    public void actionPerformed(ActionEvent e)
    {
      if ((e.getSource() == parameters) || (e.getSource() == toolBarButton_parameters)) {
        OPTICS_Visualizer.this.loadStatisticsFrame();
      }
      
      if ((e.getSource() == about) || (e.getSource() == toolBarButton_about)) {
        JOptionPane.showMessageDialog(frame, "OPTICS Visualizer\n$ Rev 1.4 $\n\nCopyright (C) 2004 Rainer Holzmann, Zhanna Melnikova-Albrecht", "About", 1);
      }
      



      if ((e.getSource() == help) || (e.getSource() == toolBarButton_help)) {
        OPTICS_Visualizer.this.loadHelpFrame();
      }
      
      if (e.getSource() == exit) {
        frame.dispose();
      }
      
      if ((e.getSource() == open) || (e.getSource() == toolBarButton_open)) {
        jFileChooser.setDialogTitle("Open OPTICS-Session");
        if (lastPath == null) {
          lastPath = System.getProperty("user.dir");
        }
        jFileChooser.setCurrentDirectory(new File(lastPath));
        int ret = jFileChooser.showOpenDialog(frame);
        SERObject serObject_1 = null;
        if (ret == 0) {
          File f = jFileChooser.getSelectedFile();
          try {
            FileInputStream fs = new FileInputStream(f.getAbsolutePath());
            ObjectInputStream is = new ObjectInputStream(fs);
            serObject_1 = (SERObject)is.readObject();
            is.close();
          } catch (FileNotFoundException e1) {
            JOptionPane.showMessageDialog(frame, "File not found.", "Error", 0);
          }
          catch (ClassNotFoundException e1)
          {
            JOptionPane.showMessageDialog(frame, "OPTICS-Session could not be read.", "Error", 0);
          }
          catch (IOException e1)
          {
            JOptionPane.showMessageDialog(frame, "This file does not contain a valid OPTICS-Session.", "Error", 0);
          }
          

          if (serObject_1 != null) {
            int ret_1 = JOptionPane.showConfirmDialog(frame, "Open OPTICS-Session in a new window?", "Open", 1);
            

            switch (ret_1) {
            case 0: 
              new OPTICS_Visualizer(serObject_1, "OPTICS Visualizer - " + f.getName());
              break;
            case 1: 
              serObject = serObject_1;
              resultVectorTable.setModel(new ResultVectorTableModel(serObject.getResultVector()));
              tabbedPane.setTitleAt(1, "Graph - Epsilon: " + serObject.getEpsilon() + ", MinPoints: " + serObject.getMinPoints());
              

              graphPanel.setResultVector(serObject.getResultVector());
              graphPanel.adjustSize(serObject);
              graphPanel.repaint();
              break;
            }
            
          }
        }
      }
      

      if ((e.getSource() == save) || (e.getSource() == toolBarButton_save)) {
        jFileChooser.setDialogTitle("Save OPTICS-Session");
        
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        String timeStamp = gregorianCalendar.get(5) + "-" + (gregorianCalendar.get(2) + 1) + "-" + gregorianCalendar.get(1) + "--" + gregorianCalendar.get(11) + "-" + gregorianCalendar.get(12) + "-" + gregorianCalendar.get(13);
        




        String filename = "OPTICS_" + timeStamp + ".ser";
        
        File file = new File(filename);
        jFileChooser.setSelectedFile(file);
        if (lastPath == null) {
          lastPath = System.getProperty("user.dir");
        }
        jFileChooser.setCurrentDirectory(new File(lastPath));
        
        int ret = jFileChooser.showSaveDialog(frame);
        if (ret == 0) {
          file = jFileChooser.getSelectedFile();
          try {
            FileOutputStream fs = new FileOutputStream(file.getAbsolutePath());
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(serObject);
            os.flush();
            os.close();
          } catch (IOException e1) {
            JOptionPane.showMessageDialog(frame, "OPTICS-Session could not be saved.", "Error", 0);
          }
        }
      }
    }
    






    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 4791 $");
    }
  }
  
  private class SettingsPanelListener
    implements ActionListener, RevisionHandler
  {
    private SettingsPanelListener() {}
    
    public void actionPerformed(ActionEvent e)
    {
      if (e.getSource() == coreDistanceColorButton) {
        Color c = getSelectedColor("Select 'Core-Distance' color");
        if (c != null) {
          coreDistanceColorButton.setBackground(c);
          graphPanel.setCoreDistanceColor(c);
        }
      }
      if (e.getSource() == reachDistanceColorButton) {
        Color c = getSelectedColor("Select 'Reachability-Distance' color");
        if (c != null) {
          reachDistanceColorButton.setBackground(c);
          graphPanel.setReachabilityDistanceColor(c);
        }
      }
      if (e.getSource() == graphBackgroundColorButton) {
        Color c = getSelectedColor("Select 'Graph Background' color");
        if (c != null) {
          graphBackgroundColorButton.setBackground(c);
          graphPanel.setBackground(c);
        }
      }
      if (e.getSource() == resetColorButton) {
        coreDistanceColorButton.setBackground(new Color(100, 100, 100));
        graphPanel.setCoreDistanceColor(new Color(100, 100, 100));
        reachDistanceColorButton.setBackground(Color.orange);
        graphPanel.setReachabilityDistanceColor(Color.orange);
        graphBackgroundColorButton.setBackground(new Color(255, 255, 179));
        graphPanel.setBackground(new Color(255, 255, 179));
        graphPanel.repaint();
      }
    }
    
    private Color getSelectedColor(String title) {
      Color c = JColorChooser.showDialog(frame, title, Color.black);
      return c;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 4791 $");
    }
  }
}
