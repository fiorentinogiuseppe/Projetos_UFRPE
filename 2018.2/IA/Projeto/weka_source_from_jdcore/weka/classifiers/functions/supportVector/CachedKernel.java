package weka.classifiers.functions.supportVector;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;










































public abstract class CachedKernel
  extends Kernel
{
  private static final long serialVersionUID = 702810182699015136L;
  protected int m_kernelEvals;
  protected int m_cacheHits;
  protected int m_cacheSize = 250007;
  

  protected double[] m_storage;
  

  protected long[] m_keys;
  

  protected double[][] m_kernelMatrix;
  
  protected int m_numInsts;
  
  protected int m_cacheSlots = 4;
  







  public CachedKernel() {}
  






  protected CachedKernel(Instances data, int cacheSize)
    throws Exception
  {
    setCacheSize(cacheSize);
    
    buildKernel(data);
  }
  







  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tThe size of the cache (a prime number), 0 for full cache and \n\t-1 to turn it off.\n\t(default: 250007)", "C", 1, "-C <num>"));
    




    return result.elements();
  }
  






  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      setCacheSize(Integer.parseInt(tmpStr));
    } else {
      setCacheSize(250007);
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
    result.add("-C");
    result.add("" + getCacheSize());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  









  protected abstract double evaluate(int paramInt1, int paramInt2, Instance paramInstance)
    throws Exception;
  









  public double eval(int id1, int id2, Instance inst1)
    throws Exception
  {
    double result = 0.0D;
    long key = -1L;
    int location = -1;
    


    if ((id1 >= 0) && (m_cacheSize != -1))
    {

      if (m_cacheSize == 0) {
        if (m_kernelMatrix == null) {
          m_kernelMatrix = new double[m_data.numInstances()][];
          for (int i = 0; i < m_data.numInstances(); i++) {
            m_kernelMatrix[i] = new double[i + 1];
            for (int j = 0; j <= i; j++) {
              m_kernelEvals += 1;
              m_kernelMatrix[i][j] = evaluate(i, j, m_data.instance(i));
            }
          }
        }
        m_cacheHits += 1;
        result = id1 > id2 ? m_kernelMatrix[id1][id2] : m_kernelMatrix[id2][id1];
        return result;
      }
      

      if (id1 > id2) {
        key = id1 + id2 * m_numInsts;
      } else {
        key = id2 + id1 * m_numInsts;
      }
      location = (int)(key % m_cacheSize) * m_cacheSlots;
      int loc = location;
      for (int i = 0; i < m_cacheSlots; i++) {
        long thiskey = m_keys[loc];
        if (thiskey == 0L)
          break;
        if (thiskey == key + 1L) {
          m_cacheHits += 1;
          

          if (i > 0) {
            double tmps = m_storage[loc];
            m_storage[loc] = m_storage[location];
            m_keys[loc] = m_keys[location];
            m_storage[location] = tmps;
            m_keys[location] = thiskey;
            return tmps;
          }
          return m_storage[loc];
        }
        loc++;
      }
    }
    
    result = evaluate(id1, id2, inst1);
    
    m_kernelEvals += 1;
    

    if ((key != -1L) && (m_cacheSize != -1))
    {

      System.arraycopy(m_keys, location, m_keys, location + 1, m_cacheSlots - 1);
      
      System.arraycopy(m_storage, location, m_storage, location + 1, m_cacheSlots - 1);
      
      m_storage[location] = result;
      m_keys[location] = (key + 1L);
    }
    return result;
  }
  




  public int numEvals()
  {
    return m_kernelEvals;
  }
  




  public int numCacheHits()
  {
    return m_cacheHits;
  }
  


  public void clean()
  {
    m_storage = null;
    m_keys = null;
    m_kernelMatrix = ((double[][])null);
  }
  








  protected final double dotProd(Instance inst1, Instance inst2)
    throws Exception
  {
    double result = 0.0D;
    

    int n1 = inst1.numValues();
    int n2 = inst2.numValues();
    int classIndex = m_data.classIndex();
    int p1 = 0; for (int p2 = 0; (p1 < n1) && (p2 < n2);) {
      int ind1 = inst1.index(p1);
      int ind2 = inst2.index(p2);
      if (ind1 == ind2) {
        if (ind1 != classIndex) {
          result += inst1.valueSparse(p1) * inst2.valueSparse(p2);
        }
        p1++;
        p2++;
      } else if (ind1 > ind2) {
        p2++;
      } else {
        p1++;
      }
    }
    return result;
  }
  




  public void setCacheSize(int value)
  {
    if (value >= -1) {
      m_cacheSize = value;
      clean();
    }
    else {
      System.out.println("Cache size cannot be smaller than -1 (provided: " + value + ")!");
    }
  }
  





  public int getCacheSize()
  {
    return m_cacheSize;
  }
  





  public String cacheSizeTipText()
  {
    return "The size of the cache (a prime number), 0 for full cache and -1 to turn it off.";
  }
  




  protected void initVars(Instances data)
  {
    super.initVars(data);
    
    m_kernelEvals = 0;
    m_cacheHits = 0;
    m_numInsts = m_data.numInstances();
    
    if (getCacheSize() > 0)
    {
      m_storage = new double[m_cacheSize * m_cacheSlots];
      m_keys = new long[m_cacheSize * m_cacheSlots];
    }
    else {
      m_storage = null;
      m_keys = null;
      m_kernelMatrix = ((double[][])null);
    }
  }
  






  public void buildKernel(Instances data)
    throws Exception
  {
    if (!getChecksTurnedOff()) {
      getCapabilities().testWithFail(data);
    }
    initVars(data);
  }
}
