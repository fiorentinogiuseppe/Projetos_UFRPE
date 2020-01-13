package weka.core.pmml;

import java.io.Serializable;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.core.Attribute;


























public class DerivedFieldMetaInfo
  extends FieldMetaInfo
  implements Serializable
{
  protected String m_displayName = null;
  





  protected ArrayList<FieldMetaInfo.Value> m_values = new ArrayList();
  
  protected Expression m_expression;
  
  public DerivedFieldMetaInfo(Element derivedField, ArrayList<Attribute> fieldDefs, TransformationDictionary transDict)
    throws Exception
  {
    super(derivedField);
    
    String displayName = derivedField.getAttribute("displayName");
    if ((displayName != null) && (displayName.length() > 0)) {
      m_displayName = displayName;
    }
    

    NodeList valL = derivedField.getElementsByTagName("Value");
    if (valL.getLength() > 0) {
      for (int i = 0; i < valL.getLength(); i++) {
        Node valueN = valL.item(i);
        if (valueN.getNodeType() == 1) {
          FieldMetaInfo.Value v = new FieldMetaInfo.Value((Element)valueN);
          m_values.add(v);
        }
      }
    }
    

    m_expression = Expression.getExpression(derivedField, m_optype, fieldDefs, transDict);
  }
  




  public void setFieldDefs(ArrayList<Attribute> fieldDefs)
    throws Exception
  {
    m_expression.setFieldDefs(fieldDefs);
  }
  




  public Attribute getFieldAsAttribute()
  {
    return m_expression.getOutputDef().copy(m_fieldName);
  }
  













  public double getDerivedValue(double[] incoming)
    throws Exception
  {
    return m_expression.getResult(incoming);
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append(getFieldAsAttribute() + "\nexpression:\n");
    buff.append(m_expression + "\n");
    
    return buff.toString();
  }
}
