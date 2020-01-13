package weka.core.pmml;

import java.io.Serializable;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.core.Attribute;






























public abstract class Expression
  implements Serializable
{
  private static final long serialVersionUID = 4448840549804800321L;
  protected FieldMetaInfo.Optype m_opType;
  protected ArrayList<Attribute> m_fieldDefs = null;
  

  public Expression(FieldMetaInfo.Optype opType, ArrayList<Attribute> fieldDefs)
  {
    m_opType = opType;
    m_fieldDefs = fieldDefs;
  }
  




  public void setFieldDefs(ArrayList<Attribute> fieldDefs)
    throws Exception
  {
    m_fieldDefs = fieldDefs;
  }
  









  public abstract double getResult(double[] paramArrayOfDouble)
    throws Exception;
  








  public double getResultContinuous(double[] incoming)
    throws Exception
  {
    if (m_opType != FieldMetaInfo.Optype.CONTINUOUS) {
      throw new Exception("[Expression] Can't return continuous result as optype is not continuous");
    }
    
    return getResult(incoming);
  }
  













  public abstract String getResultCategorical(double[] paramArrayOfDouble)
    throws Exception;
  













  protected abstract Attribute getOutputDef();
  













  public static Expression getExpression(Node container, FieldMetaInfo.Optype opType, ArrayList<Attribute> fieldDefs, TransformationDictionary transDict)
    throws Exception
  {
    Expression result = null;
    String tagName = "";
    
    NodeList children = container.getChildNodes();
    if (children.getLength() == 0) {
      throw new Exception("[Expression] container has no children!");
    }
    


    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == 1) {
        tagName = ((Element)child).getTagName();
        result = getExpression(tagName, child, opType, fieldDefs, transDict);
        if (result != null) {
          break;
        }
      }
    }
    
    return result;
  }
  




















  public static Expression getExpression(String name, Node expression, FieldMetaInfo.Optype opType, ArrayList<Attribute> fieldDefs, TransformationDictionary transDict)
    throws Exception
  {
    Expression result = null;
    
    if (name.equals("Constant"))
    {
      result = new Constant((Element)expression, opType, fieldDefs);
    } else if (name.equals("FieldRef"))
    {
      result = new FieldRef((Element)expression, opType, fieldDefs);
    } else if (name.equals("Apply"))
    {
      result = new Apply((Element)expression, opType, fieldDefs, transDict);
    } else if (name.equals("NormDiscrete")) {
      result = new NormDiscrete((Element)expression, opType, fieldDefs);
    } else if (name.equals("NormContinuous")) {
      result = new NormContinuous((Element)expression, opType, fieldDefs);
    } else if (name.equals("Discretize")) {
      result = new Discretize((Element)expression, opType, fieldDefs);
    } else if ((name.equals("MapValues")) || (name.equals("Aggregate")))
    {
      throw new Exception("[Expression] Unhandled Expression type " + name);
    }
    return result;
  }
  





  public Attribute getFieldDef(String attName)
  {
    Attribute returnV = null;
    for (int i = 0; i < m_fieldDefs.size(); i++) {
      if (((Attribute)m_fieldDefs.get(i)).name().equals(attName)) {
        returnV = (Attribute)m_fieldDefs.get(i);
        break;
      }
    }
    return returnV;
  }
  
  public int getFieldDefIndex(String attName) {
    int returnV = -1;
    for (int i = 0; i < m_fieldDefs.size(); i++) {
      if (((Attribute)m_fieldDefs.get(i)).name().equals(attName)) {
        returnV = i;
        break;
      }
    }
    return returnV;
  }
  




  public FieldMetaInfo.Optype getOptype()
  {
    return m_opType;
  }
  
  public String toString() {
    return toString("");
  }
  
  public String toString(String pad) {
    return pad + getClass().getName();
  }
}
