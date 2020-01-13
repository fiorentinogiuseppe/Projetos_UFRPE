package weka.classifiers.bayes.net.estimate;

import java.util.Enumeration;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.estimators.Estimator;











































public class SimpleEstimator
  extends BayesNetEstimator
{
  static final long serialVersionUID = 5874941612331806172L;
  
  public SimpleEstimator() {}
  
  public String globalInfo()
  {
    return "SimpleEstimator is used for estimating the conditional probability tables of a Bayes network once the structure has been learned. Estimates probabilities directly from data.";
  }
  








  public void estimateCPTs(BayesNet bayesNet)
    throws Exception
  {
    initCPTs(bayesNet);
    

    Enumeration enumInsts = m_Instances.enumerateInstances();
    while (enumInsts.hasMoreElements()) {
      Instance instance = (Instance)enumInsts.nextElement();
      
      updateClassifier(bayesNet, instance);
    }
  }
  






  public void updateClassifier(BayesNet bayesNet, Instance instance)
    throws Exception
  {
    for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
      double iCPT = 0.0D;
      
      for (int iParent = 0; iParent < bayesNet.getParentSet(iAttribute).getNrOfParents(); iParent++) {
        int nParent = bayesNet.getParentSet(iAttribute).getParent(iParent);
        
        iCPT = iCPT * m_Instances.attribute(nParent).numValues() + instance.value(nParent);
      }
      
      m_Distributions[iAttribute][((int)iCPT)].addValue(instance.value(iAttribute), instance.weight());
    }
  }
  





  public void initCPTs(BayesNet bayesNet)
    throws Exception
  {
    Instances instances = m_Instances;
    

    int nMaxParentCardinality = 1;
    for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
      if (bayesNet.getParentSet(iAttribute).getCardinalityOfParents() > nMaxParentCardinality) {
        nMaxParentCardinality = bayesNet.getParentSet(iAttribute).getCardinalityOfParents();
      }
    }
    

    m_Distributions = new Estimator[instances.numAttributes()][nMaxParentCardinality];
    

    for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
      for (int iParent = 0; iParent < bayesNet.getParentSet(iAttribute).getCardinalityOfParents(); iParent++) {
        m_Distributions[iAttribute][iParent] = new DiscreteEstimatorBayes(instances.attribute(iAttribute).numValues(), m_fAlpha);
      }
    }
  }
  








  public double[] distributionForInstance(BayesNet bayesNet, Instance instance)
    throws Exception
  {
    Instances instances = m_Instances;
    int nNumClasses = instances.numClasses();
    double[] fProbs = new double[nNumClasses];
    
    for (int iClass = 0; iClass < nNumClasses; iClass++) {
      fProbs[iClass] = 1.0D;
    }
    
    for (int iClass = 0; iClass < nNumClasses; iClass++) {
      double logfP = 0.0D;
      
      for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
        double iCPT = 0.0D;
        
        for (int iParent = 0; iParent < bayesNet.getParentSet(iAttribute).getNrOfParents(); iParent++) {
          int nParent = bayesNet.getParentSet(iAttribute).getParent(iParent);
          
          if (nParent == instances.classIndex()) {
            iCPT = iCPT * nNumClasses + iClass;
          } else {
            iCPT = iCPT * instances.attribute(nParent).numValues() + instance.value(nParent);
          }
        }
        
        if (iAttribute == instances.classIndex())
        {

          logfP += Math.log(m_Distributions[iAttribute][((int)iCPT)].getProbability(iClass));

        }
        else
        {
          logfP += Math.log(m_Distributions[iAttribute][((int)iCPT)].getProbability(instance.value(iAttribute)));
        }
      }
      


      fProbs[iClass] += logfP;
    }
    

    double fMax = fProbs[0];
    for (int iClass = 0; iClass < nNumClasses; iClass++) {
      if (fProbs[iClass] > fMax) {
        fMax = fProbs[iClass];
      }
    }
    
    for (int iClass = 0; iClass < nNumClasses; iClass++) {
      fProbs[iClass] = Math.exp(fProbs[iClass] - fMax);
    }
    

    Utils.normalize(fProbs);
    
    return fProbs;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
}
