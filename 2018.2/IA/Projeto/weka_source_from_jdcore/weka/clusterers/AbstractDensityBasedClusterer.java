package weka.clusterers;

import weka.core.Instance;
import weka.core.SerializedObject;
import weka.core.Utils;























































public abstract class AbstractDensityBasedClusterer
  extends AbstractClusterer
  implements DensityBasedClusterer
{
  private static final long serialVersionUID = -5950728041704213845L;
  
  public AbstractDensityBasedClusterer() {}
  
  public abstract double[] clusterPriors()
    throws Exception;
  
  public abstract double[] logDensityPerClusterForInstance(Instance paramInstance)
    throws Exception;
  
  public double logDensityForInstance(Instance instance)
    throws Exception
  {
    double[] a = logJointDensitiesForInstance(instance);
    double max = a[Utils.maxIndex(a)];
    double sum = 0.0D;
    
    for (int i = 0; i < a.length; i++) {
      sum += Math.exp(a[i] - max);
    }
    
    return max + Math.log(sum);
  }
  






  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    return Utils.logs2probs(logJointDensitiesForInstance(instance));
  }
  







  public double[] logJointDensitiesForInstance(Instance inst)
    throws Exception
  {
    double[] weights = logDensityPerClusterForInstance(inst);
    double[] priors = clusterPriors();
    
    for (int i = 0; i < weights.length; i++) {
      if (priors[i] > 0.0D) {
        weights[i] += Math.log(priors[i]);
      } else {
        throw new IllegalArgumentException("Cluster empty!");
      }
    }
    return weights;
  }
  










  public static DensityBasedClusterer[] makeCopies(DensityBasedClusterer model, int num)
    throws Exception
  {
    if (model == null) {
      throw new Exception("No model clusterer set");
    }
    DensityBasedClusterer[] clusterers = new DensityBasedClusterer[num];
    SerializedObject so = new SerializedObject(model);
    for (int i = 0; i < clusterers.length; i++) {
      clusterers[i] = ((DensityBasedClusterer)so.getObject());
    }
    return clusterers;
  }
}
