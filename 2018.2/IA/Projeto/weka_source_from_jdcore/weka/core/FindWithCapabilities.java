package weka.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import weka.gui.GenericPropertiesCreator;









































































































































public class FindWithCapabilities
  implements OptionHandler, CapabilitiesHandler, RevisionHandler
{
  protected Capabilities m_Capabilities = new Capabilities(this);
  

  protected Capabilities m_NotCapabilities = new Capabilities(this);
  

  protected Vector m_Packages = new Vector();
  

  protected CapabilitiesHandler m_Handler = null;
  

  protected String m_Filename = "";
  

  protected SingleIndex m_ClassIndex = new SingleIndex();
  

  protected String m_Superclass = "";
  

  protected boolean m_GenericPropertiesCreator = false;
  

  protected Vector m_Matches = new Vector();
  

  protected Vector m_Misses = new Vector();
  

  public FindWithCapabilities() {}
  

  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("", "", 0, "All class and attribute options can be prefixed with 'not',\ne.g., '-not-numeric-class'. This makes sure that the returned\nschemes 'cannot' handle numeric classes."));
    




    result.addElement(new Option("\tThe minimum number of instances (default 1).", "num-instances", 1, "-num-instances <num>"));
    


    result.addElement(new Option("\tMust handle unray classes.", "unary-class", 0, "-unary-class"));
    


    result.addElement(new Option("\tMust handle binary classes.", "binary-class", 0, "-binary-class"));
    


    result.addElement(new Option("\tMust handle nominal classes.", "nominal-class", 0, "-nominal-class"));
    


    result.addElement(new Option("\tMust handle numeric classes.", "numeric-class", 0, "-numeric-class"));
    


    result.addElement(new Option("\tMust handle string classes.", "string-class", 0, "-string-class"));
    


    result.addElement(new Option("\tMust handle date classes.", "date-class", 0, "-date-class"));
    


    result.addElement(new Option("\tMust handle relational classes.", "relational-class", 0, "-relational-class"));
    


    result.addElement(new Option("\tMust handle missing class values.", "missing-class-values", 0, "-missing-class-values"));
    


    result.addElement(new Option("\tDoesn't need a class.", "no-class", 0, "-no-class"));
    


    result.addElement(new Option("\tMust handle unary attributes.", "unary-atts", 0, "-unary-atts"));
    


    result.addElement(new Option("\tMust handle binary attributes.", "binary-atts", 0, "-binary-atts"));
    


    result.addElement(new Option("\tMust handle nominal attributes.", "nominal-atts", 0, "-nominal-atts"));
    


    result.addElement(new Option("\tMust handle numeric attributes.", "numeric-atts", 0, "-numeric-atts"));
    


    result.addElement(new Option("\tMust handle string attributes.", "string-atts", 0, "-string-atts"));
    


    result.addElement(new Option("\tMust handle date attributes.", "date-atts", 0, "-date-atts"));
    


    result.addElement(new Option("\tMust handle relational attributes.", "relational-atts", 0, "-relational-atts"));
    


    result.addElement(new Option("\tMust handle missing attribute values.", "missing-att-values", 0, "-missing-att-values"));
    


    result.addElement(new Option("\tMust handle multi-instance data.", "only-multiinstance", 0, "-only-multiinstance"));
    


    result.addElement(new Option("\tThe Capabilities handler to base the handling on.\n\tThe other parameters can be used to override the ones\n\tdetermined from the handler. Additional parameters for\n\thandler can be passed on after the '--'.\n\tEither '-W' or '-t' can be used.", "W", 1, "-W <classname>"));
    






    result.addElement(new Option("\tThe dataset to base the capabilities on.\n\tThe other parameters can be used to override the ones\n\tdetermined from the handler.\n\tEither '-t' or '-W' can be used.", "t", 1, "-t <file>"));
    





    result.addElement(new Option("\tThe index of the class attribute, -1 for none.\n\t'first' and 'last' are also valid.\n\tOnly in conjunction with option '-t'.", "c", 1, "-c <num>"));
    




    result.addElement(new Option("\tSuperclass to look for in the packages.\n", "superclass", 1, "-superclass"));
    


    result.addElement(new Option("\tComma-separated list of packages to search in.", "packages", 1, "-packages"));
    


    result.addElement(new Option("\tRetrieves the package list from the GenericPropertiesCreator\n\tfor the given superclass. (overrides -packages <list>).", "generic", 1, "-generic"));
    



    result.addElement(new Option("\tAlso prints the classname that didn't match the criteria.", "misses", 0, "-misses"));
    


    return result.elements();
  }
  












  public void setOptions(String[] options)
    throws Exception
  {
    m_Capabilities = new Capabilities(this);
    boolean initialized = false;
    
    String tmpStr = Utils.getOption('W', options);
    if (tmpStr.length() != 0) {
      Class cls = Class.forName(tmpStr);
      if (ClassDiscovery.hasInterface(CapabilitiesHandler.class, cls)) {
        initialized = true;
        CapabilitiesHandler handler = (CapabilitiesHandler)cls.newInstance();
        if ((handler instanceof OptionHandler))
          ((OptionHandler)handler).setOptions(Utils.partitionOptions(options));
        setHandler(handler);
      }
      else {
        throw new IllegalArgumentException("Class '" + tmpStr + "' is not a CapabilitiesHandler!");
      }
    }
    else {
      tmpStr = Utils.getOption('c', options);
      if (tmpStr.length() != 0) {
        setClassIndex(tmpStr);
      } else {
        setClassIndex("last");
      }
      tmpStr = Utils.getOption('t', options);
      setFilename(tmpStr);
    }
    
    tmpStr = Utils.getOption("num-instances", options);
    if (tmpStr.length() != 0) {
      m_Capabilities.setMinimumNumberInstances(Integer.parseInt(tmpStr));
    } else if (!initialized) {
      m_Capabilities.setMinimumNumberInstances(1);
    }
    
    if (Utils.getFlag("no-class", options)) {
      enable(Capabilities.Capability.NO_CLASS);
    }
    if (Utils.getFlag("not-no-class", options)) {
      enableNot(Capabilities.Capability.NO_CLASS);
    }
    if (!m_Capabilities.handles(Capabilities.Capability.NO_CLASS))
    {
      if (Utils.getFlag("nominal-class", options)) {
        enable(Capabilities.Capability.NOMINAL_CLASS);
        disable(Capabilities.Capability.BINARY_CLASS);
      }
      if (Utils.getFlag("binary-class", options)) {
        enable(Capabilities.Capability.BINARY_CLASS);
        disable(Capabilities.Capability.UNARY_CLASS);
      }
      if (Utils.getFlag("unary-class", options))
        enable(Capabilities.Capability.UNARY_CLASS);
      if (Utils.getFlag("numeric-class", options))
        enable(Capabilities.Capability.NUMERIC_CLASS);
      if (Utils.getFlag("string-class", options))
        enable(Capabilities.Capability.STRING_CLASS);
      if (Utils.getFlag("date-class", options))
        enable(Capabilities.Capability.DATE_CLASS);
      if (Utils.getFlag("relational-class", options))
        enable(Capabilities.Capability.RELATIONAL_CLASS);
      if (Utils.getFlag("missing-class-values", options)) {
        enable(Capabilities.Capability.MISSING_CLASS_VALUES);
      }
    }
    if (Utils.getFlag("not-nominal-class", options)) {
      enableNot(Capabilities.Capability.NOMINAL_CLASS);
      disableNot(Capabilities.Capability.BINARY_CLASS);
    }
    if (Utils.getFlag("not-binary-class", options)) {
      enableNot(Capabilities.Capability.BINARY_CLASS);
      disableNot(Capabilities.Capability.UNARY_CLASS);
    }
    if (Utils.getFlag("not-unary-class", options))
      enableNot(Capabilities.Capability.UNARY_CLASS);
    if (Utils.getFlag("not-numeric-class", options))
      enableNot(Capabilities.Capability.NUMERIC_CLASS);
    if (Utils.getFlag("not-string-class", options))
      enableNot(Capabilities.Capability.STRING_CLASS);
    if (Utils.getFlag("not-date-class", options))
      enableNot(Capabilities.Capability.DATE_CLASS);
    if (Utils.getFlag("not-relational-class", options))
      enableNot(Capabilities.Capability.RELATIONAL_CLASS);
    if (Utils.getFlag("not-relational-class", options))
      enableNot(Capabilities.Capability.RELATIONAL_CLASS);
    if (Utils.getFlag("not-missing-class-values", options)) {
      enableNot(Capabilities.Capability.MISSING_CLASS_VALUES);
    }
    
    if (Utils.getFlag("nominal-atts", options)) {
      enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
      disable(Capabilities.Capability.BINARY_ATTRIBUTES);
    }
    if (Utils.getFlag("binary-atts", options)) {
      enable(Capabilities.Capability.BINARY_ATTRIBUTES);
      disable(Capabilities.Capability.UNARY_ATTRIBUTES);
    }
    if (Utils.getFlag("unary-atts", options))
      enable(Capabilities.Capability.UNARY_ATTRIBUTES);
    if (Utils.getFlag("numeric-atts", options))
      enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    if (Utils.getFlag("string-atts", options))
      enable(Capabilities.Capability.STRING_ATTRIBUTES);
    if (Utils.getFlag("date-atts", options))
      enable(Capabilities.Capability.DATE_ATTRIBUTES);
    if (Utils.getFlag("relational-atts", options))
      enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    if (Utils.getFlag("missing-att-values", options)) {
      enable(Capabilities.Capability.MISSING_VALUES);
    }
    if (Utils.getFlag("not-nominal-atts", options)) {
      enableNot(Capabilities.Capability.NOMINAL_ATTRIBUTES);
      disableNot(Capabilities.Capability.BINARY_ATTRIBUTES);
    }
    if (Utils.getFlag("not-binary-atts", options)) {
      enableNot(Capabilities.Capability.BINARY_ATTRIBUTES);
      disableNot(Capabilities.Capability.UNARY_ATTRIBUTES);
    }
    if (Utils.getFlag("not-unary-atts", options))
      enableNot(Capabilities.Capability.UNARY_ATTRIBUTES);
    if (Utils.getFlag("not-numeric-atts", options))
      enableNot(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    if (Utils.getFlag("not-string-atts", options))
      enableNot(Capabilities.Capability.STRING_ATTRIBUTES);
    if (Utils.getFlag("not-date-atts", options))
      enableNot(Capabilities.Capability.DATE_ATTRIBUTES);
    if (Utils.getFlag("not-relational-atts", options))
      enableNot(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    if (Utils.getFlag("not-missing-att-values", options)) {
      enableNot(Capabilities.Capability.MISSING_VALUES);
    }
    if (Utils.getFlag("only-multiinstance", options)) {
      enable(Capabilities.Capability.ONLY_MULTIINSTANCE);
    }
    tmpStr = Utils.getOption("superclass", options);
    if (tmpStr.length() != 0) {
      m_Superclass = tmpStr;
    } else {
      throw new IllegalArgumentException("A superclass has to be specified!");
    }
    tmpStr = Utils.getOption("packages", options);
    if (tmpStr.length() != 0) {
      StringTokenizer tok = new StringTokenizer(tmpStr, ",");
      m_Packages = new Vector();
      while (tok.hasMoreTokens()) {
        m_Packages.add(tok.nextToken());
      }
    }
    if (Utils.getFlag("generic", options)) {
      GenericPropertiesCreator creator = new GenericPropertiesCreator();
      creator.execute(false);
      Properties props = creator.getInputProperties();
      StringTokenizer tok = new StringTokenizer(props.getProperty(m_Superclass), ",");
      m_Packages = new Vector();
      while (tok.hasMoreTokens()) {
        m_Packages.add(tok.nextToken());
      }
    }
  }
  







  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-num-instances");
    result.add("" + m_Capabilities.getMinimumNumberInstances());
    
    if (isEnabled(Capabilities.Capability.NO_CLASS)) {
      result.add("-no-class");
    }
    else {
      if (isEnabled(Capabilities.Capability.UNARY_CLASS))
        result.add("-unary-class");
      if (isEnabled(Capabilities.Capability.BINARY_CLASS))
        result.add("-binary-class");
      if (isEnabled(Capabilities.Capability.NOMINAL_CLASS))
        result.add("-nominal-class");
      if (isEnabled(Capabilities.Capability.NUMERIC_CLASS))
        result.add("-numeric-class");
      if (isEnabled(Capabilities.Capability.STRING_CLASS))
        result.add("-string-class");
      if (isEnabled(Capabilities.Capability.DATE_CLASS))
        result.add("-date-class");
      if (isEnabled(Capabilities.Capability.RELATIONAL_CLASS))
        result.add("-relational-class");
      if (isEnabled(Capabilities.Capability.MISSING_CLASS_VALUES)) {
        result.add("-missing-class-values");
      }
    }
    if (isEnabled(Capabilities.Capability.UNARY_ATTRIBUTES))
      result.add("-unary-atts");
    if (isEnabled(Capabilities.Capability.BINARY_ATTRIBUTES))
      result.add("-binary-atts");
    if (isEnabled(Capabilities.Capability.NOMINAL_ATTRIBUTES))
      result.add("-nominal-atts");
    if (isEnabled(Capabilities.Capability.NUMERIC_ATTRIBUTES))
      result.add("-numeric-atts");
    if (isEnabled(Capabilities.Capability.STRING_ATTRIBUTES))
      result.add("-string-atts");
    if (isEnabled(Capabilities.Capability.DATE_ATTRIBUTES))
      result.add("-date-atts");
    if (isEnabled(Capabilities.Capability.RELATIONAL_ATTRIBUTES))
      result.add("-relational-atts");
    if (isEnabled(Capabilities.Capability.MISSING_VALUES)) {
      result.add("-missing-att-values");
    }
    
    if (isEnabledNot(Capabilities.Capability.NO_CLASS))
      result.add("-not-no-class");
    if (isEnabledNot(Capabilities.Capability.UNARY_CLASS))
      result.add("-not-unary-class");
    if (isEnabledNot(Capabilities.Capability.BINARY_CLASS))
      result.add("-not-binary-class");
    if (isEnabledNot(Capabilities.Capability.NOMINAL_CLASS))
      result.add("-not-nominal-class");
    if (isEnabledNot(Capabilities.Capability.NUMERIC_CLASS))
      result.add("-not-numeric-class");
    if (isEnabledNot(Capabilities.Capability.STRING_CLASS))
      result.add("-not-string-class");
    if (isEnabledNot(Capabilities.Capability.DATE_CLASS))
      result.add("-not-date-class");
    if (isEnabledNot(Capabilities.Capability.RELATIONAL_CLASS))
      result.add("-not-relational-class");
    if (isEnabledNot(Capabilities.Capability.MISSING_CLASS_VALUES)) {
      result.add("-not-missing-class-values");
    }
    if (isEnabledNot(Capabilities.Capability.UNARY_ATTRIBUTES))
      result.add("-not-unary-atts");
    if (isEnabledNot(Capabilities.Capability.BINARY_ATTRIBUTES))
      result.add("-not-binary-atts");
    if (isEnabledNot(Capabilities.Capability.NOMINAL_ATTRIBUTES))
      result.add("-not-nominal-atts");
    if (isEnabledNot(Capabilities.Capability.NUMERIC_ATTRIBUTES))
      result.add("-not-numeric-atts");
    if (isEnabledNot(Capabilities.Capability.STRING_ATTRIBUTES))
      result.add("-not-string-atts");
    if (isEnabledNot(Capabilities.Capability.DATE_ATTRIBUTES))
      result.add("-not-date-atts");
    if (isEnabledNot(Capabilities.Capability.RELATIONAL_ATTRIBUTES))
      result.add("-not-relational-atts");
    if (isEnabledNot(Capabilities.Capability.MISSING_VALUES)) {
      result.add("-not-missing-att-values");
    }
    if (isEnabled(Capabilities.Capability.ONLY_MULTIINSTANCE)) {
      result.add("-only-multi-instance");
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
    } else if (getFilename().length() != 0) {
      result.add("-t");
      result.add(getFilename());
      result.add("-c");
      result.add(m_ClassIndex.getSingleIndex());
    }
    
    if (m_Superclass.length() != 0) {
      result.add("-superclass");
      result.add(m_Superclass);
    }
    else {
      result.add("-packages");
      result.add(m_Packages.toString().replaceAll("\\[", "").replaceAll("\\]", ""));
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public void setHandler(CapabilitiesHandler value)
  {
    m_Handler = value;
    setCapabilities(m_Handler.getCapabilities());
  }
  





  public CapabilitiesHandler getHandler()
  {
    return m_Handler;
  }
  







  public void setFilename(String value)
  {
    m_Filename = value;
    
    if (m_Filename.length() != 0) {
      try {
        Instances insts = new Instances(new BufferedReader(new FileReader(m_Filename)));
        m_ClassIndex.setUpper(insts.numAttributes());
        insts.setClassIndex(Integer.parseInt(getClassIndex()) - 1);
        
        setCapabilities(Capabilities.forInstances(insts));
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  




  public String getFilename()
  {
    return m_Filename;
  }
  




  public void setClassIndex(String value)
  {
    if (value.equals("-1")) {
      m_ClassIndex = null;
    } else {
      m_ClassIndex = new SingleIndex(value);
    }
  }
  



  public String getClassIndex()
  {
    if (m_ClassIndex == null) {
      return "-1";
    }
    return "" + m_ClassIndex.getIndex();
  }
  




  public void enable(Capabilities.Capability c)
  {
    m_Capabilities.enable(c);
  }
  





  public boolean isEnabled(Capabilities.Capability c)
  {
    return m_Capabilities.handles(c);
  }
  




  public void disable(Capabilities.Capability c)
  {
    m_Capabilities.disable(c);
  }
  




  public void enableNot(Capabilities.Capability c)
  {
    m_NotCapabilities.enable(c);
  }
  





  public boolean isEnabledNot(Capabilities.Capability c)
  {
    return m_NotCapabilities.handles(c);
  }
  




  public void disableNot(Capabilities.Capability c)
  {
    m_NotCapabilities.disable(c);
  }
  





  public boolean handles(Capabilities.Capability c)
  {
    return m_Capabilities.handles(c);
  }
  





  public Capabilities getCapabilities()
  {
    return m_Capabilities;
  }
  




  public void setCapabilities(Capabilities c)
  {
    m_Capabilities = ((Capabilities)c.clone());
  }
  





  public Capabilities getNotCapabilities()
  {
    return m_NotCapabilities;
  }
  




  public void setNotCapabilities(Capabilities c)
  {
    m_NotCapabilities = ((Capabilities)c.clone());
  }
  




  public Vector getMatches()
  {
    return m_Matches;
  }
  




  public Vector getMisses()
  {
    return m_Misses;
  }
  












  public Vector find()
  {
    m_Matches = new Vector();
    m_Misses = new Vector();
    
    Vector list = ClassDiscovery.find(m_Superclass, (String[])m_Packages.toArray(new String[m_Packages.size()]));
    for (int i = 0; i < list.size(); i++) {
      try {
        Class cls = Class.forName((String)list.get(i));
        Object obj = cls.newInstance();
        

        if (cls != getClass())
        {


          if ((obj instanceof CapabilitiesHandler))
          {


            CapabilitiesHandler handler = (CapabilitiesHandler)obj;
            Capabilities caps = handler.getCapabilities();
            boolean fits = true;
            for (Capabilities.Capability cap : Capabilities.Capability.values()) {
              if ((m_Capabilities.handles(cap)) && 
                (!caps.handles(cap))) {
                fits = false;
                break;
              }
            }
            
            if (!fits) {
              m_Misses.add(list.get(i));

            }
            else
            {
              for (Capabilities.Capability cap : Capabilities.Capability.values()) {
                if ((m_NotCapabilities.handles(cap)) && 
                  (caps.handles(cap))) {
                  fits = false;
                  break;
                }
              }
              
              if (!fits) {
                m_Misses.add(list.get(i));



              }
              else if (caps.getMinimumNumberInstances() > m_Capabilities.getMinimumNumberInstances()) {
                m_Misses.add(list.get(i));

              }
              else
              {
                m_Matches.add(list.get(i));
              }
            }
          }
        }
      } catch (Exception e) {}
    }
    return m_Matches;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
  












  public static void main(String[] args)
  {
    boolean printMisses = false;
    try
    {
      FindWithCapabilities find = new FindWithCapabilities();
      try
      {
        printMisses = Utils.getFlag("misses", args);
        find.setOptions(args);
        Utils.checkForRemainingOptions(args);
      }
      catch (Exception ex) {
        String result = ex.getMessage() + "\n\n" + find.getClass().getName().replaceAll(".*\\.", "") + " Options:\n\n";
        Enumeration enm = find.listOptions();
        while (enm.hasMoreElements()) {
          Option option = (Option)enm.nextElement();
          result = result + option.synopsis() + "\n" + option.description() + "\n";
        }
        throw new Exception(result);
      }
      
      System.out.println("\nSearching for the following Capabilities:");
      
      System.out.print("- allowed: ");
      Iterator iter = find.getCapabilities().capabilities();
      boolean first = true;
      while (iter.hasNext()) {
        if (!first)
          System.out.print(", ");
        first = false;
        System.out.print(iter.next());
      }
      System.out.println();
      
      System.out.print("- not allowed: ");
      iter = find.getNotCapabilities().capabilities();
      first = true;
      if (iter.hasNext()) {
        while (iter.hasNext()) {
          if (!first)
            System.out.print(", ");
          first = false;
          System.out.print(iter.next());
        }
        System.out.println();
      }
      else {
        System.out.println("-");
      }
      
      find.find();
      

      Vector list = find.getMatches();
      if (list.size() == 1) {
        System.out.println("\nFound " + list.size() + " class that matched the criteria:\n");
      } else
        System.out.println("\nFound " + list.size() + " classes that matched the criteria:\n");
      for (int i = 0; i < list.size(); i++) {
        System.out.println(list.get(i));
      }
      
      if (printMisses) {
        list = find.getMisses();
        if (list.size() == 1) {
          System.out.println("\nFound " + list.size() + " class that didn't match the criteria:\n");
        } else
          System.out.println("\nFound " + list.size() + " classes that didn't match the criteria:\n");
        for (i = 0; i < list.size(); i++) {
          System.out.println(list.get(i));
        }
      }
      System.out.println();
    }
    catch (Exception ex) {
      System.err.println(ex.getMessage());
    }
  }
}
