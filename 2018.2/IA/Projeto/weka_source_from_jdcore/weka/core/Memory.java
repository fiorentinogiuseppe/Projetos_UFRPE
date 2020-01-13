package weka.core;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import javax.swing.JOptionPane;

































public class Memory
  implements RevisionHandler
{
  public static final long OUT_OF_MEMORY_THRESHOLD = 52428800L;
  public static final long LOW_MEMORY_MINIMUM = 104857600L;
  public static final long MAX_SLEEP_TIME = 10L;
  protected static boolean m_Enabled = true;
  

  protected boolean m_UseGUI = false;
  

  protected long m_SleepTime = 10L;
  

  protected static MemoryMXBean m_MemoryMXBean = ManagementFactory.getMemoryMXBean();
  


  protected MemoryUsage m_MemoryUsage = null;
  


  public Memory()
  {
    this(false);
  }
  




  public Memory(boolean useGUI)
  {
    m_UseGUI = useGUI;
  }
  




  public boolean isEnabled()
  {
    return m_Enabled;
  }
  




  public void setEnabled(boolean value)
  {
    m_Enabled = value;
  }
  





  public boolean getUseGUI()
  {
    return m_UseGUI;
  }
  





  public long getInitial()
  {
    m_MemoryUsage = m_MemoryMXBean.getHeapMemoryUsage();
    return m_MemoryUsage.getInit();
  }
  





  public long getCurrent()
  {
    m_MemoryUsage = m_MemoryMXBean.getHeapMemoryUsage();
    return m_MemoryUsage.getUsed();
  }
  





  public long getMax()
  {
    m_MemoryUsage = m_MemoryMXBean.getHeapMemoryUsage();
    return m_MemoryUsage.getMax();
  }
  







  public boolean isOutOfMemory()
  {
    try
    {
      Thread.sleep(m_SleepTime);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
    
    m_MemoryUsage = m_MemoryMXBean.getHeapMemoryUsage();
    if (isEnabled())
    {
      long avail = m_MemoryUsage.getMax() - m_MemoryUsage.getUsed();
      if (avail > 52428800L) {
        long num = (avail - 52428800L) / 5242880L + 1L;
        
        m_SleepTime = ((2.0D * (Math.log(num) + 2.5D)));
        if (m_SleepTime > 10L) {
          m_SleepTime = 10L;
        }
      }
      

      return avail < 52428800L;
    }
    return false;
  }
  






  public boolean memoryIsLow()
  {
    m_MemoryUsage = m_MemoryMXBean.getHeapMemoryUsage();
    
    if (isEnabled()) {
      long lowThreshold = (0.2D * m_MemoryUsage.getMax());
      

      if (lowThreshold < 104857600L) {
        lowThreshold = 104857600L;
      }
      
      long avail = m_MemoryUsage.getMax() - m_MemoryUsage.getUsed();
      
      return avail < lowThreshold;
    }
    return false;
  }
  





  public static double toMegaByte(long bytes)
  {
    return bytes / 1048576.0D;
  }
  







  public void showOutOfMemory()
  {
    if ((!isEnabled()) || (m_MemoryUsage == null)) {
      return;
    }
    
    System.gc();
    
    String msg = "Not enough memory (less than 50MB left on heap). Please load a smaller dataset or use a larger heap size.\n- initial heap size:   " + Utils.doubleToString(toMegaByte(m_MemoryUsage.getInit()), 1) + "MB\n" + "- current memory (heap) used:  " + Utils.doubleToString(toMegaByte(m_MemoryUsage.getUsed()), 1) + "MB\n" + "- max. memory (heap) available: " + Utils.doubleToString(toMegaByte(m_MemoryUsage.getMax()), 1) + "MB\n" + "\n" + "Note:\n" + "The Java heap size can be specified with the -Xmx option.\n" + "E.g., to use 128MB as heap size, the command line looks like this:\n" + "   java -Xmx128m -classpath ...\n" + "This does NOT work in the SimpleCLI, the above java command refers\n" + "to the one with which Weka is started. See the Weka FAQ on the web\n" + "for further info.";
    


















    System.err.println(msg);
    
    if (getUseGUI()) {
      JOptionPane.showMessageDialog(null, msg, "OutOfMemory", 2);
    }
  }
  





  public boolean showMemoryIsLow()
  {
    if ((!isEnabled()) || (m_MemoryUsage == null)) {
      return true;
    }
    
    String msg = "Warning: memory is running low - available heap space is less than 20% of maximum or 100MB (whichever is greater)\n\n- initial heap size:   " + Utils.doubleToString(toMegaByte(m_MemoryUsage.getInit()), 1) + "MB\n" + "- current memory (heap) used:  " + Utils.doubleToString(toMegaByte(m_MemoryUsage.getUsed()), 1) + "MB\n" + "- max. memory (heap) available: " + Utils.doubleToString(toMegaByte(m_MemoryUsage.getMax()), 1) + "MB\n\n" + "Consider deleting some results before continuing.\nCheck the Weka FAQ " + "on the web for suggestions on how to save memory.\n" + "Note that Weka will shut down when less than 50MB remain." + "\nDo you wish to continue regardless?\n\n";
    














    System.err.println(msg);
    
    if (getUseGUI()) {
      int result = JOptionPane.showConfirmDialog(null, msg, "Low Memory", 0);
      

      return result == 0;
    }
    
    return true;
  }
  






  public void stopThreads()
  {
    Thread[] thGroup = new Thread[Thread.activeCount()];
    Thread.enumerate(thGroup);
    
    for (int i = 0; i < thGroup.length; i++) {
      Thread t = thGroup[i];
      if ((t != null) && 
        (t != Thread.currentThread())) {
        if (t.getName().startsWith("Thread")) {
          t.stop();
        } else if (t.getName().startsWith("AWT-EventQueue")) {
          t.stop();
        }
      }
    }
    

    thGroup = null;
    
    System.gc();
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9487 $");
  }
  




  public static void main(String[] args)
  {
    Memory mem = new Memory();
    System.out.println("Initial memory: " + Utils.doubleToString(toMegaByte(mem.getInitial()), 1) + "MB" + " (" + mem.getInitial() + ")");
    

    System.out.println("Max memory: " + Utils.doubleToString(toMegaByte(mem.getMax()), 1) + "MB" + " (" + mem.getMax() + ")");
  }
}
