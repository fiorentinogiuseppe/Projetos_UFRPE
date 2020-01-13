package weka.classifiers.bayes.net.estimate;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.classifiers.bayes.net.search.local.K2;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Statistics;
import weka.core.Utils;
import weka.estimators.Estimator;















































public class BMAEstimator
  extends SimpleEstimator
{
  static final long serialVersionUID = -1846028304233257309L;
  protected boolean m_bUseK2Prior = false;
  

  public BMAEstimator() {}
  

  public String globalInfo()
  {
    return "BMAEstimator estimates conditional probability tables of a Bayes network using Bayes Model Averaging (BMA).";
  }
  







  public void estimateCPTs(BayesNet bayesNet)
    throws Exception
  {
    initCPTs(bayesNet);
    
    Instances instances = m_Instances;
    
    for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
      if (bayesNet.getParentSet(iAttribute).getNrOfParents() > 1) {
        throw new Exception("Cannot handle networks with nodes with more than 1 parent (yet).");
      }
    }
    
    BayesNet EmptyNet = new BayesNet();
    K2 oSearchAlgorithm = new K2();
    oSearchAlgorithm.setInitAsNaiveBayes(false);
    oSearchAlgorithm.setMaxNrOfParents(0);
    EmptyNet.setSearchAlgorithm(oSearchAlgorithm);
    EmptyNet.buildClassifier(instances);
    
    BayesNet NBNet = new BayesNet();
    oSearchAlgorithm.setInitAsNaiveBayes(true);
    oSearchAlgorithm.setMaxNrOfParents(1);
    NBNet.setSearchAlgorithm(oSearchAlgorithm);
    NBNet.buildClassifier(instances);
    

    for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
      if (iAttribute != instances.classIndex()) {
        double w1 = 0.0D;double w2 = 0.0D;
        int nAttValues = instances.attribute(iAttribute).numValues();
        if (m_bUseK2Prior == true)
        {
          for (int iAttValue = 0; iAttValue < nAttValues; iAttValue++) {
            w1 += Statistics.lnGamma(1.0D + ((DiscreteEstimatorBayes)m_Distributions[iAttribute][0]).getCount(iAttValue)) - Statistics.lnGamma(1.0D);
          }
          
          w1 += Statistics.lnGamma(nAttValues) - Statistics.lnGamma(nAttValues + instances.numInstances());
          
          for (int iParent = 0; iParent < bayesNet.getParentSet(iAttribute).getCardinalityOfParents(); iParent++) {
            int nTotal = 0;
            for (int iAttValue = 0; iAttValue < nAttValues; iAttValue++) {
              double nCount = ((DiscreteEstimatorBayes)m_Distributions[iAttribute][iParent]).getCount(iAttValue);
              w2 += Statistics.lnGamma(1.0D + nCount) - Statistics.lnGamma(1.0D);
              
              nTotal = (int)(nTotal + nCount);
            }
            w2 += Statistics.lnGamma(nAttValues) - Statistics.lnGamma(nAttValues + nTotal);
          }
        }
        else {
          for (int iAttValue = 0; iAttValue < nAttValues; iAttValue++) {
            w1 += Statistics.lnGamma(1.0D / nAttValues + ((DiscreteEstimatorBayes)m_Distributions[iAttribute][0]).getCount(iAttValue)) - Statistics.lnGamma(1.0D / nAttValues);
          }
          
          w1 += Statistics.lnGamma(1.0D) - Statistics.lnGamma(1 + instances.numInstances());
          
          int nParentValues = bayesNet.getParentSet(iAttribute).getCardinalityOfParents();
          for (int iParent = 0; iParent < nParentValues; iParent++) {
            int nTotal = 0;
            for (int iAttValue = 0; iAttValue < nAttValues; iAttValue++) {
              double nCount = ((DiscreteEstimatorBayes)m_Distributions[iAttribute][iParent]).getCount(iAttValue);
              w2 += Statistics.lnGamma(1.0D / (nAttValues * nParentValues) + nCount) - Statistics.lnGamma(1.0D / (nAttValues * nParentValues));
              
              nTotal = (int)(nTotal + nCount);
            }
            w2 += Statistics.lnGamma(1.0D) - Statistics.lnGamma(1 + nTotal);
          }
        }
        

        if (w1 < w2) {
          w2 -= w1;
          w1 = 0.0D;
          w1 = 1.0D / (1.0D + Math.exp(w2));
          w2 = Math.exp(w2) / (1.0D + Math.exp(w2));
        } else {
          w1 -= w2;
          w2 = 0.0D;
          w2 = 1.0D / (1.0D + Math.exp(w1));
          w1 = Math.exp(w1) / (1.0D + Math.exp(w1));
        }
        
        for (int iParent = 0; iParent < bayesNet.getParentSet(iAttribute).getCardinalityOfParents(); iParent++) {
          m_Distributions[iAttribute][iParent] = new DiscreteEstimatorFullBayes(instances.attribute(iAttribute).numValues(), w1, w2, (DiscreteEstimatorBayes)m_Distributions[iAttribute][0], (DiscreteEstimatorBayes)m_Distributions[iAttribute][iParent], m_fAlpha);
        }
      }
    }
    






    int iAttribute = instances.classIndex();
    m_Distributions[iAttribute][0] = m_Distributions[iAttribute][0];
  }
  






  public void updateClassifier(BayesNet bayesNet, Instance instance)
    throws Exception
  {
    throw new Exception("updateClassifier does not apply to BMA estimator");
  }
  





  public void initCPTs(BayesNet bayesNet)
    throws Exception
  {
    int nMaxParentCardinality = 1;
    
    for (int iAttribute = 0; iAttribute < m_Instances.numAttributes(); iAttribute++) {
      if (bayesNet.getParentSet(iAttribute).getCardinalityOfParents() > nMaxParentCardinality) {
        nMaxParentCardinality = bayesNet.getParentSet(iAttribute).getCardinalityOfParents();
      }
    }
    

    m_Distributions = new Estimator[m_Instances.numAttributes()][nMaxParentCardinality];
  }
  





  public boolean isUseK2Prior()
  {
    return m_bUseK2Prior;
  }
  




  public void setUseK2Prior(boolean bUseK2Prior)
  {
    m_bUseK2Prior = bUseK2Prior;
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    
    newVector.addElement(new Option("\tWhether to use K2 prior.\n", "k2", 0, "-k2"));
    


    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    
    return newVector.elements();
  }
  

















  public void setOptions(String[] options)
    throws Exception
  {
    setUseK2Prior(Utils.getFlag("k2", options));
    
    super.setOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[1 + superOptions.length];
    int current = 0;
    
    if (isUseK2Prior()) {
      options[(current++)] = "-k2";
    }
    
    for (int iOption = 0; iOption < superOptions.length; iOption++) {
      options[(current++)] = superOptions[iOption];
    }
    

    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
}
