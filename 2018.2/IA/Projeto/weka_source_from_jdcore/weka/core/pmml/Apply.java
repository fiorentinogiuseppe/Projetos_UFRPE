package weka.core.pmml;

import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.core.Attribute;


































class Apply
  extends Expression
{
  private static final long serialVersionUID = -2790648331300695083L;
  protected ArrayList<Expression> m_arguments = new ArrayList();
  

  protected Function m_function = null;
  

  protected Attribute m_outputStructure = null;
  













  protected Apply(Element apply, FieldMetaInfo.Optype opType, ArrayList<Attribute> fieldDefs, TransformationDictionary transDict)
    throws Exception
  {
    super(opType, fieldDefs);
    
    String functionName = apply.getAttribute("function");
    if ((functionName == null) || (functionName.length() == 0))
    {


      functionName = apply.getAttribute("name");
    }
    
    if ((functionName == null) || (functionName.length() == 0)) {
      throw new Exception("[Apply] No function name specified!!");
    }
    
    m_function = Function.getFunction(functionName, transDict);
    

    NodeList children = apply.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == 1) {
        String tagName = ((Element)child).getTagName();
        if (!tagName.equals("Extension"))
        {
          Expression tempExpression = Expression.getExpression(tagName, child, m_opType, m_fieldDefs, transDict);
          
          if (tempExpression != null) {
            m_arguments.add(tempExpression);
          }
        }
      }
    }
    
    updateDefsForArgumentsAndFunction();
  }
  
  public void setFieldDefs(ArrayList<Attribute> fieldDefs) throws Exception {
    super.setFieldDefs(fieldDefs);
    
    updateDefsForArgumentsAndFunction();
  }
  
  private void updateDefsForArgumentsAndFunction() throws Exception {
    for (int i = 0; i < m_arguments.size(); i++) {
      ((Expression)m_arguments.get(i)).setFieldDefs(m_fieldDefs);
    }
    


    ArrayList<Attribute> functionFieldDefs = new ArrayList(m_arguments.size());
    for (int i = 0; i < m_arguments.size(); i++) {
      functionFieldDefs.add(((Expression)m_arguments.get(i)).getOutputDef());
    }
    m_function.setParameterDefs(functionFieldDefs);
    m_outputStructure = m_function.getOutputDef();
  }
  












  public double getResult(double[] incoming)
    throws Exception
  {
    double[] functionIncoming = new double[m_arguments.size()];
    
    for (int i = 0; i < m_arguments.size(); i++) {
      functionIncoming[i] = ((Expression)m_arguments.get(i)).getResult(incoming);
    }
    

    double result = m_function.getResult(functionIncoming);
    
    return result;
  }
  









  public String getResultCategorical(double[] incoming)
    throws Exception
  {
    if (m_opType == FieldMetaInfo.Optype.CONTINUOUS) {
      throw new IllegalArgumentException("[Apply] Can't return result as categorical/ordinal because optype is continuous!");
    }
    

    double result = getResult(incoming);
    return m_outputStructure.value((int)result);
  }
  






  public Attribute getOutputDef()
  {
    return m_outputStructure;
  }
  
  public String toString(String pad) {
    StringBuffer buff = new StringBuffer();
    


    String[] parameterNames = null;
    
    buff.append(pad + "Apply [" + m_function.toString() + "]:\n");
    buff.append(pad + "args:");
    if ((m_function instanceof DefineFunction)) {
      parameterNames = m_function.getParameterNames();
    }
    for (int i = 0; i < m_arguments.size(); i++) {
      Expression e = (Expression)m_arguments.get(i);
      buff.append("\n" + (parameterNames != null ? pad + parameterNames[i] + " = " : "") + e.toString(new StringBuilder().append(pad).append("  ").toString()));
    }
    




    return buff.toString();
  }
}
