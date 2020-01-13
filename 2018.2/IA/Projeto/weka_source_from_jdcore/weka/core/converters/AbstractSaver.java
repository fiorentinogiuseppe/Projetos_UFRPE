package weka.core.converters;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;










































public abstract class AbstractSaver
  implements Saver, CapabilitiesHandler
{
  protected static final int WRITE = 0;
  protected static final int WAIT = 1;
  protected static final int CANCEL = 2;
  protected static final int STRUCTURE_READY = 3;
  private Instances m_instances;
  protected int m_retrieval;
  private int m_writeMode;
  
  public AbstractSaver() {}
  
  public void resetOptions()
  {
    m_instances = null;
    m_writeMode = 1;
  }
  


  public void resetStructure()
  {
    m_instances = null;
    m_writeMode = 1;
  }
  






  public void setRetrieval(int mode)
  {
    m_retrieval = mode;
  }
  





  protected int getRetrieval()
  {
    return m_retrieval;
  }
  






  protected void setWriteMode(int mode)
  {
    m_writeMode = mode;
  }
  





  public int getWriteMode()
  {
    return m_writeMode;
  }
  






  public void setInstances(Instances instances)
  {
    Capabilities cap = getCapabilities();
    if (!cap.test(instances)) {
      throw new IllegalArgumentException(cap.getFailReason());
    }
    if (m_retrieval == 2) {
      if (setStructure(instances) == 2) {
        cancel();
      }
    } else {
      m_instances = instances;
    }
  }
  




  public Instances getInstances()
  {
    return m_instances;
  }
  






  public void setDestination(File file)
    throws IOException
  {
    throw new IOException("Writing to a file not supported");
  }
  






  public void setDestination(OutputStream output)
    throws IOException
  {
    throw new IOException("Writing to an outputstream not supported");
  }
  






  public Capabilities getCapabilities()
  {
    Capabilities result = new Capabilities(this);
    
    result.setMinimumNumberInstances(0);
    
    return result;
  }
  





  public int setStructure(Instances headerInfo)
  {
    Capabilities cap = getCapabilities();
    if (!cap.test(headerInfo)) {
      throw new IllegalArgumentException(cap.getFailReason());
    }
    if ((m_writeMode == 1) && (headerInfo != null)) {
      m_instances = headerInfo;
      m_writeMode = 3;

    }
    else if ((headerInfo == null) || (m_writeMode != 3) || (!headerInfo.equalHeaders(m_instances))) {
      m_instances = null;
      if (m_writeMode != 1)
        System.err.println("A structure cannot be set up during an active incremental saving process.");
      m_writeMode = 2;
    }
    
    return m_writeMode;
  }
  

  public void cancel()
  {
    if (m_writeMode == 2) {
      resetOptions();
    }
  }
  







  public void writeIncremental(Instance i)
    throws IOException
  {
    throw new IOException("No Incremental saving possible.");
  }
  





  public abstract void writeBatch()
    throws IOException;
  





  public String getFileExtension()
    throws Exception
  {
    throw new Exception("Saving in a file not supported.");
  }
  





  public void setFile(File file)
    throws IOException
  {
    throw new IOException("Saving in a file not supported.");
  }
  





  public void setFilePrefix(String prefix)
    throws Exception
  {
    throw new Exception("Saving in a file not supported.");
  }
  




  public String filePrefix()
    throws Exception
  {
    throw new Exception("Saving in a file not supported.");
  }
  





  public void setDir(String dir)
    throws IOException
  {
    throw new IOException("Saving in a file not supported.");
  }
  






  public void setDirAndPrefix(String relationName, String add)
    throws IOException
  {
    throw new IOException("Saving in a file not supported.");
  }
  




  public String retrieveDir()
    throws IOException
  {
    throw new IOException("Saving in a file not supported.");
  }
}
