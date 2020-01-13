package weka.classifiers.mi;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.SingleClassifierEnhancer;
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
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.MultiInstanceToPropositional;



































































































public class MIWrapper
  extends SingleClassifierEnhancer
  implements MultiInstanceCapabilitiesHandler, OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -7707766152904315910L;
  protected int m_NumClasses;
  public static final int TESTMETHOD_ARITHMETIC = 1;
  public static final int TESTMETHOD_GEOMETRIC = 2;
  public static final int TESTMETHOD_MAXPROB = 3;
  public static final Tag[] TAGS_TESTMETHOD = { new Tag(1, "arithmetic average"), new Tag(2, "geometric average"), new Tag(3, "max probability of positive bag") };
  





  protected int m_Method = 2;
  

  protected MultiInstanceToPropositional m_ConvertToProp = new MultiInstanceToPropositional();
  

  protected int m_WeightMethod = 3;
  


  public MIWrapper() {}
  

  public String globalInfo()
  {
    return "A simple Wrapper method for applying standard propositional learners to multi-instance data.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  












  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.TECHREPORT);
    result.setValue(TechnicalInformation.Field.AUTHOR, "E. T. Frank and X. Xu");
    result.setValue(TechnicalInformation.Field.TITLE, "Applying propositional learning algorithms to multi-instance data");
    result.setValue(TechnicalInformation.Field.YEAR, "2003");
    result.setValue(TechnicalInformation.Field.MONTH, "06");
    result.setValue(TechnicalInformation.Field.INSTITUTION, "University of Waikato");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Department of Computer Science, University of Waikato, Hamilton, NZ");
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    result.addElement(new Option("\tThe method used in testing:\n\t1.arithmetic average\n\t2.geometric average\n\t3.max probability of positive bag.\n\t(default: 1)", "P", 1, "-P [1|2|3]"));
    






    result.addElement(new Option("\tThe type of weight setting for each single-instance:\n\t0.keep the weight to be the same as the original value;\n\t1.weight = 1.0\n\t2.weight = 1.0/Total number of single-instance in the\n\t\tcorresponding bag\n\t3. weight = Total number of single-instance / (Total\n\t\tnumber of bags * Total number of single-instance \n\t\tin the corresponding bag).\n\t(default: 3)", "A", 1, "-A [0|1|2|3]"));
    










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
    
    String methodString = Utils.getOption('P', options);
    if (methodString.length() != 0) {
      setMethod(new SelectedTag(Integer.parseInt(methodString), TAGS_TESTMETHOD));
    }
    else {
      setMethod(new SelectedTag(1, TAGS_TESTMETHOD));
    }
    

    String weightString = Utils.getOption('A', options);
    if (weightString.length() != 0) {
      setWeightMethod(new SelectedTag(Integer.parseInt(weightString), MultiInstanceToPropositional.TAGS_WEIGHTMETHOD));

    }
    else
    {
      setWeightMethod(new SelectedTag(3, MultiInstanceToPropositional.TAGS_WEIGHTMETHOD));
    }
    



    super.setOptions(options);
  }
  








  public String[] getOptions()
  {
    Vector result = new Vector();
    
    result.add("-P");
    result.add("" + m_Method);
    
    result.add("-A");
    result.add("" + m_WeightMethod);
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String weightMethodTipText()
  {
    return "The method used for weighting the instances.";
  }
  




  public void setWeightMethod(SelectedTag method)
  {
    if (method.getTags() == MultiInstanceToPropositional.TAGS_WEIGHTMETHOD) {
      m_WeightMethod = method.getSelectedTag().getID();
    }
  }
  



  public SelectedTag getWeightMethod()
  {
    return new SelectedTag(m_WeightMethod, MultiInstanceToPropositional.TAGS_WEIGHTMETHOD);
  }
  






  public String methodTipText()
  {
    return "The method used for testing.";
  }
  




  public void setMethod(SelectedTag method)
  {
    if (method.getTags() == TAGS_TESTMETHOD) {
      m_Method = method.getSelectedTag().getID();
    }
  }
  



  public SelectedTag getMethod()
  {
    return new SelectedTag(m_Method, TAGS_TESTMETHOD);
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    if (super.getCapabilities().handles(Capabilities.Capability.NOMINAL_CLASS))
      result.enable(Capabilities.Capability.NOMINAL_CLASS);
    if (super.getCapabilities().handles(Capabilities.Capability.BINARY_CLASS))
      result.enable(Capabilities.Capability.BINARY_CLASS);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    result.disable(Capabilities.Capability.MISSING_VALUES);
    

    result.enable(Capabilities.Capability.ONLY_MULTIINSTANCE);
    
    return result;
  }
  






  public Capabilities getMultiInstanceCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.enable(Capabilities.Capability.NO_CLASS);
    
    return result;
  }
  







  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    Instances train = new Instances(data);
    train.deleteWithMissingClass();
    
    if (m_Classifier == null) {
      throw new Exception("A base classifier has not been specified!");
    }
    
    if (getDebug())
      System.out.println("Start training ...");
    m_NumClasses = train.numClasses();
    

    m_ConvertToProp.setWeightMethod(getWeightMethod());
    m_ConvertToProp.setInputFormat(train);
    train = Filter.useFilter(train, m_ConvertToProp);
    train.deleteAttributeAt(0);
    
    m_Classifier.buildClassifier(train);
  }
  







  public double[] distributionForInstance(Instance exmp)
    throws Exception
  {
    Instances testData = new Instances(exmp.dataset(), 0);
    testData.add(exmp);
    

    m_ConvertToProp.setWeightMethod(new SelectedTag(0, MultiInstanceToPropositional.TAGS_WEIGHTMETHOD));
    


    testData = Filter.useFilter(testData, m_ConvertToProp);
    testData.deleteAttributeAt(0);
    

    double[] distribution = new double[m_NumClasses];
    double nI = testData.numInstances();
    double[] maxPr = new double[m_NumClasses];
    
    for (int i = 0; i < nI; i++) {
      double[] dist = m_Classifier.distributionForInstance(testData.instance(i));
      for (int j = 0; j < m_NumClasses; j++)
      {
        switch (m_Method) {
        case 1: 
          distribution[j] += dist[j] / nI;
          break;
        
        case 2: 
          if (dist[j] < 0.001D) {
            dist[j] = 0.001D;
          } else if (dist[j] > 0.999D) {
            dist[j] = 0.999D;
          }
          distribution[j] += Math.log(dist[j]) / nI;
          break;
        case 3: 
          if (dist[j] > maxPr[j]) {
            maxPr[j] = dist[j];
          }
          break;
        }
      }
    }
    if (m_Method == 2) {
      for (int j = 0; j < m_NumClasses; j++)
        distribution[j] = Math.exp(distribution[j]);
    }
    if (m_Method == 3) {
      distribution[1] = maxPr[1];
      distribution[0] = (1.0D - distribution[1]);
    }
    
    if (Utils.eq(Utils.sum(distribution), 0.0D)) {
      for (int i = 0; i < distribution.length; i++) {
        distribution[i] = (1.0D / distribution.length);
      }
    } else {
      Utils.normalize(distribution);
    }
    
    return distribution;
  }
  




  public String toString()
  {
    return "MIWrapper with base classifier: \n" + m_Classifier.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9144 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new MIWrapper(), argv);
  }
}
