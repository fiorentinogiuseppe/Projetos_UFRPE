package weka.core;



























public class ManhattanDistance
  extends NormalizableDistance
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = 6783782554224000243L;
  


























  public ManhattanDistance() {}
  


























  public ManhattanDistance(Instances data)
  {
    super(data);
  }
  





  public String globalInfo()
  {
    return "Implements the Manhattan distance (or Taxicab geometry). The distance between two points is the sum of the (absolute) differences of their coordinates.\n\nFor more information, see:\n\n" + getTechnicalInformation().toString();
  }
  













  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MISC);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Wikipedia");
    result.setValue(TechnicalInformation.Field.TITLE, "Taxicab geometry");
    result.setValue(TechnicalInformation.Field.URL, "http://en.wikipedia.org/wiki/Taxicab_geometry");
    
    return result;
  }
  











  protected double updateDistance(double currDist, double diff)
  {
    double result = currDist;
    result += Math.abs(diff);
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}
