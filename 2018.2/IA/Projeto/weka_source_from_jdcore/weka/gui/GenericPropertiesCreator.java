package weka.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import weka.core.ClassDiscovery;
import weka.core.ClassDiscovery.StringCompare;
import weka.core.Utils;












































































public class GenericPropertiesCreator
{
  public static final boolean VERBOSE = false;
  public static final String USE_DYNAMIC = "UseDynamic";
  protected static String CREATOR_FILE = "weka/gui/GenericPropertiesCreator.props";
  



  protected static String EXCLUDE_FILE = "weka/gui/GenericPropertiesCreator.excludes";
  

  protected static String EXCLUDE_INTERFACE = "I";
  

  protected static String EXCLUDE_CLASS = "C";
  

  protected static String EXCLUDE_SUPERCLASS = "S";
  





  protected static String PROPERTY_FILE = "weka/gui/GenericObjectEditor.props";
  


  protected String m_InputFilename;
  


  protected String m_OutputFilename;
  


  protected Properties m_InputProperties;
  


  protected Properties m_OutputProperties;
  


  protected boolean m_ExplicitPropsFile;
  


  protected Hashtable m_Excludes;
  


  public GenericPropertiesCreator()
    throws Exception
  {
    this(CREATOR_FILE);
    m_ExplicitPropsFile = false;
  }
  









  public GenericPropertiesCreator(String filename)
    throws Exception
  {
    m_InputFilename = filename;
    m_OutputFilename = PROPERTY_FILE;
    m_InputProperties = null;
    m_OutputProperties = null;
    m_ExplicitPropsFile = true;
    m_Excludes = new Hashtable();
  }
  








  public void setExplicitPropsFile(boolean value)
  {
    m_ExplicitPropsFile = value;
  }
  








  public boolean getExplicitPropsFile()
  {
    return m_ExplicitPropsFile;
  }
  




  public String getOutputFilename()
  {
    return m_OutputFilename;
  }
  




  public void setOutputFilename(String filename)
  {
    m_OutputFilename = filename;
  }
  




  public String getInputFilename()
  {
    return m_InputFilename;
  }
  






  public void setInputFilename(String filename)
  {
    m_InputFilename = filename;
    setExplicitPropsFile(true);
  }
  




  public Properties getInputProperties()
  {
    return m_InputProperties;
  }
  





  public Properties getOutputProperties()
  {
    return m_OutputProperties;
  }
  








