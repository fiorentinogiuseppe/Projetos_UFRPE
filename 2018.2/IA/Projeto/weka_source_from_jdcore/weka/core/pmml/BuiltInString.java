package weka.core.pmml;

import java.util.ArrayList;
import weka.core.Attribute;
import weka.core.FastVector;











public class BuiltInString
  extends Function
{
  private static final long serialVersionUID = -7391516909331728653L;
  protected StringFunc m_func;
  
  static abstract enum StringFunc
  {
    UPPERCASE("uppercase"), 
    











    SUBSTRING("substring"), 
    














    TRIMBLANKS("trimBlanks");
    


    private String m_stringVal;
    


    abstract String eval(Object[] paramArrayOfObject);
    


    abstract boolean legalNumParams(int paramInt);
    


    abstract String[] getParameterNames();
    

    private StringFunc(String funcName)
    {
      m_stringVal = funcName;
    }
    
    public String toString() {
      return m_stringVal;
    }
  }
  




  protected Attribute m_outputDef = null;
  
  BuiltInString(StringFunc func) {
    m_func = func;
    m_functionName = m_func.toString();
  }
  






  public Attribute getOutputDef()
  {
    if (m_outputDef == null) {
      if (m_func == StringFunc.SUBSTRING)
      {

        m_outputDef = new Attribute("BuiltInStringResult:substring", (FastVector)null);
      }
      
      Attribute inputVals = (Attribute)m_parameterDefs.get(0);
      FastVector newVals = new FastVector();
      for (int i = 0; i < inputVals.numValues(); i++) {
        String inVal = inputVals.value(i);
        newVals.addElement(m_func.eval(new Object[] { inVal }));
      }
      m_outputDef = new Attribute("BuiltInStringResult:" + m_func.toString(), newVals);
    }
    
    return m_outputDef;
  }
  







  public String[] getParameterNames()
  {
    return m_func.getParameterNames();
  }
  
  private Object[] setUpArgs(double[] incoming)
  {
    Object[] args = new Object[incoming.length];
    Attribute input = (Attribute)m_parameterDefs.get(0);
    args[0] = input.value((int)incoming[0]);
    for (int i = 1; i < incoming.length; i++) {
      args[i] = new Integer((int)incoming[i]);
    }
    
    return args;
  }
  









  public double getResult(double[] incoming)
    throws Exception
  {
    if (m_parameterDefs == null) {
      throw new Exception("[BuiltInString] incoming parameter structure has not been set");
    }
    
    if (!m_func.legalNumParams(incoming.length)) {
      throw new Exception("[BuiltInString] wrong number of parameters!");
    }
    

    Object[] args = setUpArgs(incoming);
    

    String result = m_func.eval(args);
    int resultI = m_outputDef.indexOfValue(result);
    if (resultI < 0) {
      if (m_outputDef.isString())
      {
        resultI = m_outputDef.addStringValue(result);
      } else {
        throw new Exception("[BuiltInString] unable to find value " + result + " in nominal result type!");
      }
    }
    

    return resultI;
  }
  






































  public void setParameterDefs(ArrayList<Attribute> paramDefs)
    throws Exception
  {
    m_parameterDefs = paramDefs;
    
    if (!m_func.legalNumParams(m_parameterDefs.size())) {
      throw new Exception("[BuiltInMath] illegal number of parameters for function: " + m_functionName);
    }
  }
  
  public String toString()
  {
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
