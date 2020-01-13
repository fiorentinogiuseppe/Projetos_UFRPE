package weka.classifiers.meta.nestedDichotomies;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Random;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableSingleClassifierEnhancer;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.MakeIndicator;
import weka.filters.unsupervised.instance.RemoveWithValues;



























































































































public class ND
  extends RandomizableSingleClassifierEnhancer
  implements TechnicalInformationHandler
{
  static final long serialVersionUID = -6355893369855683820L;
  
  protected class NDTree
    implements Serializable, RevisionHandler
  {
    private static final long serialVersionUID = 4284655952754474880L;
    protected FastVector m_indices = null;
    

    protected NDTree m_parent = null;
    

    protected NDTree m_left = null;
    

    protected NDTree m_right = null;
    



    protected NDTree()
    {
      m_indices = new FastVector(1);
      m_indices.addElement(new Integer(Integer.MAX_VALUE));
    }
    



    protected NDTree locateNode(int nodeIndex, int[] currentIndex)
    {
      if (nodeIndex == currentIndex[0])
        return this;
      if (m_left == null) {
        return null;
      }
      currentIndex[0] += 1;
      NDTree leftresult = m_left.locateNode(nodeIndex, currentIndex);
      if (leftresult != null) {
        return leftresult;
      }
      currentIndex[0] += 1;
      return m_right.locateNode(nodeIndex, currentIndex);
    }
    








    protected void insertClassIndex(int classIndex)
    {
      NDTree right = new NDTree(ND.this);
      if (m_left != null) {
        m_right.m_parent = right;
        m_left.m_parent = right;
        m_right = m_right;
        m_left = m_left;
      }
      m_right = right;
      m_right.m_indices = ((FastVector)m_indices.copy());
      m_right.m_parent = this;
      m_left = new NDTree(ND.this);
      m_left.insertClassIndexAtNode(classIndex);
      m_left.m_parent = this;
      

      propagateClassIndex(classIndex);
    }
    





    protected void propagateClassIndex(int classIndex)
    {
      insertClassIndexAtNode(classIndex);
      if (m_parent != null) {
        m_parent.propagateClassIndex(classIndex);
      }
    }
    





    protected void insertClassIndexAtNode(int classIndex)
    {
      int i = 0;
      while (classIndex > ((Integer)m_indices.elementAt(i)).intValue()) {
        i++;
      }
      m_indices.insertElementAt(new Integer(classIndex), i);
    }
    





    protected int[] getIndices()
    {
      int[] ints = new int[m_indices.size() - 1];
      for (int i = 0; i < m_indices.size() - 1; i++) {
        ints[i] = ((Integer)m_indices.elementAt(i)).intValue();
      }
      return ints;
    }
    






    protected boolean contains(int index)
    {
      for (int i = 0; i < m_indices.size() - 1; i++) {
        if (index == ((Integer)m_indices.elementAt(i)).intValue()) {
          return true;
        }
      }
      return false;
    }
    





    protected String getString()
    {
      StringBuffer string = new StringBuffer();
      for (int i = 0; i < m_indices.size() - 1; i++) {
        if (i > 0) {
          string.append(',');
        }
        string.append(((Integer)m_indices.elementAt(i)).intValue() + 1);
      }
      return string.toString();
    }
    



    protected void unifyTree()
    {
      if (m_left != null) {
        if (((Integer)m_left.m_indices.elementAt(0)).intValue() > ((Integer)m_right.m_indices.elementAt(0)).intValue())
        {
          NDTree temp = m_left;
          m_left = m_right;
          m_right = temp;
        }
        m_left.unifyTree();
        m_right.unifyTree();
      }
    }
    







    protected void toString(StringBuffer text, int[] id, int level)
    {
      for (int i = 0; i < level; i++) {
        text.append("   | ");
      }
      text.append(id[0] + ": " + getString() + "\n");
      if (m_left != null) {
        id[0] += 1;
        m_left.toString(text, id, level + 1);
        id[0] += 1;
        m_right.toString(text, id, level + 1);
      }
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 1.9 $");
    }
  }
  

  protected NDTree m_ndtree = null;
  

  protected Hashtable m_classifiers = null;
  

  protected boolean m_hashtablegiven = false;
  



  public ND()
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
    

    int[] indices = new int[data.numClasses()];
    for (int i = 0; i < indices.length; i++) {
      indices[i] = i;
    }
    

    for (int i = indices.length - 1; i > 0; i--) {
      int help = indices[i];
      int index = random.nextInt(i + 1);
      indices[i] = indices[index];
      indices[index] = help;
    }
    

    m_ndtree = new NDTree();
    m_ndtree.insertClassIndexAtNode(indices[0]);
    for (int i = 1; i < indices.length; i++) {
      int nodeIndex = random.nextInt(2 * i - 1);
      
      NDTree node = m_ndtree.locateNode(nodeIndex, new int[1]);
      node.insertClassIndex(indices[i]);
    }
    m_ndtree.unifyTree();
    


    buildClassifierForNode(m_ndtree, data);
  }
  







  public void buildClassifierForNode(NDTree node, Instances data)
    throws Exception
  {
    if (m_left != null)
    {

      MakeIndicator filter = new MakeIndicator();
      filter.setAttributeIndex("" + (data.classIndex() + 1));
      filter.setValueIndices(m_right.getString());
      filter.setNumeric(false);
      filter.setInputFormat(data);
      FilteredClassifier classifier = new FilteredClassifier();
      if (data.numInstances() > 0) {
        classifier.setClassifier(Classifier.makeCopies(m_Classifier, 1)[0]);
      } else {
        classifier.setClassifier(new ZeroR());
      }
      classifier.setFilter(filter);
      
      if (!m_classifiers.containsKey(m_left.getString() + "|" + m_right.getString())) {
        classifier.buildClassifier(data);
        m_classifiers.put(m_left.getString() + "|" + m_right.getString(), classifier);
      } else {
        classifier = (FilteredClassifier)m_classifiers.get(m_left.getString() + "|" + m_right.getString());
      }
      


      if (m_left.m_left != null) {
        RemoveWithValues rwv = new RemoveWithValues();
        rwv.setInvertSelection(true);
        rwv.setNominalIndices(m_left.getString());
        rwv.setAttributeIndex("" + (data.classIndex() + 1));
        rwv.setInputFormat(data);
        Instances firstSubset = Filter.useFilter(data, rwv);
        buildClassifierForNode(m_left, firstSubset);
      }
      if (m_right.m_left != null) {
        RemoveWithValues rwv = new RemoveWithValues();
        rwv.setInvertSelection(true);
        rwv.setNominalIndices(m_right.getString());
        rwv.setAttributeIndex("" + (data.classIndex() + 1));
        rwv.setInputFormat(data);
        Instances secondSubset = Filter.useFilter(data, rwv);
        buildClassifierForNode(m_right, secondSubset);
      }
    }
  }
  






  public double[] distributionForInstance(Instance inst)
    throws Exception
  {
    return distributionForInstance(inst, m_ndtree);
  }
  







  protected double[] distributionForInstance(Instance inst, NDTree node)
    throws Exception
  {
    double[] newDist = new double[inst.numClasses()];
    if (m_left == null) {
      newDist[node.getIndices()[0]] = 1.0D;
      return newDist;
    }
    Classifier classifier = (Classifier)m_classifiers.get(m_left.getString() + "|" + m_right.getString());
    
    double[] leftDist = distributionForInstance(inst, m_left);
    double[] rightDist = distributionForInstance(inst, m_right);
    double[] dist = classifier.distributionForInstance(inst);
    
    for (int i = 0; i < inst.numClasses(); i++) {
      if (m_right.contains(i)) {
        newDist[i] = (dist[1] * rightDist[i]);
      } else {
        newDist[i] = (dist[0] * leftDist[i]);
      }
    }
    return newDist;
  }
  






  public String toString()
  {
    if (m_classifiers == null) {
      return "ND: No model built yet.";
    }
    StringBuffer text = new StringBuffer();
    text.append("ND\n\n");
    m_ndtree.toString(text, new int[1], 0);
    
    return text.toString();
  }
  




  public String globalInfo()
  {
    return "A meta classifier for handling multi-class datasets with 2-class classifiers by building a random tree structure.\n\nFor more info, check\n\n" + getTechnicalInformation().toString();
  }
  








  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.9 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new ND(), argv);
  }
}
