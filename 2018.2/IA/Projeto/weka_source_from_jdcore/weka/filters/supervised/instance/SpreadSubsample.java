package weka.filters.supervised.instance;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
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
import weka.filters.SupervisedFilter;




























































public class SpreadSubsample
  extends Filter
  implements SupervisedFilter, OptionHandler
{
  static final long serialVersionUID = -3947033795243930016L;
  private int m_RandomSeed = 1;
  

  private int m_MaxCount;
  

  private double m_DistributionSpread = 0.0D;
  




  private boolean m_AdjustWeights = false;
  


  public SpreadSubsample() {}
  


  public String globalInfo()
  {
    return "Produces a random subsample of a dataset. The original dataset must fit entirely in memory. This filter allows you to specify the maximum \"spread\" between the rarest and most common class. For example, you may specify that there be at most a 2:1 difference in class frequencies. When used in batch mode, subsequent batches are NOT resampled.";
  }
  










  public String adjustWeightsTipText()
  {
    return "Wether instance weights will be adjusted to maintain total weight per class.";
  }
  








  public boolean getAdjustWeights()
  {
    return m_AdjustWeights;
  }
  






  public void setAdjustWeights(boolean newAdjustWeights)
  {
    m_AdjustWeights = newAdjustWeights;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tSpecify the random number seed (default 1)", "S", 1, "-S <num>"));
    

    newVector.addElement(new Option("\tThe maximum class distribution spread.\n\t0 = no maximum spread, 1 = uniform distribution, 10 = allow at most\n\ta 10:1 ratio between the classes (default 0)", "M", 1, "-M <num>"));
    



    newVector.addElement(new Option("\tAdjust weights so that total weight per class is maintained.\n\tIndividual instance weighting is not preserved. (default no\n\tweights adjustment", "W", 0, "-W"));
    



    newVector.addElement(new Option("\tThe maximum count for any class value (default 0 = unlimited).\n", "X", 0, "-X <num>"));
    


    return newVector.elements();
  }
  




























  public void setOptions(String[] options)
    throws Exception
  {
    String seedString = Utils.getOption('S', options);
    if (seedString.length() != 0) {
      setRandomSeed(Integer.parseInt(seedString));
    } else {
      setRandomSeed(1);
    }
    
    String maxString = Utils.getOption('M', options);
    if (maxString.length() != 0) {
      setDistributionSpread(Double.valueOf(maxString).doubleValue());
    } else {
      setDistributionSpread(0.0D);
    }
    
    String maxCount = Utils.getOption('X', options);
    if (maxCount.length() != 0) {
      setMaxCount(Double.valueOf(maxCount).doubleValue());
    } else {
      setMaxCount(0.0D);
    }
    
    setAdjustWeights(Utils.getFlag('W', options));
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[7];
    int current = 0;
    
    options[(current++)] = "-M";
    options[(current++)] = ("" + getDistributionSpread());
    
    options[(current++)] = "-X";
    options[(current++)] = ("" + getMaxCount());
    
    options[(current++)] = "-S";
    options[(current++)] = ("" + getRandomSeed());
    
    if (getAdjustWeights()) {
      options[(current++)] = "-W";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public String distributionSpreadTipText()
  {
    return "The maximum class distribution spread. (0 = no maximum spread, 1 = uniform distribution, 10 = allow at most a 10:1 ratio between the classes).";
  }
  







  public void setDistributionSpread(double spread)
  {
    m_DistributionSpread = spread;
  }
  





  public double getDistributionSpread()
  {
    return m_DistributionSpread;
  }
  





  public String maxCountTipText()
  {
    return "The maximum count for any class value (0 = unlimited).";
  }
  





  public void setMaxCount(double maxcount)
  {
    m_MaxCount = ((int)maxcount);
  }
  





  public double getMaxCount()
  {
    return m_MaxCount;
  }
  





  public String randomSeedTipText()
  {
    return "Sets the random number seed for subsampling.";
  }
  





  public int getRandomSeed()
  {
    return m_RandomSeed;
  }
  





  public void setRandomSeed(int newSeed)
  {
    m_RandomSeed = newSeed;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    
    return result;
  }
  











  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    setOutputFormat(instanceInfo);
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
    
    if (!isFirstBatchDone())
    {
      createSubsample();
    }
    
    flushInput();
    m_NewBatch = true;
    m_FirstBatchDone = true;
    return numPendingOutput() != 0;
  }
  





  private void createSubsample()
  {
    int classI = getInputFormat().classIndex();
    
    getInputFormat().sort(classI);
    
    int[] classIndices = getClassIndices();
    

    int[] counts = new int[getInputFormat().numClasses()];
    double[] weights = new double[getInputFormat().numClasses()];
    int min = -1;
    for (int i = 0; i < getInputFormat().numInstances(); i++) {
      Instance current = getInputFormat().instance(i);
      if (!current.classIsMissing()) {
        counts[((int)current.classValue())] += 1;
        weights[((int)current.classValue())] += current.weight();
      }
    }
    

    for (int i = 0; i < counts.length; i++) {
      if (counts[i] > 0) {
        weights[i] /= counts[i];
      }
    }
    







    int minIndex = -1;
    for (int i = 0; i < counts.length; i++) {
      if ((min < 0) && (counts[i] > 0)) {
        min = counts[i];
        minIndex = i;
      } else if ((counts[i] < min) && (counts[i] > 0)) {
        min = counts[i];
        minIndex = i;
      }
    }
    
    if (min < 0) {
      System.err.println("SpreadSubsample: *warning* none of the classes have any values in them.");
      return;
    }
    

    int[] new_counts = new int[getInputFormat().numClasses()];
    for (int i = 0; i < counts.length; i++) {
      new_counts[i] = ((int)Math.abs(Math.min(counts[i], min * m_DistributionSpread)));
      
      if ((i == minIndex) && 
        (m_DistributionSpread > 0.0D) && (m_DistributionSpread < 1.0D))
      {
        new_counts[i] = counts[i];
      }
      
      if (m_DistributionSpread == 0.0D) {
        new_counts[i] = counts[i];
      }
      
      if (m_MaxCount > 0) {
        new_counts[i] = Math.min(new_counts[i], m_MaxCount);
      }
    }
    

    Random random = new Random(m_RandomSeed);
    Hashtable t = new Hashtable();
    for (int j = 0; j < new_counts.length; j++) {
      double newWeight = 1.0D;
      if ((m_AdjustWeights) && (new_counts[j] > 0)) {
        newWeight = weights[j] * counts[j] / new_counts[j];
      }
      







      for (int k = 0; k < new_counts[j]; k++) {
        boolean ok = false;
        do {
          int index = classIndices[j] + Math.abs(random.nextInt()) % (classIndices[(j + 1)] - classIndices[j]);
          

          if (t.get("" + index) == null)
          {
            t.put("" + index, "");
            ok = true;
            if (index >= 0) {
              Instance newInst = (Instance)getInputFormat().instance(index).copy();
              if (m_AdjustWeights) {
                newInst.setWeight(newWeight);
              }
              push(newInst);
            }
          }
        } while (!ok);
      }
    }
  }
  







  private int[] getClassIndices()
  {
    int[] classIndices = new int[getInputFormat().numClasses() + 1];
    int currentClass = 0;
    classIndices[currentClass] = 0;
    for (int i = 0; i < getInputFormat().numInstances(); i++) {
      Instance current = getInputFormat().instance(i);
      if (current.classIsMissing()) {
        for (int j = currentClass + 1; j < classIndices.length; j++) {
          classIndices[j] = i;
        }
        break; }
      if (current.classValue() != currentClass) {
        for (int j = currentClass + 1; j <= current.classValue(); j++) {
          classIndices[j] = i;
        }
        currentClass = (int)current.classValue();
      }
    }
    if (currentClass <= getInputFormat().numClasses()) {
      for (int j = currentClass + 1; j < classIndices.length; j++) {
        classIndices[j] = getInputFormat().numInstances();
      }
    }
    return classIndices;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5542 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new SpreadSubsample(), argv);
  }
}
