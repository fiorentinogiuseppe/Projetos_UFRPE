package weka.core.pmml;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.gui.Logger;









































public class MappingInfo
  implements Serializable
{
  public static final int UNKNOWN_NOMINAL_VALUE = -1;
  private int[] m_fieldsMap = null;
  










  private int[][] m_nominalValueMaps = (int[][])null;
  

  private String m_fieldsMappingText = null;
  

  private Logger m_log = null;
  
  public MappingInfo(Instances dataSet, MiningSchema miningSchema, Logger log) throws Exception
  {
    m_log = log;
    
    Instances fieldsI = miningSchema.getMiningSchemaAsInstances();
    
    m_fieldsMap = new int[fieldsI.numAttributes()];
    m_nominalValueMaps = new int[fieldsI.numAttributes()][];
    
    for (int i = 0; i < fieldsI.numAttributes(); i++) {
      String schemaAttName = fieldsI.attribute(i).name();
      boolean found = false;
      for (int j = 0; j < dataSet.numAttributes(); j++) {
        if (dataSet.attribute(j).name().equals(schemaAttName)) {
          Attribute miningSchemaAtt = fieldsI.attribute(i);
          Attribute incomingAtt = dataSet.attribute(j);
          
          if ((miningSchemaAtt.type() != incomingAtt.type()) && (
            (!miningSchemaAtt.isString()) || (!incomingAtt.isNominal())))
          {




            throw new Exception("[MappingInfo] type mismatch for field " + schemaAttName + ". Mining schema type " + miningSchemaAtt.toString() + ". Incoming type " + incomingAtt.toString() + ".");
          }
          





          if (miningSchemaAtt.numValues() != incomingAtt.numValues()) {
            String warningString = "[MappingInfo] WARNING: incoming nominal attribute " + incomingAtt.name() + " does not have the same " + "number of values as the corresponding mining " + "schema attribute.";
            


            if (m_log != null) {
              m_log.logMessage(warningString);
            } else {
              System.err.println(warningString);
            }
          }
          if ((miningSchemaAtt.isNominal()) || (miningSchemaAtt.isString())) {
            int[] valuesMap = new int[incomingAtt.numValues()];
            for (int k = 0; k < incomingAtt.numValues(); k++) {
              String incomingNomVal = incomingAtt.value(k);
              int indexInSchema = miningSchemaAtt.indexOfValue(incomingNomVal);
              if (indexInSchema < 0) {
                String warningString = "[MappingInfo] WARNING: incoming nominal attribute " + incomingAtt.name() + " has value " + incomingNomVal + " that doesn't occur in the mining schema.";
                

                if (m_log != null) {
                  m_log.logMessage(warningString);
                } else {
                  System.err.println(warningString);
                }
                valuesMap[k] = -1;
              } else {
                valuesMap[k] = indexInSchema;
              }
            }
            m_nominalValueMaps[i] = valuesMap;
          }
          












          found = true;
          m_fieldsMap[i] = j;
        }
      }
      if (!found) {
        throw new Exception("[MappingInfo] Unable to find a match for mining schema attribute " + schemaAttName + " in the " + "incoming instances!");
      }
    }
    



    if (fieldsI.classIndex() >= 0) {
      if (dataSet.classIndex() < 0)
      {
        String className = fieldsI.classAttribute().name();
        Attribute classMatch = dataSet.attribute(className);
        if (classMatch == null) {
          throw new Exception("[MappingInfo] Can't find match for target field " + className + "in incoming instances!");
        }
        
        dataSet.setClass(classMatch);
      } else if (!fieldsI.classAttribute().name().equals(dataSet.classAttribute().name())) {
        throw new Exception("[MappingInfo] class attribute in mining schema does not match class attribute in incoming instances!");
      }
    }
    


    fieldsMappingString(fieldsI, dataSet);
  }
  
  private void fieldsMappingString(Instances miningSchemaI, Instances incomingI) {
    StringBuffer result = new StringBuffer();
    
    int maxLength = 0;
    for (int i = 0; i < miningSchemaI.numAttributes(); i++) {
      if (miningSchemaI.attribute(i).name().length() > maxLength) {
        maxLength = miningSchemaI.attribute(i).name().length();
      }
    }
    maxLength += 12;
    
    int minLength = 13;
    String headerS = "Mining schema";
    String sep = "-------------";
    
    if (maxLength < minLength) {
      maxLength = minLength;
    }
    
    headerS = PMMLUtils.pad(headerS, " ", maxLength, false);
    sep = PMMLUtils.pad(sep, "-", maxLength, false);
    
    sep = sep + "\t    ----------------\n";
    headerS = headerS + "\t    Incoming fields\n";
    result.append(headerS);
    result.append(sep);
    
    for (int i = 0; i < miningSchemaI.numAttributes(); i++) {
      Attribute temp = miningSchemaI.attribute(i);
      String attName = "(" + (temp.isNumeric() ? "numeric)" : "nominal)") + " " + temp.name();
      



      attName = PMMLUtils.pad(attName, " ", maxLength, false);
      attName = attName + "\t--> ";
      result.append(attName);
      
      Attribute incoming = incomingI.attribute(m_fieldsMap[i]);
      String fieldName = "" + (m_fieldsMap[i] + 1) + " (" + (incoming.isNumeric() ? "numeric)" : "nominal)");
      


      fieldName = fieldName + " " + incoming.name();
      result.append(fieldName + "\n");
    }
    
    m_fieldsMappingText = result.toString();
  }
  












  public double[] instanceToSchema(Instance inst, MiningSchema miningSchema)
    throws Exception
  {
    Instances miningSchemaI = miningSchema.getMiningSchemaAsInstances();
    

    double[] result = new double[miningSchema.getFieldsAsInstances().numAttributes()];
    

    for (int i = 0; i < miningSchemaI.numAttributes(); i++)
    {
      result[i] = inst.value(m_fieldsMap[i]);
      if ((miningSchemaI.attribute(i).isNominal()) || (miningSchemaI.attribute(i).isString()))
      {


        if (!Instance.isMissingValue(inst.value(m_fieldsMap[i]))) {
          int[] valueMap = m_nominalValueMaps[i];
          int index = valueMap[((int)inst.value(m_fieldsMap[i]))];
          String incomingAttValue = inst.attribute(m_fieldsMap[i]).value((int)inst.value(m_fieldsMap[i]));
          

          if (index >= 0) {
            result[i] = index;
          }
          else {
            result[i] = -1.0D;
            String warningString = "[MappingInfo] WARNING: Can't match nominal value " + incomingAttValue;
            
            if (m_log != null) {
              m_log.logMessage(warningString);
            } else {
              System.err.println(warningString);
            }
          }
        }
      }
    }
    

    miningSchema.applyMissingAndOutlierTreatments(result);
    


    ArrayList<DerivedFieldMetaInfo> derivedFields = miningSchema.getDerivedFields();
    for (int i = 0; i < derivedFields.size(); i++) {
      DerivedFieldMetaInfo temp = (DerivedFieldMetaInfo)derivedFields.get(i);
      
      double r = temp.getDerivedValue(result);
      result[(i + miningSchemaI.numAttributes())] = r;
    }
    






    return result;
  }
  





  public String getFieldsMappingString()
  {
    if (m_fieldsMappingText == null) {
      return "No fields mapping constructed!";
    }
    return m_fieldsMappingText;
  }
}
