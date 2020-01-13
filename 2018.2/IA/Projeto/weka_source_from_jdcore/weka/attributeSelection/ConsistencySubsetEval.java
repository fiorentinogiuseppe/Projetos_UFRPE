package weka.attributeSelection;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;



























































































public class ConsistencySubsetEval
  extends ASEvaluation
  implements SubsetEvaluator, TechnicalInformationHandler
{
  static final long serialVersionUID = -2880323763295270402L;
  private Instances m_trainInstances;
  private int m_classIndex;
  private int m_numAttribs;
  private int m_numInstances;
  private Discretize m_disTransform;
  private Hashtable m_table;
  
  public class hashKey
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = 6144138512017017408L;
    private final double[] attributes;
    private final boolean[] missing;
    private int key;
    
    public hashKey(Instance t, int numAtts)
      throws Exception
    {
      int cindex = t.classIndex();
      
      key = 64537;
      attributes = new double[numAtts];
      missing = new boolean[numAtts];
      for (int i = 0; i < numAtts; i++) {
        if (i == cindex) {
          missing[i] = true;
        }
        else if (!(missing[i] = t.isMissing(i))) {
          attributes[i] = t.value(i);
        }
      }
    }
    









    public String toString(Instances t, int maxColWidth)
    {
      int cindex = t.classIndex();
      StringBuffer text = new StringBuffer();
      
      for (int i = 0; i < attributes.length; i++) {
        if (i != cindex) {
          if (missing[i] != 0) {
            text.append("?");
            for (int j = 0; j < maxColWidth; j++) {
              text.append(" ");
            }
          } else {
            String ss = t.attribute(i).value((int)attributes[i]);
            StringBuffer sb = new StringBuffer(ss);
            
            for (int j = 0; j < maxColWidth - ss.length() + 1; j++) {
              sb.append(" ");
            }
            text.append(sb);
          }
        }
      }
      return text.toString();
    }
    






    public hashKey(double[] t)
    {
      int l = t.length;
      
      key = 64537;
      attributes = new double[l];
      missing = new boolean[l];
      for (int i = 0; i < l; i++) {
        if (t[i] == Double.MAX_VALUE) {
          missing[i] = true;
        } else {
          missing[i] = false;
          attributes[i] = t[i];
        }
      }
    }
    






    public int hashCode()
    {
      int hv = 0;
      
      if (key != 64537) {
        return key;
      }
      for (int i = 0; i < attributes.length; i++) {
        if (missing[i] != 0) {
          hv += i * 13;
        } else {
          hv = (int)(hv + i * 5 * (attributes[i] + 1.0D));
        }
      }
      if (key == 64537) {
        key = hv;
      }
      return hv;
    }
    







    public boolean equals(Object b)
    {
      if ((b == null) || (!b.getClass().equals(getClass()))) {
        return false;
      }
      boolean ok = true;
      
      if ((b instanceof hashKey)) {
        hashKey n = (hashKey)b;
        for (int i = 0; i < attributes.length; i++) {
          boolean l = missing[i];
          if ((missing[i] != 0) || (l)) {
            if (((missing[i] != 0) && (!l)) || ((missing[i] == 0) && (l))) {
              ok = false;
              break;
            }
          }
          else if (attributes[i] != attributes[i]) {
            ok = false;
            break;
          }
        }
      }
      else {
        return false;
      }
      return ok;
    }
    



    public void print_hash_code()
    {
      System.out.println("Hash val: " + hashCode());
    }
    





    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 11851 $");
    }
  }
  





  public String globalInfo()
  {
    return "ConsistencySubsetEval :\n\nEvaluates the worth of a subset of attributes by the level of consistency in the class values when the training instances are projected onto the subset of attributes. \n\nConsistency of any subset can never be lower than that of the full set of attributes, hence the usual practice is to use this subset evaluator in conjunction with a Random or Exhaustive search which looks for the smallest subset with consistency equal to that of the full set of attributes.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  


















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "H. Liu and R. Setiono");
    result.setValue(TechnicalInformation.Field.TITLE, "A probabilistic approach to feature selection - A filter solution");
    
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "13th International Conference on Machine Learning");
    
    result.setValue(TechnicalInformation.Field.YEAR, "1996");
    result.setValue(TechnicalInformation.Field.PAGES, "319-327");
    
    return result;
  }
  


  public ConsistencySubsetEval()
  {
    resetOptions();
  }
  


  private void resetOptions()
  {
    m_trainInstances = null;
  }
  






  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  








  public void buildEvaluator(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    
    m_trainInstances = new Instances(data);
    m_trainInstances.deleteWithMissingClass();
    m_classIndex = m_trainInstances.classIndex();
    m_numAttribs = m_trainInstances.numAttributes();
    m_numInstances = m_trainInstances.numInstances();
    
    m_disTransform = new Discretize();
    m_disTransform.setUseBetterEncoding(true);
    m_disTransform.setInputFormat(m_trainInstances);
    m_trainInstances = Filter.useFilter(m_trainInstances, m_disTransform);
  }
  







  public double evaluateSubset(BitSet subset)
    throws Exception
  {
    int count = 0;
    
    for (int i = 0; i < m_numAttribs; i++) {
      if (subset.get(i)) {
        count++;
      }
    }
    
    double[] instArray = new double[count];
    int index = 0;
    int[] fs = new int[count];
    for (i = 0; i < m_numAttribs; i++) {
      if (subset.get(i)) {
        fs[(index++)] = i;
      }
    }
    

    m_table = new Hashtable((int)(m_numInstances * 1.5D));
    
    for (i = 0; i < m_numInstances; i++) {
      Instance inst = m_trainInstances.instance(i);
      for (int j = 0; j < fs.length; j++) {
        if (fs[j] == m_classIndex) {
          throw new Exception("A subset should not contain the class!");
        }
        if (inst.isMissing(fs[j])) {
          instArray[j] = Double.MAX_VALUE;
        } else {
          instArray[j] = inst.value(fs[j]);
        }
      }
      insertIntoTable(inst, instArray);
    }
    
    return consistencyCount();
  }
  









  private double consistencyCount()
  {
    Enumeration e = m_table.keys();
    
    double count = 0.0D;
    
    while (e.hasMoreElements()) {
      hashKey tt = (hashKey)e.nextElement();
      double[] classDist = (double[])m_table.get(tt);
      count += Utils.sum(classDist);
      int max = Utils.maxIndex(classDist);
      count -= classDist[max];
    }
    
    count /= m_numInstances;
    return 1.0D - count;
  }
  











  private void insertIntoTable(Instance inst, double[] instA)
    throws Exception
  {
    hashKey thekey = new hashKey(instA);
    

    double[] tempClassDist2 = (double[])m_table.get(thekey);
    if (tempClassDist2 == null) {
      double[] newDist = new double[m_trainInstances.classAttribute().numValues()];
      newDist[((int)inst.classValue())] = inst.weight();
      

      m_table.put(thekey, newDist);
    }
    else {
      tempClassDist2[((int)inst.classValue())] += inst.weight();
      

      m_table.put(thekey, tempClassDist2);
    }
  }
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_trainInstances == null) {
      text.append("\tConsistency subset evaluator has not been built yet\n");
    }
    else {
      text.append("\tConsistency Subset Evaluator\n");
    }
    
    return text.toString();
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11851 $");
  }
  


  public void clean()
  {
    m_trainInstances = new Instances(m_trainInstances, 0);
  }
  




  public static void main(String[] args)
  {
    runEvaluator(new ConsistencySubsetEval(), args);
  }
}
