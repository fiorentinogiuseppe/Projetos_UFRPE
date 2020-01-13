package weka.core.converters;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.SingleIndex;
import weka.core.Utils;






















































public class SVMLightSaver
  extends AbstractFileSaver
  implements BatchConverter, IncrementalConverter
{
  private static final long serialVersionUID = 2605714599263995835L;
  public static String FILE_EXTENSION = SVMLightLoader.FILE_EXTENSION;
  

  public static int MAX_DIGITS = 18;
  

  protected SingleIndex m_ClassIndex = new SingleIndex("last");
  


  public SVMLightSaver()
  {
    resetOptions();
  }
  





  public String globalInfo()
  {
    return "Writes to a destination that is in svm light format.\n\nFor more information about svm light see:\n\nhttp://svmlight.joachims.org/";
  }
  









  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tThe class index\n\t(default: last)", "c", 1, "-c <class index>"));
    




    return result.elements();
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-c");
    result.add(getClassIndex());
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





















  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('c', options);
    if (tmpStr.length() != 0) {
      setClassIndex(tmpStr);
    } else {
      setClassIndex("last");
    }
  }
  



  public String getFileDescription()
  {
    return "svm light data files";
  }
  


  public void resetOptions()
  {
    super.resetOptions();
    setFileExtension(SVMLightLoader.FILE_EXTENSION);
  }
  





  public String classIndexTipText()
  {
    return "Sets the class index (\"first\" and \"last\" are valid values)";
  }
  




  public String getClassIndex()
  {
    return m_ClassIndex.getSingleIndex();
  }
  




  public void setClassIndex(String value)
  {
    m_ClassIndex.setSingleIndex(value);
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    

    result.enable(Capabilities.Capability.BINARY_CLASS);
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    
    return result;
  }
  




  public void setInstances(Instances instances)
  {
    m_ClassIndex.setUpper(instances.numAttributes() - 1);
    instances.setClassIndex(m_ClassIndex.getIndex());
    
    super.setInstances(instances);
  }
  








  protected String instanceToSvmlight(Instance inst)
  {
    StringBuffer result = new StringBuffer();
    

    if (inst.classAttribute().isNominal()) {
      if (inst.classValue() == 0.0D) {
        result.append("1");
      } else if (inst.classValue() == 1.0D) {
        result.append("-1");
      }
    } else {
      result.append("" + Utils.doubleToString(inst.classValue(), MAX_DIGITS));
    }
    

    for (int i = 0; i < inst.numAttributes(); i++) {
      if (i != inst.classIndex())
      {
        if (inst.value(i) != 0.0D)
        {
          result.append(" " + (i + 1) + ":" + Utils.doubleToString(inst.value(i), MAX_DIGITS)); }
      }
    }
    return result.toString();
  }
  






  public void writeIncremental(Instance inst)
    throws IOException
  {
    int writeMode = getWriteMode();
    Instances structure = getInstances();
    PrintWriter outW = null;
    
    if ((getRetrieval() == 1) || (getRetrieval() == 0)) {
      throw new IOException("Batch and incremental saving cannot be mixed.");
    }
    if (getWriter() != null) {
      outW = new PrintWriter(getWriter());
    }
    if (writeMode == 1) {
      if (structure == null) {
        setWriteMode(2);
        if (inst != null) {
          System.err.println("Structure (Header Information) has to be set in advance");
        }
      } else {
        setWriteMode(3);
      }
      writeMode = getWriteMode();
    }
    
    if (writeMode == 2) {
      if (outW != null)
        outW.close();
      cancel();
    }
    

    if (writeMode == 3) {
      setWriteMode(0);
      
      writeMode = getWriteMode();
    }
    

    if (writeMode == 0) {
      if (structure == null) {
        throw new IOException("No instances information available.");
      }
      if (inst != null)
      {
        if ((retrieveFile() == null) && (outW == null)) {
          System.out.println(instanceToSvmlight(inst));
        }
        else {
          outW.println(instanceToSvmlight(inst));
          m_incrementalCounter += 1;
          
          if (m_incrementalCounter > 100) {
            m_incrementalCounter = 0;
            outW.flush();
          }
        }
      }
      else
      {
        if (outW != null) {
          outW.flush();
          outW.close();
        }
        m_incrementalCounter = 0;
        resetStructure();
        outW = null;
        resetWriter();
      }
    }
  }
  




  public void writeBatch()
    throws IOException
  {
    if (getInstances() == null) {
      throw new IOException("No instances to save");
    }
    if (getRetrieval() == 2) {
      throw new IOException("Batch and incremental saving cannot be mixed.");
    }
    setRetrieval(1);
    setWriteMode(0);
    
    if ((retrieveFile() == null) && (getWriter() == null)) {
      for (int i = 0; i < getInstances().numInstances(); i++)
        System.out.println(instanceToSvmlight(getInstances().instance(i)));
      setWriteMode(1);
    }
    else {
      PrintWriter outW = new PrintWriter(getWriter());
      for (int i = 0; i < getInstances().numInstances(); i++)
        outW.println(instanceToSvmlight(getInstances().instance(i)));
      outW.flush();
      outW.close();
      setWriteMode(1);
      outW = null;
      resetWriter();
      setWriteMode(2);
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7950 $");
  }
  




  public static void main(String[] args)
  {
    runFileSaver(new SVMLightSaver(), args);
  }
}
