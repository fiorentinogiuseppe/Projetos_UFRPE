package weka.classifiers.functions.neural;

import java.io.Serializable;

public abstract interface NeuralMethod
  extends Serializable
{
  public abstract double outputValue(NeuralNode paramNeuralNode);
  
  public abstract double errorValue(NeuralNode paramNeuralNode);
  
  public abstract void updateWeights(NeuralNode paramNeuralNode, double paramDouble1, double paramDouble2);
}
