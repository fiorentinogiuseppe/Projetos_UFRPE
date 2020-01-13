package weka.core.matrix;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;


































public class DoubleVector
  implements Cloneable, RevisionHandler
{
  double[] V;
  private int sizeOfVector;
  
  public DoubleVector()
  {
    this(0);
  }
  


  public DoubleVector(int n)
  {
    V = new double[n];
    setSize(n);
  }
  



  public DoubleVector(int n, double s)
  {
    this(n);
    set(s);
  }
  


  public DoubleVector(double[] v)
  {
    if (v == null) {
      V = new double[0];
      setSize(0);
    }
    else {
      V = v;
      setSize(v.length);
    }
  }
  








  public void set(int i, double s)
  {
    V[i] = s;
  }
  


  public void set(double s)
  {
    set(0, size() - 1, s);
  }
  





  public void set(int i0, int i1, double s)
  {
    for (int i = i0; i <= i1; i++) {
      V[i] = s;
    }
  }
  




  public void set(int i0, int i1, double[] v, int j0)
  {
    for (int i = i0; i <= i1; i++) {
      V[i] = v[(j0 + i - i0)];
    }
  }
  

  public void set(DoubleVector v)
  {
    set(0, v.size() - 1, v, 0);
  }
  





  public void set(int i0, int i1, DoubleVector v, int j0)
  {
    for (int i = i0; i <= i1; i++) {
      V[i] = V[(j0 + i - i0)];
    }
  }
  

  public double[] getArray()
  {
    return V;
  }
  
  void setArray(double[] a) {
    V = a;
  }
  

  public double[] getArrayCopy()
  {
    double[] v = new double[size()];
    
    for (int i = 0; i < size(); i++) {
      v[i] = V[i];
    }
    return v;
  }
  
  public void sort()
  {
    Arrays.sort(V, 0, size());
  }
  
  public IntVector sortWithIndex()
  {
    IntVector index = IntVector.seq(0, size() - 1);
    sortWithIndex(0, size() - 1, index);
    return index;
  }
  




  public void sortWithIndex(int xi, int xj, IntVector index)
  {
    if (xi < xj)
    {
      int xm = (xi + xj) / 2;
      double x = Math.min(V[xi], Math.max(V[xm], V[xj]));
      
      int i = xi;
      int j = xj;
      while (i < j) {
        while ((V[i] < x) && (i < xj)) i++;
        while ((V[j] > x) && (j > xi)) j--;
        if (i <= j) {
          swap(i, j);
          index.swap(i, j);
          i++;
          j--;
        }
      }
      sortWithIndex(xi, j, index);
      sortWithIndex(i, xj, index);
    }
  }
  


  public int size()
  {
    return sizeOfVector;
  }
  



  public void setSize(int m)
  {
    if (m > capacity())
      throw new IllegalArgumentException("insufficient capacity");
    sizeOfVector = m;
  }
  


  public int capacity()
  {
    if (V == null) return 0;
    return V.length;
  }
  


  public void setCapacity(int n)
  {
    if (n == capacity()) return;
    double[] oldV = V;
    int m = Math.min(n, size());
    V = new double[n];
    setSize(m);
    set(0, m - 1, oldV, 0);
  }
  



  public double get(int i)
  {
    return V[i];
  }
  




  public void setPlus(int i, double s)
  {
    V[i] += s;
  }
  




  public void setTimes(int i, double s)
  {
    V[i] *= s;
  }
  



  public void addElement(double x)
  {
    if (capacity() == 0) setCapacity(10);
    if (size() == capacity()) setCapacity(2 * capacity());
    V[size()] = x;
    setSize(size() + 1);
  }
  


  public DoubleVector square()
  {
    DoubleVector v = new DoubleVector(size());
    for (int i = 0; i < size(); i++) V[i] *= V[i];
    return v;
  }
  


  public DoubleVector sqrt()
  {
    DoubleVector v = new DoubleVector(size());
    for (int i = 0; i < size(); i++) V[i] = Math.sqrt(V[i]);
    return v;
  }
  

  public DoubleVector copy()
  {
    return (DoubleVector)clone();
  }
  

  public Object clone()
  {
    int n = size();
    DoubleVector u = new DoubleVector(n);
    for (int i = 0; i < n; i++)
      V[i] = V[i];
    return u;
  }
  




  public double innerProduct(DoubleVector v)
  {
    if (size() != v.size())
      throw new IllegalArgumentException("sizes unmatch");
    double p = 0.0D;
    for (int i = 0; i < size(); i++) {
      p += V[i] * V[i];
    }
    return p;
  }
  



  public DoubleVector sign()
  {
    DoubleVector s = new DoubleVector(size());
    for (int i = 0; i < size(); i++) {
      if (V[i] > 0.0D) { V[i] = 1.0D;
      } else if (V[i] < 0.0D) V[i] = -1.0D; else
        V[i] = 0.0D;
    }
    return s;
  }
  


  public double sum()
  {
    double s = 0.0D;
    for (int i = 0; i < size(); i++) s += V[i];
    return s;
  }
  


  public double sum2()
  {
    double s2 = 0.0D;
    for (int i = 0; i < size(); i++) s2 += V[i] * V[i];
    return s2;
  }
  


  public double norm1()
  {
    double s = 0.0D;
    for (int i = 0; i < size(); i++) s += Math.abs(V[i]);
    return s;
  }
  


  public double norm2()
  {
    return Math.sqrt(sum2());
  }
  



  public double sum2(DoubleVector v)
  {
    return minus(v).sum2();
  }
  





  public DoubleVector subvector(int i0, int i1)
  {
    DoubleVector v = new DoubleVector(i1 - i0 + 1);
    v.set(0, i1 - i0, this, i0);
    return v;
  }
  



  public DoubleVector subvector(IntVector index)
  {
    DoubleVector v = new DoubleVector(index.size());
    for (int i = 0; i < index.size(); i++)
      V[i] = V[V[i]];
    return v;
  }
  




  public DoubleVector unpivoting(IntVector index, int length)
  {
    if (index.size() > length)
      throw new IllegalArgumentException("index.size() > length ");
    DoubleVector u = new DoubleVector(length);
    for (int i = 0; i < index.size(); i++) {
      V[V[i]] = V[i];
    }
    return u;
  }
  


  public DoubleVector plus(double x)
  {
    return copy().plusEquals(x);
  }
  


  public DoubleVector plusEquals(double x)
  {
    for (int i = 0; i < size(); i++)
      V[i] += x;
    return this;
  }
  



  public DoubleVector plus(DoubleVector v)
  {
    return copy().plusEquals(v);
  }
  



  public DoubleVector plusEquals(DoubleVector v)
  {
    for (int i = 0; i < size(); i++)
      V[i] += V[i];
    return this;
  }
  



  public DoubleVector minus(double x)
  {
    return plus(-x);
  }
  



  public DoubleVector minusEquals(double x)
  {
    plusEquals(-x);
    return this;
  }
  



  public DoubleVector minus(DoubleVector v)
  {
    return copy().minusEquals(v);
  }
  



  public DoubleVector minusEquals(DoubleVector v)
  {
    for (int i = 0; i < size(); i++)
      V[i] -= V[i];
    return this;
  }
  



  public DoubleVector times(double s)
  {
    return copy().timesEquals(s);
  }
  



  public DoubleVector timesEquals(double s)
  {
    for (int i = 0; i < size(); i++) {
      V[i] *= s;
    }
    return this;
  }
  



  public DoubleVector times(DoubleVector v)
  {
    return copy().timesEquals(v);
  }
  




  public DoubleVector timesEquals(DoubleVector v)
  {
    for (int i = 0; i < size(); i++)
      V[i] *= V[i];
    return this;
  }
  



  public DoubleVector dividedBy(DoubleVector v)
  {
    return copy().dividedByEquals(v);
  }
  



  public DoubleVector dividedByEquals(DoubleVector v)
  {
    for (int i = 0; i < size(); i++) {
      V[i] /= V[i];
    }
    return this;
  }
  


  public boolean isEmpty()
  {
    if (size() == 0) return true;
    return false;
  }
  



  public DoubleVector cumulate()
  {
    return copy().cumulateInPlace();
  }
  



  public DoubleVector cumulateInPlace()
  {
    for (int i = 1; i < size(); i++) {
      V[i] += V[(i - 1)];
    }
    return this;
  }
  




  public int indexOfMax()
  {
    int index = 0;
    double ma = V[0];
    
    for (int i = 1; i < size(); i++) {
      if (ma < V[i]) {
        ma = V[i];
        index = i;
      }
    }
    return index;
  }
  



  public boolean unsorted()
  {
    if (size() < 2) return false;
    for (int i = 1; i < size(); i++) {
      if (V[(i - 1)] > V[i])
        return true;
    }
    return false;
  }
  



  public DoubleVector cat(DoubleVector v)
  {
    DoubleVector w = new DoubleVector(size() + v.size());
    w.set(0, size() - 1, this, 0);
    w.set(size(), size() + v.size() - 1, v, 0);
    return w;
  }
  




  public void swap(int i, int j)
  {
    if (i == j) return;
    double t = V[i];
    V[i] = V[j];
    V[j] = t;
  }
  


  public double max()
  {
    if (size() < 1) throw new IllegalArgumentException("zero size");
    double ma = V[0];
    if (size() < 2) return ma;
    for (int i = 1; i < size(); i++) {
      if (V[i] > ma) ma = V[i];
    }
    return ma;
  }
  




  public DoubleVector map(String className, String method)
  {
    try
    {
      Class c = Class.forName(className);
      Class[] cs = new Class[1];
      cs[0] = Double.TYPE;
      Method m = c.getMethod(method, cs);
      
      DoubleVector w = new DoubleVector(size());
      Object[] obj = new Object[1];
      for (int i = 0; i < size(); i++) {
        obj[0] = new Double(V[i]);
        w.set(i, Double.parseDouble(m.invoke(null, obj).toString()));
      }
      return w;
    }
    catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return null;
  }
  


  public DoubleVector rev()
  {
    int n = size();
    DoubleVector w = new DoubleVector(n);
    for (int i = 0; i < n; i++)
      V[i] = V[(n - i - 1)];
    return w;
  }
  



  public static DoubleVector random(int n)
  {
    DoubleVector v = new DoubleVector(n);
    for (int i = 0; i < n; i++) {
      V[i] = Math.random();
    }
    return v;
  }
  

  public String toString()
  {
    return toString(5, false);
  }
  



  public String toString(int digits, boolean trailing)
  {
    if (isEmpty()) { return "null vector";
    }
    StringBuffer text = new StringBuffer();
    FlexibleDecimalFormat nf = new FlexibleDecimalFormat(digits, trailing);
    
    nf.grouping(true);
    for (int i = 0; i < size(); i++) nf.update(V[i]);
    int count = 0;
    int width = 80;
    
    for (int i = 0; i < size(); i++) {
      String number = nf.format(V[i]);
      count += 1 + number.length();
      if (count > width - 1) {
        text.append('\n');
        count = 1 + number.length();
      }
      text.append(" " + number);
    }
    
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
  

  public static void main(String[] args)
  {
    DoubleVector u = random(10);
    DoubleVector v = random(10);
    DoubleVector a = random(10);
    DoubleVector w = a;
    
    System.out.println(random(10).plus(v).plus(w));
  }
}
