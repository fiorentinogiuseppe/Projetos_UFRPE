package weka.core;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
























































































































public class TestInstances
  implements Cloneable, Serializable, OptionHandler, RevisionHandler
{
  private static final long serialVersionUID = -6263968936330390469L;
  public static final int CLASS_IS_LAST = -1;
  public static final int NO_CLASS = -2;
  public static final String[] DEFAULT_WORDS = { "The", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog" };
  

  public static final String DEFAULT_SEPARATORS = " ";
  

  protected String[] m_Words = DEFAULT_WORDS;
  

  protected String m_WordSeparators = " ";
  

  protected String m_Relation = "Testdata";
  

  protected int m_Seed = 1;
  

  protected Random m_Random = new Random(m_Seed);
  

  protected int m_NumInstances = 20;
  

  protected int m_ClassType = 1;
  

  protected int m_NumClasses = 2;
  



  protected int m_ClassIndex = -1;
  

  protected int m_NumNominal = 1;
  

  protected int m_NumNominalValues = 2;
  

  protected int m_NumNumeric = 0;
  

  protected int m_NumString = 0;
  

  protected int m_NumDate = 0;
  

  protected int m_NumRelational = 0;
  

  protected int m_NumRelationalNominal = 1;
  

  protected int m_NumRelationalNominalValues = 2;
  

  protected int m_NumRelationalNumeric = 0;
  

  protected int m_NumRelationalString = 0;
  

  protected int m_NumRelationalDate = 0;
  

  protected boolean m_MultiInstance = false;
  


  protected int m_NumInstancesRelational = 10;
  

  protected Instances[] m_RelationalFormat = null;
  

  protected Instances m_RelationalClassFormat = null;
  

  protected Instances m_Data = null;
  

  protected CapabilitiesHandler m_Handler = null;
  




  public TestInstances()
  {
    setRelation("Testdata");
    setSeed(1);
    setNumInstances(20);
    setClassType(1);
    setNumClasses(2);
    setClassIndex(-1);
    setNumNominal(1);
    setNumNominalValues(2);
    setNumNumeric(0);
    setNumString(0);
    setNumDate(0);
    setNumRelational(0);
    setNumRelationalNominal(1);
    setNumRelationalNominalValues(2);
    setNumRelationalNumeric(0);
    setNumRelationalString(0);
    setNumRelationalDate(0);
    setNumInstancesRelational(10);
    setMultiInstance(false);
    setWords(arrayToList(DEFAULT_WORDS));
    setWordSeparators(" ");
  }
  






  public Object clone()
  {
    TestInstances result = new TestInstances();
    result.assign(this);
    
    return result;
  }
  





  public void assign(TestInstances t)
  {
    setRelation(t.getRelation());
    setSeed(t.getSeed());
    setNumInstances(t.getNumInstances());
    setClassType(t.getClassType());
    setNumClasses(t.getNumClasses());
    setClassIndex(t.getClassIndex());
    setNumNominal(t.getNumNominal());
    setNumNominalValues(t.getNumNominalValues());
    setNumNumeric(t.getNumNumeric());
    setNumString(t.getNumString());
    setNumDate(t.getNumDate());
    setNumRelational(t.getNumRelational());
    setNumRelationalNominal(t.getNumRelationalNominal());
    setNumRelationalNominalValues(t.getNumRelationalNominalValues());
    setNumRelationalNumeric(t.getNumRelationalNumeric());
    setNumRelationalString(t.getNumRelationalString());
    setNumRelationalDate(t.getNumRelationalDate());
    setMultiInstance(t.getMultiInstance());
    for (int i = 0; i < t.getNumRelational(); i++)
      setRelationalFormat(i, t.getRelationalFormat(i));
    setRelationalClassFormat(t.getRelationalClassFormat());
    setNumInstancesRelational(t.getNumInstancesRelational());
    setWords(t.getWords());
    setWordSeparators(t.getWordSeparators());
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe name of the data set.", "relation", 1, "-relation <name>"));
    


    result.addElement(new Option("\tThe seed value.", "seed", 1, "-seed <num>"));
    


    result.addElement(new Option("\tThe number of instances in the datasets (default 20).", "num-instances", 1, "-num-instances <num>"));
    


    result.addElement(new Option("\tThe class type, see constants in weka.core.Attribute\n\t(default 1=nominal).", "class-type", 1, "-class-type <num>"));
    



    result.addElement(new Option("\tThe number of classes to generate (for nominal classes only)\n\t(default 2).", "class-values", 1, "-class-values <num>"));
    



    result.addElement(new Option("\tThe class index, with -1=last, (default -1).", "class-index", 1, "-class-index <num>"));
    


    result.addElement(new Option("\tDoesn't include a class attribute in the output.", "no-class", 0, "-no-class"));
    


    result.addElement(new Option("\tThe number of nominal attributes (default 1).", "nominal", 1, "-nominal <num>"));
    


    result.addElement(new Option("\tThe number of values for nominal attributes (default 2).", "nominal-values", 1, "-nominal-values <num>"));
    


    result.addElement(new Option("\tThe number of numeric attributes (default 0).", "numeric", 1, "-numeric <num>"));
    


    result.addElement(new Option("\tThe number of string attributes (default 0).", "string", 1, "-string <num>"));
    


    result.addElement(new Option("\tThe words to use in string attributes.", "words", 1, "-words <comma-separated-list>"));
    


    result.addElement(new Option("\tThe word separators to use in string attributes.", "word-separators", 1, "-word-separators <chars>"));
    


    result.addElement(new Option("\tThe number of date attributes (default 0).", "date", 1, "-date <num>"));
    


    result.addElement(new Option("\tThe number of relational attributes (default 0).", "relational", 1, "-relational <num>"));
    


    result.addElement(new Option("\tThe number of nominal attributes in a rel. attribute (default 1).", "relational-nominal", 1, "-relational-nominal <num>"));
    


    result.addElement(new Option("\tThe number of values for nominal attributes in a rel. attribute (default 2).", "relational-nominal-values", 1, "-relational-nominal-values <num>"));
    


    result.addElement(new Option("\tThe number of numeric attributes in a rel. attribute (default 0).", "relational-numeric", 1, "-relational-numeric <num>"));
    


    result.addElement(new Option("\tThe number of string attributes in a rel. attribute (default 0).", "relational-string", 1, "-relational-string <num>"));
    


    result.addElement(new Option("\tThe number of date attributes in a rel. attribute (default 0).", "relational-date", 1, "-relational-date <num>"));
    


    result.addElement(new Option("\tThe number of instances in relational/bag attributes (default 10).", "num-instances-relational", 1, "-num-instances-relational <num>"));
    


    result.addElement(new Option("\tGenerates multi-instance data.", "multi-instance", 0, "-multi-instance"));
    


    result.addElement(new Option("\tThe Capabilities handler to base the dataset on.\n\tThe other parameters can be used to override the ones\n\tdetermined from the handler. Additional parameters for\n\thandler can be passed on after the '--'.", "W", 1, "-W <classname>"));
    





    return result.elements();
  }
  
























































































  public void setOptions(String[] options)
    throws Exception
  {
    boolean initialized = false;
    
    String tmpStr = Utils.getOption('W', options);
    if (tmpStr.length() > 0) {
      Class cls = Class.forName(tmpStr);
      if (ClassDiscovery.hasInterface(CapabilitiesHandler.class, cls)) {
        initialized = true;
        CapabilitiesHandler handler = (CapabilitiesHandler)cls.newInstance();
        if ((handler instanceof OptionHandler))
          ((OptionHandler)handler).setOptions(Utils.partitionOptions(options));
        setHandler(handler);
        
        assign(forCapabilities(handler.getCapabilities()));
      }
      else {
        throw new IllegalArgumentException("Class '" + tmpStr + "' is not a CapabilitiesHandler!");
      }
    }
    
    tmpStr = Utils.getOption("relation", options);
    if (tmpStr.length() != 0) {
      setRelation(tmpStr);
    } else if (!initialized) {
      setRelation("Testdata");
    }
    tmpStr = Utils.getOption("seed", options);
    if (tmpStr.length() != 0) {
      setSeed(Integer.parseInt(tmpStr));
    } else if (!initialized) {
      setSeed(1);
    }
    tmpStr = Utils.getOption("num-instances", options);
    if (tmpStr.length() != 0) {
      setNumInstances(Integer.parseInt(tmpStr));
    } else if (!initialized) {
      setNumInstances(20);
    }
    setNoClass(Utils.getFlag("no-class", options));
    
    if (!getNoClass()) {
      tmpStr = Utils.getOption("class-type", options);
      if (tmpStr.length() != 0) {
        setClassType(Integer.parseInt(tmpStr));
      } else if (!initialized) {
        setClassType(1);
      }
      tmpStr = Utils.getOption("class-values", options);
      if (tmpStr.length() != 0) {
        setNumClasses(Integer.parseInt(tmpStr));
      } else if (!initialized) {
        setNumClasses(2);
      }
      tmpStr = Utils.getOption("class-index", options);
      if (tmpStr.length() != 0) {
        setClassIndex(Integer.parseInt(tmpStr));
      } else if (!initialized) {
        setClassIndex(-1);
      }
    }
    tmpStr = Utils.getOption("nominal", options);
    if (tmpStr.length() != 0) {
      setNumNominal(Integer.parseInt(tmpStr));
    } else if (!initialized) {
      setNumNominal(1);
    }
    tmpStr = Utils.getOption("nominal-values", options);
    if (tmpStr.length() != 0) {
      setNumNominalValues(Integer.parseInt(tmpStr));
    } else if (!initialized) {
      setNumNominalValues(2);
    }
    tmpStr = Utils.getOption("numeric", options);
    if (tmpStr.length() != 0) {
      setNumNumeric(Integer.parseInt(tmpStr));
    } else if (!initialized) {
      setNumNumeric(0);
    }
    tmpStr = Utils.getOption("string", options);
    if (tmpStr.length() != 0) {
      setNumString(Integer.parseInt(tmpStr));
    } else if (!initialized) {
      setNumString(0);
    }
    tmpStr = Utils.getOption("words", options);
    if (tmpStr.length() != 0) {
      setWords(tmpStr);
    } else if (!initialized) {
      setWords(arrayToList(DEFAULT_WORDS));
    }
    if (Utils.getOptionPos("word-separators", options) > -1) {
      tmpStr = Utils.getOption("word-separators", options);
      setWordSeparators(tmpStr);
    }
    else if (!initialized) {
      setWordSeparators(" ");
    }
    
    tmpStr = Utils.getOption("date", options);
    if (tmpStr.length() != 0) {
      setNumDate(Integer.parseInt(tmpStr));
    } else if (!initialized) {
      setNumDate(0);
    }
    tmpStr = Utils.getOption("relational", options);
    if (tmpStr.length() != 0) {
      setNumRelational(Integer.parseInt(tmpStr));
    } else if (!initialized) {
      setNumRelational(0);
    }
    tmpStr = Utils.getOption("relational-nominal", options);
    if (tmpStr.length() != 0) {
      setNumRelationalNominal(Integer.parseInt(tmpStr));
    } else if (!initialized) {
      setNumRelationalNominal(1);
    }
    tmpStr = Utils.getOption("relational-nominal-values", options);
    if (tmpStr.length() != 0) {
      setNumRelationalNominalValues(Integer.parseInt(tmpStr));
    } else if (!initialized) {
      setNumRelationalNominalValues(2);
    }
    tmpStr = Utils.getOption("relational-numeric", options);
    if (tmpStr.length() != 0) {
      setNumRelationalNumeric(Integer.parseInt(tmpStr));
    } else if (!initialized) {
      setNumRelationalNumeric(0);
    }
    tmpStr = Utils.getOption("relational-string", options);
    if (tmpStr.length() != 0) {
      setNumRelationalString(Integer.parseInt(tmpStr));
    } else if (!initialized) {
      setNumRelationalString(0);
    }
    tmpStr = Utils.getOption("num-instances-relational", options);
    if (tmpStr.length() != 0) {
      setNumInstancesRelational(Integer.parseInt(tmpStr));
    } else if (!initialized) {
      setNumInstancesRelational(10);
    }
    if (!initialized) {
      setMultiInstance(Utils.getFlag("multi-instance", options));
    }
  }
  







  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-relation");
    result.add(getRelation());
    
    result.add("-seed");
    result.add("" + getSeed());
    
    result.add("-num-instances");
    result.add("" + getNumInstances());
    
    if (getNoClass()) {
      result.add("-no-class");
    }
    else {
      result.add("-class-type");
      result.add("" + getClassType());
      
      result.add("-class-values");
      result.add("" + getNumClasses());
      
      result.add("-class-index");
      result.add("" + getClassIndex());
    }
    
    result.add("-nominal");
    result.add("" + getNumNominal());
    
    result.add("-nominal-values");
    result.add("" + getNumNominalValues());
    
    result.add("-numeric");
    result.add("" + getNumNumeric());
    
    result.add("-string");
    result.add("" + getNumString());
    
    result.add("-words");
    result.add("" + getWords());
    
    result.add("-word-separators");
    result.add("" + getWordSeparators());
    
    result.add("-date");
    result.add("" + getNumDate());
    
    result.add("-relational");
    result.add("" + getNumRelational());
    
    result.add("-relational-nominal");
    result.add("" + getNumRelationalNominal());
    
    result.add("-relational-nominal-values");
    result.add("" + getNumRelationalNominalValues());
    
    result.add("-relational-numeric");
    result.add("" + getNumRelationalNumeric());
    
    result.add("-relational-string");
    result.add("" + getNumRelationalString());
    
    result.add("-relational-date");
    result.add("" + getNumRelationalDate());
    
    result.add("-num-instances-relational");
    result.add("" + getNumInstancesRelational());
    
    if (getMultiInstance()) {
      result.add("-multi-instance");
    }
    if (getHandler() != null) {
      result.add("-W");
      result.add(getHandler().getClass().getName());
      if ((getHandler() instanceof OptionHandler)) {
        result.add("--");
        String[] options = ((OptionHandler)getHandler()).getOptions();
        for (int i = 0; i < options.length; i++) {
          result.add(options[i]);
        }
      }
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public void setRelation(String value)
  {
    m_Relation = value;
  }
  




  public String getRelation()
  {
    return m_Relation;
  }
  




  public void setSeed(int value)
  {
    m_Seed = value;
    m_Random = new Random(m_Seed);
  }
  




  public int getSeed()
  {
    return m_Seed;
  }
  




  public void setNumInstances(int value)
  {
    m_NumInstances = value;
  }
  




  public int getNumInstances()
  {
    return m_NumInstances;
  }
  




  public void setClassType(int value)
  {
    m_ClassType = value;
    m_RelationalClassFormat = null;
  }
  




  public int getClassType()
  {
    return m_ClassType;
  }
  




  public void setNumClasses(int value)
  {
    m_NumClasses = value;
  }
  




  public int getNumClasses()
  {
    return m_NumClasses;
  }
  






  public void setClassIndex(int value)
  {
    m_ClassIndex = value;
  }
  






  public int getClassIndex()
  {
    return m_ClassIndex;
  }
  







  public void setNoClass(boolean value)
  {
    if (value) {
      setClassIndex(-2);
    } else {
      setClassIndex(-1);
    }
  }
  



  public boolean getNoClass()
  {
    return getClassIndex() == -2;
  }
  




  public void setNumNominal(int value)
  {
    m_NumNominal = value;
  }
  




  public int getNumNominal()
  {
    return m_NumNominal;
  }
  




  public void setNumNominalValues(int value)
  {
    m_NumNominalValues = value;
  }
  




  public int getNumNominalValues()
  {
    return m_NumNominalValues;
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
    m_RelationalFormat = new Instances[value];
  }
  




  public int getNumRelational()
  {
    return m_NumRelational;
  }
  




  public void setNumRelationalNominal(int value)
  {
    m_NumRelationalNominal = value;
  }
  




  public int getNumRelationalNominal()
  {
    return m_NumRelationalNominal;
  }
  




  public void setNumRelationalNominalValues(int value)
  {
    m_NumRelationalNominalValues = value;
  }
  




  public int getNumRelationalNominalValues()
  {
    return m_NumRelationalNominalValues;
  }
  




  public void setNumRelationalNumeric(int value)
  {
    m_NumRelationalNumeric = value;
  }
  




  public int getNumRelationalNumeric()
  {
    return m_NumRelationalNumeric;
  }
  




  public void setNumRelationalString(int value)
  {
    m_NumRelationalString = value;
  }
  




  public int getNumRelationalString()
  {
    return m_NumRelationalString;
  }
  




  public void setNumRelationalDate(int value)
  {
    m_NumRelationalDate = value;
  }
  




  public int getNumRelationalDate()
  {
    return m_NumRelationalDate;
  }
  




  public void setNumInstancesRelational(int value)
  {
    m_NumInstancesRelational = value;
  }
  




  public int getNumInstancesRelational()
  {
    return m_NumInstancesRelational;
  }
  





  public void setMultiInstance(boolean value)
  {
    m_MultiInstance = value;
  }
  




  public boolean getMultiInstance()
  {
    return m_MultiInstance;
  }
  





  public void setRelationalFormat(int index, Instances value)
  {
    if (value != null) {
      m_RelationalFormat[index] = new Instances(value, 0);
    } else {
      m_RelationalFormat[index] = null;
    }
  }
  




  public Instances getRelationalFormat(int index)
  {
    return m_RelationalFormat[index];
  }
  




  public void setRelationalClassFormat(Instances value)
  {
    if (value != null) {
      m_RelationalClassFormat = new Instances(value, 0);
    } else {
      m_RelationalClassFormat = null;
    }
  }
  




  public Instances getRelationalClassFormat()
  {
    return m_RelationalClassFormat;
  }
  







  public int getNumAttributes()
  {
    int result = m_NumNominal + m_NumNumeric + m_NumString + m_NumDate + m_NumRelational;
    
    if (!getNoClass()) {
      result++;
    }
    return result;
  }
  




  public Instances getData()
  {
    return m_Data;
  }
  




  public void setHandler(CapabilitiesHandler value)
  {
    m_Handler = value;
  }
  





  public CapabilitiesHandler getHandler()
  {
    return m_Handler;
  }
  

















  protected Attribute generateAttribute(int index, int attType, String namePrefix)
    throws Exception
  {
    Attribute result = null;
    int nomCount;
    int valIndex;
    int nomCount; String prefix; String name; if (index == -1) {
      int valIndex = 0;
      String name = "Class";
      String prefix = "class";
      nomCount = getNumClasses();
    }
    else {
      valIndex = index;
      nomCount = getNumNominalValues();
      prefix = "att" + (valIndex + 1) + "val";
      
      switch (attType) {
      case 1: 
        name = "Nominal" + (valIndex + 1);
        break;
      
      case 0: 
        name = "Numeric" + (valIndex + 1);
        break;
      
      case 2: 
        name = "String" + (valIndex + 1);
        break;
      
      case 3: 
        name = "Date" + (valIndex + 1);
        break;
      
      case 4: 
        name = "Relational" + (valIndex + 1);
        break;
      
      default: 
        throw new IllegalArgumentException("Attribute type '" + attType + "' unknown!");
      }
      
    }
    switch (attType) {
    case 1: 
      FastVector nomStrings = new FastVector(valIndex + 1);
      for (int j = 0; j < nomCount; j++)
        nomStrings.addElement(prefix + (j + 1));
      result = new Attribute(namePrefix + name, nomStrings);
      break;
    
    case 0: 
      result = new Attribute(namePrefix + name);
      break;
    
    case 2: 
      result = new Attribute(namePrefix + name, (FastVector)null);
      break;
    
    case 3: 
      result = new Attribute(namePrefix + name, "yyyy-mm-dd");
      break;
    case 4: 
      Instances rel;
      Instances rel;
      if (index == -1) {
        rel = getRelationalClassFormat();
      } else {
        rel = getRelationalFormat(index);
      }
      if (rel == null) {
        TestInstances dataset = new TestInstances();
        dataset.setNumNominal(getNumRelationalNominal());
        dataset.setNumNominalValues(getNumRelationalNominalValues());
        dataset.setNumNumeric(getNumRelationalNumeric());
        dataset.setNumString(getNumRelationalString());
        dataset.setNumDate(getNumRelationalDate());
        dataset.setNumInstances(0);
        dataset.setClassType(1);
        rel = new Instances(dataset.generate());
        if (!getNoClass()) {
          int clsIndex = rel.classIndex();
          rel.setClassIndex(-1);
          rel.deleteAttributeAt(clsIndex);
        }
      }
      result = new Attribute(namePrefix + name, rel);
      break;
    
    default: 
      throw new IllegalArgumentException("Attribute type '" + attType + "' unknown!");
    }
    
    return result;
  }
  





  protected double generateClassValue(Instances data)
    throws Exception
  {
    double result = NaN.0D;
    
    switch (m_ClassType) {
    case 0: 
      result = m_Random.nextFloat() * 0.25D + Math.abs(m_Random.nextInt()) % Math.max(2, m_NumNominal);
      

      break;
    
    case 1: 
      result = Math.abs(m_Random.nextInt()) % data.numClasses();
      break;
    
    case 2: 
      String str = "";
      for (int n = 0; n < m_Words.length; n++) {
        if ((n > 0) && (m_WordSeparators.length() != 0))
          str = str + m_WordSeparators.charAt(m_Random.nextInt(m_WordSeparators.length()));
        str = str + m_Words[m_Random.nextInt(m_Words.length)];
      }
      result = data.classAttribute().addStringValue(str);
      break;
    
    case 3: 
      result = data.classAttribute().parseDate(2000 + m_Random.nextInt(100) + "-01-01");
      
      break;
    
    case 4: 
      if (getRelationalClassFormat() != null) {
        result = data.classAttribute().addRelation(getRelationalClassFormat());
      }
      else {
        TestInstances dataset = new TestInstances();
        dataset.setNumNominal(getNumRelationalNominal());
        dataset.setNumNominalValues(getNumRelationalNominalValues());
        dataset.setNumNumeric(getNumRelationalNumeric());
        dataset.setNumString(getNumRelationalString());
        dataset.setNumDate(getNumRelationalDate());
        dataset.setNumInstances(getNumInstancesRelational());
        dataset.setClassType(1);
        Instances rel = new Instances(dataset.generate());
        int clsIndex = rel.classIndex();
        rel.setClassIndex(-1);
        rel.deleteAttributeAt(clsIndex);
        result = data.classAttribute().addRelation(rel);
      }
      break;
    }
    
    return result;
  }
  









  protected double generateAttributeValue(Instances data, int index, double classVal)
    throws Exception
  {
    double result = NaN.0D;
    
    switch (data.attribute(index).type()) {
    case 0: 
      result = classVal * 4.0D + m_Random.nextFloat() * 1.0F - 0.5D;
      break;
    
    case 1: 
      if (m_Random.nextFloat() < 0.2D) {
        result = Math.abs(m_Random.nextInt()) % data.attribute(index).numValues();
      }
      else {
        result = (int)classVal % data.attribute(index).numValues();
      }
      
      break;
    
    case 2: 
      String str = "";
      for (int n = 0; n < m_Words.length; n++) {
        if ((n > 0) && (m_WordSeparators.length() != 0))
          str = str + m_WordSeparators.charAt(m_Random.nextInt(m_WordSeparators.length()));
        str = str + m_Words[m_Random.nextInt(m_Words.length)];
      }
      result = data.attribute(index).addStringValue(str);
      break;
    
    case 3: 
      result = data.attribute(index).parseDate(2000 + m_Random.nextInt(100) + "-01-01");
      
      break;
    
    case 4: 
      Instances rel = new Instances(data.attribute(index).relation(), 0);
      for (int n = 0; n < getNumInstancesRelational(); n++) {
        Instance inst = new Instance(rel.numAttributes());
        inst.setDataset(data);
        for (int i = 0; i < rel.numAttributes(); i++) {
          inst.setValue(i, generateAttributeValue(rel, i, 0.0D));
        }
        rel.add(inst);
      }
      result = data.attribute(index).addRelation(rel);
    }
    
    
    return result;
  }
  




  public Instances generate()
    throws Exception
  {
    return generate("");
  }
  





  public Instances generate(String namePrefix)
    throws Exception
  {
    if (getMultiInstance()) {
      TestInstances bag = (TestInstances)clone();
      bag.setMultiInstance(false);
      bag.setNumInstances(0);
      bag.setSeed(m_Random.nextInt());
      Instances bagFormat = bag.generate("bagAtt_");
      bagFormat.setClassIndex(-1);
      bagFormat.deleteAttributeAt(bagFormat.numAttributes() - 1);
      

      TestInstances structure = new TestInstances();
      structure.setSeed(m_Random.nextInt());
      structure.setNumNominal(1);
      structure.setNumRelational(1);
      structure.setRelationalFormat(0, bagFormat);
      structure.setClassType(getClassType());
      structure.setNumClasses(getNumClasses());
      structure.setRelationalClassFormat(getRelationalClassFormat());
      structure.setNumInstances(getNumInstances());
      m_Data = structure.generate();
      

      bag.setNumInstances(getNumInstancesRelational());
      for (int i = 0; i < getNumInstances(); i++) {
        bag.setSeed(m_Random.nextInt());
        Instances bagData = new Instances(bag.generate("bagAtt_"));
        bagData.setClassIndex(-1);
        bagData.deleteAttributeAt(bagData.numAttributes() - 1);
        double val = m_Data.attribute(1).addRelation(bagData);
        m_Data.instance(i).setValue(1, val);
      }
    }
    else
    {
      int clsIndex = m_ClassIndex;
      if (clsIndex == -1) {
        clsIndex = getNumAttributes() - 1;
      }
      
      FastVector attributes = new FastVector(getNumAttributes());
      
      for (int i = 0; i < getNumNominal(); i++) {
        attributes.addElement(generateAttribute(i, 1, namePrefix));
      }
      
      for (int i = 0; i < getNumNumeric(); i++) {
        attributes.addElement(generateAttribute(i, 0, namePrefix));
      }
      
      for (int i = 0; i < getNumString(); i++) {
        attributes.addElement(generateAttribute(i, 2, namePrefix));
      }
      
      for (int i = 0; i < getNumDate(); i++) {
        attributes.addElement(generateAttribute(i, 3, namePrefix));
      }
      
      for (int i = 0; i < getNumRelational(); i++) {
        attributes.addElement(generateAttribute(i, 4, namePrefix));
      }
      
      if (clsIndex != -2) {
        attributes.insertElementAt(generateAttribute(-1, getClassType(), namePrefix), clsIndex);
      }
      m_Data = new Instances(getRelation(), attributes, getNumInstances());
      m_Data.setClassIndex(clsIndex);
      

      for (int i = 0; i < getNumInstances(); i++) {
        Instance current = new Instance(getNumAttributes());
        current.setDataset(m_Data);
        
        double classVal;
        
        if (clsIndex != -2) {
          double classVal = generateClassValue(m_Data);
          current.setClassValue(classVal);
        }
        else {
          classVal = m_Random.nextFloat();
        }
        

        for (int n = 0; n < getNumAttributes(); n++) {
          if (clsIndex != n)
          {

            current.setValue(n, generateAttributeValue(m_Data, n, classVal));
          }
        }
        m_Data.add(current);
      }
    }
    
    if (m_Data.classIndex() == -2) {
      m_Data.setClassIndex(-1);
    }
    return getData();
  }
  








  public static TestInstances forCapabilities(Capabilities c)
  {
    TestInstances result = new TestInstances();
    

    if ((c.getOwner() instanceof MultiInstanceCapabilitiesHandler)) {
      Capabilities multi = (Capabilities)((MultiInstanceCapabilitiesHandler)c.getOwner()).getMultiInstanceCapabilities().clone();
      multi.setOwner(null);
      result = forCapabilities(multi);
      result.setMultiInstance(true);
    }
    else
    {
      if (c.handles(Capabilities.Capability.NO_CLASS)) {
        result.setClassIndex(-2);
      } else if (c.handles(Capabilities.Capability.NOMINAL_CLASS)) {
        result.setClassType(1);
      } else if (c.handles(Capabilities.Capability.BINARY_CLASS)) {
        result.setClassType(1);
      } else if (c.handles(Capabilities.Capability.NUMERIC_CLASS)) {
        result.setClassType(0);
      } else if (c.handles(Capabilities.Capability.DATE_CLASS)) {
        result.setClassType(3);
      } else if (c.handles(Capabilities.Capability.STRING_CLASS)) {
        result.setClassType(2);
      } else if (c.handles(Capabilities.Capability.RELATIONAL_CLASS)) {
        result.setClassType(4);
      }
      
      if (c.handles(Capabilities.Capability.UNARY_CLASS))
        result.setNumClasses(1);
      if (c.handles(Capabilities.Capability.BINARY_CLASS))
        result.setNumClasses(2);
      if (c.handles(Capabilities.Capability.NOMINAL_CLASS)) {
        result.setNumClasses(4);
      }
      
      if (c.handles(Capabilities.Capability.NOMINAL_ATTRIBUTES)) {
        result.setNumNominal(1);
        result.setNumRelationalNominal(1);
      }
      else {
        result.setNumNominal(0);
        result.setNumRelationalNominal(0);
      }
      
      if (c.handles(Capabilities.Capability.NUMERIC_ATTRIBUTES)) {
        result.setNumNumeric(1);
        result.setNumRelationalNumeric(1);
      }
      else {
        result.setNumNumeric(0);
        result.setNumRelationalNumeric(0);
      }
      
      if (c.handles(Capabilities.Capability.DATE_ATTRIBUTES)) {
        result.setNumDate(1);
        result.setNumRelationalDate(1);
      }
      else {
        result.setNumDate(0);
        result.setNumRelationalDate(0);
      }
      
      if (c.handles(Capabilities.Capability.STRING_ATTRIBUTES)) {
        result.setNumString(1);
        result.setNumRelationalString(1);
      }
      else {
        result.setNumString(0);
        result.setNumRelationalString(0);
      }
      
      if (c.handles(Capabilities.Capability.RELATIONAL_ATTRIBUTES)) {
        result.setNumRelational(1);
      } else {
        result.setNumRelational(0);
      }
    }
    return result;
  }
  






  public String toString()
  {
    String result = "";
    result = result + "Relation: " + getRelation() + "\n";
    result = result + "Seed: " + getSeed() + "\n";
    result = result + "# Instances: " + getNumInstances() + "\n";
    result = result + "ClassType: " + getClassType() + "\n";
    result = result + "# Classes: " + getNumClasses() + "\n";
    result = result + "Class index: " + getClassIndex() + "\n";
    result = result + "# Nominal: " + getNumNominal() + "\n";
    result = result + "# Nominal values: " + getNumNominalValues() + "\n";
    result = result + "# Numeric: " + getNumNumeric() + "\n";
    result = result + "# String: " + getNumString() + "\n";
    result = result + "# Date: " + getNumDate() + "\n";
    result = result + "# Relational: " + getNumRelational() + "\n";
    result = result + "  - # Nominal: " + getNumRelationalNominal() + "\n";
    result = result + "  - # Nominal values: " + getNumRelationalNominalValues() + "\n";
    result = result + "  - # Numeric: " + getNumRelationalNumeric() + "\n";
    result = result + "  - # String: " + getNumRelationalString() + "\n";
    result = result + "  - # Date: " + getNumRelationalDate() + "\n";
    result = result + "  - # Instances: " + getNumInstancesRelational() + "\n";
    result = result + "Multi-Instance: " + getMultiInstance() + "\n";
    result = result + "Words: " + getWords() + "\n";
    result = result + "Word separators: " + getWordSeparators() + "\n";
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6325 $");
  }
  







  public static void main(String[] args)
    throws Exception
  {
    TestInstances inst = new TestInstances();
    

    if ((Utils.getFlag("h", args)) || (Utils.getFlag("help", args))) {
      StringBuffer result = new StringBuffer();
      result.append("\nTest data generator options:\n\n");
      
      result.append("-h|-help\n\tprints this help\n");
      
      Enumeration enm = inst.listOptions();
      while (enm.hasMoreElements()) {
        Option option = (Option)enm.nextElement();
        result.append(option.synopsis() + "\n" + option.description() + "\n");
      }
      
      System.out.println(result);
      System.exit(0);
    }
    

    inst.setOptions(args);
    System.out.println(inst.generate());
  }
}
