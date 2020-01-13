package weka.classifiers.functions.pace;

import java.io.PrintStream;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.matrix.DoubleVector;
import weka.core.matrix.FlexibleDecimalFormat;
import weka.core.matrix.IntVector;































public class DiscreteFunction
  implements RevisionHandler
{
  protected DoubleVector points;
  protected DoubleVector values;
  
  public DiscreteFunction()
  {
    this(null, null);
  }
  




  public DiscreteFunction(DoubleVector p)
  {
    this(p, null);
  }
  




  public DiscreteFunction(DoubleVector p, DoubleVector v)
  {
    points = p;
    values = v;
    formalize();
  }
  
  private DiscreteFunction formalize()
  {
    if (points == null) points = new DoubleVector();
    if (values == null) { values = new DoubleVector();
    }
    if (points.isEmpty()) {
      if (!values.isEmpty()) {
        throw new IllegalArgumentException("sizes not match");
      }
    } else {
      int n = points.size();
      if (values.isEmpty()) {
        values = new DoubleVector(n, 1.0D / n);

      }
      else if (values.size() != n) {
        throw new IllegalArgumentException("sizes not match");
      }
    }
    return this;
  }
  



  public DiscreteFunction normalize()
  {
    if (!values.isEmpty()) {
      double s = values.sum();
      if ((s != 0.0D) && (s != 1.0D)) values.timesEquals(1.0D / s);
    }
    return this;
  }
  



  public void sort()
  {
    IntVector index = points.sortWithIndex();
    values = values.subvector(index);
  }
  



  public Object clone()
  {
    DiscreteFunction d = new DiscreteFunction();
    points = ((DoubleVector)points.clone());
    values = ((DoubleVector)values.clone());
    return d;
  }
  



  public DiscreteFunction unique()
  {
    int count = 0;
    
    if (size() < 2) return this;
    for (int i = 1; i <= size() - 1; i++) {
      if (points.get(count) != points.get(i)) {
        count++;
        points.set(count, points.get(i));
        values.set(count, values.get(i));
      }
      else {
        values.set(count, values.get(count) + values.get(i));
      }
    }
    points = points.subvector(0, count);
    values = values.subvector(0, count);
    return this;
  }
  



  public int size()
  {
    if (points == null) return 0;
    return points.size();
  }
  




  public double getPointValue(int i)
  {
    return points.get(i);
  }
  




  public double getFunctionValue(int i)
  {
    return values.get(i);
  }
  




  public void setPointValue(int i, double p)
  {
    points.set(i, p);
  }
  




  public void setFunctionValue(int i, double v)
  {
    values.set(i, v);
  }
  



  protected DoubleVector getPointValues()
  {
    return points;
  }
  



  protected DoubleVector getFunctionValues()
  {
    return values;
  }
  



  public boolean isEmpty()
  {
    if (size() == 0) return true;
    return false;
  }
  










  public DiscreteFunction plus(DiscreteFunction d)
  {
    return ((DiscreteFunction)clone()).plusEquals(d);
  }
  





  public DiscreteFunction plusEquals(DiscreteFunction d)
  {
    points = points.cat(points);
    values = values.cat(values);
    return this;
  }
  




  public DiscreteFunction timesEquals(double x)
  {
    values.timesEquals(x);
    return this;
  }
  



  public String toString()
  {
    StringBuffer text = new StringBuffer();
    FlexibleDecimalFormat nf1 = new FlexibleDecimalFormat(5);
    nf1.grouping(true);
    FlexibleDecimalFormat nf2 = new FlexibleDecimalFormat(5);
    nf2.grouping(true);
    for (int i = 0; i < size(); i++) {
      nf1.update(points.get(i));
      nf2.update(values.get(i));
    }
    
    text.append("\t" + nf1.formatString("Points") + "\t" + nf2.formatString("Values") + "\n\n");
    
    for (int i = 0; i <= size() - 1; i++) {
      text.append("\t" + nf1.format(points.get(i)) + "\t" + nf2.format(values.get(i)) + "\n");
    }
    

    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  

  public static void main(String[] args)
  {
    double[] points = { 2.0D, 1.0D, 2.0D, 3.0D, 3.0D };
    double[] values = { 3.0D, 2.0D, 4.0D, 1.0D, 3.0D };
    DiscreteFunction d = new DiscreteFunction(new DoubleVector(points), new DoubleVector(values));
    
    System.out.println(d);
    d.normalize();
    System.out.println("d (after normalize) = \n" + d);
    points[1] = 10.0D;
    System.out.println("d (after setting [1]) = \n" + d);
    d.sort();
    System.out.println("d (after sorting) = \n" + d);
    d.unique();
    System.out.println("d (after unique) = \n" + d);
  }
}
