package weka.estimators;

import java.io.PrintStream;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.RevisionUtils;
import weka.core.Utils;






































public class DiscreteEstimator
  extends Estimator
  implements IncrementalEstimator
{
  private static final long serialVersionUID = -5526486742612434779L;
  private double[] m_Counts;
  private double m_SumOfCounts;
  
  public DiscreteEstimator(int numSymbols, boolean laplace)
  {
    m_Counts = new double[numSymbols];
    m_SumOfCounts = 0.0D;
    if (laplace) {
      for (int i = 0; i < numSymbols; i++) {
        m_Counts[i] = 1.0D;
      }
      m_SumOfCounts = numSymbols;
    }
  }
  






  public DiscreteEstimator(int nSymbols, double fPrior)
  {
    m_Counts = new double[nSymbols];
    for (int iSymbol = 0; iSymbol < nSymbols; iSymbol++) {
      m_Counts[iSymbol] = fPrior;
    }
    m_SumOfCounts = (fPrior * nSymbols);
  }
  






  public void addValue(double data, double weight)
  {
    m_Counts[((int)data)] += weight;
    m_SumOfCounts += weight;
  }
  






  public double getProbability(double data)
  {
    if (m_SumOfCounts == 0.0D) {
      return 0.0D;
    }
    return m_Counts[((int)data)] / m_SumOfCounts;
  }
  





  public int getNumSymbols()
  {
    return m_Counts == null ? 0 : m_Counts.length;
  }
  







  public double getCount(double data)
  {
    if (m_SumOfCounts == 0.0D) {
      return 0.0D;
    }
    return m_Counts[((int)data)];
  }
  






  public double getSumOfCounts()
  {
    return m_SumOfCounts;
  }
  




  public String toString()
  {
    StringBuffer result = new StringBuffer("Discrete Estimator. Counts = ");
    if (m_SumOfCounts > 1.0D) {
      for (int i = 0; i < m_Counts.length; i++) {
        result.append(" ").append(Utils.doubleToString(m_Counts[i], 2));
      }
      result.append("  (Total = ").append(Utils.doubleToString(m_SumOfCounts, 2));
      result.append(")\n");
    } else {
      for (int i = 0; i < m_Counts.length; i++) {
        result.append(" ").append(m_Counts[i]);
      }
      result.append("  (Total = ").append(m_SumOfCounts).append(")\n");
    }
    return result.toString();
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.10 $");
  }
  





  public static void main(String[] argv)
  {
    try
    {
      if (argv.length == 0) {
        System.out.println("Please specify a set of instances.");
        return;
      }
      int current = Integer.parseInt(argv[0]);
      int max = current;
      for (int i = 1; i < argv.length; i++) {
        current = Integer.parseInt(argv[i]);
        if (current > max) {
          max = current;
        }
      }
      DiscreteEstimator newEst = new DiscreteEstimator(max + 1, true);
      for (int i = 0; i < argv.length; i++) {
        current = Integer.parseInt(argv[i]);
        System.out.println(newEst);
        System.out.println("Prediction for " + current + " = " + newEst.getProbability(current));
        
        newEst.addValue(current, 1.0D);
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
