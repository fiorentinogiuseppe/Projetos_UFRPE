package weka.classifiers.trees.j48;

import java.util.Enumeration;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;










































public class NBTreeModelSelection
  extends ModelSelection
{
  private static final long serialVersionUID = 990097748931976704L;
  private int m_minNoObj;
  private Instances m_allData;
  
  public NBTreeModelSelection(int minNoObj, Instances allData)
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
    double globalErrors = 0.0D;
    



    NBTreeSplit bestModel = null;
    NBTreeNoSplit noSplitModel = null;
    int validModels = 0;
    boolean multiVal = true;
    




    try
    {
      noSplitModel = new NBTreeNoSplit();
      noSplitModel.buildClassifier(data);
      if (data.numInstances() < 5) {
        return noSplitModel;
      }
      

      globalErrors = noSplitModel.getErrors();
      if (globalErrors == 0.0D) {
        return noSplitModel;
      }
      


      Distribution checkDistribution = new Distribution(data);
      if ((Utils.sm(checkDistribution.total(), m_minNoObj)) || (Utils.eq(checkDistribution.total(), checkDistribution.perClass(checkDistribution.maxClass()))))
      {

        return noSplitModel;
      }
      


      if (m_allData != null) {
        Enumeration enu = data.enumerateAttributes();
        while (enu.hasMoreElements()) {
          Attribute attribute = (Attribute)enu.nextElement();
          if ((attribute.isNumeric()) || (Utils.sm(attribute.numValues(), 0.3D * m_allData.numInstances())))
          {

            multiVal = false;
          }
        }
      }
      

      NBTreeSplit[] currentModel = new NBTreeSplit[data.numAttributes()];
      double sumOfWeights = data.sumOfWeights();
      

      for (int i = 0; i < data.numAttributes(); i++)
      {

        if (i != data.classIndex())
        {

          currentModel[i] = new NBTreeSplit(i, m_minNoObj, sumOfWeights);
          currentModel[i].setGlobalModel(noSplitModel);
          currentModel[i].buildClassifier(data);
          



          if (currentModel[i].checkModel()) {
            validModels++;
          }
        } else {
          currentModel[i] = null;
        }
      }
      

      if (validModels == 0) {
        return noSplitModel;
      }
      

      double minResult = globalErrors;
      for (i = 0; i < data.numAttributes(); i++) {
        if ((i != data.classIndex()) && (currentModel[i].checkModel()))
        {


          if (currentModel[i].getErrors() < minResult) {
            bestModel = currentModel[i];
            minResult = currentModel[i].getErrors();
          }
        }
      }
      



      if ((globalErrors - minResult) / globalErrors < 0.05D) {
        return noSplitModel;
      }
      








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
    return RevisionUtils.extract("$Revision: 1.5 $");
  }
}
