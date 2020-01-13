package weka.clusterers;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Randomizable;
import weka.core.Utils;


































public abstract class RandomizableSingleClustererEnhancer
  extends AbstractClusterer
  implements OptionHandler, Randomizable
{
  private static final long serialVersionUID = -644847037106316249L;
  protected int m_SeedDefault = 1;
  

  protected int m_Seed = m_SeedDefault;
  

  public RandomizableSingleClustererEnhancer() {}
  

  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tRandom number seed.\n\t(default " + m_SeedDefault + ")", "S", 1, "-S <num>"));
    



    return result.elements();
  }
  






  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      setSeed(Integer.parseInt(tmpStr));
    } else {
      setSeed(m_SeedDefault);
    }
  }
  





  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-S");
    result.add("" + getSeed());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String seedTipText()
  {
    return "The random number seed to be used.";
  }
  




  public void setSeed(int value)
  {
    m_Seed = value;
  }
  




  public int getSeed()
  {
    return m_Seed;
  }
}
