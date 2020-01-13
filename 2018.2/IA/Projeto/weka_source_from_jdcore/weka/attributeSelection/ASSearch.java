package weka.attributeSelection;

import java.io.Serializable;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SerializedObject;
import weka.core.Utils;





































public abstract class ASSearch
  implements Serializable, RevisionHandler
{
  private static final long serialVersionUID = 7591673350342236548L;
  
  public ASSearch() {}
  
  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.12 $");
  }
  










  public abstract int[] search(ASEvaluation paramASEvaluation, Instances paramInstances)
    throws Exception;
  










  public static ASSearch forName(String searchName, String[] options)
    throws Exception
  {
    return (ASSearch)Utils.forName(ASSearch.class, searchName, options);
  }
  












  public static ASSearch[] makeCopies(ASSearch model, int num)
    throws Exception
  {
    if (model == null) {
      throw new Exception("No model search scheme set");
    }
    ASSearch[] result = new ASSearch[num];
    SerializedObject so = new SerializedObject(model);
    for (int i = 0; i < result.length; i++) {
      result[i] = ((ASSearch)so.getObject());
    }
    return result;
  }
}
