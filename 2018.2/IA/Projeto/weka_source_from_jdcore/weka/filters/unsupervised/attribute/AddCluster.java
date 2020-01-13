package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.Vector;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;


















































public class AddCluster
  extends Filter
  implements UnsupervisedFilter, OptionHandler
{
  static final long serialVersionUID = 7414280611943807337L;
  protected Clusterer m_Clusterer = new SimpleKMeans();
  

  protected Range m_IgnoreAttributesRange = null;
  

  protected Filter m_removeAttributes = new Remove();
  




  public AddCluster() {}
  



  public Capabilities getCapabilities(Instances data)
  {
    Instances newData = new Instances(data, 0);
    newData.setClassIndex(-1);
    
    return super.getCapabilities(newData);
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = m_Clusterer.getCapabilities();
    result.enableAllClasses();
    
    result.setMinimumNumberInstances(0);
    
    return result;
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
    
    return false;
  }
  





  protected Instances removeIgnored(Instances data)
    throws Exception
  {
    Instances result = data;
    
    if ((m_IgnoreAttributesRange != null) || (data.classIndex() >= 0)) {
      m_removeAttributes = new Remove();
      String rangeString = "";
      if (m_IgnoreAttributesRange != null) {
        rangeString = rangeString + m_IgnoreAttributesRange.getRanges();
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
    
    Instances toFilter = getInputFormat();
    
    if (!isFirstBatchDone())
    {
      Instances toFilterIgnoringAttributes = removeIgnored(toFilter);
      

      m_Clusterer.buildClusterer(toFilterIgnoringAttributes);
      

      Instances filtered = new Instances(toFilter, 0);
      FastVector nominal_values = new FastVector(m_Clusterer.numberOfClusters());
      for (int i = 0; i < m_Clusterer.numberOfClusters(); i++) {
        nominal_values.addElement("cluster" + (i + 1));
      }
      filtered.insertAttributeAt(new Attribute("cluster", nominal_values), filtered.numAttributes());
      

      setOutputFormat(filtered);
    }
    

    for (int i = 0; i < toFilter.numInstances(); i++) {
      convertInstance(toFilter.instance(i));
    }
    
    flushInput();
    m_NewBatch = true;
    m_FirstBatchDone = true;
    
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
  






  protected void convertInstance(Instance instance)
    throws Exception
  {
    Instance original = instance;
    

    double[] instanceVals = new double[instance.numAttributes() + 1];
    for (int j = 0; j < instance.numAttributes(); j++) {
      instanceVals[j] = original.value(j);
    }
    Instance filteredI = null;
    if (m_removeAttributes != null) {
      m_removeAttributes.input(instance);
      filteredI = m_removeAttributes.output();
    } else {
      filteredI = instance;
    }
    
    try
    {
      instanceVals[instance.numAttributes()] = m_Clusterer.clusterInstance(filteredI);
    }
    catch (Exception e)
    {
      instanceVals[instance.numAttributes()] = Instance.missingValue();
    }
    Instance processed;
    Instance processed;
    if ((original instanceof SparseInstance)) {
      processed = new SparseInstance(original.weight(), instanceVals);
    } else {
      processed = new Instance(original.weight(), instanceVals);
    }
    
    processed.setDataset(instance.dataset());
    copyValues(processed, false, instance.dataset(), getOutputFormat());
    processed.setDataset(getOutputFormat());
    
    push(processed);
  }
  





  public Enumeration listOptions()
  {
    Vector newVector = new Vector(2);
    
    newVector.addElement(new Option("\tFull class name of clusterer to use, followed\n\tby scheme options. eg:\n\t\t\"weka.clusterers.SimpleKMeans -N 3\"\n\t(default: weka.clusterers.SimpleKMeans)", "W", 1, "-W <clusterer specification>"));
    





    newVector.addElement(new Option("\tThe range of attributes the clusterer should ignore.\n", "I", 1, "-I <att1,att2-att4,...>"));
    


    return newVector.elements();
  }
  





















  public void setOptions(String[] options)
    throws Exception
  {
    String clustererString = Utils.getOption('W', options);
    if (clustererString.length() == 0)
      clustererString = SimpleKMeans.class.getName();
    String[] clustererSpec = Utils.splitOptions(clustererString);
    if (clustererSpec.length == 0) {
      throw new Exception("Invalid clusterer specification string");
    }
    String clustererName = clustererSpec[0];
    clustererSpec[0] = "";
    setClusterer(AbstractClusterer.forName(clustererName, clustererSpec));
    
    setIgnoredAttributeIndices(Utils.getOption('I', options));
    
    Utils.checkForRemainingOptions(options);
  }
  





  public String[] getOptions()
  {
    String[] options = new String[5];
    int current = 0;
    
    options[(current++)] = "-W";options[(current++)] = ("" + getClustererSpec());
    
    if (!getIgnoredAttributeIndices().equals("")) {
      options[(current++)] = "-I";options[(current++)] = getIgnoredAttributeIndices();
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  






  public String globalInfo()
  {
    return "A filter that adds a new nominal attribute representing the cluster assigned to each instance by the specified clustering algorithm.";
  }
  







  public String clustererTipText()
  {
    return "The clusterer to assign clusters with.";
  }
  





  public void setClusterer(Clusterer clusterer)
  {
    m_Clusterer = clusterer;
  }
  





  public Clusterer getClusterer()
  {
    return m_Clusterer;
  }
  






  protected String getClustererSpec()
  {
    Clusterer c = getClusterer();
    if ((c instanceof OptionHandler)) {
      return c.getClass().getName() + " " + Utils.joinOptions(((OptionHandler)c).getOptions());
    }
    
    return c.getClass().getName();
  }
  






  public String ignoredAttributeIndicesTipText()
  {
    return "The range of attributes to be ignored by the clusterer. eg: first-3,5,9-last";
  }
  





  public String getIgnoredAttributeIndices()
  {
    if (m_IgnoreAttributesRange == null) {
      return "";
    }
    return m_IgnoreAttributesRange.getRanges();
  }
  









  public void setIgnoredAttributeIndices(String rangeList)
  {
    if ((rangeList == null) || (rangeList.length() == 0)) {
      m_IgnoreAttributesRange = null;
    } else {
      m_IgnoreAttributesRange = new Range();
      m_IgnoreAttributesRange.setRanges(rangeList);
    }
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 1.13 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new AddCluster(), argv);
  }
}
