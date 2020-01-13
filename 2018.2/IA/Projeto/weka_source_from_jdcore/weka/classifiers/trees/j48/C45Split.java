package weka.classifiers.trees.j48;

import java.util.Enumeration;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
















































public class C45Split
  extends ClassifierSplitModel
{
  private static final long serialVersionUID = 3064079330067903161L;
  private int m_complexityIndex;
  private int m_attIndex;
  private int m_minNoObj;
  private double m_splitPoint;
  private double m_infoGain;
  private double m_gainRatio;
  private double m_sumOfWeights;
  private int m_index;
  private static InfoGainSplitCrit infoGainCrit = new InfoGainSplitCrit();
  

  private static GainRatioSplitCrit gainRatioCrit = new GainRatioSplitCrit();
  




  public C45Split(int attIndex, int minNoObj, double sumOfWeights)
  {
    m_attIndex = attIndex;
    

    m_minNoObj = minNoObj;
    

    m_sumOfWeights = sumOfWeights;
  }
  







  public void buildClassifier(Instances trainInstances)
    throws Exception
  {
    m_numSubsets = 0;
    m_splitPoint = Double.MAX_VALUE;
    m_infoGain = 0.0D;
    m_gainRatio = 0.0D;
    


    if (trainInstances.attribute(m_attIndex).isNominal()) {
      m_complexityIndex = trainInstances.attribute(m_attIndex).numValues();
      m_index = m_complexityIndex;
      handleEnumeratedAttribute(trainInstances);
    } else {
      m_complexityIndex = 2;
      m_index = 0;
      trainInstances.sort(trainInstances.attribute(m_attIndex));
      handleNumericAttribute(trainInstances);
    }
  }
  



  public final int attIndex()
  {
    return m_attIndex;
  }
  





  public final double classProb(int classIndex, Instance instance, int theSubset)
    throws Exception
  {
    if (theSubset <= -1) {
      double[] weights = weights(instance);
      if (weights == null) {
        return m_distribution.prob(classIndex);
      }
      double prob = 0.0D;
      for (int i = 0; i < weights.length; i++) {
        prob += weights[i] * m_distribution.prob(classIndex, i);
      }
      return prob;
    }
    
    if (Utils.gr(m_distribution.perBag(theSubset), 0.0D)) {
      return m_distribution.prob(classIndex, theSubset);
    }
    return m_distribution.prob(classIndex);
  }
  





  public final double codingCost()
  {
    return Utils.log2(m_index);
  }
  


  public final double gainRatio()
  {
    return m_gainRatio;
  }
  







  private void handleEnumeratedAttribute(Instances trainInstances)
    throws Exception
  {
    m_distribution = new Distribution(m_complexityIndex, trainInstances.numClasses());
    


    Enumeration enu = trainInstances.enumerateInstances();
    while (enu.hasMoreElements()) {
      Instance instance = (Instance)enu.nextElement();
      if (!instance.isMissing(m_attIndex)) {
        m_distribution.add((int)instance.value(m_attIndex), instance);
      }
    }
    

    if (m_distribution.check(m_minNoObj)) {
      m_numSubsets = m_complexityIndex;
      m_infoGain = infoGainCrit.splitCritValue(m_distribution, m_sumOfWeights);
      
      m_gainRatio = gainRatioCrit.splitCritValue(m_distribution, m_sumOfWeights, m_infoGain);
    }
  }
  








  private void handleNumericAttribute(Instances trainInstances)
    throws Exception
  {
    int next = 1;
    int last = 0;
    int splitIndex = -1;
    






    m_distribution = new Distribution(2, trainInstances.numClasses());
    

    Enumeration enu = trainInstances.enumerateInstances();
    int i = 0;
    while (enu.hasMoreElements()) {
      Instance instance = (Instance)enu.nextElement();
      if (instance.isMissing(m_attIndex))
        break;
      m_distribution.add(1, instance);
      i++;
    }
    int firstMiss = i;
    


    double minSplit = 0.1D * m_distribution.total() / trainInstances.numClasses();
    
    if (Utils.smOrEq(minSplit, m_minNoObj)) {
      minSplit = m_minNoObj;
    }
    else if (Utils.gr(minSplit, 25.0D)) {
      minSplit = 25.0D;
    }
    
    if (Utils.sm(firstMiss, 2.0D * minSplit)) {
      return;
    }
    

    double defaultEnt = infoGainCrit.oldEnt(m_distribution);
    while (next < firstMiss)
    {
      if (trainInstances.instance(next - 1).value(m_attIndex) + 1.0E-5D < trainInstances.instance(next).value(m_attIndex))
      {



        m_distribution.shiftRange(1, 0, trainInstances, last, next);
        


        if ((Utils.grOrEq(m_distribution.perBag(0), minSplit)) && (Utils.grOrEq(m_distribution.perBag(1), minSplit)))
        {
          double currentInfoGain = infoGainCrit.splitCritValue(m_distribution, m_sumOfWeights, defaultEnt);
          

          if (Utils.gr(currentInfoGain, m_infoGain)) {
            m_infoGain = currentInfoGain;
            splitIndex = next - 1;
          }
          m_index += 1;
        }
        last = next;
      }
      next++;
    }
    

    if (m_index == 0) {
      return;
    }
    
    m_infoGain -= Utils.log2(m_index) / m_sumOfWeights;
    if (Utils.smOrEq(m_infoGain, 0.0D)) {
      return;
    }
    

    m_numSubsets = 2;
    m_splitPoint = ((trainInstances.instance(splitIndex + 1).value(m_attIndex) + trainInstances.instance(splitIndex).value(m_attIndex)) / 2.0D);
    




    if (m_splitPoint == trainInstances.instance(splitIndex + 1).value(m_attIndex)) {
      m_splitPoint = trainInstances.instance(splitIndex).value(m_attIndex);
    }
    

    m_distribution = new Distribution(2, trainInstances.numClasses());
    m_distribution.addRange(0, trainInstances, 0, splitIndex + 1);
    m_distribution.addRange(1, trainInstances, splitIndex + 1, firstMiss);
    

    m_gainRatio = gainRatioCrit.splitCritValue(m_distribution, m_sumOfWeights, m_infoGain);
  }
  





  public final double infoGain()
  {
    return m_infoGain;
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
  








  public final String sourceExpression(int index, Instances data)
  {
    StringBuffer expr = null;
    if (index < 0) {
      return "i[" + m_attIndex + "] == null";
    }
    if (data.attribute(m_attIndex).isNominal()) {
      expr = new StringBuffer("i[");
      expr.append(m_attIndex).append("]");
      expr.append(".equals(\"").append(data.attribute(m_attIndex).value(index)).append("\")");
    }
    else {
      expr = new StringBuffer("((Double) i[");
      expr.append(m_attIndex).append("])");
      if (index == 0) {
        expr.append(".doubleValue() <= ").append(m_splitPoint);
      } else {
        expr.append(".doubleValue() > ").append(m_splitPoint);
      }
    }
    return expr.toString();
  }
  





  public final void setSplitPoint(Instances allInstances)
  {
    double newSplitPoint = -1.7976931348623157E308D;
    


    if ((allInstances.attribute(m_attIndex).isNumeric()) && (m_numSubsets > 1))
    {
      Enumeration enu = allInstances.enumerateInstances();
      while (enu.hasMoreElements()) {
        Instance instance = (Instance)enu.nextElement();
        if (!instance.isMissing(m_attIndex)) {
          double tempValue = instance.value(m_attIndex);
          if ((Utils.gr(tempValue, newSplitPoint)) && (Utils.smOrEq(tempValue, m_splitPoint)))
          {
            newSplitPoint = tempValue; }
        }
      }
      m_splitPoint = newSplitPoint;
    }
  }
  




  public final double[][] minsAndMaxs(Instances data, double[][] minsAndMaxs, int index)
  {
    double[][] newMinsAndMaxs = new double[data.numAttributes()][2];
    
    for (int i = 0; i < data.numAttributes(); i++) {
      newMinsAndMaxs[i][0] = minsAndMaxs[i][0];
      newMinsAndMaxs[i][1] = minsAndMaxs[i][1];
      if (i == m_attIndex) {
        if (data.attribute(m_attIndex).isNominal()) {
          newMinsAndMaxs[m_attIndex][1] = 1.0D;
        } else
          newMinsAndMaxs[m_attIndex][(1 - index)] = m_splitPoint;
      }
    }
    return newMinsAndMaxs;
  }
  


  public void resetDistribution(Instances data)
    throws Exception
  {
    Instances insts = new Instances(data, data.numInstances());
    for (int i = 0; i < data.numInstances(); i++) {
      if (whichSubset(data.instance(i)) > -1) {
        insts.add(data.instance(i));
      }
    }
    Distribution newD = new Distribution(insts, this);
    newD.addInstWithUnknown(data, m_attIndex);
    m_distribution = newD;
  }
  







  public final double[] weights(Instance instance)
  {
    if (instance.isMissing(m_attIndex)) {
      double[] weights = new double[m_numSubsets];
      for (int i = 0; i < m_numSubsets; i++)
        weights[i] = (m_distribution.perBag(i) / m_distribution.total());
      return weights;
    }
    return null;
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
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.13 $");
  }
}
