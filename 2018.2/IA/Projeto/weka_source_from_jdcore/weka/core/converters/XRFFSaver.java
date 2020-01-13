package weka.core.converters;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.SingleIndex;
import weka.core.Utils;
import weka.core.xml.XMLInstances;























































public class XRFFSaver
  extends AbstractFileSaver
  implements BatchConverter
{
  private static final long serialVersionUID = -7226404765213522043L;
  protected SingleIndex m_ClassIndex = new SingleIndex();
  

  protected XMLInstances m_XMLInstances;
  

  protected boolean m_CompressOutput = false;
  


  public XRFFSaver()
  {
    resetOptions();
  }
  





  public String globalInfo()
  {
    return "Writes to a destination that is in the XML version of the ARFF format. The data can be compressed with gzip, in order to save space.";
  }
  








  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tThe class index (first and last are valid as well).\n\t(default: last)", "C", 1, "-C <class index>"));
    




    result.addElement(new Option("\tCompresses the data (uses '" + XRFFLoader.FILE_EXTENSION_COMPRESSED + "' as extension instead of '" + XRFFLoader.FILE_EXTENSION + "')\n" + "\t(default: off)", "compress", 0, "-compress"));
    







    return result.elements();
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getClassIndex().length() != 0) {
      result.add("-C");
      result.add(getClassIndex());
    }
    
    if (getCompressOutput()) {
      result.add("-compress");
    }
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  

























  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      setClassIndex(tmpStr);
    } else {
      setClassIndex("last");
    }
    setCompressOutput(Utils.getFlag("compress", options));
    
    super.setOptions(options);
  }
  




  public String getFileDescription()
  {
    return "XRFF data files";
  }
  




  public String[] getFileExtensions()
  {
    return new String[] { XRFFLoader.FILE_EXTENSION, XRFFLoader.FILE_EXTENSION_COMPRESSED };
  }
  




  public void setFile(File outputFile)
    throws IOException
  {
    if (outputFile.getAbsolutePath().endsWith(XRFFLoader.FILE_EXTENSION_COMPRESSED)) {
      setCompressOutput(true);
    }
    super.setFile(outputFile);
  }
  


  public void resetOptions()
  {
    super.resetOptions();
    
    if (getCompressOutput()) {
      setFileExtension(XRFFLoader.FILE_EXTENSION_COMPRESSED);
    } else {
      setFileExtension(XRFFLoader.FILE_EXTENSION);
    }
    try {
      m_XMLInstances = new XMLInstances();
    }
    catch (Exception e) {
      m_XMLInstances = null;
    }
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
  





  public String compressOutputTipText()
  {
    return "Optional compression of the output data";
  }
  




  public boolean getCompressOutput()
  {
    return m_CompressOutput;
  }
  




  public void setCompressOutput(boolean value)
  {
    m_CompressOutput = value;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  




  public void setInstances(Instances instances)
  {
    if (m_ClassIndex.getSingleIndex().length() != 0) {
      m_ClassIndex.setUpper(instances.numAttributes() - 1);
      instances.setClassIndex(m_ClassIndex.getIndex());
    }
    
    super.setInstances(instances);
  }
  




  public void setDestination(OutputStream output)
    throws IOException
  {
    if (getCompressOutput()) {
      super.setDestination(new GZIPOutputStream(output));
    } else {
      super.setDestination(output);
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
    

    m_XMLInstances.setInstances(getInstances());
    
    if ((retrieveFile() == null) && (getWriter() == null)) {
      System.out.println(m_XMLInstances.toString());
      setWriteMode(1);
    }
    else {
      PrintWriter outW = new PrintWriter(getWriter());
      outW.println(m_XMLInstances.toString());
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
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
  




  public static void main(String[] args)
  {
    runFileSaver(new XRFFSaver(), args);
  }
}
