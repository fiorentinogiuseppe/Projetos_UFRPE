package weka.core.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.Utils;



















































































public class CSVLoader
  extends AbstractFileLoader
  implements BatchConverter, OptionHandler
{
  static final long serialVersionUID = 5607529739745491340L;
  public static String FILE_EXTENSION = ".csv";
  


  protected FastVector m_cumulativeStructure;
  


  protected FastVector m_cumulativeInstances;
  


  protected transient BufferedReader m_sourceReader;
  


  protected transient StreamTokenizer m_st;
  

  protected Range m_NominalAttributes = new Range();
  

  protected Range m_StringAttributes = new Range();
  

  protected Range m_dateAttributes = new Range();
  

  protected String m_dateFormat = "";
  

  protected SimpleDateFormat m_formatter;
  

  protected String m_MissingValue = "?";
  

  protected boolean m_FirstCheck;
  

  protected String m_Enclosures = "\",'";
  



  public CSVLoader()
  {
    setRetrieval(0);
  }
  





  public String getFileExtension()
  {
    return FILE_EXTENSION;
  }
  





  public String getFileDescription()
  {
    return "CSV data files";
  }
  





  public String[] getFileExtensions()
  {
    return new String[] { getFileExtension() };
  }
  





  public String globalInfo()
  {
    return "Reads a source that is in comma separated or tab separated format. Assumes that the first row in the file determines the number of and names of the attributes.";
  }
  







  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe range of attributes to force type to be NOMINAL.\n\t'first' and 'last' are accepted as well.\n\tExamples: \"first-last\", \"1,4,5-27,50-last\"\n\t(default: -none-)", "N", 1, "-N <range>"));
    




    result.addElement(new Option("\tThe range of attribute to force type to be STRING.\n\t'first' and 'last' are accepted as well.\n\tExamples: \"first-last\", \"1,4,5-27,50-last\"\n\t(default: -none-)", "S", 1, "-S <range>"));
    




    result.add(new Option("\tThe range of attribute to force type to be DATE.\n\t'first' and 'last' are accepted as well.\n\tExamples: \"first-last\", \"1,4,5-27,50-last\"\n\t(default: -none-)", "D", 1, "-D <range>"));
    




    result.add(new Option("\tThe date formatting string to use to parse date values.\n\t(default: \"yyyy-MM-dd'T'HH:mm:ss\")", "format", 1, "-format <date format>"));
    



    result.addElement(new Option("\tThe string representing a missing value.\n\t(default: ?)", "M", 1, "-M <str>"));
    

    result.addElement(new Option("\tThe enclosure character(s) to use for strings.\n\tSpecify as a comma separated list (e.g. \",' (default: \",')", "E", 1, "-E <enclosures>"));
    




    return result.elements();
  }
  























































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setNominalAttributes(tmpStr);
    } else {
      setNominalAttributes("");
    }
    tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      setStringAttributes(tmpStr);
    } else {
      setStringAttributes("");
    }
    tmpStr = Utils.getOption('M', options);
    if (tmpStr.length() != 0) {
      setMissingValue(tmpStr);
    } else {
      setMissingValue("?");
    }
    tmpStr = Utils.getOption('D', options);
    if (tmpStr.length() > 0) {
      setDateAttributes(tmpStr);
    }
    tmpStr = Utils.getOption("format", options);
    if (tmpStr.length() > 0) {
      setDateFormat(tmpStr);
    }
    tmpStr = Utils.getOption("E", options);
    if (tmpStr.length() > 0) {
      setEnclosureCharacters(tmpStr);
    }
  }
  







  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    if (getNominalAttributes().length() > 0) {
      result.add("-N");
      result.add(getNominalAttributes());
    }
    
    if (getStringAttributes().length() > 0) {
      result.add("-S");
      result.add(getStringAttributes());
    }
    
    if (getDateAttributes().length() > 0) {
      result.add("-D");
      result.add(getDateAttributes());
      result.add("-format");
      result.add(getDateFormat());
    }
    
    result.add("-M");
    result.add(getMissingValue());
    
    result.add("-E");
    result.add(getEnclosureCharacters());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public void setNominalAttributes(String value)
  {
    m_NominalAttributes.setRanges(value);
  }
  




  public String getNominalAttributes()
  {
    return m_NominalAttributes.getRanges();
  }
  





  public String nominalAttributesTipText()
  {
    return "The range of attributes to force to be of type NOMINAL, example ranges: 'first-last', '1,4,7-14,50-last'.";
  }
  





  public void setStringAttributes(String value)
  {
    m_StringAttributes.setRanges(value);
  }
  




  public String getStringAttributes()
  {
    return m_StringAttributes.getRanges();
  }
  





  public String stringAttributesTipText()
  {
    return "The range of attributes to force to be of type STRING, example ranges: 'first-last', '1,4,7-14,50-last'.";
  }
  





  public void setDateAttributes(String value)
  {
    m_dateAttributes.setRanges(value);
  }
  




  public String getDateAttributes()
  {
    return m_dateAttributes.getRanges();
  }
  





  public String dateAttributesTipText()
  {
    return "The range of attributes to force to type STRING, example ranges: 'first-last', '1,4,7-14, 50-last'.";
  }
  





  public void setDateFormat(String value)
  {
    m_dateFormat = value;
    m_formatter = null;
  }
  





  public String getDateFormat()
  {
    return m_dateFormat;
  }
  





  public String dateFormatTipText()
  {
    return "The format to use for parsing date values.";
  }
  





  public String enclosureCharactersTipText()
  {
    return "The characters to use as enclosures for strings. E.g. \",'";
  }
  




  public void setEnclosureCharacters(String enclosure)
  {
    m_Enclosures = enclosure;
  }
  




  public String getEnclosureCharacters()
  {
    return m_Enclosures;
  }
  




  public void setMissingValue(String value)
  {
    m_MissingValue = value;
  }
  




  public String getMissingValue()
  {
    return m_MissingValue;
  }
  





  public String missingValueTipText()
  {
    return "The placeholder for missing values, default is '?'.";
  }
  






  public void setSource(InputStream input)
    throws IOException
  {
    m_structure = null;
    m_sourceFile = null;
    m_File = null;
    m_FirstCheck = true;
    
    m_sourceReader = new BufferedReader(new InputStreamReader(input));
  }
  






  public void setSource(File file)
    throws IOException
  {
    super.setSource(file);
  }
  






  public Instances getStructure()
    throws IOException
  {
    if ((m_sourceFile == null) && (m_sourceReader == null)) {
      throw new IOException("No source has been specified");
    }
    
    if (m_structure == null) {
      try {
        m_st = new StreamTokenizer(m_sourceReader);
        initTokenizer(m_st);
        readStructure(m_st);
      }
      catch (FileNotFoundException ex) {}
    }
    
    return m_structure;
  }
  




  private void readStructure(StreamTokenizer st)
    throws IOException
  {
    readHeader(st);
  }
  







  public Instances getDataSet()
    throws IOException
  {
    if ((m_sourceFile == null) && (m_sourceReader == null)) {
      throw new IOException("No source has been specified");
    }
    
    if (m_structure == null) {
      getStructure();
    }
    
    if (m_st == null) {
      m_st = new StreamTokenizer(m_sourceReader);
      initTokenizer(m_st);
    }
    
    m_st.ordinaryChar(44);
    m_st.ordinaryChar(9);
    
    m_cumulativeStructure = new FastVector(m_structure.numAttributes());
    for (int i = 0; i < m_structure.numAttributes(); i++) {
      m_cumulativeStructure.addElement(new Hashtable());
    }
    
    m_cumulativeInstances = new FastVector();
    FastVector current;
    while ((current = getInstance(m_st)) != null) {
      m_cumulativeInstances.addElement(current);
    }
    
    FastVector atts = new FastVector(m_structure.numAttributes());
    for (int i = 0; i < m_structure.numAttributes(); i++) {
      String attname = m_structure.attribute(i).name();
      Hashtable tempHash = (Hashtable)m_cumulativeStructure.elementAt(i);
      if (tempHash.size() == 0) {
        if (m_dateAttributes.isInRange(i)) {
          atts.addElement(new Attribute(attname, m_dateFormat));
        } else {
          atts.addElement(new Attribute(attname));
        }
      }
      else if (m_StringAttributes.isInRange(i)) {
        atts.addElement(new Attribute(attname, (FastVector)null));
      } else {
        FastVector values = new FastVector(tempHash.size());
        

        for (int z = 0; z < tempHash.size(); z++) {
          values.addElement("dummy");
        }
        Enumeration e = tempHash.keys();
        while (e.hasMoreElements()) {
          Object ob = e.nextElement();
          
          int index = ((Integer)tempHash.get(ob)).intValue();
          String s = ob.toString();
          if ((s.startsWith("'")) || (s.startsWith("\"")))
            s = s.substring(1, s.length() - 1);
          values.setElementAt(new String(s), index);
        }
        
        atts.addElement(new Attribute(attname, values));
      }
    }
    
    String relationName;
    
    String relationName;
    if (m_sourceFile != null) {
      relationName = m_sourceFile.getName().replaceAll("\\.[cC][sS][vV]$", "");
    }
    else
      relationName = "stream";
    Instances dataSet = new Instances(relationName, atts, m_cumulativeInstances.size());
    

    for (int i = 0; i < m_cumulativeInstances.size(); i++) {
      current = (FastVector)m_cumulativeInstances.elementAt(i);
      double[] vals = new double[dataSet.numAttributes()];
      for (int j = 0; j < current.size(); j++) {
        Object cval = current.elementAt(j);
        if ((cval instanceof String)) {
          if (((String)cval).compareTo(m_MissingValue) == 0) {
            vals[j] = Instance.missingValue();
          }
          else if (dataSet.attribute(j).isString()) {
            vals[j] = dataSet.attribute(j).addStringValue((String)cval);
          } else if (dataSet.attribute(j).isNominal())
          {
            Hashtable lookup = (Hashtable)m_cumulativeStructure.elementAt(j);
            int index = ((Integer)lookup.get(cval)).intValue();
            vals[j] = index;
          } else {
            throw new IllegalStateException("Wrong attribute type at position " + (i + 1) + "!!!");
          }
          
        }
        else if (dataSet.attribute(j).isNominal())
        {
          Hashtable lookup = (Hashtable)m_cumulativeStructure.elementAt(j);
          int index = ((Integer)lookup.get(cval)).intValue();
          vals[j] = index;
        } else if (dataSet.attribute(j).isString()) {
          vals[j] = dataSet.attribute(j).addStringValue("" + cval);
        } else {
          vals[j] = ((Double)cval).doubleValue();
        }
      }
      dataSet.add(new Instance(1.0D, vals));
    }
    m_structure = new Instances(dataSet, 0);
    setRetrieval(1);
    m_cumulativeStructure = null;
    m_cumulativeInstances = null;
    

    m_sourceReader.close();
    
    return dataSet;
  }
  







  public Instance getNextInstance(Instances structure)
    throws IOException
  {
    throw new IOException("CSVLoader can't read data sets incrementally.");
  }
  




















  private FastVector getInstance(StreamTokenizer tokenizer)
    throws IOException
  {
    FastVector current = new FastVector();
    

    ConverterUtils.getFirstToken(tokenizer);
    if (ttype == -1) {
      return null;
    }
    boolean first = true;
    


    while ((ttype != 10) && (ttype != -1))
    {

      if (!first)
        ConverterUtils.getToken(tokenizer);
      boolean wasSep;
      boolean wasSep;
      if ((ttype == 44) || (ttype == 9) || (ttype == 10))
      {
        current.addElement(m_MissingValue);
        wasSep = true;
      } else {
        wasSep = false;
        if ((sval.equals(m_MissingValue)) || (sval.trim().length() == 0))
        {
          current.addElement(new String(m_MissingValue));
        } else {
          try
          {
            double val = Double.valueOf(sval).doubleValue();
            current.addElement(new Double(val));
          }
          catch (NumberFormatException e) {
            current.addElement(new String(sval));
          }
        }
      }
      
      if (!wasSep) {
        ConverterUtils.getToken(tokenizer);
      }
      first = false;
    }
    

    if (current.size() != m_structure.numAttributes()) {
      ConverterUtils.errms(tokenizer, "wrong number of values. Read " + current.size() + ", expected " + m_structure.numAttributes());
    }
    


    try
    {
      checkStructure(current);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    return current;
  }
  



















  private void checkStructure(FastVector current)
    throws Exception
  {
    if (current == null) {
      throw new Exception("current shouldn't be null in checkStructure");
    }
    

    if (m_FirstCheck) {
      m_NominalAttributes.setUpper(current.size() - 1);
      m_StringAttributes.setUpper(current.size() - 1);
      m_dateAttributes.setUpper(current.size() - 1);
      m_FirstCheck = false;
    }
    
    for (int i = 0; i < current.size(); i++) {
      Object ob = current.elementAt(i);
      if (((ob instanceof String)) || (m_NominalAttributes.isInRange(i)) || (m_StringAttributes.isInRange(i)) || (m_dateAttributes.isInRange(i)))
      {
        if (ob.toString().compareTo(m_MissingValue) != 0)
        {


          boolean notDate = true;
          if (m_dateAttributes.isInRange(i))
          {
            if (m_formatter == null) {
              m_formatter = new SimpleDateFormat(m_dateFormat);
            }
            try
            {
              long time = m_formatter.parse(ob.toString()).getTime();
              Double timeL = new Double(time);
              current.setElementAt(timeL, i);
              notDate = false;
            } catch (ParseException e) {
              notDate = true;
            }
          }
          
          if (notDate) {
            Hashtable tempHash = (Hashtable)m_cumulativeStructure.elementAt(i);
            if (!tempHash.containsKey(ob))
            {


              if (tempHash.size() == 0) {
                for (int j = 0; j < m_cumulativeInstances.size(); j++) {
                  FastVector tempUpdate = (FastVector)m_cumulativeInstances.elementAt(j);
                  
                  Object tempO = tempUpdate.elementAt(i);
                  if (!(tempO instanceof String))
                  {

                    if (!tempHash.containsKey(tempO)) {
                      tempHash.put(new Double(((Double)tempO).doubleValue()), new Integer(tempHash.size()));
                    }
                  }
                }
              }
              
              int newIndex = tempHash.size();
              tempHash.put(ob, new Integer(newIndex));
            }
          }
        }
      } else if ((ob instanceof Double)) {
        Hashtable tempHash = (Hashtable)m_cumulativeStructure.elementAt(i);
        if ((tempHash.size() != 0) && 
          (!tempHash.containsKey(ob))) {
          int newIndex = tempHash.size();
          tempHash.put(new Double(((Double)ob).doubleValue()), new Integer(newIndex));
        }
      }
      else
      {
        throw new Exception("Wrong object type in checkStructure!");
      }
    }
  }
  





















  private void readHeader(StreamTokenizer tokenizer)
    throws IOException
  {
    FastVector attribNames = new FastVector();
    ConverterUtils.getFirstToken(tokenizer);
    if (ttype == -1) {
      ConverterUtils.errms(tokenizer, "premature end of file");
    }
    
    while (ttype != 10) {
      attribNames.addElement(new Attribute(sval, (FastVector)null));
      ConverterUtils.getToken(tokenizer); }
    String relationName;
    String relationName;
    if (m_sourceFile != null) {
      relationName = m_sourceFile.getName().replaceAll("\\.[cC][sS][vV]$", "");
    }
    else
      relationName = "stream";
    m_structure = new Instances(relationName, attribNames, 0);
  }
  




  private void initTokenizer(StreamTokenizer tokenizer)
  {
    tokenizer.resetSyntax();
    tokenizer.whitespaceChars(0, 31);
    tokenizer.wordChars(32, 255);
    tokenizer.whitespaceChars(44, 44);
    tokenizer.whitespaceChars(9, 9);
    tokenizer.commentChar(37);
    
    String[] parts = m_Enclosures.split(",");
    for (String e : parts) {
      if ((e.length() > 1) || (e.length() == 0)) {
        throw new IllegalArgumentException("Enclosures can only be single characters");
      }
      
      tokenizer.quoteChar(e.charAt(0));
    }
    
    tokenizer.eolIsSignificant(true);
  }
  




  public void reset()
    throws IOException
  {
    m_structure = null;
    m_cumulativeStructure = null;
    m_cumulativeInstances = null;
    m_st = null;
    setRetrieval(0);
    
    if (m_File != null) {
      setFile(new File(m_File));
    }
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10372 $");
  }
  




  public static void main(String[] args)
  {
    runFileLoader(new CSVLoader(), args);
  }
}
