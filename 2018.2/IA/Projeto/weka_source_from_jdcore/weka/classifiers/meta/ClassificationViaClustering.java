package weka.classifiers.meta;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.rules.ZeroR;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;




















































































public class ClassificationViaClustering
  extends Classifier
{
  private static final long serialVersionUID = -5687069451420259135L;
  protected Clusterer m_Clusterer;
  protected Clusterer m_ActualClusterer;
  protected Instances m_OriginalHeader;
  protected Instances m_ClusteringHeader;
  protected double[] m_ClustersToClasses;
  protected Classifier m_ZeroR;
  
  public ClassificationViaClustering()
  {
    m_Clusterer = new SimpleKMeans();
  }
  





  public String globalInfo()
  {
    return "A simple meta-classifier that uses a clusterer for classification. For cluster algorithms that use a fixed number of clusterers, like SimpleKMeans, the user has to make sure that the number of clusters to generate are the same as the number of class labels in the dataset in order to obtain a useful model.\n\nNote: at prediction time, a missing value is returned if no cluster is found for the instance.\n\nThe code is based on the 'clusters to classes' functionality of the weka.clusterers.ClusterEvaluation class by Mark Hall.";
  }
  


















  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    result.addElement(new Option("\tFull name of clusterer.\n\t(default: " + defaultClustererString() + ")", "W", 1, "-W"));
    



    result.addElement(new Option("", "", 0, "\nOptions specific to clusterer " + m_Clusterer.getClass().getName() + ":"));
    


    enm = ((OptionHandler)m_Clusterer).listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    return result.elements();
  }
  








  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-W");
    result.add("" + getClusterer().getClass().getName());
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    if ((getClusterer() instanceof OptionHandler)) {
      result.add("--");
      options = ((OptionHandler)getClusterer()).getOptions();
      for (i = 0; i < options.length; i++) {
        result.add(options[i]);
      }
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  







































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('W', options);
    if (tmpStr.length() > 0)
    {

      setClusterer(AbstractClusterer.forName(tmpStr, null));
      setClusterer(AbstractClusterer.forName(tmpStr, Utils.partitionOptions(options)));

    }
    else
    {
      setClusterer(AbstractClusterer.forName(defaultClustererString(), null));
      setClusterer(AbstractClusterer.forName(defaultClustererString(), Utils.partitionOptions(options)));
    }
  }
  




  protected String defaultClustererString()
  {
    return SimpleKMeans.class.getName();
  }
  





  public String clustererTipText()
  {
    return "The clusterer to be used.";
  }
  




  public void setClusterer(Clusterer value)
  {
    m_Clusterer = value;
  }
  




  public Clusterer getClusterer()
  {
    return m_Clusterer;
  }
  




  public double classifyInstance(Instance instance)
    throws Exception
  {
    double result;
    


    double result;
    


    if (m_ZeroR != null) {
      result = m_ZeroR.classifyInstance(instance);

    }
    else if (m_ActualClusterer != null)
    {
      double[] values = new double[m_ClusteringHeader.numAttributes()];
      int n = 0;
      for (int i = 0; i < instance.numAttributes(); i++)
        if (i != instance.classIndex())
        {
          values[n] = instance.value(i);
          n++;
        }
      Instance newInst = new Instance(instance.weight(), values);
      newInst.setDataset(m_ClusteringHeader);
      

      double result = m_ClustersToClasses[m_ActualClusterer.clusterInstance(newInst)];
      if (result == -1.0D) {
        result = Instance.missingValue();
      }
    } else {
      result = Instance.missingValue();
    }
    

    return result;
  }
  






  public Capabilities getCapabilities()
  {
    Capabilities result = m_Clusterer.getCapabilities();
    

    result.disableAllClasses();
    result.disable(Capabilities.Capability.NO_CLASS);
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    
    return result;
  }
  















  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    

    m_OriginalHeader = new Instances(data, 0);
    

    Instances clusterData = new Instances(data);
    clusterData.setClassIndex(-1);
    clusterData.deleteAttributeAt(m_OriginalHeader.classIndex());
    m_ClusteringHeader = new Instances(clusterData, 0);
    
    if (m_ClusteringHeader.numAttributes() == 0) {
      System.err.println("Data contains only class attribute, defaulting to ZeroR model.");
      m_ZeroR = new ZeroR();
      m_ZeroR.buildClassifier(data);
    }
    else {
      m_ZeroR = null;
      

      m_ActualClusterer = AbstractClusterer.makeCopy(m_Clusterer);
      m_ActualClusterer.buildClusterer(clusterData);
      

      ClusterEvaluation eval = new ClusterEvaluation();
      eval.setClusterer(m_ActualClusterer);
      eval.evaluateClusterer(clusterData);
      double[] clusterAssignments = eval.getClusterAssignments();
      

      int[][] counts = new int[eval.getNumClusters()][m_OriginalHeader.numClasses()];
      int[] clusterTotals = new int[eval.getNumClusters()];
      double[] best = new double[eval.getNumClusters() + 1];
      double[] current = new double[eval.getNumClusters() + 1];
      for (int i = 0; i < data.numInstances(); i++) {
        Instance instance = data.instance(i);
        counts[((int)clusterAssignments[i])][((int)instance.classValue())] += 1;
        clusterTotals[((int)clusterAssignments[i])] += 1;
        i++;
      }
      best[eval.getNumClusters()] = Double.MAX_VALUE;
      ClusterEvaluation.mapClasses(eval.getNumClusters(), 0, counts, clusterTotals, current, best, 0);
      m_ClustersToClasses = new double[best.length];
      System.arraycopy(best, 0, m_ClustersToClasses, 0, best.length);
    }
  }
  









  public String toString()
  {
    StringBuffer result = new StringBuffer();
    

    result.append(getClass().getName().replaceAll(".*\\.", "") + "\n");
    result.append(getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n");
    

    if (m_ActualClusterer != null)
    {
      result.append(m_ActualClusterer + "\n");
      

      result.append("Clusters to classes mapping:\n");
      for (int i = 0; i < m_ClustersToClasses.length - 1; i++) {
        result.append("  " + (i + 1) + ". Cluster: ");
        if (m_ClustersToClasses[i] < 0.0D) {
          result.append("no class");
        } else {
          result.append(m_OriginalHeader.classAttribute().value((int)m_ClustersToClasses[i]) + " (" + ((int)m_ClustersToClasses[i] + 1) + ")");
        }
        
        result.append("\n");
      }
      result.append("\n");
      

      result.append("Classes to clusters mapping:\n");
      for (i = 0; i < m_OriginalHeader.numClasses(); i++) {
        result.append("  " + (i + 1) + ". Class (" + m_OriginalHeader.classAttribute().value(i) + "): ");
        


        boolean found = false;
        for (int n = 0; n < m_ClustersToClasses.length - 1; n++) {
          if ((int)m_ClustersToClasses[n] == i) {
            found = true;
            result.append(n + 1 + ". Cluster");
            break;
          }
        }
        
        if (!found) {
          result.append("no cluster");
        }
        result.append("\n");
      }
      
      result.append("\n");
    }
    else {
      result.append("no model built yet\n");
    }
    
    return result.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new ClassificationViaClustering(), args);
  }
}