  protected void loadInputProperties()
  {
    m_InputProperties = new Properties();
    try {
      File f = new File(getInputFilename());
      if ((getExplicitPropsFile()) && (f.exists())) {
        m_InputProperties.load(new FileInputStream(getInputFilename()));
      } else {
        m_InputProperties = Utils.readProperties(getInputFilename());
      }
      
      m_Excludes.clear();
      Properties p = Utils.readProperties(EXCLUDE_FILE);
      Enumeration enm = p.propertyNames();
      while (enm.hasMoreElements()) {
        String name = enm.nextElement().toString();
        
        Hashtable t = new Hashtable();
        m_Excludes.put(name, t);
        t.put(EXCLUDE_INTERFACE, new Vector());
        t.put(EXCLUDE_CLASS, new Vector());
        t.put(EXCLUDE_SUPERCLASS, new Vector());
        

        StringTokenizer tok = new StringTokenizer(p.getProperty(name), ",");
        while (tok.hasMoreTokens()) {
          String item = tok.nextToken();
          
          Vector list = new Vector();
          if (item.startsWith(EXCLUDE_INTERFACE + ":")) {
            list = (Vector)t.get(EXCLUDE_INTERFACE);
          } else if (item.startsWith(EXCLUDE_CLASS + ":")) {
            list = (Vector)t.get(EXCLUDE_CLASS);
          } else if (item.startsWith(EXCLUDE_SUPERCLASS)) {
            list = (Vector)t.get(EXCLUDE_SUPERCLASS);
          }
          list.add(item.substring(item.indexOf(":") + 1));
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  




  public boolean useDynamic()
  {
    if (getInputProperties() == null) {
      loadInputProperties();
    }
    


    if (!ClassLoader.getSystemClassLoader().equals(getClass().getClassLoader())) {
      if (Boolean.parseBoolean(getInputProperties().getProperty("UseDynamic", "true")) == true) {
        Messages.getInstance();System.out.println(Messages.getString("GenericPropertiesCreator_UseDynamic_Text"));
      }
      return false;
    }
    
    return Boolean.parseBoolean(getInputProperties().getProperty("UseDynamic", "true"));
  }
  






  protected boolean isValidClassname(String classname)
  {
    return classname.indexOf("$") == -1;
  }
  














  protected boolean isValidClassname(String key, String classname)
  {
    boolean result = true;
    

    if (m_Excludes.containsKey(key)) {
      Class clsCurrent;
      try { clsCurrent = Class.forName(classname);
      }
      catch (Exception e)
      {
        clsCurrent = null;
      }
      
      Class cls;
      if ((clsCurrent != null) && (result)) {
        Vector list = (Vector)((Hashtable)m_Excludes.get(key)).get(EXCLUDE_INTERFACE);
        for (int i = 0; i < list.size(); i++) {
          try {
            cls = Class.forName(list.get(i).toString());
            if (ClassDiscovery.hasInterface(cls, clsCurrent)) {
              result = false;
              break;
            }
          }
          catch (Exception e) {}
        }
      }
      



      if ((clsCurrent != null) && (result)) {
        Vector list = (Vector)((Hashtable)m_Excludes.get(key)).get(EXCLUDE_SUPERCLASS);
        for (int i = 0; i < list.size(); i++) {
          try {
            cls = Class.forName(list.get(i).toString());
            if (ClassDiscovery.isSubclass(cls, clsCurrent)) {
              result = false;
              break;
            }
          }
          catch (Exception e) {}
        }
      }
      



      if ((clsCurrent != null) && (result)) {
        Vector list = (Vector)((Hashtable)m_Excludes.get(key)).get(EXCLUDE_CLASS);
        for (int i = 0; i < list.size(); i++) {
          try {
            cls = Class.forName(list.get(i).toString());
            if (cls.getName().equals(clsCurrent.getName())) {
              result = false;
            }
          }
          catch (Exception e) {}
        }
      }
    }
    

    return result;
  }
  














  protected void generateOutputProperties()
    throws Exception
  {
    m_OutputProperties = new Properties();
    Enumeration keys = m_InputProperties.propertyNames();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement().toString();
      if (!key.equals("UseDynamic"))
      {
        StringTokenizer tok = new StringTokenizer(m_InputProperties.getProperty(key), ",");
        HashSet names = new HashSet();
        

        while (tok.hasMoreTokens()) {
          String pkg = tok.nextToken().trim();
          Vector classes;
          try {
            classes = ClassDiscovery.find(Class.forName(key), pkg);
          }
          catch (Exception e) {
            Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("GenericPropertiesCreator_GenerateOutputProperties_Text_First") + key + Messages.getString("GenericPropertiesCreator_GenerateOutputProperties_Text_Second") + e);
            classes = new Vector();
          }
          
          for (int i = 0; i < classes.size(); i++)
          {
            if (isValidClassname(classes.get(i).toString()))
            {

              if (isValidClassname(key, classes.get(i).toString()))
              {
                names.add(classes.get(i));
              }
            }
          }
        }
        String value = "";
        Vector classes = new Vector();
        classes.addAll(names);
        Collections.sort(classes, new ClassDiscovery.StringCompare());
        for (int i = 0; i < classes.size(); i++) {
          if (!value.equals(""))
            value = value + ",";
          value = value + classes.get(i).toString();
        }
        



        m_OutputProperties.setProperty(key, value);
      }
    }
  }
  






  protected void storeOutputProperties()
    throws Exception
  {
    Messages.getInstance();m_OutputProperties.store(new FileOutputStream(getOutputFilename()), Messages.getString("GenericPropertiesCreator_StoreOutputProperties_Text_Third"));
  }
  






  public void execute()
    throws Exception
  {
    execute(true);
  }
  












  public void execute(boolean store)
    throws Exception
  {
    loadInputProperties();
    

    generateOutputProperties();
    

    if (store) {
      storeOutputProperties();
    }
  }
  




















  public static void main(String[] args)
    throws Exception
  {
    GenericPropertiesCreator c = null;
    
    if (args.length == 0) {
      c = new GenericPropertiesCreator();
    }
    else if (args.length == 1) {
      c = new GenericPropertiesCreator();
      c.setOutputFilename(args[0]);
    }
    else if (args.length == 2) {
      c = new GenericPropertiesCreator(args[0]);
      c.setOutputFilename(args[1]);
    }
    else {
      Messages.getInstance();Messages.getInstance();System.out.println(Messages.getString("GenericPropertiesCreator_Main_Text_First") + GenericPropertiesCreator.class.getName() + Messages.getString("GenericPropertiesCreator_Main_Text_Second"));
      System.exit(1);
    }
    
    c.execute(true);
  }
}
