package weka.core.stemmers;

import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;






















































public class IteratedLovinsStemmer
  extends LovinsStemmer
{
  static final long serialVersionUID = 960689687163788264L;
  
  public IteratedLovinsStemmer() {}
  
  public String globalInfo()
  {
    return "An iterated version of the Lovins stemmer. It stems the word (in case it's longer than 2 characters) until it no further changes.\n\nFor more information about the Lovins stemmer see:\n\n" + getTechnicalInformation().toString();
  }
  











  public String stem(String str)
  {
    if (str.length() <= 2) {
      return str;
    }
    String stemmed = super.stem(str);
    while (!stemmed.equals(str)) {
      str = stemmed;
      stemmed = super.stem(stemmed);
    }
    return stemmed;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
  



  public static void main(String[] args)
  {
    try
    {
      Stemming.useStemmer(new IteratedLovinsStemmer(), args);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
