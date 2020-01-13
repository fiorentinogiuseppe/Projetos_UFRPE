package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.clusterers.AbstractDensityBasedClusterer;
import weka.clusterers.DensityBasedClusterer;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;





















































public class ClusterMembership
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  static final long serialVersionUID = 6675702504667714026L;
  protected DensityBasedClusterer m_clusterer = new EM();
  

  protected DensityBasedClusterer[] m_clusterers;
  

  protected Range m_ignoreAttributesRange;
  

  protected Filter m_removeAttributes;
  

  protected double[] m_priors;
  


  public ClusterMembership() {}
  

  public Capabilities getCapabilities()
  {
    Capabilities result = m_clusterer.getCapabilities();
    
    result.setMinimumNumberInstances(0);
    
    return result;
  }
  









  public Capabilities getCapabilities(Instances data)
  {
    Instances newData = new Instances(data, 0);
    newData.setClassIndex(-1);
    
    return super.getCapabilities(newData);
  }
  




  protected void testInputFormat(Instances instanceInfo)
    throws Exception
  {
    getCapabilities(instanceInfo).testWithFail(removeIgnored(instanceInfo));
  }
  








  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    m_removeAttributes = null;
    m_priors = null;
    
    return false;
  }
  





  protected Instances removeIgnored(Instances data)
    throws Exception
  {
    Instances result = data;
    
    if ((m_ignoreAttributesRange != null) || (data.classIndex() >= 0)) {
      result = new Instances(data);
      m_removeAttributes = new Remove();
      String rangeString = "";
      if (m_ignoreAttributesRange != null) {
        rangeString = rangeString + m_ignoreAttributesRange.getRanges();
      }
      if (data.classIndex() >= 0) {
        if (rangeString.length() > 0) {
          rangeString = rangeString + "," + (data.classIndex() + 1);
        } else {
          rangeString = "" + (data.classIndex() + 1);
        }
      }
      ((Remove)m_removeAttributes).setAttributeIndices(rangeString);
      ((Remove)m_removeAttributes).setInvertSelection(false);
      m_removeAttributes.setInputFormat(data);
      result = Filter.useFilter(data, m_removeAttributes);
    }
    
    return result;
  }
  





  public boolean batchFinished()
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    
    if (outputFormatPeek() == null) {
      Instances toFilter = getInputFormat();
      
      Instances[] toFilterIgnoringAttributes;
      
      if ((toFilter.classIndex() >= 0) && (toFilter.classAttribute().isNominal())) {
        Instances[] toFilterIgnoringAttributes = new Instances[toFilter.numClasses()];
        for (int i = 0; i < toFilter.numClasses(); i++) {
          toFilterIgnoringAttributes[i] = new Instances(toFilter, toFilter.numInstances());
        }
        for (int i = 0; i < toFilter.numInstances(); i++) {
          toFilterIgnoringAttributes[((int)toFilter.instance(i).classValue())].add(toFilter.instance(i));
        }
        m_priors = new double[toFilter.numClasses()];
        for (int i = 0; i < toFilter.numClasses(); i++) {
          toFilterIgnoringAttributes[i].compactify();
          m_priors[i] = toFilterIgnoringAttributes[i].sumOfWeights();
        }
        Utils.normalize(m_priors);
      } else {
        toFilterIgnoringAttributes = new Instances[1];
        toFilterIgnoringAttributes[0] = toFilter;
        m_priors = new double[1];
        m_priors[0] = 1.0D;
      }
      

      for (int i = 0; i < toFilterIgnoringAttributes.length; i++) {
        toFilterIgnoringAttributes[i] = removeIgnored(toFilterIgnoringAttributes[i]);
      }
      
      if ((toFilter.classIndex() <= 0) || (!toFilter.classAttribute().isNominal())) {
        m_clusterers = AbstractDensityBasedClusterer.makeCopies(m_clusterer, 1);
        m_clusterers[0].buildClusterer(toFilterIgnoringAttributes[0]);
      } else {
        m_clusterers = AbstractDensityBasedClusterer.makeCopies(m_clusterer, toFilter.numClasses());
        for (int i = 0; i < m_clusterers.length; i++) {
          if (toFilterIgnoringAttributes[i].numInstances() == 0) {
            m_clusterers[i] = null;
          } else {
            m_clusterers[i].buildClusterer(toFilterIgnoringAttributes[i]);
          }
        }
      }
      

      FastVector attInfo = new FastVector();
      for (int j = 0; j < m_clusterers.length; j++) {
        if (m_clusterers[j] != null) {
          for (int i = 0; i < m_clusterers[j].numberOfClusters(); i++) {
            attInfo.addElement(new Attribute("pCluster_" + j + "_" + i));
          }
        }
      }
      if (toFilter.classIndex() >= 0) {
        attInfo.addElement(toFilter.classAttribute().copy());
      }
      attInfo.trimToSize();
      Instances filtered = new Instances(toFilter.relationName() + "_clusterMembership", attInfo, 0);
      
      if (toFilter.classIndex() >= 0) {
        filtered.setClassIndex(filtered.numAttributes() - 1);
      }
      setOutputFormat(filtered);
      

      for (int i = 0; i < toFilter.numInstances(); i++) {
        convertInstance(toFilter.instance(i));
      }
    }
    flushInput();
    
    m_NewBatch = true;
    return numPendingOutput() != 0;
  }
  









  public boolean input(Instance instance)
    throws Exception
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    
    if (outputFormatPeek() != null) {
      convertInstance(instance);
      return true;
    }
    
    bufferInput(instance);
    return false;
  }
  







  protected double[] logs2densities(int j, Instance in)
    throws Exception
  {
    double[] logs = m_clusterers[j].logJointDensitiesForInstance(in);
    
    for (int i = 0; i < logs.length; i++) {
      logs[i] += Math.log(m_priors[j]);
    }
    return logs;
  }
  







  protected void convertInstance(Instance instance)
    throws Exception
  {
    double[] instanceVals = new double[outputFormatPeek().numAttributes()];
    double[] tempvals;
    if (instance.classIndex() >= 0) {
      tempvals = new double[outputFormatPeek().numAttributes() - 1];
    } else {
      tempvals = new double[outputFormatPeek().numAttributes()];
    }
    int pos = 0;
    for (int j = 0; j < m_clusterers.length; j++) {
      if (m_clusterers[j] != null) { double[] probs;
        double[] probs;
        if (m_removeAttributes != null) {
          m_removeAttributes.input(instance);
          probs = logs2densities(j, m_removeAttributes.output());
        } else {
          probs = logs2densities(j, instance);
        }
        System.arraycopy(probs, 0, tempvals, pos, probs.length);
        pos += probs.length;
      }
    }
    double[] tempvals = Utils.logs2probs(tempvals);
    System.arraycopy(tempvals, 0, instanceVals, 0, tempvals.length);
    if (instance.classIndex() >= 0) {
      instanceVals[(instanceVals.length - 1)] = instance.classValue();
    }
    
    push(new Instance(instance.weight(), instanceVals));
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tFull name of clusterer to use. eg:\n\t\tweka.clusterers.EM\n\tAdditional options after the '--'.\n\t(default: weka.clusterers.EM)", "W", 1, "-W <clusterer name>"));
    





    newVector.addElement(new Option("\tThe range of attributes the clusterer should ignore.\n\t(the class attribute is automatically ignored)", "I", 1, "-I <att1,att2-att4,...>"));
    



    return newVector.elements();
  }
  






















  public void setOptions(String[] options)
    throws Exception
  {
    String clustererString = Utils.getOption('W', options);
    if (clustererString.length() == 0)
      clustererString = EM.class.getName();
    setDensityBasedClusterer((DensityBasedClusterer)Utils.forName(DensityBasedClusterer.class, clustererString, Utils.partitionOptions(options)));
    


    setIgnoredAttributeIndices(Utils.getOption('I', options));
    Utils.checkForRemainingOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] clustererOptions = new String[0];
    if ((m_clusterer != null) && ((m_clusterer instanceof OptionHandler)))
    {
      clustererOptions = ((OptionHandler)m_clusterer).getOptions();
    }
    String[] options = new String[clustererOptions.length + 5];
    int current = 0;
    
    if (!getIgnoredAttributeIndices().equals("")) {
      options[(current++)] = "-I";
      options[(current++)] = getIgnoredAttributeIndices();
    }
    
    if (m_clusterer != null) {
      options[(current++)] = "-W";
      options[(current++)] = getDensityBasedClusterer().getClass().getName();
    }
    
    options[(current++)] = "--";
    System.arraycopy(clustererOptions, 0, options, current, clustererOptions.length);
    
    current += clustererOptions.length;
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  






  public String globalInfo()
  {
    return "A filter that uses a density-based clusterer to generate cluster membership values; filtered instances are composed of these values plus the class attribute (if set in the input data). If a (nominal) class attribute is set, the clusterer is run separately for each class. The class attribute (if set) and any user-specified attributes are ignored during the clustering operation";
  }
  










  public String densityBasedClustererTipText()
  {
    return "The clusterer that will generate membership values for the instances.";
  }
  




  public void setDensityBasedClusterer(DensityBasedClusterer newClusterer)
  {
    m_clusterer = newClusterer;
  }
  




  public DensityBasedClusterer getDensityBasedClusterer()
  {
    return m_clusterer;
  }
  






  public String ignoredAttributeIndicesTipText()
  {
    return "The range of attributes to be ignored by the clusterer. eg: first-3,5,9-last";
  }
  





  public String getIgnoredAttributeIndices()
  {
    if (m_ignoreAttributesRange == null) {
      return "";
    }
    return m_ignoreAttributesRange.getRanges();
  }
  









  public void setIgnoredAttributeIndices(String rangeList)
  {
    if ((rangeList == null) || (rangeList.length() == 0)) {
      m_ignoreAttributesRange = null;
    } else {
      m_ignoreAttributesRange = new Range();
      m_ignoreAttributesRange.setRanges(rangeList);
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.16 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new ClusterMembership(), argv);
  }
}
