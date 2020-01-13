package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SingleIndex;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;






















































public class AddNoise
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  static final long serialVersionUID = -8499673222857299082L;
  private SingleIndex m_AttIndex = new SingleIndex("last");
  

  private boolean m_UseMissing = false;
  

  private int m_Percent = 10;
  

  private int m_RandomSeed = 1;
  


  public AddNoise() {}
  


  public String globalInfo()
  {
    return "An instance filter that changes a percentage of a given attributes values. The attribute must be nominal. Missing value can be treated as value itself.";
  }
  







  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tIndex of the attribute to be changed \n\t(default last attribute)", "C", 1, "-C <col>"));
    


    newVector.addElement(new Option("\tTreat missing values as an extra value \n", "M", 1, "-M"));
    

    newVector.addElement(new Option("\tSpecify the percentage of noise introduced \n\tto the data (default 10)", "P", 1, "-P <num>"));
    


    newVector.addElement(new Option("\tSpecify the random number seed (default 1)", "S", 1, "-S <num>"));
    


    return newVector.elements();
  }
  

























  public void setOptions(String[] options)
    throws Exception
  {
    String indexString = Utils.getOption('C', options);
    if (indexString.length() != 0) {
      setAttributeIndex(indexString);
    } else {
      setAttributeIndex("last");
    }
    
    if (Utils.getFlag('M', options)) {
      setUseMissing(true);
    }
    
    String percentString = Utils.getOption('P', options);
    if (percentString.length() != 0) {
      setPercent((int)Double.valueOf(percentString).doubleValue());
    } else {
      setPercent(10);
    }
    
    String seedString = Utils.getOption('S', options);
    if (seedString.length() != 0) {
      setRandomSeed(Integer.parseInt(seedString));
    } else {
      setRandomSeed(1);
    }
  }
  






  public String[] getOptions()
  {
    String[] options = new String[7];
    int current = 0;
    
    options[(current++)] = "-C";options[(current++)] = ("" + getAttributeIndex());
    
    if (getUseMissing()) {
      options[(current++)] = "-M";
    }
    
    options[(current++)] = "-P";options[(current++)] = ("" + getPercent());
    
    options[(current++)] = "-S";options[(current++)] = ("" + getRandomSeed());
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  






  public String useMissingTipText()
  {
    return "Flag to set if missing values are used.";
  }
  





  public boolean getUseMissing()
  {
    return m_UseMissing;
  }
  





  public void setUseMissing(boolean newUseMissing)
  {
    m_UseMissing = newUseMissing;
  }
  






  public String randomSeedTipText()
  {
    return "Random number seed.";
  }
  





  public int getRandomSeed()
  {
    return m_RandomSeed;
  }
  





  public void setRandomSeed(int newSeed)
  {
    m_RandomSeed = newSeed;
  }
  






  public String percentTipText()
  {
    return "Percentage of introduced noise to data.";
  }
  





  public int getPercent()
  {
    return m_Percent;
  }
  





  public void setPercent(int newPercent)
  {
    m_Percent = newPercent;
  }
  






  public String attributeIndexTipText()
  {
    return "Index of the attribute that is to changed.";
  }
  





  public String getAttributeIndex()
  {
    return m_AttIndex.getSingleIndex();
  }
  





  public void setAttributeIndex(String attIndex)
  {
    m_AttIndex.setSingleIndex(attIndex);
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
    

    m_AttIndex.setUpper(getInputFormat().numAttributes() - 1);
    


    if (!getInputFormat().attribute(m_AttIndex.getIndex()).isNominal()) {
      throw new Exception("Adding noise is not possible:Chosen attribute is numeric.");
    }
    


    if ((getInputFormat().attribute(m_AttIndex.getIndex()).numValues() < 2) && (!m_UseMissing))
    {
      throw new Exception("Adding noise is not possible:Chosen attribute has less than two values.");
    }
    

    setOutputFormat(getInputFormat());
    m_NewBatch = true;
    return false;
  }
  








  public boolean input(Instance instance)
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new Exception("No input instance format defined");
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
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new Exception("No input instance format defined");
    }
    

    addNoise(getInputFormat(), m_RandomSeed, m_Percent, m_AttIndex.getIndex(), m_UseMissing);
    

    for (int i = 0; i < getInputFormat().numInstances(); i++) {
      push((Instance)getInputFormat().instance(i).copy());
    }
    
    flushInput();
    
    m_NewBatch = true;
    m_FirstBatchDone = true;
    return numPendingOutput() != 0;
  }
  





















  public void addNoise(Instances instances, int seed, int percent, int attIndex, boolean useMissing)
  {
    double splitPercent = percent;
    

    int[] indexList = new int[instances.numInstances()];
    for (int i = 0; i < instances.numInstances(); i++) {
      indexList[i] = i;
    }
    

    Random random = new Random(seed);
    for (int i = instances.numInstances() - 1; i >= 0; i--) {
      int hValue = indexList[i];
      int hIndex = (int)(random.nextDouble() * i);
      indexList[i] = indexList[hIndex];
      indexList[hIndex] = hValue;
    }
    





    int numValues = instances.attribute(attIndex).numValues();
    
    int[] partition_count = new int[numValues];
    int[] partition_max = new int[numValues];
    int missing_count = 0;
    int missing_max = 0;
    
    for (int i = 0; i < numValues; i++) {
      partition_count[i] = 0;
      partition_max[i] = 0;
    }
    



    Enumeration e = instances.enumerateInstances();
    while (e.hasMoreElements()) {
      Instance instance = (Instance)e.nextElement();
      if (instance.isMissing(attIndex)) {
        missing_max++;
      }
      else {
        int j = (int)instance.value(attIndex);
        partition_max[((int)instance.value(attIndex))] += 1;
      }
    }
    



    if (!useMissing) {
      missing_max = missing_count;
    } else {
      missing_max = (int)(missing_max / 100.0D * splitPercent + 0.5D);
    }
    int sum_max = missing_max;
    for (int i = 0; i < numValues; i++) {
      partition_max[i] = ((int)(partition_max[i] / 100.0D * splitPercent + 0.5D));
      
      sum_max += partition_max[i];
    }
    


    int sum_count = 0;
    



    Random randomValue = new Random(seed);
    int numOfValues = instances.attribute(attIndex).numValues();
    for (int i = 0; i < instances.numInstances(); i++) {
      if (sum_count >= sum_max) break;
      Instance currInstance = instances.instance(indexList[i]);
      
      if (currInstance.isMissing(attIndex)) {
        if (missing_count < missing_max) {
          changeValueRandomly(randomValue, numOfValues, attIndex, currInstance, useMissing);
          



          missing_count++;
          sum_count++;
        }
      }
      else {
        int vIndex = (int)currInstance.value(attIndex);
        if (partition_count[vIndex] < partition_max[vIndex]) {
          changeValueRandomly(randomValue, numOfValues, attIndex, currInstance, useMissing);
          



          partition_count[vIndex] += 1;
          sum_count++;
        }
      }
    }
  }
  





  private void changeValueRandomly(Random r, int numOfValues, int indexOfAtt, Instance instance, boolean useMissing)
  {
    int currValue;
    




    int currValue;
    



    if (instance.isMissing(indexOfAtt)) {
      currValue = numOfValues;
    } else {
      currValue = (int)instance.value(indexOfAtt);
    }
    

    if ((numOfValues == 2) && (!instance.isMissing(indexOfAtt))) {
      instance.setValue(indexOfAtt, (currValue + 1) % 2);
    } else {
      for (;;)
      {
        int newValue;
        
        int newValue;
        if (useMissing) {
          newValue = (int)(r.nextDouble() * (numOfValues + 1));
        } else {
          newValue = (int)(r.nextDouble() * numOfValues);
        }
        
        if (newValue != currValue)
        {

          if (newValue == numOfValues) { instance.setMissing(indexOfAtt); break; }
          instance.setValue(indexOfAtt, newValue);
          break;
        }
      }
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5543 $");
  }
  





  public static void main(String[] argv)
  {
    runFilter(new AddNoise(), argv);
  }
}
