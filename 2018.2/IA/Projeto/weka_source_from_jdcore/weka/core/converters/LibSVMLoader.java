package weka.core.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;








































public class LibSVMLoader
  extends AbstractFileLoader
  implements BatchConverter, URLSourcedLoader
{
  private static final long serialVersionUID = 4988360125354664417L;
  public static String FILE_EXTENSION = ".libsvm";
  

  protected String m_URL = "http://";
  

  protected transient Reader m_sourceReader = null;
  

  protected Vector m_Buffer = null;
  


  public LibSVMLoader() {}
  

  public String globalInfo()
  {
    return "Reads a source that is in libsvm format.\n\nFor more information about libsvm see:\n\nhttp://www.csie.ntu.edu.tw/~cjlin/libsvm/";
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
    return "libsvm data files";
  }
  




  public void reset()
    throws IOException
  {
    m_structure = null;
    m_Buffer = null;
    
    setRetrieval(0);
    
    if ((m_File != null) && (new File(m_File).isFile())) {
      setFile(new File(m_File));
    }
    else if ((m_URL != null) && (!m_URL.equals("http://"))) {
      setURL(m_URL);
    }
  }
  





  public void setSource(URL url)
    throws IOException
  {
    m_structure = null;
    m_Buffer = null;
    
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
  













  protected double[] libsvmToArray(String row)
  {
    int max = 0;
    StringTokenizer tok = new StringTokenizer(row, " \t");
    tok.nextToken();
    while (tok.hasMoreTokens()) {
      String col = tok.nextToken();
      int index = Integer.parseInt(col.substring(0, col.indexOf(":")));
      if (index > max) {
        max = index;
      }
    }
    

    tok = new StringTokenizer(row, " \t");
    double[] result = new double[max + 1];
    

    result[(result.length - 1)] = Double.parseDouble(tok.nextToken());
    

    while (tok.hasMoreTokens()) {
      String col = tok.nextToken();
      int index = Integer.parseInt(col.substring(0, col.indexOf(":")));
      double value = Double.parseDouble(col.substring(col.indexOf(":") + 1));
      result[(index - 1)] = value;
    }
    
    return result;
  }
  











  protected int determineNumAttributes(String row, int num)
  {
    int result = num;
    
    int count = libsvmToArray(row).length;
    if (count > result) {
      result = count;
    }
    
    return result;
  }
  














  public Instances getStructure()
    throws IOException
  {
    if (m_sourceReader == null) {
      throw new IOException("No source has been specified");
    }
    
    if (m_structure == null) {
      m_Buffer = new Vector();
      try
      {
        int numAtt = 0;
        int len = 8388608;
        char[] cbuf = new char[len];
        int iter = 0;
        String linesplitter = null;
        
        String oldLine = null;
        String read = null;
        int cInt; while ((cInt = m_sourceReader.read(cbuf, 0, len)) != -1) {
          read = String.valueOf(cbuf, 0, cInt);
          
          if (oldLine != null) {
            read = oldLine + read;
          }
          
          if (linesplitter == null) {
            if (read.contains("\r\n")) {
              linesplitter = "\r\n";
            } else if (read.contains("\n"))
              linesplitter = "\n";
          }
          String[] lines;
          String[] lines;
          if (linesplitter != null) {
            lines = read.split(linesplitter, -1);
          } else {
            lines = new String[] { read };
          }
          
          for (int j = 0; j < lines.length - 1; j++) {
            String line = lines[j];
            
            m_Buffer.add(libsvmToArray(line));
            numAtt = determineNumAttributes(line, numAtt);
          }
          
          oldLine = lines[(lines.length - 1)];
        }
        

        if ((oldLine != null) && (oldLine.length() != 0)) {
          m_Buffer.add(libsvmToArray(oldLine));
          numAtt = determineNumAttributes(oldLine, numAtt);
        }
        

        FastVector atts = new FastVector(numAtt);
        for (int i = 0; i < numAtt - 1; i++) {
          atts.addElement(new Attribute("att_" + (i + 1)));
        }
        atts.addElement(new Attribute("class"));
        String relName;
        String relName; if (!m_URL.equals("http://")) {
          relName = m_URL;
        } else {
          relName = m_File;
        }
        
        m_structure = new Instances(relName, atts, 0);
        m_structure.setClassIndex(m_structure.numAttributes() - 1);
      } catch (Exception ex) {
        ex.printStackTrace();
        throw new IOException("Unable to determine structure as libsvm: " + ex);
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
    
    Instances result = new Instances(m_structure, 0);
    

    for (int i = 0; i < m_Buffer.size(); i++) {
      double[] sparse = (double[])m_Buffer.get(i);
      double[] data;
      if (sparse.length != m_structure.numAttributes()) {
        double[] data = new double[m_structure.numAttributes()];
        
        System.arraycopy(sparse, 0, data, 0, sparse.length - 1);
        
        data[(data.length - 1)] = sparse[(sparse.length - 1)];
      }
      else {
        data = sparse;
      }
      
      result.add(new SparseInstance(1.0D, data));
    }
    
    try
    {
      m_sourceReader.close();
    }
    catch (Exception ex) {}
    

    return result;
  }
  







  public Instance getNextInstance(Instances structure)
    throws IOException
  {
    throw new IOException("LibSVMLoader can't read data sets incrementally.");
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11362 $");
  }
  




  public static void main(String[] args)
  {
    runFileLoader(new LibSVMLoader(), args);
  }
}
