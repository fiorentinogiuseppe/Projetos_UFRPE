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
import weka.core.pmml.FieldMetaInfo.Optype;
import weka.core.pmml.MappingInfo;
import weka.core.pmml.MiningSchema;
import weka.core.pmml.PMMLUtils;
import weka.core.pmml.TargetMetaInfo;
import weka.gui.Logger;































public class GeneralRegression
  extends PMMLClassifier
  implements Serializable
{
  private static final long serialVersionUID = 2583880411828388959L;
  
  static enum ModelType
  {
    REGRESSION("regression"), 
    GENERALLINEAR("generalLinear"), 
    MULTINOMIALLOGISTIC("multinomialLogistic"), 
    ORDINALMULTINOMIAL("ordinalMultinomial"), 
    GENERALIZEDLINEAR("generalizedLinear");
    
    private final String m_stringVal;
    
    private ModelType(String name) { m_stringVal = name; }
    
    public String toString()
    {
      return m_stringVal;
    }
  }
  

  protected ModelType m_modelType = ModelType.REGRESSION;
  

  protected String m_modelName;
  

  protected String m_algorithmName;
  

  protected int m_functionType = 0;
  



  static abstract enum CumulativeLinkFunction
  {
    NONE("none"), 
    



    LOGIT("logit"), 
    



    PROBIT("probit"), 
    



    CLOGLOG("cloglog"), 
    



    LOGLOG("loglog"), 
    



    CAUCHIT("cauchit");
    





    private final String m_stringVal;
    





    abstract double eval(double paramDouble1, double paramDouble2);
    





    private CumulativeLinkFunction(String name)
    {
      m_stringVal = name;
    }
    


    public String toString()
    {
      return m_stringVal;
    }
  }
  

  protected CumulativeLinkFunction m_cumulativeLinkFunction = CumulativeLinkFunction.NONE;
  





  static abstract enum LinkFunction
  {
    NONE("none"), 
    




    CLOGLOG("cloglog"), 
    




    IDENTITY("identity"), 
    




    LOG("log"), 
    




    LOGC("logc"), 
    




    LOGIT("logit"), 
    




    LOGLOG("loglog"), 
    




    NEGBIN("negbin"), 
    




    ODDSPOWER("oddspower"), 
    






    POWER("power"), 
    






    PROBIT("probit");
    







    private final String m_stringVal;
    







    abstract double eval(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4, double paramDouble5);
    






    private LinkFunction(String name)
    {
      m_stringVal = name;
    }
    


    public String toString()
    {
      return m_stringVal;
    }
  }
  

  protected LinkFunction m_linkFunction = LinkFunction.NONE;
  protected double m_linkParameter = NaN.0D;
  protected String m_trialsVariable;
  protected double m_trialsValue = NaN.0D;
  



  static enum Distribution
  {
    NONE("none"), 
    NORMAL("normal"), 
    BINOMIAL("binomial"), 
    GAMMA("gamma"), 
    INVGAUSSIAN("igauss"), 
    NEGBINOMIAL("negbin"), 
    POISSON("poisson");
    
    private final String m_stringVal;
    
    private Distribution(String name) { m_stringVal = name; }
    



    public String toString()
    {
      return m_stringVal;
    }
  }
  

  protected Distribution m_distribution = Distribution.NORMAL;
  

  protected double m_distParameter = NaN.0D;
  



  protected String m_offsetVariable;
  


  protected double m_offsetValue = NaN.0D;
  



  static class Parameter
    implements Serializable
  {
    private static final long serialVersionUID = 6502780192411755341L;
    


    protected String m_name = null;
    protected String m_label = null;
    
    Parameter() {} }
  
  protected ArrayList<Parameter> m_parameterList = new ArrayList();
  

  static class Predictor
    implements Serializable
  {
    private static final long serialVersionUID = 6502780192411755341L;
    

    Predictor() {}
    

    protected String m_name = null;
    protected int m_miningSchemaIndex = -1;
    
    public String toString() {
      return m_name;
    }
  }
  

  protected ArrayList<Predictor> m_factorList = new ArrayList();
  

  protected ArrayList<Predictor> m_covariateList = new ArrayList();
  
  protected PPCell[][] m_ppMatrix;
  
  protected PCell[][] m_paramMatrix;
  

  static class PPCell
    implements Serializable
  {
    private static final long serialVersionUID = 6502780192411755341L;
    protected String m_predictorName = null;
    protected String m_parameterName = null;
    


    protected double m_value = 0.0D;
    



    protected String m_targetCategory = null;
    




    PPCell() {}
  }
  




  static class PCell
    implements Serializable
  {
    private static final long serialVersionUID = 6502780192411755341L;
    


    protected String m_targetCategory = null;
    protected String m_parameterName = null;
    
    protected double m_beta = 0.0D;
    
    protected int m_df = -1;
    






    PCell() {}
  }
  





  public GeneralRegression(Element model, Instances dataDictionary, MiningSchema miningSchema)
    throws Exception
  {
    super(dataDictionary, miningSchema);
    

    String mType = model.getAttribute("modelType");
    boolean found = false;
    for (ModelType m : ModelType.values()) {
      if (m.toString().equals(mType)) {
        m_modelType = m;
        found = true;
        break;
      }
    }
    if (!found) {
      throw new Exception("[GeneralRegression] unknown model type: " + mType);
    }
    
    if (m_modelType == ModelType.ORDINALMULTINOMIAL)
    {
      String cLink = model.getAttribute("cumulativeLink");
      found = false;
      for (CumulativeLinkFunction c : CumulativeLinkFunction.values()) {
        if (c.toString().equals(cLink)) {
          m_cumulativeLinkFunction = c;
          found = true;
          break;
        }
      }
      if (!found) {
        throw new Exception("[GeneralRegression] cumulative link function " + cLink);
      }
    } else if ((m_modelType == ModelType.GENERALIZEDLINEAR) || (m_modelType == ModelType.GENERALLINEAR))
    {

      String link = model.getAttribute("linkFunction");
      found = false;
      for (LinkFunction l : LinkFunction.values()) {
        if (l.toString().equals(link)) {
          m_linkFunction = l;
          found = true;
          break;
        }
      }
      if (!found) {
        throw new Exception("[GeneralRegression] unknown link function " + link);
      }
      

      String linkP = model.getAttribute("linkParameter");
      if ((linkP != null) && (linkP.length() > 0)) {
        try {
          m_linkParameter = Double.parseDouble(linkP);
        } catch (IllegalArgumentException ex) {
          throw new Exception("[GeneralRegression] unable to parse the link parameter");
        }
      }
      

      String trials = model.getAttribute("trialsVariable");
      if ((trials != null) && (trials.length() > 0)) {
        m_trialsVariable = trials;
      }
      

      String trialsV = model.getAttribute("trialsValue");
      if ((trialsV != null) && (trialsV.length() > 0)) {
        try {
          m_trialsValue = Double.parseDouble(trialsV);
        } catch (IllegalArgumentException ex) {
          throw new Exception("[GeneralRegression] unable to parse the trials value");
        }
      }
    }
    
    String mName = model.getAttribute("modelName");
    if ((mName != null) && (mName.length() > 0)) {
      m_modelName = mName;
    }
    
    String fName = model.getAttribute("functionName");
    if (fName.equals("classification")) {
      m_functionType = 1;
    }
    
    String algName = model.getAttribute("algorithmName");
    if ((algName != null) && (algName.length() > 0)) {
      m_algorithmName = algName;
    }
    
    String distribution = model.getAttribute("distribution");
    if ((distribution != null) && (distribution.length() > 0)) {
      found = false;
      for (Distribution d : Distribution.values()) {
        if (d.toString().equals(distribution)) {
          m_distribution = d;
          found = true;
          break;
        }
      }
      if (!found) {
        throw new Exception("[GeneralRegression] unknown distribution type " + distribution);
      }
    }
    
    String distP = model.getAttribute("distParameter");
    if ((distP != null) && (distP.length() > 0)) {
      try {
        m_distParameter = Double.parseDouble(distP);
      } catch (IllegalArgumentException ex) {
        throw new Exception("[GeneralRegression] unable to parse the distribution parameter");
      }
    }
    
    String offsetV = model.getAttribute("offsetVariable");
    if ((offsetV != null) && (offsetV.length() > 0)) {
      m_offsetVariable = offsetV;
    }
    
    String offsetVal = model.getAttribute("offsetValue");
    if ((offsetVal != null) && (offsetVal.length() > 0)) {
      try {
        m_offsetValue = Double.parseDouble(offsetVal);
      } catch (IllegalArgumentException ex) {
        throw new Exception("[GeneralRegression] unable to parse the offset value");
      }
    }
    

    readParameterList(model);
    

    readFactorsAndCovariates(model, "FactorList");
    readFactorsAndCovariates(model, "CovariateList");
    

    readPPMatrix(model);
    

    readParamMatrix(model);
  }
  





  protected void readParameterList(Element model)
    throws Exception
  {
    NodeList paramL = model.getElementsByTagName("ParameterList");
    

    if (paramL.getLength() == 1) {
      Node paramN = paramL.item(0);
      if (paramN.getNodeType() == 1) {
        NodeList parameterList = ((Element)paramN).getElementsByTagName("Parameter");
        for (int i = 0; i < parameterList.getLength(); i++) {
          Node parameter = parameterList.item(i);
          if (parameter.getNodeType() == 1) {
            Parameter p = new Parameter();
            m_name = ((Element)parameter).getAttribute("name");
            String label = ((Element)parameter).getAttribute("label");
            if ((label != null) && (label.length() > 0)) {
              m_label = label;
            }
            m_parameterList.add(p);
          }
        }
      }
    } else {
      throw new Exception("[GeneralRegression] more than one parameter list!");
    }
  }
  









  protected void readFactorsAndCovariates(Element model, String factorOrCovariate)
    throws Exception
  {
    Instances miningSchemaI = m_miningSchema.getFieldsAsInstances();
    
    NodeList factorL = model.getElementsByTagName(factorOrCovariate);
    if (factorL.getLength() == 1) {
      Node factor = factorL.item(0);
      if (factor.getNodeType() == 1) {
        NodeList predL = ((Element)factor).getElementsByTagName("Predictor");
        for (int i = 0; i < predL.getLength(); i++) {
          Node pred = predL.item(i);
          if (pred.getNodeType() == 1) {
            Predictor p = new Predictor();
            m_name = ((Element)pred).getAttribute("name");
            
            boolean found = false;
            for (int j = 0; j < miningSchemaI.numAttributes(); j++) {
              if (miningSchemaI.attribute(j).name().equals(m_name)) {
                found = true;
                m_miningSchemaIndex = j;
                break;
              }
            }
            if (found) {
              if (factorOrCovariate.equals("FactorList")) {
                m_factorList.add(p);
              } else {
                m_covariateList.add(p);
              }
            } else {
              throw new Exception("[GeneralRegression] reading factors and covariates - unable to find predictor " + m_name + " in the mining schema");
            }
            
          }
        }
      }
    }
    else if (factorL.getLength() > 1) {
      throw new Exception("[GeneralRegression] more than one " + factorOrCovariate + "! ");
    }
  }
  





  protected void readPPMatrix(Element model)
    throws Exception
  {
    Instances miningSchemaI = m_miningSchema.getFieldsAsInstances();
    
    NodeList matrixL = model.getElementsByTagName("PPMatrix");
    

    if (matrixL.getLength() == 1)
    {


      m_ppMatrix = new PPCell[m_parameterList.size()][miningSchemaI.numAttributes()];
      
      Node ppM = matrixL.item(0);
      if (ppM.getNodeType() == 1) {
        NodeList cellL = ((Element)ppM).getElementsByTagName("PPCell");
        for (int i = 0; i < cellL.getLength(); i++) {
          Node cell = cellL.item(i);
          if (cell.getNodeType() == 1) {
            String predictorName = ((Element)cell).getAttribute("predictorName");
            String parameterName = ((Element)cell).getAttribute("parameterName");
            String value = ((Element)cell).getAttribute("value");
            double expOrIndex = -1.0D;
            int predictorIndex = -1;
            int parameterIndex = -1;
            for (int j = 0; j < m_parameterList.size(); j++) {
              if (m_parameterList.get(j)).m_name.equals(parameterName)) {
                parameterIndex = j;
                break;
              }
            }
            if (parameterIndex == -1) {
              throw new Exception("[GeneralRegression] unable to find parameter name " + parameterName + " in parameter list");
            }
            

            Predictor p = getCovariate(predictorName);
            if (p != null) {
              try {
                expOrIndex = Double.parseDouble(value);
                predictorIndex = m_miningSchemaIndex;
              } catch (IllegalArgumentException ex) {
                throw new Exception("[GeneralRegression] unable to parse PPCell value: " + value);
              }
            }
            else
            {
              p = getFactor(predictorName);
              if (p != null)
              {


                if (miningSchemaI.attribute(m_miningSchemaIndex).isNumeric())
                {

                  try
                  {
                    expOrIndex = Double.parseDouble(value);
                  } catch (IllegalArgumentException ex) {
                    throw new Exception("[GeneralRegresion] unable to parse PPCell value: " + value);
                  }
                  
                }
                else
                {
                  Attribute att = miningSchemaI.attribute(m_miningSchemaIndex);
                  expOrIndex = att.indexOfValue(value);
                  if (expOrIndex == -1.0D) {
                    throw new Exception("[GeneralRegression] unable to find PPCell value " + value + " in mining schema attribute " + att.name());
                  }
                  
                }
              }
              else {
                throw new Exception("[GeneralRegression] cant find predictor " + predictorName + "in either the factors list " + "or the covariates list");
              }
              

              predictorIndex = m_miningSchemaIndex;
            }
            

            PPCell ppc = new PPCell();
            m_predictorName = predictorName;m_parameterName = parameterName;
            m_value = expOrIndex;
            

            m_ppMatrix[parameterIndex][predictorIndex] = ppc;
          }
        }
      }
    } else {
      throw new Exception("[GeneralRegression] more than one PPMatrix!");
    }
  }
  
  private Predictor getCovariate(String predictorName) {
    for (int i = 0; i < m_covariateList.size(); i++) {
      if (predictorName.equals(m_covariateList.get(i)).m_name)) {
        return (Predictor)m_covariateList.get(i);
      }
    }
    return null;
  }
  
  private Predictor getFactor(String predictorName) {
    for (int i = 0; i < m_factorList.size(); i++) {
      if (predictorName.equals(m_factorList.get(i)).m_name)) {
        return (Predictor)m_factorList.get(i);
      }
    }
    return null;
  }
  






  private void readParamMatrix(Element model)
    throws Exception
  {
    Instances miningSchemaI = m_miningSchema.getFieldsAsInstances();
    Attribute classAtt = miningSchemaI.classAttribute();
    


    ArrayList<String> targetVals = null;
    
    NodeList matrixL = model.getElementsByTagName("ParamMatrix");
    if (matrixL.getLength() != 1) {
      throw new Exception("[GeneralRegression] more than one ParamMatrix!");
    }
    Element matrix = (Element)matrixL.item(0);
    



    if ((m_functionType == 1) && (classAtt.isNumeric()))
    {


      if (!m_miningSchema.hasTargetMetaData()) {
        throw new Exception("[GeneralRegression] function type is classification and class attribute in mining schema is numeric, however, there is no Target element specifying legal discrete values for the target!");
      }
      




      if (m_miningSchema.getTargetMetaData().getOptype() != FieldMetaInfo.Optype.CATEGORICAL)
      {
        throw new Exception("[GeneralRegression] function type is classification and class attribute in mining schema is numeric, however Target element in PMML does not have optype categorical!");
      }
      



      targetVals = m_miningSchema.getTargetMetaData().getValues();
      if (targetVals.size() == 0) {
        throw new Exception("[GeneralRegression] function type is classification and class attribute in mining schema is numeric, however Target element in PMML does not have any discrete values defined!");
      }
      




      m_miningSchema.convertNumericAttToNominal(miningSchemaI.classIndex(), targetVals);
    }
    

    m_paramMatrix = new PCell[classAtt.isNumeric() ? 1 : classAtt.numValues()][m_parameterList.size()];
    



    NodeList pcellL = matrix.getElementsByTagName("PCell");
    for (int i = 0; i < pcellL.getLength(); i++)
    {

      int targetCategoryIndex = -1;
      int parameterIndex = -1;
      Node pcell = pcellL.item(i);
      if (pcell.getNodeType() == 1) {
        String paramName = ((Element)pcell).getAttribute("parameterName");
        String targetCatName = ((Element)pcell).getAttribute("targetCategory");
        String coefficient = ((Element)pcell).getAttribute("beta");
        String df = ((Element)pcell).getAttribute("df");
        
        for (int j = 0; j < m_parameterList.size(); j++) {
          if (m_parameterList.get(j)).m_name.equals(paramName)) {
            parameterIndex = j;
            
            if (m_parameterList.get(j)).m_label == null) break;
            paramName = m_parameterList.get(j)).m_label; break;
          }
        }
        

        if (parameterIndex == -1) {
          throw new Exception("[GeneralRegression] unable to find parameter name " + paramName + " in parameter list");
        }
        

        if ((targetCatName != null) && (targetCatName.length() > 0)) {
          if ((classAtt.isNominal()) || (classAtt.isString())) {
            targetCategoryIndex = classAtt.indexOfValue(targetCatName);
          } else {
            throw new Exception("[GeneralRegression] found a PCell with a named target category: " + targetCatName + " but class attribute is numeric in " + "mining schema");
          }
        }
        



        PCell p = new PCell();
        if (targetCategoryIndex != -1) {
          m_targetCategory = targetCatName;
        }
        m_parameterName = paramName;
        try {
          m_beta = Double.parseDouble(coefficient);
        } catch (IllegalArgumentException ex) {
          throw new Exception("[GeneralRegression] unable to parse beta value " + coefficient + " as a double from PCell");
        }
        
        if ((df != null) && (df.length() > 0)) {
          try {
            m_df = Integer.parseInt(df);
          } catch (IllegalArgumentException ex) {
            throw new Exception("[GeneralRegression] unable to parse df value " + df + " as an int from PCell");
          }
        }
        

        if (targetCategoryIndex != -1) {
          m_paramMatrix[targetCategoryIndex][parameterIndex] = p;
        }
        else
        {
          for (int j = 0; j < m_paramMatrix.length; j++) {
            m_paramMatrix[j][parameterIndex] = p;
          }
        }
      }
    }
  }
  




  public String toString()
  {
    StringBuffer temp = new StringBuffer();
    temp.append("PMML version " + getPMMLVersion());
    if (!getCreatorApplication().equals("?")) {
      temp.append("\nApplication: " + getCreatorApplication());
    }
    temp.append("\nPMML Model: " + m_modelType);
    temp.append("\n\n");
    temp.append(m_miningSchema);
    
    if (m_factorList.size() > 0) {
      temp.append("Factors:\n");
      for (Predictor p : m_factorList) {
        temp.append("\t" + p + "\n");
      }
    }
    temp.append("\n");
    if (m_covariateList.size() > 0) {
      temp.append("Covariates:\n");
      for (Predictor p : m_covariateList) {
        temp.append("\t" + p + "\n");
      }
    }
    temp.append("\n");
    
    printPPMatrix(temp);
    temp.append("\n");
    printParameterMatrix(temp);
    

    temp.append("\n");
    
    if (m_linkFunction != LinkFunction.NONE) {
      temp.append("Link function: " + m_linkFunction);
      if (m_offsetVariable != null) {
        temp.append("\n\tOffset variable " + m_offsetVariable);
      } else if (!Double.isNaN(m_offsetValue)) {
        temp.append("\n\tOffset value " + m_offsetValue);
      }
      
      if (m_trialsVariable != null) {
        temp.append("\n\tTrials variable " + m_trialsVariable);
      } else if (!Double.isNaN(m_trialsValue)) {
        temp.append("\n\tTrials value " + m_trialsValue);
      }
      
      if (m_distribution != Distribution.NONE) {
        temp.append("\nDistribution: " + m_distribution);
      }
      
      if ((m_linkFunction == LinkFunction.NEGBIN) && (m_distribution == Distribution.NEGBINOMIAL) && (!Double.isNaN(m_distParameter)))
      {

        temp.append("\n\tDistribution parameter " + m_distParameter);
      }
      
      if ((m_linkFunction == LinkFunction.POWER) || (m_linkFunction == LinkFunction.ODDSPOWER))
      {
        if (!Double.isNaN(m_linkParameter)) {
          temp.append("\n\nLink parameter " + m_linkParameter);
        }
      }
    }
    
    if (m_cumulativeLinkFunction != CumulativeLinkFunction.NONE) {
      temp.append("Cumulative link function: " + m_cumulativeLinkFunction);
      
      if (m_offsetVariable != null) {
        temp.append("\n\tOffset variable " + m_offsetVariable);
      } else if (!Double.isNaN(m_offsetValue)) {
        temp.append("\n\tOffset value " + m_offsetValue);
      }
    }
    temp.append("\n");
    
    return temp.toString();
  }
  




  protected void printPPMatrix(StringBuffer buff)
  {
    Instances miningSchemaI = m_miningSchema.getFieldsAsInstances();
    int maxAttWidth = 0;
    for (int i = 0; i < miningSchemaI.numAttributes(); i++) {
      Attribute a = miningSchemaI.attribute(i);
      if (a.name().length() > maxAttWidth) {
        maxAttWidth = a.name().length();
      }
    }
    

    for (int i = 0; i < m_parameterList.size(); i++) {
      for (int j = 0; j < miningSchemaI.numAttributes(); j++) {
        if (m_ppMatrix[i][j] != null) {
          double width = Math.log(Math.abs(m_ppMatrix[i][j].m_value)) / Math.log(10.0D);
          
          if (width < 0.0D) {
            width = 1.0D;
          }
          
          width += 2.0D;
          if ((int)width > maxAttWidth) {
            maxAttWidth = (int)width;
          }
          if ((miningSchemaI.attribute(j).isNominal()) || (miningSchemaI.attribute(j).isString()))
          {

            String val = miningSchemaI.attribute(j).value((int)m_ppMatrix[i][j].m_value) + " ";
            if (val.length() > maxAttWidth) {
              maxAttWidth = val.length();
            }
          }
        }
      }
    }
    

    int maxParamWidth = "Parameter  ".length();
    for (Parameter p : m_parameterList) {
      String temp = m_name + " ";
      


      if (temp.length() > maxParamWidth) {
        maxParamWidth = temp.length();
      }
    }
    
    buff.append("Predictor-to-Parameter matrix:\n");
    buff.append(PMMLUtils.pad("Predictor", " ", maxParamWidth + (maxAttWidth * 2 + 2) - "Predictor".length(), true));
    
    buff.append("\n" + PMMLUtils.pad("Parameter", " ", maxParamWidth - "Parameter".length(), false));
    
    for (int i = 0; i < miningSchemaI.numAttributes(); i++) {
      if (i != miningSchemaI.classIndex()) {
        String attName = miningSchemaI.attribute(i).name();
        buff.append(PMMLUtils.pad(attName, " ", maxAttWidth + 1 - attName.length(), true));
      }
    }
    buff.append("\n");
    
    for (int i = 0; i < m_parameterList.size(); i++) {
      Parameter param = (Parameter)m_parameterList.get(i);
      String paramS = m_label != null ? m_label : m_name;
      

      buff.append(PMMLUtils.pad(paramS, " ", maxParamWidth - paramS.length(), false));
      
      for (int j = 0; j < miningSchemaI.numAttributes(); j++) {
        if (j != miningSchemaI.classIndex()) {
          PPCell p = m_ppMatrix[i][j];
          String val = " ";
          if (p != null) {
            if ((miningSchemaI.attribute(j).isNominal()) || (miningSchemaI.attribute(j).isString()))
            {
              val = miningSchemaI.attribute(j).value((int)m_value);
            } else {
              val = "" + Utils.doubleToString(m_value, maxAttWidth, 4).trim();
            }
          }
          buff.append(PMMLUtils.pad(val, " ", maxAttWidth + 1 - val.length(), true));
        }
      }
      buff.append("\n");
    }
  }
  




  protected void printParameterMatrix(StringBuffer buff)
  {
    Instances miningSchemaI = m_miningSchema.getFieldsAsInstances();
    

    int maxClassWidth = miningSchemaI.classAttribute().name().length();
    if ((miningSchemaI.classAttribute().isNominal()) || (miningSchemaI.classAttribute().isString()))
    {
      for (int i = 0; i < miningSchemaI.classAttribute().numValues(); i++) {
        if (miningSchemaI.classAttribute().value(i).length() > maxClassWidth) {
          maxClassWidth = miningSchemaI.classAttribute().value(i).length();
        }
      }
    }
    

    int maxParamWidth = 0;
    for (int i = 0; i < m_parameterList.size(); i++) {
      Parameter p = (Parameter)m_parameterList.get(i);
      String val = m_name + " ";
      

      if (val.length() > maxParamWidth) {
        maxParamWidth = val.length();
      }
    }
    

    int maxBetaWidth = "Coeff.".length();
    for (int i = 0; i < m_paramMatrix.length; i++) {
      for (int j = 0; j < m_parameterList.size(); j++) {
        PCell p = m_paramMatrix[i][j];
        if (p != null) {
          double width = Math.log(Math.abs(m_beta)) / Math.log(10.0D);
          if (width < 0.0D) {
            width = 1.0D;
          }
          
          width += 7.0D;
          if ((int)width > maxBetaWidth) {
            maxBetaWidth = (int)width;
          }
        }
      }
    }
    
    buff.append("Parameter estimates:\n");
    buff.append(PMMLUtils.pad(miningSchemaI.classAttribute().name(), " ", maxClassWidth + maxParamWidth + 2 - miningSchemaI.classAttribute().name().length(), false));
    

    buff.append(PMMLUtils.pad("Coeff.", " ", maxBetaWidth + 1 - "Coeff.".length(), true));
    buff.append(PMMLUtils.pad("df", " ", maxBetaWidth - "df".length(), true));
    buff.append("\n");
    for (int i = 0; i < m_paramMatrix.length; i++)
    {
      boolean ok = false;
      for (int j = 0; j < m_parameterList.size(); j++) {
        if (m_paramMatrix[i][j] != null) {
          ok = true;
        }
      }
      if (ok)
      {


        String cVal = (miningSchemaI.classAttribute().isNominal()) || (miningSchemaI.classAttribute().isString()) ? miningSchemaI.classAttribute().value(i) : " ";
        


        buff.append(PMMLUtils.pad(cVal, " ", maxClassWidth - cVal.length(), false));
        buff.append("\n");
        for (int j = 0; j < m_parameterList.size(); j++) {
          PCell p = m_paramMatrix[i][j];
          if (p != null) {
            String label = m_parameterName;
            buff.append(PMMLUtils.pad(label, " ", maxClassWidth + maxParamWidth + 2 - label.length(), true));
            
            String betaS = Utils.doubleToString(m_beta, maxBetaWidth, 4).trim();
            buff.append(PMMLUtils.pad(betaS, " ", maxBetaWidth + 1 - betaS.length(), true));
            String dfS = Utils.doubleToString(m_df, maxBetaWidth, 4).trim();
            buff.append(PMMLUtils.pad(dfS, " ", maxBetaWidth - dfS.length(), true));
            buff.append("\n");
          }
        }
      }
    }
  }
  







  private double[] incomingParamVector(double[] incomingInst)
    throws Exception
  {
    Instances miningSchemaI = m_miningSchema.getFieldsAsInstances();
    double[] incomingPV = new double[m_parameterList.size()];
    
    for (int i = 0; i < m_parameterList.size(); i++)
    {


      incomingPV[i] = 1.0D;
      

      for (int j = 0; j < miningSchemaI.numAttributes(); j++) {
        PPCell cellEntry = m_ppMatrix[i][j];
        Predictor p = null;
        if (cellEntry != null) {
          if ((p = getFactor(m_predictorName)) != null) {
            if ((int)incomingInst[m_miningSchemaIndex] == (int)m_value) {
              incomingPV[i] *= 1.0D;
            } else {
              incomingPV[i] *= 0.0D;
            }
          } else if ((p = getCovariate(m_predictorName)) != null) {
            incomingPV[i] *= Math.pow(incomingInst[m_miningSchemaIndex], m_value);
          } else {
            throw new Exception("[GeneralRegression] can't find predictor " + m_predictorName + " in either the list of factors or covariates");
          }
        }
      }
    }
    

    return incomingPV;
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
      if ((i != m_miningSchema.getFieldsAsInstances().classIndex()) && (Double.isNaN(incoming[i])))
      {
        hasMissing = true;
        break;
      }
    }
    
    if (hasMissing) {
      if (!m_miningSchema.hasTargetMetaData()) {
        String message = "[GeneralRegression] WARNING: Instance to predict has missing value(s) but there is no missing value handling meta data and no prior probabilities/default value to fall back to. No prediction will be made (" + ((m_miningSchema.getFieldsAsInstances().classAttribute().isNominal()) || (m_miningSchema.getFieldsAsInstances().classAttribute().isString()) ? "zero probabilities output)." : "NaN output).");
        






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
    

    double[] inputParamVector = incomingParamVector(incoming);
    computeResponses(incoming, inputParamVector, preds);
    

    return preds;
  }
  










  private void computeResponses(double[] incomingInst, double[] incomingParamVector, double[] responses)
    throws Exception
  {
    for (int i = 0; i < responses.length; i++) {
      for (int j = 0; j < m_parameterList.size(); j++)
      {



        PCell p = m_paramMatrix[i][j];
        if (p == null) {
          responses[i] += 0.0D * incomingParamVector[j];
        } else {
          responses[i] += incomingParamVector[j] * m_beta;
        }
      }
    }
    
    switch (1.$SwitchMap$weka$classifiers$pmml$consumer$GeneralRegression$ModelType[m_modelType.ordinal()]) {
    case 1: 
      computeProbabilitiesMultinomialLogistic(responses);
      break;
    case 2: 
      break;
    
    case 3: 
    case 4: 
      if (m_linkFunction != LinkFunction.NONE) {
        computeResponseGeneralizedLinear(incomingInst, responses);
      } else {
        throw new Exception("[GeneralRegression] no link function specified!");
      }
      break;
    case 5: 
      if (m_cumulativeLinkFunction != CumulativeLinkFunction.NONE) {
        computeResponseOrdinalMultinomial(incomingInst, responses);
      } else {
        throw new Exception("[GeneralRegression] no cumulative link function specified!");
      }
      break;
    default: 
      throw new Exception("[GeneralRegression] unknown model type");
    }
    
  }
  



  private static void computeProbabilitiesMultinomialLogistic(double[] responses)
  {
    double[] r = (double[])responses.clone();
    for (int j = 0; j < r.length; j++) {
      double sum = 0.0D;
      boolean overflow = false;
      for (int k = 0; k < r.length; k++) {
        if (r[k] - r[j] > 700.0D) {
          overflow = true;
          break;
        }
        sum += Math.exp(r[k] - r[j]);
      }
      if (overflow) {
        responses[j] = 0.0D;
      } else {
        responses[j] = (1.0D / sum);
      }
    }
  }
  









  private void computeResponseGeneralizedLinear(double[] incomingInst, double[] responses)
    throws Exception
  {
    double[] r = (double[])responses.clone();
    
    double offset = 0.0D;
    if (m_offsetVariable != null) {
      Attribute offsetAtt = m_miningSchema.getFieldsAsInstances().attribute(m_offsetVariable);
      
      if (offsetAtt == null) {
        throw new Exception("[GeneralRegression] unable to find offset variable " + m_offsetVariable + " in the mining schema!");
      }
      
      offset = incomingInst[offsetAtt.index()];
    } else if (!Double.isNaN(m_offsetValue)) {
      offset = m_offsetValue;
    }
    
    double trials = 1.0D;
    if (m_trialsVariable != null) {
      Attribute trialsAtt = m_miningSchema.getFieldsAsInstances().attribute(m_trialsVariable);
      if (trialsAtt == null) {
        throw new Exception("[GeneralRegression] unable to find trials variable " + m_trialsVariable + " in the mining schema!");
      }
      
      trials = incomingInst[trialsAtt.index()];
    } else if (!Double.isNaN(m_trialsValue)) {
      trials = m_trialsValue;
    }
    
    double distParam = 0.0D;
    if ((m_linkFunction == LinkFunction.NEGBIN) && (m_distribution == Distribution.NEGBINOMIAL))
    {
      if (Double.isNaN(m_distParameter)) {
        throw new Exception("[GeneralRegression] no distribution parameter defined!");
      }
      distParam = m_distParameter;
    }
    
    double linkParam = 0.0D;
    if ((m_linkFunction == LinkFunction.POWER) || (m_linkFunction == LinkFunction.ODDSPOWER))
    {
      if (Double.isNaN(m_linkParameter)) {
        throw new Exception("[GeneralRegression] no link parameter defined!");
      }
      linkParam = m_linkParameter;
    }
    
    for (int i = 0; i < r.length; i++) {
      responses[i] = m_linkFunction.eval(r[i], offset, trials, distParam, linkParam);
    }
  }
  








  private void computeResponseOrdinalMultinomial(double[] incomingInst, double[] responses)
    throws Exception
  {
    double[] r = (double[])responses.clone();
    
    double offset = 0.0D;
    if (m_offsetVariable != null) {
      Attribute offsetAtt = m_miningSchema.getFieldsAsInstances().attribute(m_offsetVariable);
      
      if (offsetAtt == null) {
        throw new Exception("[GeneralRegression] unable to find offset variable " + m_offsetVariable + " in the mining schema!");
      }
      
      offset = incomingInst[offsetAtt.index()];
    } else if (!Double.isNaN(m_offsetValue)) {
      offset = m_offsetValue;
    }
    
    for (int i = 0; i < r.length; i++) {
      if (i == 0) {
        responses[i] = m_cumulativeLinkFunction.eval(r[i], offset);
      }
      else if (i == r.length - 1) {
        responses[i] = (1.0D - responses[(i - 1)]);
      } else {
        responses[i] = (m_cumulativeLinkFunction.eval(r[i], offset) - responses[(i - 1)]);
      }
    }
  }
  


  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5562 $");
  }
}
