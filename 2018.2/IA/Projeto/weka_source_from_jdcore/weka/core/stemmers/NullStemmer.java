package weka.core.stemmers;

import weka.core.RevisionUtils;



































public class NullStemmer
  implements Stemmer
{
  static final long serialVersionUID = -3671261636532625496L;
  
  public NullStemmer() {}
  
  public String globalInfo()
  {
    return "A dummy stemmer that performs no stemming at all.";
  }
  






  public String stem(String word)
  {
    return new String(word);
  }
  




  public String toString()
  {
    return getClass().getName();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
  



  public static void main(String[] args)
  {
    try
    {
      Stemming.useStemmer(new NullStemmer(), args);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
