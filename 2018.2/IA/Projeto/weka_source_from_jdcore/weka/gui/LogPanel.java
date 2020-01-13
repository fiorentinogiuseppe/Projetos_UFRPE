package weka.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import weka.core.logging.Logger.Level;












































public class LogPanel
  extends JPanel
  implements Logger, TaskLogger
{
  private static final long serialVersionUID = -4072464549112439484L;
  protected JLabel m_StatusLab;
  protected JTextArea m_LogText;
  protected JButton m_logButton;
  protected boolean m_First;
  protected WekaTaskMonitor m_TaskMonitor;
  
  public LogPanel()
  {
    this(null, false, false, true);
  }
  






  public LogPanel(WekaTaskMonitor tm)
  {
    this(tm, true, false, true);
  }
  








  public LogPanel(WekaTaskMonitor tm, boolean logHidden)
  {
    this(tm, logHidden, false, true);
  }
  
  public LogPanel(WekaTaskMonitor tm, boolean logHidden, boolean statusHidden, boolean titledBorder)
  {
    Messages.getInstance();m_StatusLab = new JLabel(Messages.getString("LogPanel_TaskLogger_StatusLab_JLabel_Text"));
    

    m_LogText = new JTextArea(4, 20);
    

    Messages.getInstance();m_logButton = new JButton(Messages.getString("LogPanel_TaskLogger_LogButton_JButton_Text"));
    

    m_First = true;
    

    m_TaskMonitor = null;
    


















































    m_TaskMonitor = tm;
    m_LogText.setEditable(false);
    m_LogText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    Messages.getInstance();m_StatusLab.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("LogPanel_StatusLab_SetBorder_BorderFactoryCreateCompoundBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    



    JScrollPane js = new JScrollPane(m_LogText);
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
    
    if (logHidden)
    {

      Messages.getInstance();final JFrame jf = new JFrame(Messages.getString("LogPanel_Jf_JFrame_Text"));
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          jf.setVisible(false);
        }
      });
      jf.getContentPane().setLayout(new BorderLayout());
      jf.getContentPane().add(js, "Center");
      jf.pack();
      jf.setSize(450, 350);
      

      m_logButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          jf.setVisible(true);
        }
        

      });
      setLayout(new BorderLayout());
      JPanel logButPanel = new JPanel();
      logButPanel.setLayout(new BorderLayout());
      logButPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
      logButPanel.add(m_logButton, "Center");
      JPanel p1 = new JPanel();
      p1.setLayout(new BorderLayout());
      p1.add(m_StatusLab, "Center");
      p1.add(logButPanel, "East");
      
      if (tm == null) {
        add(p1, "South");
      } else {
        JPanel p2 = new JPanel();
        p2.setLayout(new BorderLayout());
        p2.add(p1, "Center");
        p2.add(m_TaskMonitor, "East");
        add(p2, "South");
      }
    }
    else
    {
      JPanel p1 = new JPanel();
      if (titledBorder) {
        Messages.getInstance();p1.setBorder(BorderFactory.createTitledBorder(Messages.getString("LogPanel_P1_SetBorder_BorderFactoryCreateTitledBorder_Text")));
      }
      p1.setLayout(new BorderLayout());
      p1.add(js, "Center");
      setLayout(new BorderLayout());
      add(p1, "Center");
      
      if (tm == null) {
        if (!statusHidden) {
          add(m_StatusLab, "South");
        }
      }
      else if (!statusHidden) {
        JPanel p2 = new JPanel();
        p2.setLayout(new BorderLayout());
        p2.add(m_StatusLab, "Center");
        p2.add(m_TaskMonitor, "East");
        add(p2, "South");
      }
    }
    
    addPopup();
  }
  









  private String printLong(long l)
  {
    String str = Long.toString(l);
    String result = "";
    int count = 0;
    
    for (int i = str.length() - 1; i >= 0; i--) {
      count++;
      result = str.charAt(i) + result;
      if ((count == 3) && (i > 0)) {
        result = "," + result;
        count = 0;
      }
    }
    
    return result;
  }
  



  private void addPopup()
  {
    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (((e.getModifiers() & 0x10) != 16) || (e.isAltDown()))
        {
          JPopupMenu gcMenu = new JPopupMenu();
          Messages.getInstance();JMenuItem availMem = new JMenuItem(Messages.getString("LogPanel_AddPopup_AvailMem_JMenuItem_Text"));
          availMem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ee) {
              System.gc();
              Runtime currR = Runtime.getRuntime();
              long freeM = currR.freeMemory();
              long totalM = currR.totalMemory();
              long maxM = currR.maxMemory();
              Messages.getInstance();Messages.getInstance();Messages.getInstance();logMessage(Messages.getString("LogPanel_AddPopup_LogMessage_Text_First") + LogPanel.this.printLong(freeM) + Messages.getString("LogPanel_AddPopup_LogMessage_Text_Second") + LogPanel.this.printLong(totalM) + Messages.getString("LogPanel_AddPopup_LogMessage_Text_Third") + LogPanel.this.printLong(maxM));
              Messages.getInstance();Messages.getInstance();Messages.getInstance();statusMessage(Messages.getString("LogPanel_AddPopup_StatusMessage_Text_First") + LogPanel.this.printLong(freeM) + Messages.getString("LogPanel_AddPopup_StatusMessage_Text_Second") + LogPanel.this.printLong(totalM) + Messages.getString("LogPanel_AddPopup_StatusMessage_Text_Third") + LogPanel.this.printLong(maxM));
            }
          });
          gcMenu.add(availMem);
          Messages.getInstance();JMenuItem runGC = new JMenuItem(Messages.getString("LogPanel_AddPopup_RunGC_JMenuItem_Text"));
          runGC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ee) {
              Messages.getInstance();statusMessage(Messages.getString("LogPanel_AddPopup_StatusMessage_Text_Fourth"));
              System.gc();
              Messages.getInstance();statusMessage(Messages.getString("LogPanel_AddPopup_StatusMessage_Text_Fifth"));
            }
          });
          gcMenu.add(runGC);
          gcMenu.show(LogPanel.this, e.getX(), e.getY());
        }
      }
    });
  }
  


  public void taskStarted()
  {
    if (m_TaskMonitor != null) {
      m_TaskMonitor.taskStarted();
    }
  }
  


  public void taskFinished()
  {
    if (m_TaskMonitor != null) {
      m_TaskMonitor.taskFinished();
    }
  }
  





  protected static String getTimestamp()
  {
    return new SimpleDateFormat("HH:mm:ss:").format(new Date());
  }
  






  public synchronized void logMessage(String message)
  {
    if (m_First) {
      m_First = false;
    } else {
      m_LogText.append("\n");
    }
    m_LogText.append(getTimestamp() + ' ' + message);
    weka.core.logging.Logger.log(Logger.Level.INFO, message);
  }
  





  public synchronized void statusMessage(String message)
  {
    m_StatusLab.setText(message);
  }
  





  public static void main(String[] args)
  {
    try
    {
      Messages.getInstance();JFrame jf = new JFrame(Messages.getString("LogPanel_Main_JFrame_Text"));
      jf.getContentPane().setLayout(new BorderLayout());
      LogPanel lp = new LogPanel();
      jf.getContentPane().add(lp, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
      Messages.getInstance();lp.logMessage(Messages.getString("LogPanel_Main_Lp_LogMessage_Text_First"));
      Messages.getInstance();lp.statusMessage(Messages.getString("LogPanel_Main_Lp_StatusMessage_Text"));
      Messages.getInstance();lp.logMessage(Messages.getString("LogPanel_Main_Lp_LogMessage_Text_Second"));
    }
    catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
