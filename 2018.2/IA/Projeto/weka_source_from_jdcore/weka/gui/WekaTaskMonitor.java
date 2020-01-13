package weka.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;





























public class WekaTaskMonitor
  extends JPanel
  implements TaskLogger
{
  private static final long serialVersionUID = 508309816292197578L;
  private int m_ActiveTasks = 0;
  

  private JLabel m_MonitorLabel;
  

  private ImageIcon m_iconStationary;
  

  private ImageIcon m_iconAnimated;
  

  private boolean m_animating = false;
  


  public WekaTaskMonitor()
  {
    URL imageURL = getClass().getClassLoader().getResource("weka/gui/weka_stationary.gif");
    

    if (imageURL != null) {
      Image pic = Toolkit.getDefaultToolkit().getImage(imageURL);
      imageURL = getClass().getClassLoader().getResource("weka/gui/weka_animated.gif");
      
      Image pic2 = Toolkit.getDefaultToolkit().getImage(imageURL);
      





      m_iconStationary = new ImageIcon(pic);
      m_iconAnimated = new ImageIcon(pic2);
    }
    
    Messages.getInstance();m_MonitorLabel = new JLabel(Messages.getString("WekaTaskMonitor_MonitorLabel_JLabel_Text") + m_ActiveTasks, m_iconStationary, 0);
    





    setLayout(new BorderLayout());
    Dimension d = m_MonitorLabel.getPreferredSize();
    m_MonitorLabel.setPreferredSize(new Dimension(width + 15, height));
    m_MonitorLabel.setMinimumSize(new Dimension(width + 15, height));
    add(m_MonitorLabel, "Center");
  }
  




  public synchronized void taskStarted()
  {
    m_ActiveTasks += 1;
    updateMonitor();
  }
  


  public synchronized void taskFinished()
  {
    m_ActiveTasks -= 1;
    if (m_ActiveTasks < 0) {
      m_ActiveTasks = 0;
    }
    updateMonitor();
  }
  



  private void updateMonitor()
  {
    m_MonitorLabel.setText(" x " + m_ActiveTasks);
    if ((m_ActiveTasks > 0) && (!m_animating)) {
      m_MonitorLabel.setIcon(m_iconAnimated);
      m_animating = true;
    }
    
    if ((m_ActiveTasks == 0) && (m_animating)) {
      m_MonitorLabel.setIcon(m_iconStationary);
      m_animating = false;
    }
  }
  


  public static void main(String[] args)
  {
    try
    {
      JFrame jf = new JFrame();
      jf.getContentPane().setLayout(new BorderLayout());
      WekaTaskMonitor tm = new WekaTaskMonitor();
      Messages.getInstance();tm.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Messages.getString("WekaTaskMonitor_Main_JFrame_BorderFactoryCreateTitledBorder_Text")), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
      


      jf.getContentPane().add(tm, "Center");
      jf.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          val$jf.dispose();
          System.exit(0);
        }
      });
      jf.pack();
      jf.setVisible(true);
      tm.taskStarted();
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
}
