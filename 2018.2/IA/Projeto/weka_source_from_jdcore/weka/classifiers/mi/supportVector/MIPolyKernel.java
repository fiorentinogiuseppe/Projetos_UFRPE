package weka.classifiers.mi.supportVector;

import weka.classifiers.functions.supportVector.PolyKernel;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MultiInstanceCapabilitiesHandler;
import weka.core.RevisionUtils;












































































public class MIPolyKernel
  extends PolyKernel
  implements MultiInstanceCapabilitiesHandler
{
  private static final long serialVersionUID = 7926421479341051777L;
  
  public MIPolyKernel() {}
  
  public MIPolyKernel(Instances data, int cacheSize, double exponent, boolean lowerOrder)
    throws Exception
  {
    super(data, cacheSize, exponent, lowerOrder);
  }
  









  protected double evaluate(int id1, int id2, Instance inst1)
    throws Exception
  {
    Instances data1 = new Instances(inst1.relationalValue(1));
    Instances data2;
    Instances data2; if (id1 == id2) {
      data2 = new Instances(data1);
    } else {
      data2 = new Instances(m_data.instance(id2).relationalValue(1));
    }
    double res = 0.0D;
    for (int i = 0; i < data1.numInstances(); i++) {
      for (int j = 0; j < data2.numInstances(); j++) {
        double result = dotProd(data1.instance(i), data2.instance(j));
        

        if (getUseLowerOrder()) {
          result += 1.0D;
        }
        if (getExponent() != 1.0D) {
          result = Math.pow(result, getExponent());
        }
        
        res += result;
      }
    }
    
    return res;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    result.disable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    

    result.enable(Capabilities.Capability.ONLY_MULTIINSTANCE);
    
    return result;
  }
  






  public Capabilities getMultiInstanceCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  


  public void clean()
  {
    m_storage = null;
    m_keys = null;
    m_kernelMatrix = ((double[][])null);
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 10036 $");
  }
}
