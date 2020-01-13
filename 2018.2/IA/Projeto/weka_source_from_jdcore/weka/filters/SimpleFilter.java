package weka.filters;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;





































public abstract class SimpleFilter
  extends Filter
  implements OptionHandler
{
  private static final long serialVersionUID = 5702974949137433141L;
  protected boolean m_Debug = false;
  



  public SimpleFilter() {}
  



  public abstract String globalInfo();
  


  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tTurns on output of debugging information.", "D", 0, "-D"));
    


    return result.elements();
  }
  







  public void setOptions(String[] options)
    throws Exception
  {
    reset();
    
    setDebug(Utils.getFlag('D', options));
  }
  






  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getDebug()) {
      result.add("-D");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public void setDebug(boolean value)
  {
    m_Debug = value;
  }
  




  public boolean getDebug()
  {
    return m_Debug;
  }
  




  public String debugTipText()
  {
    return "Turns on output of debugging information.";
  }
  






  protected void reset()
  {
    m_NewBatch = true;
    m_FirstBatchDone = false;
  }
  










  protected abstract boolean hasImmediateOutputFormat();
  










  protected abstract Instances determineOutputFormat(Instances paramInstances)
    throws Exception;
  









  protected abstract Instances process(Instances paramInstances)
    throws Exception;
  









  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    
    reset();
    
    if (hasImmediateOutputFormat()) {
      setOutputFormat(determineOutputFormat(instanceInfo));
    }
    return hasImmediateOutputFormat();
  }
}
