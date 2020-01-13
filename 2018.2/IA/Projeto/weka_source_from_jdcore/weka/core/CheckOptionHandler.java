package weka.core;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.rules.ZeroR;






























































public class CheckOptionHandler
  extends Check
{
  protected OptionHandler m_OptionHandler = new ZeroR();
  

  protected String[] m_UserOptions = new String[0];
  

  protected boolean m_Success;
  

  public CheckOptionHandler() {}
  

  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tFull name of the OptionHandler analysed.\n\teg: weka.classifiers.rules.ZeroR\n\t(default weka.classifiers.rules.ZeroR)", "W", 1, "-W"));
    




    if (m_OptionHandler != null) {
      result.addElement(new Option("", "", 0, "\nOptions specific to option handler " + m_OptionHandler.getClass().getName() + ":"));
      



      Enumeration enm = m_OptionHandler.listOptions();
      while (enm.hasMoreElements()) {
        result.addElement(enm.nextElement());
      }
    }
    return result.elements();
  }
  






























  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption('W', options);
    if (tmpStr.length() == 0)
      tmpStr = ZeroR.class.getName();
    setUserOptions(Utils.partitionOptions(options));
    setOptionHandler((OptionHandler)Utils.forName(OptionHandler.class, tmpStr, null));
  }
  










  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    if (getOptionHandler() != null) {
      result.add("-W");
      result.add(getOptionHandler().getClass().getName());
    }
    
    if (m_OptionHandler != null) {
      options = m_OptionHandler.getOptions();
      result.add("--");
      for (i = 0; i < options.length; i++) {
        result.add(options[i]);
      }
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public void setOptionHandler(OptionHandler value)
  {
    m_OptionHandler = value;
  }
  




  public OptionHandler getOptionHandler()
  {
    return m_OptionHandler;
  }
  




  public void setUserOptions(String[] value)
  {
    m_UserOptions = getCopy(value);
  }
  




  public String[] getUserOptions()
  {
    return getCopy(m_UserOptions);
  }
  




  public boolean getSuccess()
  {
    return m_Success;
  }
  





  protected String printOptions(String[] options)
  {
    if (options == null) {
      return "<null>";
    }
    return Utils.joinOptions(options);
  }
  








  protected void compareOptions(String[] options1, String[] options2)
    throws Exception
  {
    if (options1 == null) {
      throw new Exception("first set of options is null!");
    }
    if (options2 == null) {
      throw new Exception("second set of options is null!");
    }
    if (options1.length != options2.length) {
      throw new Exception("problem found!\nFirst set: " + printOptions(options1) + '\n' + "Second set: " + printOptions(options2) + '\n' + "options differ in length");
    }
    


    for (int i = 0; i < options1.length; i++) {
      if (!options1[i].equals(options2[i]))
      {
        throw new Exception("problem found!\n\tFirst set: " + printOptions(options1) + '\n' + "\tSecond set: " + printOptions(options2) + '\n' + '\t' + options1[i] + " != " + options2[i]);
      }
    }
  }
  










  protected String[] getCopy(String[] options)
  {
    String[] result = new String[options.length];
    System.arraycopy(options, 0, result, 0, options.length);
    
    return result;
  }
  


  protected OptionHandler getDefaultHandler()
  {
    OptionHandler result;
    

    try
    {
      result = (OptionHandler)m_OptionHandler.getClass().newInstance();
    }
    catch (Exception e) {
      e.printStackTrace();
      result = null;
    }
    
    return result;
  }
  







  protected String[] getDefaultOptions()
  {
    OptionHandler o = getDefaultHandler();
    String[] result; String[] result; if (o == null) {
      println("WARNING: couldn't create default handler, cannot use default options!");
      result = new String[0];
    }
    else {
      result = o.getOptions();
    }
    
    return result;
  }
  






  public boolean checkListOptions()
  {
    print("ListOptions...");
    boolean result;
    try {
      Enumeration enu = getOptionHandler().listOptions();
      if ((getDebug()) && (enu.hasMoreElements()))
        println("");
      while (enu.hasMoreElements()) {
        Option option = (Option)enu.nextElement();
        if (getDebug()) {
          println(option.synopsis());
          println(option.description());
        }
      }
      
      println("yes");
      result = true;
    }
    catch (Exception e) {
      println("no");
      result = false;
      
      if (getDebug()) {
        println(e);
      }
    }
    return result;
  }
  






  public boolean checkSetOptions()
  {
    print("SetOptions...");
    boolean result;
    try {
      getDefaultHandler().setOptions(getUserOptions());
      println("yes");
      result = true;
    }
    catch (Exception e) {
      println("no");
      result = false;
      
      if (getDebug()) {
        println(e);
      }
    }
    return result;
  }
  








  public boolean checkDefaultOptions()
  {
    print("Default options...");
    
    String[] options = getDefaultOptions();
    boolean result;
    try {
      getDefaultHandler().setOptions(options);
      Utils.checkForRemainingOptions(options);
      println("yes");
      result = true;
    }
    catch (Exception e) {
      println("no");
      result = false;
      
      if (getDebug()) {
        println(e);
      }
    }
    return result;
  }
  








  public boolean checkRemainingOptions()
  {
    print("Remaining options...");
    
    String[] options = getUserOptions();
    boolean result;
    try {
      getDefaultHandler().setOptions(options);
      if (getDebug())
        println("\n  remaining: " + printOptions(options));
      println("yes");
      result = true;
    }
    catch (Exception e) {
      println("no");
      result = false;
      
      if (getDebug()) {
        println(e);
      }
    }
    return result;
  }
  










  public boolean checkCanonicalUserOptions()
  {
    print("Canonical user options...");
    boolean result;
    try {
      OptionHandler handler = getDefaultHandler();
      handler.setOptions(getUserOptions());
      if (getDebug())
        print("\n  Getting canonical user options: ");
      String[] userOptions = handler.getOptions();
      if (getDebug())
        println(printOptions(userOptions));
      if (getDebug())
        println("  Setting canonical user options");
      handler.setOptions((String[])userOptions.clone());
      if (getDebug())
        println("  Checking canonical user options");
      String[] userOptionsCheck = handler.getOptions();
      compareOptions(userOptions, userOptionsCheck);
      
      println("yes");
      result = true;
    }
    catch (Exception e) {
      println("no");
      result = false;
      
      if (getDebug()) {
        println(e);
      }
    }
    return result;
  }
  










  public boolean checkResettingOptions()
  {
    print("Resetting options...");
    boolean result;
    try {
      if (getDebug())
        println("\n  Setting user options");
      OptionHandler handler = getDefaultHandler();
      handler.setOptions(getUserOptions());
      String[] defaultOptions = getDefaultOptions();
      if (getDebug())
        println("  Resetting to default options");
      handler.setOptions(getCopy(defaultOptions));
      if (getDebug())
        println("  Checking default options match previous default");
      String[] defaultOptionsCheck = handler.getOptions();
      compareOptions(defaultOptions, defaultOptionsCheck);
      
      println("yes");
      result = true;
    }
    catch (Exception e) {
      println("no");
      result = false;
      
      if (getDebug()) {
        println(e);
      }
    }
    return result;
  }
  



  public void doTests()
  {
    println("OptionHandler: " + m_OptionHandler.getClass().getName() + "\n");
    
    if (getDebug()) {
      println("--> Info");
      print("Default options: ");
      println(printOptions(getDefaultOptions()));
      print("User options: ");
      println(printOptions(getUserOptions()));
    }
    
    println("--> Tests");
    m_Success = checkListOptions();
    
    if (m_Success) {
      m_Success = checkSetOptions();
    }
    if (m_Success) {
      m_Success = checkDefaultOptions();
    }
    if (m_Success) {
      m_Success = checkRemainingOptions();
    }
    if (m_Success) {
      m_Success = checkCanonicalUserOptions();
    }
    if (m_Success) {
      m_Success = checkResettingOptions();
    }
  }
  



  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.13 $");
  }
  




  public static void main(String[] args)
  {
    CheckOptionHandler check = new CheckOptionHandler();
    runCheck(check, args);
    if (check.getSuccess()) {
      System.exit(0);
    } else {
      System.exit(1);
    }
  }
}
