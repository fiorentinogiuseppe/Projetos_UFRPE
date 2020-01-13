package weka.clusterers;

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

































































































































































public class CheckClusterer
  extends CheckScheme
{
  protected Clusterer m_Clusterer = new SimpleKMeans();
  




  public CheckClusterer()
  {
    setNumInstances(40);
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tFull name of the clusterer analyzed.\n\teg: weka.clusterers.SimpleKMeans\n\t(default weka.clusterers.SimpleKMeans)", "W", 1, "-W"));
    




    if ((m_Clusterer != null) && ((m_Clusterer instanceof OptionHandler)))
    {
      result.addElement(new Option("", "", 0, "\nOptions specific to clusterer " + m_Clusterer.getClass().getName() + ":"));
      


      Enumeration enu = ((OptionHandler)m_Clusterer).listOptions();
      while (enu.hasMoreElements()) {
        result.addElement(enu.nextElement());
      }
    }
    return result.elements();
  }
  








































































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('N', options);
    
    super.setOptions(options);
    
    if (tmpStr.length() != 0) {
      setNumInstances(Integer.parseInt(tmpStr));
    } else {
      setNumInstances(40);
    }
    tmpStr = Utils.getOption('W', options);
    if (tmpStr.length() == 0)
      tmpStr = SimpleKMeans.class.getName();
    setClusterer((Clusterer)forName("weka.clusterers", Clusterer.class, tmpStr, Utils.partitionOptions(options)));
  }
  













  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    if (getClusterer() != null) {
      result.add("-W");
      result.add(getClusterer().getClass().getName());
    }
    
    if ((m_Clusterer != null) && ((m_Clusterer instanceof OptionHandler))) {
      options = ((OptionHandler)m_Clusterer).getOptions();
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
    if (getClusterer() == null) {
      println("\n=== No clusterer set ===");
      return;
    }
    println("\n=== Check on Clusterer: " + getClusterer().getClass().getName() + " ===\n");
    



    println("--> Checking for interfaces");
    canTakeOptions();
    boolean updateable = updateableClusterer()[0];
    boolean weightedInstancesHandler = weightedInstancesHandler()[0];
    boolean multiInstanceHandler = multiInstanceHandler()[0];
    println("--> Clusterer tests");
    declaresSerialVersionUID();
    runTests(weightedInstancesHandler, multiInstanceHandler, updateable);
  }
  




  public void setClusterer(Clusterer newClusterer)
  {
    m_Clusterer = newClusterer;
  }
  




  public Clusterer getClusterer()
  {
    return m_Clusterer;
  }
  







  protected void runTests(boolean weighted, boolean multiInstance, boolean updateable)
  {
    boolean PNom = canPredict(true, false, false, false, false, multiInstance)[0];
    boolean PNum = canPredict(false, true, false, false, false, multiInstance)[0];
    boolean PStr = canPredict(false, false, true, false, false, multiInstance)[0];
    boolean PDat = canPredict(false, false, false, true, false, multiInstance)[0];
    boolean PRel;
    boolean PRel; if (!multiInstance) {
      PRel = canPredict(false, false, false, false, true, multiInstance)[0];
    } else {
      PRel = false;
    }
    if ((PNom) || (PNum) || (PStr) || (PDat) || (PRel)) {
      if (weighted) {
        instanceWeights(PNom, PNum, PStr, PDat, PRel, multiInstance);
      }
      canHandleZeroTraining(PNom, PNum, PStr, PDat, PRel, multiInstance);
      boolean handleMissingPredictors = canHandleMissing(PNom, PNum, PStr, PDat, PRel, multiInstance, true, 20)[0];
      
      if (handleMissingPredictors) {
        canHandleMissing(PNom, PNum, PStr, PDat, PRel, multiInstance, true, 100);
      }
      correctBuildInitialisation(PNom, PNum, PStr, PDat, PRel, multiInstance);
      datasetIntegrity(PNom, PNum, PStr, PDat, PRel, multiInstance, handleMissingPredictors);
      if (updateable) {
        updatingEquality(PNom, PNum, PStr, PDat, PRel, multiInstance);
      }
    }
  }
  




  protected boolean[] canTakeOptions()
  {
    boolean[] result = new boolean[2];
    
    print("options...");
    if ((m_Clusterer instanceof OptionHandler)) {
      println("yes");
      if (m_Debug) {
        println("\n=== Full report ===");
        Enumeration enu = ((OptionHandler)m_Clusterer).listOptions();
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
  





  protected boolean[] updateableClusterer()
  {
    boolean[] result = new boolean[2];
    
    print("updateable clusterer...");
    if ((m_Clusterer instanceof UpdateableClusterer)) {
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
    
    print("weighted instances clusterer...");
    if ((m_Clusterer instanceof WeightedInstancesHandler)) {
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
    
    print("multi-instance clusterer...");
    if ((m_Clusterer instanceof MultiInstanceCapabilitiesHandler)) {
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
    
    result[0] = (!SerializationHelper.needsUID(m_Clusterer.getClass()) ? 1 : false);
    
    if (result[0] != 0) {
      println("yes");
    } else {
      println("no");
    }
    return result;
  }
  



















  protected boolean[] canPredict(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance)
  {
    print("basic predict");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance);
    
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
    int numTrain = getNumInstances();int missingLevel = 0;
    boolean predictorMissing = false;
    
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, missingLevel, predictorMissing, numTrain, accepts);
  }
  























  protected boolean[] canHandleZeroTraining(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance)
  {
    print("handle zero training instances");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance);
    
    print("...");
    FastVector accepts = new FastVector();
    accepts.addElement("train");
    accepts.addElement("value");
    int numTrain = 0;int missingLevel = 0;
    boolean predictorMissing = false;
    
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, missingLevel, predictorMissing, numTrain, accepts);
  }
  




























  protected boolean[] correctBuildInitialisation(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance)
  {
    boolean[] result = new boolean[2];
    
    print("correct initialisation during buildClusterer");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance);
    
    print("...");
    int numTrain = getNumInstances();int missingLevel = 0;
    boolean predictorMissing = false;
    
    Instances train1 = null;
    Instances train2 = null;
    Clusterer clusterer = null;
    ClusterEvaluation evaluation1A = null;
    ClusterEvaluation evaluation1B = null;
    ClusterEvaluation evaluation2 = null;
    boolean built = false;
    int stage = 0;
    
    try
    {
      train1 = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, multiInstance);
      





      train2 = makeTestDataset(84, numTrain, nominalPredictor ? getNumNominal() + 1 : 0, numericPredictor ? getNumNumeric() + 1 : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, multiInstance);
      





      if ((nominalPredictor) && (!multiInstance)) {
        train1.deleteAttributeAt(0);
        train2.deleteAttributeAt(0);
      }
      if (missingLevel > 0) {
        addMissing(train1, missingLevel, predictorMissing);
        addMissing(train2, missingLevel, predictorMissing);
      }
      
      clusterer = AbstractClusterer.makeCopies(getClusterer(), 1)[0];
      evaluation1A = new ClusterEvaluation();
      evaluation1B = new ClusterEvaluation();
      evaluation2 = new ClusterEvaluation();
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      stage = 0;
      clusterer.buildClusterer(train1);
      built = true;
      evaluation1A.setClusterer(clusterer);
      evaluation1A.evaluateClusterer(train1);
      
      stage = 1;
      built = false;
      clusterer.buildClusterer(train2);
      built = true;
      evaluation2.setClusterer(clusterer);
      evaluation2.evaluateClusterer(train2);
      
      stage = 2;
      built = false;
      clusterer.buildClusterer(train1);
      built = true;
      evaluation1B.setClusterer(clusterer);
      evaluation1B.evaluateClusterer(train1);
      
      stage = 3;
      if (!evaluation1A.equals(evaluation1B)) {
        if (m_Debug) {
          println("\n=== Full report ===\n");
          println("First buildClusterer()");
          println(evaluation1A.clusterResultsToString() + "\n\n");
          println("Second buildClusterer()");
          println(evaluation1B.clusterResultsToString() + "\n\n");
        }
        throw new Exception("Results differ between buildClusterer calls");
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
        
        println("=== Train2 Dataset ===\n" + train2.toString() + "\n");
      }
    }
    

    return result;
  }
  

























  protected boolean[] canHandleMissing(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, boolean predictorMissing, int missingLevel)
  {
    if (missingLevel == 100)
      print("100% ");
    print("missing");
    if (predictorMissing) {
      print(" predictor");
    }
    print(" values");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance);
    
    print("...");
    FastVector accepts = new FastVector();
    accepts.addElement("missing");
    accepts.addElement("value");
    accepts.addElement("train");
    int numTrain = getNumInstances();
    
    return runBasicTest(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance, missingLevel, predictorMissing, numTrain, accepts);
  }
  





























  protected boolean[] instanceWeights(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance)
  {
    print("clusterer uses instance weights");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance);
    
    print("...");
    int numTrain = 2 * getNumInstances();int missingLevel = 0;
    boolean predictorMissing = false;
    
    boolean[] result = new boolean[2];
    Instances train = null;
    Clusterer[] clusterers = null;
    ClusterEvaluation evaluationB = null;
    ClusterEvaluation evaluationI = null;
    boolean built = false;
    boolean evalFail = false;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() + 1 : 0, numericPredictor ? getNumNumeric() + 1 : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, multiInstance);
      





      if ((nominalPredictor) && (!multiInstance))
        train.deleteAttributeAt(0);
      if (missingLevel > 0)
        addMissing(train, missingLevel, predictorMissing);
      clusterers = AbstractClusterer.makeCopies(getClusterer(), 2);
      evaluationB = new ClusterEvaluation();
      evaluationI = new ClusterEvaluation();
      clusterers[0].buildClusterer(train);
      evaluationB.setClusterer(clusterers[0]);
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
      clusterers[1].buildClusterer(train);
      built = true;
      evaluationI.setClusterer(clusterers[1]);
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
      
      if (!m_Debug) break label645; }
    println("\n=== Full Report ===");
    
    if (evalFail) {
      println("Results don't differ between non-weighted and weighted instance models.");
      
      println("Here are the results:\n");
      println("\nboth methods\n");
      println(evaluationB.clusterResultsToString());
    } else {
      print("Problem during");
      if (built) {
        print(" testing");
      } else {
        print(" training");
      }
      println(": " + ex.getMessage() + "\n");
    }
    println("Here is the dataset:\n");
    println("=== Train Dataset ===\n" + train.toString() + "\n");
    
    println("=== Train Weights ===\n");
    for (int i = 0; i < train.numInstances(); i++) {
      println(" " + (i + 1) + "    " + train.instance(i).weight());
    }
    

    label645:
    
    return result;
  }
  
























  protected boolean[] datasetIntegrity(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, boolean predictorMissing)
  {
    print("clusterer doesn't alter original datasets");
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance);
    
    print("...");
    int numTrain = getNumInstances();int missingLevel = 20;
    
    boolean[] result = new boolean[2];
    Instances train = null;
    Clusterer clusterer = null;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, multiInstance);
      





      if ((nominalPredictor) && (!multiInstance))
        train.deleteAttributeAt(0);
      if (missingLevel > 0)
        addMissing(train, missingLevel, predictorMissing);
      clusterer = AbstractClusterer.makeCopies(getClusterer(), 1)[0];
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      Instances trainCopy = new Instances(train);
      clusterer.buildClusterer(trainCopy);
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
        println("Here is the dataset:\n");
        println("=== Train Dataset ===\n" + train.toString() + "\n");
      }
    }
    

    return result;
  }
  





















  protected boolean[] updatingEquality(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance)
  {
    print("incremental training produces the same results as batch training");
    
    printAttributeSummary(nominalPredictor, numericPredictor, stringPredictor, datePredictor, relationalPredictor, multiInstance);
    
    print("...");
    int numTrain = getNumInstances();int missingLevel = 0;
    boolean predictorMissing = false;boolean classMissing = false;
    
    boolean[] result = new boolean[2];
    Instances train = null;
    Clusterer[] clusterers = null;
    ClusterEvaluation evaluationB = null;
    ClusterEvaluation evaluationI = null;
    boolean built = false;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, multiInstance);
      





      if (missingLevel > 0)
        addMissing(train, missingLevel, predictorMissing, classMissing);
      clusterers = AbstractClusterer.makeCopies(getClusterer(), 2);
      evaluationB = new ClusterEvaluation();
      evaluationI = new ClusterEvaluation();
      clusterers[0].buildClusterer(train);
      evaluationB.setClusterer(clusterers[0]);
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      clusterers[1].buildClusterer(new Instances(train, 0));
      for (int i = 0; i < train.numInstances(); i++) {
        ((UpdateableClusterer)clusterers[1]).updateClusterer(train.instance(i));
      }
      
      built = true;
      evaluationI.setClusterer(clusterers[1]);
      if (!evaluationB.equals(evaluationI)) {
        println("no");
        result[0] = false;
        
        if (m_Debug) {
          println("\n=== Full Report ===");
          println("Results differ between batch and incrementally built models.\nDepending on the classifier, this may be OK");
          

          println("Here are the results:\n");
          println("\nbatch built results\n" + evaluationB.clusterResultsToString());
          println("\nincrementally built results\n" + evaluationI.clusterResultsToString());
          println("Here are the datasets:\n");
          println("=== Train Dataset ===\n" + train.toString() + "\n");
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
  


























  protected boolean[] runBasicTest(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance, int missingLevel, boolean predictorMissing, int numTrain, FastVector accepts)
  {
    boolean[] result = new boolean[2];
    Instances train = null;
    Clusterer clusterer = null;
    try {
      train = makeTestDataset(42, numTrain, nominalPredictor ? getNumNominal() : 0, numericPredictor ? getNumNumeric() : 0, stringPredictor ? getNumString() : 0, datePredictor ? getNumDate() : 0, relationalPredictor ? getNumRelational() : 0, multiInstance);
      





      if ((nominalPredictor) && (!multiInstance))
        train.deleteAttributeAt(0);
      if (missingLevel > 0)
        addMissing(train, missingLevel, predictorMissing);
      clusterer = AbstractClusterer.makeCopies(getClusterer(), 1)[0];
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      clusterer.buildClusterer(train);
      println("yes");
      result[0] = true;
    }
    catch (Exception ex) {
      boolean acceptable = false;
      String msg = ex.getMessage().toLowerCase();
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
  









  protected void addMissing(Instances data, int level, boolean predictorMissing)
  {
    Random random = new Random(1L);
    for (int i = 0; i < data.numInstances(); i++) {
      Instance current = data.instance(i);
      for (int j = 0; j < data.numAttributes(); j++) {
        if ((predictorMissing) && 
          (Math.abs(random.nextInt()) % 100 < level)) {
          current.setMissing(j);
        }
      }
    }
  }
  




















  protected Instances makeTestDataset(int seed, int numInstances, int numNominal, int numNumeric, int numString, int numDate, int numRelational, boolean multiInstance)
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
    dataset.setClassIndex(-2);
    dataset.setMultiInstance(multiInstance);
    
    return dataset.generate();
  }
  















  protected void printAttributeSummary(boolean nominalPredictor, boolean numericPredictor, boolean stringPredictor, boolean datePredictor, boolean relationalPredictor, boolean multiInstance)
  {
    String str = "";
    
    if (numericPredictor) {
      str = str + "numeric";
    }
    if (nominalPredictor) {
      if (str.length() > 0)
        str = str + " & ";
      str = str + "nominal";
    }
    
    if (stringPredictor) {
      if (str.length() > 0)
        str = str + " & ";
      str = str + "string";
    }
    
    if (datePredictor) {
      if (str.length() > 0)
        str = str + " & ";
      str = str + "date";
    }
    
    if (relationalPredictor) {
      if (str.length() > 0)
        str = str + " & ";
      str = str + "relational";
    }
    
    str = " (" + str + " predictors)";
    
    print(str);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.11 $");
  }
  




  public static void main(String[] args)
  {
    runCheck(new CheckClusterer(), args);
  }
}
