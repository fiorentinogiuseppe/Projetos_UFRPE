package weka.core.matrix;

import java.util.Random;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Statistics;


































public class Maths
  implements RevisionHandler
{
  public static final double PSI = 0.3989422804014327D;
  public static final double logPSI = -0.9189385332046727D;
  public static final int undefinedDistribution = 0;
  public static final int normalDistribution = 1;
  public static final int chisqDistribution = 2;
  
  public Maths() {}
  
  public static double hypot(double a, double b)
  {
    double r;
    if (Math.abs(a) > Math.abs(b)) {
      double r = b / a;
      r = Math.abs(a) * Math.sqrt(1.0D + r * r);
    } else if (b != 0.0D) {
      double r = a / b;
      r = Math.abs(b) * Math.sqrt(1.0D + r * r);
    } else {
      r = 0.0D;
    }
    return r;
  }
  





  public static double square(double x)
  {
    return x * x;
  }
  






  public static double pnorm(double x)
  {
    return Statistics.normalProbability(x);
  }
  






  public static double pnorm(double x, double mean, double sd)
  {
    if (sd <= 0.0D)
      throw new IllegalArgumentException("standard deviation <= 0.0");
    return pnorm((x - mean) / sd);
  }
  








  public static DoubleVector pnorm(double x, DoubleVector mean, double sd)
  {
    DoubleVector p = new DoubleVector(mean.size());
    
    for (int i = 0; i < mean.size(); i++) {
      p.set(i, pnorm(x, mean.get(i), sd));
    }
    return p;
  }
  




  public static double dnorm(double x)
  {
    return Math.exp(-x * x / 2.0D) * 0.3989422804014327D;
  }
  





  public static double dnorm(double x, double mean, double sd)
  {
    if (sd <= 0.0D)
      throw new IllegalArgumentException("standard deviation <= 0.0");
    return dnorm((x - mean) / sd);
  }
  







  public static DoubleVector dnorm(double x, DoubleVector mean, double sd)
  {
    DoubleVector den = new DoubleVector(mean.size());
    
    for (int i = 0; i < mean.size(); i++) {
      den.set(i, dnorm(x, mean.get(i), sd));
    }
    return den;
  }
  




  public static double dnormLog(double x)
  {
    return -0.9189385332046727D - x * x / 2.0D;
  }
  




  public static double dnormLog(double x, double mean, double sd)
  {
    if (sd <= 0.0D)
      throw new IllegalArgumentException("standard deviation <= 0.0");
    return -Math.log(sd) + dnormLog((x - mean) / sd);
  }
  







  public static DoubleVector dnormLog(double x, DoubleVector mean, double sd)
  {
    DoubleVector denLog = new DoubleVector(mean.size());
    
    for (int i = 0; i < mean.size(); i++) {
      denLog.set(i, dnormLog(x, mean.get(i), sd));
    }
    return denLog;
  }
  









  public static DoubleVector rnorm(int n, double mean, double sd, Random random)
  {
    if (sd < 0.0D) {
      throw new IllegalArgumentException("standard deviation < 0.0");
    }
    if (sd == 0.0D) return new DoubleVector(n, mean);
    DoubleVector v = new DoubleVector(n);
    for (int i = 0; i < n; i++)
      v.set(i, (random.nextGaussian() + mean) / sd);
    return v;
  }
  





  public static double pchisq(double x)
  {
    double xh = Math.sqrt(x);
    return pnorm(xh) - pnorm(-xh);
  }
  




  public static double pchisq(double x, double ncp)
  {
    double mean = Math.sqrt(ncp);
    double xh = Math.sqrt(x);
    return pnorm(xh - mean) - pnorm(-xh - mean);
  }
  




  public static DoubleVector pchisq(double x, DoubleVector ncp)
  {
    int n = ncp.size();
    DoubleVector p = new DoubleVector(n);
    
    double xh = Math.sqrt(x);
    
    for (int i = 0; i < n; i++) {
      double mean = Math.sqrt(ncp.get(i));
      p.set(i, pnorm(xh - mean) - pnorm(-xh - mean));
    }
    return p;
  }
  




  public static double dchisq(double x)
  {
    if (x == 0.0D) return Double.POSITIVE_INFINITY;
    double xh = Math.sqrt(x);
    return dnorm(xh) / xh;
  }
  




  public static double dchisq(double x, double ncp)
  {
    if (ncp == 0.0D) return dchisq(x);
    double xh = Math.sqrt(x);
    double mean = Math.sqrt(ncp);
    return (dnorm(xh - mean) + dnorm(-xh - mean)) / (2.0D * xh);
  }
  




  public static DoubleVector dchisq(double x, DoubleVector ncp)
  {
    int n = ncp.size();
    DoubleVector d = new DoubleVector(n);
    double xh = Math.sqrt(x);
    
    for (int i = 0; i < n; i++) {
      double mean = Math.sqrt(ncp.get(i));
      if (ncp.get(i) == 0.0D) d.set(i, dchisq(x)); else {
        d.set(i, (dnorm(xh - mean) + dnorm(-xh - mean)) / (2.0D * xh));
      }
    }
    return d;
  }
  




  public static double dchisqLog(double x)
  {
    if (x == 0.0D) return Double.POSITIVE_INFINITY;
    double xh = Math.sqrt(x);
    return dnormLog(xh) - Math.log(xh);
  }
  



  public static double dchisqLog(double x, double ncp)
  {
    if (ncp == 0.0D) return dchisqLog(x);
    double xh = Math.sqrt(x);
    double mean = Math.sqrt(ncp);
    return Math.log(dnorm(xh - mean) + dnorm(-xh - mean)) - Math.log(2.0D * xh);
  }
  





  public static DoubleVector dchisqLog(double x, DoubleVector ncp)
  {
    DoubleVector dLog = new DoubleVector(ncp.size());
    double xh = Math.sqrt(x);
    

    for (int i = 0; i < ncp.size(); i++) {
      double mean = Math.sqrt(ncp.get(i));
      if (ncp.get(i) == 0.0D) dLog.set(i, dchisqLog(x)); else {
        dLog.set(i, Math.log(dnorm(xh - mean) + dnorm(-xh - mean)) - Math.log(2.0D * xh));
      }
    }
    return dLog;
  }
  







  public static DoubleVector rchisq(int n, double ncp, Random random)
  {
    DoubleVector v = new DoubleVector(n);
    double mean = Math.sqrt(ncp);
    
    for (int i = 0; i < n; i++) {
      double x = random.nextGaussian() + mean;
      v.set(i, x * x);
    }
    return v;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
  }
}
