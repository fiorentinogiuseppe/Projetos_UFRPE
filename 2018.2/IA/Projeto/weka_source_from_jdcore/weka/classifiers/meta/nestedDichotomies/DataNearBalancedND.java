package weka.classifiers.meta.nestedDichotomies;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableSingleClassifierEnhancer;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.MakeIndicator;
import weka.filters.unsupervised.instance.RemoveWithValues;























































































































public class DataNearBalancedND
  extends RandomizableSingleClassifierEnhancer
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = 5117477294209496368L;
  protected FilteredClassifier m_FilteredClassifier;
  protected Hashtable m_classifiers = new Hashtable();
  

  protected DataNearBalancedND m_FirstSuccessor = null;
  

  protected DataNearBalancedND m_SecondSuccessor = null;
  

  protected Range m_Range = null;
  

  protected boolean m_hashtablegiven = false;
  



  public DataNearBalancedND()
  {
    m_Classifier = new J48();
  }
  





  protected String defaultClassifierString()
  {
    return "weka.classifiers.trees.J48";
  }
  









  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.INPROCEEDINGS);
    result.setValue(TechnicalInformation.Field.AUTHOR, "Lin Dong and Eibe Frank and Stefan Kramer");
    result.setValue(TechnicalInformation.Field.TITLE, "Ensembles of Balanced Nested Dichotomies for Multi-class Problems");
    result.setValue(TechnicalInformation.Field.BOOKTITLE, "PKDD");
    result.setValue(TechnicalInformation.Field.YEAR, "2005");
    result.setValue(TechnicalInformation.Field.PAGES, "84-95");
    result.setValue(TechnicalInformation.Field.PUBLISHER, "Springer");
    
    TechnicalInformation additional = result.add(TechnicalInformation.Type.INPROCEEDINGS);
    additional.setValue(TechnicalInformation.Field.AUTHOR, "Eibe Frank and Stefan Kramer");
    additional.setValue(TechnicalInformation.Field.TITLE, "Ensembles of nested dichotomies for multi-class problems");
    additional.setValue(TechnicalInformation.Field.BOOKTITLE, "Twenty-first International Conference on Machine Learning");
    additional.setValue(TechnicalInformation.Field.YEAR, "2004");
    additional.setValue(TechnicalInformation.Field.PUBLISHER, "ACM");
    
    return result;
  }
  





  public void setHashtable(Hashtable table)
  {
    m_hashtablegiven = true;
    m_classifiers = table;
  }
  














  private void generateClassifierForNode(Instances data, Range classes, Random rand, Classifier classifier, Hashtable table, double[] instsNumAllClasses)
    throws Exception
  {
    int[] indices = classes.getSelection();
    

    for (int j = indices.length - 1; j > 0; j--) {
      int randPos = rand.nextInt(j + 1);
      int temp = indices[randPos];
      indices[randPos] = indices[j];
      indices[j] = temp;
    }
    

    double total = 0.0D;
    for (int j = 0; j < indices.length; j++) {
      total += instsNumAllClasses[indices[j]];
    }
    double halfOfTotal = total / 2.0D;
    


    double sumLeft = 0.0D;double sumRight = 0.0D;
    int i = 0;int j = indices.length - 1;
    do {
      if (i == j) {
        if (rand.nextBoolean()) {
          sumLeft += instsNumAllClasses[indices[(i++)]];
        } else {
          sumRight += instsNumAllClasses[indices[(j--)]];
        }
      } else {
        sumLeft += instsNumAllClasses[indices[(i++)]];
        sumRight += instsNumAllClasses[indices[(j--)]];
      }
    } while ((Utils.sm(sumLeft, halfOfTotal)) && (Utils.sm(sumRight, halfOfTotal)));
    
    int first = 0;int second = 0;
    if (!Utils.sm(sumLeft, halfOfTotal)) {
      first = i;
    } else {
      first = j + 1;
    }
    second = indices.length - first;
    
    int[] firstInds = new int[first];
    int[] secondInds = new int[second];
    System.arraycopy(indices, 0, firstInds, 0, first);
    System.arraycopy(indices, first, secondInds, 0, second);
    

    int[] sortedFirst = Utils.sort(firstInds);
    int[] sortedSecond = Utils.sort(secondInds);
    int[] firstCopy = new int[first];
    int[] secondCopy = new int[second];
    for (int k = 0; k < sortedFirst.length; k++) {
      firstCopy[k] = firstInds[sortedFirst[k]];
    }
    firstInds = firstCopy;
    for (int k = 0; k < sortedSecond.length; k++) {
      secondCopy[k] = secondInds[sortedSecond[k]];
    }
    secondInds = secondCopy;
    

    if (firstInds[0] > secondInds[0]) {
      int[] help = secondInds;
      secondInds = firstInds;
      firstInds = help;
      int help2 = second;
      second = first;
      first = help2;
    }
    
    m_Range = new Range(Range.indicesToRangeList(firstInds));
    m_Range.setUpper(data.numClasses() - 1);
    
    Range secondRange = new Range(Range.indicesToRangeList(secondInds));
    secondRange.setUpper(data.numClasses() - 1);
    

    MakeIndicator filter = new MakeIndicator();
    filter.setAttributeIndex("" + (data.classIndex() + 1));
    filter.setValueIndices(m_Range.getRanges());
    filter.setNumeric(false);
    filter.setInputFormat(data);
    m_FilteredClassifier = new FilteredClassifier();
    if (data.numInstances() > 0) {
      m_FilteredClassifier.setClassifier(Classifier.makeCopies(classifier, 1)[0]);
    } else {
      m_FilteredClassifier.setClassifier(new ZeroR());
    }
    m_FilteredClassifier.setFilter(filter);
    

    m_classifiers = table;
    
    if (!m_classifiers.containsKey(getString(firstInds) + "|" + getString(secondInds))) {
      m_FilteredClassifier.buildClassifier(data);
      m_classifiers.put(getString(firstInds) + "|" + getString(secondInds), m_FilteredClassifier);
    } else {
      m_FilteredClassifier = ((FilteredClassifier)m_classifiers.get(getString(firstInds) + "|" + getString(secondInds)));
    }
    


    m_FirstSuccessor = new DataNearBalancedND();
    if (first == 1) {
      m_FirstSuccessor.m_Range = m_Range;
    } else {
      RemoveWithValues rwv = new RemoveWithValues();
      rwv.setInvertSelection(true);
      rwv.setNominalIndices(m_Range.getRanges());
      rwv.setAttributeIndex("" + (data.classIndex() + 1));
      rwv.setInputFormat(data);
      Instances firstSubset = Filter.useFilter(data, rwv);
      m_FirstSuccessor.generateClassifierForNode(firstSubset, m_Range, rand, classifier, m_classifiers, instsNumAllClasses);
    }
    

    m_SecondSuccessor = new DataNearBalancedND();
    if (second == 1) {
      m_SecondSuccessor.m_Range = secondRange;
    } else {
      RemoveWithValues rwv = new RemoveWithValues();
      rwv.setInvertSelection(true);
      rwv.setNominalIndices(secondRange.getRanges());
      rwv.setAttributeIndex("" + (data.classIndex() + 1));
      rwv.setInputFormat(data);
      Instances secondSubset = Filter.useFilter(data, rwv);
      m_SecondSuccessor = new DataNearBalancedND();
      
      m_SecondSuccessor.generateClassifierForNode(secondSubset, secondRange, rand, classifier, m_classifiers, instsNumAllClasses);
    }
  }
  






  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    

    result.disableAllClasses();
    result.enable(Capabilities.Capability.NOMINAL_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(1);
    
    return result;
  }
  






  public void buildClassifier(Instances data)
    throws Exception
  {
    getCapabilities().testWithFail(data);
    

    data = new Instances(data);
    data.deleteWithMissingClass();
    
    Random random = data.getRandomNumberGenerator(m_Seed);
    
    if (!m_hashtablegiven) {
      m_classifiers = new Hashtable();
    }
    


    boolean[] present = new boolean[data.numClasses()];
    for (int i = 0; i < data.numInstances(); i++) {
      present[((int)data.instance(i).classValue())] = true;
    }
    StringBuffer list = new StringBuffer();
    for (int i = 0; i < present.length; i++) {
      if (present[i] != 0) {
        if (list.length() > 0) {
          list.append(",");
        }
        list.append(i + 1);
      }
    }
    

    double[] instsNum = new double[data.numClasses()];
    for (int i = 0; i < data.numInstances(); i++) {
      instsNum[((int)data.instance(i).classValue())] += data.instance(i).weight();
    }
    
    Range newRange = new Range(list.toString());
    newRange.setUpper(data.numClasses() - 1);
    
    generateClassifierForNode(data, newRange, random, m_Classifier, m_classifiers, instsNum);
  }
  






  public double[] distributionForInstance(Instance inst)
    throws Exception
  {
    double[] newDist = new double[inst.numClasses()];
    if (m_FirstSuccessor == null) {
      for (int i = 0; i < inst.numClasses(); i++) {
        if (m_Range.isInRange(i)) {
          newDist[i] = 1.0D;
        }
      }
      return newDist;
    }
    double[] firstDist = m_FirstSuccessor.distributionForInstance(inst);
    double[] secondDist = m_SecondSuccessor.distributionForInstance(inst);
    double[] dist = m_FilteredClassifier.distributionForInstance(inst);
    for (int i = 0; i < inst.numClasses(); i++) {
      if ((firstDist[i] > 0.0D) && (secondDist[i] > 0.0D)) {
        System.err.println("Panik!!");
      }
      if (m_Range.isInRange(i)) {
        newDist[i] = (dist[1] * firstDist[i]);
      } else {
        newDist[i] = (dist[0] * secondDist[i]);
      }
    }
    if (!Utils.eq(Utils.sum(newDist), 1.0D)) {
      System.err.println(Utils.sum(newDist));
      for (int j = 0; j < dist.length; j++) {
        System.err.print(dist[j] + " ");
      }
      System.err.println();
      for (int j = 0; j < newDist.length; j++) {
        System.err.print(newDist[j] + " ");
      }
      System.err.println();
      System.err.println(inst);
      System.err.println(m_FilteredClassifier);
      
      System.err.println("bad");
    }
    return newDist;
  }
  







  public String getString(int[] indices)
  {
    StringBuffer string = new StringBuffer();
    for (int i = 0; i < indices.length; i++) {
      if (i > 0) {
        string.append(',');
      }
      string.append(indices[i]);
    }
    return string.toString();
  }
  




  public String globalInfo()
  {
    return "A meta classifier for handling multi-class datasets with 2-class classifiers by building a random data-balanced tree structure.\n\nFor more info, check\n\n" + getTechnicalInformation().toString();
  }
  









  public String toString()
  {
    if (m_classifiers == null) {
      return "DataNearBalancedND: No model built yet.";
    }
    StringBuffer text = new StringBuffer();
    text.append("DataNearBalancedND");
    treeToString(text, 0);
    
    return text.toString();
  }
  







  private int treeToString(StringBuffer text, int nn)
  {
    nn++;
    text.append("\n\nNode number: " + nn + "\n\n");
    if (m_FilteredClassifier != null) {
      text.append(m_FilteredClassifier);
    } else {
      text.append("null");
    }
    if (m_FirstSuccessor != null) {
      nn = m_FirstSuccessor.treeToString(text, nn);
      nn = m_SecondSuccessor.treeToString(text, nn);
    }
    return nn;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.8 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new DataNearBalancedND(), argv);
  }
}
