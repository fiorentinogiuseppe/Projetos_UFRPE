package weka.core.pmml;

import java.io.Serializable;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
































public class TargetMetaInfo
  extends FieldMetaInfo
  implements Serializable
{
  private static final long serialVersionUID = 863500462237904927L;
  protected double m_min = NaN.0D;
  protected double m_max = NaN.0D;
  

  protected double m_rescaleConstant = 0.0D;
  protected double m_rescaleFactor = 1.0D;
  

  protected String m_castInteger = "";
  


  protected double[] m_defaultValueOrPriorProbs;
  


  protected ArrayList<String> m_values = new ArrayList();
  

  protected ArrayList<String> m_displayValues = new ArrayList();
  







  protected TargetMetaInfo(Element target)
    throws Exception
  {
    super(target);
    












    String min = target.getAttribute("min");
    if ((min != null) && (min.length() > 0)) {
      try {
        m_min = Double.parseDouble(min);
      } catch (IllegalArgumentException ex) {
        throw new Exception("[TargetMetaInfo] can't parse min value for target field " + m_fieldName);
      }
    }
    

    String max = target.getAttribute("max");
    if ((max != null) && (max.length() > 0)) {
      try {
        m_max = Double.parseDouble(max);
      } catch (IllegalArgumentException ex) {
        throw new Exception("[TargetMetaInfo] can't parse max value for target field " + m_fieldName);
      }
    }
    


    String rsc = target.getAttribute("rescaleConstant");
    if ((rsc != null) && (rsc.length() > 0)) {
      try {
        m_rescaleConstant = Double.parseDouble(rsc);
      } catch (IllegalArgumentException ex) {
        throw new Exception("[TargetMetaInfo] can't parse rescale constant value for target field " + m_fieldName);
      }
    }
    
    String rsf = target.getAttribute("rescaleFactor");
    if ((rsf != null) && (rsf.length() > 0)) {
      try {
        m_rescaleFactor = Double.parseDouble(rsf);
      } catch (IllegalArgumentException ex) {
        throw new Exception("[TargetMetaInfo] can't parse rescale factor value for target field " + m_fieldName);
      }
    }
    


    String cstI = target.getAttribute("castInteger");
    if ((cstI != null) && (cstI.length() > 0)) {
      m_castInteger = cstI;
    }
    


    NodeList vals = target.getElementsByTagName("TargetValue");
    if (vals.getLength() > 0) {
      m_defaultValueOrPriorProbs = new double[vals.getLength()];
      
      for (int i = 0; i < vals.getLength(); i++) {
        Node value = vals.item(i);
        if (value.getNodeType() == 1) {
          Element valueE = (Element)value;
          String valueName = valueE.getAttribute("value");
          if ((valueName != null) && (valueName.length() > 0))
          {

            if ((m_optype != FieldMetaInfo.Optype.CATEGORICAL) && (m_optype != FieldMetaInfo.Optype.NONE))
            {
              throw new Exception("[TargetMetaInfo] TargetValue element has categorical value but optype is not categorical!");
            }
            

            if (m_optype == FieldMetaInfo.Optype.NONE) {
              m_optype = FieldMetaInfo.Optype.CATEGORICAL;
            }
            
            m_values.add(valueName);
            
            String displayValue = valueE.getAttribute("displayValue");
            if ((displayValue != null) && (displayValue.length() > 0)) {
              m_displayValues.add(displayValue);
            }
            else {
              m_displayValues.add(valueName);
            }
            

            String prior = valueE.getAttribute("priorProbability");
            if ((prior != null) && (prior.length() > 0)) {
              try {
                m_defaultValueOrPriorProbs[i] = Double.parseDouble(prior);
              } catch (IllegalArgumentException ex) {
                throw new Exception("[TargetMetaInfo] Can't parse probability from TargetValue element.");
              }
              
            } else {
              throw new Exception("[TargetMetaInfo] No prior probability defined for value " + valueName);
            }
            
          }
          else
          {
            if ((m_optype != FieldMetaInfo.Optype.CONTINUOUS) && (m_optype != FieldMetaInfo.Optype.NONE))
            {
              throw new Exception("[TargetMetaInfo] TargetValue element has continuous value but optype is not continuous!");
            }
            

            if (m_optype == FieldMetaInfo.Optype.NONE) {
              m_optype = FieldMetaInfo.Optype.CONTINUOUS;
            }
            

            String defaultV = valueE.getAttribute("defaultValue");
            if ((defaultV != null) && (defaultV.length() > 0)) {
              try {
                m_defaultValueOrPriorProbs[i] = Double.parseDouble(defaultV);
              } catch (IllegalArgumentException ex) {
                throw new Exception("[TargetMetaInfo] Can't parse default value from TargetValue element.");
              }
              
            } else {
              throw new Exception("[TargetMetaInfo] No default value defined for target " + m_fieldName);
            }
          }
        }
      }
    }
  }
  







  public double getPriorProbability(String value)
    throws Exception
  {
    if (m_defaultValueOrPriorProbs == null) {
      throw new Exception("[TargetMetaInfo] no TargetValues defined (getPriorProbability)");
    }
    double result = NaN.0D;
    boolean found = false;
    for (int i = 0; i < m_values.size(); i++) {
      if (value.equals(m_values.get(i))) {
        found = true;
        result = m_defaultValueOrPriorProbs[i];
        break;
      }
    }
    if (!found) {
      throw new Exception("[TargetMetaInfo] couldn't find value " + value + "(getPriorProbability)");
    }
    
    return result;
  }
  




  public double getDefaultValue()
    throws Exception
  {
    if (m_defaultValueOrPriorProbs == null) {
      throw new Exception("[TargetMetaInfo] no TargetValues defined (getPriorProbability)");
    }
    return m_defaultValueOrPriorProbs[0];
  }
  





  public ArrayList<String> getValues()
  {
    return new ArrayList(m_values);
  }
  






  public double applyMinMaxRescaleCast(double prediction)
    throws Exception
  {
    if (m_optype != FieldMetaInfo.Optype.CONTINUOUS) {
      throw new Exception("[TargetMetaInfo] target must be continuous!");
    }
    
    if ((!Instance.isMissingValue(m_min)) && (prediction < m_min)) {
      prediction = m_min;
    }
    if ((!Instance.isMissingValue(m_max)) && (prediction > m_max)) {
      prediction = m_max;
    }
    
    prediction *= m_rescaleFactor;
    prediction += m_rescaleConstant;
    
    if (m_castInteger.length() > 0) {
      if (m_castInteger.equals("round")) {
        prediction = Math.round(prediction);
      } else if (m_castInteger.equals("ceiling")) {
        prediction = Math.ceil(prediction);
      } else if (m_castInteger.equals("floor")) {
        prediction = Math.floor(prediction);
      } else {
        throw new Exception("[TargetMetaInfo] unknown castInteger value " + m_castInteger);
      }
    }
    

    return prediction;
  }
  




  public Attribute getFieldAsAttribute()
  {
    if (m_optype == FieldMetaInfo.Optype.CONTINUOUS) {
      return new Attribute(m_fieldName);
    }
    if (m_values.size() == 0)
    {
      return new Attribute(m_fieldName, (FastVector)null);
    }
    
    FastVector values = new FastVector();
    for (String val : m_values) {
      values.addElement(val);
    }
    return new Attribute(m_fieldName, values);
  }
}
