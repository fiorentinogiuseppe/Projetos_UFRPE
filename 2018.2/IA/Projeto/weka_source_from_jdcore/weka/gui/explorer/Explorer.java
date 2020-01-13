package weka.gui.explorer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import weka.core.Capabilities;
import weka.core.Copyright;
import weka.core.Instances;
import weka.core.Memory;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ConverterUtils;
import weka.core.logging.Logger.Level;
import weka.gui.LogPanel;
import weka.gui.LookAndFeel;
import weka.gui.WekaTaskMonitor;










































public class Explorer
  extends JPanel
{
  private static final long serialVersionUID = -7674003708867909578L;
  
  public static abstract interface LogHandler
  {
    public abstract void setLog(weka.gui.Logger paramLogger);
  }
  
  public static abstract interface ExplorerPanel
  {
    public abstract void setExplorer(Explorer paramExplorer);
    
    public abstract Explorer getExplorer();
    
    public abstract void setInstances(Instances paramInstances);
    
    public abstract String getTabTitle();
    
    public abstract String getTabTitleToolTip();
  }
  
  public static class CapabilitiesFilterChangeEvent
    extends ChangeEvent
  {
    private static final long serialVersionUID = 1194260517270385559L;
    protected Capabilities m_Filter;
    
    public CapabilitiesFilterChangeEvent(Object source, Capabilities filter)
    {
      super();
      m_Filter = filter;
    }
    




    public Capabilities getFilter()
    {
      return m_Filter;
    }
  }
  






























































  protected PreprocessPanel m_PreprocessPanel = new PreprocessPanel();
  

  protected Vector<ExplorerPanel> m_Panels = new Vector();
  

  protected JTabbedPane m_TabbedPane = new JTabbedPane();
  

  protected LogPanel m_LogPanel = new LogPanel(new WekaTaskMonitor());
  

  protected HashSet<CapabilitiesFilterChangeListener> m_CapabilitiesFilterChangeListeners = new HashSet();
  
  private static Explorer m_explorer;
  

  public Explorer()
  {
    String date = new SimpleDateFormat("EEEE, d MMMM yyyy").format(new Date());
    
    Messages.getInstance();m_LogPanel.logMessage(Messages.getString("Explorer_LogPanel_LogMessage_Text_First"));
    
    Messages.getInstance();Messages.getInstance();m_LogPanel.logMessage(Messages.getString("Explorer_LogPanel_LogMessage_Text_Second") + Copyright.getFromYear() + Messages.getString("Explorer_LogPanel_LogMessage_Text_Third") + Copyright.getToYear() + " " + Copyright.getOwner() + ", " + Copyright.getAddress());
    





    Messages.getInstance();m_LogPanel.logMessage(Messages.getString("Explorer_LogPanel_LogMessage_Text_Fourth") + Copyright.getURL());
    

    Messages.getInstance();m_LogPanel.logMessage(Messages.getString("Explorer_LogPanel_LogMessage_Text_Fifth") + date);
    

    Messages.getInstance();m_LogPanel.statusMessage(Messages.getString("Explorer_LogPanel_StatusMessage_Text_First"));
    


    m_PreprocessPanel.setLog(m_LogPanel);
    m_TabbedPane.addTab(m_PreprocessPanel.getTabTitle(), null, m_PreprocessPanel, m_PreprocessPanel.getTabTitleToolTip());
    


    String[] tabs = ExplorerDefaults.getTabs();
    Hashtable<String, HashSet> tabOptions = new Hashtable();
    for (String tab : tabs) {
      try
      {
        String[] optionsStr = tab.split(":");
        String classname = optionsStr[0];
        HashSet options = new HashSet();
        tabOptions.put(classname, options);
        for (int n = 1; n < optionsStr.length; n++) {
          options.add(optionsStr[n]);
        }
        

        ExplorerPanel panel = (ExplorerPanel)Class.forName(classname).newInstance();
        
        panel.setExplorer(this);
        m_Panels.add(panel);
        if ((panel instanceof LogHandler)) {
          ((LogHandler)panel).setLog(m_LogPanel);
        }
        m_TabbedPane.addTab(panel.getTabTitle(), null, (JPanel)panel, panel.getTabTitleToolTip());
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    

    m_TabbedPane.setSelectedIndex(0);
    for (int i = 0; i < m_Panels.size(); i++) {
      HashSet options = (HashSet)tabOptions.get(((ExplorerPanel)m_Panels.get(i)).getClass().getName());
      m_TabbedPane.setEnabledAt(i + 1, options.contains("standalone"));
    }
    

    m_PreprocessPanel.addPropertyChangeListener(new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent e) {
        for (int i = 0; i < m_Panels.size(); i++) {
          ((Explorer.ExplorerPanel)m_Panels.get(i)).setInstances(m_PreprocessPanel.getInstances());
          m_TabbedPane.setEnabledAt(i + 1, true);
        }
        
      }
      
    });
    m_PreprocessPanel.setExplorer(this);
    addCapabilitiesFilterListener(m_PreprocessPanel);
    for (int i = 0; i < m_Panels.size(); i++) {
      if ((m_Panels.get(i) instanceof CapabilitiesFilterChangeListener)) {
        addCapabilitiesFilterListener((CapabilitiesFilterChangeListener)m_Panels.get(i));
      }
    }
    


    setLayout(new BorderLayout());
    add(m_TabbedPane, "Center");
    add(m_LogPanel, "South");
  }
  




  public Vector<ExplorerPanel> getPanels()
  {
    return m_Panels;
  }
  





  public PreprocessPanel getPreprocessPanel()
  {
    return m_PreprocessPanel;
  }
  




  public JTabbedPane getTabbedPane()
  {
    return m_TabbedPane;
  }
  






  public void addCapabilitiesFilterListener(CapabilitiesFilterChangeListener l)
  {
    m_CapabilitiesFilterChangeListeners.add(l);
  }
  






  public boolean removeCapabilitiesFilterListener(CapabilitiesFilterChangeListener l)
  {
    return m_CapabilitiesFilterChangeListeners.remove(l);
  }
  




  public void notifyCapabilitiesFilterListener(Capabilities filter)
  {
    for (CapabilitiesFilterChangeListener l : m_CapabilitiesFilterChangeListeners) {
      if (l != this)
      {

        l.capabilitiesFilterChanged(new CapabilitiesFilterChangeEvent(this, filter));
      }
    }
  }
  







  protected static Memory m_Memory = new Memory(true);
  





  public static void main(String[] args)
  {
    Messages.getInstance();weka.core.logging.Logger.log(Logger.Level.INFO, Messages.getString("Explorer_Main_Logger_Text"));
    

    LookAndFeel.setLookAndFeel();
    


    try
    {
      m_explorer = new Explorer();
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("Explorer_Main_JFrame_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(m_explorer, "Center");
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
      Image icon = Toolkit.getDefaultToolkit().getImage(m_explorer.getClass().getClassLoader().getResource("weka/gui/weka_icon_new_48.png"));
      

      jf.setIconImage(icon);
      
      if (args.length == 1) {
        Messages.getInstance();System.err.println(Messages.getString("Explorer_Main_Run_Error_Text") + args[0]);
        

        AbstractFileLoader loader = ConverterUtils.getLoaderForFile(args[0]);
        loader.setFile(new File(args[0]));
        m_explorerm_PreprocessPanel.setInstancesFromFile(loader);
      }
      
      Thread memMonitor = new Thread()
      {


        public void run()
        {

          for (;;)
          {

            if (Explorer.m_Memory.isOutOfMemory())
            {
              val$jf.dispose();
              Explorer.access$002(null);
              System.gc();
              

              Messages.getInstance();System.err.println(Messages.getString("Explorer_Main_Run_Error_Text_First"));
              
              Explorer.m_Memory.showOutOfMemory();
              Messages.getInstance();System.err.println(Messages.getString("Explorer_Main_Run_Error_Text_Second"));
              
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
  
  public static abstract interface CapabilitiesFilterChangeListener
    extends EventListener
  {
    public abstract void capabilitiesFilterChanged(Explorer.CapabilitiesFilterChangeEvent paramCapabilitiesFilterChangeEvent);
  }
}
