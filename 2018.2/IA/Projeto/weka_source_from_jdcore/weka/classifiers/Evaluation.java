package weka.classifiers;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.pmml.consumer.PMMLClassifier;
import weka.classifiers.xml.XMLClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Drawable;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Summarizable;
import weka.core.Utils;
import weka.core.Version;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.pmml.PMMLFactory;
import weka.core.pmml.PMMLModel;
import weka.core.xml.KOML;
import weka.core.xml.XMLOptions;
import weka.core.xml.XMLSerialization;
import weka.estimators.Estimator;
import weka.estimators.KernelEstimator;

















































































































































































































public class Evaluation
  implements Summarizable, RevisionHandler
{
  protected int m_NumClasses;
  protected int m_NumFolds;
  protected double m_Incorrect;
  protected double m_Correct;
  protected double m_Unclassified;
  protected double m_MissingClass;
  protected double m_WithClass;
  protected double[][] m_ConfusionMatrix;
  protected String[] m_ClassNames;
  protected boolean m_ClassIsNominal;
  protected double[] m_ClassPriors;
  protected double m_ClassPriorsSum;
  protected CostMatrix m_CostMatrix;
  protected double m_TotalCost;
  protected double m_SumErr;
  protected double m_SumAbsErr;
  protected double m_SumSqrErr;
  protected double m_SumClass;
  protected double m_SumSqrClass;
  protected double m_SumPredicted;
  protected double m_SumSqrPredicted;
  protected double m_SumClassPredicted;
  protected double m_SumPriorAbsErr;
  protected double m_SumPriorSqrErr;
  protected double m_SumKBInfo;
  protected static int k_MarginResolution = 500;
  


  protected double[] m_MarginCounts;
  


  protected int m_NumTrainClassVals;
  


  protected double[] m_TrainClassVals;
  


  protected double[] m_TrainClassWeights;
  


  protected Estimator m_PriorErrorEstimator;
  


  protected Estimator m_ErrorEstimator;
  

  protected static final double MIN_SF_PROB = Double.MIN_VALUE;
  

  protected double m_SumPriorEntropy;
  

  protected double m_SumSchemeEntropy;
  

  private FastVector m_Predictions;
  

  protected boolean m_NoPriors = false;
  











  public Evaluation(Instances data)
    throws Exception
  {
    this(data, null);
  }
  













  public Evaluation(Instances data, CostMatrix costMatrix)
    throws Exception
  {
    m_NumClasses = data.numClasses();
    m_NumFolds = 1;
    m_ClassIsNominal = data.classAttribute().isNominal();
    
    if (m_ClassIsNominal) {
      m_ConfusionMatrix = new double[m_NumClasses][m_NumClasses];
      m_ClassNames = new String[m_NumClasses];
      for (int i = 0; i < m_NumClasses; i++) {
        m_ClassNames[i] = data.classAttribute().value(i);
      }
    }
    m_CostMatrix = costMatrix;
    if (m_CostMatrix != null) {
      if (!m_ClassIsNominal) {
        throw new Exception("Class has to be nominal if cost matrix given!");
      }
      
      if (m_CostMatrix.size() != m_NumClasses) {
        throw new Exception("Cost matrix not compatible with data!");
      }
    }
    m_ClassPriors = new double[m_NumClasses];
    setPriors(data);
    m_MarginCounts = new double[k_MarginResolution + 1];
  }
  









  public double areaUnderROC(int classIndex)
  {
    if (m_Predictions == null) {
      return Instance.missingValue();
    }
    ThresholdCurve tc = new ThresholdCurve();
    Instances result = tc.getCurve(m_Predictions, classIndex);
    return ThresholdCurve.getROCArea(result);
  }
  





  public double weightedAreaUnderROC()
  {
    double[] classCounts = new double[m_NumClasses];
    double classCountSum = 0.0D;
    
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumClasses; j++) {
        classCounts[i] += m_ConfusionMatrix[i][j];
      }
      classCountSum += classCounts[i];
    }
    
    double aucTotal = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      double temp = areaUnderROC(i);
      if (!Instance.isMissingValue(temp)) {
        aucTotal += temp * classCounts[i];
      }
    }
    
    return aucTotal / classCountSum;
  }
  





  public double[][] confusionMatrix()
  {
    double[][] newMatrix = new double[m_ConfusionMatrix.length][0];
    
    for (int i = 0; i < m_ConfusionMatrix.length; i++) {
      newMatrix[i] = new double[m_ConfusionMatrix[i].length];
      System.arraycopy(m_ConfusionMatrix[i], 0, newMatrix[i], 0, m_ConfusionMatrix[i].length);
    }
    
    return newMatrix;
  }
  



















  public void crossValidateModel(Classifier classifier, Instances data, int numFolds, Random random, Object... forPredictionsPrinting)
    throws Exception
  {
    data = new Instances(data);
    data.randomize(random);
    if (data.classAttribute().isNominal()) {
      data.stratify(numFolds);
    }
    





    if (forPredictionsPrinting.length > 0)
    {
      StringBuffer buff = (StringBuffer)forPredictionsPrinting[0];
      Range attsToOutput = (Range)forPredictionsPrinting[1];
      boolean printDist = ((Boolean)forPredictionsPrinting[2]).booleanValue();
      printClassificationsHeader(data, attsToOutput, printDist, buff);
    }
    

    for (int i = 0; i < numFolds; i++) {
      Instances train = data.trainCV(numFolds, i, random);
      setPriors(train);
      Classifier copiedClassifier = Classifier.makeCopy(classifier);
      copiedClassifier.buildClassifier(train);
      Instances test = data.testCV(numFolds, i);
      evaluateModel(copiedClassifier, test, forPredictionsPrinting);
    }
    m_NumFolds = numFolds;
  }
  













  public void crossValidateModel(String classifierString, Instances data, int numFolds, String[] options, Random random)
    throws Exception
  {
    crossValidateModel(Classifier.forName(classifierString, options), data, numFolds, random, new Object[0]);
  }
  


























































  public static String evaluateModel(String classifierString, String[] options)
    throws Exception
  {
    Classifier classifier;
    
























































    try
    {
      classifier = (Classifier)Class.forName(classifierString).newInstance();
    } catch (Exception e) {
      throw new Exception("Can't find class with name " + classifierString + '.');
    }
    
    return evaluateModel(classifier, options);
  }
  






  public static void main(String[] args)
  {
    try
    {
      if (args.length == 0) {
        throw new Exception("The first argument must be the class name of a classifier");
      }
      
      String classifier = args[0];
      args[0] = "";
      System.out.println(evaluateModel(classifier, args));
    } catch (Exception ex) {
      ex.printStackTrace();
      System.err.println(ex.getMessage());
    }
  }
  







































































































  public static String evaluateModel(Classifier classifier, String[] options)
    throws Exception
  {
    Instances train = null;Instances test = null;Instances template = null;
    int seed = 1;int folds = 10;int classIndex = -1;
    boolean noCrossValidation = false;
    
    boolean noOutput = false;boolean printClassifications = false;boolean trainStatistics = true;
    boolean printMargins = false;boolean printComplexityStatistics = false;boolean printGraph = false;
    boolean classStatistics = false;boolean printSource = false;
    StringBuffer text = new StringBuffer();
    ConverterUtils.DataSource trainSource = null;ConverterUtils.DataSource testSource = null;
    ObjectInputStream objectInputStream = null;
    BufferedInputStream xmlInputStream = null;
    CostMatrix costMatrix = null;
    StringBuffer schemeOptionsText = null;
    Range attributesToOutput = null;
    long trainTimeStart = 0L;long trainTimeElapsed = 0L;long testTimeStart = 0L;long testTimeElapsed = 0L;
    
    String xml = "";
    String[] optionsTmp = null;
    
    boolean printDistribution = false;
    int actualClassIndex = -1;
    String splitPercentageString = "";
    double splitPercentage = -1.0D;
    boolean preserveOrder = false;
    boolean trainSetPresent = false;
    boolean testSetPresent = false;
    

    StringBuffer predsBuff = null;
    

    if ((Utils.getFlag("h", options)) || (Utils.getFlag("help", options)))
    {

      boolean globalInfo = (Utils.getFlag("synopsis", options)) || (Utils.getFlag("info", options));
      

      throw new Exception("\nHelp requested." + makeOptionString(classifier, globalInfo)); }
    String objectInputFileName;
    String objectOutputFileName;
    String sourceClass;
    String thresholdFile;
    String thresholdLabel;
    try { xml = Utils.getOption("xml", options);
      if (!xml.equals("")) {
        options = new XMLOptions(xml).toArray();
      }
      

      optionsTmp = new String[options.length];
      for (int i = 0; i < options.length; i++) {
        optionsTmp[i] = options[i];
      }
      
      String tmpO = Utils.getOption('l', optionsTmp);
      
      if (tmpO.endsWith(".xml"))
      {
        boolean success = false;
        try {
          PMMLModel pmmlModel = PMMLFactory.getPMMLModel(tmpO);
          if ((pmmlModel instanceof PMMLClassifier)) {
            classifier = (PMMLClassifier)pmmlModel;
            success = true;
          }
        } catch (IllegalArgumentException ex) {
          success = false;
        }
        if (!success)
        {
          XMLClassifier xmlserial = new XMLClassifier();
          Classifier cl = (Classifier)xmlserial.read(Utils.getOption('l', options));
          


          optionsTmp = new String[options.length + cl.getOptions().length];
          System.arraycopy(cl.getOptions(), 0, optionsTmp, 0, cl.getOptions().length);
          
          System.arraycopy(options, 0, optionsTmp, cl.getOptions().length, options.length);
          
          options = optionsTmp;
        }
      }
      
      noCrossValidation = Utils.getFlag("no-cv", options);
      
      String classIndexString = Utils.getOption('c', options);
      if (classIndexString.length() != 0) {
        if (classIndexString.equals("first")) {
          classIndex = 1;
        } else if (classIndexString.equals("last")) {
          classIndex = -1;
        } else {
          classIndex = Integer.parseInt(classIndexString);
        }
      }
      String trainFileName = Utils.getOption('t', options);
      objectInputFileName = Utils.getOption('l', options);
      objectOutputFileName = Utils.getOption('d', options);
      String testFileName = Utils.getOption('T', options);
      String foldsString = Utils.getOption('x', options);
      if (foldsString.length() != 0) {
        folds = Integer.parseInt(foldsString);
      }
      String seedString = Utils.getOption('s', options);
      if (seedString.length() != 0) {
        seed = Integer.parseInt(seedString);
      }
      if (trainFileName.length() == 0) {
        if (objectInputFileName.length() == 0) {
          throw new Exception("No training file and no object input file given.");
        }
        
        if (testFileName.length() == 0) {
          throw new Exception("No training file and no test file given.");
        }
      } else if ((objectInputFileName.length() != 0) && ((!(classifier instanceof UpdateableClassifier)) || (testFileName.length() == 0)))
      {

        throw new Exception("Classifier not incremental, or no test file provided: can't use both train and model file.");
      }
      try
      {
        if (trainFileName.length() != 0) {
          trainSetPresent = true;
          trainSource = new ConverterUtils.DataSource(trainFileName);
        }
        if (testFileName.length() != 0) {
          testSetPresent = true;
          testSource = new ConverterUtils.DataSource(testFileName);
        }
        if (objectInputFileName.length() != 0) {
          if (objectInputFileName.endsWith(".xml"))
          {

            objectInputStream = null;
            xmlInputStream = null;
          } else {
            InputStream is = new FileInputStream(objectInputFileName);
            if (objectInputFileName.endsWith(".gz")) {
              is = new GZIPInputStream(is);
            }
            
            if ((!objectInputFileName.endsWith(".koml")) || (!KOML.isPresent())) {
              objectInputStream = new ObjectInputStream(is);
              xmlInputStream = null;
            } else {
              objectInputStream = null;
              xmlInputStream = new BufferedInputStream(is);
            }
          }
        }
      } catch (Exception e) {
        throw new Exception("Can't open file " + e.getMessage() + '.');
      }
      if (testSetPresent) {
        template = test = testSource.getStructure();
        if (classIndex != -1) {
          test.setClassIndex(classIndex - 1);
        }
        else if ((test.classIndex() == -1) || (classIndexString.length() != 0)) {
          test.setClassIndex(test.numAttributes() - 1);
        }
        
        actualClassIndex = test.classIndex();
      }
      else {
        splitPercentageString = Utils.getOption("split-percentage", options);
        if (splitPercentageString.length() != 0) {
          if (foldsString.length() != 0) {
            throw new Exception("Percentage split cannot be used in conjunction with cross-validation ('-x').");
          }
          

          splitPercentage = Double.parseDouble(splitPercentageString);
          if ((splitPercentage <= 0.0D) || (splitPercentage >= 100.0D)) {
            throw new Exception("Percentage split value needs be >0 and <100.");
          }
        } else {
          splitPercentage = -1.0D;
        }
        preserveOrder = Utils.getFlag("preserve-order", options);
        if ((preserveOrder) && 
          (splitPercentage == -1.0D)) {
          throw new Exception("Percentage split ('-split-percentage') is missing.");
        }
        


        if (splitPercentage > 0.0D) {
          testSetPresent = true;
          Instances tmpInst = trainSource.getDataSet(actualClassIndex);
          if (!preserveOrder) {
            tmpInst.randomize(new Random(seed));
          }
          int trainSize = (int)Math.round(tmpInst.numInstances() * splitPercentage / 100.0D);
          
          int testSize = tmpInst.numInstances() - trainSize;
          Instances trainInst = new Instances(tmpInst, 0, trainSize);
          Instances testInst = new Instances(tmpInst, trainSize, testSize);
          trainSource = new ConverterUtils.DataSource(trainInst);
          testSource = new ConverterUtils.DataSource(testInst);
          template = test = testSource.getStructure();
          if (classIndex != -1) {
            test.setClassIndex(classIndex - 1);
          }
          else if ((test.classIndex() == -1) || (classIndexString.length() != 0)) {
            test.setClassIndex(test.numAttributes() - 1);
          }
          
          actualClassIndex = test.classIndex();
        }
      }
      if (trainSetPresent) {
        template = train = trainSource.getStructure();
        if (classIndex != -1) {
          train.setClassIndex(classIndex - 1);
        }
        else if ((train.classIndex() == -1) || (classIndexString.length() != 0)) {
          train.setClassIndex(train.numAttributes() - 1);
        }
        
        actualClassIndex = train.classIndex();
        if ((testSetPresent) && (!test.equalHeaders(train))) {
          throw new IllegalArgumentException("Train and test file not compatible!");
        }
      }
      
      if (template == null) {
        throw new Exception("No actual dataset provided to use as template");
      }
      costMatrix = handleCostOption(Utils.getOption('m', options), template.numClasses());
      

      classStatistics = Utils.getFlag('i', options);
      noOutput = Utils.getFlag('o', options);
      trainStatistics = !Utils.getFlag('v', options);
      printComplexityStatistics = Utils.getFlag('k', options);
      printMargins = Utils.getFlag('r', options);
      printGraph = Utils.getFlag('g', options);
      sourceClass = Utils.getOption('z', options);
      printSource = sourceClass.length() != 0;
      printDistribution = Utils.getFlag("distribution", options);
      thresholdFile = Utils.getOption("threshold-file", options);
      thresholdLabel = Utils.getOption("threshold-label", options);
      String attributeRangeString;
      try
      {
        attributeRangeString = Utils.getOption('p', options);
      } catch (Exception e) {
        throw new Exception(e.getMessage() + "\nNOTE: the -p option has changed. " + "It now expects a parameter specifying a range of attributes " + "to list with the predictions. Use '-p 0' for none.");
      }
      


      if (attributeRangeString.length() != 0) {
        printClassifications = true;
        noOutput = true;
        if (!attributeRangeString.equals("0")) {
          attributesToOutput = new Range(attributeRangeString);
        }
      }
      
      if ((!printClassifications) && (printDistribution)) {
        throw new Exception("Cannot print distribution without '-p' option!");
      }
      

      if ((!trainSetPresent) && (printComplexityStatistics)) {
        throw new Exception("Cannot print complexity statistics ('-k') without training file ('-t')!");
      }
      



      if (objectInputFileName.length() != 0) {
        Utils.checkForRemainingOptions(options);


      }
      else if ((classifier instanceof OptionHandler)) {
        for (String option : options) {
          if (option.length() != 0) {
            if (schemeOptionsText == null) {
              schemeOptionsText = new StringBuffer();
            }
            if (option.indexOf(' ') != -1) {
              schemeOptionsText.append('"' + option + "\" ");
            } else {
              schemeOptionsText.append(option + " ");
            }
          }
        }
        classifier.setOptions(options);
      }
      
      Utils.checkForRemainingOptions(options);
    } catch (Exception e) {
      throw new Exception("\nWeka exception: " + e.getMessage() + makeOptionString(classifier, false));
    }
    


    Evaluation trainingEvaluation = new Evaluation(new Instances(template, 0), costMatrix);
    
    Evaluation testingEvaluation = new Evaluation(new Instances(template, 0), costMatrix);
    


    if (!trainSetPresent) {
      testingEvaluation.useNoPriors();
    }
    
    if (objectInputFileName.length() != 0)
    {
      if (objectInputStream != null) {
        classifier = (Classifier)objectInputStream.readObject();
        
        Instances savedStructure = null;
        try {
          savedStructure = (Instances)objectInputStream.readObject();
        }
        catch (Exception ex) {}
        
        if (savedStructure != null)
        {
          if (!template.equalHeaders(savedStructure)) {
            throw new Exception("training and test set are not compatible");
          }
        }
        objectInputStream.close();
      } else if (xmlInputStream != null)
      {

        classifier = (Classifier)KOML.read(xmlInputStream);
        xmlInputStream.close();
      }
    }
    

    Classifier classifierBackup = Classifier.makeCopy(classifier);
    

    if (((classifier instanceof UpdateableClassifier)) && ((testSetPresent) || (noCrossValidation)) && (costMatrix == null) && (trainSetPresent))
    {


      trainingEvaluation.setPriors(train);
      testingEvaluation.setPriors(train);
      trainTimeStart = System.currentTimeMillis();
      if (objectInputFileName.length() == 0) {
        classifier.buildClassifier(train);
      }
      
      while (trainSource.hasMoreElements(train)) {
        Instance trainInst = trainSource.nextElement(train);
        trainingEvaluation.updatePriors(trainInst);
        testingEvaluation.updatePriors(trainInst);
        ((UpdateableClassifier)classifier).updateClassifier(trainInst);
      }
      trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
    } else if (objectInputFileName.length() == 0)
    {
      Instances tempTrain = trainSource.getDataSet(actualClassIndex);
      trainingEvaluation.setPriors(tempTrain);
      testingEvaluation.setPriors(tempTrain);
      trainTimeStart = System.currentTimeMillis();
      classifier.buildClassifier(tempTrain);
      trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
    }
    

    if (objectOutputFileName.length() != 0) {
      OutputStream os = new FileOutputStream(objectOutputFileName);
      
      if ((!objectOutputFileName.endsWith(".xml")) && ((!objectOutputFileName.endsWith(".koml")) || (!KOML.isPresent())))
      {
        if (objectOutputFileName.endsWith(".gz")) {
          os = new GZIPOutputStream(os);
        }
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
        objectOutputStream.writeObject(classifier);
        if (template != null) {
          objectOutputStream.writeObject(template);
        }
        objectOutputStream.flush();
        objectOutputStream.close();
      }
      else
      {
        BufferedOutputStream xmlOutputStream = new BufferedOutputStream(os);
        if (objectOutputFileName.endsWith(".xml")) {
          XMLSerialization xmlSerial = new XMLClassifier();
          xmlSerial.write(xmlOutputStream, classifier);


        }
        else if (objectOutputFileName.endsWith(".koml")) {
          KOML.write(xmlOutputStream, classifier);
        }
        xmlOutputStream.close();
      }
    }
    

    if (((classifier instanceof Drawable)) && (printGraph)) {
      return ((Drawable)classifier).graph();
    }
    

    if (((classifier instanceof Sourcable)) && (printSource)) {
      return wekaStaticWrapper((Sourcable)classifier, sourceClass);
    }
    

    if ((!noOutput) && (!printMargins)) {
      if (((classifier instanceof OptionHandler)) && 
        (schemeOptionsText != null)) {
        text.append("\nOptions: " + schemeOptionsText);
        text.append("\n");
      }
      
      text.append("\n" + classifier.toString() + "\n");
    }
    
    if ((!printMargins) && (costMatrix != null)) {
      text.append("\n=== Evaluation Cost Matrix ===\n\n");
      text.append(costMatrix.toString());
    }
    

    if (printClassifications) {
      ConverterUtils.DataSource source = testSource;
      predsBuff = new StringBuffer();
      
      if ((source == null) && (noCrossValidation)) {
        source = trainSource;
        predsBuff.append("\n=== Predictions on training data ===\n\n");
      } else {
        predsBuff.append("\n=== Predictions on test data ===\n\n");
      }
      if (source != null)
      {




        printClassifications(classifier, new Instances(template, 0), source, actualClassIndex + 1, attributesToOutput, printDistribution, predsBuff);
      }
    }
    




    if ((trainStatistics) && (trainSetPresent))
    {
      if (((classifier instanceof UpdateableClassifier)) && ((testSetPresent) || (noCrossValidation)) && (costMatrix == null))
      {



        trainSource.reset();
        

        train = trainSource.getStructure(actualClassIndex);
        testTimeStart = System.currentTimeMillis();
        
        while (trainSource.hasMoreElements(train)) {
          Instance trainInst = trainSource.nextElement(train);
          trainingEvaluation.evaluateModelOnce(classifier, trainInst);
        }
        testTimeElapsed = System.currentTimeMillis() - testTimeStart;
      } else {
        testTimeStart = System.currentTimeMillis();
        trainingEvaluation.evaluateModel(classifier, trainSource.getDataSet(actualClassIndex), new Object[0]);
        
        testTimeElapsed = System.currentTimeMillis() - testTimeStart;
      }
      

      if (printMargins) {
        return trainingEvaluation.toCumulativeMarginDistributionString();
      }
      if (!printClassifications) {
        text.append("\nTime taken to build model: " + Utils.doubleToString(trainTimeElapsed / 1000.0D, 2) + " seconds");
        

        if (splitPercentage > 0.0D) {
          text.append("\nTime taken to test model on training split: ");
        } else {
          text.append("\nTime taken to test model on training data: ");
        }
        text.append(Utils.doubleToString(testTimeElapsed / 1000.0D, 2) + " seconds");
        

        if (splitPercentage > 0.0D) {
          text.append(trainingEvaluation.toSummaryString("\n\n=== Error on training split ===\n", printComplexityStatistics));
        }
        else
        {
          text.append(trainingEvaluation.toSummaryString("\n\n=== Error on training data ===\n", printComplexityStatistics));
        }
        


        if (template.classAttribute().isNominal()) {
          if (classStatistics) {
            text.append("\n\n" + trainingEvaluation.toClassDetailsString());
          }
          
          text.append("\n\n" + trainingEvaluation.toMatrixString());
        }
      }
    }
    


    if (testSource != null)
    {
      testSource.reset();
      test = testSource.getStructure(test.classIndex());
      
      while (testSource.hasMoreElements(test)) {
        Instance testInst = testSource.nextElement(test);
        testingEvaluation.evaluateModelOnceAndRecordPrediction(classifier, testInst);
      }
      

      if (splitPercentage > 0.0D) {
        if (!printClassifications) {
          text.append("\n\n" + testingEvaluation.toSummaryString("=== Error on test split ===\n", printComplexityStatistics));
        }
        

      }
      else if (!printClassifications) {
        text.append("\n\n" + testingEvaluation.toSummaryString("=== Error on test data ===\n", printComplexityStatistics));

      }
      

    }
    else if ((trainSource != null) && 
      (!noCrossValidation))
    {
      Random random = new Random(seed);
      
      classifier = Classifier.makeCopy(classifierBackup);
      if (!printClassifications) {
        testingEvaluation.crossValidateModel(classifier, trainSource.getDataSet(actualClassIndex), folds, random, new Object[0]);
        
        if (template.classAttribute().isNumeric()) {
          text.append("\n\n\n" + testingEvaluation.toSummaryString("=== Cross-validation ===\n", printComplexityStatistics));
        }
        else
        {
          text.append("\n\n\n" + testingEvaluation.toSummaryString("=== Stratified cross-validation ===\n", printComplexityStatistics));
        }
      }
      else
      {
        predsBuff = new StringBuffer();
        predsBuff.append("\n=== Predictions under cross-validation ===\n\n");
        testingEvaluation.crossValidateModel(classifier, trainSource.getDataSet(actualClassIndex), folds, random, new Object[] { predsBuff, attributesToOutput, new Boolean(printDistribution) });
      }
    }
    









    if ((template.classAttribute().isNominal()) && (!printClassifications) && ((!noCrossValidation) || (testSource != null)))
    {

      if (classStatistics) {
        text.append("\n\n" + testingEvaluation.toClassDetailsString());
      }
      text.append("\n\n" + testingEvaluation.toMatrixString());
    }
    

    if (predsBuff != null) {
      text.append("\n" + predsBuff);
    }
    
    if ((thresholdFile.length() != 0) && (template.classAttribute().isNominal())) {
      int labelIndex = 0;
      if (thresholdLabel.length() != 0) {
        labelIndex = template.classAttribute().indexOfValue(thresholdLabel);
      }
      if (labelIndex == -1) {
        throw new IllegalArgumentException("Class label '" + thresholdLabel + "' is unknown!");
      }
      
      ThresholdCurve tc = new ThresholdCurve();
      Instances result = tc.getCurve(testingEvaluation.predictions(), labelIndex);
      
      ConverterUtils.DataSink.write(thresholdFile, result);
    }
    
    return text.toString();
  }
  









  protected static CostMatrix handleCostOption(String costFileName, int numClasses)
    throws Exception
  {
    if ((costFileName != null) && (costFileName.length() != 0)) {
      System.out.println("NOTE: The behaviour of the -m option has changed between WEKA 3.0 and WEKA 3.1. -m now carries out cost-sensitive *evaluation* only. For cost-sensitive *prediction*, use one of the cost-sensitive metaschemes such as weka.classifiers.meta.CostSensitiveClassifier or weka.classifiers.meta.MetaCost");
      






      Reader costReader = null;
      try {
        costReader = new BufferedReader(new FileReader(costFileName));
      } catch (Exception e) {
        throw new Exception("Can't open file " + e.getMessage() + '.');
      }
      try
      {
        return new CostMatrix(costReader);
      }
      catch (Exception ex)
      {
        try {
          try {
            costReader.close();
            costReader = new BufferedReader(new FileReader(costFileName));
          } catch (Exception e) {
            throw new Exception("Can't open file " + e.getMessage() + '.');
          }
          CostMatrix costMatrix = new CostMatrix(numClasses);
          
          costMatrix.readOldFormat(costReader);
          return costMatrix;

        }
        catch (Exception e2)
        {
          throw ex;
        }
      }
    }
    return null;
  }
  
















  public double[] evaluateModel(Classifier classifier, Instances data, Object... forPredictionsPrinting)
    throws Exception
  {
    StringBuffer buff = null;
    Range attsToOutput = null;
    boolean printDist = false;
    
    double[] predictions = new double[data.numInstances()];
    
    if (forPredictionsPrinting.length > 0) {
      buff = (StringBuffer)forPredictionsPrinting[0];
      attsToOutput = (Range)forPredictionsPrinting[1];
      printDist = ((Boolean)forPredictionsPrinting[2]).booleanValue();
    }
    


    for (int i = 0; i < data.numInstances(); i++) {
      predictions[i] = evaluateModelOnceAndRecordPrediction(classifier, data.instance(i));
      
      if (buff != null) {
        buff.append(predictionText(classifier, data.instance(i), i, attsToOutput, printDist));
      }
    }
    

    return predictions;
  }
  










  public double evaluateModelOnceAndRecordPrediction(Classifier classifier, Instance instance)
    throws Exception
  {
    Instance classMissing = (Instance)instance.copy();
    double pred = 0.0D;
    classMissing.setDataset(instance.dataset());
    classMissing.setClassMissing();
    if (m_ClassIsNominal) {
      if (m_Predictions == null) {
        m_Predictions = new FastVector();
      }
      double[] dist = classifier.distributionForInstance(classMissing);
      pred = Utils.maxIndex(dist);
      if (dist[((int)pred)] <= 0.0D) {
        pred = Instance.missingValue();
      }
      updateStatsForClassifier(dist, instance);
      m_Predictions.addElement(new NominalPrediction(instance.classValue(), dist, instance.weight()));
    }
    else {
      pred = classifier.classifyInstance(classMissing);
      updateStatsForPredictor(pred, instance);
    }
    return pred;
  }
  









  public double evaluateModelOnce(Classifier classifier, Instance instance)
    throws Exception
  {
    Instance classMissing = (Instance)instance.copy();
    double pred = 0.0D;
    classMissing.setDataset(instance.dataset());
    classMissing.setClassMissing();
    if (m_ClassIsNominal) {
      double[] dist = classifier.distributionForInstance(classMissing);
      pred = Utils.maxIndex(dist);
      if (dist[((int)pred)] <= 0.0D) {
        pred = Instance.missingValue();
      }
      updateStatsForClassifier(dist, instance);
    } else {
      pred = classifier.classifyInstance(classMissing);
      updateStatsForPredictor(pred, instance);
    }
    return pred;
  }
  




  public double evaluateModelOnce(double[] dist, Instance instance)
    throws Exception
  {
    double pred;
    


    if (m_ClassIsNominal) {
      double pred = Utils.maxIndex(dist);
      if (dist[((int)pred)] <= 0.0D) {
        pred = Instance.missingValue();
      }
      updateStatsForClassifier(dist, instance);
    } else {
      pred = dist[0];
      updateStatsForPredictor(pred, instance);
    }
    return pred;
  }
  




  public double evaluateModelOnceAndRecordPrediction(double[] dist, Instance instance)
    throws Exception
  {
    double pred;
    


    if (m_ClassIsNominal) {
      if (m_Predictions == null) {
        m_Predictions = new FastVector();
      }
      double pred = Utils.maxIndex(dist);
      if (dist[((int)pred)] <= 0.0D) {
        pred = Instance.missingValue();
      }
      updateStatsForClassifier(dist, instance);
      m_Predictions.addElement(new NominalPrediction(instance.classValue(), dist, instance.weight()));
    }
    else {
      pred = dist[0];
      updateStatsForPredictor(pred, instance);
    }
    return pred;
  }
  







  public void evaluateModelOnce(double prediction, Instance instance)
    throws Exception
  {
    if (m_ClassIsNominal) {
      updateStatsForClassifier(makeDistribution(prediction), instance);
    } else {
      updateStatsForPredictor(prediction, instance);
    }
  }
  







  public FastVector predictions()
  {
    return m_Predictions;
  }
  










  public static String wekaStaticWrapper(Sourcable classifier, String className)
    throws Exception
  {
    StringBuffer result = new StringBuffer();
    String staticClassifier = classifier.toSource(className);
    
    result.append("// Generated with Weka " + Version.VERSION + "\n");
    result.append("//\n");
    result.append("// This code is public domain and comes with no warranty.\n");
    
    result.append("//\n");
    result.append("// Timestamp: " + new Date() + "\n");
    result.append("\n");
    result.append("package weka.classifiers;\n");
    result.append("\n");
    result.append("import weka.core.Attribute;\n");
    result.append("import weka.core.Capabilities;\n");
    result.append("import weka.core.Capabilities.Capability;\n");
    result.append("import weka.core.Instance;\n");
    result.append("import weka.core.Instances;\n");
    result.append("import weka.core.RevisionUtils;\n");
    result.append("import weka.classifiers.Classifier;\n");
    result.append("\n");
    result.append("public class WekaWrapper\n");
    result.append("  extends Classifier {\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * Returns only the toString() method.\n");
    result.append("   *\n");
    result.append("   * @return a string describing the classifier\n");
    result.append("   */\n");
    result.append("  public String globalInfo() {\n");
    result.append("    return toString();\n");
    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * Returns the capabilities of this classifier.\n");
    result.append("   *\n");
    result.append("   * @return the capabilities\n");
    result.append("   */\n");
    result.append("  public Capabilities getCapabilities() {\n");
    result.append(((Classifier)classifier).getCapabilities().toSource("result", 4));
    
    result.append("    return result;\n");
    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * only checks the data against its capabilities.\n");
    result.append("   *\n");
    result.append("   * @param i the training data\n");
    result.append("   */\n");
    result.append("  public void buildClassifier(Instances i) throws Exception {\n");
    
    result.append("    // can classifier handle the data?\n");
    result.append("    getCapabilities().testWithFail(i);\n");
    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * Classifies the given instance.\n");
    result.append("   *\n");
    result.append("   * @param i the instance to classify\n");
    result.append("   * @return the classification result\n");
    result.append("   */\n");
    result.append("  public double classifyInstance(Instance i) throws Exception {\n");
    
    result.append("    Object[] s = new Object[i.numAttributes()];\n");
    result.append("    \n");
    result.append("    for (int j = 0; j < s.length; j++) {\n");
    result.append("      if (!i.isMissing(j)) {\n");
    result.append("        if (i.attribute(j).isNominal())\n");
    result.append("          s[j] = new String(i.stringValue(j));\n");
    result.append("        else if (i.attribute(j).isNumeric())\n");
    result.append("          s[j] = new Double(i.value(j));\n");
    result.append("      }\n");
    result.append("    }\n");
    result.append("    \n");
    result.append("    // set class value to missing\n");
    result.append("    s[i.classIndex()] = null;\n");
    result.append("    \n");
    result.append("    return " + className + ".classify(s);\n");
    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * Returns the revision string.\n");
    result.append("   * \n");
    result.append("   * @return        the revision\n");
    result.append("   */\n");
    result.append("  public String getRevision() {\n");
    result.append("    return RevisionUtils.extract(\"1.0\");\n");
    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * Returns only the classnames and what classifier it is based on.\n");
    
    result.append("   *\n");
    result.append("   * @return a short description\n");
    result.append("   */\n");
    result.append("  public String toString() {\n");
    result.append("    return \"Auto-generated classifier wrapper, based on " + classifier.getClass().getName() + " (generated with Weka " + Version.VERSION + ").\\n" + "\" + this.getClass().getName() + \"/" + className + "\";\n");
    


    result.append("  }\n");
    

    result.append("\n");
    result.append("  /**\n");
    result.append("   * Runs the classfier from commandline.\n");
    result.append("   *\n");
    result.append("   * @param args the commandline arguments\n");
    result.append("   */\n");
    result.append("  public static void main(String args[]) {\n");
    result.append("    runClassifier(new WekaWrapper(), args);\n");
    result.append("  }\n");
    result.append("}\n");
    

    result.append("\n");
    result.append(staticClassifier);
    
    return result.toString();
  }
  






  public final double numInstances()
  {
    return m_WithClass;
  }
  







  public final double incorrect()
  {
    return m_Incorrect;
  }
  






  public final double pctIncorrect()
  {
    return 100.0D * m_Incorrect / m_WithClass;
  }
  






  public final double totalCost()
  {
    return m_TotalCost;
  }
  






  public final double avgCost()
  {
    return m_TotalCost / m_WithClass;
  }
  







  public final double correct()
  {
    return m_Correct;
  }
  






  public final double pctCorrect()
  {
    return 100.0D * m_Correct / m_WithClass;
  }
  







  public final double unclassified()
  {
    return m_Unclassified;
  }
  






  public final double pctUnclassified()
  {
    return 100.0D * m_Unclassified / m_WithClass;
  }
  








  public final double errorRate()
  {
    if (!m_ClassIsNominal) {
      return Math.sqrt(m_SumSqrErr / (m_WithClass - m_Unclassified));
    }
    if (m_CostMatrix == null) {
      return m_Incorrect / m_WithClass;
    }
    return avgCost();
  }
  






  public final double kappa()
  {
    double[] sumRows = new double[m_ConfusionMatrix.length];
    double[] sumColumns = new double[m_ConfusionMatrix.length];
    double sumOfWeights = 0.0D;
    for (int i = 0; i < m_ConfusionMatrix.length; i++) {
      for (int j = 0; j < m_ConfusionMatrix.length; j++) {
        sumRows[i] += m_ConfusionMatrix[i][j];
        sumColumns[j] += m_ConfusionMatrix[i][j];
        sumOfWeights += m_ConfusionMatrix[i][j];
      }
    }
    double correct = 0.0D;double chanceAgreement = 0.0D;
    for (int i = 0; i < m_ConfusionMatrix.length; i++) {
      chanceAgreement += sumRows[i] * sumColumns[i];
      correct += m_ConfusionMatrix[i][i];
    }
    chanceAgreement /= sumOfWeights * sumOfWeights;
    correct /= sumOfWeights;
    
    if (chanceAgreement < 1.0D) {
      return (correct - chanceAgreement) / (1.0D - chanceAgreement);
    }
    return 1.0D;
  }
  






  public final double correlationCoefficient()
    throws Exception
  {
    if (m_ClassIsNominal) {
      throw new Exception("Can't compute correlation coefficient: class is nominal!");
    }
    

    double correlation = 0.0D;
    double varActual = m_SumSqrClass - m_SumClass * m_SumClass / (m_WithClass - m_Unclassified);
    
    double varPredicted = m_SumSqrPredicted - m_SumPredicted * m_SumPredicted / (m_WithClass - m_Unclassified);
    

    double varProd = m_SumClassPredicted - m_SumClass * m_SumPredicted / (m_WithClass - m_Unclassified);
    


    if (varActual * varPredicted <= 0.0D) {
      correlation = 0.0D;
    } else {
      correlation = varProd / Math.sqrt(varActual * varPredicted);
    }
    
    return correlation;
  }
  







  public final double meanAbsoluteError()
  {
    return m_SumAbsErr / (m_WithClass - m_Unclassified);
  }
  





  public final double meanPriorAbsoluteError()
  {
    if (m_NoPriors) {
      return NaN.0D;
    }
    
    return m_SumPriorAbsErr / m_WithClass;
  }
  





  public final double relativeAbsoluteError()
    throws Exception
  {
    if (m_NoPriors) {
      return NaN.0D;
    }
    
    return 100.0D * meanAbsoluteError() / meanPriorAbsoluteError();
  }
  





  public final double rootMeanSquaredError()
  {
    return Math.sqrt(m_SumSqrErr / (m_WithClass - m_Unclassified));
  }
  





  public final double rootMeanPriorSquaredError()
  {
    if (m_NoPriors) {
      return NaN.0D;
    }
    
    return Math.sqrt(m_SumPriorSqrErr / m_WithClass);
  }
  





  public final double rootRelativeSquaredError()
  {
    if (m_NoPriors) {
      return NaN.0D;
    }
    
    return 100.0D * rootMeanSquaredError() / rootMeanPriorSquaredError();
  }
  





  public final double priorEntropy()
    throws Exception
  {
    if (!m_ClassIsNominal) {
      throw new Exception("Can't compute entropy of class prior: class numeric!");
    }
    

    if (m_NoPriors) {
      return NaN.0D;
    }
    
    double entropy = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      entropy -= m_ClassPriors[i] / m_ClassPriorsSum * Utils.log2(m_ClassPriors[i] / m_ClassPriorsSum);
    }
    

    return entropy;
  }
  





  public final double KBInformation()
    throws Exception
  {
    if (!m_ClassIsNominal) {
      throw new Exception("Can't compute K&B Info score: class numeric!");
    }
    
    if (m_NoPriors) {
      return NaN.0D;
    }
    
    return m_SumKBInfo;
  }
  





  public final double KBMeanInformation()
    throws Exception
  {
    if (!m_ClassIsNominal) {
      throw new Exception("Can't compute K&B Info score: class numeric!");
    }
    
    if (m_NoPriors) {
      return NaN.0D;
    }
    
    return m_SumKBInfo / (m_WithClass - m_Unclassified);
  }
  





  public final double KBRelativeInformation()
    throws Exception
  {
    if (!m_ClassIsNominal) {
      throw new Exception("Can't compute K&B Info score: class numeric!");
    }
    
    if (m_NoPriors) {
      return NaN.0D;
    }
    
    return 100.0D * KBInformation() / priorEntropy();
  }
  





  public final double SFPriorEntropy()
  {
    if (m_NoPriors) {
      return NaN.0D;
    }
    
    return m_SumPriorEntropy;
  }
  





  public final double SFMeanPriorEntropy()
  {
    if (m_NoPriors) {
      return NaN.0D;
    }
    
    return m_SumPriorEntropy / m_WithClass;
  }
  





  public final double SFSchemeEntropy()
  {
    if (m_NoPriors) {
      return NaN.0D;
    }
    
    return m_SumSchemeEntropy;
  }
  





  public final double SFMeanSchemeEntropy()
  {
    if (m_NoPriors) {
      return NaN.0D;
    }
    
    return m_SumSchemeEntropy / (m_WithClass - m_Unclassified);
  }
  






  public final double SFEntropyGain()
  {
    if (m_NoPriors) {
      return NaN.0D;
    }
    
    return m_SumPriorEntropy - m_SumSchemeEntropy;
  }
  






  public final double SFMeanEntropyGain()
  {
    if (m_NoPriors) {
      return NaN.0D;
    }
    
    return (m_SumPriorEntropy - m_SumSchemeEntropy) / (m_WithClass - m_Unclassified);
  }
  







  public String toCumulativeMarginDistributionString()
    throws Exception
  {
    if (!m_ClassIsNominal) {
      throw new Exception("Class must be nominal for margin distributions");
    }
    String result = "";
    double cumulativeCount = 0.0D;
    
    for (int i = 0; i <= k_MarginResolution; i++) {
      if (m_MarginCounts[i] != 0.0D) {
        cumulativeCount += m_MarginCounts[i];
        double margin = i * 2.0D / k_MarginResolution - 1.0D;
        result = result + Utils.doubleToString(margin, 7, 3) + ' ' + Utils.doubleToString(cumulativeCount * 100.0D / m_WithClass, 7, 3) + '\n';


      }
      else if (i == 0) {
        result = Utils.doubleToString(-1.0D, 7, 3) + ' ' + Utils.doubleToString(0.0D, 7, 3) + '\n';
      }
    }
    

    return result;
  }
  






  public String toSummaryString()
  {
    return toSummaryString("", false);
  }
  







  public String toSummaryString(boolean printComplexityStatistics)
  {
    return toSummaryString("=== Summary ===\n", printComplexityStatistics);
  }
  











  public String toSummaryString(String title, boolean printComplexityStatistics)
  {
    StringBuffer text = new StringBuffer();
    
    if ((printComplexityStatistics) && (m_NoPriors)) {
      printComplexityStatistics = false;
      System.err.println("Priors disabled, cannot print complexity statistics!");
    }
    

    text.append(title + "\n");
    try {
      if (m_WithClass > 0.0D) {
        if (m_ClassIsNominal)
        {
          text.append("Correctly Classified Instances     ");
          text.append(Utils.doubleToString(correct(), 12, 4) + "     " + Utils.doubleToString(pctCorrect(), 12, 4) + " %\n");
          
          text.append("Incorrectly Classified Instances   ");
          text.append(Utils.doubleToString(incorrect(), 12, 4) + "     " + Utils.doubleToString(pctIncorrect(), 12, 4) + " %\n");
          
          text.append("Kappa statistic                    ");
          text.append(Utils.doubleToString(kappa(), 12, 4) + "\n");
          
          if (m_CostMatrix != null) {
            text.append("Total Cost                         ");
            text.append(Utils.doubleToString(totalCost(), 12, 4) + "\n");
            text.append("Average Cost                       ");
            text.append(Utils.doubleToString(avgCost(), 12, 4) + "\n");
          }
          if (printComplexityStatistics) {
            text.append("K&B Relative Info Score            ");
            text.append(Utils.doubleToString(KBRelativeInformation(), 12, 4) + " %\n");
            
            text.append("K&B Information Score              ");
            text.append(Utils.doubleToString(KBInformation(), 12, 4) + " bits");
            text.append(Utils.doubleToString(KBMeanInformation(), 12, 4) + " bits/instance\n");
          }
        }
        else {
          text.append("Correlation coefficient            ");
          text.append(Utils.doubleToString(correlationCoefficient(), 12, 4) + "\n");
        }
        
        if (printComplexityStatistics) {
          text.append("Class complexity | order 0         ");
          text.append(Utils.doubleToString(SFPriorEntropy(), 12, 4) + " bits");
          text.append(Utils.doubleToString(SFMeanPriorEntropy(), 12, 4) + " bits/instance\n");
          
          text.append("Class complexity | scheme          ");
          text.append(Utils.doubleToString(SFSchemeEntropy(), 12, 4) + " bits");
          text.append(Utils.doubleToString(SFMeanSchemeEntropy(), 12, 4) + " bits/instance\n");
          
          text.append("Complexity improvement     (Sf)    ");
          text.append(Utils.doubleToString(SFEntropyGain(), 12, 4) + " bits");
          text.append(Utils.doubleToString(SFMeanEntropyGain(), 12, 4) + " bits/instance\n");
        }
        

        text.append("Mean absolute error                ");
        text.append(Utils.doubleToString(meanAbsoluteError(), 12, 4) + "\n");
        text.append("Root mean squared error            ");
        text.append(Utils.doubleToString(rootMeanSquaredError(), 12, 4) + "\n");
        if (!m_NoPriors) {
          text.append("Relative absolute error            ");
          text.append(Utils.doubleToString(relativeAbsoluteError(), 12, 4) + " %\n");
          
          text.append("Root relative squared error        ");
          text.append(Utils.doubleToString(rootRelativeSquaredError(), 12, 4) + " %\n");
        }
      }
      
      if (Utils.gr(unclassified(), 0.0D)) {
        text.append("UnClassified Instances             ");
        text.append(Utils.doubleToString(unclassified(), 12, 4) + "     " + Utils.doubleToString(pctUnclassified(), 12, 4) + " %\n");
      }
      
      text.append("Total Number of Instances          ");
      text.append(Utils.doubleToString(m_WithClass, 12, 4) + "\n");
      if (m_MissingClass > 0.0D) {
        text.append("Ignored Class Unknown Instances            ");
        text.append(Utils.doubleToString(m_MissingClass, 12, 4) + "\n");
      }
    }
    catch (Exception ex)
    {
      System.err.println("Arggh - Must be a bug in Evaluation class");
    }
    
    return text.toString();
  }
  





  public String toMatrixString()
    throws Exception
  {
    return toMatrixString("=== Confusion Matrix ===\n");
  }
  







  public String toMatrixString(String title)
    throws Exception
  {
    StringBuffer text = new StringBuffer();
    char[] IDChars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
    


    boolean fractional = false;
    
    if (!m_ClassIsNominal) {
      throw new Exception("Evaluation: No confusion matrix possible!");
    }
    


    double maxval = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumClasses; j++) {
        double current = m_ConfusionMatrix[i][j];
        if (current < 0.0D) {
          current *= -10.0D;
        }
        if (current > maxval) {
          maxval = current;
        }
        double fract = current - Math.rint(current);
        if ((!fractional) && (Math.log(fract) / Math.log(10.0D) >= -2.0D)) {
          fractional = true;
        }
      }
    }
    
    int IDWidth = 1 + Math.max((int)(Math.log(maxval) / Math.log(10.0D) + (fractional ? 3 : 0)), (int)(Math.log(m_NumClasses) / Math.log(IDChars.length)));
    


    text.append(title).append("\n");
    for (int i = 0; i < m_NumClasses; i++) {
      if (fractional) {
        text.append(" ").append(num2ShortID(i, IDChars, IDWidth - 3)).append("   ");
      }
      else {
        text.append(" ").append(num2ShortID(i, IDChars, IDWidth));
      }
    }
    text.append("   <-- classified as\n");
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumClasses; j++) {
        text.append(" ").append(Utils.doubleToString(m_ConfusionMatrix[i][j], IDWidth, fractional ? 2 : 0));
      }
      

      text.append(" | ").append(num2ShortID(i, IDChars, IDWidth)).append(" = ").append(m_ClassNames[i]).append("\n");
    }
    
    return text.toString();
  }
  








  public String toClassDetailsString()
    throws Exception
  {
    return toClassDetailsString("=== Detailed Accuracy By Class ===\n");
  }
  









  public String toClassDetailsString(String title)
    throws Exception
  {
    if (!m_ClassIsNominal) {
      throw new Exception("Evaluation: No confusion matrix possible!");
    }
    
    StringBuffer text = new StringBuffer(title + "\n               TP Rate   FP Rate" + "   Precision   Recall" + "  F-Measure   ROC Area  Class\n");
    

    for (int i = 0; i < m_NumClasses; i++) {
      text.append("               " + Utils.doubleToString(truePositiveRate(i), 7, 3)).append("   ");
      

      text.append(Utils.doubleToString(falsePositiveRate(i), 7, 3)).append("    ");
      
      text.append(Utils.doubleToString(precision(i), 7, 3)).append("   ");
      text.append(Utils.doubleToString(recall(i), 7, 3)).append("   ");
      text.append(Utils.doubleToString(fMeasure(i), 7, 3)).append("    ");
      
      double rocVal = areaUnderROC(i);
      if (Instance.isMissingValue(rocVal)) {
        text.append("  ?    ").append("    ");
      } else {
        text.append(Utils.doubleToString(rocVal, 7, 3)).append("    ");
      }
      text.append(m_ClassNames[i]).append('\n');
    }
    
    text.append("Weighted Avg.  " + Utils.doubleToString(weightedTruePositiveRate(), 7, 3));
    
    text.append("   " + Utils.doubleToString(weightedFalsePositiveRate(), 7, 3));
    
    text.append("    " + Utils.doubleToString(weightedPrecision(), 7, 3));
    text.append("   " + Utils.doubleToString(weightedRecall(), 7, 3));
    text.append("   " + Utils.doubleToString(weightedFMeasure(), 7, 3));
    text.append("    " + Utils.doubleToString(weightedAreaUnderROC(), 7, 3));
    text.append("\n");
    
    return text.toString();
  }
  












  public double numTruePositives(int classIndex)
  {
    double correct = 0.0D;
    for (int j = 0; j < m_NumClasses; j++) {
      if (j == classIndex) {
        correct += m_ConfusionMatrix[classIndex][j];
      }
    }
    return correct;
  }
  














  public double truePositiveRate(int classIndex)
  {
    double correct = 0.0D;double total = 0.0D;
    for (int j = 0; j < m_NumClasses; j++) {
      if (j == classIndex) {
        correct += m_ConfusionMatrix[classIndex][j];
      }
      total += m_ConfusionMatrix[classIndex][j];
    }
    if (total == 0.0D) {
      return 0.0D;
    }
    return correct / total;
  }
  




  public double weightedTruePositiveRate()
  {
    double[] classCounts = new double[m_NumClasses];
    double classCountSum = 0.0D;
    
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumClasses; j++) {
        classCounts[i] += m_ConfusionMatrix[i][j];
      }
      classCountSum += classCounts[i];
    }
    
    double truePosTotal = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      double temp = truePositiveRate(i);
      truePosTotal += temp * classCounts[i];
    }
    
    return truePosTotal / classCountSum;
  }
  












  public double numTrueNegatives(int classIndex)
  {
    double correct = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      if (i != classIndex) {
        for (int j = 0; j < m_NumClasses; j++) {
          if (j != classIndex) {
            correct += m_ConfusionMatrix[i][j];
          }
        }
      }
    }
    return correct;
  }
  














  public double trueNegativeRate(int classIndex)
  {
    double correct = 0.0D;double total = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      if (i != classIndex) {
        for (int j = 0; j < m_NumClasses; j++) {
          if (j != classIndex) {
            correct += m_ConfusionMatrix[i][j];
          }
          total += m_ConfusionMatrix[i][j];
        }
      }
    }
    if (total == 0.0D) {
      return 0.0D;
    }
    return correct / total;
  }
  




  public double weightedTrueNegativeRate()
  {
    double[] classCounts = new double[m_NumClasses];
    double classCountSum = 0.0D;
    
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumClasses; j++) {
        classCounts[i] += m_ConfusionMatrix[i][j];
      }
      classCountSum += classCounts[i];
    }
    
    double trueNegTotal = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      double temp = trueNegativeRate(i);
      trueNegTotal += temp * classCounts[i];
    }
    
    return trueNegTotal / classCountSum;
  }
  












  public double numFalsePositives(int classIndex)
  {
    double incorrect = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      if (i != classIndex) {
        for (int j = 0; j < m_NumClasses; j++) {
          if (j == classIndex) {
            incorrect += m_ConfusionMatrix[i][j];
          }
        }
      }
    }
    return incorrect;
  }
  














  public double falsePositiveRate(int classIndex)
  {
    double incorrect = 0.0D;double total = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      if (i != classIndex) {
        for (int j = 0; j < m_NumClasses; j++) {
          if (j == classIndex) {
            incorrect += m_ConfusionMatrix[i][j];
          }
          total += m_ConfusionMatrix[i][j];
        }
      }
    }
    if (total == 0.0D) {
      return 0.0D;
    }
    return incorrect / total;
  }
  




  public double weightedFalsePositiveRate()
  {
    double[] classCounts = new double[m_NumClasses];
    double classCountSum = 0.0D;
    
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumClasses; j++) {
        classCounts[i] += m_ConfusionMatrix[i][j];
      }
      classCountSum += classCounts[i];
    }
    
    double falsePosTotal = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      double temp = falsePositiveRate(i);
      falsePosTotal += temp * classCounts[i];
    }
    
    return falsePosTotal / classCountSum;
  }
  












  public double numFalseNegatives(int classIndex)
  {
    double incorrect = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      if (i == classIndex) {
        for (int j = 0; j < m_NumClasses; j++) {
          if (j != classIndex) {
            incorrect += m_ConfusionMatrix[i][j];
          }
        }
      }
    }
    return incorrect;
  }
  














  public double falseNegativeRate(int classIndex)
  {
    double incorrect = 0.0D;double total = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      if (i == classIndex) {
        for (int j = 0; j < m_NumClasses; j++) {
          if (j != classIndex) {
            incorrect += m_ConfusionMatrix[i][j];
          }
          total += m_ConfusionMatrix[i][j];
        }
      }
    }
    if (total == 0.0D) {
      return 0.0D;
    }
    return incorrect / total;
  }
  




  public double weightedFalseNegativeRate()
  {
    double[] classCounts = new double[m_NumClasses];
    double classCountSum = 0.0D;
    
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumClasses; j++) {
        classCounts[i] += m_ConfusionMatrix[i][j];
      }
      classCountSum += classCounts[i];
    }
    
    double falseNegTotal = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      double temp = falseNegativeRate(i);
      falseNegTotal += temp * classCounts[i];
    }
    
    return falseNegTotal / classCountSum;
  }
  















  public double recall(int classIndex)
  {
    return truePositiveRate(classIndex);
  }
  




  public double weightedRecall()
  {
    return weightedTruePositiveRate();
  }
  














  public double precision(int classIndex)
  {
    double correct = 0.0D;double total = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      if (i == classIndex) {
        correct += m_ConfusionMatrix[i][classIndex];
      }
      total += m_ConfusionMatrix[i][classIndex];
    }
    if (total == 0.0D) {
      return 0.0D;
    }
    return correct / total;
  }
  




  public double weightedPrecision()
  {
    double[] classCounts = new double[m_NumClasses];
    double classCountSum = 0.0D;
    
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumClasses; j++) {
        classCounts[i] += m_ConfusionMatrix[i][j];
      }
      classCountSum += classCounts[i];
    }
    
    double precisionTotal = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      double temp = precision(i);
      precisionTotal += temp * classCounts[i];
    }
    
    return precisionTotal / classCountSum;
  }
  














  public double fMeasure(int classIndex)
  {
    double precision = precision(classIndex);
    double recall = recall(classIndex);
    if (precision + recall == 0.0D) {
      return 0.0D;
    }
    return 2.0D * precision * recall / (precision + recall);
  }
  




  public double weightedFMeasure()
  {
    double[] classCounts = new double[m_NumClasses];
    double classCountSum = 0.0D;
    
    for (int i = 0; i < m_NumClasses; i++) {
      for (int j = 0; j < m_NumClasses; j++) {
        classCounts[i] += m_ConfusionMatrix[i][j];
      }
      classCountSum += classCounts[i];
    }
    
    double fMeasureTotal = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      double temp = fMeasure(i);
      fMeasureTotal += temp * classCounts[i];
    }
    
    return fMeasureTotal / classCountSum;
  }
  





  public void setPriors(Instances train)
    throws Exception
  {
    m_NoPriors = false;
    
    if (!m_ClassIsNominal)
    {
      m_NumTrainClassVals = 0;
      m_TrainClassVals = null;
      m_TrainClassWeights = null;
      m_PriorErrorEstimator = null;
      m_ErrorEstimator = null;
      
      for (int i = 0; i < train.numInstances(); i++) {
        Instance currentInst = train.instance(i);
        if (!currentInst.classIsMissing()) {
          addNumericTrainClass(currentInst.classValue(), currentInst.weight());
        }
      }
    }
    else {
      for (int i = 0; i < m_NumClasses; i++) {
        m_ClassPriors[i] = 1.0D;
      }
      m_ClassPriorsSum = m_NumClasses;
      for (int i = 0; i < train.numInstances(); i++) {
        if (!train.instance(i).classIsMissing()) {
          m_ClassPriors[((int)train.instance(i).classValue())] += train.instance(i).weight();
          
          m_ClassPriorsSum += train.instance(i).weight();
        }
      }
    }
  }
  




  public double[] getClassPriors()
  {
    return m_ClassPriors;
  }
  




  public void updatePriors(Instance instance)
    throws Exception
  {
    if (!instance.classIsMissing()) {
      if (!m_ClassIsNominal) {
        if (!instance.classIsMissing()) {
          addNumericTrainClass(instance.classValue(), instance.weight());
        }
      } else {
        m_ClassPriors[((int)instance.classValue())] += instance.weight();
        m_ClassPriorsSum += instance.weight();
      }
    }
  }
  




  public void useNoPriors()
  {
    m_NoPriors = true;
  }
  








  public boolean equals(Object obj)
  {
    if ((obj == null) || (!obj.getClass().equals(getClass()))) {
      return false;
    }
    Evaluation cmp = (Evaluation)obj;
    if (m_ClassIsNominal != m_ClassIsNominal) {
      return false;
    }
    if (m_NumClasses != m_NumClasses) {
      return false;
    }
    
    if (m_Incorrect != m_Incorrect) {
      return false;
    }
    if (m_Correct != m_Correct) {
      return false;
    }
    if (m_Unclassified != m_Unclassified) {
      return false;
    }
    if (m_MissingClass != m_MissingClass) {
      return false;
    }
    if (m_WithClass != m_WithClass) {
      return false;
    }
    
    if (m_SumErr != m_SumErr) {
      return false;
    }
    if (m_SumAbsErr != m_SumAbsErr) {
      return false;
    }
    if (m_SumSqrErr != m_SumSqrErr) {
      return false;
    }
    if (m_SumClass != m_SumClass) {
      return false;
    }
    if (m_SumSqrClass != m_SumSqrClass) {
      return false;
    }
    if (m_SumPredicted != m_SumPredicted) {
      return false;
    }
    if (m_SumSqrPredicted != m_SumSqrPredicted) {
      return false;
    }
    if (m_SumClassPredicted != m_SumClassPredicted) {
      return false;
    }
    
    if (m_ClassIsNominal) {
      for (int i = 0; i < m_NumClasses; i++) {
        for (int j = 0; j < m_NumClasses; j++) {
          if (m_ConfusionMatrix[i][j] != m_ConfusionMatrix[i][j]) {
            return false;
          }
        }
      }
    }
    
    return true;
  }
  














  public static void printClassifications(Classifier classifier, Instances train, ConverterUtils.DataSource testSource, int classIndex, Range attributesToOutput, StringBuffer predsText)
    throws Exception
  {
    printClassifications(classifier, train, testSource, classIndex, attributesToOutput, false, predsText);
  }
  











  protected static void printClassificationsHeader(Instances test, Range attributesToOutput, boolean printDistribution, StringBuffer text)
  {
    if (test.classAttribute().isNominal()) {
      if (printDistribution) {
        text.append(" inst#     actual  predicted error distribution");
      } else {
        text.append(" inst#     actual  predicted error prediction");
      }
    } else {
      text.append(" inst#     actual  predicted      error");
    }
    if (attributesToOutput != null) {
      attributesToOutput.setUpper(test.numAttributes() - 1);
      text.append(" (");
      boolean first = true;
      for (int i = 0; i < test.numAttributes(); i++) {
        if (i != test.classIndex())
        {


          if (attributesToOutput.isInRange(i)) {
            if (!first) {
              text.append(",");
            }
            text.append(test.attribute(i).name());
            first = false;
          } }
      }
      text.append(")");
    }
    text.append("\n");
  }
  

















  public static void printClassifications(Classifier classifier, Instances train, ConverterUtils.DataSource testSource, int classIndex, Range attributesToOutput, boolean printDistribution, StringBuffer text)
    throws Exception
  {
    if (testSource != null) {
      Instances test = testSource.getStructure();
      if (classIndex != -1) {
        test.setClassIndex(classIndex - 1);
      }
      else if (test.classIndex() == -1) {
        test.setClassIndex(test.numAttributes() - 1);
      }
      


      printClassificationsHeader(test, attributesToOutput, printDistribution, text);
      


      int i = 0;
      testSource.reset();
      test = testSource.getStructure(test.classIndex());
      while (testSource.hasMoreElements(test)) {
        Instance inst = testSource.nextElement(test);
        text.append(predictionText(classifier, inst, i, attributesToOutput, printDistribution));
        
        i++;
      }
    }
  }
  

















  protected static String predictionText(Classifier classifier, Instance inst, int instNum, Range attributesToOutput, boolean printDistribution)
    throws Exception
  {
    StringBuffer result = new StringBuffer();
    int width = 10;
    int prec = 3;
    
    Instance withMissing = (Instance)inst.copy();
    withMissing.setDataset(inst.dataset());
    withMissing.setMissing(withMissing.classIndex());
    double predValue = classifier.classifyInstance(withMissing);
    

    result.append(Utils.padLeft("" + (instNum + 1), 6));
    
    if (inst.dataset().classAttribute().isNumeric())
    {
      if (inst.classIsMissing()) {
        result.append(" " + Utils.padLeft("?", width));
      } else {
        result.append(" " + Utils.doubleToString(inst.classValue(), width, prec));
      }
      

      if (Instance.isMissingValue(predValue)) {
        result.append(" " + Utils.padLeft("?", width));
      } else {
        result.append(" " + Utils.doubleToString(predValue, width, prec));
      }
      
      if ((Instance.isMissingValue(predValue)) || (inst.classIsMissing())) {
        result.append(" " + Utils.padLeft("?", width));
      } else {
        result.append(" " + Utils.doubleToString(predValue - inst.classValue(), width, prec));
      }
    }
    else
    {
      result.append(" " + Utils.padLeft(new StringBuilder().append((int)inst.classValue() + 1).append(":").append(inst.toString(inst.classIndex())).toString(), width));
      



      if (Instance.isMissingValue(predValue)) {
        result.append(" " + Utils.padLeft("?", width));
      } else {
        result.append(" " + Utils.padLeft(new StringBuilder().append((int)predValue + 1).append(":").append(inst.dataset().classAttribute().value((int)predValue)).toString(), width));
      }
      


      if ((!Instance.isMissingValue(predValue)) && (!inst.classIsMissing()) && ((int)predValue + 1 != (int)inst.classValue() + 1))
      {
        result.append("   +  ");
      } else {
        result.append("      ");
      }
      
      if (printDistribution) {
        if (Instance.isMissingValue(predValue)) {
          result.append(" ?");
        } else {
          result.append(" ");
          double[] dist = classifier.distributionForInstance(withMissing);
          for (int n = 0; n < dist.length; n++) {
            if (n > 0) {
              result.append(",");
            }
            if (n == (int)predValue) {
              result.append("*");
            }
            result.append(Utils.doubleToString(dist[n], prec));
          }
        }
      }
      else if (Instance.isMissingValue(predValue)) {
        result.append(" ?");
      } else {
        result.append(" " + Utils.doubleToString(classifier.distributionForInstance(withMissing)[((int)predValue)], prec));
      }
    }
    





    result.append(" " + attributeValuesString(withMissing, attributesToOutput) + "\n");
    

    return result.toString();
  }
  








  protected static String attributeValuesString(Instance instance, Range attRange)
  {
    StringBuffer text = new StringBuffer();
    if (attRange != null) {
      boolean firstOutput = true;
      attRange.setUpper(instance.numAttributes() - 1);
      for (int i = 0; i < instance.numAttributes(); i++) {
        if ((attRange.isInRange(i)) && (i != instance.classIndex())) {
          if (firstOutput) {
            text.append("(");
          } else {
            text.append(",");
          }
          text.append(instance.toString(i));
          firstOutput = false;
        }
      }
      if (!firstOutput) {
        text.append(")");
      }
    }
    return text.toString();
  }
  









  protected static String makeOptionString(Classifier classifier, boolean globalInfo)
  {
    StringBuffer optionsText = new StringBuffer("");
    

    optionsText.append("\n\nGeneral options:\n\n");
    optionsText.append("-h or -help\n");
    optionsText.append("\tOutput help information.\n");
    optionsText.append("-synopsis or -info\n");
    optionsText.append("\tOutput synopsis for classifier (use in conjunction  with -h)\n");
    
    optionsText.append("-t <name of training file>\n");
    optionsText.append("\tSets training file.\n");
    optionsText.append("-T <name of test file>\n");
    optionsText.append("\tSets test file. If missing, a cross-validation will be performed\n");
    
    optionsText.append("\ton the training data.\n");
    optionsText.append("-c <class index>\n");
    optionsText.append("\tSets index of class attribute (default: last).\n");
    optionsText.append("-x <number of folds>\n");
    optionsText.append("\tSets number of folds for cross-validation (default: 10).\n");
    
    optionsText.append("-no-cv\n");
    optionsText.append("\tDo not perform any cross validation.\n");
    optionsText.append("-split-percentage <percentage>\n");
    optionsText.append("\tSets the percentage for the train/test set split, e.g., 66.\n");
    
    optionsText.append("-preserve-order\n");
    optionsText.append("\tPreserves the order in the percentage split.\n");
    optionsText.append("-s <random number seed>\n");
    optionsText.append("\tSets random number seed for cross-validation or percentage split\n");
    
    optionsText.append("\t(default: 1).\n");
    optionsText.append("-m <name of file with cost matrix>\n");
    optionsText.append("\tSets file with cost matrix.\n");
    optionsText.append("-l <name of input file>\n");
    optionsText.append("\tSets model input file. In case the filename ends with '.xml',\n");
    
    optionsText.append("\ta PMML file is loaded or, if that fails, options are loaded\n");
    
    optionsText.append("\tfrom the XML file.\n");
    optionsText.append("-d <name of output file>\n");
    optionsText.append("\tSets model output file. In case the filename ends with '.xml',\n");
    
    optionsText.append("\tonly the options are saved to the XML file, not the model.\n");
    
    optionsText.append("-v\n");
    optionsText.append("\tOutputs no statistics for training data.\n");
    optionsText.append("-o\n");
    optionsText.append("\tOutputs statistics only, not the classifier.\n");
    optionsText.append("-i\n");
    optionsText.append("\tOutputs detailed information-retrieval");
    optionsText.append(" statistics for each class.\n");
    optionsText.append("-k\n");
    optionsText.append("\tOutputs information-theoretic statistics.\n");
    optionsText.append("-p <attribute range>\n");
    optionsText.append("\tOnly outputs predictions for test instances (or the train\n\tinstances if no test instances provided and -no-cv is used),\n\talong with attributes (0 for none).\n");
    


    optionsText.append("-distribution\n");
    optionsText.append("\tOutputs the distribution instead of only the prediction\n");
    
    optionsText.append("\tin conjunction with the '-p' option (only nominal classes).\n");
    
    optionsText.append("-r\n");
    optionsText.append("\tOnly outputs cumulative margin distribution.\n");
    if ((classifier instanceof Sourcable)) {
      optionsText.append("-z <class name>\n");
      optionsText.append("\tOnly outputs the source representation of the classifier,\n\tgiving it the supplied name.\n");
    }
    
    if ((classifier instanceof Drawable)) {
      optionsText.append("-g\n");
      optionsText.append("\tOnly outputs the graph representation of the classifier.\n");
    }
    
    optionsText.append("-xml filename | xml-string\n");
    optionsText.append("\tRetrieves the options from the XML-data instead of the command line.\n");
    

    optionsText.append("-threshold-file <file>\n");
    optionsText.append("\tThe file to save the threshold data to.\n\tThe format is determined by the extensions, e.g., '.arff' for ARFF \n\tformat or '.csv' for CSV.\n");
    


    optionsText.append("-threshold-label <label>\n");
    optionsText.append("\tThe class label to determine the threshold data for\n\t(default is the first label)\n");
    



    if ((classifier instanceof OptionHandler)) {
      optionsText.append("\nOptions specific to " + classifier.getClass().getName() + ":\n\n");
      
      Enumeration enu = classifier.listOptions();
      while (enu.hasMoreElements()) {
        Option option = (Option)enu.nextElement();
        optionsText.append(option.synopsis() + '\n');
        optionsText.append(option.description() + "\n");
      }
    }
    

    if (globalInfo) {
      try {
        String gi = getGlobalInfo(classifier);
        optionsText.append(gi);
      }
      catch (Exception ex) {}
    }
    
    return optionsText.toString();
  }
  





  protected static String getGlobalInfo(Classifier classifier)
    throws Exception
  {
    BeanInfo bi = Introspector.getBeanInfo(classifier.getClass());
    
    MethodDescriptor[] methods = bi.getMethodDescriptors();
    Object[] args = new Object[0];
    String result = "\nSynopsis for " + classifier.getClass().getName() + ":\n\n";
    

    for (MethodDescriptor method : methods) {
      String name = method.getDisplayName();
      Method meth = method.getMethod();
      if (name.equals("globalInfo")) {
        String globalInfo = (String)meth.invoke(classifier, args);
        result = result + globalInfo;
        break;
      }
    }
    
    return result;
  }
  








  protected String num2ShortID(int num, char[] IDChars, int IDWidth)
  {
    char[] ID = new char[IDWidth];
    

    for (int i = IDWidth - 1; i >= 0; i--) {
      ID[i] = IDChars[(num % IDChars.length)];
      num = num / IDChars.length - 1;
      if (num < 0) {
        break;
      }
    }
    for (i--; i >= 0; i--) {
      ID[i] = ' ';
    }
    
    return new String(ID);
  }
  







  protected double[] makeDistribution(double predictedClass)
  {
    double[] result = new double[m_NumClasses];
    if (Instance.isMissingValue(predictedClass)) {
      return result;
    }
    if (m_ClassIsNominal) {
      result[((int)predictedClass)] = 1.0D;
    } else {
      result[0] = predictedClass;
    }
    return result;
  }
  








  protected void updateStatsForClassifier(double[] predictedDistribution, Instance instance)
    throws Exception
  {
    int actualClass = (int)instance.classValue();
    
    if (!instance.classIsMissing()) {
      updateMargins(predictedDistribution, actualClass, instance.weight());
      


      int predictedClass = -1;
      double bestProb = 0.0D;
      for (int i = 0; i < m_NumClasses; i++) {
        if (predictedDistribution[i] > bestProb) {
          predictedClass = i;
          bestProb = predictedDistribution[i];
        }
      }
      
      m_WithClass += instance.weight();
      

      if (m_CostMatrix != null) {
        if (predictedClass < 0)
        {




          m_TotalCost += instance.weight() * m_CostMatrix.getMaxCost(actualClass, instance);
        }
        else {
          m_TotalCost += instance.weight() * m_CostMatrix.getElement(actualClass, predictedClass, instance);
        }
      }
      



      if (predictedClass < 0) {
        m_Unclassified += instance.weight();
        return;
      }
      
      double predictedProb = Math.max(Double.MIN_VALUE, predictedDistribution[actualClass]);
      
      double priorProb = Math.max(Double.MIN_VALUE, m_ClassPriors[actualClass] / m_ClassPriorsSum);
      
      if (predictedProb >= priorProb) {
        m_SumKBInfo += (Utils.log2(predictedProb) - Utils.log2(priorProb)) * instance.weight();
      }
      else
      {
        m_SumKBInfo -= (Utils.log2(1.0D - predictedProb) - Utils.log2(1.0D - priorProb)) * instance.weight();
      }
      


      m_SumSchemeEntropy -= Utils.log2(predictedProb) * instance.weight();
      m_SumPriorEntropy -= Utils.log2(priorProb) * instance.weight();
      
      updateNumericScores(predictedDistribution, makeDistribution(instance.classValue()), instance.weight());
      


      m_ConfusionMatrix[actualClass][predictedClass] += instance.weight();
      if (predictedClass != actualClass) {
        m_Incorrect += instance.weight();
      } else {
        m_Correct += instance.weight();
      }
    } else {
      m_MissingClass += instance.weight();
    }
  }
  








  protected void updateStatsForPredictor(double predictedValue, Instance instance)
    throws Exception
  {
    if (!instance.classIsMissing())
    {

      m_WithClass += instance.weight();
      if (Instance.isMissingValue(predictedValue)) {
        m_Unclassified += instance.weight();
        return;
      }
      m_SumClass += instance.weight() * instance.classValue();
      m_SumSqrClass += instance.weight() * instance.classValue() * instance.classValue();
      
      m_SumClassPredicted += instance.weight() * instance.classValue() * predictedValue;
      
      m_SumPredicted += instance.weight() * predictedValue;
      m_SumSqrPredicted += instance.weight() * predictedValue * predictedValue;
      
      if (m_ErrorEstimator == null) {
        setNumericPriorsFromBuffer();
      }
      double predictedProb = Math.max(m_ErrorEstimator.getProbability(predictedValue - instance.classValue()), Double.MIN_VALUE);
      


      double priorProb = Math.max(m_PriorErrorEstimator.getProbability(instance.classValue()), Double.MIN_VALUE);
      


      m_SumSchemeEntropy -= Utils.log2(predictedProb) * instance.weight();
      m_SumPriorEntropy -= Utils.log2(priorProb) * instance.weight();
      m_ErrorEstimator.addValue(predictedValue - instance.classValue(), instance.weight());
      

      updateNumericScores(makeDistribution(predictedValue), makeDistribution(instance.classValue()), instance.weight());
    }
    else
    {
      m_MissingClass += instance.weight();
    }
  }
  









  protected void updateMargins(double[] predictedDistribution, int actualClass, double weight)
  {
    double probActual = predictedDistribution[actualClass];
    double probNext = 0.0D;
    
    for (int i = 0; i < m_NumClasses; i++) {
      if ((i != actualClass) && (predictedDistribution[i] > probNext)) {
        probNext = predictedDistribution[i];
      }
    }
    
    double margin = probActual - probNext;
    int bin = (int)((margin + 1.0D) / 2.0D * k_MarginResolution);
    m_MarginCounts[bin] += weight;
  }
  











  protected void updateNumericScores(double[] predicted, double[] actual, double weight)
  {
    double sumErr = 0.0D;double sumAbsErr = 0.0D;double sumSqrErr = 0.0D;
    double sumPriorAbsErr = 0.0D;double sumPriorSqrErr = 0.0D;
    for (int i = 0; i < m_NumClasses; i++) {
      double diff = predicted[i] - actual[i];
      sumErr += diff;
      sumAbsErr += Math.abs(diff);
      sumSqrErr += diff * diff;
      diff = m_ClassPriors[i] / m_ClassPriorsSum - actual[i];
      sumPriorAbsErr += Math.abs(diff);
      sumPriorSqrErr += diff * diff;
    }
    m_SumErr += weight * sumErr / m_NumClasses;
    m_SumAbsErr += weight * sumAbsErr / m_NumClasses;
    m_SumSqrErr += weight * sumSqrErr / m_NumClasses;
    m_SumPriorAbsErr += weight * sumPriorAbsErr / m_NumClasses;
    m_SumPriorSqrErr += weight * sumPriorSqrErr / m_NumClasses;
  }
  







  protected void addNumericTrainClass(double classValue, double weight)
  {
    if (m_TrainClassVals == null) {
      m_TrainClassVals = new double[100];
      m_TrainClassWeights = new double[100];
    }
    if (m_NumTrainClassVals == m_TrainClassVals.length) {
      double[] temp = new double[m_TrainClassVals.length * 2];
      System.arraycopy(m_TrainClassVals, 0, temp, 0, m_TrainClassVals.length);
      m_TrainClassVals = temp;
      
      temp = new double[m_TrainClassWeights.length * 2];
      System.arraycopy(m_TrainClassWeights, 0, temp, 0, m_TrainClassWeights.length);
      
      m_TrainClassWeights = temp;
    }
    m_TrainClassVals[m_NumTrainClassVals] = classValue;
    m_TrainClassWeights[m_NumTrainClassVals] = weight;
    m_NumTrainClassVals += 1;
  }
  




  protected void setNumericPriorsFromBuffer()
  {
    double numPrecision = 0.01D;
    if (m_NumTrainClassVals > 1) {
      double[] temp = new double[m_NumTrainClassVals];
      System.arraycopy(m_TrainClassVals, 0, temp, 0, m_NumTrainClassVals);
      int[] index = Utils.sort(temp);
      double lastVal = temp[index[0]];
      double deltaSum = 0.0D;
      int distinct = 0;
      for (int i = 1; i < temp.length; i++) {
        double current = temp[index[i]];
        if (current != lastVal) {
          deltaSum += current - lastVal;
          lastVal = current;
          distinct++;
        }
      }
      if (distinct > 0) {
        numPrecision = deltaSum / distinct;
      }
    }
    m_PriorErrorEstimator = new KernelEstimator(numPrecision);
    m_ErrorEstimator = new KernelEstimator(numPrecision); double 
      tmp146_145 = 0.0D;m_ClassPriorsSum = tmp146_145;m_ClassPriors[0] = tmp146_145;
    for (int i = 0; i < m_NumTrainClassVals; i++) {
      m_ClassPriors[0] += m_TrainClassVals[i] * m_TrainClassWeights[i];
      m_ClassPriorsSum += m_TrainClassWeights[i];
      m_PriorErrorEstimator.addValue(m_TrainClassVals[i], m_TrainClassWeights[i]);
    }
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10974 $");
  }
}
