package weka.classifiers.lazy;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.lazy.kstar.KStarCache;
import weka.classifiers.lazy.kstar.KStarConstants;
import weka.classifiers.lazy.kstar.KStarNominalAttribute;
import weka.classifiers.lazy.kstar.KStarNumericAttribute;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
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





















































































public class KStar
  extends Classifier
  implements KStarConstants, UpdateableClassifier, TechnicalInformationHandler
{
  static final long serialVersionUID = 332458330800479083L;
  protected Instances m_Train;
  protected int m_NumInstances;
  protected int m_NumClasses;
  protected int m_NumAttributes;
  protected int m_ClassType;
  protected int[][] m_RandClassCols;
  protected int m_ComputeRandomCols = 1;
  

  protected int m_InitFlag = 1;
  



  protected KStarCache[] m_Cache;
  


  protected int m_MissingMode = 4;
  

  protected int m_BlendMethod = 1;
  

  protected int m_GlobalBlend = 20;
  

  public static final Tag[] TAGS_MISSING = { new Tag(1, "Ignore the instances with missing values"), new Tag(2, "Treat missing values as maximally different"), new Tag(3, "Normalize over the attributes"), new Tag(4, "Average column entropy curves") };
  




  public KStar() {}
  




  public String globalInfo()
  {
    return "K* is an instance-based classifier, that is the class of a test instance is based upon the class of those training instances similar to it, as determined by some similarity function.  It differs from other instance-based learners in that it uses an entropy-based distance function.\n\nFor more information on K*, see\n\n" + getTechnicalInformation().toString();
  }
  














  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "John G. Cleary and Leonard E. Trigg");
    result.setValue(TechnicalInformation.Field.TITLE, "K*: An Instance-based Learner Using an Entropic Distance Measure");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "12th International Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.YEAR, "1995");
    result.setValue(TechnicalInformation.Field.PAGES, "108-114");
    
    return result;
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
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  




  public void buildClassifier(Instances instances)
    throws Exception
  {
    String debug = "(KStar.buildClassifier) ";
    

    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    
    m_Train = new Instances(instances, 0, instances.numInstances());
    

    init_m_Attributes();
  }
  




  public void updateClassifier(Instance instance)
    throws Exception
  {
    String debug = "(KStar.updateClassifier) ";
    
    if (!m_Train.equalHeaders(instance.dataset()))
      throw new Exception("Incompatible instance types");
    if (instance.classIsMissing())
      return;
    m_Train.add(instance);
    
    update_m_Attributes();
  }
  






  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    String debug = "(KStar.distributionForInstance) ";
    double transProb = 0.0D;double temp = 0.0D;
    double[] classProbability = new double[m_NumClasses];
    double[] predictedValue = new double[1];
    

    for (int i = 0; i < classProbability.length; i++) {
      classProbability[i] = 0.0D;
    }
    predictedValue[0] = 0.0D;
    if (m_InitFlag == 1)
    {

      if (m_BlendMethod == 2) {
        generateRandomClassColomns();
      }
      m_Cache = new KStarCache[m_NumAttributes];
      for (int i = 0; i < m_NumAttributes; i++) {
        m_Cache[i] = new KStarCache();
      }
      m_InitFlag = 0;
    }
    


    Enumeration enu = m_Train.enumerateInstances();
    while (enu.hasMoreElements()) {
      Instance trainInstance = (Instance)enu.nextElement();
      transProb = instanceTransformationProbability(instance, trainInstance);
      switch (m_ClassType)
      {
      case 1: 
        classProbability[((int)trainInstance.classValue())] += transProb;
        break;
      case 0: 
        predictedValue[0] += transProb * trainInstance.classValue();
        temp += transProb;
      }
      
    }
    if (m_ClassType == 1) {
      double sum = Utils.sum(classProbability);
      if (sum <= 0.0D)
        for (int i = 0; i < classProbability.length; i++)
          classProbability[i] = (1.0D / m_NumClasses); else
        Utils.normalize(classProbability, sum);
      return classProbability;
    }
    
    predictedValue[0] = (temp != 0.0D ? predictedValue[0] / temp : 0.0D);
    return predictedValue;
  }
  











  private double instanceTransformationProbability(Instance first, Instance second)
  {
    String debug = "(KStar.instanceTransformationProbability) ";
    double transProb = 1.0D;
    int numMissAttr = 0;
    for (int i = 0; i < m_NumAttributes; i++) {
      if (i != m_Train.classIndex())
      {

        if (first.isMissing(i)) {
          numMissAttr++;
        }
        else {
          transProb *= attrTransProb(first, second, i);
          
          if (numMissAttr != m_NumAttributes) {
            transProb = Math.pow(transProb, m_NumAttributes / (m_NumAttributes - numMissAttr));
          }
          else
          {
            transProb = 0.0D; }
        }
      }
    }
    return transProb / m_NumInstances;
  }
  








  private double attrTransProb(Instance first, Instance second, int col)
  {
    String debug = "(KStar.attrTransProb)";
    double transProb = 0.0D;
    

    switch (m_Train.attribute(col).type())
    {
    case 1: 
      KStarNominalAttribute ksNominalAttr = new KStarNominalAttribute(first, second, col, m_Train, m_RandClassCols, m_Cache[col]);
      

      ksNominalAttr.setOptions(m_MissingMode, m_BlendMethod, m_GlobalBlend);
      transProb = ksNominalAttr.transProb();
      ksNominalAttr = null;
      break;
    
    case 0: 
      KStarNumericAttribute ksNumericAttr = new KStarNumericAttribute(first, second, col, m_Train, m_RandClassCols, m_Cache[col]);
      

      ksNumericAttr.setOptions(m_MissingMode, m_BlendMethod, m_GlobalBlend);
      transProb = ksNumericAttr.transProb();
      ksNumericAttr = null;
    }
    
    return transProb;
  }
  




  public String missingModeTipText()
  {
    return "Determines how missing attribute values are treated.";
  }
  






  public SelectedTag getMissingMode()
  {
    return new SelectedTag(m_MissingMode, TAGS_MISSING);
  }
  






  public void setMissingMode(SelectedTag newMode)
  {
    if (newMode.getTags() == TAGS_MISSING) {
      m_MissingMode = newMode.getSelectedTag().getID();
    }
  }
  





  public Enumeration listOptions()
  {
    Vector optVector = new Vector(3);
    optVector.addElement(new Option("\tManual blend setting (default 20%)\n", "B", 1, "-B <num>"));
    

    optVector.addElement(new Option("\tEnable entropic auto-blend setting (symbolic class only)\n", "E", 0, "-E"));
    

    optVector.addElement(new Option("\tSpecify the missing value treatment mode (default a)\n\tValid options are: a(verage), d(elete), m(axdiff), n(ormal)\n", "M", 1, "-M <char>"));
    


    return optVector.elements();
  }
  




  public String globalBlendTipText()
  {
    return "The parameter for global blending. Values are restricted to [0,100].";
  }
  



  public void setGlobalBlend(int b)
  {
    m_GlobalBlend = b;
    if (m_GlobalBlend > 100) {
      m_GlobalBlend = 100;
    }
    if (m_GlobalBlend < 0) {
      m_GlobalBlend = 0;
    }
  }
  



  public int getGlobalBlend()
  {
    return m_GlobalBlend;
  }
  




  public String entropicAutoBlendTipText()
  {
    return "Whether entropy-based blending is to be used.";
  }
  



  public void setEntropicAutoBlend(boolean e)
  {
    if (e) {
      m_BlendMethod = 2;
    } else {
      m_BlendMethod = 1;
    }
  }
  



  public boolean getEntropicAutoBlend()
  {
    if (m_BlendMethod == 2) {
      return true;
    }
    
    return false;
  }
  






















  public void setOptions(String[] options)
    throws Exception
  {
    String debug = "(KStar.setOptions)";
    String blendStr = Utils.getOption('B', options);
    if (blendStr.length() != 0) {
      setGlobalBlend(Integer.parseInt(blendStr));
    }
    
    setEntropicAutoBlend(Utils.getFlag('E', options));
    
    String missingModeStr = Utils.getOption('M', options);
    if (missingModeStr.length() != 0) {
      switch (missingModeStr.charAt(0)) {
      case 'a': 
        setMissingMode(new SelectedTag(4, TAGS_MISSING));
        break;
      case 'd': 
        setMissingMode(new SelectedTag(1, TAGS_MISSING));
        break;
      case 'm': 
        setMissingMode(new SelectedTag(2, TAGS_MISSING));
        break;
      case 'n': 
        setMissingMode(new SelectedTag(3, TAGS_MISSING));
        break;
      default: 
        setMissingMode(new SelectedTag(4, TAGS_MISSING));
      }
    }
    Utils.checkForRemainingOptions(options);
  }
  






  public String[] getOptions()
  {
    String[] options = new String[5];
    int itr = 0;
    options[(itr++)] = "-B";
    options[(itr++)] = ("" + m_GlobalBlend);
    
    if (getEntropicAutoBlend()) {
      options[(itr++)] = "-E";
    }
    
    options[(itr++)] = "-M";
    if (m_MissingMode == 4) {
      options[(itr++)] = "a";
    }
    else if (m_MissingMode == 1) {
      options[(itr++)] = "d";
    }
    else if (m_MissingMode == 2) {
      options[(itr++)] = "m";
    }
    else if (m_MissingMode == 3) {
      options[(itr++)] = "n";
    }
    while (itr < options.length) {
      options[(itr++)] = "";
    }
    return options;
  }
  




  public String toString()
  {
    StringBuffer st = new StringBuffer();
    st.append("KStar Beta Verion (0.1b).\nCopyright (c) 1995-97 by Len Trigg (trigg@cs.waikato.ac.nz).\nJava port to Weka by Abdelaziz Mahoui (am14@cs.waikato.ac.nz).\n\nKStar options : ");
    


    String[] ops = getOptions();
    for (int i = 0; i < ops.length; i++) {
      st.append(ops[i] + ' ');
    }
    return st.toString();
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new KStar(), argv);
  }
  

  private void init_m_Attributes()
  {
    try
    {
      m_NumInstances = m_Train.numInstances();
      m_NumClasses = m_Train.numClasses();
      m_NumAttributes = m_Train.numAttributes();
      m_ClassType = m_Train.classAttribute().type();
      m_InitFlag = 1;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  


  private void update_m_Attributes()
  {
    m_NumInstances = m_Train.numInstances();
    m_InitFlag = 1;
  }
  



  private void generateRandomClassColomns()
  {
    String debug = "(KStar.generateRandomClassColomns)";
    Random generator = new Random(42L);
    
    m_RandClassCols = new int[6][];
    int[] classvals = classValues();
    for (int i = 0; i < 5; i++)
    {
      m_RandClassCols[i] = randomize(classvals, generator);
    }
    
    m_RandClassCols[5] = classvals;
  }
  





  private int[] classValues()
  {
    String debug = "(KStar.classValues)";
    int[] classval = new int[m_NumInstances];
    for (int i = 0; i < m_NumInstances; i++) {
      try {
        classval[i] = ((int)m_Train.instance(i).classValue());
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    return classval;
  }
  






  private int[] randomize(int[] array, Random generator)
  {
    String debug = "(KStar.randomize)";
    

    int[] newArray = new int[array.length];
    System.arraycopy(array, 0, newArray, 0, array.length);
    for (int j = newArray.length - 1; j > 0; j--) {
      int index = (int)(generator.nextDouble() * j);
      int temp = newArray[j];
      newArray[j] = newArray[index];
      newArray[index] = temp;
    }
    return newArray;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5525 $");
  }
}
