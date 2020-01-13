package weka.classifiers.functions;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.rules.ZeroR;
import weka.clusterers.MakeDensityBasedClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ClusterMembership;
import weka.filters.unsupervised.attribute.Standardize;





























































public class RBFNetwork
  extends Classifier
  implements OptionHandler
{
  static final long serialVersionUID = -3669814959712675720L;
  private Logistic m_logistic;
  private LinearRegression m_linear;
  private ClusterMembership m_basisFilter;
  private Standardize m_standardize;
  private int m_numClusters = 2;
  

  protected double m_ridge = 1.0E-8D;
  

  private int m_maxIts = -1;
  

  private int m_clusteringSeed = 1;
  

  private double m_minStdDev = 0.1D;
  

  private Classifier m_ZeroR;
  

  public RBFNetwork() {}
  

  public String globalInfo()
  {
    return "Class that implements a normalized Gaussian radial basisbasis function network.\nIt uses the k-means clustering algorithm to provide the basis functions and learns either a logistic regression (discrete class problems) or linear regression (numeric class problems) on top of that. Symmetric multivariate Gaussians are fit to the data from each cluster. If the class is nominal it uses the given number of clusters per class.It standardizes all numeric attributes to zero mean and unit variance.";
  }
  
















  public Capabilities getCapabilities()
  {
    Capabilities result = new Logistic().getCapabilities();
    result.or(new LinearRegression().getCapabilities());
    Capabilities classes = result.getClassCapabilities();
    result.and(new SimpleKMeans().getCapabilities());
    result.or(classes);
    return result;
  }
  






  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    

    if (instances.numAttributes() == 1) {
      System.err.println("Cannot build model (only class attribute present in data!), using ZeroR model instead!");
      

      m_ZeroR = new ZeroR();
      m_ZeroR.buildClassifier(instances);
      return;
    }
    
    m_ZeroR = null;
    

    m_standardize = new Standardize();
    m_standardize.setInputFormat(instances);
    instances = Filter.useFilter(instances, m_standardize);
    
    SimpleKMeans sk = new SimpleKMeans();
    sk.setNumClusters(m_numClusters);
    sk.setSeed(m_clusteringSeed);
    MakeDensityBasedClusterer dc = new MakeDensityBasedClusterer();
    dc.setClusterer(sk);
    dc.setMinStdDev(m_minStdDev);
    m_basisFilter = new ClusterMembership();
    m_basisFilter.setDensityBasedClusterer(dc);
    m_basisFilter.setInputFormat(instances);
    Instances transformed = Filter.useFilter(instances, m_basisFilter);
    
    if (instances.classAttribute().isNominal()) {
      m_linear = null;
      m_logistic = new Logistic();
      m_logistic.setRidge(m_ridge);
      m_logistic.setMaxIts(m_maxIts);
      m_logistic.buildClassifier(transformed);
    } else {
      m_logistic = null;
      m_linear = new LinearRegression();
      m_linear.setAttributeSelectionMethod(new SelectedTag(1, LinearRegression.TAGS_SELECTION));
      
      m_linear.setRidge(m_ridge);
      m_linear.buildClassifier(transformed);
    }
  }
  








  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_ZeroR != null) {
      return m_ZeroR.distributionForInstance(instance);
    }
    
    m_standardize.input(instance);
    m_basisFilter.input(m_standardize.output());
    Instance transformed = m_basisFilter.output();
    
    return instance.classAttribute().isNominal() ? m_logistic.distributionForInstance(transformed) : m_linear.distributionForInstance(transformed);
  }
  








  public String toString()
  {
    if (m_ZeroR != null) {
      StringBuffer buf = new StringBuffer();
      buf.append(getClass().getName().replaceAll(".*\\.", "") + "\n");
      buf.append(getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n\n");
      buf.append("Warning: No model could be built, hence ZeroR model is used:\n\n");
      buf.append(m_ZeroR.toString());
      return buf.toString();
    }
    
    if (m_basisFilter == null) {
      return "No classifier built yet!";
    }
    
    StringBuffer sb = new StringBuffer();
    sb.append("Radial basis function network\n");
    sb.append(m_linear == null ? "(Logistic regression " : "(Linear regression ");
    

    sb.append("applied to K-means clusters as basis functions):\n\n");
    sb.append(m_linear == null ? m_logistic.toString() : m_linear.toString());
    

    return sb.toString();
  }
  




  public String maxItsTipText()
  {
    return "Maximum number of iterations for the logistic regression to perform. Only applied to discrete class problems.";
  }
  






  public int getMaxIts()
  {
    return m_maxIts;
  }
  





  public void setMaxIts(int newMaxIts)
  {
    m_maxIts = newMaxIts;
  }
  




  public String ridgeTipText()
  {
    return "Set the Ridge value for the logistic or linear regression.";
  }
  




  public void setRidge(double ridge)
  {
    m_ridge = ridge;
  }
  




  public double getRidge()
  {
    return m_ridge;
  }
  




  public String numClustersTipText()
  {
    return "The number of clusters for K-Means to generate.";
  }
  




  public void setNumClusters(int numClusters)
  {
    if (numClusters > 0) {
      m_numClusters = numClusters;
    }
  }
  




  public int getNumClusters()
  {
    return m_numClusters;
  }
  




  public String clusteringSeedTipText()
  {
    return "The random seed to pass on to K-means.";
  }
  




  public void setClusteringSeed(int seed)
  {
    m_clusteringSeed = seed;
  }
  




  public int getClusteringSeed()
  {
    return m_clusteringSeed;
  }
  




  public String minStdDevTipText()
  {
    return "Sets the minimum standard deviation for the clusters.";
  }
  



  public double getMinStdDev()
  {
    return m_minStdDev;
  }
  



  public void setMinStdDev(double newMinStdDev)
  {
    m_minStdDev = newMinStdDev;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tSet the number of clusters (basis functions) to generate. (default = 2).", "B", 1, "-B <number>"));
    

    newVector.addElement(new Option("\tSet the random seed to be used by K-means. (default = 1).", "S", 1, "-S <seed>"));
    

    newVector.addElement(new Option("\tSet the ridge value for the logistic or linear regression.", "R", 1, "-R <ridge>"));
    

    newVector.addElement(new Option("\tSet the maximum number of iterations for the logistic regression. (default -1, until convergence).", "M", 1, "-M <number>"));
    


    newVector.addElement(new Option("\tSet the minimum standard deviation for the clusters. (default 0.1).", "W", 1, "-W <number>"));
    


    return newVector.elements();
  }
  
























  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
    
    String ridgeString = Utils.getOption('R', options);
    if (ridgeString.length() != 0) {
      m_ridge = Double.parseDouble(ridgeString);
    } else {
      m_ridge = 1.0E-8D;
    }
    
    String maxItsString = Utils.getOption('M', options);
    if (maxItsString.length() != 0) {
      m_maxIts = Integer.parseInt(maxItsString);
    } else {
      m_maxIts = -1;
    }
    
    String numClustersString = Utils.getOption('B', options);
    if (numClustersString.length() != 0) {
      setNumClusters(Integer.parseInt(numClustersString));
    }
    
    String seedString = Utils.getOption('S', options);
    if (seedString.length() != 0) {
      setClusteringSeed(Integer.parseInt(seedString));
    }
    String stdString = Utils.getOption('W', options);
    if (stdString.length() != 0) {
      setMinStdDev(Double.parseDouble(stdString));
    }
    Utils.checkForRemainingOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] options = new String[10];
    int current = 0;
    
    options[(current++)] = "-B";
    options[(current++)] = ("" + m_numClusters);
    options[(current++)] = "-S";
    options[(current++)] = ("" + m_clusteringSeed);
    options[(current++)] = "-R";
    options[(current++)] = ("" + m_ridge);
    options[(current++)] = "-M";
    options[(current++)] = ("" + m_maxIts);
    options[(current++)] = "-W";
    options[(current++)] = ("" + m_minStdDev);
    
    while (current < options.length)
      options[(current++)] = "";
    return options;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.10 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new RBFNetwork(), argv);
  }
}
