package weka.filters.unsupervised.instance;

import java.io.PrintStream;
import java.util.Collections;
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























































public class Resample
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  static final long serialVersionUID = 3119607037607101160L;
  protected double m_SampleSizePercent = 100.0D;
  

  protected int m_RandomSeed = 1;
  

  protected boolean m_NoReplacement = false;
  



  protected boolean m_InvertSelection = false;
  

  public Resample() {}
  

  public String globalInfo()
  {
    return "Produces a random subsample of a dataset using either sampling with replacement or without replacement. The original dataset must fit entirely in memory. The number of instances in the generated dataset may be specified. When used in batch mode, subsequent batches are NOT resampled.";
  }
  









  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tSpecify the random number seed (default 1)", "S", 1, "-S <num>"));
    


    result.addElement(new Option("\tThe size of the output dataset, as a percentage of\n\tthe input dataset (default 100)", "Z", 1, "-Z <num>"));
    



    result.addElement(new Option("\tDisables replacement of instances\n\t(default: with replacement)", "no-replacement", 0, "-no-replacement"));
    



    result.addElement(new Option("\tInverts the selection - only available with '-no-replacement'.", "V", 0, "-V"));
    


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
      setSampleSizePercent(Double.parseDouble(tmpStr));
    } else {
      setSampleSizePercent(100.0D);
    }
    setNoReplacement(Utils.getFlag("no-replacement", options));
    
    if (getNoReplacement()) {
      setInvertSelection(Utils.getFlag('V', options));
    }
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  





  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-S");
    result.add("" + getRandomSeed());
    
    result.add("-Z");
    result.add("" + getSampleSizePercent());
    
    if (getNoReplacement()) {
      result.add("-no-replacement");
      if (getInvertSelection()) {
        result.add("-V");
      }
    }
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
  





  public String sampleSizePercentTipText()
  {
    return "Size of the subsample as a percentage of the original dataset.";
  }
  




  public double getSampleSizePercent()
  {
    return m_SampleSizePercent;
  }
  




  public void setSampleSizePercent(double newSampleSizePercent)
  {
    m_SampleSizePercent = newSampleSizePercent;
  }
  





  public String noReplacementTipText()
  {
    return "Disables the replacement of instances.";
  }
  




  public boolean getNoReplacement()
  {
    return m_NoReplacement;
  }
  




  public void setNoReplacement(boolean value)
  {
    m_NoReplacement = value;
  }
  





  public String invertSelectionTipText()
  {
    return "Inverts the selection (only if instances are drawn WITHOUT replacement).";
  }
  






  public boolean getInvertSelection()
  {
    return m_InvertSelection;
  }
  





  public void setInvertSelection(boolean value)
  {
    m_InvertSelection = value;
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
  








  public void createSubsampleWithReplacement(Random random, int origSize, int sampleSize)
  {
    for (int i = 0; i < sampleSize; i++) {
      int index = random.nextInt(origSize);
      push((Instance)getInputFormat().instance(index).copy());
    }
  }
  








  public void createSubsampleWithoutReplacement(Random random, int origSize, int sampleSize)
  {
    if (sampleSize > origSize) {
      sampleSize = origSize;
      System.err.println("Resampling with replacement can only use percentage <=100% - Using full dataset!");
    }
    


    Vector<Integer> indices = new Vector(origSize);
    Vector<Integer> indicesNew = new Vector(sampleSize);
    

    for (int i = 0; i < origSize; i++) {
      indices.add(Integer.valueOf(i));
    }
    
    for (int i = 0; i < sampleSize; i++) {
      int index = random.nextInt(indices.size());
      indicesNew.add(indices.get(index));
      indices.remove(index);
    }
    
    if (getInvertSelection()) {
      indicesNew = indices;
    } else {
      Collections.sort(indicesNew);
    }
    for (int i = 0; i < indicesNew.size(); i++) {
      push((Instance)getInputFormat().instance(((Integer)indicesNew.get(i)).intValue()).copy());
    }
    
    indices.clear();
    indicesNew.clear();
    indices = null;
    indicesNew = null;
  }
  



  protected void createSubsample()
  {
    int origSize = getInputFormat().numInstances();
    int sampleSize = (int)(origSize * m_SampleSizePercent / 100.0D);
    Random random = new Random(m_RandomSeed);
    
    if (getNoReplacement()) {
      createSubsampleWithoutReplacement(random, origSize, sampleSize);
    } else {
      createSubsampleWithReplacement(random, origSize, sampleSize);
    }
  }
  



  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5548 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new Resample(), argv);
  }
}
