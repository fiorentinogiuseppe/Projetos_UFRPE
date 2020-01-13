package weka.attributeSelection;

import java.io.Serializable;
import java.util.BitSet;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;








































































public class CostSensitiveSubsetEval
  extends CostSensitiveASEvaluation
  implements Serializable, SubsetEvaluator, OptionHandler
{
  static final long serialVersionUID = 2924546096103426700L;
  
  public CostSensitiveSubsetEval()
  {
    setEvaluator(new CfsSubsetEval());
  }
  




  public void setEvaluator(ASEvaluation newEvaluator)
    throws IllegalArgumentException
  {
    if (!(newEvaluator instanceof SubsetEvaluator)) {
      throw new IllegalArgumentException("Evaluator must be an SubsetEvaluator!");
    }
    
    m_evaluator = newEvaluator;
  }
  







  public double evaluateSubset(BitSet subset)
    throws Exception
  {
    return ((SubsetEvaluator)m_evaluator).evaluateSubset(subset);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5562 $");
  }
  




  public static void main(String[] args)
  {
    runEvaluator(new CostSensitiveSubsetEval(), args);
  }
}
