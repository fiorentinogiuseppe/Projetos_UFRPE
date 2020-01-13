package weka.experiment;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Summarizable;
import weka.core.Utils;













































































public class CostSensitiveClassifierSplitEvaluator
  extends ClassifierSplitEvaluator
{
  static final long serialVersionUID = -8069566663019501276L;
  protected File m_OnDemandDirectory = new File(System.getProperty("user.dir"));
  

  private static final int RESULT_SIZE = 31;
  

  public CostSensitiveClassifierSplitEvaluator() {}
  

  public String globalInfo()
  {
    return " SplitEvaluator that produces results for a classification scheme on a nominal class attribute, including weighted misclassification costs.";
  }
  







  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    
    newVector.addElement(new Option("\tName of a directory to search for cost files when loading\n\tcosts on demand (default current directory).", "D", 1, "-D <directory>"));
    



    return newVector.elements();
  }
  











































  public void setOptions(String[] options)
    throws Exception
  {
    String demandDir = Utils.getOption('D', options);
    if (demandDir.length() != 0) {
      setOnDemandDirectory(new File(demandDir));
    }
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 3];
    int current = 0;
    
    options[(current++)] = "-D";
    options[(current++)] = ("" + getOnDemandDirectory());
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    
    current += superOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String onDemandDirectoryTipText()
  {
    return "The directory to look in for cost files. This directory will be searched for cost files when loading on demand.";
  }
  







  public File getOnDemandDirectory()
  {
    return m_OnDemandDirectory;
  }
  






  public void setOnDemandDirectory(File newDir)
  {
    if (newDir.isDirectory()) {
      m_OnDemandDirectory = newDir;
    } else {
      m_OnDemandDirectory = new File(newDir.getParent());
    }
  }
  







  public Object[] getResultTypes()
  {
    int addm = m_AdditionalMeasures != null ? m_AdditionalMeasures.length : 0;
    

    Object[] resultTypes = new Object[31 + addm];
    Double doub = new Double(0.0D);
    int current = 0;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    

    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    

    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    resultTypes[(current++)] = doub;
    
    resultTypes[(current++)] = "";
    

    for (int i = 0; i < addm; i++) {
      resultTypes[(current++)] = doub;
    }
    if (current != 31 + addm) {
      throw new Error("ResultTypes didn't fit RESULT_SIZE");
    }
    return resultTypes;
  }
  






  public String[] getResultNames()
  {
    int addm = m_AdditionalMeasures != null ? m_AdditionalMeasures.length : 0;
    

    String[] resultNames = new String[31 + addm];
    int current = 0;
    resultNames[(current++)] = "Number_of_training_instances";
    resultNames[(current++)] = "Number_of_testing_instances";
    

    resultNames[(current++)] = "Number_correct";
    resultNames[(current++)] = "Number_incorrect";
    resultNames[(current++)] = "Number_unclassified";
    resultNames[(current++)] = "Percent_correct";
    resultNames[(current++)] = "Percent_incorrect";
    resultNames[(current++)] = "Percent_unclassified";
    resultNames[(current++)] = "Total_cost";
    resultNames[(current++)] = "Average_cost";
    

    resultNames[(current++)] = "Mean_absolute_error";
    resultNames[(current++)] = "Root_mean_squared_error";
    resultNames[(current++)] = "Relative_absolute_error";
    resultNames[(current++)] = "Root_relative_squared_error";
    

    resultNames[(current++)] = "SF_prior_entropy";
    resultNames[(current++)] = "SF_scheme_entropy";
    resultNames[(current++)] = "SF_entropy_gain";
    resultNames[(current++)] = "SF_mean_prior_entropy";
    resultNames[(current++)] = "SF_mean_scheme_entropy";
    resultNames[(current++)] = "SF_mean_entropy_gain";
    

    resultNames[(current++)] = "KB_information";
    resultNames[(current++)] = "KB_mean_information";
    resultNames[(current++)] = "KB_relative_information";
    

    resultNames[(current++)] = "Elapsed_Time_training";
    resultNames[(current++)] = "Elapsed_Time_testing";
    resultNames[(current++)] = "UserCPU_Time_training";
    resultNames[(current++)] = "UserCPU_Time_testing";
    

    resultNames[(current++)] = "Serialized_Model_Size";
    resultNames[(current++)] = "Serialized_Train_Set_Size";
    resultNames[(current++)] = "Serialized_Test_Set_Size";
    

    resultNames[(current++)] = "Summary";
    
    for (int i = 0; i < addm; i++) {
      resultNames[(current++)] = m_AdditionalMeasures[i];
    }
    if (current != 31 + addm) {
      throw new Error("ResultNames didn't fit RESULT_SIZE");
    }
    return resultNames;
  }
  











  public Object[] getResult(Instances train, Instances test)
    throws Exception
  {
    if (train.classAttribute().type() != 1) {
      throw new Exception("Class attribute is not nominal!");
    }
    if (m_Template == null) {
      throw new Exception("No classifier has been specified");
    }
    ThreadMXBean thMonitor = ManagementFactory.getThreadMXBean();
    boolean canMeasureCPUTime = thMonitor.isThreadCpuTimeSupported();
    if ((canMeasureCPUTime) && (!thMonitor.isThreadCpuTimeEnabled())) {
      thMonitor.setThreadCpuTimeEnabled(true);
    }
    int addm = m_AdditionalMeasures != null ? m_AdditionalMeasures.length : 0;
    Object[] result = new Object[31 + addm];
    long thID = Thread.currentThread().getId();
    long CPUStartTime = -1L;long trainCPUTimeElapsed = -1L;long testCPUTimeElapsed = -1L;
    

    String costName = train.relationName() + CostMatrix.FILE_EXTENSION;
    File costFile = new File(getOnDemandDirectory(), costName);
    if (!costFile.exists()) {
      throw new Exception("On-demand cost file doesn't exist: " + costFile);
    }
    CostMatrix costMatrix = new CostMatrix(new BufferedReader(new FileReader(costFile)));
    

    Evaluation eval = new Evaluation(train, costMatrix);
    m_Classifier = Classifier.makeCopy(m_Template);
    
    long trainTimeStart = System.currentTimeMillis();
    if (canMeasureCPUTime)
      CPUStartTime = thMonitor.getThreadUserTime(thID);
    m_Classifier.buildClassifier(train);
    if (canMeasureCPUTime)
      trainCPUTimeElapsed = thMonitor.getThreadUserTime(thID) - CPUStartTime;
    long trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
    long testTimeStart = System.currentTimeMillis();
    if (canMeasureCPUTime)
      CPUStartTime = thMonitor.getThreadUserTime(thID);
    eval.evaluateModel(m_Classifier, test, new Object[0]);
    if (canMeasureCPUTime)
      testCPUTimeElapsed = thMonitor.getThreadUserTime(thID) - CPUStartTime;
    long testTimeElapsed = System.currentTimeMillis() - testTimeStart;
    thMonitor = null;
    
    m_result = eval.toSummaryString();
    

    int current = 0;
    result[(current++)] = new Double(train.numInstances());
    result[(current++)] = new Double(eval.numInstances());
    
    result[(current++)] = new Double(eval.correct());
    result[(current++)] = new Double(eval.incorrect());
    result[(current++)] = new Double(eval.unclassified());
    result[(current++)] = new Double(eval.pctCorrect());
    result[(current++)] = new Double(eval.pctIncorrect());
    result[(current++)] = new Double(eval.pctUnclassified());
    result[(current++)] = new Double(eval.totalCost());
    result[(current++)] = new Double(eval.avgCost());
    
    result[(current++)] = new Double(eval.meanAbsoluteError());
    result[(current++)] = new Double(eval.rootMeanSquaredError());
    result[(current++)] = new Double(eval.relativeAbsoluteError());
    result[(current++)] = new Double(eval.rootRelativeSquaredError());
    
    result[(current++)] = new Double(eval.SFPriorEntropy());
    result[(current++)] = new Double(eval.SFSchemeEntropy());
    result[(current++)] = new Double(eval.SFEntropyGain());
    result[(current++)] = new Double(eval.SFMeanPriorEntropy());
    result[(current++)] = new Double(eval.SFMeanSchemeEntropy());
    result[(current++)] = new Double(eval.SFMeanEntropyGain());
    

    result[(current++)] = new Double(eval.KBInformation());
    result[(current++)] = new Double(eval.KBMeanInformation());
    result[(current++)] = new Double(eval.KBRelativeInformation());
    

    result[(current++)] = new Double(trainTimeElapsed / 1000.0D);
    result[(current++)] = new Double(testTimeElapsed / 1000.0D);
    if (canMeasureCPUTime) {
      result[(current++)] = new Double(trainCPUTimeElapsed / 1000000.0D / 1000.0D);
      result[(current++)] = new Double(testCPUTimeElapsed / 1000000.0D / 1000.0D);
    }
    else {
      result[(current++)] = new Double(Instance.missingValue());
      result[(current++)] = new Double(Instance.missingValue());
    }
    

    ByteArrayOutputStream bastream = new ByteArrayOutputStream();
    ObjectOutputStream oostream = new ObjectOutputStream(bastream);
    oostream.writeObject(m_Classifier);
    result[(current++)] = new Double(bastream.size());
    bastream = new ByteArrayOutputStream();
    oostream = new ObjectOutputStream(bastream);
    oostream.writeObject(train);
    result[(current++)] = new Double(bastream.size());
    bastream = new ByteArrayOutputStream();
    oostream = new ObjectOutputStream(bastream);
    oostream.writeObject(test);
    result[(current++)] = new Double(bastream.size());
    
    if ((m_Classifier instanceof Summarizable)) {
      result[(current++)] = ((Summarizable)m_Classifier).toSummaryString();
    } else {
      result[(current++)] = null;
    }
    
    for (int i = 0; i < addm; i++) {
      if (m_doesProduce[i] != 0) {
        try {
          double dv = ((AdditionalMeasureProducer)m_Classifier).getMeasure(m_AdditionalMeasures[i]);
          
          if (!Instance.isMissingValue(dv)) {
            Double value = new Double(dv);
            result[(current++)] = value;
          } else {
            result[(current++)] = null;
          }
        } catch (Exception ex) {
          System.err.println(ex);
        }
      } else {
        result[(current++)] = null;
      }
    }
    
    if (current != 31 + addm) {
      throw new Error("Results didn't fit RESULT_SIZE");
    }
    return result;
  }
  





  public String toString()
  {
    String result = "CostSensitiveClassifierSplitEvaluator: ";
    if (m_Template == null) {
      return result + "<null> classifier";
    }
    return result + m_Template.getClass().getName() + " " + m_ClassifierOptions + "(version " + m_ClassifierVersion + ")";
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7516 $");
  }
}
