package weka.clusterers;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Randomizable;
import weka.core.Utils;


































public abstract class RandomizableDensityBasedClusterer
  extends AbstractDensityBasedClusterer
  implements OptionHandler, Randomizable
{
  private static final long serialVersionUID = -5325270357918932849L;
  protected int m_SeedDefault = 1;
  

  protected int m_Seed = m_SeedDefault;
  

  public RandomizableDensityBasedClusterer() {}
  

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
