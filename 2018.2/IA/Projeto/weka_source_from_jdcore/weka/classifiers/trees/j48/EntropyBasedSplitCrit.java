package weka.classifiers.trees.j48;

















public abstract class EntropyBasedSplitCrit
  extends SplitCriterion
{
  private static final long serialVersionUID = -2618691439791653056L;
  















  protected static double log2 = Math.log(2.0D);
  

  public EntropyBasedSplitCrit() {}
  

  public final double logFunc(double num)
  {
    if (num < 1.0E-6D) {
      return 0.0D;
    }
    return num * Math.log(num) / log2;
  }
  



  public final double oldEnt(Distribution bags)
  {
    double returnValue = 0.0D;
    

    for (int j = 0; j < bags.numClasses(); j++)
      returnValue += logFunc(bags.perClass(j));
    return logFunc(bags.total()) - returnValue;
  }
  



  public final double newEnt(Distribution bags)
  {
    double returnValue = 0.0D;
    

    for (int i = 0; i < bags.numBags(); i++) {
      for (int j = 0; j < bags.numClasses(); j++)
        returnValue += logFunc(bags.perClassPerBag(i, j));
      returnValue -= logFunc(bags.perBag(i));
    }
    return -returnValue;
  }
  




  public final double splitEnt(Distribution bags)
  {
    double returnValue = 0.0D;
    

    for (int i = 0; i < bags.numBags(); i++)
      returnValue += logFunc(bags.perBag(i));
    return logFunc(bags.total()) - returnValue;
  }
}
