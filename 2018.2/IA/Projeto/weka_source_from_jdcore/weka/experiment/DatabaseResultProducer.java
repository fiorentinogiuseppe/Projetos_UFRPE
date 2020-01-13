package weka.experiment;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.AdditionalMeasureProducer;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;






























































































































public class DatabaseResultProducer
  extends DatabaseResultListener
  implements ResultProducer, OptionHandler, AdditionalMeasureProducer
{
  static final long serialVersionUID = -5620660780203158666L;
  protected Instances m_Instances;
  protected ResultListener m_ResultListener = new CSVResultListener();
  

  protected ResultProducer m_ResultProducer = new CrossValidationResultProducer();
  


  protected String[] m_AdditionalMeasures = null;
  






  public String globalInfo()
  {
    return "Examines a database and extracts out the results produced by the specified ResultProducer and submits them to the specified ResultListener. If a result needs to be generated, the ResultProducer is used to obtain the result.";
  }
  









  public DatabaseResultProducer()
    throws Exception
  {}
  









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
    m_ResultProducer.doRunKeys(run);
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
    

    m_ResultProducer.setResultListener(this);
    m_ResultProducer.setInstances(m_Instances);
    m_ResultProducer.doRun(run);
  }
  







  public void preProcess(ResultProducer rp)
    throws Exception
  {
    super.preProcess(rp);
    if (m_ResultListener == null) {
      throw new Exception("No ResultListener set");
    }
    m_ResultListener.preProcess(this);
  }
  







  public void postProcess(ResultProducer rp)
    throws Exception
  {
    super.postProcess(rp);
    m_ResultListener.postProcess(this);
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
    


    boolean isRequiredByListener = m_ResultListener.isResultRequired(this, key);
    

    boolean isRequiredByDatabase = super.isResultRequired(rp, key);
    

    if (isRequiredByDatabase)
    {

      if (result != null)
      {

        super.acceptResult(rp, key, result);
      }
    }
    

    if (isRequiredByListener) {
      m_ResultListener.acceptResult(this, key, result);
    }
  }
  










  public boolean isResultRequired(ResultProducer rp, Object[] key)
    throws Exception
  {
    if (m_ResultProducer != rp) {
      throw new Error("Unrecognized ResultProducer sending results!!");
    }
    


    boolean isRequiredByListener = m_ResultListener.isResultRequired(this, key);
    

    boolean isRequiredByDatabase = super.isResultRequired(rp, key);
    
    if ((!isRequiredByDatabase) && (isRequiredByListener))
    {
      Object[] result = getResultFromTable(m_ResultsTableName, rp, key);
      
      System.err.println("Got result from database: " + DatabaseUtils.arrayToString(result));
      
      m_ResultListener.acceptResult(this, key, result);
      return false;
    }
    
    return (isRequiredByListener) || (isRequiredByDatabase);
  }
  






  public String[] getKeyNames()
    throws Exception
  {
    return m_ResultProducer.getKeyNames();
  }
  








  public Object[] getKeyTypes()
    throws Exception
  {
    return m_ResultProducer.getKeyTypes();
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
    String result = "";
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
    
    newVector.addElement(new Option("\tThe name of the database field to cache over.\n\teg: \"Fold\" (default none)", "F", 1, "-F <field name>"));
    



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
    setCacheKeyName(Utils.getOption('F', options));
    
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
    
    String[] options = new String[seOptions.length + 8];
    int current = 0;
    
    if (!getCacheKeyName().equals("")) {
      options[(current++)] = "-F";
      options[(current++)] = getCacheKeyName();
    }
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
      System.err.println("DatabaseResultProducer: setting additional measures for ResultProducer");
      

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
    
    throw new IllegalArgumentException("DatabaseResultProducer: Can't return value for : " + additionalMeasureName + ". " + m_ResultProducer.getClass().getName() + " " + "is not an AdditionalMeasureProducer");
  }
  










  public void setInstances(Instances instances)
  {
    m_Instances = instances;
  }
  






  public void setResultListener(ResultListener listener)
  {
    m_ResultListener = listener;
  }
  





  public String resultProducerTipText()
  {
    return "Set the result producer to use. If some results are not found in the source database then this result producer is used to generate them.";
  }
  







  public ResultProducer getResultProducer()
  {
    return m_ResultProducer;
  }
  





  public void setResultProducer(ResultProducer newResultProducer)
  {
    m_ResultProducer = newResultProducer;
  }
  






  public String toString()
  {
    String result = "DatabaseResultProducer: ";
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
