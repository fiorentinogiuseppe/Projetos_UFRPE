package weka.filters.unsupervised.attribute;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.filters.AllFilter;
import weka.filters.Filter;
import weka.filters.SimpleBatchFilter;

























































public class PartitionedMultiFilter
  extends SimpleBatchFilter
{
  private static final long serialVersionUID = -6293720886005713120L;
  protected Filter[] m_Filters = { new AllFilter() };
  

  protected Range[] m_Ranges = { new Range("first-last") };
  

  protected boolean m_RemoveUnused = false;
  

  protected int[] m_IndicesUnused = new int[0];
  

  public PartitionedMultiFilter() {}
  

  public String globalInfo()
  {
    return "A filter that applies filters on subsets of attributes and assembles the output into a new dataset. Attributes that are not covered by any of the ranges can be either retained or removed from the output.";
  }
  








  public Enumeration listOptions()
  {
    Vector result = new Vector();
    Enumeration enm = super.listOptions();
    while (enm.hasMoreElements()) {
      result.add(enm.nextElement());
    }
    result.addElement(new Option("\tA filter to apply (can be specified multiple times).", "F", 1, "-F <classname [options]>"));
    


    result.addElement(new Option("\tAn attribute range (can be specified multiple times).\n\tFor each filter a range must be supplied. 'first' and 'last'\n\tare valid indices. 'inv(...)' around the range denotes an\n\tinverted range.", "R", 1, "-R <range>"));
    





    result.addElement(new Option("\tFlag for leaving unused attributes out of the output, by default\n\tthese are included in the filter output.", "U", 0, "-U"));
    



    return result.elements();
  }
  































  public void setOptions(String[] options)
    throws Exception
  {
    super.setOptions(options);
    
    setRemoveUnused(Utils.getFlag("U", options));
    
    Vector objects = new Vector();
    String tmpStr; while ((tmpStr = Utils.getOption("F", options)).length() != 0) {
      String[] options2 = Utils.splitOptions(tmpStr);
      String classname = options2[0];
      options2[0] = "";
      objects.add(Utils.forName(Filter.class, classname, options2));
    }
    

    if (objects.size() == 0) {
      objects.add(new AllFilter());
    }
    setFilters((Filter[])objects.toArray(new Filter[objects.size()]));
    
    objects = new Vector();
    while ((tmpStr = Utils.getOption("R", options)).length() != 0) { Range range;
      if ((tmpStr.startsWith("inv(")) && (tmpStr.endsWith(")"))) {
        Range range = new Range(tmpStr.substring(4, tmpStr.length() - 1));
        range.setInvert(true);
      }
      else {
        range = new Range(tmpStr);
      }
      objects.add(range);
    }
    

    if (objects.size() == 0) {
      objects.add(new Range("first-last"));
    }
    setRanges((Range[])objects.toArray(new Range[objects.size()]));
    

    checkDimensions();
  }
  









  public String[] getOptions()
  {
    Vector result = new Vector();
    
    String[] options = super.getOptions();
    for (int i = 0; i < options.length; i++) {
      result.add(options[i]);
    }
    if (getRemoveUnused()) {
      result.add("-U");
    }
    for (i = 0; i < getFilters().length; i++) {
      result.add("-F");
      result.add(getFilterSpec(getFilter(i)));
    }
    
    for (i = 0; i < getRanges().length; i++) {
      String tmpStr = getRange(i).getRanges();
      if (getRange(i).getInvert())
        tmpStr = "inv(" + tmpStr + ")";
      result.add("-R");
      result.add(tmpStr);
    }
    
    return (String[])result.toArray(new String[result.size()]);
  }
  



  protected void checkDimensions()
    throws Exception
  {
    if (getFilters().length != getRanges().length) {
      throw new IllegalArgumentException("Number of filters (= " + getFilters().length + ") " + "and ranges (= " + getRanges().length + ") don't match!");
    }
  }
  





  protected void testInputFormat(Instances instanceInfo)
    throws Exception
  {
    for (int i = 0; i < getRanges().length; i++) {
      Instances newi = new Instances(instanceInfo, 0);
      if (instanceInfo.numInstances() > 0) {
        newi.add((Instance)instanceInfo.instance(0).copy());
      }
      Range range = getRanges()[i];
      range.setUpper(instanceInfo.numAttributes() - 1);
      Instances subset = generateSubset(newi, range);
      getFilters()[i].setInputFormat(subset);
    }
  }
  





  public void setRemoveUnused(boolean value)
  {
    m_RemoveUnused = value;
  }
  





  public boolean getRemoveUnused()
  {
    return m_RemoveUnused;
  }
  





  public String removeUnusedTipText()
  {
    return "If true then unused attributes (ones that are not covered by any of the ranges) will be removed from the output.";
  }
  









  public void setFilters(Filter[] filters)
  {
    m_Filters = filters;
    reset();
  }
  




  public Filter[] getFilters()
  {
    return m_Filters;
  }
  





  public String filtersTipText()
  {
    return "The base filters to be used.";
  }
  





  public Filter getFilter(int index)
  {
    return m_Filters[index];
  }
  


  protected String getFilterSpec(Filter filter)
  {
    String result;
    

    String result;
    
    if (filter == null) {
      result = "";
    }
    else {
      result = filter.getClass().getName();
      if ((filter instanceof OptionHandler)) {
        result = result + " " + Utils.joinOptions(((OptionHandler)filter).getOptions());
      }
    }
    
    return result;
  }
  







  public void setRanges(Range[] Ranges)
  {
    m_Ranges = Ranges;
    reset();
  }
  




  public Range[] getRanges()
  {
    return m_Ranges;
  }
  





  public String rangesTipText()
  {
    return "The attribute ranges to be used; 'inv(...)' denotes an inverted range.";
  }
  





  public Range getRange(int index)
  {
    return m_Ranges[index];
  }
  












  protected void determineUnusedIndices(Instances data)
  {
    Vector<Integer> indices = new Vector();
    for (int i = 0; i < data.numAttributes(); i++) {
      if (i != data.classIndex())
      {

        boolean covered = false;
        for (int n = 0; n < getRanges().length; n++) {
          if (getRanges()[n].isInRange(i)) {
            covered = true;
            break;
          }
        }
        
        if (!covered) {
          indices.add(new Integer(i));
        }
      }
    }
    m_IndicesUnused = new int[indices.size()];
    for (i = 0; i < indices.size(); i++) {
      m_IndicesUnused[i] = ((Integer)indices.get(i)).intValue();
    }
    if (getDebug()) {
      System.out.println("Unused indices: " + Utils.arrayToString(m_IndicesUnused));
    }
  }
  














  protected Instances generateSubset(Instances data, Range range)
    throws Exception
  {
    int[] indices = range.getSelection();
    StringBuilder atts = new StringBuilder();
    for (int i = 0; i < indices.length; i++) {
      if (i > 0)
        atts.append(",");
      atts.append("" + (indices[i] + 1));
    }
    if ((data.classIndex() > -1) && (!range.isInRange(data.classIndex()))) {
      atts.append("," + (data.classIndex() + 1));
    }
    
    Remove filter = new Remove();
    filter.setAttributeIndices(atts.toString());
    filter.setInvertSelection(true);
    filter.setInputFormat(data);
    

    Instances result = Filter.useFilter(data, filter);
    
    return result;
  }
  












  protected Instances renameAttributes(Instances data, String prefix)
    throws Exception
  {
    FastVector atts = new FastVector();
    for (int i = 0; i < data.numAttributes(); i++) {
      if (i == data.classIndex()) {
        atts.addElement((Attribute)data.attribute(i).copy());
      } else {
        atts.addElement(data.attribute(i).copy(prefix + data.attribute(i).name()));
      }
    }
    
    Instances result = new Instances(data.relationName(), atts, data.numInstances());
    for (i = 0; i < data.numInstances(); i++) {
      result.add((Instance)data.instance(i).copy());
    }
    

    if (data.classIndex() > -1) {
      result.setClassIndex(data.classIndex());
    }
    return result;
  }
  








  protected Instances determineOutputFormat(Instances inputFormat)
    throws Exception
  {
    Instances result;
    







    if (!isFirstBatchDone())
    {
      if (inputFormat.numInstances() == 0) {
        return null;
      }
      checkDimensions();
      

      determineUnusedIndices(inputFormat);
      
      FastVector atts = new FastVector();
      for (int i = 0; i < getFilters().length; i++) {
        if (!isFirstBatchDone())
        {
          Instances processed = generateSubset(inputFormat, getRange(i));
          
          if (!getFilter(i).setInputFormat(processed)) {
            Filter.useFilter(processed, getFilter(i));
          }
        }
        
        Instances processed = getFilter(i).getOutputFormat();
        

        processed = renameAttributes(processed, "filtered-" + i + "-");
        

        for (int n = 0; n < processed.numAttributes(); n++) {
          if (n != processed.classIndex())
          {
            atts.addElement((Attribute)processed.attribute(n).copy());
          }
        }
      }
      
      if (!getRemoveUnused()) {
        for (i = 0; i < m_IndicesUnused.length; i++) {
          Attribute att = inputFormat.attribute(m_IndicesUnused[i]);
          atts.addElement(att.copy("unfiltered-" + att.name()));
        }
      }
      

      if (inputFormat.classIndex() > -1) {
        atts.addElement((Attribute)inputFormat.classAttribute().copy());
      }
      
      Instances result = new Instances(inputFormat.relationName(), atts, 0);
      if (inputFormat.classIndex() > -1) {
        result.setClassIndex(result.numAttributes() - 1);
      }
    } else {
      result = getOutputFormat();
    }
    
    return result;
  }
  


















  protected Instances process(Instances instances)
    throws Exception
  {
    if (!isFirstBatchDone()) {
      checkDimensions();
      

      for (int i = 0; i < m_Ranges.length; i++) {
        m_Ranges[i].setUpper(instances.numAttributes() - 1);
      }
      
      determineUnusedIndices(instances);
    }
    

    Instances[] processed = new Instances[getFilters().length];
    for (int i = 0; i < getFilters().length; i++) {
      processed[i] = generateSubset(instances, getRange(i));
      if (!isFirstBatchDone())
        getFilter(i).setInputFormat(processed[i]);
      processed[i] = Filter.useFilter(processed[i], getFilter(i));
    }
    
    Instances result;
    if (!isFirstBatchDone()) {
      Instances result = determineOutputFormat(instances);
      setOutputFormat(result);
    }
    else {
      result = getOutputFormat();
    }
    

    Vector errors = new Vector();
    for (i = 0; i < processed.length; i++) {
      if (processed[i].numInstances() != instances.numInstances())
        errors.add(new Integer(i));
    }
    if (errors.size() > 0) {
      throw new IllegalStateException("The following filter(s) changed the number of instances: " + errors);
    }
    

    for (i = 0; i < instances.numInstances(); i++) {
      Instance inst = instances.instance(i);
      double[] values = new double[result.numAttributes()];
      

      int index = 0;
      for (int n = 0; n < processed.length; n++) {
        for (int m = 0; m < processed[n].numAttributes(); m++) {
          if (m != processed[n].classIndex())
          {
            if (result.attribute(index).isString()) {
              values[index] = result.attribute(index).addStringValue(processed[n].instance(i).stringValue(m));
            } else if (result.attribute(index).isRelationValued()) {
              values[index] = result.attribute(index).addRelation(processed[n].instance(i).relationalValue(m));
            } else
              values[index] = processed[n].instance(i).value(m);
            index++;
          }
        }
      }
      
      if (!getRemoveUnused()) {
        for (n = 0; n < m_IndicesUnused.length; n++) {
          if (result.attribute(index).isString()) {
            values[index] = result.attribute(index).addStringValue(inst.stringValue(m_IndicesUnused[n]));
          } else if (result.attribute(index).isRelationValued()) {
            values[index] = result.attribute(index).addRelation(inst.relationalValue(m_IndicesUnused[n]));
          } else
            values[index] = inst.value(m_IndicesUnused[n]);
          index++;
        }
      }
      

      if (instances.classIndex() > -1)
        values[(values.length - 1)] = inst.value(instances.classIndex());
      Instance newInst;
      Instance newInst;
      if ((inst instanceof SparseInstance)) {
        newInst = new SparseInstance(instances.instance(i).weight(), values);
      } else
        newInst = new Instance(instances.instance(i).weight(), values);
      result.add(newInst);
    }
    
    return result;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 7506 $");
  }
  




  public static void main(String[] args)
  {
    runFilter(new PartitionedMultiFilter(), args);
  }
}
