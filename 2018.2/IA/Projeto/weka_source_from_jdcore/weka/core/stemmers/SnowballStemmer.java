package weka.core.stemmers;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.ClassDiscovery;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.gui.GenericObjectEditor;



























































public class SnowballStemmer
  implements Stemmer, OptionHandler
{
  static final long serialVersionUID = -6111170431963015178L;
  public static final String PACKAGE = "org.tartarus.snowball";
  public static final String PACKAGE_EXT = "org.tartarus.snowball.ext";
  protected static final String SNOWBALL_PROGRAM = "org.tartarus.snowball.SnowballProgram";
  protected static boolean m_Present = false;
  

  protected static Vector m_Stemmers;
  

  protected Object m_Stemmer;
  

  protected transient Method m_StemMethod;
  

  protected transient Method m_SetCurrentMethod;
  
  protected transient Method m_GetCurrentMethod;
  

  static
  {
    checkForSnowball();
  }
  


  public SnowballStemmer()
  {
    this("porter");
    initStemmers();
  }
  






  public SnowballStemmer(String name)
  {
    setStemmer(name);
  }
  

  private static void checkForSnowball()
  {
    try
    {
      Class.forName("org.tartarus.snowball.SnowballProgram");
      m_Present = true;
    }
    catch (Exception e) {
      m_Present = false;
    }
  }
  





  public String globalInfo()
  {
    return "A wrapper class for the Snowball stemmers. Only available if the Snowball classes are in the classpath.\nIf the class discovery is not dynamic, i.e., the property 'UseDynamic' in the props file 'weka/gui/GenericPropertiesCreator.props' is 'false', then the property 'org.tartarus.snowball.SnowballProgram' in the 'weka/gui/GenericObjectEditor.props' file has to be uncommented as well. If necessary you have to discover and fill in the snowball stemmers manually. You can use the 'weka.core.ClassDiscovery' for this:\n  java weka.core.ClassDiscovery org.tartarus.snowball.SnowballProgram org.tartarus.snowball.ext\n\nFor more information visit these web sites:\n  http://weka.wikispaces.com/Stemmers\n  http://snowball.tartarus.org/\n";
  }
  



















  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe name of the snowball stemmer (default 'porter').\n\tavailable stemmers:\n" + getStemmerList(65, "\t   "), "S", 1, "-S <name>"));
    




    return result.elements();
  }
  


















  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0) {
      setStemmer(tmpStr);
    } else {
      setStemmer("porter");
    }
  }
  





  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getStemmer() != null) {
      result.add("-S");
      result.add("" + getStemmer());
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  private static String getStemmerName(String classname)
  {
    return classname.replaceAll(".*\\.", "").replaceAll("Stemmer$", "");
  }
  






  private static String getStemmerClassname(String name)
  {
    return "org.tartarus.snowball.ext." + name + "Stemmer";
  }
  





  private static void initStemmers()
  {
    if (m_Stemmers != null) {
      return;
    }
    m_Stemmers = new Vector();
    
    if (!m_Present) {
      return;
    }
    Vector classnames = GenericObjectEditor.getClassnames("org.tartarus.snowball.SnowballProgram");
    
    if (classnames.size() == 0) {
      classnames = ClassDiscovery.find("org.tartarus.snowball.SnowballProgram", "org.tartarus.snowball.ext");
      for (int i = 0; i < classnames.size(); i++) {
        m_Stemmers.add(getStemmerName(classnames.get(i).toString()));
      }
    }
  }
  




  public static boolean isPresent()
  {
    return m_Present;
  }
  




  public static Enumeration listStemmers()
  {
    initStemmers();
    
    return m_Stemmers.elements();
  }
  












  private static String getStemmerList(int lineLength, String indention)
  {
    String result = "";
    String line = "";
    Enumeration enm = listStemmers();
    while (enm.hasMoreElements()) {
      String name = enm.nextElement().toString();
      if (line.length() > 0)
        line = line + ", ";
      if ((lineLength > 0) && (line.length() + name.length() > lineLength)) {
        result = result + indention + line + "\n";
        line = "";
      }
      line = line + name;
    }
    
    if (line.length() > 0) {
      result = result + indention + line + "\n";
    }
    return result;
  }
  



  public String getStemmer()
  {
    
    

    if (m_Stemmer == null) {
      return null;
    }
    return getStemmerName(m_Stemmer.getClass().getName());
  }
  




  public void setStemmer(String name)
  {
    
    



    if (m_Stemmers.contains(name)) {
      try {
        Class snowballClass = Class.forName(getStemmerClassname(name));
        m_Stemmer = snowballClass.newInstance();
        

        Class[] argClasses = new Class[0];
        m_StemMethod = snowballClass.getMethod("stem", argClasses);
        
        argClasses = new Class[1];
        argClasses[0] = String.class;
        m_SetCurrentMethod = snowballClass.getMethod("setCurrent", argClasses);
        
        argClasses = new Class[0];
        m_GetCurrentMethod = snowballClass.getMethod("getCurrent", argClasses);
      }
      catch (Exception e) {
        System.out.println("Error initializing stemmer '" + name + "'!" + e.getMessage());
        

        m_Stemmer = null;
      }
    }
    else {
      System.err.println("Stemmer '" + name + "' unknown!");
      m_Stemmer = null;
    }
  }
  





  public String stemmerTipText()
  {
    return "The Snowball stemmer to use, available: " + getStemmerList(0, "");
  }
  


  public String stem(String word)
  {
    String result;
    

    String result;
    

    if (m_Stemmer == null) {
      result = new String(word);

    }
    else
    {
      if (m_SetCurrentMethod == null) {
        setStemmer(getStemmer());
      }
      try
      {
        Object[] args = new Object[1];
        args[0] = word;
        m_SetCurrentMethod.invoke(m_Stemmer, args);
        

        args = new Object[0];
        m_StemMethod.invoke(m_Stemmer, args);
        

        args = new Object[0];
        result = (String)m_GetCurrentMethod.invoke(m_Stemmer, args);
      }
      catch (Exception e) {
        e.printStackTrace();
        result = word;
      }
    }
    
    return result;
  }
  






  public String toString()
  {
    String result = getClass().getName();
    result = result + " " + Utils.joinOptions(getOptions());
    
    return result.trim();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5836 $");
  }
  



  public static void main(String[] args)
  {
    try
    {
      Stemming.useStemmer(new SnowballStemmer(), args);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
