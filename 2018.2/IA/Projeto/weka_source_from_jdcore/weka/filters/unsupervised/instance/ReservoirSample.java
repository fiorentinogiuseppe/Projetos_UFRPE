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
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;






























































public class ReservoirSample
  extends Filter
  implements UnsupervisedFilter, OptionHandler, StreamableFilter
{
  static final long serialVersionUID = 3119607037607101160L;
  protected int m_SampleSize = 100;
  

  protected Instance[] m_subSample;
  

  protected int m_currentInst;
  

  protected int m_RandomSeed = 1;
  

  protected Random m_random;
  

  public ReservoirSample() {}
  

  public String globalInfo()
  {
    return "Produces a random subsample of a dataset using the reservoir sampling Algorithm \"R\" by Vitter. The original data set does not have to fit into main memory, but the reservoir does. ";
  }
  







  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tSpecify the random number seed (default 1)", "S", 1, "-S <num>"));
    


    result.addElement(new Option("\tThe size of the output dataset - number of instances\n\t(default 100)", "Z", 1, "-Z <num>"));
    



    return result.elements();
  }
  



















  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      setRandomSeed(Integer.parseInt(tmpStr));
    } else {
      setRandomSeed(1);
    }
    
    tmpStr = Utils.getOption('Z', options);
    if (tmpStr.length() != 0) {
      setSampleSize(Integer.parseInt(tmpStr));
    } else {
      setSampleSize(100);
    }
  }
  






  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-S");
    result.add("" + getRandomSeed());
    
    result.add("-Z");
    result.add("" + getSampleSize());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String randomSeedTipText()
  {
    return "The seed used for random sampling.";
  }
  




  public int getRandomSeed()
  {
    return m_RandomSeed;
  }
  




  public void setRandomSeed(int newSeed)
  {
    m_RandomSeed = newSeed;
  }
  





  public String sampleSizeTipText()
  {
    return "Size of the subsample (reservoir). i.e. the number of instances.";
  }
  




  public int getSampleSize()
  {
    return m_SampleSize;
  }
  




  public void setSampleSize(int newSampleSize)
  {
    m_SampleSize = newSampleSize;
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
    
    m_subSample = new Instance[m_SampleSize];
    m_currentInst = 0;
    m_random = new Random(m_RandomSeed);
    
    return true;
  }
  





  protected void processInstance(Instance instance)
  {
    if (m_currentInst < m_SampleSize) {
      m_subSample[m_currentInst] = ((Instance)instance.copy());
    } else {
      double r = m_random.nextDouble();
      if (r < m_SampleSize / m_currentInst) {
        r = m_random.nextDouble();
        int replace = (int)(m_SampleSize * r);
        m_subSample[replace] = ((Instance)instance.copy());
      }
    }
    m_currentInst += 1;
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
    
    copyValues(instance, false);
    processInstance(instance);
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
  




  protected void createSubsample()
  {
    for (int i = 0; i < m_SampleSize; i++) {
      if (m_subSample[i] == null) break;
      Instance copy = (Instance)m_subSample[i].copy();
      push(copy);
    }
    




    m_subSample = null;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5562 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new ReservoirSample(), argv);
  }
}
