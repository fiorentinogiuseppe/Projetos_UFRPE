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
































































public class PolyKernel
  extends CachedKernel
{
  static final long serialVersionUID = -321831645846363201L;
  protected boolean m_lowerOrder = false;
  

  protected double m_exponent = 1.0D;
  




  public PolyKernel() {}
  



  public void clean()
  {
    if (getExponent() == 1.0D) {
      m_data = null;
    }
    super.clean();
  }
  












  public PolyKernel(Instances data, int cacheSize, double exponent, boolean lowerOrder)
    throws Exception
  {
    setCacheSize(cacheSize);
    setExponent(exponent);
    setUseLowerOrder(lowerOrder);
    
    buildKernel(data);
  }
  





  public String globalInfo()
  {
    return "The polynomial kernel : K(x, y) = <x, y>^p or K(x, y) = (<x, y>+1)^p";
  }
  








  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tThe Exponent to use.\n\t(default: 1.0)", "E", 1, "-E <num>"));
    



    result.addElement(new Option("\tUse lower-order terms.\n\t(default: no)", "L", 0, "-L"));
    



    return result.elements();
  }
  
































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('E', options);
    if (tmpStr.length() != 0) {
      setExponent(Double.parseDouble(tmpStr));
    } else {
      setExponent(1.0D);
    }
    setUseLowerOrder(Utils.getFlag('L', options));
    
    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-E");
    result.add("" + getExponent());
    
    if (getUseLowerOrder()) {
      result.add("-L");
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  



  protected double evaluate(int id1, int id2, Instance inst1)
    throws Exception
  {
    double result;
    

    double result;
    

    if (id1 == id2) {
      result = dotProd(inst1, inst1);
    } else {
      result = dotProd(inst1, m_data.instance(id2));
    }
    
    if (m_lowerOrder) {
      result += 1.0D;
    }
    if (m_exponent != 1.0D) {
      result = Math.pow(result, m_exponent);
    }
    return result;
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
  




  public void setExponent(double value)
  {
    m_exponent = value;
  }
  




  public double getExponent()
  {
    return m_exponent;
  }
  





  public String exponentTipText()
  {
    return "The exponent value.";
  }
  




  public void setUseLowerOrder(boolean value)
  {
    m_lowerOrder = value;
  }
  




  public boolean getUseLowerOrder()
  {
    return m_lowerOrder;
  }
  





  public String useLowerOrderTipText()
  {
    return "Whether to use lower-order terms.";
  }
  


  public String toString()
  {
    String result;
    
    String result;
    
    if (getExponent() == 1.0D) { String result;
      if (getUseLowerOrder()) {
        result = "Linear Kernel with lower order: K(x,y) = <x,y> + 1";
      } else
        result = "Linear Kernel: K(x,y) = <x,y>";
    } else {
      String result;
      if (getUseLowerOrder()) {
        result = "Poly Kernel with lower order: K(x,y) = (<x,y> + 1)^" + getExponent();
      } else {
        result = "Poly Kernel: K(x,y) = <x,y>^" + getExponent();
      }
    }
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9993 $");
  }
}
