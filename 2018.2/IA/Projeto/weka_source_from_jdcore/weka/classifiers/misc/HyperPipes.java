package weka.classifiers.misc;

import java.io.PrintStream;
import java.io.Serializable;
import weka.classifiers.Classifier;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.UnsupportedAttributeTypeException;
import weka.core.Utils;






















































public class HyperPipes
  extends Classifier
{
  static final long serialVersionUID = -7527596632268975274L;
  protected int m_ClassIndex;
  protected Instances m_Instances;
  protected HyperPipe[] m_HyperPipes;
  protected Classifier m_ZeroR;
  
  public HyperPipes() {}
  
  public String globalInfo()
  {
    return "Class implementing a HyperPipe classifier. For each category a HyperPipe is constructed that contains all points of that category (essentially records the attribute bounds observed for each category). Test instances are classified according to the category that \"most contains the instance\".\nDoes not handle numeric class, or missing values in test cases. Extremely simple algorithm, but has the advantage of being extremely fast, and works quite well when you have \"smegloads\" of attributes.";
  }
  






  class HyperPipe
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = 3972254260367902025L;
    





    protected double[][] m_NumericBounds;
    





    protected boolean[][] m_NominalBounds;
    





    public HyperPipe(Instances instances)
      throws Exception
    {
      m_NumericBounds = new double[instances.numAttributes()][];
      m_NominalBounds = new boolean[instances.numAttributes()][];
      
      for (int i = 0; i < instances.numAttributes(); i++) {
        switch (instances.attribute(i).type()) {
        case 0: 
          m_NumericBounds[i] = new double[2];
          m_NumericBounds[i][0] = Double.POSITIVE_INFINITY;
          m_NumericBounds[i][1] = Double.NEGATIVE_INFINITY;
          break;
        case 1: 
          m_NominalBounds[i] = new boolean[instances.attribute(i).numValues()];
          break;
        default: 
          throw new UnsupportedAttributeTypeException("Cannot process string attributes!");
        }
        
      }
      for (int i = 0; i < instances.numInstances(); i++) {
        addInstance(instances.instance(i));
      }
    }
    







    public void addInstance(Instance instance)
      throws Exception
    {
      for (int j = 0; j < instance.numAttributes(); j++) {
        if ((j != m_ClassIndex) && (!instance.isMissing(j)))
        {
          double current = instance.value(j);
          
          if (m_NumericBounds[j] != null) {
            if (current < m_NumericBounds[j][0])
              m_NumericBounds[j][0] = current;
            if (current > m_NumericBounds[j][1]) {
              m_NumericBounds[j][1] = current;
            }
          } else {
            m_NominalBounds[j][((int)current)] = 1;
          }
        }
      }
    }
    








    public double partialContains(Instance instance)
      throws Exception
    {
      int count = 0;
      for (int i = 0; i < instance.numAttributes(); i++)
      {
        if (i != m_ClassIndex)
        {

          if (!instance.isMissing(i))
          {


            double current = instance.value(i);
            
            if (m_NumericBounds[i] != null) {
              if ((current >= m_NumericBounds[i][0]) && (current <= m_NumericBounds[i][1]))
              {
                count++;
              }
            }
            else if (m_NominalBounds[i][((int)current)] != 0) {
              count++;
            }
          }
        }
      }
      return count / (instance.numAttributes() - 1);
    }
    




    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 5528 $");
    }
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
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    

    result.setMinimumNumberInstances(0);
    
    return result;
  }
  






  public void buildClassifier(Instances instances)
    throws Exception
  {
    getCapabilities().testWithFail(instances);
    

    instances = new Instances(instances);
    instances.deleteWithMissingClass();
    

    if (instances.numAttributes() == 1) {
      System.err.println("Cannot build model (only class attribute present in data!), using ZeroR model instead!");
      

      m_ZeroR = new ZeroR();
      m_ZeroR.buildClassifier(instances);
      return;
    }
    
    m_ZeroR = null;
    

    m_ClassIndex = instances.classIndex();
    m_Instances = new Instances(instances, 0);
    

    m_HyperPipes = new HyperPipe[instances.numClasses()];
    for (int i = 0; i < m_HyperPipes.length; i++) {
      m_HyperPipes[i] = new HyperPipe(new Instances(instances, 0));
    }
    

    for (int i = 0; i < instances.numInstances(); i++) {
      updateClassifier(instances.instance(i));
    }
  }
  






  public void updateClassifier(Instance instance)
    throws Exception
  {
    if (instance.classIsMissing()) {
      return;
    }
    m_HyperPipes[((int)instance.classValue())].addInstance(instance);
  }
  








  public double[] distributionForInstance(Instance instance)
    throws Exception
  {
    if (m_ZeroR != null) {
      return m_ZeroR.distributionForInstance(instance);
    }
    
    double[] dist = new double[m_HyperPipes.length];
    
    for (int j = 0; j < m_HyperPipes.length; j++) {
      dist[j] = m_HyperPipes[j].partialContains(instance);
    }
    
    double sum = Utils.sum(dist);
    if (sum <= 0.0D) {
      for (int j = 0; j < dist.length; j++) {
        dist[j] = (1.0D / dist.length);
      }
      return dist;
    }
    Utils.normalize(dist, sum);
    return dist;
  }
  








  public String toString()
  {
    if (m_ZeroR != null) {
      StringBuffer buf = new StringBuffer();
      buf.append(getClass().getName().replaceAll(".*\\.", "") + "\n");
      buf.append(getClass().getName().replaceAll(".*\\.", "").replaceAll(".", "=") + "\n\n");
      buf.append("Warning: No model could be built, hence ZeroR model is used:\n\n");
      buf.append(m_ZeroR.toString());
      return buf.toString();
    }
    
    if (m_HyperPipes == null) {
      return "HyperPipes classifier";
    }
    
    StringBuffer text = new StringBuffer("HyperPipes classifier\n");
    








    return text.toString();
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5528 $");
  }
  





  public static void main(String[] argv)
  {
    runClassifier(new HyperPipes(), argv);
  }
}
