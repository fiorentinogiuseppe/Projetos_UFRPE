package weka.gui.streams;

import java.awt.BorderLayout;
import java.io.PrintStream;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import weka.core.Instance;
import weka.core.Instances;


































public class InstanceViewer
  extends JPanel
  implements InstanceListener
{
  private static final long serialVersionUID = -4925729441294121772L;
  private JTextArea m_OutputTex;
  private boolean m_Debug;
  private boolean m_Clear;
  private String m_UpdateString;
  
  private void updateOutput()
  {
    m_OutputTex.append(m_UpdateString);
    m_UpdateString = "";
  }
  
  private void clearOutput()
  {
    m_UpdateString = "";
    m_OutputTex.setText("");
  }
  
  public void inputFormat(Instances instanceInfo)
  {
    if (m_Debug) {
      Messages.getInstance();System.err.println(Messages.getString("InstanceViewer_InputFormat_Error_Text") + instanceInfo.toString());
    }
    
    if (m_Clear) {
      clearOutput();
    }
    m_UpdateString += instanceInfo.toString();
    updateOutput();
  }
  
  public void input(Instance instance) throws Exception
  {
    if (m_Debug) {
      Messages.getInstance();Messages.getInstance();System.err.println(Messages.getString("InstanceViewer_Input_Error_Text_First") + instance + Messages.getString("InstanceViewer_Input_Error_Text_Second"));
    }
    m_UpdateString = (m_UpdateString + instance.toString() + "\n");
    updateOutput();
  }
  
  public void batchFinished()
  {
    updateOutput();
    if (m_Debug) {
      Messages.getInstance();System.err.println(Messages.getString("InstanceViewer_BatchFinished_Error_Text"));
    }
  }
  
  public InstanceViewer()
  {
    setLayout(new BorderLayout());
    m_UpdateString = "";
    setClearEachDataset(true);
    m_OutputTex = new JTextArea(10, 20);
    m_OutputTex.setEditable(false);
    add("Center", new JScrollPane(m_OutputTex));
  }
  
  public void setClearEachDataset(boolean clear)
  {
    m_Clear = clear;
  }
  
  public boolean getClearEachDataset()
  {
    return m_Clear;
  }
  
  public void setDebug(boolean debug)
  {
    m_Debug = debug;
  }
  
  public boolean getDebug()
  {
    return m_Debug;
  }
  
  public void instanceProduced(InstanceEvent e)
  {
    Object source = e.getSource();
    if ((source instanceof InstanceProducer)) {
      try {
        InstanceProducer a = (InstanceProducer)source;
        switch (e.getID()) {
        case 1: 
          inputFormat(a.outputFormat());
          break;
        case 2: 
          input(a.outputPeek());
          break;
        case 3: 
          batchFinished();
          break;
        default: 
          Messages.getInstance();System.err.println(Messages.getString("InstanceViewer_InstanceProduced_InstanceEventDEFAULT_Error_Text"));
        }
      }
      catch (Exception ex) {
        System.err.println(ex.getMessage());
      }
    } else {
      Messages.getInstance();System.err.println(Messages.getString("InstanceViewer_InstanceProduced_Error_Text"));
    }
  }
}
