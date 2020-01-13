package weka.estimators;

import java.io.PrintStream;
import weka.core.RevisionUtils;







































public class DNConditionalEstimator
  implements ConditionalEstimator
{
  private NormalEstimator[] m_Estimators;
  private DiscreteEstimator m_Weights;
  
  public DNConditionalEstimator(int numSymbols, double precision)
  {
    m_Estimators = new NormalEstimator[numSymbols];
    for (int i = 0; i < numSymbols; i++) {
      m_Estimators[i] = new NormalEstimator(precision);
    }
    m_Weights = new DiscreteEstimator(numSymbols, true);
  }
  







  public void addValue(double data, double given, double weight)
  {
    m_Estimators[((int)data)].addValue(given, weight);
    m_Weights.addValue((int)data, weight);
  }
  






  public Estimator getEstimator(double given)
  {
    Estimator result = new DiscreteEstimator(m_Estimators.length, false);
    for (int i = 0; i < m_Estimators.length; i++) {
      result.addValue(i, m_Weights.getProbability(i) * m_Estimators[i].getProbability(given));
    }
    
    return result;
  }
  







  public double getProbability(double data, double given)
  {
    return getEstimator(given).getProbability(data);
  }
  

  public String toString()
  {
    String result = "DN Conditional Estimator. " + m_Estimators.length + " sub-estimators:\n";
    
    for (int i = 0; i < m_Estimators.length; i++) {
      result = result + "Sub-estimator " + i + ": " + m_Estimators[i];
    }
    result = result + "Weights of each estimator given by " + m_Weights;
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
      DNConditionalEstimator newEst = new DNConditionalEstimator(maxA + 1, 1.0D);
      
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
