package weka.classifiers.pmml.consumer;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.matrix.Maths;
import weka.core.pmml.MappingInfo;
import weka.core.pmml.MiningSchema;
import weka.core.pmml.TargetMetaInfo;
import weka.gui.Logger;




































public class Regression
  extends PMMLClassifier
  implements Serializable
{
  private static final long serialVersionUID = -5551125528409488634L;
  protected String m_algorithmName;
  protected RegressionTable[] m_regressionTables;
  
  static class RegressionTable
    implements Serializable
  {
    private static final long serialVersionUID = -5259866093996338995L;
    public static final int REGRESSION = 0;
    public static final int CLASSIFICATION = 1;
    
    static abstract class Predictor
      implements Serializable
    {
      private static final long serialVersionUID = 7043831847273383618L;
      protected String m_name;
      protected int m_miningSchemaAttIndex = -1;
      

      protected double m_coefficient = 1.0D;
      





      protected Predictor(Element predictor, Instances miningSchema)
        throws Exception
      {
        m_name = predictor.getAttribute("name");
        for (int i = 0; i < miningSchema.numAttributes(); i++) {
          Attribute temp = miningSchema.attribute(i);
          if (temp.name().equals(m_name)) {
            m_miningSchemaAttIndex = i;
          }
        }
        
        if (m_miningSchemaAttIndex == -1) {
          throw new Exception("[Predictor] unable to find matching attribute for predictor " + m_name);
        }
        

        String coeff = predictor.getAttribute("coefficient");
        if (coeff.length() > 0) {
          m_coefficient = Double.parseDouble(coeff);
        }
      }
      



      public String toString()
      {
        return Utils.doubleToString(m_coefficient, 12, 4) + " * ";
      }
      





      public abstract void add(double[] paramArrayOfDouble1, double[] paramArrayOfDouble2);
    }
    





    protected class NumericPredictor
      extends Regression.RegressionTable.Predictor
    {
      private static final long serialVersionUID = -4335075205696648273L;
      



      protected double m_exponent = 1.0D;
      







      protected NumericPredictor(Element predictor, Instances miningSchema)
        throws Exception
      {
        super(miningSchema);
        
        String exponent = predictor.getAttribute("exponent");
        if (exponent.length() > 0) {
          m_exponent = Double.parseDouble(exponent);
        }
      }
      


      public String toString()
      {
        String output = super.toString();
        output = output + m_name;
        if ((m_exponent > 1.0D) || (m_exponent < 1.0D)) {
          output = output + "^" + Utils.doubleToString(m_exponent, 4);
        }
        return output;
      }
      







      public void add(double[] preds, double[] input)
      {
        if (m_targetCategory == -1) {
          preds[0] += m_coefficient * Math.pow(input[m_miningSchemaAttIndex], m_exponent);
        } else {
          preds[m_targetCategory] += m_coefficient * Math.pow(input[m_miningSchemaAttIndex], m_exponent);
        }
      }
    }
    



    protected class CategoricalPredictor
      extends Regression.RegressionTable.Predictor
    {
      private static final long serialVersionUID = 3077920125549906819L;
      


      protected String m_valueName;
      

      protected int m_valueIndex = -1;
      







      protected CategoricalPredictor(Element predictor, Instances miningSchema)
        throws Exception
      {
        super(miningSchema);
        
        String valName = predictor.getAttribute("value");
        if (valName.length() == 0) {
          throw new Exception("[CategoricalPredictor] attribute value not specified!");
        }
        
        m_valueName = valName;
        
        Attribute att = miningSchema.attribute(m_miningSchemaAttIndex);
        if (att.isString())
        {


          att.addStringValue(m_valueName);
        }
        m_valueIndex = att.indexOfValue(m_valueName);
        





        if (m_valueIndex == -1) {
          throw new Exception("[CategoricalPredictor] unable to find value " + m_valueName + " in mining schema attribute " + att.name());
        }
      }
      




      public String toString()
      {
        String output = super.toString();
        output = output + m_name + "=" + m_valueName;
        return output;
      }
      









      public void add(double[] preds, double[] input)
      {
        if (m_valueIndex == (int)input[m_miningSchemaAttIndex]) {
          if (m_targetCategory == -1) {
            preds[0] += m_coefficient;
          } else {
            preds[m_targetCategory] += m_coefficient;
          }
        }
      }
    }
    



    protected class PredictorTerm
      implements Serializable
    {
      private static final long serialVersionUID = 5493100145890252757L;
      

      protected double m_coefficient = 1.0D;
      



      protected int[] m_indexes;
      



      protected String[] m_fieldNames;
      




      protected PredictorTerm(Element predictorTerm, Instances miningSchema)
        throws Exception
      {
        String coeff = predictorTerm.getAttribute("coefficient");
        if ((coeff != null) && (coeff.length() > 0)) {
          try {
            m_coefficient = Double.parseDouble(coeff);
          } catch (IllegalArgumentException ex) {
            throw new Exception("[PredictorTerm] unable to parse coefficient");
          }
        }
        
        NodeList fields = predictorTerm.getElementsByTagName("FieldRef");
        if (fields.getLength() > 0) {
          m_indexes = new int[fields.getLength()];
          m_fieldNames = new String[fields.getLength()];
          
          for (int i = 0; i < fields.getLength(); i++) {
            Node fieldRef = fields.item(i);
            if (fieldRef.getNodeType() == 1) {
              String fieldName = ((Element)fieldRef).getAttribute("field");
              if ((fieldName != null) && (fieldName.length() > 0)) {
                boolean found = false;
                
                for (int j = 0; j < miningSchema.numAttributes(); j++) {
                  if (miningSchema.attribute(j).name().equals(fieldName))
                  {

                    if (!miningSchema.attribute(j).isNumeric()) {
                      throw new Exception("[PredictorTerm] field is not continuous: " + fieldName);
                    }
                    
                    found = true;
                    m_indexes[i] = j;
                    m_fieldNames[i] = fieldName;
                    break;
                  }
                }
                if (!found) {
                  throw new Exception("[PredictorTerm] Unable to find field " + fieldName + " in mining schema!");
                }
              }
            }
          }
        }
      }
      



      public String toString()
      {
        StringBuffer result = new StringBuffer();
        result.append("(" + Utils.doubleToString(m_coefficient, 12, 4));
        for (int i = 0; i < m_fieldNames.length; i++) {
          result.append(" * " + m_fieldNames[i]);
        }
        result.append(")");
        return result.toString();
      }
      







      public void add(double[] preds, double[] input)
      {
        int indx = 0;
        if (m_targetCategory != -1) {
          indx = m_targetCategory;
        }
        
        double result = m_coefficient;
        for (int i = 0; i < m_indexes.length; i++) {
          result *= input[m_indexes[i]];
        }
        preds[indx] += result;
      }
    }
    







    protected int m_functionType = 0;
    

    protected MiningSchema m_miningSchema;
    

    protected double m_intercept = 0.0D;
    

    protected int m_targetCategory = -1;
    

    protected ArrayList<Predictor> m_predictors = new ArrayList();
    


    protected ArrayList<PredictorTerm> m_predictorTerms = new ArrayList();
    



    public String toString()
    {
      Instances miningSchema = m_miningSchema.getFieldsAsInstances();
      StringBuffer temp = new StringBuffer();
      temp.append("Regression table:\n");
      temp.append(miningSchema.classAttribute().name());
      if (m_functionType == 1) {
        temp.append("=" + miningSchema.classAttribute().value(m_targetCategory));
      }
      

      temp.append(" =\n\n");
      

      for (int i = 0; i < m_predictors.size(); i++) {
        temp.append(((Predictor)m_predictors.get(i)).toString() + " +\n");
      }
      

      for (int i = 0; i < m_predictorTerms.size(); i++) {
        temp.append(((PredictorTerm)m_predictorTerms.get(i)).toString() + " +\n");
      }
      
      temp.append(Utils.doubleToString(m_intercept, 12, 4));
      temp.append("\n\n");
      
      return temp.toString();
    }
    












    protected RegressionTable(Element table, int functionType, MiningSchema mSchema)
      throws Exception
    {
      m_miningSchema = mSchema;
      m_functionType = functionType;
      
      Instances miningSchema = m_miningSchema.getFieldsAsInstances();
      

      String intercept = table.getAttribute("intercept");
      if (intercept.length() > 0) {
        m_intercept = Double.parseDouble(intercept);
      }
      

      if (m_functionType == 1)
      {
        String targetCat = table.getAttribute("targetCategory");
        if (targetCat.length() > 0) {
          Attribute classA = miningSchema.classAttribute();
          for (int i = 0; i < classA.numValues(); i++) {
            if (classA.value(i).equals(targetCat)) {
              m_targetCategory = i;
            }
          }
        }
        if (m_targetCategory == -1) {
          throw new Exception("[RegressionTable] No target categories defined for classification");
        }
      }
      

      NodeList numericPs = table.getElementsByTagName("NumericPredictor");
      for (int i = 0; i < numericPs.getLength(); i++) {
        Node nP = numericPs.item(i);
        if (nP.getNodeType() == 1) {
          NumericPredictor numP = new NumericPredictor((Element)nP, miningSchema);
          m_predictors.add(numP);
        }
      }
      

      NodeList categoricalPs = table.getElementsByTagName("CategoricalPredictor");
      for (int i = 0; i < categoricalPs.getLength(); i++) {
        Node cP = categoricalPs.item(i);
        if (cP.getNodeType() == 1) {
          CategoricalPredictor catP = new CategoricalPredictor((Element)cP, miningSchema);
          m_predictors.add(catP);
        }
      }
      

      NodeList predictorTerms = table.getElementsByTagName("PredictorTerm");
      for (int i = 0; i < predictorTerms.getLength(); i++) {
        Node pT = predictorTerms.item(i);
        PredictorTerm predT = new PredictorTerm((Element)pT, miningSchema);
        m_predictorTerms.add(predT);
      }
    }
    
    public void predict(double[] preds, double[] input) {
      if (m_targetCategory == -1) {
        preds[0] = m_intercept;
      } else {
        preds[m_targetCategory] = m_intercept;
      }
      

      for (int i = 0; i < m_predictors.size(); i++) {
        Predictor p = (Predictor)m_predictors.get(i);
        p.add(preds, input);
      }
      

      for (int i = 0; i < m_predictorTerms.size(); i++) {
        PredictorTerm pt = (PredictorTerm)m_predictorTerms.get(i);
        pt.add(preds, input);
      }
    }
  }
  








  static enum Normalization
  {
    NONE,  SIMPLEMAX,  SOFTMAX,  LOGIT,  PROBIT,  CLOGLOG, 
    EXP,  LOGLOG,  CAUCHIT;
    
    private Normalization() {} }
  protected Normalization m_normalizationMethod = Normalization.NONE;
  







  public Regression(Element model, Instances dataDictionary, MiningSchema miningSchema)
    throws Exception
  {
    super(dataDictionary, miningSchema);
    
    int functionType = 0;
    

    String fName = model.getAttribute("functionName");
    
    if (fName.equals("regression")) {
      functionType = 0;
    } else if (fName.equals("classification")) {
      functionType = 1;
    } else {
      throw new Exception("[PMML Regression] Function name not defined in pmml!");
    }
    

    String algName = model.getAttribute("algorithmName");
    if ((algName != null) && (algName.length() > 0)) {
      m_algorithmName = algName;
    }
    

    m_normalizationMethod = determineNormalization(model);
    
    setUpRegressionTables(model, functionType);
  }
  











  private void setUpRegressionTables(Element model, int functionType)
    throws Exception
  {
    NodeList tableList = model.getElementsByTagName("RegressionTable");
    
    if (tableList.getLength() == 0) {
      throw new Exception("[Regression] no regression tables defined!");
    }
    
    m_regressionTables = new RegressionTable[tableList.getLength()];
    
    for (int i = 0; i < tableList.getLength(); i++) {
      Node table = tableList.item(i);
      if (table.getNodeType() == 1) {
        RegressionTable tempRTable = new RegressionTable((Element)table, functionType, m_miningSchema);
        


        m_regressionTables[i] = tempRTable;
      }
    }
  }
  






  private static Normalization determineNormalization(Element model)
  {
    Normalization normMethod = Normalization.NONE;
    
    String normName = model.getAttribute("normalizationMethod");
    if (normName.equals("simplemax")) {
      normMethod = Normalization.SIMPLEMAX;
    } else if (normName.equals("softmax")) {
      normMethod = Normalization.SOFTMAX;
    } else if (normName.equals("logit")) {
      normMethod = Normalization.LOGIT;
    } else if (normName.equals("probit")) {
      normMethod = Normalization.PROBIT;
    } else if (normName.equals("cloglog")) {
      normMethod = Normalization.CLOGLOG;
    } else if (normName.equals("exp")) {
      normMethod = Normalization.EXP;
    } else if (normName.equals("loglog")) {
      normMethod = Normalization.LOGLOG;
    } else if (normName.equals("cauchit")) {
      normMethod = Normalization.CAUCHIT;
    }
    return normMethod;
  }
  


  public String toString()
  {
    StringBuffer temp = new StringBuffer();
    temp.append("PMML version " + getPMMLVersion());
    if (!getCreatorApplication().equals("?")) {
      temp.append("\nApplication: " + getCreatorApplication());
    }
    if (m_algorithmName != null) {
      temp.append("\nPMML Model: " + m_algorithmName);
    }
    temp.append("\n\n");
    temp.append(m_miningSchema);
    
    for (RegressionTable table : m_regressionTables) {
      temp.append(table);
    }
    
    if (m_normalizationMethod != Normalization.NONE) {
      temp.append("Normalization: " + m_normalizationMethod);
    }
    temp.append("\n");
    
    return temp.toString();
  }
  







  public double[] distributionForInstance(Instance inst)
    throws Exception
  {
    if (!m_initialized) {
      mapToMiningSchema(inst.dataset());
    }
    double[] preds = null;
    if (m_miningSchema.getFieldsAsInstances().classAttribute().isNumeric()) {
      preds = new double[1];
    } else {
      preds = new double[m_miningSchema.getFieldsAsInstances().classAttribute().numValues()];
    }
    




    double[] incoming = m_fieldsMap.instanceToSchema(inst, m_miningSchema);
    














    boolean hasMissing = false;
    for (int i = 0; i < incoming.length; i++) {
      if ((i != m_miningSchema.getFieldsAsInstances().classIndex()) && (Instance.isMissingValue(incoming[i])))
      {
        hasMissing = true;
        break;
      }
    }
    
    if (hasMissing) {
      if (!m_miningSchema.hasTargetMetaData()) {
        String message = "[Regression] WARNING: Instance to predict has missing value(s) but there is no missing value handling meta data and no prior probabilities/default value to fall back to. No prediction will be made (" + ((m_miningSchema.getFieldsAsInstances().classAttribute().isNominal()) || (m_miningSchema.getFieldsAsInstances().classAttribute().isString()) ? "zero probabilities output)." : "NaN output).");
        






        if (m_log == null) {
          System.err.println(message);
        } else {
          m_log.logMessage(message);
        }
        if (m_miningSchema.getFieldsAsInstances().classAttribute().isNumeric()) {
          preds[0] = Instance.missingValue();
        }
        return preds;
      }
      
      TargetMetaInfo targetData = m_miningSchema.getTargetMetaData();
      if (m_miningSchema.getFieldsAsInstances().classAttribute().isNumeric()) {
        preds[0] = targetData.getDefaultValue();
      } else {
        Instances miningSchemaI = m_miningSchema.getFieldsAsInstances();
        for (int i = 0; i < miningSchemaI.classAttribute().numValues(); i++) {
          preds[i] = targetData.getPriorProbability(miningSchemaI.classAttribute().value(i));
        }
      }
      return preds;
    }
    

    for (int i = 0; i < m_regressionTables.length; i++) {
      m_regressionTables[i].predict(preds, incoming);
    }
    

    switch (1.$SwitchMap$weka$classifiers$pmml$consumer$Regression$Normalization[m_normalizationMethod.ordinal()])
    {
    case 1: 
      break;
    case 2: 
      Utils.normalize(preds);
      break;
    case 3: 
      for (int i = 0; i < preds.length; i++) {
        preds[i] = Math.exp(preds[i]);
      }
      if (preds.length == 1)
      {

        preds[0] /= (preds[0] + 1.0D);
      } else {
        Utils.normalize(preds);
      }
      break;
    case 4: 
      for (int i = 0; i < preds.length; i++) {
        preds[i] = (1.0D / (1.0D + Math.exp(-preds[i])));
      }
      Utils.normalize(preds);
      break;
    case 5: 
      for (int i = 0; i < preds.length; i++) {
        preds[i] = Maths.pnorm(preds[i]);
      }
      Utils.normalize(preds);
      break;
    
    case 6: 
      for (int i = 0; i < preds.length; i++) {
        preds[i] = (1.0D - Math.exp(-Math.exp(-preds[i])));
      }
      Utils.normalize(preds);
      break;
    case 7: 
      for (int i = 0; i < preds.length; i++) {
        preds[i] = Math.exp(preds[i]);
      }
      Utils.normalize(preds);
      break;
    
    case 8: 
      for (int i = 0; i < preds.length; i++) {
        preds[i] = Math.exp(-Math.exp(-preds[i]));
      }
      Utils.normalize(preds);
      break;
    case 9: 
      for (int i = 0; i < preds.length; i++) {
        preds[i] = (0.5D + 0.3183098861837907D * Math.atan(preds[i]));
      }
      Utils.normalize(preds);
      break;
    default: 
      throw new Exception("[Regression] unknown normalization method");
    }
    
    

    if ((m_miningSchema.getFieldsAsInstances().classAttribute().isNumeric()) && (m_miningSchema.hasTargetMetaData()))
    {
      TargetMetaInfo targetData = m_miningSchema.getTargetMetaData();
      preds[0] = targetData.applyMinMaxRescaleCast(preds[0]);
    }
    

    return preds;
  }
  


  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6019 $");
  }
}
