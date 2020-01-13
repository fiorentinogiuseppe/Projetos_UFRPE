package weka.core.pmml;

import java.util.ArrayList;
import org.w3c.dom.Element;
import weka.core.Attribute;



































public class FieldRef
  extends Expression
{
  private static final long serialVersionUID = -8009605897876168409L;
  protected String m_fieldName = null;
  
  public FieldRef(Element fieldRef, FieldMetaInfo.Optype opType, ArrayList<Attribute> fieldDefs) throws Exception
  {
    super(opType, fieldDefs);
    
    m_fieldName = fieldRef.getAttribute("field");
    validateField();
  }
  
  public void setFieldDefs(ArrayList<Attribute> fieldDefs) throws Exception {
    super.setFieldDefs(fieldDefs);
    validateField();
  }
  
  protected void validateField() throws Exception
  {
    if (m_fieldDefs != null) {
      Attribute a = getFieldDef(m_fieldName);
      if (a == null) {
        throw new Exception("[FieldRef] Can't find field " + m_fieldName + " in the supplied field definitions");
      }
      
      if (((m_opType == FieldMetaInfo.Optype.CATEGORICAL) || (m_opType == FieldMetaInfo.Optype.ORDINAL)) && (a.isNumeric()))
      {
        throw new IllegalArgumentException("[FieldRef] Optype is categorical/ordinal but matching parameter in the field definitions is not!");
      }
      

      if ((m_opType == FieldMetaInfo.Optype.CONTINUOUS) && (a.isNominal())) {
        throw new IllegalArgumentException("[FieldRef] Optype is continuous but matching parameter in the field definitions is not!");
      }
    }
  }
  

  public double getResult(double[] incoming)
    throws Exception
  {
    double result = NaN.0D;
    boolean found = false;
    
    for (int i = 0; i < m_fieldDefs.size(); i++) {
      Attribute a = (Attribute)m_fieldDefs.get(i);
      if (a.name().equals(m_fieldName)) {
        if (a.isNumeric()) {
          if ((m_opType == FieldMetaInfo.Optype.CATEGORICAL) || (m_opType == FieldMetaInfo.Optype.ORDINAL))
          {
            throw new IllegalArgumentException("[FieldRef] Optype is categorical/ordinal but matching parameter is not!");
          }
        }
        else if (a.isNominal()) {
          if (m_opType == FieldMetaInfo.Optype.CONTINUOUS) {
            throw new IllegalArgumentException("[FieldRef] Optype is continuous but matching parameter is not!");
          }
        }
        else {
          throw new IllegalArgumentException("[FieldRef] Unhandled attribute type");
        }
        result = incoming[i];
        found = true;
        break;
      }
    }
    
    if (!found) {
      throw new Exception("[FieldRef] this field: " + m_fieldName + " is not in the supplied " + "list of parameters!");
    }
    
    return result;
  }
  

  public String getResultCategorical(double[] incoming)
    throws Exception
  {
    if (m_opType == FieldMetaInfo.Optype.CONTINUOUS) {
      throw new IllegalArgumentException("[FieldRef] Can't return result as categorical/ordinal because optype is continuous!");
    }
    

    boolean found = false;
    String result = null;
    
    for (int i = 0; i < m_fieldDefs.size(); i++) {
      Attribute a = (Attribute)m_fieldDefs.get(i);
      if (a.name().equals(m_fieldName)) {
        found = true;
        result = a.value((int)incoming[i]);
        break;
      }
    }
    
    if (!found) {
      throw new Exception("[FieldRef] this field: " + m_fieldName + " is not in the supplied " + "list of parameters!");
    }
    
    return result;
  }
  







  public Attribute getOutputDef()
  {
    Attribute a = getFieldDef(m_fieldName);
    if (a != null) {
      return a;
    }
    




    return null;
  }
  
  public String toString(String pad) {
    return pad + "FieldRef: " + m_fieldName;
  }
}
