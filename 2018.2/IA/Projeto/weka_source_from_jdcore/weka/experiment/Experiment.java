package weka.experiment;

import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.DefaultListModel;
import weka.core.AdditionalMeasureProducer;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.ConverterUtils;
import weka.core.xml.KOML;
import weka.core.xml.XMLOptions;
import weka.experiment.xml.XMLExperiment;



























































































































public class Experiment
  implements Serializable, OptionHandler, RevisionHandler
{
  static final long serialVersionUID = 44945596742646663L;
  public static String FILE_EXTENSION = ".exp";
  

  protected ResultListener m_ResultListener = new InstancesResultListener();
  

  protected ResultProducer m_ResultProducer = new RandomSplitResultProducer();
  

  protected int m_RunLower = 1;
  

  protected int m_RunUpper = 10;
  

  protected DefaultListModel m_Datasets = new DefaultListModel();
  

  protected boolean m_UsePropertyIterator = false;
  

  protected PropertyNode[] m_PropertyPath;
  

  protected Object m_PropertyArray;
  

  protected String m_Notes = "";
  



  protected String[] m_AdditionalMeasures = null;
  


  protected boolean m_ClassFirst = false;
  


  protected boolean m_AdvanceDataSetFirst = true;
  
  protected transient int m_RunNumber;
  
  protected transient int m_DatasetNumber;
  protected transient int m_PropertyNumber;
  
  public Experiment() {}
  
  public void classFirst(boolean flag)
  {
    m_ClassFirst = flag;
  }
  





  public boolean getAdvanceDataSetFirst()
  {
    return m_AdvanceDataSetFirst;
  }
  





  public void setAdvanceDataSetFirst(boolean newAdvanceDataSetFirst)
  {
    m_AdvanceDataSetFirst = newAdvanceDataSetFirst;
  }
  





  public boolean getUsePropertyIterator()
  {
    return m_UsePropertyIterator;
  }
  





  public void setUsePropertyIterator(boolean newUsePropertyIterator)
  {
    m_UsePropertyIterator = newUsePropertyIterator;
  }
  






  public PropertyNode[] getPropertyPath()
  {
    return m_PropertyPath;
  }
  






  public void setPropertyPath(PropertyNode[] newPropertyPath)
  {
    m_PropertyPath = newPropertyPath;
  }
  






  public void setPropertyArray(Object newPropArray)
  {
    m_PropertyArray = newPropArray;
  }
  






  public Object getPropertyArray()
  {
    return m_PropertyArray;
  }
  






  public int getPropertyArrayLength()
  {
    return Array.getLength(m_PropertyArray);
  }
  






  public Object getPropertyArrayValue(int index)
  {
    return Array.get(m_PropertyArray, index);
  }
  










  protected transient boolean m_Finished = true;
  

  protected transient Instances m_CurrentInstances;
  

  protected transient int m_CurrentProperty;
  


  public int getCurrentRunNumber()
  {
    return m_RunNumber;
  }
  




  public int getCurrentDatasetNumber()
  {
    return m_DatasetNumber;
  }
  





  public int getCurrentPropertyNumber()
  {
    return m_PropertyNumber;
  }
  





  public void initialize()
    throws Exception
  {
    m_RunNumber = getRunLower();
    m_DatasetNumber = 0;
    m_PropertyNumber = 0;
    m_CurrentProperty = -1;
    m_CurrentInstances = null;
    m_Finished = false;
    if ((m_UsePropertyIterator) && (m_PropertyArray == null)) {
      throw new Exception("Null array for property iterator");
    }
    if (getRunLower() > getRunUpper()) {
      throw new Exception("Lower run number is greater than upper run number");
    }
    if (getDatasets().size() == 0) {
      throw new Exception("No datasets have been specified");
    }
    if (m_ResultProducer == null) {
      throw new Exception("No ResultProducer set");
    }
    if (m_ResultListener == null) {
      throw new Exception("No ResultListener set");
    }
    

    determineAdditionalResultMeasures();
    

    m_ResultProducer.setResultListener(m_ResultListener);
    m_ResultProducer.setAdditionalMeasures(m_AdditionalMeasures);
    m_ResultProducer.preProcess();
    


    String[] columnConstraints = m_ResultListener.determineColumnConstraints(m_ResultProducer);
    

    if (columnConstraints != null) {
      m_ResultProducer.setAdditionalMeasures(columnConstraints);
    }
  }
  





  private void determineAdditionalResultMeasures()
    throws Exception
  {
    m_AdditionalMeasures = null;
    FastVector measureNames = new FastVector();
    

    if ((m_ResultProducer instanceof AdditionalMeasureProducer)) {
      Enumeration am = ((AdditionalMeasureProducer)m_ResultProducer).enumerateMeasures();
      
      while (am.hasMoreElements()) {
        String mname = (String)am.nextElement();
        if (mname.startsWith("measure")) {
          if (measureNames.indexOf(mname) == -1) {
            measureNames.addElement(mname);
          }
        } else {
          throw new Exception("Additional measures in " + m_ResultProducer.getClass().getName() + " must obey the naming convention" + " of starting with \"measure\"");
        }
      }
    }
    



    if ((m_UsePropertyIterator) && (m_PropertyArray != null)) {
      for (int i = 0; i < Array.getLength(m_PropertyArray); i++) {
        Object current = Array.get(m_PropertyArray, i);
        
        if ((current instanceof AdditionalMeasureProducer)) {
          Enumeration am = ((AdditionalMeasureProducer)current).enumerateMeasures();
          
          while (am.hasMoreElements()) {
            String mname = (String)am.nextElement();
            if (mname.startsWith("measure")) {
              if (measureNames.indexOf(mname) == -1) {
                measureNames.addElement(mname);
              }
            } else {
              throw new Exception("Additional measures in " + current.getClass().getName() + " must obey the naming convention" + " of starting with \"measure\"");
            }
          }
        }
      }
    }
    


    if (measureNames.size() > 0) {
      m_AdditionalMeasures = new String[measureNames.size()];
      for (int i = 0; i < measureNames.size(); i++) {
        m_AdditionalMeasures[i] = ((String)measureNames.elementAt(i));
      }
    }
  }
  









  protected void setProperty(int propertyDepth, Object origValue)
    throws Exception
  {
    PropertyDescriptor current = m_PropertyPath[propertyDepth].property;
    Object subVal = null;
    if (propertyDepth < m_PropertyPath.length - 1) {
      Method getter = current.getReadMethod();
      Object[] getArgs = new Object[0];
      subVal = getter.invoke(origValue, getArgs);
      setProperty(propertyDepth + 1, subVal);
    } else {
      subVal = Array.get(m_PropertyArray, m_PropertyNumber);
    }
    Method setter = current.getWriteMethod();
    Object[] args = { subVal };
    setter.invoke(origValue, args);
  }
  





  public boolean hasMoreIterations()
  {
    return !m_Finished;
  }
  




  public void nextIteration()
    throws Exception
  {
    if ((m_UsePropertyIterator) && 
      (m_CurrentProperty != m_PropertyNumber)) {
      setProperty(0, m_ResultProducer);
      m_CurrentProperty = m_PropertyNumber;
    }
    

    if (m_CurrentInstances == null) {
      File currentFile = (File)getDatasets().elementAt(m_DatasetNumber);
      AbstractFileLoader loader = ConverterUtils.getLoaderForFile(currentFile);
      loader.setFile(currentFile);
      Instances data = new Instances(loader.getDataSet());
      
      if (data.classIndex() == -1) {
        if (m_ClassFirst) {
          data.setClassIndex(0);
        } else {
          data.setClassIndex(data.numAttributes() - 1);
        }
      }
      m_CurrentInstances = data;
      m_ResultProducer.setInstances(m_CurrentInstances);
    }
    
    m_ResultProducer.doRun(m_RunNumber);
    
    advanceCounters();
  }
  



  public void advanceCounters()
  {
    if (m_AdvanceDataSetFirst) {
      m_RunNumber += 1;
      if (m_RunNumber > getRunUpper()) {
        m_RunNumber = getRunLower();
        m_DatasetNumber += 1;
        m_CurrentInstances = null;
        if (m_DatasetNumber >= getDatasets().size()) {
          m_DatasetNumber = 0;
          if (m_UsePropertyIterator) {
            m_PropertyNumber += 1;
            if (m_PropertyNumber >= Array.getLength(m_PropertyArray)) {
              m_Finished = true;
            }
          } else {
            m_Finished = true;
          }
        }
      }
    } else {
      m_RunNumber += 1;
      if (m_RunNumber > getRunUpper()) {
        m_RunNumber = getRunLower();
        if (m_UsePropertyIterator) {
          m_PropertyNumber += 1;
          if (m_PropertyNumber >= Array.getLength(m_PropertyArray)) {
            m_PropertyNumber = 0;
            m_DatasetNumber += 1;
            m_CurrentInstances = null;
            if (m_DatasetNumber >= getDatasets().size()) {
              m_Finished = true;
            }
          }
        } else {
          m_DatasetNumber += 1;
          m_CurrentInstances = null;
          if (m_DatasetNumber >= getDatasets().size()) {
            m_Finished = true;
          }
        }
      }
    }
  }
  



  public void runExperiment()
  {
    while (hasMoreIterations()) {
      try {
        nextIteration();
      } catch (Exception ex) {
        ex.printStackTrace();
        System.err.println(ex.getMessage());
        advanceCounters();
      }
    }
  }
  





  public void postProcess()
    throws Exception
  {
    m_ResultProducer.postProcess();
  }
  




  public DefaultListModel getDatasets()
  {
    return m_Datasets;
  }
  



  public void setDatasets(DefaultListModel ds)
  {
    m_Datasets = ds;
  }
  





  public ResultListener getResultListener()
  {
    return m_ResultListener;
  }
  





  public void setResultListener(ResultListener newResultListener)
  {
    m_ResultListener = newResultListener;
  }
  





  public ResultProducer getResultProducer()
  {
    return m_ResultProducer;
  }
  






  public void setResultProducer(ResultProducer newResultProducer)
  {
    m_ResultProducer = newResultProducer;
  }
  





  public int getRunUpper()
  {
    return m_RunUpper;
  }
  





  public void setRunUpper(int newRunUpper)
  {
    m_RunUpper = newRunUpper;
  }
  





  public int getRunLower()
  {
    return m_RunLower;
  }
  





  public void setRunLower(int newRunLower)
  {
    m_RunLower = newRunLower;
  }
  






  public String getNotes()
  {
    return m_Notes;
  }
  





  public void setNotes(String newNotes)
  {
    m_Notes = newNotes;
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(6);
    
    newVector.addElement(new Option("\tThe lower run number to start the experiment from.\n\t(default 1)", "L", 1, "-L <num>"));
    



    newVector.addElement(new Option("\tThe upper run number to end the experiment at (inclusive).\n\t(default 10)", "U", 1, "-U <num>"));
    



    newVector.addElement(new Option("\tThe dataset to run the experiment on.\n\t(required, may be specified multiple times)", "T", 1, "-T <arff file>"));
    



    newVector.addElement(new Option("\tThe full class name of a ResultProducer (required).\n\teg: weka.experiment.RandomSplitResultProducer", "P", 1, "-P <class name>"));
    



    newVector.addElement(new Option("\tThe full class name of a ResultListener (required).\n\teg: weka.experiment.CSVResultListener", "D", 1, "-D <class name>"));
    



    newVector.addElement(new Option("\tA string containing any notes about the experiment.\n\t(default none)", "N", 1, "-N <string>"));
    




    if ((m_ResultProducer != null) && ((m_ResultProducer instanceof OptionHandler)))
    {
      newVector.addElement(new Option("", "", 0, "\nOptions specific to result producer " + m_ResultProducer.getClass().getName() + ":"));
      


      Enumeration enm = ((OptionHandler)m_ResultProducer).listOptions();
      while (enm.hasMoreElements()) {
        newVector.addElement(enm.nextElement());
      }
    }
    return newVector.elements();
  }
  




























































































  public void setOptions(String[] options)
    throws Exception
  {
    String lowerString = Utils.getOption('L', options);
    if (lowerString.length() != 0) {
      setRunLower(Integer.parseInt(lowerString));
    } else {
      setRunLower(1);
    }
    String upperString = Utils.getOption('U', options);
    if (upperString.length() != 0) {
      setRunUpper(Integer.parseInt(upperString));
    } else {
      setRunUpper(10);
    }
    if (getRunLower() > getRunUpper()) {
      throw new Exception("Lower (" + getRunLower() + ") is greater than upper (" + getRunUpper() + ")");
    }
    


    setNotes(Utils.getOption('N', options));
    
    getDatasets().removeAllElements();
    String dataName;
    do {
      dataName = Utils.getOption('T', options);
      if (dataName.length() != 0) {
        File dataset = new File(dataName);
        getDatasets().addElement(dataset);
      }
    } while (dataName.length() != 0);
    if (getDatasets().size() == 0) {
      throw new Exception("Required: -T <arff file name>");
    }
    
    String rlName = Utils.getOption('D', options);
    if (rlName.length() == 0) {
      throw new Exception("Required: -D <ResultListener class name>");
    }
    rlName = rlName.trim();
    
    int breakLoc = rlName.indexOf(' ');
    String clName = rlName;
    String rlOptionsString = "";
    String[] rlOptions = null;
    if (breakLoc != -1) {
      clName = rlName.substring(0, breakLoc);
      rlOptionsString = rlName.substring(breakLoc).trim();
      rlOptions = Utils.splitOptions(rlOptionsString);
    }
    setResultListener((ResultListener)Utils.forName(ResultListener.class, clName, rlOptions));
    

    String rpName = Utils.getOption('P', options);
    if (rpName.length() == 0) {
      throw new Exception("Required: -P <ResultProducer class name>");
    }
    




    setResultProducer((ResultProducer)Utils.forName(ResultProducer.class, rpName, Utils.partitionOptions(options)));
  }
  














  public String[] getOptions()
  {
    m_UsePropertyIterator = false;
    m_PropertyPath = null;
    m_PropertyArray = null;
    
    String[] rpOptions = new String[0];
    if ((m_ResultProducer != null) && ((m_ResultProducer instanceof OptionHandler)))
    {
      rpOptions = ((OptionHandler)m_ResultProducer).getOptions();
    }
    
    String[] options = new String[rpOptions.length + getDatasets().size() * 2 + 11];
    

    int current = 0;
    
    options[(current++)] = "-L";options[(current++)] = ("" + getRunLower());
    options[(current++)] = "-U";options[(current++)] = ("" + getRunUpper());
    if (getDatasets().size() != 0) {
      for (int i = 0; i < getDatasets().size(); i++) {
        options[(current++)] = "-T";
        options[(current++)] = getDatasets().elementAt(i).toString();
      }
    }
    if (getResultListener() != null) {
      options[(current++)] = "-D";
      options[(current++)] = getResultListener().getClass().getName();
    }
    if (getResultProducer() != null) {
      options[(current++)] = "-P";
      options[(current++)] = getResultProducer().getClass().getName();
    }
    if (!getNotes().equals("")) {
      options[(current++)] = "-N";options[(current++)] = getNotes();
    }
    options[(current++)] = "--";
    
    System.arraycopy(rpOptions, 0, options, current, rpOptions.length);
    
    current += rpOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  





  public String toString()
  {
    String result = "Runs from: " + m_RunLower + " to: " + m_RunUpper + '\n';
    result = result + "Datasets:";
    for (int i = 0; i < m_Datasets.size(); i++) {
      result = result + " " + m_Datasets.elementAt(i);
    }
    result = result + '\n';
    result = result + "Custom property iterator: " + (m_UsePropertyIterator ? "on" : "off") + "\n";
    

    if (m_UsePropertyIterator) {
      if (m_PropertyPath == null) {
        throw new Error("*** null propertyPath ***");
      }
      if (m_PropertyArray == null) {
        throw new Error("*** null propertyArray ***");
      }
      if (m_PropertyPath.length > 1) {
        result = result + "Custom property path:\n";
        for (int i = 0; i < m_PropertyPath.length - 1; i++) {
          PropertyNode pn = m_PropertyPath[i];
          result = result + "" + (i + 1) + "  " + parentClass.getName() + "::" + pn.toString() + ' ' + value.toString() + '\n';
        }
      }
      

      result = result + "Custom property name:" + m_PropertyPath[(m_PropertyPath.length - 1)].toString() + '\n';
      
      result = result + "Custom property values:\n";
      for (int i = 0; i < Array.getLength(m_PropertyArray); i++) {
        Object current = Array.get(m_PropertyArray, i);
        result = result + " " + (i + 1) + " " + current.getClass().getName() + " " + current.toString() + '\n';
      }
    }
    

    result = result + "ResultProducer: " + m_ResultProducer + '\n';
    result = result + "ResultListener: " + m_ResultListener + '\n';
    if (!getNotes().equals("")) {
      result = result + "Notes: " + getNotes();
    }
    return result;
  }
  


  public static Experiment read(String filename)
    throws Exception
  {
    Experiment result;
    

    Experiment result;
    

    if ((KOML.isPresent()) && (filename.toLowerCase().endsWith(".koml"))) {
      result = (Experiment)KOML.read(filename);
    } else {
      Experiment result;
      if (filename.toLowerCase().endsWith(".xml")) {
        XMLExperiment xml = new XMLExperiment();
        result = (Experiment)xml.read(filename);
      }
      else
      {
        FileInputStream fi = new FileInputStream(filename);
        ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(fi));
        
        result = (Experiment)oi.readObject();
        oi.close();
      }
    }
    return result;
  }
  






  public static void write(String filename, Experiment exp)
    throws Exception
  {
    if ((KOML.isPresent()) && (filename.toLowerCase().endsWith(".koml"))) {
      KOML.write(filename, exp);

    }
    else if (filename.toLowerCase().endsWith(".xml")) {
      XMLExperiment xml = new XMLExperiment();
      xml.write(filename, exp);
    }
    else
    {
      FileOutputStream fo = new FileOutputStream(filename);
      ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(fo));
      
      oo.writeObject(exp);
      oo.close();
    }
  }
  




  public static void main(String[] args)
  {
    try
    {
      Experiment exp = null;
      
      String xmlOption = Utils.getOption("xml", args);
      if (!xmlOption.equals("")) {
        args = new XMLOptions(xmlOption).toArray();
      }
      String expFile = Utils.getOption('l', args);
      String saveFile = Utils.getOption('s', args);
      boolean runExp = Utils.getFlag('r', args);
      if (expFile.length() == 0) {
        exp = new Experiment();
        try {
          exp.setOptions(args);
          Utils.checkForRemainingOptions(args);
        } catch (Exception ex) {
          ex.printStackTrace();
          String result = "Usage:\n\n-l <exp|xml file>\n\tLoad experiment from file (default use cli options).\n\tThe type is determined, based on the extension (" + FILE_EXTENSION + " or .xml)\n" + "-s <exp|xml file>\n" + "\tSave experiment to file after setting other options.\n" + "\tThe type is determined, based on the extension (" + FILE_EXTENSION + " or .xml)\n" + "\t(default don't save)\n" + "-r\n" + "\tRun experiment (default don't run)\n" + "-xml <filename | xml-string>\n" + "\tget options from XML-Data instead from parameters\n" + "\n";
          













          Enumeration enm = exp.listOptions();
          while (enm.hasMoreElements()) {
            Option option = (Option)enm.nextElement();
            result = result + option.synopsis() + "\n";
            result = result + option.description() + "\n";
          }
          throw new Exception(result + "\n" + ex.getMessage());
        }
      } else {
        exp = read(expFile);
        
        String dataName;
        do
        {
          dataName = Utils.getOption('T', args);
          if (dataName.length() != 0) {
            File dataset = new File(dataName);
            exp.getDatasets().addElement(dataset);
          }
        } while (dataName.length() != 0);
      }
      
      System.err.println("Experiment:\n" + exp.toString());
      
      if (saveFile.length() != 0) {
        write(saveFile, exp);
      }
      if (runExp) {
        System.err.println("Initializing...");
        exp.initialize();
        System.err.println("Iterating...");
        exp.runExperiment();
        System.err.println("Postprocessing...");
        exp.postProcess();
      }
    }
    catch (Exception ex) {
      System.err.println(ex.getMessage());
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5401 $");
  }
}
