package weka.attributeSelection;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import weka.classifiers.functions.SMO;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
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
import weka.filters.unsupervised.attribute.MakeIndicator;
import weka.filters.unsupervised.attribute.Remove;










































































































public class SVMAttributeEval
  extends ASEvaluation
  implements AttributeEvaluator, OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -6489975709033967447L;
  private double[] m_attScores;
  private int m_numToEliminate = 1;
  


  private int m_percentToEliminate = 0;
  


  private int m_percentThreshold = 0;
  

  private double m_smoCParameter = 1.0D;
  

  private double m_smoTParameter = 1.0E-10D;
  

  private double m_smoPParameter = 1.0E-25D;
  

  private int m_smoFilterType = 0;
  




  public String globalInfo()
  {
    return "SVMAttributeEval :\n\nEvaluates the worth of an attribute by using an SVM classifier. Attributes are ranked by the square of the weight assigned by the SVM. Attribute selection for multiclass problems is handled by ranking attributes for each class seperately using a one-vs-all method and then \"dealing\" from the top of each pile to give a final ranking.\n\nFor more information see:\n\n" + getTechnicalInformation().toString();
  }
  















  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "I. Guyon and J. Weston and S. Barnhill and V. Vapnik");
    result.setValue(TechnicalInformation.Field.YEAR, "2002");
    result.setValue(TechnicalInformation.Field.TITLE, "Gene selection for cancer classification using support vector machines");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "46");
    result.setValue(TechnicalInformation.Field.PAGES, "389-422");
    
    return result;
  }
  


  public SVMAttributeEval()
  {
    resetOptions();
  }
  




  public Enumeration listOptions()
  {
    Vector newVector = new Vector(4);
    
    newVector.addElement(new Option("\tSpecify the constant rate of attribute\n\telimination per invocation of\n\tthe support vector machine.\n\tDefault = 1.", "X", 1, "-X <constant rate of elimination>"));
    








    newVector.addElement(new Option("\tSpecify the percentage rate of attributes to\n\telimination per invocation of\n\tthe support vector machine.\n\tTrumps constant rate (above threshold).\n\tDefault = 0.", "Y", 1, "-Y <percent rate of elimination>"));
    









    newVector.addElement(new Option("\tSpecify the threshold below which \n\tpercentage attribute elimination\n\treverts to the constant method.", "Z", 1, "-Z <threshold for percent elimination>"));
    








    newVector.addElement(new Option("\tSpecify the value of P (epsilon\n\tparameter) to pass on to the\n\tsupport vector machine.\n\tDefault = 1.0e-25", "P", 1, "-P <epsilon>"));
    








    newVector.addElement(new Option("\tSpecify the value of T (tolerance\n\tparameter) to pass on to the\n\tsupport vector machine.\n\tDefault = 1.0e-10", "T", 1, "-T <tolerance>"));
    








    newVector.addElement(new Option("\tSpecify the value of C (complexity\n\tparameter) to pass on to the\n\tsupport vector machine.\n\tDefault = 1.0", "C", 1, "-C <complexity>"));
    








    newVector.addElement(new Option("\tWhether the SVM should 0=normalize/1=standardize/2=neither.\n\t(default 0=normalize)", "N", 1, "-N"));
    





    return newVector.elements();
  }
  



















































  public void setOptions(String[] options)
    throws Exception
  {
    String optionString = Utils.getOption('X', options);
    if (optionString.length() != 0) {
      setAttsToEliminatePerIteration(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('Y', options);
    if (optionString.length() != 0) {
      setPercentToEliminatePerIteration(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('Z', options);
    if (optionString.length() != 0) {
      setPercentThreshold(Integer.parseInt(optionString));
    }
    
    optionString = Utils.getOption('P', options);
    if (optionString.length() != 0) {
      setEpsilonParameter(new Double(optionString).doubleValue());
    }
    
    optionString = Utils.getOption('T', options);
    if (optionString.length() != 0) {
      setToleranceParameter(new Double(optionString).doubleValue());
    }
    
    optionString = Utils.getOption('C', options);
    if (optionString.length() != 0) {
      setComplexityParameter(new Double(optionString).doubleValue());
    }
    
    optionString = Utils.getOption('N', options);
    if (optionString.length() != 0) {
      setFilterType(new SelectedTag(Integer.parseInt(optionString), SMO.TAGS_FILTER));
    } else {
      setFilterType(new SelectedTag(0, SMO.TAGS_FILTER));
    }
    
    Utils.checkForRemainingOptions(options);
  }
  




  public String[] getOptions()
  {
    String[] options = new String[14];
    int current = 0;
    
    options[(current++)] = "-X";
    options[(current++)] = ("" + getAttsToEliminatePerIteration());
    
    options[(current++)] = "-Y";
    options[(current++)] = ("" + getPercentToEliminatePerIteration());
    
    options[(current++)] = "-Z";
    options[(current++)] = ("" + getPercentThreshold());
    
    options[(current++)] = "-P";
    options[(current++)] = ("" + getEpsilonParameter());
    
    options[(current++)] = "-T";
    options[(current++)] = ("" + getToleranceParameter());
    
    options[(current++)] = "-C";
    options[(current++)] = ("" + getComplexityParameter());
    
    options[(current++)] = "-N";
    options[(current++)] = ("" + m_smoFilterType);
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    
    return options;
  }
  







  public String attsToEliminatePerIterationTipText()
  {
    return "Constant rate of attribute elimination.";
  }
  





  public String percentToEliminatePerIterationTipText()
  {
    return "Percent rate of attribute elimination.";
  }
  





  public String percentThresholdTipText()
  {
    return "Threshold below which percent elimination reverts to constant elimination.";
  }
  





  public String epsilonParameterTipText()
  {
    return "P epsilon parameter to pass to the SVM";
  }
  





  public String toleranceParameterTipText()
  {
    return "T tolerance parameter to pass to the SVM";
  }
  





  public String complexityParameterTipText()
  {
    return "C complexity parameter to pass to the SVM";
  }
  





  public String filterTypeTipText()
  {
    return "filtering used by the SVM";
  }
  






  public void setAttsToEliminatePerIteration(int cRate)
  {
    m_numToEliminate = cRate;
  }
  




  public int getAttsToEliminatePerIteration()
  {
    return m_numToEliminate;
  }
  




  public void setPercentToEliminatePerIteration(int pRate)
  {
    m_percentToEliminate = pRate;
  }
  




  public int getPercentToEliminatePerIteration()
  {
    return m_percentToEliminate;
  }
  





  public void setPercentThreshold(int pThresh)
  {
    m_percentThreshold = pThresh;
  }
  





  public int getPercentThreshold()
  {
    return m_percentThreshold;
  }
  




  public void setEpsilonParameter(double svmP)
  {
    m_smoPParameter = svmP;
  }
  




  public double getEpsilonParameter()
  {
    return m_smoPParameter;
  }
  




  public void setToleranceParameter(double svmT)
  {
    m_smoTParameter = svmT;
  }
  




  public double getToleranceParameter()
  {
    return m_smoTParameter;
  }
  





  public void setComplexityParameter(double svmC)
  {
    m_smoCParameter = svmC;
  }
  




  public double getComplexityParameter()
  {
    return m_smoCParameter;
  }
  





  public void setFilterType(SelectedTag newType)
  {
    if (newType.getTags() == SMO.TAGS_FILTER) {
      m_smoFilterType = newType.getSelectedTag().getID();
    }
  }
  





  public SelectedTag getFilterType()
  {
    return new SelectedTag(m_smoFilterType, SMO.TAGS_FILTER);
  }
  









  public Capabilities getCapabilities()
  {
    Capabilities result = new SMO().getCapabilities();
    
    result.setOwner(this);
    



    result.disable(Capabilities.Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capabilities.Capability.BINARY_ATTRIBUTES);
    result.disableAllAttributeDependencies();
    
    return result;
  }
  






  public void buildEvaluator(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    


    m_numToEliminate = (m_numToEliminate > 1 ? m_numToEliminate : 1);
    m_percentToEliminate = (m_percentToEliminate < 100 ? m_percentToEliminate : 100);
    m_percentToEliminate = (m_percentToEliminate > 0 ? m_percentToEliminate : 0);
    m_percentThreshold = (m_percentThreshold < data.numAttributes() ? m_percentThreshold : data.numAttributes() - 1);
    m_percentThreshold = (m_percentThreshold > 0 ? m_percentThreshold : 0);
    


    int numAttr = data.numAttributes() - 1;
    int[][] attScoresByClass; if (data.numClasses() > 2) {
      int[][] attScoresByClass = new int[data.numClasses()][numAttr];
      for (int i = 0; i < data.numClasses(); i++) {
        attScoresByClass[i] = rankBySVM(i, data);
      }
    }
    else {
      attScoresByClass = new int[1][numAttr];
      attScoresByClass[0] = rankBySVM(0, data);
    }
    


    ArrayList ordered = new ArrayList(numAttr);
    for (int i = 0; i < numAttr; i++) {
      for (int j = 0; j < (data.numClasses() > 2 ? data.numClasses() : 1); j++) {
        Integer rank = new Integer(attScoresByClass[j][i]);
        if (!ordered.contains(rank))
          ordered.add(rank);
      }
    }
    m_attScores = new double[data.numAttributes()];
    Iterator listIt = ordered.iterator();
    for (double i = numAttr; listIt.hasNext(); i -= 1.0D) {
      m_attScores[((Integer)listIt.next()).intValue()] = i;
    }
  }
  




  private int[] rankBySVM(int classInd, Instances data)
  {
    int[] origIndices = new int[data.numAttributes()];
    for (int i = 0; i < origIndices.length; i++) {
      origIndices[i] = i;
    }
    
    int numAttrLeft = data.numAttributes() - 1;
    
    int[] attRanks = new int[numAttrLeft];
    try
    {
      MakeIndicator filter = new MakeIndicator();
      filter.setAttributeIndex("" + (data.classIndex() + 1));
      filter.setNumeric(false);
      filter.setValueIndex(classInd);
      filter.setInputFormat(data);
      Instances trainCopy = Filter.useFilter(data, filter);
      double pctToElim = m_percentToEliminate / 100.0D;
      while (numAttrLeft > 0) {
        int numToElim;
        if (pctToElim > 0.0D) {
          int numToElim = (int)(trainCopy.numAttributes() * pctToElim);
          numToElim = numToElim > 1 ? numToElim : 1;
          if (numAttrLeft - numToElim <= m_percentThreshold) {
            pctToElim = 0.0D;
            numToElim = numAttrLeft - m_percentThreshold;
          }
        } else {
          numToElim = numAttrLeft >= m_numToEliminate ? m_numToEliminate : numAttrLeft;
        }
        

        SMO smo = new SMO();
        


        smo.setFilterType(new SelectedTag(m_smoFilterType, SMO.TAGS_FILTER));
        smo.setEpsilon(m_smoPParameter);
        smo.setToleranceParameter(m_smoTParameter);
        smo.setC(m_smoCParameter);
        smo.buildClassifier(trainCopy);
        

        double[] weightsSparse = smo.sparseWeights()[0][1];
        int[] indicesSparse = smo.sparseIndices()[0][1];
        double[] weights = new double[trainCopy.numAttributes()];
        for (int j = 0; j < weightsSparse.length; j++) {
          weights[indicesSparse[j]] = (weightsSparse[j] * weightsSparse[j]);
        }
        weights[trainCopy.classIndex()] = Double.MAX_VALUE;
        
        int[] featArray = new int[numToElim];
        boolean[] eliminated = new boolean[origIndices.length];
        for (int j = 0; j < numToElim; j++) {
          int minWeightIndex = Utils.minIndex(weights);
          attRanks[(--numAttrLeft)] = origIndices[minWeightIndex];
          featArray[j] = minWeightIndex;
          eliminated[minWeightIndex] = true;
          weights[minWeightIndex] = Double.MAX_VALUE;
        }
        

        Remove delTransform = new Remove();
        
        delTransform.setInvertSelection(false);
        delTransform.setAttributeIndicesArray(featArray);
        delTransform.setInputFormat(trainCopy);
        trainCopy = Filter.useFilter(trainCopy, delTransform);
        

        int[] temp = new int[origIndices.length - numToElim];
        int k = 0;
        for (int j = 0; j < origIndices.length; j++) {
          if (eliminated[j] == 0) {
            temp[(k++)] = origIndices[j];
          }
        }
        origIndices = temp;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return attRanks;
  }
  


  protected void resetOptions()
  {
    m_attScores = null;
  }
  





  public double evaluateAttribute(int attribute)
    throws Exception
  {
    return m_attScores[attribute];
  }
  




  public String toString()
  {
    StringBuffer text = new StringBuffer();
    if (m_attScores == null) {
      text.append("\tSVM feature evaluator has not been built yet");
    } else {
      text.append("\tSVM feature evaluator");
    }
    
    text.append("\n");
    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.28 $");
  }
  




  public static void main(String[] args)
  {
    runEvaluator(new SVMAttributeEval(), args);
  }
}
