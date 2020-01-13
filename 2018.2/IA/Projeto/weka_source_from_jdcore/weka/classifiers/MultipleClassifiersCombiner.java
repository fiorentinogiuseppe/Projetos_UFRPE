package weka.classifiers;

import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.rules.ZeroR;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
































public abstract class MultipleClassifiersCombiner
  extends Classifier
{
  private static final long serialVersionUID = 2776436621129422119L;
  protected Classifier[] m_Classifiers = { new ZeroR() };
  



  public MultipleClassifiersCombiner() {}
  


  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    
    newVector.addElement(new Option("\tFull class name of classifier to include, followed\n\tby scheme options. May be specified multiple times.\n\t(default: \"weka.classifiers.rules.ZeroR\")", "B", 1, "-B <classifier specification>"));
    




    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  











  public void setOptions(String[] options)
    throws Exception
  {
    Vector classifiers = new Vector();
    for (;;) {
      String classifierString = Utils.getOption('B', options);
      if (classifierString.length() == 0) {
        break;
      }
      String[] classifierSpec = Utils.splitOptions(classifierString);
      if (classifierSpec.length == 0) {
        throw new IllegalArgumentException("Invalid classifier specification string");
      }
      String classifierName = classifierSpec[0];
      classifierSpec[0] = "";
      classifiers.addElement(Classifier.forName(classifierName, classifierSpec));
    }
    
    if (classifiers.size() == 0) {
      classifiers.addElement(new ZeroR());
    }
    Classifier[] classifiersArray = new Classifier[classifiers.size()];
    for (int i = 0; i < classifiersArray.length; i++) {
      classifiersArray[i] = ((Classifier)classifiers.elementAt(i));
    }
    setClassifiers(classifiersArray);
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    int current = 0;
    String[] options = new String[superOptions.length + m_Classifiers.length * 2];
    for (int i = 0; i < m_Classifiers.length; i++) {
      options[(current++)] = "-B";
      options[(current++)] = ("" + getClassifierSpec(i));
    }
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    
    return options;
  }
  




  public String classifiersTipText()
  {
    return "The base classifiers to be used.";
  }
  





  public void setClassifiers(Classifier[] classifiers)
  {
    m_Classifiers = classifiers;
  }
  





  public Classifier[] getClassifiers()
  {
    return m_Classifiers;
  }
  






  public Classifier getClassifier(int index)
  {
    return m_Classifiers[index];
  }
  









  protected String getClassifierSpec(int index)
  {
    if (m_Classifiers.length < index) {
      return "";
    }
    Classifier c = getClassifier(index);
    return c.getClass().getName() + " " + Utils.joinOptions(c.getOptions());
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result;
    



    if (getClassifiers().length == 0) {
      Capabilities result = new Capabilities(this);
      result.disableAll();
    }
    else {
      result = (Capabilities)getClassifier(0).getCapabilities().clone();
      for (int i = 1; i < getClassifiers().length; i++) {
        result.and(getClassifier(i).getCapabilities());
      }
    }
    
    for (Capabilities.Capability cap : Capabilities.Capability.values()) {
      result.enableDependency(cap);
    }
    result.setOwner(this);
    
    return result;
  }
}
