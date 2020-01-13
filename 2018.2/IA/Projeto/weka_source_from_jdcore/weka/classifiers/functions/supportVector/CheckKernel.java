package weka.classifiers.functions.supportVector;

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
import weka.core.TestInstances;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;




































































































































































public class CheckKernel
  extends CheckScheme
{
  protected Kernel m_Kernel = new RBFKernel();
  

  public CheckKernel() {}
  

  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tFull name of the kernel analysed.\n\teg: weka.classifiers.functions.supportVector.RBFKernel\n\t(default weka.classifiers.functions.supportVector.RBFKernel)", "W", 1, "-W"));
    




    if ((m_Kernel != null) && ((m_Kernel instanceof OptionHandler)))
    {
      result.addElement(new Option("", "", 0, "\nOptions specific to kernel " + m_Kernel.getClass().getName() + ":"));
      


      Enumeration enu = m_Kernel.listOptions();
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
      tmpStr = RBFKernel.class.getName();
    setKernel((Kernel)forName("weka.classifiers.functions.supportVector", Kernel.class, tmpStr, Utils.partitionOptions(options)));
  }
  













  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    if (getKernel() != null) {
      result.add("-W");
      result.add(getKernel().getClass().getName());
    }
    
    if ((m_Kernel != null) && ((m_Kernel instanceof OptionHandler))) {
      options = m_Kernel.getOptions();
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
    if (getKernel() == null) {
      println("\n=== No kernel set ===");
      return;
    }
    println("\n=== Check on kernel: " + getKernel().getClass().getName() + " ===\n");
    



    m_ClasspathProblems = false;
    println("--> Checking for interfaces");
    canTakeOptions();
    boolean weightedInstancesHandler = weightedInstancesHandler()[0];
    boolean multiInstanceHandler = multiInstanceHandler()[0];
    println("--> Kernel tests");
    declaresSerialVersionUID();
    testsPerClassType(1, weightedInstancesHandler, multiInstanceHandler);
    testsPerClassType(0, weightedInstancesHandler, multiInstanceHandler);
    testsPerClassType(3, weightedInstancesHandler, multiInstanceHandler);
    testsPerClassType(2, weightedInstancesHandler, multiInstanceHandler);
    testsPerClassType(4, weightedInstancesHandler, multiInstanceHandler);
  }
  




  public void setKernel(Kernel value)
  {
    m_Kernel = value;
  }
  




  public Kernel getKernel()
  {
    return m_Kernel;
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
      correctBuildInitialisation(PNom, PNum, PStr, PDat, PRel, multiInstance, classType);
      datasetIntegrity(PNom, PNum, PStr, PDat, PRel, multiInstance, classType, handleMissingPredictors, handleMissingClass);
    }
  }
  






  protected boolean[] canTakeOptions()
  {
    boolean[] result = new boolean[2];
    
    print("options...");
    if ((m_Kernel instanceof OptionHandler)) {
      println("yes");
      if (m_Debug) {
        println("\n=== Full report ===");
        Enumeration enu = m_Kernel.listOptions();
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
    
    print("weighted instances kernel...");
    if ((m_Kernel instanceof WeightedInstancesHandler)) {
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
    
    print("multi-instance kernel...");
    if ((m_Kernel instanceof MultiInstanceCapabilitiesHandler)) {
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
    
    result[0] = (!SerializationHelper.needsUID(m_Kernel.getClass()) ? 1 : false);
    
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
  































  protected boolean[] correctBuildInitialisation(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType)
  {
    boolean[] result = new boolean[2];
    
    print("correct initialisation during buildKernel");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    int numTrain = getNumInstances();
    int numClasses = 2;int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    Instances train1 = null;
    Instances train2 = null;
    Kernel kernel = null;
    KernelEvaluation evaluation1A = null;
    KernelEvaluation evaluation1B = null;
    KernelEvaluation evaluation2 = null;
    int stage = 0;
    

    try
    {
      train1 = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      train2 = makeTestDataset(84, numTrain, nominalPredictor ? getNumNominal() + 1 : 0, numericPredictor ? getNumNumeric() + 1 : 0, stringPredictor ? getNumString() + 1 : 0, datePredictor ? getNumDate() + 1 : 0, relationalPredictor ? getNumRelational() + 1 : 0, numClasses, classType, multiInstance);
      







      if (missingLevel > 0) {
        addMissing(train1, missingLevel, predictorMissing, classMissing);
        addMissing(train2, missingLevel, predictorMissing, classMissing);
      }
      
      kernel = Kernel.makeCopy(getKernel());
      evaluation1A = new KernelEvaluation();
      evaluation1B = new KernelEvaluation();
      evaluation2 = new KernelEvaluation();
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      stage = 0;
      evaluation1A.evaluate(kernel, train1);
      
      stage = 1;
      evaluation2.evaluate(kernel, train2);
      
      stage = 2;
      evaluation1B.evaluate(kernel, train1);
      
      stage = 3;
      if (!evaluation1A.equals(evaluation1B)) {
        if (m_Debug) {
          println("\n=== Full report ===\n" + evaluation1A.toSummaryString("\nFirst buildKernel()") + "\n\n");
          

          println(evaluation1B.toSummaryString("\nSecond buildKernel()") + "\n\n");
        }
        

        throw new Exception("Results differ between buildKernel calls");
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
        print("Problem during building");
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
    int numTrain = getNumInstances();int numClasses = 2;
    
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType, missingLevel, predictorMissing, classMissing, numTrain, numClasses, accepts);
  }
  
































  protected boolean[] instanceWeights(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType)
  {
    print("kernel uses instance weights");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    int numTrain = 2 * getNumInstances();
    int numClasses = 2;int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    boolean[] result = new boolean[2];
    Instances train = null;
    Kernel[] kernels = null;
    KernelEvaluation evaluationB = null;
    KernelEvaluation evaluationI = null;
    boolean evalFail = false;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() + 1 : 0, numericPredictor ? getNumNumeric() + 1 : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      if (missingLevel > 0)
        addMissing(train, missingLevel, predictorMissing, classMissing);
      kernels = Kernel.makeCopies(getKernel(), 2);
      evaluationB = new KernelEvaluation();
      evaluationI = new KernelEvaluation();
      evaluationB.evaluate(kernels[0], train);
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
      evaluationI.evaluate(kernels[1], train);
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
      
      if (!m_Debug) break label598; }
    println("\n=== Full Report ===");
    
    if (evalFail) {
      println("Results don't differ between non-weighted and weighted instance models.");
      
      println("Here are the results:\n");
      println(evaluationB.toSummaryString("\nboth methods\n"));
    } else {
      print("Problem during building");
      println(": " + ex.getMessage() + "\n");
    }
    println("Here is the dataset:\n");
    println("=== Train Dataset ===\n" + train.toString() + "\n");
    
    println("=== Train Weights ===\n");
    for (int i = 0; i < train.numInstances(); i++) {
      println(" " + (i + 1) + "    " + train.instance(i).weight());
    }
    

    label598:
    
    return result;
  }
  




























  protected boolean[] datasetIntegrity(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int classType, boolean predictorMissing, boolean classMissing)
  {
    print("kernel doesn't alter original datasets");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, classType);
    
    print("...");
    int numTrain = getNumInstances();
    int numClasses = 2;int missingLevel = 20;
    
    boolean[] result = new boolean[2];
    Instances train = null;
    Kernel kernel = null;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, multiInstance);
      







      if (missingLevel > 0)
        addMissing(train, missingLevel, predictorMissing, classMissing);
      kernel = Kernel.makeCopies(getKernel(), 1)[0];
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      Instances trainCopy = new Instances(train);
      kernel.buildKernel(trainCopy);
      compareDatasets(train, trainCopy);
      
      println("yes");
      result[0] = true;
    } catch (Exception ex) {
      println("no");
      result[0] = false;
      
      if (m_Debug) {
        println("\n=== Full Report ===");
        print("Problem during building");
        println(": " + ex.getMessage() + "\n");
        println("Here is the dataset:\n");
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
    Kernel kernel = null;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, numClasses, classType, classIndex, multiInstance);
      








      if (missingLevel > 0)
        addMissing(train, missingLevel, predictorMissing, classMissing);
      kernel = Kernel.makeCopies(getKernel(), 1)[0];
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      kernel.buildKernel(train);
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
      if (msg.indexOf("not in classpath") > -1) {
        m_ClasspathProblems = true;
      }
      for (int i = 0; i < accepts.size(); i++) {
        if (msg.indexOf((String)accepts.elementAt(i)) >= 0) {
          acceptable = true;
        }
      }
      
      println("no" + (acceptable ? " (OK error message)" : ""));
      result[1] = acceptable;
      
      if (m_Debug) {
        println("\n=== Full Report ===");
        print("Problem during building");
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
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
  




  public static void main(String[] args)
  {
    runCheck(new CheckKernel(), args);
  }
}
