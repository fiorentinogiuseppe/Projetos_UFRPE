package weka.classifiers.trees.lmt;

import weka.classifiers.trees.j48.ClassifierSplitModel;
import weka.classifiers.trees.j48.Distribution;
import weka.classifiers.trees.j48.ModelSelection;
import weka.classifiers.trees.j48.NoSplit;
import weka.core.Instances;
import weka.core.RevisionUtils;






































public class ResidualModelSelection
  extends ModelSelection
{
  private static final long serialVersionUID = -293098783159385148L;
  protected int m_minNumInstances;
  protected double m_minInfoGain;
  
  public ResidualModelSelection(int minNumInstances)
  {
    m_minNumInstances = minNumInstances;
    m_minInfoGain = 1.0E-4D;
  }
  



  public void cleanup() {}
  



  public final ClassifierSplitModel selectModel(Instances data, double[][] dataZs, double[][] dataWs)
    throws Exception
  {
    int numAttributes = data.numAttributes();
    
    if (numAttributes < 2) throw new Exception("Can't select Model without non-class attribute");
    if (data.numInstances() < m_minNumInstances) { return new NoSplit(new Distribution(data));
    }
    
    double bestGain = -1.7976931348623157E308D;
    int bestAttribute = -1;
    

    for (int i = 0; i < numAttributes; i++) {
      if (i != data.classIndex())
      {

        ResidualSplit split = new ResidualSplit(i);
        split.buildClassifier(data, dataZs, dataWs);
        
        if (split.checkModel(m_minNumInstances))
        {

          double gain = split.entropyGain();
          if (gain > bestGain) {
            bestGain = gain;
            bestAttribute = i;
          }
        }
      }
    }
    
    if (bestGain >= m_minInfoGain)
    {
      ResidualSplit split = new ResidualSplit(bestAttribute);
      split.buildClassifier(data, dataZs, dataWs);
      return split;
    }
    
    return new NoSplit(new Distribution(data));
  }
  


  public final ClassifierSplitModel selectModel(Instances train)
  {
    return null;
  }
  

  public final ClassifierSplitModel selectModel(Instances train, Instances test)
  {
    return null;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}
