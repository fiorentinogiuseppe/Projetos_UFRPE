package weka.attributeSelection;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.core.matrix.Matrix;
import weka.core.matrix.SingularValueDecomposition;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;


















































































public class LatentSemanticAnalysis
  extends UnsupervisedAttributeEvaluator
  implements AttributeTransformer, OptionHandler
{
  static final long serialVersionUID = -8712112988018106198L;
  private Instances m_trainInstances;
  private Instances m_trainHeader;
  private Instances m_transformedFormat;
  private boolean m_hasClass;
  private int m_classIndex;
  private int m_numAttributes;
  private int m_numInstances;
  private boolean m_transpose = false;
  

  private Matrix m_u = null;
  

  private Matrix m_s = null;
  

  private Matrix m_v = null;
  

  private Matrix m_transformationMatrix = null;
  
  private ReplaceMissingValues m_replaceMissingFilter;
  
  private Normalize m_normalizeFilter;
  
  private NominalToBinary m_nominalToBinaryFilter;
  
  private Remove m_attributeFilter;
  private int m_outputNumAttributes = -1;
  

  private boolean m_normalize = false;
  

  private double m_rank = 0.95D;
  

  private double m_sumSquaredSingularValues = 0.0D;
  

  private int m_actualRank = -1;
  

  private int m_maxAttributesInName = 5;
  

  public LatentSemanticAnalysis() {}
  

  public String globalInfo()
  {
    return "Performs latent semantic analysis and transformation of the data. Use in conjunction with a Ranker search. A low-rank approximation of the full data is found by either specifying the number of singular values to use or specifying a proportion of the singular values to cover.";
  }
  







  public Enumeration listOptions()
  {
    Vector options = new Vector(4);
    options.addElement(new Option("\tNormalize input data.", "N", 0, "-N"));
    
    options.addElement(new Option("\tRank approximation used in LSA. \n\tMay be actual number of LSA attributes \n\tto include (if greater than 1) or a \n\tproportion of total singular values to \n\taccount for (if between 0 and 1). \n\tA value less than or equal to zero means \n\tuse all latent variables.(default = 0.95)", "R", 1, "-R"));
    







    options.addElement(new Option("\tMaximum number of attributes to include\n\tin transformed attribute names.\n\t(-1 = include all)", "A", 1, "-A"));
    


    return options.elements();
  }
  
























  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    


    String optionString = Utils.getOption('R', options);
    if (optionString.length() != 0)
    {
      double temp = Double.valueOf(optionString).doubleValue();
      setRank(temp);
    }
    

    optionString = Utils.getOption('A', options);
    if (optionString.length() != 0) {
      setMaximumAttributeNames(Integer.parseInt(optionString));
    }
    

    setNormalize(Utils.getFlag('N', options));
  }
  


  private void resetOptions()
  {
    m_rank = 0.95D;
    m_normalize = true;
    m_maxAttributesInName = 5;
  }
  




  public String normalizeTipText()
  {
    return "Normalize input data.";
  }
  



  public void setNormalize(boolean newNormalize)
  {
    m_normalize = newNormalize;
  }
  



  public boolean getNormalize()
  {
    return m_normalize;
  }
  




  public String rankTipText()
  {
    return "Matrix rank to use for data reduction. Can be a proportion to indicate desired coverage";
  }
  




  public void setRank(double newRank)
  {
    m_rank = newRank;
  }
  



  public double getRank()
  {
    return m_rank;
  }
  




  public String maximumAttributeNamesTipText()
  {
    return "The maximum number of attributes to include in transformed attribute names.";
  }
  




  public void setMaximumAttributeNames(int newMaxAttributes)
  {
    m_maxAttributesInName = newMaxAttributes;
  }
  




  public int getMaximumAttributeNames()
  {
    return m_maxAttributesInName;
  }
  





  public String[] getOptions()
  {
    String[] options = new String[5];
    int current = 0;
    
    if (getNormalize()) {
      options[(current++)] = "-N";
    }
    
    options[(current++)] = "-R";
    options[(current++)] = ("" + getRank());
    
    options[(current++)] = "-A";
    options[(current++)] = ("" + getMaximumAttributeNames());
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  




  public void buildEvaluator(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    
    buildAttributeConstructor(data);
  }
  




  private void buildAttributeConstructor(Instances data)
    throws Exception
  {
    m_transpose = false;
    m_s = null;
    m_u = null;
    m_v = null;
    m_outputNumAttributes = -1;
    m_actualRank = -1;
    m_sumSquaredSingularValues = 0.0D;
    
    m_trainInstances = new Instances(data);
    m_trainHeader = null;
    
    m_attributeFilter = null;
    m_nominalToBinaryFilter = null;
    
    m_replaceMissingFilter = new ReplaceMissingValues();
    m_replaceMissingFilter.setInputFormat(m_trainInstances);
    m_trainInstances = Filter.useFilter(m_trainInstances, m_replaceMissingFilter);
    


    Vector attributesToRemove = new Vector();
    

    if (m_trainInstances.classIndex() >= 0)
    {
      m_hasClass = true;
      m_classIndex = m_trainInstances.classIndex();
      

      attributesToRemove.addElement(new Integer(m_classIndex));
    }
    

    m_trainHeader = new Instances(m_trainInstances, 0);
    

    if (m_normalize) {
      m_normalizeFilter = new Normalize();
      m_normalizeFilter.setInputFormat(m_trainInstances);
      m_trainInstances = Filter.useFilter(m_trainInstances, m_normalizeFilter);
    }
    

    m_nominalToBinaryFilter = new NominalToBinary();
    m_nominalToBinaryFilter.setInputFormat(m_trainInstances);
    m_trainInstances = Filter.useFilter(m_trainInstances, m_nominalToBinaryFilter);
    

    for (int i = 0; i < m_trainInstances.numAttributes(); i++) {
      if (m_trainInstances.numDistinctValues(i) <= 1) {
        attributesToRemove.addElement(new Integer(i));
      }
    }
    

    if (attributesToRemove.size() > 0) {
      m_attributeFilter = new Remove();
      int[] todelete = new int[attributesToRemove.size()];
      for (int i = 0; i < attributesToRemove.size(); i++) {
        todelete[i] = ((Integer)(Integer)attributesToRemove.elementAt(i)).intValue();
      }
      m_attributeFilter.setAttributeIndicesArray(todelete);
      m_attributeFilter.setInvertSelection(false);
      m_attributeFilter.setInputFormat(m_trainInstances);
      m_trainInstances = Filter.useFilter(m_trainInstances, m_attributeFilter);
    }
    

    getCapabilities().testWithFail(m_trainInstances);
    

    m_numInstances = m_trainInstances.numInstances();
    m_numAttributes = m_trainInstances.numAttributes();
    

    double[][] trainValues = new double[m_numAttributes][m_numInstances];
    for (int i = 0; i < m_numAttributes; i++) {
      trainValues[i] = m_trainInstances.attributeToDoubleArray(i);
    }
    Matrix trainMatrix = new Matrix(trainValues);
    
    if (m_numAttributes < m_numInstances) {
      m_transpose = true;
      trainMatrix = trainMatrix.transpose();
    }
    SingularValueDecomposition trainSVD = trainMatrix.svd();
    m_u = trainSVD.getU();
    m_s = trainSVD.getS();
    m_v = trainSVD.getV();
    

    int maxSingularValues = trainSVD.rank();
    double[] singularDiag = trainSVD.getSingularValues();
    
    for (int i = 0; i < singularDiag.length; i++)
    {
      m_sumSquaredSingularValues += singularDiag[i] * singularDiag[i];
    }
    if (maxSingularValues == 0)
    {
      m_s = null;
      m_u = null;
      m_v = null;
      m_sumSquaredSingularValues = 0.0D;
      
      throw new Exception("SVD computation produced no non-zero singular values.");
    }
    if ((m_rank > maxSingularValues) || (m_rank <= 0.0D)) {
      m_actualRank = maxSingularValues;
    } else if (m_rank < 1.0D) {
      double currentSumOfSquaredSingularValues = 0.0D;
      
      for (int i = 0; (i < singularDiag.length) && (m_actualRank == -1); i++)
      {
        currentSumOfSquaredSingularValues += singularDiag[i] * singularDiag[i];
        if (currentSumOfSquaredSingularValues / m_sumSquaredSingularValues >= m_rank) {
          m_actualRank = (i + 1);
        }
      }
    } else {
      m_actualRank = ((int)m_rank);
    }
    


    if (m_transpose) {
      Matrix tempMatrix = m_u;
      m_u = m_v;
      m_v = tempMatrix;
    }
    m_u = m_u.getMatrix(0, m_u.getRowDimension() - 1, 0, m_actualRank - 1);
    m_s = m_s.getMatrix(0, m_actualRank - 1, 0, m_actualRank - 1);
    m_v = m_v.getMatrix(0, m_v.getRowDimension() - 1, 0, m_actualRank - 1);
    m_transformationMatrix = m_u.times(m_s.inverse());
    

    m_transformedFormat = setOutputFormat();
  }
  




  private Instances setOutputFormat()
  {
    if (m_s == null) {
      return null;
    }
    

    if (m_hasClass) {
      m_outputNumAttributes = (m_actualRank + 1);
    } else {
      m_outputNumAttributes = m_actualRank;
    }
    int numAttributesInName = m_maxAttributesInName;
    if ((numAttributesInName <= 0) || (numAttributesInName >= m_numAttributes)) {
      numAttributesInName = m_numAttributes;
    }
    FastVector attributes = new FastVector(m_outputNumAttributes);
    for (int i = 0; i < m_actualRank; i++)
    {
      String attributeName = "";
      double[] attributeCoefficients = m_transformationMatrix.getMatrix(0, m_numAttributes - 1, i, i).getColumnPackedCopy();
      
      for (int j = 0; j < numAttributesInName; j++) {
        if (j > 0) {
          attributeName = attributeName + "+";
        }
        attributeName = attributeName + Utils.doubleToString(attributeCoefficients[j], 5, 3);
        attributeName = attributeName + m_trainInstances.attribute(j).name();
      }
      if (numAttributesInName < m_numAttributes) {
        attributeName = attributeName + "...";
      }
      
      attributes.addElement(new Attribute(attributeName));
    }
    
    if (m_hasClass) {
      attributes.addElement(m_trainHeader.classAttribute().copy());
    }
    
    Instances outputFormat = new Instances(m_trainInstances.relationName() + "_LSA", attributes, 0);
    
    m_outputNumAttributes = outputFormat.numAttributes();
    
    if (m_hasClass) {
      outputFormat.setClassIndex(m_outputNumAttributes - 1);
    }
    
    return outputFormat;
  }
  







  public Instances transformedHeader()
    throws Exception
  {
    if (m_s == null) {
      throw new Exception("Latent Semantic Analysis hasn't been successfully performed.");
    }
    return m_transformedFormat;
  }
  




  public Instances transformedData(Instances data)
    throws Exception
  {
    if (m_s == null) {
      throw new Exception("Latent Semantic Analysis hasn't been built yet");
    }
    
    Instances output = new Instances(m_transformedFormat, m_numInstances);
    


    for (int i = 0; i < data.numInstances(); i++) {
      Instance currentInstance = data.instance(i);
      
      double[] newValues = new double[m_outputNumAttributes];
      for (int j = 0; j < m_actualRank; j++) {
        newValues[j] = m_v.get(i, j);
      }
      if (m_hasClass) {
        newValues[(m_outputNumAttributes - 1)] = currentInstance.classValue();
      }
      Instance newInstance;
      Instance newInstance;
      if ((currentInstance instanceof SparseInstance)) {
        newInstance = new SparseInstance(currentInstance.weight(), newValues);
      } else {
        newInstance = new Instance(currentInstance.weight(), newValues);
      }
      output.add(newInstance);
    }
    
    return output;
  }
  






  public double evaluateAttribute(int att)
    throws Exception
  {
    if (m_s == null) {
      throw new Exception("Latent Semantic Analysis hasn't been successfully performed yet!");
    }
    


    return m_s.get(att, att) * m_s.get(att, att) / m_sumSquaredSingularValues;
  }
  




  public Instance convertInstance(Instance instance)
    throws Exception
  {
    if (m_s == null) {
      throw new Exception("convertInstance: Latent Semantic Analysis not performed yet.");
    }
    


    double[] newValues = new double[m_outputNumAttributes];
    

    Instance tempInstance = (Instance)instance.copy();
    if (!instance.dataset().equalHeaders(m_trainHeader)) {
      throw new Exception("Can't convert instance: headers don't match: LatentSemanticAnalysis");
    }
    

    m_replaceMissingFilter.input(tempInstance);
    m_replaceMissingFilter.batchFinished();
    tempInstance = m_replaceMissingFilter.output();
    
    if (m_normalize) {
      m_normalizeFilter.input(tempInstance);
      m_normalizeFilter.batchFinished();
      tempInstance = m_normalizeFilter.output();
    }
    
    m_nominalToBinaryFilter.input(tempInstance);
    m_nominalToBinaryFilter.batchFinished();
    tempInstance = m_nominalToBinaryFilter.output();
    
    if (m_attributeFilter != null) {
      m_attributeFilter.input(tempInstance);
      m_attributeFilter.batchFinished();
      tempInstance = m_attributeFilter.output();
    }
    

    if (m_hasClass) {
      newValues[(m_outputNumAttributes - 1)] = instance.classValue();
    }
    double[][] oldInstanceValues = new double[1][m_numAttributes];
    oldInstanceValues[0] = tempInstance.toDoubleArray();
    Matrix instanceVector = new Matrix(oldInstanceValues);
    instanceVector = instanceVector.times(m_transformationMatrix);
    for (int i = 0; i < m_actualRank; i++) {
      newValues[i] = instanceVector.get(0, i);
    }
    

    if ((instance instanceof SparseInstance)) {
      return new SparseInstance(instance.weight(), newValues);
    }
    return new Instance(instance.weight(), newValues);
  }
  




  public String toString()
  {
    if (m_s == null) {
      return "Latent Semantic Analysis hasn't been built yet!";
    }
    return "\tLatent Semantic Analysis Attribute Transformer\n\n" + lsaSummary();
  }
  





  private String lsaSummary()
  {
    StringBuffer result = new StringBuffer();
    

    result.append("Number of latent variables utilized: " + m_actualRank);
    

    result.append("\n\nSingularValue\tLatentVariable#\n");
    
    for (int i = 0; i < m_actualRank; i++) {
      result.append(Utils.doubleToString(m_s.get(i, i), 9, 5) + "\t" + (i + 1) + "\n");
    }
    

    result.append("\nAttribute vectors (left singular vectors) -- row vectors show\nthe relation between the original attributes and the latent \nvariables computed by the singular value decomposition:\n");
    

    for (int i = 0; i < m_actualRank; i++) {
      result.append("LatentVariable#" + (i + 1) + "\t");
    }
    result.append("AttributeName\n");
    for (int i = 0; i < m_u.getRowDimension(); i++) {
      for (int j = 0; j < m_u.getColumnDimension(); j++) {
        result.append(Utils.doubleToString(m_u.get(i, j), 9, 5) + "\t\t");
      }
      result.append(m_trainInstances.attribute(i).name() + "\n");
    }
    

    result.append("\n\nInstance vectors (right singular vectors) -- column\nvectors show the relation between the original instances and the\nlatent variables computed by the singular value decomposition:\n");
    

    for (int i = 0; i < m_numInstances; i++) {
      result.append("Instance#" + (i + 1) + "\t");
    }
    result.append("LatentVariable#\n");
    for (int i = 0; i < m_v.getColumnDimension(); i++) {
      for (int j = 0; j < m_v.getRowDimension(); j++)
      {

        result.append(Utils.doubleToString(m_v.get(j, i), 9, 5) + "\t");
      }
      result.append(i + 1 + "\n");
    }
    
    return result.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11821 $");
  }
  




  public static void main(String[] argv)
  {
    runEvaluator(new LatentSemanticAnalysis(), argv);
  }
}
