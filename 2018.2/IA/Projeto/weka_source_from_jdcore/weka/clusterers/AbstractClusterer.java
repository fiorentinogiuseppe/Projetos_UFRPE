package weka.clusterers;

import java.io.PrintStream;
import java.io.Serializable;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SerializedObject;
import weka.core.Utils;















































public abstract class AbstractClusterer
  implements Clusterer, Cloneable, Serializable, CapabilitiesHandler, RevisionHandler
{
  private static final long serialVersionUID = -6099962589663877632L;
  
  public AbstractClusterer() {}
  
  public abstract void buildClusterer(Instances paramInstances)
    throws Exception;
  
  public int clusterInstance(Instance instance)
    throws Exception
  {
    double[] dist = distributionForInstance(instance);
    
    if (dist == null) {
      throw new Exception("Null distribution predicted");
    }
    
    if (Utils.sum(dist) <= 0.0D) {
      throw new Exception("Unable to cluster instance");
    }
    return Utils.maxIndex(dist);
  }
  











  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] d = new double[numberOfClusters()];
    
    d[clusterInstance(instance)] = 1.0D;
    
    return d;
  }
  









  public abstract int numberOfClusters()
    throws Exception;
  









  public static Clusterer forName(String clustererName, String[] options)
    throws Exception
  {
    return (Clusterer)Utils.forName(Clusterer.class, clustererName, options);
  }
  







  public static Clusterer makeCopy(Clusterer model)
    throws Exception
  {
    return (Clusterer)new SerializedObject(model).getObject();
  }
  










  public static Clusterer[] makeCopies(Clusterer model, int num)
    throws Exception
  {
    if (model == null) {
      throw new Exception("No model clusterer set");
    }
    Clusterer[] clusterers = new Clusterer[num];
    SerializedObject so = new SerializedObject(model);
    for (int i = 0; i < clusterers.length; i++) {
      clusterers[i] = ((Clusterer)so.getObject());
    }
    return clusterers;
  }
  








  public Capabilities getCapabilities()
  {
    Capabilities result = new Capabilities(this);
    result.enableAll();
    

    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5537 $");
  }
  




  protected static void runClusterer(Clusterer clusterer, String[] options)
  {
    try
    {
      System.out.println(ClusterEvaluation.evaluateClusterer(clusterer, options));
    }
    catch (Exception e) {
      if ((e.getMessage() == null) || ((e.getMessage() != null) && (e.getMessage().indexOf("General options") == -1)))
      {

        e.printStackTrace();
      } else {
        System.err.println(e.getMessage());
      }
    }
  }
}
