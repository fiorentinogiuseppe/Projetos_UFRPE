package weka.filters.supervised.instance;

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
import weka.filters.SupervisedFilter;





























































public class Resample
  extends Filter
  implements SupervisedFilter, OptionHandler
{
  static final long serialVersionUID = 7079064953548300681L;
  protected double m_SampleSizePercent = 100.0D;
  

  protected int m_RandomSeed = 1;
  

  protected double m_BiasToUniformClass = 0.0D;
  

  protected boolean m_NoReplacement = false;
  



  protected boolean m_InvertSelection = false;
  


  public Resample() {}
  

  public String globalInfo()
  {
    return "Produces a random subsample of a dataset using either sampling with replacement or without replacement.\nThe original dataset must fit entirely in memory. The number of instances in the generated dataset may be specified. The dataset must have a nominal class attribute. If not, use the unsupervised version. The filter can be made to maintain the class distribution in the subsample, or to bias the class distribution toward a uniform distribution. When used in batch mode (i.e. in the FilteredClassifier), subsequent batches are NOT resampled.";
  }
  













  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tSpecify the random number seed (default 1)", "S", 1, "-S <num>"));
    


    result.addElement(new Option("\tThe size of the output dataset, as a percentage of\n\tthe input dataset (default 100)", "Z", 1, "-Z <num>"));
    



    result.addElement(new Option("\tBias factor towards uniform class distribution.\n\t0 = distribution in input data -- 1 = uniform distribution.\n\t(default 0)", "B", 1, "-B <num>"));
    




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
    tmpStr = Utils.getOption('B', options);
    if (tmpStr.length() != 0) {
      setBiasToUniformClass(Double.parseDouble(tmpStr));
    } else {
      setBiasToUniformClass(0.0D);
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
    
    result.add("-B");
    result.add("" + getBiasToUniformClass());
    
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
  





  public String biasToUniformClassTipText()
  {
    return "Whether to use bias towards a uniform class. A value of 0 leaves the class distribution as-is, a value of 1 ensures the class distribution is uniform in the output data.";
  }
  








  public double getBiasToUniformClass()
  {
    return m_BiasToUniformClass;
  }
  






  public void setBiasToUniformClass(double newBiasToUniformClass)
  {
    m_BiasToUniformClass = newBiasToUniformClass;
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
  





  public String sampleSizePercentTipText()
  {
    return "The subsample size as a percentage of the original set.";
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
  










  public void createSubsampleWithReplacement(Random random, int origSize, int sampleSize, int actualClasses, int[] classIndices)
  {
    for (int i = 0; i < sampleSize; i++) {
      int index = 0;
      if (random.nextDouble() < m_BiasToUniformClass)
      {
        int cIndex = random.nextInt(actualClasses);
        int j = 0; for (int k = 0; j < classIndices.length - 1; j++) {
          if ((classIndices[j] != classIndices[(j + 1)]) && (k++ >= cIndex))
          {
            index = classIndices[j] + random.nextInt(classIndices[(j + 1)] - classIndices[j]);
            
            break;
          }
        }
      }
      else {
        index = random.nextInt(origSize);
      }
      push((Instance)getInputFormat().instance(index).copy());
    }
  }
  










  public void createSubsampleWithoutReplacement(Random random, int origSize, int sampleSize, int actualClasses, int[] classIndices)
  {
    if (sampleSize > origSize) {
      sampleSize = origSize;
      System.err.println("Resampling without replacement can only use percentage <=100% - Using full dataset!");
    }
    


    Vector<Integer>[] indices = new Vector[classIndices.length - 1];
    Vector<Integer>[] indicesNew = new Vector[classIndices.length - 1];
    

    for (int i = 0; i < classIndices.length - 1; i++) {
      indices[i] = new Vector(classIndices[(i + 1)] - classIndices[i]);
      indicesNew[i] = new Vector(indices[i].capacity());
      for (int n = classIndices[i]; n < classIndices[(i + 1)]; n++) {
        indices[i].add(Integer.valueOf(n));
      }
    }
    
    int currentSize = origSize;
    for (int i = 0; i < sampleSize; i++) {
      int index = 0;
      if (random.nextDouble() < m_BiasToUniformClass)
      {
        int cIndex = random.nextInt(actualClasses);
        int j = 0; for (int k = 0; j < classIndices.length - 1; j++) {
          if ((classIndices[j] != classIndices[(j + 1)]) && (k++ >= cIndex))
          {
            if (indices[j].size() == 0) {
              i--;
              break;
            }
            
            index = random.nextInt(indices[j].size());
            indicesNew[j].add(indices[j].get(index));
            indices[j].remove(index);
            break;
          }
        }
      }
      else {
        index = random.nextInt(currentSize);
        for (int n = 0; n < actualClasses; n++) {
          if (index < indices[n].size()) {
            indicesNew[n].add(indices[n].get(index));
            indices[n].remove(index);
            break;
          }
          
          index -= indices[n].size();
        }
        
        currentSize--;
      }
    }
    

    if (getInvertSelection()) {
      indicesNew = indices;
    }
    else {
      for (int i = 0; i < indicesNew.length; i++) {
        Collections.sort(indicesNew[i]);
      }
    }
    
    for (int i = 0; i < indicesNew.length; i++) {
      for (int n = 0; n < indicesNew[i].size(); n++) {
        push((Instance)getInputFormat().instance(((Integer)indicesNew[i].get(n)).intValue()).copy());
      }
    }
    
    for (int i = 0; i < indices.length; i++) {
      indices[i].clear();
      indicesNew[i].clear();
    }
    indices = null;
    indicesNew = null;
  }
  



  protected void createSubsample()
  {
    int origSize = getInputFormat().numInstances();
    int sampleSize = (int)(origSize * m_SampleSizePercent / 100.0D);
    



    getInputFormat().sort(getInputFormat().classIndex());
    

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
    
    int actualClasses = 0;
    for (int i = 0; i < classIndices.length - 1; i++) {
      if (classIndices[i] != classIndices[(i + 1)]) {
        actualClasses++;
      }
    }
    

    Random random = new Random(m_RandomSeed);
    

    if (getNoReplacement()) {
      createSubsampleWithoutReplacement(random, origSize, sampleSize, actualClasses, classIndices);
    }
    else {
      createSubsampleWithReplacement(random, origSize, sampleSize, actualClasses, classIndices);
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5542 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new Resample(), argv);
  }
}
