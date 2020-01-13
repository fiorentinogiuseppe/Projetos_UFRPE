package weka.core.converters;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;





































public class SerializedInstancesLoader
  extends AbstractFileLoader
  implements BatchConverter, IncrementalConverter
{
  static final long serialVersionUID = 2391085836269030715L;
  public static String FILE_EXTENSION = ".bsi";
  


  protected Instances m_Dataset = null;
  

  protected int m_IncrementalIndex = 0;
  


  public SerializedInstancesLoader() {}
  

  public String globalInfo()
  {
    return "Reads a source that contains serialized Instances.";
  }
  

  public void reset()
  {
    m_Dataset = null;
    m_IncrementalIndex = 0;
  }
  




  public String getFileExtension()
  {
    return FILE_EXTENSION;
  }
  




  public String[] getFileExtensions()
  {
    return new String[] { getFileExtension() };
  }
  




  public String getFileDescription()
  {
    return "Binary serialized instances";
  }
  






  public void setSource(InputStream in)
    throws IOException
  {
    ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(in));
    try {
      m_Dataset = ((Instances)oi.readObject());
    } catch (ClassNotFoundException ex) {
      throw new IOException("Could not deserialize instances from this source.");
    }
    

    oi.close();
  }
  






  public Instances getStructure()
    throws IOException
  {
    if (m_Dataset == null) {
      throw new IOException("No source has been specified");
    }
    


    return new Instances(m_Dataset, 0);
  }
  







  public Instances getDataSet()
    throws IOException
  {
    if (m_Dataset == null) {
      throw new IOException("No source has been specified");
    }
    
    return m_Dataset;
  }
  











  public Instance getNextInstance(Instances structure)
    throws IOException
  {
    if (m_Dataset == null) {
      throw new IOException("No source has been specified");
    }
    


    if (m_IncrementalIndex == m_Dataset.numInstances()) {
      return null;
    }
    
    return m_Dataset.instance(m_IncrementalIndex++);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.15 $");
  }
  




  public static void main(String[] args)
  {
    runFileLoader(new SerializedInstancesLoader(), args);
  }
}
