package weka.core.pmml;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.core.Attribute;
import weka.core.FastVector;




























public class Constant
  extends Expression
{
  private static final long serialVersionUID = -304829687822452424L;
  protected String m_categoricalConst = null;
  protected double m_continuousConst = NaN.0D;
  









  public Constant(Element constant, FieldMetaInfo.Optype opType, ArrayList<Attribute> fieldDefs)
    throws Exception
  {
    super(opType, fieldDefs);
    
    NodeList constL = constant.getChildNodes();
    String c = constL.item(0).getNodeValue();
    
    if ((m_opType == FieldMetaInfo.Optype.CATEGORICAL) || (m_opType == FieldMetaInfo.Optype.ORDINAL))
    {
      m_categoricalConst = c;
    } else {
      try {
        m_continuousConst = Double.parseDouble(c);
      } catch (IllegalArgumentException ex) {
        throw new Exception("[Constant] Unable to parse continuous constant: " + c);
      }
    }
  }
  








  protected Attribute getOutputDef()
  {
    if (m_opType == FieldMetaInfo.Optype.CONTINUOUS) {
      return new Attribute("Constant: " + m_continuousConst);
    }
    
    FastVector nom = new FastVector();
    nom.addElement(m_categoricalConst);
    return new Attribute("Constant: " + m_categoricalConst, nom);
  }
  








  public double getResult(double[] incoming)
  {
    if (m_opType == FieldMetaInfo.Optype.CONTINUOUS) {
      return m_continuousConst;
    }
    return 0.0D;
  }
  








  public String getResultCategorical(double[] incoming)
    throws Exception
  {
    if (m_opType == FieldMetaInfo.Optype.CONTINUOUS) {
      throw new IllegalArgumentException("[Constant] Cant't return result as categorical/ordinal as optype is continuous!");
    }
    
    return m_categoricalConst;
  }
  
  public static void main(String[] args) {
    try {
      File f = new File(args[0]);
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(f);
      doc.getDocumentElement().normalize();
      NodeList constL = doc.getElementsByTagName("Constant");
      Node c = constL.item(0);
      
      if (c.getNodeType() == 1) {
        Constant constC = new Constant((Element)c, FieldMetaInfo.Optype.CONTINUOUS, null);
        System.err.println("Value of first constant: " + constC.getResult(null));
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  public String toString(String pad) {
    return pad + "Constant: " + (m_categoricalConst != null ? m_categoricalConst : new StringBuilder().append("").append(m_continuousConst).toString());
  }
}
