package weka.core.pmml;

import java.util.ArrayList;
import weka.core.Attribute;
import weka.core.Utils;


































public class BuiltInMath
  extends Function
{
  private static final long serialVersionUID = -8092338695602573652L;
  
  static abstract enum MathFunc
  {
    MIN("min"), 
    











    MAX("max"), 
    











    SUM("sum"), 
    











    AVG("avg"), 
    











    LOG10("log10"), 
    











    LN("ln"), 
    











    SQRT("sqrt"), 
    











    ABS("abs"), 
    











    EXP("exp"), 
    











    POW("pow"), 
    











    THRESHOLD("threshold"), 
    















    FLOOR("floor"), 
    











    CEIL("ceil"), 
    











    ROUND("round");
    


    private final String m_stringVal;
    


    abstract double eval(double[] paramArrayOfDouble);
    


    abstract boolean legalNumParams(int paramInt);
    


    abstract String[] getParameterNames();
    

    private MathFunc(String funcName)
    {
      m_stringVal = funcName;
    }
    
    public String toString() {
      return m_stringVal;
    }
  }
  

  protected MathFunc m_func = MathFunc.ABS;
  



  public BuiltInMath(MathFunc func)
  {
    m_func = func;
    m_functionName = m_func.toString();
  }
  






  public void setParameterDefs(ArrayList<Attribute> paramDefs)
    throws Exception
  {
    m_parameterDefs = paramDefs;
    
    if (!m_func.legalNumParams(m_parameterDefs.size())) {
      throw new Exception("[BuiltInMath] illegal number of parameters for function: " + m_functionName);
    }
  }
  






  public Attribute getOutputDef()
  {
    return new Attribute("BuiltInMathResult:" + m_func.toString());
  }
  







  public String[] getParameterNames()
  {
    return m_func.getParameterNames();
  }
  








  public double getResult(double[] incoming)
    throws Exception
  {
    if (m_parameterDefs == null) {
      throw new Exception("[BuiltInMath] incoming parameter structure has not been set");
    }
    
    if (!m_func.legalNumParams(incoming.length)) {
      throw new Exception("[BuiltInMath] wrong number of parameters!");
    }
    
    double result = m_func.eval(incoming);
    
    return result;
  }
  
  public String toString() {
    String result = m_func.toString() + "(";
    for (int i = 0; i < m_parameterDefs.size(); i++) {
      result = result + ((Attribute)m_parameterDefs.get(i)).name();
      if (i != m_parameterDefs.size() - 1) {
        result = result + ", ";
      } else {
        result = result + ")";
      }
    }
    return result;
  }
}
