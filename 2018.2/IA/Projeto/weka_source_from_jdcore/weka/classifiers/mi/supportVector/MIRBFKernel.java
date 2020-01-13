package weka.classifiers.mi.supportVector;

import weka.classifiers.functions.supportVector.RBFKernel;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MultiInstanceCapabilitiesHandler;
import weka.core.RevisionUtils;










































































public class MIRBFKernel
  extends RBFKernel
  implements MultiInstanceCapabilitiesHandler
{
  private static final long serialVersionUID = -8711882393708956962L;
  protected double[][] m_kernelPrecalc;
  
  public MIRBFKernel() {}
  
  public MIRBFKernel(Instances data, int cacheSize, double gamma)
    throws Exception
  {
    super(data, cacheSize, gamma);
  }
  








  protected double evaluate(int id1, int id2, Instance inst1)
    throws Exception
  {
    double result = 0.0D;
    Instances insts1;
    Instances insts1; if (id1 == -1) {
      insts1 = new Instances(inst1.relationalValue(1));
    } else
      insts1 = new Instances(m_data.instance(id1).relationalValue(1));
    Instances insts2 = new Instances(m_data.instance(id2).relationalValue(1));
    
    double precalc1 = 0.0D;
    for (int i = 0; i < insts1.numInstances(); i++) {
      for (int j = 0; j < insts2.numInstances(); j++) {
        if (id1 == -1) {
          precalc1 = dotProd(insts1.instance(i), insts1.instance(i));
        } else {
          precalc1 = m_kernelPrecalc[id1][i];
        }
        double res = Math.exp(m_gamma * (2.0D * dotProd(insts1.instance(i), insts2.instance(j)) - precalc1 - m_kernelPrecalc[id2][j]));
        
        result += res;
      }
    }
    
    return result;
  }
  




  protected void initVars(Instances data)
  {
    super.initVars(data);
    
    m_kernelPrecalc = new double[data.numInstances()][];
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
  






  public void buildKernel(Instances data)
    throws Exception
  {
    if (!getChecksTurnedOff()) {
      getCapabilities().testWithFail(data);
    }
    initVars(data);
    
    for (int i = 0; i < data.numInstances(); i++) {
      Instances insts = new Instances(data.instance(i).relationalValue(1));
      m_kernelPrecalc[i] = new double[insts.numInstances()];
      for (int j = 0; j < insts.numInstances(); j++) {
        m_kernelPrecalc[i][j] = dotProd(insts.instance(j), insts.instance(j));
      }
    }
  }
  



  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9144 $");
  }
}
