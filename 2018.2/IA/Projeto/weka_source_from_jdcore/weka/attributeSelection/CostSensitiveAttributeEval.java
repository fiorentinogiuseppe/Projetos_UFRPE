package weka.attributeSelection;

import java.io.Serializable;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;






























































































public class CostSensitiveAttributeEval
  extends CostSensitiveASEvaluation
  implements Serializable, AttributeEvaluator, OptionHandler
{
  static final long serialVersionUID = 4484876541145458447L;
  
  public CostSensitiveAttributeEval()
  {
    setEvaluator(new ReliefFAttributeEval());
  }
  




  public String defaultEvaluatorString()
  {
    return "weka.attributeSelection.ReliefFAttributeEval";
  }
  




  public void setEvaluator(ASEvaluation newEvaluator)
    throws IllegalArgumentException
  {
    if (!(newEvaluator instanceof AttributeEvaluator)) {
      throw new IllegalArgumentException("Evaluator must be an AttributeEvaluator!");
    }
    
    m_evaluator = newEvaluator;
  }
  






  public double evaluateAttribute(int attribute)
    throws Exception
  {
    return ((AttributeEvaluator)m_evaluator).evaluateAttribute(attribute);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5562 $");
  }
  




  public static void main(String[] args)
  {
    runEvaluator(new CostSensitiveAttributeEval(), args);
  }
}
