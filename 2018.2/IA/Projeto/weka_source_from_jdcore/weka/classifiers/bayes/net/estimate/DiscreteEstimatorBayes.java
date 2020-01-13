package weka.classifiers.bayes.net.estimate;

import java.io.PrintStream;
import weka.classifiers.bayes.net.search.local.Scoreable;
import weka.core.RevisionUtils;
import weka.core.Statistics;
import weka.core.Utils;
import weka.estimators.DiscreteEstimator;
import weka.estimators.Estimator;








































public class DiscreteEstimatorBayes
  extends Estimator
  implements Scoreable
{
  static final long serialVersionUID = 4215400230843212684L;
  protected double[] m_Counts;
  protected double m_SumOfCounts;
  protected int m_nSymbols = 0;
  



  protected double m_fPrior = 0.0D;
  





  public DiscreteEstimatorBayes(int nSymbols, double fPrior)
  {
    m_fPrior = fPrior;
    m_nSymbols = nSymbols;
    m_Counts = new double[m_nSymbols];
    
    for (int iSymbol = 0; iSymbol < m_nSymbols; iSymbol++) {
      m_Counts[iSymbol] = m_fPrior;
    }
    
    m_SumOfCounts = (m_fPrior * m_nSymbols);
  }
  





  public void addValue(double data, double weight)
  {
    m_Counts[((int)data)] += weight;
    m_SumOfCounts += weight;
  }
  





  public double getProbability(double data)
  {
    if (m_SumOfCounts == 0.0D)
    {

      return 0.0D;
    }
    
    return m_Counts[((int)data)] / m_SumOfCounts;
  }
  





  public double getCount(double data)
  {
    if (m_SumOfCounts == 0.0D)
    {
      return 0.0D;
    }
    
    return m_Counts[((int)data)];
  }
  




  public int getNumSymbols()
  {
    return m_Counts == null ? 0 : m_Counts.length;
  }
  




  public double logScore(int nType, int nCardinality)
  {
    double fScore = 0.0D;
    
    switch (nType)
    {
    case 0: 
      for (int iSymbol = 0; iSymbol < m_nSymbols; iSymbol++) {
        fScore += Statistics.lnGamma(m_Counts[iSymbol]);
      }
      
      fScore -= Statistics.lnGamma(m_SumOfCounts);
      if (m_fPrior != 0.0D) {
        fScore -= m_nSymbols * Statistics.lnGamma(m_fPrior);
        fScore += Statistics.lnGamma(m_nSymbols * m_fPrior);
      }
      

      break;
    case 1: 
      for (int iSymbol = 0; iSymbol < m_nSymbols; iSymbol++) {
        fScore += Statistics.lnGamma(m_Counts[iSymbol]);
      }
      
      fScore -= Statistics.lnGamma(m_SumOfCounts);
      

      fScore -= m_nSymbols * Statistics.lnGamma(1.0D / (m_nSymbols * nCardinality));
      fScore += Statistics.lnGamma(1.0D / nCardinality);
      
      break;
    


    case 2: 
    case 3: 
    case 4: 
      for (int iSymbol = 0; iSymbol < m_nSymbols; iSymbol++) {
        double fP = getProbability(iSymbol);
        
        fScore += m_Counts[iSymbol] * Math.log(fP);
      }
      

      break;
    }
    
    

    return fScore;
  }
  




  public String toString()
  {
    String result = "Discrete Estimator. Counts = ";
    
    if (m_SumOfCounts > 1.0D) {
      for (int i = 0; i < m_Counts.length; i++) {
        result = result + " " + Utils.doubleToString(m_Counts[i], 2);
      }
      
      result = result + "  (Total = " + Utils.doubleToString(m_SumOfCounts, 2) + ")\n";
    }
    else {
      for (int i = 0; i < m_Counts.length; i++) {
        result = result + " " + m_Counts[i];
      }
      
      result = result + "  (Total = " + m_SumOfCounts + ")\n";
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
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
