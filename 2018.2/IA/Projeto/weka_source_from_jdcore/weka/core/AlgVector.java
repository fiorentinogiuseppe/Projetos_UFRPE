package weka.core;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Random;





































public class AlgVector
  implements Cloneable, Serializable, RevisionHandler
{
  private static final long serialVersionUID = -4023736016850256591L;
  protected double[] m_Elements;
  
  public AlgVector(int n)
  {
    m_Elements = new double[n];
    initialize();
  }
  





  public AlgVector(double[] array)
  {
    m_Elements = new double[array.length];
    for (int i = 0; i < array.length; i++) {
      m_Elements[i] = array[i];
    }
  }
  









  public AlgVector(Instances format, Random random)
    throws Exception
  {
    int len = format.numAttributes();
    for (int i = 0; i < format.numAttributes(); i++) {
      if (!format.attribute(i).isNumeric()) len--;
    }
    if (len > 0) {
      m_Elements = new double[len];
      initialize(random);
    }
  }
  








  public AlgVector(Instance instance)
    throws Exception
  {
    int len = instance.numAttributes();
    for (int i = 0; i < instance.numAttributes(); i++) {
      if (!instance.attribute(i).isNumeric())
        len--;
    }
    if (len > 0) {
      m_Elements = new double[len];
      int n = 0;
      for (int i = 0; i < instance.numAttributes(); i++) {
        if (instance.attribute(i).isNumeric())
        {
          m_Elements[n] = instance.value(i);
          n++;
        }
      }
    } else {
      throw new IllegalArgumentException("No numeric attributes in data!");
    }
  }
  





  public Object clone()
    throws CloneNotSupportedException
  {
    AlgVector v = (AlgVector)super.clone();
    m_Elements = new double[numElements()];
    for (int i = 0; i < numElements(); i++) {
      m_Elements[i] = m_Elements[i];
    }
    
    return v;
  }
  



  protected void initialize()
  {
    for (int i = 0; i < m_Elements.length; i++) {
      m_Elements[i] = 0.0D;
    }
  }
  





  protected void initialize(Random random)
  {
    for (int i = 0; i < m_Elements.length; i++) {
      m_Elements[i] = random.nextDouble();
    }
  }
  





  public final double getElement(int index)
  {
    return m_Elements[index];
  }
  






  public final int numElements()
  {
    return m_Elements.length;
  }
  







  public final void setElement(int index, double value)
  {
    m_Elements[index] = value;
  }
  






  public final void setElements(double[] elements)
  {
    for (int i = 0; i < elements.length; i++) {
      m_Elements[i] = elements[i];
    }
  }
  





  public double[] getElements()
  {
    double[] elements = new double[numElements()];
    for (int i = 0; i < elements.length; i++) {
      elements[i] = m_Elements[i];
    }
    return elements;
  }
  









  public Instance getAsInstance(Instances model, Random random)
    throws Exception
  {
    Instance newInst = null;
    
    if (m_Elements != null) {
      newInst = new Instance(model.numAttributes());
      newInst.setDataset(model);
      
      int i = 0; for (int j = 0; i < model.numAttributes(); i++) {
        if (model.attribute(i).isNumeric()) {
          if (j >= m_Elements.length)
            throw new Exception("Datatypes are not compatible.");
          newInst.setValue(i, m_Elements[(j++)]);
        }
        if (model.attribute(i).isNominal()) {
          int newVal = (int)(random.nextDouble() * model.attribute(i).numValues());
          
          if (newVal == model.attribute(i).numValues())
            newVal--;
          newInst.setValue(i, newVal);
        }
      }
    }
    return newInst;
  }
  






  public final AlgVector add(AlgVector other)
  {
    AlgVector b = null;
    
    if (m_Elements != null) {
      int n = m_Elements.length;
      try {
        b = (AlgVector)clone();
      } catch (CloneNotSupportedException ex) {
        b = new AlgVector(n);
      }
      
      for (int i = 0; i < n; i++) {
        m_Elements[i] += m_Elements[i];
      }
    }
    
    return b;
  }
  






  public final AlgVector substract(AlgVector other)
  {
    int n = m_Elements.length;
    AlgVector b;
    try {
      b = (AlgVector)clone();
    } catch (CloneNotSupportedException ex) {
      b = new AlgVector(n);
    }
    
    for (int i = 0; i < n; i++) {
      m_Elements[i] -= m_Elements[i];
    }
    
    return b;
  }
  






  public final double dotMultiply(AlgVector b)
  {
    double sum = 0.0D;
    
    if (m_Elements != null) {
      int n = m_Elements.length;
      
      for (int i = 0; i < n; i++) {
        sum += m_Elements[i] * m_Elements[i];
      }
    }
    
    return sum;
  }
  





  public final void scalarMultiply(double s)
  {
    if (m_Elements != null) {
      int n = m_Elements.length;
      
      for (int i = 0; i < n; i++) {
        m_Elements[i] = (s * m_Elements[i]);
      }
    }
  }
  





  public void changeLength(double len)
  {
    double factor = norm();
    factor = len / factor;
    scalarMultiply(factor);
  }
  





  public double norm()
  {
    if (m_Elements != null) {
      int n = m_Elements.length;
      double sum = 0.0D;
      
      for (int i = 0; i < n; i++) {
        sum += m_Elements[i] * m_Elements[i];
      }
      return Math.pow(sum, 0.5D);
    }
    return 0.0D;
  }
  



  public final void normVector()
  {
    double len = norm();
    scalarMultiply(1.0D / len);
  }
  





  public String toString()
  {
    StringBuffer text = new StringBuffer();
    for (int i = 0; i < m_Elements.length; i++) {
      if (i > 0) text.append(",");
      text.append(Utils.doubleToString(m_Elements[i], 6));
    }
    
    text.append("\n");
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.10 $");
  }
  





  public static void main(String[] args)
    throws Exception
  {
    double[] first = { 2.3D, 1.2D, 5.0D };
    try
    {
      AlgVector test = new AlgVector(first);
      System.out.println("test:\n " + test);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
