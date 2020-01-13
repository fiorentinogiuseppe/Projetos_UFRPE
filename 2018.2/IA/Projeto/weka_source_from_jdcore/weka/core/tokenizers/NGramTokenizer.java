package weka.core.tokenizers;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;

























































public class NGramTokenizer
  extends CharacterDelimitedTokenizer
{
  private static final long serialVersionUID = -2181896254171647219L;
  protected int m_NMax = 3;
  

  protected int m_NMin = 1;
  

  protected int m_N;
  

  protected int m_MaxPosition;
  

  protected int m_CurrentPosition;
  

  protected String[] m_SplitString;
  


  public NGramTokenizer() {}
  


  public String globalInfo()
  {
    return "Splits a string into an n-gram with min and max grams.";
  }
  








  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    
    result.addElement(new Option("\tThe max size of the Ngram (default = 3).", "max", 1, "-max <int>"));
    


    result.addElement(new Option("\tThe min size of the Ngram (default = 1).", "min", 1, "-min <int>"));
    


    return result.elements();
  }
  









  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    
    result.add("-max");
    result.add("" + getNGramMaxSize());
    
    result.add("-min");
    result.add("" + getNGramMinSize());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





























  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String value = Utils.getOption("max", options);
    if (value.length() != 0) {
      setNGramMaxSize(Integer.parseInt(value));
    } else {
      setNGramMaxSize(3);
    }
    
    value = Utils.getOption("min", options);
    if (value.length() != 0) {
      setNGramMinSize(Integer.parseInt(value));
    } else {
      setNGramMinSize(1);
    }
  }
  




  public int getNGramMaxSize()
  {
    return m_NMax;
  }
  




  public void setNGramMaxSize(int value)
  {
    if (value < 1) {
      m_NMax = 1;
    } else {
      m_NMax = value;
    }
  }
  





  public String NGramMaxSizeTipText()
  {
    return "The max N of the NGram.";
  }
  




  public void setNGramMinSize(int value)
  {
    if (value < 1) {
      m_NMin = 1;
    } else {
      m_NMin = value;
    }
  }
  




  public int getNGramMinSize()
  {
    return m_NMin;
  }
  





  public String NGramMinSizeTipText()
  {
    return "The min N of the NGram.";
  }
  








  public boolean hasMoreElements()
  {
    return m_N >= m_NMin;
  }
  





  public Object nextElement()
  {
    String retValue = "";
    



    for (int i = 0; i < m_N; i++) {
      retValue = retValue + " " + m_SplitString[(m_CurrentPosition + i)];
    }
    
    m_CurrentPosition += 1;
    
    if (m_CurrentPosition + m_N - 1 == m_MaxPosition) {
      m_CurrentPosition = 0;
      m_N -= 1;
    }
    
    return retValue.trim();
  }
  






  protected void filterOutEmptyStrings()
  {
    LinkedList<String> clean = new LinkedList();
    
    for (int i = 0; i < m_SplitString.length; i++) {
      if (!m_SplitString[i].equals("")) {
        clean.add(m_SplitString[i]);
      }
    }
    
    String[] newSplit = new String[clean.size()];
    for (int i = 0; i < clean.size(); i++) {
      newSplit[i] = ((String)clean.get(i));
    }
    
    m_SplitString = newSplit;
  }
  





  public void tokenize(String s)
  {
    m_N = m_NMax;
    m_SplitString = s.split("[" + getDelimiters() + "]");
    
    filterOutEmptyStrings();
    
    m_CurrentPosition = 0;
    m_MaxPosition = m_SplitString.length;
    
    if (m_SplitString.length < m_NMax) {
      m_N = m_SplitString.length;
    }
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  





  public static void main(String[] args)
  {
    runTokenizer(new NGramTokenizer(), args);
  }
}
