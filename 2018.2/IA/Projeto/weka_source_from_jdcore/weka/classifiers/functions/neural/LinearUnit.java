package weka.classifiers.functions.neural;

import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
































public class LinearUnit
  implements NeuralMethod, RevisionHandler
{
  private static final long serialVersionUID = 8572152807755673630L;
  
  public LinearUnit() {}
  
  public double outputValue(NeuralNode node)
  {
    double[] weights = node.getWeights();
    NeuralConnection[] inputs = node.getInputs();
    double value = weights[0];
    for (int noa = 0; noa < node.getNumInputs(); noa++)
    {
      value += inputs[noa].outputValue(true) * weights[(noa + 1)];
    }
    

    return value;
  }
  






  public double errorValue(NeuralNode node)
  {
    NeuralConnection[] outputs = node.getOutputs();
    int[] oNums = node.getOutputNums();
    double error = 0.0D;
    
    for (int noa = 0; noa < node.getNumOutputs(); noa++) {
      error += outputs[noa].errorValue(true) * outputs[noa].weightValue(oNums[noa]);
    }
    
    return error;
  }
  







  public void updateWeights(NeuralNode node, double learn, double momentum)
  {
    NeuralConnection[] inputs = node.getInputs();
    double[] cWeights = node.getChangeInWeights();
    double[] weights = node.getWeights();
    
    double learnTimesError = 0.0D;
    learnTimesError = learn * node.errorValue(false);
    
    double c = learnTimesError + momentum * cWeights[0];
    weights[0] += c;
    cWeights[0] = c;
    
    int stopValue = node.getNumInputs() + 1;
    for (int noa = 1; noa < stopValue; noa++)
    {
      c = learnTimesError * inputs[(noa - 1)].outputValue(false);
      c += momentum * cWeights[noa];
      
      weights[noa] += c;
      cWeights[noa] = c;
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.7 $");
  }
}
