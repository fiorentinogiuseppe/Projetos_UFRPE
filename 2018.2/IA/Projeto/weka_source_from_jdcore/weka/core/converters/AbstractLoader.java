package weka.core.converters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import weka.core.Instance;
import weka.core.Instances;































public abstract class AbstractLoader
  implements Loader
{
  protected int m_retrieval;
  
  public AbstractLoader() {}
  
  public void setRetrieval(int mode)
  {
    m_retrieval = mode;
  }
  





  protected int getRetrieval()
  {
    return m_retrieval;
  }
  





  public void setSource(File file)
    throws IOException
  {
    throw new IOException("Setting File as source not supported");
  }
  



  public void reset()
    throws Exception
  {
    m_retrieval = 0;
  }
  





  public void setSource(InputStream input)
    throws IOException
  {
    throw new IOException("Setting InputStream as source not supported");
  }
  
  public abstract Instances getStructure()
    throws IOException;
  
  public abstract Instances getDataSet()
    throws IOException;
  
  public abstract Instance getNextInstance(Instances paramInstances)
    throws IOException;
}
