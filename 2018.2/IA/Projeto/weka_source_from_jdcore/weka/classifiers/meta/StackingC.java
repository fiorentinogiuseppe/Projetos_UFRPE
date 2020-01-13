package weka.classifiers.meta;

import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.MakeIndicator;
import weka.filters.unsupervised.attribute.Remove;



















































































public class StackingC
  extends Stacking
  implements OptionHandler, TechnicalInformationHandler
{
  static final long serialVersionUID = -6717545616603725198L;
  protected Classifier[] m_MetaClassifiers = null;
  

  protected Remove m_attrFilter = null;
  
  protected MakeIndicator m_makeIndicatorFilter = null;
  


  public StackingC()
  {
    m_MetaClassifier = new LinearRegression();
    ((LinearRegression)getMetaClassifier()).setAttributeSelectionMethod(new SelectedTag(1, LinearRegression.TAGS_SELECTION));
  }
  







  public String globalInfo()
  {
    return "Implements StackingC (more efficient version of stacking).\n\nFor more information, see\n\n" + getTechnicalInformation().toString() + "\n\n" + "Note: requires meta classifier to be a numeric prediction scheme.";
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "A.K. Seewald");
    result.setValue(TechnicalInformation.Field.TITLE, "How to Make Stacking Better and Faster While Also Taking Care of an Unknown Weakness");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Nineteenth International Conference on Machine Learning");
    result.setValue(TechnicalInformation.Field.EDITOR, "C. Sammut and A. Hoffmann");
    result.setValue(TechnicalInformation.Field.YEAR, "2002");
    result.setValue(TechnicalInformation.Field.PAGES, "554-561");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Morgan Kaufmann Publishers");
    
    return result;
  }
  





  protected String metaOption()
  {
    return "\tFull name of meta classifier, followed by options.\n\tMust be a numeric prediction scheme. Default: Linear Regression.";
  }
  






  protected void processMetaOptions(String[] options)
    throws Exception
  {
    String classifierString = Utils.getOption('M', options);
    String[] classifierSpec = Utils.splitOptions(classifierString);
    if (classifierSpec.length != 0) {
      String classifierName = classifierSpec[0];
      classifierSpec[0] = "";
      setMetaClassifier(Classifier.forName(classifierName, classifierSpec));
    } else {
      ((LinearRegression)getMetaClassifier()).setAttributeSelectionMethod(new SelectedTag(1, LinearRegression.TAGS_SELECTION));
    }
  }
  









  protected void generateMetaLevel(Instances newData, Random random)
    throws Exception
  {
    Instances metaData = metaFormat(newData);
    m_MetaFormat = new Instances(metaData, 0);
    for (int j = 0; j < m_NumFolds; j++) {
      Instances train = newData.trainCV(m_NumFolds, j, random);
      

      for (int i = 0; i < m_Classifiers.length; i++) {
        getClassifier(i).buildClassifier(train);
      }
      

      Instances test = newData.testCV(m_NumFolds, j);
      for (int i = 0; i < test.numInstances(); i++) {
        metaData.add(metaInstance(test.instance(i)));
      }
    }
    
    m_MetaClassifiers = Classifier.makeCopies(m_MetaClassifier, m_BaseFormat.numClasses());
    

    int[] arrIdc = new int[m_Classifiers.length + 1];
    arrIdc[m_Classifiers.length] = (metaData.numAttributes() - 1);
    
    for (int i = 0; i < m_MetaClassifiers.length; i++) {
      for (int j = 0; j < m_Classifiers.length; j++) {
        arrIdc[j] = (m_BaseFormat.numClasses() * j + i);
      }
      m_makeIndicatorFilter = new MakeIndicator();
      m_makeIndicatorFilter.setAttributeIndex("" + (metaData.classIndex() + 1));
      m_makeIndicatorFilter.setNumeric(true);
      m_makeIndicatorFilter.setValueIndex(i);
      m_makeIndicatorFilter.setInputFormat(metaData);
      Instances newInsts = Filter.useFilter(metaData, m_makeIndicatorFilter);
      
      m_attrFilter = new Remove();
      m_attrFilter.setInvertSelection(true);
      m_attrFilter.setAttributeIndicesArray(arrIdc);
      m_attrFilter.setInputFormat(m_makeIndicatorFilter.getOutputFormat());
      newInsts = Filter.useFilter(newInsts, m_attrFilter);
      
      newInsts.setClassIndex(newInsts.numAttributes() - 1);
      
      m_MetaClassifiers[i].buildClassifier(newInsts);
    }
  }
  







  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    int[] arrIdc = new int[m_Classifiers.length + 1];
    arrIdc[m_Classifiers.length] = (m_MetaFormat.numAttributes() - 1);
    double[] classProbs = new double[m_BaseFormat.numClasses()];
    
    double sum = 0.0D;
    
    for (int i = 0; i < m_MetaClassifiers.length; i++) {
      for (int j = 0; j < m_Classifiers.length; j++) {
        arrIdc[j] = (m_BaseFormat.numClasses() * j + i);
      }
      m_makeIndicatorFilter.setAttributeIndex("" + (m_MetaFormat.classIndex() + 1));
      m_makeIndicatorFilter.setNumeric(true);
      m_makeIndicatorFilter.setValueIndex(i);
      m_makeIndicatorFilter.setInputFormat(m_MetaFormat);
      m_makeIndicatorFilter.input(metaInstance(instance));
      m_makeIndicatorFilter.batchFinished();
      Instance newInst = m_makeIndicatorFilter.output();
      
      m_attrFilter.setAttributeIndicesArray(arrIdc);
      m_attrFilter.setInvertSelection(true);
      m_attrFilter.setInputFormat(m_makeIndicatorFilter.getOutputFormat());
      m_attrFilter.input(newInst);
      m_attrFilter.batchFinished();
      newInst = m_attrFilter.output();
      
      classProbs[i] = m_MetaClassifiers[i].classifyInstance(newInst);
      if (classProbs[i] > 1.0D) classProbs[i] = 1.0D;
      if (classProbs[i] < 0.0D) classProbs[i] = 0.0D;
      sum += classProbs[i];
    }
    
    if (sum != 0.0D) { Utils.normalize(classProbs, sum);
    }
    return classProbs;
  }
  





  public String toString()
  {
    if (m_MetaFormat == null) {
      return "StackingC: No model built yet.";
    }
    String result = "StackingC\n\nBase classifiers\n\n";
    for (int i = 0; i < m_Classifiers.length; i++) {
      result = result + getClassifier(i).toString() + "\n\n";
    }
    
    result = result + "\n\nMeta classifiers (one for each class)\n\n";
    for (int i = 0; i < m_MetaClassifiers.length; i++) {
      result = result + m_MetaClassifiers[i].toString() + "\n\n";
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.15 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new StackingC(), argv);
  }
}
