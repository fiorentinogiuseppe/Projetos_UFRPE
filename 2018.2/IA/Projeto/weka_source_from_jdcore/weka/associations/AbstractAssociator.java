package weka.associations;

import java.io.PrintStream;
import java.io.Serializable;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SerializedObject;
import weka.core.Utils;











































public abstract class AbstractAssociator
  implements Cloneable, Associator, Serializable, CapabilitiesHandler, RevisionHandler
{
  private static final long serialVersionUID = -3017644543382432070L;
  
  public AbstractAssociator() {}
  
  public static Associator forName(String associatorName, String[] options)
    throws Exception
  {
    return (Associator)Utils.forName(Associator.class, associatorName, options);
  }
  







  public static Associator makeCopy(Associator model)
    throws Exception
  {
    return (Associator)new SerializedObject(model).getObject();
  }
  











  public static Associator[] makeCopies(Associator model, int num)
    throws Exception
  {
    if (model == null) {
      throw new Exception("No model associator set");
    }
    Associator[] associators = new Associator[num];
    SerializedObject so = new SerializedObject(model);
    for (int i = 0; i < associators.length; i++) {
      associators[i] = ((Associator)so.getObject());
    }
    return associators;
  }
  








  public Capabilities getCapabilities()
  {
    Capabilities result = new Capabilities(this);
    result.enableAll();
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5503 $");
  }
  




  protected static void runAssociator(Associator associator, String[] options)
  {
    try
    {
      System.out.println(AssociatorEvaluation.evaluate(associator, options));
    }
    catch (Exception e)
    {
      if ((e.getMessage() != null) && (e.getMessage().indexOf("General options") == -1))
      {
        e.printStackTrace();
      } else {
        System.err.println(e.getMessage());
      }
    }
  }
}
