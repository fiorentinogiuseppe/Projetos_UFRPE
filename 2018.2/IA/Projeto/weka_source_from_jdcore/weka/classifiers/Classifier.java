package weka.classifiers;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SerializedObject;
import weka.core.Utils;



































public abstract class Classifier
  implements Cloneable, Serializable, OptionHandler, CapabilitiesHandler, RevisionHandler
{
  private static final long serialVersionUID = 6502780192411755341L;
  protected boolean m_Debug = false;
  





  public Classifier() {}
  





  public abstract void buildClassifier(Instances paramInstances)
    throws Exception;
  




  public double classifyInstance(Instance instance)
    throws Exception
  {
    double[] dist = distributionForInstance(instance);
    if (dist == null) {
      throw new Exception("Null distribution predicted");
    }
    switch (instance.classAttribute().type()) {
    case 1: 
      double max = 0.0D;
      int maxIndex = 0;
      
      for (int i = 0; i < dist.length; i++) {
        if (dist[i] > max) {
          maxIndex = i;
          max = dist[i];
        }
      }
      if (max > 0.0D) {
        return maxIndex;
      }
      return Instance.missingValue();
    
    case 0: 
    case 3: 
      return dist[0];
    }
    return Instance.missingValue();
  }
  












  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    double[] dist = new double[instance.numClasses()];
    switch (instance.classAttribute().type()) {
    case 1: 
      double classification = classifyInstance(instance);
      if (Instance.isMissingValue(classification)) {
        return dist;
      }
      dist[((int)classification)] = 1.0D;
      
      return dist;
    case 0: 
    case 3: 
      dist[0] = classifyInstance(instance);
      return dist;
    }
    return dist;
  }
  














  public static Classifier forName(String classifierName, String[] options)
    throws Exception
  {
    return (Classifier)Utils.forName(Classifier.class, classifierName, options);
  }
  







  public static Classifier makeCopy(Classifier model)
    throws Exception
  {
    return (Classifier)new SerializedObject(model).getObject();
  }
  









  public static Classifier[] makeCopies(Classifier model, int num)
    throws Exception
  {
    if (model == null) {
      throw new Exception("No model classifier set");
    }
    Classifier[] classifiers = new Classifier[num];
    SerializedObject so = new SerializedObject(model);
    for (int i = 0; i < classifiers.length; i++) {
      classifiers[i] = ((Classifier)so.getObject());
    }
    return classifiers;
  }
  






  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    
    newVector.addElement(new Option("\tIf set, classifier is run in debug mode and\n\tmay output additional info to the console", "D", 0, "-D"));
    

    return newVector.elements();
  }
  












  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
  }
  



  public String[] getOptions()
  {
    String[] options;
    


    if (getDebug()) {
      String[] options = new String[1];
      options[0] = "-D";
    } else {
      options = new String[0];
    }
    return options;
  }
  





  public void setDebug(boolean debug)
  {
    m_Debug = debug;
  }
  





  public boolean getDebug()
  {
    return m_Debug;
  }
  





  public String debugTipText()
  {
    return "If set to true, classifier may output additional info to the console.";
  }
  










  public Capabilities getCapabilities()
  {
    Capabilities result = new Capabilities(this);
    result.enableAll();
    
    return result;
  }
  





  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10485 $");
  }
  




  protected static void runClassifier(Classifier classifier, String[] options)
  {
    try
    {
      System.out.println(Evaluation.evaluateModel(classifier, options));
    } catch (Exception e) {
      if (((e.getMessage() != null) && (e.getMessage().indexOf("General options") == -1)) || (e.getMessage() == null))
      {

        e.printStackTrace();
      } else {
        System.err.println(e.getMessage());
      }
    }
  }
}
