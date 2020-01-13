package weka.core.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;




































public class C45Loader
  extends AbstractFileLoader
  implements BatchConverter, IncrementalConverter
{
  static final long serialVersionUID = 5454329403218219L;
  public static String FILE_EXTENSION = ".names";
  



  private File m_sourceFileData = null;
  



  private transient Reader m_namesReader = null;
  



  private transient Reader m_dataReader = null;
  


  private String m_fileStem;
  


  private int m_numAttribs;
  


  private boolean[] m_ignore;
  



  public C45Loader() {}
  



  public String globalInfo()
  {
    return "Reads a file that is C45 format. Can take a filestem or filestem with .names or .data appended. Assumes that path/<filestem>.names and path/<filestem>.data exist and contain the names and data respectively.";
  }
  







  public void reset()
    throws IOException
  {
    m_structure = null;
    setRetrieval(0);
    
    if (m_File != null) {
      setFile(new File(m_File));
    }
  }
  




  public String getFileExtension()
  {
    return FILE_EXTENSION;
  }
  




  public String[] getFileExtensions()
  {
    return new String[] { ".names", ".data" };
  }
  




  public String getFileDescription()
  {
    return "C4.5 data files";
  }
  





  public void setSource(File file)
    throws IOException
  {
    m_structure = null;
    setRetrieval(0);
    
    if (file == null) {
      throw new IOException("Source file object is null!");
    }
    
    String fname = file.getName();
    
    String path = file.getParent();
    if (path != null) {
      path = path + File.separator;
    } else
      path = "";
    String fileStem;
    if (fname.indexOf('.') < 0) {
      String fileStem = fname;
      fname = fname + ".names";
    } else {
      fileStem = fname.substring(0, fname.lastIndexOf('.'));
      fname = fileStem + ".names";
    }
    m_fileStem = fileStem;
    file = new File(path + fname);
    
    m_sourceFile = file;
    try {
      BufferedReader br = new BufferedReader(new FileReader(file));
      m_namesReader = br;
    } catch (FileNotFoundException ex) {
      throw new IOException("File not found : " + path + fname);
    }
    
    m_sourceFileData = new File(path + fileStem + ".data");
    try {
      BufferedReader br = new BufferedReader(new FileReader(m_sourceFileData));
      m_dataReader = br;
    } catch (FileNotFoundException ex) {
      throw new IOException("File not found : " + path + fname);
    }
    m_File = file.getAbsolutePath();
  }
  





  public Instances getStructure()
    throws IOException
  {
    if (m_sourceFile == null) {
      throw new IOException("No source has beenspecified");
    }
    
    if (m_structure == null) {
      setSource(m_sourceFile);
      StreamTokenizer st = new StreamTokenizer(m_namesReader);
      initTokenizer(st);
      readHeader(st);
    }
    
    return m_structure;
  }
  






  public Instances getDataSet()
    throws IOException
  {
    if (m_sourceFile == null) {
      throw new IOException("No source has been specified");
    }
    if (getRetrieval() == 2) {
      throw new IOException("Cannot mix getting Instances in both incremental and batch modes");
    }
    setRetrieval(1);
    if (m_structure == null) {
      getStructure();
    }
    StreamTokenizer st = new StreamTokenizer(m_dataReader);
    initTokenizer(st);
    
    Instances result = new Instances(m_structure);
    Instance current = getInstance(st);
    
    while (current != null) {
      result.add(current);
      current = getInstance(st);
    }
    try
    {
      m_dataReader.close();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    return result;
  }
  















  public Instance getNextInstance(Instances structure)
    throws IOException
  {
    if (m_sourceFile == null) {
      throw new IOException("No source has been specified");
    }
    
    if (getRetrieval() == 1) {
      throw new IOException("Cannot mix getting Instances in both incremental and batch modes");
    }
    setRetrieval(2);
    
    if (m_structure == null) {
      getStructure();
    }
    
    StreamTokenizer st = new StreamTokenizer(m_dataReader);
    initTokenizer(st);
    
    Instance nextI = getInstance(st);
    if (nextI != null) {
      nextI.setDataset(m_structure);
    }
    else {
      try
      {
        m_dataReader.close();
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    return nextI;
  }
  






  private Instance getInstance(StreamTokenizer tokenizer)
    throws IOException
  {
    double[] instance = new double[m_structure.numAttributes()];
    
    ConverterUtils.getFirstToken(tokenizer);
    if (ttype == -1) {
      return null;
    }
    
    int counter = 0;
    for (int i = 0; i < m_numAttribs; i++) {
      if (i > 0) {
        ConverterUtils.getToken(tokenizer);
      }
      
      if (m_ignore[i] == 0)
      {
        if (ttype == 63) {
          instance[(counter++)] = Instance.missingValue();
        } else {
          String val = sval;
          
          if (i == m_numAttribs - 1)
          {
            if (val.charAt(val.length() - 1) == '.') {
              val = val.substring(0, val.length() - 1);
            }
          }
          if (m_structure.attribute(counter).isNominal()) {
            int index = m_structure.attribute(counter).indexOfValue(val);
            if (index == -1) {
              ConverterUtils.errms(tokenizer, "nominal value not declared in header :" + val + " column " + i);
            }
            
            instance[(counter++)] = index;
          } else if (m_structure.attribute(counter).isNumeric()) {
            try {
              instance[(counter++)] = Double.valueOf(val).doubleValue();
            } catch (NumberFormatException e) {
              ConverterUtils.errms(tokenizer, "number expected");
            }
          } else {
            System.err.println("Shouldn't get here");
            System.exit(1);
          }
        }
      }
    }
    
    return new Instance(1.0D, instance);
  }
  






  private String removeTrailingPeriod(String val)
  {
    if (val.charAt(val.length() - 1) == '.') {
      val = val.substring(0, val.length() - 1);
    }
    return val;
  }
  





  private void readHeader(StreamTokenizer tokenizer)
    throws IOException
  {
    FastVector attribDefs = new FastVector();
    FastVector ignores = new FastVector();
    ConverterUtils.getFirstToken(tokenizer);
    if (ttype == -1) {
      ConverterUtils.errms(tokenizer, "premature end of file");
    }
    
    m_numAttribs = 1;
    
    FastVector classVals = new FastVector();
    while (ttype != 10) {
      String val = sval.trim();
      
      if (val.length() > 0) {
        val = removeTrailingPeriod(val);
        classVals.addElement(val);
      }
      ConverterUtils.getToken(tokenizer);
    }
    

    int counter = 0;
    while (ttype != -1) {
      ConverterUtils.getFirstToken(tokenizer);
      if (ttype != -1)
      {
        String attribName = sval;
        
        ConverterUtils.getToken(tokenizer);
        if (ttype == 10) {
          ConverterUtils.errms(tokenizer, "premature end of line. Expected attribute type.");
        }
        
        String temp = sval.toLowerCase().trim();
        if ((temp.startsWith("ignore")) || (temp.startsWith("label"))) {
          ignores.addElement(new Integer(counter));
          counter++;
        } else if (temp.startsWith("continuous")) {
          attribDefs.addElement(new Attribute(attribName));
          counter++;
        } else {
          counter++;
          
          FastVector attribVals = new FastVector();
          while ((ttype != 10) && (ttype != -1))
          {
            String val = sval.trim();
            
            if (val.length() > 0) {
              val = removeTrailingPeriod(val);
              attribVals.addElement(val);
            }
            ConverterUtils.getToken(tokenizer);
          }
          attribDefs.addElement(new Attribute(attribName, attribVals));
        }
      }
    }
    
    boolean ok = true;
    int i = -1;
    if (classVals.size() == 1)
    {
      for (i = 0; i < attribDefs.size(); i++) {
        if (((Attribute)attribDefs.elementAt(i)).name().compareTo((String)classVals.elementAt(0)) == 0)
        {
          ok = false;
          m_numAttribs -= 1;
          break;
        }
      }
    }
    
    if (ok) {
      attribDefs.addElement(new Attribute("Class", classVals));
    }
    
    m_structure = new Instances(m_fileStem, attribDefs, 0);
    try
    {
      if (ok) {
        m_structure.setClassIndex(m_structure.numAttributes() - 1);
      } else {
        m_structure.setClassIndex(i);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    m_numAttribs = (m_structure.numAttributes() + ignores.size());
    m_ignore = new boolean[m_numAttribs];
    for (i = 0; i < ignores.size(); i++) {
      m_ignore[((Integer)ignores.elementAt(i)).intValue()] = true;
    }
  }
  




  private void initTokenizer(StreamTokenizer tokenizer)
  {
    tokenizer.resetSyntax();
    tokenizer.whitespaceChars(0, 31);
    tokenizer.wordChars(32, 255);
    tokenizer.whitespaceChars(44, 44);
    tokenizer.whitespaceChars(58, 58);
    
    tokenizer.commentChar(124);
    tokenizer.whitespaceChars(9, 9);
    tokenizer.quoteChar(34);
    tokenizer.quoteChar(39);
    tokenizer.eolIsSignificant(true);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.16 $");
  }
  




  public static void main(String[] args)
  {
    runFileLoader(new C45Loader(), args);
  }
}
