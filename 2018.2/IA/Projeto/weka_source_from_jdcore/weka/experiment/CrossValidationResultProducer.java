package weka.experiment;

import java.io.File;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Random;
import java.util.TimeZone;
import java.util.Vector;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;

















































































































public class CrossValidationResultProducer
  implements ResultProducer, OptionHandler, AdditionalMeasureProducer, RevisionHandler
{
  static final long serialVersionUID = -1580053925080091917L;
  protected Instances m_Instances;
  protected ResultListener m_ResultListener = new CSVResultListener();
  

  protected int m_NumFolds = 10;
  

  protected boolean m_debugOutput = false;
  

  protected OutputZipper m_ZipDest = null;
  

  protected File m_OutputFile = new File(new File(System.getProperty("user.dir")), "splitEvalutorOut.zip");
  



  protected SplitEvaluator m_SplitEvaluator = new ClassifierSplitEvaluator();
  

  protected String[] m_AdditionalMeasures = null;
  

  public static String DATASET_FIELD_NAME = "Dataset";
  

  public static String RUN_FIELD_NAME = "Run";
  

  public static String FOLD_FIELD_NAME = "Fold";
  

  public static String TIMESTAMP_FIELD_NAME = "Date_time";
  


  public CrossValidationResultProducer() {}
  

  public String globalInfo()
  {
    return "Generates for each run, carries out an n-fold cross-validation, using the set SplitEvaluator to generate some results. If the class attribute is nominal, the dataset is stratified. Results for each fold are generated, so you may wish to use this in addition with an AveragingResultProducer to obtain averages for each run.";
  }
  











  public void setInstances(Instances instances)
  {
    m_Instances = instances;
  }
  






  public void setResultListener(ResultListener listener)
  {
    m_ResultListener = listener;
  }
  








  public void setAdditionalMeasures(String[] additionalMeasures)
  {
    m_AdditionalMeasures = additionalMeasures;
    
    if (m_SplitEvaluator != null) {
      System.err.println("CrossValidationResultProducer: setting additional measures for split evaluator");
      

      m_SplitEvaluator.setAdditionalMeasures(m_AdditionalMeasures);
    }
  }
  






  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector();
    if ((m_SplitEvaluator instanceof AdditionalMeasureProducer)) {
      Enumeration en = ((AdditionalMeasureProducer)m_SplitEvaluator).enumerateMeasures();
      
      while (en.hasMoreElements()) {
        String mname = (String)en.nextElement();
        newVector.addElement(mname);
      }
    }
    return newVector.elements();
  }
  







  public double getMeasure(String additionalMeasureName)
  {
    if ((m_SplitEvaluator instanceof AdditionalMeasureProducer)) {
      return ((AdditionalMeasureProducer)m_SplitEvaluator).getMeasure(additionalMeasureName);
    }
    
    throw new IllegalArgumentException("CrossValidationResultProducer: Can't return value for : " + additionalMeasureName + ". " + m_SplitEvaluator.getClass().getName() + " " + "is not an AdditionalMeasureProducer");
  }
  










  public static Double getTimestamp()
  {
    Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    double timestamp = now.get(1) * 10000 + (now.get(2) + 1) * 100 + now.get(5) + now.get(11) / 100.0D + now.get(12) / 10000.0D;
    



    return new Double(timestamp);
  }
  





  public void preProcess()
    throws Exception
  {
    if (m_SplitEvaluator == null) {
      throw new Exception("No SplitEvalutor set");
    }
    if (m_ResultListener == null) {
      throw new Exception("No ResultListener set");
    }
    m_ResultListener.preProcess(this);
  }
  







  public void postProcess()
    throws Exception
  {
    m_ResultListener.postProcess(this);
    
    if ((m_debugOutput) && 
      (m_ZipDest != null)) {
      m_ZipDest.finished();
      m_ZipDest = null;
    }
  }
  








  public void doRunKeys(int run)
    throws Exception
  {
    if (m_Instances == null) {
      throw new Exception("No Instances set");
    }
    





    for (int fold = 0; fold < m_NumFolds; fold++)
    {
      Object[] seKey = m_SplitEvaluator.getKey();
      Object[] key = new Object[seKey.length + 3];
      key[0] = Utils.backQuoteChars(m_Instances.relationName());
      key[1] = ("" + run);
      key[2] = ("" + (fold + 1));
      System.arraycopy(seKey, 0, key, 3, seKey.length);
      if (m_ResultListener.isResultRequired(this, key)) {
        try {
          m_ResultListener.acceptResult(this, key, null);
        }
        catch (Exception ex) {
          throw ex;
        }
      }
    }
  }
  








  public void doRun(int run)
    throws Exception
  {
    if ((getRawOutput()) && 
      (m_ZipDest == null)) {
      m_ZipDest = new OutputZipper(m_OutputFile);
    }
    

    if (m_Instances == null) {
      throw new Exception("No Instances set");
    }
    
    Instances runInstances = new Instances(m_Instances);
    Random random = new Random(run);
    runInstances.randomize(random);
    if (runInstances.classAttribute().isNominal()) {
      runInstances.stratify(m_NumFolds);
    }
    for (int fold = 0; fold < m_NumFolds; fold++)
    {
      Object[] seKey = m_SplitEvaluator.getKey();
      Object[] key = new Object[seKey.length + 3];
      key[0] = Utils.backQuoteChars(m_Instances.relationName());
      key[1] = ("" + run);
      key[2] = ("" + (fold + 1));
      System.arraycopy(seKey, 0, key, 3, seKey.length);
      if (m_ResultListener.isResultRequired(this, key)) {
        Instances train = runInstances.trainCV(m_NumFolds, fold, random);
        Instances test = runInstances.testCV(m_NumFolds, fold);
        try {
          Object[] seResults = m_SplitEvaluator.getResult(train, test);
          Object[] results = new Object[seResults.length + 1];
          results[0] = getTimestamp();
          System.arraycopy(seResults, 0, results, 1, seResults.length);
          
          if (m_debugOutput) {
            String resultName = ("" + run + "." + (fold + 1) + "." + Utils.backQuoteChars(runInstances.relationName()) + "." + m_SplitEvaluator.toString()).replace(' ', '_');
            


            resultName = Utils.removeSubstring(resultName, "weka.classifiers.");
            
            resultName = Utils.removeSubstring(resultName, "weka.filters.");
            
            resultName = Utils.removeSubstring(resultName, "weka.attributeSelection.");
            
            m_ZipDest.zipit(m_SplitEvaluator.getRawResultOutput(), resultName);
          }
          m_ResultListener.acceptResult(this, key, results);
        }
        catch (Exception ex) {
          throw ex;
        }
      }
    }
  }
  







  public String[] getKeyNames()
  {
    String[] keyNames = m_SplitEvaluator.getKeyNames();
    
    String[] newKeyNames = new String[keyNames.length + 3];
    newKeyNames[0] = DATASET_FIELD_NAME;
    newKeyNames[1] = RUN_FIELD_NAME;
    newKeyNames[2] = FOLD_FIELD_NAME;
    System.arraycopy(keyNames, 0, newKeyNames, 3, keyNames.length);
    return newKeyNames;
  }
  








  public Object[] getKeyTypes()
  {
    Object[] keyTypes = m_SplitEvaluator.getKeyTypes();
    
    Object[] newKeyTypes = new String[keyTypes.length + 3];
    newKeyTypes[0] = new String();
    newKeyTypes[1] = new String();
    newKeyTypes[2] = new String();
    System.arraycopy(keyTypes, 0, newKeyTypes, 3, keyTypes.length);
    return newKeyTypes;
  }
  







  public String[] getResultNames()
  {
    String[] resultNames = m_SplitEvaluator.getResultNames();
    
    String[] newResultNames = new String[resultNames.length + 1];
    newResultNames[0] = TIMESTAMP_FIELD_NAME;
    System.arraycopy(resultNames, 0, newResultNames, 1, resultNames.length);
    return newResultNames;
  }
  








  public Object[] getResultTypes()
  {
    Object[] resultTypes = m_SplitEvaluator.getResultTypes();
    
    Object[] newResultTypes = new Object[resultTypes.length + 1];
    newResultTypes[0] = new Double(0.0D);
    System.arraycopy(resultTypes, 0, newResultTypes, 1, resultTypes.length);
    return newResultTypes;
  }
  














  public String getCompatibilityState()
  {
    String result = "-X " + m_NumFolds + " ";
    if (m_SplitEvaluator == null) {
      result = result + "<null SplitEvaluator>";
    } else {
      result = result + "-W " + m_SplitEvaluator.getClass().getName();
    }
    return result + " --";
  }
  





  public String outputFileTipText()
  {
    return "Set the destination for saving raw output. If the rawOutput option is selected, then output from the splitEvaluator for individual folds is saved. If the destination is a directory, then each output is saved to an individual gzip file; if the destination is a file, then each output is saved as an entry in a zip file.";
  }
  










  public File getOutputFile()
  {
    return m_OutputFile;
  }
  





  public void setOutputFile(File newOutputFile)
  {
    m_OutputFile = newOutputFile;
  }
  





  public String numFoldsTipText()
  {
    return "Number of folds to use in cross validation.";
  }
  





  public int getNumFolds()
  {
    return m_NumFolds;
  }
  





  public void setNumFolds(int newNumFolds)
  {
    m_NumFolds = newNumFolds;
  }
  





  public String rawOutputTipText()
  {
    return "Save raw output (useful for debugging). If set, then output is sent to the destination specified by outputFile";
  }
  





  public boolean getRawOutput()
  {
    return m_debugOutput;
  }
  




  public void setRawOutput(boolean d)
  {
    m_debugOutput = d;
  }
  





  public String splitEvaluatorTipText()
  {
    return "The evaluator to apply to the cross validation folds. This may be a classifier, regression scheme etc.";
  }
  






  public SplitEvaluator getSplitEvaluator()
  {
    return m_SplitEvaluator;
  }
  





  public void setSplitEvaluator(SplitEvaluator newSplitEvaluator)
  {
    m_SplitEvaluator = newSplitEvaluator;
    m_SplitEvaluator.setAdditionalMeasures(m_AdditionalMeasures);
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tThe number of folds to use for the cross-validation.\n\t(default 10)", "X", 1, "-X <number of folds>"));
    




    newVector.addElement(new Option("Save raw split evaluator output.", "D", 0, "-D"));
    


    newVector.addElement(new Option("\tThe filename where raw output will be stored.\n\tIf a directory name is specified then then individual\n\toutputs will be gzipped, otherwise all output will be\n\tzipped to the named file. Use in conjuction with -D.\t(default splitEvalutorOut.zip)", "O", 1, "-O <file/directory name/path>"));
    







    newVector.addElement(new Option("\tThe full class name of a SplitEvaluator.\n\teg: weka.experiment.ClassifierSplitEvaluator", "W", 1, "-W <class name>"));
    




    if ((m_SplitEvaluator != null) && ((m_SplitEvaluator instanceof OptionHandler)))
    {
      newVector.addElement(new Option("", "", 0, "\nOptions specific to split evaluator " + m_SplitEvaluator.getClass().getName() + ":"));
      


      Enumeration enu = ((OptionHandler)m_SplitEvaluator).listOptions();
      while (enu.hasMoreElements()) {
        newVector.addElement(enu.nextElement());
      }
    }
    return newVector.elements();
  }
  
















































































  public void setOptions(String[] options)
    throws Exception
  {
    setRawOutput(Utils.getFlag('D', options));
    
    String fName = Utils.getOption('O', options);
    if (fName.length() != 0) {
      setOutputFile(new File(fName));
    }
    
    String numFolds = Utils.getOption('X', options);
    if (numFolds.length() != 0) {
      setNumFolds(Integer.parseInt(numFolds));
    } else {
      setNumFolds(10);
    }
    
    String seName = Utils.getOption('W', options);
    if (seName.length() > 0)
    {


      setSplitEvaluator((SplitEvaluator)Utils.forName(SplitEvaluator.class, seName, null));
    }
    


    if ((getSplitEvaluator() instanceof OptionHandler)) {
      ((OptionHandler)getSplitEvaluator()).setOptions(Utils.partitionOptions(options));
    }
  }
  







  public String[] getOptions()
  {
    String[] seOptions = new String[0];
    if ((m_SplitEvaluator != null) && ((m_SplitEvaluator instanceof OptionHandler)))
    {
      seOptions = ((OptionHandler)m_SplitEvaluator).getOptions();
    }
    
    String[] options = new String[seOptions.length + 8];
    int current = 0;
    
    options[(current++)] = "-X";
    options[(current++)] = ("" + getNumFolds());
    
    if (getRawOutput()) {
      options[(current++)] = "-D";
    }
    
    options[(current++)] = "-O";
    options[(current++)] = getOutputFile().getName();
    
    if (getSplitEvaluator() != null) {
      options[(current++)] = "-W";
      options[(current++)] = getSplitEvaluator().getClass().getName();
    }
    options[(current++)] = "--";
    
    System.arraycopy(seOptions, 0, options, current, seOptions.length);
    
    current += seOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  






  public String toString()
  {
    String result = "CrossValidationResultProducer: ";
    result = result + getCompatibilityState();
    if (m_Instances == null) {
      result = result + ": <null Instances>";
    } else {
      result = result + ": " + Utils.backQuoteChars(m_Instances.relationName());
    }
    return result;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.17 $");
  }
  





  public static void main(String[] args)
  {
    System.err.println(Utils.doubleToString(getTimestamp().doubleValue(), 4));
  }
}
