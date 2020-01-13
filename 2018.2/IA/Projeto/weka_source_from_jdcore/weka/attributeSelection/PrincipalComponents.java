package weka.attributeSelection;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Matrix;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Center;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.unsupervised.attribute.Standardize;

















































































public class PrincipalComponents
  extends UnsupervisedAttributeEvaluator
  implements AttributeTransformer, OptionHandler
{
  private static final long serialVersionUID = -3675307197777734007L;
  private Instances m_trainInstances;
  private Instances m_trainHeader;
  private Instances m_transformedFormat;
  private Instances m_originalSpaceFormat;
  private boolean m_hasClass;
  private int m_classIndex;
  private int m_numAttribs;
  private int m_numInstances;
  private double[][] m_correlation;
  private double[] m_means;
  private double[] m_stdDevs;
  private boolean m_center = false;
  


  private double[][] m_eigenvectors;
  

  private double[] m_eigenvalues = null;
  

  private int[] m_sortedEigens;
  

  private double m_sumOfEigenValues = 0.0D;
  
  private ReplaceMissingValues m_replaceMissingFilter;
  
  private NominalToBinary m_nominalToBinFilter;
  
  private Remove m_attributeFilter;
  
  private Center m_centerFilter;
  private Standardize m_standardizeFilter;
  private int m_outputNumAtts = -1;
  





  private double m_coverVariance = 0.95D;
  


  private boolean m_transBackToOriginal = false;
  

  private int m_maxAttrsInName = 5;
  

  private double[][] m_eTranspose;
  


  public PrincipalComponents() {}
  

  public String globalInfo()
  {
    return "Performs a principal components analysis and transformation of the data. Use in conjunction with a Ranker search. Dimensionality reduction is accomplished by choosing enough eigenvectors to account for some percentage of the variance in the original data---default 0.95 (95%). Attribute noise can be filtered by transforming to the PC space, eliminating some of the worst eigenvectors, and then transforming back to the original space.";
  }
  










  public Enumeration listOptions()
  {
    Vector newVector = new Vector(3);
    
    newVector.addElement(new Option("\tCenter (rather than standardize) the\n\tdata and compute PCA using the covariance (rather\n\t than the correlation) matrix.", "C", 0, "-C"));
    



    newVector.addElement(new Option("\tRetain enough PC attributes to account \n\tfor this proportion of variance in the original data.\n\t(default = 0.95)", "R", 1, "-R"));
    




    newVector.addElement(new Option("\tTransform through the PC space and \n\tback to the original space.", "O", 0, "-O"));
    


    newVector.addElement(new Option("\tMaximum number of attributes to include in \n\ttransformed attribute names. (-1 = include all)", "A", 1, "-A"));
    

    return newVector.elements();
  }
  


























  public void setOptions(String[] options)
    throws Exception
  {
    resetOptions();
    

    String optionString = Utils.getOption('R', options);
    if (optionString.length() != 0)
    {
      Double temp = Double.valueOf(optionString);
      setVarianceCovered(temp.doubleValue());
    }
    optionString = Utils.getOption('A', options);
    if (optionString.length() != 0) {
      setMaximumAttributeNames(Integer.parseInt(optionString));
    }
    
    setTransformBackToOriginal(Utils.getFlag('O', options));
    setCenterData(Utils.getFlag('C', options));
  }
  


  private void resetOptions()
  {
    m_coverVariance = 0.95D;
    m_sumOfEigenValues = 0.0D;
    m_transBackToOriginal = false;
  }
  




  public String centerDataTipText()
  {
    return "Center (rather than standardize) the data. PCA will be computed from the covariance (rather than correlation) matrix";
  }
  









  public void setCenterData(boolean center)
  {
    m_center = center;
  }
  







  public boolean getCenterData()
  {
    return m_center;
  }
  




  public String varianceCoveredTipText()
  {
    return "Retain enough PC attributes to account for this proportion of variance.";
  }
  





  public void setVarianceCovered(double vc)
  {
    m_coverVariance = vc;
  }
  




  public double getVarianceCovered()
  {
    return m_coverVariance;
  }
  




  public String maximumAttributeNamesTipText()
  {
    return "The maximum number of attributes to include in transformed attribute names.";
  }
  




  public void setMaximumAttributeNames(int m)
  {
    m_maxAttrsInName = m;
  }
  




  public int getMaximumAttributeNames()
  {
    return m_maxAttrsInName;
  }
  




  public String transformBackToOriginalTipText()
  {
    return "Transform through the PC space and back to the original space. If only the best n PCs are retained (by setting varianceCovered < 1) then this option will give a dataset in the original space but with less attribute noise.";
  }
  








  public void setTransformBackToOriginal(boolean b)
  {
    m_transBackToOriginal = b;
  }
  




  public boolean getTransformBackToOriginal()
  {
    return m_transBackToOriginal;
  }
  





  public String[] getOptions()
  {
    String[] options = new String[6];
    int current = 0;
    
    if (getCenterData()) {
      options[(current++)] = "-C";
    }
    
    options[(current++)] = "-R";
    options[(current++)] = ("" + getVarianceCovered());
    
    options[(current++)] = "-A";
    options[(current++)] = ("" + getMaximumAttributeNames());
    
    if (getTransformBackToOriginal()) {
      options[(current++)] = "-O";
    }
    
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
  
  private void buildAttributeConstructor(Instances data) throws Exception {
    m_eigenvalues = null;
    m_outputNumAtts = -1;
    m_attributeFilter = null;
    m_nominalToBinFilter = null;
    m_sumOfEigenValues = 0.0D;
    m_trainInstances = new Instances(data);
    


    m_trainHeader = new Instances(m_trainInstances, 0);
    
    m_replaceMissingFilter = new ReplaceMissingValues();
    m_replaceMissingFilter.setInputFormat(m_trainInstances);
    m_trainInstances = Filter.useFilter(m_trainInstances, m_replaceMissingFilter);
    







    m_nominalToBinFilter = new NominalToBinary();
    m_nominalToBinFilter.setInputFormat(m_trainInstances);
    m_trainInstances = Filter.useFilter(m_trainInstances, m_nominalToBinFilter);
    


    Vector deleteCols = new Vector();
    for (int i = 0; i < m_trainInstances.numAttributes(); i++) {
      if (m_trainInstances.numDistinctValues(i) <= 1) {
        deleteCols.addElement(new Integer(i));
      }
    }
    
    if (m_trainInstances.classIndex() >= 0)
    {
      m_hasClass = true;
      m_classIndex = m_trainInstances.classIndex();
      deleteCols.addElement(new Integer(m_classIndex));
    }
    

    if (deleteCols.size() > 0) {
      m_attributeFilter = new Remove();
      int[] todelete = new int[deleteCols.size()];
      for (int i = 0; i < deleteCols.size(); i++) {
        todelete[i] = ((Integer)(Integer)deleteCols.elementAt(i)).intValue();
      }
      m_attributeFilter.setAttributeIndicesArray(todelete);
      m_attributeFilter.setInvertSelection(false);
      m_attributeFilter.setInputFormat(m_trainInstances);
      m_trainInstances = Filter.useFilter(m_trainInstances, m_attributeFilter);
    }
    

    getCapabilities().testWithFail(m_trainInstances);
    
    m_numInstances = m_trainInstances.numInstances();
    m_numAttribs = m_trainInstances.numAttributes();
    

    fillCovariance();
    
    double[] d = new double[m_numAttribs];
    double[][] v = new double[m_numAttribs][m_numAttribs];
    
    Matrix corr = new Matrix(m_correlation);
    corr.eigenvalueDecomposition(v, d);
    m_eigenvectors = ((double[][])v.clone());
    m_eigenvalues = ((double[])d.clone());
    








    for (int i = 0; i < m_eigenvalues.length; i++) {
      if (m_eigenvalues[i] < 0.0D) {
        m_eigenvalues[i] = 0.0D;
      }
    }
    m_sortedEigens = Utils.sort(m_eigenvalues);
    m_sumOfEigenValues = Utils.sum(m_eigenvalues);
    
    m_transformedFormat = setOutputFormat();
    if (m_transBackToOriginal) {
      m_originalSpaceFormat = setOutputFormatOriginal();
      

      int numVectors = m_transformedFormat.classIndex() < 0 ? m_transformedFormat.numAttributes() : m_transformedFormat.numAttributes() - 1;
      


      double[][] orderedVectors = new double[m_eigenvectors.length][numVectors + 1];
      


      for (int i = m_numAttribs - 1; i > m_numAttribs - numVectors - 1; i--) {
        for (int j = 0; j < m_numAttribs; j++) {
          orderedVectors[j][(m_numAttribs - i)] = m_eigenvectors[j][m_sortedEigens[i]];
        }
      }
      


      int nr = orderedVectors.length;
      int nc = orderedVectors[0].length;
      m_eTranspose = new double[nc][nr];
      
      for (int i = 0; i < nc; i++) {
        for (int j = 0; j < nr; j++) {
          m_eTranspose[i][j] = orderedVectors[j][i];
        }
      }
    }
  }
  







  public Instances transformedHeader()
    throws Exception
  {
    if (m_eigenvalues == null) {
      throw new Exception("Principal components hasn't been built yet");
    }
    if (m_transBackToOriginal) {
      return m_originalSpaceFormat;
    }
    return m_transformedFormat;
  }
  




  public Instances transformedData(Instances data)
    throws Exception
  {
    if (m_eigenvalues == null) {
      throw new Exception("Principal components hasn't been built yet");
    }
    
    Instances output = null;
    
    if (m_transBackToOriginal) {
      output = new Instances(m_originalSpaceFormat);
    } else {
      output = new Instances(m_transformedFormat);
    }
    for (int i = 0; i < data.numInstances(); i++) {
      Instance converted = convertInstance(data.instance(i));
      output.add(converted);
    }
    
    return output;
  }
  







  public double evaluateAttribute(int att)
    throws Exception
  {
    if (m_eigenvalues == null) {
      throw new Exception("Principal components hasn't been built yet!");
    }
    
    if (m_transBackToOriginal) {
      return 1.0D;
    }
    

    double cumulative = 0.0D;
    for (int i = m_numAttribs - 1; i >= m_numAttribs - att - 1; i--) {
      cumulative += m_eigenvalues[m_sortedEigens[i]];
    }
    
    return 1.0D - cumulative / m_sumOfEigenValues;
  }
  
  private void fillCovariance() throws Exception
  {
    m_means = new double[m_trainInstances.numAttributes()];
    m_stdDevs = new double[m_trainInstances.numAttributes()];
    for (int i = 0; i < m_trainInstances.numAttributes(); i++) {
      m_means[i] = m_trainInstances.meanOrMode(i);
    }
    
    if (!m_center) {
      fillCorrelation();
      return;
    }
    
    double[] att = new double[m_trainInstances.numInstances()];
    

    m_centerFilter = new Center();
    m_centerFilter.setInputFormat(m_trainInstances);
    m_trainInstances = Filter.useFilter(m_trainInstances, m_centerFilter);
    

    m_correlation = new double[m_numAttribs][m_numAttribs];
    
    for (int i = 0; i < m_numAttribs; i++) {
      for (int j = 0; j < m_numAttribs; j++)
      {
        double cov = 0.0D;
        for (int k = 0; k < m_numInstances; k++)
        {
          if (i == j) {
            cov += m_trainInstances.instance(k).value(i) * m_trainInstances.instance(k).value(i);
          }
          else {
            cov += m_trainInstances.instance(k).value(i) * m_trainInstances.instance(k).value(j);
          }
        }
        

        cov /= (m_trainInstances.numInstances() - 1);
        m_correlation[i][j] = cov;
        m_correlation[j][i] = cov;
      }
    }
  }
  

  private void fillCorrelation()
    throws Exception
  {
    m_correlation = new double[m_numAttribs][m_numAttribs];
    double[] att1 = new double[m_numInstances];
    double[] att2 = new double[m_numInstances];
    

    for (int i = 0; i < m_numAttribs; i++) {
      for (int j = 0; j < m_numAttribs; j++) {
        for (int k = 0; k < m_numInstances; k++) {
          att1[k] = m_trainInstances.instance(k).value(i);
          att2[k] = m_trainInstances.instance(k).value(j);
        }
        if (i == j) {
          m_correlation[i][j] = 1.0D;
          
          m_stdDevs[i] = Math.sqrt(Utils.variance(att1));
        } else {
          double corr = Utils.correlation(att1, att2, m_numInstances);
          m_correlation[i][j] = corr;
          m_correlation[j][i] = corr;
        }
      }
    }
    

    m_standardizeFilter = new Standardize();
    m_standardizeFilter.setInputFormat(m_trainInstances);
    m_trainInstances = Filter.useFilter(m_trainInstances, m_standardizeFilter);
  }
  



  private String principalComponentsSummary()
  {
    StringBuffer result = new StringBuffer();
    double cumulative = 0.0D;
    Instances output = null;
    int numVectors = 0;
    try
    {
      output = setOutputFormat();
      numVectors = output.classIndex() < 0 ? output.numAttributes() : output.numAttributes() - 1;
    }
    catch (Exception ex) {}
    


    String corrCov = m_center ? "Covariance " : "Correlation ";
    result.append(corrCov + "matrix\n" + matrixToString(m_correlation) + "\n\n");
    
    result.append("eigenvalue\tproportion\tcumulative\n");
    for (int i = m_numAttribs - 1; i > m_numAttribs - numVectors - 1; i--) {
      cumulative += m_eigenvalues[m_sortedEigens[i]];
      result.append(Utils.doubleToString(m_eigenvalues[m_sortedEigens[i]], 9, 5) + "\t" + Utils.doubleToString(m_eigenvalues[m_sortedEigens[i]] / m_sumOfEigenValues, 9, 5) + "\t" + Utils.doubleToString(cumulative / m_sumOfEigenValues, 9, 5) + "\t" + output.attribute(m_numAttribs - i - 1).name() + "\n");
    }
    







    result.append("\nEigenvectors\n");
    for (int j = 1; j <= numVectors; j++) {
      result.append(" V" + j + '\t');
    }
    result.append("\n");
    for (int j = 0; j < m_numAttribs; j++)
    {
      for (int i = m_numAttribs - 1; i > m_numAttribs - numVectors - 1; i--) {
        result.append(Utils.doubleToString(m_eigenvectors[j][m_sortedEigens[i]], 7, 4) + "\t");
      }
      

      result.append(m_trainInstances.attribute(j).name() + '\n');
    }
    
    if (m_transBackToOriginal) {
      result.append("\nPC space transformed back to original space.\n(Note: can't evaluate attributes in the original space)\n");
    }
    

    return result.toString();
  }
  



  public String toString()
  {
    if (m_eigenvalues == null) {
      return "Principal components hasn't been built yet!";
    }
    return "\tPrincipal Components Attribute Transformer\n\n" + principalComponentsSummary();
  }
  






  private String matrixToString(double[][] matrix)
  {
    StringBuffer result = new StringBuffer();
    int last = matrix.length - 1;
    
    for (int i = 0; i <= last; i++) {
      for (int j = 0; j <= last; j++) {
        result.append(Utils.doubleToString(matrix[i][j], 6, 2) + " ");
        if (j == last) {
          result.append('\n');
        }
      }
    }
    return result.toString();
  }
  






  private Instance convertInstanceToOriginal(Instance inst)
    throws Exception
  {
    double[] newVals = null;
    
    if (m_hasClass) {
      newVals = new double[m_numAttribs + 1];
    } else {
      newVals = new double[m_numAttribs];
    }
    
    if (m_hasClass)
    {
      newVals[m_numAttribs] = inst.value(inst.numAttributes() - 1);
    }
    
    for (int i = 0; i < m_eTranspose[0].length; i++) {
      double tempval = 0.0D;
      for (int j = 1; j < m_eTranspose.length; j++) {
        tempval += m_eTranspose[j][i] * inst.value(j - 1);
      }
      
      newVals[i] = tempval;
      if (!m_center) {
        newVals[i] *= m_stdDevs[i];
      }
      newVals[i] += m_means[i];
    }
    
    if ((inst instanceof SparseInstance)) {
      return new SparseInstance(inst.weight(), newVals);
    }
    return new Instance(inst.weight(), newVals);
  }
  







  public Instance convertInstance(Instance instance)
    throws Exception
  {
    if (m_eigenvalues == null) {
      throw new Exception("convertInstance: Principal components not built yet");
    }
    

    double[] newVals = new double[m_outputNumAtts];
    Instance tempInst = (Instance)instance.copy();
    if (!instance.dataset().equalHeaders(m_trainHeader)) {
      throw new Exception("Can't convert instance: header's don't match: PrincipalComponents\nCan't convert instance: header's don't match.");
    }
    


    m_replaceMissingFilter.input(tempInst);
    m_replaceMissingFilter.batchFinished();
    tempInst = m_replaceMissingFilter.output();
    






    m_nominalToBinFilter.input(tempInst);
    m_nominalToBinFilter.batchFinished();
    tempInst = m_nominalToBinFilter.output();
    
    if (m_attributeFilter != null) {
      m_attributeFilter.input(tempInst);
      m_attributeFilter.batchFinished();
      tempInst = m_attributeFilter.output();
    }
    
    if (!m_center) {
      m_standardizeFilter.input(tempInst);
      m_standardizeFilter.batchFinished();
      tempInst = m_standardizeFilter.output();
    } else {
      m_centerFilter.input(tempInst);
      m_centerFilter.batchFinished();
      tempInst = m_centerFilter.output();
    }
    
    if (m_hasClass) {
      newVals[(m_outputNumAtts - 1)] = instance.value(instance.classIndex());
    }
    
    double cumulative = 0.0D;
    for (int i = m_numAttribs - 1; i >= 0; i--) {
      double tempval = 0.0D;
      for (int j = 0; j < m_numAttribs; j++) {
        tempval += m_eigenvectors[j][m_sortedEigens[i]] * tempInst.value(j);
      }
      
      newVals[(m_numAttribs - i - 1)] = tempval;
      cumulative += m_eigenvalues[m_sortedEigens[i]];
      if (cumulative / m_sumOfEigenValues >= m_coverVariance) {
        break;
      }
    }
    
    if (!m_transBackToOriginal) {
      if ((instance instanceof SparseInstance)) {
        return new SparseInstance(instance.weight(), newVals);
      }
      return new Instance(instance.weight(), newVals);
    }
    
    if ((instance instanceof SparseInstance)) {
      return convertInstanceToOriginal(new SparseInstance(instance.weight(), newVals));
    }
    
    return convertInstanceToOriginal(new Instance(instance.weight(), newVals));
  }
  







  private Instances setOutputFormatOriginal()
    throws Exception
  {
    FastVector attributes = new FastVector();
    
    for (int i = 0; i < m_numAttribs; i++) {
      String att = m_trainInstances.attribute(i).name();
      attributes.addElement(new Attribute(att));
    }
    
    if (m_hasClass) {
      attributes.addElement(m_trainHeader.classAttribute().copy());
    }
    
    Instances outputFormat = new Instances(m_trainHeader.relationName() + "->PC->original space", attributes, 0);
    



    if (m_hasClass) {
      outputFormat.setClassIndex(outputFormat.numAttributes() - 1);
    }
    
    return outputFormat;
  }
  



  private Instances setOutputFormat()
    throws Exception
  {
    if (m_eigenvalues == null) {
      return null;
    }
    
    double cumulative = 0.0D;
    FastVector attributes = new FastVector();
    for (int i = m_numAttribs - 1; i >= 0; i--) {
      StringBuffer attName = new StringBuffer();
      
      double[] coeff_mags = new double[m_numAttribs];
      for (int j = 0; j < m_numAttribs; j++)
        coeff_mags[j] = (-Math.abs(m_eigenvectors[j][m_sortedEigens[i]]));
      int num_attrs = m_maxAttrsInName > 0 ? Math.min(m_numAttribs, m_maxAttrsInName) : m_numAttribs;
      int[] coeff_inds;
      int[] coeff_inds;
      if (m_numAttribs > 0)
      {
        coeff_inds = Utils.sort(coeff_mags);
      }
      else {
        coeff_inds = new int[m_numAttribs];
        for (int j = 0; j < m_numAttribs; j++) {
          coeff_inds[j] = j;
        }
      }
      for (int j = 0; j < num_attrs; j++) {
        double coeff_value = m_eigenvectors[coeff_inds[j]][m_sortedEigens[i]];
        if ((j > 0) && (coeff_value >= 0.0D))
          attName.append("+");
        attName.append(Utils.doubleToString(coeff_value, 5, 3) + m_trainInstances.attribute(coeff_inds[j]).name());
      }
      
      if (num_attrs < m_numAttribs) {
        attName.append("...");
      }
      attributes.addElement(new Attribute(attName.toString()));
      cumulative += m_eigenvalues[m_sortedEigens[i]];
      
      if (cumulative / m_sumOfEigenValues >= m_coverVariance) {
        break;
      }
    }
    
    if (m_hasClass) {
      attributes.addElement(m_trainHeader.classAttribute().copy());
    }
    
    Instances outputFormat = new Instances(m_trainInstances.relationName() + "_principal components", attributes, 0);
    



    if (m_hasClass) {
      outputFormat.setClassIndex(outputFormat.numAttributes() - 1);
    }
    
    m_outputNumAtts = outputFormat.numAttributes();
    return outputFormat;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6690 $");
  }
  




  public static void main(String[] argv)
  {
    runEvaluator(new PrincipalComponents(), argv);
  }
}
