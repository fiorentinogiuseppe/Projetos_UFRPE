package weka.classifiers;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.rules.ZeroR;
import weka.core.CheckScheme;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MultiInstanceCapabilitiesHandler;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SerializationHelper;
import weka.core.TestInstances;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;






























































































































































public class CheckClassifier
  extends CheckScheme
{
  protected Classifier m_Classifier = new ZeroR();
  

  public CheckClassifier() {}
  

  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tFull name of the classifier analysed.\n\teg: weka.classifiers.bayes.NaiveBayes\n\t(default weka.classifiers.rules.ZeroR)", "W", 1, "-W"));
    




    if ((m_Classifier != null) && ((m_Classifier instanceof OptionHandler)))
    {
      result.addElement(new Option("", "", 0, "\nOptions specific to classifier " + m_Classifier.getClass().getName() + ":"));
      


      Enumeration enu = m_Classifier.listOptions();
      while (enu.hasMoreElements()) {
        result.addElement(enu.nextElement());
      }
    }
    return result.elements();
  }
  




























































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('W', options);
    if (tmpStr.length() == 0)
      tmpStr = ZeroR.class.getName();
    setClassifier((Classifier)forName("weka.classifiers", Classifier.class, tmpStr, Utils.partitionOptions(options)));
  }
  













  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    if (getClassifier() != null) {
      result.add("-W");
      result.add(getClassifier().getClass().getName());
    }
    
    if ((m_Classifier != null) && ((m_Classifier instanceof OptionHandler))) {
      options = m_Classifier.getOptions();
    } else {
      options = new String[0];
    }
    if (options.length > 0) {
      result.add("--");
      for (i = 0; i < options.length; i++) {
        result.add(options[i]);
      }
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  



  public void doTests()
  {
    if (getClassifier() == null) {
      println("\n=== No classifier set ===");
      return;
    }
    println("\n=== Check on Classifier: " + getClassifier().getClass().getName() + " ===\n");
    



    m_ClasspathProblems = false;
    println("--> Checking for interfaces");
    canTakeOptions();
    boolean updateableClassifier = updateableClassifier()[0];
    boolean weightedInstancesHandler = weightedInstancesHandler()[0];
    boolean multiInstanceHandler = multiInstanceHandler()[0];
    println("--> Classifier tests");
    declaresSerialVersionUID();
    testToString();
    testsPerClassType(1, updateableClassifier, weightedInstancesHandler, multiInstanceHandler);
    testsPerClassType(0, updateableClassifier, weightedInstancesHandler, multiInstanceHandler);
    testsPerClassType(3, updateableClassifier, weightedInstancesHandler, multiInstanceHandler);
    testsPerClassType(2, updateableClassifier, weightedInstancesHandler, multiInstanceHandler);
    testsPerClassType(4, updateableClassifier, weightedInstancesHandler, multiInstanceHandler);
  }
  




  public void setClassifier(Classifier newClassifier)
  {
    m_Classifier = newClassifier;
  }
  




  public Classifier getClassifier()
  {
    return m_Classifier;
  }
  











  protected void testsPerClassType(int classType, boolean updateable, boolean weighted, boolean multiInstance)
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
      canHandleOnlyClass(PNom, PNum, PStr, PDat, PRel, classType);
      
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
      correctBuildInitialisation(PNom, PNum, PStr, PDat, PRel, multiInstance, classType);
      datasetIntegrity(PNom, PNum, PStr, PDat, PRel, multiInstance, classType, handleMissingPredictors, handleMissingClass);
      
      doesntUseTestClassVal(PNom, PNum, PStr, PDat, PRel, multiInstance, classType);
      if (updateable) {
        updatingEquality(PNom, PNum, PStr, PDat, PRel, multiInstance, classType);
      }
    }
  }
  




  protected boolean[] testToString()
  {
    boolean[] result = new boolean[2];
    
    print("toString...");
    try
    {
      Classifier copy = (Classifier)m_Classifier.getClass().newInstance();
      copy.toString();
      result[0] = true;
      println("yes");
    }
    catch (Exception e) {
      result[0] = false;
      println("no");
      if (m_Debug) {
        println("\n=== Full report ===");
        e.printStackTrace();
        println("\n");
      }
    }
    
    return result;
  }
  





  protected boolean[] declaresSerialVersionUID()
  {
    boolean[] result = new boolean[2];
    
    print("serialVersionUID...");
    
    result[0] = (!SerializationHelper.needsUID(m_Classifier.getClass()) ? 1 : false);
    
    if (result[0] != 0) {
      println("yes");
    } else {
      println("no");
    }
    return result;
  }
  





  protected boolean[] canTakeOptions()
  {
    boolean[] result = new boolean[2];
    
    print("options...");
    if ((m_Classifier instanceof OptionHandler)) {
      println("yes");
      if (m_Debug) {
        println("\n=== Full report ===");
        Enumeration enu = m_Classifier.listOptions();
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
  





  protected boolean[] updateableClassifier()
  {
    boolean[] result = new boolean[2];
    
    print("updateable classifier...");
    if ((m_Classifier instanceof UpdateableClassifier)) {
      println("yes");
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
    
    print("weighted instances classifier...");
    if ((m_Classifier instanceof WeightedInstancesHandler)) {
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
    
    print("multi-instance classifier...");
    if ((m_Classifier instanceof MultiInstanceCapabilitiesHandler)) {
      println("yes");
      result[0] = true;
    }
    else {
      println("no");
      result[0] = false;
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
    int numTrain = getNumInstances();int numTest = getNumInstances();
    int numClasses = 2;int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType, missingLevel, predictorMissing, classMissing, numTrain, numTest, numClasses, accepts);
  }
  


























  protected boolean[] canHandleOnlyClass(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, int classType)
  {
    print("only class in data");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, false, classType);
    
    print("...");
    FastVector accepts = new FastVector();
    accepts.addElement("class");
    accepts.addElement("zeror");
    int numTrain = getNumInstances();int numTest = getNumInstances();
    int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    return runBasicTest(false, false, false, false, false, false, classType, missingLevel, predictorMissing, classMissing, numTrain, numTest, 2, accepts);
  }
  



























  protected boolean[] canHandleNClasses(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int numClasses)
  {
    print("more than two class problems");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, 1);
    
    print("...");
    FastVector accepts = new FastVector();
    accepts.addElement("number");
    accepts.addElement("class");
    int numTrain = getNumInstances();int numTest = getNumInstances();
    int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, 1, missingLevel, predictorMissing, classMissing, numTrain, numTest, numClasses, accepts);
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
    int numTrain = getNumInstances();int numTest = getNumInstances();int numClasses = 2;
    int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType, classIndex, missingLevel, predictorMissing, classMissing, numTrain, numTest, numClasses, accepts);
  }
  



























  protected boolean[] canHandleZeroTraining(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType)
  {
    print("handle zero training instances");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    FastVector accepts = new FastVector();
    accepts.addElement("train");
    accepts.addElement("value");
    int numTrain = 0;int numTest = getNumInstances();int numClasses = 2;
    int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType, missingLevel, predictorMissing, classMissing, numTrain, numTest, numClasses, accepts);
  }
  


































  protected boolean[] correctBuildInitialisation(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType)
  {
    boolean[] result = new boolean[2];
    
    print("correct initialisation during buildClassifier");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    int numTrain = getNumInstances();int numTest = getNumInstances();
    int numClasses = 2;int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    Instances train1 = null;
    Instances test1 = null;
    Instances train2 = null;
    Instances test2 = null;
    Classifier classifier = null;
    Evaluation evaluation1A = null;
    Evaluation evaluation1B = null;
    Evaluation evaluation2 = null;
    boolean built = false;
    int stage = 0;
    

    try
    {
      train1 = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      train2 = makeTestDataset(84, numTrain, nominalPredictor ? getNumNominal() + 1 : 0, numericPredictor ? getNumNumeric() + 1 : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      test1 = makeTestDataset(24, numTest, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      test2 = makeTestDataset(48, numTest, nominalPredictor ? getNumNominal() + 1 : 0, numericPredictor ? getNumNumeric() + 1 : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      if (missingLevel > 0) {
        addMissing(train1, missingLevel, predictorMissing, classMissing);
        addMissing(test1, Math.min(missingLevel, 50), predictorMissing, classMissing);
        
        addMissing(train2, missingLevel, predictorMissing, classMissing);
        addMissing(test2, Math.min(missingLevel, 50), predictorMissing, classMissing);
      }
      

      classifier = Classifier.makeCopies(getClassifier(), 1)[0];
      evaluation1A = new Evaluation(train1);
      evaluation1B = new Evaluation(train1);
      evaluation2 = new Evaluation(train2);
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      stage = 0;
      classifier.buildClassifier(train1);
      built = true;
      if (testWRTZeroR(classifier, evaluation1A, train1, test1)[0] == 0) {
        throw new Exception("Scheme performs worse than ZeroR");
      }
      
      stage = 1;
      built = false;
      classifier.buildClassifier(train2);
      built = true;
      if (testWRTZeroR(classifier, evaluation2, train2, test2)[0] == 0) {
        throw new Exception("Scheme performs worse than ZeroR");
      }
      
      stage = 2;
      built = false;
      classifier.buildClassifier(train1);
      built = true;
      if (testWRTZeroR(classifier, evaluation1B, train1, test1)[0] == 0) {
        throw new Exception("Scheme performs worse than ZeroR");
      }
      
      stage = 3;
      if (!evaluation1A.equals(evaluation1B)) {
        if (m_Debug) {
          println("\n=== Full report ===\n" + evaluation1A.toSummaryString("\nFirst buildClassifier()", true) + "\n\n");
          


          println(evaluation1B.toSummaryString("\nSecond buildClassifier()", true) + "\n\n");
        }
        


        throw new Exception("Results differ between buildClassifier calls");
      }
      println("yes");
      result[0] = true;





    }
    catch (Exception ex)
    {





      String msg = ex.getMessage().toLowerCase();
      if (msg.indexOf("worse than zeror") >= 0) {
        println("warning: performs worse than ZeroR");
        result[0] = (stage < 1 ? 1 : false);
        result[1] = (stage < 1 ? 1 : false);
      } else {
        println("no");
        result[0] = false;
      }
      if (m_Debug) {
        println("\n=== Full Report ===");
        print("Problem during");
        if (built) {
          print(" testing");
        } else {
          print(" training");
        }
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
        
        println("=== Test1 Dataset ===\n" + test1.toString() + "\n\n");
        
        println("=== Train2 Dataset ===\n" + train2.toString() + "\n");
        
        println("=== Test2 Dataset ===\n" + test2.toString() + "\n\n");
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
    int numTrain = getNumInstances();int numTest = getNumInstances();
    int numClasses = 2;
    
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType, missingLevel, predictorMissing, classMissing, numTrain, numTest, numClasses, accepts);
  }
  





























  protected boolean[] updatingEquality(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType)
  {
    print("incremental training produces the same results as batch training");
    
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    int numTrain = getNumInstances();int numTest = getNumInstances();
    int numClasses = 2;int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    boolean[] result = new boolean[2];
    Instances train = null;
    Instances test = null;
    Classifier[] classifiers = null;
    Evaluation evaluationB = null;
    Evaluation evaluationI = null;
    boolean built = false;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      test = makeTestDataset(24, numTest, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      if (missingLevel > 0) {
        addMissing(train, missingLevel, predictorMissing, classMissing);
        addMissing(test, Math.min(missingLevel, 50), predictorMissing, classMissing);
      }
      
      classifiers = Classifier.makeCopies(getClassifier(), 2);
      evaluationB = new Evaluation(train);
      evaluationI = new Evaluation(train);
      classifiers[0].buildClassifier(train);
      testWRTZeroR(classifiers[0], evaluationB, train, test);
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      classifiers[1].buildClassifier(new Instances(train, 0));
      for (int i = 0; i < train.numInstances(); i++) {
        ((UpdateableClassifier)classifiers[1]).updateClassifier(train.instance(i));
      }
      
      built = true;
      testWRTZeroR(classifiers[1], evaluationI, train, test);
      if (!evaluationB.equals(evaluationI)) {
        println("no");
        result[0] = false;
        
        if (m_Debug) {
          println("\n=== Full Report ===");
          println("Results differ between batch and incrementally built models.\nDepending on the classifier, this may be OK");
          

          println("Here are the results:\n");
          println(evaluationB.toSummaryString("\nbatch built results\n", true));
          
          println(evaluationI.toSummaryString("\nincrementally built results\n", true));
          
          println("Here are the datasets:\n");
          println("=== Train Dataset ===\n" + train.toString() + "\n");
          
          println("=== Test Dataset ===\n" + test.toString() + "\n\n");
        }
      }
      else
      {
        println("yes");
        result[0] = true;
      }
    } catch (Exception ex) {
      result[0] = false;
      
      print("Problem during");
      if (built) {
        print(" testing");
      } else
        print(" training");
      println(": " + ex.getMessage() + "\n");
    }
    
    return result;
  }
  






















  protected boolean[] doesntUseTestClassVal(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType)
  {
    print("classifier ignores test instance class vals");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    int numTrain = 2 * getNumInstances();int numTest = getNumInstances();
    int numClasses = 2;int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    boolean[] result = new boolean[2];
    Instances train = null;
    Instances test = null;
    Classifier[] classifiers = null;
    boolean evalFail = false;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() + 1 : 0, numericPredictor ? getNumNumeric() + 1 : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      test = makeTestDataset(24, numTest, nominalPredictor ? getNumNominal() + 1 : 0, numericPredictor ? getNumNumeric() + 1 : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      if (missingLevel > 0) {
        addMissing(train, missingLevel, predictorMissing, classMissing);
        addMissing(test, Math.min(missingLevel, 50), predictorMissing, classMissing);
      }
      
      classifiers = Classifier.makeCopies(getClassifier(), 2);
      classifiers[0].buildClassifier(train);
      classifiers[1].buildClassifier(train);
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    
    try
    {
      for (int i = 0; i < test.numInstances(); i++) {
        Instance testInst = test.instance(i);
        Instance classMissingInst = (Instance)testInst.copy();
        classMissingInst.setDataset(test);
        classMissingInst.setClassMissing();
        double[] dist0 = classifiers[0].distributionForInstance(testInst);
        double[] dist1 = classifiers[1].distributionForInstance(classMissingInst);
        for (int j = 0; j < dist0.length; j++)
        {
          if ((Double.isNaN(dist0[j])) && (Double.isNaN(dist1[j]))) {
            if (getDebug()) {
              System.out.println("Both predictions are NaN!");
            }
            
          }
          else if (dist0[j] != dist1[j]) {
            throw new Exception("Prediction different for instance " + (i + 1));
          }
        }
      }
      
      println("yes");
      result[0] = true;
    } catch (Exception ex) {
      println("no");
      result[0] = false;
      
      if (m_Debug) {
        println("\n=== Full Report ===");
        
        if (evalFail) {
          println("Results differ between non-missing and missing test class values.");
        }
        else {
          print("Problem during testing");
          println(": " + ex.getMessage() + "\n");
        }
        println("Here are the datasets:\n");
        println("=== Train Dataset ===\n" + train.toString() + "\n");
        
        println("=== Train Weights ===\n");
        for (int i = 0; i < train.numInstances(); i++) {
          println(" " + (i + 1) + "    " + train.instance(i).weight());
        }
        
        println("=== Test Dataset ===\n" + test.toString() + "\n\n");
        
        println("(test weights all 1.0\n");
      }
    }
    
    return result;
  }
  


























  protected boolean[] instanceWeights(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType)
  {
    print("classifier uses instance weights");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    int numTrain = 2 * getNumInstances();int numTest = getNumInstances();
    int numClasses = 2;int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    boolean[] result = new boolean[2];
    Instances train = null;
    Instances test = null;
    Classifier[] classifiers = null;
    Evaluation evaluationB = null;
    Evaluation evaluationI = null;
    boolean built = false;
    boolean evalFail = false;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() + 1 : 0, numericPredictor ? getNumNumeric() + 1 : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      test = makeTestDataset(24, numTest, nominalPredictor ? getNumNominal() + 1 : 0, numericPredictor ? getNumNumeric() + 1 : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      if (missingLevel > 0) {
        addMissing(train, missingLevel, predictorMissing, classMissing);
        addMissing(test, Math.min(missingLevel, 50), predictorMissing, classMissing);
      }
      
      classifiers = Classifier.makeCopies(getClassifier(), 2);
      evaluationB = new Evaluation(train);
      evaluationI = new Evaluation(train);
      classifiers[0].buildClassifier(train);
      testWRTZeroR(classifiers[0], evaluationB, train, test);
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
      classifiers[1].buildClassifier(train);
      built = true;
      testWRTZeroR(classifiers[1], evaluationI, train, test);
      if (evaluationB.equals(evaluationI))
      {
        evalFail = true;
        throw new Exception("evalFail");
      }
      
      println("yes");
      result[0] = true;
    } catch (Exception ex) {
      println("no");
      result[0] = false;
      
      if (m_Debug) {
        println("\n=== Full Report ===");
        
        if (evalFail) {
          println("Results don't differ between non-weighted and weighted instance models.");
          
          println("Here are the results:\n");
          println(evaluationB.toSummaryString("\nboth methods\n", true));
        }
        else {
          print("Problem during");
          if (built) {
            print(" testing");
          } else {
            print(" training");
          }
          println(": " + ex.getMessage() + "\n");
        }
        println("Here are the datasets:\n");
        println("=== Train Dataset ===\n" + train.toString() + "\n");
        
        println("=== Train Weights ===\n");
        for (int i = 0; i < train.numInstances(); i++) {
          println(" " + (i + 1) + "    " + train.instance(i).weight());
        }
        
        println("=== Test Dataset ===\n" + test.toString() + "\n\n");
        
        println("(test weights all 1.0\n");
      }
    }
    
    return result;
  }
  





























  protected boolean[] datasetIntegrity(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType, boolean predictorMissing, boolean classMissing)
  {
    print("classifier doesn't alter original datasets");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    int numTrain = getNumInstances();int numTest = getNumInstances();
    int numClasses = 2;int missingLevel = 20;
    
    boolean[] result = new boolean[2];
    Instances train = null;
    Instances test = null;
    Classifier classifier = null;
    Evaluation evaluation = null;
    boolean built = false;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      test = makeTestDataset(24, numTest, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      if (missingLevel > 0) {
        addMissing(train, missingLevel, predictorMissing, classMissing);
        addMissing(test, Math.min(missingLevel, 50), predictorMissing, classMissing);
      }
      
      classifier = Classifier.makeCopies(getClassifier(), 1)[0];
      evaluation = new Evaluation(train);
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      Instances trainCopy = new Instances(train);
      Instances testCopy = new Instances(test);
      classifier.buildClassifier(trainCopy);
      compareDatasets(train, trainCopy);
      built = true;
      testWRTZeroR(classifier, evaluation, trainCopy, testCopy);
      compareDatasets(test, testCopy);
      
      println("yes");
      result[0] = true;
    } catch (Exception ex) {
      println("no");
      result[0] = false;
      
      if (m_Debug) {
        println("\n=== Full Report ===");
        print("Problem during");
        if (built) {
          print(" testing");
        } else {
          print(" training");
        }
        println(": " + ex.getMessage() + "\n");
        println("Here are the datasets:\n");
        println("=== Train Dataset ===\n" + train.toString() + "\n");
        
        println("=== Test Dataset ===\n" + test.toString() + "\n\n");
      }
    }
    

    return result;
  }
  


































  protected boolean[] runBasicTest(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType, int missingLevel, boolean predictorMissing, boolean classMissing, int numTrain, int numTest, int numClasses, FastVector accepts)
  {
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType, -1, missingLevel, predictorMissing, classMissing, numTrain, numTest, numClasses, accepts);
  }
  



















































  protected boolean[] runBasicTest(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType, int classIndex, int missingLevel, boolean predictorMissing, boolean classMissing, int numTrain, int numTest, int numClasses, FastVector accepts)
  {
    boolean[] result = new boolean[2];
    Instances train = null;
    Instances test = null;
    Classifier classifier = null;
    Evaluation evaluation = null;
    boolean built = false;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, classIndex, multiInstance);
      








      test = makeTestDataset(24, numTest, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, classIndex, multiInstance);
      








      if (missingLevel > 0) {
        addMissing(train, missingLevel, predictorMissing, classMissing);
        addMissing(test, Math.min(missingLevel, 50), predictorMissing, classMissing);
      }
      
      classifier = Classifier.makeCopies(getClassifier(), 1)[0];
      evaluation = new Evaluation(train);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      classifier.buildClassifier(train);
      built = true;
      if (testWRTZeroR(classifier, evaluation, train, test)[0] == 0) {
        result[0] = true;
        result[1] = true;
        throw new Exception("Scheme performs worse than ZeroR");
      }
      
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
      if (msg.indexOf("worse than zeror") >= 0) {
        println("warning: performs worse than ZeroR");
        result[0] = true;
        result[1] = true;
      } else {
        for (int i = 0; i < accepts.size(); i++) {
          if (msg.indexOf((String)accepts.elementAt(i)) >= 0) {
            acceptable = true;
          }
        }
        
        println("no" + (acceptable ? " (OK error message)" : ""));
        result[1] = acceptable;
      }
      
      if (m_Debug) {
        println("\n=== Full Report ===");
        print("Problem during");
        if (built) {
          print(" testing");
        } else {
          print(" training");
        }
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
          println("here are the datasets:\n");
          println("=== Train Dataset ===\n" + train.toString() + "\n");
          
          println("=== Test Dataset ===\n" + test.toString() + "\n\n");
        }
      }
    }
    

    return result;
  }
  












  protected boolean[] testWRTZeroR(Classifier classifier, Evaluation evaluation, Instances train, Instances test)
    throws Exception
  {
    boolean[] result = new boolean[2];
    
    evaluation.evaluateModel(classifier, test, new Object[0]);
    
    try
    {
      Classifier zeroR = new ZeroR();
      zeroR.buildClassifier(train);
      Evaluation zeroREval = new Evaluation(train);
      zeroREval.evaluateModel(zeroR, test, new Object[0]);
      result[0] = Utils.grOrEq(zeroREval.errorRate(), evaluation.errorRate());
    }
    catch (Exception ex) {
      throw new Error("Problem determining ZeroR performance: " + ex.getMessage());
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
    return RevisionUtils.extract("$Revision: 1.33 $");
  }
  




  public static void main(String[] args)
  {
    runCheck(new CheckClassifier(), args);
  }
}
