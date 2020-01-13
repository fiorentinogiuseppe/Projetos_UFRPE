package weka.classifiers.functions.supportVector;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;







































































































































































































































































public class StringKernel
  extends Kernel
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = -4902954211202690123L;
  private int m_cacheSize = 250007;
  

  private int m_internalCacheSize = 200003;
  

  private int m_strAttr;
  

  private double[] m_storage;
  

  private long[] m_keys;
  

  private int m_kernelEvals;
  
  private int m_numInsts;
  
  public static final int PRUNING_NONE = 0;
  
  public static final int PRUNING_LAMBDA = 1;
  
  public static final Tag[] TAGS_PRUNING = { new Tag(0, "No pruning"), new Tag(1, "Lambda pruning") };
  




  protected int m_PruningMethod = 0;
  


  protected double m_lambda = 0.5D;
  

  private int m_subsequenceLength = 3;
  

  private int m_maxSubsequenceLength = 9;
  


  protected static final int MAX_POWER_OF_LAMBDA = 10000;
  

  protected double[] m_powersOflambda = null;
  



  private boolean m_normalize = false;
  
  private int maxCache;
  
  private double[] cachekh;
  
  private int[] cachekhK;
  
  private double[] cachekh2;
  private int[] cachekh2K;
  private int m_multX;
  private int m_multY;
  private int m_multZ;
  private int m_multZZ;
  private boolean m_useRecursionCache = true;
  









  public StringKernel() {}
  








  public StringKernel(Instances data, int cacheSize, int subsequenceLength, double lambda, boolean debug)
    throws Exception
  {
    setDebug(debug);
    setCacheSize(cacheSize);
    setInternalCacheSize(200003);
    setSubsequenceLength(subsequenceLength);
    setMaxSubsequenceLength(-1);
    setLambda(lambda);
    
    buildKernel(data);
  }
  





  public String globalInfo()
  {
    return "Implementation of the subsequence kernel (SSK) as described in [1] and of the subsequence kernel with lambda pruning (SSK-LP) as described in [2].\n\nFor more information, see\n\n" + getTechnicalInformation().toString();
  }
  














  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Huma Lodhi and Craig Saunders and John Shawe-Taylor and Nello Cristianini and Christopher J. C. H. Watkins");
    result.setValue(TechnicalInformation.Field.YEAR, "2002");
    result.setValue(TechnicalInformation.Field.TITLE, "Text Classification using String Kernels");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Journal of Machine Learning Research");
    result.setValue(TechnicalInformation.Field.VOLUME, "2");
    result.setValue(TechnicalInformation.Field.PAGES, "419-444");
    result.setValue(TechnicalInformation.Field.HTTP, "http://www.jmlr.org/papers/v2/lodhi02a.html");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.TECHREPORT);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "F. Kleedorfer and A. Seewald");
    additional.setValue(TechnicalInformation.Field.YEAR, "2005");
    additional.setValue(TechnicalInformation.Field.TITLE, "Implementation of a String Kernel for WEKA");
    additional.setValue(TechnicalInformation.Field.INSTITUTION, "Oesterreichisches Forschungsinstitut fuer Artificial Intelligence");
    additional.setValue(TechnicalInformation.Field.ADDRESS, "Wien, Austria");
    additional.setValue(TechnicalInformation.Field.NUMBER, "TR-2005-13");
    
    return result;
  }
  











  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    String desc = "";
    String param = "";
    for (int i = 0; i < TAGS_PRUNING.length; i++) {
      if (i > 0)
        param = param + "|";
      SelectedTag tag = new SelectedTag(TAGS_PRUNING[i].getID(), TAGS_PRUNING);
      param = param + "" + tag.getSelectedTag().getID();
      desc = desc + "\t" + tag.getSelectedTag().getID() + " = " + tag.getSelectedTag().getReadable() + "\n";
    }
    


    result.addElement(new Option("\tThe pruning method to use:\n" + desc + "\t(default: " + 0 + ")", "P", 1, "-P <" + param + ">"));
    




    result.addElement(new Option("\tThe size of the cache (a prime number).\n\t(default: 250007)", "C", 1, "-C <num>"));
    



    result.addElement(new Option("\tThe size of the internal cache (a prime number).\n\t(default: 200003)", "IC", 1, "-IC <num>"));
    



    result.addElement(new Option("\tThe lambda constant. Penalizes non-continuous subsequence\n\tmatches. Must be in (0,1).\n\t(default: 0.5)", "L", 1, "-L <num>"));
    




    result.addElement(new Option("\tThe length of the subsequence.\n\t(default: 3)", "ssl", 1, "-ssl <num>"));
    



    result.addElement(new Option("\tThe maximum length of the subsequence.\n\t(default: 9)", "ssl-max", 1, "-ssl-max <num>"));
    



    result.addElement(new Option("\tUse normalization.\n\t(default: no)", "N", 0, "-N"));
    



    return result.elements();
  }
  


















































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('P', options);
    if (tmpStr.length() != 0) {
      setPruningMethod(new SelectedTag(Integer.parseInt(tmpStr), TAGS_PRUNING));
    }
    else {
      setPruningMethod(new SelectedTag(0, TAGS_PRUNING));
    }
    
    tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      setCacheSize(Integer.parseInt(tmpStr));
    } else {
      setCacheSize(250007);
    }
    tmpStr = Utils.getOption("IC", options);
    if (tmpStr.length() != 0) {
      setInternalCacheSize(Integer.parseInt(tmpStr));
    } else {
      setInternalCacheSize(200003);
    }
    tmpStr = Utils.getOption('L', options);
    if (tmpStr.length() != 0) {
      setLambda(Double.parseDouble(tmpStr));
    } else {
      setLambda(0.5D);
    }
    tmpStr = Utils.getOption("ssl", options);
    if (tmpStr.length() != 0) {
      setSubsequenceLength(Integer.parseInt(tmpStr));
    } else {
      setSubsequenceLength(3);
    }
    tmpStr = Utils.getOption("ssl-max", options);
    if (tmpStr.length() != 0) {
      setMaxSubsequenceLength(Integer.parseInt(tmpStr));
    } else {
      setMaxSubsequenceLength(9);
    }
    setUseNormalization(Utils.getFlag('N', options));
    
    if (getMaxSubsequenceLength() < 2 * getSubsequenceLength()) {
      throw new IllegalArgumentException("Lambda Pruning forbids even contiguous substring matches! Use a bigger value for ssl-max (at least 2*ssl).");
    }
    

    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-P");
    result.add("" + m_PruningMethod);
    
    result.add("-C");
    result.add("" + getCacheSize());
    
    result.add("-IC");
    result.add("" + getInternalCacheSize());
    
    result.add("-L");
    result.add("" + getLambda());
    
    result.add("-ssl");
    result.add("" + getSubsequenceLength());
    
    result.add("-ssl-max");
    result.add("" + getMaxSubsequenceLength());
    
    if (getUseNormalization()) {
      result.add("-L");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String pruningMethodTipText()
  {
    return "The pruning method.";
  }
  




  public void setPruningMethod(SelectedTag value)
  {
    if (value.getTags() == TAGS_PRUNING) {
      m_PruningMethod = value.getSelectedTag().getID();
    }
  }
  



  public SelectedTag getPruningMethod()
  {
    return new SelectedTag(m_PruningMethod, TAGS_PRUNING);
  }
  





  public void setCacheSize(int value)
  {
    if (value >= 0) {
      m_cacheSize = value;
      clean();
    }
    else {
      System.out.println("Cache size cannot be smaller than 0 (provided: " + value + ")!");
    }
  }
  





  public int getCacheSize()
  {
    return m_cacheSize;
  }
  





  public String cacheSizeTipText()
  {
    return "The size of the cache (a prime number).";
  }
  






  public void setInternalCacheSize(int value)
  {
    if (value >= 0) {
      m_internalCacheSize = value;
      clean();
    } else {
      System.out.println("Cache size cannot be smaller than 0 (provided: " + value + ")!");
    }
  }
  





  public int getInternalCacheSize()
  {
    return m_internalCacheSize;
  }
  





  public String internalCacheSizeTipText()
  {
    return "The size of the internal cache (a prime number).";
  }
  




  public void setSubsequenceLength(int value)
  {
    m_subsequenceLength = value;
  }
  




  public int getSubsequenceLength()
  {
    return m_subsequenceLength;
  }
  





  public String subsequenceLengthTipText()
  {
    return "The subsequence length.";
  }
  




  public void setMaxSubsequenceLength(int value)
  {
    m_maxSubsequenceLength = value;
  }
  




  public int getMaxSubsequenceLength()
  {
    return m_maxSubsequenceLength;
  }
  





  public String maxSubsequenceLengthTipText()
  {
    return "The maximum subsequence length (theta in the paper)";
  }
  




  public void setLambda(double value)
  {
    m_lambda = value;
  }
  




  public double getLambda()
  {
    return m_lambda;
  }
  





  public String lambdaTipText()
  {
    return "Penalizes non-continuous subsequence matches, from (0,1)";
  }
  





  public void setUseNormalization(boolean value)
  {
    if (value != m_normalize) {
      clean();
    }
    m_normalize = value;
  }
  




  public boolean getUseNormalization()
  {
    return m_normalize;
  }
  





  public String useNormalizationTipText()
  {
    return "Whether to use normalization.";
  }
  








  public double eval(int id1, int id2, Instance inst1)
    throws Exception
  {
    if ((m_Debug) && (id1 > -1) && (id2 > -1)) {
      System.err.println("\nEvaluation of string kernel for");
      System.err.println(m_data.instance(id1).stringValue(m_strAttr));
      System.err.println("and");
      System.err.println(m_data.instance(id2).stringValue(m_strAttr));
    }
    


    if ((id1 == id2) && (m_normalize)) {
      return 1.0D;
    }
    double result = 0.0D;
    long key = -1L;
    int location = -1;
    

    if ((id1 >= 0) && (m_keys != null)) {
      if (id1 > id2) {
        key = id1 * m_numInsts + id2;
      } else {
        key = id2 * m_numInsts + id1;
      }
      if (key < 0L) {
        throw new Exception("Cache overflow detected!");
      }
      location = (int)(key % m_keys.length);
      if (m_keys[location] == key + 1L) {
        if (m_Debug)
          System.err.println("result (cached): " + m_storage[location]);
        return m_storage[location];
      }
    }
    
    m_kernelEvals += 1;
    long start = System.currentTimeMillis();
    
    Instance inst2 = m_data.instance(id2);
    char[] s1 = inst1.stringValue(m_strAttr).toCharArray();
    char[] s2 = inst2.stringValue(m_strAttr).toCharArray();
    

    if ((s1.length == 0) || (s2.length == 0)) { return 0.0D;
    }
    if (m_normalize) {
      result = normalizedKernel(s1, s2);
    } else {
      result = unnormalizedKernel(s1, s2);
    }
    
    if (m_Debug) {
      long duration = System.currentTimeMillis() - start;
      System.err.println("result: " + result);
      System.err.println("evaluation time:" + duration + "\n");
    }
    

    if (key != -1L) {
      m_storage[location] = result;
      m_keys[location] = (key + 1L);
    }
    return result;
  }
  





  public void clean()
  {
    m_storage = null;
    m_keys = null;
  }
  




  public int numEvals()
  {
    return m_kernelEvals;
  }
  






  public int numCacheHits()
  {
    return -1;
  }
  







  public double normalizedKernel(char[] s, char[] t)
  {
    double k1 = unnormalizedKernel(s, s);
    double k2 = unnormalizedKernel(t, t);
    double normTerm = Math.sqrt(k1 * k2);
    return unnormalizedKernel(s, t) / normTerm;
  }
  







  public double unnormalizedKernel(char[] s, char[] t)
  {
    if (t.length > s.length)
    {

      char[] buf = s;
      s = t;
      t = buf;
    }
    if (m_PruningMethod == 0) {
      m_multX = ((s.length + 1) * (t.length + 1));
      m_multY = (t.length + 1);
      m_multZ = 1;
      maxCache = m_internalCacheSize;
      if (maxCache == 0) {
        maxCache = ((m_subsequenceLength + 1) * m_multX);
      }
      else if ((m_subsequenceLength + 1) * m_multX < maxCache) {
        maxCache = ((m_subsequenceLength + 1) * m_multX);
      }
      m_useRecursionCache = true;
      cachekhK = new int[maxCache];
      cachekh2K = new int[maxCache];
      cachekh = new double[maxCache];
      cachekh2 = new double[maxCache];
    } else if (m_PruningMethod == 1) {
      maxCache = 0;
      m_useRecursionCache = false;
    }
    double res;
    double res;
    if (m_PruningMethod == 1) {
      res = kernelLP(m_subsequenceLength, s, s.length - 1, t, t.length - 1, m_maxSubsequenceLength);
    }
    else
    {
      res = kernel(m_subsequenceLength, s, s.length - 1, t, t.length - 1);
    }
    
    cachekh = null;
    cachekhK = null;
    cachekh2 = null;
    cachekh2K = null;
    
    return res;
  }
  






  protected double getReturnValue(int n)
  {
    if (n == 0) return 1.0D; return 0.0D;
  }
  

















  protected double kernel(int n, char[] s, int endIndexS, char[] t, int endIndexT)
  {
    if (Math.min(endIndexS + 1, endIndexT + 1) < n) { return getReturnValue(n);
    }
    
    double result = 0.0D;
    



    for (int iS = endIndexS; iS > n - 2; iS--) {
      double buf = 0.0D;
      
      char x = s[iS];
      
      for (int j = 0; j <= endIndexT; j++) {
        if (t[j] == x)
        {



          buf += kernelHelper(n - 1, s, iS - 1, t, j - 1);
        }
      }
      


      result += buf * m_powersOflambda[2];
    }
    return result;
  }
  













  protected double kernelHelper(int n, char[] s, int endIndexS, char[] t, int endIndexT)
  {
    if (n <= 0) {
      return getReturnValue(n);
    }
    





    if (Math.min(endIndexS + 1, endIndexT + 1) < n) {
      return getReturnValue(n);
    }
    int adr = 0;
    if (m_useRecursionCache) {
      adr = m_multX * n + m_multY * endIndexS + m_multZ * endIndexT;
      if (cachekhK[(adr % maxCache)] == adr + 1) { return cachekh[(adr % maxCache)];
      }
    }
    






    double result = 0.0D;
    










    result = m_lambda * kernelHelper(n, s, endIndexS - 1, t, endIndexT) + kernelHelper2(n, s, endIndexS, t, endIndexT);
    
    if (m_useRecursionCache) {
      cachekhK[(adr % maxCache)] = (adr + 1);cachekh[(adr % maxCache)] = result;
    }
    return result;
  }
  













  protected double kernelHelper2(int n, char[] s, int endIndexS, char[] t, int endIndexT)
  {
    if ((endIndexS < 0) || (endIndexT < 0)) {
      return getReturnValue(n);
    }
    
    int adr = 0;
    if (m_useRecursionCache) {
      adr = m_multX * n + m_multY * endIndexS + m_multZ * endIndexT;
      if (cachekh2K[(adr % maxCache)] == adr + 1) { return cachekh2[(adr % maxCache)];
      }
    }
    
    char x = s[endIndexS];
    












    if (x == t[endIndexT]) {
      double ret = m_lambda * (kernelHelper2(n, s, endIndexS, t, endIndexT - 1) + m_lambda * kernelHelper(n - 1, s, endIndexS - 1, t, endIndexT - 1));
      
      if (m_useRecursionCache) {
        cachekh2K[(adr % maxCache)] = (adr + 1);cachekh2[(adr % maxCache)] = ret;
      }
      return ret;
    }
    double ret = m_lambda * kernelHelper2(n, s, endIndexS, t, endIndexT - 1);
    if (m_useRecursionCache) {
      cachekh2K[(adr % maxCache)] = (adr + 1);cachekh2[(adr % maxCache)] = ret;
    }
    return ret;
  }
  










































  protected double kernelLP(int n, char[] s, int endIndexS, char[] t, int endIndexT, int remainingMatchLength)
  {
    if (Math.min(endIndexS + 1, endIndexT + 1) < n) {
      return getReturnValue(n);
    }
    




    if (remainingMatchLength == 0) return getReturnValue(n);
    double result = 0.0D;
    
    for (int iS = endIndexS; iS > n - 2; iS--) {
      double buf = 0.0D;
      char x = s[iS];
      for (int j = 0; j <= endIndexT; j++) {
        if (t[j] == x)
        {

          buf += kernelHelperLP(n - 1, s, iS - 1, t, j - 1, remainingMatchLength - 2);
        }
      }
      result += buf * m_powersOflambda[2];
    }
    return result;
  }
  















  protected double kernelHelperLP(int n, char[] s, int endIndexS, char[] t, int endIndexT, int remainingMatchLength)
  {
    if (n == 0) {
      return getReturnValue(n);
    }
    

    if (Math.min(endIndexS + 1, endIndexT + 1) < n) {
      return getReturnValue(n);
    }
    





    if (remainingMatchLength < 2 * n) {
      return getReturnValue(n);
    }
    int adr = 0;
    if (m_useRecursionCache) {
      adr = m_multX * n + m_multY * endIndexS + m_multZ * endIndexT + m_multZZ * remainingMatchLength;
      
      if (cachekh2K[(adr % maxCache)] == adr + 1) {
        return cachekh2[(adr % maxCache)];
      }
    }
    
    int rml = 0;
    double result = 0.0D;
    





    for (int iS = endIndexS - remainingMatchLength; iS <= endIndexS; iS++) {
      result *= m_lambda;
      result += kernelHelper2LP(n, s, iS, t, endIndexT, rml++);
    }
    
    if ((m_useRecursionCache) && (endIndexS >= 0) && (endIndexT >= 0) && (n >= 0)) {
      cachekhK[(adr % maxCache)] = (adr + 1);cachekh[(adr % maxCache)] = result;
    }
    return result;
  }
  





















  protected double kernelHelper2LP(int n, char[] s, int endIndexS, char[] t, int endIndexT, int remainingMatchLength)
  {
    if (remainingMatchLength < 2 * n) { return getReturnValue(n);
    }
    
    if ((endIndexS < 0) || (endIndexT < 0)) return getReturnValue(n);
    int adr = 0;
    if (m_useRecursionCache) {
      adr = m_multX * n + m_multY * endIndexS + m_multZ * endIndexT + m_multZZ * remainingMatchLength;
      
      if (cachekh2K[(adr % maxCache)] == adr + 1) {
        return cachekh2[(adr % maxCache)];
      }
    }
    
    char x = s[endIndexS];
    if (x == t[endIndexT]) {
      double ret = m_lambda * (kernelHelper2LP(n, s, endIndexS, t, endIndexT - 1, remainingMatchLength - 1) + m_lambda * kernelHelperLP(n - 1, s, endIndexS - 1, t, endIndexT - 1, remainingMatchLength - 2));
      



      if ((m_useRecursionCache) && (endIndexS >= 0) && (endIndexT >= 0) && (n >= 0)) {
        cachekh2K[(adr % maxCache)] = (adr + 1);cachekh2[(adr % maxCache)] = ret; }
      return ret;
    }
    





    int minIndex = endIndexT - remainingMatchLength;
    if (minIndex < 0) minIndex = 0;
    for (int i = endIndexT; i >= minIndex; i--) {
      if (x == t[i]) {
        int skipLength = endIndexT - i;
        double ret = getPowerOfLambda(skipLength) * kernelHelper2LP(n, s, endIndexS, t, i, remainingMatchLength - skipLength);
        
        if ((m_useRecursionCache) && (endIndexS >= 0) && (endIndexT >= 0) && (n >= 0)) {
          cachekh2K[(adr % maxCache)] = (adr + 1);cachekh2[(adr % maxCache)] = ret;
        }
        return ret;
      }
    }
    double ret = getReturnValue(n);
    if ((m_useRecursionCache) && (endIndexS >= 0) && (endIndexT >= 0) && (n >= 0)) {
      cachekh2K[(adr % maxCache)] = (adr + 1);cachekh2[(adr % maxCache)] = ret;
    }
    return ret;
  }
  




  private double[] calculatePowersOfLambda()
  {
    double[] powers = new double['✑'];
    powers[0] = 1.0D;
    double val = 1.0D;
    for (int i = 1; i <= 10000; i++) {
      val *= m_lambda;
      powers[i] = val;
    }
    return powers;
  }
  






  private double getPowerOfLambda(int exponent)
  {
    if (exponent > 10000) {
      return Math.pow(m_lambda, exponent);
    }
    if (exponent < 0) {
      throw new IllegalArgumentException("only positive powers of lambda may be computed");
    }
    
    return m_powersOflambda[exponent];
  }
  




  protected void initVars(Instances data)
  {
    super.initVars(data);
    
    m_kernelEvals = 0;
    
    m_strAttr = -1;
    for (int i = 0; i < data.numAttributes(); i++) {
      if (i != data.classIndex())
      {
        if (data.attribute(i).type() == 2) {
          m_strAttr = i;
          break;
        } }
    }
    m_numInsts = m_data.numInstances();
    m_storage = new double[m_cacheSize];
    m_keys = new long[m_cacheSize];
    m_powersOflambda = calculatePowersOfLambda();
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    
    result.enable(Capabilities.Capability.STRING_ATTRIBUTES);
    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  





  public void buildKernel(Instances data)
    throws Exception
  {
    super.buildKernel(data);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5518 $");
  }
}
