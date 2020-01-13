package weka.gui.boundaryvisualizer;

import weka.core.Instances;

public abstract interface DataGenerator
{
  public abstract void buildGenerator(Instances paramInstances)
    throws Exception;
  
  public abstract double[][] generateInstances(int[] paramArrayOfInt)
    throws Exception;
  
  public abstract double[] getWeights()
    throws Exception;
  
  public abstract void setWeightingDimensions(boolean[] paramArrayOfBoolean);
  
  public abstract void setWeightingValues(double[] paramArrayOfDouble);
  
  public abstract int getNumGeneratingModels();
  
  public abstract void setSeed(int paramInt);
}
