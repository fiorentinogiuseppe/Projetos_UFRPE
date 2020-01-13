package weka.core;

import java.io.PrintStream;






























public final class SpecialFunctions
  implements RevisionHandler
{
  private static double log2 = Math.log(2.0D);
  


  public SpecialFunctions() {}
  


  public static double lnFactorial(double x)
  {
    return Statistics.lnGamma(x + 1.0D);
  }
  







  public static double log2Binomial(double a, double b)
  {
    if (Utils.gr(b, a)) {
      throw new ArithmeticException("Can't compute binomial coefficient.");
    }
    return (lnFactorial(a) - lnFactorial(b) - lnFactorial(a - b)) / log2;
  }
  








  public static double log2Multinomial(double a, double[] bs)
  {
    double sum = 0.0D;
    

    for (int i = 0; i < bs.length; i++) {
      if (Utils.gr(bs[i], a)) {
        throw new ArithmeticException("Can't compute multinomial coefficient.");
      }
      
      sum += lnFactorial(bs[i]);
    }
    
    return (lnFactorial(a) - sum) / log2;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
  



  public static void main(String[] ops)
  {
    double[] doubles = { 1.0D, 2.0D, 3.0D };
    
    System.out.println("6!: " + Math.exp(lnFactorial(6.0D)));
    System.out.println("Binomial 6 over 2: " + Math.pow(2.0D, log2Binomial(6.0D, 2.0D)));
    
    System.out.println("Multinomial 6 over 1, 2, 3: " + Math.pow(2.0D, log2Multinomial(6.0D, doubles)));
  }
}
