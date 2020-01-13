package weka.classifiers.functions.supportVector;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
































































public class RBFKernel
  extends CachedKernel
{
  static final long serialVersionUID = 5247117544316387852L;
  protected double[] m_kernelPrecalc;
  protected double m_gamma = 0.01D;
  








  public RBFKernel() {}
  







  public RBFKernel(Instances data, int cacheSize, double gamma)
    throws Exception
  {
    setCacheSize(cacheSize);
    setGamma(gamma);
    
    buildKernel(data);
  }
  





  public String globalInfo()
  {
    return "The RBF kernel. K(x, y) = e^-(gamma * <x-y, x-y>^2)";
  }
  








  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tThe Gamma parameter.\n\t(default: 0.01)", "G", 1, "-G <num>"));
    



    return result.elements();
  }
  




























  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('G', options);
    if (tmpStr.length() != 0) {
      setGamma(Double.parseDouble(tmpStr));
    } else {
      setGamma(0.01D);
    }
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-G");
    result.add("" + getGamma());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  








  protected double evaluate(int id1, int id2, Instance inst1)
    throws Exception
  {
    if (id1 == id2)
      return 1.0D;
    double precalc1;
    double precalc1;
    if (id1 == -1) {
      precalc1 = dotProd(inst1, inst1);
    } else
      precalc1 = m_kernelPrecalc[id1];
    Instance inst2 = m_data.instance(id2);
    double result = Math.exp(m_gamma * (2.0D * dotProd(inst1, inst2) - precalc1 - m_kernelPrecalc[id2]));
    

    return result;
  }
  





  public void setGamma(double value)
  {
    m_gamma = value;
  }
  




  public double getGamma()
  {
    return m_gamma;
  }
  





  public String gammaTipText()
  {
    return "The Gamma value.";
  }
  




  protected void initVars(Instances data)
  {
    super.initVars(data);
    
    m_kernelPrecalc = new double[data.numInstances()];
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enableAllClasses();
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  






  public void buildKernel(Instances data)
    throws Exception
  {
    if (!getChecksTurnedOff()) {
      getCapabilities().testWithFail(data);
    }
    initVars(data);
    
    for (int i = 0; i < data.numInstances(); i++) {
      m_kernelPrecalc[i] = dotProd(data.instance(i), data.instance(i));
    }
  }
  



  public String toString()
  {
    return "RBF kernel: K(x,y) = e^-(" + getGamma() + "* <x-y,x-y>^2)";
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5518 $");
  }
}
