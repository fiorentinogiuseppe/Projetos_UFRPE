package weka.core.pmml;

import java.io.Serializable;
import org.w3c.dom.Element;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;





























public class MiningFieldMetaInfo
  extends FieldMetaInfo
  implements Serializable
{
  private static final long serialVersionUID = -1256774332779563185L;
  
  static enum Usage
  {
    ACTIVE("active"), 
    PREDICTED("predicted"), 
    SUPPLEMENTARY("supplementary"), 
    GROUP("group"), 
    ORDER("order");
    
    private final String m_stringVal;
    
    private Usage(String name) { m_stringVal = name; }
    
    public String toString()
    {
      return m_stringVal;
    }
  }
  

  Usage m_usageType = Usage.ACTIVE;
  
  static enum Outlier {
    ASIS("asIs"), 
    ASMISSINGVALUES("asMissingValues"), 
    ASEXTREMEVALUES("asExtremeValues");
    
    private final String m_stringVal;
    
    private Outlier(String name) { m_stringVal = name; }
    
    public String toString()
    {
      return m_stringVal;
    }
  }
  
  protected Outlier m_outlierTreatmentMethod = Outlier.ASIS;
  
  protected double m_lowValue;
  
  protected double m_highValue;
  
  static enum Missing
  {
    ASIS("asIs"), 
    ASMEAN("asMean"), 
    ASMODE("asMode"), 
    ASMEDIAN("asMedian"), 
    ASVALUE("asValue");
    
    private final String m_stringVal;
    
    private Missing(String name) { m_stringVal = name; }
    
    public String toString()
    {
      return m_stringVal;
    }
  }
  
  protected Missing m_missingValueTreatmentMethod = Missing.ASIS;
  

  protected String m_missingValueReplacementNominal;
  

  protected double m_missingValueReplacementNumeric;
  
  protected FieldMetaInfo.Optype m_optypeOverride = FieldMetaInfo.Optype.NONE;
  

  protected int m_index;
  

  protected double m_importance;
  

  Instances m_miningSchemaI = null;
  








  protected void setMiningSchemaInstances(Instances miningSchemaI)
  {
    m_miningSchemaI = miningSchemaI;
  }
  




  public Usage getUsageType()
  {
    return m_usageType;
  }
  




  public String toString()
  {
    StringBuffer temp = new StringBuffer();
    temp.append(m_miningSchemaI.attribute(m_index));
    temp.append("\n\tusage: " + m_usageType + "\n\toutlier treatment: " + m_outlierTreatmentMethod);
    
    if (m_outlierTreatmentMethod == Outlier.ASEXTREMEVALUES) {
      temp.append(" (lowValue = " + m_lowValue + " highValue = " + m_highValue + ")");
    }
    
    temp.append("\n\tmissing value treatment: " + m_missingValueTreatmentMethod);
    
    if (m_missingValueTreatmentMethod != Missing.ASIS) {
      temp.append(" (replacementValue = " + (m_missingValueReplacementNominal != null ? m_missingValueReplacementNominal : Utils.doubleToString(m_missingValueReplacementNumeric, 4)) + ")");
    }
    




    return temp.toString();
  }
  





  public void setIndex(int index)
  {
    m_index = index;
  }
  




  public String getName()
  {
    return m_fieldName;
  }
  




  public Outlier getOutlierTreatmentMethod()
  {
    return m_outlierTreatmentMethod;
  }
  




  public Missing getMissingValueTreatmentMethod()
  {
    return m_missingValueTreatmentMethod;
  }
  





  public double applyMissingValueTreatment(double value)
    throws Exception
  {
    double newVal = value;
    if ((m_missingValueTreatmentMethod != Missing.ASIS) && (Instance.isMissingValue(value)))
    {
      if (m_missingValueReplacementNominal != null) {
        Attribute att = m_miningSchemaI.attribute(m_index);
        int valIndex = att.indexOfValue(m_missingValueReplacementNominal);
        if (valIndex < 0) {
          throw new Exception("[MiningSchema] Nominal missing value replacement value doesn't exist in the mining schema Instances!");
        }
        
        newVal = valIndex;
      } else {
        newVal = m_missingValueReplacementNumeric;
      }
    }
    return newVal;
  }
  





  public double applyOutlierTreatment(double value)
    throws Exception
  {
    double newVal = value;
    if (m_outlierTreatmentMethod != Outlier.ASIS) {
      if (m_outlierTreatmentMethod == Outlier.ASMISSINGVALUES) {
        newVal = applyMissingValueTreatment(value);
      }
      else if (value < m_lowValue) {
        newVal = m_lowValue;
      } else if (value > m_highValue) {
        newVal = m_highValue;
      }
    }
    
    return newVal;
  }
  




  public Attribute getFieldAsAttribute()
  {
    return m_miningSchemaI.attribute(m_index);
  }
  



  public MiningFieldMetaInfo(Element field)
    throws Exception
  {
    super(field);
    


    String usage = field.getAttribute("usageType");
    for (Usage u : Usage.values()) {
      if (u.toString().equals(usage)) {
        m_usageType = u;
        break;
      }
    }
    













    String importance = field.getAttribute("importance");
    if (importance.length() > 0) {
      m_importance = Double.parseDouble(importance);
    }
    

    String outliers = field.getAttribute("outliers");
    for (Outlier o : Outlier.values()) {
      if (o.toString().equals(outliers)) {
        m_outlierTreatmentMethod = o;
        break;
      }
    }
    
    if ((outliers.length() > 0) && (m_outlierTreatmentMethod == Outlier.ASEXTREMEVALUES))
    {
      String lowValue = field.getAttribute("lowValue");
      if (lowValue.length() > 0) {
        m_lowValue = Double.parseDouble(lowValue);
      } else {
        throw new Exception("[MiningFieldMetaInfo] as extreme values outlier treatment specified, but no low value defined!");
      }
      
      String highValue = field.getAttribute("highValue");
      if (highValue.length() > 0) {
        m_highValue = Double.parseDouble(highValue);
      } else {
        throw new Exception("[MiningFieldMetaInfo] as extreme values outlier treatment specified, but no high value defined!");
      }
    }
    



    String missingReplacement = field.getAttribute("missingValueReplacement");
    if (missingReplacement.length() > 0)
    {
      try {
        m_missingValueReplacementNumeric = Double.parseDouble(missingReplacement);
      }
      catch (IllegalArgumentException ex) {
        m_missingValueReplacementNominal = missingReplacement;
      }
      

      String missingTreatment = field.getAttribute("missingValueTreatment");
      for (Missing m : Missing.values()) {
        if (m.toString().equals(missingTreatment)) {
          m_missingValueTreatmentMethod = m;
          break;
        }
      }
    }
  }
}
