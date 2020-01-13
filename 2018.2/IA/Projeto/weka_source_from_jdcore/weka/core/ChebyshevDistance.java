package weka.core;



























public class ChebyshevDistance
  extends NormalizableDistance
  implements TechnicalInformationHandler
{
  private static final long serialVersionUID = -7739904999895461429L;
  


























  public ChebyshevDistance() {}
  


























  public ChebyshevDistance(Instances data)
  {
    super(data);
  }
  





  public String globalInfo()
  {
    return "Implements the Chebyshev distance. The distance between two vectors is the greatest of their differences along any coordinate dimension.\n\nFor more information, see:\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.MISC);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Wikipedia");
    result.setValue(TechnicalInformation.Field.TITLE, "Chebyshev distance");
    result.setValue(TechnicalInformation.Field.URL, "http://en.wikipedia.org/wiki/Chebyshev_distance");
    
    return result;
  }
  











  protected double updateDistance(double currDist, double diff)
  {
    double result = currDist;
    
    diff = Math.abs(diff);
    if (diff > result) {
      result = diff;
    }
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}
