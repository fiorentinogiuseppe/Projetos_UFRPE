package weka.experiment;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.core.AdditionalMeasureProducer;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;






























































































































































public class LearningRateResultProducer
  implements ResultListener, ResultProducer, OptionHandler, AdditionalMeasureProducer, RevisionHandler
{
  static final long serialVersionUID = -3841159673490861331L;
  protected Instances m_Instances;
  protected ResultListener m_ResultListener = new CSVResultListener();
  

  protected ResultProducer m_ResultProducer = new AveragingResultProducer();
  

  protected String[] m_AdditionalMeasures = null;
  




  protected int m_LowerSize = 0;
  




  protected int m_UpperSize = -1;
  

  protected int m_StepSize = 10;
  

  protected int m_CurrentSize = 0;
  

  public static String STEP_FIELD_NAME = "Total_instances";
  


  public LearningRateResultProducer() {}
  

  public String globalInfo()
  {
    return "Tells a sub-ResultProducer to reproduce the current run for varying sized subsamples of the dataset. Normally used with an AveragingResultProducer and CrossValidationResultProducer combo to generate learning curve results. For non-numeric result fields, the first value is used.";
  }
  















  public String[] determineColumnConstraints(ResultProducer rp)
    throws Exception
  {
    return null;
  }
  








  public void doRunKeys(int run)
    throws Exception
  {
    if (m_ResultProducer == null) {
      throw new Exception("No ResultProducer set");
    }
    if (m_ResultListener == null) {
      throw new Exception("No ResultListener set");
    }
    if (m_Instances == null) {
      throw new Exception("No Instances set");
    }
    

    m_ResultProducer.setResultListener(this);
    m_ResultProducer.setInstances(m_Instances);
    

    if (m_LowerSize == 0) {
      m_CurrentSize = m_StepSize;
    } else {
      m_CurrentSize = m_LowerSize;
    }
    while ((m_CurrentSize <= m_Instances.numInstances()) && ((m_UpperSize == -1) || (m_CurrentSize <= m_UpperSize)))
    {

      m_ResultProducer.doRunKeys(run);
      m_CurrentSize += m_StepSize;
    }
  }
  








  public void doRun(int run)
    throws Exception
  {
    if (m_ResultProducer == null) {
      throw new Exception("No ResultProducer set");
    }
    if (m_ResultListener == null) {
      throw new Exception("No ResultListener set");
    }
    if (m_Instances == null) {
      throw new Exception("No Instances set");
    }
    

    Instances runInstances = new Instances(m_Instances);
    runInstances.randomize(new Random(run));
    





    m_ResultProducer.setResultListener(this);
    

    if (m_LowerSize == 0) {
      m_CurrentSize = m_StepSize;
    } else {
      m_CurrentSize = m_LowerSize;
    }
    while ((m_CurrentSize <= m_Instances.numInstances()) && ((m_UpperSize == -1) || (m_CurrentSize <= m_UpperSize)))
    {

      m_ResultProducer.setInstances(new Instances(runInstances, 0, m_CurrentSize));
      
      m_ResultProducer.doRun(run);
      m_CurrentSize += m_StepSize;
    }
  }
  






  public void preProcess(ResultProducer rp)
    throws Exception
  {
    if (m_ResultListener == null) {
      throw new Exception("No ResultListener set");
    }
    m_ResultListener.preProcess(this);
  }
  






  public void preProcess()
    throws Exception
  {
    if (m_ResultProducer == null) {
      throw new Exception("No ResultProducer set");
    }
    
    m_ResultProducer.setResultListener(this);
    m_ResultProducer.preProcess();
  }
  







  public void postProcess(ResultProducer rp)
    throws Exception
  {
    m_ResultListener.postProcess(this);
  }
  







  public void postProcess()
    throws Exception
  {
    m_ResultProducer.postProcess();
  }
  











  public void acceptResult(ResultProducer rp, Object[] key, Object[] result)
    throws Exception
  {
    if (m_ResultProducer != rp) {
      throw new Error("Unrecognized ResultProducer sending results!!");
    }
    
    Object[] newKey = new Object[key.length + 1];
    System.arraycopy(key, 0, newKey, 0, key.length);
    newKey[key.length] = new String("" + m_CurrentSize);
    
    m_ResultListener.acceptResult(this, newKey, result);
  }
  










  public boolean isResultRequired(ResultProducer rp, Object[] key)
    throws Exception
  {
    if (m_ResultProducer != rp) {
      throw new Error("Unrecognized ResultProducer sending results!!");
    }
    
    Object[] newKey = new Object[key.length + 1];
    System.arraycopy(key, 0, newKey, 0, key.length);
    newKey[key.length] = new String("" + m_CurrentSize);
    
    return m_ResultListener.isResultRequired(this, newKey);
  }
  






  public String[] getKeyNames()
    throws Exception
  {
    String[] keyNames = m_ResultProducer.getKeyNames();
    String[] newKeyNames = new String[keyNames.length + 1];
    System.arraycopy(keyNames, 0, newKeyNames, 0, keyNames.length);
    
    newKeyNames[keyNames.length] = STEP_FIELD_NAME;
    return newKeyNames;
  }
  









  public Object[] getKeyTypes()
    throws Exception
  {
    Object[] keyTypes = m_ResultProducer.getKeyTypes();
    Object[] newKeyTypes = new Object[keyTypes.length + 1];
    System.arraycopy(keyTypes, 0, newKeyTypes, 0, keyTypes.length);
    newKeyTypes[keyTypes.length] = "";
    return newKeyTypes;
  }
  











  public String[] getResultNames()
    throws Exception
  {
    return m_ResultProducer.getResultNames();
  }
  








  public Object[] getResultTypes()
    throws Exception
  {
    return m_ResultProducer.getResultTypes();
  }
  














  public String getCompatibilityState()
  {
    String result = " ";
    

    if (m_ResultProducer == null) {
      result = result + "<null ResultProducer>";
    } else {
      result = result + "-W " + m_ResultProducer.getClass().getName();
      result = result + " -- " + m_ResultProducer.getCompatibilityState();
    }
    
    return result.trim();
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tThe number of steps in the learning rate curve.\n\t(default 10)", "X", 1, "-X <num steps>"));
    



    newVector.addElement(new Option("\tThe full class name of a ResultProducer.\n\teg: weka.experiment.CrossValidationResultProducer", "W", 1, "-W <class name>"));
    




    if ((m_ResultProducer != null) && ((m_ResultProducer instanceof OptionHandler)))
    {
      newVector.addElement(new Option("", "", 0, "\nOptions specific to result producer " + m_ResultProducer.getClass().getName() + ":"));
      


      Enumeration enu = ((OptionHandler)m_ResultProducer).listOptions();
      while (enu.hasMoreElements()) {
        newVector.addElement(enu.nextElement());
      }
    }
    return newVector.elements();
  }
  




























































































































  public void setOptions(String[] options)
    throws Exception
  {
    String stepSize = Utils.getOption('S', options);
    if (stepSize.length() != 0) {
      setStepSize(Integer.parseInt(stepSize));
    } else {
      setStepSize(10);
    }
    
    String lowerSize = Utils.getOption('L', options);
    if (lowerSize.length() != 0) {
      setLowerSize(Integer.parseInt(lowerSize));
    } else {
      setLowerSize(0);
    }
    
    String upperSize = Utils.getOption('U', options);
    if (upperSize.length() != 0) {
      setUpperSize(Integer.parseInt(upperSize));
    } else {
      setUpperSize(-1);
    }
    
    String rpName = Utils.getOption('W', options);
    if (rpName.length() > 0)
    {



      setResultProducer((ResultProducer)Utils.forName(ResultProducer.class, rpName, null));
    }
    


    if ((getResultProducer() instanceof OptionHandler)) {
      ((OptionHandler)getResultProducer()).setOptions(Utils.partitionOptions(options));
    }
  }
  







  public String[] getOptions()
  {
    String[] seOptions = new String[0];
    if ((m_ResultProducer != null) && ((m_ResultProducer instanceof OptionHandler)))
    {
      seOptions = ((OptionHandler)m_ResultProducer).getOptions();
    }
    
    String[] options = new String[seOptions.length + 9];
    int current = 0;
    
    options[(current++)] = "-S";
    options[(current++)] = ("" + getStepSize());
    options[(current++)] = "-L";
    options[(current++)] = ("" + getLowerSize());
    options[(current++)] = "-U";
    options[(current++)] = ("" + getUpperSize());
    if (getResultProducer() != null) {
      options[(current++)] = "-W";
      options[(current++)] = getResultProducer().getClass().getName();
    }
    options[(current++)] = "--";
    
    System.arraycopy(seOptions, 0, options, current, seOptions.length);
    
    current += seOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  








  public void setAdditionalMeasures(String[] additionalMeasures)
  {
    m_AdditionalMeasures = additionalMeasures;
    
    if (m_ResultProducer != null) {
      System.err.println("LearningRateResultProducer: setting additional measures for ResultProducer");
      

      m_ResultProducer.setAdditionalMeasures(m_AdditionalMeasures);
    }
  }
  






  public Enumeration enumerateMeasures()
  {
    Vector newVector = new Vector();
    if ((m_ResultProducer instanceof AdditionalMeasureProducer)) {
      Enumeration en = ((AdditionalMeasureProducer)m_ResultProducer).enumerateMeasures();
      
      while (en.hasMoreElements()) {
        String mname = (String)en.nextElement();
        newVector.addElement(mname);
      }
    }
    return newVector.elements();
  }
  







  public double getMeasure(String additionalMeasureName)
  {
    if ((m_ResultProducer instanceof AdditionalMeasureProducer)) {
      return ((AdditionalMeasureProducer)m_ResultProducer).getMeasure(additionalMeasureName);
    }
    
    throw new IllegalArgumentException("LearningRateResultProducer: Can't return value for : " + additionalMeasureName + ". " + m_ResultProducer.getClass().getName() + " " + "is not an AdditionalMeasureProducer");
  }
  










  public void setInstances(Instances instances)
  {
    m_Instances = instances;
  }
  





  public String lowerSizeTipText()
  {
    return "Set the minmum number of instances in a dataset. Setting zero here will actually use <stepSize> number of instances at the first step (since it makes no sense to use zero instances :-))";
  }
  







  public int getLowerSize()
  {
    return m_LowerSize;
  }
  





  public void setLowerSize(int newLowerSize)
  {
    m_LowerSize = newLowerSize;
  }
  





  public String upperSizeTipText()
  {
    return "Set the maximum number of instances in a dataset. Setting -1 sets no upper limit (other than the total number of instances in the full dataset)";
  }
  







  public int getUpperSize()
  {
    return m_UpperSize;
  }
  





  public void setUpperSize(int newUpperSize)
  {
    m_UpperSize = newUpperSize;
  }
  





  public String stepSizeTipText()
  {
    return "Set the number of instances to add at each step.";
  }
  





  public int getStepSize()
  {
    return m_StepSize;
  }
  





  public void setStepSize(int newStepSize)
  {
    m_StepSize = newStepSize;
  }
  






  public void setResultListener(ResultListener listener)
  {
    m_ResultListener = listener;
  }
  





  public String resultProducerTipText()
  {
    return "Set the resultProducer for which learning rate results should be generated.";
  }
  






  public ResultProducer getResultProducer()
  {
    return m_ResultProducer;
  }
  





  public void setResultProducer(ResultProducer newResultProducer)
  {
    m_ResultProducer = newResultProducer;
    m_ResultProducer.setResultListener(this);
  }
  






  public String toString()
  {
    String result = "LearningRateResultProducer: ";
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
    return RevisionUtils.extract("$Revision: 11198 $");
  }
}
