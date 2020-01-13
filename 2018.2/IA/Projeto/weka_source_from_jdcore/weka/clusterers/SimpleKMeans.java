package weka.clusterers;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.rules.DecisionTableHashKey;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ManhattanDistance;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;




















































































public class SimpleKMeans
  extends RandomizableClusterer
  implements NumberOfClustersRequestable, WeightedInstancesHandler
{
  static final long serialVersionUID = -3235809600124455376L;
  private ReplaceMissingValues m_ReplaceMissingFilter;
  private int m_NumClusters = 2;
  


  private Instances m_ClusterCentroids;
  


  private Instances m_ClusterStdDevs;
  


  private int[][][] m_ClusterNominalCounts;
  


  private int[][] m_ClusterMissingCounts;
  


  private double[] m_FullMeansOrMediansOrModes;
  


  private double[] m_FullStdDevs;
  


  private int[][] m_FullNominalCounts;
  


  private int[] m_FullMissingCounts;
  

  private boolean m_displayStdDevs;
  

  private boolean m_dontReplaceMissing = false;
  



  private int[] m_ClusterSizes;
  



  private int m_MaxIterations = 500;
  



  private int m_Iterations = 0;
  


  private double[] m_squaredErrors;
  


  protected DistanceFunction m_DistanceFunction = new EuclideanDistance();
  



  private boolean m_PreserveOrder = false;
  



  protected int[] m_Assignments = null;
  




  public SimpleKMeans()
  {
    m_SeedDefault = 10;
    setSeed(m_SeedDefault);
  }
  





  public String globalInfo()
  {
    return "Cluster data using the k means algorithm. Can use either the Euclidean distance (default) or the Manhattan distance. If the Manhattan distance is used, then centroids are computed as the component-wise median rather than mean.";
  }
  








  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    result.enable(Capabilities.Capability.NO_CLASS);
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    
    return result;
  }
  








  public void buildClusterer(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    
    m_Iterations = 0;
    
    m_ReplaceMissingFilter = new ReplaceMissingValues();
    Instances instances = new Instances(data);
    
    instances.setClassIndex(-1);
    if (!m_dontReplaceMissing) {
      m_ReplaceMissingFilter.setInputFormat(instances);
      instances = Filter.useFilter(instances, m_ReplaceMissingFilter);
    }
    
    m_FullMissingCounts = new int[instances.numAttributes()];
    if (m_displayStdDevs) {
      m_FullStdDevs = new double[instances.numAttributes()];
    }
    m_FullNominalCounts = new int[instances.numAttributes()][0];
    
    m_FullMeansOrMediansOrModes = moveCentroid(0, instances, false);
    for (int i = 0; i < instances.numAttributes(); i++) {
      m_FullMissingCounts[i] = attributeStatsmissingCount;
      if (instances.attribute(i).isNumeric()) {
        if (m_displayStdDevs) {
          m_FullStdDevs[i] = Math.sqrt(instances.variance(i));
        }
        if (m_FullMissingCounts[i] == instances.numInstances()) {
          m_FullMeansOrMediansOrModes[i] = NaN.0D;
        }
      } else {
        m_FullNominalCounts[i] = attributeStatsnominalCounts;
        if (m_FullMissingCounts[i] > m_FullNominalCounts[i][Utils.maxIndex(m_FullNominalCounts[i])])
        {
          m_FullMeansOrMediansOrModes[i] = -1.0D;
        }
      }
    }
    

    m_ClusterCentroids = new Instances(instances, m_NumClusters);
    int[] clusterAssignments = new int[instances.numInstances()];
    
    if (m_PreserveOrder) {
      m_Assignments = clusterAssignments;
    }
    
    m_DistanceFunction.setInstances(instances);
    
    Random RandomO = new Random(getSeed());
    
    HashMap initC = new HashMap();
    DecisionTableHashKey hk = null;
    
    Instances initInstances = null;
    if (m_PreserveOrder) {
      initInstances = new Instances(instances);
    } else {
      initInstances = instances;
    }
    
    for (int j = initInstances.numInstances() - 1; j >= 0; j--) {
      int instIndex = RandomO.nextInt(j + 1);
      hk = new DecisionTableHashKey(initInstances.instance(instIndex), initInstances.numAttributes(), true);
      
      if (!initC.containsKey(hk)) {
        m_ClusterCentroids.add(initInstances.instance(instIndex));
        initC.put(hk, null);
      }
      initInstances.swap(j, instIndex);
      
      if (m_ClusterCentroids.numInstances() == m_NumClusters) {
        break;
      }
    }
    
    m_NumClusters = m_ClusterCentroids.numInstances();
    

    initInstances = null;
    

    boolean converged = false;
    
    Instances[] tempI = new Instances[m_NumClusters];
    m_squaredErrors = new double[m_NumClusters];
    m_ClusterNominalCounts = new int[m_NumClusters][instances.numAttributes()][0];
    m_ClusterMissingCounts = new int[m_NumClusters][instances.numAttributes()];
    while (!converged) {
      int emptyClusterCount = 0;
      m_Iterations += 1;
      converged = true;
      for (int i = 0; i < instances.numInstances(); i++) {
        Instance toCluster = instances.instance(i);
        int newC = clusterProcessedInstance(toCluster, true);
        if (newC != clusterAssignments[i]) {
          converged = false;
        }
        clusterAssignments[i] = newC;
      }
      

      m_ClusterCentroids = new Instances(instances, m_NumClusters);
      for (i = 0; i < m_NumClusters; i++) {
        tempI[i] = new Instances(instances, 0);
      }
      for (i = 0; i < instances.numInstances(); i++) {
        tempI[clusterAssignments[i]].add(instances.instance(i));
      }
      for (i = 0; i < m_NumClusters; i++) {
        if (tempI[i].numInstances() == 0)
        {
          emptyClusterCount++;
        } else {
          moveCentroid(i, tempI[i], true);
        }
      }
      
      if (m_Iterations == m_MaxIterations) {
        converged = true;
      }
      
      if (emptyClusterCount > 0) {
        m_NumClusters -= emptyClusterCount;
        if (converged) {
          Instances[] t = new Instances[m_NumClusters];
          int index = 0;
          for (int k = 0; k < tempI.length; k++) {
            if (tempI[k].numInstances() > 0) {
              t[index] = tempI[k];
              
              for (i = 0; i < tempI[k].numAttributes(); i++) {
                m_ClusterNominalCounts[index][i] = m_ClusterNominalCounts[k][i];
              }
              index++;
            }
          }
          tempI = t;
        } else {
          tempI = new Instances[m_NumClusters];
        }
      }
      
      if (!converged) {
        m_squaredErrors = new double[m_NumClusters];
        m_ClusterNominalCounts = new int[m_NumClusters][instances.numAttributes()][0];
      }
    }
    

    if (m_displayStdDevs) {
      m_ClusterStdDevs = new Instances(instances, m_NumClusters);
    }
    m_ClusterSizes = new int[m_NumClusters];
    for (int i = 0; i < m_NumClusters; i++) {
      if (m_displayStdDevs) {
        double[] vals2 = new double[instances.numAttributes()];
        for (int j = 0; j < instances.numAttributes(); j++) {
          if (instances.attribute(j).isNumeric()) {
            vals2[j] = Math.sqrt(tempI[i].variance(j));
          } else {
            vals2[j] = Instance.missingValue();
          }
        }
        m_ClusterStdDevs.add(new Instance(1.0D, vals2));
      }
      m_ClusterSizes[i] = tempI[i].numInstances();
    }
    

    m_DistanceFunction.clean();
  }
  













  protected double[] moveCentroid(int centroidIndex, Instances members, boolean updateClusterInfo)
  {
    double[] vals = new double[members.numAttributes()];
    

    Instances sortedMembers = null;
    int middle = 0;
    boolean dataIsEven = false;
    
    if ((m_DistanceFunction instanceof ManhattanDistance)) {
      middle = (members.numInstances() - 1) / 2;
      dataIsEven = members.numInstances() % 2 == 0;
      if (m_PreserveOrder) {
        sortedMembers = members;
      } else {
        sortedMembers = new Instances(members);
      }
    }
    
    for (int j = 0; j < members.numAttributes(); j++)
    {



      if (((m_DistanceFunction instanceof EuclideanDistance)) || (members.attribute(j).isNominal()))
      {
        vals[j] = members.meanOrMode(j);
      } else if ((m_DistanceFunction instanceof ManhattanDistance))
      {
        if (members.numInstances() == 1) {
          vals[j] = members.instance(0).value(j);
        } else {
          vals[j] = sortedMembers.kthSmallestValue(j, middle + 1);
          if (dataIsEven) {
            vals[j] = ((vals[j] + sortedMembers.kthSmallestValue(j, middle + 2)) / 2.0D);
          }
        }
      }
      
      if (updateClusterInfo) {
        m_ClusterMissingCounts[centroidIndex][j] = attributeStatsmissingCount;
        m_ClusterNominalCounts[centroidIndex][j] = attributeStatsnominalCounts;
        if (members.attribute(j).isNominal()) {
          if (m_ClusterMissingCounts[centroidIndex][j] > m_ClusterNominalCounts[centroidIndex][j][Utils.maxIndex(m_ClusterNominalCounts[centroidIndex][j])])
          {
            vals[j] = Instance.missingValue();
          }
        }
        else if (m_ClusterMissingCounts[centroidIndex][j] == members.numInstances())
        {
          vals[j] = Instance.missingValue();
        }
      }
    }
    
    if (updateClusterInfo) {
      m_ClusterCentroids.add(new Instance(1.0D, vals));
    }
    return vals;
  }
  






  private int clusterProcessedInstance(Instance instance, boolean updateErrors)
  {
    double minDist = 2.147483647E9D;
    int bestCluster = 0;
    for (int i = 0; i < m_NumClusters; i++) {
      double dist = m_DistanceFunction.distance(instance, m_ClusterCentroids.instance(i));
      
      if (dist < minDist) {
        minDist = dist;
        bestCluster = i;
      }
    }
    if (updateErrors) {
      if ((m_DistanceFunction instanceof EuclideanDistance))
      {
        minDist *= minDist;
      }
      m_squaredErrors[bestCluster] += minDist;
    }
    return bestCluster;
  }
  







  public int clusterInstance(Instance instance)
    throws Exception
  {
    Instance inst = null;
    if (!m_dontReplaceMissing) {
      m_ReplaceMissingFilter.input(instance);
      m_ReplaceMissingFilter.batchFinished();
      inst = m_ReplaceMissingFilter.output();
    } else {
      inst = instance;
    }
    
    return clusterProcessedInstance(inst, false);
  }
  





  public int numberOfClusters()
    throws Exception
  {
    return m_NumClusters;
  }
  





  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tnumber of clusters.\n\t(default 2).", "N", 1, "-N <num>"));
    
    result.addElement(new Option("\tDisplay std. deviations for centroids.\n", "V", 0, "-V"));
    
    result.addElement(new Option("\tDon't replace missing values with mean/mode.\n", "M", 0, "-M"));
    

    result.add(new Option("\tDistance function to use.\n\t(default: weka.core.EuclideanDistance)", "A", 1, "-A <classname and options>"));
    


    result.add(new Option("\tMaximum number of iterations.\n", "I", 1, "-I <num>"));
    

    result.addElement(new Option("\tPreserve order of instances.\n", "O", 0, "-O"));
    

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
    if (n <= 0) {
      throw new Exception("Number of clusters must be > 0");
    }
    m_NumClusters = n;
  }
  




  public int getNumClusters()
  {
    return m_NumClusters;
  }
  





  public String maxIterationsTipText()
  {
    return "set maximum number of iterations";
  }
  




  public void setMaxIterations(int n)
    throws Exception
  {
    if (n <= 0) {
      throw new Exception("Maximum number of iterations must be > 0");
    }
    m_MaxIterations = n;
  }
  




  public int getMaxIterations()
  {
    return m_MaxIterations;
  }
  





  public String displayStdDevsTipText()
  {
    return "Display std deviations of numeric attributes and counts of nominal attributes.";
  }
  






  public void setDisplayStdDevs(boolean stdD)
  {
    m_displayStdDevs = stdD;
  }
  





  public boolean getDisplayStdDevs()
  {
    return m_displayStdDevs;
  }
  





  public String dontReplaceMissingValuesTipText()
  {
    return "Replace missing values globally with mean/mode.";
  }
  




  public void setDontReplaceMissingValues(boolean r)
  {
    m_dontReplaceMissing = r;
  }
  




  public boolean getDontReplaceMissingValues()
  {
    return m_dontReplaceMissing;
  }
  





  public String distanceFunctionTipText()
  {
    return "The distance function to use for instances comparison (default: weka.core.EuclideanDistance). ";
  }
  





  public DistanceFunction getDistanceFunction()
  {
    return m_DistanceFunction;
  }
  




  public void setDistanceFunction(DistanceFunction df)
    throws Exception
  {
    if ((!(df instanceof EuclideanDistance)) && (!(df instanceof ManhattanDistance)))
    {
      throw new Exception("SimpleKMeans currently only supports the Euclidean and Manhattan distances.");
    }
    
    m_DistanceFunction = df;
  }
  





  public String preserveInstancesOrderTipText()
  {
    return "Preserve order of instances.";
  }
  




  public void setPreserveInstancesOrder(boolean r)
  {
    m_PreserveOrder = r;
  }
  




  public boolean getPreserveInstancesOrder()
  {
    return m_PreserveOrder;
  }
  


















































  public void setOptions(String[] options)
    throws Exception
  {
    m_displayStdDevs = Utils.getFlag("V", options);
    m_dontReplaceMissing = Utils.getFlag("M", options);
    
    String optionString = Utils.getOption('N', options);
    
    if (optionString.length() != 0) {
      setNumClusters(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption("I", options);
    if (optionString.length() != 0) {
      setMaxIterations(Integer.parseInt(optionString));
    }
    
    String distFunctionClass = Utils.getOption('A', options);
    if (distFunctionClass.length() != 0) {
      String[] distFunctionClassSpec = Utils.splitOptions(distFunctionClass);
      if (distFunctionClassSpec.length == 0) {
        throw new Exception("Invalid DistanceFunction specification string.");
      }
      String className = distFunctionClassSpec[0];
      distFunctionClassSpec[0] = "";
      
      setDistanceFunction((DistanceFunction)Utils.forName(DistanceFunction.class, className, distFunctionClassSpec));
    }
    else {
      setDistanceFunction(new EuclideanDistance());
    }
    
    m_PreserveOrder = Utils.getFlag("O", options);
    
    super.setOptions(options);
  }
  









  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (m_displayStdDevs) {
      result.add("-V");
    }
    
    if (m_dontReplaceMissing) {
      result.add("-M");
    }
    
    result.add("-N");
    result.add("" + getNumClusters());
    
    result.add("-A");
    result.add((m_DistanceFunction.getClass().getName() + " " + Utils.joinOptions(m_DistanceFunction.getOptions())).trim());
    

    result.add("-I");
    result.add("" + getMaxIterations());
    
    if (m_PreserveOrder) {
      result.add("-O");
    }
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String toString()
  {
    if (m_ClusterCentroids == null) {
      return "No clusterer built yet!";
    }
    
    int maxWidth = 0;
    int maxAttWidth = 0;
    boolean containsNumeric = false;
    for (int i = 0; i < m_NumClusters; i++) {
      for (int j = 0; j < m_ClusterCentroids.numAttributes(); j++) {
        if (m_ClusterCentroids.attribute(j).name().length() > maxAttWidth) {
          maxAttWidth = m_ClusterCentroids.attribute(j).name().length();
        }
        if (m_ClusterCentroids.attribute(j).isNumeric()) {
          containsNumeric = true;
          double width = Math.log(Math.abs(m_ClusterCentroids.instance(i).value(j))) / Math.log(10.0D);
          

          if (width < 0.0D) {
            width = 1.0D;
          }
          
          width += 6.0D;
          if ((int)width > maxWidth) {
            maxWidth = (int)width;
          }
        }
      }
    }
    
    for (int i = 0; i < m_ClusterCentroids.numAttributes(); i++) {
      if (m_ClusterCentroids.attribute(i).isNominal()) {
        Attribute a = m_ClusterCentroids.attribute(i);
        for (int j = 0; j < m_ClusterCentroids.numInstances(); j++) {
          String val = a.value((int)m_ClusterCentroids.instance(j).value(i));
          if (val.length() > maxWidth) {
            maxWidth = val.length();
          }
        }
        for (int j = 0; j < a.numValues(); j++) {
          String val = a.value(j) + " ";
          if (val.length() > maxAttWidth) {
            maxAttWidth = val.length();
          }
        }
      }
    }
    
    if (m_displayStdDevs)
    {
      for (int i = 0; i < m_ClusterCentroids.numAttributes(); i++) {
        if (m_ClusterCentroids.attribute(i).isNominal()) {
          int maxV = Utils.maxIndex(m_FullNominalCounts[i]);
          



          int percent = 6;
          String nomV = "" + m_FullNominalCounts[i][maxV];
          
          if (nomV.length() + percent > maxWidth) {
            maxWidth = nomV.length() + 1;
          }
        }
      }
    }
    

    for (int m_ClusterSize : m_ClusterSizes) {
      String size = "(" + m_ClusterSize + ")";
      if (size.length() > maxWidth) {
        maxWidth = size.length();
      }
    }
    
    if ((m_displayStdDevs) && (maxAttWidth < "missing".length())) {
      maxAttWidth = "missing".length();
    }
    
    String plusMinus = "+/-";
    maxAttWidth += 2;
    if ((m_displayStdDevs) && (containsNumeric)) {
      maxWidth += plusMinus.length();
    }
    if (maxAttWidth < "Attribute".length() + 2) {
      maxAttWidth = "Attribute".length() + 2;
    }
    
    if (maxWidth < "Full Data".length()) {
      maxWidth = "Full Data".length() + 1;
    }
    
    if (maxWidth < "missing".length()) {
      maxWidth = "missing".length() + 1;
    }
    
    StringBuffer temp = new StringBuffer();
    




    temp.append("\nkMeans\n======\n");
    temp.append("\nNumber of iterations: " + m_Iterations + "\n");
    
    if ((m_DistanceFunction instanceof EuclideanDistance)) {
      temp.append("Within cluster sum of squared errors: " + Utils.sum(m_squaredErrors));
    }
    else {
      temp.append("Sum of within cluster distances: " + Utils.sum(m_squaredErrors));
    }
    

    if (!m_dontReplaceMissing) {
      temp.append("\nMissing values globally replaced with mean/mode");
    }
    
    temp.append("\n\nCluster centroids:\n");
    temp.append(pad("Cluster#", " ", maxAttWidth + (maxWidth * 2 + 2) - "Cluster#".length(), true));
    

    temp.append("\n");
    temp.append(pad("Attribute", " ", maxAttWidth - "Attribute".length(), false));
    

    temp.append(pad("Full Data", " ", maxWidth + 1 - "Full Data".length(), true));
    


    for (int i = 0; i < m_NumClusters; i++) {
      String clustNum = "" + i;
      temp.append(pad(clustNum, " ", maxWidth + 1 - clustNum.length(), true));
    }
    temp.append("\n");
    

    String cSize = "(" + Utils.sum(m_ClusterSizes) + ")";
    temp.append(pad(cSize, " ", maxAttWidth + maxWidth + 1 - cSize.length(), true));
    
    for (int i = 0; i < m_NumClusters; i++) {
      cSize = "(" + m_ClusterSizes[i] + ")";
      temp.append(pad(cSize, " ", maxWidth + 1 - cSize.length(), true));
    }
    temp.append("\n");
    
    temp.append(pad("", "=", maxAttWidth + (maxWidth * (m_ClusterCentroids.numInstances() + 1) + m_ClusterCentroids.numInstances() + 1), true));
    


    temp.append("\n");
    
    for (int i = 0; i < m_ClusterCentroids.numAttributes(); i++) {
      String attName = m_ClusterCentroids.attribute(i).name();
      temp.append(attName);
      for (int j = 0; j < maxAttWidth - attName.length(); j++) {
        temp.append(" ");
      }
      
      String valMeanMode;
      
      String valMeanMode;
      if (m_ClusterCentroids.attribute(i).isNominal()) { String valMeanMode;
        if (m_FullMeansOrMediansOrModes[i] == -1.0D) {
          valMeanMode = pad("missing", " ", maxWidth + 1 - "missing".length(), true);
        } else {
          String strVal;
          valMeanMode = pad(strVal = m_ClusterCentroids.attribute(i).value((int)m_FullMeansOrMediansOrModes[i]), " ", maxWidth + 1 - strVal.length(), true);
        }
      }
      else
      {
        String valMeanMode;
        if (Double.isNaN(m_FullMeansOrMediansOrModes[i])) {
          valMeanMode = pad("missing", " ", maxWidth + 1 - "missing".length(), true);
        } else {
          String strVal;
          valMeanMode = pad(strVal = Utils.doubleToString(m_FullMeansOrMediansOrModes[i], maxWidth, 4).trim(), " ", maxWidth + 1 - strVal.length(), true);
        }
      }
      

      temp.append(valMeanMode);
      
      for (int j = 0; j < m_NumClusters; j++) {
        if (m_ClusterCentroids.attribute(i).isNominal()) {
          if (m_ClusterCentroids.instance(j).isMissing(i)) {
            valMeanMode = pad("missing", " ", maxWidth + 1 - "missing".length(), true);
          } else {
            String strVal;
            valMeanMode = pad(strVal = m_ClusterCentroids.attribute(i).value((int)m_ClusterCentroids.instance(j).value(i)), " ", maxWidth + 1 - strVal.length(), true);

          }
          

        }
        else if (m_ClusterCentroids.instance(j).isMissing(i)) {
          valMeanMode = pad("missing", " ", maxWidth + 1 - "missing".length(), true);
        } else {
          String strVal;
          valMeanMode = pad(strVal = Utils.doubleToString(m_ClusterCentroids.instance(j).value(i), maxWidth, 4).trim(), " ", maxWidth + 1 - strVal.length(), true);
        }
        



        temp.append(valMeanMode);
      }
      temp.append("\n");
      
      if (m_displayStdDevs)
      {
        String stdDevVal = "";
        
        if (m_ClusterCentroids.attribute(i).isNominal())
        {
          Attribute a = m_ClusterCentroids.attribute(i);
          for (int j = 0; j < a.numValues(); j++)
          {
            String val = "  " + a.value(j);
            temp.append(pad(val, " ", maxAttWidth + 1 - val.length(), false));
            int count = m_FullNominalCounts[i][j];
            int percent = (int)(m_FullNominalCounts[i][j] / Utils.sum(m_ClusterSizes) * 100.0D);
            
            String percentS = "" + percent + "%)";
            percentS = pad(percentS, " ", 5 - percentS.length(), true);
            stdDevVal = "" + count + " (" + percentS;
            stdDevVal = pad(stdDevVal, " ", maxWidth + 1 - stdDevVal.length(), true);
            
            temp.append(stdDevVal);
            

            for (int k = 0; k < m_NumClusters; k++) {
              count = m_ClusterNominalCounts[k][i][j];
              percent = (int)(m_ClusterNominalCounts[k][i][j] / m_ClusterSizes[k] * 100.0D);
              
              percentS = "" + percent + "%)";
              percentS = pad(percentS, " ", 5 - percentS.length(), true);
              stdDevVal = "" + count + " (" + percentS;
              stdDevVal = pad(stdDevVal, " ", maxWidth + 1 - stdDevVal.length(), true);
              
              temp.append(stdDevVal);
            }
            temp.append("\n");
          }
          
          if (m_FullMissingCounts[i] > 0)
          {
            temp.append(pad("  missing", " ", maxAttWidth + 1 - "  missing".length(), false));
            
            int count = m_FullMissingCounts[i];
            int percent = (int)(m_FullMissingCounts[i] / Utils.sum(m_ClusterSizes) * 100.0D);
            
            String percentS = "" + percent + "%)";
            percentS = pad(percentS, " ", 5 - percentS.length(), true);
            stdDevVal = "" + count + " (" + percentS;
            stdDevVal = pad(stdDevVal, " ", maxWidth + 1 - stdDevVal.length(), true);
            
            temp.append(stdDevVal);
            

            for (int k = 0; k < m_NumClusters; k++) {
              count = m_ClusterMissingCounts[k][i];
              percent = (int)(m_ClusterMissingCounts[k][i] / m_ClusterSizes[k] * 100.0D);
              
              percentS = "" + percent + "%)";
              percentS = pad(percentS, " ", 5 - percentS.length(), true);
              stdDevVal = "" + count + " (" + percentS;
              stdDevVal = pad(stdDevVal, " ", maxWidth + 1 - stdDevVal.length(), true);
              
              temp.append(stdDevVal);
            }
            
            temp.append("\n");
          }
          
          temp.append("\n");
        }
        else {
          if (Double.isNaN(m_FullMeansOrMediansOrModes[i])) {
            stdDevVal = pad("--", " ", maxAttWidth + maxWidth + 1 - 2, true);
          } else { String strVal;
            stdDevVal = pad(strVal = plusMinus + Utils.doubleToString(m_FullStdDevs[i], maxWidth, 4).trim(), " ", maxWidth + maxAttWidth + 1 - strVal.length(), true);
          }
          


          temp.append(stdDevVal);
          

          for (int j = 0; j < m_NumClusters; j++) {
            if (m_ClusterCentroids.instance(j).isMissing(i)) {
              stdDevVal = pad("--", " ", maxWidth + 1 - 2, true);
            } else { String strVal;
              stdDevVal = pad(strVal = plusMinus + Utils.doubleToString(m_ClusterStdDevs.instance(j).value(i), maxWidth, 4).trim(), " ", maxWidth + 1 - strVal.length(), true);
            }
            



            temp.append(stdDevVal);
          }
          temp.append("\n\n");
        }
      }
    }
    
    temp.append("\n\n");
    return temp.toString();
  }
  
  private String pad(String source, String padChar, int length, boolean leftPad) {
    StringBuffer temp = new StringBuffer();
    
    if (leftPad) {
      for (int i = 0; i < length; i++) {
        temp.append(padChar);
      }
      temp.append(source);
    } else {
      temp.append(source);
      for (int i = 0; i < length; i++) {
        temp.append(padChar);
      }
    }
    return temp.toString();
  }
  




  public Instances getClusterCentroids()
  {
    return m_ClusterCentroids;
  }
  




  public Instances getClusterStandardDevs()
  {
    return m_ClusterStdDevs;
  }
  





  public int[][][] getClusterNominalCounts()
  {
    return m_ClusterNominalCounts;
  }
  




  public double getSquaredError()
  {
    return Utils.sum(m_squaredErrors);
  }
  




  public int[] getClusterSizes()
  {
    return m_ClusterSizes;
  }
  





  public int[] getAssignments()
    throws Exception
  {
    if (!m_PreserveOrder) {
      throw new Exception("The assignments are only available when order of instances is preserved (-O)");
    }
    
    if (m_Assignments == null) {
      throw new Exception("No assignments made.");
    }
    return m_Assignments;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10537 $");
  }
  






  public static void main(String[] argv)
  {
    runClusterer(new SimpleKMeans(), argv);
  }
}
