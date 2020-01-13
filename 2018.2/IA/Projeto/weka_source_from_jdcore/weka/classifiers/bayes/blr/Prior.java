package weka.classifiers.bayes.blr;

import java.io.Serializable;
import weka.classifiers.bayes.BayesianLogisticRegression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;































public abstract class Prior
  implements Serializable, RevisionHandler
{
  protected Instances m_Instances;
  protected double Beta = 0.0D;
  protected double Hyperparameter = 0.0D;
  protected double DeltaUpdate;
  protected double[] R;
  protected double Delta = 0.0D;
  protected double log_posterior = 0.0D;
  protected double log_likelihood = 0.0D;
  protected double penalty = 0.0D;
  


  public Prior() {}
  

  public double update(int j, Instances instances, double beta, double hyperparameter, double[] r, double deltaV)
  {
    return 0.0D;
  }
  






  public void computelogLikelihood(double[] betas, Instances instances)
  {
    log_likelihood = 0.0D;
    
    for (int i = 0; i < instances.numInstances(); i++) {
      Instance instance = instances.instance(i);
      
      double log_row = 0.0D;
      
      for (int j = 0; j < instance.numAttributes(); j++) {
        if (instance.value(j) != 0.0D) {
          log_row += betas[j] * instance.value(j) * instance.value(j);
        }
      }
      
      log_row *= BayesianLogisticRegression.classSgn(instance.classValue());
      log_likelihood += Math.log(1.0D + Math.exp(0.0D - log_row));
    }
    
    log_likelihood = (0.0D - log_likelihood);
  }
  





  public void computePenalty(double[] betas, double[] hyperparameters) {}
  





  public double getLoglikelihood()
  {
    return log_likelihood;
  }
  



  public double getLogPosterior()
  {
    log_posterior = (log_likelihood + penalty);
    
    return log_posterior;
  }
  



  public double getPenalty()
  {
    return penalty;
  }
}
