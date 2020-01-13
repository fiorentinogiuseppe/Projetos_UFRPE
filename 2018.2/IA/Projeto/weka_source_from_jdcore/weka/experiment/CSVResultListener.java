package weka.experiment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;















































public class CSVResultListener
  implements ResultListener, OptionHandler, RevisionHandler
{
  static final long serialVersionUID = -623185072785174658L;
  protected ResultProducer m_RP;
  protected File m_OutputFile = null;
  

  protected String m_OutputFileName = "";
  

  protected transient PrintWriter m_Out = new PrintWriter(System.out, true);
  

  public CSVResultListener()
  {
    File resultsFile;
    
    try
    {
      resultsFile = File.createTempFile("weka_experiment", ".csv");
      resultsFile.deleteOnExit();
    } catch (Exception e) {
      System.err.println("Cannot create temp file, writing to standard out.");
      resultsFile = new File("-");
    }
    setOutputFile(resultsFile);
    setOutputFileName("");
  }
  




  public String globalInfo()
  {
    return "Takes results from a result producer and assembles them into comma separated value form.";
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    
    newVector.addElement(new Option("\tThe filename where output will be stored. Use - for stdout.\n\t(default temp file)", "O", 1, "-O <file name>"));
    




    return newVector.elements();
  }
  














  public void setOptions(String[] options)
    throws Exception
  {
    String fName = Utils.getOption('O', options);
    if (fName.length() != 0) {
      setOutputFile(new File(fName));
    } else {
      File resultsFile;
      try {
        resultsFile = File.createTempFile("weka_experiment", null);
        resultsFile.deleteOnExit();
      } catch (Exception e) {
        System.err.println("Cannot create temp file, writing to standard out.");
        resultsFile = new File("-");
      }
      setOutputFile(resultsFile);
      setOutputFileName("");
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[2];
    int current = 0;
    
    options[(current++)] = "-O";
    options[(current++)] = getOutputFile().getName();
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String outputFileTipText()
  {
    return "File to save to. Use '-' to write to standard out.";
  }
  





  public File getOutputFile()
  {
    return m_OutputFile;
  }
  






  public void setOutputFile(File newOutputFile)
  {
    m_OutputFile = newOutputFile;
    setOutputFileName(newOutputFile.getName());
  }
  





  public String outputFileName()
  {
    return m_OutputFileName;
  }
  






  public void setOutputFileName(String name)
  {
    m_OutputFileName = name;
  }
  





  public void preProcess(ResultProducer rp)
    throws Exception
  {
    m_RP = rp;
    if ((m_OutputFile == null) || (m_OutputFile.getName().equals("-"))) {
      m_Out = new PrintWriter(System.out, true);
    } else {
      m_Out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(m_OutputFile)), true);
    }
    

    printResultNames(m_RP);
  }
  







  public void postProcess(ResultProducer rp)
    throws Exception
  {
    if ((m_OutputFile != null) && (!m_OutputFile.getName().equals("-"))) {
      m_Out.close();
    }
  }
  









  public String[] determineColumnConstraints(ResultProducer rp)
    throws Exception
  {
    return null;
  }
  








  public void acceptResult(ResultProducer rp, Object[] key, Object[] result)
    throws Exception
  {
    if (m_RP != rp) {
      throw new Error("Unrecognized ResultProducer sending results!!");
    }
    for (int i = 0; i < key.length; i++) {
      if (i != 0) {
        m_Out.print(',');
      }
      if (key[i] == null) {
        m_Out.print("?");
      } else {
        m_Out.print(Utils.quote(key[i].toString()));
      }
    }
    for (int i = 0; i < result.length; i++) {
      m_Out.print(',');
      if (result[i] == null) {
        m_Out.print("?");
      } else {
        m_Out.print(Utils.quote(result[i].toString()));
      }
    }
    m_Out.println("");
  }
  










  public boolean isResultRequired(ResultProducer rp, Object[] key)
    throws Exception
  {
    return true;
  }
  






  private void printResultNames(ResultProducer rp)
    throws Exception
  {
    String[] key = rp.getKeyNames();
    for (int i = 0; i < key.length; i++) {
      if (i != 0) {
        m_Out.print(',');
      }
      if (key[i] == null) {
        m_Out.print("?");
      } else {
        m_Out.print("Key_" + key[i].toString());
      }
    }
    String[] result = rp.getResultNames();
    for (int i = 0; i < result.length; i++) {
      m_Out.print(',');
      if (result[i] == null) {
        m_Out.print("?");
      } else {
        m_Out.print(result[i].toString());
      }
    }
    m_Out.println("");
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.13 $");
  }
}
