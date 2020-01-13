package weka.core;

import java.util.Enumeration;
import java.util.Vector;































































public class TechnicalInformationHandlerJavadoc
  extends Javadoc
{
  public static final String PLAINTEXT_STARTTAG = "<!-- technical-plaintext-start -->";
  public static final String PLAINTEXT_ENDTAG = "<!-- technical-plaintext-end -->";
  public static final String BIBTEX_STARTTAG = "<!-- technical-bibtex-start -->";
  public static final String BIBTEX_ENDTAG = "<!-- technical-bibtex-end -->";
  protected boolean m_Prolog = true;
  




  public TechnicalInformationHandlerJavadoc()
  {
    m_StartTag = new String[2];
    m_EndTag = new String[2];
    m_StartTag[0] = "<!-- technical-plaintext-start -->";
    m_EndTag[0] = "<!-- technical-plaintext-end -->";
    m_StartTag[1] = "<!-- technical-bibtex-start -->";
    m_EndTag[1] = "<!-- technical-bibtex-end -->";
  }
  







  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tSuppresses the 'BibTex:' prolog in the Javadoc.", "noprolog", 0, "-noprolog"));
    


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
    
    if (!canInstantiateClass()) {
      return result;
    }
    if (!ClassDiscovery.hasInterface(TechnicalInformationHandler.class, getInstance().getClass())) {
      throw new Exception("Class '" + getClassname() + "' is not a TechnicalInformationHandler!");
    }
    TechnicalInformationHandler handler = (TechnicalInformationHandler)getInstance();
    
    switch (index) {
    case 0: 
      result = toHTML(handler.getTechnicalInformation().toString()) + "\n";
      break;
    

    case 1: 
      if (getProlog())
        result = "BibTeX:\n";
      result = result + "<pre>\n";
      result = result + toHTML(handler.getTechnicalInformation().toBibTex()).replaceAll("<br/>", "") + "\n";
      result = result + "</pre>\n<p/>\n";
    }
    
    

    if (getUseStars()) {
      result = indent(result, 1, "* ");
    }
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.6 $");
  }
  




  public static void main(String[] args)
  {
    runJavadoc(new TechnicalInformationHandlerJavadoc(), args);
  }
}
