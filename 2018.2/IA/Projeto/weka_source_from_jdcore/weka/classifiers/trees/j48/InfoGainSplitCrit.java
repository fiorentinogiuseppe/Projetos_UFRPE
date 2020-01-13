package weka.classifiers.trees.j48;

import weka.core.RevisionUtils;
import weka.core.Utils;


































public final class InfoGainSplitCrit
  extends EntropyBasedSplitCrit
{
  private static final long serialVersionUID = 4892105020180728499L;
  
  public InfoGainSplitCrit() {}
  
  public final double splitCritValue(Distribution bags)
  {
    double numerator = oldEnt(bags) - newEnt(bags);
    

    if (Utils.eq(numerator, 0.0D)) {
      return Double.MAX_VALUE;
    }
    

    return bags.total() / numerator;
  }
  













  public final double splitCritValue(Distribution bags, double totalNoInst)
  {
    double noUnknown = totalNoInst - bags.total();
    double unknownRate = noUnknown / totalNoInst;
    double numerator = oldEnt(bags) - newEnt(bags);
    numerator = (1.0D - unknownRate) * numerator;
    

    if (Utils.eq(numerator, 0.0D)) {
      return 0.0D;
    }
    return numerator / bags.total();
  }
  














  public final double splitCritValue(Distribution bags, double totalNoInst, double oldEnt)
  {
    double noUnknown = totalNoInst - bags.total();
    double unknownRate = noUnknown / totalNoInst;
    double numerator = oldEnt - newEnt(bags);
    numerator = (1.0D - unknownRate) * numerator;
    

    if (Utils.eq(numerator, 0.0D)) {
      return 0.0D;
    }
    return numerator / bags.total();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.10 $");
  }
}
