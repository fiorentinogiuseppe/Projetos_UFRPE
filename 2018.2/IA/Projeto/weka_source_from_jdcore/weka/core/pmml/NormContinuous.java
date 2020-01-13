package weka.core.pmml;

import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.core.Attribute;
import weka.core.Instance;







































public class NormContinuous
  extends Expression
{
  private static final long serialVersionUID = 4714332374909851542L;
  protected String m_fieldName;
  protected int m_fieldIndex;
  protected boolean m_mapMissingDefined = false;
  

  protected double m_mapMissingTo;
  

  protected MiningFieldMetaInfo.Outlier m_outlierTreatmentMethod = MiningFieldMetaInfo.Outlier.ASIS;
  

  protected double[] m_linearNormOrig;
  
  protected double[] m_linearNormNorm;
  

  public NormContinuous(Element normCont, FieldMetaInfo.Optype opType, ArrayList<Attribute> fieldDefs)
    throws Exception
  {
    super(opType, fieldDefs);
    
    if (opType != FieldMetaInfo.Optype.CONTINUOUS) {
      throw new Exception("[NormContinuous] can only have a continuous optype");
    }
    
    m_fieldName = normCont.getAttribute("field");
    
    String mapMissing = normCont.getAttribute("mapMissingTo");
    if ((mapMissing != null) && (mapMissing.length() > 0)) {
      m_mapMissingTo = Double.parseDouble(mapMissing);
      m_mapMissingDefined = true;
    }
    
    String outliers = normCont.getAttribute("outliers");
    if ((outliers != null) && (outliers.length() > 0)) {
      for (MiningFieldMetaInfo.Outlier o : MiningFieldMetaInfo.Outlier.values()) {
        if (o.toString().equals(outliers)) {
          m_outlierTreatmentMethod = o;
          break;
        }
      }
    }
    

    NodeList lnL = normCont.getElementsByTagName("LinearNorm");
    if (lnL.getLength() < 2) {
      throw new Exception("[NormContinuous] Must be at least 2 LinearNorm elements!");
    }
    m_linearNormOrig = new double[lnL.getLength()];
    m_linearNormNorm = new double[lnL.getLength()];
    
    for (int i = 0; i < lnL.getLength(); i++) {
      Node lnN = lnL.item(i);
      if (lnN.getNodeType() == 1) {
        Element lnE = (Element)lnN;
        
        String orig = lnE.getAttribute("orig");
        m_linearNormOrig[i] = Double.parseDouble(orig);
        
        String norm = lnE.getAttribute("norm");
        m_linearNormNorm[i] = Double.parseDouble(norm);
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
        throw new Exception("[NormContinuous] Can't find field " + m_fieldName + " in the supplied field definitions.");
      }
      

      Attribute field = (Attribute)m_fieldDefs.get(m_fieldIndex);
      if (!field.isNumeric()) {
        throw new Exception("[NormContinuous] reference field " + m_fieldName + " must be continuous.");
      }
    }
  }
  







  protected Attribute getOutputDef()
  {
    return new Attribute(m_fieldName + "_normContinuous");
  }
  









  public double getResult(double[] incoming)
    throws Exception
  {
    double[] a = m_linearNormOrig;
    double[] b = m_linearNormNorm;
    
    return computeNorm(a, b, incoming);
  }
  





  public double getResultInverse(double[] incoming)
  {
    double[] a = m_linearNormNorm;
    double[] b = m_linearNormOrig;
    
    return computeNorm(a, b, incoming);
  }
  
  private double computeNorm(double[] a, double[] b, double[] incoming) {
    double result = 0.0D;
    
    if (Instance.isMissingValue(incoming[m_fieldIndex])) {
      if (m_mapMissingDefined) {
        result = m_mapMissingTo;
      } else {
        result = incoming[m_fieldIndex];
      }
    } else {
      double x = incoming[m_fieldIndex];
      



      if (x < a[0]) {
        if (m_outlierTreatmentMethod == MiningFieldMetaInfo.Outlier.ASIS) {
          double slope = (b[1] - b[0]) / (a[1] - a[0]);
          
          double offset = b[0] - slope * a[0];
          result = slope * x + offset;
        } else if (m_outlierTreatmentMethod == MiningFieldMetaInfo.Outlier.ASEXTREMEVALUES) {
          result = b[0];
        }
        else {
          result = m_mapMissingTo;
        }
      } else if (x > a[(a.length - 1)]) {
        int length = a.length;
        if (m_outlierTreatmentMethod == MiningFieldMetaInfo.Outlier.ASIS) {
          double slope = (b[(length - 1)] - b[(length - 2)]) / (a[(length - 1)] - a[(length - 2)]);
          
          double offset = b[(length - 1)] - slope * a[(length - 1)];
          result = slope * x + offset;
        } else if (m_outlierTreatmentMethod == MiningFieldMetaInfo.Outlier.ASEXTREMEVALUES) {
          result = b[(length - 1)];
        }
        else {
          result = m_mapMissingTo;
        }
      }
      else {
        for (int i = 1; i < a.length; i++) {
          if (x <= a[i]) {
            result = b[(i - 1)];
            result += (x - a[(i - 1)]) / (a[i] - a[(i - 1)]) * (b[i] - b[(i - 1)]);
            
            break;
          }
        }
      }
    }
    return result;
  }
  





  public String getResultCategorical(double[] incoming)
    throws Exception
  {
    throw new Exception("[NormContinuous] Can't return the result as a categorical value!");
  }
  
  public String toString(String pad) {
    StringBuffer buff = new StringBuffer();
    
    buff.append(pad + "NormContinuous (" + m_fieldName + "):\n" + pad + "linearNorm: ");
    for (int i = 0; i < m_linearNormOrig.length; i++) {
      buff.append("" + m_linearNormOrig[i] + ":" + m_linearNormNorm[i] + " ");
    }
    buff.append("\n" + pad);
    buff.append("outlier treatment: " + m_outlierTreatmentMethod.toString());
    if (m_mapMissingDefined) {
      buff.append("\n" + pad);
      buff.append("map missing values to: " + m_mapMissingTo);
    }
    
    return buff.toString();
  }
}
