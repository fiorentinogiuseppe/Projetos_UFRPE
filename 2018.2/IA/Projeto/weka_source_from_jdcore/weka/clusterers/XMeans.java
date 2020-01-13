package weka.clusterers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.AlgVector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.neighboursearch.KDTree;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;




































































































































public class XMeans
  extends RandomizableClusterer
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = -7941793078404132616L;
  protected Instances m_Instances = null;
  

  protected Instances m_Model = null;
  



  protected ReplaceMissingValues m_ReplaceMissingFilter;
  


  protected double m_BinValue = 1.0D;
  

  protected double m_Bic = Double.MIN_VALUE;
  

  protected double[] m_Mle = null;
  

  protected int m_MaxIterations = 1;
  




  protected int m_MaxKMeans = 1000;
  


  protected int m_MaxKMeansForChildren = 1000;
  

  protected int m_NumClusters = 2;
  

  protected int m_MinNumClusters = 2;
  

  protected int m_MaxNumClusters = 4;
  

  protected DistanceFunction m_DistanceF = new EuclideanDistance();
  

  protected Instances m_ClusterCenters;
  

  protected File m_InputCenterFile = new File(System.getProperty("user.dir"));
  


  protected Reader m_DebugVectorsInput = null;
  
  protected int m_DebugVectorsIndex = 0;
  
  protected Instances m_DebugVectors = null;
  

  protected File m_DebugVectorsFile = new File(System.getProperty("user.dir"));
  

  protected transient Reader m_CenterInput = null;
  

  protected File m_OutputCenterFile = new File(System.getProperty("user.dir"));
  

  protected transient PrintWriter m_CenterOutput = null;
  



  protected int[] m_ClusterAssignments;
  


  protected double m_CutOffFactor = 0.5D;
  

  public static int R_LOW = 0;
  
  public static int R_HIGH = 1;
  
  public static int R_WIDTH = 2;
  



  protected KDTree m_KDTree = new KDTree();
  


  protected boolean m_UseKDTree = false;
  

  protected int m_IterationCount = 0;
  

  protected int m_KMeansStopped = 0;
  

  protected int m_NumSplits = 0;
  

  protected int m_NumSplitsDone = 0;
  

  protected int m_NumSplitsStillDone = 0;
  



  protected int m_DebugLevel = 0;
  

  public static int D_PRINTCENTERS = 1;
  
  public static int D_FOLLOWSPLIT = 2;
  
  public static int D_CONVCHCLOSER = 3;
  
  public static int D_RANDOMVECTOR = 4;
  
  public static int D_KDTREE = 5;
  
  public static int D_ITERCOUNT = 6;
  
  public static int D_METH_MISUSE = 80;
  
  public static int D_CURR = 88;
  
  public static int D_GENERAL = 99;
  

  public boolean m_CurrDebugFlag = true;
  




  public XMeans()
  {
    m_SeedDefault = 10;
    setSeed(m_SeedDefault);
  }
  




  public String globalInfo()
  {
    return "Cluster data using the X-means algorithm.\n\nX-Means is K-Means extended by an Improve-Structure part In this part of the algorithm the centers are attempted to be split in its region. The decision between the children of each center and itself is done comparing the BIC-values of the two structures.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Dan Pelleg and Andrew W. Moore");
    result.setValue(TechnicalInformation.Field.TITLE, "X-means: Extending K-means with Efficient Estimation of the Number of Clusters");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Seventeenth International Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.YEAR, "2000");
    result.setValue(TechnicalInformation.Field.PAGES, "727-734");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann");
    
    return result;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    result.enable(Capabilities.Capability.NO_CLASS);
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    
    return result;
  }
  







  public void buildClusterer(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    
    if (m_MinNumClusters > m_MaxNumClusters) {
      throw new Exception("XMeans: min number of clusters can't be greater than max number of clusters!");
    }
    

    m_NumSplits = 0;
    m_NumSplitsDone = 0;
    m_NumSplitsStillDone = 0;
    

    m_ReplaceMissingFilter = new ReplaceMissingValues();
    m_ReplaceMissingFilter.setInputFormat(data);
    m_Instances = Filter.useFilter(data, m_ReplaceMissingFilter);
    

    Random random0 = new Random(m_Seed);
    

    m_NumClusters = m_MinNumClusters;
    

    if (m_DistanceF == null) {
      m_DistanceF = new EuclideanDistance();
    }
    
    m_DistanceF.setInstances(m_Instances);
    checkInstances();
    
    if ((m_DebugVectorsFile.exists()) && (m_DebugVectorsFile.isFile())) {
      initDebugVectorsInput();
    }
    
    int[] allInstList = new int[m_Instances.numInstances()];
    for (int i = 0; i < m_Instances.numInstances(); i++) {
      allInstList[i] = i;
    }
    

    m_Model = new Instances(m_Instances, 0);
    

    if (m_CenterInput != null)
    {
      m_ClusterCenters = new Instances(m_CenterInput);
      m_NumClusters = m_ClusterCenters.numInstances();
    }
    else
    {
      m_ClusterCenters = makeCentersRandomly(random0, m_Instances, m_NumClusters);
    }
    PFD(D_FOLLOWSPLIT, "\n*** Starting centers ");
    for (int k = 0; k < m_ClusterCenters.numInstances(); k++) {
      PFD(D_FOLLOWSPLIT, "Center " + k + ": " + m_ClusterCenters.instance(k));
    }
    
    PrCentersFD(D_PRINTCENTERS);
    
    boolean finished = false;
    


    if (m_UseKDTree) {
      m_KDTree.setInstances(m_Instances);
    }
    
    m_IterationCount = 0;
    








    while ((!finished) && (!stopIteration(m_IterationCount, m_MaxIterations)))
    {







      PFD(D_FOLLOWSPLIT, "\nBeginning of main loop - centers:");
      PrCentersFD(D_FOLLOWSPLIT);
      
      PFD(D_ITERCOUNT, "\n*** 1. Improve-Params " + m_IterationCount + ". time");
      
      m_IterationCount += 1;
      

      boolean converged = false;
      

      m_ClusterAssignments = initAssignments(m_Instances.numInstances());
      
      int[][] instOfCent = new int[m_ClusterCenters.numInstances()][];
      

      int kMeansIteration = 0;
      

      PFD(D_FOLLOWSPLIT, "\nConverge in K-Means:");
      while ((!converged) && (!stopKMeansIteration(kMeansIteration, m_MaxKMeans)))
      {

        kMeansIteration++;
        converged = true;
        

        converged = assignToCenters(m_UseKDTree ? m_KDTree : null, m_ClusterCenters, instOfCent, allInstList, m_ClusterAssignments, kMeansIteration);
        





        PFD(D_FOLLOWSPLIT, "\nMain loop - Assign - centers:");
        PrCentersFD(D_FOLLOWSPLIT);
        
        converged = recomputeCenters(m_ClusterCenters, instOfCent, m_Model);
        

        PFD(D_FOLLOWSPLIT, "\nMain loop - Recompute - centers:");
        PrCentersFD(D_FOLLOWSPLIT);
      }
      PFD(D_FOLLOWSPLIT, "");
      PFD(D_FOLLOWSPLIT, "End of Part: 1. Improve-Params - conventional K-means");
      





      m_Mle = distortion(instOfCent, m_ClusterCenters);
      m_Bic = calculateBIC(instOfCent, m_ClusterCenters, m_Mle);
      PFD(D_FOLLOWSPLIT, "m_Bic " + m_Bic);
      
      int currNumCent = m_ClusterCenters.numInstances();
      Instances splitCenters = new Instances(m_ClusterCenters, currNumCent * 2);
      


      double[] pbic = new double[currNumCent];
      double[] cbic = new double[currNumCent];
      

      for (int i = 0; i < currNumCent; 
          


          i++)
      {
        PFD(D_FOLLOWSPLIT, "\nsplit center " + i + " " + m_ClusterCenters.instance(i));
        
        Instance currCenter = m_ClusterCenters.instance(i);
        int[] currInstList = instOfCent[i];
        int currNumInst = instOfCent[i].length;
        

        if (currNumInst <= 2) {
          pbic[i] = Double.MAX_VALUE;
          cbic[i] = 0.0D;
          
          splitCenters.add(currCenter);
          splitCenters.add(currCenter);

        }
        else
        {
          double variance = m_Mle[i] / currNumInst;
          Instances children = splitCenter(random0, currCenter, variance, m_Model);
          

          int[] oneCentAssignments = initAssignments(currNumInst);
          int[][] instOfChCent = new int[2][];
          

          converged = false;
          int kMeansForChildrenIteration = 0;
          PFD(D_FOLLOWSPLIT, "\nConverge, K-Means for children: " + i);
          while ((!converged) && (!stopKMeansIteration(kMeansForChildrenIteration, m_MaxKMeansForChildren)))
          {

            kMeansForChildrenIteration++;
            
            converged = assignToCenters(children, instOfChCent, currInstList, oneCentAssignments);
            


            if (!converged) {
              recomputeCentersFast(children, instOfChCent, m_Model);
            }
          }
          

          splitCenters.add(children.instance(0));
          splitCenters.add(children.instance(1));
          
          PFD(D_FOLLOWSPLIT, "\nconverged cildren ");
          PFD(D_FOLLOWSPLIT, " " + children.instance(0));
          PFD(D_FOLLOWSPLIT, " " + children.instance(1));
          

          pbic[i] = calculateBIC(currInstList, currCenter, m_Mle[i], m_Model);
          double[] chMLE = distortion(instOfChCent, children);
          cbic[i] = calculateBIC(instOfChCent, children, chMLE);
        }
      }
      

      Instances newClusterCenters = null;
      newClusterCenters = newCentersAfterSplit(pbic, cbic, m_CutOffFactor, splitCenters);
      



      int newNumClusters = newClusterCenters.numInstances();
      if (newNumClusters != m_NumClusters)
      {
        PFD(D_FOLLOWSPLIT, "Compare with non-split");
        

        int[] newClusterAssignments = initAssignments(m_Instances.numInstances());
        


        int[][] newInstOfCent = new int[newClusterCenters.numInstances()][];
        

        converged = assignToCenters(m_UseKDTree ? m_KDTree : null, newClusterCenters, newInstOfCent, allInstList, newClusterAssignments, m_IterationCount);
        





        double[] newMle = distortion(newInstOfCent, newClusterCenters);
        double newBic = calculateBIC(newInstOfCent, newClusterCenters, newMle);
        PFD(D_FOLLOWSPLIT, "newBic " + newBic);
        if (newBic > m_Bic) {
          PFD(D_FOLLOWSPLIT, "*** decide for new clusters");
          m_Bic = newBic;
          m_ClusterCenters = newClusterCenters;
          m_ClusterAssignments = newClusterAssignments;
        } else {
          PFD(D_FOLLOWSPLIT, "*** keep old clusters");
        }
      }
      
      newNumClusters = m_ClusterCenters.numInstances();
      

      if ((newNumClusters >= m_MaxNumClusters) || (newNumClusters == m_NumClusters))
      {
        finished = true;
      }
      m_NumClusters = newNumClusters;
    }
    
    if ((m_ClusterCenters.numInstances() > 0) && (m_CenterOutput != null)) {
      m_CenterOutput.println(m_ClusterCenters.toString());
      m_CenterOutput.close();
      m_CenterOutput = null;
    }
  }
  






  public boolean checkForNominalAttributes(Instances data)
  {
    int i = 0;
    while (i < data.numAttributes()) {
      if ((i != data.classIndex()) && (data.attribute(i++).isNominal())) {
        return true;
      }
    }
    return false;
  }
  




  protected int[] initAssignments(int[] ass)
  {
    for (int i = 0; i < ass.length; i++)
      ass[i] = -1;
    return ass;
  }
  




  protected int[] initAssignments(int numInstances)
  {
    int[] ass = new int[numInstances];
    for (int i = 0; i < numInstances; i++)
      ass[i] = -1;
    return ass;
  }
  




  boolean[] initBoolArray(int len)
  {
    boolean[] boolArray = new boolean[len];
    for (int i = 0; i < len; i++) {
      boolArray[i] = false;
    }
    return boolArray;
  }
  























  protected Instances newCentersAfterSplit(double[] pbic, double[] cbic, double cutoffFactor, Instances splitCenters)
  {
    boolean splitPerCutoff = false;
    boolean takeSomeAway = false;
    boolean[] splitWon = initBoolArray(m_ClusterCenters.numInstances());
    int numToSplit = 0;
    Instances newCenters = null;
    

    for (int i = 0; i < cbic.length; i++) {
      if (cbic[i] > pbic[i])
      {
        splitWon[i] = true;numToSplit++;
        PFD(D_FOLLOWSPLIT, "Center " + i + " decide for children");
      }
      else
      {
        PFD(D_FOLLOWSPLIT, "Center " + i + " decide for parent");
      }
    }
    

    if ((numToSplit == 0) && (cutoffFactor > 0.0D)) {
      splitPerCutoff = true;
      

      numToSplit = (int)(m_ClusterCenters.numInstances() * m_CutOffFactor);
    }
    


    double[] diff = new double[m_NumClusters];
    for (int j = 0; j < diff.length; j++) {
      pbic[j] -= cbic[j];
    }
    int[] sortOrder = Utils.sort(diff);
    

    int possibleToSplit = m_MaxNumClusters - m_NumClusters;
    
    if (possibleToSplit > numToSplit)
    {
      possibleToSplit = numToSplit;
    }
    else {
      takeSomeAway = true;
    }
    
    if (splitPerCutoff) {
      for (int j = 0; (j < possibleToSplit) && (cbic[sortOrder[j]] > 0.0D); 
          j++) {
        splitWon[sortOrder[j]] = true;
      }
      m_NumSplitsStillDone += possibleToSplit;


    }
    else if (takeSomeAway) {
      int count = 0;
      for (int j = 0; 
          (j < splitWon.length) && (count < possibleToSplit); j++) {
        if (splitWon[sortOrder[j]] == 1) { count++;
        }
      }
      while (j < splitWon.length) {
        splitWon[sortOrder[j]] = false;
        j++;
      }
    }
    


    if (possibleToSplit > 0) {
      newCenters = newCentersAfterSplit(splitWon, splitCenters);
    } else
      newCenters = m_ClusterCenters;
    return newCenters;
  }
  










  protected Instances newCentersAfterSplit(boolean[] splitWon, Instances splitCenters)
  {
    Instances newCenters = new Instances(splitCenters, 0);
    
    int sIndex = 0;
    for (int i = 0; i < splitWon.length; i++) {
      if (splitWon[i] != 0) {
        m_NumSplitsDone += 1;
        newCenters.add(splitCenters.instance(sIndex++));
        newCenters.add(splitCenters.instance(sIndex++));
      } else {
        sIndex++;
        sIndex++;
        newCenters.add(m_ClusterCenters.instance(i));
      }
    }
    return newCenters;
  }
  









  protected boolean stopKMeansIteration(int iterationCount, int max)
  {
    boolean stopIterate = false;
    if (max >= 0)
      stopIterate = iterationCount >= max;
    if (stopIterate)
      m_KMeansStopped += 1;
    return stopIterate;
  }
  







  protected boolean stopIteration(int iterationCount, int max)
  {
    boolean stopIterate = false;
    if (max >= 0)
      stopIterate = iterationCount >= max;
    return stopIterate;
  }
  









  protected boolean recomputeCenters(Instances centers, int[][] instOfCent, Instances model)
  {
    boolean converged = true;
    
    for (int i = 0; i < centers.numInstances(); i++)
    {
      for (int j = 0; j < model.numAttributes(); j++) {
        double val = meanOrMode(m_Instances, instOfCent[i], j);
        
        for (int k = 0; k < instOfCent[i].length; k++)
        {
          if ((converged) && (m_ClusterCenters.instance(i).value(j) != val))
            converged = false; }
        if (!converged)
          m_ClusterCenters.instance(i).setValue(j, val);
      }
    }
    return converged;
  }
  










  protected void recomputeCentersFast(Instances centers, int[][] instOfCentIndexes, Instances model)
  {
    for (int i = 0; i < centers.numInstances(); i++)
    {
      for (int j = 0; j < model.numAttributes(); j++) {
        double val = meanOrMode(m_Instances, instOfCentIndexes[i], j);
        centers.instance(i).setValue(j, val);
      }
    }
  }
  











  protected double meanOrMode(Instances instances, int[] instList, int attIndex)
  {
    int numInst = instList.length;
    
    if (instances.attribute(attIndex).isNumeric()) { double found;
      double result = found = 0.0D;
      for (int j = 0; j < numInst; j++) {
        Instance currInst = instances.instance(instList[j]);
        if (!currInst.isMissing(attIndex)) {
          found += currInst.weight();
          result += currInst.weight() * currInst.value(attIndex);
        }
      }
      
      if (Utils.eq(found, 0.0D)) {
        return 0.0D;
      }
      return result / found;
    }
    if (instances.attribute(attIndex).isNominal()) {
      int[] counts = new int[instances.attribute(attIndex).numValues()];
      for (int j = 0; j < numInst; j++) {
        Instance currInst = instances.instance(instList[j]);
        if (!currInst.isMissing(attIndex)) {
          int tmp161_160 = ((int)currInst.value(attIndex)); int[] tmp161_152 = counts;tmp161_152[tmp161_160] = ((int)(tmp161_152[tmp161_160] + currInst.weight()));
        }
      }
      return Utils.maxIndex(counts);
    }
    return 0.0D;
  }
  


















  protected boolean assignToCenters(KDTree tree, Instances centers, int[][] instOfCent, int[] allInstList, int[] assignments, int iterationCount)
    throws Exception
  {
    boolean converged = true;
    if (tree != null)
    {
      converged = assignToCenters(tree, centers, instOfCent, assignments, iterationCount);

    }
    else
    {

      converged = assignToCenters(centers, instOfCent, allInstList, assignments);
    }
    


    return converged;
  }
  
















  protected boolean assignToCenters(KDTree kdtree, Instances centers, int[][] instOfCent, int[] assignments, int iterationCount)
    throws Exception
  {
    int numCent = centers.numInstances();
    int numInst = m_Instances.numInstances();
    int[] oldAssignments = new int[numInst];
    


    if (assignments == null) {
      assignments = new int[numInst];
      for (int i = 0; i < numInst; i++) {
        assignments[0] = -1;
      }
    }
    


    if (instOfCent == null) {
      instOfCent = new int[numCent][];
    }
    

    for (int i = 0; i < assignments.length; i++) {
      oldAssignments[i] = assignments[i];
    }
    

    kdtree.centerInstances(centers, assignments, Math.pow(0.8D, iterationCount));
    
    boolean converged = true;
    

    for (int i = 0; (converged) && (i < assignments.length); i++) {
      converged = oldAssignments[i] == assignments[i];
      if (assignments[i] == -1) {
        throw new Exception("Instance " + i + " has not been assigned to cluster.");
      }
    }
    
    if (!converged) {
      int[] numInstOfCent = new int[numCent];
      for (int i = 0; i < numCent; i++) {
        numInstOfCent[i] = 0;
      }
      
      for (int i = 0; i < numInst; i++) {
        numInstOfCent[assignments[i]] += 1;
      }
      
      for (int i = 0; i < numCent; i++) {
        instOfCent[i] = new int[numInstOfCent[i]];
      }
      
      for (int i = 0; i < numCent; i++) {
        int index = -1;
        for (int j = 0; j < numInstOfCent[i]; j++) {
          index = nextAssignedOne(i, index, assignments);
          instOfCent[i][j] = index;
        }
      }
    }
    
    return converged;
  }
  
















  protected boolean assignToCenters(Instances centers, int[][] instOfCent, int[] allInstList, int[] assignments)
    throws Exception
  {
    boolean converged = true;
    

    int numInst = allInstList.length;
    int numCent = centers.numInstances();
    int[] numInstOfCent = new int[numCent];
    for (int i = 0; i < numCent; i++) { numInstOfCent[i] = 0;
    }
    

    if (assignments == null) {
      assignments = new int[numInst];
      for (int i = 0; i < numInst; i++) {
        assignments[i] = -1;
      }
    }
    


    if (instOfCent == null) {
      instOfCent = new int[numCent][];
    }
    

    for (int i = 0; i < numInst; i++) {
      Instance inst = m_Instances.instance(allInstList[i]);
      int newC = clusterProcessedInstance(inst, centers);
      
      if ((converged) && (newC != assignments[i])) {
        converged = false;
      }
      
      numInstOfCent[newC] += 1;
      if (!converged) {
        assignments[i] = newC;
      }
    }
    

    if (!converged) {
      PFD(D_FOLLOWSPLIT, "assignToCenters -> it has NOT converged");
      for (int i = 0; i < numCent; i++) {
        instOfCent[i] = new int[numInstOfCent[i]];
      }
      
      for (int i = 0; i < numCent; i++) {
        int index = -1;
        for (int j = 0; j < numInstOfCent[i]; j++) {
          index = nextAssignedOne(i, index, assignments);
          instOfCent[i][j] = allInstList[index];
        }
      }
    }
    else {
      PFD(D_FOLLOWSPLIT, "assignToCenters -> it has converged");
    }
    return converged;
  }
  








  protected int nextAssignedOne(int cent, int lastIndex, int[] assignments)
  {
    int len = assignments.length;
    int index = lastIndex + 1;
    while (index < len) {
      if (assignments[index] == cent) {
        return index;
      }
      index++;
    }
    return -1;
  }
  













  protected Instances splitCenter(Random random, Instance center, double variance, Instances model)
    throws Exception
  {
    m_NumSplits += 1;
    AlgVector r = null;
    Instances children = new Instances(model, 2);
    
    if ((m_DebugVectorsFile.exists()) && (m_DebugVectorsFile.isFile())) {
      Instance nextVector = getNextDebugVectorsInstance(model);
      PFD(D_RANDOMVECTOR, "Random Vector from File " + nextVector);
      r = new AlgVector(nextVector);
    }
    else
    {
      r = new AlgVector(model, random);
    }
    r.changeLength(Math.pow(variance, 0.5D));
    PFD(D_RANDOMVECTOR, "random vector *variance " + r);
    

    AlgVector c = new AlgVector(center);
    AlgVector c2 = (AlgVector)c.clone();
    c = c.add(r);
    Instance newCenter = c.getAsInstance(model, random);
    children.add(newCenter);
    PFD(D_FOLLOWSPLIT, "first child " + newCenter);
    

    c2 = c2.substract(r);
    newCenter = c2.getAsInstance(model, random);
    children.add(newCenter);
    PFD(D_FOLLOWSPLIT, "second child " + newCenter);
    
    return children;
  }
  











  protected Instances splitCenters(Random random, Instances instances, Instances model)
  {
    Instances children = new Instances(model, 2);
    int instIndex = Math.abs(random.nextInt()) % instances.numInstances();
    children.add(instances.instance(instIndex));
    int instIndex2 = instIndex;
    int count = 0;
    while ((instIndex2 == instIndex) && (count < 10)) {
      count++;
      instIndex2 = Math.abs(random.nextInt()) % instances.numInstances();
    }
    children.add(instances.instance(instIndex2));
    
    return children;
  }
  









  protected Instances makeCentersRandomly(Random random0, Instances model, int numClusters)
  {
    Instances clusterCenters = new Instances(model, numClusters);
    m_NumClusters = numClusters;
    

    for (int i = 0; i < numClusters; i++) {
      int instIndex = Math.abs(random0.nextInt()) % m_Instances.numInstances();
      clusterCenters.add(m_Instances.instance(instIndex));
    }
    return clusterCenters;
  }
  








  protected double calculateBIC(int[] instList, Instance center, double mle, Instances model)
  {
    int[][] w1 = new int[1][instList.length];
    for (int i = 0; i < instList.length; i++) {
      w1[0][i] = instList[i];
    }
    double[] m = { mle };
    Instances w2 = new Instances(model, 1);
    w2.add(center);
    return calculateBIC(w1, w2, m);
  }
  







  protected double calculateBIC(int[][] instOfCent, Instances centers, double[] mle)
  {
    double loglike = 0.0D;
    int numInstTotal = 0;
    int numCenters = centers.numInstances();
    int numDimensions = centers.numAttributes();
    int numParameters = numCenters - 1 + numCenters * numDimensions + numCenters;
    

    for (int i = 0; i < centers.numInstances(); i++) {
      loglike += logLikelihoodEstimate(instOfCent[i].length, centers.instance(i), mle[i], centers.numInstances() * 2);
      
      numInstTotal += instOfCent[i].length;
    }
    




    loglike -= numInstTotal * Math.log(numInstTotal);
    

    loglike -= numParameters / 2.0D * Math.log(numInstTotal);
    


    return loglike;
  }
  
















  protected double logLikelihoodEstimate(int numInst, Instance center, double distortion, int numCent)
  {
    double loglike = 0.0D;
    
    if (numInst > 1)
    {






      double variance = distortion / (numInst - 1.0D);
      



      double p1 = -(numInst / 2.0D) * Math.log(6.283185307179586D);
      






      double p2 = -(numInst * center.numAttributes()) / 2 * Math.log(variance);
      







      double p3 = -(numInst - 1.0D) / 2.0D;
      



      double p4 = numInst * Math.log(numInst);
      











      loglike = p1 + p2 + p3 + p4;
    }
    


    return loglike;
  }
  





  protected double[] distortion(int[][] instOfCent, Instances centers)
  {
    double[] distortion = new double[centers.numInstances()];
    for (int i = 0; i < centers.numInstances(); i++) {
      distortion[i] = 0.0D;
      for (int j = 0; j < instOfCent[i].length; j++) {
        distortion[i] += m_DistanceF.distance(m_Instances.instance(instOfCent[i][j]), centers.instance(i));
      }
    }
    



    return distortion;
  }
  









  protected int clusterProcessedInstance(Instance instance, Instances centers)
  {
    double minDist = 2.147483647E9D;
    int bestCluster = 0;
    for (int i = 0; i < centers.numInstances(); i++) {
      double dist = m_DistanceF.distance(instance, centers.instance(i));
      
      if (dist < minDist) {
        minDist = dist;
        bestCluster = i;
      }
    }
    
    return bestCluster;
  }
  






  protected int clusterProcessedInstance(Instance instance)
  {
    double minDist = 2.147483647E9D;
    int bestCluster = 0;
    for (int i = 0; i < m_NumClusters; i++) {
      double dist = m_DistanceF.distance(instance, m_ClusterCenters.instance(i));
      
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
    Instance inst = m_ReplaceMissingFilter.output();
    
    return clusterProcessedInstance(inst);
  }
  





  public int numberOfClusters()
  {
    return m_NumClusters;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tmaximum number of overall iterations\n\t(default 1).", "I", 1, "-I <num>"));
    



    result.addElement(new Option("\tmaximum number of iterations in the kMeans loop in\n\tthe Improve-Parameter part \n\t(default 1000).", "M", 1, "-M <num>"));
    




    result.addElement(new Option("\tmaximum number of iterations in the kMeans loop\n\tfor the splitted centroids in the Improve-Structure part \n\t(default 1000).", "J", 1, "-J <num>"));
    




    result.addElement(new Option("\tminimum number of clusters\n\t(default 2).", "L", 1, "-L <num>"));
    



    result.addElement(new Option("\tmaximum number of clusters\n\t(default 4).", "H", 1, "-H <num>"));
    



    result.addElement(new Option("\tdistance value for binary attributes\n\t(default 1.0).", "B", 1, "-B <value>"));
    



    result.addElement(new Option("\tUses the KDTree internally\n\t(default no).", "use-kdtree", 0, "-use-kdtree"));
    



    result.addElement(new Option("\tFull class name of KDTree class to use, followed\n\tby scheme options.\n\teg: \"weka.core.neighboursearch.kdtrees.KDTree -P\"\n\t(default no KDTree class used).", "K", 1, "-K <KDTree class specification>"));
    





    result.addElement(new Option("\tcutoff factor, takes the given percentage of the splitted \n\tcentroids if none of the children win\n\t(default 0.0).", "C", 1, "-C <value>"));
    




    result.addElement(new Option("\tFull class name of Distance function class to use, followed\n\tby scheme options.\n\t(default weka.core.EuclideanDistance).", "D", 1, "-D <distance function class specification>"));
    




    result.addElement(new Option("\tfile to read starting centers from (ARFF format).", "N", 1, "-N <file name>"));
    


    result.addElement(new Option("\tfile to write centers to (ARFF format).", "O", 1, "-O <file name>"));
    


    result.addElement(new Option("\tThe debug level.\n\t(default 0)", "U", 1, "-U <int>"));
    



    result.addElement(new Option("\tThe debug vectors file.", "Y", 1, "-Y <file name>"));
    


    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    return result.elements();
  }
  



  public String minNumClustersTipText()
  {
    return "set minimum number of clusters";
  }
  




  public void setMinNumClusters(int n)
  {
    m_MinNumClusters = n;
  }
  



  public int getMinNumClusters()
  {
    return m_MinNumClusters;
  }
  



  public String maxNumClustersTipText()
  {
    return "set maximum number of clusters";
  }
  



  public void setMaxNumClusters(int n)
  {
    if (n >= m_MinNumClusters) {
      m_MaxNumClusters = n;
    }
  }
  



  public int getMaxNumClusters()
  {
    return m_MaxNumClusters;
  }
  



  public String maxIterationsTipText()
  {
    return "the maximum number of iterations to perform";
  }
  



  public void setMaxIterations(int i)
    throws Exception
  {
    if (i < 0) {
      throw new Exception("Only positive values for iteration number allowed (Option I).");
    }
    m_MaxIterations = i;
  }
  



  public int getMaxIterations()
  {
    return m_MaxIterations;
  }
  



  public String maxKMeansTipText()
  {
    return "the maximum number of iterations to perform in KMeans";
  }
  



  public void setMaxKMeans(int i)
  {
    m_MaxKMeans = i;
    m_MaxKMeansForChildren = i;
  }
  



  public int getMaxKMeans()
  {
    return m_MaxKMeans;
  }
  



  public String maxKMeansForChildrenTipText()
  {
    return "the maximum number of iterations KMeans that is performed on the child centers";
  }
  




  public void setMaxKMeansForChildren(int i)
  {
    m_MaxKMeansForChildren = i;
  }
  



  public int getMaxKMeansForChildren()
  {
    return m_MaxKMeansForChildren;
  }
  



  public String cutOffFactorTipText()
  {
    return "the cut-off factor to use";
  }
  



  public void setCutOffFactor(double i)
  {
    m_CutOffFactor = i;
  }
  



  public double getCutOffFactor()
  {
    return m_CutOffFactor;
  }
  




  public String binValueTipText()
  {
    return "Set the value that represents true in the new attributes.";
  }
  




  public double getBinValue()
  {
    return m_BinValue;
  }
  




  public void setBinValue(double value)
  {
    m_BinValue = value;
  }
  





  public String distanceFTipText()
  {
    return "The distance function to use.";
  }
  



  public void setDistanceF(DistanceFunction distanceF)
  {
    m_DistanceF = distanceF;
  }
  



  public DistanceFunction getDistanceF()
  {
    return m_DistanceF;
  }
  






  protected String getDistanceFSpec()
  {
    DistanceFunction d = getDistanceF();
    if ((d instanceof OptionHandler)) {
      return d.getClass().getName() + " " + Utils.joinOptions(d.getOptions());
    }
    
    return d.getClass().getName();
  }
  





  public String debugVectorsFileTipText()
  {
    return "The file containing the debug vectors (only for debugging!).";
  }
  




  public void setDebugVectorsFile(File value)
  {
    m_DebugVectorsFile = value;
  }
  




  public File getDebugVectorsFile()
  {
    return m_DebugVectorsFile;
  }
  



  public void initDebugVectorsInput()
    throws Exception
  {
    m_DebugVectorsInput = new BufferedReader(new FileReader(m_DebugVectorsFile));
    
    m_DebugVectors = new Instances(m_DebugVectorsInput);
    m_DebugVectorsIndex = 0;
  }
  






  public Instance getNextDebugVectorsInstance(Instances model)
    throws Exception
  {
    if (m_DebugVectorsIndex >= m_DebugVectors.numInstances())
      throw new Exception("no more prefabricated Vectors");
    Instance nex = m_DebugVectors.instance(m_DebugVectorsIndex);
    nex.setDataset(model);
    m_DebugVectorsIndex += 1;
    return nex;
  }
  





  public String inputCenterFileTipText()
  {
    return "The file to read the list of centers from.";
  }
  




  public void setInputCenterFile(File value)
  {
    m_InputCenterFile = value;
  }
  




  public File getInputCenterFile()
  {
    return m_InputCenterFile;
  }
  





  public String outputCenterFileTipText()
  {
    return "The file to write the list of centers to.";
  }
  




  public void setOutputCenterFile(File value)
  {
    m_OutputCenterFile = value;
  }
  




  public File getOutputCenterFile()
  {
    return m_OutputCenterFile;
  }
  





  public String KDTreeTipText()
  {
    return "The KDTree to use.";
  }
  



  public void setKDTree(KDTree k)
  {
    m_KDTree = k;
  }
  




  public KDTree getKDTree()
  {
    return m_KDTree;
  }
  





  public String useKDTreeTipText()
  {
    return "Whether to use the KDTree.";
  }
  




  public void setUseKDTree(boolean value)
  {
    m_UseKDTree = value;
  }
  




  public boolean getUseKDTree()
  {
    return m_UseKDTree;
  }
  






  protected String getKDTreeSpec()
  {
    KDTree c = getKDTree();
    if ((c instanceof OptionHandler)) {
      return c.getClass().getName() + " " + Utils.joinOptions(c.getOptions());
    }
    
    return c.getClass().getName();
  }
  





  public String debugLevelTipText()
  {
    return "The debug level to use.";
  }
  




  public void setDebugLevel(int d)
  {
    m_DebugLevel = d;
  }
  



  public int getDebugLevel()
  {
    return m_DebugLevel;
  }
  










































  protected void checkInstances() {}
  










































  public void setOptions(String[] options)
    throws Exception
  {
    String optionString = Utils.getOption('I', options);
    if (optionString.length() != 0) {
      setMaxIterations(Integer.parseInt(optionString));
    } else {
      setMaxIterations(1);
    }
    optionString = Utils.getOption('M', options);
    if (optionString.length() != 0) {
      setMaxKMeans(Integer.parseInt(optionString));
    } else {
      setMaxKMeans(1000);
    }
    optionString = Utils.getOption('J', options);
    if (optionString.length() != 0) {
      setMaxKMeansForChildren(Integer.parseInt(optionString));
    } else {
      setMaxKMeansForChildren(1000);
    }
    optionString = Utils.getOption('L', options);
    if (optionString.length() != 0) {
      setMinNumClusters(Integer.parseInt(optionString));
    } else {
      setMinNumClusters(2);
    }
    optionString = Utils.getOption('H', options);
    if (optionString.length() != 0) {
      setMaxNumClusters(Integer.parseInt(optionString));
    } else {
      setMaxNumClusters(4);
    }
    optionString = Utils.getOption('B', options);
    if (optionString.length() != 0) {
      setBinValue(Double.parseDouble(optionString));
    } else {
      setBinValue(1.0D);
    }
    setUseKDTree(Utils.getFlag("use-kdtree", options));
    
    if (getUseKDTree()) {
      String funcString = Utils.getOption('K', options);
      if (funcString.length() != 0) {
        String[] funcSpec = Utils.splitOptions(funcString);
        if (funcSpec.length == 0) {
          throw new Exception("Invalid function specification string");
        }
        String funcName = funcSpec[0];
        funcSpec[0] = "";
        setKDTree((KDTree)Utils.forName(KDTree.class, funcName, funcSpec));
      }
      else {
        setKDTree(new KDTree());
      }
    }
    else {
      setKDTree(new KDTree());
    }
    
    optionString = Utils.getOption('C', options);
    if (optionString.length() != 0) {
      setCutOffFactor(Double.parseDouble(optionString));
    } else {
      setCutOffFactor(0.0D);
    }
    String funcString = Utils.getOption('D', options);
    if (funcString.length() != 0) {
      String[] funcSpec = Utils.splitOptions(funcString);
      if (funcSpec.length == 0) {
        throw new Exception("Invalid function specification string");
      }
      String funcName = funcSpec[0];
      funcSpec[0] = "";
      setDistanceF((DistanceFunction)Utils.forName(DistanceFunction.class, funcName, funcSpec));
    }
    else
    {
      setDistanceF(new EuclideanDistance());
    }
    
    optionString = Utils.getOption('N', options);
    if (optionString.length() != 0) {
      setInputCenterFile(new File(optionString));
      m_CenterInput = new BufferedReader(new FileReader(optionString));
    }
    else
    {
      setInputCenterFile(new File(System.getProperty("user.dir")));
      m_CenterInput = null;
    }
    
    optionString = Utils.getOption('O', options);
    if (optionString.length() != 0) {
      setOutputCenterFile(new File(optionString));
      m_CenterOutput = new PrintWriter(new FileOutputStream(optionString));
    }
    else {
      setOutputCenterFile(new File(System.getProperty("user.dir")));
      m_CenterOutput = null;
    }
    
    optionString = Utils.getOption('U', options);
    int debugLevel = 0;
    if (optionString.length() != 0) {
      try {
        debugLevel = Integer.parseInt(optionString);
      } catch (NumberFormatException e) {
        throw new Exception(optionString + "is an illegal value for option -U");
      }
    }
    
    setDebugLevel(debugLevel);
    
    optionString = Utils.getOption('Y', options);
    if (optionString.length() != 0) {
      setDebugVectorsFile(new File(optionString));
    }
    else {
      setDebugVectorsFile(new File(System.getProperty("user.dir")));
      m_DebugVectorsInput = null;
      m_DebugVectors = null;
    }
    
    super.setOptions(options);
  }
  







  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-I");
    result.add("" + getMaxIterations());
    
    result.add("-M");
    result.add("" + getMaxKMeans());
    
    result.add("-J");
    result.add("" + getMaxKMeansForChildren());
    
    result.add("-L");
    result.add("" + getMinNumClusters());
    
    result.add("-H");
    result.add("" + getMaxNumClusters());
    
    result.add("-B");
    result.add("" + getBinValue());
    
    if (getUseKDTree()) {
      result.add("-use-kdtree");
      result.add("-K");
      result.add("" + getKDTreeSpec());
    }
    
    result.add("-C");
    result.add("" + getCutOffFactor());
    
    if (getDistanceF() != null) {
      result.add("-D");
      result.add("" + getDistanceFSpec());
    }
    
    if ((getInputCenterFile().exists()) && (getInputCenterFile().isFile())) {
      result.add("-N");
      result.add("" + getInputCenterFile());
    }
    
    if ((getOutputCenterFile().exists()) && (getOutputCenterFile().isFile())) {
      result.add("-O");
      result.add("" + getOutputCenterFile());
    }
    
    int dL = getDebugLevel();
    if (dL > 0) {
      result.add("-U");
      result.add("" + getDebugLevel());
    }
    
    if ((getDebugVectorsFile().exists()) && (getDebugVectorsFile().isFile())) {
      result.add("-Y");
      result.add("" + getDebugVectorsFile());
    }
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  



  public String toString()
  {
    StringBuffer temp = new StringBuffer();
    
    temp.append("\nXMeans\n======\n");
    
    temp.append("Requested iterations            : " + m_MaxIterations + "\n");
    temp.append("Iterations performed            : " + m_IterationCount + "\n");
    if (m_KMeansStopped > 0) {
      temp.append("kMeans did not converge\n");
      temp.append("  but was stopped by max-loops " + m_KMeansStopped + " times (max kMeans-iter)\n");
    }
    
    temp.append("Splits prepared                 : " + m_NumSplits + "\n");
    temp.append("Splits performed                : " + m_NumSplitsDone + "\n");
    temp.append("Cutoff factor                   : " + m_CutOffFactor + "\n");
    double perc;
    double perc; if (m_NumSplitsDone > 0) {
      perc = m_NumSplitsStillDone / m_NumSplitsDone * 100.0D;
    }
    else
      perc = 0.0D;
    temp.append("Percentage of splits accepted \nby cutoff factor                : " + Utils.doubleToString(perc, 2) + " %\n");
    

    temp.append("------\n");
    
    temp.append("Cutoff factor                   : " + m_CutOffFactor + "\n");
    temp.append("------\n");
    temp.append("\nCluster centers                 : " + m_NumClusters + " centers\n");
    for (int i = 0; i < m_NumClusters; i++) {
      temp.append("\nCluster " + i + "\n           ");
      for (int j = 0; j < m_ClusterCenters.numAttributes(); j++) {
        if (m_ClusterCenters.attribute(j).isNominal()) {
          temp.append(" " + m_ClusterCenters.attribute(j).value((int)m_ClusterCenters.instance(i).value(j)));
        }
        else {
          temp.append(" " + m_ClusterCenters.instance(i).value(j));
        }
      }
    }
    if (m_Mle != null) {
      temp.append("\n\nDistortion: " + Utils.doubleToString(Utils.sum(m_Mle), 6) + "\n");
    }
    temp.append("BIC-Value : " + Utils.doubleToString(m_Bic, 6) + "\n");
    return temp.toString();
  }
  




  public Instances getClusterCenters()
  {
    return m_ClusterCenters;
  }
  



  protected void PrCentersFD(int debugLevel)
  {
    if (debugLevel == m_DebugLevel) {
      for (int i = 0; i < m_ClusterCenters.numInstances(); i++) {
        System.out.println(m_ClusterCenters.instance(i));
      }
    }
  }
  




  protected boolean TFD(int debugLevel)
  {
    return debugLevel == m_DebugLevel;
  }
  




  protected void PFD(int debugLevel, String output)
  {
    if (debugLevel == m_DebugLevel) {
      System.out.println(output);
    }
  }
  

  protected void PFD_CURR(String output)
  {
    if (m_CurrDebugFlag) {
      System.out.println(output);
    }
  }
  



  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9986 $");
  }
  



  public static void main(String[] argv)
  {
    runClusterer(new XMeans(), argv);
  }
}
