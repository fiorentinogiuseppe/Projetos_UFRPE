package weka.core.pmml;

import java.io.Serializable;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SerializedObject;
































class TransformationDictionary
  implements Serializable
{
  protected ArrayList<DefineFunction> m_defineFunctions = new ArrayList();
  

  protected ArrayList<DerivedFieldMetaInfo> m_derivedFields = new ArrayList();
  










  protected TransformationDictionary(Element dictionary, Instances dataDictionary)
    throws Exception
  {
    ArrayList<Attribute> incomingFieldDefs = new ArrayList();
    for (int i = 0; i < dataDictionary.numAttributes(); i++) {
      incomingFieldDefs.add(dataDictionary.attribute(i));
    }
    

    NodeList derivedL = dictionary.getChildNodes();
    for (int i = 0; i < derivedL.getLength(); i++) {
      Node child = derivedL.item(i);
      if (child.getNodeType() == 1) {
        String tagName = ((Element)child).getTagName();
        if (tagName.equals("DerivedField")) {
          DerivedFieldMetaInfo df = new DerivedFieldMetaInfo((Element)child, incomingFieldDefs, null);
          m_derivedFields.add(df);
        } else if (tagName.equals("DefineFunction")) {
          DefineFunction defF = new DefineFunction((Element)child, null);
          m_defineFunctions.add(defF);
        }
      }
    }
  }
  











  protected void setFieldDefsForDerivedFields(ArrayList<Attribute> fieldDefs)
    throws Exception
  {
    for (int i = 0; i < m_derivedFields.size(); i++) {
      ((DerivedFieldMetaInfo)m_derivedFields.get(i)).setFieldDefs(fieldDefs);
    }
  }
  











  protected void setFieldDefsForDerivedFields(Instances fieldDefs)
    throws Exception
  {
    ArrayList<Attribute> tempDefs = new ArrayList();
    for (int i = 0; i < fieldDefs.numAttributes(); i++) {
      tempDefs.add(fieldDefs.attribute(i));
    }
    setFieldDefsForDerivedFields(tempDefs);
  }
  
  protected ArrayList<DerivedFieldMetaInfo> getDerivedFields() {
    return new ArrayList(m_derivedFields);
  }
  







  protected DefineFunction getFunction(String functionName)
    throws Exception
  {
    DefineFunction copy = null;
    DefineFunction match = null;
    for (DefineFunction f : m_defineFunctions) {
      if (f.getName().equals(functionName)) {
        match = f;
        
        break;
      }
    }
    
    if (match != null) {
      SerializedObject so = new SerializedObject(match, false);
      copy = (DefineFunction)so.getObject();
    }
    

    return copy;
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    
    buff.append("Transformation dictionary:\n");
    
    if (m_derivedFields.size() > 0) {
      buff.append("derived fields:\n");
      for (DerivedFieldMetaInfo d : m_derivedFields) {
        buff.append("" + d.getFieldAsAttribute() + "\n");
      }
    }
    
    if (m_defineFunctions.size() > 0) {
      buff.append("\nfunctions:\n");
      for (DefineFunction f : m_defineFunctions) {
        buff.append(f.toString("  ") + "\n");
      }
    }
    
    buff.append("\n");
    
    return buff.toString();
  }
}
