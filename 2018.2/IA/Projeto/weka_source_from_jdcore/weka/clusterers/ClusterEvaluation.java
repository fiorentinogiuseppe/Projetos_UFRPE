package weka.clusterers;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;












































































public class ClusterEvaluation
  implements Serializable, RevisionHandler
{
  static final long serialVersionUID = -830188327319128005L;
  private Clusterer m_Clusterer;
  private StringBuffer m_clusteringResults;
  private int m_numClusters;
  private double[] m_clusterAssignments;
  private double m_logL;
  private int[] m_classToCluster = null;
  



  public void setClusterer(Clusterer clusterer)
  {
    m_Clusterer = clusterer;
  }
  



  public String clusterResultsToString()
  {
    return m_clusteringResults.toString();
  }
  




  public int getNumClusters()
  {
    return m_numClusters;
  }
  




  public double[] getClusterAssignments()
  {
    return m_clusterAssignments;
  }
  




  public int[] getClassesToClusters()
  {
    return m_classToCluster;
  }
  





  public double getLogLikelihood()
  {
    return m_logL;
  }
  



  public ClusterEvaluation()
  {
    setClusterer(new SimpleKMeans());
    m_clusteringResults = new StringBuffer();
    m_clusterAssignments = null;
  }
  






  public void evaluateClusterer(Instances test)
    throws Exception
  {
    evaluateClusterer(test, "");
  }
  










  public void evaluateClusterer(Instances test, String testFileName)
    throws Exception
  {
    evaluateClusterer(test, testFileName, true);
  }
  












  public void evaluateClusterer(Instances test, String testFileName, boolean outputModel)
    throws Exception
  {
    int i = 0;
    
    double loglk = 0.0D;
    int cc = m_Clusterer.numberOfClusters();
    m_numClusters = cc;
    double[] instanceStats = new double[cc];
    Instances testRaw = null;
    boolean hasClass = test.classIndex() >= 0;
    int unclusteredInstances = 0;
    Vector<Double> clusterAssignments = new Vector();
    Filter filter = null;
    ConverterUtils.DataSource source = null;
    

    if (testFileName == null) {
      testFileName = "";
    }
    
    if (testFileName.length() != 0) {
      source = new ConverterUtils.DataSource(testFileName);
    } else
      source = new ConverterUtils.DataSource(test);
    testRaw = source.getStructure(test.classIndex());
    

    if (hasClass) {
      if (testRaw.classAttribute().isNumeric()) {
        throw new Exception("ClusterEvaluation: Class must be nominal!");
      }
      filter = new Remove();
      ((Remove)filter).setAttributeIndices("" + (testRaw.classIndex() + 1));
      ((Remove)filter).setInvertSelection(false);
      filter.setInputFormat(testRaw);
    }
    
    i = 0;
    while (source.hasMoreElements(testRaw))
    {
      Instance inst = source.nextElement(testRaw);
      if (filter != null) {
        filter.input(inst);
        filter.batchFinished();
        inst = filter.output();
      }
      
      int cnum = -1;
      try {
        if ((m_Clusterer instanceof DensityBasedClusterer)) {
          loglk += ((DensityBasedClusterer)m_Clusterer).logDensityForInstance(inst);
          
          cnum = m_Clusterer.clusterInstance(inst);
          clusterAssignments.add(Double.valueOf(cnum));
        }
        else {
          cnum = m_Clusterer.clusterInstance(inst);
          clusterAssignments.add(Double.valueOf(cnum));
        }
      }
      catch (Exception e) {
        clusterAssignments.add(Double.valueOf(-1.0D));
        unclusteredInstances++;
      }
      
      if (cnum != -1) {
        instanceStats[cnum] += 1.0D;
      }
    }
    
    double sum = Utils.sum(instanceStats);
    loglk /= sum;
    m_logL = loglk;
    m_clusterAssignments = new double[clusterAssignments.size()];
    for (i = 0; i < clusterAssignments.size(); i++)
      m_clusterAssignments[i] = ((Double)clusterAssignments.get(i)).doubleValue();
    int numInstFieldWidth = (int)(Math.log(clusterAssignments.size()) / Math.log(10.0D) + 1.0D);
    
    if (outputModel) {
      m_clusteringResults.append(m_Clusterer.toString());
    }
    m_clusteringResults.append("Clustered Instances\n\n");
    int clustFieldWidth = (int)(Math.log(cc) / Math.log(10.0D) + 1.0D);
    for (i = 0; i < cc; i++) {
      if (instanceStats[i] > 0.0D) {
        m_clusteringResults.append(Utils.doubleToString(i, clustFieldWidth, 0) + "      " + Utils.doubleToString(instanceStats[i], numInstFieldWidth, 0) + " (" + Utils.doubleToString(instanceStats[i] / sum * 100.0D, 3, 0) + "%)\n");
      }
    }
    







    if (unclusteredInstances > 0) {
      m_clusteringResults.append("\nUnclustered instances : " + unclusteredInstances);
    }
    
    if ((m_Clusterer instanceof DensityBasedClusterer)) {
      m_clusteringResults.append("\n\nLog likelihood: " + Utils.doubleToString(loglk, 1, 5) + "\n");
    }
    

    if (hasClass) {
      evaluateClustersWithRespectToClass(test, testFileName);
    }
  }
  









  private void evaluateClustersWithRespectToClass(Instances inst, String fileName)
    throws Exception
  {
    int numClasses = inst.classAttribute().numValues();
    int[][] counts = new int[m_numClusters][numClasses];
    int[] clusterTotals = new int[m_numClusters];
    double[] best = new double[m_numClusters + 1];
    double[] current = new double[m_numClusters + 1];
    ConverterUtils.DataSource source = null;
    Instances instances = null;
    Instance instance = null;
    


    if (fileName == null) {
      fileName = "";
    }
    if (fileName.length() != 0) {
      source = new ConverterUtils.DataSource(fileName);
    } else
      source = new ConverterUtils.DataSource(inst);
    instances = source.getStructure(inst.classIndex());
    
    int i = 0;
    while (source.hasMoreElements(instances)) {
      instance = source.nextElement(instances);
      if (m_clusterAssignments[i] >= 0.0D) {
        counts[((int)m_clusterAssignments[i])][((int)instance.classValue())] += 1;
        clusterTotals[((int)m_clusterAssignments[i])] += 1;
      }
      i++;
    }
    int numInstances = i;
    
    best[m_numClusters] = Double.MAX_VALUE;
    mapClasses(m_numClusters, 0, counts, clusterTotals, current, best, 0);
    
    m_clusteringResults.append("\n\nClass attribute: " + inst.classAttribute().name() + "\n");
    

    m_clusteringResults.append("Classes to Clusters:\n");
    String matrixString = toMatrixString(counts, clusterTotals, new Instances(inst, 0));
    m_clusteringResults.append(matrixString).append("\n");
    
    int Cwidth = 1 + (int)(Math.log(m_numClusters) / Math.log(10.0D));
    
    for (i = 0; i < m_numClusters; i++) {
      if (clusterTotals[i] > 0) {
        m_clusteringResults.append("Cluster " + Utils.doubleToString(i, Cwidth, 0));
        
        m_clusteringResults.append(" <-- ");
        
        if (best[i] < 0.0D) {
          m_clusteringResults.append("No class\n");
        } else {
          m_clusteringResults.append(inst.classAttribute().value((int)best[i])).append("\n");
        }
      }
    }
    
    m_clusteringResults.append("\nIncorrectly clustered instances :\t" + best[m_numClusters] + "\t" + Utils.doubleToString(best[m_numClusters] / numInstances * 100.0D, 8, 4) + " %\n");
    






    m_classToCluster = new int[m_numClusters];
    for (i = 0; i < m_numClusters; i++) {
      m_classToCluster[i] = ((int)best[i]);
    }
  }
  








  private String toMatrixString(int[][] counts, int[] clusterTotals, Instances inst)
    throws Exception
  {
    StringBuffer ms = new StringBuffer();
    
    int maxval = 0;
    for (int i = 0; i < m_numClusters; i++) {
      for (int j = 0; j < counts[i].length; j++) {
        if (counts[i][j] > maxval) {
          maxval = counts[i][j];
        }
      }
    }
    
    int Cwidth = 1 + Math.max((int)(Math.log(maxval) / Math.log(10.0D)), (int)(Math.log(m_numClusters) / Math.log(10.0D)));
    

    ms.append("\n");
    
    for (int i = 0; i < m_numClusters; i++) {
      if (clusterTotals[i] > 0) {
        ms.append(" ").append(Utils.doubleToString(i, Cwidth, 0));
      }
    }
    ms.append("  <-- assigned to cluster\n");
    
    for (int i = 0; i < counts[0].length; i++)
    {
      for (int j = 0; j < m_numClusters; j++) {
        if (clusterTotals[j] > 0) {
          ms.append(" ").append(Utils.doubleToString(counts[j][i], Cwidth, 0));
        }
      }
      
      ms.append(" | ").append(inst.classAttribute().value(i)).append("\n");
    }
    
    return ms.toString();
  }
  














  public static void mapClasses(int numClusters, int lev, int[][] counts, int[] clusterTotals, double[] current, double[] best, int error)
  {
    if (lev == numClusters) {
      if (error < best[numClusters]) {
        best[numClusters] = error;
        for (int i = 0; i < numClusters; i++) {
          best[i] = current[i];
        }
        
      }
    }
    else if (clusterTotals[lev] == 0) {
      current[lev] = -1.0D;
      mapClasses(numClusters, lev + 1, counts, clusterTotals, current, best, error);
    }
    else
    {
      current[lev] = -1.0D;
      mapClasses(numClusters, lev + 1, counts, clusterTotals, current, best, error + clusterTotals[lev]);
      

      for (int i = 0; i < counts[0].length; i++) {
        if (counts[lev][i] > 0) {
          boolean ok = true;
          
          for (int j = 0; j < lev; j++) {
            if ((int)current[j] == i) {
              ok = false;
              break;
            }
          }
          if (ok) {
            current[lev] = i;
            mapClasses(numClusters, lev + 1, counts, clusterTotals, current, best, error + (clusterTotals[lev] - counts[lev][i]));
          }
        }
      }
    }
  }
  





















  public static String evaluateClusterer(Clusterer clusterer, String[] options)
    throws Exception
  {
    int seed = 1;int folds = 10;
    boolean doXval = false;
    Instances train = null;
    



    String[] savedOptions = null;
    boolean printClusterAssignments = false;
    Range attributesToOutput = null;
    StringBuffer text = new StringBuffer();
    int theClass = -1;
    boolean updateable = clusterer instanceof UpdateableClusterer;
    ConverterUtils.DataSource source = null;
    

    if ((Utils.getFlag('h', options)) || (Utils.getFlag("help", options)))
    {

      boolean globalInfo = (Utils.getFlag("synopsis", options)) || (Utils.getFlag("info", options));
      

      throw new Exception("Help requested." + makeOptionString(clusterer, globalInfo));
    }
    String objectInputFileName;
    String objectOutputFileName;
    String trainFileName;
    String testFileName;
    String graphFileName;
    try { objectInputFileName = Utils.getOption('l', options);
      objectOutputFileName = Utils.getOption('d', options);
      trainFileName = Utils.getOption('t', options);
      testFileName = Utils.getOption('T', options);
      graphFileName = Utils.getOption('g', options);
      String attributeRangeString;
      try
      {
        attributeRangeString = Utils.getOption('p', options);
      }
      catch (Exception e) {
        throw new Exception(e.getMessage() + "\nNOTE: the -p option has changed. " + "It now expects a parameter specifying a range of attributes " + "to list with the predictions. Use '-p 0' for none.");
      }
      

      if (attributeRangeString.length() != 0) {
        printClusterAssignments = true;
        if (!attributeRangeString.equals("0")) {
          attributesToOutput = new Range(attributeRangeString);
        }
      }
      if (trainFileName.length() == 0) {
        if (objectInputFileName.length() == 0) {
          throw new Exception("No training file and no object input file given.");
        }
        

        if (testFileName.length() == 0) {
          throw new Exception("No training file and no test file given.");
        }
        
      }
      else if ((objectInputFileName.length() != 0) && (!printClusterAssignments))
      {
        throw new Exception("Can't use both train and model file unless -p specified.");
      }
      


      String seedString = Utils.getOption('s', options);
      
      if (seedString.length() != 0) {
        seed = Integer.parseInt(seedString);
      }
      
      String foldsString = Utils.getOption('x', options);
      
      if (foldsString.length() != 0) {
        folds = Integer.parseInt(foldsString);
        doXval = true;
      }
    }
    catch (Exception e) {
      throw new Exception('\n' + e.getMessage() + makeOptionString(clusterer, false));
    }
    
    try
    {
      if (trainFileName.length() != 0) {
        source = new ConverterUtils.DataSource(trainFileName);
        train = source.getStructure();
        
        String classString = Utils.getOption('c', options);
        if (classString.length() != 0) {
          if (classString.compareTo("last") == 0) {
            theClass = train.numAttributes();
          } else if (classString.compareTo("first") == 0) {
            theClass = 1;
          } else {
            theClass = Integer.parseInt(classString);
          }
          if (theClass != -1) {
            if ((doXval) || (testFileName.length() != 0)) {
              throw new Exception("Can only do class based evaluation on the training data");
            }
            
            if (objectInputFileName.length() != 0) {
              throw new Exception("Can't load a clusterer and do class based evaluation");
            }
            
            if (objectOutputFileName.length() != 0) {
              throw new Exception("Can't do class based evaluation and save clusterer");
            }
            
          }
          
        }
        else if (train.classIndex() != -1) {
          theClass = train.classIndex() + 1;
          System.err.println("Note: using class attribute from dataset, i.e., attribute #" + theClass);
        }
        



        if (theClass != -1) {
          if ((theClass < 1) || (theClass > train.numAttributes())) {
            throw new Exception("Class is out of range!");
          }
          if (!train.attribute(theClass - 1).isNominal()) {
            throw new Exception("Class must be nominal!");
          }
          train.setClassIndex(theClass - 1);
        }
      }
    }
    catch (Exception e) {
      throw new Exception("ClusterEvaluation: " + e.getMessage() + '.');
    }
    

    if (options != null) {
      savedOptions = new String[options.length];
      System.arraycopy(options, 0, savedOptions, 0, options.length);
    }
    
    if (objectInputFileName.length() != 0) {
      Utils.checkForRemainingOptions(options);
    }
    
    if ((clusterer instanceof OptionHandler)) {
      ((OptionHandler)clusterer).setOptions(options);
    }
    Utils.checkForRemainingOptions(options);
    
    Instances trainHeader = train;
    if (objectInputFileName.length() != 0)
    {

      ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(objectInputFileName)));
      


      clusterer = (Clusterer)ois.readObject();
      try
      {
        trainHeader = (Instances)ois.readObject();


      }
      catch (Exception ex) {}

    }
    else if (theClass == -1) {
      if (updateable) {
        clusterer.buildClusterer(source.getStructure());
        while (source.hasMoreElements(train)) {
          Instance inst = source.nextElement(train);
          ((UpdateableClusterer)clusterer).updateClusterer(inst);
        }
        ((UpdateableClusterer)clusterer).updateFinished();
      }
      else {
        clusterer.buildClusterer(source.getDataSet());
      }
    }
    else {
      Remove removeClass = new Remove();
      removeClass.setAttributeIndices("" + theClass);
      removeClass.setInvertSelection(false);
      removeClass.setInputFormat(train);
      if (updateable) {
        Instances clusterTrain = Filter.useFilter(train, removeClass);
        clusterer.buildClusterer(clusterTrain);
        trainHeader = clusterTrain;
        while (source.hasMoreElements(train)) {
          Instance inst = source.nextElement(train);
          removeClass.input(inst);
          removeClass.batchFinished();
          Instance clusterTrainInst = removeClass.output();
          ((UpdateableClusterer)clusterer).updateClusterer(clusterTrainInst);
        }
        ((UpdateableClusterer)clusterer).updateFinished();
      }
      else {
        Instances clusterTrain = Filter.useFilter(source.getDataSet(), removeClass);
        clusterer.buildClusterer(clusterTrain);
        trainHeader = clusterTrain;
      }
      ClusterEvaluation ce = new ClusterEvaluation();
      ce.setClusterer(clusterer);
      ce.evaluateClusterer(train, trainFileName);
      
      return "\n\n=== Clustering stats for training data ===\n\n" + ce.clusterResultsToString();
    }
    




    if (printClusterAssignments) {
      return printClusterings(clusterer, trainFileName, testFileName, attributesToOutput);
    }
    
    text.append(clusterer.toString());
    text.append("\n\n=== Clustering stats for training data ===\n\n" + printClusterStats(clusterer, trainFileName));
    

    if (testFileName.length() != 0)
    {
      ConverterUtils.DataSource test = new ConverterUtils.DataSource(testFileName);
      Instances testStructure = test.getStructure();
      if (!trainHeader.equalHeaders(testStructure)) {
        throw new Exception("Training and testing data are not compatible");
      }
      
      text.append("\n\n=== Clustering stats for testing data ===\n\n" + printClusterStats(clusterer, testFileName));
    }
    

    if (((clusterer instanceof DensityBasedClusterer)) && (doXval == true) && (testFileName.length() == 0) && (objectInputFileName.length() == 0))
    {



      Random random = new Random(seed);
      random.setSeed(seed);
      train = source.getDataSet();
      train.randomize(random);
      text.append(crossValidateModel(clusterer.getClass().getName(), train, folds, savedOptions, random));
    }
    



    if (objectOutputFileName.length() != 0)
    {
      saveClusterer(objectOutputFileName, clusterer, trainHeader);
    }
    

    if (((clusterer instanceof Drawable)) && (graphFileName.length() != 0)) {
      BufferedWriter writer = new BufferedWriter(new FileWriter(graphFileName));
      writer.write(((Drawable)clusterer).graph());
      writer.newLine();
      writer.flush();
      writer.close();
    }
    
    return text.toString();
  }
  
  private static void saveClusterer(String fileName, Clusterer clusterer, Instances header)
    throws Exception
  {
    ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
    



    oos.writeObject(clusterer);
    if (header != null) {
      oos.writeObject(header);
    }
    oos.flush();
    oos.close();
  }
  












  public static double crossValidateModel(DensityBasedClusterer clusterer, Instances data, int numFolds, Random random)
    throws Exception
  {
    double foldAv = 0.0D;
    data = new Instances(data);
    data.randomize(random);
    
    for (int i = 0; i < numFolds; i++)
    {
      Instances train = data.trainCV(numFolds, i, random);
      
      clusterer.buildClusterer(train);
      
      Instances test = data.testCV(numFolds, i);
      
      for (int j = 0; j < test.numInstances(); j++) {
        try {
          foldAv += clusterer.logDensityForInstance(test.instance(j));
        }
        catch (Exception ex) {}
      }
    }
    





    return foldAv / data.numInstances();
  }
  
















  public static String crossValidateModel(String clustererString, Instances data, int numFolds, String[] options, Random random)
    throws Exception
  {
    Clusterer clusterer = null;
    String[] savedOptions = null;
    double CvAv = 0.0D;
    StringBuffer CvString = new StringBuffer();
    
    if (options != null) {
      savedOptions = new String[options.length];
    }
    
    data = new Instances(data);
    
    try
    {
      clusterer = (Clusterer)Class.forName(clustererString).newInstance();
    }
    catch (Exception e) {
      throw new Exception("Can't find class with name " + clustererString + '.');
    }
    

    if (!(clusterer instanceof DensityBasedClusterer)) {
      throw new Exception(clustererString + " must be a distrinbution " + "clusterer.");
    }
    



    if (options != null) {
      System.arraycopy(options, 0, savedOptions, 0, options.length);
    }
    

    if ((clusterer instanceof OptionHandler)) {
      try {
        ((OptionHandler)clusterer).setOptions(savedOptions);
        Utils.checkForRemainingOptions(savedOptions);
      }
      catch (Exception e) {
        throw new Exception("Can't parse given options in cross-validation!");
      }
    }
    
    CvAv = crossValidateModel((DensityBasedClusterer)clusterer, data, numFolds, random);
    
    CvString.append("\n" + numFolds + " fold CV Log Likelihood: " + Utils.doubleToString(CvAv, 6, 4) + "\n");
    


    return CvString.toString();
  }
  













  private static String printClusterStats(Clusterer clusterer, String fileName)
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    int i = 0;
    
    double loglk = 0.0D;
    int cc = clusterer.numberOfClusters();
    double[] instanceStats = new double[cc];
    int unclusteredInstances = 0;
    
    if (fileName.length() != 0) {
      ConverterUtils.DataSource source = new ConverterUtils.DataSource(fileName);
      Instances structure = source.getStructure();
      
      while (source.hasMoreElements(structure)) {
        Instance inst = source.nextElement(structure);
        try {
          int cnum = clusterer.clusterInstance(inst);
          
          if ((clusterer instanceof DensityBasedClusterer)) {
            loglk += ((DensityBasedClusterer)clusterer).logDensityForInstance(inst);
          }
          

          instanceStats[cnum] += 1.0D;
        }
        catch (Exception e) {
          unclusteredInstances++;
        }
        i++;
      }
      




















      int clustFieldWidth = (int)(Math.log(cc) / Math.log(10.0D) + 1.0D);
      int numInstFieldWidth = (int)(Math.log(i) / Math.log(10.0D) + 1.0D);
      double sum = Utils.sum(instanceStats);
      loglk /= sum;
      text.append("Clustered Instances\n");
      
      for (i = 0; i < cc; i++) {
        if (instanceStats[i] > 0.0D) {
          text.append(Utils.doubleToString(i, clustFieldWidth, 0) + "      " + Utils.doubleToString(instanceStats[i], numInstFieldWidth, 0) + " (" + Utils.doubleToString(instanceStats[i] / sum * 100.0D, 3, 0) + "%)\n");
        }
      }
      






      if (unclusteredInstances > 0) {
        text.append("\nUnclustered Instances : " + unclusteredInstances);
      }
      
      if ((clusterer instanceof DensityBasedClusterer)) {
        text.append("\n\nLog likelihood: " + Utils.doubleToString(loglk, 1, 5) + "\n");
      }
    }
    


    return text.toString();
  }
  













  private static String printClusterings(Clusterer clusterer, String trainFileName, String testFileName, Range attributesToOutput)
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    int i = 0;
    
    ConverterUtils.DataSource source = null;
    


    if (testFileName.length() != 0) {
      source = new ConverterUtils.DataSource(testFileName);
    } else {
      source = new ConverterUtils.DataSource(trainFileName);
    }
    Instances structure = source.getStructure();
    while (source.hasMoreElements(structure)) {
      Instance inst = source.nextElement(structure);
      try {
        int cnum = clusterer.clusterInstance(inst);
        
        text.append(i + " " + cnum + " " + attributeValuesString(inst, attributesToOutput) + "\n");

      }
      catch (Exception e)
      {

        text.append(i + " Unclustered " + attributeValuesString(inst, attributesToOutput) + "\n");
      }
      
      i++;
    }
    
    return text.toString();
  }
  







  private static String attributeValuesString(Instance instance, Range attRange)
  {
    StringBuffer text = new StringBuffer();
    if (attRange != null) {
      boolean firstOutput = true;
      attRange.setUpper(instance.numAttributes() - 1);
      for (int i = 0; i < instance.numAttributes(); i++)
        if (attRange.isInRange(i)) {
          if (firstOutput) text.append("("); else
            text.append(",");
          text.append(instance.toString(i));
          firstOutput = false;
        }
      if (!firstOutput) text.append(")");
    }
    return text.toString();
  }
  






  private static String makeOptionString(Clusterer clusterer, boolean globalInfo)
  {
    StringBuffer optionsText = new StringBuffer("");
    
    optionsText.append("\n\nGeneral options:\n\n");
    optionsText.append("-h or -help\n");
    optionsText.append("\tOutput help information.\n");
    optionsText.append("-synopsis or -info\n");
    optionsText.append("\tOutput synopsis for clusterer (use in conjunction  with -h)\n");
    
    optionsText.append("-t <name of training file>\n");
    optionsText.append("\tSets training file.\n");
    optionsText.append("-T <name of test file>\n");
    optionsText.append("\tSets test file.\n");
    optionsText.append("-l <name of input file>\n");
    optionsText.append("\tSets model input file.\n");
    optionsText.append("-d <name of output file>\n");
    optionsText.append("\tSets model output file.\n");
    optionsText.append("-p <attribute range>\n");
    optionsText.append("\tOutput predictions. Predictions are for training file\n\tif only training file is specified,\n\totherwise predictions are for the test file.\n\tThe range specifies attribute values to be output\n\twith the predictions. Use '-p 0' for none.\n");
    




    optionsText.append("-x <number of folds>\n");
    optionsText.append("\tOnly Distribution Clusterers can be cross validated.\n");
    optionsText.append("-s <random number seed>\n");
    optionsText.append("\tSets the seed for randomizing the data in cross-validation\n");
    optionsText.append("-c <class index>\n");
    optionsText.append("\tSet class attribute. If supplied, class is ignored");
    optionsText.append("\n\tduring clustering but is used in a classes to");
    optionsText.append("\n\tclusters evaluation.\n");
    if ((clusterer instanceof Drawable)) {
      optionsText.append("-g <name of graph file>\n");
      optionsText.append("\tOutputs the graph representation of the clusterer to the file.\n");
    }
    

    if ((clusterer instanceof OptionHandler)) {
      optionsText.append("\nOptions specific to " + clusterer.getClass().getName() + ":\n\n");
      
      Enumeration enu = ((OptionHandler)clusterer).listOptions();
      
      while (enu.hasMoreElements()) {
        Option option = (Option)enu.nextElement();
        optionsText.append(option.synopsis() + '\n');
        optionsText.append(option.description() + "\n");
      }
    }
    

    if (globalInfo) {
      try {
        String gi = getGlobalInfo(clusterer);
        optionsText.append(gi);
      }
      catch (Exception ex) {}
    }
    

    return optionsText.toString();
  }
  





  protected static String getGlobalInfo(Clusterer clusterer)
    throws Exception
  {
    BeanInfo bi = Introspector.getBeanInfo(clusterer.getClass());
    
    MethodDescriptor[] methods = bi.getMethodDescriptors();
    Object[] args = new Object[0];
    String result = "\nSynopsis for " + clusterer.getClass().getName() + ":\n\n";
    

    for (int i = 0; i < methods.length; i++) {
      String name = methods[i].getDisplayName();
      Method meth = methods[i].getMethod();
      if (name.equals("globalInfo")) {
        String globalInfo = (String)meth.invoke(clusterer, args);
        result = result + globalInfo;
        break;
      }
    }
    
    return result;
  }
  






  public boolean equals(Object obj)
  {
    if ((obj == null) || (!obj.getClass().equals(getClass()))) {
      return false;
    }
    ClusterEvaluation cmp = (ClusterEvaluation)obj;
    
    if ((m_classToCluster != null ? 1 : 0) != (m_classToCluster != null ? 1 : 0)) return false;
    if (m_classToCluster != null) {
      for (int i = 0; i < m_classToCluster.length; i++) {
        if (m_classToCluster[i] != m_classToCluster[i]) {
          return false;
        }
      }
    }
    if ((m_clusterAssignments != null ? 1 : 0) != (m_clusterAssignments != null ? 1 : 0)) return false;
    if (m_clusterAssignments != null) {
      for (int i = 0; i < m_clusterAssignments.length; i++) {
        if (m_clusterAssignments[i] != m_clusterAssignments[i]) {
          return false;
        }
      }
    }
    if (Double.isNaN(m_logL) != Double.isNaN(m_logL)) return false;
    if ((!Double.isNaN(m_logL)) && 
      (m_logL != m_logL)) { return false;
    }
    
    if (m_numClusters != m_numClusters) { return false;
    }
    
    String clusteringResults1 = m_clusteringResults.toString().replaceAll("Elapsed time.*", "");
    String clusteringResults2 = m_clusteringResults.toString().replaceAll("Elapsed time.*", "");
    if (!clusteringResults1.equals(clusteringResults2)) { return false;
    }
    return true;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7753 $");
  }
  



  public static void main(String[] args)
  {
    try
    {
      if (args.length == 0) {
        throw new Exception("The first argument must be the name of a clusterer");
      }
      

      String ClustererString = args[0];
      args[0] = "";
      Clusterer newClusterer = AbstractClusterer.forName(ClustererString, null);
      System.out.println(evaluateClusterer(newClusterer, args));
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
