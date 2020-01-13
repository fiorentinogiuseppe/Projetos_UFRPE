package weka.core;

import java.util.Enumeration;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;


































public abstract class CheckScheme
  extends Check
{
  public CheckScheme() {}
  
  public static class PostProcessor
    implements RevisionHandler
  {
    public PostProcessor() {}
    
    public Instances process(Instances data)
    {
      return data;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.4 $");
    }
  }
  

  protected int m_NumInstances = 20;
  

  protected int m_NumNominal = 2;
  

  protected int m_NumNumeric = 1;
  

  protected int m_NumString = 1;
  

  protected int m_NumDate = 1;
  

  protected int m_NumRelational = 1;
  


  protected int m_NumInstancesRelational = 10;
  

  protected String[] m_Words = TestInstances.DEFAULT_WORDS;
  

  protected String m_WordSeparators = " ";
  

  protected PostProcessor m_PostProcessor = null;
  

  protected boolean m_ClasspathProblems = false;
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tThe number of instances in the datasets (default 20).", "N", 1, "-N <num>"));
    


    result.addElement(new Option("\tThe number of nominal attributes (default 2).", "nominal", 1, "-nominal <num>"));
    


    result.addElement(new Option("\tThe number of values for nominal attributes (default 1).", "nominal-values", 1, "-nominal-values <num>"));
    


    result.addElement(new Option("\tThe number of numeric attributes (default 1).", "numeric", 1, "-numeric <num>"));
    


    result.addElement(new Option("\tThe number of string attributes (default 1).", "string", 1, "-string <num>"));
    


    result.addElement(new Option("\tThe number of date attributes (default 1).", "date", 1, "-date <num>"));
    


    result.addElement(new Option("\tThe number of relational attributes (default 1).", "relational", 1, "-relational <num>"));
    


    result.addElement(new Option("\tThe number of instances in relational/bag attributes (default 10).", "num-instances-relational", 1, "-num-instances-relational <num>"));
    


    result.addElement(new Option("\tThe words to use in string attributes.", "words", 1, "-words <comma-separated-list>"));
    


    result.addElement(new Option("\tThe word separators to use in string attributes.", "word-separators", 1, "-word-separators <chars>"));
    


    return result.elements();
  }
  






  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setNumInstances(Integer.parseInt(tmpStr));
    } else {
      setNumInstances(20);
    }
    tmpStr = Utils.getOption("nominal", options);
    if (tmpStr.length() != 0) {
      setNumNominal(Integer.parseInt(tmpStr));
    } else {
      setNumNominal(2);
    }
    tmpStr = Utils.getOption("numeric", options);
    if (tmpStr.length() != 0) {
      setNumNumeric(Integer.parseInt(tmpStr));
    } else {
      setNumNumeric(1);
    }
    tmpStr = Utils.getOption("string", options);
    if (tmpStr.length() != 0) {
      setNumString(Integer.parseInt(tmpStr));
    } else {
      setNumString(1);
    }
    tmpStr = Utils.getOption("date", options);
    if (tmpStr.length() != 0) {
      setNumDate(Integer.parseInt(tmpStr));
    } else {
      setNumDate(1);
    }
    tmpStr = Utils.getOption("relational", options);
    if (tmpStr.length() != 0) {
      setNumRelational(Integer.parseInt(tmpStr));
    } else {
      setNumRelational(1);
    }
    tmpStr = Utils.getOption("num-instances-relational", options);
    if (tmpStr.length() != 0) {
      setNumInstancesRelational(Integer.parseInt(tmpStr));
    } else {
      setNumInstancesRelational(10);
    }
    tmpStr = Utils.getOption("words", options);
    if (tmpStr.length() != 0) {
      setWords(tmpStr);
    } else {
      setWords(new TestInstances().getWords());
    }
    if (Utils.getOptionPos("word-separators", options) > -1) {
      tmpStr = Utils.getOption("word-separators", options);
      setWordSeparators(tmpStr);
    }
    else {
      setWordSeparators(" ");
    }
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-N");
    result.add("" + getNumInstances());
    
    result.add("-nominal");
    result.add("" + getNumNominal());
    
    result.add("-numeric");
    result.add("" + getNumNumeric());
    
    result.add("-string");
    result.add("" + getNumString());
    
    result.add("-date");
    result.add("" + getNumDate());
    
    result.add("-relational");
    result.add("" + getNumRelational());
    
    result.add("-words");
    result.add("" + getWords());
    
    result.add("-word-separators");
    result.add("" + getWordSeparators());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public void setPostProcessor(PostProcessor value)
  {
    m_PostProcessor = value;
  }
  




  public PostProcessor getPostProcessor()
  {
    return m_PostProcessor;
  }
  




  public boolean hasClasspathProblems()
  {
    return m_ClasspathProblems;
  }
  




  public abstract void doTests();
  




  public void setNumInstances(int value)
  {
    m_NumInstances = value;
  }
  




  public int getNumInstances()
  {
    return m_NumInstances;
  }
  




  public void setNumNominal(int value)
  {
    m_NumNominal = value;
  }
  




  public int getNumNominal()
  {
    return m_NumNominal;
  }
  




  public void setNumNumeric(int value)
  {
    m_NumNumeric = value;
  }
  




  public int getNumNumeric()
  {
    return m_NumNumeric;
  }
  




  public void setNumString(int value)
  {
    m_NumString = value;
  }
  




  public int getNumString()
  {
    return m_NumString;
  }
  




  public void setNumDate(int value)
  {
    m_NumDate = value;
  }
  




  public int getNumDate()
  {
    return m_NumDate;
  }
  




  public void setNumRelational(int value)
  {
    m_NumRelational = value;
  }
  




  public int getNumRelational()
  {
    return m_NumRelational;
  }
  




  public void setNumInstancesRelational(int value)
  {
    m_NumInstancesRelational = value;
  }
  




  public int getNumInstancesRelational()
  {
    return m_NumInstancesRelational;
  }
  








  protected static String[] listToArray(String value)
  {
    Vector list = new Vector();
    StringTokenizer tok = new StringTokenizer(value, ",");
    while (tok.hasMoreTokens()) {
      list.add(tok.nextToken());
    }
    return (String[])list.toArray(new String[list.size()]);
  }
  








  protected static String arrayToList(String[] value)
  {
    String result = "";
    
    for (int i = 0; i < value.length; i++) {
      if (i > 0)
        result = result + ",";
      result = result + value[i];
    }
    
    return result;
  }
  



  public static String attributeTypeToString(int type)
  {
    String result;
    


    switch (type) {
    case 0: 
      result = "numeric";
      break;
    
    case 1: 
      result = "nominal";
      break;
    
    case 2: 
      result = "string";
      break;
    
    case 3: 
      result = "date";
      break;
    
    case 4: 
      result = "relational";
      break;
    
    default: 
      result = "???";
    }
    
    return result;
  }
  






  public void setWords(String value)
  {
    if (listToArray(value).length < 2) {
      throw new IllegalArgumentException("At least 2 words must be provided!");
    }
    m_Words = listToArray(value);
  }
  




  public String getWords()
  {
    return arrayToList(m_Words);
  }
  




  public void setWordSeparators(String value)
  {
    m_WordSeparators = value;
  }
  




  public String getWordSeparators()
  {
    return m_WordSeparators;
  }
  







  protected void compareDatasets(Instances data1, Instances data2)
    throws Exception
  {
    if (!data2.equalHeaders(data1)) {
      throw new Exception("header has been modified");
    }
    if (data2.numInstances() != data1.numInstances()) {
      throw new Exception("number of instances has changed");
    }
    for (int i = 0; i < data2.numInstances(); i++) {
      Instance orig = data1.instance(i);
      Instance copy = data2.instance(i);
      for (int j = 0; j < orig.numAttributes(); j++) {
        if (orig.isMissing(j)) {
          if (!copy.isMissing(j)) {
            throw new Exception("instances have changed");
          }
        } else if (orig.value(j) != copy.value(j)) {
          throw new Exception("instances have changed");
        }
        if (orig.weight() != copy.weight()) {
          throw new Exception("instance weights have changed");
        }
      }
    }
  }
  











  protected void addMissing(Instances data, int level, boolean predictorMissing, boolean classMissing)
  {
    int classIndex = data.classIndex();
    Random random = new Random(1L);
    for (int i = 0; i < data.numInstances(); i++) {
      Instance current = data.instance(i);
      for (int j = 0; j < data.numAttributes(); j++) {
        if (((j == classIndex) && (classMissing)) || ((j != classIndex) && (predictorMissing)))
        {
          if (Math.abs(random.nextInt()) % 100 < level) {
            current.setMissing(j);
          }
        }
      }
    }
  }
  





  protected Instances process(Instances data)
  {
    if (getPostProcessor() == null) {
      return data;
    }
    return getPostProcessor().process(data);
  }
}
