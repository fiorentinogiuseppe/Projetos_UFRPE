package weka.classifiers.trees.j48;

import java.io.PrintStream;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Statistics;





































public class Stats
  implements RevisionHandler
{
  public Stats() {}
  
  public static double addErrs(double N, double e, float CF)
  {
    if (CF > 0.5D) {
      System.err.println("WARNING: confidence value for pruning  too high. Error estimate not modified.");
      
      return 0.0D;
    }
    


    if (e < 1.0D)
    {


      double base = N * (1.0D - Math.pow(CF, 1.0D / N));
      if (e == 0.0D) {
        return base;
      }
      

      return base + e * (addErrs(N, 1.0D, CF) - base);
    }
    


    if (e + 0.5D >= N)
    {

      return Math.max(N - e, 0.0D);
    }
    

    double z = Statistics.normalInverse(1.0F - CF);
    

    double f = (e + 0.5D) / N;
    double r = (f + z * z / (2.0D * N) + z * Math.sqrt(f / N - f * f / N + z * z / (4.0D * N * N))) / (1.0D + z * z / N);
    




    return r * N - e;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.9 $");
  }
}
