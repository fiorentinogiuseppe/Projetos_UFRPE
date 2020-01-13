package weka.attributeSelection;

import java.util.BitSet;
import weka.core.Instance;
import weka.core.Instances;

public abstract class HoldOutSubsetEvaluator
  extends ASEvaluation
  implements SubsetEvaluator
{
  private static final long serialVersionUID = 8280529785412054174L;
  
  public HoldOutSubsetEvaluator() {}
  
  public abstract double evaluateSubset(BitSet paramBitSet, Instances paramInstances)
    throws Exception;
  
  public abstract double evaluateSubset(BitSet paramBitSet, Instance paramInstance, boolean paramBoolean)
    throws Exception;
}
