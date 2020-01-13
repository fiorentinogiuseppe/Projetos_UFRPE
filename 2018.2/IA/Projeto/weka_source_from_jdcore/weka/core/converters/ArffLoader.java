package weka.core.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;






































public class ArffLoader
  extends AbstractFileLoader
  implements BatchConverter, IncrementalConverter, URLSourcedLoader
{
  static final long serialVersionUID = 2726929550544048587L;
  public static String FILE_EXTENSION = ".arff";
  public static String FILE_EXTENSION_COMPRESSED = FILE_EXTENSION + ".gz";
  protected String m_URL;
  
  public ArffLoader() { m_URL = "http://";
    

    m_sourceReader = null;
    

    m_ArffReader = null;
  }
  






  protected transient Reader m_sourceReader;
  





  protected transient ArffReader m_ArffReader;
  





  public static class ArffReader
    implements RevisionHandler
  {
    protected StreamTokenizer m_Tokenizer;
    





    protected double[] m_ValueBuffer;
    





    protected int[] m_IndicesBuffer;
    




    protected Instances m_Data;
    




    protected int m_Lines;
    





    public ArffReader(Reader reader)
      throws IOException
    {
      m_Tokenizer = new StreamTokenizer(reader);
      initTokenizer();
      
      readHeader(1000);
      initBuffers();
      
      Instance inst;
      while ((inst = readInstance(m_Data)) != null) {
        m_Data.add(inst);
      }
      

      compactify();
    }
    









    public ArffReader(Reader reader, int capacity)
      throws IOException
    {
      if (capacity < 0) {
        throw new IllegalArgumentException("Capacity has to be positive!");
      }
      
      m_Tokenizer = new StreamTokenizer(reader);
      initTokenizer();
      
      readHeader(capacity);
      initBuffers();
    }
    









    public ArffReader(Reader reader, Instances template, int lines)
      throws IOException
    {
      this(reader, template, lines, 100);
      
      Instance inst;
      while ((inst = readInstance(m_Data)) != null) {
        m_Data.add(inst);
      }
      

      compactify();
    }
    











    public ArffReader(Reader reader, Instances template, int lines, int capacity)
      throws IOException
    {
      m_Lines = lines;
      m_Tokenizer = new StreamTokenizer(reader);
      initTokenizer();
      
      m_Data = new Instances(template, capacity);
      initBuffers();
    }
    





    protected void initBuffers()
    {
      m_ValueBuffer = new double[m_Data.numAttributes()];
      m_IndicesBuffer = new int[m_Data.numAttributes()];
    }
    


    protected void compactify()
    {
      if (m_Data != null) {
        m_Data.compactify();
      }
    }
    




    protected void errorMessage(String msg)
      throws IOException
    {
      String str = msg + ", read " + m_Tokenizer.toString();
      if (m_Lines > 0) {
        int line = Integer.parseInt(str.replaceAll(".* line ", ""));
        str = str.replaceAll(" line .*", " line " + (m_Lines + line - 1));
      }
      throw new IOException(str);
    }
    




    public int getLineNo()
    {
      return m_Lines + m_Tokenizer.lineno();
    }
    



    protected void getFirstToken()
      throws IOException
    {
      while (m_Tokenizer.nextToken() == 10) {}
      


      if ((m_Tokenizer.ttype == 39) || (m_Tokenizer.ttype == 34))
      {
        m_Tokenizer.ttype = -3;
      } else if ((m_Tokenizer.ttype == -3) && (m_Tokenizer.sval.equals("?")))
      {
        m_Tokenizer.ttype = 63;
      }
    }
    



    protected void getIndex()
      throws IOException
    {
      if (m_Tokenizer.nextToken() == 10) {
        errorMessage("premature end of line");
      }
      if (m_Tokenizer.ttype == -1) {
        errorMessage("premature end of file");
      }
    }
    




    protected void getLastToken(boolean endOfFileOk)
      throws IOException
    {
      if ((m_Tokenizer.nextToken() != 10) && ((m_Tokenizer.ttype != -1) || (!endOfFileOk)))
      {
        errorMessage("end of line expected");
      }
    }
    




    protected double getInstanceWeight()
      throws IOException
    {
      double weight = NaN.0D;
      m_Tokenizer.nextToken();
      if ((m_Tokenizer.ttype == 10) || (m_Tokenizer.ttype == -1))
      {
        return weight;
      }
      

      if (m_Tokenizer.ttype == 123) {
        m_Tokenizer.nextToken();
        String weightS = m_Tokenizer.sval;
        try
        {
          weight = Double.parseDouble(weightS);
        }
        catch (NumberFormatException e) {
          return weight;
        }
        
        m_Tokenizer.nextToken();
        if (m_Tokenizer.ttype != 125) {
          errorMessage("Problem reading instance weight");
        }
      }
      return weight;
    }
    



    protected void getNextToken()
      throws IOException
    {
      if (m_Tokenizer.nextToken() == 10) {
        errorMessage("premature end of line");
      }
      if (m_Tokenizer.ttype == -1) {
        errorMessage("premature end of file");
      } else if ((m_Tokenizer.ttype == 39) || (m_Tokenizer.ttype == 34))
      {
        m_Tokenizer.ttype = -3;
      } else if ((m_Tokenizer.ttype == -3) && (m_Tokenizer.sval.equals("?")))
      {
        m_Tokenizer.ttype = 63;
      }
    }
    


    protected void initTokenizer()
    {
      m_Tokenizer.resetSyntax();
      m_Tokenizer.whitespaceChars(0, 32);
      m_Tokenizer.wordChars(33, 255);
      m_Tokenizer.whitespaceChars(44, 44);
      m_Tokenizer.commentChar(37);
      m_Tokenizer.quoteChar(34);
      m_Tokenizer.quoteChar(39);
      m_Tokenizer.ordinaryChar(123);
      m_Tokenizer.ordinaryChar(125);
      m_Tokenizer.eolIsSignificant(true);
    }
    






    public Instance readInstance(Instances structure)
      throws IOException
    {
      return readInstance(structure, true);
    }
    








    public Instance readInstance(Instances structure, boolean flag)
      throws IOException
    {
      return getInstance(structure, flag);
    }
    








    protected Instance getInstance(Instances structure, boolean flag)
      throws IOException
    {
      m_Data = structure;
      

      if (m_Data.numAttributes() == 0) {
        errorMessage("no header information available");
      }
      

      getFirstToken();
      if (m_Tokenizer.ttype == -1) {
        return null;
      }
      

      if (m_Tokenizer.ttype == 123) {
        return getInstanceSparse(flag);
      }
      return getInstanceFull(flag);
    }
    






    protected Instance getInstanceSparse(boolean flag)
      throws IOException
    {
      int numValues = 0;int maxIndex = -1;
      

      for (;;)
      {
        getIndex();
        if (m_Tokenizer.ttype == 125) {
          break;
        }
        
        try
        {
          m_IndicesBuffer[numValues] = Integer.valueOf(m_Tokenizer.sval).intValue();
        }
        catch (NumberFormatException e) {
          errorMessage("index number expected");
        }
        if (m_IndicesBuffer[numValues] <= maxIndex) {
          errorMessage("indices have to be ordered");
        }
        if ((m_IndicesBuffer[numValues] < 0) || (m_IndicesBuffer[numValues] >= m_Data.numAttributes()))
        {
          errorMessage("index out of bounds");
        }
        maxIndex = m_IndicesBuffer[numValues];
        

        getNextToken();
        

        if (m_Tokenizer.ttype == 63) {
          m_ValueBuffer[numValues] = Instance.missingValue();
        }
        else
        {
          if (m_Tokenizer.ttype != -3) {
            errorMessage("not a valid value");
          }
          switch (m_Data.attribute(m_IndicesBuffer[numValues]).type())
          {
          case 1: 
            int valIndex = m_Data.attribute(m_IndicesBuffer[numValues]).indexOfValue(m_Tokenizer.sval);
            

            if (valIndex == -1) {
              errorMessage("nominal value not declared in header");
            }
            m_ValueBuffer[numValues] = valIndex;
            break;
          case 0: 
            try
            {
              m_ValueBuffer[numValues] = Double.valueOf(m_Tokenizer.sval).doubleValue();
            }
            catch (NumberFormatException e) {
              errorMessage("number expected");
            }
          
          case 2: 
            m_ValueBuffer[numValues] = m_Data.attribute(m_IndicesBuffer[numValues]).addStringValue(m_Tokenizer.sval);
            

            break;
          case 3: 
            try {
              m_ValueBuffer[numValues] = m_Data.attribute(m_IndicesBuffer[numValues]).parseDate(m_Tokenizer.sval);
            }
            catch (ParseException e)
            {
              errorMessage("unparseable date: " + m_Tokenizer.sval);
            }
          case 4: 
            try
            {
              ArffReader arff = new ArffReader(new StringReader(m_Tokenizer.sval), m_Data.attribute(m_IndicesBuffer[numValues]).relation(), 0);
              

              Instances data = arff.getData();
              m_ValueBuffer[numValues] = m_Data.attribute(m_IndicesBuffer[numValues]).addRelation(data);
            }
            catch (Exception e) {
              throw new IOException(e.toString() + " of line " + getLineNo());
            }
          
          default: 
            errorMessage("unknown attribute type in column " + m_IndicesBuffer[numValues]);
          }
          
        }
        numValues++;
      }
      
      double weight = 1.0D;
      if (flag)
      {
        weight = getInstanceWeight();
        if (!Double.isNaN(weight)) {
          getLastToken(true);
        } else {
          weight = 1.0D;
        }
      }
      

      double[] tempValues = new double[numValues];
      int[] tempIndices = new int[numValues];
      System.arraycopy(m_ValueBuffer, 0, tempValues, 0, numValues);
      System.arraycopy(m_IndicesBuffer, 0, tempIndices, 0, numValues);
      Instance inst = new SparseInstance(weight, tempValues, tempIndices, m_Data.numAttributes());
      

      inst.setDataset(m_Data);
      
      return inst;
    }
    





    protected Instance getInstanceFull(boolean flag)
      throws IOException
    {
      double[] instance = new double[m_Data.numAttributes()];
      


      for (int i = 0; i < m_Data.numAttributes(); i++)
      {
        if (i > 0) {
          getNextToken();
        }
        

        if (m_Tokenizer.ttype == 63) {
          instance[i] = Instance.missingValue();
        }
        else
        {
          if (m_Tokenizer.ttype != -3) {
            errorMessage("not a valid value");
          }
          switch (m_Data.attribute(i).type())
          {
          case 1: 
            int index = m_Data.attribute(i).indexOfValue(m_Tokenizer.sval);
            if (index == -1) {
              errorMessage("nominal value not declared in header");
            }
            instance[i] = index;
            break;
          case 0: 
            try
            {
              instance[i] = Double.valueOf(m_Tokenizer.sval).doubleValue();
            }
            catch (NumberFormatException e) {
              errorMessage("number expected");
            }
          
          case 2: 
            instance[i] = m_Data.attribute(i).addStringValue(m_Tokenizer.sval);
            break;
          case 3: 
            try {
              instance[i] = m_Data.attribute(i).parseDate(m_Tokenizer.sval);
            } catch (ParseException e) {
              errorMessage("unparseable date: " + m_Tokenizer.sval);
            }
          case 4: 
            try
            {
              ArffReader arff = new ArffReader(new StringReader(m_Tokenizer.sval), m_Data.attribute(i).relation(), 0);
              

              Instances data = arff.getData();
              instance[i] = m_Data.attribute(i).addRelation(data);
            } catch (Exception e) {
              throw new IOException(e.toString() + " of line " + getLineNo());
            }
          
          default: 
            errorMessage("unknown attribute type in column " + i);
          }
          
        }
      }
      double weight = 1.0D;
      if (flag)
      {
        weight = getInstanceWeight();
        if (!Double.isNaN(weight)) {
          getLastToken(true);
        } else {
          weight = 1.0D;
        }
      }
      

      Instance inst = new Instance(weight, instance);
      inst.setDataset(m_Data);
      
      return inst;
    }
    




    protected void readHeader(int capacity)
      throws IOException
    {
      m_Lines = 0;
      String relationName = "";
      

      getFirstToken();
      if (m_Tokenizer.ttype == -1) {
        errorMessage("premature end of file");
      }
      if ("@relation".equalsIgnoreCase(m_Tokenizer.sval)) {
        getNextToken();
        relationName = m_Tokenizer.sval;
        getLastToken(false);
      } else {
        errorMessage("keyword @relation expected");
      }
      

      FastVector attributes = new FastVector();
      

      getFirstToken();
      if (m_Tokenizer.ttype == -1) {
        errorMessage("premature end of file");
      }
      
      while ("@attribute".equalsIgnoreCase(m_Tokenizer.sval)) {
        attributes = parseAttribute(attributes);
      }
      

      if (!"@data".equalsIgnoreCase(m_Tokenizer.sval)) {
        errorMessage("keyword @data expected");
      }
      

      if (attributes.size() == 0) {
        errorMessage("no attributes declared");
      }
      
      m_Data = new Instances(relationName, attributes, capacity);
    }
    










    protected FastVector parseAttribute(FastVector attributes)
      throws IOException
    {
      getNextToken();
      String attributeName = m_Tokenizer.sval;
      getNextToken();
      

      if (m_Tokenizer.ttype == -3)
      {

        if ((m_Tokenizer.sval.equalsIgnoreCase("real")) || (m_Tokenizer.sval.equalsIgnoreCase("integer")) || (m_Tokenizer.sval.equalsIgnoreCase("numeric")))
        {

          attributes.addElement(new Attribute(attributeName, attributes.size()));
          
          readTillEOL();
        } else if (m_Tokenizer.sval.equalsIgnoreCase("string"))
        {
          attributes.addElement(new Attribute(attributeName, (FastVector)null, attributes.size()));
          

          readTillEOL();
        } else if (m_Tokenizer.sval.equalsIgnoreCase("date"))
        {
          String format = null;
          if (m_Tokenizer.nextToken() != 10) {
            if ((m_Tokenizer.ttype != -3) && (m_Tokenizer.ttype != 39) && (m_Tokenizer.ttype != 34))
            {

              errorMessage("not a valid date format");
            }
            format = m_Tokenizer.sval;
            readTillEOL();
          } else {
            m_Tokenizer.pushBack();
          }
          attributes.addElement(new Attribute(attributeName, format, attributes.size()));

        }
        else if (m_Tokenizer.sval.equalsIgnoreCase("relational"))
        {
          readTillEOL();
          


          FastVector atts = attributes;
          attributes = new FastVector();
          


          getFirstToken();
          if (m_Tokenizer.ttype == -1) {
            errorMessage("premature end of file");
          }
          for (;;) {
            if ("@attribute".equalsIgnoreCase(m_Tokenizer.sval)) {
              attributes = parseAttribute(attributes);
            } else { if ("@end".equalsIgnoreCase(m_Tokenizer.sval))
              {
                getNextToken();
                if (attributeName.equalsIgnoreCase(m_Tokenizer.sval)) break;
                errorMessage("declaration of subrelation " + attributeName + " must be terminated by " + "@end " + attributeName); break;
              }
              


              errorMessage("declaration of subrelation " + attributeName + " must be terminated by " + "@end " + attributeName);
            }
          }
          


          Instances relation = new Instances(attributeName, attributes, 0);
          attributes = atts;
          attributes.addElement(new Attribute(attributeName, relation, attributes.size()));
        }
        else {
          errorMessage("no valid attribute type or invalid enumeration");
        }
        
      }
      else
      {
        FastVector attributeValues = new FastVector();
        m_Tokenizer.pushBack();
        

        if (m_Tokenizer.nextToken() != 123) {
          errorMessage("{ expected at beginning of enumeration");
        }
        while (m_Tokenizer.nextToken() != 125) {
          if (m_Tokenizer.ttype == 10) {
            errorMessage("} expected at end of enumeration");
          } else {
            attributeValues.addElement(m_Tokenizer.sval);
          }
        }
        attributes.addElement(new Attribute(attributeName, attributeValues, attributes.size()));
      }
      

      getLastToken(false);
      getFirstToken();
      if (m_Tokenizer.ttype == -1) {
        errorMessage("premature end of file");
      }
      
      return attributes;
    }
    



    protected void readTillEOL()
      throws IOException
    {
      while (m_Tokenizer.nextToken() != 10) {}
      


      m_Tokenizer.pushBack();
    }
    




    public Instances getStructure()
    {
      return new Instances(m_Data, 0);
    }
    




    public Instances getData()
    {
      return m_Data;
    }
    





    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 11137 $");
    }
  }
  





  public String globalInfo()
  {
    return "Reads a source that is in arff (attribute relation file format) format. ";
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
    return "Arff data files";
  }
  




  public void reset()
    throws IOException
  {
    m_structure = null;
    m_ArffReader = null;
    setRetrieval(0);
    
    if ((m_File != null) && (!new File(m_File).isDirectory())) {
      setFile(new File(m_File));
    } else if ((m_URL != null) && (!m_URL.equals("http://"))) {
      setURL(m_URL);
    }
  }
  





  public void setSource(URL url)
    throws IOException
  {
    m_structure = null;
    setRetrieval(0);
    
    setSource(url.openStream());
    
    m_URL = url.toString();
    

    m_File = null;
  }
  





  public File retrieveFile()
  {
    return new File(m_File);
  }
  





  public void setFile(File file)
    throws IOException
  {
    m_File = file.getPath();
    setSource(file);
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
    if (m_structure == null) {
      if (m_sourceReader == null) {
        throw new IOException("No source has been specified");
      }
      try {
        m_ArffReader = new ArffReader(m_sourceReader, 1);
        m_structure = m_ArffReader.getStructure();
      } catch (Exception ex) {
        throw new IOException("Unable to determine structure as arff (Reason: " + ex.toString() + ").");
      }
    }
    

    return new Instances(m_structure, 0);
  }
  








  public Instances getDataSet()
    throws IOException
  {
    Instances insts = null;
    try {
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
      


      insts = new Instances(m_structure, 0);
      Instance inst; while ((inst = m_ArffReader.readInstance(m_structure)) != null) {
        insts.add(inst);
      }
    }
    finally
    {
      if (m_sourceReader != null)
      {
        m_sourceReader.close();
      }
    }
    
    return insts;
  }
  












  public Instance getNextInstance(Instances structure)
    throws IOException
  {
    m_structure = structure;
    
    if (getRetrieval() == 1) {
      throw new IOException("Cannot mix getting Instances in both incremental and batch modes");
    }
    
    setRetrieval(2);
    
    Instance current = null;
    if (m_sourceReader != null) {
      current = m_ArffReader.readInstance(m_structure);
    }
    
    if ((m_sourceReader != null) && (current == null)) {
      try
      {
        m_sourceReader.close();
        m_sourceReader = null;
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    return current;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11137 $");
  }
  




  public static void main(String[] args)
  {
    runFileLoader(new ArffLoader(), args);
  }
}
