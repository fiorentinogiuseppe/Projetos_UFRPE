package weka.classifiers.functions.pace;

import java.io.PrintStream;
import java.util.Random;
import weka.core.RevisionUtils;
import weka.core.matrix.DoubleVector;
import weka.core.matrix.Maths;




























































public class ChisqMixture
  extends MixtureDistribution
{
  protected double separatingThreshold = 0.05D;
  

  protected double trimingThreshold = 0.5D;
  
  protected double supportThreshold = 0.5D;
  
  protected int maxNumSupportPoints = 200;
  
  protected int fittingIntervalLength = 3;
  
  protected double fittingIntervalThreshold = 0.5D;
  




  public ChisqMixture() {}
  



  public double getSeparatingThreshold()
  {
    return separatingThreshold;
  }
  




  public void setSeparatingThreshold(double t)
  {
    separatingThreshold = t;
  }
  




  public double getTrimingThreshold()
  {
    return trimingThreshold;
  }
  




  public void setTrimingThreshold(double t)
  {
    trimingThreshold = t;
  }
  










  public boolean separable(DoubleVector data, int i0, int i1, double x)
  {
    DoubleVector dataSqrt = data.sqrt();
    double xh = Math.sqrt(x);
    
    NormalMixture m = new NormalMixture();
    m.setSeparatingThreshold(separatingThreshold);
    return m.separable(dataSqrt, i0, i1, xh);
  }
  








  public DoubleVector supportPoints(DoubleVector data, int ne)
  {
    DoubleVector sp = new DoubleVector();
    sp.setCapacity(data.size() + 1);
    
    if ((data.get(0) < supportThreshold) || (ne != 0))
      sp.addElement(0.0D);
    for (int i = 0; i < data.size(); i++) {
      if (data.get(i) > supportThreshold) {
        sp.addElement(data.get(i));
      }
    }
    if (sp.size() > maxNumSupportPoints) {
      throw new IllegalArgumentException("Too many support points. ");
    }
    return sp;
  }
  






  public PaceMatrix fittingIntervals(DoubleVector data)
  {
    PaceMatrix a = new PaceMatrix(data.size() * 2, 2);
    DoubleVector v = data.sqrt();
    int count = 0;
    
    for (int i = 0; i < data.size(); i++) {
      double left = v.get(i) - fittingIntervalLength;
      if (left < fittingIntervalThreshold) left = 0.0D;
      left *= left;
      double right = data.get(i);
      if (right < fittingIntervalThreshold)
        right = fittingIntervalThreshold;
      a.set(count, 0, left);
      a.set(count, 1, right);
      count++;
    }
    for (int i = 0; i < data.size(); i++) {
      double left = data.get(i);
      if (left < fittingIntervalThreshold) left = 0.0D;
      double right = v.get(i) + fittingIntervalThreshold;
      right *= right;
      a.set(count, 0, left);
      a.set(count, 1, right);
      count++;
    }
    a.setRowDimension(count);
    
    return a;
  }
  








  public PaceMatrix probabilityMatrix(DoubleVector s, PaceMatrix intervals)
  {
    int ns = s.size();
    int nr = intervals.getRowDimension();
    PaceMatrix p = new PaceMatrix(nr, ns);
    
    for (int i = 0; i < nr; i++) {
      for (int j = 0; j < ns; j++) {
        p.set(i, j, Maths.pchisq(intervals.get(i, 1), s.get(j)) - Maths.pchisq(intervals.get(i, 0), s.get(j)));
      }
    }
    


    return p;
  }
  







  public double pace6(double x)
  {
    if (x > 100.0D) return x;
    DoubleVector points = mixingDistribution.getPointValues();
    DoubleVector values = mixingDistribution.getFunctionValues();
    DoubleVector mean = points.sqrt();
    
    DoubleVector d = Maths.dchisqLog(x, points);
    d.minusEquals(d.max());
    d = d.map("java.lang.Math", "exp").timesEquals(values);
    double atilde = mean.innerProduct(d) / d.sum();
    return atilde * atilde;
  }
  






  public DoubleVector pace6(DoubleVector x)
  {
    DoubleVector pred = new DoubleVector(x.size());
    for (int i = 0; i < x.size(); i++)
      pred.set(i, pace6(x.get(i)));
    trim(pred);
    return pred;
  }
  






  public DoubleVector pace2(DoubleVector x)
  {
    DoubleVector chf = new DoubleVector(x.size());
    for (int i = 0; i < x.size(); i++) { chf.set(i, hf(x.get(i)));
    }
    chf.cumulateInPlace();
    
    int index = chf.indexOfMax();
    
    DoubleVector copy = x.copy();
    if (index < x.size() - 1) copy.set(index + 1, x.size() - 1, 0.0D);
    trim(copy);
    return copy;
  }
  






  public DoubleVector pace4(DoubleVector x)
  {
    DoubleVector h = h(x);
    DoubleVector copy = x.copy();
    for (int i = 0; i < x.size(); i++)
      if (h.get(i) <= 0.0D) copy.set(i, 0.0D);
    trim(copy);
    return copy;
  }
  





  public void trim(DoubleVector x)
  {
    for (int i = 0; i < x.size(); i++) {
      if (x.get(i) <= trimingThreshold) { x.set(i, 0.0D);
      }
    }
  }
  






  public double hf(double AHat)
  {
    DoubleVector points = mixingDistribution.getPointValues();
    DoubleVector values = mixingDistribution.getFunctionValues();
    
    double x = Math.sqrt(AHat);
    DoubleVector mean = points.sqrt();
    DoubleVector d1 = Maths.dnormLog(x, mean, 1.0D);
    double d1max = d1.max();
    d1.minusEquals(d1max);
    DoubleVector d2 = Maths.dnormLog(-x, mean, 1.0D);
    d2.minusEquals(d1max);
    
    d1 = d1.map("java.lang.Math", "exp");
    d1.timesEquals(values);
    d2 = d2.map("java.lang.Math", "exp");
    d2.timesEquals(values);
    
    return (points.minus(x / 2.0D).innerProduct(d1) - points.plus(x / 2.0D).innerProduct(d2)) / (d1.sum() + d2.sum());
  }
  








  public double h(double AHat)
  {
    if (AHat == 0.0D) return 0.0D;
    DoubleVector points = mixingDistribution.getPointValues();
    DoubleVector values = mixingDistribution.getFunctionValues();
    
    double aHat = Math.sqrt(AHat);
    DoubleVector aStar = points.sqrt();
    DoubleVector d1 = Maths.dnorm(aHat, aStar, 1.0D).timesEquals(values);
    DoubleVector d2 = Maths.dnorm(-aHat, aStar, 1.0D).timesEquals(values);
    
    return points.minus(aHat / 2.0D).innerProduct(d1) - points.plus(aHat / 2.0D).innerProduct(d2);
  }
  







  public DoubleVector h(DoubleVector AHat)
  {
    DoubleVector h = new DoubleVector(AHat.size());
    for (int i = 0; i < AHat.size(); i++)
      h.set(i, h(AHat.get(i)));
    return h;
  }
  






  public double f(double x)
  {
    DoubleVector points = mixingDistribution.getPointValues();
    DoubleVector values = mixingDistribution.getFunctionValues();
    
    return Maths.dchisq(x, points).timesEquals(values).sum();
  }
  






  public DoubleVector f(DoubleVector x)
  {
    DoubleVector f = new DoubleVector(x.size());
    for (int i = 0; i < x.size(); i++)
      f.set(i, h(f.get(i)));
    return f;
  }
  




  public String toString()
  {
    return mixingDistribution.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
  





  public static void main(String[] args)
  {
    int n1 = 50;
    int n2 = 50;
    double ncp1 = 0.0D;
    double ncp2 = 10.0D;
    double mu1 = Math.sqrt(ncp1);
    double mu2 = Math.sqrt(ncp2);
    DoubleVector a = Maths.rnorm(n1, mu1, 1.0D, new Random());
    a = a.cat(Maths.rnorm(n2, mu2, 1.0D, new Random()));
    DoubleVector aNormal = a;
    a = a.square();
    a.sort();
    
    DoubleVector means = new DoubleVector(n1, mu1).cat(new DoubleVector(n2, mu2));
    
    System.out.println("==========================================================");
    System.out.println("This is to test the estimation of the mixing\ndistribution of the mixture of non-central Chi-square\ndistributions. The example mixture used is of the form: \n\n   0.5 * Chi^2_1(ncp1) + 0.5 * Chi^2_1(ncp2)\n");
    



    System.out.println("It also tests the PACE estimators. Quadratic losses of the\nestimators are given, measuring their performance.");
    
    System.out.println("==========================================================");
    System.out.println("ncp1 = " + ncp1 + " ncp2 = " + ncp2 + "\n");
    
    System.out.println(a.size() + " observations are: \n\n" + a);
    
    System.out.println("\nQuadratic loss of the raw data (i.e., the MLE) = " + aNormal.sum2(means));
    
    System.out.println("==========================================================");
    

    ChisqMixture d = new ChisqMixture();
    d.fit(a, 1);
    System.out.println("The estimated mixing distribution is\n" + d);
    
    DoubleVector pred = d.pace2(a.rev()).rev();
    System.out.println("\nThe PACE2 Estimate = \n" + pred);
    System.out.println("Quadratic loss = " + pred.sqrt().times(aNormal.sign()).sum2(means));
    

    pred = d.pace4(a);
    System.out.println("\nThe PACE4 Estimate = \n" + pred);
    System.out.println("Quadratic loss = " + pred.sqrt().times(aNormal.sign()).sum2(means));
    

    pred = d.pace6(a);
    System.out.println("\nThe PACE6 Estimate = \n" + pred);
    System.out.println("Quadratic loss = " + pred.sqrt().times(aNormal.sign()).sum2(means));
  }
}
