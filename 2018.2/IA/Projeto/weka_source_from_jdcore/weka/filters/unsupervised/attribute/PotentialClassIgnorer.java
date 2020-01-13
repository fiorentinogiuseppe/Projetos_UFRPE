package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.filters.Filter;





































public abstract class PotentialClassIgnorer
  extends Filter
  implements OptionHandler
{
  private static final long serialVersionUID = 8625371119276845454L;
  protected boolean m_IgnoreClass = false;
  

  protected int m_ClassIndex = -1;
  

  public PotentialClassIgnorer() {}
  

  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tUnsets the class index temporarily before the filter is\n\tapplied to the data.\n\t(default: no)", "unset-class-temporarily", 1, "-unset-class-temporarily"));
    




    return result.elements();
  }
  




  public void setOptions(String[] options)
    throws Exception
  {
    setIgnoreClass(Utils.getFlag("unset-class-temporarily", options));
  }
  






  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getIgnoreClass()) {
      result.add("-unset-class-temporarily");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  












  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    boolean result = super.setInputFormat(instanceInfo);
    if (m_IgnoreClass) {
      m_ClassIndex = inputFormatPeek().classIndex();
      inputFormatPeek().setClassIndex(-1);
    }
    return result;
  }
  











  public Instances getOutputFormat()
  {
    if (m_IgnoreClass) {
      outputFormatPeek().setClassIndex(m_ClassIndex);
    }
    return super.getOutputFormat();
  }
  





  public String ignoreClassTipText()
  {
    return "The class index will be unset temporarily before the filter is applied.";
  }
  





  public void setIgnoreClass(boolean newIgnoreClass)
  {
    m_IgnoreClass = newIgnoreClass;
  }
  





  public boolean getIgnoreClass()
  {
    return m_IgnoreClass;
  }
}
