package weka.datagenerators;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Randomizable;
import weka.core.RevisionHandler;
import weka.core.Utils;
































public abstract class DataGenerator
  implements OptionHandler, Randomizable, Serializable, RevisionHandler
{
  private static final long serialVersionUID = -3698585946221802578L;
  protected boolean m_Debug = false;
  

  protected Instances m_DatasetFormat = null;
  

  protected String m_RelationName = "";
  



  protected int m_NumExamplesAct;
  


  protected transient PrintWriter m_DefaultOutput = new PrintWriter(new OutputStreamWriter(System.out));
  


  protected transient PrintWriter m_Output = m_DefaultOutput;
  

  protected int m_Seed;
  

  protected Random m_Random = null;
  

  protected boolean m_CreatingRelationName = false;
  








  protected static HashSet m_OptionBlacklist = new HashSet();
  







  public DataGenerator()
  {
    clearBlacklist();
    
    setNumExamplesAct(defaultNumExamplesAct());
    setSeed(defaultSeed());
  }
  








  protected Vector enumToVector(Enumeration enm)
  {
    Vector result = new Vector();
    
    while (enm.hasMoreElements()) {
      result.add(enm.nextElement());
    }
    return result;
  }
  







  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tPrints this help.", "h", 1, "-h"));
    
    result.addElement(new Option("\tThe name of the output file, otherwise the generated data is\n\tprinted to stdout.", "o", 1, "-o <file>"));
    


    result.addElement(new Option("\tThe name of the relation.", "r", 1, "-r <name>"));
    

    result.addElement(new Option("\tWhether to print debug informations.", "d", 0, "-d"));
    

    result.addElement(new Option("\tThe seed for random function (default " + defaultSeed() + ")", "S", 1, "-S"));
    

    return result.elements();
  }
  












  public void setOptions(String[] options)
    throws Exception
  {
    options = removeBlacklist(options);
    
    String tmpStr = Utils.getOption('r', options);
    if (tmpStr.length() != 0) {
      setRelationName(Utils.unquote(tmpStr));
    } else {
      setRelationName("");
    }
    tmpStr = Utils.getOption('o', options);
    if (tmpStr.length() != 0) {
      setOutput(new PrintWriter(new FileOutputStream(tmpStr)));
    } else if (getOutput() == null) {
      throw new Exception("No Output defined!");
    }
    setDebug(Utils.getFlag('d', options));
    
    tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      setSeed(Integer.parseInt(tmpStr));
    } else {
      setSeed(defaultSeed());
    }
  }
  









  public String[] getOptions()
  {
    Vector result = new Vector();
    

    if (!m_CreatingRelationName) {
      result.add("-r");
      result.add(Utils.quote(getRelationNameToUse()));
    }
    
    if (getDebug()) {
      result.add("-d");
    }
    result.add("-S");
    result.add("" + getSeed());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  







  public Instances defineDataFormat()
    throws Exception
  {
    if (getRelationName().length() == 0) {
      setRelationName(defaultRelationName());
    }
    return m_DatasetFormat;
  }
  







  public abstract Instance generateExample()
    throws Exception;
  






  public abstract Instances generateExamples()
    throws Exception;
  






  public abstract String generateStart()
    throws Exception;
  






  public abstract String generateFinished()
    throws Exception;
  






  public abstract boolean getSingleModeFlag()
    throws Exception;
  






  public void setDebug(boolean debug)
  {
    m_Debug = debug;
  }
  




  public boolean getDebug()
  {
    return m_Debug;
  }
  





  public String debugTipText()
  {
    return "Whether the generator is run in debug mode or not.";
  }
  




  public void setRelationName(String relationName)
  {
    m_RelationName = relationName;
  }
  









  protected String defaultRelationName()
  {
    m_CreatingRelationName = true;
    
    StringBuffer result = new StringBuffer(getClass().getName());
    
    String[] options = getOptions();
    for (int i = 0; i < options.length; i++) {
      String option = options[i].trim();
      if (i > 0)
        result.append("_");
      result.append(option.replaceAll(" ", "_"));
    }
    
    m_CreatingRelationName = false;
    
    return result.toString();
  }
  










  protected String getRelationNameToUse()
  {
    String result = getRelationName();
    if (result.length() == 0) {
      result = defaultRelationName();
    }
    return result;
  }
  




  public String getRelationName()
  {
    return m_RelationName;
  }
  





  public String relationNameTipText()
  {
    return "The relation name of the generated data (if empty, a generic one will be supplied).";
  }
  




  protected int defaultNumExamplesAct()
  {
    return 0;
  }
  




  protected void setNumExamplesAct(int numExamplesAct)
  {
    m_NumExamplesAct = numExamplesAct;
  }
  




  public int getNumExamplesAct()
  {
    return m_NumExamplesAct;
  }
  





  protected String numExamplesActTipText()
  {
    return "The actual number of examples to generate.";
  }
  




  public void setOutput(PrintWriter newOutput)
  {
    m_Output = newOutput;
    m_DefaultOutput = null;
  }
  




  public PrintWriter getOutput()
  {
    return m_Output;
  }
  





  public PrintWriter defaultOutput()
  {
    return m_DefaultOutput;
  }
  





  public String outputTipText()
  {
    return "The output writer to use for printing the generated data.";
  }
  




  public void setDatasetFormat(Instances newFormat)
  {
    m_DatasetFormat = new Instances(newFormat, 0);
  }
  




  public Instances getDatasetFormat()
  {
    if (m_DatasetFormat != null) {
      return new Instances(m_DatasetFormat, 0);
    }
    return null;
  }
  





  public String formatTipText()
  {
    return "The data format to use.";
  }
  




  protected int defaultSeed()
  {
    return 1;
  }
  





  public int getSeed()
  {
    return m_Seed;
  }
  





  public void setSeed(int newSeed)
  {
    m_Seed = newSeed;
    m_Random = new Random(newSeed);
  }
  





  public String seedTipText()
  {
    return "The seed value for the random number generator.";
  }
  




  public Random getRandom()
  {
    if (m_Random == null) {
      m_Random = new Random(getSeed());
    }
    return m_Random;
  }
  




  public void setRandom(Random newRandom)
  {
    m_Random = newRandom;
  }
  





  public String randomTipText()
  {
    return "The random number generator to use.";
  }
  




  protected String toStringFormat()
  {
    if (m_DatasetFormat == null)
      return "";
    return m_DatasetFormat.toString();
  }
  


  protected static void clearBlacklist()
  {
    m_OptionBlacklist.clear();
  }
  






  protected static void addToBlacklist(String option)
  {
    m_OptionBlacklist.add(option);
  }
  







  protected static boolean isOnBlacklist(String option)
  {
    return m_OptionBlacklist.contains(option);
  }
  










  protected String[] removeBlacklist(String[] options)
  {
    Enumeration enm = listOptions();
    Hashtable pool = new Hashtable();
    while (enm.hasMoreElements()) {
      Option option = (Option)enm.nextElement();
      if (isOnBlacklist(option.name())) {
        pool.put(option.name(), option);
      }
    }
    
    enm = pool.keys();
    while (enm.hasMoreElements()) {
      Option option = (Option)pool.get(enm.nextElement());
      try {
        if (option.numArguments() == 0) {
          Utils.getFlag(option.name(), options);
        } else
          Utils.getOption(option.name(), options);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return options;
  }
  









  protected static String makeOptionString(DataGenerator generator)
  {
    StringBuffer result = new StringBuffer();
    result.append("\nData Generator options:\n\n");
    
    Enumeration enm = generator.listOptions();
    while (enm.hasMoreElements()) {
      Option option = (Option)enm.nextElement();
      
      if (!isOnBlacklist(option.name()))
      {
        result.append(option.synopsis() + "\n" + option.description() + "\n");
      }
    }
    return result.toString();
  }
  












  public static void makeData(DataGenerator generator, String[] options)
    throws Exception
  {
    boolean printhelp = Utils.getFlag('h', options);
    
    int i;
    if (!printhelp) {
      try {
        options = generator.removeBlacklist(options);
        generator.setOptions(options);
        

        Vector unknown = new Vector();
        for (i = 0; i < options.length; i++) {
          if (options[i].length() != 0)
            unknown.add(options[i]);
        }
        if (unknown.size() > 0) {
          System.out.print("Unknown options:");
          for (i = 0; i < unknown.size(); i++)
            System.out.print(" " + unknown.get(i));
          System.out.println();
        }
      } catch (Exception e) {
        e.printStackTrace();
        printhelp = true;
      }
    }
    
    if (printhelp) {
      System.out.println(makeOptionString(generator));
      return;
    }
    


    generator.setDatasetFormat(generator.defineDataFormat());
    

    PrintWriter output = generator.getOutput();
    

    output.println("%");
    output.println("% Commandline");
    output.println("%");
    output.println("% " + generator.getClass().getName() + " " + Utils.joinOptions(generator.getOptions()));
    
    output.println("%");
    

    String commentAtStart = generator.generateStart();
    
    if (commentAtStart.length() > 0) {
      output.println("%");
      output.println("% Prologue");
      output.println("%");
      output.println(commentAtStart.trim());
      output.println("%");
    }
    

    boolean singleMode = generator.getSingleModeFlag();
    

    if (singleMode)
    {
      output.println(generator.toStringFormat());
      for (i = 0; i < generator.getNumExamplesAct(); i++)
      {
        Instance inst = generator.generateExample();
        output.println(inst);
      }
    }
    Instances dataset = generator.generateExamples();
    
    output.println(dataset);
    

    String commentAtEnd = generator.generateFinished();
    
    if (commentAtEnd.length() > 0) {
      output.println("%");
      output.println("% Epilogue");
      output.println("%");
      output.println(commentAtEnd.trim());
      output.println("%");
    }
    
    output.flush();
    
    if (generator.getOutput() != generator.defaultOutput()) {
      output.close();
    }
  }
  





  protected static void runDataGenerator(DataGenerator datagenerator, String[] options)
  {
    try
    {
      makeData(datagenerator, options);
    } catch (Exception e) {
      if ((e.getMessage() != null) && (e.getMessage().indexOf("Data Generator options") == -1))
      {
        e.printStackTrace();
      } else {
        System.err.println(e.getMessage());
      }
    }
  }
}
