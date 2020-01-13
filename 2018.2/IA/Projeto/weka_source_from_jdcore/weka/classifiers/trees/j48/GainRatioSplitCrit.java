package weka.classifiers.trees.j48;

import weka.core.RevisionUtils;
import weka.core.Utils;



































public final class GainRatioSplitCrit
  extends EntropyBasedSplitCrit
{
  private static final long serialVersionUID = -433336694718670930L;
  
  public GainRatioSplitCrit() {}
  
  public final double splitCritValue(Distribution bags)
  {
    double numerator = oldEnt(bags) - newEnt(bags);
    

    if (Utils.eq(numerator, 0.0D))
      return Double.MAX_VALUE;
    double denumerator = splitEnt(bags);
    

    if (Utils.eq(denumerator, 0.0D)) {
      return Double.MAX_VALUE;
    }
    

    return denumerator / numerator;
  }
  














  public final double splitCritValue(Distribution bags, double totalnoInst, double numerator)
  {
    double denumerator = splitEnt(bags, totalnoInst);
    

    if (Utils.eq(denumerator, 0.0D))
      return 0.0D;
    denumerator /= totalnoInst;
    
    return numerator / denumerator;
  }
  



  private final double splitEnt(Distribution bags, double totalnoInst)
  {
    double returnValue = 0.0D;
    


    double noUnknown = totalnoInst - bags.total();
    if (Utils.gr(bags.total(), 0.0D)) {
      for (int i = 0; i < bags.numBags(); i++)
        returnValue -= logFunc(bags.perBag(i));
      returnValue -= logFunc(noUnknown);
      returnValue += logFunc(totalnoInst);
    }
    return returnValue;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
}
