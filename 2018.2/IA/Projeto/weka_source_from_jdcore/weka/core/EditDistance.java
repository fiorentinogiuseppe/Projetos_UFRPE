package weka.core;















public class EditDistance
  extends AbstractStringDistanceFunction
{
  public EditDistance() {}
  














  public EditDistance(Instances data)
  {
    super(data);
  }
  






  double stringDistance(String stringA, String stringB)
  {
    int lengthA = stringA.length();
    int lengthB = stringB.length();
    
    double[][] distanceMatrix = new double[lengthA + 1][lengthB + 1];
    
    for (int i = 0; i <= lengthA; i++) {
      distanceMatrix[i][0] = i;
    }
    
    for (int j = 1; j <= lengthB; j++) {
      distanceMatrix[0][j] = j;
    }
    
    for (int i = 1; i <= lengthA; i++) {
      for (int j = 1; j <= lengthB; j++) {
        if (stringA.charAt(i - 1) == stringB.charAt(j - 1)) {
          distanceMatrix[i][j] = distanceMatrix[(i - 1)][(j - 1)];
        }
        else {
          distanceMatrix[i][j] = (1.0D + Math.min(distanceMatrix[(i - 1)][j], Math.min(distanceMatrix[i][(j - 1)], distanceMatrix[(i - 1)][(j - 1)])));
        }
      }
    }
    

    return distanceMatrix[lengthA][lengthB];
  }
  






  public String globalInfo()
  {
    return "Implementing Levenshtein distance function.\n\nOne object defines not one distance but the data model in which the distances between objects of that data model can be computed.\n\nAttention: For efficiency reasons the use of consistency checks (like are the data models of the two instances exactly the same), is low.\n\nFor more information, see: http://en.wikipedia.org/wiki/Levenshtein_distance\n\n";
  }
  











  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }
}
