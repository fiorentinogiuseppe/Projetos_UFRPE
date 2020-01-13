package weka.classifiers.trees.j48;

import java.io.PrintStream;
import java.util.Random;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;

















































public class NBTreeSplit
  extends ClassifierSplitModel
{
  private static final long serialVersionUID = 8922627123884975070L;
  private int m_complexityIndex;
  private int m_attIndex;
  private int m_minNoObj;
  private double m_splitPoint;
  private double m_sumOfWeights;
  private double m_errors;
  private C45Split m_c45S;
  NBTreeNoSplit m_globalNB;
  
  public NBTreeSplit(int attIndex, int minNoObj, double sumOfWeights)
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
    m_errors = 0.0D;
    if (m_globalNB != null) {
      m_errors = m_globalNB.getErrors();
    }
    


    if (trainInstances.attribute(m_attIndex).isNominal()) {
      m_complexityIndex = trainInstances.attribute(m_attIndex).numValues();
      handleEnumeratedAttribute(trainInstances);
    } else {
      m_complexityIndex = 2;
      trainInstances.sort(trainInstances.attribute(m_attIndex));
      handleNumericAttribute(trainInstances);
    }
  }
  



  public final int attIndex()
  {
    return m_attIndex;
  }
  





  private void handleEnumeratedAttribute(Instances trainInstances)
    throws Exception
  {
    m_c45S = new C45Split(m_attIndex, 2, m_sumOfWeights);
    m_c45S.buildClassifier(trainInstances);
    if (m_c45S.numSubsets() == 0) {
      return;
    }
    m_errors = 0.0D;
    

    Instances[] trainingSets = new Instances[m_complexityIndex];
    for (int i = 0; i < m_complexityIndex; i++) {
      trainingSets[i] = new Instances(trainInstances, 0);
    }
    


    for (int i = 0; i < trainInstances.numInstances(); i++) {
      Instance instance = trainInstances.instance(i);
      int subset = m_c45S.whichSubset(instance);
      if (subset > -1) {
        trainingSets[subset].add((Instance)instance.copy());
      } else {
        double[] weights = m_c45S.weights(instance);
        for (int j = 0; j < m_complexityIndex; j++) {
          try {
            Instance temp = (Instance)instance.copy();
            if (weights.length == m_complexityIndex) {
              temp.setWeight(temp.weight() * weights[j]);
            } else {
              temp.setWeight(temp.weight() / m_complexityIndex);
            }
            trainingSets[j].add(temp);
          } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("*** " + m_complexityIndex);
            System.err.println(weights.length);
            System.exit(1);
          }
        }
      }
    }
    





















    Random r = new Random(1L);
    int minNumCount = 0;
    for (int i = 0; i < m_complexityIndex; i++) {
      if (trainingSets[i].numInstances() >= 5) {
        minNumCount++;
        
        Discretize disc = new Discretize();
        disc.setInputFormat(trainingSets[i]);
        trainingSets[i] = Filter.useFilter(trainingSets[i], disc);
        
        trainingSets[i].randomize(r);
        trainingSets[i].stratify(5);
        NaiveBayesUpdateable fullModel = new NaiveBayesUpdateable();
        fullModel.buildClassifier(trainingSets[i]);
        

        m_errors += NBTreeNoSplit.crossValidate(fullModel, trainingSets[i], r);
      }
      else {
        for (int j = 0; j < trainingSets[i].numInstances(); j++) {
          m_errors += trainingSets[i].instance(j).weight();
        }
      }
    }
    


    if (minNumCount > 1) {
      m_numSubsets = m_complexityIndex;
    }
  }
  





  private void handleNumericAttribute(Instances trainInstances)
    throws Exception
  {
    m_c45S = new C45Split(m_attIndex, 2, m_sumOfWeights);
    m_c45S.buildClassifier(trainInstances);
    if (m_c45S.numSubsets() == 0) {
      return;
    }
    m_errors = 0.0D;
    
    Instances[] trainingSets = new Instances[m_complexityIndex];
    trainingSets[0] = new Instances(trainInstances, 0);
    trainingSets[1] = new Instances(trainInstances, 0);
    int subset = -1;
    

    for (int i = 0; i < trainInstances.numInstances(); i++) {
      Instance instance = trainInstances.instance(i);
      subset = m_c45S.whichSubset(instance);
      if (subset != -1) {
        trainingSets[subset].add((Instance)instance.copy());
      } else {
        double[] weights = m_c45S.weights(instance);
        for (int j = 0; j < m_complexityIndex; j++) {
          Instance temp = (Instance)instance.copy();
          if (weights.length == m_complexityIndex) {
            temp.setWeight(temp.weight() * weights[j]);
          } else {
            temp.setWeight(temp.weight() / m_complexityIndex);
          }
          trainingSets[j].add(temp);
        }
      }
    }
    







    Random r = new Random(1L);
    int minNumCount = 0;
    for (int i = 0; i < m_complexityIndex; i++) {
      if (trainingSets[i].numInstances() > 5) {
        minNumCount++;
        
        Discretize disc = new Discretize();
        disc.setInputFormat(trainingSets[i]);
        trainingSets[i] = Filter.useFilter(trainingSets[i], disc);
        
        trainingSets[i].randomize(r);
        trainingSets[i].stratify(5);
        NaiveBayesUpdateable fullModel = new NaiveBayesUpdateable();
        fullModel.buildClassifier(trainingSets[i]);
        

        m_errors += NBTreeNoSplit.crossValidate(fullModel, trainingSets[i], r);
      } else {
        for (int j = 0; j < trainingSets[i].numInstances(); j++) {
          m_errors += trainingSets[i].instance(j).weight();
        }
      }
    }
    


    if (minNumCount > 1) {
      m_numSubsets = m_complexityIndex;
    }
  }
  






  public final int whichSubset(Instance instance)
    throws Exception
  {
    return m_c45S.whichSubset(instance);
  }
  



  public final double[] weights(Instance instance)
  {
    return m_c45S.weights(instance);
  }
  








  public final String sourceExpression(int index, Instances data)
  {
    return m_c45S.sourceExpression(index, data);
  }
  





  public final String rightSide(int index, Instances data)
  {
    return m_c45S.rightSide(index, data);
  }
  





  public final String leftSide(Instances data)
  {
    return m_c45S.leftSide(data);
  }
  










  public double classProb(int classIndex, Instance instance, int theSubset)
    throws Exception
  {
    if (theSubset > -1) {
      return m_globalNB.classProb(classIndex, instance, theSubset);
    }
    throw new Exception("This shouldn't happen!!!");
  }
  





  public NBTreeNoSplit getGlobalModel()
  {
    return m_globalNB;
  }
  




  public void setGlobalModel(NBTreeNoSplit global)
  {
    m_globalNB = global;
  }
  





  public double getErrors()
  {
    return m_errors;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}
