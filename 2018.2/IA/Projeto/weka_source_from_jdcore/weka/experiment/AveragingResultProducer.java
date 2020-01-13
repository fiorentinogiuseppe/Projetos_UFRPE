package weka.experiment;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import weka.core.AdditionalMeasureProducer;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;













































































































































public class AveragingResultProducer
  implements ResultListener, ResultProducer, OptionHandler, AdditionalMeasureProducer, RevisionHandler
{
  static final long serialVersionUID = 2551284958501991352L;
  protected Instances m_Instances;
  protected ResultListener m_ResultListener = new CSVResultListener();
  

  protected ResultProducer m_ResultProducer = new CrossValidationResultProducer();
  


  protected String[] m_AdditionalMeasures = null;
  

  protected int m_ExpectedResultsPerAverage = 10;
  



  protected boolean m_CalculateStdDevs;
  


  protected String m_CountFieldName = "Num_" + CrossValidationResultProducer.FOLD_FIELD_NAME;
  


  protected String m_KeyFieldName = CrossValidationResultProducer.FOLD_FIELD_NAME;
  


  protected int m_KeyIndex = -1;
  

  protected FastVector m_Keys = new FastVector();
  

  protected FastVector m_Results = new FastVector();
  


  public AveragingResultProducer() {}
  

  public String globalInfo()
  {
    return "Takes the results from a ResultProducer and submits the average to the result listener. Normally used with a CrossValidationResultProducer to perform n x m fold cross validation. For non-numeric result fields, the first value is used.";
  }
  










  protected int findKeyIndex()
  {
    m_KeyIndex = -1;
    try {
      if (m_ResultProducer != null) {
        String[] keyNames = m_ResultProducer.getKeyNames();
        for (int i = 0; i < keyNames.length; i++) {
          if (keyNames[i].equals(m_KeyFieldName)) {
            m_KeyIndex = i;
            break;
          }
        }
      }
    }
    catch (Exception ex) {}
    return m_KeyIndex;
  }
  











  public String[] determineColumnConstraints(ResultProducer rp)
    throws Exception
  {
    return null;
  }
  







  protected Object[] determineTemplate(int run)
    throws Exception
  {
    if (m_Instances == null) {
      throw new Exception("No Instances set");
    }
    m_ResultProducer.setInstances(m_Instances);
    

    m_Keys.removeAllElements();
    m_Results.removeAllElements();
    
    m_ResultProducer.doRunKeys(run);
    checkForMultipleDifferences();
    
    Object[] template = (Object[])((Object[])m_Keys.elementAt(0)).clone();
    template[m_KeyIndex] = null;
    
    checkForDuplicateKeys(template);
    
    return template;
  }
  









  public void doRunKeys(int run)
    throws Exception
  {
    Object[] template = determineTemplate(run);
    String[] newKey = new String[template.length - 1];
    System.arraycopy(template, 0, newKey, 0, m_KeyIndex);
    System.arraycopy(template, m_KeyIndex + 1, newKey, m_KeyIndex, template.length - m_KeyIndex - 1);
    

    m_ResultListener.acceptResult(this, newKey, null);
  }
  









  public void doRun(int run)
    throws Exception
  {
    Object[] template = determineTemplate(run);
    String[] newKey = new String[template.length - 1];
    System.arraycopy(template, 0, newKey, 0, m_KeyIndex);
    System.arraycopy(template, m_KeyIndex + 1, newKey, m_KeyIndex, template.length - m_KeyIndex - 1);
    


    if (m_ResultListener.isResultRequired(this, newKey))
    {
      m_Keys.removeAllElements();
      m_Results.removeAllElements();
      
      m_ResultProducer.doRun(run);
      




      checkForMultipleDifferences();
      
      template = (Object[])((Object[])m_Keys.elementAt(0)).clone();
      template[m_KeyIndex] = null;
      
      checkForDuplicateKeys(template);
      
      doAverageResult(template);
    }
  }
  









  protected boolean matchesTemplate(Object[] template, Object[] test)
  {
    if (template.length != test.length) {
      return false;
    }
    for (int i = 0; i < test.length; i++) {
      if ((template[i] != null) && (!template[i].equals(test[i]))) {
        return false;
      }
    }
    return true;
  }
  








  protected void doAverageResult(Object[] template)
    throws Exception
  {
    String[] newKey = new String[template.length - 1];
    System.arraycopy(template, 0, newKey, 0, m_KeyIndex);
    System.arraycopy(template, m_KeyIndex + 1, newKey, m_KeyIndex, template.length - m_KeyIndex - 1);
    

    if (m_ResultListener.isResultRequired(this, newKey)) {
      Object[] resultTypes = m_ResultProducer.getResultTypes();
      Stats[] stats = new Stats[resultTypes.length];
      for (int i = 0; i < stats.length; i++) {
        stats[i] = new Stats();
      }
      Object[] result = getResultTypes();
      int numMatches = 0;
      for (int i = 0; i < m_Keys.size(); i++) {
        Object[] currentKey = (Object[])m_Keys.elementAt(i);
        
        if (matchesTemplate(template, currentKey))
        {


          Object[] currentResult = (Object[])m_Results.elementAt(i);
          numMatches++;
          for (int j = 0; j < resultTypes.length; j++)
            if ((resultTypes[j] instanceof Double)) {
              if (currentResult[j] == null)
              {



                if (stats[j] != null) {
                  stats[j] = null;
                }
              }
              





              if (stats[j] != null) {
                double currentVal = ((Double)currentResult[j]).doubleValue();
                stats[j].add(currentVal);
              }
            }
        }
      }
      if (numMatches != m_ExpectedResultsPerAverage) {
        throw new Exception("Expected " + m_ExpectedResultsPerAverage + " results matching key \"" + DatabaseUtils.arrayToString(template) + "\" but got " + numMatches);
      }
      



      result[0] = new Double(numMatches);
      Object[] currentResult = (Object[])m_Results.elementAt(0);
      int k = 1;
      for (int j = 0; j < resultTypes.length; j++) {
        if ((resultTypes[j] instanceof Double)) {
          if (stats[j] != null) {
            stats[j].calculateDerived();
            result[(k++)] = new Double(mean);
          } else {
            result[(k++)] = null;
          }
          if (getCalculateStdDevs()) {
            if (stats[j] != null) {
              result[(k++)] = new Double(stdDev);
            } else {
              result[(k++)] = null;
            }
          }
        } else {
          result[(k++)] = currentResult[j];
        }
      }
      m_ResultListener.acceptResult(this, newKey, result);
    }
  }
  






  protected void checkForDuplicateKeys(Object[] template)
    throws Exception
  {
    Hashtable hash = new Hashtable();
    int numMatches = 0;
    for (int i = 0; i < m_Keys.size(); i++) {
      Object[] current = (Object[])m_Keys.elementAt(i);
      
      if (matchesTemplate(template, current))
      {

        if (hash.containsKey(current[m_KeyIndex])) {
          throw new Exception("Duplicate result received:" + DatabaseUtils.arrayToString(current));
        }
        
        numMatches++;
        hash.put(current[m_KeyIndex], current[m_KeyIndex]);
      } }
    if (numMatches != m_ExpectedResultsPerAverage) {
      throw new Exception("Expected " + m_ExpectedResultsPerAverage + " results matching key \"" + DatabaseUtils.arrayToString(template) + "\" but got " + numMatches);
    }
  }
  












  protected void checkForMultipleDifferences()
    throws Exception
  {
    Object[] firstKey = (Object[])m_Keys.elementAt(0);
    Object[] lastKey = (Object[])m_Keys.elementAt(m_Keys.size() - 1);
    



    for (int i = 0; i < firstKey.length; i++) {
      if ((i != m_KeyIndex) && (!firstKey[i].equals(lastKey[i]))) {
        throw new Exception("Keys differ on fields other than \"" + m_KeyFieldName + "\" -- time to implement multiple averaging");
      }
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
    findKeyIndex();
    if (m_KeyIndex == -1) {
      throw new Exception("No key field called " + m_KeyFieldName + " produced by " + m_ResultProducer.getClass().getName());
    }
    

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
    m_Keys.addElement(key);
    m_Results.addElement(result);
  }
  










  public boolean isResultRequired(ResultProducer rp, Object[] key)
    throws Exception
  {
    if (m_ResultProducer != rp) {
      throw new Error("Unrecognized ResultProducer sending results!!");
    }
    return true;
  }
  






  public String[] getKeyNames()
    throws Exception
  {
    if (m_KeyIndex == -1) {
      throw new Exception("No key field called " + m_KeyFieldName + " produced by " + m_ResultProducer.getClass().getName());
    }
    

    String[] keyNames = m_ResultProducer.getKeyNames();
    String[] newKeyNames = new String[keyNames.length - 1];
    System.arraycopy(keyNames, 0, newKeyNames, 0, m_KeyIndex);
    System.arraycopy(keyNames, m_KeyIndex + 1, newKeyNames, m_KeyIndex, keyNames.length - m_KeyIndex - 1);
    

    return newKeyNames;
  }
  









  public Object[] getKeyTypes()
    throws Exception
  {
    if (m_KeyIndex == -1) {
      throw new Exception("No key field called " + m_KeyFieldName + " produced by " + m_ResultProducer.getClass().getName());
    }
    

    Object[] keyTypes = m_ResultProducer.getKeyTypes();
    
    Object[] newKeyTypes = new String[keyTypes.length - 1];
    System.arraycopy(keyTypes, 0, newKeyTypes, 0, m_KeyIndex);
    System.arraycopy(keyTypes, m_KeyIndex + 1, newKeyTypes, m_KeyIndex, keyTypes.length - m_KeyIndex - 1);
    

    return newKeyTypes;
  }
  











  public String[] getResultNames()
    throws Exception
  {
    String[] resultNames = m_ResultProducer.getResultNames();
    
    if (getCalculateStdDevs()) {
      Object[] resultTypes = m_ResultProducer.getResultTypes();
      int numNumeric = 0;
      for (Object resultType : resultTypes) {
        if ((resultType instanceof Double)) {
          numNumeric++;
        }
      }
      String[] newResultNames = new String[resultNames.length + 1 + numNumeric];
      
      newResultNames[0] = m_CountFieldName;
      int j = 1;
      for (int i = 0; i < resultNames.length; i++) {
        newResultNames[(j++)] = ("Avg_" + resultNames[i]);
        if ((resultTypes[i] instanceof Double)) {
          newResultNames[(j++)] = ("Dev_" + resultNames[i]);
        }
      }
      return newResultNames;
    }
    String[] newResultNames = new String[resultNames.length + 1];
    newResultNames[0] = m_CountFieldName;
    System.arraycopy(resultNames, 0, newResultNames, 1, resultNames.length);
    return newResultNames;
  }
  









  public Object[] getResultTypes()
    throws Exception
  {
    Object[] resultTypes = m_ResultProducer.getResultTypes();
    
    if (getCalculateStdDevs()) {
      int numNumeric = 0;
      for (Object resultType : resultTypes) {
        if ((resultType instanceof Double)) {
          numNumeric++;
        }
      }
      Object[] newResultTypes = new Object[resultTypes.length + 1 + numNumeric];
      
      newResultTypes[0] = new Double(0.0D);
      int j = 1;
      for (Object resultType : resultTypes) {
        newResultTypes[(j++)] = resultType;
        if ((resultType instanceof Double)) {
          newResultTypes[(j++)] = new Double(0.0D);
        }
      }
      return newResultTypes;
    }
    Object[] newResultTypes = new Object[resultTypes.length + 1];
    newResultTypes[0] = new Double(0.0D);
    System.arraycopy(resultTypes, 0, newResultTypes, 1, resultTypes.length);
    return newResultTypes;
  }
  















  public String getCompatibilityState()
  {
    String result = " -X " + getExpectedResultsPerAverage() + " ";
    
    if (getCalculateStdDevs()) {
      result = result + "-S ";
    }
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
    
    newVector.addElement(new Option("\tThe name of the field to average over.\n\t(default \"Fold\")", "F", 1, "-F <field name>"));
    



    newVector.addElement(new Option("\tThe number of results expected per average.\n\t(default 10)", "X", 1, "-X <num results>"));
    



    newVector.addElement(new Option("\tCalculate standard deviations.\n\t(default only averages)", "S", 0, "-S"));
    



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
    String keyFieldName = Utils.getOption('F', options);
    if (keyFieldName.length() != 0) {
      setKeyFieldName(keyFieldName);
    } else {
      setKeyFieldName(CrossValidationResultProducer.FOLD_FIELD_NAME);
    }
    
    String numResults = Utils.getOption('X', options);
    if (numResults.length() != 0) {
      setExpectedResultsPerAverage(Integer.parseInt(numResults));
    } else {
      setExpectedResultsPerAverage(10);
    }
    
    setCalculateStdDevs(Utils.getFlag('S', options));
    
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
    
    options[(current++)] = "-F";
    options[(current++)] = ("" + getKeyFieldName());
    options[(current++)] = "-X";
    options[(current++)] = ("" + getExpectedResultsPerAverage());
    if (getCalculateStdDevs()) {
      options[(current++)] = "-S";
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
      System.err.println("AveragingResultProducer: setting additional measures for ResultProducer");
      

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
    
    throw new IllegalArgumentException("AveragingResultProducer: Can't return value for : " + additionalMeasureName + ". " + m_ResultProducer.getClass().getName() + " " + "is not an AdditionalMeasureProducer");
  }
  










  public void setInstances(Instances instances)
  {
    m_Instances = instances;
  }
  





  public String calculateStdDevsTipText()
  {
    return "Record standard deviations for each run.";
  }
  





  public boolean getCalculateStdDevs()
  {
    return m_CalculateStdDevs;
  }
  





  public void setCalculateStdDevs(boolean newCalculateStdDevs)
  {
    m_CalculateStdDevs = newCalculateStdDevs;
  }
  





  public String expectedResultsPerAverageTipText()
  {
    return "Set the expected number of results to average per run. For example if a CrossValidationResultProducer is being used (with the number of folds set to 10), then the expected number of results per run is 10.";
  }
  








  public int getExpectedResultsPerAverage()
  {
    return m_ExpectedResultsPerAverage;
  }
  






  public void setExpectedResultsPerAverage(int newExpectedResultsPerAverage)
  {
    m_ExpectedResultsPerAverage = newExpectedResultsPerAverage;
  }
  





  public String keyFieldNameTipText()
  {
    return "Set the field name that will be unique for a run.";
  }
  





  public String getKeyFieldName()
  {
    return m_KeyFieldName;
  }
  





  public void setKeyFieldName(String newKeyFieldName)
  {
    m_KeyFieldName = newKeyFieldName;
    m_CountFieldName = ("Num_" + m_KeyFieldName);
    findKeyIndex();
  }
  






  public void setResultListener(ResultListener listener)
  {
    m_ResultListener = listener;
  }
  





  public String resultProducerTipText()
  {
    return "Set the resultProducer for which results are to be averaged.";
  }
  





  public ResultProducer getResultProducer()
  {
    return m_ResultProducer;
  }
  





  public void setResultProducer(ResultProducer newResultProducer)
  {
    m_ResultProducer = newResultProducer;
    m_ResultProducer.setResultListener(this);
    findKeyIndex();
  }
  






  public String toString()
  {
    String result = "AveragingResultProducer: ";
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
