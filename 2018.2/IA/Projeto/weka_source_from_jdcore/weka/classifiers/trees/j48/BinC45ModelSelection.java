package weka.classifiers.trees.j48;

import java.util.Enumeration;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;










































public class BinC45ModelSelection
  extends ModelSelection
{
  private static final long serialVersionUID = 179170923545122001L;
  private int m_minNoObj;
  private Instances m_allData;
  
  public BinC45ModelSelection(int minNoObj, Instances allData)
  {
    m_minNoObj = minNoObj;
    m_allData = allData;
  }
  



  public void cleanup()
  {
    m_allData = null;
  }
  






  public final ClassifierSplitModel selectModel(Instances data)
  {
    BinC45Split bestModel = null;
    NoSplit noSplitModel = null;
    double averageInfoGain = 0.0D;
    int validModels = 0;
    boolean multiVal = true;
    





    try
    {
      Distribution checkDistribution = new Distribution(data);
      noSplitModel = new NoSplit(checkDistribution);
      if ((Utils.sm(checkDistribution.total(), 2 * m_minNoObj)) || (Utils.eq(checkDistribution.total(), checkDistribution.perClass(checkDistribution.maxClass()))))
      {

        return noSplitModel;
      }
      

      Enumeration enu = data.enumerateAttributes();
      while (enu.hasMoreElements()) {
        Attribute attribute = (Attribute)enu.nextElement();
        if ((attribute.isNumeric()) || (Utils.sm(attribute.numValues(), 0.3D * m_allData.numInstances())))
        {

          multiVal = false;
          break;
        }
      }
      BinC45Split[] currentModel = new BinC45Split[data.numAttributes()];
      double sumOfWeights = data.sumOfWeights();
      

      for (int i = 0; i < data.numAttributes(); i++)
      {

        if (i != data.classIndex())
        {

          currentModel[i] = new BinC45Split(i, m_minNoObj, sumOfWeights);
          currentModel[i].buildClassifier(data);
          



          if ((currentModel[i].checkModel()) && (
            (data.attribute(i).isNumeric()) || (multiVal) || (Utils.sm(data.attribute(i).numValues(), 0.3D * m_allData.numInstances()))))
          {

            averageInfoGain += currentModel[i].infoGain();
            validModels++;
          }
        } else {
          currentModel[i] = null;
        }
      }
      
      if (validModels == 0)
        return noSplitModel;
      averageInfoGain /= validModels;
      

      double minResult = 0.0D;
      for (i = 0; i < data.numAttributes(); i++) {
        if ((i != data.classIndex()) && (currentModel[i].checkModel()))
        {



          if ((currentModel[i].infoGain() >= averageInfoGain - 0.001D) && (Utils.gr(currentModel[i].gainRatio(), minResult)))
          {
            bestModel = currentModel[i];
            minResult = currentModel[i].gainRatio();
          }
        }
      }
      
      if (Utils.eq(minResult, 0.0D)) {
        return noSplitModel;
      }
      


      bestModel.distribution().addInstWithUnknown(data, bestModel.attIndex());
      


      bestModel.setSplitPoint(m_allData);
      return bestModel;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  



  public final ClassifierSplitModel selectModel(Instances train, Instances test)
  {
    return selectModel(train);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.11 $");
  }
}
