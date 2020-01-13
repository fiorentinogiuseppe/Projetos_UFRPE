package weka.classifiers.bayes.net.estimate;

import java.io.PrintStream;
import weka.core.RevisionUtils;
import weka.estimators.DiscreteEstimator;










































public class DiscreteEstimatorFullBayes
  extends DiscreteEstimatorBayes
{
  static final long serialVersionUID = 6774941981423312133L;
  
  public DiscreteEstimatorFullBayes(int nSymbols, double w1, double w2, DiscreteEstimatorBayes EmptyDist, DiscreteEstimatorBayes ClassDist, double fPrior)
  {
    super(nSymbols, fPrior);
    
    m_SumOfCounts = 0.0D;
    for (int iSymbol = 0; iSymbol < m_nSymbols; iSymbol++) {
      double p1 = EmptyDist.getProbability(iSymbol);
      double p2 = ClassDist.getProbability(iSymbol);
      m_Counts[iSymbol] = (w1 * p1 + w2 * p2);
      m_SumOfCounts += m_Counts[iSymbol];
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.3 $");
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
