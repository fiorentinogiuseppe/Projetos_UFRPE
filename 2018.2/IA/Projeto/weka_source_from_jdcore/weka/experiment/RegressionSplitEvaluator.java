package weka.experiment;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.rules.ZeroR;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Summarizable;
import weka.core.Utils;
























































public class RegressionSplitEvaluator
  implements SplitEvaluator, OptionHandler, AdditionalMeasureProducer, RevisionHandler
{
  static final long serialVersionUID = -328181640503349202L;
  protected Classifier m_Template = new ZeroR();
  

  protected Classifier m_Classifier;
  

  protected String[] m_AdditionalMeasures = null;
  





  protected boolean[] m_doesProduce = null;
  

  protected String m_result = null;
  

  protected String m_ClassifierOptions = "";
  

  protected String m_ClassifierVersion = "";
  


  private static final int KEY_SIZE = 3;
  

  private static final int RESULT_SIZE = 23;
  


  public RegressionSplitEvaluator()
  {
    updateOptions();
  }
  





  public String globalInfo()
  {
    return "A SplitEvaluator that produces results for a classification scheme on a numeric class attribute.";
  }
  







  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    
    newVector.addElement(new Option("\tThe full class name of the classifier.\n\teg: weka.classifiers.bayes.NaiveBayes", "W", 1, "-W <class name>"));
    


    if ((m_Template != null) && ((m_Template instanceof OptionHandler))) {
      newVector.addElement(new Option("", "", 0, "\nOptions specific to classifier " + m_Template.getClass().getName() + ":"));
      

      Enumeration enu = m_Template.listOptions();
      while (enu.hasMoreElements()) {
        newVector.addElement(enu.nextElement());
      }
    }
    return newVector.elements();
  }
  






























  public void setOptions(String[] options)
    throws Exception
  {
    String cName = Utils.getOption('W', options);
    if (cName.length() > 0)
    {



      setClassifier(Classifier.forName(cName, null));
    }
    if ((getClassifier() instanceof OptionHandler)) {
      getClassifier().setOptions(Utils.partitionOptions(options));
      
      updateOptions();
    }
  }
  






  public String[] getOptions()
  {
    String[] classifierOptions = new String[0];
    if ((m_Template != null) && ((m_Template instanceof OptionHandler))) {
      classifierOptions = m_Template.getOptions();
    }
    
    String[] options = new String[classifierOptions.length + 3];
    int current = 0;
    
    if (getClassifier() != null) {
      options[(current++)] = "-W";
      options[(current++)] = getClassifier().getClass().getName();
    }
    options[(current++)] = "--";
    
    System.arraycopy(classifierOptions, 0, options, current, classifierOptions.length);
    
    current += classifierOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  








  public void setAdditionalMeasures(String[] additionalMeasures)
  {
    m_AdditionalMeasures = additionalMeasures;
    


    if ((m_AdditionalMeasures != null) && (m_AdditionalMeasures.length > 0)) {
      m_doesProduce = new boolean[m_AdditionalMeasures.length];
      
      if ((m_Template instanceof AdditionalMeasureProducer)) {
        Enumeration en = ((AdditionalMeasureProducer)m_Template).enumerateMeasures();
        
        while (en.hasMoreElements()) {
          String mname = (String)en.nextElement();
          for (int j = 0; j < m_AdditionalMeasures.length; j++) {
            if (mname.compareToIgnoreCase(m_AdditionalMeasures[j]) == 0) {
              m_doesProduce[j] = true;
            }
          }
        }
      }
    } else {
      m_doesProduce = null;
    }
  }
  






  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector();
    if ((m_Template instanceof AdditionalMeasureProducer)) {
      Enumeration en = ((AdditionalMeasureProducer)m_Template).enumerateMeasures();
      
      while (en.hasMoreElements()) {
        String mname = (String)en.nextElement();
        newVector.addElement(mname);
      }
    }
    return newVector.elements();
  }
  







  public double getMeasure(String additionalMeasureName)
  {
    if ((m_Template instanceof AdditionalMeasureProducer)) {
      if (m_Classifier == null) {
        throw new IllegalArgumentException("ClassifierSplitEvaluator: Can't return result for measure, classifier has not been built yet.");
      }
      

      return ((AdditionalMeasureProducer)m_Classifier).getMeasure(additionalMeasureName);
    }
    
    throw new IllegalArgumentException("ClassifierSplitEvaluator: Can't return value for : " + additionalMeasureName + ". " + m_Template.getClass().getName() + " " + "is not an AdditionalMeasureProducer");
  }
  












  public Object[] getKeyTypes()
  {
    Object[] keyTypes = new Object[3];
    keyTypes[0] = "";
    keyTypes[1] = "";
    keyTypes[2] = "";
    return keyTypes;
  }
  







  public String[] getKeyNames()
  {
    String[] keyNames = new String[3];
    keyNames[0] = "Scheme";
    keyNames[1] = "Scheme_options";
    keyNames[2] = "Scheme_version_ID";
    return keyNames;
  }
  









  public Object[] getKey()
  {
    Object[] key = new Object[3];
    key[0] = m_Template.getClass().getName();
    key[1] = m_ClassifierOptions;
    key[2] = m_ClassifierVersion;
    return key;
  }
  








  public Object[] getResultTypes()
  {
    int addm = m_AdditionalMeasures != null ? m_AdditionalMeasures.length : 0;
    Object[] resultTypes = new Object[23 + addm];
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
    
    resultTypes[(current++)] = "";
    

    for (int i = 0; i < addm; i++) {
      resultTypes[(current++)] = doub;
    }
    if (current != 23 + addm) {
      throw new Error("ResultTypes didn't fit RESULT_SIZE");
    }
    return resultTypes;
  }
  






  public String[] getResultNames()
  {
    int addm = m_AdditionalMeasures != null ? m_AdditionalMeasures.length : 0;
    String[] resultNames = new String[23 + addm];
    int current = 0;
    resultNames[(current++)] = "Number_of_training_instances";
    resultNames[(current++)] = "Number_of_testing_instances";
    

    resultNames[(current++)] = "Mean_absolute_error";
    resultNames[(current++)] = "Root_mean_squared_error";
    resultNames[(current++)] = "Relative_absolute_error";
    resultNames[(current++)] = "Root_relative_squared_error";
    resultNames[(current++)] = "Correlation_coefficient";
    resultNames[(current++)] = "Number_unclassified";
    resultNames[(current++)] = "Percent_unclassified";
    

    resultNames[(current++)] = "SF_prior_entropy";
    resultNames[(current++)] = "SF_scheme_entropy";
    resultNames[(current++)] = "SF_entropy_gain";
    resultNames[(current++)] = "SF_mean_prior_entropy";
    resultNames[(current++)] = "SF_mean_scheme_entropy";
    resultNames[(current++)] = "SF_mean_entropy_gain";
    

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
    if (current != 23 + addm) {
      throw new Error("ResultNames didn't fit RESULT_SIZE");
    }
    return resultNames;
  }
  











  public Object[] getResult(Instances train, Instances test)
    throws Exception
  {
    if (train.classAttribute().type() != 0) {
      throw new Exception("Class attribute is not numeric!");
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
    Object[] result = new Object[23 + addm];
    long thID = Thread.currentThread().getId();
    long CPUStartTime = -1L;long trainCPUTimeElapsed = -1L;long testCPUTimeElapsed = -1L;
    Evaluation eval = new Evaluation(train);
    m_Classifier = Classifier.makeCopy(m_Template);
    
    long trainTimeStart = System.currentTimeMillis();
    if (canMeasureCPUTime) {
      CPUStartTime = thMonitor.getThreadUserTime(thID);
    }
    m_Classifier.buildClassifier(train);
    if (canMeasureCPUTime) {
      trainCPUTimeElapsed = thMonitor.getThreadUserTime(thID) - CPUStartTime;
    }
    long trainTimeElapsed = System.currentTimeMillis() - trainTimeStart;
    long testTimeStart = System.currentTimeMillis();
    if (canMeasureCPUTime) {
      CPUStartTime = thMonitor.getThreadUserTime(thID);
    }
    eval.evaluateModel(m_Classifier, test, new Object[0]);
    if (canMeasureCPUTime) {
      testCPUTimeElapsed = thMonitor.getThreadUserTime(thID) - CPUStartTime;
    }
    long testTimeElapsed = System.currentTimeMillis() - testTimeStart;
    thMonitor = null;
    
    m_result = eval.toSummaryString();
    

    int current = 0;
    result[(current++)] = new Double(train.numInstances());
    result[(current++)] = new Double(eval.numInstances());
    
    result[(current++)] = new Double(eval.meanAbsoluteError());
    result[(current++)] = new Double(eval.rootMeanSquaredError());
    result[(current++)] = new Double(eval.relativeAbsoluteError());
    result[(current++)] = new Double(eval.rootRelativeSquaredError());
    result[(current++)] = new Double(eval.correlationCoefficient());
    result[(current++)] = new Double(eval.unclassified());
    result[(current++)] = new Double(eval.pctUnclassified());
    
    result[(current++)] = new Double(eval.SFPriorEntropy());
    result[(current++)] = new Double(eval.SFSchemeEntropy());
    result[(current++)] = new Double(eval.SFEntropyGain());
    result[(current++)] = new Double(eval.SFMeanPriorEntropy());
    result[(current++)] = new Double(eval.SFMeanSchemeEntropy());
    result[(current++)] = new Double(eval.SFMeanEntropyGain());
    

    result[(current++)] = new Double(trainTimeElapsed / 1000.0D);
    result[(current++)] = new Double(testTimeElapsed / 1000.0D);
    if (canMeasureCPUTime) {
      result[(current++)] = new Double(trainCPUTimeElapsed / 1000000.0D / 1000.0D);
      
      result[(current++)] = new Double(testCPUTimeElapsed / 1000000.0D / 1000.0D);
    } else {
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
    
    if (current != 23 + addm) {
      throw new Error("Results didn't fit RESULT_SIZE");
    }
    return result;
  }
  





  public String classifierTipText()
  {
    return "The classifier to use.";
  }
  





  public Classifier getClassifier()
  {
    return m_Template;
  }
  





  public void setClassifier(Classifier newClassifier)
  {
    m_Template = newClassifier;
    updateOptions();
    
    System.err.println("RegressionSplitEvaluator: In set classifier");
  }
  



  protected void updateOptions()
  {
    if ((m_Template instanceof OptionHandler)) {
      m_ClassifierOptions = Utils.joinOptions(m_Template.getOptions());
    }
    else {
      m_ClassifierOptions = "";
    }
    if ((m_Template instanceof Serializable)) {
      ObjectStreamClass obs = ObjectStreamClass.lookup(m_Template.getClass());
      m_ClassifierVersion = ("" + obs.getSerialVersionUID());
    } else {
      m_ClassifierVersion = "";
    }
  }
  





  public void setClassifierName(String newClassifierName)
    throws Exception
  {
    try
    {
      setClassifier((Classifier)Class.forName(newClassifierName).newInstance());
    } catch (Exception ex) {
      throw new Exception("Can't find Classifier with class name: " + newClassifierName);
    }
  }
  






  public String getRawResultOutput()
  {
    StringBuffer result = new StringBuffer();
    
    if (m_Classifier == null) {
      return "<null> classifier";
    }
    result.append(toString());
    result.append("Classifier model: \n" + m_Classifier.toString() + '\n');
    

    if (m_result != null) {
      result.append(m_result);
      
      if (m_doesProduce != null) {
        for (int i = 0; i < m_doesProduce.length; i++) {
          if (m_doesProduce[i] != 0) {
            try {
              double dv = ((AdditionalMeasureProducer)m_Classifier).getMeasure(m_AdditionalMeasures[i]);
              
              if (!Instance.isMissingValue(dv)) {
                Double value = new Double(dv);
                result.append(m_AdditionalMeasures[i] + " : " + value + '\n');
              } else {
                result.append(m_AdditionalMeasures[i] + " : " + '?' + '\n');
              }
            } catch (Exception ex) {
              System.err.println(ex);
            }
          }
        }
      }
    }
    return result.toString();
  }
  






  public String toString()
  {
    String result = "RegressionSplitEvaluator: ";
    if (m_Template == null) {
      return result + "<null> classifier";
    }
    return result + m_Template.getClass().getName() + " " + m_ClassifierOptions + "(version " + m_ClassifierVersion + ")";
  }
  






  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11198 $");
  }
}
