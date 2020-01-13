package weka.classifiers.functions.supportVector;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Copyable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SerializedObject;
import weka.core.Utils;






































public abstract class Kernel
  implements Serializable, OptionHandler, CapabilitiesHandler, RevisionHandler
{
  private static final long serialVersionUID = -6102771099905817064L;
  protected Instances m_data;
  protected boolean m_Debug = false;
  

  protected boolean m_ChecksTurnedOff = false;
  






  public Kernel() {}
  





  public abstract String globalInfo();
  





  public abstract double eval(int paramInt1, int paramInt2, Instance paramInstance)
    throws Exception;
  





  public abstract void clean();
  





  public abstract int numEvals();
  





  public abstract int numCacheHits();
  





  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tEnables debugging output (if available) to be printed.\n\t(default: off)", "D", 0, "-D"));
    



    result.addElement(new Option("\tTurns off all checks - use with caution!\n\t(default: checks on)", "no-checks", 0, "-no-checks"));
    



    return result.elements();
  }
  




  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
    
    setChecksTurnedOff(Utils.getFlag("no-checks", options));
    
    Utils.checkForRemainingOptions(options);
  }
  






  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getDebug()) {
      result.add("-D");
    }
    if (getChecksTurnedOff()) {
      result.add("-no-checks");
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
    return "Turns on the output of debugging information.";
  }
  





  public void setChecksTurnedOff(boolean value)
  {
    m_ChecksTurnedOff = value;
  }
  




  public boolean getChecksTurnedOff()
  {
    return m_ChecksTurnedOff;
  }
  





  public String checksTurnedOffTipText()
  {
    return "Turns time-consuming checks off - use with caution.";
  }
  




  protected void initVars(Instances data)
  {
    m_data = data;
  }
  






  public Capabilities getCapabilities()
  {
    Capabilities result = new Capabilities(this);
    result.enableAll();
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9897 $");
  }
  





  public void buildKernel(Instances data)
    throws Exception
  {
    if (!getChecksTurnedOff()) {
      getCapabilities().testWithFail(data);
    }
    initVars(data);
  }
  






  public static Kernel makeCopy(Kernel kernel)
    throws Exception
  {
    if ((kernel instanceof Copyable)) {
      return (Kernel)((Copyable)kernel).copy();
    }
    return (Kernel)new SerializedObject(kernel).getObject();
  }
  







  public static Kernel[] makeCopies(Kernel model, int num)
    throws Exception
  {
    if (model == null) {
      throw new Exception("No model kernel set");
    }
    Kernel[] kernels = new Kernel[num];
    if ((model instanceof Copyable)) {
      for (int i = 0; i < kernels.length; i++) {
        kernels[i] = ((Kernel)((Copyable)model).copy());
      }
    } else {
      SerializedObject so = new SerializedObject(model);
      for (int i = 0; i < kernels.length; i++) {
        kernels[i] = ((Kernel)so.getObject());
      }
    }
    return kernels;
  }
  











  public static Kernel forName(String kernelName, String[] options)
    throws Exception
  {
    return (Kernel)Utils.forName(Kernel.class, kernelName, options);
  }
}
