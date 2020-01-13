package weka.clusterers;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.estimators.DiscreteEstimator;
import weka.experiment.Stats;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;











































































public class MakeDensityBasedClusterer
  extends AbstractDensityBasedClusterer
  implements NumberOfClustersRequestable, OptionHandler, WeightedInstancesHandler
{
  static final long serialVersionUID = -5643302427972186631L;
  private Instances m_theInstances;
  private double[] m_priors;
  private double[][][] m_modelNormal;
  private DiscreteEstimator[][] m_model;
  private double m_minStdDev = 1.0E-6D;
  
  private Clusterer m_wrappedClusterer = new SimpleKMeans();
  



  private ReplaceMissingValues m_replaceMissing;
  




  public MakeDensityBasedClusterer() {}
  




  public MakeDensityBasedClusterer(Clusterer toWrap)
  {
    setClusterer(toWrap);
  }
  




  public String globalInfo()
  {
    return "Class for wrapping a Clusterer to make it return a distribution and density. Fits normal distributions and discrete distributions within each cluster produced by the wrapped clusterer. Supports the NumberOfClustersRequestable interface only if the wrapped Clusterer does.";
  }
  









  protected String defaultClustererString()
  {
    return SimpleKMeans.class.getName();
  }
  





  public void setNumClusters(int n)
    throws Exception
  {
    if (m_wrappedClusterer == null) {
      throw new Exception("Can't set the number of clusters to generate - no clusterer has been set yet.");
    }
    
    if (!(m_wrappedClusterer instanceof NumberOfClustersRequestable)) {
      throw new Exception("Can't set the number of clusters to generate - wrapped clusterer does not support this facility.");
    }
    

    ((NumberOfClustersRequestable)m_wrappedClusterer).setNumClusters(n);
  }
  





  public Capabilities getCapabilities()
  {
    if (m_wrappedClusterer != null) {
      return m_wrappedClusterer.getCapabilities();
    }
    Capabilities result = super.getCapabilities();
    result.disableAll();
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  





  public void buildClusterer(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    
    m_replaceMissing = new ReplaceMissingValues();
    m_replaceMissing.setInputFormat(data);
    data = Filter.useFilter(data, m_replaceMissing);
    
    m_theInstances = new Instances(data, 0);
    if (m_wrappedClusterer == null) {
      throw new Exception("No clusterer has been set");
    }
    m_wrappedClusterer.buildClusterer(data);
    m_model = new DiscreteEstimator[m_wrappedClusterer.numberOfClusters()][data.numAttributes()];
    
    m_modelNormal = new double[m_wrappedClusterer.numberOfClusters()][data.numAttributes()][2];
    
    double[][] weights = new double[m_wrappedClusterer.numberOfClusters()][data.numAttributes()];
    m_priors = new double[m_wrappedClusterer.numberOfClusters()];
    for (int i = 0; i < m_wrappedClusterer.numberOfClusters(); i++) {
      m_priors[i] = 1.0D;
      for (int j = 0; j < data.numAttributes(); j++) {
        if (data.attribute(j).isNominal()) {
          m_model[i][j] = new DiscreteEstimator(data.attribute(j).numValues(), true);
        }
      }
    }
    

    Instance inst = null;
    

    int[] clusterIndex = new int[data.numInstances()];
    for (int i = 0; i < data.numInstances(); i++) {
      inst = data.instance(i);
      int cluster = m_wrappedClusterer.clusterInstance(inst);
      m_priors[cluster] += inst.weight();
      for (int j = 0; j < data.numAttributes(); j++) {
        if (!inst.isMissing(j)) {
          if (data.attribute(j).isNominal()) {
            m_model[cluster][j].addValue(inst.value(j), inst.weight());
          } else {
            m_modelNormal[cluster][j][0] += inst.weight() * inst.value(j);
            weights[cluster][j] += inst.weight();
          }
        }
      }
      clusterIndex[i] = cluster;
    }
    
    for (int j = 0; j < data.numAttributes(); j++) {
      if (data.attribute(j).isNumeric()) {
        for (int i = 0; i < m_wrappedClusterer.numberOfClusters(); i++) {
          if (weights[i][j] > 0.0D) {
            m_modelNormal[i][j][0] /= weights[i][j];
          }
        }
      }
    }
    

    for (int i = 0; i < data.numInstances(); i++) {
      inst = data.instance(i);
      for (int j = 0; j < data.numAttributes(); j++) {
        if ((!inst.isMissing(j)) && 
          (data.attribute(j).isNumeric())) {
          double diff = m_modelNormal[clusterIndex[i]][j][0] - inst.value(j);
          m_modelNormal[clusterIndex[i]][j][1] += inst.weight() * diff * diff;
        }
      }
    }
    

    for (int j = 0; j < data.numAttributes(); j++) {
      if (data.attribute(j).isNumeric()) {
        for (int i = 0; i < m_wrappedClusterer.numberOfClusters(); i++) {
          if (weights[i][j] > 0.0D) {
            m_modelNormal[i][j][1] = Math.sqrt(m_modelNormal[i][j][1] / weights[i][j]);
          }
          else if (weights[i][j] <= 0.0D) {
            m_modelNormal[i][j][1] = Double.MAX_VALUE;
          }
          if (m_modelNormal[i][j][1] <= m_minStdDev) {
            m_modelNormal[i][j][1] = attributeStatsnumericStats.stdDev;
            if (m_modelNormal[i][j][1] <= m_minStdDev) {
              m_modelNormal[i][j][1] = m_minStdDev;
            }
          }
        }
      }
    }
    
    Utils.normalize(m_priors);
  }
  





  public double[] clusterPriors()
  {
    double[] n = new double[m_priors.length];
    
    System.arraycopy(m_priors, 0, n, 0, n.length);
    return n;
  }
  









  public double[] logDensityPerClusterForInstance(Instance inst)
    throws Exception
  {
    double[] wghts = new double[m_wrappedClusterer.numberOfClusters()];
    
    m_replaceMissing.input(inst);
    inst = m_replaceMissing.output();
    
    for (int i = 0; i < m_wrappedClusterer.numberOfClusters(); i++) {
      double logprob = 0.0D;
      for (int j = 0; j < inst.numAttributes(); j++) {
        if (!inst.isMissing(j)) {
          if (inst.attribute(j).isNominal()) {
            logprob += Math.log(m_model[i][j].getProbability(inst.value(j)));
          } else {
            logprob += logNormalDens(inst.value(j), m_modelNormal[i][j][0], m_modelNormal[i][j][1]);
          }
        }
      }
      

      wghts[i] = logprob;
    }
    return wghts;
  }
  

  private static double m_normConst = 0.5D * Math.log(6.283185307179586D);
  







  private double logNormalDens(double x, double mean, double stdDev)
  {
    double diff = x - mean;
    
    return -(diff * diff / (2.0D * stdDev * stdDev)) - m_normConst - Math.log(stdDev);
  }
  





  public int numberOfClusters()
    throws Exception
  {
    return m_wrappedClusterer.numberOfClusters();
  }
  




  public String toString()
  {
    if (m_priors == null) {
      return "No clusterer built yet!";
    }
    
    StringBuffer text = new StringBuffer();
    text.append("MakeDensityBasedClusterer: \n\nWrapped clusterer: " + m_wrappedClusterer.toString());
    

    text.append("\nFitted estimators (with ML estimates of variance):\n");
    
    for (int j = 0; j < m_priors.length; j++) {
      text.append("\nCluster: " + j + " Prior probability: " + Utils.doubleToString(m_priors[j], 4) + "\n\n");
      

      for (int i = 0; i < m_model[0].length; i++) {
        text.append("Attribute: " + m_theInstances.attribute(i).name() + "\n");
        
        if (m_theInstances.attribute(i).isNominal()) {
          if (m_model[j][i] != null) {
            text.append(m_model[j][i].toString());
          }
        }
        else {
          text.append("Normal Distribution. Mean = " + Utils.doubleToString(m_modelNormal[j][i][0], 4) + " StdDev = " + Utils.doubleToString(m_modelNormal[j][i][1], 4) + "\n");
        }
      }
    }
    




    return text.toString();
  }
  




  public String clustererTipText()
  {
    return "the clusterer to wrap";
  }
  





  public void setClusterer(Clusterer toWrap)
  {
    m_wrappedClusterer = toWrap;
  }
  





  public Clusterer getClusterer()
  {
    return m_wrappedClusterer;
  }
  




  public String minStdDevTipText()
  {
    return "set minimum allowable standard deviation";
  }
  







  public void setMinStdDev(double m)
  {
    m_minStdDev = m;
  }
  



  public double getMinStdDev()
  {
    return m_minStdDev;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tminimum allowable standard deviation for normal density computation \n\t(default 1e-6)", "M", 1, "-M <num>"));
    



    result.addElement(new Option("\tClusterer to wrap.\n\t(default " + defaultClustererString() + ")", "W", 1, "-W <clusterer name>"));
    



    if ((m_wrappedClusterer != null) && ((m_wrappedClusterer instanceof OptionHandler)))
    {
      result.addElement(new Option("", "", 0, "\nOptions specific to clusterer " + m_wrappedClusterer.getClass().getName() + ":"));
      


      Enumeration enu = ((OptionHandler)m_wrappedClusterer).listOptions();
      while (enu.hasMoreElements()) {
        result.addElement(enu.nextElement());
      }
    }
    
    return result.elements();
  }
  






































  public void setOptions(String[] options)
    throws Exception
  {
    String optionString = Utils.getOption('M', options);
    if (optionString.length() != 0) {
      setMinStdDev(new Double(optionString).doubleValue());
    } else {
      setMinStdDev(1.0E-6D);
    }
    String wString = Utils.getOption('W', options);
    if (wString.length() == 0)
      wString = defaultClustererString();
    setClusterer(AbstractClusterer.forName(wString, Utils.partitionOptions(options)));
  }
  





  public String[] getOptions()
  {
    String[] clustererOptions = new String[0];
    if ((m_wrappedClusterer != null) && ((m_wrappedClusterer instanceof OptionHandler)))
    {
      clustererOptions = ((OptionHandler)m_wrappedClusterer).getOptions();
    }
    String[] options = new String[clustererOptions.length + 5];
    int current = 0;
    
    options[(current++)] = "-M";
    options[(current++)] = ("" + getMinStdDev());
    
    if (getClusterer() != null) {
      options[(current++)] = "-W";
      options[(current++)] = getClusterer().getClass().getName();
    }
    options[(current++)] = "--";
    
    System.arraycopy(clustererOptions, 0, options, current, clustererOptions.length);
    
    current += clustererOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5538 $");
  }
  




  public static void main(String[] argv)
  {
    runClusterer(new MakeDensityBasedClusterer(), argv);
  }
}
