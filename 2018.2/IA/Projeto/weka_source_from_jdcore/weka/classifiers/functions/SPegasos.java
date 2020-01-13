package weka.classifiers.functions;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;











































































public class SPegasos
  extends Classifier
  implements TechnicalInformationHandler, UpdateableClassifier, OptionHandler
{
  private static final long serialVersionUID = -3732968666673530290L;
  protected ReplaceMissingValues m_replaceMissing;
  protected NominalToBinary m_nominalToBinary;
  protected Normalize m_normalize;
  protected double m_lambda = 1.0E-4D;
  


  protected double[] m_weights;
  


  protected double m_t;
  


  protected int m_epochs = 500;
  




  protected boolean m_dontNormalize = false;
  





  protected boolean m_dontReplaceMissing = false;
  
  protected Instances m_data;
  
  protected static final int HINGE = 0;
  protected static final int LOGLOSS = 1;
  
  public SPegasos() {}
  
  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.BINARY_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  





  public String lambdaTipText()
  {
    return "The regularization constant. (default = 0.0001)";
  }
  




  public void setLambda(double lambda)
  {
    m_lambda = lambda;
  }
  




  public double getLambda()
  {
    return m_lambda;
  }
  





  public String epochsTipText()
  {
    return "The number of epochs to perform (batch learning). The total number of iterations is epochs * num instances.";
  }
  






  public void setEpochs(int e)
  {
    m_epochs = e;
  }
  




  public int getEpochs()
  {
    return m_epochs;
  }
  




  public void setDontNormalize(boolean m)
  {
    m_dontNormalize = m;
  }
  




  public boolean getDontNormalize()
  {
    return m_dontNormalize;
  }
  





  public String dontNormalizeTipText()
  {
    return "Turn normalization off";
  }
  






  public void setDontReplaceMissing(boolean m)
  {
    m_dontReplaceMissing = m;
  }
  






  public boolean getDontReplaceMissing()
  {
    return m_dontReplaceMissing;
  }
  





  public String dontReplaceMissingTipText()
  {
    return "Turn off global replacement of missing values";
  }
  




  public void setLossFunction(SelectedTag function)
  {
    if (function.getTags() == TAGS_SELECTION) {
      m_loss = function.getSelectedTag().getID();
    }
  }
  




  public SelectedTag getLossFunction()
  {
    return new SelectedTag(m_loss, TAGS_SELECTION);
  }
  





  public String lossFunctionTipText()
  {
    return "The loss function to use. Hinge loss (SVM) or log loss (logistic regression).";
  }
  






  public Enumeration<Option> listOptions()
  {
    Vector<Option> newVector = new Vector();
    newVector.add(new Option("\tSet the loss function to minimize. 0 = hinge loss (SVM), 1 = log loss (logistic regression).\n\t(default = 0)", "F", 1, "-F"));
    

    newVector.add(new Option("\tThe lambda regularization constant (default = 0.0001)", "L", 1, "-L <double>"));
    

    newVector.add(new Option("\tThe number of epochs to perform (batch learning only, default = 500)", "E", 1, "-E <integer>"));
    

    newVector.add(new Option("\tDon't normalize the data", "N", 0, "-N"));
    newVector.add(new Option("\tDon't replace missing values", "M", 0, "-M"));
    
    return newVector.elements();
  }
  






















  public void setOptions(String[] options)
    throws Exception
  {
    reset();
    
    String lossString = Utils.getOption('F', options);
    if (lossString.length() != 0) {
      setLossFunction(new SelectedTag(Integer.parseInt(lossString), TAGS_SELECTION));
    }
    else {
      setLossFunction(new SelectedTag(0, TAGS_SELECTION));
    }
    
    String lambdaString = Utils.getOption('L', options);
    if (lambdaString.length() > 0) {
      setLambda(Double.parseDouble(lambdaString));
    }
    
    String epochsString = Utils.getOption("E", options);
    if (epochsString.length() > 0) {
      setEpochs(Integer.parseInt(epochsString));
    }
    
    setDontNormalize(Utils.getFlag("N", options));
    setDontReplaceMissing(Utils.getFlag('M', options));
  }
  




  public String[] getOptions()
  {
    ArrayList<String> options = new ArrayList();
    
    options.add("-F");options.add("" + getLossFunction().getSelectedTag().getID());
    options.add("-L");options.add("" + getLambda());
    options.add("-E");options.add("" + getEpochs());
    if (getDontNormalize()) {
      options.add("-N");
    }
    if (getDontReplaceMissing()) {
      options.add("-M");
    }
    
    return (String[])options.toArray(new String[1]);
  }
  




  public String globalInfo()
  {
    return "Implements the stochastic variant of the Pegasos (Primal Estimated sub-GrAdient SOlver for SVM) method of Shalev-Shwartz et al. (2007). This implementation globally replaces all missing values and transforms nominal attributes into binary ones. It also normalizes all attributes, so the coefficients in the output are based on the normalized data. For more information, see\n\n" + getTechnicalInformation().toString();
  }
  















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "S. Shalev-Shwartz and Y. Singer and N. Srebro");
    result.setValue(TechnicalInformation.Field.YEAR, "2007");
    result.setValue(TechnicalInformation.Field.TITLE, "Pegasos: Primal Estimated sub-GrAdient SOlver for SVM");
    
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "24th International Conference on MachineLearning");
    
    result.setValue(TechnicalInformation.Field.PAGES, "807-814");
    
    return result;
  }
  


  public void reset()
  {
    m_t = 2.0D;
    m_weights = null;
  }
  




  public void buildClassifier(Instances data)
    throws Exception
  {
    reset();
    

    getCapabilities().testWithFail(data);
    
    data = new Instances(data);
    data.deleteWithMissingClass();
    
    if ((data.numInstances() > 0) && (!m_dontReplaceMissing)) {
      m_replaceMissing = new ReplaceMissingValues();
      m_replaceMissing.setInputFormat(data);
      data = Filter.useFilter(data, m_replaceMissing);
    }
    

    boolean onlyNumeric = true;
    for (int i = 0; i < data.numAttributes(); i++) {
      if ((i != data.classIndex()) && 
        (!data.attribute(i).isNumeric())) {
        onlyNumeric = false;
        break;
      }
    }
    

    if (!onlyNumeric) {
      m_nominalToBinary = new NominalToBinary();
      m_nominalToBinary.setInputFormat(data);
      data = Filter.useFilter(data, m_nominalToBinary);
    }
    
    if ((!m_dontNormalize) && (data.numInstances() > 0))
    {
      m_normalize = new Normalize();
      m_normalize.setInputFormat(data);
      data = Filter.useFilter(data, m_normalize);
    }
    
    m_weights = new double[data.numAttributes() + 1];
    m_data = new Instances(data, 0);
    
    if (data.numInstances() > 0) {
      train(data);
    }
  }
  
  private void train(Instances data) throws Exception {
    for (int e = 0; e < m_epochs; e++) {
      for (int i = 0; i < data.numInstances(); i++) {
        updateClassifier(data.instance(i));
      }
    }
  }
  
  protected static double dotProd(Instance inst1, double[] weights, int classIndex) {
    double result = 0.0D;
    
    int n1 = inst1.numValues();
    int n2 = weights.length - 1;
    
    int p1 = 0; for (int p2 = 0; (p1 < n1) && (p2 < n2);) {
      int ind1 = inst1.index(p1);
      int ind2 = p2;
      if (ind1 == ind2) {
        if ((ind1 != classIndex) && (!inst1.isMissingSparse(p1))) {
          result += inst1.valueSparse(p1) * weights[p2];
        }
        p1++;
        p2++;
      } else if (ind1 > ind2) {
        p2++;
      } else {
        p1++;
      }
    }
    return result;
  }
  




  protected int m_loss = 0;
  

  public static final Tag[] TAGS_SELECTION = { new Tag(0, "Hinge loss (SVM)"), new Tag(1, "Log loss (logistic regression)") };
  


  protected double dloss(double z)
  {
    if (m_loss == 0) {
      return z < 1.0D ? 1.0D : 0.0D;
    }
    

    if (z < 0.0D) {
      return 1.0D / (Math.exp(z) + 1.0D);
    }
    double t = Math.exp(-z);
    return t / (t + 1.0D);
  }
  






  public void updateClassifier(Instance instance)
    throws Exception
  {
    if (!instance.classIsMissing())
    {
      double learningRate = 1.0D / (m_lambda * m_t);
      
      double scale = 1.0D - 1.0D / m_t;
      double y = instance.classValue() == 0.0D ? -1.0D : 1.0D;
      double wx = dotProd(instance, m_weights, instance.classIndex());
      double z = y * (wx + m_weights[(m_weights.length - 1)]);
      
      for (int j = 0; j < m_weights.length - 1; j++) {
        if (j != instance.classIndex()) {
          m_weights[j] *= scale;
        }
      }
      
      if ((m_loss == 1) || (z < 1.0D)) {
        double loss = dloss(z);
        int n1 = instance.numValues();
        for (int p1 = 0; p1 < n1; p1++) {
          int indS = instance.index(p1);
          if ((indS != instance.classIndex()) && (!instance.isMissingSparse(p1))) {
            double m = learningRate * loss * (instance.valueSparse(p1) * y);
            m_weights[indS] += m;
          }
        }
        

        m_weights[(m_weights.length - 1)] += learningRate * loss * y;
      }
      
      double norm = 0.0D;
      for (int k = 0; k < m_weights.length - 1; k++) {
        if (k != instance.classIndex()) {
          norm += m_weights[k] * m_weights[k];
        }
      }
      
      double scale2 = Math.min(1.0D, 1.0D / (m_lambda * norm));
      if (scale2 < 1.0D) {
        scale2 = Math.sqrt(scale2);
        for (int j = 0; j < m_weights.length - 1; j++) {
          if (j != instance.classIndex()) {
            m_weights[j] *= scale2;
          }
        }
      }
      m_t += 1.0D;
    }
  }
  





  public double[] distributionForInstance(Instance inst)
    throws Exception
  {
    double[] result = new double[2];
    
    if (m_replaceMissing != null) {
      m_replaceMissing.input(inst);
      inst = m_replaceMissing.output();
    }
    
    if (m_nominalToBinary != null) {
      m_nominalToBinary.input(inst);
      inst = m_nominalToBinary.output();
    }
    
    if (m_normalize != null) {
      m_normalize.input(inst);
      inst = m_normalize.output();
    }
    
    double wx = dotProd(inst, m_weights, inst.classIndex());
    double z = wx + m_weights[(m_weights.length - 1)];
    

    if (z <= 0.0D)
    {
      if (m_loss == 1) {
        result[0] = (1.0D / (1.0D + Math.exp(z)));
        result[1] = (1.0D - result[0]);
      } else {
        result[0] = 1.0D;
      }
    }
    else if (m_loss == 1) {
      result[1] = (1.0D / (1.0D + Math.exp(-z)));
      result[0] = (1.0D - result[1]);
    } else {
      result[1] = 1.0D;
    }
    
    return result;
  }
  




  public String toString()
  {
    if (m_weights == null) {
      return "SPegasos: No model built yet.\n";
    }
    StringBuffer buff = new StringBuffer();
    buff.append("Loss function: ");
    if (m_loss == 0) {
      buff.append("Hinge loss (SVM)\n\n");
    } else {
      buff.append("Log loss (logistic regression)\n\n");
    }
    int printed = 0;
    
    for (int i = 0; i < m_weights.length - 1; i++) {
      if (i != m_data.classIndex()) {
        if (printed > 0) {
          buff.append(" + ");
        } else {
          buff.append("   ");
        }
        
        buff.append(Utils.doubleToString(m_weights[i], 12, 4) + " " + (m_normalize != null ? "(normalized) " : "") + m_data.attribute(i).name() + "\n");
        


        printed++;
      }
    }
    
    if (m_weights[(m_weights.length - 1)] > 0.0D) {
      buff.append(" + " + Utils.doubleToString(m_weights[(m_weights.length - 1)], 12, 4));
    } else {
      buff.append(" - " + Utils.doubleToString(-m_weights[(m_weights.length - 1)], 12, 4));
    }
    
    return buff.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6580 $");
  }
  


  public static void main(String[] args)
  {
    runClassifier(new SPegasos(), args);
  }
}
