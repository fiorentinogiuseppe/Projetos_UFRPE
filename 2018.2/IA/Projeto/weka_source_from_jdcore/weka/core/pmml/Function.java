package weka.core.pmml;

import java.io.Serializable;
import java.util.ArrayList;
import weka.core.Attribute;




































public abstract class Function
  implements Serializable
{
  private static final long serialVersionUID = -6997738288201933171L;
  protected String m_functionName;
  protected ArrayList<Attribute> m_parameterDefs = null;
  
  public Function() {}
  
  public String getName() { return m_functionName; }
  




















  public abstract String[] getParameterNames();
  



















  public abstract void setParameterDefs(ArrayList<Attribute> paramArrayList)
    throws Exception;
  



















  public abstract Attribute getOutputDef();
  



















  public abstract double getResult(double[] paramArrayOfDouble)
    throws Exception;
  



















  public static Function getFunction(String name)
  {
    Function result = null;
    
    name = name.trim();
    if (name.equals("+")) {
      result = new BuiltInArithmetic(BuiltInArithmetic.Operator.ADDITION);
    } else if (name.equals("-")) {
      result = new BuiltInArithmetic(BuiltInArithmetic.Operator.SUBTRACTION);
    } else if (name.equals("*")) {
      result = new BuiltInArithmetic(BuiltInArithmetic.Operator.MULTIPLICATION);
    } else if (name.equals("/")) {
      result = new BuiltInArithmetic(BuiltInArithmetic.Operator.DIVISION);
    } else if (name.equals(BuiltInMath.MathFunc.MIN.toString())) {
      result = new BuiltInMath(BuiltInMath.MathFunc.MIN);
    } else if (name.equals(BuiltInMath.MathFunc.MAX.toString())) {
      result = new BuiltInMath(BuiltInMath.MathFunc.MAX);
    } else if (name.equals(BuiltInMath.MathFunc.SUM.toString())) {
      result = new BuiltInMath(BuiltInMath.MathFunc.SUM);
    } else if (name.equals(BuiltInMath.MathFunc.AVG.toString())) {
      result = new BuiltInMath(BuiltInMath.MathFunc.AVG);
    } else if (name.equals(BuiltInMath.MathFunc.LOG10.toString())) {
      result = new BuiltInMath(BuiltInMath.MathFunc.LOG10);
    } else if (name.equals(BuiltInMath.MathFunc.LN.toString())) {
      result = new BuiltInMath(BuiltInMath.MathFunc.LN);
    } else if (name.equals(BuiltInMath.MathFunc.SQRT.toString())) {
      result = new BuiltInMath(BuiltInMath.MathFunc.SQRT);
    } else if (name.equals(BuiltInMath.MathFunc.ABS.toString())) {
      result = new BuiltInMath(BuiltInMath.MathFunc.ABS);
    } else if (name.equals(BuiltInMath.MathFunc.EXP.toString())) {
      result = new BuiltInMath(BuiltInMath.MathFunc.EXP);
    } else if (name.equals(BuiltInMath.MathFunc.POW.toString())) {
      result = new BuiltInMath(BuiltInMath.MathFunc.POW);
    } else if (name.equals(BuiltInMath.MathFunc.THRESHOLD.toString())) {
      result = new BuiltInMath(BuiltInMath.MathFunc.THRESHOLD);
    } else if (name.equals(BuiltInMath.MathFunc.FLOOR.toString())) {
      result = new BuiltInMath(BuiltInMath.MathFunc.FLOOR);
    } else if (name.equals(BuiltInMath.MathFunc.CEIL.toString())) {
      result = new BuiltInMath(BuiltInMath.MathFunc.CEIL);
    } else if (name.equals(BuiltInMath.MathFunc.ROUND.toString())) {
      result = new BuiltInMath(BuiltInMath.MathFunc.ROUND);
    } else if (name.equals(BuiltInString.StringFunc.SUBSTRING)) {
      result = new BuiltInString(BuiltInString.StringFunc.SUBSTRING);
    } else if (name.equals(BuiltInString.StringFunc.TRIMBLANKS)) {
      result = new BuiltInString(BuiltInString.StringFunc.TRIMBLANKS);
    }
    
    return result;
  }
  











  public static Function getFunction(String name, TransformationDictionary transDict)
    throws Exception
  {
    Function result = getFunction(name);
    

    if ((result == null) && (transDict != null)) {
      result = transDict.getFunction(name);
    }
    
    if (result == null) {
      throw new Exception("[Function] unknown/unsupported function " + name);
    }
    
    return result;
  }
  
  public String toString() {
    return toString("");
  }
  
  public String toString(String pad) {
    return pad + getClass().getName();
  }
}
