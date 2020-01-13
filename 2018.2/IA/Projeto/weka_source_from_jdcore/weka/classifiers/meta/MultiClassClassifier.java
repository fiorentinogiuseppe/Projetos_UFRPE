package weka.classifiers.meta;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableSingleClassifierEnhancer;
import weka.classifiers.functions.Logistic;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.MakeIndicator;
import weka.filters.unsupervised.instance.RemoveWithValues;
















































































public class MultiClassClassifier
  extends RandomizableSingleClassifierEnhancer
  implements OptionHandler
{
  static final long serialVersionUID = -3879602011542849141L;
  private Classifier[] m_Classifiers;
  private boolean m_pairwiseCoupling = false;
  


  private double[] m_SumOfWeights;
  


  private Filter[] m_ClassFilters;
  


  private ZeroR m_ZeroR;
  

  private Attribute m_ClassAttribute;
  

  private Instances m_TwoClassDataset;
  

  private double m_RandomWidthFactor = 2.0D;
  

  private int m_Method = 0;
  

  public static final int METHOD_1_AGAINST_ALL = 0;
  
  public static final int METHOD_ERROR_RANDOM = 1;
  
  public static final int METHOD_ERROR_EXHAUSTIVE = 2;
  
  public static final int METHOD_1_AGAINST_1 = 3;
  
  public static final Tag[] TAGS_METHOD = { new Tag(0, "1-against-all"), new Tag(1, "Random correction code"), new Tag(2, "Exhaustive correction code"), new Tag(3, "1-against-1") };
  








  public MultiClassClassifier()
  {
    m_Classifier = new Logistic();
  }
  





  protected String defaultClassifierString()
  {
    return "weka.classifiers.functions.Logistic";
  }
  



  private abstract class Code
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = 418095077487120846L;
    


    protected boolean[][] m_Codebits;
    



    private Code() {}
    


    public int size()
    {
      return m_Codebits.length;
    }
    






    public String getIndices(int which)
    {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < m_Codebits[which].length; i++) {
        if (m_Codebits[which][i] != 0) {
          if (sb.length() != 0) {
            sb.append(',');
          }
          sb.append(i + 1);
        }
      }
      return sb.toString();
    }
    



    public String toString()
    {
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < m_Codebits[0].length; i++) {
        for (int j = 0; j < m_Codebits.length; j++) {
          sb.append(m_Codebits[j][i] != 0 ? " 1" : " 0");
        }
        sb.append('\n');
      }
      return sb.toString();
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.48 $");
    }
  }
  




  private class StandardCode
    extends MultiClassClassifier.Code
  {
    static final long serialVersionUID = 3707829689461467358L;
    



    public StandardCode(int numClasses)
    {
      super(null);
      m_Codebits = new boolean[numClasses][numClasses];
      for (int i = 0; i < numClasses; i++) {
        m_Codebits[i][i] = 1;
      }
    }
    





    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.48 $");
    }
  }
  



  private class RandomCode
    extends MultiClassClassifier.Code
  {
    static final long serialVersionUID = 4413410540703926563L;
    


    Random r = null;
    





    public RandomCode(int numClasses, int numCodes, Instances data)
    {
      super(null);
      r = data.getRandomNumberGenerator(m_Seed);
      numCodes = Math.max(2, numCodes);
      m_Codebits = new boolean[numCodes][numClasses];
      int i = 0;
      do {
        randomize();
      }
      while ((!good()) && (i++ < 100));
    }
    
    private boolean good()
    {
      boolean[] ninClass = new boolean[m_Codebits[0].length];
      boolean[] ainClass = new boolean[m_Codebits[0].length];
      for (int i = 0; i < ainClass.length; i++) {
        ainClass[i] = true;
      }
      
      for (int i = 0; i < m_Codebits.length; i++) {
        boolean ninCode = false;
        boolean ainCode = true;
        for (int j = 0; j < m_Codebits[i].length; j++) {
          boolean current = m_Codebits[i][j];
          ninCode = (ninCode) || (current);
          ainCode = (ainCode) && (current);
          ninClass[j] = ((ninClass[j] != 0) || (current) ? 1 : false);
          ainClass[j] = ((ainClass[j] != 0) && (current) ? 1 : false);
        }
        if ((!ninCode) || (ainCode)) {
          return false;
        }
      }
      for (int j = 0; j < ninClass.length; j++) {
        if ((ninClass[j] == 0) || (ainClass[j] != 0)) {
          return false;
        }
      }
      return true;
    }
    


    private void randomize()
    {
      for (int i = 0; i < m_Codebits.length; i++) {
        for (int j = 0; j < m_Codebits[i].length; j++) {
          double temp = r.nextDouble();
          m_Codebits[i][j] = (temp < 0.5D ? 0 : 1);
        }
      }
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.48 $");
    }
  }
  







  private class ExhaustiveCode
    extends MultiClassClassifier.Code
  {
    static final long serialVersionUID = 8090991039670804047L;
    






    public ExhaustiveCode(int numClasses)
    {
      super(null);
      int width = (int)Math.pow(2.0D, numClasses - 1) - 1;
      m_Codebits = new boolean[width][numClasses];
      for (int j = 0; j < width; j++) {
        m_Codebits[j][0] = 1;
      }
      for (int i = 1; i < numClasses; i++) {
        int skip = (int)Math.pow(2.0D, numClasses - (i + 1));
        for (int j = 0; j < width; j++) {
          m_Codebits[j][i] = (j / skip % 2 != 0 ? 1 : 0);
        }
      }
    }
    





    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.48 $");
    }
  }
  




  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.disableAllClassDependencies();
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    
    return result;
  }
  








  public void buildClassifier(Instances insts)
    throws Exception
  {
    getCapabilities().testWithFail(insts);
    

    insts = new Instances(insts);
    insts.deleteWithMissingClass();
    
    if (m_Classifier == null) {
      throw new Exception("No base classifier has been set!");
    }
    m_ZeroR = new ZeroR();
    m_ZeroR.buildClassifier(insts);
    
    m_TwoClassDataset = null;
    
    int numClassifiers = insts.numClasses();
    if (numClassifiers <= 2)
    {
      m_Classifiers = Classifier.makeCopies(m_Classifier, 1);
      m_Classifiers[0].buildClassifier(insts);
      
      m_ClassFilters = null;
    }
    else if (m_Method == 3)
    {
      FastVector pairs = new FastVector();
      for (int i = 0; i < insts.numClasses(); i++) {
        for (int j = 0; j < insts.numClasses(); j++) {
          if (j > i) {
            int[] pair = new int[2];
            pair[0] = i;pair[1] = j;
            pairs.addElement(pair);
          }
        }
      }
      numClassifiers = pairs.size();
      m_Classifiers = Classifier.makeCopies(m_Classifier, numClassifiers);
      m_ClassFilters = new Filter[numClassifiers];
      m_SumOfWeights = new double[numClassifiers];
      

      for (int i = 0; i < numClassifiers; i++) {
        RemoveWithValues classFilter = new RemoveWithValues();
        classFilter.setAttributeIndex("" + (insts.classIndex() + 1));
        classFilter.setModifyHeader(true);
        classFilter.setInvertSelection(true);
        classFilter.setNominalIndicesArr((int[])pairs.elementAt(i));
        Instances tempInstances = new Instances(insts, 0);
        tempInstances.setClassIndex(-1);
        classFilter.setInputFormat(tempInstances);
        Instances newInsts = Filter.useFilter(insts, classFilter);
        if (newInsts.numInstances() > 0) {
          newInsts.setClassIndex(insts.classIndex());
          m_Classifiers[i].buildClassifier(newInsts);
          m_ClassFilters[i] = classFilter;
          m_SumOfWeights[i] = newInsts.sumOfWeights();
        } else {
          m_Classifiers[i] = null;
          m_ClassFilters[i] = null;
        }
      }
      

      m_TwoClassDataset = new Instances(insts, 0);
      int classIndex = m_TwoClassDataset.classIndex();
      m_TwoClassDataset.setClassIndex(-1);
      m_TwoClassDataset.deleteAttributeAt(classIndex);
      FastVector classLabels = new FastVector();
      classLabels.addElement("class0");
      classLabels.addElement("class1");
      m_TwoClassDataset.insertAttributeAt(new Attribute("class", classLabels), classIndex);
      
      m_TwoClassDataset.setClassIndex(classIndex);
    }
    else {
      Code code = null;
      switch (m_Method) {
      case 2: 
        code = new ExhaustiveCode(numClassifiers);
        break;
      case 1: 
        code = new RandomCode(numClassifiers, (int)(numClassifiers * m_RandomWidthFactor), insts);
        

        break;
      case 0: 
        code = new StandardCode(numClassifiers);
        break;
      default: 
        throw new Exception("Unrecognized correction code type");
      }
      numClassifiers = code.size();
      m_Classifiers = Classifier.makeCopies(m_Classifier, numClassifiers);
      m_ClassFilters = new MakeIndicator[numClassifiers];
      for (int i = 0; i < m_Classifiers.length; i++) {
        m_ClassFilters[i] = new MakeIndicator();
        MakeIndicator classFilter = (MakeIndicator)m_ClassFilters[i];
        classFilter.setAttributeIndex("" + (insts.classIndex() + 1));
        classFilter.setValueIndices(code.getIndices(i));
        classFilter.setNumeric(false);
        classFilter.setInputFormat(insts);
        Instances newInsts = Filter.useFilter(insts, m_ClassFilters[i]);
        m_Classifiers[i].buildClassifier(newInsts);
      }
    }
    m_ClassAttribute = insts.classAttribute();
  }
  









  public double[] individualPredictions(Instance inst)
    throws Exception
  {
    double[] result = null;
    
    if (m_Classifiers.length == 1) {
      result = new double[1];
      result[0] = m_Classifiers[0].distributionForInstance(inst)[1];
    } else {
      result = new double[m_ClassFilters.length];
      for (int i = 0; i < m_ClassFilters.length; i++) {
        if (m_Classifiers[i] != null) {
          if (m_Method == 3) {
            Instance tempInst = (Instance)inst.copy();
            tempInst.setDataset(m_TwoClassDataset);
            result[i] = m_Classifiers[i].distributionForInstance(tempInst)[1];
          } else {
            m_ClassFilters[i].input(inst);
            m_ClassFilters[i].batchFinished();
            result[i] = m_Classifiers[i].distributionForInstance(m_ClassFilters[i].output())[1];
          }
        }
      }
    }
    
    return result;
  }
  






  public double[] distributionForInstance(Instance inst)
    throws Exception
  {
    if (m_Classifiers.length == 1) {
      return m_Classifiers[0].distributionForInstance(inst);
    }
    
    double[] probs = new double[inst.numClasses()];
    
    if (m_Method == 3) {
      double[][] r = new double[inst.numClasses()][inst.numClasses()];
      double[][] n = new double[inst.numClasses()][inst.numClasses()];
      
      for (int i = 0; i < m_ClassFilters.length; i++) {
        if (m_Classifiers[i] != null) {
          Instance tempInst = (Instance)inst.copy();
          tempInst.setDataset(m_TwoClassDataset);
          double[] current = m_Classifiers[i].distributionForInstance(tempInst);
          Range range = new Range(((RemoveWithValues)m_ClassFilters[i]).getNominalIndices());
          
          range.setUpper(m_ClassAttribute.numValues());
          int[] pair = range.getSelection();
          if ((m_pairwiseCoupling) && (inst.numClasses() > 2)) {
            r[pair[0]][pair[1]] = current[0];
            n[pair[0]][pair[1]] = m_SumOfWeights[i];
          }
          else if (current[0] > current[1]) {
            probs[pair[0]] += 1.0D;
          } else {
            probs[pair[1]] += 1.0D;
          }
        }
      }
      
      if ((m_pairwiseCoupling) && (inst.numClasses() > 2)) {
        return pairwiseCoupling(n, r);
      }
    }
    else {
      for (int i = 0; i < m_ClassFilters.length; i++) {
        m_ClassFilters[i].input(inst);
        m_ClassFilters[i].batchFinished();
        double[] current = m_Classifiers[i].distributionForInstance(m_ClassFilters[i].output());
        
        for (int j = 0; j < m_ClassAttribute.numValues(); j++) {
          if (((MakeIndicator)m_ClassFilters[i]).getValueRange().isInRange(j)) {
            probs[j] += current[1];
          } else {
            probs[j] += current[0];
          }
        }
      }
    }
    
    if (Utils.gr(Utils.sum(probs), 0.0D)) {
      Utils.normalize(probs);
      return probs;
    }
    return m_ZeroR.distributionForInstance(inst);
  }
  






  public String toString()
  {
    if (m_Classifiers == null) {
      return "MultiClassClassifier: No model built yet.";
    }
    StringBuffer text = new StringBuffer();
    text.append("MultiClassClassifier\n\n");
    for (int i = 0; i < m_Classifiers.length; i++) {
      text.append("Classifier ").append(i + 1);
      if (m_Classifiers[i] != null) {
        if ((m_ClassFilters != null) && (m_ClassFilters[i] != null)) {
          if ((m_ClassFilters[i] instanceof RemoveWithValues)) {
            Range range = new Range(((RemoveWithValues)m_ClassFilters[i]).getNominalIndices());
            
            range.setUpper(m_ClassAttribute.numValues());
            int[] pair = range.getSelection();
            text.append(", " + (pair[0] + 1) + " vs " + (pair[1] + 1));
          } else if ((m_ClassFilters[i] instanceof MakeIndicator)) {
            text.append(", using indicator values: ");
            text.append(((MakeIndicator)m_ClassFilters[i]).getValueRange());
          }
        }
        text.append('\n');
        text.append(m_Classifiers[i].toString() + "\n\n");
      } else {
        text.append(" Skipped (no training examples)\n");
      }
    }
    
    return text.toString();
  }
  





  public Enumeration listOptions()
  {
    Vector vec = new Vector(4);
    
    vec.addElement(new Option("\tSets the method to use. Valid values are 0 (1-against-all),\n\t1 (random codes), 2 (exhaustive code), and 3 (1-against-1). (default 0)\n", "M", 1, "-M <num>"));
    


    vec.addElement(new Option("\tSets the multiplier when using random codes. (default 2.0)", "R", 1, "-R <num>"));
    

    vec.addElement(new Option("\tUse pairwise coupling (only has an effect for 1-against1)", "P", 0, "-P"));
    


    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      vec.addElement(enu.nextElement());
    }
    return vec.elements();
  }
  














































  public void setOptions(String[] options)
    throws Exception
  {
    String errorString = Utils.getOption('M', options);
    if (errorString.length() != 0) {
      setMethod(new SelectedTag(Integer.parseInt(errorString), TAGS_METHOD));
    }
    else {
      setMethod(new SelectedTag(0, TAGS_METHOD));
    }
    
    String rfactorString = Utils.getOption('R', options);
    if (rfactorString.length() != 0) {
      setRandomWidthFactor(new Double(rfactorString).doubleValue());
    } else {
      setRandomWidthFactor(2.0D);
    }
    
    setUsePairwiseCoupling(Utils.getFlag('P', options));
    
    super.setOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] superOptions = super.getOptions();
    String[] options = new String[superOptions.length + 5];
    
    int current = 0;
    

    options[(current++)] = "-M";
    options[(current++)] = ("" + m_Method);
    
    if (getUsePairwiseCoupling()) {
      options[(current++)] = "-P";
    }
    
    options[(current++)] = "-R";
    options[(current++)] = ("" + m_RandomWidthFactor);
    
    System.arraycopy(superOptions, 0, options, current, superOptions.length);
    

    current += superOptions.length;
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  




  public String globalInfo()
  {
    return "A metaclassifier for handling multi-class datasets with 2-class classifiers. This classifier is also capable of applying error correcting output codes for increased accuracy.";
  }
  






  public String randomWidthFactorTipText()
  {
    return "Sets the width multiplier when using random codes. The number of codes generated will be thus number multiplied by the number of classes.";
  }
  








  public double getRandomWidthFactor()
  {
    return m_RandomWidthFactor;
  }
  






  public void setRandomWidthFactor(double newRandomWidthFactor)
  {
    m_RandomWidthFactor = newRandomWidthFactor;
  }
  



  public String methodTipText()
  {
    return "Sets the method to use for transforming the multi-class problem into several 2-class ones.";
  }
  







  public SelectedTag getMethod()
  {
    return new SelectedTag(m_Method, TAGS_METHOD);
  }
  






  public void setMethod(SelectedTag newMethod)
  {
    if (newMethod.getTags() == TAGS_METHOD) {
      m_Method = newMethod.getSelectedTag().getID();
    }
  }
  





  public void setUsePairwiseCoupling(boolean p)
  {
    m_pairwiseCoupling = p;
  }
  





  public boolean getUsePairwiseCoupling()
  {
    return m_pairwiseCoupling;
  }
  



  public String usePairwiseCouplingTipText()
  {
    return "Use pairwise coupling (only has an effect for 1-against-1).";
  }
  








  public static double[] pairwiseCoupling(double[][] n, double[][] r)
  {
    double[] p = new double[r.length];
    for (int i = 0; i < p.length; i++) {
      p[i] = (1.0D / p.length);
    }
    double[][] u = new double[r.length][r.length];
    for (int i = 0; i < r.length; i++) {
      for (int j = i + 1; j < r.length; j++) {
        u[i][j] = 0.5D;
      }
    }
    

    double[] firstSum = new double[p.length];
    for (int i = 0; i < p.length; i++) {
      for (int j = i + 1; j < p.length; j++) {
        firstSum[i] += n[i][j] * r[i][j];
        firstSum[j] += n[i][j] * (1.0D - r[i][j]);
      }
    }
    
    boolean changed;
    do
    {
      changed = false;
      double[] secondSum = new double[p.length];
      for (int i = 0; i < p.length; i++) {
        for (int j = i + 1; j < p.length; j++) {
          secondSum[i] += n[i][j] * u[i][j];
          secondSum[j] += n[i][j] * (1.0D - u[i][j]);
        }
      }
      for (int i = 0; i < p.length; i++) {
        if ((firstSum[i] == 0.0D) || (secondSum[i] == 0.0D)) {
          if (p[i] > 0.0D) {
            changed = true;
          }
          p[i] = 0.0D;
        } else {
          double factor = firstSum[i] / secondSum[i];
          double pOld = p[i];
          p[i] *= factor;
          if (Math.abs(pOld - p[i]) > 0.001D) {
            changed = true;
          }
        }
      }
      Utils.normalize(p);
      for (int i = 0; i < r.length; i++) {
        for (int j = i + 1; j < r.length; j++) {
          u[i][j] = (p[i] / (p[i] + p[j]));
        }
      }
    } while (changed);
    return p;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.48 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new MultiClassClassifier(), argv);
  }
}
