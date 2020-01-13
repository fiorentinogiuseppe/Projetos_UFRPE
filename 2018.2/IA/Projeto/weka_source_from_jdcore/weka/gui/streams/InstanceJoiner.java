package weka.gui.streams;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Vector;
import weka.core.Instance;
import weka.core.Instances;











































public class InstanceJoiner
  implements Serializable, InstanceProducer, SerialInstanceListener
{
  private static final long serialVersionUID = -6529972700291329656L;
  private Vector listeners;
  private boolean b_Debug;
  protected Instances m_InputFormat;
  private Instance m_OutputInstance;
  private boolean b_FirstInputFinished;
  private boolean b_SecondInputFinished;
  
  public InstanceJoiner()
  {
    listeners = new Vector();
    m_InputFormat = null;
    m_OutputInstance = null;
    b_Debug = false;
    b_FirstInputFinished = false;
    b_SecondInputFinished = false;
  }
  












  public boolean inputFormat(Instances instanceInfo)
  {
    m_InputFormat = new Instances(instanceInfo, 0);
    notifyInstanceProduced(new InstanceEvent(this, 1));
    b_FirstInputFinished = false;
    b_SecondInputFinished = false;
    return true;
  }
  








  public Instances outputFormat()
    throws Exception
  {
    if (m_InputFormat == null) {
      Messages.getInstance();throw new Exception(Messages.getString("InstanceJoiner_OutputFormat_Exception_Text"));
    }
    return new Instances(m_InputFormat, 0);
  }
  
  public boolean input(Instance instance) throws Exception
  {
    if (m_InputFormat == null) {
      Messages.getInstance();throw new Exception(Messages.getString("InstanceJoiner_Input_Exception_Text"));
    }
    if (instance != null) {
      m_OutputInstance = ((Instance)instance.copy());
      notifyInstanceProduced(new InstanceEvent(this, 2));
      
      return true;
    }
    return false;
  }
  










  public void batchFinished()
    throws Exception
  {
    if (m_InputFormat == null) {
      Messages.getInstance();throw new Exception(Messages.getString("InstanceJoiner_BatchFinished_Exception_Text"));
    }
    notifyInstanceProduced(new InstanceEvent(this, 3));
  }
  









  public Instance outputPeek()
    throws Exception
  {
    if (m_InputFormat == null) {
      Messages.getInstance();throw new Exception(Messages.getString("InstanceJoiner_OutputPeek_Exception_Text"));
    }
    if (m_OutputInstance == null) {
      return null;
    }
    return (Instance)m_OutputInstance.copy();
  }
  

  public void setDebug(boolean debug)
  {
    b_Debug = debug;
  }
  
  public boolean getDebug()
  {
    return b_Debug;
  }
  
  public synchronized void addInstanceListener(InstanceListener ipl)
  {
    listeners.addElement(ipl);
  }
  
  public synchronized void removeInstanceListener(InstanceListener ipl)
  {
    listeners.removeElement(ipl);
  }
  
  protected void notifyInstanceProduced(InstanceEvent e)
  {
    if (listeners.size() > 0) {
      if (b_Debug) {
        Messages.getInstance();System.err.println(getClass().getName() + Messages.getString("InstanceJoiner_NotifyInstanceProduced_Error_Text_First"));
      }
      
      Vector l;
      synchronized (this) {
        l = (Vector)listeners.clone();
      }
      for (int i = 0; i < l.size(); i++) {
        ((InstanceListener)l.elementAt(i)).instanceProduced(e);
      }
      
      try
      {
        if (e.getID() == 2) {
          m_OutputInstance = null;
        }
      } catch (Exception ex) {
        Messages.getInstance();System.err.println(Messages.getString("InstanceJoiner_NotifyInstanceProduced_Exception_Text") + ex.getMessage());
      }
    }
  }
  
  public void instanceProduced(InstanceEvent e)
  {
    Object source = e.getSource();
    if ((source instanceof InstanceProducer)) {
      try {
        InstanceProducer a = (InstanceProducer)source;
        switch (e.getID()) {
        case 1: 
          if (b_Debug) {
            Messages.getInstance();System.err.println(getClass().getName() + Messages.getString("InstanceJoiner_InstanceProduced_InstanceEventFORMAT_AVAILABLE_Error_Text"));
          }
          
          inputFormat(a.outputFormat());
          break;
        case 2: 
          if (b_Debug) {
            Messages.getInstance();System.err.println(getClass().getName() + Messages.getString("InstanceJoiner_InstanceProduced_InstanceEventINSTANCE_AVAILABLE_Error_Text"));
          }
          
          input(a.outputPeek());
          break;
        case 3: 
          if (b_Debug) {
            Messages.getInstance();System.err.println(getClass().getName() + Messages.getString("InstanceJoiner_InstanceProduced_InstanceEventBATCH_FINISHED_Error_Text"));
          }
          
          batchFinished();
          b_FirstInputFinished = true;
          break;
        default: 
          Messages.getInstance();System.err.println(getClass().getName() + Messages.getString("InstanceJoiner_InstanceProduced_InstanceEventDEFAULT_Error_Text"));
        }
      }
      catch (Exception ex)
      {
        System.err.println(ex.getMessage());
      }
    } else {
      Messages.getInstance();System.err.println(getClass().getName() + Messages.getString("InstanceJoiner_InstanceProduced_InstanceEventDEFAULT_Error_Text"));
    }
  }
  

  public void secondInstanceProduced(InstanceEvent e)
  {
    Object source = e.getSource();
    if ((source instanceof InstanceProducer)) {
      try {
        if (!b_FirstInputFinished) {
          Messages.getInstance();throw new Exception(getClass().getName() + Messages.getString("InstanceJoiner_InstanceProduced_Error_Text"));
        }
        
        InstanceProducer a = (InstanceProducer)source;
        switch (e.getID()) {
        case 1: 
          if (b_Debug) {
            Messages.getInstance();System.err.println(getClass().getName() + Messages.getString("InstanceJoiner_SecondInstanceProduced_Error_Text_First"));
          }
          

          if (!a.outputFormat().equalHeaders(outputFormat())) {
            Messages.getInstance();throw new Exception(getClass().getName() + Messages.getString("InstanceJoiner_SecondInstanceProduced_InstanceEventFORMAT_AVAILABLE_Error_Text"));
          }
          
          break;
        case 2: 
          if (b_Debug) {
            Messages.getInstance();System.err.println(getClass().getName() + Messages.getString("InstanceJoiner_SecondInstanceProduced_InstanceEventINSTANCE_AVAILABLE_Error_Text"));
          }
          
          input(a.outputPeek());
          break;
        case 3: 
          if (b_Debug) {
            Messages.getInstance();System.err.println(getClass().getName() + Messages.getString("InstanceJoiner_SecondInstanceProduced_InstanceEventBATCH_FINISHED_Error_Text"));
          }
          
          batchFinished();
          break;
        default: 
          Messages.getInstance();System.err.println(getClass().getName() + Messages.getString("InstanceJoiner_SecondInstanceProduced_InstanceEventDEFAULT_Error_Text"));
        }
      }
      catch (Exception ex)
      {
        System.err.println(ex.getMessage());
      }
    } else {
      Messages.getInstance();System.err.println(getClass().getName() + Messages.getString("InstanceJoiner_SecondInstanceProduced_Error_Text_Second"));
    }
  }
}
