package weka.filters.unsupervised.attribute;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
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
import weka.filters.StreamableFilter;
import weka.filters.UnsupervisedFilter;


















































public class Reorder
  extends Filter
  implements UnsupervisedFilter, StreamableFilter, OptionHandler
{
  static final long serialVersionUID = -1135571321097202292L;
  protected String m_NewOrderCols = "first-last";
  


  protected int[] m_SelectedAttributes;
  


  protected int[] m_InputStringIndex;
  



  public Reorder() {}
  



  public Enumeration listOptions()
  {
    Vector newVector = new Vector();
    
    newVector.addElement(new Option("\tSpecify list of columns to copy. First and last are valid\n\tindexes. (default first-last)", "R", 1, "-R <index1,index2-index4,...>"));
    



    return newVector.elements();
  }
  













  public void setOptions(String[] options)
    throws Exception
  {
    String orderList = Utils.getOption('R', options);
    if (orderList.length() != 0) {
      setAttributeIndices(orderList);
    }
    
    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }
  




  public String[] getOptions()
  {
    String[] options = new String[2];
    int current = 0;
    
    if (!getAttributeIndices().equals("")) {
      options[(current++)] = "-R";
      options[(current++)] = getAttributeIndices();
    }
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  


  protected int determineIndex(String s, int numAttributes)
    throws Exception
  {
    int result;
    

    int result;
    

    if (s.equals("first")) {
      result = 0; } else { int result;
      if (s.equals("last")) {
        result = numAttributes - 1;
      } else {
        result = Integer.parseInt(s) - 1;
      }
    }
    if ((result < 0) || (result > numAttributes - 1)) {
      throw new IllegalArgumentException("'" + s + "' is not a valid index for the range '1-" + numAttributes + "'!");
    }
    
    return result;
  }
  















  protected int[] determineIndices(int numAttributes)
    throws Exception
  {
    Vector<Integer> list = new Vector();
    

    StringTokenizer tok = new StringTokenizer(m_NewOrderCols, ",");
    while (tok.hasMoreTokens()) {
      String token = tok.nextToken();
      if (token.indexOf("-") > -1) {
        String[] range = token.split("-");
        if (range.length != 2)
          throw new IllegalArgumentException("'" + token + "' is not a valid range!");
        int from = determineIndex(range[0], numAttributes);
        int to = determineIndex(range[1], numAttributes);
        
        if (from <= to) {
          for (int i = from; i <= to; i++) {
            list.add(Integer.valueOf(i));
          }
        } else {
          for (int i = from; i >= to; i--) {
            list.add(Integer.valueOf(i));
          }
        }
      } else {
        list.add(Integer.valueOf(determineIndex(token, numAttributes)));
      }
    }
    

    int[] result = new int[list.size()];
    for (int i = 0; i < list.size(); i++) {
      result[i] = ((Integer)list.get(i)).intValue();
    }
    return result;
  }
  





  public Capabilities getCapabilities()
  {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    

    result.enableAllAttributes();
    result.enable(Capabilities.Capability.MISSING_VALUES);
    

    result.enableAllClasses();
    result.enable(Capabilities.Capability.NO_CLASS);
    result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);
    
    return result;
  }
  







  public boolean setInputFormat(Instances instanceInfo)
    throws Exception
  {
    super.setInputFormat(instanceInfo);
    
    FastVector attributes = new FastVector();
    int outputClass = -1;
    m_SelectedAttributes = determineIndices(instanceInfo.numAttributes());
    for (int i = 0; i < m_SelectedAttributes.length; i++) {
      int current = m_SelectedAttributes[i];
      if (instanceInfo.classIndex() == current) {
        outputClass = attributes.size();
      }
      Attribute keep = (Attribute)instanceInfo.attribute(current).copy();
      attributes.addElement(keep);
    }
    
    initInputLocators(instanceInfo, m_SelectedAttributes);
    
    Instances outputFormat = new Instances(instanceInfo.relationName(), attributes, 0);
    
    outputFormat.setClassIndex(outputClass);
    setOutputFormat(outputFormat);
    
    return true;
  }
  










  public boolean input(Instance instance)
  {
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    
    double[] vals = new double[outputFormatPeek().numAttributes()];
    for (int i = 0; i < m_SelectedAttributes.length; i++) {
      int current = m_SelectedAttributes[i];
      vals[i] = instance.value(current);
    }
    Instance inst = null;
    if ((instance instanceof SparseInstance)) {
      inst = new SparseInstance(instance.weight(), vals);
    } else {
      inst = new Instance(instance.weight(), vals);
    }
    inst.setDataset(getOutputFormat());
    copyValues(inst, false, instance.dataset(), getOutputFormat());
    inst.setDataset(getOutputFormat());
    
    push(inst);
    
    return true;
  }
  





  public String globalInfo()
  {
    return "A filter that generates output with a new order of the attributes. Useful if one wants to move an attribute to the end to use it as class attribute (e.g. with using \"-R 2-last,1\").\nBut it's not only possible to change the order of all the attributes, but also to leave out attributes. E.g. if you have 10 attributes, you can generate the following output order: 1,3,5,7,9,10 or 10,1-5.\nYou can also duplicate attributes, e.g. for further processing later on: e.g. 1,1,1,4,4,4,2,2,2 where the second and the third column of each attribute are processed differently and the first one, i.e. the original one is kept.\nOne can simply inverse the order of the attributes via 'last-first'.\nAfter appyling the filter, the index of the class attribute is the last attribute.";
  }
  

















  public String getAttributeIndices()
  {
    return m_NewOrderCols;
  }
  





  public String attributeIndicesTipText()
  {
    return "Specify range of attributes to act on. This is a comma separated list of attribute indices, with \"first\" and \"last\" valid values. Specify an inclusive range with \"-\". E.g: \"first-3,5,6-10,last\".";
  }
  














  public void setAttributeIndices(String rangeList)
    throws Exception
  {
    if (rangeList.replaceAll("[afilrst0-9\\-,]*", "").length() != 0) {
      throw new IllegalArgumentException("Not a valid range string!");
    }
    m_NewOrderCols = rangeList;
  }
  









  public void setAttributeIndicesArray(int[] attributes)
    throws Exception
  {
    setAttributeIndices(Range.indicesToRangeList(attributes));
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 6249 $");
  }
  




  public static void main(String[] argv)
  {
    runFilter(new Reorder(), argv);
  }
}
