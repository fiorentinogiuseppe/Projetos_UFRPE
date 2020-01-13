package weka.filters.supervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.matrix.EigenvalueDecomposition;
import weka.core.matrix.Matrix;
import weka.filters.Filter;
import weka.filters.SimpleBatchFilter;
import weka.filters.SupervisedFilter;
import weka.filters.unsupervised.attribute.Center;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.unsupervised.attribute.Standardize;


















































































































public class PLSFilter
  extends SimpleBatchFilter
  implements SupervisedFilter, TechnicalInformationHandler
{
  static final long serialVersionUID = -3335106965521265631L;
  public static final int ALGORITHM_SIMPLS = 1;
  public static final int ALGORITHM_PLS1 = 2;
  public static final Tag[] TAGS_ALGORITHM = { new Tag(1, "SIMPLS"), new Tag(2, "PLS1") };
  

  public static final int PREPROCESSING_NONE = 0;
  

  public static final int PREPROCESSING_CENTER = 1;
  

  public static final int PREPROCESSING_STANDARDIZE = 2;
  

  public static final Tag[] TAGS_PREPROCESSING = { new Tag(0, "none"), new Tag(1, "center"), new Tag(2, "standardize") };
  





  protected int m_NumComponents = 20;
  

  protected int m_Algorithm = 2;
  

  protected Matrix m_PLS1_RegVector = null;
  

  protected Matrix m_PLS1_P = null;
  

  protected Matrix m_PLS1_W = null;
  

  protected Matrix m_PLS1_b_hat = null;
  

  protected Matrix m_SIMPLS_W = null;
  

  protected Matrix m_SIMPLS_B = null;
  

  protected boolean m_PerformPrediction = false;
  

  protected Filter m_Missing = null;
  

  protected boolean m_ReplaceMissing = true;
  

  protected Filter m_Filter = null;
  

  protected int m_Preprocessing = 1;
  

  protected double m_ClassMean = 0.0D;
  

  protected double m_ClassStdDev = 0.0D;
  





  public PLSFilter()
  {
    m_Missing = new ReplaceMissingValues();
    m_Filter = new Center();
  }
  





  public String globalInfo()
  {
    return "Runs Partial Least Square Regression over the given instances and computes the resulting beta matrix for prediction.\nBy default it replaces missing values and centers the data.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  














  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.BOOK);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Tormod Naes and Tomas Isaksson and Tom Fearn and Tony Davies");
    result.setValue(TechnicalInformation.Field.YEAR, "2002");
    result.setValue(TechnicalInformation.Field.TITLE, "A User Friendly Guide to Multivariate Calibration and Classification");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "NIR Publications");
    result.setValue(TechnicalInformation.Field.ISBN, "0-9528666-2-5");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.MISC);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "StatSoft, Inc.");
    additional.setValue(TechnicalInformation.Field.TITLE, "Partial Least Squares (PLS)");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "Electronic Textbook StatSoft");
    additional.setValue(TechnicalInformation.Field.HTTP, "http://www.statsoft.com/textbook/stpls.html");
    
    additional = result.add(TechnicalInformation.Type.MISC);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Bent Jorgensen and Yuri Goegebeur");
    additional.setValue(TechnicalInformation.Field.TITLE, "Module 7: Partial least squares regression I");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "ST02: Multivariate Data Analysis and Chemometrics");
    additional.setValue(TechnicalInformation.Field.HTTP, "http://statmaster.sdu.dk/courses/ST02/module07/");
    
    additional = result.add(TechnicalInformation.Type.ARTICLE);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "S. de Jong");
    additional.setValue(TechnicalInformation.Field.YEAR, "1993");
    additional.setValue(TechnicalInformation.Field.TITLE, "SIMPLS: an alternative approach to partial least squares regression");
    additional.setValue(TechnicalInformation.Field.JOURNAL, "Chemometrics and Intelligent Laboratory Systems");
    additional.setValue(TechnicalInformation.Field.VOLUME, "18");
    additional.setValue(TechnicalInformation.Field.PAGES, "251-263");
    
    return result;
  }
  










  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    result.addElement(new Option("\tThe number of components to compute.\n\t(default: 20)", "C", 1, "-C <num>"));
    



    result.addElement(new Option("\tUpdates the class attribute as well.\n\t(default: off)", "U", 0, "-U"));
    



    result.addElement(new Option("\tTurns replacing of missing values on.\n\t(default: off)", "M", 0, "-M"));
    



    String param = "";
    for (int i = 0; i < TAGS_ALGORITHM.length; i++) {
      if (i > 0)
        param = param + "|";
      SelectedTag tag = new SelectedTag(TAGS_ALGORITHM[i].getID(), TAGS_ALGORITHM);
      param = param + tag.getSelectedTag().getReadable();
    }
    result.addElement(new Option("\tThe algorithm to use.\n\t(default: PLS1)", "A", 1, "-A <" + param + ">"));
    



    param = "";
    for (i = 0; i < TAGS_PREPROCESSING.length; i++) {
      if (i > 0)
        param = param + "|";
      SelectedTag tag = new SelectedTag(TAGS_PREPROCESSING[i].getID(), TAGS_PREPROCESSING);
      param = param + tag.getSelectedTag().getReadable();
    }
    result.addElement(new Option("\tThe type of preprocessing that is applied to the data.\n\t(default: center)", "P", 1, "-P <" + param + ">"));
    



    return result.elements();
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-C");
    result.add("" + getNumComponents());
    
    if (getPerformPrediction()) {
      result.add("-U");
    }
    if (getReplaceMissing()) {
      result.add("-M");
    }
    result.add("-A");
    result.add("" + getAlgorithm().getSelectedTag().getReadable());
    
    result.add("-P");
    result.add("" + getPreprocessing().getSelectedTag().getReadable());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  


































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption("C", options);
    if (tmpStr.length() != 0) {
      setNumComponents(Integer.parseInt(tmpStr));
    } else {
      setNumComponents(20);
    }
    setPerformPrediction(Utils.getFlag("U", options));
    
    setReplaceMissing(Utils.getFlag("M", options));
    
    tmpStr = Utils.getOption("A", options);
    if (tmpStr.length() != 0) {
      setAlgorithm(new SelectedTag(tmpStr, TAGS_ALGORITHM));
    } else {
      setAlgorithm(new SelectedTag(2, TAGS_ALGORITHM));
    }
    tmpStr = Utils.getOption("P", options);
    if (tmpStr.length() != 0) {
      setPreprocessing(new SelectedTag(tmpStr, TAGS_PREPROCESSING));
    } else {
      setPreprocessing(new SelectedTag(1, TAGS_PREPROCESSING));
    }
  }
  




  public String numComponentsTipText()
  {
    return "The number of components to compute.";
  }
  




  public void setNumComponents(int value)
  {
    m_NumComponents = value;
  }
  




  public int getNumComponents()
  {
    return m_NumComponents;
  }
  





  public String performPredictionTipText()
  {
    return "Whether to update the class attribute with the predicted value.";
  }
  





  public void setPerformPrediction(boolean value)
  {
    m_PerformPrediction = value;
  }
  




  public boolean getPerformPrediction()
  {
    return m_PerformPrediction;
  }
  





  public String algorithmTipText()
  {
    return "Sets the type of algorithm to use.";
  }
  




  public void setAlgorithm(SelectedTag value)
  {
    if (value.getTags() == TAGS_ALGORITHM) {
      m_Algorithm = value.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getAlgorithm()
  {
    return new SelectedTag(m_Algorithm, TAGS_ALGORITHM);
  }
  





  public String replaceMissingTipText()
  {
    return "Whether to replace missing values.";
  }
  





  public void setReplaceMissing(boolean value)
  {
    m_ReplaceMissing = value;
  }
  





  public boolean getReplaceMissing()
  {
    return m_ReplaceMissing;
  }
  





  public String preprocessingTipText()
  {
    return "Sets the type of preprocessing to use.";
  }
  




  public void setPreprocessing(SelectedTag value)
  {
    if (value.getTags() == TAGS_PREPROCESSING) {
      m_Preprocessing = value.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getPreprocessing()
  {
    return new SelectedTag(m_Preprocessing, TAGS_PREPROCESSING);
  }
  













  protected Instances determineOutputFormat(Instances inputFormat)
    throws Exception
  {
    FastVector atts = new FastVector();
    String prefix = getAlgorithm().getSelectedTag().getReadable();
    for (int i = 0; i < getNumComponents(); i++)
      atts.addElement(new Attribute(prefix + "_" + (i + 1)));
    atts.addElement(new Attribute("Class"));
    Instances result = new Instances(prefix, atts, 0);
    result.setClassIndex(result.numAttributes() - 1);
    
    return result;
  }
  













  protected Matrix getX(Instances instances)
  {
    int clsIndex = instances.classIndex();
    double[][] x = new double[instances.numInstances()][];
    
    for (int i = 0; i < instances.numInstances(); i++) {
      double[] values = instances.instance(i).toDoubleArray();
      x[i] = new double[values.length - 1];
      
      int j = 0;
      for (int n = 0; n < values.length; n++) {
        if (n != clsIndex) {
          x[i][j] = values[n];
          j++;
        }
      }
    }
    
    Matrix result = new Matrix(x);
    
    return result;
  }
  









  protected Matrix getX(Instance instance)
  {
    double[][] x = new double[1][];
    double[] values = instance.toDoubleArray();
    x[0] = new double[values.length - 1];
    System.arraycopy(values, 0, x[0], 0, values.length - 1);
    
    Matrix result = new Matrix(x);
    
    return result;
  }
  









  protected Matrix getY(Instances instances)
  {
    double[][] y = new double[instances.numInstances()][1];
    for (int i = 0; i < instances.numInstances(); i++) {
      y[i][0] = instances.instance(i).classValue();
    }
    Matrix result = new Matrix(y);
    
    return result;
  }
  








  protected Matrix getY(Instance instance)
  {
    double[][] y = new double[1][1];
    y[0][0] = instance.classValue();
    
    Matrix result = new Matrix(y);
    
    return result;
  }
  

















  protected Instances toInstances(Instances header, Matrix x, Matrix y)
  {
    Instances result = new Instances(header, 0);
    
    int rows = x.getRowDimension();
    int cols = x.getColumnDimension();
    int clsIdx = header.classIndex();
    
    for (int i = 0; i < rows; i++) {
      double[] values = new double[cols + 1];
      int offset = 0;
      
      for (int n = 0; n < values.length; n++) {
        if (n == clsIdx) {
          offset--;
          values[n] = y.get(i, 0);
        }
        else {
          values[n] = x.get(i, n + offset);
        }
      }
      
      result.add(new Instance(1.0D, values));
    }
    
    return result;
  }
  









  protected Matrix columnAsVector(Matrix m, int columnIndex)
  {
    Matrix result = new Matrix(m.getRowDimension(), 1);
    
    for (int i = 0; i < m.getRowDimension(); i++) {
      result.set(i, 0, m.get(i, columnIndex));
    }
    return result;
  }
  







  protected void setVector(Matrix v, Matrix m, int columnIndex)
  {
    m.setMatrix(0, m.getRowDimension() - 1, columnIndex, columnIndex, v);
  }
  






  protected Matrix getVector(Matrix m, int columnIndex)
  {
    return m.getMatrix(0, m.getRowDimension() - 1, columnIndex, columnIndex);
  }
  










  protected Matrix getDominantEigenVector(Matrix m)
  {
    EigenvalueDecomposition eigendecomp = m.eig();
    double[] eigenvalues = eigendecomp.getRealEigenvalues();
    int index = Utils.maxIndex(eigenvalues);
    Matrix result = columnAsVector(eigendecomp.getV(), index);
    
    return result;
  }
  








  protected void normalizeVector(Matrix v)
  {
    double sum = 0.0D;
    for (int i = 0; i < v.getRowDimension(); i++)
      sum += v.get(i, 0) * v.get(i, 0);
    sum = StrictMath.sqrt(sum);
    

    for (i = 0; i < v.getRowDimension(); i++) {
      v.set(i, 0, v.get(i, 0) / sum);
    }
  }
  






  protected Instances processPLS1(Instances instances)
    throws Exception
  {
    Instances result;
    





    Instances result;
    




    if (!isFirstBatchDone())
    {
      Matrix X = getX(instances);
      Matrix y = getY(instances);
      Matrix X_trans = X.transpose();
      

      Matrix W = new Matrix(instances.numAttributes() - 1, getNumComponents());
      Matrix P = new Matrix(instances.numAttributes() - 1, getNumComponents());
      Matrix T = new Matrix(instances.numInstances(), getNumComponents());
      Matrix b_hat = new Matrix(getNumComponents(), 1);
      
      for (int j = 0; j < getNumComponents(); j++)
      {
        Matrix w = X_trans.times(y);
        normalizeVector(w);
        setVector(w, W, j);
        

        Matrix t = X.times(w);
        Matrix t_trans = t.transpose();
        setVector(t, T, j);
        

        double b = t_trans.times(y).get(0, 0) / t_trans.times(t).get(0, 0);
        b_hat.set(j, 0, b);
        

        Matrix p = X_trans.times(t).times(1.0D / t_trans.times(t).get(0, 0));
        Matrix p_trans = p.transpose();
        setVector(p, P, j);
        

        X = X.minus(t.times(p_trans));
        y = y.minus(t.times(b));
      }
      

      Matrix tmp = W.times(P.transpose().times(W).inverse());
      

      Matrix X_new = getX(instances).times(tmp);
      

      m_PLS1_RegVector = tmp.times(b_hat);
      

      m_PLS1_P = P;
      m_PLS1_W = W;
      m_PLS1_b_hat = b_hat;
      Instances result;
      if (getPerformPrediction()) {
        result = toInstances(getOutputFormat(), X_new, y);
      } else {
        result = toInstances(getOutputFormat(), X_new, getY(instances));
      }
    }
    else {
      result = new Instances(getOutputFormat());
      
      for (int i = 0; i < instances.numInstances(); i++)
      {
        Instances tmpInst = new Instances(instances, 0);
        tmpInst.add((Instance)instances.instance(i).copy());
        Matrix x = getX(tmpInst);
        Matrix X = new Matrix(1, getNumComponents());
        Matrix T = new Matrix(1, getNumComponents());
        
        for (int j = 0; j < getNumComponents(); j++) {
          setVector(x, X, j);
          
          Matrix t = x.times(getVector(m_PLS1_W, j));
          setVector(t, T, j);
          
          x = x.minus(getVector(m_PLS1_P, j).transpose().times(t.get(0, 0)));
        }
        
        if (getPerformPrediction()) {
          tmpInst = toInstances(getOutputFormat(), T, T.times(m_PLS1_b_hat));
        } else {
          tmpInst = toInstances(getOutputFormat(), T, getY(tmpInst));
        }
        result.add(tmpInst.instance(0));
      }
    }
    
    return result;
  }
  






  protected Instances processSIMPLS(Instances instances)
    throws Exception
  {
    Instances result;
    





    Instances result;
    




    if (!isFirstBatchDone())
    {
      Matrix X = getX(instances);
      Matrix X_trans = X.transpose();
      Matrix Y = getY(instances);
      Matrix A = X_trans.times(Y);
      Matrix M = X_trans.times(X);
      Matrix C = Matrix.identity(instances.numAttributes() - 1, instances.numAttributes() - 1);
      Matrix W = new Matrix(instances.numAttributes() - 1, getNumComponents());
      Matrix P = new Matrix(instances.numAttributes() - 1, getNumComponents());
      Matrix Q = new Matrix(1, getNumComponents());
      
      for (int h = 0; h < getNumComponents(); h++)
      {
        Matrix A_trans = A.transpose();
        Matrix q = getDominantEigenVector(A_trans.times(A));
        

        Matrix w = A.times(q);
        Matrix c = w.transpose().times(M).times(w);
        w = w.times(1.0D / StrictMath.sqrt(c.get(0, 0)));
        setVector(w, W, h);
        

        Matrix p = M.times(w);
        Matrix p_trans = p.transpose();
        setVector(p, P, h);
        

        q = A_trans.times(w);
        setVector(q, Q, h);
        

        Matrix v = C.times(p);
        normalizeVector(v);
        Matrix v_trans = v.transpose();
        

        C = C.minus(v.times(v_trans));
        M = M.minus(p.times(p_trans));
        

        A = C.times(A);
      }
      

      m_SIMPLS_W = W;
      Matrix T = X.times(m_SIMPLS_W);
      Matrix X_new = T;
      m_SIMPLS_B = W.times(Q.transpose());
      Matrix y;
      Matrix y; if (getPerformPrediction()) {
        y = T.times(P.transpose()).times(m_SIMPLS_B);
      } else {
        y = getY(instances);
      }
      result = toInstances(getOutputFormat(), X_new, y);
    }
    else {
      result = new Instances(getOutputFormat());
      
      Matrix X = getX(instances);
      Matrix X_new = X.times(m_SIMPLS_W);
      Matrix y;
      Matrix y; if (getPerformPrediction()) {
        y = X.times(m_SIMPLS_B);
      } else {
        y = getY(instances);
      }
      result = toInstances(getOutputFormat(), X_new, y);
    }
    
    return result;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.NUMERIC_CLASS);
    result.enable(Capabilities.Capability.DATE_CLASS);
    
    return result;
  }
  












  protected Instances process(Instances instances)
    throws Exception
  {
    Instances result = null;
    double[] clsValues;
    double[] clsValues;
    if (!getPerformPrediction()) {
      clsValues = instances.attributeToDoubleArray(instances.classIndex());
    } else {
      clsValues = null;
    }
    if (!isFirstBatchDone())
    {
      if (m_ReplaceMissing) {
        m_Missing.setInputFormat(instances);
      }
      switch (m_Preprocessing) {
      case 1: 
        m_ClassMean = instances.meanOrMode(instances.classIndex());
        m_ClassStdDev = 1.0D;
        m_Filter = new Center();
        ((Center)m_Filter).setIgnoreClass(true);
        break;
      case 2: 
        m_ClassMean = instances.meanOrMode(instances.classIndex());
        m_ClassStdDev = StrictMath.sqrt(instances.variance(instances.classIndex()));
        m_Filter = new Standardize();
        ((Standardize)m_Filter).setIgnoreClass(true);
        break;
      default: 
        m_ClassMean = 0.0D;
        m_ClassStdDev = 1.0D;
        m_Filter = null;
      }
      if (m_Filter != null) {
        m_Filter.setInputFormat(instances);
      }
    }
    
    if (m_ReplaceMissing)
      instances = Filter.useFilter(instances, m_Missing);
    if (m_Filter != null) {
      instances = Filter.useFilter(instances, m_Filter);
    }
    switch (m_Algorithm) {
    case 1: 
      result = processSIMPLS(instances);
      break;
    case 2: 
      result = processPLS1(instances);
      break;
    default: 
      throw new IllegalStateException("Algorithm type '" + m_Algorithm + "' is not recognized!");
    }
    
    


    for (int i = 0; i < result.numInstances(); i++) {
      if (!getPerformPrediction()) {
        result.instance(i).setClassValue(clsValues[i]);
      }
      else {
        double clsValue = result.instance(i).classValue();
        result.instance(i).setClassValue(clsValue * m_ClassStdDev + m_ClassMean);
      }
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5541 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new PLSFilter(), args);
  }
}
