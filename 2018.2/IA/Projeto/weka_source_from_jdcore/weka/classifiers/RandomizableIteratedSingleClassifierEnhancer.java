package weka.classifiers;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.Utils;

































public abstract class RandomizableIteratedSingleClassifierEnhancer
  extends IteratedSingleClassifierEnhancer
  implements Randomizable
{
  private static final long serialVersionUID = 5063351391524938557L;
  protected int m_Seed = 1;
  


  public RandomizableIteratedSingleClassifierEnhancer() {}
  

  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tRandom number seed.\n\t(default 1)", "S", 1, "-S <num>"));
    



    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  
















  public void setOptions(String[] options)
    throws Exception
  {
    String seed = Utils.getOption('S', options);
    if (seed.length() != 0) {
      setSeed(Integer.parseInt(seed));
    } else {
      setSeed(1);
    }
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 2];
    
    int current = 0;
    options[(current++)] = "-S";
    options[(current++)] = ("" + getSeed());
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    

    return options;
  }
  




  public String seedTipText()
  {
    return "The random number seed to be used.";
  }
  





  public void setSeed(int seed)
  {
    m_Seed = seed;
  }
  





  public int getSeed()
  {
    return m_Seed;
  }
}
