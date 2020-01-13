package weka.core.tokenizers;

import java.util.NoSuchElementException;
import weka.core.RevisionUtils;









































public class AlphabeticTokenizer
  extends Tokenizer
{
  private static final long serialVersionUID = 6705199562609861697L;
  protected char[] m_Str;
  protected int m_CurrentPos;
  
  public AlphabeticTokenizer() {}
  
  public String globalInfo()
  {
    return "Alphabetic string tokenizer, tokens are to be formed only from contiguous alphabetic sequences.";
  }
  






  public boolean hasMoreElements()
  {
    int beginpos = m_CurrentPos;
    

    while ((beginpos < m_Str.length) && ((m_Str[beginpos] < 'a') || (m_Str[beginpos] > 'z')) && ((m_Str[beginpos] < 'A') || (m_Str[beginpos] > 'Z')))
    {
      beginpos++;
    }
    m_CurrentPos = beginpos;
    
    if ((beginpos < m_Str.length) && (((m_Str[beginpos] >= 'a') && (m_Str[beginpos] <= 'z')) || ((m_Str[beginpos] >= 'A') && (m_Str[beginpos] <= 'Z'))))
    {

      return true;
    }
    
    return false;
  }
  







  public Object nextElement()
  {
    int beginpos = m_CurrentPos;
    

    while ((beginpos < m_Str.length) && (m_Str[beginpos] < 'a') && (m_Str[beginpos] > 'z') && (m_Str[beginpos] < 'A') && (m_Str[beginpos] > 'Z'))
    {
      beginpos++; }
    int endpos;
    m_CurrentPos = (endpos = beginpos);
    
    if (beginpos >= m_Str.length) {
      throw new NoSuchElementException("No more tokens present");
    }
    while ((endpos < m_Str.length) && (((m_Str[endpos] >= 'a') && (m_Str[endpos] <= 'z')) || ((m_Str[endpos] >= 'A') && (m_Str[endpos] <= 'Z'))))
    {

      endpos++;
    }
    
    String s = new String(m_Str, beginpos, endpos - m_CurrentPos);
    m_CurrentPos = endpos;
    
    return s;
  }
  




  public void tokenize(String s)
  {
    m_CurrentPos = 0;
    m_Str = new char[s.length()];
    s.getChars(0, s.length(), m_Str, 0);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
  





  public static void main(String[] args)
  {
    runTokenizer(new AlphabeticTokenizer(), args);
  }
}
