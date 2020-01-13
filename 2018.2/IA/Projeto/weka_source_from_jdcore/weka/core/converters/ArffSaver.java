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
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;












































public class ArffSaver
  extends AbstractFileSaver
  implements BatchConverter, IncrementalConverter
{
  static final long serialVersionUID = 2223634248900042228L;
  protected boolean m_CompressOutput = false;
  

  public ArffSaver()
  {
    resetOptions();
  }
  






  public Enumeration listOptions()
  {
    Vector<Option> result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement((Option)en.nextElement());
    }
    result.addElement(new Option("\tThe class index (first and last are valid as well).\n\t(default: last)", "C", 1, "-C <class index>"));
    




    result.addElement(new Option("\tCompresses the data (uses '" + XRFFLoader.FILE_EXTENSION_COMPRESSED + "' as extension instead of '" + XRFFLoader.FILE_EXTENSION + "')\n" + "\t(default: off)", "compress", 0, "-compress"));
    







    return result.elements();
  }
  








  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
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
    setCompressOutput(Utils.getFlag("compress", options));
    
    super.setOptions(options);
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
  





  public String globalInfo()
  {
    return "Writes to a destination that is in arff (attribute relation file format) format. ";
  }
  






  public String getFileDescription()
  {
    return "Arff data files";
  }
  




  public String[] getFileExtensions()
  {
    return new String[] { ArffLoader.FILE_EXTENSION, ArffLoader.FILE_EXTENSION_COMPRESSED };
  }
  




  public void setFile(File outputFile)
    throws IOException
  {
    if (outputFile.getAbsolutePath().endsWith(ArffLoader.FILE_EXTENSION_COMPRESSED)) {
      setCompressOutput(true);
    }
    super.setFile(outputFile);
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
  


  public void resetOptions()
  {
    super.resetOptions();
    setFileExtension(".arff");
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
  




  public void writeIncremental(Instance inst)
    throws IOException
  {
    int writeMode = getWriteMode();
    Instances structure = getInstances();
    PrintWriter outW = null;
    
    if ((getRetrieval() == 1) || (getRetrieval() == 0))
      throw new IOException("Batch and incremental saving cannot be mixed.");
    if (getWriter() != null) {
      outW = new PrintWriter(getWriter());
    }
    if (writeMode == 1) {
      if (structure == null) {
        setWriteMode(2);
        if (inst != null) {
          System.err.println("Structure(Header Information) has to be set in advance");
        }
      } else {
        setWriteMode(3); }
      writeMode = getWriteMode();
    }
    if (writeMode == 2) {
      if (outW != null)
        outW.close();
      cancel();
    }
    if (writeMode == 3) {
      setWriteMode(0);
      
      Instances header = new Instances(structure, 0);
      if ((retrieveFile() == null) && (outW == null)) {
        System.out.println(header.toString());
      } else {
        outW.print(header.toString());
        outW.print("\n");
        outW.flush();
      }
      writeMode = getWriteMode();
    }
    if (writeMode == 0) {
      if (structure == null)
        throw new IOException("No instances information available.");
      if (inst != null)
      {
        if ((retrieveFile() == null) && (outW == null)) {
          System.out.println(inst);
        } else {
          outW.println(inst);
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
    if (getInstances() == null)
      throw new IOException("No instances to save");
    if (getRetrieval() == 2)
      throw new IOException("Batch and incremental saving cannot be mixed.");
    setRetrieval(1);
    setWriteMode(0);
    if ((retrieveFile() == null) && (getWriter() == null)) {
      System.out.println(getInstances().toString());
      setWriteMode(1);
      return;
    }
    
    PrintWriter outW = new PrintWriter(getWriter());
    Instances data = getInstances();
    

    Instances header = new Instances(data, 0);
    outW.print(header.toString());
    

    for (int i = 0; i < data.numInstances(); i++) {
      if (i % 1000 == 0)
        outW.flush();
      outW.println(data.instance(i));
    }
    outW.flush();
    outW.close();
    
    setWriteMode(1);
    outW = null;
    resetWriter();
    setWriteMode(2);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7950 $");
  }
  




  public static void main(String[] args)
  {
    runFileSaver(new ArffSaver(), args);
  }
}
