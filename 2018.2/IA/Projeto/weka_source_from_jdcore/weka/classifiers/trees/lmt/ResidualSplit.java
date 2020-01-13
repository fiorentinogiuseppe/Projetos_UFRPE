package weka.classifiers.trees.lmt;

import weka.classifiers.trees.j48.ClassifierSplitModel;
import weka.classifiers.trees.j48.Distribution;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;


















































public class ResidualSplit
  extends ClassifierSplitModel
{
  private static final long serialVersionUID = -5055883734183713525L;
  protected Attribute m_attribute;
  protected int m_attIndex;
  protected int m_numInstances;
  protected int m_numClasses;
  protected Instances m_data;
  protected double[][] m_dataZs;
  protected double[][] m_dataWs;
  protected double m_splitPoint;
  
  public ResidualSplit(int attIndex)
  {
    m_attIndex = attIndex;
  }
  




  public void buildClassifier(Instances data, double[][] dataZs, double[][] dataWs)
    throws Exception
  {
    m_numClasses = data.numClasses();
    m_numInstances = data.numInstances();
    if (m_numInstances == 0) { throw new Exception("Can't build split on 0 instances");
    }
    
    m_data = data;
    m_dataZs = dataZs;
    m_dataWs = dataWs;
    m_attribute = data.attribute(m_attIndex);
    

    if (m_attribute.isNominal()) {
      m_splitPoint = 0.0D;
      m_numSubsets = m_attribute.numValues();
    } else {
      getSplitPoint();
      m_numSubsets = 2;
    }
    
    m_distribution = new Distribution(data, this);
  }
  




  protected boolean getSplitPoint()
    throws Exception
  {
    double[] splitPoints = new double[m_numInstances];
    int numSplitPoints = 0;
    
    Instances sortedData = new Instances(m_data);
    sortedData.sort(sortedData.attribute(m_attIndex));
    


    double last = sortedData.instance(0).value(m_attIndex);
    
    for (int i = 0; i < m_numInstances - 1; i++) {
      double current = sortedData.instance(i + 1).value(m_attIndex);
      if (!Utils.eq(current, last)) {
        splitPoints[(numSplitPoints++)] = ((last + current) / 2.0D);
      }
      last = current;
    }
    

    double[] entropyGain = new double[numSplitPoints];
    
    for (int i = 0; i < numSplitPoints; i++) {
      m_splitPoint = splitPoints[i];
      entropyGain[i] = entropyGain();
    }
    

    int bestSplit = -1;
    double bestGain = -1.7976931348623157E308D;
    
    for (int i = 0; i < numSplitPoints; i++) {
      if (entropyGain[i] > bestGain) {
        bestGain = entropyGain[i];
        bestSplit = i;
      }
    }
    
    if (bestSplit < 0) { return false;
    }
    m_splitPoint = splitPoints[bestSplit];
    return true;
  }
  

  public double entropyGain()
    throws Exception
  {
    int numSubsets;
    int numSubsets;
    if (m_attribute.isNominal()) {
      numSubsets = m_attribute.numValues();
    } else {
      numSubsets = 2;
    }
    
    double[][][] splitDataZs = new double[numSubsets][][];
    double[][][] splitDataWs = new double[numSubsets][][];
    

    int[] subsetSize = new int[numSubsets];
    for (int i = 0; i < m_numInstances; i++) {
      int subset = whichSubset(m_data.instance(i));
      if (subset < 0) throw new Exception("ResidualSplit: no support for splits on missing values");
      subsetSize[subset] += 1;
    }
    
    for (int i = 0; i < numSubsets; i++) {
      splitDataZs[i] = new double[subsetSize[i]][];
      splitDataWs[i] = new double[subsetSize[i]][];
    }
    

    int[] subsetCount = new int[numSubsets];
    

    for (int i = 0; i < m_numInstances; i++) {
      int subset = whichSubset(m_data.instance(i));
      splitDataZs[subset][subsetCount[subset]] = m_dataZs[i];
      splitDataWs[subset][subsetCount[subset]] = m_dataWs[i];
      subsetCount[subset] += 1;
    }
    

    double entropyOrig = entropy(m_dataZs, m_dataWs);
    
    double entropySplit = 0.0D;
    
    for (int i = 0; i < numSubsets; i++) {
      entropySplit += entropy(splitDataZs[i], splitDataWs[i]);
    }
    
    return entropyOrig - entropySplit;
  }
  



  protected double entropy(double[][] dataZs, double[][] dataWs)
  {
    double entropy = 0.0D;
    int numInstances = dataZs.length;
    
    for (int j = 0; j < m_numClasses; j++)
    {

      double m = 0.0D;
      double sum = 0.0D;
      for (int i = 0; i < numInstances; i++) {
        m += dataZs[i][j] * dataWs[i][j];
        sum += dataWs[i][j];
      }
      m /= sum;
      

      for (int i = 0; i < numInstances; i++) {
        entropy += dataWs[i][j] * Math.pow(dataZs[i][j] - m, 2.0D);
      }
    }
    

    return entropy;
  }
  



  public boolean checkModel(int minNumInstances)
  {
    int count = 0;
    for (int i = 0; i < m_distribution.numBags(); i++) {
      if (m_distribution.perBag(i) >= minNumInstances) count++;
    }
    return count >= 2;
  }
  



  public final String leftSide(Instances data)
  {
    return data.attribute(m_attIndex).name();
  }
  





  public final String rightSide(int index, Instances data)
  {
    StringBuffer text = new StringBuffer();
    if (data.attribute(m_attIndex).isNominal()) {
      text.append(" = " + data.attribute(m_attIndex).value(index));

    }
    else if (index == 0) {
      text.append(" <= " + Utils.doubleToString(m_splitPoint, 6));
    }
    else {
      text.append(" > " + Utils.doubleToString(m_splitPoint, 6));
    }
    return text.toString();
  }
  
  public final int whichSubset(Instance instance)
    throws Exception
  {
    if (instance.isMissing(m_attIndex)) {
      return -1;
    }
    if (instance.attribute(m_attIndex).isNominal()) {
      return (int)instance.value(m_attIndex);
    }
    if (Utils.smOrEq(instance.value(m_attIndex), m_splitPoint)) {
      return 0;
    }
    return 1;
  }
  



  public void buildClassifier(Instances data) {}
  


  public final double[] weights(Instance instance)
  {
    return null;
  }
  

  public final String sourceExpression(int index, Instances data)
  {
    return "";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}
