package weka.gui.experiment;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintStream;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import weka.core.Memory;
import weka.core.logging.Logger;
import weka.core.logging.Logger.Level;
import weka.experiment.Experiment;
import weka.gui.LookAndFeel;




































public class Experimenter
  extends JPanel
{
  private static final long serialVersionUID = -5751617505738193788L;
  protected SetupModePanel m_SetupPanel;
  protected RunPanel m_RunPanel;
  protected ResultsPanel m_ResultsPanel;
  protected JTabbedPane m_TabbedPane = new JTabbedPane();
  




  protected boolean m_ClassFirst = false;
  
  private static Experimenter m_experimenter;
  

  public Experimenter(boolean classFirst)
  {
    Messages.getInstance();System.out.println("[DEBUGGER] ---- " + Messages.getString("Experimenter_TabbedPane_Setup_Key_Text"));
    


    m_SetupPanel = new SetupModePanel();
    m_ResultsPanel = new ResultsPanel();
    m_RunPanel = new RunPanel();
    m_RunPanel.setResultsPanel(m_ResultsPanel);
    
    m_ClassFirst = classFirst;
    
    Messages.getInstance();Messages.getInstance();m_TabbedPane.addTab(Messages.getString("Experimenter_TabbedPane_Setup_Key_Text"), null, m_SetupPanel, Messages.getString("Experimenter_TabbedPane_Setup_Value_Text"));
    





    Messages.getInstance();Messages.getInstance();m_TabbedPane.addTab(Messages.getString("Experimenter_TabbedPane_Run_Key_Text"), null, m_RunPanel, Messages.getString("Experimenter_TabbedPane_Run_Value_Text"));
    



    Messages.getInstance();Messages.getInstance();m_TabbedPane.addTab(Messages.getString("Experimenter_TabbedPane_Analyse_Key_Text"), null, m_ResultsPanel, Messages.getString("Experimenter_TabbedPane_Analyse_Value_Text"));
    





    m_TabbedPane.setSelectedIndex(0);
    m_TabbedPane.setEnabledAt(1, false);
    m_SetupPanel.addPropertyChangeListener(new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent e)
      {
        Experiment exp = m_SetupPanel.getExperiment();
        exp.classFirst(m_ClassFirst);
        m_RunPanel.setExperiment(exp);
        
        m_TabbedPane.setEnabledAt(1, true);
      }
    });
    setLayout(new BorderLayout());
    add(m_TabbedPane, "Center");
  }
  







  protected static Memory m_Memory = new Memory(true);
  




  public static void main(String[] args)
  {
    Messages.getInstance();Logger.log(Logger.Level.INFO, Messages.getString("Experimenter_Main_LoggingStarted_Text"));
    
    LookAndFeel.setLookAndFeel();
    


    try
    {
      boolean classFirst = false;
      if (args.length > 0) {
        classFirst = args[0].equals("CLASS_FIRST");
      }
      m_experimenter = new Experimenter(classFirst);
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("Experimenter_Main_WekaExperimentEnvironment_JFrame_Text"));
      
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(m_experimenter, "Center");
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
      
      Image icon = Toolkit.getDefaultToolkit().getImage(m_experimenter.getClass().getClassLoader().getResource("weka/gui/weka_icon_new_48.png"));
      

      jf.setIconImage(icon);
      
      Thread memMonitor = new Thread()
      {

        public void run()
        {

          for (;;)
          {

            if (Experimenter.m_Memory.isOutOfMemory())
            {
              val$jf.dispose();
              Experimenter.access$002(null);
              System.gc();
              

              Messages.getInstance();System.err.println(Messages.getString("Experimenter_Main_Error_Text_First"));
              
              Experimenter.m_Memory.showOutOfMemory();
              Messages.getInstance();System.err.println(Messages.getString("Experimenter_Main_Error_Text_Second"));
              
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
