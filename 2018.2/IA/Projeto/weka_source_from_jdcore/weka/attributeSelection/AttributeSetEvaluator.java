package weka.attributeSelection;

public abstract class AttributeSetEvaluator
  extends ASEvaluation
{
  private static final long serialVersionUID = -5744881009422257389L;
  
  public AttributeSetEvaluator() {}
  
  public abstract double evaluateAttribute(int paramInt)
    throws Exception;
  
  public abstract double evaluateAttribute(int[] paramArrayOfInt1, int[] paramArrayOfInt2)
    throws Exception;
}
