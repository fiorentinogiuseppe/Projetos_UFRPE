package weka.gui.arffviewer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.PrintStream;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import weka.core.Memory;
import weka.core.logging.Logger;
import weka.core.logging.Logger.Level;
import weka.gui.ComponentHelper;
import weka.gui.LookAndFeel;






























public class ArffViewer
  extends JFrame
  implements WindowListener
{
  static final long serialVersionUID = -7455845566922685175L;
  private ArffViewerMainPanel m_MainPanel;
  private static Memory m_Memory = new Memory(true);
  

  private static ArffViewer m_Viewer;
  

  private static boolean m_FilesLoaded;
  

  private static String[] m_Args;
  


  public ArffViewer()
  {
    super(Messages.getString("ArffViewer_ArffViewer_Text"));
    createFrame();
  }
  



  protected void createFrame()
  {
    setIconImage(ComponentHelper.getImage("weka_icon.gif"));
    setSize(800, 600);
    setCenteredLocation();
    setDefaultCloseOperation(2);
    

    removeWindowListener(this);
    
    addWindowListener(this);
    
    getContentPane().setLayout(new BorderLayout());
    
    m_MainPanel = new ArffViewerMainPanel(this);
    m_MainPanel.setConfirmExit(false);
    getContentPane().add(m_MainPanel, "Center");
    
    setJMenuBar(m_MainPanel.getMenu());
  }
  







  protected int getCenteredLeft()
  {
    int width = getBoundswidth;
    int x = (getGraphicsConfigurationgetBoundswidth - width) / 2;
    
    if (x < 0) {
      x = 0;
    }
    
    return x;
  }
  







  protected int getCenteredTop()
  {
    int height = getBoundsheight;
    int y = (getGraphicsConfigurationgetBoundsheight - height) / 2;
    
    if (y < 0) {
      y = 0;
    }
    
    return y;
  }
  


  public void setCenteredLocation()
  {
    setLocation(getCenteredLeft(), getCenteredTop());
  }
  




  public void setConfirmExit(boolean confirm)
  {
    m_MainPanel.setConfirmExit(confirm);
  }
  





  public boolean getConfirmExit()
  {
    return m_MainPanel.getConfirmExit();
  }
  




  public void setExitOnClose(boolean value)
  {
    m_MainPanel.setExitOnClose(value);
  }
  




  public boolean getExitOnClose()
  {
    return m_MainPanel.getExitOnClose();
  }
  




  public ArffViewerMainPanel getMainPanel()
  {
    return m_MainPanel;
  }
  


  public void refresh()
  {
    validate();
    repaint();
  }
  







  public void windowActivated(WindowEvent e) {}
  







  public void windowClosed(WindowEvent e) {}
  







  public void windowClosing(WindowEvent e)
  {
    while (getMainPanel().getTabbedPane().getTabCount() > 0) {
      getMainPanel().closeFile(false);
    }
    
    if (getConfirmExit()) {
      Messages.getInstance();Messages.getInstance();int button = ComponentHelper.showMessageBox(this, Messages.getString("ArffViewer_WindowClosing_ComponentHelperShowMessageBox_Text_First") + getTitle(), Messages.getString("ArffViewer_WindowClosing_ComponentHelperShowMessageBox_Text_Second"), 0, 3);
      











      if (button == 0) {
        dispose();
      }
    } else {
      dispose();
    }
    
    if (getExitOnClose()) {
      System.exit(0);
    }
  }
  







  public void windowDeactivated(WindowEvent e) {}
  







  public void windowDeiconified(WindowEvent e) {}
  







  public void windowIconified(WindowEvent e) {}
  






  public void windowOpened(WindowEvent e) {}
  






  public String toString()
  {
    return getClass().getName();
  }
  





  public static void main(String[] args)
    throws Exception
  {
    Messages.getInstance();Logger.log(Logger.Level.INFO, Messages.getString("ArffViewer_Main_Logger_Text"));
    
    LookAndFeel.setLookAndFeel();
    


    try
    {
      m_Viewer = new ArffViewer();
      m_Viewer.setExitOnClose(true);
      m_Viewer.setVisible(true);
      m_FilesLoaded = false;
      m_Args = args;
      
      Thread memMonitor = new Thread()
      {
        public void run()
        {
          for (;;) {
            if ((ArffViewer.m_Args.length > 0) && (!ArffViewer.m_FilesLoaded)) {
              for (int i = 0; i < ArffViewer.m_Args.length; i++) {
                System.out.println("Loading " + (i + 1) + "/" + ArffViewer.m_Args.length + ": '" + ArffViewer.m_Args[i] + "'...");
                
                ArffViewer.m_Viewer.getMainPanel().loadFile(ArffViewer.m_Args[i]);
              }
              ArffViewer.m_Viewer.getMainPanel().getTabbedPane().setSelectedIndex(0);
              System.out.println("Finished!");
              ArffViewer.access$102(true);
            }
            





            if (ArffViewer.m_Memory.isOutOfMemory())
            {
              ArffViewer.m_Viewer.dispose();
              ArffViewer.access$202(null);
              System.gc();
              

              ArffViewer.m_Memory.stopThreads();
              

              Messages.getInstance();System.err.println(Messages.getString("ArffViewer_Main_Error_DisplayedMessage_Text"));
              
              ArffViewer.m_Memory.showOutOfMemory();
              Messages.getInstance();System.err.println(Messages.getString("ArffViewer_Main_Error_Restarting_Text"));
              


              System.gc();
              ArffViewer.access$202(new ArffViewer());
              ArffViewer.m_Viewer.setExitOnClose(true);
              ArffViewer.m_Viewer.setVisible(true);

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
