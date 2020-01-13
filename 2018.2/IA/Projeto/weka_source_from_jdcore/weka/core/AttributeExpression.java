package weka.core;

import java.io.Serializable;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;



















































public class AttributeExpression
  implements Serializable, RevisionHandler
{
  static final long serialVersionUID = 402130123261736245L;
  public AttributeExpression() {}
  
  private class AttributeOperand
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = -7674280127286031105L;
    protected int m_attributeIndex;
    protected boolean m_negative;
    
    public AttributeOperand(String operand, boolean sign)
      throws Exception
    {
      m_attributeIndex = (Integer.parseInt(operand.substring(1)) - 1);
      m_negative = sign;
    }
    



    public String toString()
    {
      String result = "";
      if (m_negative) {
        result = result + '-';
      }
      return result + "a" + (m_attributeIndex + 1);
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5989 $");
    }
  }
  




  private class NumericOperand
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = 9037007836243662859L;
    



    protected double m_numericConst;
    



    public NumericOperand(String operand, boolean sign)
      throws Exception
    {
      m_numericConst = Double.valueOf(operand).doubleValue();
      if (sign) {
        m_numericConst *= -1.0D;
      }
    }
    



    public String toString()
    {
      return "" + m_numericConst;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5989 $");
    }
  }
  




  private class Operator
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = -2760353522666004638L;
    


    protected char m_operator;
    



    public Operator(char opp)
    {
      if (!AttributeExpression.this.isOperator(opp)) {
        throw new IllegalArgumentException("Unrecognized operator:" + opp);
      }
      m_operator = opp;
    }
    





    protected double applyOperator(double first, double second)
    {
      switch (m_operator) {
      case '+': 
        return first + second;
      case '-': 
        return first - second;
      case '*': 
        return first * second;
      case '/': 
        return first / second;
      case '^': 
        return Math.pow(first, second);
      }
      return NaN.0D;
    }
    




    protected double applyFunction(double value)
    {
      switch (m_operator) {
      case 'l': 
        return Math.log(value);
      case 'b': 
        return Math.abs(value);
      case 'c': 
        return Math.cos(value);
      case 'e': 
        return Math.exp(value);
      case 's': 
        return Math.sqrt(value);
      case 'f': 
        return Math.floor(value);
      case 'h': 
        return Math.ceil(value);
      case 'r': 
        return Math.rint(value);
      case 't': 
        return Math.tan(value);
      case 'n': 
        return Math.sin(value);
      }
      return NaN.0D;
    }
    



    public String toString()
    {
      return "" + m_operator;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5989 $");
    }
  }
  

  private Stack m_operatorStack = new Stack();
  


  private static final String OPERATORS = "+-*/()^lbcesfhrtn";
  

  private static final String UNARY_FUNCTIONS = "lbcesfhrtn";
  

  private String m_originalInfix;
  

  private Vector m_postFixExpVector;
  

  private boolean m_signMod = false;
  

  private String m_previousTok = "";
  




  private void handleOperand(String tok)
    throws Exception
  {
    if (tok.indexOf('a') != -1) {
      m_postFixExpVector.addElement(new AttributeOperand(tok, m_signMod));
    } else {
      try
      {
        m_postFixExpVector.addElement(new NumericOperand(tok, m_signMod));
      } catch (NumberFormatException ne) {
        throw new Exception("Trouble parsing numeric constant");
      }
    }
    m_signMod = false;
  }
  



  private void handleOperator(String tok)
    throws Exception
  {
    boolean push = true;
    
    char tokchar = tok.charAt(0);
    if (tokchar == ')') {
      String popop = " ";
      do {
        popop = (String)m_operatorStack.pop();
        if (popop.charAt(0) != '(') {
          m_postFixExpVector.addElement(new Operator(popop.charAt(0)));
        }
      } while (popop.charAt(0) != '(');
    } else {
      int infixToc = infixPriority(tok.charAt(0));
      while ((!m_operatorStack.empty()) && (stackPriority(((String)m_operatorStack.peek()).charAt(0)) >= infixToc))
      {




        if ((m_previousTok.length() == 1) && (isOperator(m_previousTok.charAt(0))) && (m_previousTok.charAt(0) != ')'))
        {

          if (tok.charAt(0) == '-') {
            m_signMod = true;
          } else {
            m_signMod = false;
          }
          push = false;
          break;
        }
        String popop = (String)m_operatorStack.pop();
        m_postFixExpVector.addElement(new Operator(popop.charAt(0)));
      }
      
      if ((m_postFixExpVector.size() == 0) && 
        (tok.charAt(0) == '-')) {
        m_signMod = true;
        push = false;
      }
      

      if (push) {
        m_operatorStack.push(tok);
      }
    }
  }
  





  public void convertInfixToPostfix(String infixExp)
    throws Exception
  {
    m_originalInfix = infixExp;
    
    infixExp = Utils.removeSubstring(infixExp, " ");
    infixExp = Utils.replaceSubstring(infixExp, "log", "l");
    infixExp = Utils.replaceSubstring(infixExp, "abs", "b");
    infixExp = Utils.replaceSubstring(infixExp, "cos", "c");
    infixExp = Utils.replaceSubstring(infixExp, "exp", "e");
    infixExp = Utils.replaceSubstring(infixExp, "sqrt", "s");
    infixExp = Utils.replaceSubstring(infixExp, "floor", "f");
    infixExp = Utils.replaceSubstring(infixExp, "ceil", "h");
    infixExp = Utils.replaceSubstring(infixExp, "rint", "r");
    infixExp = Utils.replaceSubstring(infixExp, "tan", "t");
    infixExp = Utils.replaceSubstring(infixExp, "sin", "n");
    
    StringTokenizer tokenizer = new StringTokenizer(infixExp, "+-*/()^lbcesfhrtn", true);
    m_postFixExpVector = new Vector();
    
    while (tokenizer.hasMoreTokens()) {
      String tok = tokenizer.nextToken();
      
      if (tok.length() > 1) {
        handleOperand(tok);

      }
      else if (isOperator(tok.charAt(0))) {
        handleOperator(tok);
      }
      else {
        handleOperand(tok);
      }
      
      m_previousTok = tok;
    }
    while (!m_operatorStack.empty()) {
      String popop = (String)m_operatorStack.pop();
      if ((popop.charAt(0) == '(') || (popop.charAt(0) == ')')) {
        throw new Exception("Mis-matched parenthesis!");
      }
      m_postFixExpVector.addElement(new Operator(popop.charAt(0)));
    }
  }
  








  public double evaluateExpression(Instance instance)
    throws Exception
  {
    double[] vals = new double[instance.numAttributes() + 1];
    for (int i = 0; i < instance.numAttributes(); i++) {
      if (instance.isMissing(i)) {
        vals[i] = Instance.missingValue();
      } else {
        vals[i] = instance.value(i);
      }
    }
    
    evaluateExpression(vals);
    return vals[(vals.length - 1)];
  }
  






  public void evaluateExpression(double[] vals)
    throws Exception
  {
    Stack operands = new Stack();
    
    for (int i = 0; i < m_postFixExpVector.size(); i++) {
      Object nextob = m_postFixExpVector.elementAt(i);
      if ((nextob instanceof NumericOperand)) {
        operands.push(new Double(m_numericConst));
      } else if ((nextob instanceof AttributeOperand)) {
        double value = vals[m_attributeIndex];
        



        if (m_negative) {
          value = -value;
        }
        operands.push(new Double(value));
      } else if ((nextob instanceof Operator)) {
        char op = m_operator;
        if (isUnaryFunction(op)) {
          double operand = ((Double)operands.pop()).doubleValue();
          double result = ((Operator)nextob).applyFunction(operand);
          operands.push(new Double(result));
        } else {
          double second = ((Double)operands.pop()).doubleValue();
          double first = ((Double)operands.pop()).doubleValue();
          double result = ((Operator)nextob).applyOperator(first, second);
          operands.push(new Double(result));
        }
      } else {
        throw new Exception("Unknown object in postfix vector!");
      }
    }
    
    if (operands.size() != 1) {
      throw new Exception("Problem applying function");
    }
    
    Double result = (Double)operands.pop();
    if ((result.isNaN()) || (result.isInfinite())) {
      vals[(vals.length - 1)] = Instance.missingValue();
    } else {
      vals[(vals.length - 1)] = result.doubleValue();
    }
  }
  




  private boolean isOperator(char tok)
  {
    if ("+-*/()^lbcesfhrtn".indexOf(tok) == -1) {
      return false;
    }
    
    return true;
  }
  




  private boolean isUnaryFunction(char tok)
  {
    if ("lbcesfhrtn".indexOf(tok) == -1) {
      return false;
    }
    
    return true;
  }
  




  private int infixPriority(char opp)
  {
    switch (opp) {
    case 'b': 
    case 'c': 
    case 'e': 
    case 'f': 
    case 'h': 
    case 'l': 
    case 'n': 
    case 'r': 
    case 's': 
    case 't': 
      return 3;
    case '^': 
      return 2;
    case '*': 
      return 2;
    case '/': 
      return 2;
    case '+': 
      return 1;
    case '-': 
      return 1;
    case '(': 
      return 4;
    case ')': 
      return 0;
    }
    throw new IllegalArgumentException("Unrecognized operator:" + opp);
  }
  





  private int stackPriority(char opp)
  {
    switch (opp) {
    case 'b': 
    case 'c': 
    case 'e': 
    case 'f': 
    case 'h': 
    case 'l': 
    case 'n': 
    case 'r': 
    case 's': 
    case 't': 
      return 3;
    case '^': 
      return 2;
    case '*': 
      return 2;
    case '/': 
      return 2;
    case '+': 
      return 1;
    case '-': 
      return 1;
    case '(': 
      return 0;
    case ')': 
      return -1;
    }
    throw new IllegalArgumentException("Unrecognized operator:" + opp);
  }
  





  public String getPostFixExpression()
  {
    return m_postFixExpVector.toString();
  }
  
  public String toString() {
    return m_originalInfix;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5989 $");
  }
}
