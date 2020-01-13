package weka.core.tokenizers;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Option;
import weka.core.Utils;































public abstract class CharacterDelimitedTokenizer
  extends Tokenizer
{
  protected String m_Delimiters = " \r\n\t.,;:'\"()?!";
  


  public CharacterDelimitedTokenizer() {}
  


  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe delimiters to use\n\t(default ' \\r\\n\\t.,;:'\"()?!').", "delimiters", 1, "-delimiters <value>"));
    



    return result.elements();
  }
  







  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-delimiters");
    result.add(getDelimiters());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  








  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption("delimiters", options);
    if (tmpStr.length() != 0) {
      setDelimiters(tmpStr);
    } else {
      setDelimiters(" \r\n\t.,;:'\"()?!");
    }
  }
  



  public String getDelimiters()
  {
    return m_Delimiters;
  }
  









  public void setDelimiters(String value)
  {
    m_Delimiters = Utils.unbackQuoteChars(value);
  }
  





  public String delimitersTipText()
  {
    return "Set of delimiter characters to use in tokenizing (\\r, \\n and \\t can be used for carriage-return, line-feed and tab)";
  }
}
