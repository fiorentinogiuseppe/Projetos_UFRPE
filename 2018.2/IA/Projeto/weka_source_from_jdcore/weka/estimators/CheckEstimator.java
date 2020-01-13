package weka.estimators;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TestInstances;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;



































































































































public class CheckEstimator
  implements OptionHandler, RevisionHandler
{
  protected Estimator m_Estimator;
  protected String[] m_EstimatorOptions;
  protected String m_AnalysisResults;
  protected boolean m_Debug;
  protected boolean m_Silent;
  protected int m_NumInstances;
  protected PostProcessor m_PostProcessor;
  protected boolean m_ClasspathProblems;
  
  public class PostProcessor
    implements RevisionHandler
  {
    public PostProcessor() {}
    
    protected Instances process(Instances data)
    {
      return data;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.5 $");
    }
  }
  
  public CheckEstimator() {
    m_Estimator = new NormalEstimator(1.0E-6D);
    







    m_Debug = false;
    

    m_Silent = false;
    

    m_NumInstances = 100;
    

    m_PostProcessor = null;
    

    m_ClasspathProblems = false;
  }
  



  public static class AttrTypes
    implements RevisionHandler
  {
    boolean nominal = false;
    boolean numeric = false;
    boolean string = false;
    boolean date = false;
    boolean relational = false;
    
    AttrTypes() {}
    
    AttrTypes(AttrTypes newTypes)
    {
      nominal = nominal;
      numeric = numeric;
      string = string;
      date = date;
      relational = relational;
    }
    
    AttrTypes(int type) {
      if (type == 1) nominal = true;
      if (type == 0) numeric = true;
      if (type == 2) string = true;
      if (type == 3) date = true;
      if (type == 4) relational = true;
    }
    
    int getSetType() throws Exception {
      int sum = 0;
      int type = -1;
      if (nominal) { sum++;type = 1; }
      if (numeric) { sum++;type = 0; }
      if (string) { sum++;type = 2; }
      if (date) { sum++;type = 3; }
      if (relational) { sum++;type = 4; }
      if (sum > 1)
        throw new Exception("Expected to have only one type set used wrongly.");
      if (type < 0)
        throw new Exception("No type set.");
      return type;
    }
    
    boolean oneIsSet() {
      return (nominal) || (numeric) || (string) || (date) || (relational);
    }
    
    public Vector getVectorOfAttrTypes() {
      Vector attrs = new Vector();
      if (nominal) attrs.add(new Integer(1));
      if (numeric) attrs.add(new Integer(0));
      if (string) attrs.add(new Integer(2));
      if (date) attrs.add(new Integer(3));
      if (relational) attrs.add(new Integer(4));
      return attrs;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.5 $");
    }
  }
  




  public static class EstTypes
    implements RevisionHandler
  {
    boolean incremental = false;
    boolean weighted = false;
    boolean supervised = false;
    



    public EstTypes() {}
    



    public EstTypes(boolean i, boolean w, boolean s)
    {
      incremental = i;
      weighted = w;
      supervised = s;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.5 $");
    }
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tTurn on debugging output.", "D", 0, "-D"));
    


    newVector.addElement(new Option("\tSilent mode - prints nothing to stdout.", "S", 0, "-S"));
    


    newVector.addElement(new Option("\tThe number of instances in the datasets (default 100).", "N", 1, "-N <num>"));
    


    newVector.addElement(new Option("\tFull name of the estimator analysed.\n\teg: weka.estimators.NormalEstimator", "W", 1, "-W"));
    



    if ((m_Estimator != null) && ((m_Estimator instanceof OptionHandler)))
    {
      newVector.addElement(new Option("", "", 0, "\nOptions specific to estimator " + m_Estimator.getClass().getName() + ":"));
      


      Enumeration enu = m_Estimator.listOptions();
      while (enu.hasMoreElements()) {
        newVector.addElement(enu.nextElement());
      }
    }
    return newVector.elements();
  }
  
































  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
    
    setSilent(Utils.getFlag('S', options));
    
    String tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setNumInstances(Integer.parseInt(tmpStr));
    } else {
      setNumInstances(100);
    }
    tmpStr = Utils.getOption('W', options);
    if (tmpStr.length() == 0)
      throw new Exception("A estimator must be specified with the -W option.");
    setEstimator(Estimator.forName(tmpStr, Utils.partitionOptions(options)));
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getDebug()) {
      result.add("-D");
    }
    if (getSilent()) {
      result.add("-S");
    }
    result.add("-N");
    result.add("" + getNumInstances());
    
    if (getEstimator() != null) {
      result.add("-W");
      result.add(getEstimator().getClass().getName()); }
    String[] options;
    String[] options;
    if ((m_Estimator != null) && ((m_Estimator instanceof OptionHandler))) {
      options = m_Estimator.getOptions();
    } else {
      options = new String[0];
    }
    if (options.length > 0) {
      result.add("--");
      for (int i = 0; i < options.length; i++) {
        result.add(options[i]);
      }
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public void setPostProcessor(PostProcessor value)
  {
    m_PostProcessor = value;
  }
  




  public PostProcessor getPostProcessor()
  {
    return m_PostProcessor;
  }
  




  public boolean hasClasspathProblems()
  {
    return m_ClasspathProblems;
  }
  



  public void doTests()
  {
    if (getEstimator() == null) {
      println("\n=== No estimator set ===");
      return;
    }
    println("\n=== Check on Estimator: " + getEstimator().getClass().getName() + " ===\n");
    


    m_ClasspathProblems = false;
    

    canTakeOptions();
    

    EstTypes estTypes = new EstTypes();
    incremental = incrementalEstimator()[0];
    weighted = weightedInstancesHandler()[0];
    supervised = supervisedEstimator()[0];
    


    int classType = 1;
    AttrTypes attrTypes = testsPerClassType(classType, estTypes);
    


    canSplitUpClass(attrTypes, classType);
  }
  





  public void setDebug(boolean debug)
  {
    m_Debug = debug;
    

    if (getDebug()) {
      setSilent(false);
    }
  }
  



  public boolean getDebug()
  {
    return m_Debug;
  }
  




  public void setSilent(boolean value)
  {
    m_Silent = value;
  }
  




  public boolean getSilent()
  {
    return m_Silent;
  }
  





  public void setNumInstances(int value)
  {
    m_NumInstances = value;
  }
  




  public int getNumInstances()
  {
    return m_NumInstances;
  }
  




  public void setEstimator(Estimator newEstimator)
  {
    m_Estimator = newEstimator;
  }
  




  public Estimator getEstimator()
  {
    return m_Estimator;
  }
  




  protected void print(Object msg)
  {
    if (!getSilent()) {
      System.out.print(msg);
    }
  }
  



  protected void println(Object msg)
  {
    print(msg + "\n");
  }
  


  protected void println()
  {
    print("\n");
  }
  











  protected AttrTypes testsPerClassType(int classType, EstTypes estTypes)
  {
    AttrTypes attrTypes = new AttrTypes();
    AttrTypes at = new AttrTypes(1);
    nominal = canEstimate(at, supervised, classType)[0];
    at = new AttrTypes(0);
    numeric = canEstimate(at, supervised, classType)[0];
    string = false;
    date = false;
    relational = false;
    







    if (attrTypes.oneIsSet()) {
      Vector attributesSet = attrTypes.getVectorOfAttrTypes();
      

      for (int i = 0; i < attributesSet.size(); i++) {
        AttrTypes workAttrTypes = new AttrTypes(((Integer)attributesSet.elementAt(i)).intValue());
        

        if (weighted) {
          instanceWeights(workAttrTypes, classType);
        }
        if (classType == 1) {
          int numClasses = 4;
          canHandleNClasses(workAttrTypes, numClasses);
        }
        



        int numAtt = 4;
        
        canHandleClassAsNthAttribute(workAttrTypes, numAtt, 0, classType, 1);
        



        canHandleZeroTraining(workAttrTypes, classType);
        boolean handleMissingAttributes = canHandleMissing(workAttrTypes, classType, true, false, 20)[0];
        
        if (handleMissingAttributes) {
          canHandleMissing(workAttrTypes, classType, true, false, 100);
        }
        boolean handleMissingClass = canHandleMissing(workAttrTypes, classType, false, true, 20)[0];
        

        if (handleMissingClass) {
          canHandleMissing(workAttrTypes, classType, false, true, 100);
        }
        correctBuildInitialisation(workAttrTypes, classType);
        datasetIntegrity(workAttrTypes, classType, handleMissingAttributes, handleMissingClass);
        

        if (incremental)
          incrementingEquality(workAttrTypes, classType);
      }
    }
    return attrTypes;
  }
  





  protected boolean[] canTakeOptions()
  {
    boolean[] result = new boolean[2];
    
    print("options...");
    if ((m_Estimator instanceof OptionHandler)) {
      println("yes");
      if (m_Debug) {
        println("\n=== Full report ===");
        Enumeration enu = m_Estimator.listOptions();
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
  





  protected boolean[] incrementalEstimator()
  {
    boolean[] result = new boolean[2];
    
    print("incremental estimator...");
    if ((m_Estimator instanceof IncrementalEstimator)) {
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
    
    print("weighted instances estimator...");
    if ((m_Estimator instanceof WeightedInstancesHandler)) {
      println("yes");
      result[0] = true;
    }
    else {
      println("no");
      result[0] = false;
    }
    
    return result;
  }
  




  protected boolean[] supervisedEstimator()
  {
    boolean[] result = new boolean[2];
    result[0] = false;
    return result;
  }
  











  protected boolean[] canEstimate(AttrTypes attrTypes, boolean supervised, int classType)
  {
    print("basic estimation");
    printAttributeSummary(attrTypes, classType);
    print("...");
    FastVector accepts = new FastVector();
    accepts.addElement("nominal");
    accepts.addElement("numeric");
    accepts.addElement("string");
    accepts.addElement("date");
    accepts.addElement("relational");
    accepts.addElement("not in classpath");
    int numTrain = getNumInstances();int numTest = getNumInstances();
    int numClasses = 2;int missingLevel = 0;
    boolean attributeMissing = false;boolean classMissing = false;
    int numAtts = 1;int attrIndex = 0;
    
    return runBasicTest(attrTypes, numAtts, attrIndex, classType, missingLevel, attributeMissing, classMissing, numTrain, numTest, numClasses, accepts);
  }
  











  protected void canSplitUpClass(AttrTypes attrTypes, int classType)
  {
    if (nominal)
      canSplitUpClass(1, classType);
    if (numeric) {
      canSplitUpClass(0, classType);
    }
  }
  








  protected boolean[] canSplitUpClass(int attrType, int classType)
  {
    boolean[] result = new boolean[2];
    
    FastVector accepts = new FastVector();
    accepts.addElement("not in classpath");
    

    print("split per class type ");
    printAttributeSummary(attrType, 1);
    print("...");
    
    int numTrain = getNumInstances();int numTest = getNumInstances();
    int numClasses = 2;
    boolean attributeMissing = false;boolean classMissing = false;
    int numAtts = 3;int attrIndex = 0;int classIndex = 1;
    Instances train = null;
    
    Estimator estimator = null;
    boolean built = false;
    Vector test;
    try {
      AttrTypes at = new AttrTypes(attrType);
      train = makeTestDataset(42, numTrain, numAtts, at, numClasses, classType, classIndex);
      


      test = makeTestValueList(24, numTest, train, attrIndex, attrType);
      

      estimator = Estimator.makeCopies(getEstimator(), 1)[0];
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      estimator.addValues(train, attrIndex, classType, classIndex);
      built = true;
      
      testWithTestValues(estimator, test);
      
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
  










  protected boolean[] canHandleNClasses(AttrTypes attrTypes, int numClasses)
  {
    print("more than two class problems");
    printAttributeSummary(attrTypes, 1);
    print("...");
    
    FastVector accepts = new FastVector();
    accepts.addElement("number");
    accepts.addElement("class");
    
    int numTrain = getNumInstances();int numTest = getNumInstances();
    int missingLevel = 0;
    boolean attributeMissing = false;boolean classMissing = false;
    int numAttr = 1;int attrIndex = 0;
    
    return runBasicTest(attrTypes, numAttr, attrIndex, 1, missingLevel, attributeMissing, classMissing, numTrain, numTest, numClasses, accepts);
  }
  





















  protected boolean[] canHandleClassAsNthAttribute(AttrTypes attrTypes, int numAtts, int attrIndex, int classType, int classIndex)
  {
    if (classIndex == -1) {
      print("class attribute as last attribute");
    } else
      print("class attribute as " + (classIndex + 1) + ". attribute");
    printAttributeSummary(attrTypes, classType);
    print("...");
    FastVector accepts = new FastVector();
    int numTrain = getNumInstances();int numTest = getNumInstances();int numClasses = 2;
    int missingLevel = 0;
    boolean attributeMissing = false;boolean classMissing = false;
    
    return runBasicTest(attrTypes, numAtts, attrIndex, classType, classIndex, missingLevel, attributeMissing, classMissing, numTrain, numTest, numClasses, accepts);
  }
  













  protected boolean[] canHandleZeroTraining(AttrTypes attrTypes, int classType)
  {
    print("handle zero training instances");
    printAttributeSummary(attrTypes, classType);
    
    print("...");
    FastVector accepts = new FastVector();
    accepts.addElement("train");
    accepts.addElement("value");
    int numTrain = 0;int numTest = getNumInstances();int numClasses = 2;
    int missingLevel = 0;
    boolean attributeMissing = false;boolean classMissing = false;
    int numAtts = 1;
    int attrIndex = 0;
    return runBasicTest(attrTypes, numAtts, attrIndex, classType, missingLevel, attributeMissing, classMissing, numTrain, numTest, numClasses, accepts);
  }
  





















  protected boolean[] correctBuildInitialisation(AttrTypes attrTypes, int classType)
  {
    boolean[] result = new boolean[2];
    
    print("correct initialisation during buildEstimator");
    printAttributeSummary(attrTypes, classType);
    
    print("...");
    int numTrain = getNumInstances();int numTest = getNumInstances();
    int numClasses = 2;int missingLevel = 0;
    boolean attributeMissing = false;boolean classMissing = false;
    
    Instances train1 = null;
    Instances test1 = null;
    Instances train2 = null;
    Instances test2 = null;
    Estimator estimator = null;
    Estimator estimator1 = null;
    
    boolean built = false;
    int stage = 0;
    int attrIndex1 = 1;
    int attrIndex2 = 2;
    


    try
    {
      train1 = makeTestDataset(42, numTrain, 2, attrTypes, numClasses, classType);
      

      train2 = makeTestDataset(84, numTrain, 3, attrTypes, numClasses, classType);
      

      if (missingLevel > 0) {
        addMissing(train1, missingLevel, attributeMissing, classMissing, attrIndex1);
        addMissing(train2, missingLevel, attributeMissing, classMissing, attrIndex2);
      }
      
      estimator = Estimator.makeCopies(getEstimator(), 1)[0];
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try
    {
      stage = 0;
      estimator.addValues(train1, attrIndex1);
      built = true;
      
      estimator1 = Estimator.makeCopies(getEstimator(), 1)[0];
      
      stage = 1;
      built = false;
      estimator.addValues(train2, attrIndex2);
      built = true;
      
      stage = 2;
      built = false;
      estimator.addValues(train1, attrIndex1);
      built = true;
      
      stage = 3;
      if (!estimator.equals(estimator1)) {
        if (m_Debug) {
          println("\n=== Full report ===\n\nFirst build estimator\n" + estimator.toString() + "\n\n");
          

          println("\nSecond build estimator\n" + estimator.toString() + "\n\n");
        }
        
        throw new Exception("Results differ between buildEstimator calls");
      }
      println("yes");
      result[0] = true;




    }
    catch (Exception ex)
    {



      String msg = ex.getMessage().toLowerCase();
      if (msg.indexOf("worse than zeror") >= 0) {
        println("warning: performs worse than ZeroR");
        result[0] = true;
        result[1] = true;
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
  


















  protected boolean[] canHandleMissing(AttrTypes attrTypes, int classType, boolean attributeMissing, boolean classMissing, int missingLevel)
  {
    if (missingLevel == 100)
      print("100% ");
    print("missing");
    if (attributeMissing) {
      print(" attribute");
      if (classMissing)
        print(" and");
    }
    if (classMissing)
      print(" class");
    print(" values");
    printAttributeSummary(attrTypes, classType);
    
    print("...");
    FastVector accepts = new FastVector();
    accepts.addElement("missing");
    accepts.addElement("value");
    accepts.addElement("train");
    int numTrain = getNumInstances();int numTest = getNumInstances();
    int numClasses = 2;
    
    int numAtts = 1;int attrIndex = 0;
    return runBasicTest(attrTypes, numAtts, attrIndex, classType, missingLevel, attributeMissing, classMissing, numTrain, numTest, numClasses, accepts);
  }
  

















  protected boolean[] incrementingEquality(AttrTypes attrTypes, int classType)
  {
    print("incremental training produces the same results as batch training");
    
    printAttributeSummary(attrTypes, classType);
    
    print("...");
    int numTrain = getNumInstances();int numTest = getNumInstances();
    int numClasses = 2;int missingLevel = 0;
    boolean attributeMissing = false;boolean classMissing = false;
    
    boolean[] result = new boolean[2];
    Instances train = null;
    Estimator[] estimators = null;
    boolean built = false;
    int attrIndex = 0;
    Vector test;
    try {
      train = makeTestDataset(42, numTrain, 1, attrTypes, numClasses, classType);
      




      test = makeTestValueList(24, numTest, train, attrIndex, attrTypes.getSetType());
      

      if (missingLevel > 0) {
        addMissing(train, missingLevel, attributeMissing, classMissing, attrIndex);
      }
      estimators = Estimator.makeCopies(getEstimator(), 2);
      estimators[0].addValues(train, attrIndex);
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      for (int i = 0; i < train.numInstances(); i++) {
        ((IncrementalEstimator)estimators[1]).addValue(train.instance(i).value(attrIndex), 1.0D);
      }
      built = true;
      if (!estimators[0].equals(estimators[1])) {
        println("no");
        result[0] = false;
        
        if (m_Debug) {
          println("\n=== Full Report ===");
          println("Results differ between batch and incrementally built models.\nDepending on the estimator, this may be OK");
          

          println("Here are the results:\n");
          println("batch built results\n" + estimators[0].toString());
          println("incrementally built results\n" + estimators[1].toString());
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
  
















  protected boolean[] instanceWeights(AttrTypes attrTypes, int classType)
  {
    print("estimator uses instance weights");
    printAttributeSummary(attrTypes, classType);
    
    print("...");
    
    int numTrain = 2 * getNumInstances();int numTest = getNumInstances();
    int numClasses = 2;int missingLevel = 0;
    boolean attributeMissing = false;boolean classMissing = false;
    
    boolean[] result = new boolean[2];
    Instances train = null;
    Vector test = null;
    Estimator[] estimators = null;
    
    Vector resultProbsO = null;
    Vector resultProbsW = null;
    boolean built = false;
    boolean evalFail = false;
    int attrIndex = 0;
    try {
      train = makeTestDataset(42, numTrain, 1, attrTypes, numClasses, classType);
      



      test = makeTestValueList(24, numTest, train, attrIndex, attrTypes.getSetType());
      

      if (missingLevel > 0) {
        addMissing(train, missingLevel, attributeMissing, classMissing, attrIndex);
      }
      
      estimators = Estimator.makeCopies(getEstimator(), 2);
      
      estimators[0].addValues(train, attrIndex);
      resultProbsO = testWithTestValues(estimators[0], test);
    }
    catch (Exception ex) {
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
      estimators[1].addValues(train, attrIndex);
      resultProbsW = testWithTestValues(estimators[1], test);
      
      built = true;
      if (resultProbsO.equals(resultProbsW))
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
          println(probsToString(resultProbsO));
        } else {
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
  


















  protected boolean[] datasetIntegrity(AttrTypes attrTypes, int classType, boolean attributeMissing, boolean classMissing)
  {
    Estimator estimator = null;
    print("estimator doesn't alter original datasets");
    printAttributeSummary(attrTypes, classType);
    print("...");
    int numTrain = getNumInstances();int numTest = getNumInstances();
    int numClasses = 2;int missingLevel = 100;
    
    boolean[] result = new boolean[2];
    Instances train = null;
    boolean built = false;
    try {
      train = makeTestDataset(42, numTrain, 1, attrTypes, numClasses, classType);
      

      int attrIndex = 0;
      
      if (missingLevel > 0) {
        addMissing(train, missingLevel, attributeMissing, classMissing, attrIndex);
      }
      estimator = Estimator.makeCopies(getEstimator(), 1)[0];
    } catch (Exception ex) {
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      Instances trainCopy = new Instances(train);
      int attrIndex = 0;
      estimator.addValues(trainCopy, attrIndex);
      compareDatasets(train, trainCopy);
      built = true;
      
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
      }
    }
    

    return result;
  }
  




























  protected boolean[] runBasicTest(AttrTypes attrTypes, int numAtts, int attrIndex, int classType, int missingLevel, boolean attributeMissing, boolean classMissing, int numTrain, int numTest, int numClasses, FastVector accepts)
  {
    return runBasicTest(attrTypes, numAtts, attrIndex, classType, -1, missingLevel, attributeMissing, classMissing, numTrain, numTest, numClasses, accepts);
  }
  








































  protected boolean[] runBasicTest(AttrTypes attrTypes, int numAtts, int attrIndex, int classType, int classIndex, int missingLevel, boolean attributeMissing, boolean classMissing, int numTrain, int numTest, int numClasses, FastVector accepts)
  {
    boolean[] result = new boolean[2];
    Instances train = null;
    Vector test = null;
    Estimator estimator = null;
    boolean built = false;
    try
    {
      train = makeTestDataset(42, numTrain, numAtts, attrTypes, numClasses, classType, classIndex);
      




      if (numTrain > 0) {
        test = makeTestValueList(24, numTest, train, attrIndex, attrTypes.getSetType());
      }
      else
      {
        double min = -10.0D;
        double max = 8.0D;
        test = makeTestValueList(24, numTest, min, max, attrTypes.getSetType());
      }
      

      if (missingLevel > 0) {
        addMissing(train, missingLevel, attributeMissing, classMissing, attrIndex);
      }
      estimator = Estimator.makeCopies(getEstimator(), 1)[0];
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new Error("Error setting up for tests: " + ex.getMessage());
    }
    try {
      estimator.addValues(train, attrIndex);
      built = true;
      
      testWithTestValues(estimator, test);
      
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
  






  protected void compareDatasets(Instances data1, Instances data2)
    throws Exception
  {
    if (!data2.equalHeaders(data1)) {
      throw new Exception("header has been modified");
    }
    if (data2.numInstances() != data1.numInstances()) {
      throw new Exception("number of instances has changed");
    }
    for (int i = 0; i < data2.numInstances(); i++) {
      Instance orig = data1.instance(i);
      Instance copy = data2.instance(i);
      for (int j = 0; j < orig.numAttributes(); j++) {
        if (orig.isMissing(j)) {
          if (!copy.isMissing(j)) {
            throw new Exception("instances have changed");
          }
        } else if (orig.value(j) != copy.value(j)) {
          throw new Exception("instances have changed");
        }
        if (orig.weight() != copy.weight()) {
          throw new Exception("instance weights have changed");
        }
      }
    }
  }
  













  protected void addMissing(Instances data, int level, boolean attributeMissing, boolean classMissing, int attrIndex)
  {
    int classIndex = data.classIndex();
    Random random = new Random(1L);
    for (int i = 0; i < data.numInstances(); i++) {
      Instance current = data.instance(i);
      
      for (int j = 0; j < data.numAttributes(); j++) {
        if (((j == classIndex) && (classMissing)) || ((j == attrIndex) && (attributeMissing)))
        {
          if (Math.abs(random.nextInt()) % 100 < level) {
            current.setMissing(j);
          }
        }
      }
    }
  }
  


















  protected Instances makeTestDataset(int seed, int numInstances, int numAttr, AttrTypes attrTypes, int numClasses, int classType)
    throws Exception
  {
    return makeTestDataset(seed, numInstances, numAttr, attrTypes, numClasses, classType, -1);
  }
  



























  protected Instances makeTestDataset(int seed, int numInstances, int numAttr, AttrTypes attrTypes, int numClasses, int classType, int classIndex)
    throws Exception
  {
    TestInstances dataset = new TestInstances();
    
    dataset.setSeed(seed);
    dataset.setNumInstances(numInstances);
    dataset.setNumNominal(nominal ? numAttr : 0);
    dataset.setNumNumeric(numeric ? numAttr : 0);
    dataset.setNumString(string ? numAttr : 0);
    dataset.setNumDate(date ? numAttr : 0);
    dataset.setNumRelational(relational ? numAttr : 0);
    dataset.setNumClasses(numClasses);
    dataset.setClassType(classType);
    dataset.setClassIndex(classIndex);
    
    return process(dataset.generate());
  }
  














  protected Vector makeTestValueList(int seed, int numValues, Instances data, int attrIndex, int attrType)
    throws Exception
  {
    double[] minMax = getMinimumMaximum(data, attrIndex);
    double minValue = minMax[0];
    double maxValue = minMax[1];
    

    double range = maxValue - minValue;
    Vector values = new Vector(numValues);
    Random random = new Random(seed);
    
    if (attrType == 1) {
      for (int i = 0; i < numValues; i++) {
        Double v = new Double(Math.abs(random.nextInt()) % (int)range + (int)minValue);
        values.add(v);
      }
    }
    if (attrType == 0) {
      for (int i = 0; i < numValues; i++) {
        Double v = new Double(random.nextDouble() * range + minValue);
        values.add(v);
      }
    }
    return values;
  }
  















  protected Vector makeTestValueList(int seed, int numValues, double minValue, double maxValue, int attrType)
    throws Exception
  {
    double range = maxValue - minValue;
    Vector values = new Vector(numValues);
    Random random = new Random(seed);
    
    if (attrType == 1) {
      for (int i = 0; i < numValues; i++) {
        Double v = new Double(Math.abs(random.nextInt()) % (int)range + (int)minValue);
        values.add(v);
      }
    }
    if (attrType == 0) {
      for (int i = 0; i < numValues; i++) {
        Double v = new Double(random.nextDouble() * range + minValue);
        values.add(v);
      }
    }
    return values;
  }
  







  protected Vector testWithTestValues(Estimator est, Vector test)
  {
    Vector results = new Vector();
    for (int i = 0; i < test.size(); i++) {
      double testValue = ((Double)test.elementAt(i)).doubleValue();
      double prob = est.getProbability(testValue);
      Double p = new Double(prob);
      results.add(p);
    }
    return results;
  }
  








  protected double[] getMinimumMaximum(Instances inst, int attrIndex)
  {
    double[] minMax = new double[2];
    try
    {
      num = getMinMax(inst, attrIndex, minMax);
    } catch (Exception ex) { int num;
      ex.printStackTrace();
      System.out.println(ex.getMessage());
    }
    return minMax;
  }
  










  public static int getMinMax(Instances inst, int attrIndex, double[] minMax)
    throws Exception
  {
    double min = NaN.0D;
    double max = NaN.0D;
    Instance instance = null;
    int numNotMissing = 0;
    if ((minMax == null) || (minMax.length < 2)) {
      throw new Exception("Error in Program, privat method getMinMax");
    }
    
    Enumeration enumInst = inst.enumerateInstances();
    if (enumInst.hasMoreElements()) {
      do {
        instance = (Instance)enumInst.nextElement();
      } while ((instance.isMissing(attrIndex)) && (enumInst.hasMoreElements()));
      

      if (!instance.isMissing(attrIndex)) {
        numNotMissing++;
        min = instance.value(attrIndex);
        max = instance.value(attrIndex);
      }
      while (enumInst.hasMoreElements()) {
        instance = (Instance)enumInst.nextElement();
        if (!instance.isMissing(attrIndex)) {
          numNotMissing++;
          if (instance.value(attrIndex) < min) {
            min = instance.value(attrIndex);
          }
          else if (instance.value(attrIndex) > max) {
            max = instance.value(attrIndex);
          }
        }
      }
    }
    
    minMax[0] = min;
    minMax[1] = max;
    return numNotMissing;
  }
  




  private String probsToString(Vector probs)
  {
    StringBuffer txt = new StringBuffer(" ");
    for (int i = 0; i < probs.size(); i++) {
      txt.append("" + ((Double)probs.elementAt(i)).doubleValue() + " ");
    }
    return txt.toString();
  }
  






  protected Instances process(Instances data)
  {
    if (getPostProcessor() == null) {
      return data;
    }
    return getPostProcessor().process(data);
  }
  






  protected void printAttributeSummary(AttrTypes attrTypes, int classType)
  {
    String str = "";
    
    if (numeric) {
      str = str + " numeric";
    }
    if (nominal) {
      if (str.length() > 0)
        str = str + " &";
      str = str + " nominal";
    }
    
    if (string) {
      if (str.length() > 0)
        str = str + " &";
      str = str + " string";
    }
    
    if (date) {
      if (str.length() > 0)
        str = str + " &";
      str = str + " date";
    }
    
    if (relational) {
      if (str.length() > 0)
        str = str + " &";
      str = str + " relational";
    }
    
    str = str + " attributes)";
    
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
  






  protected void printAttributeSummary(int attrType, int classType)
  {
    String str = "";
    
    switch (attrType) {
    case 0: 
      str = " numeric" + str;
      break;
    case 1: 
      str = " nominal" + str;
      break;
    case 2: 
      str = " string" + str;
      break;
    case 3: 
      str = " date" + str;
      break;
    case 4: 
      str = " relational" + str;
    }
    
    str = str + " attribute(s))";
    
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
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
  



  public static void main(String[] args)
  {
    try
    {
      CheckEstimator check = new CheckEstimator();
      try
      {
        check.setOptions(args);
        Utils.checkForRemainingOptions(args);
      } catch (Exception ex) {
        String result = ex.getMessage() + "\n\n" + check.getClass().getName().replaceAll(".*\\.", "") + " Options:\n\n";
        Enumeration enu = check.listOptions();
        while (enu.hasMoreElements()) {
          Option option = (Option)enu.nextElement();
          result = result + option.synopsis() + "\n" + option.description() + "\n";
        }
        throw new Exception(result);
      }
      
      check.doTests();
    } catch (Exception ex) {
      System.err.println(ex.getMessage());
    }
  }
}
