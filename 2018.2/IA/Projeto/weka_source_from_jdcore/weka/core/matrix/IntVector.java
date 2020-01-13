package weka.core.matrix;

import java.io.PrintStream;
import java.util.Arrays;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;





































public class IntVector
  implements Cloneable, RevisionHandler
{
  int[] V;
  private int sizeOfVector;
  
  public IntVector()
  {
    V = new int[0];
    setSize(0);
  }
  


  public IntVector(int n)
  {
    V = new int[n];
    setSize(n);
  }
  


  public IntVector(int n, int s)
  {
    this(n);
    set(s);
  }
  


  public IntVector(int[] v)
  {
    if (v == null) {
      V = new int[0];
      setSize(0);
    }
    else {
      V = new int[v.length];
      setSize(v.length);
      set(0, size() - 1, v, 0);
    }
  }
  





  public int size()
  {
    return sizeOfVector;
  }
  




  public void setSize(int size)
  {
    if (size > capacity())
      throw new IllegalArgumentException("insufficient capacity");
    sizeOfVector = size;
  }
  

  public void set(int s)
  {
    for (int i = 0; i < size(); i++) {
      set(i, s);
    }
  }
  



  public void set(int i0, int i1, int[] v, int j0)
  {
    for (int i = i0; i <= i1; i++) {
      set(i, v[(j0 + i - i0)]);
    }
  }
  



  public void set(int i0, int i1, IntVector v, int j0)
  {
    for (int i = i0; i <= i1; i++) {
      set(i, v.get(j0 + i - i0));
    }
  }
  

  public void set(IntVector v)
  {
    set(0, v.size() - 1, v, 0);
  }
  




  public static IntVector seq(int i0, int i1)
  {
    if (i1 < i0) throw new IllegalArgumentException("i1 < i0 ");
    IntVector v = new IntVector(i1 - i0 + 1);
    for (int i = 0; i < i1 - i0 + 1; i++) {
      v.set(i, i + i0);
    }
    return v;
  }
  

  public int[] getArray()
  {
    return V;
  }
  

  protected void setArray(int[] a)
  {
    V = a;
  }
  

  public void sort()
  {
    Arrays.sort(V, 0, size());
  }
  

  public int[] getArrayCopy()
  {
    int[] b = new int[size()];
    for (int i = 0; i <= size() - 1; i++) {
      b[i] = V[i];
    }
    return b;
  }
  

  public int capacity()
  {
    return V.length;
  }
  


  public void setCapacity(int capacity)
  {
    if (capacity == capacity()) return;
    int[] old_V = V;
    int m = Math.min(capacity, size());
    V = new int[capacity];
    setSize(capacity);
    set(0, m - 1, old_V, 0);
  }
  



  public void set(int i, int s)
  {
    V[i] = s;
  }
  



  public int get(int i)
  {
    return V[i];
  }
  

  public IntVector copy()
  {
    return (IntVector)clone();
  }
  

  public Object clone()
  {
    IntVector u = new IntVector(size());
    for (int i = 0; i < size(); i++)
      V[i] = V[i];
    return u;
  }
  





  public IntVector subvector(int i0, int i1)
  {
    IntVector v = new IntVector(i1 - i0 + 1);
    v.set(0, i1 - i0, this, i0);
    return v;
  }
  



  public IntVector subvector(IntVector index)
  {
    IntVector v = new IntVector(index.size());
    for (int i = 0; i < index.size(); i++)
      V[i] = V[V[i]];
    return v;
  }
  




  public void swap(int i, int j)
  {
    if (i == j) return;
    int t = get(i);
    set(i, get(j));
    set(j, t);
  }
  




  public void shift(int i, int j)
  {
    if (i == j) return;
    if (i < j) {
      int t = V[i];
      for (int k = i; k <= j - 1; k++)
        V[k] = V[(k + 1)];
      V[j] = t;
    } else {
      shift(j, i);
    }
  }
  



  public void shiftToEnd(int j)
  {
    shift(j, size() - 1);
  }
  


  public boolean isEmpty()
  {
    if (size() == 0) return true;
    return false;
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
    for (int i = 0; i < size(); i++) nf.update(get(i));
    int count = 0;
    int width = 80;
    
    for (int i = 0; i < size(); i++) {
      String number = nf.format(get(i));
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
    IntVector u = new IntVector();
    System.out.println(u);
    
    IntVector v = seq(10, 25);
    System.out.println(v);
    
    IntVector w = seq(25, 10);
    System.out.println(w);
  }
}
