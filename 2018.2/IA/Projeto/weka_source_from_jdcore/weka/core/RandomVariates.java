package weka.core;

import java.io.PrintStream;
import java.util.Random;

































public final class RandomVariates
  extends Random
  implements RevisionHandler
{
  private static final long serialVersionUID = -4763742718209460354L;
  
  public RandomVariates() {}
  
  public RandomVariates(long seed)
  {
    super(seed);
  }
  




  protected int next(int bits)
  {
    return super.next(bits);
  }
  






  public double nextExponential()
  {
    return -Math.log(1.0D - super.nextDouble());
  }
  









  public double nextErlang(int a)
    throws Exception
  {
    if (a < 1) {
      throw new Exception("Shape parameter of Erlang distribution must be greater than 1!");
    }
    double product = 1.0D;
    for (int i = 1; i <= a; i++) {
      product *= super.nextDouble();
    }
    return -Math.log(product);
  }
  


















  public double nextGamma(double a)
    throws Exception
  {
    if (a <= 0.0D) {
      throw new Exception("Shape parameter of Gamma distributionmust be greater than 0!");
    }
    if (a == 1.0D)
      return nextExponential();
    if (a < 1.0D) {
      double b = 1.0D + Math.exp(-1.0D) * a;
      double x;
      double condition; do { double p = b * super.nextDouble();
        double condition; if (p < 1.0D) {
          double x = Math.exp(Math.log(p) / a);
          condition = x;
        }
        else {
          x = -Math.log((b - p) / a);
          condition = (1.0D - a) * Math.log(x);
        }
        
      } while (nextExponential() < condition);
      return x;
    }
    
    double b = a - 1.0D;double D = Math.sqrt(b);
    double f1;
    double D1;
    double x2;
    double x1; double xl; double f1; if (a <= 2.0D) {
      double D1 = b / 2.0D;
      double x1 = 0.0D;
      double x2 = D1;
      double xl = -1.0D;
      f1 = 0.0D;
    }
    else {
      D1 = D - 0.5D;
      x2 = b - D1;
      x1 = x2 - D1;
      xl = 1.0D - b / x1;
      f1 = Math.exp(b * Math.log(x1 / b) + 2.0D * D1);
    }
    
    double f2 = Math.exp(b * Math.log(x2 / b) + D1);
    double x4 = b + D;
    double x5 = x4 + D;
    double xr = 1.0D - b / x5;
    double f4 = Math.exp(b * Math.log(x4 / b) - D);
    double f5 = Math.exp(b * Math.log(x5 / b) - 2.0D * D);
    double p1 = 2.0D * f4 * D;
    double p2 = 2.0D * f2 * D1 + p1;
    double p3 = f5 / xr + p2;
    double p4 = -f1 / xl + p3;
    

    double w = Double.MAX_VALUE;double x = b;
    while ((Math.log(w) > b * Math.log(x / b) + b - x) || (x < 0.0D)) {
      double u = super.nextDouble() * p4;
      if (u <= p1) {
        w = u / D - f4;
        if (w <= 0.0D) return b + u / f4;
        if (w <= f5) { return x4 + w * D / f5;
        }
        double v = super.nextDouble();
        x = x4 + v * D;
        double xp = 2.0D * x4 - x;
        
        if (w >= f4 + (f4 - 1.0D) * (x - x4) / (x4 - b))
          return xp;
        if (w <= f4 + (b / x4 - 1.0D) * f4 * (x - x4))
          return x;
        if ((w >= 2.0D * f4 - 1.0D) && (w >= 2.0D * f4 - Math.exp(b * Math.log(xp / b) + b - xp)))
        {

          return xp;
        }
      } else if (u <= p2) {
        w = (u - p1) / D1 - f2;
        if (w <= 0.0D) return b - (u - p1) / f2;
        if (w <= f1) { return x1 + w * D1 / f1;
        }
        double v = super.nextDouble();
        x = x1 + v * D1;
        double xp = 2.0D * x2 - x;
        
        if (w >= f2 + (f2 - 1.0D) * (x - x2) / (x2 - b))
          return xp;
        if (w <= f2 * (x - x1) / D1)
          return x;
        if ((w >= 2.0D * f2 - 1.0D) && (w >= 2.0D * f2 - Math.exp(b * Math.log(xp / b) + b - xp)))
        {

          return xp;
        }
      } else if (u < p3) {
        w = super.nextDouble();
        u = (p3 - u) / (p3 - p2);
        x = x5 - Math.log(u) / xr;
        if (w <= (xr * (x5 - x) + 1.0D) / u) return x;
        w = w * f5 * u;
      }
      else {
        w = super.nextDouble();
        u = (p4 - u) / (p4 - p3);
        x = x1 - Math.log(u) / xl;
        if (x >= 0.0D) {
          if (w <= (xl * (x1 - x) + 1.0D) / u) return x;
          w = w * f1 * u;
        }
      }
    }
    return x;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5360 $");
  }
  






  public static void main(String[] ops)
  {
    int n = Integer.parseInt(ops[0]);
    if (n <= 0)
      n = 10;
    long seed = Long.parseLong(ops[1]);
    if (seed <= 0L)
      seed = 45L;
    RandomVariates var = new RandomVariates(seed);
    double[] varb = new double[n];
    try
    {
      System.out.println("Generate " + n + " values with std. exp dist:");
      for (int i = 0; i < n; i++) {
        varb[i] = var.nextExponential();
        System.out.print("[" + i + "] " + varb[i] + ", ");
      }
      
      System.out.println("\nMean is " + Utils.mean(varb) + ", Variance is " + Utils.variance(varb) + "\n\nGenerate " + n + " values with" + " std. Erlang-5 dist:");
      



      for (int i = 0; i < n; i++) {
        varb[i] = var.nextErlang(5);
        System.out.print("[" + i + "] " + varb[i] + ", ");
      }
      
      System.out.println("\nMean is " + Utils.mean(varb) + ", Variance is " + Utils.variance(varb) + "\n\nGenerate " + n + " values with" + " std. Gamma(4.5) dist:");
      



      for (int i = 0; i < n; i++) {
        varb[i] = var.nextGamma(4.5D);
        System.out.print("[" + i + "] " + varb[i] + ", ");
      }
      
      System.out.println("\nMean is " + Utils.mean(varb) + ", Variance is " + Utils.variance(varb) + "\n\nGenerate " + n + " values with" + " std. Gamma(0.5) dist:");
      



      for (int i = 0; i < n; i++) {
        varb[i] = var.nextGamma(0.5D);
        System.out.print("[" + i + "] " + varb[i] + ", ");
      }
      
      System.out.println("\nMean is " + Utils.mean(varb) + ", Variance is " + Utils.variance(varb) + "\n\nGenerate " + n + " values with" + " std. Gaussian(5, 2) dist:");
      



      for (int i = 0; i < n; i++) {
        varb[i] = (var.nextGaussian() * 2.0D + 5.0D);
        System.out.print("[" + i + "] " + varb[i] + ", ");
      }
      System.out.println("\nMean is " + Utils.mean(varb) + ", Variance is " + Utils.variance(varb) + "\n");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
