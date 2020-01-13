package weka.classifiers;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.rules.ZeroR;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;

































public abstract class SingleClassifierEnhancer
  extends Classifier
{
  private static final long serialVersionUID = -3665885256363525164L;
  protected Classifier m_Classifier = new ZeroR();
  

  public SingleClassifierEnhancer() {}
  
  protected String defaultClassifierString()
  {
    return "weka.classifiers.rules.ZeroR";
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    
    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    
    newVector.addElement(new Option("\tFull name of base classifier.\n\t(default: " + defaultClassifierString() + ")", "W", 1, "-W"));
    



    newVector.addElement(new Option("", "", 0, "\nOptions specific to classifier " + m_Classifier.getClass().getName() + ":"));
    


    enu = m_Classifier.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    
    return newVector.elements();
  }
  










  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String classifierName = Utils.getOption('W', options);
    
    if (classifierName.length() > 0)
    {


      setClassifier(Classifier.forName(classifierName, null));
      setClassifier(Classifier.forName(classifierName, Utils.partitionOptions(options)));

    }
    else
    {

      setClassifier(Classifier.forName(defaultClassifierString(), null));
      setClassifier(Classifier.forName(defaultClassifierString(), Utils.partitionOptions(options)));
    }
  }
  






  public String[] getOptions()
  {
    String[] classifierOptions = m_Classifier.getOptions();
    int extraOptionsLength = classifierOptions.length;
    if (extraOptionsLength > 0) {
      extraOptionsLength++;
    }
    
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + extraOptionsLength + 2];
    

    int current = 0;
    options[(current++)] = "-W";
    options[(current++)] = getClassifier().getClass().getName();
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    
    current += superOptions.length;
    
    if (classifierOptions.length > 0) {
      options[(current++)] = "--";
      System.arraycopy(classifierOptions, 0, options, current, classifierOptions.length);
    }
    

    return options;
  }
  




  public String classifierTipText()
  {
    return "The base classifier to be used.";
  }
  


  public Capabilities getCapabilities()
  {
    Capabilities result;
    
    Capabilities result;
    
    if (getClassifier() != null) {
      result = getClassifier().getCapabilities();
    } else {
      result = new Capabilities(this);
      result.disableAll();
    }
    

    for (Capabilities.Capability cap : Capabilities.Capability.values()) {
      result.enableDependency(cap);
    }
    result.setOwner(this);
    
    return result;
  }
  





  public void setClassifier(Classifier newClassifier)
  {
    m_Classifier = newClassifier;
  }
  





  public Classifier getClassifier()
  {
    return m_Classifier;
  }
  






  protected String getClassifierSpec()
  {
    Classifier c = getClassifier();
    return c.getClass().getName() + " " + Utils.joinOptions(c.getOptions());
  }
}
