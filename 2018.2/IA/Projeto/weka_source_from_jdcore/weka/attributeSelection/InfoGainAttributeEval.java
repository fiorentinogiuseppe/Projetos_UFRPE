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
import weka.filters.unsupervised.attribute.NumericToBinary;





























































public class InfoGainAttributeEval
  extends ASEvaluation
  implements AttributeEvaluator, OptionHandler
{
  static final long serialVersionUID = -1949849512589218930L;
  private boolean m_missing_merge;
  private boolean m_Binarize;
  private double[] m_InfoGains;
  
  public String globalInfo()
  {
    return "InfoGainAttributeEval :\n\nEvaluates the worth of an attribute by measuring the information gain with respect to the class.\n\nInfoGain(Class,Attribute) = H(Class) - H(Class | Attribute).\n";
  }
  




  public InfoGainAttributeEval()
  {
    resetOptions();
  }
  



  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    newVector.addElement(new Option("\ttreat missing values as a seperate value.", "M", 0, "-M"));
    
    newVector.addElement(new Option("\tjust binarize numeric attributes instead \n\tof properly discretizing them.", "B", 0, "-B"));
    

    return newVector.elements();
  }
  



















  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    setMissingMerge(!Utils.getFlag('M', options));
    setBinarizeNumericAttributes(Utils.getFlag('B', options));
  }
  





  public String[] getOptions()
  {
    String[] options = new String[2];
    int current = 0;
    
    if (!getMissingMerge()) {
      options[(current++)] = "-M";
    }
    if (getBinarizeNumericAttributes()) {
      options[(current++)] = "-B";
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  




  public String binarizeNumericAttributesTipText()
  {
    return "Just binarize numeric attributes instead of properly discretizing them.";
  }
  




  public void setBinarizeNumericAttributes(boolean b)
  {
    m_Binarize = b;
  }
  





  public boolean getBinarizeNumericAttributes()
  {
    return m_Binarize;
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
    
    int classIndex = data.classIndex();
    int numInstances = data.numInstances();
    
    if (!m_Binarize) {
      Discretize disTransform = new Discretize();
      disTransform.setUseBetterEncoding(true);
      disTransform.setInputFormat(data);
      data = Filter.useFilter(data, disTransform);
    } else {
      NumericToBinary binTransform = new NumericToBinary();
      binTransform.setInputFormat(data);
      data = Filter.useFilter(data, binTransform);
    }
    int numClasses = data.attribute(classIndex).numValues();
    

    double[][][] counts = new double[data.numAttributes()][][];
    for (int k = 0; k < data.numAttributes(); k++) {
      if (k != classIndex) {
        int numValues = data.attribute(k).numValues();
        counts[k] = new double[numValues + 1][numClasses + 1];
      }
    }
    

    double[] temp = new double[numClasses + 1];
    for (int k = 0; k < numInstances; k++) {
      Instance inst = data.instance(k);
      if (inst.classIsMissing()) {
        temp[numClasses] += inst.weight();
      } else {
        temp[((int)inst.classValue())] += inst.weight();
      }
    }
    for (int k = 0; k < counts.length; k++) {
      if (k != classIndex) {
        for (int i = 0; i < temp.length; i++) {
          counts[k][0][i] = temp[i];
        }
      }
    }
    

    for (int k = 0; k < numInstances; k++) {
      Instance inst = data.instance(k);
      for (int i = 0; i < inst.numValues(); i++) {
        if (inst.index(i) != classIndex) {
          if ((inst.isMissingSparse(i)) || (inst.classIsMissing())) {
            if (!inst.isMissingSparse(i)) {
              counts[inst.index(i)][((int)inst.valueSparse(i))][numClasses] += inst.weight();
              
              counts[inst.index(i)][0][numClasses] -= inst.weight();
            } else if (!inst.classIsMissing()) {
              counts[inst.index(i)][data.attribute(inst.index(i)).numValues()][((int)inst.classValue())] += inst.weight();
              
              counts[inst.index(i)][0][((int)inst.classValue())] -= inst.weight();
            }
            else {
              counts[inst.index(i)][data.attribute(inst.index(i)).numValues()][numClasses] += inst.weight();
              
              counts[inst.index(i)][0][numClasses] -= inst.weight();
            }
          } else {
            counts[inst.index(i)][((int)inst.valueSparse(i))][((int)inst.classValue())] += inst.weight();
            
            counts[inst.index(i)][0][((int)inst.classValue())] -= inst.weight();
          }
        }
      }
    }
    

    if (m_missing_merge)
    {
      for (int k = 0; k < data.numAttributes(); k++) {
        if (k != classIndex) {
          int numValues = data.attribute(k).numValues();
          

          double[] rowSums = new double[numValues];
          double[] columnSums = new double[numClasses];
          double sum = 0.0D;
          for (int i = 0; i < numValues; i++) {
            for (int j = 0; j < numClasses; j++) {
              rowSums[i] += counts[k][i][j];
              columnSums[j] += counts[k][i][j];
            }
            sum += rowSums[i];
          }
          
          if (Utils.gr(sum, 0.0D)) {
            double[][] additions = new double[numValues][numClasses];
            

            for (int i = 0; i < numValues; i++) {
              for (int j = 0; j < numClasses; j++) {
                additions[i][j] = (rowSums[i] / sum * counts[k][numValues][j]);
              }
            }
            

            for (int i = 0; i < numClasses; i++) {
              for (int j = 0; j < numValues; j++) {
                additions[j][i] += columnSums[i] / sum * counts[k][j][numClasses];
              }
            }
            


            for (int i = 0; i < numClasses; i++) {
              for (int j = 0; j < numValues; j++) {
                additions[j][i] += counts[k][j][i] / sum * counts[k][numValues][numClasses];
              }
            }
            


            double[][] newTable = new double[numValues][numClasses];
            for (int i = 0; i < numValues; i++) {
              for (int j = 0; j < numClasses; j++) {
                newTable[i][j] = (counts[k][i][j] + additions[i][j]);
              }
            }
            counts[k] = newTable;
          }
        }
      }
    }
    

    m_InfoGains = new double[data.numAttributes()];
    for (int i = 0; i < data.numAttributes(); i++) {
      if (i != classIndex) {
        m_InfoGains[i] = (ContingencyTables.entropyOverColumns(counts[i]) - ContingencyTables.entropyConditionedOnRows(counts[i]));
      }
    }
  }
  




  protected void resetOptions()
  {
    m_InfoGains = null;
    m_missing_merge = true;
    m_Binarize = false;
  }
  









  public double evaluateAttribute(int attribute)
    throws Exception
  {
    return m_InfoGains[attribute];
  }
  



  public String toString()
  {
    StringBuffer text = new StringBuffer();
    
    if (m_InfoGains == null) {
      text.append("Information Gain attribute evaluator has not been built");
    }
    else {
      text.append("\tInformation Gain Ranking Filter");
      if (!m_missing_merge) {
        text.append("\n\tMissing values treated as seperate");
      }
      if (m_Binarize) {
        text.append("\n\tNumeric attributes are just binarized");
      }
    }
    
    text.append("\n");
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5511 $");
  }
  







  public static void main(String[] args)
  {
    runEvaluator(new InfoGainAttributeEval(), args);
  }
}
