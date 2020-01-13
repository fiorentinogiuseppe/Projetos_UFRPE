package weka.classifiers.functions;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
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
import weka.filters.supervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.unsupervised.instance.RemoveRange;











































































public class LeastMedSq
  extends Classifier
  implements OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 4288954049987652970L;
  private double[] m_Residuals;
  private double[] m_weight;
  private double m_SSR;
  private double m_scalefactor;
  private double m_bestMedian = Double.POSITIVE_INFINITY;
  
  private LinearRegression m_currentRegression;
  
  private LinearRegression m_bestRegression;
  
  private LinearRegression m_ls;
  
  private Instances m_Data;
  
  private Instances m_RLSData;
  
  private Instances m_SubSample;
  
  private ReplaceMissingValues m_MissingFilter;
  
  private NominalToBinary m_TransformFilter;
  
  private RemoveRange m_SplitFilter;
  
  private int m_samplesize = 4;
  
  private int m_samples;
  
  private boolean m_israndom = false;
  
  private boolean m_debug = false;
  
  private Random m_random;
  
  private long m_randomseed = 0L;
  

  public LeastMedSq() {}
  

  public String globalInfo()
  {
    return "Implements a least median sqaured linear regression utilising the existing weka LinearRegression class to form predictions. \nLeast squared regression functions are generated from random subsamples of the data. The least squared regression with the lowest meadian squared error is chosen as the final model.\n\nThe basis of the algorithm is \n\n" + getTechnicalInformation().toString();
  }
  














  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.BOOK);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Peter J. Rousseeuw and Annick M. Leroy");
    result.setValue(TechnicalInformation.Field.YEAR, "1987");
    result.setValue(TechnicalInformation.Field.TITLE, "Robust regression and outlier detection");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  






  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    cleanUpData(data);
    
    getSamples();
    
    findBestRegression();
    
    buildRLSRegression();
  }
  








  public double classifyInstance(Instance instance)
    throws Exception
  {
    Instance transformedInstance = instance;
    m_TransformFilter.input(transformedInstance);
    transformedInstance = m_TransformFilter.output();
    m_MissingFilter.input(transformedInstance);
    transformedInstance = m_MissingFilter.output();
    
    return m_ls.classifyInstance(transformedInstance);
  }
  





  private void cleanUpData(Instances data)
    throws Exception
  {
    m_Data = data;
    m_TransformFilter = new NominalToBinary();
    m_TransformFilter.setInputFormat(m_Data);
    m_Data = Filter.useFilter(m_Data, m_TransformFilter);
    m_MissingFilter = new ReplaceMissingValues();
    m_MissingFilter.setInputFormat(m_Data);
    m_Data = Filter.useFilter(m_Data, m_MissingFilter);
    m_Data.deleteWithMissingClass();
  }
  




  private void getSamples()
    throws Exception
  {
    int[] stuf = { 500, 50, 22, 17, 15, 14 };
    if (m_samplesize < 7) {
      if (m_Data.numInstances() < stuf[(m_samplesize - 1)]) {
        m_samples = combinations(m_Data.numInstances(), m_samplesize);
      } else
        m_samples = (m_samplesize * 500);
    } else
      m_samples = 3000;
    if (m_debug) {
      System.out.println("m_samplesize: " + m_samplesize);
      System.out.println("m_samples: " + m_samples);
      System.out.println("m_randomseed: " + m_randomseed);
    }
  }
  





  private void setRandom()
  {
    m_random = new Random(getRandomSeed());
  }
  





  private void findBestRegression()
    throws Exception
  {
    setRandom();
    m_bestMedian = Double.POSITIVE_INFINITY;
    if (m_debug) {
      System.out.println("Starting:");
    }
    int s = 0; for (int r = 0; s < m_samples; r++) {
      if ((m_debug) && 
        (s % (m_samples / 100) == 0)) {
        System.out.print("*");
      }
      genRegression();
      getMedian();s++;
    }
    
    if (m_debug) {
      System.out.println("");
    }
    m_currentRegression = m_bestRegression;
  }
  





  private void genRegression()
    throws Exception
  {
    m_currentRegression = new LinearRegression();
    m_currentRegression.setOptions(new String[] { "-S", "1" });
    selectSubSample(m_Data);
    m_currentRegression.buildClassifier(m_SubSample);
  }
  





  private void findResiduals()
    throws Exception
  {
    m_SSR = 0.0D;
    m_Residuals = new double[m_Data.numInstances()];
    for (int i = 0; i < m_Data.numInstances(); i++) {
      m_Residuals[i] = m_currentRegression.classifyInstance(m_Data.instance(i));
      m_Residuals[i] -= m_Data.instance(i).value(m_Data.classAttribute());
      m_Residuals[i] *= m_Residuals[i];
      m_SSR += m_Residuals[i];
    }
  }
  





  private void getMedian()
    throws Exception
  {
    findResiduals();
    int p = m_Residuals.length;
    select(m_Residuals, 0, p - 1, p / 2);
    if (m_Residuals[(p / 2)] < m_bestMedian) {
      m_bestMedian = m_Residuals[(p / 2)];
      m_bestRegression = m_currentRegression;
    }
  }
  






  public String toString()
  {
    if (m_ls == null) {
      return "model has not been built";
    }
    return m_ls.toString();
  }
  





  private void buildWeight()
    throws Exception
  {
    findResiduals();
    m_scalefactor = (1.4826D * (1 + 5 / (m_Data.numInstances() - m_Data.numAttributes())) * Math.sqrt(m_bestMedian));
    

    m_weight = new double[m_Residuals.length];
    for (int i = 0; i < m_Residuals.length; i++) {
      m_weight[i] = (Math.sqrt(m_Residuals[i]) / m_scalefactor < 2.5D ? 1.0D : 0.0D);
    }
  }
  




  private void buildRLSRegression()
    throws Exception
  {
    buildWeight();
    m_RLSData = new Instances(m_Data);
    int x = 0;
    int y = 0;
    int n = m_RLSData.numInstances();
    while (y < n) {
      if (m_weight[x] == 0.0D) {
        m_RLSData.delete(y);
        n = m_RLSData.numInstances();
        y--;
      }
      x++;
      y++;
    }
    if (m_RLSData.numInstances() == 0) {
      System.err.println("rls regression unbuilt");
      m_ls = m_currentRegression;
    } else {
      m_ls = new LinearRegression();
      m_ls.setOptions(new String[] { "-S", "1" });
      m_ls.buildClassifier(m_RLSData);
      m_currentRegression = m_ls;
    }
  }
  









  private static void select(double[] a, int l, int r, int k)
  {
    if (r <= l) return;
    int i = partition(a, l, r);
    if (i > k) select(a, l, i - 1, k);
    if (i < k) { select(a, i + 1, r, k);
    }
  }
  










  private static int partition(double[] a, int l, int r)
  {
    int i = l - 1;int j = r;
    double v = a[r];
    for (;;) {
      if (a[(++i)] >= v) {
        while (v < a[(--j)]) if (j == l) break;
        if (i >= j) break;
        double temp = a[i];
        a[i] = a[j];
        a[j] = temp;
      } }
    double temp = a[i];
    a[i] = a[r];
    a[r] = temp;
    return i;
  }
  






  private void selectSubSample(Instances data)
    throws Exception
  {
    m_SplitFilter = new RemoveRange();
    m_SplitFilter.setInvertSelection(true);
    m_SubSample = data;
    m_SplitFilter.setInputFormat(m_SubSample);
    m_SplitFilter.setInstancesIndices(selectIndices(m_SubSample));
    m_SubSample = Filter.useFilter(m_SubSample, m_SplitFilter);
  }
  







  private String selectIndices(Instances data)
  {
    StringBuffer text = new StringBuffer();
    int i = 0; for (int x = 0; i < m_samplesize; i++) {
      do { x = (int)(m_random.nextDouble() * data.numInstances());
      } while (x == 0);
      text.append(Integer.toString(x));
      if (i < m_samplesize - 1) {
        text.append(",");
      } else
        text.append("\n");
    }
    return text.toString();
  }
  




  public String sampleSizeTipText()
  {
    return "Set the size of the random samples used to generate the least sqaured regression functions.";
  }
  






  public void setSampleSize(int samplesize)
  {
    m_samplesize = samplesize;
  }
  





  public int getSampleSize()
  {
    return m_samplesize;
  }
  




  public String randomSeedTipText()
  {
    return "Set the seed for selecting random subsamples of the training data.";
  }
  





  public void setRandomSeed(long randomseed)
  {
    m_randomseed = randomseed;
  }
  





  public long getRandomSeed()
  {
    return m_randomseed;
  }
  





  public void setDebug(boolean debug)
  {
    m_debug = debug;
  }
  





  public boolean getDebug()
  {
    return m_debug;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    newVector.addElement(new Option("\tSet sample size\n\t(default: 4)\n", "S", 4, "-S <sample size>"));
    

    newVector.addElement(new Option("\tSet the seed used to generate samples\n\t(default: 0)\n", "G", 0, "-G <seed>"));
    

    newVector.addElement(new Option("\tProduce debugging output\n\t(default no debugging output)\n", "D", 0, "-D"));
    


    return newVector.elements();
  }
  



























  public void setOptions(String[] options)
    throws Exception
  {
    String curropt = Utils.getOption('S', options);
    if (curropt.length() != 0) {
      setSampleSize(Integer.parseInt(curropt));
    } else {
      setSampleSize(4);
    }
    curropt = Utils.getOption('G', options);
    if (curropt.length() != 0) {
      setRandomSeed(Long.parseLong(curropt));
    } else {
      setRandomSeed(0L);
    }
    
    setDebug(Utils.getFlag('D', options));
  }
  





  public String[] getOptions()
  {
    String[] options = new String[9];
    int current = 0;
    
    options[(current++)] = "-S";
    options[(current++)] = ("" + getSampleSize());
    
    options[(current++)] = "-G";
    options[(current++)] = ("" + getRandomSeed());
    
    if (getDebug()) {
      options[(current++)] = "-D";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  







  public static int combinations(int n, int r)
    throws Exception
  {
    int c = 1;int denom = 1;int num = 1;int orig = r;
    if (r > n) throw new Exception("r must be less that or equal to n.");
    r = Math.min(r, n - r);
    
    for (int i = 1; i <= r; i++)
    {
      num *= (n - i + 1);
      denom *= i;
    }
    
    c = num / denom;
    


    return c;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5523 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new LeastMedSq(), argv);
  }
}
