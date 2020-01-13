package weka.classifiers.rules;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;
import weka.classifiers.Classifier;
import weka.classifiers.Sourcable;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WekaException;



























































public class OneR
  extends Classifier
  implements TechnicalInformationHandler, Sourcable
{
  static final long serialVersionUID = -3459427003147861443L;
  private OneRRule m_rule;
  
  public OneR() {}
  
  public String globalInfo()
  {
    return "Class for building and using a 1R classifier; in other words, uses the minimum-error attribute for prediction, discretizing numeric attributes. For more information, see:\n\n" + getTechnicalInformation().toString();
  }
  











  public TechnicalInformation getTechnicalInformation()
  {
    TechnicalInformation result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
    result.setValue(TechnicalInformation.Field.AUTHOR, "R.C. Holte");
    result.setValue(TechnicalInformation.Field.YEAR, "1993");
    result.setValue(TechnicalInformation.Field.TITLE, "Very simple classification rules perform well on most commonly used datasets");
    result.setValue(TechnicalInformation.Field.JOURNAL, "Machine Learning");
    result.setValue(TechnicalInformation.Field.VOLUME, "11");
    result.setValue(TechnicalInformation.Field.PAGES, "63-91");
    
    return result;
  }
  



  private class OneRRule
    implements Serializable, RevisionHandler
  {
    static final long serialVersionUID = 2252814630957092281L;
    


    private Attribute m_class;
    

    private int m_numInst;
    

    private Attribute m_attr;
    

    private int m_correct;
    

    private int[] m_classifications;
    

    private int m_missingValueClass = -1;
    



    private double[] m_breakpoints;
    




    public OneRRule(Instances data, Attribute attribute)
      throws Exception
    {
      m_class = data.classAttribute();
      m_numInst = data.numInstances();
      m_attr = attribute;
      m_correct = 0;
      m_classifications = new int[m_attr.numValues()];
    }
    







    public OneRRule(Instances data, Attribute attribute, int nBreaks)
      throws Exception
    {
      m_class = data.classAttribute();
      m_numInst = data.numInstances();
      m_attr = attribute;
      m_correct = 0;
      m_classifications = new int[nBreaks];
      m_breakpoints = new double[nBreaks - 1];
    }
    




    public String toString()
    {
      try
      {
        StringBuffer text = new StringBuffer();
        text.append(m_attr.name() + ":\n");
        for (int v = 0; v < m_classifications.length; v++) {
          text.append("\t");
          if (m_attr.isNominal()) {
            text.append(m_attr.value(v));
          } else if (v < m_breakpoints.length) {
            text.append("< " + m_breakpoints[v]);
          } else if (v > 0) {
            text.append(">= " + m_breakpoints[(v - 1)]);
          } else {
            text.append("not ?");
          }
          text.append("\t-> " + m_class.value(m_classifications[v]) + "\n");
        }
        if (m_missingValueClass != -1) {
          text.append("\t?\t-> " + m_class.value(m_missingValueClass) + "\n");
        }
        text.append("(" + m_correct + "/" + m_numInst + " instances correct)\n");
        return text.toString();
      } catch (Exception e) {}
      return "Can't print OneR classifier!";
    }
    





    public String getRevision()
    {
      return RevisionUtils.extract("$Revision: 9918 $");
    }
  }
  




  private int m_minBucketSize = 6;
  



  private Classifier m_ZeroR;
  




  public double classifyInstance(Instance inst)
    throws Exception
  {
    if (m_ZeroR != null) {
      return m_ZeroR.classifyInstance(inst);
    }
    
    int v = 0;
    if (inst.isMissing(m_rule.m_attr)) {
      if (m_rule.m_missingValueClass != -1) {
        return m_rule.m_missingValueClass;
      }
      return 0.0D;
    }
    
    if (m_rule.m_attr.isNominal()) {
      v = (int)inst.value(m_rule.m_attr);
    } else {
      while ((v < m_rule.m_breakpoints.length) && (inst.value(m_rule.m_attr) >= m_rule.m_breakpoints[v]))
      {
        v++;
      }
    }
    return m_rule.m_classifications[v];
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
    
    return result;
  }
  






  public void buildClassifier(Instances instances)
    throws Exception
  {
    boolean noRule = true;
    

    getCapabilities().testWithFail(instances);
    

    Instances data = new Instances(instances);
    data.deleteWithMissingClass();
    

    if (data.numAttributes() == 1) {
      System.err.println("Cannot build model (only class attribute present in data!), using ZeroR model instead!");
      

      m_ZeroR = new ZeroR();
      m_ZeroR.buildClassifier(data);
      return;
    }
    
    m_ZeroR = null;
    


    Enumeration enu = instances.enumerateAttributes();
    while (enu.hasMoreElements()) {
      try {
        OneRRule r = newRule((Attribute)enu.nextElement(), data);
        

        if ((noRule) || (m_correct > m_rule.m_correct)) {
          m_rule = r;
        }
        noRule = false;
      }
      catch (Exception ex) {}
    }
    
    if (noRule) {
      throw new WekaException("No attributes found to work with!");
    }
  }
  









  public OneRRule newRule(Attribute attr, Instances data)
    throws Exception
  {
    int[] missingValueCounts = new int[data.classAttribute().numValues()];
    OneRRule r;
    OneRRule r;
    if (attr.isNominal()) {
      r = newNominalRule(attr, data, missingValueCounts);
    } else {
      r = newNumericRule(attr, data, missingValueCounts);
    }
    m_missingValueClass = Utils.maxIndex(missingValueCounts);
    if (missingValueCounts[m_missingValueClass] == 0) {
      m_missingValueClass = -1;
    } else {
      OneRRule.access$412(r, missingValueCounts[m_missingValueClass]);
    }
    return r;
  }
  










  public OneRRule newNominalRule(Attribute attr, Instances data, int[] missingValueCounts)
    throws Exception
  {
    int[][] counts = new int[attr.numValues()][data.classAttribute().numValues()];
    


    Enumeration enu = data.enumerateInstances();
    while (enu.hasMoreElements()) {
      Instance i = (Instance)enu.nextElement();
      if (i.isMissing(attr)) {
        missingValueCounts[((int)i.classValue())] += 1;
      } else {
        counts[((int)i.value(attr))][((int)i.classValue())] += 1;
      }
    }
    
    OneRRule r = new OneRRule(data, attr);
    for (int value = 0; value < attr.numValues(); value++) {
      int best = Utils.maxIndex(counts[value]);
      m_classifications[value] = best;
      OneRRule.access$412(r, counts[value][best]);
    }
    return r;
  }
  












  public OneRRule newNumericRule(Attribute attr, Instances data, int[] missingValueCounts)
    throws Exception
  {
    data = new Instances(data);
    
    int lastInstance = data.numInstances();
    

    data.sort(attr);
    while ((lastInstance > 0) && (data.instance(lastInstance - 1).isMissing(attr)))
    {
      lastInstance--;
      missingValueCounts[((int)data.instance(lastInstance).classValue())] += 1;
    }
    
    if (lastInstance == 0) {
      throw new Exception("Only missing values in the training data!");
    }
    

    double lastValue = 0.0D;
    LinkedList<int[]> distributions = new LinkedList();
    LinkedList<Double> values = new LinkedList();
    int[] distribution = null;
    for (int i = 0; i < lastInstance; i++)
    {

      if ((i == 0) || (data.instance(i).value(attr) > lastValue)) {
        if (i != 0) {
          values.add(Double.valueOf((lastValue + data.instance(i).value(attr)) / 2.0D));
        }
        lastValue = data.instance(i).value(attr);
        distribution = new int[data.numClasses()];
        distributions.add(distribution);
      }
      distribution[((int)data.instance(i).classValue())] += 1;
    }
    values.add(Double.valueOf(Double.MAX_VALUE));
    

    ListIterator<int[]> it = distributions.listIterator();
    ListIterator<Double> itVals = values.listIterator();
    int[] oldDist = null;
    while (it.hasNext())
    {

      int[] newDist = (int[])it.next();
      double val = ((Double)itVals.next()).doubleValue();
      

      if ((oldDist != null) && ((Utils.maxIndex(newDist) == Utils.maxIndex(oldDist)) || (oldDist[Utils.maxIndex(oldDist)] < m_minBucketSize)))
      {







        for (int j = 0; j < oldDist.length; j++) {
          newDist[j] += oldDist[j];
        }
        

        it.previous();
        it.previous();
        it.remove();
        it.next();
        

        itVals.previous();
        itVals.previous();
        itVals.remove();
        itVals.next();
      }
      

      oldDist = newDist;
    }
    

    int numCorrect = 0;
    it = distributions.listIterator();
    itVals = values.listIterator();
    oldDist = null;
    while (it.hasNext())
    {

      int[] newDist = (int[])it.next();
      double val = ((Double)itVals.next()).doubleValue();
      

      numCorrect += newDist[Utils.maxIndex(newDist)];
      

      if ((oldDist != null) && (Utils.maxIndex(newDist) == Utils.maxIndex(oldDist)))
      {




        for (int j = 0; j < oldDist.length; j++) {
          newDist[j] += oldDist[j];
        }
        

        it.previous();
        it.previous();
        it.remove();
        it.next();
        

        itVals.previous();
        itVals.previous();
        itVals.remove();
        itVals.next();
      }
      

      oldDist = newDist;
    }
    
    OneRRule r = new OneRRule(data, attr, distributions.size());
    m_correct = numCorrect;
    it = distributions.listIterator();
    itVals = values.listIterator();
    int v = 0;
    while (it.hasNext()) {
      m_classifications[v] = Utils.maxIndex((int[])it.next());
      double splitPoint = ((Double)itVals.next()).doubleValue();
      if (itVals.hasNext()) {
        m_breakpoints[v] = splitPoint;
      }
      v++;
    }
    
    return r;
  }
  





  public Enumeration listOptions()
  {
    String string = "\tThe minimum number of objects in a bucket (default: 6).";
    
    Vector newVector = new Vector(1);
    
    newVector.addElement(new Option(string, "B", 1, "-B <minimum bucket size>"));
    

    return newVector.elements();
  }
  













  public void setOptions(String[] options)
    throws Exception
  {
    String bucketSizeString = Utils.getOption('B', options);
    if (bucketSizeString.length() != 0) {
      m_minBucketSize = Integer.parseInt(bucketSizeString);
    } else {
      m_minBucketSize = 6;
    }
  }
  





  public String[] getOptions()
  {
    String[] options = new String[2];
    int current = 0;
    
    options[(current++)] = "-B";options[(current++)] = ("" + m_minBucketSize);
    
    while (current < options.length) {
      options[(current++)] = "";
    }
    return options;
  }
  

















  public String toSource(String className)
    throws Exception
  {
    StringBuffer result = new StringBuffer();
    
    if (m_ZeroR != null) {
      result.append(((ZeroR)m_ZeroR).toSource(className));
    }
    else {
      result.append("class " + className + " {\n");
      result.append("  public static double classify(Object[] i) {\n");
      result.append("    // chosen attribute: " + m_rule.m_attr.name() + " (" + m_rule.m_attr.index() + ")\n");
      result.append("\n");
      
      result.append("    // missing value?\n");
      result.append("    if (i[" + m_rule.m_attr.index() + "] == null)\n");
      if (m_rule.m_missingValueClass != -1) {
        result.append("      return Double.NaN;\n");
      } else
        result.append("      return 0;\n");
      result.append("\n");
      

      result.append("    // prediction\n");
      result.append("    double v = 0;\n");
      result.append("    double[] classifications = new double[]{" + Utils.arrayToString(m_rule.m_classifications) + "};");
      result.append(" // ");
      for (int i = 0; i < m_rule.m_classifications.length; i++) {
        if (i > 0)
          result.append(", ");
        result.append(m_rule.m_class.value(m_rule.m_classifications[i]));
      }
      result.append("\n");
      if (m_rule.m_attr.isNominal()) {
        for (i = 0; i < m_rule.m_attr.numValues(); i++) {
          result.append("    ");
          if (i > 0)
            result.append("else ");
          result.append("if (((String) i[" + m_rule.m_attr.index() + "]).equals(\"" + m_rule.m_attr.value(i) + "\"))\n");
          result.append("      v = " + i + "; // " + m_rule.m_class.value(m_rule.m_classifications[i]) + "\n");
        }
      }
      
      result.append("    double[] breakpoints = new double[]{" + Utils.arrayToString(m_rule.m_breakpoints) + "};\n");
      result.append("    while (v < breakpoints.length && \n");
      result.append("           ((Double) i[" + m_rule.m_attr.index() + "]) >= breakpoints[(int) v]) {\n");
      result.append("      v++;\n");
      result.append("    }\n");
      
      result.append("    return classifications[(int) v];\n");
      
      result.append("  }\n");
      result.append("}\n");
    }
    
    return result.toString();
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
    
    if (m_rule == null) {
      return "OneR: No model built yet.";
    }
    return m_rule.toString();
  }
  




  public String minBucketSizeTipText()
  {
    return "The minimum bucket size used for discretizing numeric attributes.";
  }
  





  public int getMinBucketSize()
  {
    return m_minBucketSize;
  }
  




  public void setMinBucketSize(int v)
  {
    m_minBucketSize = v;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 9918 $");
  }
  




  public static void main(String[] argv)
  {
    runClassifier(new OneR(), argv);
  }
}
