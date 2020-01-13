package weka.core.tokenizers;

import java.util.StringTokenizer;
import weka.core.RevisionUtils;















































public class WordTokenizer
  extends CharacterDelimitedTokenizer
{
  private static final long serialVersionUID = -930893034037880773L;
  protected transient StringTokenizer m_Tokenizer;
  
  public WordTokenizer() {}
  
  public String globalInfo()
  {
    return "A simple tokenizer that is using the java.util.StringTokenizer class to tokenize the strings.";
  }
  







  public boolean hasMoreElements()
  {
    return m_Tokenizer.hasMoreElements();
  }
  





  public Object nextElement()
  {
    return m_Tokenizer.nextElement();
  }
  




  public void tokenize(String s)
  {
    m_Tokenizer = new StringTokenizer(s, getDelimiters());
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  





  public static void main(String[] args)
  {
    runTokenizer(new WordTokenizer(), args);
  }
}
