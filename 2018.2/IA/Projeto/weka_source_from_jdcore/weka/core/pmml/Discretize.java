package weka.core.pmml;

import java.io.Serializable;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;


































public class Discretize
  extends Expression
{
  protected String m_fieldName;
  protected int m_fieldIndex;
  
  protected class DiscretizeBin
    implements Serializable
  {
    private static final long serialVersionUID = 5810063243316808400L;
    private ArrayList<FieldMetaInfo.Interval> m_intervals = new ArrayList();
    
    private String m_binValue;
    
    protected DiscretizeBin(Element bin)
      throws Exception
    {
      NodeList iL = bin.getElementsByTagName("Interval");
      for (int i = 0; i < iL.getLength(); i++) {
        Node iN = iL.item(i);
        if (iN.getNodeType() == 1) {
          FieldMetaInfo.Interval tempInterval = new FieldMetaInfo.Interval((Element)iN);
          m_intervals.add(tempInterval);
        }
      }
      
      m_binValue = bin.getAttribute("binValue");
    }
    




    protected String getBinValue()
    {
      return m_binValue;
    }
    






    protected boolean containsValue(double value)
    {
      boolean result = false;
      
      for (FieldMetaInfo.Interval i : m_intervals) {
        if (i.containsValue(value)) {
          result = true;
          break;
        }
      }
      
      return result;
    }
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      
      buff.append("\"" + m_binValue + "\" if value in: ");
      boolean first = true;
      for (FieldMetaInfo.Interval i : m_intervals) {
        if (!first) {
          buff.append(", ");
        } else {
          first = false;
        }
        buff.append(i.toString());
      }
      
      return buff.toString();
    }
  }
  








  protected boolean m_mapMissingDefined = false;
  

  protected String m_mapMissingTo;
  

  protected boolean m_defaultValueDefined = false;
  

  protected String m_defaultValue;
  

  protected ArrayList<DiscretizeBin> m_bins = new ArrayList();
  




  protected Attribute m_outputDef;
  





  public Discretize(Element discretize, FieldMetaInfo.Optype opType, ArrayList<Attribute> fieldDefs)
    throws Exception
  {
    super(opType, fieldDefs);
    
    if (opType == FieldMetaInfo.Optype.CONTINUOUS) {
      throw new Exception("[Discretize] must have a categorical or ordinal optype");
    }
    
    m_fieldName = discretize.getAttribute("field");
    
    m_mapMissingTo = discretize.getAttribute("mapMissingTo");
    if ((m_mapMissingTo != null) && (m_mapMissingTo.length() > 0)) {
      m_mapMissingDefined = true;
    }
    
    m_defaultValue = discretize.getAttribute("defaultValue");
    if ((m_defaultValue != null) && (m_defaultValue.length() > 0)) {
      m_defaultValueDefined = true;
    }
    

    NodeList dbL = discretize.getElementsByTagName("DiscretizeBin");
    for (int i = 0; i < dbL.getLength(); i++) {
      Node dbN = dbL.item(i);
      if (dbN.getNodeType() == 1) {
        Element dbE = (Element)dbN;
        DiscretizeBin db = new DiscretizeBin(dbE);
        m_bins.add(db);
      }
    }
    
    setUpField();
  }
  




  public void setFieldDefs(ArrayList<Attribute> fieldDefs)
    throws Exception
  {
    super.setFieldDefs(fieldDefs);
    setUpField();
  }
  
  private void setUpField() throws Exception {
    m_fieldIndex = -1;
    
    if (m_fieldDefs != null) {
      m_fieldIndex = getFieldDefIndex(m_fieldName);
      if (m_fieldIndex < 0) {
        throw new Exception("[Discretize] Can't find field " + m_fieldName + " in the supplied field definitions.");
      }
      

      Attribute field = (Attribute)m_fieldDefs.get(m_fieldIndex);
      if (!field.isNumeric()) {
        throw new Exception("[Discretize] reference field " + m_fieldName + " must be continuous.");
      }
    }
    


    Attribute tempAtt = new Attribute("temp", (FastVector)null);
    for (DiscretizeBin d : m_bins) {
      tempAtt.addStringValue(d.getBinValue());
    }
    


    if (m_defaultValueDefined) {
      tempAtt.addStringValue(m_defaultValue);
    }
    


    if (m_mapMissingDefined) {
      tempAtt.addStringValue(m_mapMissingTo);
    }
    

    FastVector values = new FastVector();
    for (int i = 0; i < tempAtt.numValues(); i++) {
      values.addElement(tempAtt.value(i));
    }
    
    m_outputDef = new Attribute(m_fieldName + "_discretized", values);
  }
  






  protected Attribute getOutputDef()
  {
    return m_outputDef;
  }
  











  public double getResult(double[] incoming)
    throws Exception
  {
    double result = Instance.missingValue();
    
    double value = incoming[m_fieldIndex];
    
    if (Instance.isMissingValue(value)) {
      if (m_mapMissingDefined) {
        result = m_outputDef.indexOfValue(m_mapMissingTo);
      }
    }
    else {
      boolean found = false;
      for (DiscretizeBin b : m_bins) {
        if (b.containsValue(value)) {
          found = true;
          result = m_outputDef.indexOfValue(b.getBinValue());
          break;
        }
      }
      
      if ((!found) && 
        (m_defaultValueDefined)) {
        result = m_outputDef.indexOfValue(m_defaultValue);
      }
    }
    

    return result;
  }
  







  public String getResultCategorical(double[] incoming)
    throws Exception
  {
    double index = getResult(incoming);
    if (Instance.isMissingValue(index)) {
      return "**Missing Value**";
    }
    
    return m_outputDef.value((int)index);
  }
  
  public String toString(String pad) {
    StringBuffer buff = new StringBuffer();
    
    buff.append(pad + "Discretize (" + m_fieldName + "):");
    for (DiscretizeBin d : m_bins) {
      buff.append("\n" + pad + d.toString());
    }
    
    if (m_mapMissingDefined) {
      buff.append("\n" + pad + "map missing values to: " + m_mapMissingTo);
    }
    
    if (m_defaultValueDefined) {
      buff.append("\n" + pad + "defautl value: " + m_defaultValue);
    }
    
    return buff.toString();
  }
}
