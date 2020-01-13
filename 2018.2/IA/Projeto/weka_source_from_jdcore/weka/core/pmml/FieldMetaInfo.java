package weka.core.pmml;

import java.io.Serializable;
import org.w3c.dom.Element;
import weka.core.Attribute;












































public abstract class FieldMetaInfo
  implements Serializable
{
  protected String m_fieldName;
  
  public static class Value
    implements Serializable
  {
    private static final long serialVersionUID = -3981030320273649739L;
    protected String m_value;
    protected String m_displayValue;
    
    public static enum Property
    {
      VALID("valid"), 
      INVALID("invalid"), 
      MISSING("missing");
      
      private final String m_stringVal;
      
      private Property(String name) {
        m_stringVal = name;
      }
      

      public String toString() { return m_stringVal; }
    }
    
    protected Property m_property = Property.VALID;
    




    protected Value(Element value)
      throws Exception
    {
      m_value = value.getAttribute("value");
      String displayV = value.getAttribute("displayValue");
      if ((displayV != null) && (displayV.length() > 0)) {
        m_displayValue = displayV;
      }
      String property = value.getAttribute("property");
      if ((property != null) && (property.length() > 0)) {
        for (Property p : Property.values()) {
          if (p.toString().equals(property)) {
            m_property = p;
            break;
          }
        }
      }
    }
    
    public String toString() {
      String retV = m_value;
      if (m_displayValue != null) {
        retV = retV + "(" + m_displayValue + "): " + m_property.toString();
      }
      return retV;
    }
    
    public String getValue() {
      return m_value;
    }
    
    public String getDisplayValue() {
      return m_displayValue;
    }
    
    public Property getProperty() {
      return m_property;
    }
  }
  




  public static class Interval
    implements Serializable
  {
    private static final long serialVersionUID = -7339790632684638012L;
    


    protected double m_leftMargin = Double.NEGATIVE_INFINITY;
    

    protected double m_rightMargin = Double.POSITIVE_INFINITY;
    


    public static enum Closure
    {
      OPENCLOSED("openClosed", "(", "]"), 
      OPENOPEN("openOpen", "(", ")"), 
      CLOSEDOPEN("closedOpen", "[", ")"), 
      CLOSEDCLOSED("closedClosed", "[", "]");
      
      private final String m_stringVal;
      private final String m_left;
      private final String m_right;
      
      private Closure(String name, String left, String right) {
        m_stringVal = name;
        m_left = left;
        m_right = right;
      }
      
      public String toString() {
        return m_stringVal;
      }
      

      public String toString(double leftMargin, double rightMargin) { return m_left + leftMargin + "-" + rightMargin + m_right; }
    }
    
    protected Closure m_closure = Closure.OPENOPEN;
    




    protected Interval(Element interval)
      throws Exception
    {
      String leftM = interval.getAttribute("leftMargin");
      try {
        m_leftMargin = Double.parseDouble(leftM);
      } catch (IllegalArgumentException ex) {
        throw new Exception("[Interval] Can't parse left margin as a number");
      }
      
      String rightM = interval.getAttribute("rightMargin");
      try {
        m_rightMargin = Double.parseDouble(rightM);
      } catch (IllegalArgumentException ex) {
        throw new Exception("[Interval] Can't parse right margin as a number");
      }
      
      String closure = interval.getAttribute("closure");
      if ((closure == null) || (closure.length() == 0)) {
        throw new Exception("[Interval] No closure specified!");
      }
      for (Closure c : Closure.values()) {
        if (c.toString().equals(closure)) {
          m_closure = c;
          break;
        }
      }
    }
    





    public boolean containsValue(double value)
    {
      boolean result = false;
      
      switch (FieldMetaInfo.1.$SwitchMap$weka$core$pmml$FieldMetaInfo$Interval$Closure[m_closure.ordinal()]) {
      case 1: 
        if ((value > m_leftMargin) && (value <= m_rightMargin)) {
          result = true;
        }
        break;
      case 2: 
        if ((value > m_leftMargin) && (value < m_rightMargin)) {
          result = true;
        }
        break;
      case 3: 
        if ((value >= m_leftMargin) && (value < m_rightMargin)) {
          result = true;
        }
        break;
      case 4: 
        if ((value >= m_leftMargin) && (value <= m_rightMargin)) {
          result = true;
        }
        break;
      default: 
        result = false;
      }
      
      
      return result;
    }
    
    public String toString() {
      return m_closure.toString(m_leftMargin, m_rightMargin);
    }
  }
  








  public static enum Optype
  {
    NONE("none"), 
    CONTINUOUS("continuous"), 
    CATEGORICAL("categorical"), 
    ORDINAL("ordinal");
    
    private final String m_stringVal;
    
    private Optype(String name) {
      m_stringVal = name;
    }
    
    public String toString() {
      return m_stringVal;
    }
  }
  

  protected Optype m_optype = Optype.NONE;
  




  public Optype getOptype()
  {
    return m_optype;
  }
  




  public String getFieldName()
  {
    return m_fieldName;
  }
  




  public FieldMetaInfo(Element field)
  {
    m_fieldName = field.getAttribute("name");
    
    String opType = field.getAttribute("optype");
    if ((opType != null) && (opType.length() > 0)) {
      for (Optype o : Optype.values()) {
        if (o.toString().equals(opType)) {
          m_optype = o;
          break;
        }
      }
    }
  }
  
  public abstract Attribute getFieldAsAttribute();
}
