package weka.core;

import java.util.Enumeration;
import java.util.Vector;


























































public class OptionHandlerJavadoc
  extends Javadoc
{
  public static final String OPTIONS_STARTTAG = "<!-- options-start -->";
  public static final String OPTIONS_ENDTAG = "<!-- options-end -->";
  protected boolean m_Prolog = true;
  




  public OptionHandlerJavadoc()
  {
    m_StartTag = new String[1];
    m_EndTag = new String[1];
    m_StartTag[0] = "<!-- options-start -->";
    m_EndTag[0] = "<!-- options-end -->";
  }
  







  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tSuppresses the 'Valid options are...' prolog in the Javadoc.", "noprolog", 0, "-noprolog"));
    


    return result.elements();
  }
  




  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    setProlog(!Utils.getFlag("noprolog", options));
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    if (!getProlog()) {
      result.add("-noprolog");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  




  public void setProlog(boolean value)
  {
    m_Prolog = value;
  }
  




  public boolean getProlog()
  {
    return m_Prolog;
  }
  









  protected String generateJavadoc(int index)
    throws Exception
  {
    String result = "";
    
    if (index == 0) {
      if (!canInstantiateClass()) {
        return result;
      }
      if (!ClassDiscovery.hasInterface(OptionHandler.class, getInstance().getClass())) {
        throw new Exception("Class '" + getClassname() + "' is not an OptionHandler!");
      }
      
      OptionHandler handler = (OptionHandler)getInstance();
      Enumeration enm = handler.listOptions();
      if (!enm.hasMoreElements()) {
        return result;
      }
      
      if (getProlog()) {
        result = "Valid options are: <p/>\n\n";
      }
      
      enm = handler.listOptions();
      while (enm.hasMoreElements()) {
        Option option = (Option)enm.nextElement();
        String optionStr = toHTML(option.synopsis()) + "\n" + toHTML(option.description().replaceAll("\\t", " "));
        

        result = result + "<pre> " + optionStr.replaceAll("<br/>", "") + "</pre>\n\n";
      }
      

      if (getUseStars()) {
        result = indent(result, 1, "* ");
      }
    }
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
  




  public static void main(String[] args)
  {
    runJavadoc(new OptionHandlerJavadoc(), args);
  }
}
