package weka.filters.unsupervised.instance;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;















































public class Normalize
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  static final long serialVersionUID = -7947971807522917395L;
  protected double m_Norm = 1.0D;
  

  protected double m_LNorm = 2.0D;
  


  public Normalize() {}
  


  public String globalInfo()
  {
    return "An instance filter that normalize instances considering only numeric attributes and ignoring class index.";
  }
  






  public String LNormTipText()
  {
    return "The LNorm to use.";
  }
  





  public String normTipText()
  {
    return "The norm of the instances after normalization.";
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    newVector.addElement(new Option("\tSpecify the norm that each instance must have (default 1.0)", "N", 1, "-N <num>"));
    

    newVector.addElement(new Option("\tSpecify L-norm to use (default 2.0)", "L", 1, "-L <num>"));
    

    return newVector.elements();
  }
  

















  public void setOptions(String[] options)
    throws Exception
  {
    String normString = Utils.getOption('N', options);
    if (normString.length() != 0) {
      setNorm(Double.parseDouble(normString));
    } else {
      setNorm(1.0D);
    }
    
    String lNormString = Utils.getOption('L', options);
    if (lNormString.length() != 0) {
      setLNorm(Double.parseDouble(lNormString));
    } else {
      setLNorm(2.0D);
    }
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  




  public String[] getOptions()
  {
    String[] options = new String[4];
    int current = 0;
    
    options[(current++)] = "-N";
    options[(current++)] = ("" + getNorm());
    
    options[(current++)] = "-L";
    options[(current++)] = ("" + getLNorm());
    
    return options;
  }
  





  public double getNorm()
  {
    return m_Norm;
  }
  




  public void setNorm(double newNorm)
  {
    m_Norm = newNorm;
  }
  




  public double getLNorm()
  {
    return m_LNorm;
  }
  




  public void setLNorm(double newLNorm)
  {
    m_LNorm = newLNorm;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  


















  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    setOutputFormat(instanceInfo);
    return true;
  }
  









  public boolean input(Instance instance)
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    Instance inst = (Instance)instance.copy();
    

    double iNorm = 0.0D;
    for (int i = 0; i < getInputFormat().numAttributes(); i++) {
      if ((getInputFormat().classIndex() != i) && 
        (getInputFormat().attribute(i).isNumeric()))
        iNorm += Math.pow(Math.abs(inst.value(i)), getLNorm());
    }
    iNorm = Math.pow(iNorm, 1.0D / getLNorm());
    

    for (int i = 0; i < getInputFormat().numAttributes(); i++) {
      if ((getInputFormat().classIndex() != i) && 
        (getInputFormat().attribute(i).isNumeric())) {
        inst.setValue(i, inst.value(i) / iNorm * getNorm());
      }
    }
    push(inst);
    return true;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5548 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new Normalize(), argv);
  }
}
