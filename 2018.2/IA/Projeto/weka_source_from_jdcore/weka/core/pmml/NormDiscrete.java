package weka.core.pmml;

import java.util.ArrayList;
import org.w3c.dom.Element;
import weka.core.Attribute;
import weka.core.Instance;








































public class NormDiscrete
  extends Expression
{
  private static final long serialVersionUID = -8854409417983908220L;
  protected String m_fieldName;
  protected Attribute m_field;
  protected int m_fieldIndex = -1;
  

  protected String m_fieldValue;
  

  protected boolean m_mapMissingDefined = false;
  



  protected double m_mapMissingTo;
  



  protected int m_fieldValueIndex = -1;
  











  public NormDiscrete(Element normDisc, FieldMetaInfo.Optype opType, ArrayList<Attribute> fieldDefs)
    throws Exception
  {
    super(opType, fieldDefs);
    
    if (opType != FieldMetaInfo.Optype.CONTINUOUS) {
      throw new Exception("[NormDiscrete] can only have a continuous optype");
    }
    
    m_fieldName = normDisc.getAttribute("field");
    m_fieldValue = normDisc.getAttribute("value");
    
    String mapMissing = normDisc.getAttribute("mapMissingTo");
    if ((mapMissing != null) && (mapMissing.length() > 0)) {
      m_mapMissingTo = Double.parseDouble(mapMissing);
      m_mapMissingDefined = true;
    }
    
    setUpField();
  }
  




  public void setFieldDefs(ArrayList<Attribute> fieldDefs)
    throws Exception
  {
    super.setFieldDefs(fieldDefs);
    setUpField();
  }
  



  private void setUpField()
    throws Exception
  {
    m_fieldIndex = -1;
    m_fieldValueIndex = -1;
    m_field = null;
    
    if (m_fieldDefs != null) {
      m_fieldIndex = getFieldDefIndex(m_fieldName);
      
      if (m_fieldIndex < 0) {
        throw new Exception("[NormDiscrete] Can't find field " + m_fieldName + " in the supplied field definitions.");
      }
      
      m_field = ((Attribute)m_fieldDefs.get(m_fieldIndex));
      
      if ((!m_field.isString()) && (!m_field.isNominal())) {
        throw new Exception("[NormDiscrete] reference field " + m_fieldName + " must be categorical");
      }
      

      if (m_field.isNominal())
      {
        m_fieldValueIndex = m_field.indexOfValue(m_fieldValue);
        if (m_fieldValueIndex < 0) {
          throw new Exception("[NormDiscrete] Unable to find value " + m_fieldValue + " in nominal attribute " + m_field.name());
        }
      }
      else if (m_field.isString())
      {

        m_fieldValueIndex = m_field.addStringValue(m_fieldValue);
      }
    }
  }
  






  protected Attribute getOutputDef()
  {
    return new Attribute(m_fieldName + "=" + m_fieldValue);
  }
  









  public double getResult(double[] incoming)
    throws Exception
  {
    double result = 0.0D;
    if (Instance.isMissingValue(incoming[m_fieldIndex])) {
      if (m_mapMissingDefined) {
        result = m_mapMissingTo;
      } else {
        result = incoming[m_fieldIndex];
      }
    }
    else if (m_fieldValueIndex == (int)incoming[m_fieldIndex]) {
      result = 1.0D;
    }
    

    return result;
  }
  





  public String getResultCategorical(double[] incoming)
    throws Exception
  {
    throw new Exception("[NormDiscrete] Can't return the result as a categorical value!");
  }
  
  public String toString(String pad) {
    StringBuffer buff = new StringBuffer();
    buff.append("NormDiscrete: " + m_fieldName + "=" + m_fieldValue);
    if (m_mapMissingDefined) {
      buff.append("\n" + pad + "map missing values to: " + m_mapMissingTo);
    }
    
    return buff.toString();
  }
}
