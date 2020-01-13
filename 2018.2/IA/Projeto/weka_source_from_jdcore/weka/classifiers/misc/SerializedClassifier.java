package weka.classifiers.misc;

import java.io.File;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.SerializationHelper;
import weka.core.Utils;

















































public class SerializedClassifier
  extends Classifier
{
  private static final long serialVersionUID = 4599593909947628642L;
  protected transient Classifier m_Model = null;
  

  protected File m_ModelFile = new File(System.getProperty("user.dir"));
  


  public SerializedClassifier() {}
  

  public String globalInfo()
  {
    return "A wrapper around a serialized classifier model. This classifier loads a serialized models and uses it to make predictions.\n\nWarning: since the serialized model doesn't get changed, cross-validation cannot bet used with this classifier.";
  }
  











  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    result.addElement(new Option("\tThe file containing the serialized model.\n\t(required)", "model", 1, "-model <filename>"));
    



    return result.elements();
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    result.add("-model");
    result.add("" + getModelFile());
    
    return (String[])result.toArray(new String[result.size()]);
  }
  



















  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    String tmpStr = Utils.getOption("model", options);
    if (tmpStr.length() != 0) {
      setModelFile(new File(tmpStr));
    } else {
      setModelFile(new File(System.getProperty("user.dir")));
    }
  }
  




  public String modelFileTipText()
  {
    return "The serialized classifier model to use for predictions.";
  }
  




  public File getModelFile()
  {
    return m_ModelFile;
  }
  




  public void setModelFile(File value)
  {
    m_ModelFile = value;
    
    if ((value.exists()) && (value.isFile())) {
      try {
        initModel();
      }
      catch (Exception e) {
        throw new IllegalArgumentException("Cannot load model from file '" + value + "': " + e);
      }
    }
  }
  






  public void setModel(Classifier value)
  {
    m_Model = value;
  }
  






  public Classifier getCurrentModel()
  {
    return m_Model;
  }
  




  protected void initModel()
    throws Exception
  {
    if (m_Model == null) {
      m_Model = ((Classifier)SerializationHelper.read(m_ModelFile.getAbsolutePath()));
    }
    m_Model.setDebug(getDebug());
  }
  







  public Capabilities getCapabilities()
  {
    if ((m_ModelFile != null) && (m_ModelFile.exists()) && (m_ModelFile.isFile()))
      try {
        initModel();
      }
      catch (Exception e) {
        System.err.println(e);
      }
    Capabilities result;
    Capabilities result;
    if (m_Model != null) {
      result = m_Model.getCapabilities();
    } else {
      result = new Capabilities(this);
      result.disableAll();
    }
    

    for (Capabilities.Capability cap : Capabilities.Capability.values()) {
      result.enableDependency(cap);
    }
    result.setOwner(this);
    
    return result;
  }
  









  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    initModel();
    
    double[] result = m_Model.distributionForInstance(instance);
    
    return result;
  }
  





  public void buildClassifier(Instances data)
    throws Exception
  {
    initModel();
    

    getCapabilities().testWithFail(data);
  }
  


  public String toString()
  {
    StringBuffer result;
    
    StringBuffer result;
    
    if (m_Model == null) {
      result = new StringBuffer("No model loaded yet.");
    }
    else {
      result = new StringBuffer();
      result.append("SerializedClassifier\n");
      result.append("====================\n\n");
      result.append("File: " + getModelFile() + "\n\n");
      result.append(m_Model.toString());
    }
    
    return result.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7560 $");
  }
  




  public static void main(String[] args)
  {
    runClassifier(new SerializedClassifier(), args);
  }
}
