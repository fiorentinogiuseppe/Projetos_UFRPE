package weka.gui.streams;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import weka.core.Instance;
import weka.core.Instances;






























public class InstanceLoader
  extends JPanel
  implements ActionListener, InstanceProducer
{
  private static final long serialVersionUID = -8725567310271862492L;
  private Vector m_Listeners;
  private Thread m_LoaderThread;
  private Instance m_OutputInstance;
  private Instances m_OutputInstances;
  private boolean m_Debug;
  private JButton m_StartBut;
  private JTextField m_FileNameTex;
  
  private class LoadThread
    extends Thread
  {
    private InstanceProducer m_IP;
    
    public LoadThread(InstanceProducer ip)
    {
      m_IP = ip;
    }
    
    public void run()
    {
      try {
        Messages.getInstance();m_StartBut.setText(Messages.getString("InstanceLoader_LoadThread_Run_StartBut_SetText_Text"));
        m_StartBut.setBackground(Color.red);
        if (m_Debug) {
          Messages.getInstance();System.err.println(Messages.getString("InstanceLoader_LoadThread_Run_Error_Text_First"));
        }
        
        Reader input = new BufferedReader(new FileReader(m_FileNameTex.getText()));
        
        m_OutputInstances = new Instances(input, 1);
        if (m_Debug) {
          Messages.getInstance();System.err.println(Messages.getString("InstanceLoader_LoadThread_Run_Error_Text_Second") + m_FileNameTex.getText());
        }
        InstanceEvent ie = new InstanceEvent(m_IP, 1);
        
        notifyInstanceProduced(ie);
        while (m_OutputInstances.readInstance(input)) {
          if (m_LoaderThread != this) {
            return;
          }
          if (m_Debug) {
            Messages.getInstance();System.err.println(Messages.getString("InstanceLoader_LoadThread_Run_Error_Text_Third"));
          }
          
          m_OutputInstance = m_OutputInstances.instance(0);
          m_OutputInstances.delete(0);
          ie = new InstanceEvent(m_IP, 2);
          notifyInstanceProduced(ie);
        }
        ie = new InstanceEvent(m_IP, 3);
        notifyInstanceProduced(ie);
      } catch (Exception ex) {
        System.err.println(ex.getMessage());
      } finally {
        m_LoaderThread = null;
        Messages.getInstance();m_StartBut.setText(Messages.getString("InstanceLoader_LoadThread_Run_StatusBut_SetText_Text"));
        m_StartBut.setBackground(Color.green);
      }
    }
  }
  
  public InstanceLoader() {
    setLayout(new BorderLayout());
    Messages.getInstance();m_StartBut = new JButton(Messages.getString("InstanceLoader_StartBut_JButton_Text"));
    m_StartBut.setBackground(Color.green);
    Messages.getInstance();add(Messages.getString("InstanceLoader_StartBut_JButton_Add_Text_First"), m_StartBut);
    m_StartBut.addActionListener(this);
    m_FileNameTex = new JTextField("/home/trigg/datasets/UCI/iris.arff");
    Messages.getInstance();add(Messages.getString("InstanceLoader_StartBut_JButton_Add_Text_Second"), m_FileNameTex);
    m_Listeners = new Vector();
  }
  

  public void setDebug(boolean debug)
  {
    m_Debug = debug;
  }
  
  public boolean getDebug()
  {
    return m_Debug;
  }
  
  public void setArffFile(String newArffFile)
  {
    m_FileNameTex.setText(newArffFile);
  }
  
  public String getArffFile() {
    return m_FileNameTex.getText();
  }
  
  public synchronized void addInstanceListener(InstanceListener ipl)
  {
    m_Listeners.addElement(ipl);
  }
  
  public synchronized void removeInstanceListener(InstanceListener ipl)
  {
    m_Listeners.removeElement(ipl);
  }
  
  protected void notifyInstanceProduced(InstanceEvent e)
  {
    if (m_Debug) {
      Messages.getInstance();System.err.println(Messages.getString("InstanceLoader_NotifyInstanceProduced_Error_Text"));
    }
    Vector l;
    synchronized (this) {
      l = (Vector)m_Listeners.clone();
    }
    if (l.size() > 0) {
      for (int i = 0; i < l.size(); i++) {
        ((InstanceListener)l.elementAt(i)).instanceProduced(e);
      }
      if (e.getID() == 2) {
        m_OutputInstance = null;
      }
    }
  }
  
  public Instances outputFormat() throws Exception
  {
    if (m_OutputInstances == null) {
      Messages.getInstance();throw new Exception(Messages.getString("InstanceLoader_OutputFormat_Exception_Text"));
    }
    return new Instances(m_OutputInstances, 0);
  }
  
  public Instance outputPeek() throws Exception
  {
    if ((m_OutputInstances == null) || (m_OutputInstance == null))
    {
      return null;
    }
    return (Instance)m_OutputInstance.copy();
  }
  
  public void actionPerformed(ActionEvent e)
  {
    Object source = e.getSource();
    
    if (source == m_StartBut)
    {
      if (m_LoaderThread == null) {
        m_LoaderThread = new LoadThread(this);
        m_LoaderThread.setPriority(1);
        m_LoaderThread.start();
      } else {
        m_LoaderThread = null;
      }
    }
  }
}
