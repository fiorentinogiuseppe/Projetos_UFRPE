package weka.filters.unsupervised.attribute;

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
import weka.core.matrix.EigenvalueDecomposition;
import weka.core.matrix.Matrix;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;

















































































public class PrincipalComponents
  extends Filter
  implements OptionHandler, UnsupervisedFilter
{
  private static final long serialVersionUID = -5649876869480249303L;
  protected Instances m_TrainInstances;
  protected Instances m_TrainCopy;
  protected Instances m_TransformedFormat;
  protected boolean m_HasClass;
  protected int m_ClassIndex;
  protected int m_NumAttribs;
  protected int m_NumInstances;
  protected double[][] m_Correlation;
  private boolean m_center = false;
  


  protected double[][] m_Eigenvectors;
  

  protected double[] m_Eigenvalues = null;
  

  protected int[] m_SortedEigens;
  

  protected double m_SumOfEigenValues = 0.0D;
  

  protected ReplaceMissingValues m_ReplaceMissingFilter;
  

  protected NominalToBinary m_NominalToBinaryFilter;
  

  protected Remove m_AttributeFilter;
  

  protected Standardize m_standardizeFilter;
  

  protected Center m_centerFilter;
  

  protected int m_OutputNumAtts = -1;
  


  protected double m_CoverVariance = 0.95D;
  

  protected int m_MaxAttrsInName = 5;
  

  protected int m_MaxAttributes = -1;
  


  public PrincipalComponents() {}
  

  public String globalInfo()
  {
    return "Performs a principal components analysis and transformation of the data.\nDimensionality reduction is accomplished by choosing enough eigenvectors to account for some percentage of the variance in the original data -- default 0.95 (95%).\nBased on code of the attribute selection scheme 'PrincipalComponents' by Mark Hall and Gabi Schmidberger.";
  }
  











  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tCenter (rather than standardize) the\n\tdata and compute PCA using the covariance (rather\n\t than the correlation) matrix.", "C", 0, "-C"));
    



    result.addElement(new Option("\tRetain enough PC attributes to account\n\tfor this proportion of variance in the original data.\n\t(default: 0.95)", "R", 1, "-R <num>"));
    




    result.addElement(new Option("\tMaximum number of attributes to include in \n\ttransformed attribute names.\n\t(-1 = include all, default: 5)", "A", 1, "-A <num>"));
    




    result.addElement(new Option("\tMaximum number of PC attributes to retain.\n\t(-1 = include all, default: -1)", "M", 1, "-M <num>"));
    



    return result.elements();
  }
  




























  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('R', options);
    if (tmpStr.length() != 0) {
      setVarianceCovered(Double.parseDouble(tmpStr));
    } else {
      setVarianceCovered(0.95D);
    }
    tmpStr = Utils.getOption('A', options);
    if (tmpStr.length() != 0) {
      setMaximumAttributeNames(Integer.parseInt(tmpStr));
    } else {
      setMaximumAttributeNames(5);
    }
    tmpStr = Utils.getOption('M', options);
    if (tmpStr.length() != 0) {
      setMaximumAttributes(Integer.parseInt(tmpStr));
    } else {
      setMaximumAttributes(-1);
    }
    setCenterData(Utils.getFlag('C', options));
  }
  






  public String[] getOptions()
  {
    Vector<String> result = new Vector();
    
    result.add("-R");
    result.add("" + getVarianceCovered());
    
    result.add("-A");
    result.add("" + getMaximumAttributeNames());
    
    result.add("-M");
    result.add("" + getMaximumAttributes());
    
    if (getCenterData()) {
      result.add("-C");
    }
    return (String[])result.toArray(new String[result.size()]);
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
  





  public void setVarianceCovered(double value)
  {
    m_CoverVariance = value;
  }
  





  public double getVarianceCovered()
  {
    return m_CoverVariance;
  }
  





  public String maximumAttributeNamesTipText()
  {
    return "The maximum number of attributes to include in transformed attribute names.";
  }
  





  public void setMaximumAttributeNames(int value)
  {
    m_MaxAttrsInName = value;
  }
  





  public int getMaximumAttributeNames()
  {
    return m_MaxAttrsInName;
  }
  





  public String maximumAttributesTipText()
  {
    return "The maximum number of PC attributes to retain.";
  }
  




  public void setMaximumAttributes(int value)
  {
    m_MaxAttributes = value;
  }
  




  public int getMaximumAttributes()
  {
    return m_MaxAttributes;
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
    result.enable(Capabilities.Capability.UNARY_CLASS);
    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  





















  protected Instances determineOutputFormat(Instances inputFormat)
    throws Exception
  {
    if (m_Eigenvalues == null)
      return inputFormat;
    int numAttsLowerBound;
    int numAttsLowerBound; if (m_MaxAttributes > 0) {
      numAttsLowerBound = m_NumAttribs - m_MaxAttributes;
    } else
      numAttsLowerBound = 0;
    if (numAttsLowerBound < 0) {
      numAttsLowerBound = 0;
    }
    double cumulative = 0.0D;
    FastVector attributes = new FastVector();
    for (int i = m_NumAttribs - 1; i >= numAttsLowerBound; i--) {
      StringBuffer attName = new StringBuffer();
      
      double[] coeff_mags = new double[m_NumAttribs];
      for (int j = 0; j < m_NumAttribs; j++)
        coeff_mags[j] = (-Math.abs(m_Eigenvectors[j][m_SortedEigens[i]]));
      int num_attrs = m_MaxAttrsInName > 0 ? Math.min(m_NumAttribs, m_MaxAttrsInName) : m_NumAttribs;
      int[] coeff_inds;
      int[] coeff_inds;
      if (m_NumAttribs > 0)
      {
        coeff_inds = Utils.sort(coeff_mags);
      }
      else
      {
        coeff_inds = new int[m_NumAttribs];
        for (j = 0; j < m_NumAttribs; j++) {
          coeff_inds[j] = j;
        }
      }
      for (j = 0; j < num_attrs; j++) {
        double coeff_value = m_Eigenvectors[coeff_inds[j]][m_SortedEigens[i]];
        if ((j > 0) && (coeff_value >= 0.0D))
          attName.append("+");
        attName.append(Utils.doubleToString(coeff_value, 5, 3) + inputFormat.attribute(coeff_inds[j]).name());
      }
      

      if (num_attrs < m_NumAttribs) {
        attName.append("...");
      }
      attributes.addElement(new Attribute(attName.toString()));
      cumulative += m_Eigenvalues[m_SortedEigens[i]];
      
      if (cumulative / m_SumOfEigenValues >= m_CoverVariance) {
        break;
      }
    }
    if (m_HasClass) {
      attributes.addElement(m_TrainCopy.classAttribute().copy());
    }
    Instances outputFormat = new Instances(m_TrainCopy.relationName() + "_principal components", attributes, 0);
    



    if (m_HasClass) {
      outputFormat.setClassIndex(outputFormat.numAttributes() - 1);
    }
    m_OutputNumAtts = outputFormat.numAttributes();
    
    return outputFormat;
  }
  
  protected void fillCovariance() throws Exception
  {
    if (!m_center) {
      fillCorrelation();
      return;
    }
    
    double[] att = new double[m_TrainInstances.numInstances()];
    

    m_centerFilter = new Center();
    m_centerFilter.setInputFormat(m_TrainInstances);
    m_TrainInstances = Filter.useFilter(m_TrainInstances, m_centerFilter);
    

    m_Correlation = new double[m_NumAttribs][m_NumAttribs];
    
    for (int i = 0; i < m_NumAttribs; i++) {
      for (int j = 0; j < m_NumAttribs; j++)
      {
        double cov = 0.0D;
        for (int k = 0; k < m_NumInstances; k++)
        {
          if (i == j) {
            cov += m_TrainInstances.instance(k).value(i) * m_TrainInstances.instance(k).value(i);
          }
          else {
            cov += m_TrainInstances.instance(k).value(i) * m_TrainInstances.instance(k).value(j);
          }
        }
        

        cov /= (m_TrainInstances.numInstances() - 1);
        m_Correlation[i][j] = cov;
        m_Correlation[j][i] = cov;
      }
    }
  }
  








  protected void fillCorrelation()
    throws Exception
  {
    m_Correlation = new double[m_NumAttribs][m_NumAttribs];
    double[] att1 = new double[m_NumInstances];
    double[] att2 = new double[m_NumInstances];
    
    for (int i = 0; i < m_NumAttribs; i++) {
      for (int j = 0; j < m_NumAttribs; j++) {
        for (int k = 0; k < m_NumInstances; k++) {
          att1[k] = m_TrainInstances.instance(k).value(i);
          att2[k] = m_TrainInstances.instance(k).value(j);
        }
        if (i == j) {
          m_Correlation[i][j] = 1.0D;
        }
        else {
          double corr = Utils.correlation(att1, att2, m_NumInstances);
          m_Correlation[i][j] = corr;
          m_Correlation[j][i] = corr;
        }
      }
    }
    

    m_standardizeFilter = new Standardize();
    m_standardizeFilter.setInputFormat(m_TrainInstances);
    m_TrainInstances = Filter.useFilter(m_TrainInstances, m_standardizeFilter);
  }
  














  protected Instance convertInstance(Instance instance)
    throws Exception
  {
    double[] newVals = new double[m_OutputNumAtts];
    Instance tempInst = (Instance)instance.copy();
    
    m_ReplaceMissingFilter.input(tempInst);
    m_ReplaceMissingFilter.batchFinished();
    tempInst = m_ReplaceMissingFilter.output();
    
    m_NominalToBinaryFilter.input(tempInst);
    m_NominalToBinaryFilter.batchFinished();
    tempInst = m_NominalToBinaryFilter.output();
    
    if (m_AttributeFilter != null) {
      m_AttributeFilter.input(tempInst);
      m_AttributeFilter.batchFinished();
      tempInst = m_AttributeFilter.output();
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
    
    if (m_HasClass)
      newVals[(m_OutputNumAtts - 1)] = instance.value(instance.classIndex());
    int numAttsLowerBound;
    int numAttsLowerBound; if (m_MaxAttributes > 0) {
      numAttsLowerBound = m_NumAttribs - m_MaxAttributes;
    } else
      numAttsLowerBound = 0;
    if (numAttsLowerBound < 0) {
      numAttsLowerBound = 0;
    }
    double cumulative = 0.0D;
    for (int i = m_NumAttribs - 1; i >= numAttsLowerBound; i--) {
      double tempval = 0.0D;
      for (int j = 0; j < m_NumAttribs; j++) {
        tempval += m_Eigenvectors[j][m_SortedEigens[i]] * tempInst.value(j);
      }
      newVals[(m_NumAttribs - i - 1)] = tempval;
      cumulative += m_Eigenvalues[m_SortedEigens[i]];
      if (cumulative / m_SumOfEigenValues >= m_CoverVariance)
        break;
    }
    Instance result;
    Instance result;
    if ((instance instanceof SparseInstance)) {
      result = new SparseInstance(instance.weight(), newVals);
    } else {
      result = new Instance(instance.weight(), newVals);
    }
    return result;
  }
  














  protected void setup(Instances instances)
    throws Exception
  {
    m_TrainInstances = new Instances(instances);
    


    m_TrainCopy = new Instances(m_TrainInstances, 0);
    
    m_ReplaceMissingFilter = new ReplaceMissingValues();
    m_ReplaceMissingFilter.setInputFormat(m_TrainInstances);
    m_TrainInstances = Filter.useFilter(m_TrainInstances, m_ReplaceMissingFilter);
    
    m_NominalToBinaryFilter = new NominalToBinary();
    m_NominalToBinaryFilter.setInputFormat(m_TrainInstances);
    m_TrainInstances = Filter.useFilter(m_TrainInstances, m_NominalToBinaryFilter);
    

    Vector<Integer> deleteCols = new Vector();
    for (int i = 0; i < m_TrainInstances.numAttributes(); i++) {
      if (m_TrainInstances.numDistinctValues(i) <= 1) {
        deleteCols.addElement(Integer.valueOf(i));
      }
    }
    if (m_TrainInstances.classIndex() >= 0)
    {
      m_HasClass = true;
      m_ClassIndex = m_TrainInstances.classIndex();
      deleteCols.addElement(new Integer(m_ClassIndex));
    }
    

    if (deleteCols.size() > 0) {
      m_AttributeFilter = new Remove();
      int[] todelete = new int[deleteCols.size()];
      for (i = 0; i < deleteCols.size(); i++)
        todelete[i] = ((Integer)deleteCols.elementAt(i)).intValue();
      m_AttributeFilter.setAttributeIndicesArray(todelete);
      m_AttributeFilter.setInvertSelection(false);
      m_AttributeFilter.setInputFormat(m_TrainInstances);
      m_TrainInstances = Filter.useFilter(m_TrainInstances, m_AttributeFilter);
    }
    

    getCapabilities().testWithFail(m_TrainInstances);
    
    m_NumInstances = m_TrainInstances.numInstances();
    m_NumAttribs = m_TrainInstances.numAttributes();
    

    fillCovariance();
    

    Matrix corr = new Matrix(m_Correlation);
    EigenvalueDecomposition eig = corr.eig();
    Matrix V = eig.getV();
    double[][] v = new double[m_NumAttribs][m_NumAttribs];
    for (i = 0; i < v.length; i++) {
      for (int j = 0; j < v[0].length; j++)
        v[i][j] = V.get(i, j);
    }
    m_Eigenvectors = ((double[][])v.clone());
    m_Eigenvalues = ((double[])eig.getRealEigenvalues().clone());
    

    for (i = 0; i < m_Eigenvalues.length; i++) {
      if (m_Eigenvalues[i] < 0.0D)
        m_Eigenvalues[i] = 0.0D;
    }
    m_SortedEigens = Utils.sort(m_Eigenvalues);
    m_SumOfEigenValues = Utils.sum(m_Eigenvalues);
    
    m_TransformedFormat = determineOutputFormat(m_TrainInstances);
    setOutputFormat(m_TransformedFormat);
    
    m_TrainInstances = null;
  }
  









  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    
    m_Eigenvalues = null;
    m_OutputNumAtts = -1;
    m_AttributeFilter = null;
    m_NominalToBinaryFilter = null;
    m_SumOfEigenValues = 0.0D;
    
    return false;
  }
  










  public boolean input(Instance instance)
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (isNewBatch()) {
      resetQueue();
      m_NewBatch = false;
    }
    
    if (isFirstBatchDone()) {
      Instance inst = convertInstance(instance);
      inst.setDataset(getOutputFormat());
      push(inst);
      return true;
    }
    
    bufferInput(instance);
    return false;
  }
  










  public boolean batchFinished()
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new NullPointerException("No input instance format defined");
    }
    Instances insts = getInputFormat();
    
    if (!isFirstBatchDone()) {
      setup(insts);
    }
    for (int i = 0; i < insts.numInstances(); i++) {
      Instance inst = convertInstance(insts.instance(i));
      inst.setDataset(getOutputFormat());
      push(inst);
    }
    
    flushInput();
    m_NewBatch = true;
    m_FirstBatchDone = true;
    
    return numPendingOutput() != 0;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 11449 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new PrincipalComponents(), args);
  }
}
