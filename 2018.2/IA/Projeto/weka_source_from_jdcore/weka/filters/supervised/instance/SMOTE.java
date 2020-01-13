package weka.filters.supervised.instance;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.SupervisedFilter;



















































































public class SMOTE
  extends Filter
  implements SupervisedFilter, OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -1653880819059250364L;
  protected int m_NearestNeighbors = 5;
  

  protected int m_RandomSeed = 1;
  

  protected double m_Percentage = 100.0D;
  

  protected String m_ClassValueIndex = "0";
  

  protected boolean m_DetectMinorityClass = true;
  


  public SMOTE() {}
  

  public String globalInfo()
  {
    return "Resamples a dataset by applying the Synthetic Minority Oversampling TEchnique (SMOTE). The original dataset must fit entirely in memory. The amount of SMOTE and number of nearest neighbors may be specified. For more information, see \n\n" + getTechnicalInformation().toString();
  }
  










  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    
    result.setValue(TechnicalInformation.Field.AUTHOR, "Nitesh V. Chawla et. al.");
    result.setValue(TechnicalInformation.Field.TITLE, "Synthetic Minority Over-sampling Technique");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Journal of Artificial Intelligence Research");
    
    result.setValue(TechnicalInformation.Field.YEAR, "2002");
    result.setValue(TechnicalInformation.Field.VOLUME, "16");
    result.setValue(TechnicalInformation.Field.PAGES, "321-357");
    
    return result;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9657 $");
  }
  






  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tSpecifies the random number seed\n\t(default 1)", "S", 1, "-S <num>"));
    

    newVector.addElement(new Option("\tSpecifies percentage of SMOTE instances to create.\n\t(default 100.0)\n", "P", 1, "-P <percentage>"));
    


    newVector.addElement(new Option("\tSpecifies the number of nearest neighbors to use.\n\t(default 5)\n", "K", 1, "-K <nearest-neighbors>"));
    


    newVector.addElement(new Option("\tSpecifies the index of the nominal class value to SMOTE\n\t(default 0: auto-detect non-empty minority class))\n", "C", 1, "-C <value-index>"));
    



    return newVector.elements();
  }
  

































  public void setOptions(String[] options)
    throws Exception
  {
    String seedStr = Utils.getOption('S', options);
    if (seedStr.length() != 0) {
      setRandomSeed(Integer.parseInt(seedStr));
    } else {
      setRandomSeed(1);
    }
    
    String percentageStr = Utils.getOption('P', options);
    if (percentageStr.length() != 0) {
      setPercentage(new Double(percentageStr).doubleValue());
    } else {
      setPercentage(100.0D);
    }
    
    String nnStr = Utils.getOption('K', options);
    if (nnStr.length() != 0) {
      setNearestNeighbors(Integer.parseInt(nnStr));
    } else {
      setNearestNeighbors(5);
    }
    
    String classValueIndexStr = Utils.getOption('C', options);
    if (classValueIndexStr.length() != 0) {
      setClassValue(classValueIndexStr);
    } else {
      m_DetectMinorityClass = true;
    }
  }
  






  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-C");
    result.add(getClassValue());
    
    result.add("-K");
    result.add("" + getNearestNeighbors());
    
    result.add("-P");
    result.add("" + getPercentage());
    
    result.add("-S");
    result.add("" + getRandomSeed());
    
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
  




  public void setRandomSeed(int value)
  {
    m_RandomSeed = value;
  }
  





  public String percentageTipText()
  {
    return "The percentage of SMOTE instances to create.";
  }
  




  public void setPercentage(double value)
  {
    if (value >= 0.0D) {
      m_Percentage = value;
    } else {
      System.err.println("Percentage must be >= 0!");
    }
  }
  



  public double getPercentage()
  {
    return m_Percentage;
  }
  





  public String nearestNeighborsTipText()
  {
    return "The number of nearest neighbors to use.";
  }
  




  public void setNearestNeighbors(int value)
  {
    if (value >= 1) {
      m_NearestNeighbors = value;
    } else {
      System.err.println("At least 1 neighbor necessary!");
    }
  }
  



  public int getNearestNeighbors()
  {
    return m_NearestNeighbors;
  }
  





  public String classValueTipText()
  {
    return "The index of the class value to which SMOTE should be applied. Use a value of 0 to auto-detect the non-empty minority class.";
  }
  





  public void setClassValue(String value)
  {
    m_ClassValueIndex = value;
    if (m_ClassValueIndex.equals("0")) {
      m_DetectMinorityClass = true;
    } else {
      m_DetectMinorityClass = false;
    }
  }
  




  public String getClassValue()
  {
    return m_ClassValueIndex;
  }
  








  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    super.setOutputFormat(instanceInfo);
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
    if (m_FirstBatchDone) {
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
      throw new IllegalStateException("No input instance format defined");
    }
    
    if (!m_FirstBatchDone)
    {
      doSMOTE();
    }
    flushInput();
    
    m_NewBatch = true;
    m_FirstBatchDone = true;
    return numPendingOutput() != 0;
  }
  




  protected void doSMOTE()
    throws Exception
  {
    int minIndex = 0;
    int min = Integer.MAX_VALUE;
    if (m_DetectMinorityClass)
    {
      int[] classCounts = getInputFormatattributeStatsgetInputFormatclassIndexnominalCounts;
      
      for (int i = 0; i < classCounts.length; i++) {
        if ((classCounts[i] != 0) && (classCounts[i] < min)) {
          min = classCounts[i];
          minIndex = i;
        }
      }
    } else {
      String classVal = getClassValue();
      if (classVal.equalsIgnoreCase("first")) {
        minIndex = 1;
      } else if (classVal.equalsIgnoreCase("last")) {
        minIndex = getInputFormat().numClasses();
      } else {
        minIndex = Integer.parseInt(classVal);
      }
      if (minIndex > getInputFormat().numClasses()) {
        throw new Exception("value index must be <= the number of classes");
      }
      minIndex--;
    }
    int nearestNeighbors;
    int nearestNeighbors;
    if (min <= getNearestNeighbors()) {
      nearestNeighbors = min - 1;
    } else {
      nearestNeighbors = getNearestNeighbors();
    }
    if (nearestNeighbors < 1) {
      throw new Exception("Cannot use 0 neighbors!");
    }
    

    Instances sample = getInputFormat().stringFreeStructure();
    Enumeration instanceEnum = getInputFormat().enumerateInstances();
    while (instanceEnum.hasMoreElements()) {
      Instance instance = (Instance)instanceEnum.nextElement();
      push((Instance)instance.copy());
      if ((int)instance.classValue() == minIndex) {
        sample.add(instance);
      }
    }
    

    Map vdmMap = new HashMap();
    Enumeration attrEnum = getInputFormat().enumerateAttributes();
    while (attrEnum.hasMoreElements()) {
      Attribute attr = (Attribute)attrEnum.nextElement();
      if ((!attr.equals(getInputFormat().classAttribute())) && (
        (attr.isNominal()) || (attr.isString()))) {
        double[][] vdm = new double[attr.numValues()][attr.numValues()];
        vdmMap.put(attr, vdm);
        int[] featureValueCounts = new int[attr.numValues()];
        int[][] featureValueCountsByClass = new int[getInputFormat().classAttribute().numValues()][attr.numValues()];
        
        instanceEnum = getInputFormat().enumerateInstances();
        while (instanceEnum.hasMoreElements()) {
          Instance instance = (Instance)instanceEnum.nextElement();
          int value = (int)instance.value(attr);
          int classValue = (int)instance.classValue();
          featureValueCounts[value] += 1;
          featureValueCountsByClass[classValue][value] += 1;
        }
        for (int valueIndex1 = 0; valueIndex1 < attr.numValues(); valueIndex1++) {
          for (int valueIndex2 = 0; valueIndex2 < attr.numValues(); valueIndex2++) {
            double sum = 0.0D;
            for (int classValueIndex = 0; classValueIndex < getInputFormat().numClasses(); 
                classValueIndex++) {
              double c1i = featureValueCountsByClass[classValueIndex][valueIndex1];
              double c2i = featureValueCountsByClass[classValueIndex][valueIndex2];
              double c1 = featureValueCounts[valueIndex1];
              double c2 = featureValueCounts[valueIndex2];
              double term1 = c1i / c1;
              double term2 = c2i / c2;
              sum += Math.abs(term1 - term2);
            }
            vdm[valueIndex1][valueIndex2] = sum;
          }
        }
      }
    }
    


    Random rand = new Random(getRandomSeed());
    


    List extraIndices = new LinkedList();
    double percentageRemainder = getPercentage() / 100.0D - Math.floor(getPercentage() / 100.0D);
    
    int extraIndicesCount = (int)(percentageRemainder * sample.numInstances());
    if (extraIndicesCount >= 1) {
      for (int i = 0; i < sample.numInstances(); i++) {
        extraIndices.add(Integer.valueOf(i));
      }
    }
    Collections.shuffle(extraIndices, rand);
    extraIndices = extraIndices.subList(0, extraIndicesCount);
    Set extraIndexSet = new HashSet(extraIndices);
    


    Instance[] nnArray = new Instance[nearestNeighbors];
    for (int i = 0; i < sample.numInstances(); i++) {
      Instance instanceI = sample.instance(i);
      
      List distanceToInstance = new LinkedList();
      for (int j = 0; j < sample.numInstances(); j++) {
        Instance instanceJ = sample.instance(j);
        if (i != j) {
          double distance = 0.0D;
          attrEnum = getInputFormat().enumerateAttributes();
          while (attrEnum.hasMoreElements()) {
            Attribute attr = (Attribute)attrEnum.nextElement();
            if (!attr.equals(getInputFormat().classAttribute())) {
              double iVal = instanceI.value(attr);
              double jVal = instanceJ.value(attr);
              if (attr.isNumeric()) {
                distance += Math.pow(iVal - jVal, 2.0D);
              } else {
                distance += ((double[][])(double[][])vdmMap.get(attr))[((int)iVal)][((int)jVal)];
              }
            }
          }
          distance = Math.pow(distance, 0.5D);
          distanceToInstance.add(new Object[] { Double.valueOf(distance), instanceJ });
        }
      }
      

      Collections.sort(distanceToInstance, new Comparator() {
        public int compare(Object o1, Object o2) {
          double distance1 = ((Double)((Object[])(Object[])o1)[0]).doubleValue();
          double distance2 = ((Double)((Object[])(Object[])o2)[0]).doubleValue();
          return Double.compare(distance1, distance2);
        }
        

      });
      Iterator entryIterator = distanceToInstance.iterator();
      int j = 0;
      while ((entryIterator.hasNext()) && (j < nearestNeighbors)) {
        nnArray[j] = ((Instance)((Object[])(Object[])entryIterator.next())[1]);
        j++;
      }
      

      int n = (int)Math.floor(getPercentage() / 100.0D);
      while ((n > 0) || (extraIndexSet.remove(Integer.valueOf(i)))) {
        double[] values = new double[sample.numAttributes()];
        int nn = rand.nextInt(nearestNeighbors);
        attrEnum = getInputFormat().enumerateAttributes();
        while (attrEnum.hasMoreElements()) {
          Attribute attr = (Attribute)attrEnum.nextElement();
          if (!attr.equals(getInputFormat().classAttribute())) {
            if (attr.isNumeric()) {
              double dif = nnArray[nn].value(attr) - instanceI.value(attr);
              double gap = rand.nextDouble();
              values[attr.index()] = (instanceI.value(attr) + gap * dif);
            } else if (attr.isDate()) {
              double dif = nnArray[nn].value(attr) - instanceI.value(attr);
              double gap = rand.nextDouble();
              values[attr.index()] = ((instanceI.value(attr) + gap * dif));
            } else {
              int[] valueCounts = new int[attr.numValues()];
              int iVal = (int)instanceI.value(attr);
              valueCounts[iVal] += 1;
              for (int nnEx = 0; nnEx < nearestNeighbors; nnEx++) {
                int val = (int)nnArray[nnEx].value(attr);
                valueCounts[val] += 1;
              }
              int maxIndex = 0;
              int max = Integer.MIN_VALUE;
              for (int index = 0; index < attr.numValues(); index++) {
                if (valueCounts[index] > max) {
                  max = valueCounts[index];
                  maxIndex = index;
                }
              }
              values[attr.index()] = maxIndex;
            }
          }
        }
        values[sample.classIndex()] = minIndex;
        Instance synthetic = new Instance(1.0D, values);
        push(synthetic);
        n--;
      }
    }
  }
  




  public static void main(String[] args)
  {
    runFilter(new SMOTE(), args);
  }
}
