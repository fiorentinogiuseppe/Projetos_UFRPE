package weka.classifiers.mi;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SMO.BinarySMO;
import weka.classifiers.functions.supportVector.Kernel;
import weka.classifiers.functions.supportVector.PolyKernel;
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
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.Standardize;
import weka.filters.unsupervised.instance.SparseToNonSparse;

























































































































public class MISVM
  extends Classifier
  implements OptionHandler, MultiInstanceCapabilitiesHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = 7622231064035278145L;
  protected Filter m_SparseFilter;
  protected SVM m_SVM;
  protected Kernel m_kernel;
  protected double m_C;
  protected Filter m_Filter;
  protected int m_filterType;
  public static final int FILTER_NORMALIZE = 0;
  public static final int FILTER_STANDARDIZE = 1;
  public static final int FILTER_NONE = 2;
  public static final Tag[] TAGS_FILTER = { new Tag(0, "Normalize training data"), new Tag(1, "Standardize training data"), new Tag(2, "No normalization/standardization") };
  protected int m_MaxIterations;
  protected MultiInstanceToPropositional m_ConvertToProp;
  
  public MISVM()
  {
    m_SparseFilter = new SparseToNonSparse();
    




    m_kernel = new PolyKernel();
    

    m_C = 1.0D;
    

    m_Filter = null;
    

    m_filterType = 0;
    














    m_MaxIterations = 500;
    

    m_ConvertToProp = new MultiInstanceToPropositional();
  }
  




  public String globalInfo()
  {
    return "Implements Stuart Andrews' mi_SVM (Maximum pattern Margin Formulation of MIL). Applying weka.classifiers.functions.SMO to solve multiple instances problem.\nThe algorithm first assign the bag label to each instance in the bag as its initial class label.  After that applying SMO to compute SVM solution for all instances in positive bags And then reassign the class label of each instance in the positive bag according to the SVM result Keep on iteration until labels do not change anymore.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  



















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Stuart Andrews and Ioannis Tsochantaridis and Thomas Hofmann");
    result.setValue(TechnicalInformation.Field.YEAR, "2003");
    result.setValue(TechnicalInformation.Field.TITLE, "Support Vector Machines for Multiple-Instance Learning");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Advances in Neural Information Processing Systems 15");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "MIT Press");
    result.setValue(TechnicalInformation.Field.PAGES, "561-568");
    
    return result;
  }
  




  public Enumeration listOptions()
  {
    Vector result = new Vector();
    
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    result.addElement(new Option("\tThe complexity constant C. (default 1)", "C", 1, "-C <double>"));
    


    result.addElement(new Option("\tWhether to 0=normalize/1=standardize/2=neither.\n\t(default: 0=normalize)", "N", 1, "-N <default 0>"));
    



    result.addElement(new Option("\tThe maximum number of iterations to perform.\n\t(default: 500)", "I", 1, "-I <num>"));
    



    result.addElement(new Option("\tThe Kernel to use.\n\t(default: weka.classifiers.functions.supportVector.PolyKernel)", "K", 1, "-K <classname and parameters>"));
    



    result.addElement(new Option("", "", 0, "\nOptions specific to kernel " + getKernel().getClass().getName() + ":"));
    



    enm = getKernel().listOptions();
    while (enm.hasMoreElements()) {
      result.addElement(enm.nextElement());
    }
    return result.elements();
  }
  


























































  public void setOptions(String[] options)
    throws Exception
  {
    String tmpStr = Utils.getOption('C', options);
    if (tmpStr.length() != 0) {
      setC(Double.parseDouble(tmpStr));
    } else {
      setC(1.0D);
    }
    tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      setFilterType(new SelectedTag(Integer.parseInt(tmpStr), TAGS_FILTER));
    } else {
      setFilterType(new SelectedTag(0, TAGS_FILTER));
    }
    tmpStr = Utils.getOption('I', options);
    if (tmpStr.length() != 0) {
      setMaxIterations(Integer.parseInt(tmpStr));
    } else {
      setMaxIterations(500);
    }
    tmpStr = Utils.getOption('K', options);
    String[] tmpOptions = Utils.splitOptions(tmpStr);
    if (tmpOptions.length != 0) {
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      setKernel(Kernel.forName(tmpStr, tmpOptions));
    }
    
    super.setOptions(options);
  }
  







  public String[] getOptions()
  {
    Vector result = new Vector();
    
    if (getDebug()) {
      result.add("-D");
    }
    result.add("-C");
    result.add("" + getC());
    
    result.add("-N");
    result.add("" + m_filterType);
    
    result.add("-K");
    result.add("" + getKernel().getClass().getName() + " " + Utils.joinOptions(getKernel().getOptions()));
    
    return (String[])result.toArray(new String[result.size()]);
  }
  





  public String kernelTipText()
  {
    return "The kernel to use.";
  }
  




  public Kernel getKernel()
  {
    return m_kernel;
  }
  




  public void setKernel(Kernel value)
  {
    m_kernel = value;
  }
  





  public String filterTypeTipText()
  {
    return "The filter type for transforming the training data.";
  }
  






  public void setFilterType(SelectedTag newType)
  {
    if (newType.getTags() == TAGS_FILTER) {
      m_filterType = newType.getSelectedTag().getID();
    }
  }
  






  public SelectedTag getFilterType()
  {
    return new SelectedTag(m_filterType, TAGS_FILTER);
  }
  





  public String cTipText()
  {
    return "The value for C.";
  }
  





  public double getC()
  {
    return m_C;
  }
  




  public void setC(double v)
  {
    m_C = v;
  }
  





  public String maxIterationsTipText()
  {
    return "The maximum number of iterations to perform.";
  }
  




  public int getMaxIterations()
  {
    return m_MaxIterations;
  }
  




  public void setMaxIterations(int value)
  {
    if (value < 1) {
      System.out.println("At least 1 iteration is necessary (provided: " + value + ")!");
    }
    else {
      m_MaxIterations = value;
    }
  }
  





  private class SVM
    extends SMO
  {
    static final long serialVersionUID = -8325638229658828931L;
    





    protected SVM() {}
    




    protected double output(int index, Instance inst)
      throws Exception
    {
      double output = 0.0D;
      output = m_classifiers[0][1].SVMOutput(index, inst);
      return output;
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9144 $");
    }
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.RELATIONAL_ATTRIBUTES);
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capabilities.Capability.BINARY_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.enable(Capabilities.Capability.ONLY_MULTIINSTANCE);
    
    return result;
  }
  









  public Capabilities getMultiInstanceCapabilities()
  {
    SVM classifier = null;
    Capabilities result = null;
    try
    {
      classifier = new SVM();
      classifier.setKernel(Kernel.makeCopy(getKernel()));
      result = classifier.getCapabilities();
      result.setOwner(this);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    

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
    
    int numBags = train.numInstances();
    int[] bagSize = new int[numBags];
    int[] classes = new int[numBags];
    
    Vector instLabels = new Vector();
    Vector pre_instLabels = new Vector();
    
    for (int h = 0; h < numBags; h++) {
      classes[h] = ((int)train.instance(h).classValue());
      bagSize[h] = train.instance(h).relationalValue(1).numInstances();
      for (int i = 0; i < bagSize[h]; i++) {
        instLabels.addElement(new Double(classes[h]));
      }
    }
    
    m_ConvertToProp.setWeightMethod(new SelectedTag(1, MultiInstanceToPropositional.TAGS_WEIGHTMETHOD));
    


    m_ConvertToProp.setInputFormat(train);
    train = Filter.useFilter(train, m_ConvertToProp);
    train.deleteAttributeAt(0);
    
    if (m_filterType == 1) {
      m_Filter = new Standardize();
    } else if (m_filterType == 0) {
      m_Filter = new Normalize();
    } else {
      m_Filter = null;
    }
    if (m_Filter != null) {
      m_Filter.setInputFormat(train);
      train = Filter.useFilter(train, m_Filter);
    }
    
    if (m_Debug) {
      System.out.println("\nIteration History...");
    }
    
    if (getDebug()) {
      System.out.println("\nstart building model ...");
    }
    

    Vector max_index = new Vector();
    Instance inst = null;
    
    int loopNum = 0;
    do {
      loopNum++;
      int index = -1;
      if (m_Debug) {
        System.out.println("=====================loop: " + loopNum);
      }
      
      pre_instLabels = (Vector)instLabels.clone();
      

      m_SVM = new SVM();
      m_SVM.setC(getC());
      m_SVM.setKernel(Kernel.makeCopy(getKernel()));
      
      m_SVM.setFilterType(new SelectedTag(2, TAGS_FILTER));
      
      m_SVM.buildClassifier(train);
      
      for (int h = 0; h < numBags; h++)
        if (classes[h] == 1) {
          if (m_Debug)
            System.out.println("--------------- " + h + " ----------------");
          double sum = 0.0D;
          

          for (int i = 0; i < bagSize[h]; i++) {
            index++;
            
            inst = train.instance(index);
            double output = m_SVM.output(-1, inst);
            if (output <= 0.0D) {
              if (inst.classValue() == 1.0D) {
                train.instance(index).setClassValue(0.0D);
                instLabels.set(index, new Double(0.0D));
                
                if (m_Debug) {
                  System.out.println(index + "- changed to 0");
                }
              }
            }
            else if (inst.classValue() == 0.0D) {
              train.instance(index).setClassValue(1.0D);
              instLabels.set(index, new Double(1.0D));
              
              if (m_Debug) {
                System.out.println(index + "+ changed to 1");
              }
            }
            sum += train.instance(index).classValue();
          }
          




          if (sum == 0.0D)
          {
            double max_output = -1.7976931348623157E308D;
            max_index.clear();
            for (int j = index - bagSize[h] + 1; j < index + 1; j++) {
              inst = train.instance(j);
              double output = m_SVM.output(-1, inst);
              if (max_output < output) {
                max_output = output;
                max_index.clear();
                max_index.add(new Integer(j));
              }
              else if (max_output == output) {
                max_index.add(new Integer(j));
              }
            }
            
            for (int vecIndex = 0; vecIndex < max_index.size(); vecIndex++) {
              Integer i = (Integer)max_index.get(vecIndex);
              train.instance(i.intValue()).setClassValue(1.0D);
              instLabels.set(i.intValue(), new Double(1.0D));
              
              if (m_Debug) {
                System.out.println("##change to 1 ###outpput: " + max_output + " max_index: " + i + " bag: " + h);
              }
            }
          }
        } else {
          index += bagSize[h];
        }
    } while ((!instLabels.equals(pre_instLabels)) && (loopNum < m_MaxIterations));
    
    if (getDebug()) {
      System.out.println("finish building model.");
    }
  }
  






  public double[] distributionForInstance(Instance exmp)
    throws Exception
  {
    double sum = 0.0D;
    
    double[] distribution = new double[2];
    
    Instances testData = new Instances(exmp.dataset(), 0);
    testData.add(exmp);
    

    testData = Filter.useFilter(testData, m_ConvertToProp);
    testData.deleteAttributeAt(0);
    
    if (m_Filter != null) {
      testData = Filter.useFilter(testData, m_Filter);
    }
    for (int j = 0; j < testData.numInstances(); j++) {
      Instance inst = testData.instance(j);
      double output = m_SVM.output(-1, inst);
      double classValue; double classValue; if (output <= 0.0D) {
        classValue = 0.0D;
      } else
        classValue = 1.0D;
      sum += classValue;
    }
    if (sum == 0.0D) {
      distribution[0] = 1.0D;
    } else
      distribution[0] = 0.0D;
    distribution[1] = (1.0D - distribution[0]);
    
    return distribution;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9144 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new MISVM(), argv);
  }
}
