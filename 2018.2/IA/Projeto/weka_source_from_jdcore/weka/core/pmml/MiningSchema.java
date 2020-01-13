package weka.core.pmml;

import java.io.Serializable;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
















































public class MiningSchema
  implements Serializable
{
  private static final long serialVersionUID = 7144380586726330455L;
  protected Instances m_fieldInstancesStructure;
  protected Instances m_miningSchemaInstancesStructure;
  protected ArrayList<MiningFieldMetaInfo> m_miningMeta = new ArrayList();
  






  protected ArrayList<DerivedFieldMetaInfo> m_derivedMeta = new ArrayList();
  


  protected TransformationDictionary m_transformationDictionary = null;
  

  protected TargetMetaInfo m_targetMetaInfo = null;
  
  private void getLocalTransformations(Element model) throws Exception {
    NodeList temp = model.getElementsByTagName("LocalTransformations");
    
    if (temp.getLength() > 0)
    {
      Element localT = (Element)temp.item(0);
      

      ArrayList<Attribute> fieldDefs = new ArrayList();
      for (int i = 0; i < m_miningSchemaInstancesStructure.numAttributes(); i++) {
        fieldDefs.add(m_miningSchemaInstancesStructure.attribute(i));
      }
      
      NodeList localDerivedL = localT.getElementsByTagName("DerivedField");
      for (int i = 0; i < localDerivedL.getLength(); i++) {
        Node localDerived = localDerivedL.item(i);
        if (localDerived.getNodeType() == 1) {
          DerivedFieldMetaInfo d = new DerivedFieldMetaInfo((Element)localDerived, fieldDefs, m_transformationDictionary);
          
          m_derivedMeta.add(d);
        }
      }
    }
  }
  

















  public MiningSchema(Element model, Instances dataDictionary, TransformationDictionary transDict)
    throws Exception
  {
    FastVector attInfo = new FastVector();
    NodeList fieldList = model.getElementsByTagName("MiningField");
    int classIndex = -1;
    int addedCount = 0;
    for (int i = 0; i < fieldList.getLength(); i++) {
      Node miningField = fieldList.item(i);
      if (miningField.getNodeType() == 1) {
        Element miningFieldEl = (Element)miningField;
        
        MiningFieldMetaInfo mfi = new MiningFieldMetaInfo(miningFieldEl);
        
        if ((mfi.getUsageType() == MiningFieldMetaInfo.Usage.ACTIVE) || (mfi.getUsageType() == MiningFieldMetaInfo.Usage.PREDICTED))
        {


          Attribute miningAtt = dataDictionary.attribute(mfi.getName());
          if (miningAtt != null) {
            mfi.setIndex(addedCount);
            attInfo.addElement(miningAtt);
            addedCount++;
            
            if (mfi.getUsageType() == MiningFieldMetaInfo.Usage.PREDICTED) {
              classIndex = addedCount - 1;
            }
            

            m_miningMeta.add(mfi);
          } else {
            throw new Exception("Can't find mining field: " + mfi.getName() + " in the data dictionary.");
          }
        }
      }
    }
    

    m_miningSchemaInstancesStructure = new Instances("miningSchema", attInfo, 0);
    


    for (MiningFieldMetaInfo m : m_miningMeta) {
      m.setMiningSchemaInstances(m_miningSchemaInstancesStructure);
    }
    
    m_transformationDictionary = transDict;
    

    if (m_transformationDictionary != null)
    {



      m_transformationDictionary.setFieldDefsForDerivedFields(m_miningSchemaInstancesStructure);
      
      ArrayList<DerivedFieldMetaInfo> transDerived = transDict.getDerivedFields();
      m_derivedMeta.addAll(transDerived);
    }
    

    getLocalTransformations(model);
    
    FastVector newStructure = new FastVector();
    for (MiningFieldMetaInfo m : m_miningMeta) {
      newStructure.addElement(m.getFieldAsAttribute());
    }
    
    for (DerivedFieldMetaInfo d : m_derivedMeta) {
      newStructure.addElement(d.getFieldAsAttribute());
    }
    m_fieldInstancesStructure = new Instances("FieldStructure", newStructure, 0);
    
    if (classIndex != -1) {
      m_fieldInstancesStructure.setClassIndex(classIndex);
      m_miningSchemaInstancesStructure.setClassIndex(classIndex);
    }
    

    NodeList targetsList = model.getElementsByTagName("Targets");
    if (targetsList.getLength() > 0) {
      if (targetsList.getLength() > 1) {
        throw new Exception("[MiningSchema] Can only handle a single Target");
      }
      Node te = targetsList.item(0);
      if (te.getNodeType() == 1) {
        m_targetMetaInfo = new TargetMetaInfo((Element)te);
        


        if ((m_fieldInstancesStructure.classIndex() >= 0) && (m_fieldInstancesStructure.classAttribute().isString()))
        {
          ArrayList<String> targetVals = m_targetMetaInfo.getValues();
          if (targetVals.size() > 0) {
            Attribute classAtt = m_fieldInstancesStructure.classAttribute();
            for (int i = 0; i < targetVals.size(); i++) {
              classAtt.addStringValue((String)targetVals.get(i));
            }
          }
        }
      }
    }
  }
  







  public void applyMissingValuesTreatment(double[] values)
    throws Exception
  {
    for (int i = 0; i < m_miningMeta.size(); i++) {
      MiningFieldMetaInfo mfi = (MiningFieldMetaInfo)m_miningMeta.get(i);
      values[i] = mfi.applyMissingValueTreatment(values[i]);
    }
  }
  






  public void applyOutlierTreatment(double[] values)
    throws Exception
  {
    for (int i = 0; i < m_miningMeta.size(); i++) {
      MiningFieldMetaInfo mfi = (MiningFieldMetaInfo)m_miningMeta.get(i);
      values[i] = mfi.applyOutlierTreatment(values[i]);
    }
  }
  





  public void applyMissingAndOutlierTreatments(double[] values)
    throws Exception
  {
    for (int i = 0; i < m_miningMeta.size(); i++) {
      MiningFieldMetaInfo mfi = (MiningFieldMetaInfo)m_miningMeta.get(i);
      values[i] = mfi.applyMissingValueTreatment(values[i]);
      values[i] = mfi.applyOutlierTreatment(values[i]);
    }
  }
  







  public Instances getFieldsAsInstances()
  {
    return m_fieldInstancesStructure;
  }
  




  public Instances getMiningSchemaAsInstances()
  {
    return m_miningSchemaInstancesStructure;
  }
  





  public TransformationDictionary getTransformationDictionary()
  {
    return m_transformationDictionary;
  }
  




  public boolean hasTargetMetaData()
  {
    return m_targetMetaInfo != null;
  }
  




  public TargetMetaInfo getTargetMetaData()
  {
    return m_targetMetaInfo;
  }
  






  public void convertStringAttsToNominal()
  {
    Instances miningSchemaI = getFieldsAsInstances();
    if (miningSchemaI.checkForStringAttributes()) {
      FastVector attInfo = new FastVector();
      for (int i = 0; i < miningSchemaI.numAttributes(); i++) {
        Attribute tempA = miningSchemaI.attribute(i);
        if (tempA.isString()) {
          FastVector valueVector = new FastVector();
          for (int j = 0; j < tempA.numValues(); j++) {
            valueVector.addElement(tempA.value(j));
          }
          Attribute newAtt = new Attribute(tempA.name(), valueVector);
          attInfo.addElement(newAtt);
        } else {
          attInfo.addElement(tempA);
        }
      }
      Instances newI = new Instances("miningSchema", attInfo, 0);
      if (m_fieldInstancesStructure.classIndex() >= 0) {
        newI.setClassIndex(m_fieldInstancesStructure.classIndex());
      }
      m_fieldInstancesStructure = newI;
    }
  }
  











  public void convertNumericAttToNominal(int index, ArrayList<String> newVals)
  {
    Instances miningSchemaI = getFieldsAsInstances();
    if (miningSchemaI.attribute(index).isNominal()) {
      throw new IllegalArgumentException("[MiningSchema] convertNumericAttToNominal: attribute is already nominal!");
    }
    

    FastVector newValues = new FastVector();
    for (int i = 0; i < newVals.size(); i++) {
      newValues.addElement(newVals.get(i));
    }
    
    FastVector attInfo = new FastVector();
    for (int i = 0; i < miningSchemaI.numAttributes(); i++) {
      Attribute tempA = miningSchemaI.attribute(i);
      if (i == index) {
        Attribute newAtt = new Attribute(tempA.name(), newValues);
        attInfo.addElement(newAtt);
      } else {
        attInfo.addElement(tempA);
      }
    }
    
    Instances newI = new Instances("miningSchema", attInfo, 0);
    if (m_fieldInstancesStructure.classIndex() >= 0) {
      newI.setClassIndex(m_fieldInstancesStructure.classIndex());
    }
    m_fieldInstancesStructure = newI;
  }
  
  public ArrayList<DerivedFieldMetaInfo> getDerivedFields() {
    return m_derivedMeta;
  }
  
  public ArrayList<MiningFieldMetaInfo> getMiningFields() {
    return m_miningMeta;
  }
  




  public String toString()
  {
    StringBuffer temp = new StringBuffer();
    
    if (m_transformationDictionary != null) {
      temp.append(m_transformationDictionary);
    }
    
    temp.append("Mining schema:\n\n");
    for (MiningFieldMetaInfo m : m_miningMeta) {
      temp.append(m + "\n");
    }
    
    if (m_derivedMeta.size() > 0) {
      temp.append("\nDerived fields:\n\n");
      for (DerivedFieldMetaInfo d : m_derivedMeta) {
        temp.append(d + "\n");
      }
    }
    temp.append("\n");
    return temp.toString();
  }
}
