package weka.estimators;

import java.io.PrintStream;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.RevisionUtils;
import weka.core.Utils;












































public class PoissonEstimator
  extends Estimator
  implements IncrementalEstimator
{
  private static final long serialVersionUID = 7669362595289236662L;
  private double m_NumValues;
  private double m_SumOfValues;
  private double m_Lambda;
  
  public PoissonEstimator() {}
  
  private double logFac(double x)
  {
    double result = 0.0D;
    for (double i = 2.0D; i <= x; i += 1.0D) {
      result += Math.log(i);
    }
    return result;
  }
  






  private double Poisson(double x)
  {
    return Math.exp(-m_Lambda + x * Math.log(m_Lambda) - logFac(x));
  }
  






  public void addValue(double data, double weight)
  {
    m_NumValues += weight;
    m_SumOfValues += data * weight;
    if (m_NumValues != 0.0D) {
      m_Lambda = (m_SumOfValues / m_NumValues);
    }
  }
  






  public double getProbability(double data)
  {
    return Poisson(data);
  }
  

  public String toString()
  {
    return "Poisson Lambda = " + Utils.doubleToString(m_Lambda, 4, 2) + "\n";
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    
    if (!m_noClass) {
      result.enable(Capabilities.Capability.NOMINAL_CLASS);
      result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    } else {
      result.enable(Capabilities.Capability.NO_CLASS);
    }
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5540 $");
  }
  




  public static void main(String[] argv)
  {
    try
    {
      if (argv.length == 0) {
        System.out.println("Please specify a set of instances.");
        return;
      }
      PoissonEstimator newEst = new PoissonEstimator();
      for (int i = 0; i < argv.length; i++) {
        double current = Double.valueOf(argv[i]).doubleValue();
        System.out.println(newEst);
        System.out.println("Prediction for " + current + " = " + newEst.getProbability(current));
        
        newEst.addValue(current, 1.0D);
      }
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
