package weka.attributeSelection;

import java.util.BitSet;

public abstract interface SubsetEvaluator
{
  public abstract double evaluateSubset(BitSet paramBitSet)
    throws Exception;
}
