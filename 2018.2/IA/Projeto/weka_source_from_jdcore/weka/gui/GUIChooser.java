package weka.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import weka.classifiers.bayes.net.GUI;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Copyright;
import weka.core.Instances;
import weka.core.Memory;
import weka.core.SystemInfo;
import weka.core.Utils;
import weka.core.Version;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ConverterUtils;
import weka.core.logging.Logger;
import weka.core.logging.Logger.Level;
import weka.gui.arffviewer.ArffViewer;
import weka.gui.beans.KnowledgeFlow;
import weka.gui.beans.KnowledgeFlowApp;
import weka.gui.beans.StartUpListener;
import weka.gui.boundaryvisualizer.BoundaryVisualizer;
import weka.gui.experiment.Experimenter;
import weka.gui.explorer.Explorer;
import weka.gui.explorer.PreprocessPanel;
import weka.gui.graphvisualizer.GraphVisualizer;
import weka.gui.sql.SqlViewer;
import weka.gui.treevisualizer.Node;
import weka.gui.treevisualizer.NodePlace;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeBuild;
import weka.gui.treevisualizer.TreeVisualizer;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;
import weka.gui.visualize.VisualizePanel;














public class GUIChooser
  extends JFrame
{
  private static final long serialVersionUID = 9001529425230247914L;
  protected GUIChooser m_Self;
  private JMenuBar m_jMenuBar;
  private JMenu m_jMenuProgram;
  private JMenu m_jMenuVisualization;
  private JMenu m_jMenuTools;
  private JMenu m_jMenuHelp;
  
  static
  {
    try
    {
      Object MacApp = Class.forName("com.apple.eawt.Application").newInstance();
      
      Object macArffHandler = Class.forName("weka.gui.MacArffOpenFilesHandler").newInstance();
      

      Class fileHandlerClass = Class.forName("com.apple.eawt.OpenFilesHandler");
      Class[] paramClass = new Class[1];
      paramClass[0] = fileHandlerClass;
      Object[] args = new Object[1];
      args[0] = macArffHandler;
      
      Method m = MacApp.getClass().getMethod("setOpenFileHandler", paramClass);
      System.out.println("Trying to install a file handler for Mac...");
      m.invoke(MacApp, new Object[] { macArffHandler });
    }
    catch (Exception ex) {}
  }
  


















  protected JPanel m_PanelApplications = new JPanel();
  


  protected JButton m_ExplorerBut;
  


  protected JFrame m_ExplorerFrame;
  


  protected JButton m_ExperimenterBut;
  


  protected JFrame m_ExperimenterFrame;
  


  protected JButton m_KnowledgeFlowBut;
  


  protected String m_pendingKnowledgeFlowLoad;
  


  protected JFrame m_KnowledgeFlowFrame;
  


  protected JButton m_SimpleBut;
  


  protected SimpleCLI m_SimpleCLI;
  


  protected Vector m_ArffViewers;
  

  protected JFrame m_SqlViewerFrame;
  

  protected JFrame m_BayesNetGUIFrame;
  

  protected JFrame m_EnsembleLibraryFrame;
  

  protected Vector m_Plots;
  

  protected Vector m_ROCs;
  

  protected Vector m_TreeVisualizers;
  

  protected Vector m_GraphVisualizers;
  

  protected JFrame m_BoundaryVisualizerFrame;
  

  protected JFrame m_SystemInfoFrame;
  

  protected JFrame m_MemoryUsageFrame;
  

  protected static LogWindow m_LogWindow = new LogWindow();
  


  Image m_weka;
  


  protected JFileChooser m_FileChooserTreeVisualizer;
  


  protected JFileChooser m_FileChooserGraphVisualizer;
  

  protected JFileChooser m_FileChooserPlot;
  

  protected JFileChooser m_FileChooserROC;
  

  protected Image m_Icon;
  

  protected HashSet<Container> m_ChildFrames;
  

  private static GUIChooser m_chooser;
  


  public static synchronized void createSingleton()
  {
    if (m_chooser == null) {
      m_chooser = new GUIChooser();
    }
  }
  




  public static GUIChooser getSingleton()
  {
    return m_chooser;
  }
  



  public GUIChooser()
  {
    super(Messages.getString("GUIChooser_Title_Text"));Messages.getInstance();m_ExplorerBut = new JButton(Messages.getString("GUIChooser_Explorer_Text"));Messages.getInstance();m_ExperimenterBut = new JButton(Messages.getString("GUIChooser_Experimenter_Text"));Messages.getInstance();m_KnowledgeFlowBut = new JButton(Messages.getString("GUIChooser_KnowledgeFlow_Text"));m_pendingKnowledgeFlowLoad = null;Messages.getInstance();m_SimpleBut = new JButton(Messages.getString("GUIChooser_Simple_CLI_Text"));m_ArffViewers = new Vector();m_Plots = new Vector();m_ROCs = new Vector();m_TreeVisualizers = new Vector();m_GraphVisualizers = new Vector();m_weka = Toolkit.getDefaultToolkit().getImage(GUIChooser.class.getClassLoader().getResource("weka/gui/images/weka_background.gif"));m_FileChooserTreeVisualizer = new JFileChooser(new File(System.getProperty("user.dir")));m_FileChooserGraphVisualizer = new JFileChooser(new File(System.getProperty("user.dir")));m_FileChooserPlot = new JFileChooser(new File(System.getProperty("user.dir")));m_FileChooserROC = new JFileChooser(new File(System.getProperty("user.dir")));m_ChildFrames = new HashSet();
    
    m_Self = this;
    

    Messages.getInstance();m_FileChooserGraphVisualizer.addChoosableFileFilter(new ExtensionFileFilter(".bif", Messages.getString("GUIChooser_BIF_Files_Text")));
    

    Messages.getInstance();m_FileChooserGraphVisualizer.addChoosableFileFilter(new ExtensionFileFilter(".xml", Messages.getString("GUIChooser_XML_Files_Text")));
    


    Messages.getInstance();Messages.getInstance();m_FileChooserPlot.addChoosableFileFilter(new ExtensionFileFilter(".arff", Messages.getString("GUIChooser_ARFF_Files_Text_Front") + ".arff" + Messages.getString("GUIChooser_ARFF_Files_Text_End")));
    



    m_FileChooserPlot.setMultiSelectionEnabled(true);
    
    Messages.getInstance();Messages.getInstance();m_FileChooserROC.addChoosableFileFilter(new ExtensionFileFilter(".arff", Messages.getString("GUIChooser_ARFF_Files_Text_Front") + ".arff" + Messages.getString("GUIChooser_ARFF_Files_Text_End")));
    





    m_Icon = Toolkit.getDefaultToolkit().getImage(GUIChooser.class.getClassLoader().getResource("weka/gui/weka_icon_new_48.png"));
    


    setIconImage(m_Icon);
    getContentPane().setLayout(new BorderLayout());
    
    getContentPane().add(m_PanelApplications, "East");
    

    Messages.getInstance();m_PanelApplications.setBorder(BorderFactory.createTitledBorder(Messages.getString("GUIChooser_Applications_Text")));
    
    m_PanelApplications.setLayout(new GridLayout(4, 1));
    m_PanelApplications.add(m_ExplorerBut);
    m_PanelApplications.add(m_ExperimenterBut);
    m_PanelApplications.add(m_KnowledgeFlowBut);
    m_PanelApplications.add(m_SimpleBut);
    

    JPanel wekaPan = new JPanel();
    wekaPan.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    wekaPan.setLayout(new BorderLayout());
    Messages.getInstance();wekaPan.setToolTipText(Messages.getString("GUIChooser_Weka_Native_Bird_Text"));
    
    ImageIcon wii = new ImageIcon(m_weka);
    JLabel wekaLab = new JLabel(wii);
    wekaPan.add(wekaLab, "Center");
    Messages.getInstance();Messages.getInstance();String infoString = Messages.getString("GUIChooser_Information_Text_Front") + " " + Version.VERSION + "<br>" + "(c) " + Copyright.getFromYear() + " - " + Copyright.getToYear() + "<br>" + Copyright.getOwner() + "<br>" + Copyright.getAddress() + Messages.getString("GUIChooser_Information_Text_End");
    





    JLabel infoLab = new JLabel(infoString);
    infoLab.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    wekaPan.add(infoLab, "South");
    
    getContentPane().add(wekaPan, "Center");
    

    m_jMenuBar = new JMenuBar();
    

    m_jMenuProgram = new JMenu();
    m_jMenuBar.add(m_jMenuProgram);
    Messages.getInstance();m_jMenuProgram.setText(Messages.getString("GUIChooser_Program_Text"));
    
    m_jMenuProgram.setMnemonic('P');
    

    JMenuItem jMenuItemProgramLogWindow = new JMenuItem();
    m_jMenuProgram.add(jMenuItemProgramLogWindow);
    Messages.getInstance();jMenuItemProgramLogWindow.setText(Messages.getString("GUIChooser_LogWindow_Text"));
    

    jMenuItemProgramLogWindow.setAccelerator(KeyStroke.getKeyStroke(76, 2));
    
    m_LogWindow.setIconImage(m_Icon);
    jMenuItemProgramLogWindow.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        GUIChooser.m_LogWindow.setVisible(true);
      }
      
    });
    final JMenuItem jMenuItemProgramMemUsage = new JMenuItem();
    m_jMenuProgram.add(jMenuItemProgramMemUsage);
    Messages.getInstance();jMenuItemProgramMemUsage.setText(Messages.getString("GUIChooser_Memory_Usage_Text"));
    

    jMenuItemProgramMemUsage.setAccelerator(KeyStroke.getKeyStroke(77, 2));
    

    jMenuItemProgramMemUsage.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        if (m_MemoryUsageFrame == null) {
          final MemoryUsagePanel panel = new MemoryUsagePanel();
          jMenuItemProgramMemUsage.setEnabled(false);
          Messages.getInstance();m_MemoryUsageFrame = new JFrame(Messages.getString("GUIChooser_Memory_Usage_List_Text"));
          

          m_MemoryUsageFrame.setIconImage(m_Icon);
          m_MemoryUsageFrame.getContentPane().setLayout(new BorderLayout());
          m_MemoryUsageFrame.getContentPane().add(panel, "Center");
          m_MemoryUsageFrame.addWindowListener(new WindowAdapter()
          {
            public void windowClosing(WindowEvent w) {
              panel.stopMonitoring();
              m_MemoryUsageFrame.dispose();
              m_MemoryUsageFrame = null;
              val$jMenuItemProgramMemUsage.setEnabled(true);
              GUIChooser.this.checkExit();
            }
          });
          m_MemoryUsageFrame.pack();
          m_MemoryUsageFrame.setSize(400, 50);
          Point l = panel.getFrameLocation();
          if ((x != -1) && (y != -1)) {
            m_MemoryUsageFrame.setLocation(l);
          }
          m_MemoryUsageFrame.setVisible(true);
          Dimension size = m_MemoryUsageFrame.getPreferredSize();
          m_MemoryUsageFrame.setSize(new Dimension((int)size.getWidth(), (int)size.getHeight()));
        }
        
      }
      
    });
    m_jMenuProgram.add(new JSeparator());
    

    JMenuItem jMenuItemProgramExit = new JMenuItem();
    m_jMenuProgram.add(jMenuItemProgramExit);
    Messages.getInstance();jMenuItemProgramExit.setText(Messages.getString("GUIChooser_Exit_Text"));
    

    jMenuItemProgramExit.setAccelerator(KeyStroke.getKeyStroke(69, 2));
    
    jMenuItemProgramExit.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        dispose();
        GUIChooser.this.checkExit();
      }
      

    });
    m_jMenuVisualization = new JMenu();
    m_jMenuBar.add(m_jMenuVisualization);
    m_jMenuVisualization.setText(Messages.getString("GUIChooser_Visualization_Text"));
    
    m_jMenuVisualization.setMnemonic('V');
    

    JMenuItem jMenuItemVisualizationPlot = new JMenuItem();
    m_jMenuVisualization.add(jMenuItemVisualizationPlot);
    Messages.getInstance();jMenuItemVisualizationPlot.setText(Messages.getString("GUIChooser_Plot_Text"));
    

    jMenuItemVisualizationPlot.setAccelerator(KeyStroke.getKeyStroke(80, 2));
    

    jMenuItemVisualizationPlot.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        int retVal = m_FileChooserPlot.showOpenDialog(m_Self);
        if (retVal != 0) {
          return;
        }
        

        VisualizePanel panel = new VisualizePanel();
        String filenames = "";
        File[] files = m_FileChooserPlot.getSelectedFiles();
        for (int j = 0; j < files.length; j++) {
          String filename = files[j].getAbsolutePath();
          if (j > 0) {
            filenames = filenames + ", ";
          }
          filenames = filenames + filename;
          Messages.getInstance();System.err.println(Messages.getString("GUIChooser_LoadingInstancesFrom_Error_Text") + filename);
          

          try
          {
            Reader r = new BufferedReader(new FileReader(filename));
            Instances i = new Instances(r);
            i.setClassIndex(i.numAttributes() - 1);
            PlotData2D pd1 = new PlotData2D(i);
            
            if (j == 0) {
              Messages.getInstance();pd1.setPlotName(Messages.getString("GUIChooser_MasterPlot_Text"));
              
              panel.setMasterPlot(pd1);
            } else {
              Messages.getInstance();pd1.setPlotName(Messages.getString("GUIChooser_Plot_Text") + (j + 1));
              

              m_useCustomColour = true;
              m_customColour = (j % 2 == 0 ? Color.red : Color.blue);
              panel.addPlot(pd1);
            }
          } catch (Exception ex) {
            ex.printStackTrace();
            Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(m_Self, Messages.getString("GUIChooser_Plot_LoadingFile_Error_Text_Front") + files[j] + Messages.getString("GUIChooser_Plot_LoadingFile_Error_Text_End") + ex.getMessage());
            






            return;
          }
        }
        

        Messages.getInstance();final JFrame frame = new JFrame(Messages.getString("GUIChooser_Plot_PlotName_Text") + filenames);
        


        frame.setIconImage(m_Icon);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, "Center");
        frame.addWindowListener(new WindowAdapter()
        {
          public void windowClosing(WindowEvent e) {
            m_Plots.remove(frame);
            frame.dispose();
            GUIChooser.this.checkExit();
          }
        });
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
        m_Plots.add(frame);
      }
      

    });
    JMenuItem jMenuItemVisualizationROC = new JMenuItem();
    m_jMenuVisualization.add(jMenuItemVisualizationROC);
    Messages.getInstance();jMenuItemVisualizationROC.setText(Messages.getString("GUIChooser_ROC_Text"));
    

    jMenuItemVisualizationROC.setAccelerator(KeyStroke.getKeyStroke(82, 2));
    

    jMenuItemVisualizationROC.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        int retVal = m_FileChooserROC.showOpenDialog(m_Self);
        if (retVal != 0) {
          return;
        }
        

        String filename = m_FileChooserROC.getSelectedFile().getAbsolutePath();
        Instances result = null;
        try {
          result = new Instances(new BufferedReader(new FileReader(filename)));
        } catch (Exception ex) {
          ex.printStackTrace();
          Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(m_Self, Messages.getString("GUIChooser_ROC_LoadingFile_Error_Text_Front") + filename + Messages.getString("GUIChooser_ROC_LoadingFile_Error_Text_End") + ex.getMessage());
          





          return;
        }
        result.setClassIndex(result.numAttributes() - 1);
        ThresholdVisualizePanel vmc = new ThresholdVisualizePanel();
        Messages.getInstance();Messages.getInstance();vmc.setROCString(Messages.getString("GUIChooser_ROC_AreaUnderROC_Text_Front") + Utils.doubleToString(ThresholdCurve.getROCArea(result), 4) + Messages.getString("GUIChooser_ROC_AreaUnderROC_Text_End"));
        



        vmc.setName(result.relationName());
        PlotData2D tempd = new PlotData2D(result);
        tempd.setPlotName(result.relationName());
        tempd.addInstanceNumberAttribute();
        try {
          vmc.addPlot(tempd);
        } catch (Exception ex) {
          ex.printStackTrace();
          Messages.getInstance();JOptionPane.showMessageDialog(m_Self, Messages.getString("GUIChooser_ROC_AddingPlot_Error_Text") + ex.getMessage());
          



          return;
        }
        
        Messages.getInstance();final JFrame frame = new JFrame(Messages.getString("GUIChooser_ROC_File_Text") + filename);
        


        frame.setIconImage(m_Icon);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(vmc, "Center");
        frame.addWindowListener(new WindowAdapter()
        {
          public void windowClosing(WindowEvent e) {
            m_ROCs.remove(frame);
            frame.dispose();
            GUIChooser.this.checkExit();
          }
        });
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
        m_ROCs.add(frame);
      }
      

    });
    JMenuItem jMenuItemVisualizationTree = new JMenuItem();
    m_jMenuVisualization.add(jMenuItemVisualizationTree);
    Messages.getInstance();jMenuItemVisualizationTree.setText(Messages.getString("GUIChooser_TreeVisualizer_Text"));
    

    jMenuItemVisualizationTree.setAccelerator(KeyStroke.getKeyStroke(84, 2));
    

    jMenuItemVisualizationTree.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        int retVal = m_FileChooserTreeVisualizer.showOpenDialog(m_Self);
        if (retVal != 0) {
          return;
        }
        

        String filename = m_FileChooserTreeVisualizer.getSelectedFile().getAbsolutePath();
        
        TreeBuild builder = new TreeBuild();
        Node top = null;
        NodePlace arrange = new PlaceNode2();
        try {
          top = builder.create(new FileReader(filename));
        } catch (Exception ex) {
          ex.printStackTrace();
          Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(m_Self, Messages.getString("GUIChooser_TreeVisualizer_LoadingFile_Error_Text_Front") + filename + Messages.getString("GUIChooser_TreeVisualizer_LoadingFile_Error_Text_End") + ex.getMessage());
          






          return;
        }
        

        Messages.getInstance();final JFrame frame = new JFrame(Messages.getString("GUIChooser_TreeVisualizer_File_Text") + filename);
        


        frame.setIconImage(m_Icon);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new TreeVisualizer(null, top, arrange), "Center");
        
        frame.addWindowListener(new WindowAdapter()
        {
          public void windowClosing(WindowEvent e) {
            m_TreeVisualizers.remove(frame);
            frame.dispose();
            GUIChooser.this.checkExit();
          }
        });
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
        m_TreeVisualizers.add(frame);
      }
      

    });
    JMenuItem jMenuItemVisualizationGraph = new JMenuItem();
    m_jMenuVisualization.add(jMenuItemVisualizationGraph);
    Messages.getInstance();jMenuItemVisualizationGraph.setText(Messages.getString("GUIChooser_GraphVisualizer_Text"));
    

    jMenuItemVisualizationGraph.setAccelerator(KeyStroke.getKeyStroke(71, 2));
    

    jMenuItemVisualizationGraph.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        int retVal = m_FileChooserGraphVisualizer.showOpenDialog(m_Self);
        if (retVal != 0) {
          return;
        }
        

        String filename = m_FileChooserGraphVisualizer.getSelectedFile().getAbsolutePath();
        
        GraphVisualizer panel = new GraphVisualizer();
        try {
          if ((filename.toLowerCase().endsWith(".xml")) || (filename.toLowerCase().endsWith(".bif")))
          {
            panel.readBIF(new FileInputStream(filename));
          } else {
            panel.readDOT(new FileReader(filename));
          }
        } catch (Exception ex) {
          ex.printStackTrace();
          Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(m_Self, Messages.getString("GUIChooser_GraphVisualizer_LoadingFile_Error_Text_Front") + filename + Messages.getString("GUIChooser_GraphVisualizer_LoadingFile_Error_Text_End") + ex.getMessage());
          






          return;
        }
        

        Messages.getInstance();final JFrame frame = new JFrame(Messages.getString("GUIChooser_GraphVisualizer_File_Text") + filename);
        


        frame.setIconImage(m_Icon);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, "Center");
        frame.addWindowListener(new WindowAdapter()
        {
          public void windowClosing(WindowEvent e) {
            m_GraphVisualizers.remove(frame);
            frame.dispose();
            GUIChooser.this.checkExit();
          }
        });
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
        m_GraphVisualizers.add(frame);
      }
      

    });
    final JMenuItem jMenuItemVisualizationBoundary = new JMenuItem();
    m_jMenuVisualization.add(jMenuItemVisualizationBoundary);
    Messages.getInstance();jMenuItemVisualizationBoundary.setText(Messages.getString("GUIChooser_BoundaryVisualizer_Text"));
    

    jMenuItemVisualizationBoundary.setAccelerator(KeyStroke.getKeyStroke(66, 2));
    

    jMenuItemVisualizationBoundary.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        if (m_BoundaryVisualizerFrame == null) {
          jMenuItemVisualizationBoundary.setEnabled(false);
          Messages.getInstance();m_BoundaryVisualizerFrame = new JFrame(Messages.getString("GUIChooser_BoundaryVisualizer_JFrame_Text"));
          

          m_BoundaryVisualizerFrame.setIconImage(m_Icon);
          m_BoundaryVisualizerFrame.getContentPane().setLayout(new BorderLayout());
          

          final BoundaryVisualizer bv = new BoundaryVisualizer();
          m_BoundaryVisualizerFrame.getContentPane().add(bv, "Center");
          
          m_BoundaryVisualizerFrame.setSize(bv.getMinimumSize());
          m_BoundaryVisualizerFrame.addWindowListener(new WindowAdapter()
          {
            public void windowClosing(WindowEvent w) {
              bv.stopPlotting();
              m_BoundaryVisualizerFrame.dispose();
              m_BoundaryVisualizerFrame = null;
              val$jMenuItemVisualizationBoundary.setEnabled(true);
              GUIChooser.this.checkExit();
            }
          });
          m_BoundaryVisualizerFrame.pack();
          
          m_BoundaryVisualizerFrame.setResizable(false);
          m_BoundaryVisualizerFrame.setVisible(true);
          
          BoundaryVisualizer.setExitIfNoWindowsOpen(false);
        }
        
      }
      
    });
    Messages.getInstance();JMenu jMenuExtensions = new JMenu(Messages.getString("GUIChooser_Extensions_Text"));
    
    jMenuExtensions.setMnemonic(69);
    m_jMenuBar.add(jMenuExtensions);
    jMenuExtensions.setVisible(false);
    
    String extensions = GenericObjectEditor.EDITOR_PROPERTIES.getProperty(MainMenuExtension.class.getName(), "");
    


    if (extensions.length() > 0) {
      jMenuExtensions.setVisible(true);
      String[] classnames = GenericObjectEditor.EDITOR_PROPERTIES.getProperty(MainMenuExtension.class.getName(), "").split(",");
      

      Hashtable<String, JMenu> submenus = new Hashtable();
      

      for (String classname : classnames) {
        try {
          MainMenuExtension ext = (MainMenuExtension)Class.forName(classname).newInstance();
          


          JMenu submenu = null;
          if (ext.getSubmenuTitle() != null) {
            submenu = (JMenu)submenus.get(ext.getSubmenuTitle());
            if (submenu == null) {
              submenu = new JMenu(ext.getSubmenuTitle());
              submenus.put(ext.getSubmenuTitle(), submenu);
              insertMenuItem(jMenuExtensions, submenu);
            }
          }
          

          JMenuItem menuitem = new JMenuItem();
          menuitem.setText(ext.getMenuTitle());
          

          ActionListener listener = ext.getActionListener(m_Self);
          if (listener != null) {
            menuitem.addActionListener(listener);
          } else {
            final JMenuItem finalMenuitem = menuitem;
            final MainMenuExtension finalExt = ext;
            menuitem.addActionListener(new ActionListener()
            {
              public void actionPerformed(ActionEvent e) {
                Component frame = createFrame(m_Self, finalMenuitem.getText(), null, null, null, -1, -1, null, false, false);
                

                finalExt.fillFrame(frame);
                frame.setVisible(true);
              }
            });
          }
          

          if (submenu != null) {
            insertMenuItem(submenu, menuitem);
          } else {
            insertMenuItem(jMenuExtensions, menuitem);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    

    m_jMenuTools = new JMenu();
    m_jMenuBar.add(m_jMenuTools);
    Messages.getInstance();m_jMenuTools.setText(Messages.getString("GUIChooser_Tools_Text"));
    
    m_jMenuTools.setMnemonic('T');
    

    JMenuItem jMenuItemToolsArffViewer = new JMenuItem();
    m_jMenuTools.add(jMenuItemToolsArffViewer);
    Messages.getInstance();jMenuItemToolsArffViewer.setText(Messages.getString("GUIChooser_ArffViewer_Text"));
    

    jMenuItemToolsArffViewer.setAccelerator(KeyStroke.getKeyStroke(65, 2));
    

    jMenuItemToolsArffViewer.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        final ArffViewer av = new ArffViewer();
        av.addWindowListener(new WindowAdapter()
        {
          public void windowClosing(WindowEvent w) {
            m_ArffViewers.remove(av);
            GUIChooser.this.checkExit();
          }
        });
        av.setVisible(true);
        m_ArffViewers.add(av);
      }
      

    });
    final JMenuItem jMenuItemToolsSql = new JMenuItem();
    m_jMenuTools.add(jMenuItemToolsSql);
    Messages.getInstance();jMenuItemToolsSql.setText(Messages.getString("GUIChooser_SqlViewer_Text"));
    

    jMenuItemToolsSql.setAccelerator(KeyStroke.getKeyStroke(83, 2));
    

    jMenuItemToolsSql.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        if (m_SqlViewerFrame == null) {
          jMenuItemToolsSql.setEnabled(false);
          final SqlViewer sql = new SqlViewer(null);
          Messages.getInstance();m_SqlViewerFrame = new JFrame(Messages.getString("GUIChooser_SqlViewer_JFrame_Text"));
          

          m_SqlViewerFrame.setIconImage(m_Icon);
          m_SqlViewerFrame.getContentPane().setLayout(new BorderLayout());
          m_SqlViewerFrame.getContentPane().add(sql, "Center");
          m_SqlViewerFrame.addWindowListener(new WindowAdapter()
          {
            public void windowClosing(WindowEvent w) {
              sql.saveSize();
              m_SqlViewerFrame.dispose();
              m_SqlViewerFrame = null;
              val$jMenuItemToolsSql.setEnabled(true);
              GUIChooser.this.checkExit();
            }
          });
          m_SqlViewerFrame.pack();
          m_SqlViewerFrame.setVisible(true);
        }
        
      }
      
    });
    final JMenuItem jMenuItemBayesNet = new JMenuItem();
    m_jMenuTools.add(jMenuItemBayesNet);
    Messages.getInstance();jMenuItemBayesNet.setText(Messages.getString("GUIChooser_BayesNetEditor_Text"));
    
    jMenuItemBayesNet.setAccelerator(KeyStroke.getKeyStroke(78, 2));
    
    jMenuItemBayesNet.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        if (m_BayesNetGUIFrame == null) {
          jMenuItemBayesNet.setEnabled(false);
          GUI bayesNetGUI = new GUI();
          JMenuBar bayesBar = bayesNetGUI.getMenuBar();
          Messages.getInstance();m_BayesNetGUIFrame = new JFrame(Messages.getString("GUIChooser_BayesNetworkEditor_JFrame_Text"));
          

          m_BayesNetGUIFrame.setIconImage(m_Icon);
          m_BayesNetGUIFrame.setJMenuBar(bayesBar);
          m_BayesNetGUIFrame.getContentPane().add(bayesNetGUI, "Center");
          
          m_BayesNetGUIFrame.addWindowListener(new WindowAdapter()
          {
            public void windowClosing(WindowEvent w) {
              m_BayesNetGUIFrame.dispose();
              m_BayesNetGUIFrame = null;
              val$jMenuItemBayesNet.setEnabled(true);
              GUIChooser.this.checkExit();
            }
          });
          m_BayesNetGUIFrame.setSize(800, 600);
          m_BayesNetGUIFrame.setVisible(true);
        }
        
      }
      
    });
    m_jMenuHelp = new JMenu();
    m_jMenuBar.add(m_jMenuHelp);
    Messages.getInstance();m_jMenuHelp.setText(Messages.getString("GUIChooser_Help_Text"));
    
    m_jMenuHelp.setMnemonic('H');
    

    JMenuItem jMenuItemHelpHomepage = new JMenuItem();
    m_jMenuHelp.add(jMenuItemHelpHomepage);
    Messages.getInstance();jMenuItemHelpHomepage.setText(Messages.getString("GUIChooser_WekaHomepage_Text"));
    

    jMenuItemHelpHomepage.setAccelerator(KeyStroke.getKeyStroke(72, 2));
    
    jMenuItemHelpHomepage.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        Messages.getInstance();BrowserHelper.openURL(Messages.getString("GUIChooser_WekaHomepage_URL_Text"));
      }
      

    });
    m_jMenuHelp.add(new JSeparator());
    

    JMenuItem jMenuItemHelpWekaWiki = new JMenuItem();
    m_jMenuHelp.add(jMenuItemHelpWekaWiki);
    Messages.getInstance();jMenuItemHelpWekaWiki.setText(Messages.getString("GUIChooser_WekaHOWTO_Text"));
    

    jMenuItemHelpWekaWiki.setAccelerator(KeyStroke.getKeyStroke(87, 2));
    

    jMenuItemHelpWekaWiki.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        Messages.getInstance();BrowserHelper.openURL(Messages.getString("GUIChooser_WekaWiki_URL_Text"));

      }
      

    });
    JMenuItem jMenuItemHelpSourceforge = new JMenuItem();
    m_jMenuHelp.add(jMenuItemHelpSourceforge);
    Messages.getInstance();jMenuItemHelpSourceforge.setText(Messages.getString("GUIChooser_WekaOnSourceforge_Text"));
    

    jMenuItemHelpSourceforge.setAccelerator(KeyStroke.getKeyStroke(70, 2));
    

    jMenuItemHelpSourceforge.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        Messages.getInstance();BrowserHelper.openURL(Messages.getString("GUIChooser_WekaOnSourceforge_URL_Text"));

      }
      

    });
    final JMenuItem jMenuItemHelpSysInfo = new JMenuItem();
    m_jMenuHelp.add(jMenuItemHelpSysInfo);
    Messages.getInstance();jMenuItemHelpSysInfo.setText(Messages.getString("GUIChooser_SystemInfo_Text"));
    

    jMenuItemHelpSysInfo.setAccelerator(KeyStroke.getKeyStroke(73, 2));
    

    jMenuItemHelpSysInfo.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        if (m_SystemInfoFrame == null) {
          jMenuItemHelpSysInfo.setEnabled(false);
          Messages.getInstance();m_SystemInfoFrame = new JFrame(Messages.getString("GUIChooser_SystemInfo_JFrame_Text"));
          

          m_SystemInfoFrame.setIconImage(m_Icon);
          m_SystemInfoFrame.getContentPane().setLayout(new BorderLayout());
          

          Hashtable info = new SystemInfo().getSystemInfo();
          

          Vector names = new Vector();
          Enumeration enm = info.keys();
          while (enm.hasMoreElements()) {
            names.add(enm.nextElement());
          }
          Collections.sort(names);
          

          String[][] data = new String[info.size()][2];
          for (int i = 0; i < names.size(); i++) {
            data[i][0] = names.get(i).toString();
            data[i][1] = info.get(data[i][0]).toString();
          }
          
          String[] tmp202_199 = new String[2];Messages.getInstance();tmp202_199[0] = Messages.getString("GUIChooser_SystemInfo_TitleKey_Text"); String[] tmp214_202 = tmp202_199;Messages.getInstance();tmp214_202[1] = Messages.getString("GUIChooser_SystemInfo_TitleValue_Text");String[] titles = tmp214_202;
          




          JTable table = new JTable(data, titles);
          
          m_SystemInfoFrame.getContentPane().add(new JScrollPane(table), "Center");
          
          m_SystemInfoFrame.addWindowListener(new WindowAdapter()
          {
            public void windowClosing(WindowEvent w) {
              m_SystemInfoFrame.dispose();
              m_SystemInfoFrame = null;
              val$jMenuItemHelpSysInfo.setEnabled(true);
              GUIChooser.this.checkExit();
            }
            
          });m_SystemInfoFrame.pack();
          m_SystemInfoFrame.setSize(800, 600);
          m_SystemInfoFrame.setVisible(true);
        }
        
      }
      

    });
    m_ExplorerBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        showExplorer(null);
      }
      
    });
    m_ExperimenterBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        if (m_ExperimenterFrame == null) {
          System.out.println("CALLED ExperimentEnvironment");
          
          m_ExperimenterBut.setEnabled(false);
          Messages.getInstance();m_ExperimenterFrame = new JFrame(Messages.getString("GUIChooser_WekaExperimentEnvironment_JFrame_Text"));
          

          m_ExperimenterFrame.setIconImage(m_Icon);
          m_ExperimenterFrame.getContentPane().setLayout(new BorderLayout());
          m_ExperimenterFrame.getContentPane().add(new Experimenter(false), "Center");
          
          m_ExperimenterFrame.addWindowListener(new WindowAdapter()
          {
            public void windowClosing(WindowEvent w) {
              m_ExperimenterFrame.dispose();
              m_ExperimenterFrame = null;
              m_ExperimenterBut.setEnabled(true);
              GUIChooser.this.checkExit();
            }
          });
          m_ExperimenterFrame.pack();
          m_ExperimenterFrame.setSize(800, 600);
          m_ExperimenterFrame.setVisible(true);
        }
        
      }
    });
    KnowledgeFlowApp.addStartupListener(new StartUpListener()
    {
      public void startUpComplete() {
        if (m_KnowledgeFlowFrame == null) {
          final KnowledgeFlowApp kna = KnowledgeFlowApp.getSingleton();
          m_KnowledgeFlowBut.setEnabled(false);
          if ((m_pendingKnowledgeFlowLoad != null) && (m_pendingKnowledgeFlowLoad.length() > 0))
          {
            KnowledgeFlowApp.getSingleton().loadInitialLayout(m_pendingKnowledgeFlowLoad);
            
            m_pendingKnowledgeFlowLoad = null;
          }
          Messages.getInstance();m_KnowledgeFlowFrame = new JFrame(Messages.getString("GUIChooser_WekaKnowledgeFlowEnvironment_JFrame_Text"));
          

          m_KnowledgeFlowFrame.setIconImage(m_Icon);
          m_KnowledgeFlowFrame.getContentPane().setLayout(new BorderLayout());
          m_KnowledgeFlowFrame.getContentPane().add(kna, "Center");
          m_KnowledgeFlowFrame.addWindowListener(new WindowAdapter()
          {
            public void windowClosing(WindowEvent w) {
              kna.clearLayout();
              m_KnowledgeFlowFrame.dispose();
              m_KnowledgeFlowFrame = null;
              m_KnowledgeFlowBut.setEnabled(true);
              GUIChooser.this.checkExit();
            }
          });
          m_KnowledgeFlowFrame.pack();
          m_KnowledgeFlowFrame.setSize(1000, 750);
          m_KnowledgeFlowFrame.setVisible(true);
        }
        
      }
    });
    m_KnowledgeFlowBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        showKnowledgeFlow(null);
      }
      
    });
    m_SimpleBut.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e) {
        if (m_SimpleCLI == null) {
          m_SimpleBut.setEnabled(false);
          try {
            m_SimpleCLI = new SimpleCLI();
            m_SimpleCLI.setIconImage(m_Icon);
          } catch (Exception ex) {
            Messages.getInstance();throw new Error(Messages.getString("GUIChooser_UnableToStartSimpleCLI_Error_Text"));
          }
          
          m_SimpleCLI.addWindowListener(new WindowAdapter()
          {
            public void windowClosing(WindowEvent w) {
              m_SimpleCLI.dispose();
              m_SimpleCLI = null;
              m_SimpleBut.setEnabled(true);
              GUIChooser.this.checkExit();
            }
          });
          m_SimpleCLI.setVisible(true);






        }
        






      }
      






    });
    setJMenuBar(m_jMenuBar);
    
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent w) {
        dispose();
        GUIChooser.this.checkExit();
      }
    });
    pack();
  }
  
  public void showKnowledgeFlow(String fileToLoad) {
    if (m_KnowledgeFlowFrame == null) {
      KnowledgeFlow.startApp();
      m_pendingKnowledgeFlowLoad = fileToLoad;
    }
    else if (fileToLoad != null) {
      KnowledgeFlowApp.getSingleton().loadInitialLayout(fileToLoad);
    }
  }
  
  public void showExplorer(String fileToLoad)
  {
    Explorer expl = null;
    if (m_ExplorerFrame == null) {
      m_ExplorerBut.setEnabled(false);
      m_ExplorerFrame = new JFrame("Weka Explorer");
      m_ExplorerFrame.setIconImage(m_Icon);
      m_ExplorerFrame.getContentPane().setLayout(new BorderLayout());
      expl = new Explorer();
      
      m_ExplorerFrame.getContentPane().add(expl, "Center");
      m_ExplorerFrame.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent w) {
          m_ExplorerFrame.dispose();
          m_ExplorerFrame = null;
          m_ExplorerBut.setEnabled(true);
          GUIChooser.this.checkExit();
        }
      });
      m_ExplorerFrame.pack();
      m_ExplorerFrame.setSize(800, 600);
      m_ExplorerFrame.setVisible(true);
    } else {
      Object o = m_ExplorerFrame.getContentPane().getComponent(0);
      if ((o instanceof Explorer)) {
        expl = (Explorer)o;
      }
    }
    
    if (fileToLoad != null) {
      try {
        AbstractFileLoader loader = ConverterUtils.getLoaderForFile(fileToLoad);
        
        loader.setFile(new File(fileToLoad));
        expl.getPreprocessPanel().setInstancesFromFile(loader);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  





  protected void insertMenuItem(JMenu menu, JMenuItem menuitem)
  {
    insertMenuItem(menu, menuitem, 0);
  }
  












  protected void insertMenuItem(JMenu menu, JMenuItem menuitem, int startIndex)
  {
    boolean inserted = false;
    String newStr = menuitem.getText().toLowerCase();
    

    for (int i = startIndex; i < menu.getMenuComponentCount(); i++) {
      if ((menu.getMenuComponent(i) instanceof JMenuItem))
      {


        JMenuItem current = (JMenuItem)menu.getMenuComponent(i);
        String currentStr = current.getText().toLowerCase();
        if (currentStr.compareTo(newStr) > 0) {
          inserted = true;
          menu.insert(menuitem, i);
          break;
        }
      }
    }
    
    if (!inserted) {
      menu.add(menuitem);
    }
  }
  

















  protected Container createFrame(GUIChooser parent, String title, Component c, LayoutManager layout, Object layoutConstraints, int width, int height, JMenuBar menu, boolean listener, boolean visible)
  {
    Container result = null;
    
    final ChildFrameSDI frame = new ChildFrameSDI(parent, title);
    

    frame.setLayout(layout);
    if (c != null) {
      frame.getContentPane().add(c, layoutConstraints);
    }
    

    frame.setJMenuBar(menu);
    

    frame.pack();
    if ((width > -1) && (height > -1)) {
      frame.setSize(width, height);
    }
    frame.validate();
    

    int screenHeight = getGraphicsConfigurationgetBoundsheight;
    int screenWidth = getGraphicsConfigurationgetBoundswidth;
    frame.setLocation((screenWidth - getBoundswidth) / 2, (screenHeight - getBoundsheight) / 2);
    


    if (listener) {
      frame.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e) {
          frame.dispose();
        }
      });
    }
    

    if (visible) {
      frame.setVisible(true);
    }
    
    result = frame;
    
    return result;
  }
  





  public static class ChildFrameSDI
    extends JFrame
  {
    private static final long serialVersionUID = 8588293938686425618L;
    



    protected GUIChooser m_Parent;
    




    public ChildFrameSDI(GUIChooser parent, String title)
    {
      super();
      
      m_Parent = parent;
      
      addWindowListener(new WindowAdapter()
      {
        public void windowActivated(WindowEvent e)
        {
          if (getParentFrame() != null) {
            getParentFrame().createTitle(getTitle());
          }
        }
      });
      

      if (getParentFrame() != null) {
        getParentFrame().addChildFrame(this);
        setIconImage(getParentFrame().getIconImage());
      }
    }
    




    public GUIChooser getParentFrame()
    {
      return m_Parent;
    }
    



    public void dispose()
    {
      if (getParentFrame() != null) {
        getParentFrame().removeChildFrame(this);
        getParentFrame().createTitle("");
      }
      
      super.dispose();
    }
  }
  






  protected void createTitle(String title)
  {
    Messages.getInstance();String newTitle = Messages.getString("GUIChooser_CreateTitle_Text_Front") + new Version();
    

    if (title.length() != 0) {
      Messages.getInstance();newTitle = newTitle + Messages.getString("GUIChooser_CreateTitle_Text_End") + title;
    }
    


    setTitle(newTitle);
  }
  




  public void addChildFrame(Container c)
  {
    m_ChildFrames.add(c);
  }
  





  public boolean removeChildFrame(Container c)
  {
    boolean result = m_ChildFrames.remove(c);
    return result;
  }
  



  private void checkExit()
  {
    if ((!isVisible()) && (m_ExplorerFrame == null) && (m_ExperimenterFrame == null) && (m_KnowledgeFlowFrame == null) && (m_SimpleCLI == null) && (m_ArffViewers.size() == 0) && (m_SqlViewerFrame == null) && (m_EnsembleLibraryFrame == null) && (m_Plots.size() == 0) && (m_ROCs.size() == 0) && (m_TreeVisualizers.size() == 0) && (m_GraphVisualizers.size() == 0) && (m_BoundaryVisualizerFrame == null) && (m_SystemInfoFrame == null))
    {














      System.exit(0);
    }
  }
  







  private static Memory m_Memory = new Memory(true);
  





  public static void main(String[] args)
  {
    Messages.getInstance();Logger.log(Logger.Level.INFO, Messages.getString("GUIChooser_Main_LoggingStarted_Text"));
    
    LookAndFeel.setLookAndFeel();
    




    try
    {
      createSingleton();
      m_chooser.setVisible(true);
      
      if ((args != null) && (args.length > 0)) {
        m_chooser.showExplorer(args[0]);
      }
      
      Thread memMonitor = new Thread()
      {


        public void run()
        {

          for (;;)
          {

            if (GUIChooser.m_Memory.isOutOfMemory())
            {
              GUIChooser.m_chooser.dispose();
              if (m_chooserm_ExperimenterFrame != null) {
                m_chooserm_ExperimenterFrame.dispose();
                m_chooserm_ExperimenterFrame = null;
              }
              if (m_chooserm_ExplorerFrame != null) {
                m_chooserm_ExplorerFrame.dispose();
                m_chooserm_ExplorerFrame = null;
              }
              if (m_chooserm_KnowledgeFlowFrame != null) {
                m_chooserm_KnowledgeFlowFrame.dispose();
                m_chooserm_KnowledgeFlowFrame = null;
              }
              if (m_chooserm_SimpleCLI != null) {
                m_chooserm_SimpleCLI.dispose();
                m_chooserm_SimpleCLI = null;
              }
              if (m_chooserm_ArffViewers.size() > 0) {
                for (int i = 0; i < m_chooserm_ArffViewers.size(); i++) {
                  ArffViewer av = (ArffViewer)m_chooserm_ArffViewers.get(i);
                  av.dispose();
                }
                m_chooserm_ArffViewers.clear();
              }
              GUIChooser.access$202(null);
              System.gc();
              

              GUIChooser.m_LogWindow.setVisible(true);
              GUIChooser.m_LogWindow.toFront();
              Messages.getInstance();System.err.println(Messages.getString("GUIChooser_Main_Error_Text_Front"));
              
              GUIChooser.m_Memory.showOutOfMemory();
              Messages.getInstance();System.err.println(Messages.getString("GUIChooser_Main_Error_Text_End"));
              
              System.exit(-1);

            }
            
          }
          
        }
        

      };
      memMonitor.setPriority(5);
      memMonitor.start();
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
