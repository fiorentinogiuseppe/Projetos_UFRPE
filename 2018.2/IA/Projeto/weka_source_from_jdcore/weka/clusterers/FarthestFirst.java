package weka.clusterers;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

































































































public class FarthestFirst
  extends RandomizableClusterer
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = 7499838100631329509L;
  protected Instances m_instances;
  protected ReplaceMissingValues m_ReplaceMissingFilter;
  protected int m_NumClusters = 2;
  


  protected Instances m_ClusterCentroids;
  


  private double[] m_Min;
  


  private double[] m_Max;
  



  public FarthestFirst() {}
  


  public String globalInfo()
  {
    return "Cluster data using the FarthestFirst algorithm.\n\nFor more information see:\n\n" + getTechnicalInformation().toString() + "\n\n" + "Notes:\n" + "- works as a fast simple approximate clusterer\n" + "- modelled after SimpleKMeans, might be a useful initializer for it";
  }
  














  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Hochbaum and Shmoys");
    result.setValue(TechnicalInformation.Field.YEAR, "1985");
    result.setValue(TechnicalInformation.Field.TITLE, "A best possible heuristic for the k-center problem");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Mathematics of Operations Research");
    result.setValue(TechnicalInformation.Field.VOLUME, "10");
    result.setValue(TechnicalInformation.Field.NUMBER, "2");
    result.setValue(TechnicalInformation.Field.PAGES, "180-184");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.INPROCEEDINGS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Sanjoy Dasgupta");
    additional.setValue(TechnicalInformation.Field.TITLE, "Performance Guarantees for Hierarchical Clustering");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "15th Annual Conference on Computational Learning Theory");
    additional.setValue(TechnicalInformation.Field.YEAR, "2002");
    additional.setValue(TechnicalInformation.Field.PAGES, "351-363");
    additional.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    result.enable(Capabilities.Capability.NO_CLASS);
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    
    return result;
  }
  








  public void buildClusterer(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    


    m_ReplaceMissingFilter = new ReplaceMissingValues();
    m_ReplaceMissingFilter.setInputFormat(data);
    m_instances = Filter.useFilter(data, m_ReplaceMissingFilter);
    
    initMinMax(m_instances);
    
    m_ClusterCentroids = new Instances(m_instances, m_NumClusters);
    
    int n = m_instances.numInstances();
    Random r = new Random(getSeed());
    boolean[] selected = new boolean[n];
    double[] minDistance = new double[n];
    
    for (int i = 0; i < n; i++) { minDistance[i] = Double.MAX_VALUE;
    }
    int firstI = r.nextInt(n);
    m_ClusterCentroids.add(m_instances.instance(firstI));
    selected[firstI] = true;
    
    updateMinDistance(minDistance, selected, m_instances, m_instances.instance(firstI));
    
    if (m_NumClusters > n) { m_NumClusters = n;
    }
    for (int i = 1; i < m_NumClusters; i++) {
      int nextI = farthestAway(minDistance, selected);
      m_ClusterCentroids.add(m_instances.instance(nextI));
      selected[nextI] = true;
      updateMinDistance(minDistance, selected, m_instances, m_instances.instance(nextI));
    }
    
    m_instances = new Instances(m_instances, 0);
  }
  



  protected void updateMinDistance(double[] minDistance, boolean[] selected, Instances data, Instance center)
  {
    for (int i = 0; i < selected.length; i++)
      if (selected[i] == 0) {
        double d = distance(center, data.instance(i));
        if (d < minDistance[i])
          minDistance[i] = d;
      }
  }
  
  protected int farthestAway(double[] minDistance, boolean[] selected) {
    double maxDistance = -1.0D;
    int maxI = -1;
    for (int i = 0; i < selected.length; i++)
      if ((selected[i] == 0) && 
        (maxDistance < minDistance[i])) {
        maxDistance = minDistance[i];
        maxI = i;
      }
    return maxI;
  }
  
  protected void initMinMax(Instances data) {
    m_Min = new double[data.numAttributes()];
    m_Max = new double[data.numAttributes()];
    for (int i = 0; i < data.numAttributes(); i++) {
      double tmp43_40 = NaN.0D;m_Max[i] = tmp43_40;m_Min[i] = tmp43_40;
    }
    
    for (int i = 0; i < data.numInstances(); i++) {
      updateMinMax(data.instance(i));
    }
  }
  







  private void updateMinMax(Instance instance)
  {
    for (int j = 0; j < instance.numAttributes(); j++) {
      if (Double.isNaN(m_Min[j])) {
        m_Min[j] = instance.value(j);
        m_Max[j] = instance.value(j);
      }
      else if (instance.value(j) < m_Min[j]) {
        m_Min[j] = instance.value(j);
      }
      else if (instance.value(j) > m_Max[j]) {
        m_Max[j] = instance.value(j);
      }
    }
  }
  








  protected int clusterProcessedInstance(Instance instance)
  {
    double minDist = Double.MAX_VALUE;
    int bestCluster = 0;
    for (int i = 0; i < m_NumClusters; i++) {
      double dist = distance(instance, m_ClusterCentroids.instance(i));
      if (dist < minDist) {
        minDist = dist;
        bestCluster = i;
      }
    }
    return bestCluster;
  }
  







  public int clusterInstance(Instance instance)
    throws Exception
  {
    m_ReplaceMissingFilter.input(instance);
    m_ReplaceMissingFilter.batchFinished();
    Instance inst = m_ReplaceMissingFilter.output();
    
    return clusterProcessedInstance(inst);
  }
  







  protected double distance(Instance first, Instance second)
  {
    double distance = 0.0D;
    

    int p1 = 0;int p2 = 0;
    while ((p1 < first.numValues()) || (p2 < second.numValues())) { int firstI;
      int firstI; if (p1 >= first.numValues()) {
        firstI = m_instances.numAttributes();
      } else
        firstI = first.index(p1);
      int secondI;
      int secondI; if (p2 >= second.numValues()) {
        secondI = m_instances.numAttributes();
      } else {
        secondI = second.index(p2);
      }
      if (firstI == m_instances.classIndex()) {
        p1++;
      }
      else if (secondI == m_instances.classIndex()) {
        p2++;
      } else {
        double diff;
        if (firstI == secondI) {
          double diff = difference(firstI, first.valueSparse(p1), second.valueSparse(p2));
          

          p1++;p2++;
        } else if (firstI > secondI) {
          double diff = difference(secondI, 0.0D, second.valueSparse(p2));
          
          p2++;
        } else {
          diff = difference(firstI, first.valueSparse(p1), 0.0D);
          
          p1++;
        }
        distance += diff * diff;
      }
    }
    return Math.sqrt(distance / m_instances.numAttributes());
  }
  




  protected double difference(int index, double val1, double val2)
  {
    switch (m_instances.attribute(index).type())
    {

    case 1: 
      if ((Instance.isMissingValue(val1)) || (Instance.isMissingValue(val2)) || ((int)val1 != (int)val2))
      {

        return 1.0D;
      }
      return 0.0D;
    


    case 0: 
      if ((Instance.isMissingValue(val1)) || (Instance.isMissingValue(val2)))
      {
        if ((Instance.isMissingValue(val1)) && (Instance.isMissingValue(val2)))
        {
          return 1.0D; }
        double diff;
        double diff;
        if (Instance.isMissingValue(val2)) {
          diff = norm(val1, index);
        } else {
          diff = norm(val2, index);
        }
        if (diff < 0.5D) {
          diff = 1.0D - diff;
        }
        return diff;
      }
      
      return norm(val1, index) - norm(val2, index);
    }
    
    return 0.0D;
  }
  








  protected double norm(double x, int i)
  {
    if ((Double.isNaN(m_Min[i])) || (Utils.eq(m_Max[i], m_Min[i]))) {
      return 0.0D;
    }
    return (x - m_Min[i]) / (m_Max[i] - m_Min[i]);
  }
  






  public int numberOfClusters()
    throws Exception
  {
    return m_NumClusters;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tnumber of clusters. (default = 2).", "N", 1, "-N <num>"));
    


    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    return result.elements();
  }
  




  public String numClustersTipText()
  {
    return "set number of clusters";
  }
  




  public void setNumClusters(int n)
    throws Exception
  {
    if (n < 0) {
      throw new Exception("Number of clusters must be > 0");
    }
    m_NumClusters = n;
  }
  




  public int getNumClusters()
  {
    return m_NumClusters;
  }
  


















  public void setOptions(String[] options)
    throws Exception
  {
    String optionString = Utils.getOption('N', options);
    
    if (optionString.length() != 0) {
      setNumClusters(Integer.parseInt(optionString));
    }
    
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-N");
    result.add("" + getNumClusters());
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public String toString()
  {
    StringBuffer temp = new StringBuffer();
    
    temp.append("\n FarthestFirst\n==============\n");
    
    temp.append("\nCluster centroids:\n");
    for (int i = 0; i < m_NumClusters; i++) {
      temp.append("\nCluster " + i + "\n\t");
      for (int j = 0; j < m_ClusterCentroids.numAttributes(); j++) {
        if (m_ClusterCentroids.attribute(j).isNominal()) {
          temp.append(" " + m_ClusterCentroids.attribute(j).value((int)m_ClusterCentroids.instance(i).value(j)));
        }
        else {
          temp.append(" " + m_ClusterCentroids.instance(i).value(j));
        }
      }
    }
    temp.append("\n\n");
    return temp.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5538 $");
  }
  





  public static void main(String[] argv)
  {
    runClusterer(new FarthestFirst(), argv);
  }
}
