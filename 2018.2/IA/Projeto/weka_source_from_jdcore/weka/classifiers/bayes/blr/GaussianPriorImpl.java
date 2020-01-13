package weka.classifiers.bayes.blr;

import weka.classifiers.bayes.BayesianLogisticRegression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;





































public class GaussianPriorImpl
  extends Prior
{
  private static final long serialVersionUID = -2995684220141159223L;
  
  public GaussianPriorImpl() {}
  
  public double update(int j, Instances instances, double beta, double hyperparameter, double[] r, double deltaV)
  {
    double numerator = 0.0D;
    double denominator = 0.0D;
    double value = 0.0D;
    

    m_Instances = instances;
    Beta = beta;
    Hyperparameter = hyperparameter;
    Delta = deltaV;
    R = r;
    


    for (int i = 0; i < m_Instances.numInstances(); i++) {
      Instance instance = m_Instances.instance(i);
      
      if (instance.value(j) != 0.0D)
      {
        numerator += instance.value(j) * BayesianLogisticRegression.classSgn(instance.classValue()) * (0.0D - 1.0D / (1.0D + Math.exp(R[i])));
        


        denominator += instance.value(j) * instance.value(j) * BayesianLogisticRegression.bigF(R[i], Delta * Math.abs(instance.value(j)));
      }
    }
    

    numerator += 2.0D * Beta / Hyperparameter;
    denominator += 2.0D / Hyperparameter;
    value = numerator / denominator;
    
    return 0.0D - value;
  }
  





  public void computeLoglikelihood(double[] betas, Instances instances)
  {
    super.computelogLikelihood(betas, instances);
  }
  




  public void computePenalty(double[] betas, double[] hyperparameters)
  {
    penalty = 0.0D;
    
    for (int j = 0; j < betas.length; j++) {
      penalty += Math.log(Math.sqrt(hyperparameters[j])) + Math.log(6.283185307179586D) / 2.0D + betas[j] * betas[j] / (2.0D * hyperparameters[j]);
    }
    


    penalty = (0.0D - penalty);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}
