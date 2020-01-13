package weka.core.pmml;

import java.util.ArrayList;
import weka.core.Attribute;


































public class BuiltInArithmetic
  extends Function
{
  private static final long serialVersionUID = 2275009453597279459L;
  
  static abstract enum Operator
  {
    ADDITION(" + "), 
    



    SUBTRACTION(" - "), 
    



    MULTIPLICATION(" * "), 
    



    DIVISION(" / ");
    

    private final String m_stringVal;
    

    abstract double eval(double paramDouble1, double paramDouble2);
    

    private Operator(String opName)
    {
      m_stringVal = opName;
    }
    
    public String toString() {
      return m_stringVal;
    }
  }
  

  protected Operator m_operator = Operator.ADDITION;
  



  public BuiltInArithmetic(Operator op)
  {
    m_operator = op;
    m_functionName = m_operator.toString();
  }
  






  public void setParameterDefs(ArrayList<Attribute> paramDefs)
    throws Exception
  {
    m_parameterDefs = paramDefs;
    
    if (m_parameterDefs.size() != 2) {
      throw new Exception("[Arithmetic] wrong number of parameters. Recieved " + m_parameterDefs.size() + ", expected 2.");
    }
  }
  






  public String[] getParameterNames()
  {
    String[] result = { "A", "B" };
    return result;
  }
  





  public Attribute getOutputDef()
  {
    return new Attribute("BuiltInArithmeticResult:" + m_operator.toString());
  }
  








  public double getResult(double[] incoming)
    throws Exception
  {
    if (m_parameterDefs == null) {
      throw new Exception("[BuiltInArithmetic] incoming parameter structure has not been set!");
    }
    
    if ((m_parameterDefs.size() != 2) || (incoming.length != 2)) {
      throw new Exception("[BuiltInArithmetic] wrong number of parameters!");
    }
    
    double result = m_operator.eval(incoming[0], incoming[1]);
    
    return result;
  }
  
  public String toString() {
    return toString("");
  }
  
  public String toString(String pad) {
    return pad + ((Attribute)m_parameterDefs.get(0)).name() + m_functionName + ((Attribute)m_parameterDefs.get(1)).name();
  }
}
