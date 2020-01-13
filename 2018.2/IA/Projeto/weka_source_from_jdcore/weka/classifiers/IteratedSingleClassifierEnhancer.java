package weka.classifiers;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;




































public abstract class IteratedSingleClassifierEnhancer
  extends SingleClassifierEnhancer
{
  private static final long serialVersionUID = -6217979135443319724L;
  protected Classifier[] m_Classifiers;
  protected int m_NumIterations = 10;
  


  public IteratedSingleClassifierEnhancer() {}
  


  public void buildClassifier(Instances data)
    throws Exception
  {
    if (m_Classifier == null) {
      throw new Exception("A base classifier has not been specified!");
    }
    m_Classifiers = Classifier.makeCopies(m_Classifier, m_NumIterations);
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tNumber of iterations.\n\t(default 10)", "I", 1, "-I <num>"));
    



    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }
  













  public void setOptions(String[] options)
    throws Exception
  {
    String iterations = Utils.getOption('I', options);
    if (iterations.length() != 0) {
      setNumIterations(Integer.parseInt(iterations));
    } else {
      setNumIterations(10);
    }
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 2];
    
    int current = 0;
    options[(current++)] = "-I";
    options[(current++)] = ("" + getNumIterations());
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    

    return options;
  }
  




  public String numIterationsTipText()
  {
    return "The number of iterations to be performed.";
  }
  



  public void setNumIterations(int numIterations)
  {
    m_NumIterations = numIterations;
  }
  





  public int getNumIterations()
  {
    return m_NumIterations;
  }
}
