package weka.core.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
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








































public class SVMLightLoader
  extends AbstractFileLoader
  implements BatchConverter, URLSourcedLoader
{
  private static final long serialVersionUID = 4988360125354664417L;
  public static String FILE_EXTENSION = ".dat";
  

  protected String m_URL = "http://";
  

  protected transient Reader m_sourceReader = null;
  

  protected Vector m_Buffer = null;
  


  public SVMLightLoader() {}
  

  public String globalInfo()
  {
    return "Reads a source that is in svm light format.\n\nFor more information about svm light see:\n\nhttp://svmlight.joachims.org/";
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
    return "svm light data files";
  }
  



  public void reset()
    throws IOException
  {
    m_structure = null;
    m_Buffer = null;
    
    setRetrieval(0);
    
    if (m_File != null) {
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
  







  protected double[] svmlightToArray(String row)
    throws Exception
  {
    double[] result;
    





    try
    {
      int max = 0;
      StringTokenizer tok = new StringTokenizer(row, " \t");
      tok.nextToken();
      while (tok.hasMoreTokens()) {
        String col = tok.nextToken();
        
        if (col.startsWith("#")) {
          break;
        }
        if (!col.startsWith("qid:"))
        {

          int index = Integer.parseInt(col.substring(0, col.indexOf(":")));
          if (index > max) {
            max = index;
          }
        }
      }
      tok = new StringTokenizer(row, " \t");
      result = new double[max + 1];
      

      result[(result.length - 1)] = Double.parseDouble(tok.nextToken());
      

      while (tok.hasMoreTokens()) {
        String col = tok.nextToken();
        
        if (col.startsWith("#")) {
          break;
        }
        if (!col.startsWith("qid:"))
        {

          int index = Integer.parseInt(col.substring(0, col.indexOf(":")));
          double value = Double.parseDouble(col.substring(col.indexOf(":") + 1));
          result[(index - 1)] = value;
        }
      }
    } catch (Exception e) {
      System.err.println("Error parsing line '" + row + "': " + e);
      throw new Exception(e);
    }
    
    return result;
  }
  











  protected int determineNumAttributes(double[] values, int num)
    throws Exception
  {
    int result = num;
    
    int count = values.length;
    if (count > result) {
      result = count;
    }
    return result;
  }
  











  protected Attribute determineClassAttribute()
  {
    boolean binary = true;
    
    for (int i = 0; i < m_Buffer.size(); i++) {
      double[] dbls = (double[])m_Buffer.get(i);
      double cls = dbls[(dbls.length - 1)];
      if ((cls != -1.0D) && (cls != 1.0D)) {
        binary = false;
        break;
      } }
    Attribute result;
    Attribute result;
    if (binary) {
      FastVector values = new FastVector();
      values.addElement("+1");
      values.addElement("-1");
      result = new Attribute("class", values);
    }
    else {
      result = new Attribute("class");
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
        StringBuffer line = new StringBuffer();
        int cInt; while ((cInt = m_sourceReader.read()) != -1) {
          char c = (char)cInt;
          if ((c == '\n') || (c == '\r')) {
            if ((line.length() > 0) && (line.charAt(0) != '#')) {
              try
              {
                m_Buffer.add(svmlightToArray(line.toString()));
                numAtt = determineNumAttributes((double[])m_Buffer.lastElement(), numAtt);
              }
              catch (Exception e) {
                throw new Exception("Error parsing line '" + line + "': " + e);
              }
            }
            line = new StringBuffer();
          }
          else {
            line.append(c);
          }
        }
        

        if ((line.length() != 0) && (line.charAt(0) != '#')) {
          m_Buffer.add(svmlightToArray(line.toString()));
          numAtt = determineNumAttributes((double[])m_Buffer.lastElement(), numAtt);
        }
        

        FastVector atts = new FastVector(numAtt);
        for (int i = 0; i < numAtt - 1; i++)
          atts.addElement(new Attribute("att_" + (i + 1)));
        atts.addElement(determineClassAttribute());
        String relName;
        String relName; if (!m_URL.equals("http://")) {
          relName = m_URL;
        } else {
          relName = m_File;
        }
        m_structure = new Instances(relName, atts, 0);
        m_structure.setClassIndex(m_structure.numAttributes() - 1);
      }
      catch (Exception ex) {
        ex.printStackTrace();
        throw new IOException("Unable to determine structure as svm light: " + ex);
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
      

      if (result.classAttribute().isNominal()) {
        if (data[(data.length - 1)] == 1.0D) {
          data[(data.length - 1)] = result.classAttribute().indexOfValue("+1");
        } else if (data[(data.length - 1)] == -1.0D) {
          data[(data.length - 1)] = result.classAttribute().indexOfValue("-1");
        } else {
          throw new IllegalStateException("Class is not binary!");
        }
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
    throw new IOException("SVMLightLoader can't read data sets incrementally.");
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 4985 $");
  }
  




  public static void main(String[] args)
  {
    runFileLoader(new SVMLightLoader(), args);
  }
}
