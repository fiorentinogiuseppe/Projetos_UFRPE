package weka.attributeSelection;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.ContingencyTables;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;

































































public class GainRatioAttributeEval
  extends ASEvaluation
  implements AttributeEvaluator, OptionHandler
{
  static final long serialVersionUID = -8504656625598579926L;
  private Instances m_trainInstances;
  private int m_classIndex;
  private int m_numAttribs;
  private int m_numInstances;
  private int m_numClasses;
  private boolean m_missing_merge;
  
  public String globalInfo()
  {
    return "GainRatioAttributeEval :\n\nEvaluates the worth of an attribute by measuring the gain ratio with respect to the class.\n\nGainR(Class, Attribute) = (H(Class) - H(Class | Attribute)) / H(Attribute).\n";
  }
  





  public GainRatioAttributeEval()
  {
    resetOptions();
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    newVector.addElement(new Option("\ttreat missing values as a seperate value.", "M", 0, "-M"));
    
    return newVector.elements();
  }
  

















  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    setMissingMerge(!Utils.getFlag('M', options));
  }
  





  public String missingMergeTipText()
  {
    return "Distribute counts for missing values. Counts are distributed across other values in proportion to their frequency. Otherwise, missing is treated as a separate value.";
  }
  






  public void setMissingMerge(boolean b)
  {
    m_missing_merge = b;
  }
  




  public boolean getMissingMerge()
  {
    return m_missing_merge;
  }
  





  public String[] getOptions()
  {
    String[] options = new String[1];
    int current = 0;
    
    if (!getMissingMerge()) {
      options[(current++)] = "-M";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
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
    
    m_trainInstances = data;
    m_classIndex = m_trainInstances.classIndex();
    m_numAttribs = m_trainInstances.numAttributes();
    m_numInstances = m_trainInstances.numInstances();
    Discretize disTransform = new Discretize();
    disTransform.setUseBetterEncoding(true);
    disTransform.setInputFormat(m_trainInstances);
    m_trainInstances = Filter.useFilter(m_trainInstances, disTransform);
    m_numClasses = m_trainInstances.attribute(m_classIndex).numValues();
  }
  


  protected void resetOptions()
  {
    m_trainInstances = null;
    m_missing_merge = true;
  }
  










  public double evaluateAttribute(int attribute)
    throws Exception
  {
    double sum = 0.0D;
    int ni = m_trainInstances.attribute(attribute).numValues() + 1;
    int nj = m_numClasses + 1;
    

    double temp = 0.0D;
    double[] sumi = new double[ni];
    double[] sumj = new double[nj];
    double[][] counts = new double[ni][nj];
    sumi = new double[ni];
    sumj = new double[nj];
    
    for (int i = 0; i < ni; i++) {
      sumi[i] = 0.0D;
      
      for (int j = 0; j < nj; j++) {
        sumj[j] = 0.0D;
        counts[i][j] = 0.0D;
      }
    }
    

    for (i = 0; i < m_numInstances; i++) {
      Instance inst = m_trainInstances.instance(i);
      int ii;
      int ii; if (inst.isMissing(attribute)) {
        ii = ni - 1;
      }
      else
        ii = (int)inst.value(attribute);
      int jj;
      int jj;
      if (inst.isMissing(m_classIndex)) {
        jj = nj - 1;
      }
      else {
        jj = (int)inst.value(m_classIndex);
      }
      
      counts[ii][jj] += inst.weight();
    }
    

    for (i = 0; i < ni; i++) {
      sumi[i] = 0.0D;
      
      for (int j = 0; j < nj; j++) {
        sumi[i] += counts[i][j];
        sum += counts[i][j];
      }
    }
    

    for (int j = 0; j < nj; j++) {
      sumj[j] = 0.0D;
      
      for (i = 0; i < ni; i++) {
        sumj[j] += counts[i][j];
      }
    }
    

    if ((m_missing_merge) && (sumi[(ni - 1)] < sum) && (sumj[(nj - 1)] < sum))
    {

      double[] i_copy = new double[sumi.length];
      double[] j_copy = new double[sumj.length];
      double[][] counts_copy = new double[sumi.length][sumj.length];
      
      for (i = 0; i < ni; i++) {
        System.arraycopy(counts[i], 0, counts_copy[i], 0, sumj.length);
      }
      
      System.arraycopy(sumi, 0, i_copy, 0, sumi.length);
      System.arraycopy(sumj, 0, j_copy, 0, sumj.length);
      double total_missing = sumi[(ni - 1)] + sumj[(nj - 1)] - counts[(ni - 1)][(nj - 1)];
      


      if (sumi[(ni - 1)] > 0.0D) {
        for (j = 0; j < nj - 1; j++) {
          if (counts[(ni - 1)][j] > 0.0D) {
            for (i = 0; i < ni - 1; i++) {
              temp = i_copy[i] / (sum - i_copy[(ni - 1)]) * counts[(ni - 1)][j];
              counts[i][j] += temp;
              sumi[i] += temp;
            }
            
            counts[(ni - 1)][j] = 0.0D;
          }
        }
      }
      
      sumi[(ni - 1)] = 0.0D;
      

      if (sumj[(nj - 1)] > 0.0D) {
        for (i = 0; i < ni - 1; i++) {
          if (counts[i][(nj - 1)] > 0.0D) {
            for (j = 0; j < nj - 1; j++) {
              temp = j_copy[j] / (sum - j_copy[(nj - 1)]) * counts[i][(nj - 1)];
              counts[i][j] += temp;
              sumj[j] += temp;
            }
            
            counts[i][(nj - 1)] = 0.0D;
          }
        }
      }
      
      sumj[(nj - 1)] = 0.0D;
      

      if ((counts[(ni - 1)][(nj - 1)] > 0.0D) && (total_missing < sum)) {
        for (i = 0; i < ni - 1; i++) {
          for (j = 0; j < nj - 1; j++) {
            temp = counts_copy[i][j] / (sum - total_missing) * counts_copy[(ni - 1)][(nj - 1)];
            
            counts[i][j] += temp;
            sumi[i] += temp;
            sumj[j] += temp;
          }
        }
        
        counts[(ni - 1)][(nj - 1)] = 0.0D;
      }
    }
    
    return ContingencyTables.gainRatio(counts);
  }
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_trainInstances == null) {
      text.append("\tGain Ratio evaluator has not been built");
    }
    else {
      text.append("\tGain Ratio feature evaluator");
      
      if (!m_missing_merge) {
        text.append("\n\tMissing values treated as seperate");
      }
    }
    
    text.append("\n");
    return text.toString();
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11219 $");
  }
  


  public int[] postProcess(int[] attributeSet)
  {
    m_trainInstances = new Instances(m_trainInstances, 0);
    
    return attributeSet;
  }
  




  public static void main(String[] args)
  {
    runEvaluator(new GainRatioAttributeEval(), args);
  }
}
