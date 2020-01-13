package weka.classifiers.meta;

import java.util.Random;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;



















































































public class Grading
  extends Stacking
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = 5207837947890081170L;
  protected Classifier[] m_MetaClassifiers = new Classifier[0];
  

  protected double[] m_InstPerClass = null;
  


  public Grading() {}
  

  public String globalInfo()
  {
    return "Implements Grading. The base classifiers are \"graded\".\n\nFor more information, see\n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "A.K. Seewald and J. Fuernkranz");
    result.setValue(TechnicalInformation.Field.TITLE, "An Evaluation of Grading Classifiers");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "Advances in Intelligent Data Analysis: 4th International Conference");
    result.setValue(TechnicalInformation.Field.EDITOR, "F. Hoffmann et al.");
    result.setValue(TechnicalInformation.Field.YEAR, "2001");
    result.setValue(TechnicalInformation.Field.PAGES, "115-124");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    result.setValue(TechnicalInformation.Field.ADDRESS, "Berlin/Heidelberg/New York/Tokyo");
    
    return result;
  }
  







  protected void generateMetaLevel(Instances newData, Random random)
    throws Exception
  {
    m_MetaFormat = metaFormat(newData);
    Instances[] metaData = new Instances[m_Classifiers.length];
    for (int i = 0; i < m_Classifiers.length; i++) {
      metaData[i] = metaFormat(newData);
    }
    for (int j = 0; j < m_NumFolds; j++)
    {
      Instances train = newData.trainCV(m_NumFolds, j, random);
      Instances test = newData.testCV(m_NumFolds, j);
      

      for (int i = 0; i < m_Classifiers.length; i++) {
        getClassifier(i).buildClassifier(train);
        for (int k = 0; k < test.numInstances(); k++) {
          metaData[i].add(metaInstance(test.instance(k), i));
        }
      }
    }
    

    m_InstPerClass = new double[newData.numClasses()];
    for (int i = 0; i < newData.numClasses(); i++) m_InstPerClass[i] = 0.0D;
    for (int i = 0; i < newData.numInstances(); i++) {
      m_InstPerClass[((int)newData.instance(i).classValue())] += 1.0D;
    }
    
    m_MetaClassifiers = Classifier.makeCopies(m_MetaClassifier, m_Classifiers.length);
    

    for (int i = 0; i < m_Classifiers.length; i++) {
      m_MetaClassifiers[i].buildClassifier(metaData[i]);
    }
  }
  









  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    int numPreds = 0;
    int numClassifiers = m_Classifiers.length;
    
    double[] predConfs = new double[numClassifiers];
    

    for (int i = 0; i < numClassifiers; i++) {
      double[] preds = m_MetaClassifiers[i].distributionForInstance(metaInstance(instance, i));
      if (m_MetaClassifiers[i].classifyInstance(metaInstance(instance, i)) == 1.0D) {
        predConfs[i] = preds[1];
      } else
        predConfs[i] = (-preds[0]);
    }
    if (predConfs[Utils.maxIndex(predConfs)] < 0.0D) {
      for (int i = 0; i < numClassifiers; i++)
        predConfs[i] = (1.0D + predConfs[i]);
    } else {
      for (int i = 0; i < numClassifiers; i++) {
        if (predConfs[i] < 0.0D) { predConfs[i] = 0.0D;
        }
      }
    }
    



    double[] preds = new double[instance.numClasses()];
    for (int i = 0; i < instance.numClasses(); i++) preds[i] = 0.0D;
    for (int i = 0; i < numClassifiers; i++) {
      int idxPreds = (int)m_Classifiers[i].classifyInstance(instance);
      preds[idxPreds] += predConfs[i];
    }
    
    double maxPreds = preds[Utils.maxIndex(preds)];
    int MaxInstPerClass = -100;
    int MaxClass = -1;
    for (int i = 0; i < instance.numClasses(); i++) {
      if (preds[i] == maxPreds) {
        numPreds++;
        if (m_InstPerClass[i] > MaxInstPerClass) {
          MaxInstPerClass = (int)m_InstPerClass[i];
          MaxClass = i;
        }
      }
    }
    int predictedIndex;
    int predictedIndex;
    if (numPreds == 1) {
      predictedIndex = Utils.maxIndex(preds);



    }
    else
    {



      predictedIndex = MaxClass;
    }
    double[] classProbs = new double[instance.numClasses()];
    classProbs[predictedIndex] = 1.0D;
    return classProbs;
  }
  





  public String toString()
  {
    if (m_Classifiers.length == 0) {
      return "Grading: No base schemes entered.";
    }
    if (m_MetaClassifiers.length == 0) {
      return "Grading: No meta scheme selected.";
    }
    if (m_MetaFormat == null) {
      return "Grading: No model built yet.";
    }
    String result = "Grading\n\nBase classifiers\n\n";
    for (int i = 0; i < m_Classifiers.length; i++) {
      result = result + getClassifier(i).toString() + "\n\n";
    }
    
    result = result + "\n\nMeta classifiers\n\n";
    for (int i = 0; i < m_Classifiers.length; i++) {
      result = result + m_MetaClassifiers[i].toString() + "\n\n";
    }
    
    return result;
  }
  






  protected Instances metaFormat(Instances instances)
    throws Exception
  {
    FastVector attributes = new FastVector();
    

    for (int i = 0; i < instances.numAttributes(); i++) {
      if (i != instances.classIndex()) {
        attributes.addElement(instances.attribute(i));
      }
    }
    
    FastVector nomElements = new FastVector(2);
    nomElements.addElement("0");
    nomElements.addElement("1");
    attributes.addElement(new Attribute("PredConf", nomElements));
    
    Instances metaFormat = new Instances("Meta format", attributes, 0);
    metaFormat.setClassIndex(metaFormat.numAttributes() - 1);
    return metaFormat;
  }
  







  protected Instance metaInstance(Instance instance, int k)
    throws Exception
  {
    double[] values = new double[m_MetaFormat.numAttributes()];
    





    int idx = 0;
    for (int i = 0; i < instance.numAttributes(); i++) {
      if (i != instance.classIndex()) {
        values[idx] = instance.value(i);
        idx++;
      }
    }
    
    Classifier classifier = getClassifier(k);
    
    if (m_BaseFormat.classAttribute().isNumeric()) {
      throw new Exception("Class Attribute must not be numeric!");
    }
    double[] dist = classifier.distributionForInstance(instance);
    
    int maxIdx = 0;
    double maxVal = dist[0];
    for (int j = 1; j < dist.length; j++) {
      if (dist[j] > maxVal) {
        maxVal = dist[j];
        maxIdx = j;
      }
    }
    double predConf = instance.classValue() == maxIdx ? 1.0D : 0.0D;
    

    values[idx] = predConf;
    Instance metaInstance = new Instance(1.0D, values);
    metaInstance.setDataset(m_MetaFormat);
    return metaInstance;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.13 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new Grading(), argv);
  }
}
