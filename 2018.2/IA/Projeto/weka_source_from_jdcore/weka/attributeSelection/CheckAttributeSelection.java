package weka.attributeSelection;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.CheckScheme;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MultiInstanceCapabilitiesHandler;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SerializationHelper;
import weka.core.SerializedObject;
import weka.core.TestInstances;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;















































































































































































public class CheckAttributeSelection
  extends CheckScheme
{
  protected ASEvaluation m_Evaluator = new CfsSubsetEval();
  

  protected ASSearch m_Search = new Ranker();
  

  protected boolean m_TestEvaluator = true;
  

  public CheckAttributeSelection() {}
  

  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tFull name and options of the evaluator analyzed.\n\teg: weka.attributeSelection.CfsSubsetEval", "eval", 1, "-eval name [options]"));
    



    result.addElement(new Option("\tFull name and options of the search method analyzed.\n\teg: weka.attributeSelection.Ranker", "search", 1, "-search name [options]"));
    



    result.addElement(new Option("\tThe scheme to test, either the evaluator or the search method.\n\t(Default: eval)", "test", 1, "-test <eval|search>"));
    



    if ((m_Evaluator != null) && ((m_Evaluator instanceof OptionHandler))) {
      result.addElement(new Option("", "", 0, "\nOptions specific to evaluator " + m_Evaluator.getClass().getName() + ":"));
      


      Enumeration enm = ((OptionHandler)m_Evaluator).listOptions();
      while (enm.hasMoreElements()) {
        result.addElement(enm.nextElement());
      }
    }
    if ((m_Search != null) && ((m_Search instanceof OptionHandler))) {
      result.addElement(new Option("", "", 0, "\nOptions specific to search method " + m_Search.getClass().getName() + ":"));
      


      Enumeration enm = ((OptionHandler)m_Search).listOptions();
      while (enm.hasMoreElements()) {
        result.addElement(enm.nextElement());
      }
    }
    return result.elements();
  }
  























































































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption("eval", options);
    String[] tmpOptions = Utils.splitOptions(tmpStr);
    if (tmpOptions.length != 0) {
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      setEvaluator((ASEvaluation)forName("weka.attributeSelection", ASEvaluation.class, tmpStr, tmpOptions));
    }
    





    tmpStr = Utils.getOption("search", options);
    tmpOptions = Utils.splitOptions(tmpStr);
    if (tmpOptions.length != 0) {
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      setSearch((ASSearch)forName("weka.attributeSelection", ASSearch.class, tmpStr, tmpOptions));
    }
    





    tmpStr = Utils.getOption("test", options);
    setTestEvaluator(!tmpStr.equalsIgnoreCase("search"));
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-eval");
    if ((getEvaluator() instanceof OptionHandler)) {
      result.add(getEvaluator().getClass().getName() + " " + Utils.joinOptions(((OptionHandler)getEvaluator()).getOptions()));

    }
    else
    {
      result.add(getEvaluator().getClass().getName());
    }
    
    result.add("-search");
    if ((getSearch() instanceof OptionHandler)) {
      result.add(getSearch().getClass().getName() + " " + Utils.joinOptions(((OptionHandler)getSearch()).getOptions()));

    }
    else
    {
      result.add(getSearch().getClass().getName());
    }
    
    result.add("-test");
    if (getTestEvaluator()) {
      result.add("eval");
    } else {
      result.add("search");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  



  public void doTests()
  {
    if (getTestObject() == null) {
      println("\n=== No scheme set ===");
      return;
    }
    println("\n=== Check on scheme: " + getTestObject().getClass().getName() + " ===\n");
    



    m_ClasspathProblems = false;
    println("--> Checking for interfaces");
    canTakeOptions();
    boolean weightedInstancesHandler = weightedInstancesHandler()[0];
    boolean multiInstanceHandler = multiInstanceHandler()[0];
    println("--> Scheme tests");
    declaresSerialVersionUID();
    testsPerClassType(1, weightedInstancesHandler, multiInstanceHandler);
    testsPerClassType(0, weightedInstancesHandler, multiInstanceHandler);
    testsPerClassType(3, weightedInstancesHandler, multiInstanceHandler);
    testsPerClassType(2, weightedInstancesHandler, multiInstanceHandler);
    testsPerClassType(4, weightedInstancesHandler, multiInstanceHandler);
  }
  




  public void setEvaluator(ASEvaluation value)
  {
    m_Evaluator = value;
  }
  




  public ASEvaluation getEvaluator()
  {
    return m_Evaluator;
  }
  




  public void setSearch(ASSearch value)
  {
    m_Search = value;
  }
  




  public ASSearch getSearch()
  {
    return m_Search;
  }
  




  public void setTestEvaluator(boolean value)
  {
    m_TestEvaluator = value;
  }
  




  public boolean getTestEvaluator()
  {
    return m_TestEvaluator;
  }
  





  protected Object getTestObject()
  {
    if (getTestEvaluator()) {
      return getEvaluator();
    }
    return getSearch();
  }
  






  protected Object[] makeCopies(Object obj, int num)
    throws Exception
  {
    if (obj == null) {
      throw new Exception("No object set");
    }
    Object[] objs = new Object[num];
    SerializedObject so = new SerializedObject(obj);
    for (int i = 0; i < objs.length; i++) {
      objs[i] = so.getObject();
    }
    
    return objs;
  }
  












  protected AttributeSelection search(ASSearch search, ASEvaluation eval, Instances data)
    throws Exception
  {
    AttributeSelection result = new AttributeSelection();
    result.setSeed(42);
    result.setSearch(search);
    result.setEvaluator(eval);
    result.SelectAttributes(data);
    
    return result;
  }
  









  protected void testsPerClassType(int classType, boolean weighted, boolean multiInstance)
  {
    boolean PNom = canPredict(true, false, false, false, false, multiInstance, classType)[0];
    boolean PNum = canPredict(false, true, false, false, false, multiInstance, classType)[0];
    boolean PStr = canPredict(false, false, true, false, false, multiInstance, classType)[0];
    boolean PDat = canPredict(false, false, false, true, false, multiInstance, classType)[0];
    boolean PRel;
    boolean PRel; if (!multiInstance) {
      PRel = canPredict(false, false, false, false, true, multiInstance, classType)[0];
    } else {
      PRel = false;
    }
    if ((PNom) || (PNum) || (PStr) || (PDat) || (PRel)) {
      if (weighted) {
        instanceWeights(PNom, PNum, PStr, PDat, PRel, multiInstance, classType);
      }
      if (classType == 1) {
        canHandleNClasses(PNom, PNum, PStr, PDat, PRel, multiInstance, 4);
      }
      if (!multiInstance) {
        canHandleClassAsNthAttribute(PNom, PNum, PStr, PDat, PRel, multiInstance, classType, 0);
        canHandleClassAsNthAttribute(PNom, PNum, PStr, PDat, PRel, multiInstance, classType, 1);
      }
      
      canHandleZeroTraining(PNom, PNum, PStr, PDat, PRel, multiInstance, classType);
      boolean handleMissingPredictors = canHandleMissing(PNom, PNum, PStr, PDat, PRel, multiInstance, classType, true, false, 20)[0];
      

      if (handleMissingPredictors) {
        canHandleMissing(PNom, PNum, PStr, PDat, PRel, multiInstance, classType, true, false, 100);
      }
      boolean handleMissingClass = canHandleMissing(PNom, PNum, PStr, PDat, PRel, multiInstance, classType, false, true, 20)[0];
      

      if (handleMissingClass) {
        canHandleMissing(PNom, PNum, PStr, PDat, PRel, multiInstance, classType, false, true, 100);
      }
      correctSearchInitialisation(PNom, PNum, PStr, PDat, PRel, multiInstance, classType);
      datasetIntegrity(PNom, PNum, PStr, PDat, PRel, multiInstance, classType, handleMissingPredictors, handleMissingClass);
    }
  }
  






  protected boolean[] canTakeOptions()
  {
    boolean[] result = new boolean[2];
    
    print("options...");
    if ((getTestObject() instanceof OptionHandler)) {
      println("yes");
      if (m_Debug) {
        println("\n=== Full report ===");
        Enumeration enu = ((OptionHandler)getTestObject()).listOptions();
        while (enu.hasMoreElements()) {
          Option option = (Option)enu.nextElement();
          print(option.synopsis() + "\n" + option.description() + "\n");
        }
        
        println("\n");
      }
      result[0] = true;
    }
    else {
      println("no");
      result[0] = false;
    }
    
    return result;
  }
  





  protected boolean[] weightedInstancesHandler()
  {
    boolean[] result = new boolean[2];
    
    print("weighted instances scheme...");
    if ((getTestObject() instanceof WeightedInstancesHandler)) {
      println("yes");
      result[0] = true;
    }
    else {
      println("no");
      result[0] = false;
    }
    
    return result;
  }
  




  protected boolean[] multiInstanceHandler()
  {
    boolean[] result = new boolean[2];
    
    print("multi-instance scheme...");
    if ((getTestObject() instanceof MultiInstanceCapabilitiesHandler)) {
      println("yes");
      result[0] = true;
    }
    else {
      println("no");
      result[0] = false;
    }
    
    return result;
  }
  





  protected boolean[] declaresSerialVersionUID()
  {
    boolean[] result = new boolean[2];
    


    print("serialVersionUID...");
    
    boolean eval = !SerializationHelper.needsUID(m_Evaluator.getClass());
    boolean search = !SerializationHelper.needsUID(m_Search.getClass());
    
    result[0] = ((eval) && (search) ? 1 : false);
    
    if (result[0] != 0) {
      println("yes");
    } else {
      println("no");
    }
    return result;
  }
  





















  protected boolean[] canPredict(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType)
  {
    print("basic predict");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    FastVector accepts = new FastVector();
    accepts.addElement("unary");
    accepts.addElement("binary");
    accepts.addElement("nominal");
    accepts.addElement("numeric");
    accepts.addElement("string");
    accepts.addElement("date");
    accepts.addElement("relational");
    accepts.addElement("multi-instance");
    accepts.addElement("not in classpath");
    int numTrain = getNumInstances();int numClasses = 2;int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType, missingLevel, predictorMissing, classMissing, numTrain, numClasses, accepts);
  }
  




























  protected boolean[] canHandleNClasses(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int numClasses)
  {
    print("more than two class problems");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, 1);
    
    print("...");
    FastVector accepts = new FastVector();
    accepts.addElement("number");
    accepts.addElement("class");
    int numTrain = getNumInstances();int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, 1, missingLevel, predictorMissing, classMissing, numTrain, numClasses, accepts);
  }
  





























  protected boolean[] canHandleClassAsNthAttribute(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType, int classIndex)
  {
    if (classIndex == -1) {
      print("class attribute as last attribute");
    } else
      print("class attribute as " + (classIndex + 1) + ". attribute");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    FastVector accepts = new FastVector();
    int numTrain = getNumInstances();int numClasses = 2;int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType, classIndex, missingLevel, predictorMissing, classMissing, numTrain, numClasses, accepts);
  }
  



























  protected boolean[] canHandleZeroTraining(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType)
  {
    print("handle zero training instances");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    FastVector accepts = new FastVector();
    accepts.addElement("train");
    accepts.addElement("value");
    int numTrain = 0;int numClasses = 2;int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType, missingLevel, predictorMissing, classMissing, numTrain, numClasses, accepts);
  }
  































  protected boolean[] correctSearchInitialisation(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType)
  {
    boolean[] result = new boolean[2];
    print("correct initialisation during search");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    int numTrain = getNumInstances();
    int numClasses = 2;int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    Instances train1 = null;
    Instances train2 = null;
    ASSearch search = null;
    ASEvaluation evaluation1A = null;
    ASEvaluation evaluation1B = null;
    ASEvaluation evaluation2 = null;
    AttributeSelection attsel1A = null;
    AttributeSelection attsel1B = null;
    int stage = 0;
    
    try
    {
      train1 = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      train2 = makeTestDataset(84, numTrain, nominalPredictor ? getNumNominal() + 1 : 0, numericPredictor ? getNumNumeric() + 1 : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      if (missingLevel > 0) {
        addMissing(train1, missingLevel, predictorMissing, classMissing);
        addMissing(train2, missingLevel, predictorMissing, classMissing);
      }
      
      search = ASSearch.makeCopies(getSearch(), 1)[0];
      evaluation1A = ASEvaluation.makeCopies(getEvaluator(), 1)[0];
      evaluation1B = ASEvaluation.makeCopies(getEvaluator(), 1)[0];
      evaluation2 = ASEvaluation.makeCopies(getEvaluator(), 1)[0];
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      stage = 0;
      attsel1A = search(search, evaluation1A, train1);
      
      stage = 1;
      search(search, evaluation2, train2);
      
      stage = 2;
      attsel1B = search(search, evaluation1B, train1);
      
      stage = 3;
      if (!attsel1A.toResultsString().equals(attsel1B.toResultsString())) {
        if (m_Debug) {
          println("\n=== Full report ===\n\nFirst search\n" + attsel1A.toResultsString() + "\n\n");
          



          println("\nSecond search\n" + attsel1B.toResultsString() + "\n\n");
        }
        


        throw new Exception("Results differ between search calls");
      }
      println("yes");
      result[0] = true;






    }
    catch (Exception ex)
    {





      println("no");
      result[0] = false;
      if (m_Debug) {
        println("\n=== Full Report ===");
        print("Problem during  training");
        switch (stage) {
        case 0: 
          print(" of dataset 1");
          break;
        case 1: 
          print(" of dataset 2");
          break;
        case 2: 
          print(" of dataset 1 (2nd build)");
          break;
        case 3: 
          print(", comparing results from builds of dataset 1");
        }
        
        println(": " + ex.getMessage() + "\n");
        println("here are the datasets:\n");
        println("=== Train1 Dataset ===\n" + train1.toString() + "\n");
        
        println("=== Train2 Dataset ===\n" + train2.toString() + "\n");
      }
    }
    

    return result;
  }
  





























  protected boolean[] canHandleMissing(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType, boolean predictorMissing, boolean classMissing, int missingLevel)
  {
    if (missingLevel == 100)
      print("100% ");
    print("missing");
    if (predictorMissing) {
      print(" predictor");
      if (classMissing)
        print(" and");
    }
    if (classMissing)
      print(" class");
    print(" values");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    FastVector accepts = new FastVector();
    accepts.addElement("missing");
    accepts.addElement("value");
    accepts.addElement("train");
    accepts.addElement("no attributes");
    int numTrain = getNumInstances();int numClasses = 2;
    
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType, missingLevel, predictorMissing, classMissing, numTrain, numClasses, accepts);
  }
  
































  protected boolean[] instanceWeights(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType)
  {
    print("scheme uses instance weights");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    int numTrain = 2 * getNumInstances();
    int numClasses = 2;int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    boolean[] result = new boolean[2];
    Instances train = null;
    ASSearch[] search = null;
    ASEvaluation evaluationB = null;
    ASEvaluation evaluationI = null;
    AttributeSelection attselB = null;
    AttributeSelection attselI = null;
    boolean evalFail = false;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() + 1 : 0, numericPredictor ? getNumNumeric() + 1 : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      if (missingLevel > 0)
        addMissing(train, missingLevel, predictorMissing, classMissing);
      search = ASSearch.makeCopies(getSearch(), 2);
      evaluationB = ASEvaluation.makeCopies(getEvaluator(), 1)[0];
      evaluationI = ASEvaluation.makeCopies(getEvaluator(), 1)[0];
      attselB = search(search[0], evaluationB, train);
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    
    try
    {
      for (int i = 0; i < train.numInstances(); i++) {
        train.instance(i).setWeight(0.0D);
      }
      Random random = new Random(1L);
      for (int i = 0; i < train.numInstances() / 2; i++) {
        int inst = Math.abs(random.nextInt()) % train.numInstances();
        int weight = Math.abs(random.nextInt()) % 10 + 1;
        train.instance(inst).setWeight(weight);
      }
      attselI = search(search[1], evaluationI, train);
      if (attselB.toResultsString().equals(attselI.toResultsString()))
      {
        evalFail = true;
        throw new Exception("evalFail");
      }
      
      println("yes");
      result[0] = true;
    } catch (Exception ex) {
      println("no");
      result[0] = false;
      
      if (!m_Debug) break label624; }
    println("\n=== Full Report ===");
    
    if (evalFail) {
      println("Results don't differ between non-weighted and weighted instance models.");
      
      println("Here are the results:\n");
      println("\nboth methods\n");
      println(evaluationB.toString());
    } else {
      print("Problem during training");
      println(": " + ex.getMessage() + "\n");
    }
    println("Here is the dataset:\n");
    println("=== Train Dataset ===\n" + train.toString() + "\n");
    
    println("=== Train Weights ===\n");
    for (int i = 0; i < train.numInstances(); i++) {
      println(" " + (i + 1) + "    " + train.instance(i).weight());
    }
    

    label624:
    
    return result;
  }
  





























  protected boolean[] datasetIntegrity(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType, boolean predictorMissing, boolean classMissing)
  {
    print("scheme doesn't alter original datasets");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    int numTrain = getNumInstances();
    int numClasses = 2;int missingLevel = 20;
    
    boolean[] result = new boolean[2];
    Instances train = null;
    Instances trainCopy = null;
    ASSearch search = null;
    ASEvaluation evaluation = null;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      if (missingLevel > 0)
        addMissing(train, missingLevel, predictorMissing, classMissing);
      search = ASSearch.makeCopies(getSearch(), 1)[0];
      evaluation = ASEvaluation.makeCopies(getEvaluator(), 1)[0];
      trainCopy = new Instances(train);
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      search(search, evaluation, trainCopy);
      compareDatasets(train, trainCopy);
      
      println("yes");
      result[0] = true;
    } catch (Exception ex) {
      println("no");
      result[0] = false;
      
      if (m_Debug) {
        println("\n=== Full Report ===");
        print("Problem during training");
        println(": " + ex.getMessage() + "\n");
        println("Here are the datasets:\n");
        println("=== Train Dataset (original) ===\n" + trainCopy.toString() + "\n");
        
        println("=== Train Dataset ===\n" + train.toString() + "\n");
      }
    }
    

    return result;
  }
  
































  protected boolean[] runBasicTest(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType, int missingLevel, boolean predictorMissing, boolean classMissing, int numTrain, int numClasses, FastVector accepts)
  {
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType, -1, missingLevel, predictorMissing, classMissing, numTrain, numClasses, accepts);
  }
  
















































  protected boolean[] runBasicTest(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType, int classIndex, int missingLevel, boolean predictorMissing, boolean classMissing, int numTrain, int numClasses, FastVector accepts)
  {
    boolean[] result = new boolean[2];
    Instances train = null;
    ASSearch search = null;
    ASEvaluation evaluation = null;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, classIndex, multiInstance);
      








      if (missingLevel > 0)
        addMissing(train, missingLevel, predictorMissing, classMissing);
      search = ASSearch.makeCopies(getSearch(), 1)[0];
      evaluation = ASEvaluation.makeCopies(getEvaluator(), 1)[0];
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      search(search, evaluation, train);
      println("yes");
      result[0] = true;
    }
    catch (Exception ex) {
      boolean acceptable = false;
      String msg;
      String msg; if (ex.getMessage() == null) {
        msg = "";
      } else
        msg = ex.getMessage().toLowerCase();
      if (msg.indexOf("not in classpath") > -1)
        m_ClasspathProblems = true;
      for (int i = 0; i < accepts.size(); i++) {
        if (msg.indexOf((String)accepts.elementAt(i)) >= 0) {
          acceptable = true;
        }
      }
      
      println("no" + (acceptable ? " (OK error message)" : ""));
      result[1] = acceptable;
      
      if (m_Debug) {
        println("\n=== Full Report ===");
        print("Problem during training");
        println(": " + ex.getMessage() + "\n");
        if (!acceptable) {
          if (accepts.size() > 0) {
            print("Error message doesn't mention ");
            for (int i = 0; i < accepts.size(); i++) {
              if (i != 0) {
                print(" or ");
              }
              print('"' + (String)accepts.elementAt(i) + '"');
            }
          }
          println("here is the dataset:\n");
          println("=== Train Dataset ===\n" + train.toString() + "\n");
        }
      }
    }
    

    return result;
  }
  























  protected Instances makeTestDataset(int seed, int numInstances, int numNominal, int numNumeric, int numString, int numDate, int numRelational, int numClasses, int classType, boolean multiInstance)
    throws Exception
  {
    return makeTestDataset(seed, numInstances, numNominal, numNumeric, numString, numDate, numRelational, numClasses, classType, -1, multiInstance);
  }
  





































  protected Instances makeTestDataset(int seed, int numInstances, int numNominal, int numNumeric, int numString, int numDate, int numRelational, int numClasses, int classType, int classIndex, boolean multiInstance)
    throws Exception
  {
    TestInstances dataset = new TestInstances();
    
    dataset.setSeed(seed);
    dataset.setNumInstances(numInstances);
    dataset.setNumNominal(numNominal);
    dataset.setNumNumeric(numNumeric);
    dataset.setNumString(numString);
    dataset.setNumDate(numDate);
    dataset.setNumRelational(numRelational);
    dataset.setNumClasses(numClasses);
    dataset.setClassType(classType);
    dataset.setClassIndex(classIndex);
    dataset.setNumClasses(numClasses);
    dataset.setMultiInstance(multiInstance);
    dataset.setWords(getWords());
    dataset.setWordSeparators(getWordSeparators());
    
    return process(dataset.generate());
  }
  

















  protected void printAttributeSummary(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType)
  {
    String str = "";
    
    if (numericPredictor) {
      str = str + " numeric";
    }
    if (nominalPredictor) {
      if (str.length() > 0)
        str = str + " &";
      str = str + " nominal";
    }
    
    if (stringPredictor) {
      if (str.length() > 0)
        str = str + " &";
      str = str + " string";
    }
    
    if (datePredictor) {
      if (str.length() > 0)
        str = str + " &";
      str = str + " date";
    }
    
    if (relationalPredictor) {
      if (str.length() > 0)
        str = str + " &";
      str = str + " relational";
    }
    
    str = str + " predictors)";
    
    switch (classType) {
    case 0: 
      str = " (numeric class," + str;
      break;
    case 1: 
      str = " (nominal class," + str;
      break;
    case 2: 
      str = " (string class," + str;
      break;
    case 3: 
      str = " (date class," + str;
      break;
    case 4: 
      str = " (relational class," + str;
    }
    
    
    print(str);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 4783 $");
  }
  




  public static void main(String[] args)
  {
    runCheck(new CheckAttributeSelection(), args);
  }
}
