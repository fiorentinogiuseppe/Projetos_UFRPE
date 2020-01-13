package weka.classifiers.functions.pace;

import java.io.PrintStream;
import java.util.Random;
import weka.core.RevisionUtils;
import weka.core.matrix.DoubleVector;
import weka.core.matrix.Maths;




























































public class NormalMixture
  extends MixtureDistribution
{
  protected double separatingThreshold = 0.05D;
  

  protected double trimingThreshold = 0.7D;
  
  protected double fittingIntervalLength = 3.0D;
  




  public NormalMixture() {}
  




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
    double p = 0.0D;
    for (int i = i0; i <= i1; i++) {
      p += Maths.pnorm(-Math.abs(x - data.get(i)));
    }
    if (p < separatingThreshold) return true;
    return false;
  }
  







  public DoubleVector supportPoints(DoubleVector data, int ne)
  {
    if (data.size() < 2) {
      throw new IllegalArgumentException("data size < 2");
    }
    return data.copy();
  }
  





  public PaceMatrix fittingIntervals(DoubleVector data)
  {
    DoubleVector left = data.cat(data.minus(fittingIntervalLength));
    DoubleVector right = data.plus(fittingIntervalLength).cat(data);
    
    PaceMatrix a = new PaceMatrix(left.size(), 2);
    
    a.setMatrix(0, left.size() - 1, 0, left);
    a.setMatrix(0, right.size() - 1, 1, right);
    
    return a;
  }
  









  public PaceMatrix probabilityMatrix(DoubleVector s, PaceMatrix intervals)
  {
    int ns = s.size();
    int nr = intervals.getRowDimension();
    PaceMatrix p = new PaceMatrix(nr, ns);
    
    for (int i = 0; i < nr; i++) {
      for (int j = 0; j < ns; j++) {
        p.set(i, j, Maths.pnorm(intervals.get(i, 1), s.get(j), 1.0D) - Maths.pnorm(intervals.get(i, 0), s.get(j), 1.0D));
      }
    }
    


    return p;
  }
  





  public double empiricalBayesEstimate(double x)
  {
    if (Math.abs(x) > 10.0D) return x;
    DoubleVector d = Maths.dnormLog(x, mixingDistribution.getPointValues(), 1.0D);
    

    d.minusEquals(d.max());
    d = d.map("java.lang.Math", "exp");
    d.timesEquals(mixingDistribution.getFunctionValues());
    return mixingDistribution.getPointValues().innerProduct(d) / d.sum();
  }
  





  public DoubleVector empiricalBayesEstimate(DoubleVector x)
  {
    DoubleVector pred = new DoubleVector(x.size());
    for (int i = 0; i < x.size(); i++)
      pred.set(i, empiricalBayesEstimate(x.get(i)));
    trim(pred);
    return pred;
  }
  






  public DoubleVector nestedEstimate(DoubleVector x)
  {
    DoubleVector chf = new DoubleVector(x.size());
    for (int i = 0; i < x.size(); i++) chf.set(i, hf(x.get(i)));
    chf.cumulateInPlace();
    int index = chf.indexOfMax();
    DoubleVector copy = x.copy();
    if (index < x.size() - 1) copy.set(index + 1, x.size() - 1, 0.0D);
    trim(copy);
    return copy;
  }
  






  public DoubleVector subsetEstimate(DoubleVector x)
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
      if (Math.abs(x.get(i)) <= trimingThreshold) { x.set(i, 0.0D);
      }
    }
  }
  





  public double hf(double x)
  {
    DoubleVector points = mixingDistribution.getPointValues();
    DoubleVector values = mixingDistribution.getFunctionValues();
    
    DoubleVector d = Maths.dnormLog(x, points, 1.0D);
    d.minusEquals(d.max());
    
    d = d.map("java.lang.Math", "exp");
    d.timesEquals(values);
    
    return points.times(2.0D * x).minusEquals(x * x).innerProduct(d) / d.sum();
  }
  






  public double h(double x)
  {
    DoubleVector points = mixingDistribution.getPointValues();
    DoubleVector values = mixingDistribution.getFunctionValues();
    DoubleVector d = Maths.dnorm(x, points, 1.0D).timesEquals(values);
    return points.times(2.0D * x).minusEquals(x * x).innerProduct(d);
  }
  






  public DoubleVector h(DoubleVector x)
  {
    DoubleVector h = new DoubleVector(x.size());
    for (int i = 0; i < x.size(); i++)
      h.set(i, h(x.get(i)));
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
    double mu1 = 0.0D;
    double mu2 = 5.0D;
    DoubleVector a = Maths.rnorm(n1, mu1, 1.0D, new Random());
    a = a.cat(Maths.rnorm(n2, mu2, 1.0D, new Random()));
    DoubleVector means = new DoubleVector(n1, mu1).cat(new DoubleVector(n2, mu2));
    
    System.out.println("==========================================================");
    System.out.println("This is to test the estimation of the mixing\ndistribution of the mixture of unit variance normal\ndistributions. The example mixture used is of the form: \n\n   0.5 * N(mu1, 1) + 0.5 * N(mu2, 1)\n");
    



    System.out.println("It also tests three estimators: the subset\nselector, the nested model selector, and the empirical Bayes\nestimator. Quadratic losses of the estimators are given, \nand are taken as the measure of their performance.");
    


    System.out.println("==========================================================");
    System.out.println("mu1 = " + mu1 + " mu2 = " + mu2 + "\n");
    
    System.out.println(a.size() + " observations are: \n\n" + a);
    
    System.out.println("\nQuadratic loss of the raw data (i.e., the MLE) = " + a.sum2(means));
    
    System.out.println("==========================================================");
    

    NormalMixture d = new NormalMixture();
    d.fit(a, 1);
    System.out.println("The estimated mixing distribution is:\n" + d);
    
    DoubleVector pred = d.nestedEstimate(a.rev()).rev();
    System.out.println("\nThe Nested Estimate = \n" + pred);
    System.out.println("Quadratic loss = " + pred.sum2(means));
    
    pred = d.subsetEstimate(a);
    System.out.println("\nThe Subset Estimate = \n" + pred);
    System.out.println("Quadratic loss = " + pred.sum2(means));
    
    pred = d.empiricalBayesEstimate(a);
    System.out.println("\nThe Empirical Bayes Estimate = \n" + pred);
    System.out.println("Quadratic loss = " + pred.sum2(means));
  }
}
