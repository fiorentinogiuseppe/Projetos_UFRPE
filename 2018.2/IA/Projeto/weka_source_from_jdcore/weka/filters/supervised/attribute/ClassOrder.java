package weka.filters.supervised.attribute;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.SupervisedFilter;



















































public class ClassOrder
  extends Filter
  implements SupervisedFilter, OptionHandler
{
  static final long serialVersionUID = -2116226838887628411L;
  private long m_Seed = 1L;
  

  private Random m_Random = null;
  




  private int[] m_Converter = null;
  

  private Attribute m_ClassAttribute = null;
  

  private int m_ClassOrder = 0;
  


  public static final int FREQ_ASCEND = 0;
  

  public static final int FREQ_DESCEND = 1;
  

  public static final int RANDOM = 2;
  

  private double[] m_ClassCounts = null;
  


  public ClassOrder() {}
  


  public String globalInfo()
  {
    return "Changes the order of the classes so that the class values are no longer of in the order specified in the header. The values will be in the order specified by the user -- it could be either in ascending/descending order by the class frequency or in random order. Note that this filter currently does not change the header, only the class values of the instances, so there is not much point in using it in conjunction with the FilteredClassifier. The value can also be converted back using 'originalValue(double value)' procedure.";
  }
  













  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    
    newVector.addElement(new Option("\tSpecify the seed of randomization\n\tused to randomize the class\n\torder (default: 1)", "R", 1, "-R <seed>"));
    



    newVector.addElement(new Option("\tSpecify the class order to be\n\tsorted, could be 0: ascending\n\t1: descending and 2: random.(default: 0)", "C", 1, "-C <order>"));
    



    return newVector.elements();
  }
  





















  public void setOptions(String[] options)
    throws Exception
  {
    String seedString = Utils.getOption('R', options);
    if (seedString.length() != 0) {
      m_Seed = Long.parseLong(seedString);
    } else {
      m_Seed = 1L;
    }
    String orderString = Utils.getOption('C', options);
    if (orderString.length() != 0) {
      m_ClassOrder = Integer.parseInt(orderString);
    } else {
      m_ClassOrder = 0;
    }
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
    m_Random = null;
  }
  





  public String[] getOptions()
  {
    String[] options = new String[4];
    int current = 0;
    
    options[(current++)] = "-R";
    options[(current++)] = ("" + m_Seed);
    options[(current++)] = "-C";
    options[(current++)] = ("" + m_ClassOrder);
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public String seedTipText()
  {
    return "Specify the seed of randomization of the class order";
  }
  




  public long getSeed()
  {
    return m_Seed;
  }
  




  public void setSeed(long seed)
  {
    m_Seed = seed;
    m_Random = null;
  }
  





  public String classOrderTipText()
  {
    return "Specify the class order after the filtering";
  }
  




  public int getClassOrder()
  {
    return m_ClassOrder;
  }
  




  public void setClassOrder(int order)
  {
    m_ClassOrder = order;
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
    super.setInputFormat(new Instances(instanceInfo, 0));
    
    m_ClassAttribute = instanceInfo.classAttribute();
    m_Random = new Random(m_Seed);
    m_Converter = null;
    
    int numClasses = instanceInfo.numClasses();
    m_ClassCounts = new double[numClasses];
    return false;
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
    


    if (m_Converter != null) {
      Instance datum = (Instance)instance.copy();
      if (!datum.isMissing(m_ClassAttribute)) {
        datum.setClassValue(m_Converter[((int)datum.classValue())]);
      }
      push(datum);
      return true;
    }
    
    if (!instance.isMissing(m_ClassAttribute)) {
      m_ClassCounts[((int)instance.classValue())] += instance.weight();
    }
    
    bufferInput(instance);
    return false;
  }
  












  public boolean batchFinished()
    throws Exception
  {
    Instances data = getInputFormat();
    if (data == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_Converter == null)
    {

      int[] randomIndices = new int[m_ClassCounts.length];
      for (int i = 0; i < randomIndices.length; i++) {
        randomIndices[i] = i;
      }
      for (int j = randomIndices.length - 1; j > 0; j--) {
        int toSwap = m_Random.nextInt(j + 1);
        int tmpIndex = randomIndices[j];
        randomIndices[j] = randomIndices[toSwap];
        randomIndices[toSwap] = tmpIndex;
      }
      
      double[] randomizedCounts = new double[m_ClassCounts.length];
      for (int i = 0; i < randomizedCounts.length; i++) {
        randomizedCounts[i] = m_ClassCounts[randomIndices[i]];
      }
      


      if (m_ClassOrder == 2) {
        m_Converter = randomIndices;
        m_ClassCounts = randomizedCounts;
      } else {
        int[] sorted = Utils.sort(randomizedCounts);
        m_Converter = new int[sorted.length];
        if (m_ClassOrder == 0) {
          for (int i = 0; i < sorted.length; i++) {
            m_Converter[i] = randomIndices[sorted[i]];
          }
        } else if (m_ClassOrder == 1) {
          for (int i = 0; i < sorted.length; i++) {
            m_Converter[i] = randomIndices[sorted[(sorted.length - i - 1)]];
          }
        } else {
          throw new IllegalArgumentException("Class order not defined!");
        }
        

        double[] tmp2 = new double[m_ClassCounts.length];
        for (int i = 0; i < m_Converter.length; i++) {
          tmp2[i] = m_ClassCounts[m_Converter[i]];
        }
        m_ClassCounts = tmp2;
      }
      

      FastVector values = new FastVector(data.classAttribute().numValues());
      for (int i = 0; i < data.numClasses(); i++) {
        values.addElement(data.classAttribute().value(m_Converter[i]));
      }
      FastVector newVec = new FastVector(data.numAttributes());
      for (int i = 0; i < data.numAttributes(); i++) {
        if (i == data.classIndex()) {
          newVec.addElement(new Attribute(data.classAttribute().name(), values, data.classAttribute().getMetadata()));
        }
        else {
          newVec.addElement(data.attribute(i));
        }
      }
      Instances newInsts = new Instances(data.relationName(), newVec, 0);
      newInsts.setClassIndex(data.classIndex());
      setOutputFormat(newInsts);
      

      int[] temp = new int[m_Converter.length];
      for (int i = 0; i < temp.length; i++) {
        temp[m_Converter[i]] = i;
      }
      m_Converter = temp;
      

      for (int xyz = 0; xyz < data.numInstances(); xyz++) {
        Instance datum = data.instance(xyz);
        if (!datum.isMissing(datum.classIndex())) {
          datum.setClassValue(m_Converter[((int)datum.classValue())]);
        }
        push(datum);
      }
    }
    flushInput();
    m_NewBatch = true;
    return numPendingOutput() != 0;
  }
  






  public double[] getClassCounts()
  {
    if (m_ClassAttribute.isNominal()) {
      return m_ClassCounts;
    }
    return null;
  }
  







  public double[] distributionsByOriginalIndex(double[] before)
  {
    double[] after = new double[m_Converter.length];
    for (int i = 0; i < m_Converter.length; i++) {
      after[i] = before[m_Converter[i]];
    }
    return after;
  }
  










  public double originalValue(double value)
    throws Exception
  {
    if (m_Converter == null) {
      throw new IllegalStateException("Coverter table not defined yet!");
    }
    for (int i = 0; i < m_Converter.length; i++) {
      if ((int)value == m_Converter[i])
        return i;
    }
    return -1.0D;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5541 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new ClassOrder(), argv);
  }
}
