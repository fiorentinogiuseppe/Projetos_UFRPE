package weka.core.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.xml.XMLInstances;





































public class XRFFLoader
  extends AbstractFileLoader
  implements BatchConverter, URLSourcedLoader
{
  private static final long serialVersionUID = 3764533621135196582L;
  public static String FILE_EXTENSION = XMLInstances.FILE_EXTENSION;
  

  public static String FILE_EXTENSION_COMPRESSED = FILE_EXTENSION + ".gz";
  

  protected String m_URL = "http://";
  

  protected transient Reader m_sourceReader = null;
  

  protected XMLInstances m_XMLInstances;
  


  public XRFFLoader() {}
  

  public String globalInfo()
  {
    return "Reads a source that is in the XML version of the ARFF format. It automatically decompresses the data if the extension is '" + FILE_EXTENSION_COMPRESSED + "'.";
  }
  







  public String getFileExtension()
  {
    return FILE_EXTENSION;
  }
  




  public String[] getFileExtensions()
  {
    return new String[] { FILE_EXTENSION, FILE_EXTENSION_COMPRESSED };
  }
  




  public String getFileDescription()
  {
    return "XRFF data files";
  }
  



  public void reset()
    throws IOException
  {
    m_structure = null;
    m_XMLInstances = null;
    
    setRetrieval(0);
    
    if (m_File != null) {
      setFile(new File(m_File));
    }
    else if ((m_URL != null) && (!m_URL.equals("http://"))) {
      setURL(m_URL);
    }
  }
  





  public void setSource(File file)
    throws IOException
  {
    m_structure = null;
    m_XMLInstances = null;
    
    setRetrieval(0);
    
    if (file == null) {
      throw new IOException("Source file object is null!");
    }
    try {
      if (file.getName().endsWith(FILE_EXTENSION_COMPRESSED)) {
        setSource(new GZIPInputStream(new FileInputStream(file)));
      } else {
        setSource(new FileInputStream(file));
      }
    } catch (FileNotFoundException ex) {
      throw new IOException("File not found");
    }
    
    m_sourceFile = file;
    m_File = file.getAbsolutePath();
  }
  





  public void setSource(URL url)
    throws IOException
  {
    m_structure = null;
    m_XMLInstances = null;
    
    setRetrieval(0);
    
    setSource(url.openStream());
    
    m_URL = url.toString();
  }
  




  public void setURL(String url)
    throws IOException
  {
    m_URL = url;
    setSource(new URL(url));
  }
  




  public String retrieveURL()
  {
    return m_URL;
  }
  





  public void setSource(InputStream in)
    throws IOException
  {
    m_File = new File(System.getProperty("user.dir")).getAbsolutePath();
    m_URL = "http://";
    
    m_sourceReader = new BufferedReader(new InputStreamReader(in));
  }
  






  public Instances getStructure()
    throws IOException
  {
    if (m_sourceReader == null) {
      throw new IOException("No source has been specified");
    }
    if (m_structure == null) {
      try {
        m_XMLInstances = new XMLInstances(m_sourceReader);
        m_structure = new Instances(m_XMLInstances.getInstances(), 0);
      }
      catch (IOException ioe)
      {
        throw ioe;
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    
    return new Instances(m_structure, 0);
  }
  







  public Instances getDataSet()
    throws IOException
  {
    if (m_sourceReader == null) {
      throw new IOException("No source has been specified");
    }
    if (getRetrieval() == 2) {
      throw new IOException("Cannot mix getting Instances in both incremental and batch modes");
    }
    setRetrieval(1);
    if (m_structure == null) {
      getStructure();
    }
    try
    {
      m_sourceReader.close();
    }
    catch (Exception ex) {}
    
    return m_XMLInstances.getInstances();
  }
  






  public Instance getNextInstance(Instances structure)
    throws IOException
  {
    throw new IOException("XRFFLoader can't read data sets incrementally.");
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 4985 $");
  }
  




  public static void main(String[] args)
  {
    runFileLoader(new XRFFLoader(), args);
  }
}
