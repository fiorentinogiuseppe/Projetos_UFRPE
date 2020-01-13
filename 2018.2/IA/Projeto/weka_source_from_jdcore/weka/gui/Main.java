package weka.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import weka.classifiers.bayes.net.GUI;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Copyright;
import weka.core.Instances;
import weka.core.Memory;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.SelectedTag;
import weka.core.SystemInfo;
import weka.core.Tag;
import weka.core.Utils;
import weka.core.Version;
import weka.core.logging.Logger;
import weka.core.logging.Logger.Level;
import weka.gui.arffviewer.ArffViewerMainPanel;
import weka.gui.beans.KnowledgeFlowApp;
import weka.gui.beans.StartUpListener;
import weka.gui.boundaryvisualizer.BoundaryVisualizer;
import weka.gui.experiment.Experimenter;
import weka.gui.explorer.Explorer;
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


















































public class Main
  extends JFrame
  implements OptionHandler
{
  private static final long serialVersionUID = 1453813254824253849L;
  public static final int GUI_MDI = 0;
  public static final int GUI_SDI = 1;
  
  public static class BackgroundDesktopPane
    extends JDesktopPane
  {
    private static final long serialVersionUID = 2046713123452402745L;
    protected Image m_Background;
    
    public BackgroundDesktopPane(String image)
    {
      try
      {
        m_Background = Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource(image));
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    





    public void paintComponent(Graphics g)
    {
      super.paintComponent(g);
      
      if (m_Background != null) {
        g.setColor(Color.WHITE);
        g.clearRect(0, 0, getWidth(), getHeight());
        
        int width = m_Background.getWidth(null);
        int height = m_Background.getHeight(null);
        int x = (getWidth() - width) / 2;
        int y = (getHeight() - height) / 2;
        g.drawImage(m_Background, x, y, width, height, this);
      }
    }
  }
  





  public static class ChildFrameSDI
    extends JFrame
  {
    private static final long serialVersionUID = 8588293938686425618L;
    



    protected Main m_Parent;
    




    public ChildFrameSDI(Main parent, String title)
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
    




    public Main getParentFrame()
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
  





  public static class ChildFrameMDI
    extends JInternalFrame
  {
    private static final long serialVersionUID = 3772573515346899959L;
    



    protected Main m_Parent;
    




    public ChildFrameMDI(Main parent, String title)
    {
      super(true, true, true, true);
      
      m_Parent = parent;
      
      addInternalFrameListener(new InternalFrameAdapter()
      {
        public void internalFrameActivated(InternalFrameEvent e)
        {
          if (getParentFrame() != null) {
            getParentFrame().createTitle(getTitle());
          }
        }
      });
      

      if (getParentFrame() != null) {
        getParentFrame().addChildFrame(this);
        getParentFramejDesktopPane.add(this);
      }
    }
    




    public Main getParentFrame()
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
  



  static
  {
    Tag[] tmp4_1 = new Tag[2];Messages.getInstance();tmp4_1[0] = new Tag(0, "MDI", Messages.getString("Main_Tag_GUI_Text_First")); Tag[] tmp28_4 = tmp4_1;Messages.getInstance();tmp28_4[1] = new Tag(1, "SDI", Messages.getString("Main_Tag_GUI_Text_Second")); } public static final Tag[] TAGS_GUI = tmp28_4;
  



  protected Main m_Self;
  



  protected int m_GUIType = 0;
  



  protected static Main m_MainCommandline;
  



  protected static Main m_MainSingleton;
  



  protected static Vector m_StartupListeners = new Vector();
  

  protected static Memory m_Memory = new Memory(true);
  

  protected HashSet<Container> m_ChildFrames = new HashSet();
  

  protected static LogWindow m_LogWindow = new LogWindow();
  

  protected JFileChooser m_FileChooserTreeVisualizer = new JFileChooser(new File(System.getProperty("user.dir")));
  


  protected JFileChooser m_FileChooserGraphVisualizer = new JFileChooser(new File(System.getProperty("user.dir")));
  


  protected JFileChooser m_FileChooserPlot = new JFileChooser(new File(System.getProperty("user.dir")));
  


  protected JFileChooser m_FileChooserROC = new JFileChooser(new File(System.getProperty("user.dir")));
  
  private JMenu jMenuHelp;
  
  private JMenu jMenuVisualization;
  
  private JMenu jMenuTools;
  
  private JDesktopPane jDesktopPane;
  
  private JMenu jMenuApplications;
  
  private JMenuItem jMenuItemHelpSystemInfo;
  
  private JMenuItem jMenuItemHelpAbout;
  
  private JMenuItem jMenuItemHelpHomepage;
  
  private JMenuItem jMenuItemHelpWekaWiki;
  
  private JMenuItem jMenuItemHelpSourceforge;
  
  private JMenuItem jMenuItemVisualizationBoundaryVisualizer;
  
  private JMenuItem jMenuItemVisualizationGraphVisualizer;
  
  private JMenuItem jMenuItemVisualizationTreeVisualizer;
  
  private JMenuItem jMenuItemVisualizationROC;
  
  private JMenuItem jMenuItemVisualizationPlot;
  
  private JMenuItem jMenuItemToolsSqlViewer;
  
  private JMenuItem jMenuItemToolsArffViewer;
  
  private JMenuItem jMenuItemApplicationsSimpleCLI;
  
  private JMenuItem jMenuItemApplicationsKnowledgeFlow;
  
  private JMenuItem jMenuItemApplicationsExperimenter;
  
  private JMenuItem jMenuItemApplicationsExplorer;
  
  private JMenuItem jMenuItemProgramExit;
  
  private JMenuItem jMenuItemProgramLogWindow;
  
  private JMenuItem jMenuItemProgramMemoryUsage;
  
  private JMenu jMenuProgram;
  
  private JMenu jMenuExtensions;
  
  private JMenu jMenuWindows;
  
  private JMenuBar jMenuBar;
  
  protected Container createFrame(Main parent, String title, Component c, LayoutManager layout, Object layoutConstraints, int width, int height, JMenuBar menu, boolean listener, boolean visible)
  {
    Container result = null;
    
    if (m_GUIType == 0) {
      final ChildFrameMDI frame = new ChildFrameMDI(parent, title);
      

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
      

      if (listener) {
        frame.addInternalFrameListener(new InternalFrameAdapter()
        {
          public void internalFrameClosing(InternalFrameEvent e) {
            frame.dispose();
          }
        });
      }
      

      if (visible) {
        frame.setVisible(true);
        try {
          frame.setSelected(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      
      result = frame;
    } else if (m_GUIType == 1) {
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
    }
    
    return result;
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
  


  protected void initGUI()
  {
    m_Self = this;
    
    try
    {
      createTitle("");
      setDefaultCloseOperation(3);
      setIconImage(new ImageIcon(getClass().getClassLoader().getResource("weka/gui/weka_icon_new_48.png")).getImage());
      


      Messages.getInstance();m_FileChooserGraphVisualizer.addChoosableFileFilter(new ExtensionFileFilter(".bif", Messages.getString("Main_InitGUI_ExtensionFileFilter_Text_First")));
      


      Messages.getInstance();m_FileChooserGraphVisualizer.addChoosableFileFilter(new ExtensionFileFilter(".xml", Messages.getString("Main_InitGUI_ExtensionFileFilter_Text_Second")));
      



      Messages.getInstance();m_FileChooserPlot.addChoosableFileFilter(new ExtensionFileFilter(".arff", Messages.getString("Main_InitGUI_ExtensionFileFilter_Text_Third") + ".arff" + ")"));
      


      m_FileChooserPlot.setMultiSelectionEnabled(true);
      
      Messages.getInstance();m_FileChooserROC.addChoosableFileFilter(new ExtensionFileFilter(".arff", Messages.getString("Main_InitGUI_ExtensionFileFilter_Text_Third") + ".arff" + ")"));
      




      if (m_GUIType == 0) {
        jDesktopPane = new BackgroundDesktopPane("weka/gui/images/weka_background.gif");
        
        jDesktopPane.setDragMode(1);
        setContentPane(jDesktopPane);
      } else {
        jDesktopPane = null;
      }
      

      jMenuBar = new JMenuBar();
      setJMenuBar(jMenuBar);
      

      jMenuProgram = new JMenu();
      jMenuBar.add(jMenuProgram);
      Messages.getInstance();jMenuProgram.setText(Messages.getString("Main_InitGUI_JMenuProgram_SetText_Text"));
      
      jMenuProgram.setMnemonic('P');
      















      jMenuItemProgramLogWindow = new JMenuItem();
      jMenuProgram.add(jMenuItemProgramLogWindow);
      Messages.getInstance();jMenuItemProgramLogWindow.setText(Messages.getString("Main_InitGUI_JMenuItemProgramLogWindow_SetText_Text"));
      
      jMenuItemProgramLogWindow.setMnemonic('L');
      jMenuItemProgramLogWindow.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt) {
          Main.m_LogWindow.setVisible(true);
        }
        
      });
      jMenuItemProgramMemoryUsage = new JMenuItem();
      jMenuProgram.add(jMenuItemProgramMemoryUsage);
      Messages.getInstance();jMenuItemProgramMemoryUsage.setText(Messages.getString("Main_InitGUI_JMenuItemProgramMemoryUsage_SetText_Text"));
      
      jMenuItemProgramMemoryUsage.setMnemonic('M');
      jMenuItemProgramMemoryUsage.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt) {
          String title = jMenuItemProgramMemoryUsage.getText();
          if (!containsWindow(title)) {
            final MemoryUsagePanel panel = new MemoryUsagePanel();
            Container c = createFrame(m_Self, title, panel, new BorderLayout(), "Center", 400, 50, null, true, true);
            


            Dimension size = c.getPreferredSize();
            c.setSize(new Dimension((int)size.getWidth(), (int)size.getHeight()));
            


            if (m_GUIType == 0) {
              Main.ChildFrameMDI frame = (Main.ChildFrameMDI)c;
              Point l = panel.getFrameLocation();
              if ((x != -1) && (y != -1)) {
                frame.setLocation(l);
              }
              frame.addInternalFrameListener(new InternalFrameAdapter()
              {
                public void internalFrameClosing(InternalFrameEvent e) {
                  panel.stopMonitoring();
                }
              });
            } else {
              Main.ChildFrameSDI frame = (Main.ChildFrameSDI)c;
              Point l = panel.getFrameLocation();
              if ((x != -1) && (y != -1)) {
                frame.setLocation(l);
              }
              frame.addWindowListener(new WindowAdapter()
              {
                public void windowClosing(WindowEvent e) {
                  panel.stopMonitoring();
                }
              });
            }
          } else {
            showWindow(getWindow(title));
          }
          
        }
      });
      jMenuProgram.add(new JSeparator());
      

      jMenuItemProgramExit = new JMenuItem();
      jMenuProgram.add(jMenuItemProgramExit);
      Messages.getInstance();jMenuItemProgramExit.setText(Messages.getString("Main_InitGUI_JMenuItemProgramExit_SetText_Text"));
      
      jMenuItemProgramExit.setMnemonic('E');
      jMenuItemProgramExit.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt)
        {
          Iterator iter = getWindowList();
          Vector<Container> list = new Vector();
          while (iter.hasNext()) {
            list.add((Container)iter.next());
          }
          for (int i = 0; i < list.size(); i++) {
            Container c = (Container)list.get(i);
            if ((c instanceof Main.ChildFrameMDI)) {
              ((Main.ChildFrameMDI)c).dispose();
            } else if ((c instanceof Main.ChildFrameSDI)) {
              ((Main.ChildFrameSDI)c).dispose();
            }
          }
          
          Main.m_LogWindow.dispose();
          
          m_Self.dispose();
          
          System.exit(0);
        }
        

      });
      jMenuApplications = new JMenu();
      jMenuBar.add(jMenuApplications);
      Messages.getInstance();jMenuApplications.setText(Messages.getString("Main_InitGUI_JMenuApplications_SetText_Text"));
      
      jMenuApplications.setMnemonic('A');
      

      jMenuItemApplicationsExplorer = new JMenuItem();
      jMenuApplications.add(jMenuItemApplicationsExplorer);
      Messages.getInstance();jMenuItemApplicationsExplorer.setText(Messages.getString("Main_InitGUI_JMenuItemApplicationsExplorer_SetText_Text"));
      
      jMenuItemApplicationsExplorer.setMnemonic('E');
      jMenuItemApplicationsExplorer.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt) {
          String title = jMenuItemApplicationsExplorer.getText();
          if (!containsWindow(title)) {
            createFrame(m_Self, title, new Explorer(), new BorderLayout(), "Center", 800, 600, null, true, true);
          }
          else {
            showWindow(getWindow(title));
          }
          
        }
        
      });
      jMenuItemApplicationsExperimenter = new JMenuItem();
      jMenuApplications.add(jMenuItemApplicationsExperimenter);
      Messages.getInstance();jMenuItemApplicationsExperimenter.setText(Messages.getString("Main_InitGUI_JMenuItemApplicationsExperimenter_SetText_Text"));
      

      jMenuItemApplicationsExperimenter.setMnemonic('X');
      jMenuItemApplicationsExperimenter.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt) {
          String title = jMenuItemApplicationsExperimenter.getText();
          if (!containsWindow(title)) {
            createFrame(m_Self, title, new Experimenter(false), new BorderLayout(), "Center", 800, 600, null, true, true);
          }
          else
          {
            showWindow(getWindow(title));
          }
          
        }
        
      });
      jMenuItemApplicationsKnowledgeFlow = new JMenuItem();
      jMenuApplications.add(jMenuItemApplicationsKnowledgeFlow);
      Messages.getInstance();jMenuItemApplicationsKnowledgeFlow.setText(Messages.getString("Main_InitGUI_JMenuItemApplicationsKnowledgeFlow_SetText_Text"));
      

      jMenuItemApplicationsKnowledgeFlow.setMnemonic('K');
      jMenuItemApplicationsKnowledgeFlow.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt)
        {
          String title = jMenuItemApplicationsKnowledgeFlow.getText();
          if (!containsWindow(title)) {
            KnowledgeFlowApp.createSingleton(new String[0]);
            createFrame(m_Self, title, KnowledgeFlowApp.getSingleton(), new BorderLayout(), "Center", 900, 600, null, true, true);
          }
          else
          {
            showWindow(getWindow(title));
          }
          
        }
        
      });
      jMenuItemApplicationsSimpleCLI = new JMenuItem();
      jMenuApplications.add(jMenuItemApplicationsSimpleCLI);
      Messages.getInstance();jMenuItemApplicationsSimpleCLI.setText(Messages.getString("Main_InitGUI_JMenuItemApplicationsSimpleCLI_SetText_Text"));
      
      jMenuItemApplicationsSimpleCLI.setMnemonic('S');
      jMenuItemApplicationsSimpleCLI.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt) {
          String title = jMenuItemApplicationsSimpleCLI.getText();
          if (!containsWindow(title)) {
            try {
              createFrame(m_Self, title, new SimpleCLIPanel(), new BorderLayout(), "Center", 600, 500, null, true, true);
            }
            catch (Exception e)
            {
              e.printStackTrace();
              Messages.getInstance();JOptionPane.showMessageDialog(m_Self, Messages.getString("Main_InitGUI_Exception_JOptionPaneShowMessageDialog_Text") + e.getMessage());
              



              return;
            }
          } else {
            showWindow(getWindow(title));
          }
          
        }
        
      });
      jMenuTools = new JMenu();
      jMenuBar.add(jMenuTools);
      Messages.getInstance();jMenuTools.setText(Messages.getString("Main_InitGUI_JMenuTools_JMenu_SetText_Text"));
      
      jMenuTools.setMnemonic('T');
      

      jMenuItemToolsArffViewer = new JMenuItem();
      jMenuTools.add(jMenuItemToolsArffViewer);
      Messages.getInstance();jMenuItemToolsArffViewer.setText(Messages.getString("Main_InitGUI_JMenuItemToolsArffViewer_SetText_Text"));
      
      jMenuItemToolsArffViewer.setMnemonic('A');
      jMenuItemToolsArffViewer.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt) {
          String title = jMenuItemToolsArffViewer.getText();
          if (!containsWindow(title)) {
            ArffViewerMainPanel panel = new ArffViewerMainPanel(null);
            panel.setConfirmExit(false);
            Container frame = createFrame(m_Self, title, panel, new BorderLayout(), "Center", 800, 600, panel.getMenu(), true, true);
            

            panel.setParent(frame);
          } else {
            showWindow(getWindow(title));
          }
          
        }
        
      });
      jMenuItemToolsSqlViewer = new JMenuItem();
      jMenuTools.add(jMenuItemToolsSqlViewer);
      Messages.getInstance();jMenuItemToolsSqlViewer.setText(Messages.getString("Main_InitGUI_JMenuItemToolsSqlViewer_SetText_Text"));
      
      jMenuItemToolsSqlViewer.setMnemonic('S');
      jMenuItemToolsSqlViewer.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt) {
          String title = jMenuItemToolsSqlViewer.getText();
          if (!containsWindow(title)) {
            final SqlViewer sql = new SqlViewer(null);
            final Container frame = createFrame(m_Self, title, sql, new BorderLayout(), "Center", -1, -1, null, false, true);
            



            if ((frame instanceof Main.ChildFrameMDI)) {
              ((Main.ChildFrameMDI)frame).addInternalFrameListener(new InternalFrameAdapter()
              {
                public void internalFrameClosing(InternalFrameEvent e)
                {
                  sql.saveSize();
                  ((Main.ChildFrameMDI)frame).dispose();
                }
              });
            } else if ((frame instanceof Main.ChildFrameSDI)) {
              ((Main.ChildFrameSDI)frame).addWindowListener(new WindowAdapter()
              {
                public void windowClosing(WindowEvent e) {
                  sql.saveSize();
                  ((Main.ChildFrameSDI)frame).dispose();
                }
              });
            }
          } else {
            showWindow(getWindow(title));
          }
          
        }
        

      });
      final JMenuItem jMenuItemBayesNet = new JMenuItem();
      jMenuTools.add(jMenuItemBayesNet);
      Messages.getInstance();jMenuItemBayesNet.setText(Messages.getString("Main_InitGUI_JMenuItemBayesNet_SetText_Text"));
      
      jMenuItemBayesNet.setMnemonic('N');
      
      jMenuItemBayesNet.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e) {
          String title = jMenuItemBayesNet.getText();
          Container frame;
          if (!containsWindow(title)) {
            GUI bayesNetGUI = new GUI();
            frame = createFrame(m_Self, title, bayesNetGUI, new BorderLayout(), "Center", 800, 600, bayesNetGUI.getMenuBar(), false, true);
          }
          else
          {
            showWindow(getWindow(title));






          }
          






        }
        






      });
      jMenuVisualization = new JMenu();
      jMenuBar.add(jMenuVisualization);
      Messages.getInstance();jMenuVisualization.setText(Messages.getString("Main_InitGUI_JMenuVisualization_SetText_Text"));
      
      jMenuVisualization.setMnemonic('V');
      

      jMenuItemVisualizationPlot = new JMenuItem();
      jMenuVisualization.add(jMenuItemVisualizationPlot);
      Messages.getInstance();jMenuItemVisualizationPlot.setText(Messages.getString("Main_InitGUI_JMenuItemVisualizationPlot_SetText_Text"));
      
      jMenuItemVisualizationPlot.setMnemonic('P');
      jMenuItemVisualizationPlot.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt)
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
            Messages.getInstance();System.err.println(Messages.getString("Main_InitGUI_Error_Text") + filename);
            
            try
            {
              Reader r = new BufferedReader(new FileReader(filename));
              Instances i = new Instances(r);
              i.setClassIndex(i.numAttributes() - 1);
              PlotData2D pd1 = new PlotData2D(i);
              
              if (j == 0) {
                Messages.getInstance();pd1.setPlotName(Messages.getString("Main_InitGUI_Pd1_SetPlotName_Text_First"));
                
                panel.setMasterPlot(pd1);
              } else {
                Messages.getInstance();pd1.setPlotName(Messages.getString("Main_InitGUI_Pd1_SetPlotName_Text_Second") + (j + 1));
                

                m_useCustomColour = true;
                m_customColour = (j % 2 == 0 ? Color.red : Color.blue);
                panel.addPlot(pd1);
              }
            } catch (Exception e) {
              e.printStackTrace();
              Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(m_Self, Messages.getString("Main_InitGUI_Exception_JOptionPaneShowMessageDialog_Text_First") + files[j] + Messages.getString("Main_InitGUI_Exception_JOptionPaneShowMessageDialog_Text_Second") + e.getMessage());
              











              return;
            }
          }
          

          createFrame(m_Self, jMenuItemVisualizationPlot.getText() + " - " + filenames, panel, new BorderLayout(), "Center", 800, 600, null, true, true);


        }
        



      });
      jMenuItemVisualizationROC = new JMenuItem();
      jMenuVisualization.add(jMenuItemVisualizationROC);
      Messages.getInstance();jMenuItemVisualizationROC.setText(Messages.getString("Main_InitGUI_JMenuItemVisualizationROC_SetText_Text"));
      
      jMenuItemVisualizationROC.setMnemonic('R');
      jMenuItemVisualizationROC.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt)
        {
          int retVal = m_FileChooserROC.showOpenDialog(m_Self);
          if (retVal != 0) {
            return;
          }
          

          String filename = m_FileChooserROC.getSelectedFile().getAbsolutePath();
          
          Instances result = null;
          try {
            result = new Instances(new BufferedReader(new FileReader(filename)));
          } catch (Exception e) {
            e.printStackTrace();
            Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(m_Self, Messages.getString("Main_InitGUI_Exception_JOptionPaneShowMessageDialog_Text_Third") + filename + Messages.getString("Main_InitGUI_Exception_JOptionPaneShowMessageDialog_Text_Fourth") + e.getMessage());
            











            return;
          }
          result.setClassIndex(result.numAttributes() - 1);
          ThresholdVisualizePanel vmc = new ThresholdVisualizePanel();
          Messages.getInstance();vmc.setROCString(Messages.getString("Main_InitGUI_Vmc_SetROCString_Text") + Utils.doubleToString(ThresholdCurve.getROCArea(result), 4) + ")");
          

          vmc.setName(result.relationName());
          PlotData2D tempd = new PlotData2D(result);
          tempd.setPlotName(result.relationName());
          tempd.addInstanceNumberAttribute();
          try {
            vmc.addPlot(tempd);
          } catch (Exception e) {
            e.printStackTrace();
            Messages.getInstance();JOptionPane.showMessageDialog(m_Self, Messages.getString("Main_InitGUI_Exception_JOptionPaneShowMessageDialog_Text_Fifth") + e.getMessage());
            






            return;
          }
          
          createFrame(m_Self, jMenuItemVisualizationROC.getText() + " - " + filename, vmc, new BorderLayout(), "Center", 800, 600, null, true, true);

        }
        


      });
      jMenuItemVisualizationTreeVisualizer = new JMenuItem();
      jMenuVisualization.add(jMenuItemVisualizationTreeVisualizer);
      Messages.getInstance();jMenuItemVisualizationTreeVisualizer.setText(Messages.getString("Main_InitGUI_JMenuItemVisualizationTreeVisualizer_SetText_Text"));
      

      jMenuItemVisualizationTreeVisualizer.setMnemonic('T');
      jMenuItemVisualizationTreeVisualizer.addActionListener(new ActionListener()
      {

        public void actionPerformed(ActionEvent evt)
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
          } catch (Exception e) {
            e.printStackTrace();
            Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(m_Self, Messages.getString("Main_InitGUI_Exception_JOptionPaneShowMessageDialog_Text_Sixth") + filename + Messages.getString("Main_InitGUI_Exception_JOptionPaneShowMessageDialog_Text_Seventh") + e.getMessage());
            











            return;
          }
          

          createFrame(m_Self, jMenuItemVisualizationTreeVisualizer.getText() + " - " + filename, new TreeVisualizer(null, top, arrange), new BorderLayout(), "Center", 800, 600, null, true, true);


        }
        


      });
      jMenuItemVisualizationGraphVisualizer = new JMenuItem();
      jMenuVisualization.add(jMenuItemVisualizationGraphVisualizer);
      Messages.getInstance();jMenuItemVisualizationGraphVisualizer.setText(Messages.getString("Main_InitGUI_JMenuItemVisualizationGraphVisualizer_SetText_Text"));
      

      jMenuItemVisualizationGraphVisualizer.setMnemonic('G');
      jMenuItemVisualizationGraphVisualizer.addActionListener(new ActionListener()
      {

        public void actionPerformed(ActionEvent evt)
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
          } catch (Exception e) {
            e.printStackTrace();
            Messages.getInstance();Messages.getInstance();JOptionPane.showMessageDialog(m_Self, Messages.getString("Main_InitGUI_Exception_JOptionPaneShowMessageDialog_Text_Eighth") + filename + Messages.getString("Main_InitGUI_Exception_JOptionPaneShowMessageDialog_Text_nineth") + e.getMessage());
            











            return;
          }
          

          createFrame(m_Self, jMenuItemVisualizationGraphVisualizer.getText() + " - " + filename, panel, new BorderLayout(), "Center", 800, 600, null, true, true);

        }
        


      });
      jMenuItemVisualizationBoundaryVisualizer = new JMenuItem();
      jMenuVisualization.add(jMenuItemVisualizationBoundaryVisualizer);
      Messages.getInstance();jMenuItemVisualizationBoundaryVisualizer.setText(Messages.getString("Main_InitGUI_JMenuItemVisualizationBoundaryVisualizer_SetText_Text"));
      

      jMenuItemVisualizationBoundaryVisualizer.setMnemonic('B');
      jMenuItemVisualizationBoundaryVisualizer.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt)
        {
          String title = jMenuItemVisualizationBoundaryVisualizer.getText();
          if (!containsWindow(title)) {
            createFrame(m_Self, title, new BoundaryVisualizer(), new BorderLayout(), "Center", 800, 600, null, true, true);
            


            BoundaryVisualizer.setExitIfNoWindowsOpen(false);
          } else {
            showWindow(getWindow(title));
          }
          
        }
        
      });
      Messages.getInstance();jMenuExtensions = new JMenu(Messages.getString("Main_InitGUI_JMenuExtensions_JMenu_Text"));
      
      jMenuExtensions.setMnemonic(69);
      jMenuBar.add(jMenuExtensions);
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
      

      Messages.getInstance();jMenuWindows = new JMenu(Messages.getString("Main_InitGUI_JMenuWindows_JMenu_Text"));
      
      jMenuWindows.setMnemonic(87);
      jMenuBar.add(jMenuWindows);
      jMenuWindows.setVisible(false);
      

      jMenuHelp = new JMenu();
      jMenuBar.add(jMenuHelp);
      Messages.getInstance();jMenuHelp.setText(Messages.getString("Main_InitGUI_JMenuHelp_SetText_Text"));
      
      jMenuHelp.setMnemonic('H');
      

      jMenuItemHelpHomepage = new JMenuItem();
      jMenuHelp.add(jMenuItemHelpHomepage);
      Messages.getInstance();jMenuItemHelpHomepage.setText(Messages.getString("Main_InitGUI_JMenuItemHelpHomepage_SetText_Text"));
      
      jMenuItemHelpHomepage.setMnemonic('H');
      jMenuItemHelpHomepage.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt) {
          Messages.getInstance();BrowserHelper.openURL(m_Self, Messages.getString("Main_InitGUI_BrowserHelper_OpenURL_Text_First"));

        }
        


      });
      jMenuHelp.add(new JSeparator());
      











      jMenuItemHelpWekaWiki = new JMenuItem();
      jMenuHelp.add(jMenuItemHelpWekaWiki);
      Messages.getInstance();jMenuItemHelpWekaWiki.setText(Messages.getString("Main_InitGUI_JMenuItemHelpWekaWiki_SetText_Text"));
      
      jMenuItemHelpWekaWiki.setMnemonic('W');
      jMenuItemHelpWekaWiki.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt) {
          Messages.getInstance();BrowserHelper.openURL(m_Self, Messages.getString("Main_InitGUI_BrowserHelper_OpenURL_Text_Second"));


        }
        


      });
      jMenuItemHelpSourceforge = new JMenuItem();
      jMenuHelp.add(jMenuItemHelpSourceforge);
      Messages.getInstance();jMenuItemHelpSourceforge.setText(Messages.getString("Main_InitGUI_JMenuItemHelpSourceforge_SetText_Text"));
      
      jMenuItemHelpSourceforge.setMnemonic('F');
      jMenuItemHelpSourceforge.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt) {
          Messages.getInstance();BrowserHelper.openURL(m_Self, Messages.getString("Main_InitGUI_BrowserHelperOpenURL_Text_Third"));

        }
        


      });
      jMenuHelp.add(new JSeparator());
      

      jMenuItemHelpSystemInfo = new JMenuItem();
      jMenuHelp.add(jMenuItemHelpSystemInfo);
      Messages.getInstance();jMenuItemHelpSystemInfo.setText(Messages.getString("Main_InitGUI_JMenuItemHelpSystemInfo_SetText_Text"));
      
      jMenuItemHelpHomepage.setMnemonic('S');
      jMenuItemHelpSystemInfo.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt) {
          String title = jMenuItemHelpSystemInfo.getText();
          if (!containsWindow(title))
          {
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
            String[] tmp151_148 = new String[2];Messages.getInstance();tmp151_148[0] = Messages.getString("Main_InitGUI_Title_Text_First"); String[] tmp163_151 = tmp151_148;Messages.getInstance();tmp163_151[1] = Messages.getString("Main_InitGUI_Title_Text_Second");String[] titles = tmp163_151;
            


            JTable table = new JTable(data, titles);
            
            createFrame(m_Self, title, new JScrollPane(table), new BorderLayout(), "Center", 800, 600, null, true, true);
          }
          else
          {
            showWindow(getWindow(title));
          }
          
        }
      });
      jMenuHelp.add(new JSeparator());
      

      jMenuItemHelpAbout = new JMenuItem();
      jMenuHelp.add(jMenuItemHelpAbout);
      Messages.getInstance();jMenuItemHelpAbout.setText(Messages.getString("Main_InitGUI_JMenuItemHelpAbout_SetText_Text"));
      
      jMenuItemHelpAbout.setMnemonic('A');
      jMenuItemHelpAbout.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt) {
          String title = jMenuItemHelpAbout.getText();
          if (!containsWindow(title)) {
            JPanel wekaPan = new JPanel();
            Messages.getInstance();wekaPan.setToolTipText(Messages.getString("Main_InitGUI_WekaPan_JPanel_SetToolTipText_Text"));
            
            ImageIcon wii = new ImageIcon(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("weka/gui/weka3.gif")));
            
            JLabel wekaLab = new JLabel(wii);
            wekaPan.add(wekaLab);
            Container frame = createFrame(m_Self, title, wekaPan, new BorderLayout(), "Center", -1, -1, null, true, true);
            

            JPanel titlePan = new JPanel();
            titlePan.setLayout(new GridLayout(8, 1));
            titlePan.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
            Messages.getInstance();titlePan.add(new JLabel(Messages.getString("Main_InitGUI_TitlePan_Add_JLabel_Text_First"), 0));
            

            Messages.getInstance();titlePan.add(new JLabel(Messages.getString("Main_InitGUI_TitlePan_Add_JLabel_Text_Second"), 0));
            

            Messages.getInstance();titlePan.add(new JLabel(Messages.getString("Main_InitGUI_TitlePan_Add_JLabel_Text_Third")));
            
            Messages.getInstance();titlePan.add(new JLabel(Messages.getString("Main_InitGUI_TitlePan_Add_JLabel_Text_Fourth") + Version.VERSION, 0));
            

            Messages.getInstance();titlePan.add(new JLabel(Messages.getString("Main_InitGUI_TitlePan_Add_JLabel_Text_Fifth")));
            
            Messages.getInstance();titlePan.add(new JLabel(Messages.getString("Main_InitGUI_TitlePan_Add_JLabel_Text_Sixth") + Copyright.getFromYear() + " - " + Copyright.getToYear(), 0));
            


            titlePan.add(new JLabel(Copyright.getOwner(), 0));
            titlePan.add(new JLabel(Copyright.getAddress(), 0));
            

            if ((frame instanceof Main.ChildFrameMDI)) {
              ((Main.ChildFrameMDI)frame).getContentPane().add(titlePan, "North");
              
              ((Main.ChildFrameMDI)frame).pack();
            } else if ((frame instanceof Main.ChildFrameSDI)) {
              ((Main.ChildFrameSDI)frame).getContentPane().add(titlePan, "North");
              
              ((Main.ChildFrameSDI)frame).pack();
            }
          } else {
            showWindow(getWindow(title));
          }
          
        }
        
      });
      int screenHeight = getGraphicsConfigurationgetBoundsheight;
      int screenWidth = getGraphicsConfigurationgetBoundswidth;
      if (m_GUIType == 0) {
        int newHeight = (int)(screenHeight * 0.75D);
        int newWidth = (int)(screenWidth * 0.75D);
        setSize(1000 > newWidth ? newWidth : 1000, 800 > newHeight ? newHeight : 800);
        
        setLocation((screenWidth - getBoundswidth) / 2, (screenHeight - getBoundsheight) / 2);
      }
      else if (m_GUIType == 1) {
        pack();
        setSize(screenWidth, getHeight());
        setLocation(0, 0);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  






  protected void createTitle(String title)
  {
    Messages.getInstance();String newTitle = Messages.getString("Main_CreateTitle_NewTitle_Text") + new Version();
    

    if (title.length() != 0) {
      newTitle = newTitle + " - " + title;
    }
    
    setTitle(newTitle);
  }
  




  public void addChildFrame(Container c)
  {
    m_ChildFrames.add(c);
    windowListChanged();
  }
  





  public boolean removeChildFrame(Container c)
  {
    boolean result = m_ChildFrames.remove(c);
    windowListChanged();
    return result;
  }
  



  public boolean showWindow(Container c)
  {
    boolean result;
    

    boolean result;
    

    if (c != null) {
      try {
        if ((c instanceof ChildFrameMDI)) {
          ChildFrameMDI mdiFrame = (ChildFrameMDI)c;
          mdiFrame.setIcon(false);
          mdiFrame.toFront();
          createTitle(mdiFrame.getTitle());
        } else if ((c instanceof ChildFrameSDI)) {
          ChildFrameSDI sdiFrame = (ChildFrameSDI)c;
          sdiFrame.setExtendedState(0);
          sdiFrame.toFront();
          createTitle(sdiFrame.getTitle());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      result = true;
    } else {
      result = false;
    }
    
    return result;
  }
  





  public boolean showWindow(Class windowClass)
  {
    return showWindow(getWindow(windowClass));
  }
  




  public Iterator getWindowList()
  {
    return m_ChildFrames.iterator();
  }
  










  public Container getWindow(Class windowClass)
  {
    Container result = null;
    Iterator iter = getWindowList();
    while (iter.hasNext()) {
      Container current = (Container)iter.next();
      if (current.getClass() == windowClass) {
        result = current;
      }
    }
    

    return result;
  }
  










  public Container getWindow(String title)
  {
    Container result = null;
    Iterator iter = getWindowList();
    while (iter.hasNext()) {
      Container current = (Container)iter.next();
      boolean found = false;
      
      if ((current instanceof ChildFrameMDI)) {
        found = ((ChildFrameMDI)current).getTitle().equals(title);
      } else if ((current instanceof ChildFrameSDI)) {
        found = ((ChildFrameSDI)current).getTitle().equals(title);
      }
      
      if (found) {
        result = current;
      }
    }
    

    return result;
  }
  







  public boolean containsWindow(Class windowClass)
  {
    return getWindow(windowClass) != null;
  }
  







  public boolean containsWindow(String title)
  {
    return getWindow(title) != null;
  }
  





  public void minimizeWindows()
  {
    Iterator iter = getWindowList();
    while (iter.hasNext()) {
      Container frame = (Container)iter.next();
      try {
        if ((frame instanceof ChildFrameMDI)) {
          ((ChildFrameMDI)frame).setIcon(true);
        } else if ((frame instanceof ChildFrameSDI)) {
          ((ChildFrameSDI)frame).setExtendedState(1);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  





  public void restoreWindows()
  {
    Iterator iter = getWindowList();
    while (iter.hasNext()) {
      Container frame = (Container)iter.next();
      try {
        if ((frame instanceof ChildFrameMDI)) {
          ((ChildFrameMDI)frame).setIcon(false);
        } else if ((frame instanceof ChildFrameSDI)) {
          ((ChildFrameSDI)frame).setExtendedState(0);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  


  public void windowListChanged()
  {
    createWindowMenu();
  }
  







  protected synchronized void createWindowMenu()
  {
    jMenuWindows.removeAll();
    

    Messages.getInstance();JMenuItem menuItem = new JMenuItem(Messages.getString("Main_CreateWindowMenu_MenuItem_JMenuItem_Text_First"));
    
    menuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt) {
        minimizeWindows();
      }
    });
    jMenuWindows.add(menuItem);
    
    Messages.getInstance();menuItem = new JMenuItem(Messages.getString("Main_CreateWindowMenu_MenuItem_JMenuItem_Text_Second"));
    
    menuItem.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt) {
        restoreWindows();
      }
    });
    jMenuWindows.add(menuItem);
    
    jMenuWindows.addSeparator();
    

    int startIndex = jMenuWindows.getMenuComponentCount() - 1;
    Iterator iter = getWindowList();
    jMenuWindows.setVisible(iter.hasNext());
    while (iter.hasNext()) {
      Container frame = (Container)iter.next();
      if ((frame instanceof ChildFrameMDI)) {
        menuItem = new JMenuItem(((ChildFrameMDI)frame).getTitle());
      } else if ((frame instanceof ChildFrameSDI)) {
        menuItem = new JMenuItem(((ChildFrameSDI)frame).getTitle());
      }
      insertMenuItem(jMenuWindows, menuItem, startIndex);
      menuItem.setActionCommand(Integer.toString(frame.hashCode()));
      menuItem.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt) {
          Container frame = null;
          Iterator iter = getWindowList();
          while (iter.hasNext()) {
            frame = (Container)iter.next();
            String hashFrame = Integer.toString(frame.hashCode());
            if (hashFrame.equals(evt.getActionCommand())) {
              showWindow(frame);
              break;
            }
          }
          showWindow(frame);
        }
      });
    }
  }
  





  public void setVisible(boolean b)
  {
    super.setVisible(b);
    
    if (b) {
      paint(getGraphics());
    }
  }
  




  public static void createSingleton(String[] args)
  {
    if (m_MainSingleton == null) {
      m_MainSingleton = new Main();
    }
    
    try
    {
      m_MainSingleton.setOptions(args);
    } catch (Exception e) {
      e.printStackTrace();
    }
    

    for (int i = 0; i < m_StartupListeners.size(); i++) {
      ((StartUpListener)m_StartupListeners.elementAt(i)).startUpComplete();
    }
  }
  




  public static Main getSingleton()
  {
    return m_MainSingleton;
  }
  




  public static void addStartupListener(StartUpListener s)
  {
    m_StartupListeners.add(s);
  }
  










  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    String desc = "";
    for (int i = 0; i < TAGS_GUI.length; i++) {
      SelectedTag tag = new SelectedTag(TAGS_GUI[i].getID(), TAGS_GUI);
      desc = desc + "\t" + tag.getSelectedTag().getIDStr() + " = " + tag.getSelectedTag().getReadable() + "\n";
    }
    
    Messages.getInstance();Messages.getInstance();result.addElement(new Option(Messages.getString("Main_ListOptions_Option_Text_First") + desc + Messages.getString("Main_ListOptions_Option_Text_Second") + new SelectedTag(0, TAGS_GUI) + ")", "gui", 1, "-gui " + Tag.toOptionList(TAGS_GUI)));
    





    return result.elements();
  }
  







  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-gui");
    result.add("" + getGUIType());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





















  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption("gui", options);
    if (tmpStr.length() != 0) {
      setGUIType(new SelectedTag(tmpStr, TAGS_GUI));
    } else {
      setGUIType(new SelectedTag(0, TAGS_GUI));
    }
  }
  




  public void setGUIType(SelectedTag value)
  {
    if (value.getTags() == TAGS_GUI) {
      m_GUIType = value.getSelectedTag().getID();
      initGUI();
    }
  }
  




  public SelectedTag getGUIType()
  {
    return new SelectedTag(m_GUIType, TAGS_GUI);
  }
  




  public static void main(String[] args)
  {
    Messages.getInstance();Logger.log(Logger.Level.INFO, Messages.getString("Main_Main_Logger_Text"));
    

    LookAndFeel.setLookAndFeel();
    



    try
    {
      if (Utils.getFlag('h', args)) {
        System.out.println();
        Messages.getInstance();System.out.println(Messages.getString("Main_Main_Text_First"));
        
        System.out.println();
        Messages.getInstance();System.out.println(Messages.getString("Main_Main_Text_Second"));
        
        System.out.println();
        System.out.println("-h");
        Messages.getInstance();System.out.println(Messages.getString("Main_Main_Text_Third"));
        
        System.out.println();
        
        Enumeration enu = new Main().listOptions();
        while (enu.hasMoreElements()) {
          Option option = (Option)enu.nextElement();
          System.out.println(option.synopsis());
          System.out.println(option.description());
        }
        
        System.out.println();
        System.exit(0);
      }
      

      addStartupListener(new StartUpListener()
      {
        public void startUpComplete() {
          Main.m_MainCommandline = Main.getSingleton();
          Main.m_MainCommandline.setVisible(true);
        }
      });
      addStartupListener(new StartUpListener()
      {

        public void startUpComplete() {}

      });
      SplashWindow.splash(ClassLoader.getSystemResource("weka/gui/images/weka_splash.gif"));
      


      String[] options = (String[])args.clone();
      Thread nt = new Thread()
      {
        public void run() {
          SplashWindow.invokeMethod(Main.class.getName(), "createSingleton", val$options);
        }
        
      };
      nt.start();
      
      Thread memMonitor = new Thread()
      {

        public void run()
        {

          for (;;)
          {
            if (Main.m_Memory.isOutOfMemory())
            {
              Main.m_MainCommandline = null;
              System.gc();
              

              Messages.getInstance();System.err.println(Messages.getString("Main_Main_Thread_Run_Error_Text_First"));
              
              Main.m_Memory.showOutOfMemory();
              Messages.getInstance();System.err.println(Messages.getString("Main_Main_Thread_Run_Error_Text_Second"));
              
              System.exit(-1);
            }
            
          }
          
        }
        
      };
      memMonitor.setPriority(10);
      memMonitor.start();
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
  
  public Main() {}
}
