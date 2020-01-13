package weka.core.pmml;

import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.core.Attribute;
import weka.core.FastVector;



































public class DefineFunction
  extends Function
{
  private static final long serialVersionUID = -1976646917527243888L;
  
  protected class ParameterField
    extends FieldMetaInfo
  {
    private static final long serialVersionUID = 3918895902507585558L;
    
    protected ParameterField(Element field)
    {
      super();
    }
    
    public Attribute getFieldAsAttribute() {
      if (m_optype == FieldMetaInfo.Optype.CONTINUOUS) {
        return new Attribute(m_fieldName);
      }
      
      return new Attribute(m_fieldName, (FastVector)null);
    }
  }
  




  protected ArrayList<ParameterField> m_parameters = new ArrayList();
  

  FieldMetaInfo.Optype m_optype = FieldMetaInfo.Optype.NONE;
  

  protected Expression m_expression = null;
  
  public DefineFunction(Element container, TransformationDictionary transDict) throws Exception
  {
    m_functionName = container.getAttribute("name");
    

    String opType = container.getAttribute("optype");
    if ((opType != null) && (opType.length() > 0)) {
      for (FieldMetaInfo.Optype o : FieldMetaInfo.Optype.values()) {
        if (o.toString().equals(opType)) {
          m_optype = o;
          break;
        }
      }
    } else {
      throw new Exception("[DefineFunction] no optype specified!!");
    }
    
    m_parameterDefs = new ArrayList();
    

    NodeList paramL = container.getElementsByTagName("ParameterField");
    for (int i = 0; i < paramL.getLength(); i++) {
      Node paramN = paramL.item(i);
      if (paramN.getNodeType() == 1) {
        ParameterField newP = new ParameterField((Element)paramN);
        m_parameters.add(newP);
        



        m_parameterDefs.add(newP.getFieldAsAttribute());
      }
    }
    
    m_expression = Expression.getExpression(container, m_optype, m_parameterDefs, transDict);
    

    if ((m_optype == FieldMetaInfo.Optype.CONTINUOUS) && (m_expression.getOptype() != m_optype))
    {
      throw new Exception("[DefineFunction] optype is continuous but our Expression's optype is not.");
    }
    

    if (((m_optype == FieldMetaInfo.Optype.CATEGORICAL) || (m_optype == FieldMetaInfo.Optype.ORDINAL) ? 1 : 0) != ((m_expression.getOptype() == FieldMetaInfo.Optype.CATEGORICAL) || (m_expression.getOptype() == FieldMetaInfo.Optype.ORDINAL) ? 1 : 0))
    {

      throw new Exception("[DefineFunction] optype is categorical/ordinal but our Expression's optype is not.");
    }
  }
  





  public Attribute getOutputDef()
  {
    return m_expression.getOutputDef();
  }
  







  public String[] getParameterNames()
  {
    String[] result = new String[m_parameters.size()];
    for (int i = 0; i < m_parameters.size(); i++) {
      result[i] = ((ParameterField)m_parameters.get(i)).getFieldName();
    }
    
    return result;
  }
  









  public double getResult(double[] incoming)
    throws Exception
  {
    if (incoming.length != m_parameters.size()) {
      throw new IllegalArgumentException("[DefineFunction] wrong number of arguments: expected " + m_parameters.size() + ", recieved " + incoming.length);
    }
    

    return m_expression.getResult(incoming);
  }
  






  public void setParameterDefs(ArrayList<Attribute> paramDefs)
    throws Exception
  {
    if (paramDefs.size() != m_parameters.size()) {
      throw new Exception("[DefineFunction] number of parameter definitions does not match number of parameters!");
    }
    


    for (int i = 0; i < m_parameters.size(); i++) {
      if (((ParameterField)m_parameters.get(i)).getOptype() == FieldMetaInfo.Optype.CONTINUOUS) {
        if (!((Attribute)paramDefs.get(i)).isNumeric()) {
          throw new Exception("[DefineFunction] parameter " + ((ParameterField)m_parameters.get(i)).getFieldName() + " is continuous, but corresponding " + "supplied parameter def " + ((Attribute)paramDefs.get(i)).name() + " is not!");
        }
        

      }
      else if ((!((Attribute)paramDefs.get(i)).isNominal()) && (!((Attribute)paramDefs.get(i)).isString())) {
        throw new Exception("[DefineFunction] parameter " + ((ParameterField)m_parameters.get(i)).getFieldName() + " is categorical/ordinal, but corresponding " + "supplied parameter def " + ((Attribute)paramDefs.get(i)).name() + " is not!");
      }
    }
    





    ArrayList<Attribute> newParamDefs = new ArrayList();
    for (int i = 0; i < paramDefs.size(); i++) {
      Attribute a = (Attribute)paramDefs.get(i);
      newParamDefs.add(a.copy(((ParameterField)m_parameters.get(i)).getFieldName()));
    }
    
    m_parameterDefs = newParamDefs;
    

    m_expression.setFieldDefs(m_parameterDefs);
  }
  
  public String toString() {
    return toString("");
  }
  
  public String toString(String pad) {
    StringBuffer buff = new StringBuffer();
    
    buff.append(pad + "DefineFunction (" + m_functionName + "):\n" + pad + "nparameters:\n");
    

    for (ParameterField p : m_parameters) {
      buff.append(pad + p.getFieldAsAttribute() + "\n");
    }
    
    buff.append(pad + "expression:\n" + m_expression.toString(new StringBuilder().append(pad).append("  ").toString()));
    return buff.toString();
  }
}
