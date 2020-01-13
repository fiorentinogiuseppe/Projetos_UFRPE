package weka.filters.unsupervised.attribute;

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
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;







































































































public class RandomProjection
  extends Filter
  implements UnsupervisedFilter, OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 4428905532728645880L;
  protected int m_k = 10;
  




  protected double m_percent = 0.0D;
  



  protected boolean m_useGaussian = false;
  


  public static final int SPARSE1 = 1;
  

  public static final int SPARSE2 = 2;
  

  public static final int GAUSSIAN = 3;
  

  public static final Tag[] TAGS_DSTRS_TYPE = { new Tag(1, "Sparse1"), new Tag(2, "Sparse2"), new Tag(3, "Gaussian") };
  




  protected int m_distribution = 1;
  




  protected boolean m_useReplaceMissing = false;
  

  protected boolean m_OutputFormatDefined = false;
  


  protected Filter m_ntob;
  

  protected Filter m_replaceMissing;
  

  protected long m_rndmSeed = 42L;
  

  protected double[][] m_rmatrix;
  

  protected Random m_random;
  


  public RandomProjection() {}
  


  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tThe number of dimensions (attributes) the data should be reduced to\n\t(default 10; exclusive of the class attribute, if it is set).", "N", 1, "-N <number>"));
    



    newVector.addElement(new Option("\tThe distribution to use for calculating the random matrix.\n\tSparse1 is:\n\t  sqrt(3)*{-1 with prob(1/6), 0 with prob(2/3), +1 with prob(1/6)}\n\tSparse2 is:\n\t  {-1 with prob(1/2), +1 with prob(1/2)}\n", "D", 1, "-D [SPARSE1|SPARSE2|GAUSSIAN]"));
    










    newVector.addElement(new Option("\tThe percentage of dimensions (attributes) the data should\n\tbe reduced to (exclusive of the class attribute, if it is set). This -N\n\toption is ignored if this option is present or is greater\n\tthan zero.", "P", 1, "-P <percent>"));
    





    newVector.addElement(new Option("\tReplace missing values using the ReplaceMissingValues filter", "M", 0, "-M"));
    


    newVector.addElement(new Option("\tThe random seed for the random number generator used for\n\tcalculating the random matrix (default 42).", "R", 0, "-R <num>"));
    


    return newVector.elements();
  }
  














































  public void setOptions(String[] options)
    throws Exception
  {
    String mString = Utils.getOption('P', options);
    if (mString.length() != 0) {
      setPercent(Double.parseDouble(mString));
    }
    else {
      setPercent(0.0D);
      mString = Utils.getOption('N', options);
      if (mString.length() != 0) {
        setNumberOfAttributes(Integer.parseInt(mString));
      } else {
        setNumberOfAttributes(10);
      }
    }
    
    mString = Utils.getOption('R', options);
    if (mString.length() != 0) {
      setRandomSeed(Long.parseLong(mString));
    }
    
    mString = Utils.getOption('D', options);
    if (mString.length() != 0) {
      if (mString.equalsIgnoreCase("sparse1")) {
        setDistribution(new SelectedTag(1, TAGS_DSTRS_TYPE));
      } else if (mString.equalsIgnoreCase("sparse2")) {
        setDistribution(new SelectedTag(2, TAGS_DSTRS_TYPE));
      } else if (mString.equalsIgnoreCase("gaussian")) {
        setDistribution(new SelectedTag(3, TAGS_DSTRS_TYPE));
      }
    }
    
    if (Utils.getFlag('M', options)) {
      setReplaceMissingValues(true);
    } else {
      setReplaceMissingValues(false);
    }
  }
  












  public String[] getOptions()
  {
    String[] options = new String[10];
    int current = 0;
    




    if (getReplaceMissingValues()) {
      options[(current++)] = "-M";
    }
    
    if (getPercent() <= 0.0D) {
      options[(current++)] = "-N";
      options[(current++)] = ("" + getNumberOfAttributes());
    } else {
      options[(current++)] = "-P";
      options[(current++)] = ("" + getPercent());
    }
    
    options[(current++)] = "-R";
    options[(current++)] = ("" + getRandomSeed());
    
    SelectedTag t = getDistribution();
    options[(current++)] = "-D";
    options[(current++)] = ("" + t.getSelectedTag().getReadable());
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  






  public String globalInfo()
  {
    return "Reduces the dimensionality of the data by projecting it onto a lower dimensional subspace using a random matrix with columns of unit length (i.e. It will reduce the number of attributes in the data while preserving much of its variation like PCA, but at a much less computational cost).\nIt first applies the  NominalToBinary filter to convert all attributes to numeric before reducing the dimension. It preserves the class attribute.\n\nFor more information, see:\n\n" + getTechnicalInformation().toString();
  }
  


















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Dmitriy Fradkin and David Madigan");
    result.setValue(TechnicalInformation.Field.TITLE, "Experiments with random projections for machine learning");
    
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "KDD '03: Proceedings of the ninth ACM SIGKDD international conference on Knowledge discovery and data mining");
    


    result.setValue(TechnicalInformation.Field.YEAR, "003");
    result.setValue(TechnicalInformation.Field.PAGES, "517-522");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "ACM Press");
    result.setValue(TechnicalInformation.Field.ADDRESS, "New York, NY, USA");
    
    return result;
  }
  






  public String numberOfAttributesTipText()
  {
    return "The number of dimensions (attributes) the data should be reduced to.";
  }
  





  public void setNumberOfAttributes(int newAttNum)
  {
    m_k = newAttNum;
  }
  





  public int getNumberOfAttributes()
  {
    return m_k;
  }
  






  public String percentTipText()
  {
    return " The percentage of dimensions (attributes) the data should be reduced to  (inclusive of the class attribute). This  NumberOfAttributes option is ignored if this option is present or is greater than zero.";
  }
  








  public void setPercent(double newPercent)
  {
    if (newPercent > 0.0D) {
      newPercent /= 100.0D;
    }
    m_percent = newPercent;
  }
  




  public double getPercent()
  {
    return m_percent * 100.0D;
  }
  





  public String randomSeedTipText()
  {
    return "The random seed used by the random number generator used for generating the random matrix ";
  }
  





  public void setRandomSeed(long seed)
  {
    m_rndmSeed = seed;
  }
  




  public long getRandomSeed()
  {
    return m_rndmSeed;
  }
  





  public String distributionTipText()
  {
    return "The distribution to use for calculating the random matrix.\nSparse1 is:\n sqrt(3) * { -1 with prob(1/6), \n               0 with prob(2/3),  \n              +1 with prob(1/6) } \nSparse2 is:\n { -1 with prob(1/2), \n   +1 with prob(1/2) } ";
  }
  










  public void setDistribution(SelectedTag newDstr)
  {
    if (newDstr.getTags() == TAGS_DSTRS_TYPE) {
      m_distribution = newDstr.getSelectedTag().getID();
    }
  }
  





  public SelectedTag getDistribution()
  {
    return new SelectedTag(m_distribution, TAGS_DSTRS_TYPE);
  }
  






  public String replaceMissingValuesTipText()
  {
    return "If set the filter uses weka.filters.unsupervised.attribute.ReplaceMissingValues to replace the missing values";
  }
  





  public void setReplaceMissingValues(boolean t)
  {
    m_useReplaceMissing = t;
  }
  




  public boolean getReplaceMissingValues()
  {
    return m_useReplaceMissing;
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
    





    for (int i = 0; i < instanceInfo.numAttributes(); i++) {
      if ((i != instanceInfo.classIndex()) && (instanceInfo.attribute(i).isNominal()))
      {
        if (instanceInfo.classIndex() >= 0) {
          m_ntob = new weka.filters.supervised.attribute.NominalToBinary(); break;
        }
        m_ntob = new NominalToBinary();
        

        break;
      }
    }
    



    boolean temp = true;
    if (m_replaceMissing != null) {
      m_replaceMissing = new ReplaceMissingValues();
      
      if (m_replaceMissing.setInputFormat(instanceInfo)) {
        temp = true;
      } else {
        temp = false;
      }
    }
    
    if (m_ntob != null) {
      if (m_ntob.setInputFormat(instanceInfo)) {
        setOutputFormat();
        return temp;
      }
      return false;
    }
    
    setOutputFormat();
    return temp;
  }
  








  public boolean input(Instance instance)
    throws Exception
  {
    Instance newInstance = null;
    
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      

      m_NewBatch = false;
    }
    
    boolean replaceDone = false;
    if (m_replaceMissing != null) {
      if (m_replaceMissing.input(instance)) {
        if (!m_OutputFormatDefined) {
          setOutputFormat();
        }
        newInstance = m_replaceMissing.output();
        replaceDone = true;
      } else {
        return false;
      }
    }
    

    if (m_ntob != null) {
      if (!replaceDone) {
        newInstance = instance;
      }
      if (m_ntob.input(newInstance)) {
        if (!m_OutputFormatDefined) {
          setOutputFormat();
        }
        newInstance = m_ntob.output();
        newInstance = convertInstance(newInstance);
        push(newInstance);
        return true;
      }
      return false;
    }
    
    if (!replaceDone) {
      newInstance = instance;
    }
    newInstance = convertInstance(newInstance);
    push(newInstance);
    return true;
  }
  







  public boolean batchFinished()
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new NullPointerException("No input instance format defined");
    }
    
    boolean conversionDone = false;
    if ((m_replaceMissing != null) && 
      (m_replaceMissing.batchFinished()))
    {
      Instance instance;
      while ((instance = m_replaceMissing.output()) != null) {
        if (!m_OutputFormatDefined) {
          setOutputFormat();
        }
        if (m_ntob != null) {
          m_ntob.input(instance);
        } else {
          Instance newInstance = convertInstance(instance);
          push(newInstance);
        }
      }
      
      if ((m_ntob != null) && 
        (m_ntob.batchFinished()))
      {
        while ((instance = m_ntob.output()) != null) {
          if (!m_OutputFormatDefined) {
            setOutputFormat();
          }
          Instance newInstance = convertInstance(instance);
          push(newInstance);
        }
        m_ntob = null;
      }
      
      m_replaceMissing = null;
      conversionDone = true;
    }
    

    if ((!conversionDone) && (m_ntob != null) && 
      (m_ntob.batchFinished())) {
      Instance instance;
      while ((instance = m_ntob.output()) != null) {
        if (!m_OutputFormatDefined) {
          setOutputFormat();
        }
        Instance newInstance = convertInstance(instance);
        push(newInstance);
      }
      m_ntob = null;
    }
    
    m_OutputFormatDefined = false;
    return super.batchFinished();
  }
  
  protected void setOutputFormat() {
    Instances currentFormat;
    Instances currentFormat;
    if (m_ntob != null) {
      currentFormat = m_ntob.getOutputFormat();
    } else {
      currentFormat = getInputFormat();
    }
    
    if (m_percent > 0.0D) {
      m_k = ((int)((getInputFormat().numAttributes() - 1) * m_percent));
    }
    





    int newClassIndex = -1;
    FastVector attributes = new FastVector();
    for (int i = 0; i < m_k; i++) {
      attributes.addElement(new Attribute("K" + (i + 1)));
    }
    if (currentFormat.classIndex() != -1)
    {
      attributes.addElement(currentFormat.attribute(currentFormat.classIndex()).copy());
      
      newClassIndex = attributes.size() - 1;
    }
    
    Instances newFormat = new Instances(currentFormat.relationName(), attributes, 0);
    if (newClassIndex != -1) {
      newFormat.setClassIndex(newClassIndex);
    }
    m_OutputFormatDefined = true;
    
    m_random = new Random();
    m_random.setSeed(m_rndmSeed);
    
    m_rmatrix = new double[m_k][currentFormat.numAttributes()];
    if (m_distribution == 3) {
      for (int i = 0; i < m_rmatrix.length; i++) {
        for (int j = 0; j < m_rmatrix[i].length; j++) {
          m_rmatrix[i][j] = m_random.nextGaussian();
        }
      }
    } else {
      boolean useDstrWithZero = m_distribution == 1;
      for (int i = 0; i < m_rmatrix.length; i++) {
        for (int j = 0; j < m_rmatrix[i].length; j++) {
          m_rmatrix[i][j] = rndmNum(useDstrWithZero);
        }
      }
    }
    
    setOutputFormat(newFormat);
  }
  







  protected Instance convertInstance(Instance currentInstance)
  {
    double[] vals = new double[getOutputFormat().numAttributes()];
    int classIndex = m_ntob == null ? getInputFormat().classIndex() : m_ntob.getOutputFormat().classIndex();
    


    for (int i = 0; i < m_k; i++) {
      vals[i] = computeRandomProjection(i, classIndex, currentInstance);
    }
    if (classIndex != -1) {
      vals[m_k] = currentInstance.value(classIndex);
    }
    
    Instance newInstance = new Instance(currentInstance.weight(), vals);
    newInstance.setDataset(getOutputFormat());
    
    return newInstance;
  }
  










  protected double computeRandomProjection(int rpIndex, int classIndex, Instance instance)
  {
    double sum = 0.0D;
    for (int i = 0; i < instance.numValues(); i++) {
      int index = instance.index(i);
      if (index != classIndex) {
        double value = instance.valueSparse(i);
        if (!Instance.isMissingValue(value)) {
          sum += m_rmatrix[rpIndex][index] * value;
        }
      }
    }
    return sum;
  }
  
  private static final int[] weights = { 1, 1, 4 };
  private static final int[] vals = { -1, 1, 0 };
  private static final int[] weights2 = { 1, 1 };
  private static final int[] vals2 = { -1, 1 };
  private static final double sqrt3 = Math.sqrt(3.0D);
  






  protected double rndmNum(boolean useDstrWithZero)
  {
    if (useDstrWithZero) {
      return sqrt3 * vals[weightedDistribution(weights)];
    }
    return vals2[weightedDistribution(weights2)];
  }
  






  protected int weightedDistribution(int[] weights)
  {
    int sum = 0;
    
    for (int weight : weights) {
      sum += weight;
    }
    
    int val = (int)Math.floor(m_random.nextDouble() * sum);
    
    for (int i = 0; i < weights.length; i++) {
      val -= weights[i];
      if (val < 0) {
        return i;
      }
    }
    return -1;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10832 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new RandomProjection(), argv);
  }
}
