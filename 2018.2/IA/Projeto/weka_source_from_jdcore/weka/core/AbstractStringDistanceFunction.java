package weka.core;

import weka.core.neighboursearch.PerformanceStats;








































public abstract class AbstractStringDistanceFunction
  extends NormalizableDistance
{
  public AbstractStringDistanceFunction() {}
  
  public AbstractStringDistanceFunction(Instances data)
  {
    super(data);
  }
  










  protected double updateDistance(double currDist, double diff)
  {
    return currDist + diff * diff;
  }
  








  protected double difference(int index, String string1, String string2)
  {
    switch (m_Data.attribute(index).type()) {
    case 2: 
      double diff = stringDistance(string1, string2);
      if (m_DontNormalize == true) {
        return diff;
      }
      
      if (string1.length() > string2.length()) {
        return diff / string1.length();
      }
      
      return diff / string2.length();
    }
    
    

    return 0.0D;
  }
  


















  public double distance(Instance first, Instance second, double cutOffValue, PerformanceStats stats)
  {
    double sqDistance = 0.0D;
    int numAttributes = m_Data.numAttributes();
    
    validate();
    


    for (int i = 0; i < numAttributes; i++) {
      double diff = 0.0D;
      if (m_ActiveIndices[i] != 0) {
        diff = difference(i, first.stringValue(i), second.stringValue(i));
      }
      sqDistance = updateDistance(sqDistance, diff);
      if (sqDistance > cutOffValue * cutOffValue) return Double.POSITIVE_INFINITY;
    }
    double distance = Math.sqrt(sqDistance);
    return distance;
  }
  
  abstract double stringDistance(String paramString1, String paramString2);
}
