package weka.filters.unsupervised.instance;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;













































public class Randomize
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  static final long serialVersionUID = 8854479785121877582L;
  protected int m_Seed = 42;
  

  protected Random m_Random;
  

  public Randomize() {}
  

  public String globalInfo()
  {
    return "Randomly shuffles the order of instances passed through it. The random number generator is reset with the seed value whenever a new set of instances is passed in.";
  }
  







  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    
    newVector.addElement(new Option("\tSpecify the random number seed (default 42)", "S", 1, "-S <num>"));
    


    return newVector.elements();
  }
  














  public void setOptions(String[] options)
    throws Exception
  {
    String seedString = Utils.getOption('S', options);
    if (seedString.length() != 0) {
      setRandomSeed(Integer.parseInt(seedString));
    } else {
      setRandomSeed(42);
    }
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[2];
    int current = 0;
    
    options[(current++)] = "-S";options[(current++)] = ("" + getRandomSeed());
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String randomSeedTipText()
  {
    return "Seed for the random number generator.";
  }
  





  public int getRandomSeed()
  {
    return m_Seed;
  }
  





  public void setRandomSeed(int newRandomSeed)
  {
    m_Seed = newRandomSeed;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  








  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    setOutputFormat(instanceInfo);
    m_Random = new Random(m_Seed);
    return true;
  }
  









  public boolean input(Instance instance)
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    if (isFirstBatchDone()) {
      push(instance);
      return true;
    }
    bufferInput(instance);
    return false;
  }
  













  public boolean batchFinished()
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    
    if (!isFirstBatchDone()) {
      getInputFormat().randomize(m_Random);
    }
    for (int i = 0; i < getInputFormat().numInstances(); i++) {
      push(getInputFormat().instance(i));
    }
    flushInput();
    
    m_NewBatch = true;
    m_FirstBatchDone = true;
    return numPendingOutput() != 0;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5548 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new Randomize(), argv);
  }
}
