package weka.classifiers.trees.j48;

import java.io.PrintStream;
import java.io.Serializable;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.Utils;



































public abstract class ClassifierSplitModel
  implements Cloneable, Serializable, RevisionHandler
{
  private static final long serialVersionUID = 4280730118393457457L;
  protected Distribution m_distribution;
  protected int m_numSubsets;
  
  public ClassifierSplitModel() {}
  
  public Object clone()
  {
    Object clone = null;
    try
    {
      clone = super.clone();
    }
    catch (CloneNotSupportedException e) {}
    return clone;
  }
  




  public abstract void buildClassifier(Instances paramInstances)
    throws Exception;
  



  public final boolean checkModel()
  {
    if (m_numSubsets > 0) {
      return true;
    }
    return false;
  }
  







  public final double classifyInstance(Instance instance)
    throws Exception
  {
    int theSubset = whichSubset(instance);
    if (theSubset > -1) {
      return m_distribution.maxClass(theSubset);
    }
    return m_distribution.maxClass();
  }
  





  public double classProb(int classIndex, Instance instance, int theSubset)
    throws Exception
  {
    if (theSubset > -1) {
      return m_distribution.prob(classIndex, theSubset);
    }
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
  







  public double classProbLaplace(int classIndex, Instance instance, int theSubset)
    throws Exception
  {
    if (theSubset > -1) {
      return m_distribution.laplaceProb(classIndex, theSubset);
    }
    double[] weights = weights(instance);
    if (weights == null) {
      return m_distribution.laplaceProb(classIndex);
    }
    double prob = 0.0D;
    for (int i = 0; i < weights.length; i++) {
      prob += weights[i] * m_distribution.laplaceProb(classIndex, i);
    }
    return prob;
  }
  





  public double codingCost()
  {
    return 0.0D;
  }
  



  public final Distribution distribution()
  {
    return m_distribution;
  }
  





  public abstract String leftSide(Instances paramInstances);
  





  public abstract String rightSide(int paramInt, Instances paramInstances);
  




  public final String dumpLabel(int index, Instances data)
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    text.append(data.classAttribute().value(m_distribution.maxClass(index)));
    
    text.append(" (" + Utils.roundDouble(m_distribution.perBag(index), 2));
    if (Utils.gr(m_distribution.numIncorrect(index), 0.0D))
      text.append("/" + Utils.roundDouble(m_distribution.numIncorrect(index), 2));
    text.append(")");
    
    return text.toString();
  }
  
  public final String sourceClass(int index, Instances data) throws Exception
  {
    System.err.println("sourceClass");
    return m_distribution.maxClass(index);
  }
  




  public abstract String sourceExpression(int paramInt, Instances paramInstances);
  



  public final String dumpModel(Instances data)
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    for (int i = 0; i < m_numSubsets; i++) {
      text.append(leftSide(data) + rightSide(i, data) + ": ");
      text.append(dumpLabel(i, data) + "\n");
    }
    return text.toString();
  }
  



  public final int numSubsets()
  {
    return m_numSubsets;
  }
  


  public void resetDistribution(Instances data)
    throws Exception
  {
    m_distribution = new Distribution(data, this);
  }
  





  public final Instances[] split(Instances data)
    throws Exception
  {
    Instances[] instances = new Instances[m_numSubsets];
    




    for (int j = 0; j < m_numSubsets; j++) {
      instances[j] = new Instances(data, data.numInstances());
    }
    for (int i = 0; i < data.numInstances(); i++) {
      Instance instance = data.instance(i);
      double[] weights = weights(instance);
      int subset = whichSubset(instance);
      if (subset > -1) {
        instances[subset].add(instance);
      } else
        for (j = 0; j < m_numSubsets; j++)
          if (Utils.gr(weights[j], 0.0D)) {
            double newWeight = weights[j] * instance.weight();
            instances[j].add(instance);
            instances[j].lastInstance().setWeight(newWeight);
          }
    }
    for (j = 0; j < m_numSubsets; j++) {
      instances[j].compactify();
    }
    return instances;
  }
  
  public abstract double[] weights(Instance paramInstance);
  
  public abstract int whichSubset(Instance paramInstance)
    throws Exception;
}
