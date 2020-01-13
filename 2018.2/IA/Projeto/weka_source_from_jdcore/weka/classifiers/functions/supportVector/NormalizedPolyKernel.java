package weka.classifiers.functions.supportVector;

import java.io.PrintStream;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;































































public class NormalizedPolyKernel
  extends PolyKernel
{
  static final long serialVersionUID = 1248574185532130851L;
  
  public NormalizedPolyKernel()
  {
    setExponent(2.0D);
  }
  









  public NormalizedPolyKernel(Instances dataset, int cacheSize, double exponent, boolean lowerOrder)
    throws Exception
  {
    super(dataset, cacheSize, exponent, lowerOrder);
  }
  





  public String globalInfo()
  {
    return "The normalized polynomial kernel.\nK(x,y) = <x,y>/sqrt(<x,x><y,y>) where <x,y> = PolyKernel(x,y)";
  }
  













  public double eval(int id1, int id2, Instance inst1)
    throws Exception
  {
    double div = Math.sqrt(super.eval(id1, id1, inst1) * (m_keys != null ? super.eval(id2, id2, m_data.instance(id2)) : super.eval(-1, -1, m_data.instance(id2))));
    


    if (div != 0.0D) {
      return super.eval(id1, id2, inst1) / div;
    }
    return 0.0D;
  }
  





  public void setExponent(double value)
  {
    if (value != 1.0D) {
      super.setExponent(value);
    } else {
      System.out.println("A linear kernel, i.e., Exponent=1, is not possible!");
    }
  }
  

  public String toString()
  {
    String result;
    
    String result;
    
    if (getUseLowerOrder()) {
      result = "Normalized Poly Kernel with lower order: K(x,y) = (<x,y>+1)^" + getExponent() + "/" + "((<x,x>+1)^" + getExponent() + "*" + "(<y,y>+1)^" + getExponent() + ")^(1/2)";
    }
    else {
      result = "Normalized Poly Kernel: K(x,y) = <x,y>^" + getExponent() + "/" + "(<x,x>^" + getExponent() + "*" + "<y,y>^" + getExponent() + ")^(1/2)";
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
}
