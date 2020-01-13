package weka.attributeSelection;

import java.io.PrintStream;
import java.io.Serializable;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SerializedObject;
import weka.core.Utils;















































public abstract class ASEvaluation
  implements Serializable, CapabilitiesHandler, RevisionHandler
{
  private static final long serialVersionUID = 2091705669885950849L;
  
  public ASEvaluation() {}
  
  public abstract void buildEvaluator(Instances paramInstances)
    throws Exception;
  
  public int[] postProcess(int[] attributeSet)
    throws Exception
  {
    return attributeSet;
  }
  













  public static ASEvaluation forName(String evaluatorName, String[] options)
    throws Exception
  {
    return (ASEvaluation)Utils.forName(ASEvaluation.class, evaluatorName, options);
  }
  













  public static ASEvaluation[] makeCopies(ASEvaluation model, int num)
    throws Exception
  {
    if (model == null) {
      throw new Exception("No model evaluator set");
    }
    ASEvaluation[] evaluators = new ASEvaluation[num];
    SerializedObject so = new SerializedObject(model);
    for (int i = 0; i < evaluators.length; i++) {
      evaluators[i] = ((ASEvaluation)so.getObject());
    }
    return evaluators;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = new Capabilities(this);
    result.enableAll();
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11851 $");
  }
  






  public void clean() {}
  






  protected static void runEvaluator(ASEvaluation evaluator, String[] options)
  {
    try
    {
      System.out.println(AttributeSelection.SelectAttributes(evaluator, options));
    }
    catch (Exception e)
    {
      String msg = e.toString().toLowerCase();
      if ((msg.indexOf("help requested") == -1) && (msg.indexOf("no training file given") == -1))
      {
        e.printStackTrace(); }
      System.err.println(e.getMessage());
    }
  }
}
