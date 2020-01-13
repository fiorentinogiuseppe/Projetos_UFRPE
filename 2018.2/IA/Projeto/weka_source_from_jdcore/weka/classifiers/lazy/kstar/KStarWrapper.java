package weka.classifiers.lazy.kstar;

import weka.core.RevisionHandler;
import weka.core.RevisionUtils;






























public class KStarWrapper
  implements RevisionHandler
{
  public double sphere = 0.0D;
  

  public double actEntropy = 0.0D;
  

  public double randEntropy = 0.0D;
  

  public double avgProb = 0.0D;
  

  public double minProb = 0.0D;
  

  public KStarWrapper() {}
  

  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
}
