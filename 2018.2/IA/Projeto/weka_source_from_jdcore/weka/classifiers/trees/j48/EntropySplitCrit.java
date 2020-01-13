package weka.classifiers.trees.j48;

import weka.core.RevisionUtils;
import weka.core.Utils;































public final class EntropySplitCrit
  extends EntropyBasedSplitCrit
{
  private static final long serialVersionUID = 5986252682266803935L;
  
  public EntropySplitCrit() {}
  
  public final double splitCritValue(Distribution bags)
  {
    return newEnt(bags);
  }
  



  public final double splitCritValue(Distribution train, Distribution test)
  {
    double result = 0.0D;
    int numClasses = 0;
    


    for (int j = 0; j < test.numClasses(); j++) {
      if ((Utils.gr(train.perClass(j), 0.0D)) || (Utils.gr(test.perClass(j), 0.0D))) {
        numClasses++;
      }
    }
    for (int i = 0; i < test.numBags(); i++) {
      if (Utils.gr(test.perBag(i), 0.0D)) {
        for (j = 0; j < test.numClasses(); j++) {
          if (Utils.gr(test.perClassPerBag(i, j), 0.0D))
            result -= test.perClassPerBag(i, j) * Math.log(train.perClassPerBag(i, j) + 1.0D);
        }
        result += test.perBag(i) * Math.log(train.perBag(i) + numClasses);
      }
    }
    return result / log2;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
}
