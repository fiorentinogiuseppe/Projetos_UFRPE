package weka.classifiers.trees;

import java.io.PrintStream;
import weka.classifiers.Classifier;
import weka.classifiers.Sourcable;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.ContingencyTables;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;

























































public class DecisionStump
  extends Classifier
  implements WeightedInstancesHandler, Sourcable
{
  static final long serialVersionUID = 1618384535950391L;
  private int m_AttIndex;
  private double m_SplitPoint;
  private double[][] m_Distribution;
  private Instances m_Instances;
  private Classifier m_ZeroR;
  
  public DecisionStump() {}
  
  public String globalInfo()
  {
    return "Class for building and using a decision stump. Usually used in conjunction with a boosting algorithm. Does regression (based on mean-squared error) or classification (based on entropy). Missing is treated as a separate value.";
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
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  





  public void buildClassifier(Instances instances)
    throws Exception
  {
    double bestVal = Double.MAX_VALUE;
    double bestPoint = -1.7976931348623157E308D;
    int bestAtt = -1;
    

    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    

    if (instances.numAttributes() == 1) {
      System.err.println("Cannot build model (only class attribute present in data!), using ZeroR model instead!");
      

      m_ZeroR = new ZeroR();
      m_ZeroR.buildClassifier(instances);
      return;
    }
    
    m_ZeroR = null;
    

    double[][] bestDist = new double[3][instances.numClasses()];
    
    m_Instances = new Instances(instances);
    int numClasses;
    int numClasses; if (m_Instances.classAttribute().isNominal()) {
      numClasses = m_Instances.numClasses();
    } else {
      numClasses = 1;
    }
    

    boolean first = true;
    for (int i = 0; i < m_Instances.numAttributes(); i++) {
      if (i != m_Instances.classIndex())
      {

        m_Distribution = new double[3][numClasses];
        double currVal;
        double currVal;
        if (m_Instances.attribute(i).isNominal()) {
          currVal = findSplitNominal(i);
        } else {
          currVal = findSplitNumeric(i);
        }
        if ((first) || (currVal < bestVal)) {
          bestVal = currVal;
          bestAtt = i;
          bestPoint = m_SplitPoint;
          for (int j = 0; j < 3; j++) {
            System.arraycopy(m_Distribution[j], 0, bestDist[j], 0, numClasses);
          }
        }
        


        first = false;
      }
    }
    

    m_AttIndex = bestAtt;
    m_SplitPoint = bestPoint;
    m_Distribution = bestDist;
    if (m_Instances.classAttribute().isNominal()) {
      for (int i = 0; i < m_Distribution.length; i++) {
        double sumCounts = Utils.sum(m_Distribution[i]);
        if (sumCounts == 0.0D) {
          System.arraycopy(m_Distribution[2], 0, m_Distribution[i], 0, m_Distribution[2].length);
          
          Utils.normalize(m_Distribution[i]);
        } else {
          Utils.normalize(m_Distribution[i], sumCounts);
        }
      }
    }
    

    m_Instances = new Instances(m_Instances, 0);
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_ZeroR != null) {
      return m_ZeroR.distributionForInstance(instance);
    }
    
    return m_Distribution[whichSubset(instance)];
  }
  






  public String toSource(String className)
    throws Exception
  {
    StringBuffer text = new StringBuffer("class ");
    Attribute c = m_Instances.classAttribute();
    text.append(className).append(" {\n  public static double classify(Object[] i) {\n");
    

    text.append("    /* " + m_Instances.attribute(m_AttIndex).name() + " */\n");
    text.append("    if (i[").append(m_AttIndex);
    text.append("] == null) { return ");
    text.append(sourceClass(c, m_Distribution[2])).append(";");
    if (m_Instances.attribute(m_AttIndex).isNominal()) {
      text.append(" } else if (((String)i[").append(m_AttIndex);
      text.append("]).equals(\"");
      text.append(m_Instances.attribute(m_AttIndex).value((int)m_SplitPoint));
      text.append("\")");
    } else {
      text.append(" } else if (((Double)i[").append(m_AttIndex);
      text.append("]).doubleValue() <= ").append(m_SplitPoint);
    }
    text.append(") { return ");
    text.append(sourceClass(c, m_Distribution[0])).append(";");
    text.append(" } else { return ");
    text.append(sourceClass(c, m_Distribution[1])).append(";");
    text.append(" }\n  }\n}\n");
    return text.toString();
  }
  







  private String sourceClass(Attribute c, double[] dist)
  {
    if (c.isNominal()) {
      return Integer.toString(Utils.maxIndex(dist));
    }
    return Double.toString(dist[0]);
  }
  







  public String toString()
  {
    if (m_ZeroR != null) {
      StringBuffer buf = new StringBuffer();
      buf.append(getClass().getName().replaceAll(".*\\.", "") + "\n");
      buf.append(getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n\n");
      buf.append("Warning: No model could be built, hence ZeroR model is used:\n\n");
      buf.append(m_ZeroR.toString());
      return buf.toString();
    }
    
    if (m_Instances == null) {
      return "Decision Stump: No model built yet.";
    }
    try {
      StringBuffer text = new StringBuffer();
      
      text.append("Decision Stump\n\n");
      text.append("Classifications\n\n");
      Attribute att = m_Instances.attribute(m_AttIndex);
      if (att.isNominal()) {
        text.append(att.name() + " = " + att.value((int)m_SplitPoint) + " : ");
        
        text.append(printClass(m_Distribution[0]));
        text.append(att.name() + " != " + att.value((int)m_SplitPoint) + " : ");
        
        text.append(printClass(m_Distribution[1]));
      } else {
        text.append(att.name() + " <= " + m_SplitPoint + " : ");
        text.append(printClass(m_Distribution[0]));
        text.append(att.name() + " > " + m_SplitPoint + " : ");
        text.append(printClass(m_Distribution[1]));
      }
      text.append(att.name() + " is missing : ");
      text.append(printClass(m_Distribution[2]));
      
      if (m_Instances.classAttribute().isNominal()) {
        text.append("\nClass distributions\n\n");
        if (att.isNominal()) {
          text.append(att.name() + " = " + att.value((int)m_SplitPoint) + "\n");
          
          text.append(printDist(m_Distribution[0]));
          text.append(att.name() + " != " + att.value((int)m_SplitPoint) + "\n");
          
          text.append(printDist(m_Distribution[1]));
        } else {
          text.append(att.name() + " <= " + m_SplitPoint + "\n");
          text.append(printDist(m_Distribution[0]));
          text.append(att.name() + " > " + m_SplitPoint + "\n");
          text.append(printDist(m_Distribution[1]));
        }
        text.append(att.name() + " is missing\n");
        text.append(printDist(m_Distribution[2]));
      }
      
      return text.toString();
    } catch (Exception e) {}
    return "Can't print decision stump classifier!";
  }
  







  private String printDist(double[] dist)
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    
    if (m_Instances.classAttribute().isNominal()) {
      for (int i = 0; i < m_Instances.numClasses(); i++) {
        text.append(m_Instances.classAttribute().value(i) + "\t");
      }
      text.append("\n");
      for (int i = 0; i < m_Instances.numClasses(); i++) {
        text.append(dist[i] + "\t");
      }
      text.append("\n");
    }
    
    return text.toString();
  }
  






  private String printClass(double[] dist)
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    
    if (m_Instances.classAttribute().isNominal()) {
      text.append(m_Instances.classAttribute().value(Utils.maxIndex(dist)));
    } else {
      text.append(dist[0]);
    }
    
    return text.toString() + "\n";
  }
  






  private double findSplitNominal(int index)
    throws Exception
  {
    if (m_Instances.classAttribute().isNominal()) {
      return findSplitNominalNominal(index);
    }
    return findSplitNominalNumeric(index);
  }
  








  private double findSplitNominalNominal(int index)
    throws Exception
  {
    double bestVal = Double.MAX_VALUE;
    double[][] counts = new double[m_Instances.attribute(index).numValues() + 1][m_Instances.numClasses()];
    
    double[] sumCounts = new double[m_Instances.numClasses()];
    double[][] bestDist = new double[3][m_Instances.numClasses()];
    int numMissing = 0;
    

    for (int i = 0; i < m_Instances.numInstances(); i++) {
      Instance inst = m_Instances.instance(i);
      if (inst.isMissing(index)) {
        numMissing++;
        counts[m_Instances.attribute(index).numValues()][((int)inst.classValue())] += inst.weight();
      }
      else {
        counts[((int)inst.value(index))][((int)inst.classValue())] += inst.weight();
      }
    }
    


    for (int i = 0; i < m_Instances.attribute(index).numValues(); i++) {
      for (int j = 0; j < m_Instances.numClasses(); j++) {
        sumCounts[j] += counts[i][j];
      }
    }
    

    System.arraycopy(counts[m_Instances.attribute(index).numValues()], 0, m_Distribution[2], 0, m_Instances.numClasses());
    
    for (int i = 0; i < m_Instances.attribute(index).numValues(); i++) {
      for (int j = 0; j < m_Instances.numClasses(); j++) {
        m_Distribution[0][j] = counts[i][j];
        m_Distribution[1][j] = (sumCounts[j] - counts[i][j]);
      }
      double currVal = ContingencyTables.entropyConditionedOnRows(m_Distribution);
      if (currVal < bestVal) {
        bestVal = currVal;
        m_SplitPoint = i;
        for (int j = 0; j < 3; j++) {
          System.arraycopy(m_Distribution[j], 0, bestDist[j], 0, m_Instances.numClasses());
        }
      }
    }
    


    if (numMissing == 0) {
      System.arraycopy(sumCounts, 0, bestDist[2], 0, m_Instances.numClasses());
    }
    

    m_Distribution = bestDist;
    return bestVal;
  }
  







  private double findSplitNominalNumeric(int index)
    throws Exception
  {
    double bestVal = Double.MAX_VALUE;
    double[] sumsSquaresPerValue = new double[m_Instances.attribute(index).numValues()];
    
    double[] sumsPerValue = new double[m_Instances.attribute(index).numValues()];
    double[] weightsPerValue = new double[m_Instances.attribute(index).numValues()];
    double totalSumSquaresW = 0.0D;double totalSumW = 0.0D;double totalSumOfWeightsW = 0.0D;
    double totalSumOfWeights = 0.0D;double totalSum = 0.0D;
    double[] sumsSquares = new double[3];double[] sumOfWeights = new double[3];
    double[][] bestDist = new double[3][1];
    

    for (int i = 0; i < m_Instances.numInstances(); i++) {
      Instance inst = m_Instances.instance(i);
      if (inst.isMissing(index)) {
        m_Distribution[2][0] += inst.classValue() * inst.weight();
        sumsSquares[2] += inst.classValue() * inst.classValue() * inst.weight();
        
        sumOfWeights[2] += inst.weight();
      } else {
        weightsPerValue[((int)inst.value(index))] += inst.weight();
        sumsPerValue[((int)inst.value(index))] += inst.classValue() * inst.weight();
        
        sumsSquaresPerValue[((int)inst.value(index))] += inst.classValue() * inst.classValue() * inst.weight();
      }
      
      totalSumOfWeights += inst.weight();
      totalSum += inst.classValue() * inst.weight();
    }
    

    if (totalSumOfWeights <= 0.0D) {
      return bestVal;
    }
    

    for (int i = 0; i < m_Instances.attribute(index).numValues(); i++) {
      totalSumOfWeightsW += weightsPerValue[i];
      totalSumSquaresW += sumsSquaresPerValue[i];
      totalSumW += sumsPerValue[i];
    }
    

    for (int i = 0; i < m_Instances.attribute(index).numValues(); i++)
    {
      m_Distribution[0][0] = sumsPerValue[i];
      sumsSquares[0] = sumsSquaresPerValue[i];
      sumOfWeights[0] = weightsPerValue[i];
      m_Distribution[1][0] = (totalSumW - sumsPerValue[i]);
      sumsSquares[1] = (totalSumSquaresW - sumsSquaresPerValue[i]);
      sumOfWeights[1] = (totalSumOfWeightsW - weightsPerValue[i]);
      
      double currVal = variance(m_Distribution, sumsSquares, sumOfWeights);
      
      if (currVal < bestVal) {
        bestVal = currVal;
        m_SplitPoint = i;
        for (int j = 0; j < 3; j++) {
          if (sumOfWeights[j] > 0.0D) {
            bestDist[j][0] = (m_Distribution[j][0] / sumOfWeights[j]);
          } else {
            bestDist[j][0] = (totalSum / totalSumOfWeights);
          }
        }
      }
    }
    
    m_Distribution = bestDist;
    return bestVal;
  }
  






  private double findSplitNumeric(int index)
    throws Exception
  {
    if (m_Instances.classAttribute().isNominal()) {
      return findSplitNumericNominal(index);
    }
    return findSplitNumericNumeric(index);
  }
  








  private double findSplitNumericNominal(int index)
    throws Exception
  {
    double bestVal = Double.MAX_VALUE;
    int numMissing = 0;
    double[] sum = new double[m_Instances.numClasses()];
    double[][] bestDist = new double[3][m_Instances.numClasses()];
    

    for (int i = 0; i < m_Instances.numInstances(); i++) {
      Instance inst = m_Instances.instance(i);
      if (!inst.isMissing(index)) {
        m_Distribution[1][((int)inst.classValue())] += inst.weight();
      } else {
        m_Distribution[2][((int)inst.classValue())] += inst.weight();
        numMissing++;
      }
    }
    System.arraycopy(m_Distribution[1], 0, sum, 0, m_Instances.numClasses());
    

    for (int j = 0; j < 3; j++) {
      System.arraycopy(m_Distribution[j], 0, bestDist[j], 0, m_Instances.numClasses());
    }
    


    m_Instances.sort(index);
    

    for (int i = 0; i < m_Instances.numInstances() - (numMissing + 1); i++) {
      Instance inst = m_Instances.instance(i);
      Instance instPlusOne = m_Instances.instance(i + 1);
      m_Distribution[0][((int)inst.classValue())] += inst.weight();
      m_Distribution[1][((int)inst.classValue())] -= inst.weight();
      if (inst.value(index) < instPlusOne.value(index)) {
        double currCutPoint = (inst.value(index) + instPlusOne.value(index)) / 2.0D;
        double currVal = ContingencyTables.entropyConditionedOnRows(m_Distribution);
        if (currVal < bestVal) {
          m_SplitPoint = currCutPoint;
          bestVal = currVal;
          for (int j = 0; j < 3; j++) {
            System.arraycopy(m_Distribution[j], 0, bestDist[j], 0, m_Instances.numClasses());
          }
        }
      }
    }
    


    if (numMissing == 0) {
      System.arraycopy(sum, 0, bestDist[2], 0, m_Instances.numClasses());
    }
    
    m_Distribution = bestDist;
    return bestVal;
  }
  







  private double findSplitNumericNumeric(int index)
    throws Exception
  {
    double bestVal = Double.MAX_VALUE;
    int numMissing = 0;
    double[] sumsSquares = new double[3];double[] sumOfWeights = new double[3];
    double[][] bestDist = new double[3][1];
    double totalSum = 0.0D;double totalSumOfWeights = 0.0D;
    

    for (int i = 0; i < m_Instances.numInstances(); i++) {
      Instance inst = m_Instances.instance(i);
      if (!inst.isMissing(index)) {
        m_Distribution[1][0] += inst.classValue() * inst.weight();
        sumsSquares[1] += inst.classValue() * inst.classValue() * inst.weight();
        
        sumOfWeights[1] += inst.weight();
      } else {
        m_Distribution[2][0] += inst.classValue() * inst.weight();
        sumsSquares[2] += inst.classValue() * inst.classValue() * inst.weight();
        
        sumOfWeights[2] += inst.weight();
        numMissing++;
      }
      totalSumOfWeights += inst.weight();
      totalSum += inst.classValue() * inst.weight();
    }
    

    if (totalSumOfWeights <= 0.0D) {
      return bestVal;
    }
    

    m_Instances.sort(index);
    

    for (int i = 0; i < m_Instances.numInstances() - (numMissing + 1); i++) {
      Instance inst = m_Instances.instance(i);
      Instance instPlusOne = m_Instances.instance(i + 1);
      m_Distribution[0][0] += inst.classValue() * inst.weight();
      sumsSquares[0] += inst.classValue() * inst.classValue() * inst.weight();
      sumOfWeights[0] += inst.weight();
      m_Distribution[1][0] -= inst.classValue() * inst.weight();
      sumsSquares[1] -= inst.classValue() * inst.classValue() * inst.weight();
      sumOfWeights[1] -= inst.weight();
      if (inst.value(index) < instPlusOne.value(index)) {
        double currCutPoint = (inst.value(index) + instPlusOne.value(index)) / 2.0D;
        double currVal = variance(m_Distribution, sumsSquares, sumOfWeights);
        if (currVal < bestVal) {
          m_SplitPoint = currCutPoint;
          bestVal = currVal;
          for (int j = 0; j < 3; j++) {
            if (sumOfWeights[j] > 0.0D) {
              bestDist[j][0] = (m_Distribution[j][0] / sumOfWeights[j]);
            } else {
              bestDist[j][0] = (totalSum / totalSumOfWeights);
            }
          }
        }
      }
    }
    
    m_Distribution = bestDist;
    return bestVal;
  }
  








  private double variance(double[][] s, double[] sS, double[] sumOfWeights)
  {
    double var = 0.0D;
    
    for (int i = 0; i < s.length; i++) {
      if (sumOfWeights[i] > 0.0D) {
        var += sS[i] - s[i][0] * s[i][0] / sumOfWeights[i];
      }
    }
    
    return var;
  }
  






  private int whichSubset(Instance instance)
    throws Exception
  {
    if (instance.isMissing(m_AttIndex))
      return 2;
    if (instance.attribute(m_AttIndex).isNominal()) {
      if ((int)instance.value(m_AttIndex) == m_SplitPoint) {
        return 0;
      }
      return 1;
    }
    
    if (instance.value(m_AttIndex) <= m_SplitPoint) {
      return 0;
    }
    return 1;
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5535 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new DecisionStump(), argv);
  }
}
