package weka.classifiers.bayes.blr;

import weka.classifiers.bayes.BayesianLogisticRegression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;

































public class LaplacePriorImpl
  extends Prior
{
  private static final long serialVersionUID = 2353576123257012607L;
  Instances m_Instances;
  double Beta;
  double Hyperparameter;
  double DeltaUpdate;
  double[] R;
  double Delta;
  
  public LaplacePriorImpl() {}
  
  public double update(int j, Instances instances, double beta, double hyperparameter, double[] r, double deltaV)
  {
    double sign = 0.0D;
    double change = 0.0D;
    DeltaUpdate = 0.0D;
    m_Instances = instances;
    Beta = beta;
    Hyperparameter = hyperparameter;
    R = r;
    Delta = deltaV;
    
    if (Beta == 0.0D) {
      sign = 1.0D;
      DeltaUpdate = laplaceUpdate(j, sign);
      
      if (DeltaUpdate <= 0.0D) {
        sign = -1.0D;
        DeltaUpdate = laplaceUpdate(j, sign);
        
        if (DeltaUpdate >= 0.0D) {
          DeltaUpdate = 0.0D;
        }
      }
    } else {
      sign = Beta / Math.abs(Beta);
      DeltaUpdate = laplaceUpdate(j, sign);
      change = Beta + DeltaUpdate;
      change /= Math.abs(change);
      
      if (change < 0.0D) {
        DeltaUpdate = (0.0D - Beta);
      }
    }
    
    return DeltaUpdate;
  }
  















  public double laplaceUpdate(int j, double sign)
  {
    double value = 0.0D;
    double numerator = 0.0D;
    double denominator = 0.0D;
    


    for (int i = 0; i < m_Instances.numInstances(); i++) {
      Instance instance = m_Instances.instance(i);
      
      if (instance.value(j) != 0.0D) {
        numerator += instance.value(j) * BayesianLogisticRegression.classSgn(instance.classValue()) * (1.0D / (1.0D + Math.exp(R[i])));
        
        denominator += instance.value(j) * instance.value(j) * BayesianLogisticRegression.bigF(R[i], Delta * instance.value(j));
      }
    }
    

    numerator -= Math.sqrt(2.0D / Hyperparameter) * sign;
    
    if (denominator != 0.0D) {
      value = numerator / denominator;
    }
    
    return value;
  }
  






  public void computeLogLikelihood(double[] betas, Instances instances)
  {
    super.computelogLikelihood(betas, instances);
  }
  




  public void computePenalty(double[] betas, double[] hyperparameters)
  {
    penalty = 0.0D;
    
    double lambda = 0.0D;
    
    for (int j = 0; j < betas.length; j++) {
      lambda = Math.sqrt(hyperparameters[j]);
      penalty += Math.log(2.0D) - Math.log(lambda) + lambda * Math.abs(betas[j]);
    }
    

    penalty = (0.0D - penalty);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}
