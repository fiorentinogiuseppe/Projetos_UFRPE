package weka.classifiers.bayes.net.estimate;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instance;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;















































public class BayesNetEstimator
  implements OptionHandler, Serializable, RevisionHandler
{
  static final long serialVersionUID = 2184330197666253884L;
  protected double m_fAlpha = 0.5D;
  


  public BayesNetEstimator() {}
  

  public void estimateCPTs(BayesNet bayesNet)
    throws Exception
  {
    throw new Exception("Incorrect BayesNetEstimator: use subclass instead.");
  }
  





  public void updateClassifier(BayesNet bayesNet, Instance instance)
    throws Exception
  {
    throw new Exception("Incorrect BayesNetEstimator: use subclass instead.");
  }
  







  public double[] distributionForInstance(BayesNet bayesNet, Instance instance)
    throws Exception
  {
    throw new Exception("Incorrect BayesNetEstimator: use subclass instead.");
  }
  




  public void initCPTs(BayesNet bayesNet)
    throws Exception
  {
    throw new Exception("Incorrect BayesNetEstimator: use subclass instead.");
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(1);
    
    newVector.addElement(new Option("\tInitial count (alpha)\n", "A", 1, "-A <alpha>"));
    
    return newVector.elements();
  }
  













  public void setOptions(String[] options)
    throws Exception
  {
    String sAlpha = Utils.getOption('A', options);
    
    if (sAlpha.length() != 0) {
      m_fAlpha = new Float(sAlpha).floatValue();
    } else {
      m_fAlpha = 0.5D;
    }
    
    Utils.checkForRemainingOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] options = new String[2];
    int current = 0;
    
    options[(current++)] = "-A";
    options[(current++)] = ("" + m_fAlpha);
    
    return options;
  }
  



  public void setAlpha(double fAlpha)
  {
    m_fAlpha = fAlpha;
  }
  



  public double getAlpha()
  {
    return m_fAlpha;
  }
  



  public String alphaTipText()
  {
    return "Alpha is used for estimating the probability tables and can be interpreted as the initial count on each value.";
  }
  




  public String globalInfo()
  {
    return "BayesNetEstimator is the base class for estimating the conditional probability tables of a Bayes network once the structure has been learned.";
  }
  







  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.4 $");
  }
}
