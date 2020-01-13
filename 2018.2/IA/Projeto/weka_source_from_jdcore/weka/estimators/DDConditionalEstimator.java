package weka.estimators;

import java.io.PrintStream;
import weka.core.RevisionUtils;






































public class DDConditionalEstimator
  implements ConditionalEstimator
{
  private DiscreteEstimator[] m_Estimators;
  
  public DDConditionalEstimator(int numSymbols, int numCondSymbols, boolean laplace)
  {
    m_Estimators = new DiscreteEstimator[numCondSymbols];
    for (int i = 0; i < numCondSymbols; i++) {
      m_Estimators[i] = new DiscreteEstimator(numSymbols, laplace);
    }
  }
  







  public void addValue(double data, double given, double weight)
  {
    m_Estimators[((int)given)].addValue(data, weight);
  }
  






  public Estimator getEstimator(double given)
  {
    return m_Estimators[((int)given)];
  }
  







  public double getProbability(double data, double given)
  {
    return getEstimator(given).getProbability(data);
  }
  

  public String toString()
  {
    String result = "DD Conditional Estimator. " + m_Estimators.length + " sub-estimators:\n";
    
    for (int i = 0; i < m_Estimators.length; i++) {
      result = result + "Sub-estimator " + i + ": " + m_Estimators[i];
    }
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
  





  public static void main(String[] argv)
  {
    try
    {
      if (argv.length == 0) {
        System.out.println("Please specify a set of instances.");
        return;
      }
      int currentA = Integer.parseInt(argv[0]);
      int maxA = currentA;
      int currentB = Integer.parseInt(argv[1]);
      int maxB = currentB;
      for (int i = 2; i < argv.length - 1; i += 2) {
        currentA = Integer.parseInt(argv[i]);
        currentB = Integer.parseInt(argv[(i + 1)]);
        if (currentA > maxA) {
          maxA = currentA;
        }
        if (currentB > maxB) {
          maxB = currentB;
        }
      }
      DDConditionalEstimator newEst = new DDConditionalEstimator(maxA + 1, maxB + 1, true);
      

      for (int i = 0; i < argv.length - 1; i += 2) {
        currentA = Integer.parseInt(argv[i]);
        currentB = Integer.parseInt(argv[(i + 1)]);
        System.out.println(newEst);
        System.out.println("Prediction for " + currentA + '|' + currentB + " = " + newEst.getProbability(currentA, currentB));
        

        newEst.addValue(currentA, currentB, 1.0D);
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
