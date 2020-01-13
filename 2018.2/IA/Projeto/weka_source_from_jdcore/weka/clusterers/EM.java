package weka.clusterers;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.estimators.DiscreteEstimator;
import weka.estimators.Estimator;
import weka.experiment.Stats;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;














































































public class EM
  extends RandomizableDensityBasedClusterer
  implements NumberOfClustersRequestable, WeightedInstancesHandler
{
  static final long serialVersionUID = 8348181483812829475L;
  private Estimator[][] m_modelPrev;
  private double[][][] m_modelNormalPrev;
  private double[] m_priorsPrev;
  private Estimator[][] m_model;
  private double[][][] m_modelNormal;
  private double m_minStdDev = 1.0E-6D;
  

  private double[] m_minStdDevPerAtt;
  

  private double[][] m_weights;
  

  private double[] m_priors;
  

  private double m_loglikely;
  
  private Instances m_theInstances = null;
  


  private int m_num_clusters;
  


  private int m_initialNumClusters;
  


  private int m_num_attribs;
  

  private int m_num_instances;
  

  private int m_max_iterations;
  

  private double[] m_minValues;
  

  private double[] m_maxValues;
  

  private Random m_rr;
  

  private boolean m_verbose;
  

  private ReplaceMissingValues m_replaceMissing;
  

  private boolean m_displayModelInOldFormat;
  


  public String globalInfo()
  {
    return "Simple EM (expectation maximisation) class.\n\nEM assigns a probability distribution to each instance which indicates the probability of it belonging to each of the clusters. EM can decide how many clusters to create by cross validation, or you may specify apriori how many clusters to generate.\n\nThe cross validation performed to determine the number of clusters is done in the following steps:\n1. the number of clusters is set to 1\n2. the training set is split randomly into 10 folds.\n3. EM is performed 10 times using the 10 folds the usual CV way.\n4. the loglikelihood is averaged over all 10 results.\n5. if loglikelihood has increased the number of clusters is increased by 1 and the program continues at step 2. \n\nThe number of folds is fixed to 10, as long as the number of instances in the training set is not smaller 10. If this is the case the number of folds is set equal to the number of instances.";
  }
  




















  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tnumber of clusters. If omitted or -1 specified, then \n\tcross validation is used to select the number of clusters.", "N", 1, "-N <num>"));
    



    result.addElement(new Option("\tmax iterations.\n(default 100)", "I", 1, "-I <num>"));
    



    result.addElement(new Option("\tverbose.", "V", 0, "-V"));
    


    result.addElement(new Option("\tminimum allowable standard deviation for normal density\n\tcomputation\n\t(default 1e-6)", "M", 1, "-M <num>"));
    




    result.addElement(new Option("\tDisplay model in old format (good when there are many clusters)\n", "O", 0, "-O"));
    



    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    return result.elements();
  }
  



































  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    setDebug(Utils.getFlag('V', options));
    String optionString = Utils.getOption('I', options);
    
    if (optionString.length() != 0) {
      setMaxIterations(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('N', options);
    if (optionString.length() != 0) {
      setNumClusters(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('M', options);
    if (optionString.length() != 0) {
      setMinStdDev(new Double(optionString).doubleValue());
    }
    
    setDisplayModelInOldFormat(Utils.getFlag('O', options));
    
    super.setOptions(options);
  }
  




  public String displayModelInOldFormatTipText()
  {
    return "Use old format for model output. The old format is better when there are many clusters. The new format is better when there are fewer clusters and many attributes.";
  }
  







  public void setDisplayModelInOldFormat(boolean d)
  {
    m_displayModelInOldFormat = d;
  }
  





  public boolean getDisplayModelInOldFormat()
  {
    return m_displayModelInOldFormat;
  }
  




  public String minStdDevTipText()
  {
    return "set minimum allowable standard deviation";
  }
  







  public void setMinStdDev(double m)
  {
    m_minStdDev = m;
  }
  
  public void setMinStdDevPerAtt(double[] m) {
    m_minStdDevPerAtt = m;
  }
  



  public double getMinStdDev()
  {
    return m_minStdDev;
  }
  




  public String numClustersTipText()
  {
    return "set number of clusters. -1 to select number of clusters automatically by cross validation.";
  }
  







  public void setNumClusters(int n)
    throws Exception
  {
    if (n == 0) {
      throw new Exception("Number of clusters must be > 0. (or -1 to select by cross validation).");
    }
    

    if (n < 0) {
      m_num_clusters = -1;
      m_initialNumClusters = -1;
    }
    else {
      m_num_clusters = n;
      m_initialNumClusters = n;
    }
  }
  





  public int getNumClusters()
  {
    return m_initialNumClusters;
  }
  




  public String maxIterationsTipText()
  {
    return "maximum number of iterations";
  }
  





  public void setMaxIterations(int i)
    throws Exception
  {
    if (i < 1) {
      throw new Exception("Maximum number of iterations must be > 0!");
    }
    
    m_max_iterations = i;
  }
  





  public int getMaxIterations()
  {
    return m_max_iterations;
  }
  





  public String debugTipText()
  {
    return "If set to true, clusterer may output additional info to the console.";
  }
  






  public void setDebug(boolean v)
  {
    m_verbose = v;
  }
  





  public boolean getDebug()
  {
    return m_verbose;
  }
  









  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-I");
    result.add("" + m_max_iterations);
    result.add("-N");
    result.add("" + getNumClusters());
    result.add("-M");
    result.add("" + getMinStdDev());
    if (m_displayModelInOldFormat) {
      result.add("-O");
    }
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  








  private void EM_Init(Instances inst)
    throws Exception
  {
    SimpleKMeans bestK = null;
    double bestSqE = Double.MAX_VALUE;
    for (int i = 0; i < 10; i++) {
      SimpleKMeans sk = new SimpleKMeans();
      sk.setSeed(m_rr.nextInt());
      sk.setNumClusters(m_num_clusters);
      sk.setDisplayStdDevs(true);
      sk.buildClusterer(inst);
      if (sk.getSquaredError() < bestSqE) {
        bestSqE = sk.getSquaredError();
        bestK = sk;
      }
    }
    

    m_num_clusters = bestK.numberOfClusters();
    m_weights = new double[inst.numInstances()][m_num_clusters];
    m_model = new DiscreteEstimator[m_num_clusters][m_num_attribs];
    m_modelNormal = new double[m_num_clusters][m_num_attribs][3];
    m_priors = new double[m_num_clusters];
    
    m_modelPrev = new DiscreteEstimator[m_num_clusters][m_num_attribs];
    m_modelNormalPrev = new double[m_num_clusters][m_num_attribs][3];
    m_priorsPrev = new double[m_num_clusters];
    
    Instances centers = bestK.getClusterCentroids();
    Instances stdD = bestK.getClusterStandardDevs();
    int[][][] nominalCounts = bestK.getClusterNominalCounts();
    int[] clusterSizes = bestK.getClusterSizes();
    
    for (i = 0; i < m_num_clusters; i++) {
      Instance center = centers.instance(i);
      for (int j = 0; j < m_num_attribs; j++) {
        if (inst.attribute(j).isNominal()) {
          m_model[i][j] = new DiscreteEstimator(m_theInstances.attribute(j).numValues(), true);
          

          for (int k = 0; k < inst.attribute(j).numValues(); k++) {
            m_model[i][j].addValue(k, nominalCounts[i][j][k]);
          }
        }
        double minStdD = m_minStdDevPerAtt != null ? m_minStdDevPerAtt[j] : m_minStdDev;
        

        double mean = center.isMissing(j) ? inst.meanOrMode(j) : center.value(j);
        

        m_modelNormal[i][j][0] = mean;
        double stdv = stdD.instance(i).isMissing(j) ? (m_maxValues[j] - m_minValues[j]) / (2 * m_num_clusters) : stdD.instance(i).value(j);
        

        if (stdv < minStdD) {
          stdv = attributeStatsnumericStats.stdDev;
          if (Double.isInfinite(stdv)) {
            stdv = minStdD;
          }
          if (stdv < minStdD) {
            stdv = minStdD;
          }
        }
        if (stdv <= 0.0D) {
          stdv = m_minStdDev;
        }
        
        m_modelNormal[i][j][1] = stdv;
        m_modelNormal[i][j][2] = 1.0D;
      }
    }
    


    for (int j = 0; j < m_num_clusters; j++)
    {
      m_priors[j] = clusterSizes[j];
    }
    Utils.normalize(m_priors);
  }
  







  private void estimate_priors(Instances inst)
    throws Exception
  {
    for (int i = 0; i < m_num_clusters; i++) {
      m_priorsPrev[i] = m_priors[i];
      m_priors[i] = 0.0D;
    }
    
    for (int i = 0; i < inst.numInstances(); i++) {
      for (int j = 0; j < m_num_clusters; j++) {
        m_priors[j] += inst.instance(i).weight() * m_weights[i][j];
      }
    }
    
    Utils.normalize(m_priors);
  }
  


  private static double m_normConst = Math.log(Math.sqrt(6.283185307179586D));
  







  private double logNormalDens(double x, double mean, double stdDev)
  {
    double diff = x - mean;
    


    return -(diff * diff / (2.0D * stdDev * stdDev)) - m_normConst - Math.log(stdDev);
  }
  


  private void new_estimators()
  {
    for (int i = 0; i < m_num_clusters; i++) {
      for (int j = 0; j < m_num_attribs; j++) {
        if (m_theInstances.attribute(j).isNominal()) {
          m_modelPrev[i][j] = m_model[i][j];
          m_model[i][j] = new DiscreteEstimator(m_theInstances.attribute(j).numValues(), true);

        }
        else
        {
          m_modelNormalPrev[i][j][0] = m_modelNormal[i][j][0];
          m_modelNormalPrev[i][j][1] = m_modelNormal[i][j][1];
          m_modelNormalPrev[i][j][2] = m_modelNormal[i][j][2]; double 
            tmp170_169 = (m_modelNormal[i][j][2] = 0.0D);m_modelNormal[i][j][1] = tmp170_169;m_modelNormal[i][j][0] = tmp170_169;
        }
      }
    }
  }
  









  private void M(Instances inst)
    throws Exception
  {
    new_estimators();
    estimate_priors(inst);
    
    for (int i = 0; i < m_num_clusters; i++) {
      for (int j = 0; j < m_num_attribs; j++) {
        for (int l = 0; l < inst.numInstances(); l++) {
          Instance in = inst.instance(l);
          if (!in.isMissing(j)) {
            if (inst.attribute(j).isNominal()) {
              m_model[i][j].addValue(in.value(j), in.weight() * m_weights[l][i]);
            }
            else
            {
              m_modelNormal[i][j][0] += in.value(j) * in.weight() * m_weights[l][i];
              
              m_modelNormal[i][j][2] += in.weight() * m_weights[l][i];
              m_modelNormal[i][j][1] += in.value(j) * in.value(j) * in.weight() * m_weights[l][i];
            }
          }
        }
      }
    }
    


    for (int j = 0; j < m_num_attribs; j++) {
      if (!inst.attribute(j).isNominal()) {
        for (i = 0; i < m_num_clusters; i++) {
          if (m_modelNormal[i][j][2] <= 0.0D) {
            m_modelNormal[i][j][1] = Double.MAX_VALUE;
            
            m_modelNormal[i][j][0] = m_minStdDev;
          }
          else
          {
            m_modelNormal[i][j][1] = ((m_modelNormal[i][j][1] - m_modelNormal[i][j][0] * m_modelNormal[i][j][0] / m_modelNormal[i][j][2]) / m_modelNormal[i][j][2]);
            




            if (m_modelNormal[i][j][1] < 0.0D) {
              m_modelNormal[i][j][1] = 0.0D;
            }
            

            double minStdD = m_minStdDevPerAtt != null ? m_minStdDevPerAtt[j] : m_minStdDev;
            


            m_modelNormal[i][j][1] = Math.sqrt(m_modelNormal[i][j][1]);
            
            if (m_modelNormal[i][j][1] <= minStdD) {
              m_modelNormal[i][j][1] = attributeStatsnumericStats.stdDev;
              if (m_modelNormal[i][j][1] <= minStdD) {
                m_modelNormal[i][j][1] = minStdD;
              }
            }
            if (m_modelNormal[i][j][1] <= 0.0D) {
              m_modelNormal[i][j][1] = m_minStdDev;
            }
            if (Double.isInfinite(m_modelNormal[i][j][1])) {
              m_modelNormal[i][j][1] = m_minStdDev;
            }
            

            m_modelNormal[i][j][0] /= m_modelNormal[i][j][2];
          }
        }
      }
    }
  }
  









  private double E(Instances inst, boolean change_weights)
    throws Exception
  {
    double loglk = 0.0D;double sOW = 0.0D;
    
    for (int l = 0; l < inst.numInstances(); l++)
    {
      Instance in = inst.instance(l);
      
      loglk += in.weight() * logDensityForInstance(in);
      sOW += in.weight();
      
      if (change_weights) {
        m_weights[l] = distributionForInstance(in);
      }
    }
    




    return loglk / sOW;
  }
  






  public EM()
  {
    m_SeedDefault = 100;
    resetOptions();
  }
  



  protected void resetOptions()
  {
    m_minStdDev = 1.0E-6D;
    m_max_iterations = 100;
    m_Seed = m_SeedDefault;
    m_num_clusters = -1;
    m_initialNumClusters = -1;
    m_verbose = false;
  }
  




  public double[][][] getClusterModelsNumericAtts()
  {
    return m_modelNormal;
  }
  




  public double[] getClusterPriors()
  {
    return m_priors;
  }
  




  public String toString()
  {
    if (m_displayModelInOldFormat) {
      return toStringOriginal();
    }
    
    if (m_priors == null) {
      return "No clusterer built yet!";
    }
    StringBuffer temp = new StringBuffer();
    temp.append("\nEM\n==\n");
    if (m_initialNumClusters == -1) {
      temp.append("\nNumber of clusters selected by cross validation: " + m_num_clusters + "\n");
    }
    else {
      temp.append("\nNumber of clusters: " + m_num_clusters + "\n");
    }
    
    int maxWidth = 0;
    int maxAttWidth = 0;
    boolean containsKernel = false;
    


    for (int i = 0; i < m_num_attribs; i++) {
      Attribute a = m_theInstances.attribute(i);
      if (a.name().length() > maxAttWidth) {
        maxAttWidth = m_theInstances.attribute(i).name().length();
      }
      if (a.isNominal())
      {
        for (int j = 0; j < a.numValues(); j++) {
          String val = a.value(j) + "  ";
          if (val.length() > maxAttWidth) {
            maxAttWidth = val.length();
          }
        }
      }
    }
    
    for (int i = 0; i < m_num_clusters; i++) {
      for (int j = 0; j < m_num_attribs; j++) {
        if (m_theInstances.attribute(j).isNumeric())
        {
          double mean = Math.log(Math.abs(m_modelNormal[i][j][0])) / Math.log(10.0D);
          double stdD = Math.log(Math.abs(m_modelNormal[i][j][1])) / Math.log(10.0D);
          double width = mean > stdD ? mean : stdD;
          

          if (width < 0.0D) {
            width = 1.0D;
          }
          
          width += 6.0D;
          if ((int)width > maxWidth) {
            maxWidth = (int)width;
          }
        }
        else {
          DiscreteEstimator d = (DiscreteEstimator)m_model[i][j];
          for (int k = 0; k < d.getNumSymbols(); k++) {
            String size = Utils.doubleToString(d.getCount(k), maxWidth, 4).trim();
            if (size.length() > maxWidth) {
              maxWidth = size.length();
            }
          }
          int sum = Utils.doubleToString(d.getSumOfCounts(), maxWidth, 4).trim().length();
          
          if (sum > maxWidth) {
            maxWidth = sum;
          }
        }
      }
    }
    
    if (maxAttWidth < "Attribute".length()) {
      maxAttWidth = "Attribute".length();
    }
    
    maxAttWidth += 2;
    
    temp.append("\n\n");
    temp.append(pad("Cluster", " ", maxAttWidth + maxWidth + 1 - "Cluster".length(), true));
    


    temp.append("\n");
    temp.append(pad("Attribute", " ", maxAttWidth - "Attribute".length(), false));
    

    for (int i = 0; i < m_num_clusters; i++) {
      String classL = "" + i;
      temp.append(pad(classL, " ", maxWidth + 1 - classL.length(), true));
    }
    temp.append("\n");
    

    temp.append(pad("", " ", maxAttWidth, true));
    for (int i = 0; i < m_num_clusters; i++) {
      String priorP = Utils.doubleToString(m_priors[i], maxWidth, 2).trim();
      priorP = "(" + priorP + ")";
      temp.append(pad(priorP, " ", maxWidth + 1 - priorP.length(), true));
    }
    
    temp.append("\n");
    temp.append(pad("", "=", maxAttWidth + maxWidth * m_num_clusters + m_num_clusters + 1, true));
    

    temp.append("\n");
    
    for (int i = 0; i < m_num_attribs; i++) {
      String attName = m_theInstances.attribute(i).name();
      temp.append(attName + "\n");
      
      if (m_theInstances.attribute(i).isNumeric()) {
        String meanL = "  mean";
        temp.append(pad(meanL, " ", maxAttWidth + 1 - meanL.length(), false));
        for (int j = 0; j < m_num_clusters; j++)
        {
          String mean = Utils.doubleToString(m_modelNormal[j][i][0], maxWidth, 4).trim();
          
          temp.append(pad(mean, " ", maxWidth + 1 - mean.length(), true));
        }
        temp.append("\n");
        
        String stdDevL = "  std. dev.";
        temp.append(pad(stdDevL, " ", maxAttWidth + 1 - stdDevL.length(), false));
        for (int j = 0; j < m_num_clusters; j++) {
          String stdDev = Utils.doubleToString(m_modelNormal[j][i][1], maxWidth, 4).trim();
          
          temp.append(pad(stdDev, " ", maxWidth + 1 - stdDev.length(), true));
        }
        temp.append("\n\n");
      } else {
        Attribute a = m_theInstances.attribute(i);
        for (int j = 0; j < a.numValues(); j++) {
          String val = "  " + a.value(j);
          temp.append(pad(val, " ", maxAttWidth + 1 - val.length(), false));
          for (int k = 0; k < m_num_clusters; k++) {
            DiscreteEstimator d = (DiscreteEstimator)m_model[k][i];
            String count = Utils.doubleToString(d.getCount(j), maxWidth, 4).trim();
            temp.append(pad(count, " ", maxWidth + 1 - count.length(), true));
          }
          temp.append("\n");
        }
        
        String total = "  [total]";
        temp.append(pad(total, " ", maxAttWidth + 1 - total.length(), false));
        for (int k = 0; k < m_num_clusters; k++) {
          DiscreteEstimator d = (DiscreteEstimator)m_model[k][i];
          String count = Utils.doubleToString(d.getSumOfCounts(), maxWidth, 4).trim();
          
          temp.append(pad(count, " ", maxWidth + 1 - count.length(), true));
        }
        temp.append("\n");
      }
    }
    
    return temp.toString();
  }
  
  private String pad(String source, String padChar, int length, boolean leftPad)
  {
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
  




  protected String toStringOriginal()
  {
    if (m_priors == null) {
      return "No clusterer built yet!";
    }
    StringBuffer temp = new StringBuffer();
    temp.append("\nEM\n==\n");
    if (m_initialNumClusters == -1) {
      temp.append("\nNumber of clusters selected by cross validation: " + m_num_clusters + "\n");
    }
    else {
      temp.append("\nNumber of clusters: " + m_num_clusters + "\n");
    }
    
    for (int j = 0; j < m_num_clusters; j++) {
      temp.append("\nCluster: " + j + " Prior probability: " + Utils.doubleToString(m_priors[j], 4) + "\n\n");
      

      for (int i = 0; i < m_num_attribs; i++) {
        temp.append("Attribute: " + m_theInstances.attribute(i).name() + "\n");
        
        if (m_theInstances.attribute(i).isNominal()) {
          if (m_model[j][i] != null) {
            temp.append(m_model[j][i].toString());
          }
        }
        else {
          temp.append("Normal Distribution. Mean = " + Utils.doubleToString(m_modelNormal[j][i][0], 4) + " StdDev = " + Utils.doubleToString(m_modelNormal[j][i][1], 4) + "\n");
        }
      }
    }
    




    return temp.toString();
  }
  





  private void EM_Report(Instances inst)
  {
    System.out.println("======================================");
    
    for (int j = 0; j < m_num_clusters; j++) {
      for (int i = 0; i < m_num_attribs; i++) {
        System.out.println("Clust: " + j + " att: " + i + "\n");
        
        if (m_theInstances.attribute(i).isNominal()) {
          if (m_model[j][i] != null) {
            System.out.println(m_model[j][i].toString());
          }
        }
        else {
          System.out.println("Normal Distribution. Mean = " + Utils.doubleToString(m_modelNormal[j][i][0], 8, 4) + " StandardDev = " + Utils.doubleToString(m_modelNormal[j][i][1], 8, 4) + " WeightSum = " + Utils.doubleToString(m_modelNormal[j][i][2], 8, 4));
        }
      }
    }
    








    for (int l = 0; l < inst.numInstances(); l++) {
      int m = Utils.maxIndex(m_weights[l]);
      System.out.print("Inst " + Utils.doubleToString(l, 5, 0) + " Class " + m + "\t");
      
      for (j = 0; j < m_num_clusters; j++) {
        System.out.print(Utils.doubleToString(m_weights[l][j], 7, 5) + "  ");
      }
      System.out.println();
    }
  }
  






  private void CVClusters()
    throws Exception
  {
    double CVLogLikely = -1.7976931348623157E308D;
    
    boolean CVincreased = true;
    m_num_clusters = 1;
    int num_clusters = m_num_clusters;
    


    int numFolds = m_theInstances.numInstances() < 10 ? m_theInstances.numInstances() : 10;
    


    boolean ok = true;
    int seed = getSeed();
    int restartCount = 0;
    while (CVincreased)
    {

      CVincreased = false;
      Random cvr = new Random(getSeed());
      Instances trainCopy = new Instances(m_theInstances);
      trainCopy.randomize(cvr);
      double templl = 0.0D;
      for (int i = 0; i < numFolds; i++) {
        Instances cvTrain = trainCopy.trainCV(numFolds, i, cvr);
        if (num_clusters > cvTrain.numInstances()) {
          break label429;
        }
        Instances cvTest = trainCopy.testCV(numFolds, i);
        m_rr = new Random(seed);
        for (int z = 0; z < 10; z++) m_rr.nextDouble();
        m_num_clusters = num_clusters;
        EM_Init(cvTrain);
        try {
          iterate(cvTrain, false);
        }
        catch (Exception ex) {
          ex.printStackTrace();
          
          seed++;
          restartCount++;
          ok = false;
          if (restartCount <= 5) break label233; }
        break label429;
        label233:
        break;
        double tll;
        try {
          tll = E(cvTest, false);
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
          

          seed++;
          restartCount++;
          ok = false;
          if (restartCount <= 5) break label273; }
        break label429;
        label273:
        break;
        

        if (m_verbose) {
          System.out.println("# clust: " + num_clusters + " Fold: " + i + " Loglikely: " + tll);
        }
        
        templl += tll;
      }
      
      if (ok) {
        restartCount = 0;
        seed = getSeed();
        templl /= numFolds;
        
        if (m_verbose) {
          System.out.println("=================================================\n# clust: " + num_clusters + " Mean Loglikely: " + templl + "\n================================" + "=================");
        }
        






        if (templl > CVLogLikely) {
          CVLogLikely = templl;
          CVincreased = true;
          num_clusters++;
        }
      }
    }
    label429:
    if (m_verbose) {
      System.out.println("Number of clusters: " + (num_clusters - 1));
    }
    
    m_num_clusters = (num_clusters - 1);
  }
  







  public int numberOfClusters()
    throws Exception
  {
    if (m_num_clusters == -1) {
      throw new Exception("Haven't generated any clusters!");
    }
    
    return m_num_clusters;
  }
  






  private void updateMinMax(Instance instance)
  {
    for (int j = 0; j < m_theInstances.numAttributes(); j++) {
      if (!instance.isMissing(j)) {
        if (Double.isNaN(m_minValues[j])) {
          m_minValues[j] = instance.value(j);
          m_maxValues[j] = instance.value(j);
        }
        else if (instance.value(j) < m_minValues[j]) {
          m_minValues[j] = instance.value(j);
        }
        else if (instance.value(j) > m_maxValues[j]) {
          m_maxValues[j] = instance.value(j);
        }
      }
    }
  }
  







  public Capabilities getCapabilities()
  {
    Capabilities result = new SimpleKMeans().getCapabilities();
    result.setOwner(this);
    return result;
  }
  









  public void buildClusterer(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    
    m_replaceMissing = new ReplaceMissingValues();
    Instances instances = new Instances(data);
    instances.setClassIndex(-1);
    m_replaceMissing.setInputFormat(instances);
    data = Filter.useFilter(instances, m_replaceMissing);
    instances = null;
    
    m_theInstances = data;
    

    m_minValues = new double[m_theInstances.numAttributes()];
    m_maxValues = new double[m_theInstances.numAttributes()];
    for (int i = 0; i < m_theInstances.numAttributes(); i++) {
      double tmp110_107 = NaN.0D;m_maxValues[i] = tmp110_107;m_minValues[i] = tmp110_107;
    }
    for (int i = 0; i < m_theInstances.numInstances(); i++) {
      updateMinMax(m_theInstances.instance(i));
    }
    
    doEM();
    

    m_theInstances = new Instances(m_theInstances, 0);
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
    double[] wghts = new double[m_num_clusters];
    
    m_replaceMissing.input(inst);
    inst = m_replaceMissing.output();
    
    for (int i = 0; i < m_num_clusters; i++)
    {
      double logprob = 0.0D;
      
      for (int j = 0; j < m_num_attribs; j++) {
        if (!inst.isMissing(j)) {
          if (inst.attribute(j).isNominal()) {
            logprob += Math.log(m_model[i][j].getProbability(inst.value(j)));
          }
          else {
            logprob += logNormalDens(inst.value(j), m_modelNormal[i][j][0], m_modelNormal[i][j][1]);
          }
        }
      }
      






      wghts[i] = logprob;
    }
    return wghts;
  }
  






  private void doEM()
    throws Exception
  {
    if (m_verbose) {
      System.out.println("Seed: " + getSeed());
    }
    
    m_rr = new Random(getSeed());
    


    for (int i = 0; i < 10; i++) { m_rr.nextDouble();
    }
    m_num_instances = m_theInstances.numInstances();
    m_num_attribs = m_theInstances.numAttributes();
    
    if (m_verbose) {
      System.out.println("Number of instances: " + m_num_instances + "\nNumber of atts: " + m_num_attribs + "\n");
    }
    






    if (m_initialNumClusters == -1) {
      if (m_theInstances.numInstances() > 9) {
        CVClusters();
        m_rr = new Random(getSeed());
        for (int i = 0; i < 10; i++) m_rr.nextDouble();
      } else {
        m_num_clusters = 1;
      }
    }
    

    EM_Init(m_theInstances);
    m_loglikely = iterate(m_theInstances, m_verbose);
  }
  











  private double iterate(Instances inst, boolean report)
    throws Exception
  {
    double llkold = 0.0D;
    double llk = 0.0D;
    
    if (report) {
      EM_Report(inst);
    }
    
    boolean ok = false;
    int seed = getSeed();
    int restartCount = 0;
    while (!ok) {
      try {
        for (int i = 0; i < m_max_iterations; i++) {
          llkold = llk;
          llk = E(inst, true);
          
          if (report) {
            System.out.println("Loglikely: " + llk);
          }
          
          if ((i > 0) && 
            (llk - llkold < 1.0E-6D)) {
            if (llk - llkold >= 0.0D) {
              break;
            }
            m_modelNormal = m_modelNormalPrev;
            m_model = m_modelPrev;
            m_priors = m_priorsPrev; break;
          }
          


          M(inst);
        }
        ok = true;
      }
      catch (Exception ex) {
        ex.printStackTrace();
        seed++;
        restartCount++;
        m_rr = new Random(seed);
        for (int z = 0; z < 10; z++) {
          m_rr.nextDouble();m_rr.nextInt();
        }
        if (restartCount > 5)
        {
          m_num_clusters -= 1;
          restartCount = 0;
        }
        EM_Init(m_theInstances);
      }
    }
    
    if (report) {
      EM_Report(inst);
    }
    
    return llk;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9988 $");
  }
  








  public static void main(String[] argv)
  {
    runClusterer(new EM(), argv);
  }
}
