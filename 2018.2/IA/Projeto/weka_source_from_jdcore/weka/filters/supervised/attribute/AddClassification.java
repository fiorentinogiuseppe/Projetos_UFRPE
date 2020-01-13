package weka.filters.supervised.attribute;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.core.WekaException;
import weka.filters.SimpleBatchFilter;







































































public class AddClassification
  extends SimpleBatchFilter
{
  private static final long serialVersionUID = -1931467132568441909L;
  protected Classifier m_Classifier = new ZeroR();
  

  protected File m_SerializedClassifierFile = new File(System.getProperty("user.dir"));
  

  protected Classifier m_ActualClassifier = null;
  

  protected Instances m_SerializedHeader = null;
  

  protected boolean m_OutputClassification = false;
  

  protected boolean m_RemoveOldClass = false;
  

  protected boolean m_OutputDistribution = false;
  

  protected boolean m_OutputErrorFlag = false;
  


  public AddClassification() {}
  

  public String globalInfo()
  {
    return "A filter for adding the classification, the class distribution and an error flag to a dataset with a classifier. The classifier is either trained on the data itself or provided as serialized model.";
  }
  










  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration en = super.listOptions();
    while (en.hasMoreElements()) {
      result.addElement(en.nextElement());
    }
    result.addElement(new Option("\tFull class name of classifier to use, followed\n\tby scheme options. eg:\n\t\t\"weka.classifiers.bayes.NaiveBayes -D\"\n\t(default: weka.classifiers.rules.ZeroR)", "W", 1, "-W <classifier specification>"));
    





    result.addElement(new Option("\tInstead of training a classifier on the data, one can also provide\n\ta serialized model and use that for tagging the data.", "serialized", 1, "-serialized <file>"));
    



    result.addElement(new Option("\tAdds an attribute with the actual classification.\n\t(default: off)", "classification", 0, "-classification"));
    



    result.addElement(new Option("\tRemoves the old class attribute.\n\t(default: off)", "remove-old-class", 0, "-remove-old-class"));
    



    result.addElement(new Option("\tAdds attributes with the distribution for all classes \n\t(for numeric classes this will be identical to the attribute \n\toutput with '-classification').\n\t(default: off)", "distribution", 0, "-distribution"));
    





    result.addElement(new Option("\tAdds an attribute indicating whether the classifier output \n\ta wrong classification (for numeric classes this is the numeric \n\tdifference).\n\t(default: off)", "error", 0, "-error"));
    





    return result.elements();
  }
  















































  public void setOptions(String[] options)
    throws Exception
  {
    setOutputClassification(Utils.getFlag("classification", options));
    
    setRemoveOldClass(Utils.getFlag("remove-old-class", options));
    
    setOutputDistribution(Utils.getFlag("distribution", options));
    
    setOutputErrorFlag(Utils.getFlag("error", options));
    
    boolean serializedModel = false;
    String tmpStr = Utils.getOption("serialized", options);
    if (tmpStr.length() != 0) {
      File file = new File(tmpStr);
      if (!file.exists()) {
        throw new FileNotFoundException("File '" + file.getAbsolutePath() + "' not found!");
      }
      if (file.isDirectory()) {
        throw new FileNotFoundException("'" + file.getAbsolutePath() + "' points to a directory not a file!");
      }
      setSerializedClassifierFile(file);
      serializedModel = true;
    }
    else {
      setSerializedClassifierFile(null);
    }
    
    if (!serializedModel) {
      tmpStr = Utils.getOption('W', options);
      if (tmpStr.length() == 0)
        tmpStr = ZeroR.class.getName();
      String[] tmpOptions = Utils.splitOptions(tmpStr);
      if (tmpOptions.length == 0)
        throw new Exception("Invalid classifier specification string");
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      setClassifier(Classifier.forName(tmpStr, tmpOptions));
    }
    
    super.setOptions(options);
  }
  









  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    if (getOutputClassification()) {
      result.add("-classification");
    }
    if (getRemoveOldClass()) {
      result.add("-remove-old-class");
    }
    if (getOutputDistribution()) {
      result.add("-distribution");
    }
    if (getOutputErrorFlag()) {
      result.add("-error");
    }
    File file = getSerializedClassifierFile();
    if ((file != null) && (!file.isDirectory())) {
      result.add("-serialized");
      result.add(file.getAbsolutePath());
    }
    else {
      result.add("-W");
      result.add(getClassifierSpec());
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  




  protected void reset()
  {
    super.reset();
    
    m_ActualClassifier = null;
    m_SerializedHeader = null;
  }
  








  protected Classifier getActualClassifier()
  {
    if (m_ActualClassifier == null) {
      try {
        File file = getSerializedClassifierFile();
        if (!file.isDirectory()) {
          ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
          m_ActualClassifier = ((Classifier)ois.readObject());
          m_SerializedHeader = null;
          try
          {
            m_SerializedHeader = ((Instances)ois.readObject());
          }
          catch (Exception e)
          {
            m_SerializedHeader = null;
          }
          ois.close();
        }
        else {
          m_ActualClassifier = Classifier.makeCopy(m_Classifier);
        }
      }
      catch (Exception e) {
        m_ActualClassifier = null;
        System.err.println("Failed to instantiate classifier:");
        e.printStackTrace();
      }
    }
    
    return m_ActualClassifier;
  }
  



  public Capabilities getCapabilities()
  {
    Capabilities result;
    


    if (getActualClassifier() == null) {
      Capabilities result = super.getCapabilities();
      result.disableAll();
    } else {
      result = getActualClassifier().getCapabilities();
    }
    
    result.setMinimumNumberInstances(0);
    
    return result;
  }
  





  public String classifierTipText()
  {
    return "The classifier to use for classification.";
  }
  




  public void setClassifier(Classifier value)
  {
    m_Classifier = value;
  }
  




  public Classifier getClassifier()
  {
    return m_Classifier;
  }
  








  protected String getClassifierSpec()
  {
    Classifier c = getClassifier();
    String result = c.getClass().getName();
    if ((c instanceof OptionHandler)) {
      result = result + " " + Utils.joinOptions(c.getOptions());
    }
    return result;
  }
  





  public String serializedClassifierFileTipText()
  {
    return "A file containing the serialized model of a trained classifier.";
  }
  






  public File getSerializedClassifierFile()
  {
    return m_SerializedClassifierFile;
  }
  






  public void setSerializedClassifierFile(File value)
  {
    if ((value == null) || (!value.exists())) {
      value = new File(System.getProperty("user.dir"));
    }
    m_SerializedClassifierFile = value;
  }
  





  public String outputClassificationTipText()
  {
    return "Whether to add an attribute with the actual classification.";
  }
  




  public boolean getOutputClassification()
  {
    return m_OutputClassification;
  }
  




  public void setOutputClassification(boolean value)
  {
    m_OutputClassification = value;
  }
  





  public String removeOldClassTipText()
  {
    return "Whether to remove the old class attribute.";
  }
  




  public boolean getRemoveOldClass()
  {
    return m_RemoveOldClass;
  }
  




  public void setRemoveOldClass(boolean value)
  {
    m_RemoveOldClass = value;
  }
  





  public String outputDistributionTipText()
  {
    return "Whether to add attributes with the distribution for all classes (for numeric classes this will be identical to the attribute output with 'outputClassification').";
  }
  







  public boolean getOutputDistribution()
  {
    return m_OutputDistribution;
  }
  




  public void setOutputDistribution(boolean value)
  {
    m_OutputDistribution = value;
  }
  





  public String outputErrorFlagTipText()
  {
    return "Whether to add an attribute indicating whether the classifier output a wrong classification (for numeric classes this is the numeric difference).";
  }
  







  public boolean getOutputErrorFlag()
  {
    return m_OutputErrorFlag;
  }
  




  public void setOutputErrorFlag(boolean value)
  {
    m_OutputErrorFlag = value;
  }
  
















  protected Instances determineOutputFormat(Instances inputFormat)
    throws Exception
  {
    int classindex = -1;
    

    FastVector atts = new FastVector();
    for (int i = 0; i < inputFormat.numAttributes(); i++)
    {
      if ((i != inputFormat.classIndex()) || (!getRemoveOldClass()))
      {

        if (i == inputFormat.classIndex())
          classindex = i;
        atts.addElement(inputFormat.attribute(i).copy());
      }
    }
    

    if (getOutputClassification())
    {
      if (classindex == -1)
        classindex = atts.size();
      atts.addElement(inputFormat.classAttribute().copy("classification"));
    }
    

    if (getOutputDistribution()) {
      if (inputFormat.classAttribute().isNominal()) {
        for (i = 0; i < inputFormat.classAttribute().numValues(); i++) {
          atts.addElement(new Attribute("distribution_" + inputFormat.classAttribute().value(i)));
        }
      }
      
      atts.addElement(new Attribute("distribution"));
    }
    


    if (getOutputErrorFlag()) {
      if (inputFormat.classAttribute().isNominal()) {
        FastVector values = new FastVector();
        values.addElement("no");
        values.addElement("yes");
        atts.addElement(new Attribute("error", values));
      }
      else {
        atts.addElement(new Attribute("error"));
      }
    }
    

    Instances result = new Instances(inputFormat.relationName(), atts, 0);
    result.setClassIndex(classindex);
    
    return result;
  }
  


















  protected Instances process(Instances instances)
    throws Exception
  {
    if (!isFirstBatchDone()) {
      getActualClassifier();
      if (!getSerializedClassifierFile().isDirectory())
      {
        if ((m_SerializedHeader != null) && (!m_SerializedHeader.equalHeaders(instances))) {
          throw new WekaException("Training header of classifier and filter dataset don't match:\n" + m_SerializedHeader.equalHeaders(instances));
        }
        
      }
      else {
        m_ActualClassifier.buildClassifier(instances);
      }
    }
    
    Instances result = getOutputFormat();
    

    for (int i = 0; i < instances.numInstances(); i++) {
      Instance oldInstance = instances.instance(i);
      double[] oldValues = oldInstance.toDoubleArray();
      double[] newValues = new double[result.numAttributes()];
      
      int start = oldValues.length;
      if (getRemoveOldClass()) {
        start--;
      }
      
      System.arraycopy(oldValues, 0, newValues, 0, start);
      


      if (getOutputClassification()) {
        newValues[start] = m_ActualClassifier.classifyInstance(oldInstance);
        start++;
      }
      

      if (getOutputDistribution()) {
        double[] distribution = m_ActualClassifier.distributionForInstance(oldInstance);
        for (int n = 0; n < distribution.length; n++) {
          newValues[start] = distribution[n];
          start++;
        }
      }
      

      if (getOutputErrorFlag()) {
        if (result.classAttribute().isNominal()) {
          if (oldInstance.classValue() == m_ActualClassifier.classifyInstance(oldInstance)) {
            newValues[start] = 0.0D;
          } else {
            newValues[start] = 1.0D;
          }
        } else {
          newValues[start] = (m_ActualClassifier.classifyInstance(oldInstance) - oldInstance.classValue());
        }
        start++;
      }
      Instance newInstance;
      Instance newInstance;
      if ((oldInstance instanceof SparseInstance)) {
        newInstance = new SparseInstance(oldInstance.weight(), newValues);
      } else {
        newInstance = new Instance(oldInstance.weight(), newValues);
      }
      
      copyValues(newInstance, false, oldInstance.dataset(), getOutputFormat());
      
      result.add(newInstance);
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6900 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new AddClassification(), args);
  }
}
