package weka.classifiers.mi;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.MultiInstanceCapabilitiesHandler;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;







































































public class SimpleMI
  extends SingleClassifierEnhancer
  implements OptionHandler, MultiInstanceCapabilitiesHandler
{
  static final long serialVersionUID = 9137795893666592662L;
  public static final int TRANSFORMMETHOD_ARITHMETIC = 1;
  public static final int TRANSFORMMETHOD_GEOMETRIC = 2;
  public static final int TRANSFORMMETHOD_MINIMAX = 3;
  public static final Tag[] TAGS_TRANSFORMMETHOD = { new Tag(1, "arithmetic average"), new Tag(2, "geometric average"), new Tag(3, "using minimax combined features of a bag") };
  





  protected int m_TransformMethod = 1;
  


  public SimpleMI() {}
  

  public String globalInfo()
  {
    return "Reduces MI data into mono-instance data.";
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe method used in transformation:\n\t1.arithmatic average; 2.geometric centor;\n\t3.using minimax combined features of a bag (default: 1)\n\n\tMethod 3:\n\tDefine s to be the vector of the coordinate-wise maxima\n\tand minima of X, ie., \n\ts(X)=(minx1, ..., minxm, maxx1, ...,maxxm), transform\n\tthe exemplars into mono-instance which contains attributes\n\ts(X)", "M", 1, "-M [1|2|3]"));
    










    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      result.addElement(enu.nextElement());
    }
    
    return result.elements();
  }
  







































  public void setOptions(String[] options)
    throws Exception
  {
    setDebug(Utils.getFlag('D', options));
    
    String methodString = Utils.getOption('M', options);
    if (methodString.length() != 0) {
      setTransformMethod(new SelectedTag(Integer.parseInt(methodString), TAGS_TRANSFORMMETHOD));
    }
    else
    {
      setTransformMethod(new SelectedTag(1, TAGS_TRANSFORMMETHOD));
    }
    


    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-M");
    result.add("" + m_TransformMethod);
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String transformMethodTipText()
  {
    return "The method used in transformation.";
  }
  




  public void setTransformMethod(SelectedTag newMethod)
  {
    if (newMethod.getTags() == TAGS_TRANSFORMMETHOD) {
      m_TransformMethod = newMethod.getSelectedTag().getID();
    }
  }
  



  public SelectedTag getTransformMethod()
  {
    return new SelectedTag(m_TransformMethod, TAGS_TRANSFORMMETHOD);
  }
  









  public Instances transform(Instances train)
    throws Exception
  {
    Attribute classAttribute = (Attribute)train.classAttribute().copy();
    Attribute bagLabel = train.attribute(0);
    

    Instances newData = train.attribute(1).relation().stringFreeStructure();
    

    newData.insertAttributeAt(bagLabel, 0);
    

    newData.insertAttributeAt(classAttribute, newData.numAttributes());
    newData.setClassIndex(newData.numAttributes() - 1);
    
    Instances mini_data = newData.stringFreeStructure();
    Instances max_data = newData.stringFreeStructure();
    
    Instance newInst = new Instance(newData.numAttributes());
    Instance mini_Inst = new Instance(mini_data.numAttributes());
    Instance max_Inst = new Instance(max_data.numAttributes());
    newInst.setDataset(newData);
    mini_Inst.setDataset(mini_data);
    max_Inst.setDataset(max_data);
    
    double N = train.numInstances();
    for (int i = 0; i < N; i++) {
      int attIdx = 1;
      Instance bag = train.instance(i);
      double labelValue = bag.value(0);
      if (m_TransformMethod != 3) {
        newInst.setValue(0, labelValue);
      } else {
        mini_Inst.setValue(0, labelValue);
        max_Inst.setValue(0, labelValue);
      }
      
      Instances data = bag.relationalValue(1);
      for (int j = 0; j < data.numAttributes(); j++)
      {
        if (m_TransformMethod == 1) {
          double value = data.meanOrMode(j);
          newInst.setValue(attIdx++, value);
        }
        else if (m_TransformMethod == 2) {
          double[] minimax = minimax(data, j);
          double value = (minimax[0] + minimax[1]) / 2.0D;
          newInst.setValue(attIdx++, value);
        }
        else {
          double[] minimax = minimax(data, j);
          mini_Inst.setValue(attIdx, minimax[0]);
          max_Inst.setValue(attIdx, minimax[1]);
          attIdx++;
        }
      }
      
      if (m_TransformMethod == 3) {
        if (!bag.classIsMissing())
          max_Inst.setClassValue(bag.classValue());
        mini_data.add(mini_Inst);
        max_data.add(max_Inst);
      }
      else {
        if (!bag.classIsMissing())
          newInst.setClassValue(bag.classValue());
        newData.add(newInst);
      }
    }
    
    if (m_TransformMethod == 3) {
      mini_data.setClassIndex(-1);
      mini_data.deleteAttributeAt(mini_data.numAttributes() - 1);
      max_data.deleteAttributeAt(0);
      
      newData = Instances.mergeInstances(mini_data, max_data);
      newData.setClassIndex(newData.numAttributes() - 1);
    }
    

    return newData;
  }
  






  public static double[] minimax(Instances data, int attIndex)
  {
    double[] rt = { Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
    for (int i = 0; i < data.numInstances(); i++) {
      double val = data.instance(i).value(attIndex);
      if (val > rt[1])
        rt[1] = val;
      if (val < rt[0]) {
        rt[0] = val;
      }
    }
    for (int j = 0; j < 2; j++) {
      if (Double.isInfinite(rt[j]))
        rt[j] = NaN.0D;
    }
    return rt;
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    result.disable(Capabilities.Capability.MISSING_VALUES);
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    if (super.getCapabilities().handles(Capabilities.Capability.NOMINAL_CLASS))
      result.enable(Capabilities.Capability.NOMINAL_CLASS);
    if (super.getCapabilities().handles(Capabilities.Capability.BINARY_CLASS))
      result.enable(Capabilities.Capability.BINARY_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.enable(Capabilities.Capability.ONLY_MULTIINSTANCE);
    
    return result;
  }
  






  public Capabilities getMultiInstanceCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capabilities.Capability.DATE_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.disableAllClasses();
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  







  public void buildClassifier(Instances train)
    throws Exception
  {
    getCapabilities().testWithFail(train);
    

    train = new Instances(train);
    train.deleteWithMissingClass();
    
    if (m_Classifier == null) {
      throw new Exception("A base classifier has not been specified!");
    }
    
    if (getDebug())
      System.out.println("Start training ...");
    Instances data = transform(train);
    
    data.deleteAttributeAt(0);
    m_Classifier.buildClassifier(data);
    
    if (getDebug()) {
      System.out.println("Finish building model");
    }
  }
  






  public double[] distributionForInstance(Instance newBag)
    throws Exception
  {
    double[] distribution = new double[2];
    Instances test = new Instances(newBag.dataset(), 0);
    test.add(newBag);
    
    test = transform(test);
    test.deleteAttributeAt(0);
    Instance newInst = test.firstInstance();
    
    distribution = m_Classifier.distributionForInstance(newInst);
    
    return distribution;
  }
  




  public String toString()
  {
    return "SimpleMI with base classifier: \n" + m_Classifier.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9144 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new SimpleMI(), argv);
  }
}
